package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.normalization.NormalizationSummaryDto;
import com.hazard.dto.normalization.NormalizedDailyRainfall;
import com.hazard.dto.normalization.NormalizedHazardObservation;
import com.hazard.dto.normalization.NormalizedRollingRainfall;
import com.hazard.service.normalization.HazardNormalizationService;
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
class HazardNormalizationServiceTests {

    private static final Logger log = LoggerFactory.getLogger(HazardNormalizationServiceTests.class);

    @Autowired
    private HazardNormalizationService hazardNormalizationService;

    @Test
    @DisplayName("1. Normalized DFO Flood Event: Normalized Duration, Area, Density & Indices")
    void testNormalizeDfoFloodEvent() {
        NormalizedHazardObservation dfo = hazardNormalizationService.getNormalizedHazardById("DFO-3");

        assertNotNull(dfo);
        assertEquals("DFO-3", dfo.getId());
        assertEquals(HazardType.FLOOD, dfo.getHazardType());
        assertEquals(QualityStatus.VALID, dfo.getQualityStatus());
        assertEquals("Sitamarhi", dfo.getAssociatedDistrict());

        // Check normalized metrics
        assertNotNull(dfo.getNormalizedMetrics().get("FLOOD_DURATION_DAYS"));
        assertEquals(4.0, dfo.getNormalizedMetrics().get("FLOOD_DURATION_DAYS").getRawValue());
        // Duration 4 days on [1, 90] -> (4 - 1) / (90 - 1) = 3 / 89 = 0.0337
        assertEquals(0.0337, dfo.getNormalizedMetrics().get("FLOOD_DURATION_DAYS").getNormalizedValue(), 0.001);

        assertNotNull(dfo.getNormalizedMetrics().get("FLOOD_AFFECTED_AREA_SQKM"));
        assertTrue(dfo.getNormalizedMetrics().get("FLOOD_AFFECTED_AREA_SQKM").getNormalizedValue() >= 0.0);
        assertTrue(dfo.getNormalizedMetrics().get("FLOOD_AFFECTED_AREA_SQKM").getNormalizedValue() <= 1.0);

        assertNotNull(dfo.getNormalizedMetrics().get("FLOOD_SEVERITY_INDEX"));
        assertEquals(1.5, dfo.getNormalizedMetrics().get("FLOOD_SEVERITY_INDEX").getRawValue());
        // Severity 1.5 on [1.0, 2.0] -> 0.5000
        assertEquals(0.5000, dfo.getNormalizedMetrics().get("FLOOD_SEVERITY_INDEX").getNormalizedValue(), 0.0001);

        log.info("✅ DFO Flood Event normalized metrics verified (Duration norm: {}, Severity norm: {})",
                dfo.getNormalizedMetrics().get("FLOOD_DURATION_DAYS").getNormalizedValue(),
                dfo.getNormalizedMetrics().get("FLOOD_SEVERITY_INDEX").getNormalizedValue());
    }

    @Test
    @DisplayName("2. Normalized Daily Rainfall: Patna 2020-06-29 Peak Event (101.7 mm -> 0.6780)")
    void testNormalizeDailyRainfall() {
        LocalDate start = LocalDate.of(2020, 6, 25);
        LocalDate end = LocalDate.of(2020, 7, 5);

        List<NormalizedDailyRainfall> dailyList = hazardNormalizationService.getNormalizedDailyRainfall("Patna", start, end);
        assertFalse(dailyList.isEmpty());

        NormalizedDailyRainfall peakDay = dailyList.stream()
                .filter(d -> d.getDate().equals(LocalDate.of(2020, 6, 29)))
                .findFirst()
                .orElse(null);

        assertNotNull(peakDay);
        assertEquals("Patna", peakDay.getStationName());
        assertEquals("Patna", peakDay.getAssociatedDistrict());
        assertEquals(101.7, peakDay.getRawDailyTotalMm(), 0.1);
        assertEquals(41.5, peakDay.getRawPeakHourlyMm(), 0.1);

        // Daily total 101.7mm on [0.0, 150.0] -> 101.7 / 150.0 = 0.6780
        assertEquals(0.6780, peakDay.getNormalizedDailyTotal().getNormalizedValue(), 0.001);
        // Peak hourly 41.5mm on [0.0, 50.0] -> 41.5 / 50.0 = 0.8300
        assertEquals(0.8300, peakDay.getNormalizedPeakHourly().getNormalizedValue(), 0.001);

        log.info("✅ Normalized Daily Rainfall verified (Patna peak: raw=101.7mm -> norm=0.6780, peak hourly raw=41.5mm -> norm=0.8300)");
    }

