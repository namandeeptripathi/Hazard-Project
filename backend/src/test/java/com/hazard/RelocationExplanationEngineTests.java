package com.hazard;

import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.explain.RelocationExplanationDto;
import com.hazard.service.relocation.RecommendationScoringConfig;
import com.hazard.service.relocation.explain.RelocationExplanationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7C.3 — Relocation Explanation Engine Tests.
 */
class RelocationExplanationEngineTests {

    private RelocationExplanationEngine engine;
    private RecommendationScoringConfig config;

    @BeforeEach
    void setUp() {
        config = new RecommendationScoringConfig();
        engine = new RelocationExplanationEngine(config);
    }

    private RecommendedDestinationDto createDestination(String id, String name, double score, int rank,
                                                        SuitabilityClass sClass, double distKm, int cap) {
        RecommendedDestinationDto d = new RecommendedDestinationDto(id, name, score, rank);
        d.setSuitabilityClass(sClass);
        d.setSuitabilityScore(sClass == SuitabilityClass.HIGHLY_SUITABLE ? 92.0 : 75.0);
        d.setDistanceKilometers(distKm);
        d.setDistanceMeters(distKm * 1000.0);
        d.setTotalCapacity(cap);
        d.setAvailableCapacity(cap);
        d.setHazardSafetyStatus(HazardSafetyStatus.SAFE);

        Map<String, Double> contribs = new LinkedHashMap<>();
        contribs.put(RecommendationScoringConfig.SUITABILITY_QUALITY, 0.92);
        contribs.put(RecommendationScoringConfig.TRANSIT_PROXIMITY, 0.85);
        contribs.put(RecommendationScoringConfig.CAPACITY_FIT, 0.90);
        contribs.put(RecommendationScoringConfig.ACCESS_RELIABILITY, 0.80);
        d.setScoringContributors(contribs);

        return d;
    }

    @Nested
    @DisplayName("Feasible Destination Explanations")
    class FeasibleDestinationTests {

        @Test
        @DisplayName("Generates comprehensive gate and soft preference explanations for feasible recommendation")
        void testFeasibleExplanation() {
            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.RECOMMENDED);
            rec.setHabitationName("Village Alpha");
            rec.setVulnerablePopulation(200L);
            rec.setTotalCandidatesEvaluated(5);
            rec.setTotalFeasibleCandidates(3);

            RecommendedDestinationDto primary = createDestination(
                    "SITE-01", "Central Community Shelter", 0.8850, 1,
                    SuitabilityClass.HIGHLY_SUITABLE, 3.5, 600
            );
            rec.setPrimaryDestination(primary);

            RecommendedDestinationDto alt1 = createDestination(
                    "SITE-02", "Secondary School Hall", 0.7200, 2,
                    SuitabilityClass.SUITABLE, 6.2, 400
            );
            rec.addAlternativeDestination(alt1);

            RelocationExplanationDto exp = engine.explainRelocation(rec);

            assertNotNull(exp);
            assertTrue(exp.isFeasible());
            assertEquals("SITE-01", exp.getDestinationId());
            assertEquals("Central Community Shelter", exp.getDestinationName());
            assertEquals(1, exp.getDestinationRank());
            assertEquals(0.8850, exp.getDestinationScore());

            // 4 contributors
            assertEquals(4, exp.getContributors().size());

            // Narrative checks
            assertTrue(exp.getFeasibilityGateSummary().contains("Hard Feasibility Passed"));
            assertTrue(exp.getSoftPreferenceSummary().contains("Soft Preference Selection"));
            assertTrue(exp.getComparativeRankNarrative().contains("Rank #1 among 3 feasible"));
            assertTrue(exp.getAlternativeDestinationsSummary().contains("Secondary School Hall"));
        }
    }

    @Nested
    @DisplayName("No Feasible Destination Explanations")
    class NoFeasibleDestinationTests {

        @Test
        @DisplayName("Generates explicit rejection explanation when zero candidates pass feasibility gates")
        void testNoFeasibleExplanation() {
            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.NO_FEASIBLE_DESTINATION);
            rec.setTotalCandidatesEvaluated(4);
            rec.setTotalFeasibleCandidates(0);
            rec.setPrimaryDestination(null);

            RelocationExplanationDto exp = engine.explainRelocation(rec);

            assertNotNull(exp);
            assertFalse(exp.isFeasible());
            assertEquals("NONE", exp.getDestinationId());
            assertTrue(exp.getFeasibilityGateSummary().contains("Hard Feasibility Rejection"));
            assertTrue(exp.getAlternativeDestinationsSummary().contains("No fallback alternative"));
        }
    }
}
