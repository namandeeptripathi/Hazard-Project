package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.dto.relocation.*;
import com.hazard.service.relocation.RelocationPriorityService;
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
 * Stage 7A — Relocation Priority Service Integration Tests.
 * Tests the full scoring → classification → ranking pipeline.
 */
class RelocationPriorityServiceTests {

    private RelocationPriorityService service;

    @BeforeEach
    void setUp() {
        service = new RelocationPriorityService();
    }

    private VulnerableHabitationDto createHabitation(String id, Double riskScore, Double hazardSeverity,
                                                      Long pop, RelocationUrgency urgency) {
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId(id);
        hab.setHabitationName("Hab-" + id);
        hab.setDistrict("District-" + id);
        hab.setState("Bihar");
        hab.setRiskScore(riskScore);
        hab.setHazardSeverityScore(hazardSeverity);
        hab.setVulnerablePopulation(pop);
        hab.setUrgency(urgency);
        return hab;
    }

    private RelocationPlanDto createPlan(String planId, RelocationStatus status, Long unallocated) {
        RelocationPlanDto plan = new RelocationPlanDto();
        plan.setPlanId(planId);
        plan.setOverallStatus(status);
        plan.setTotalUnallocatedPopulation(unallocated);
        return plan;
    }

    @Nested
    @DisplayName("scoreSingle()")
    class ScoreSingleTests {

        @Test
        @DisplayName("Scores and classifies a single case correctly")
        void scoreSingleCase() {
            VulnerableHabitationDto hab = createHabitation("H1", 0.85, 0.90, 50000L, RelocationUrgency.CRITICAL);
            RelocationPlanDto plan = createPlan("P1", RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 50000L);

            RelocationPriorityResultDto result = service.scoreSingle(plan, hab);

            assertNotNull(result);
            assertNotNull(result.getPriorityScore());
            assertNotNull(result.getPriorityLevel());
            assertTrue(result.getPriorityScore() > 0.5, "High-risk unallocated case should have high priority");
            assertEquals("H1", result.getHabitationId());
        }

        @Test
        @DisplayName("Both null → default MONITORING")
        void bothNull() {
            RelocationPriorityResultDto result = service.scoreSingle(null, null);

            assertNotNull(result);
            assertEquals(0.0, result.getPriorityScore());
            assertEquals(PriorityLevel.MONITORING, result.getPriorityLevel());
        }
    }

    @Nested
    @DisplayName("scoreAndRankAll()")
    class ScoreAndRankAllTests {

        @Test
        @DisplayName("Ranks multiple cases by priority correctly")
        void rankMultiple() {
            List<RelocationPlanDto> plans = Arrays.asList(
                    createPlan("P1", RelocationStatus.ALLOCATED, 0L),
                    createPlan("P2", RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 10000L),
                    createPlan("P3", RelocationStatus.PARTIALLY_ALLOCATED, 3000L)
            );
            List<VulnerableHabitationDto> habitations = Arrays.asList(
                    createHabitation("H1", 0.1, 0.1, 500L, RelocationUrgency.LOW),
                    createHabitation("H2", 0.9, 0.9, 10000L, RelocationUrgency.CRITICAL),
                    createHabitation("H3", 0.5, 0.5, 5000L, RelocationUrgency.HIGH)
            );

            PriorityRankingResultDto result = service.scoreAndRankAll(plans, habitations);

            assertEquals(3, result.getTotalCases());
            // H2 should be ranked first (highest risk, critical urgency, unallocated)
            assertEquals("H2", result.getRankedPriorities().get(0).getHabitationId());
            assertEquals(1, result.getRankedPriorities().get(0).getPriorityRank());
            // H1 should be ranked last (low risk, allocated)
            assertEquals("H1", result.getRankedPriorities().get(2).getHabitationId());
            assertEquals(3, result.getRankedPriorities().get(2).getPriorityRank());
        }

        @Test
        @DisplayName("Null plans → empty result")
        void nullPlans() {
            PriorityRankingResultDto result = service.scoreAndRankAll(null, null);

            assertEquals(0, result.getTotalCases());
            assertNotNull(result.getRankingSummary());
        }

        @Test
        @DisplayName("Empty lists → empty result")
        void emptyLists() {
            PriorityRankingResultDto result = service.scoreAndRankAll(
                    Collections.emptyList(), Collections.emptyList());

            assertEquals(0, result.getTotalCases());
        }

        @Test
        @DisplayName("Mismatched list sizes → processes only shorter length")
        void mismatchedSizes() {
            List<RelocationPlanDto> plans = Arrays.asList(
                    createPlan("P1", RelocationStatus.ALLOCATED, 0L),
                    createPlan("P2", RelocationStatus.ALLOCATED, 0L)
            );
            List<VulnerableHabitationDto> habitations = Collections.singletonList(
                    createHabitation("H1", 0.5, 0.5, 1000L, RelocationUrgency.MODERATE)
            );

            PriorityRankingResultDto result = service.scoreAndRankAll(plans, habitations);

            assertEquals(1, result.getTotalCases());
        }