    @Test
    @DisplayName("3. Normalized Multi-Window Rolling Rainfall (3h, 6h, 12h, 24h)")
    void testNormalizeRollingRainfall() {
        LocalDateTime target = LocalDateTime.of(2020, 6, 29, 18, 0);
        NormalizedRollingRainfall rolling = hazardNormalizationService.getNormalizedRollingRainfall("Patna", target);

        assertNotNull(rolling);
        assertEquals("Patna", rolling.getStationName());
        assertEquals("Patna", rolling.getAssociatedDistrict());

        // Check normalized rolling components
        assertNotNull(rolling.getCurrentHourly());
        assertNotNull(rolling.getRolling3h());
        assertNotNull(rolling.getRolling6h());
        assertNotNull(rolling.getRolling12h());
        assertNotNull(rolling.getRolling24h());

        // 24h rolling 103.6mm on [0.0, 150.0] -> 103.6 / 150.0 = 0.6907
        assertEquals(0.6907, rolling.getRolling24h().getNormalizedValue(), 0.005);
        assertTrue(rolling.getRolling24h().getNormalizedValue() <= 1.0);
        assertTrue(rolling.getRolling24h().getNormalizedValue() >= 0.0);

        log.info("✅ Normalized Rolling Rainfall verified (24h rolling: raw=103.6mm -> norm=0.6907)");
    }

    @Test
    @DisplayName("4. Filter Normalized Hazards by Metric Name (DAILY_RAINFALL_MM vs FLOOD_DURATION_DAYS)")
    void testFilterNormalizedHazardsByMetric() {
        List<NormalizedHazardObservation> durationHazards = hazardNormalizationService.getAllNormalizedHazards(
                null, null, null, "FLOOD_DURATION_DAYS", 20
        );
        assertFalse(durationHazards.isEmpty());
        assertTrue(durationHazards.stream().allMatch(h -> h.getNormalizedMetrics().containsKey("FLOOD_DURATION_DAYS")));

        List<NormalizedHazardObservation> precipHazards = hazardNormalizationService.getAllNormalizedHazards(
                null, null, null, "HOURLY_PRECIPITATION_MM", 20
        );
        assertFalse(precipHazards.isEmpty());
        assertTrue(precipHazards.stream().allMatch(h -> h.getNormalizedMetrics().containsKey("HOURLY_PRECIPITATION_MM")));

        log.info("✅ Filtering by metric name verified for FLOOD_DURATION_DAYS and HOURLY_PRECIPITATION_MM");
    }

    @Test
    @DisplayName("5. Executive Normalization Catalog Summary Report")
    void testGetNormalizationSummary() {
        NormalizationSummaryDto summary = hazardNormalizationService.getNormalizationSummary();

        assertNotNull(summary);
        assertEquals("EPSG:4326 (WGS 84)", summary.getCanonicalCrs());
        assertTrue(summary.getTotalConfiguredMetrics() >= 12);
        assertTrue(summary.getConfiguredMetrics().stream().anyMatch(c -> "DAILY_RAINFALL_MM".equals(c.getMetricName())));
        assertTrue(summary.getConfiguredMetrics().stream().anyMatch(c -> "FLOOD_DURATION_DAYS".equals(c.getMetricName())));
        assertEquals(38, summary.getSupportedDistricts().size());
        assertEquals(3, summary.getActiveStations().size());

        log.info("✅ Normalization Catalog Summary verified ({} configured metrics)", summary.getTotalConfiguredMetrics());
    }

    @Test
    @DisplayName("6. GeoJSON Vector Output of Normalized Hazards")
    void testGetNormalizedHazardsGeoJson() {
        GeoJsonFeatureCollectionDto geojson = hazardNormalizationService.getNormalizedHazardsGeoJson(HazardType.FLOOD, null, 50);

        assertNotNull(geojson);
        assertEquals("FeatureCollection", geojson.getType());
        assertEquals(7, geojson.getCount());
        assertTrue(geojson.getFeatures().stream().allMatch(f -> f.getProperties().containsKey("normalizedMetrics")));

        log.info("✅ Normalized GeoJSON Vector Layer verified ({} features with normalized metrics in properties)", geojson.getCount());
    }
}
