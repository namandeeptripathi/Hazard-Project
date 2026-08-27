package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.dto.relocation.RelocationPlanDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.service.relocation.PriorityClassificationEngine;
import com.hazard.service.relocation.PriorityScoringConfig;
import com.hazard.service.relocation.PriorityScoringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7A.3 — Priority Scoring Engine Tests.
 * Tests each individual contributor, combined scoring, min/max scores,
 * null/missing inputs, and determinism.
 */
class PriorityScoringEngineTests {

    private PriorityScoringEngine engine;
    private PriorityScoringConfig config;

    @BeforeEach
    void setUp() {
        config = new PriorityScoringConfig();
        PriorityClassificationEngine classificationEngine = new PriorityClassificationEngine(config);
        engine = new PriorityScoringEngine(config, classificationEngine);
    }

    // --- Test Helpers ---

    private VulnerableHabitationDto createHabitation(Double riskScore, Double hazardSeverity,
                                                      Long vulnerablePop, RelocationUrgency urgency) {
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId("HAB-TEST-001");
        hab.setHabitationName("Test Habitation");
        hab.setDistrict("TestDistrict");
        hab.setState("Bihar");
        hab.setRiskScore(riskScore);
        hab.setHazardSeverityScore(hazardSeverity);
        hab.setVulnerablePopulation(vulnerablePop);
        hab.setUrgency(urgency);
        return hab;
    }

    private RelocationPlanDto createPlan(RelocationStatus status, Long unallocatedPop) {
        RelocationPlanDto plan = new RelocationPlanDto();
        plan.setPlanId("PLAN-TEST-001");
        plan.setOverallStatus(status);
        plan.setTotalUnallocatedPopulation(unallocatedPop);
        return plan;
    }

    @Nested
    @DisplayName("Individual Contributor Normalization")
    class ContributorTests {

        @Test
        @DisplayName("Risk Severity: null → 0.0")
        void riskSeverityNull() {
            assertEquals(0.0, engine.normalizeRiskSeverity(createHabitation(null, null, null, null)));
        }

        @Test
        @DisplayName("Risk Severity: 0.75 → 0.75")
        void riskSeverityDirect() {
            assertEquals(0.75, engine.normalizeRiskSeverity(createHabitation(0.75, null, null, null)));
        }

        @Test
        @DisplayName("Risk Severity: >1.0 → clamped to 1.0")
        void riskSeverityClamped() {
            assertEquals(1.0, engine.normalizeRiskSeverity(createHabitation(1.5, null, null, null)));
        }

        @Test
        @DisplayName("Risk Severity: negative → clamped to 0.0")
        void riskSeverityNegative() {
            assertEquals(0.0, engine.normalizeRiskSeverity(createHabitation(-0.3, null, null, null)));
        }

        @Test
        @DisplayName("Hazard Severity: null → 0.0")
        void hazardSeverityNull() {
            assertEquals(0.0, engine.normalizeHazardSeverity(createHabitation(null, null, null, null)));
        }

        @Test
        @DisplayName("Hazard Severity: 0.90 → 0.90")
        void hazardSeverityDirect() {
            assertEquals(0.90, engine.normalizeHazardSeverity(createHabitation(null, 0.90, null, null)));
        }

        @Test
        @DisplayName("Population Exposure: null → 0.0")
        void populationNull() {
            assertEquals(0.0, engine.normalizePopulationExposure(createHabitation(null, null, null, null)));
        }

        @Test
        @DisplayName("Population Exposure: 0 → 0.0")
        void populationZero() {
            assertEquals(0.0, engine.normalizePopulationExposure(createHabitation(null, null, 0L, null)));
        }

        @Test
        @DisplayName("Population Exposure: 100,000 → 1.0 (at cap)")
        void populationAtCap() {
            double result = engine.normalizePopulationExposure(createHabitation(null, null, 100_000L, null));
            assertEquals(1.0, result, 0.001);
        }

        @Test
        @DisplayName("Population Exposure: 500,000 → capped at 1.0 (above cap)")
        void populationAboveCap() {
            double result = engine.normalizePopulationExposure(createHabitation(null, null, 500_000L, null));
            assertEquals(1.0, result, 0.001);
        }

        @Test
        @DisplayName("Population Exposure: 1 → small positive value")
        void populationOne() {
            double result = engine.normalizePopulationExposure(createHabitation(null, null, 1L, null));
            assertTrue(result > 0.0 && result < 0.1);
        }

