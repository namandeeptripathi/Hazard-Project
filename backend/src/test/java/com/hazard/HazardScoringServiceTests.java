package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.scoring.DailyRainfallScoreDto;
import com.hazard.dto.scoring.HazardScoreDto;
import com.hazard.dto.scoring.HazardScoringSummaryDto;
import com.hazard.dto.scoring.RollingRainfallScoreDto;
import com.hazard.service.scoring.HazardScoringService;
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
class HazardScoringServiceTests {

    private static final Logger log = LoggerFactory.getLogger(HazardScoringServiceTests.class);

    @Autowired
    private HazardScoringService hazardScoringService;

    @Test
    @DisplayName("1. Score DFO Flood Event: Flood Hazard Score & Severity Tier Classification")
    void testScoreDfoFloodEvent() {
        HazardScoreDto floodScore = hazardScoringService.getHazardScoreById("DFO-3");

        assertNotNull(floodScore);
        assertEquals("DFO-3", floodScore.getId());
        assertEquals(HazardType.FLOOD, floodScore.getHazardType());
        assertEquals(QualityStatus.VALID, floodScore.getQualityStatus());
        assertEquals("Sitamarhi", floodScore.getAssociatedDistrict());
        assertTrue(floodScore.getIsWithinBiharBoundary());

        // Score must be in [0.0000, 1.0000]
        assertNotNull(floodScore.getHazardScore());
        assertTrue(floodScore.getHazardScore() >= 0.0);
        assertTrue(floodScore.getHazardScore() <= 1.0);
        assertNotNull(floodScore.getSeverityTier());
        assertTrue(floodScore.getCompletenessRatio() > 0.5);
        assertFalse(floodScore.getMetricContributions().isEmpty());

        log.info("✅ DFO Flood Score verified: score={}, tier={}, completeness={}%, contributions={}",
                floodScore.getHazardScore(), floodScore.getSeverityTier(),
                floodScore.getCompletenessRatio() * 100, floodScore.getMetricContributions().size());
    }

    @Test
    @DisplayName("2. Score Daily Meteorological Rainfall: Patna Peak Storm (2020-06-29)")
    void testScoreDailyRainfall() {
        LocalDate start = LocalDate.of(2020, 6, 25);
        LocalDate end = LocalDate.of(2020, 7, 5);

        List<DailyRainfallScoreDto> scoredList = hazardScoringService.getScoredDailyRainfall("Patna", start, end);
        assertFalse(scoredList.isEmpty());

        DailyRainfallScoreDto peakDay = scoredList.stream()
                .filter(d -> d.getDate().equals(LocalDate.of(2020, 6, 29)))
                .findFirst()
                .orElse(null);

        assertNotNull(peakDay);
        assertEquals("Patna", peakDay.getStationName());
        assertEquals("Patna", peakDay.getAssociatedDistrict());
        assertEquals(101.7, peakDay.getRawDailyTotalMm(), 0.1);
        assertEquals(41.5, peakDay.getRawPeakHourlyMm(), 0.1);

        // Daily total 0.6780 * 0.60 + Peak hourly 0.8300 * 0.40 = 0.4068 + 0.3320 = 0.7388 -> HIGH
        assertNotNull(peakDay.getRainfallHazardScore());
        assertEquals(0.7388, peakDay.getRainfallHazardScore(), 0.01);
        assertEquals(SeverityTier.HIGH, peakDay.getSeverityTier());
        assertEquals(2, peakDay.getMetricContributions().size());

        log.info("✅ Daily Rainfall Score verified for Patna peak day: raw=101.7mm -> score={}, tier={}",
                peakDay.getRainfallHazardScore(), peakDay.getSeverityTier());
    }

    @Test
    @DisplayName("3. Score Rolling Rainfall Accumulation: Multi-Window Aggregation")
    void testScoreRollingRainfall() {
        LocalDateTime target = LocalDateTime.of(2020, 6, 29, 18, 0);
        RollingRainfallScoreDto rollingScore = hazardScoringService.getScoredRollingRainfall("Patna", target);

        assertNotNull(rollingScore);
        assertEquals("Patna", rollingScore.getStationName());
        assertEquals("Patna", rollingScore.getAssociatedDistrict());
        assertNotNull(rollingScore.getRollingRainfallScore());
        assertTrue(rollingScore.getRollingRainfallScore() >= 0.0);
        assertTrue(rollingScore.getRollingRainfallScore() <= 1.0);
        assertNotNull(rollingScore.getSeverityTier());

        log.info("✅ Rolling Rainfall Score verified: score={}, tier={}",
                rollingScore.getRollingRainfallScore(), rollingScore.getSeverityTier());
    }

    @Test
    @DisplayName("4. Filter Hazard Scores by Hazard Type (FLOOD vs EXTREME_RAINFALL)")
    void testFilterHazardScoresByType() {
        List<HazardScoreDto> floodScores = hazardScoringService.getHazardScoresByType(HazardType.FLOOD, null, 20);
        assertFalse(floodScores.isEmpty());
        assertTrue(floodScores.stream().allMatch(s -> s.getHazardType() == HazardType.FLOOD));

        List<HazardScoreDto> rainScores = hazardScoringService.getHazardScoresByType(HazardType.EXTREME_RAINFALL, null, 20);
        assertFalse(rainScores.isEmpty());
        assertTrue(rainScores.stream().allMatch(s -> s.getHazardType() == HazardType.EXTREME_RAINFALL));

        log.info("✅ Type filtering verified: {} flood scores and {} extreme rainfall scores",
                floodScores.size(), rainScores.size());
    }

    @Test
    @DisplayName("5. Executive Hazard Scoring Summary Report")
    void testGetHazardScoringSummary() {
        HazardScoringSummaryDto summary = hazardScoringService.getHazardScoringSummary();

        assertNotNull(summary);
        assertEquals("EPSG:4326 (WGS 84)", summary.getCanonicalCrs());
        assertTrue(summary.getTotalScoredObservations() > 0);
        assertNotNull(summary.getSeverityTierDistribution().get("LOW"));
        assertNotNull(summary.getSeverityTierDistribution().get("MODERATE"));
        assertNotNull(summary.getSeverityTierDistribution().get("HIGH"));
        assertNotNull(summary.getSeverityTierDistribution().get("SEVERE"));
        assertEquals(2, summary.getActiveScoringConfigurations().size());

        log.info("✅ Hazard Scoring Summary verified: totalScored={}, tierDistribution={}",
                summary.getTotalScoredObservations(), summary.getSeverityTierDistribution());
    }

    @Test
    @DisplayName("6. GeoJSON Vector Output of Scored Hazards with Hazard Score and Tier Properties")
    void testGetHazardScoresGeoJson() {
        GeoJsonFeatureCollectionDto geojson = hazardScoringService.getHazardScoresGeoJson(HazardType.FLOOD, null, 50);

        assertNotNull(geojson);
        assertEquals("FeatureCollection", geojson.getType());
        assertEquals(7, geojson.getCount());
        assertTrue(geojson.getFeatures().stream().allMatch(f ->
                f.getProperties().containsKey("hazardScore") && f.getProperties().containsKey("severityTier")));

        log.info("✅ Scored GeoJSON Vector Layer verified: {} features with score and severity tier", geojson.getCount());
    }
}
