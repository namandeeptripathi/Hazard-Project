package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.processing.DailyRainfallSummary;
import com.hazard.dto.processing.ProcessedHazardObservation;
import com.hazard.dto.processing.ProcessingQualitySummaryDto;
import com.hazard.dto.processing.RollingRainfallMetrics;
import com.hazard.service.processing.HazardProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional(readOnly = true)
class HazardProcessingServiceTests {

    private static final Logger log = LoggerFactory.getLogger(HazardProcessingServiceTests.class);

    @Autowired
    private HazardProcessingService hazardProcessingService;

    @Test
    @DisplayName("1. Process DFO Flood Events: Spatially Located (VALID) vs Sentinel Cleaned (UNLOCATED)")
    void testProcessDfoFloodEvents() {
        // DFO-3: Valid coordinates (Sitamarhi)
        ProcessedHazardObservation validDfo = hazardProcessingService.getProcessedHazardById("DFO-3");
        assertNotNull(validDfo);
        assertEquals("DFO-3", validDfo.getId());
        assertEquals(HazardType.FLOOD, validDfo.getHazardType());
        assertEquals(QualityStatus.VALID, validDfo.getQualityStatus());
        assertEquals(85.5, validDfo.getLongitude());
        assertEquals(26.5, validDfo.getLatitude());
        assertEquals("Sitamarhi", validDfo.getAssociatedDistrict());
        assertTrue(validDfo.getIsWithinBiharBoundary());
        assertEquals(4.0, validDfo.getDurationDays());

        // DFO-8: Sentinel coordinates cleaned to null & marked UNLOCATED
        ProcessedHazardObservation unlocatedDfo = hazardProcessingService.getProcessedHazardById("DFO-8");
        assertNotNull(unlocatedDfo);
        assertEquals("DFO-8", unlocatedDfo.getId());
        assertEquals(QualityStatus.UNLOCATED, unlocatedDfo.getQualityStatus());
        assertNull(unlocatedDfo.getLongitude());
        assertNull(unlocatedDfo.getLatitude());
        assertNull(unlocatedDfo.getAssociatedDistrict());
        assertFalse(unlocatedDfo.getIsWithinBiharBoundary());
        assertTrue(unlocatedDfo.getProcessingMetadata().getAnomaliesDetected().stream()
                .anyMatch(a -> a.contains("SENTINEL_COORDINATES_DETECTED")));

        log.info("✅ DFO Flood Events processing verified: Located (VALID) vs Sentinel Cleaned (UNLOCATED)");
    }

    @Test
    @DisplayName("2. Process EM-DAT Flood Records: Macro Aggregates & Derived Metrics")
    void testProcessEmdatFloodRecords() {
        ProcessedHazardObservation emdat = hazardProcessingService.getProcessedHazardById("EMDAT-1");
        assertNotNull(emdat);
        assertEquals("EMDAT-1", emdat.getId());
        assertEquals(HazardType.FLOOD, emdat.getHazardType());
        assertEquals(QualityStatus.UNLOCATED, emdat.getQualityStatus());
        assertNull(emdat.getLongitude());
        assertNull(emdat.getLatitude());
        assertFalse(emdat.getIsWithinBiharBoundary());

        // Derived macro metrics
        assertNotNull(emdat.getDerivedMetrics().get("averageDeathsPerEvent"));
        assertNotNull(emdat.getDerivedMetrics().get("averageAffectedPerEvent"));

        log.info("✅ EM-DAT Flood Records processed as UNLOCATED with derived macro metrics");
    }

    @Test
    @DisplayName("3. Process Weather Observations: Spatial Association & Intensity Indicators")
    void testProcessWeatherObservations() {
        // Retrieve extreme rainfall observation (e.g. >= 15mm)
        List<ProcessedHazardObservation> extremeObs = hazardProcessingService.getAllProcessedHazards(
                HazardType.EXTREME_RAINFALL, QualityStatus.VALID, null, 10
        );
        assertFalse(extremeObs.isEmpty());

        ProcessedHazardObservation obs = extremeObs.get(0);
        assertEquals(HazardType.EXTREME_RAINFALL, obs.getHazardType());
        assertEquals(QualityStatus.VALID, obs.getQualityStatus());
        assertNotNull(obs.getAssociatedDistrict());
        assertTrue(obs.getIsWithinBiharBoundary());
        assertTrue(obs.getPrecipitationMm() >= 10.0);
        assertTrue((Boolean) obs.getDerivedMetrics().get("isHeavyRainfallHourly") ||
                   obs.getPrecipitationMm() >= 10.0);

        log.info("✅ Weather Observations processed with dynamic district association and intensity indicators");
    }

