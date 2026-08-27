package com.hazard;

import com.hazard.domain.safesite.HealthcareAccessStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.domain.safesite.WaterAccessStatus;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.relocation.DestinationScoringEngine;
import com.hazard.service.relocation.RecommendationScoringConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7B.4 — Destination Scoring Engine Tests.
 * Tests each contributor independently, combined scoring, normalization, null handling, and determinism.
 */
class DestinationScoringEngineTests {

    private DestinationScoringEngine engine;
    private RecommendationScoringConfig config;

    @BeforeEach
    void setUp() {
        config = new RecommendationScoringConfig();
        engine = new DestinationScoringEngine(config);
    }

    private VulnerableHabitationDto createHabitation(long pop) {
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId("HAB-01");
        hab.setVulnerablePopulation(pop);
        return hab;
    }

    private RecommendedDestinationDto createDestination(Double suitabilityScore, SuitabilityClass sClass,
                                                        Double distKm, Integer availableCap) {
        RecommendedDestinationDto dest = new RecommendedDestinationDto();
        dest.setSiteId("SITE-01");
        dest.setSiteName("Test Destination");
        dest.setSuitabilityScore(suitabilityScore);
        dest.setSuitabilityClass(sClass);
        dest.setDistanceKilometers(distKm);
        if (distKm != null) {
            dest.setDistanceMeters(distKm * 1000.0);
        }
        dest.setAvailableCapacity(availableCap);
        dest.setFeasible(true);
        return dest;
    }

    @Nested
    @DisplayName("Suitability Quality Contributor")
    class SuitabilityQualityTests {

        @Test
        @DisplayName("Normalizes 0-100 score scale to [0, 1]")
        void test100ScaleNormalization() {
            RecommendedDestinationDto dest = createDestination(85.0, SuitabilityClass.SUITABLE, 5.0, 500);
            double score = engine.normalizeSuitabilityQuality(dest);
            assertEquals(0.85, score, 0.001);
        }

        @Test
        @DisplayName("Normalizes 0-1 score scale directly")
        void test01ScaleNormalization() {
            RecommendedDestinationDto dest = createDestination(0.92, SuitabilityClass.HIGHLY_SUITABLE, 5.0, 500);
            double score = engine.normalizeSuitabilityQuality(dest);
            assertEquals(0.92, score, 0.001);
        }

        @Test
        @DisplayName("Falls back to tier default when numerical score is null")
        void testTierFallbackWhenScoreNull() {
            RecommendedDestinationDto dest1 = createDestination(null, SuitabilityClass.HIGHLY_SUITABLE, 5.0, 500);
            RecommendedDestinationDto dest2 = createDestination(null, SuitabilityClass.SUITABLE, 5.0, 500);
            RecommendedDestinationDto dest3 = createDestination(null, SuitabilityClass.MARGINAL, 5.0, 500);

            assertEquals(0.95, engine.normalizeSuitabilityQuality(dest1), 0.001);
            assertEquals(0.80, engine.normalizeSuitabilityQuality(dest2), 0.001);
            assertEquals(0.50, engine.normalizeSuitabilityQuality(dest3), 0.001);
        }
    }

    @Nested
    @DisplayName("Transit Proximity Contributor")
    class TransitProximityTests {

        @Test
        @DisplayName("0 km distance gives maximum proximity (1.0)")
        void testZeroDistanceMaxScore() {
            RecommendedDestinationDto dest = createDestination(80.0, SuitabilityClass.SUITABLE, 0.0, 500);
            double prox = engine.normalizeTransitProximity(dest, 50.0);
            assertEquals(1.0, prox, 0.001);
        }

        @Test
        @DisplayName("Halfway distance gives 0.5 proximity")
        void testHalfwayDistance() {
            RecommendedDestinationDto dest = createDestination(80.0, SuitabilityClass.SUITABLE, 25.0, 500);
            double prox = engine.normalizeTransitProximity(dest, 50.0);
            assertEquals(0.5, prox, 0.001);
        }

        @Test
        @DisplayName("Distance >= maxRadius gives 0.0 proximity")
        void testExceededDistanceZeroScore() {
            RecommendedDestinationDto dest = createDestination(80.0, SuitabilityClass.SUITABLE, 50.0, 500);
            double prox = engine.normalizeTransitProximity(dest, 50.0);
            assertEquals(0.0, prox, 0.001);
        }

