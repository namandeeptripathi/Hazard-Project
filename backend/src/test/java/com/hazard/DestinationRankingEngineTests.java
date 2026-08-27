package com.hazard;

import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.service.relocation.DestinationRankingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7B.5 — Destination Ranking Engine Tests.
 * Verifies ranking order, multi-level tie-breaking, immutability, and edge cases.
 */
class DestinationRankingEngineTests {

    private DestinationRankingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DestinationRankingEngine();
    }

    private RecommendedDestinationDto createDest(String id, double score, SuitabilityClass sClass,
                                                 Double distMeters, Integer availCap, boolean feasible) {
        RecommendedDestinationDto d = new RecommendedDestinationDto();
        d.setSiteId(id);
        d.setSiteName("Site " + id);
        d.setDestinationScore(score);
        d.setSuitabilityClass(sClass);
        d.setDistanceMeters(distMeters);
        if (distMeters != null) {
            d.setDistanceKilometers(distMeters / 1000.0);
        }
        d.setAvailableCapacity(availCap);
        d.setFeasible(feasible);
        return d;
    }

    @Nested
    @DisplayName("Score-Based Ranking")
    class ScoreRankingTests {

        @Test
        @DisplayName("Higher score ranked before lower score")
        void testHigherScoreFirst() {
            RecommendedDestinationDto d1 = createDest("S1", 0.75, SuitabilityClass.SUITABLE, 5000.0, 500, true);
            RecommendedDestinationDto d2 = createDest("S2", 0.90, SuitabilityClass.HIGHLY_SUITABLE, 4000.0, 600, true);
            RecommendedDestinationDto d3 = createDest("S3", 0.60, SuitabilityClass.MARGINAL, 8000.0, 300, true);

            List<RecommendedDestinationDto> ranked = engine.rankDestinations(Arrays.asList(d1, d2, d3));

            assertEquals(3, ranked.size());
            assertEquals("S2", ranked.get(0).getSiteId());
            assertEquals(1, ranked.get(0).getDestinationRank());
            assertEquals("S1", ranked.get(1).getSiteId());
            assertEquals(2, ranked.get(1).getDestinationRank());
            assertEquals("S3", ranked.get(2).getSiteId());
            assertEquals(3, ranked.get(2).getDestinationRank());
        }

        @Test
        @DisplayName("Feasible candidates strictly ranked above unfeasible candidates")
        void testFeasibleAboveUnfeasible() {
            RecommendedDestinationDto unfeasible = createDest("UNFEASIBLE", 0.95, SuitabilityClass.HIGHLY_SUITABLE, 1000.0, 0, false);
            RecommendedDestinationDto feasible = createDest("FEASIBLE", 0.70, SuitabilityClass.SUITABLE, 10000.0, 300, true);

            List<RecommendedDestinationDto> ranked = engine.rankDestinations(Arrays.asList(unfeasible, feasible));

            assertEquals("FEASIBLE", ranked.get(0).getSiteId());
            assertEquals("UNFEASIBLE", ranked.get(1).getSiteId());
        }
    }

    @Nested
    @DisplayName("Tie-Breaking Comparator")
    class TieBreakingTests {

        @Test
        @DisplayName("Same score: higher suitability tier ranks first")
        void testTieBreakBySuitabilityTier() {
            RecommendedDestinationDto d1 = createDest("S1", 0.80, SuitabilityClass.SUITABLE, 5000.0, 500, true);
            RecommendedDestinationDto d2 = createDest("S2", 0.80, SuitabilityClass.HIGHLY_SUITABLE, 5000.0, 500, true);

            List<RecommendedDestinationDto> ranked = engine.rankDestinations(Arrays.asList(d1, d2));

            assertEquals("S2", ranked.get(0).getSiteId());
            assertEquals("S1", ranked.get(1).getSiteId());
        }

        @Test
        @DisplayName("Same score and tier: shorter transit distance ranks first")
        void testTieBreakByDistance() {
            RecommendedDestinationDto d1 = createDest("FAR", 0.80, SuitabilityClass.SUITABLE, 12000.0, 500, true);
            RecommendedDestinationDto d2 = createDest("NEAR", 0.80, SuitabilityClass.SUITABLE, 3000.0, 500, true);

            List<RecommendedDestinationDto> ranked = engine.rankDestinations(Arrays.asList(d1, d2));

            assertEquals("NEAR", ranked.get(0).getSiteId());
            assertEquals("FAR", ranked.get(1).getSiteId());
        }

        @Test
        @DisplayName("Same score, tier, and distance: higher capacity ranks first")
        void testTieBreakByCapacity() {
            RecommendedDestinationDto d1 = createDest("SMALL", 0.80, SuitabilityClass.SUITABLE, 5000.0, 200, true);
            RecommendedDestinationDto d2 = createDest("LARGE", 0.80, SuitabilityClass.SUITABLE, 5000.0, 800, true);

            List<RecommendedDestinationDto> ranked = engine.rankDestinations(Arrays.asList(d1, d2));

            assertEquals("LARGE", ranked.get(0).getSiteId());
            assertEquals("SMALL", ranked.get(1).getSiteId());
        }

        @Test
        @DisplayName("Same score, tier, distance, and capacity: alphabetical site ID ranks first")
        void testTieBreakBySiteId() {
            RecommendedDestinationDto d1 = createDest("ZZZ", 0.80, SuitabilityClass.SUITABLE, 5000.0, 500, true);
            RecommendedDestinationDto d2 = createDest("AAA", 0.80, SuitabilityClass.SUITABLE, 5000.0, 500, true);

            List<RecommendedDestinationDto> ranked = engine.rankDestinations(Arrays.asList(d1, d2));

            assertEquals("AAA", ranked.get(0).getSiteId());
            assertEquals("ZZZ", ranked.get(1).getSiteId());
        }
    }

    @Nested
    @DisplayName("Immutability & Safety")
    class ImmutabilityTests {

        @Test
        @DisplayName("Source list is not mutated during ranking")
        void testSourceListNotMutated() {
            RecommendedDestinationDto d1 = createDest("S1", 0.50, SuitabilityClass.MARGINAL, 5000.0, 500, true);
            RecommendedDestinationDto d2 = createDest("S2", 0.90, SuitabilityClass.HIGHLY_SUITABLE, 2000.0, 500, true);

            List<RecommendedDestinationDto> source = new ArrayList<>(Arrays.asList(d1, d2));
            List<RecommendedDestinationDto> ranked = engine.rankDestinations(source);

            assertEquals("S1", source.get(0).getSiteId(), "Source list order must remain unchanged");
            assertEquals("S2", source.get(1).getSiteId());
            assertEquals("S2", ranked.get(0).getSiteId(), "Ranked list must have highest score first");
        }

        @Test
        @DisplayName("Empty or null candidate list returns empty list")
        void testEmptyOrNullSafety() {
            assertTrue(engine.rankDestinations(null).isEmpty());
            assertTrue(engine.rankDestinations(Collections.emptyList()).isEmpty());
        }

        @Test
        @DisplayName("Null items in candidate list are safely filtered")
        void testNullItemsFiltered() {
            RecommendedDestinationDto d = createDest("S1", 0.80, SuitabilityClass.SUITABLE, 3000.0, 500, true);
            List<RecommendedDestinationDto> listWithNulls = Arrays.asList(null, d, null);

            List<RecommendedDestinationDto> ranked = engine.rankDestinations(listWithNulls);

            assertEquals(1, ranked.size());
            assertEquals("S1", ranked.get(0).getSiteId());
        }
    }
}