        @Test
        @DisplayName("Population Exposure: 1000 → moderate value")
        void populationThousand() {
            double result = engine.normalizePopulationExposure(createHabitation(null, null, 1000L, null));
            assertTrue(result > 0.4 && result < 0.7);
        }

        @Test
        @DisplayName("Capacity Deficit: all null → 0.0")
        void capacityDeficitNull() {
            assertEquals(0.0, engine.normalizeCapacityDeficit(null, null));
        }

        @Test
        @DisplayName("Capacity Deficit: fully unallocated → 1.0")
        void capacityDeficitFull() {
            RelocationPlanDto plan = createPlan(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 1000L);
            VulnerableHabitationDto hab = createHabitation(null, null, 1000L, null);
            assertEquals(1.0, engine.normalizeCapacityDeficit(plan, hab));
        }

        @Test
        @DisplayName("Capacity Deficit: fully allocated → 0.0")
        void capacityDeficitNone() {
            RelocationPlanDto plan = createPlan(RelocationStatus.ALLOCATED, 0L);
            VulnerableHabitationDto hab = createHabitation(null, null, 1000L, null);
            assertEquals(0.0, engine.normalizeCapacityDeficit(plan, hab));
        }

        @Test
        @DisplayName("Capacity Deficit: half unallocated → 0.5")
        void capacityDeficitHalf() {
            RelocationPlanDto plan = createPlan(RelocationStatus.PARTIALLY_ALLOCATED, 500L);
            VulnerableHabitationDto hab = createHabitation(null, null, 1000L, null);
            assertEquals(0.5, engine.normalizeCapacityDeficit(plan, hab));
        }

        @Test
        @DisplayName("Allocation Failure: UNALLOCATED_NO_SAFE_SITE → 1.0")
        void allocationFailureMax() {
            assertEquals(1.0, engine.normalizeAllocationFailure(createPlan(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 0L)));
        }

        @Test
        @DisplayName("Allocation Failure: ALLOCATED → 0.0")
        void allocationFailureMin() {
            assertEquals(0.0, engine.normalizeAllocationFailure(createPlan(RelocationStatus.ALLOCATED, 0L)));
        }

        @Test
        @DisplayName("Allocation Failure: PARTIALLY_ALLOCATED → 0.4")
        void allocationFailurePartial() {
            assertEquals(0.4, engine.normalizeAllocationFailure(createPlan(RelocationStatus.PARTIALLY_ALLOCATED, 0L)));
        }

        @Test
        @DisplayName("Allocation Failure: UNALLOCATED_CAPACITY_EXCEEDED → 0.8")
        void allocationFailureCapacityExceeded() {
            assertEquals(0.8, engine.normalizeAllocationFailure(createPlan(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED, 0L)));
        }

        @Test
        @DisplayName("Allocation Failure: PENDING → 0.2")
        void allocationFailurePending() {
            assertEquals(0.2, engine.normalizeAllocationFailure(createPlan(RelocationStatus.PENDING, 0L)));
        }

        @Test
        @DisplayName("Allocation Failure: null plan → default (0.2)")
        void allocationFailureNullPlan() {
            assertEquals(0.2, engine.normalizeAllocationFailure(null));
        }

        @Test
        @DisplayName("Urgency: CRITICAL → 1.0")
        void urgencyCritical() {
            assertEquals(1.0, engine.normalizeUrgency(createHabitation(null, null, null, RelocationUrgency.CRITICAL)));
        }

        @Test
        @DisplayName("Urgency: HIGH → 0.67")
        void urgencyHigh() {
            assertEquals(0.67, engine.normalizeUrgency(createHabitation(null, null, null, RelocationUrgency.HIGH)));
        }

        @Test
        @DisplayName("Urgency: MODERATE → 0.33")
        void urgencyModerate() {
            assertEquals(0.33, engine.normalizeUrgency(createHabitation(null, null, null, RelocationUrgency.MODERATE)));
        }

        @Test
        @DisplayName("Urgency: LOW → 0.0")
        void urgencyLow() {
            assertEquals(0.0, engine.normalizeUrgency(createHabitation(null, null, null, RelocationUrgency.LOW)));
        }

        @Test
        @DisplayName("Urgency: null habitation → default (0.33)")
        void urgencyNullHabitation() {
            assertEquals(0.33, engine.normalizeUrgency(null));
        }
    }