        @Test
        @DisplayName("Tier distribution is accurate")
        void tierDistribution() {
            List<RelocationPlanDto> plans = Arrays.asList(
                    createPlan("P1", RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 50000L),
                    createPlan("P2", RelocationStatus.ALLOCATED, 0L)
            );
            List<VulnerableHabitationDto> habitations = Arrays.asList(
                    createHabitation("H1", 0.95, 0.95, 50000L, RelocationUrgency.CRITICAL),
                    createHabitation("H2", 0.05, 0.05, 100L, RelocationUrgency.LOW)
            );

            PriorityRankingResultDto result = service.scoreAndRankAll(plans, habitations);

            assertEquals(2, result.getTotalCases());
            assertTrue(result.getImmediateCount() + result.getShortTermCount()
                    + result.getMediumTermCount() + result.getMonitoringCount() == 2);
        }
    }

    @Nested
    @DisplayName("scoreAndRankPlans()")
    class ScoreAndRankPlansTests {

        @Test
        @DisplayName("Extracts habitation from plan assignments")
        void extractFromAssignments() {
            RelocationPlanDto plan = createPlan("P1", RelocationStatus.ALLOCATED, 0L);
            RelocationAssignmentDto assignment = new RelocationAssignmentDto();
            assignment.setHabitationId("HAB-EXTRACT-1");
            assignment.setHabitationName("Extracted Village");
            assignment.setOriginDistrict("Sitamarhi");
            assignment.setVulnerablePopulation(5000L);
            assignment.setUrgency(RelocationUrgency.HIGH);
            plan.setAssignments(Collections.singletonList(assignment));

            PriorityRankingResultDto result = service.scoreAndRankPlans(Collections.singletonList(plan));

            assertEquals(1, result.getTotalCases());
            assertEquals("HAB-EXTRACT-1", result.getRankedPriorities().get(0).getHabitationId());
        }

        @Test
        @DisplayName("Extracts habitation from unallocated list when no assignments")
        void extractFromUnallocated() {
            RelocationPlanDto plan = createPlan("P1", RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 3000L);
            VulnerableHabitationDto unalloc = new VulnerableHabitationDto();
            unalloc.setHabitationId("HAB-UNALLOC-1");
            unalloc.setHabitationName("Unallocated Village");
            unalloc.setVulnerablePopulation(3000L);
            unalloc.setUrgency(RelocationUrgency.CRITICAL);
            plan.setUnallocatedHabitations(Collections.singletonList(unalloc));

            PriorityRankingResultDto result = service.scoreAndRankPlans(Collections.singletonList(plan));

            assertEquals(1, result.getTotalCases());
        }

        @Test
        @DisplayName("Null plans → empty result")
        void nullPlans() {
            PriorityRankingResultDto result = service.scoreAndRankPlans(null);
            assertEquals(0, result.getTotalCases());
        }
    }

    @Nested
    @DisplayName("End-to-End Pipeline")
    class EndToEndTests {

        @Test
        @DisplayName("Full pipeline produces consistent, complete results")
        void fullPipeline() {
            List<RelocationPlanDto> plans = new ArrayList<>();
            List<VulnerableHabitationDto> habitations = new ArrayList<>();

            // Case 1: Critical emergency
            plans.add(createPlan("P1", RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 10000L));
            habitations.add(createHabitation("H1", 0.95, 0.90, 10000L, RelocationUrgency.CRITICAL));

            // Case 2: Moderate risk, partial allocation
            plans.add(createPlan("P2", RelocationStatus.PARTIALLY_ALLOCATED, 2000L));
            habitations.add(createHabitation("H2", 0.45, 0.40, 5000L, RelocationUrgency.MODERATE));

            // Case 3: Low risk, fully allocated
            plans.add(createPlan("P3", RelocationStatus.ALLOCATED, 0L));
            habitations.add(createHabitation("H3", 0.10, 0.10, 200L, RelocationUrgency.LOW));

            PriorityRankingResultDto result = service.scoreAndRankAll(plans, habitations);

            // Verify completeness
            assertEquals(3, result.getTotalCases());
            assertEquals(3, result.getRankedPriorities().size());

            // Verify ordering: H1 (highest risk) → H2 → H3 (lowest risk)
            assertEquals("H1", result.getRankedPriorities().get(0).getHabitationId());
            assertEquals("H3", result.getRankedPriorities().get(2).getHabitationId());

            // Verify ranks
            assertEquals(1, result.getRankedPriorities().get(0).getPriorityRank());
            assertEquals(2, result.getRankedPriorities().get(1).getPriorityRank());
            assertEquals(3, result.getRankedPriorities().get(2).getPriorityRank());

            // Verify all scores are valid
            for (RelocationPriorityResultDto r : result.getRankedPriorities()) {
                assertNotNull(r.getPriorityScore());
                assertTrue(r.getPriorityScore() >= 0.0 && r.getPriorityScore() <= 1.0);
                assertNotNull(r.getPriorityLevel());
                assertNotNull(r.getScoringContributors());
                assertEquals(6, r.getScoringContributors().size());
            }

            // Verify summary
            assertNotNull(result.getRankingSummary());
            assertTrue(result.getRankingSummary().length() > 0);
        }
    }
}