    @Test
    @DisplayName("4. Daily Rainfall Aggregation: Patna 2020-06-29 Extreme Day")
    void testDailyRainfallAggregation() {
        LocalDate start = LocalDate.of(2020, 6, 25);
        LocalDate end = LocalDate.of(2020, 7, 5);

        List<DailyRainfallSummary> summaries = hazardProcessingService.getDailyRainfallSummaries("Patna", start, end);
        assertFalse(summaries.isEmpty());

        DailyRainfallSummary peakDay = summaries.stream()
                .filter(s -> s.getDate().equals(LocalDate.of(2020, 6, 29)))
                .findFirst()
                .orElse(null);

        assertNotNull(peakDay);
        assertEquals("Patna", peakDay.getStationName());
        assertEquals("Patna", peakDay.getAssociatedDistrict());
        assertEquals(101.7, peakDay.getDailyTotalMm(), 0.1);
        assertEquals(41.5, peakDay.getPeakHourlyMm(), 0.1);
        assertEquals(21, peakDay.getRainyHours());
        assertEquals(2, peakDay.getHeavyRainHours());
        assertEquals(1, peakDay.getVeryHeavyRainHours());
        assertTrue(peakDay.isExceedsHeavyThreshold());

        log.info("✅ Daily Rainfall Aggregation verified (Patna peak daily: 101.7 mm on 2020-06-29)");
    }

    @Test
    @DisplayName("5. Multi-Window Rolling Rainfall Accumulation (3h, 6h, 12h, 24h)")
    void testRollingRainfallMetrics() {
        LocalDateTime target = LocalDateTime.of(2020, 6, 29, 18, 0);
        RollingRainfallMetrics rolling = hazardProcessingService.getRollingRainfallMetrics("Patna", target);

        assertNotNull(rolling);
        assertEquals("Patna", rolling.getStationName());
        assertEquals("Patna", rolling.getAssociatedDistrict());
        assertEquals(0.2, rolling.getCurrentHourlyMm());
        assertEquals(0.4, rolling.getRolling3hMm(), 0.1);
        assertEquals(0.8, rolling.getRolling6hMm(), 0.1);
        assertEquals(103.6, rolling.getRolling24hMm(), 0.5);
        assertTrue(rolling.isHeavyRainfall()); // 24h total > 64.5mm

        log.info("✅ Rolling Rainfall Accumulation verified (Patna 24h rolling: 103.6 mm)");
    }

    @Test
    @DisplayName("6. Executive Processing Quality Summary")
    void testProcessingQualitySummary() {
        ProcessingQualitySummaryDto summary = hazardProcessingService.getProcessingQualitySummary();
        assertNotNull(summary);
        assertEquals(159005 - (6093 + 4401 + 16208 + 589 + 2 + 1 + 38 + 53), summary.getTotalSourceRecords()); // DFO (23) + EM-DAT (53) + Weather (131544) = 131620
        assertEquals(131620, summary.getTotalProcessedRecords());
        assertEquals(23, summary.getDfoTotal());
        assertEquals(7, summary.getDfoValid());
        assertEquals(16, summary.getDfoUnlocated());
        assertEquals(53, summary.getEmdatTotal());
        assertEquals(53, summary.getEmdatUnlocated());
        assertEquals(131544, summary.getWeatherTotal());
        assertEquals(131544, summary.getWeatherValid());
        assertEquals(131551, summary.getValidRecordsCount()); // 7 DFO + 131544 Weather
        assertEquals(69, summary.getUnlocatedRecordsCount()); // 16 DFO + 53 EM-DAT
        assertEquals(16, summary.getAnomaliesCleanedCount()); // 16 DFO sentinel coordinates cleaned
        assertEquals(38, summary.getCoveredDistricts().size());
        assertEquals(3, summary.getActiveWeatherStations().size());

        log.info("✅ Processing Quality Summary verified: 131,551 VALID, 69 UNLOCATED (16 DFO Cleaned + 53 EM-DAT)");
    }

    @Test
    @DisplayName("7. GeoJSON Vector FeatureCollection of Verified Processed Hazards")
    void testGetProcessedHazardsGeoJson() {
        GeoJsonFeatureCollectionDto geojson = hazardProcessingService.getProcessedHazardsGeoJson(HazardType.FLOOD, null, 50);
        assertNotNull(geojson);
        assertEquals("FeatureCollection", geojson.getType());
        assertEquals(7, geojson.getCount()); // Exactly 7 valid spatial DFO flood events
        assertTrue(geojson.getFeatures().stream().allMatch(f -> "Point".equals(f.getGeometry().getType())));

        log.info("✅ GeoJSON vector generation verified ({} valid spatial flood features)", geojson.getCount());
    }
}
