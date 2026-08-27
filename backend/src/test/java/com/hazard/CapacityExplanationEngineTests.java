package com.hazard;

import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.explain.CapacityExplanationDto;
import com.hazard.service.relocation.explain.CapacityExplanationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7C.4 — Capacity Explanation Engine Tests.
 */
class CapacityExplanationEngineTests {

    private CapacityExplanationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CapacityExplanationEngine();
    }

    private RelocationRecommendationDto createRec(long reqPop, long allocPop, long unallocPop,
                                                  Integer totalCap, Integer availCap, RecommendationStatus status) {
        RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", status);
        rec.setVulnerablePopulation(reqPop);
        rec.setAllocatedPopulation(allocPop);
        rec.setUnallocatedPopulation(unallocPop);
        rec.setCapacityFitRatePercentage((double) allocPop / reqPop * 100.0);

        if (status != RecommendationStatus.NO_FEASIBLE_DESTINATION) {
            RecommendedDestinationDto dest = new RecommendedDestinationDto("SITE-01", "Test Shelter", 0.85, 1);
            dest.setTotalCapacity(totalCap);
            dest.setAvailableCapacity(availCap);
            dest.setAccommodatablePopulation(allocPop);
            rec.setPrimaryDestination(dest);
        }

        return rec;
    }

    @Nested
    @DisplayName("Capacity Sufficiency Tiers")
    class SufficiencyTierTests {

        @Test
        @DisplayName("Available capacity > required population produces SUFFICIENT_HEADROOM status")
        void testSufficientHeadroom() {
            RelocationRecommendationDto rec = createRec(200L, 200L, 0L, 500, 500, RecommendationStatus.RECOMMENDED);

            CapacityExplanationDto exp = engine.explainCapacity(rec);

            assertEquals("SUFFICIENT_HEADROOM", exp.getCapacitySufficiencyStatus());
            assertEquals(200L, exp.getRequiredPopulation());
            assertEquals(200L, exp.getAllocatedPopulation());
            assertEquals(0L, exp.getUnallocatedPopulation());
            assertTrue(exp.getHeadroomMetric().contains("+300 surplus beds"));
            assertTrue(exp.getCapacityNarrative().contains("surplus safety buffer"));
        }

        @Test
        @DisplayName("Available capacity == required population produces EXACT_MATCH status")
        void testExactMatch() {
            RelocationRecommendationDto rec = createRec(250L, 250L, 0L, 250, 250, RecommendationStatus.RECOMMENDED);

            CapacityExplanationDto exp = engine.explainCapacity(rec);

            assertEquals("EXACT_MATCH", exp.getCapacitySufficiencyStatus());
            assertTrue(exp.getHeadroomMetric().contains("0 beds surplus buffer"));
            assertTrue(exp.getCapacityNarrative().contains("Exact Capacity Match"));
        }

        @Test
        @DisplayName("Null available capacity produces UNBOUNDED status")
        void testUnboundedCapacity() {
            RelocationRecommendationDto rec = createRec(1000L, 1000L, 0L, null, null, RecommendationStatus.RECOMMENDED);

            CapacityExplanationDto exp = engine.explainCapacity(rec);

            assertEquals("UNBOUNDED", exp.getCapacitySufficiencyStatus());
            assertTrue(exp.getHeadroomMetric().contains("Unbounded"));
            assertTrue(exp.getCapacityNarrative().contains("unbounded emergency capacity"));
        }

        @Test
        @DisplayName("Partial allocation produces PARTIAL_DEFICIT status")
        void testPartialDeficit() {
            RelocationRecommendationDto rec = createRec(500L, 300L, 200L, 300, 300, RecommendationStatus.CAPACITY_DEFICIT);

            CapacityExplanationDto exp = engine.explainCapacity(rec);

            assertEquals("PARTIAL_DEFICIT", exp.getCapacitySufficiencyStatus());
            assertEquals(200L, exp.getUnallocatedPopulation());
            assertTrue(exp.getHeadroomMetric().contains("-200 bed deficit"));
            assertTrue(exp.getCapacityNarrative().contains("Partial Capacity Deficit"));
        }

        @Test
        @DisplayName("No destination produces NO_DESTINATION status")
        void testNoDestination() {
            RelocationRecommendationDto rec = createRec(300L, 0L, 300L, 0, 0, RecommendationStatus.NO_FEASIBLE_DESTINATION);

            CapacityExplanationDto exp = engine.explainCapacity(rec);

            assertEquals("NO_DESTINATION", exp.getCapacitySufficiencyStatus());
            assertEquals(300L, exp.getUnallocatedPopulation());
            assertTrue(exp.getCapacityNarrative().contains("Capacity Unavailable"));
        }
    }
}
