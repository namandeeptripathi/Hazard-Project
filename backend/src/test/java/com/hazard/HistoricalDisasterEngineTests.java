package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.historical.HistoricalHotspotTier;
import com.hazard.dto.historical.*;
import com.hazard.service.historical.HistoricalDisasterConfig;
import com.hazard.service.historical.HistoricalDisasterEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HistoricalDisasterEngine calculations (frequency, recurrence, severity stats, temporal patterns, hotspots).
 */
class HistoricalDisasterEngineTests {

    private HistoricalDisasterConfig config;
    private HistoricalDisasterEngine engine;

    @BeforeEach
    void setUp() {
        config = new HistoricalDisasterConfig();
        engine = new HistoricalDisasterEngine(config);
    }

    private HistoricalEventDto createEvent(String id, HazardType type, LocalDate date, double severity, String cause) {
        HistoricalEventDto dto = new HistoricalEventDto();
        dto.setEventId(id);
        dto.setHazardType(type);
        dto.setEventDate(date);
        dto.setStartDate(date);
        dto.setSeverity(severity);
        dto.setMainCause(cause);
        dto.setSource("DFO");
        return dto;
    }

    @Test
    @DisplayName("4.6.1: Event Filtering by Date Range & Hazard Type")
    void testFilterEvents() {
        List<HistoricalEventDto> events = List.of(
                createEvent("E1", HazardType.FLOOD, LocalDate.of(2008, 8, 18), 1.5, "Kosi breach"),
                createEvent("E2", HazardType.FLOOD, LocalDate.of(2017, 7, 25), 1.8, "Monsoon flood"),
                createEvent("E3", HazardType.EXTREME_RAINFALL, LocalDate.of(2021, 6, 15), 35.0, "Heavy rain")
        );

        // Filter by FLOOD only
        List<HistoricalEventDto> floodOnly = engine.filterEvents(events, null, null, HazardType.FLOOD, null);
        assertEquals(2, floodOnly.size());

        // Filter by Date range (2015 to 2022)
        List<HistoricalEventDto> dateFiltered = engine.filterEvents(events, LocalDate.of(2015, 1, 1), LocalDate.of(2022, 1, 1), null, null);
        assertEquals(2, dateFiltered.size());
    }

    @Test
    @DisplayName("4.6.2: Severity Statistics Calculation")
    void testSeverityStatistics() {
        List<HistoricalEventDto> events = List.of(
                createEvent("E1", HazardType.FLOOD, LocalDate.of(2008, 8, 18), 1.0, "Cause 1"),
                createEvent("E2", HazardType.FLOOD, LocalDate.of(2017, 7, 25), 1.5, "Cause 2"),
                createEvent("E3", HazardType.FLOOD, LocalDate.of(2020, 7, 10), 2.0, "Cause 3")
        );

        SeverityStatisticsDto stats = engine.calculateSeverityStatistics(events);

        assertNotNull(stats);
        assertEquals(1.0, stats.getMinimumSeverity(), 0.0001);
        assertEquals(2.0, stats.getMaximumSeverity(), 0.0001);
        assertEquals(1.5, stats.getAverageSeverity(), 0.0001);
        assertEquals(1.5, stats.getMedianSeverity(), 0.0001);
        assertEquals(2, stats.getHighSeverityEventCount(), "Events with severity >= 1.5");
    }

    @Test
    @DisplayName("4.6.3: Empirical Recurrence Interval Calculation")
    void testRecurrenceStatistics() {
        List<HistoricalEventDto> events = List.of(
                createEvent("E1", HazardType.FLOOD, LocalDate.of(2010, 1, 1), 1.5, "Flood 1"),
                createEvent("E2", HazardType.FLOOD, LocalDate.of(2015, 1, 1), 1.5, "Flood 2"),
                createEvent("E3", HazardType.FLOOD, LocalDate.of(2020, 1, 1), 1.5, "Flood 3")
        );

        RecurrenceStatisticsDto rec = engine.calculateRecurrenceStatistics(events, 15.0);

        assertNotNull(rec);
        assertEquals(2, rec.getTotalIntervalsEvaluated());
        assertEquals(5.0, rec.getAverageHistoricalGapYears(), 0.1);
        assertEquals("EMPIRICAL_ONLY", rec.getStatus());
    }

    @Test
    @DisplayName("4.6.4: Seasonal & Monthly Temporal Patterns")
    void testTemporalPatterns() {
        List<HistoricalEventDto> events = List.of(
                createEvent("E1", HazardType.FLOOD, LocalDate.of(2008, 7, 15), 1.5, "Monsoon 1"),
                createEvent("E2", HazardType.FLOOD, LocalDate.of(2017, 7, 25), 1.8, "Monsoon 2"),
                createEvent("E3", HazardType.FLOOD, LocalDate.of(2020, 8, 10), 1.2, "Monsoon 3")
        );

        TemporalPatternDto temp = engine.calculateTemporalPatterns(events);

        assertNotNull(temp);
        assertEquals("JULY", temp.getPeakDisasterMonth());
        assertEquals("MONSOON (Jun-Sep)", temp.getPrimaryDisasterSeason());
        assertEquals(3, temp.getEventsBySeason().get("MONSOON (Jun-Sep)"));
    }

    @Test
    @DisplayName("4.6.5: Historical Hotspot Index & Classification")
    void testCalculateHotspotIndex() {
        // High frequency (1.5 events/yr), High severity (1.8), Short gap (1.5 yrs) -> High Hotspot
        double highIndex = engine.calculateHotspotIndex(15, 1.5, 1.8, 8, 1.5);
        assertTrue(highIndex >= 0.70, "Chronic high recurrence should yield severe/high hotspot index");
        assertEquals(HistoricalHotspotTier.SEVERE_HOTSPOT, HistoricalHotspotTier.fromIndex(highIndex));

        // Zero events -> 0.0000 (Low)
        double zeroIndex = engine.calculateHotspotIndex(0, 0.0, 0.0, 0, null);
        assertEquals(0.0000, zeroIndex);
        assertEquals(HistoricalHotspotTier.LOW, HistoricalHotspotTier.fromIndex(zeroIndex));
    }

    @Test
    @DisplayName("4.6.6: Boundary & Empty History Handling")
    void testBoundaryAndEmptyHandling() {
        SeverityStatisticsDto emptyStats = engine.calculateSeverityStatistics(Collections.emptyList());
        assertEquals(0.0, emptyStats.getAverageSeverity());

        RecurrenceStatisticsDto emptyRec = engine.calculateRecurrenceStatistics(Collections.emptyList(), 25.0);
        assertNull(emptyRec.getAverageHistoricalGapYears());

        TemporalPatternDto emptyTemp = engine.calculateTemporalPatterns(Collections.emptyList());
        assertEquals("INSUFFICIENT_DATA", emptyTemp.getDescriptiveTrend());
    }
}