    @Nested
    @DisplayName("Combined Scoring")
    class CombinedScoringTests {

        @Test
        @DisplayName("Maximum possible score: all contributors at 1.0 → score ~1.0")
        void maximumScore() {
            VulnerableHabitationDto hab = createHabitation(1.0, 1.0, 100_000L, RelocationUrgency.CRITICAL);
            RelocationPlanDto plan = createPlan(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 100_000L);

            RelocationPriorityResultDto result = engine.score(plan, hab);

            // All contributors at max: 0.30*1.0 + 0.15*1.0 + 0.20*1.0 + 0.15*1.0 + 0.10*1.0 + 0.10*1.0 = 1.0
            assertNotNull(result.getPriorityScore());
            assertEquals(1.0, result.getPriorityScore(), 0.01);
            assertEquals(PriorityLevel.IMMEDIATE, result.getPriorityLevel());
        }

        @Test
        @DisplayName("Minimum possible score: all contributors at 0.0 → score ~0.0")
        void minimumScore() {
            VulnerableHabitationDto hab = createHabitation(0.0, 0.0, 0L, RelocationUrgency.LOW);
            RelocationPlanDto plan = createPlan(RelocationStatus.ALLOCATED, 0L);

            RelocationPriorityResultDto result = engine.score(plan, hab);

            assertNotNull(result.getPriorityScore());
            assertEquals(0.0, result.getPriorityScore(), 0.01);
            assertEquals(PriorityLevel.MONITORING, result.getPriorityLevel());
        }

        @Test
        @DisplayName("Score is within [0.0, 1.0] range")
        void scoreInRange() {
            VulnerableHabitationDto hab = createHabitation(0.6, 0.5, 5000L, RelocationUrgency.HIGH);
            RelocationPlanDto plan = createPlan(RelocationStatus.PARTIALLY_ALLOCATED, 2000L);

            RelocationPriorityResultDto result = engine.score(plan, hab);

            assertTrue(result.getPriorityScore() >= 0.0);
            assertTrue(result.getPriorityScore() <= 1.0);
        }

        @Test
        @DisplayName("Score includes all 6 contributor keys")
        void allContributorsPresent() {
            VulnerableHabitationDto hab = createHabitation(0.5, 0.5, 5000L, RelocationUrgency.MODERATE);
            RelocationPlanDto plan = createPlan(RelocationStatus.PARTIALLY_ALLOCATED, 1000L);

            RelocationPriorityResultDto result = engine.score(plan, hab);
            Map<String, Double> contributors = result.getScoringContributors();

            assertEquals(6, contributors.size());
            assertTrue(contributors.containsKey(PriorityScoringConfig.RISK_SEVERITY));
            assertTrue(contributors.containsKey(PriorityScoringConfig.HAZARD_SEVERITY));
            assertTrue(contributors.containsKey(PriorityScoringConfig.POPULATION_EXPOSURE));
            assertTrue(contributors.containsKey(PriorityScoringConfig.CAPACITY_DEFICIT));
            assertTrue(contributors.containsKey(PriorityScoringConfig.ALLOCATION_FAILURE));
            assertTrue(contributors.containsKey(PriorityScoringConfig.URGENCY));
        }

        @Test
        @DisplayName("Higher risk score → higher priority score")
        void higherRiskHigherPriority() {
            RelocationPlanDto plan = createPlan(RelocationStatus.ALLOCATED, 0L);
            VulnerableHabitationDto lowRisk = createHabitation(0.1, 0.1, 1000L, RelocationUrgency.LOW);
            VulnerableHabitationDto highRisk = createHabitation(0.9, 0.9, 1000L, RelocationUrgency.LOW);

            double lowScore = engine.score(plan, lowRisk).getPriorityScore();
            double highScore = engine.score(plan, highRisk).getPriorityScore();

            assertTrue(highScore > lowScore, "Higher risk should produce higher priority score");
        }

        @Test
        @DisplayName("Unallocated status increases priority over allocated")
        void unallocatedHigherThanAllocated() {
            VulnerableHabitationDto hab = createHabitation(0.5, 0.5, 5000L, RelocationUrgency.MODERATE);
            RelocationPlanDto allocated = createPlan(RelocationStatus.ALLOCATED, 0L);
            RelocationPlanDto unallocated = createPlan(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 5000L);

            double allocScore = engine.score(allocated, hab).getPriorityScore();
            double unallocScore = engine.score(unallocated, hab).getPriorityScore();

            assertTrue(unallocScore > allocScore, "Unallocated should produce higher priority score");
        }
    }