        @Test
        @DisplayName("Missing distance gives default conservative proximity")
        void testMissingDistanceConservativeDefault() {
            RecommendedDestinationDto dest = createDestination(80.0, SuitabilityClass.SUITABLE, null, 500);
            double prox = engine.normalizeTransitProximity(dest, 50.0);
            assertEquals(0.20, prox, 0.001);
        }
    }

    @Nested
    @DisplayName("Capacity Fit Contributor")
    class CapacityFitTests {

        @Test
        @DisplayName("Unbounded capacity (null) receives perfect capacity fit score (1.0)")
        void testUnboundedCapacityScore() {
            RecommendedDestinationDto dest = createDestination(80.0, SuitabilityClass.SUITABLE, 5.0, null);
            double fit = engine.normalizeCapacityFit(dest, 200L);
            assertEquals(1.0, fit, 0.001);
        }

        @Test
        @DisplayName("Available capacity >= 2x required population receives max headroom score (1.0)")
        void testLargeHeadroomCapacityScore() {
            RecommendedDestinationDto dest = createDestination(80.0, SuitabilityClass.SUITABLE, 5.0, 1000);
            double fit = engine.normalizeCapacityFit(dest, 200L); // 1000 >= 400
            assertEquals(1.0, fit, 0.001);
        }

        @Test
        @DisplayName("Available capacity == required population receives base sufficiency score (0.8)")
        void testExactCapacitySufficiencyScore() {
            RecommendedDestinationDto dest = createDestination(80.0, SuitabilityClass.SUITABLE, 5.0, 200);
            double fit = engine.normalizeCapacityFit(dest, 200L);
            // 0.6 + 0.4 * (200 / 400) = 0.6 + 0.2 = 0.8
            assertEquals(0.80, fit, 0.001);
        }
    }

    @Nested
    @DisplayName("Access Reliability Contributor")
    class AccessReliabilityTests {

        @Test
        @DisplayName("NEAR access across road, water, healthcare produces high access score")
        void testNearAccessScore() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setRoadAccessStatus(RoadAccessStatus.NEAR);
            site.setWaterAccessStatus(WaterAccessStatus.NEAR);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.NEAR);

            double score = engine.normalizeAccessReliability(site);
            assertEquals(1.0, score, 0.001);
        }

        @Test
        @DisplayName("Null raw site produces neutral default score")
        void testNullSiteDefaultAccessScore() {
            double score = engine.normalizeAccessReliability(null);
            assertEquals(0.50, score, 0.001);
        }
    }

    @Nested
    @DisplayName("Combined Scoring & Determinism")
    class CombinedScoringTests {

        @Test
        @DisplayName("Scores destination and populates scoringContributors map accurately")
        void testFullScoringAndBreakdown() {
            VulnerableHabitationDto hab = createHabitation(200L);
            RecommendedDestinationDto dest = createDestination(85.0, SuitabilityClass.SUITABLE, 10.0, 600);

            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setRoadAccessStatus(RoadAccessStatus.NEAR);
            site.setWaterAccessStatus(WaterAccessStatus.MODERATE);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.NEAR);

            double finalScore = engine.scoreDestination(hab, dest, site, 50.0);

            assertTrue(finalScore > 0.5 && finalScore <= 1.0);
            assertEquals(finalScore, dest.getDestinationScore());

            Map<String, Double> map = dest.getScoringContributors();
            assertEquals(4, map.size());
            assertTrue(map.containsKey(RecommendationScoringConfig.SUITABILITY_QUALITY));
            assertTrue(map.containsKey(RecommendationScoringConfig.TRANSIT_PROXIMITY));
            assertTrue(map.containsKey(RecommendationScoringConfig.CAPACITY_FIT));
            assertTrue(map.containsKey(RecommendationScoringConfig.ACCESS_RELIABILITY));
        }

        @Test
        @DisplayName("Scoring is 100% deterministic over multiple runs")
        void testScoringDeterminism() {
            VulnerableHabitationDto hab = createHabitation(250L);
            RecommendedDestinationDto dest = createDestination(92.0, SuitabilityClass.HIGHLY_SUITABLE, 8.5, 800);

            double score1 = engine.scoreDestination(hab, dest, null, 40.0);
            for (int i = 0; i < 50; i++) {
                double scoreN = engine.scoreDestination(hab, dest, null, 40.0);
                assertEquals(score1, scoreN, "Score must remain perfectly deterministic on run " + i);
            }
        }
    }
}
