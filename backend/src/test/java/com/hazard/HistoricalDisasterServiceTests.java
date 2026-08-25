package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.historical.HistoricalDataQualityStatus;
import com.hazard.domain.historical.HistoricalTimeWindow;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.historical.*;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.service.historical.HistoricalDisasterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for HistoricalDisasterService with real PostGIS database.
 */
@SpringBootTest
@Transactional(readOnly = true)
class HistoricalDisasterServiceTests {

    private static final Logger log = LoggerFactory.getLogger(HistoricalDisasterServiceTests.class);

    @Autowired
    private HistoricalDisasterService historicalDisasterService;

    @Test
    @DisplayName("4.6.7: District Historical Summary — Sitamarhi (Flood Basin)")
    void testDistrictHistoricalSummarySitamarhi() {
        DistrictHistoricalSummaryDto summary = historicalDisasterService.getDistrictHistoricalSummary(
                "Sitamarhi", HistoricalTimeWindow.ALL_HISTORY, null, null, null);

        assertNotNull(summary);
        assertEquals("Sitamarhi", summary.getDistrictName());
        assertTrue(summary.getTotalHistoricalEvents() >= 1, "Sitamarhi must have recorded historical flood events in DFO");
        assertTrue(summary.getEventsPerYear() > 0.0);
        assertNotNull(summary.getSeverityStatistics());
        assertNotNull(summary.getRecurrenceStatistics());
        assertEquals("EMPIRICAL_ONLY", summary.getRecurrenceStatistics().getStatus());
        assertNotNull(summary.getTemporalPatterns());
        assertNotNull(summary.getHotspotTier());
        assertNotNull(summary.getSummaryExplanation());

        log.info("✅ Sitamarhi Historical Summary: Events={}, Events/Year={}, AvgSeverity={}, HotspotTier={}",
                summary.getTotalHistoricalEvents(), summary.getEventsPerYear(),
                summary.getSeverityStatistics().getAverageSeverity(), summary.getHotspotTier());
    }

    @Test
    @DisplayName("4.6.8: District Historical Timeline Generation")
    void testGetDistrictTimeline() {
        List<HistoricalEventDto> timeline = historicalDisasterService.getDistrictTimeline("Sitamarhi", HazardType.FLOOD);

        assertNotNull(timeline);
        assertFalse(timeline.isEmpty());
        for (HistoricalEventDto e : timeline) {
            assertNotNull(e.getEventId());
            assertEquals(HazardType.FLOOD, e.getHazardType());
            assertNotNull(e.getEventDate());
            assertNotNull(e.getSource());
        }

        log.info("✅ Sitamarhi Timeline: {} events, First={}", timeline.size(), timeline.get(0).getEventId());
    }

    @Test
    @DisplayName("4.6.9: All 38 Districts Historical Summaries")
    void testGetAllDistrictsHistoricalSummaries() {
        List<DistrictHistoricalSummaryDto> summaries = historicalDisasterService.getAllDistrictsHistoricalSummaries(HistoricalTimeWindow.ALL_HISTORY);

        assertNotNull(summaries);
        assertEquals(38, summaries.size(), "Should evaluate all 38 Bihar districts");
    }

    @Test
    @DisplayName("4.6.10: Historical Hotspots Ranking")
    void testGetHistoricalHotspots() {
        List<HistoricalHotspotDto> hotspots = historicalDisasterService.getHistoricalHotspots(HistoricalTimeWindow.ALL_HISTORY, 10);

        assertNotNull(hotspots);
        assertFalse(hotspots.isEmpty());
        assertTrue(hotspots.size() <= 10);

        // Verify sorted descending by hotspot index
        for (int i = 0; i < hotspots.size() - 1; i++) {
            assertTrue(hotspots.get(i).getHotspotIndex() >= hotspots.get(i + 1).getHotspotIndex(),
                    "Hotspots must be ranked in descending order");
        }

        log.info("✅ Top Historical Hotspot: District={}, HotspotScore={}/100, Tier={}",
                hotspots.get(0).getDistrictName(), hotspots.get(0).getHotspotScore100(), hotspots.get(0).getHotspotTier());
    }

    @Test
    @DisplayName("4.6.11: Historical GeoJSON Point Feature Collection")
    void testGenerateHistoricalGeoJson() {
        GeoJsonFeatureCollectionDto geojson = historicalDisasterService.generateHistoricalGeoJson(HistoricalTimeWindow.ALL_HISTORY);

        assertNotNull(geojson);
        assertEquals("FeatureCollection", geojson.getType());
        assertFalse(geojson.getFeatures().isEmpty());

        var first = geojson.getFeatures().get(0);
        assertEquals("Point", first.getGeometry().getType());
        assertNotNull(first.getProperties().get("eventId"));
        assertNotNull(first.getProperties().get("hazardType"));
        assertNotNull(first.getProperties().get("colorHex"));

        log.info("✅ Historical GeoJSON: {} point features generated", geojson.getFeatures().size());
    }

    @Test
    @DisplayName("4.6.12: Historical Configuration Catalog")
    void testGetHistoricalConfig() {
        HistoricalConfigDto cfg = historicalDisasterService.getHistoricalConfig();

        assertNotNull(cfg);
        assertFalse(cfg.getSupportedHazardTypes().isEmpty());
        assertEquals("v1.0", cfg.getCalculationVersion());
    }

    @Test
    @DisplayName("4.6.13: Error Handling — Unknown District")
    void testErrorHandling() {
        assertThrows(HazardNotFoundException.class, () ->
                historicalDisasterService.getDistrictHistoricalSummary("UnknownDistrict404", null, null, null, null)
        );
    }
}