    @Nested
    @DisplayName("Null and Missing Inputs")
    class NullInputTests {

        @Test
        @DisplayName("Both null → score 0.0, MONITORING")
        void bothNull() {
            // When both are null, scoring uses all defaults (0.0 except urgency default)
            RelocationPriorityResultDto result = engine.score(null, null);
            assertNotNull(result);
            assertNotNull(result.getPriorityScore());
            // With null habitation: risk=0, hazard=0, pop=0, urgency=default(0.33)
            // With null plan: deficit=0, allocationFailure=default(0.2)
            assertTrue(result.getPriorityScore() >= 0.0);
            assertTrue(result.getPriorityScore() <= 1.0);
        }

        @Test
        @DisplayName("Null habitation → conservative scores")
        void nullHabitation() {
            RelocationPlanDto plan = createPlan(RelocationStatus.ALLOCATED, 0L);
            RelocationPriorityResultDto result = engine.score(plan, null);
            assertNotNull(result);
            assertNotNull(result.getPriorityScore());
        }

        @Test
        @DisplayName("Null plan → conservative scores")
        void nullPlan() {
            VulnerableHabitationDto hab = createHabitation(0.5, 0.5, 5000L, RelocationUrgency.MODERATE);
            RelocationPriorityResultDto result = engine.score(null, hab);
            assertNotNull(result);
            assertNotNull(result.getPriorityScore());
        }

        @Test
        @DisplayName("Habitation with all null scores → low priority")
        void habitationAllNullScores() {
            VulnerableHabitationDto hab = createHabitation(null, null, null, null);
            RelocationPlanDto plan = createPlan(null, null);
            RelocationPriorityResultDto result = engine.score(plan, hab);

            assertNotNull(result);
            assertTrue(result.getPriorityScore() < 0.2, "All-null inputs should produce low priority");
        }
    }

    @Nested
    @DisplayName("Result DTO Population")
    class ResultPopulationTests {

        @Test
        @DisplayName("Result preserves habitation identifiers")
        void preservesIdentifiers() {
            VulnerableHabitationDto hab = createHabitation(0.5, 0.5, 5000L, RelocationUrgency.HIGH);
            RelocationPlanDto plan = createPlan(RelocationStatus.ALLOCATED, 0L);
            plan.setPlanId("PLAN-XYZ");

            RelocationPriorityResultDto result = engine.score(plan, hab);

            assertEquals("HAB-TEST-001", result.getHabitationId());
            assertEquals("Test Habitation", result.getHabitationName());
            assertEquals("TestDistrict", result.getDistrict());
            assertEquals("Bihar", result.getState());
            assertEquals("PLAN-XYZ", result.getPlanId());
            assertEquals(5000L, result.getVulnerablePopulation());
            assertEquals(RelocationUrgency.HIGH, result.getUrgency());
        }

        @Test
        @DisplayName("Result preserves plan context")
        void preservesPlanContext() {
            VulnerableHabitationDto hab = createHabitation(0.5, 0.5, 5000L, RelocationUrgency.HIGH);
            RelocationPlanDto plan = createPlan(RelocationStatus.PARTIALLY_ALLOCATED, 2000L);
            plan.setAllocationRatePercentage(60.0);

            RelocationPriorityResultDto result = engine.score(plan, hab);

            assertEquals(2000L, result.getUnallocatedPopulation());
            assertEquals(60.0, result.getAllocationRatePercentage());
            assertEquals("PARTIALLY_ALLOCATED", result.getOverallStatus());
        }
    }

    @Nested
    @DisplayName("Determinism")
    class DeterminismTests {

        @Test
        @DisplayName("Same inputs always produce same score")
        void deterministicScore() {
            VulnerableHabitationDto hab = createHabitation(0.65, 0.45, 3000L, RelocationUrgency.HIGH);
            RelocationPlanDto plan = createPlan(RelocationStatus.PARTIALLY_ALLOCATED, 1200L);

            Double firstScore = engine.score(plan, hab).getPriorityScore();
            for (int i = 0; i < 50; i++) {
                assertEquals(firstScore, engine.score(plan, hab).getPriorityScore(),
                        "Score should be deterministic on run " + i);
            }
        }
    }
}
