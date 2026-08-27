package com.hazard;

import com.hazard.controller.relocation.RelocationController;
import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.relocation.*;
import com.hazard.dto.relocation.explain.*;
import com.hazard.service.relocation.*;
import com.hazard.service.relocation.explain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Stage 7D — Complete Decision Consolidation & Pipeline Integration Tests.
 *
 * Validates the final unified Stage 7 pipeline:
 * Stage 6 (Feasibility & Allocation) -> Stage 7A (Priority) -> Stage 7B (Recommendation) -> Stage 7C (Explainability) -> Stage 7D (Validation & Decision)
 */
class Stage7DecisionIntegrationTests {

    private RelocationExplainabilityService explainabilityService;
    private RelocationRecommendationService recommendationService;
    private RelocationPriorityService priorityService;
    private RelocationPlanningService planningService;
    private RelocationController controller;

    private ExplanationValidationEngine validationEngine;
    private RelocationExplainabilityEngine explainabilityEngine;

    @BeforeEach
    void setUp() {
        validationEngine = new ExplanationValidationEngine();
        explainabilityEngine = new RelocationExplainabilityEngine(
                new RiskExplanationEngine(),
                new RelocationExplanationEngine(),
                new CapacityExplanationEngine(),
                new DecisionRationaleEngine(),
                validationEngine
        );

        recommendationService = mock(RelocationRecommendationService.class);
        priorityService = mock(RelocationPriorityService.class);
        planningService = mock(RelocationPlanningService.class);

        explainabilityService = new RelocationExplainabilityService(recommendationService, explainabilityEngine);
        controller = new RelocationController(planningService, priorityService, recommendationService, explainabilityService);
    }

    private RelocationRecommendationDto buildSampleRecommendation(String habId, String habName, double pScore,
                                                                   PriorityLevel pLevel, RecommendationStatus status,
                                                                   long reqPop, long allocPop, String siteId,
                                                                   String siteName, double dScore, double distKm) {
        RelocationRecommendationDto rec = new RelocationRecommendationDto(habId, status);
        rec.setHabitationName(habName);
        rec.setDistrict("Sitamarhi");
        rec.setState("Bihar");
        rec.setPriorityScore(pScore);
        rec.setPriorityLevel(pLevel);
        rec.setPriorityRank(1);
        rec.setVulnerablePopulation(reqPop);
        rec.setAllocatedPopulation(allocPop);
        rec.setUnallocatedPopulation(reqPop - allocPop);
        rec.setUrgency(RelocationUrgency.CRITICAL);
        rec.setCapacityFitRatePercentage(reqPop > 0 ? ((double) allocPop / reqPop) * 100.0 : 100.0);
        rec.setTotalCandidatesEvaluated(5);
        rec.setTotalFeasibleCandidates(status == RecommendationStatus.RECOMMENDED ? 3 : 0);

        if (status == RecommendationStatus.RECOMMENDED) {
            RecommendedDestinationDto primary = new RecommendedDestinationDto(siteId, siteName, dScore, 1);
            primary.setDistrict("Sitamarhi");
            primary.setDistanceKilometers(distKm);
            primary.setDistanceMeters(distKm * 1000.0);
            primary.setTotalCapacity(500);
            primary.setAvailableCapacity(500);
            primary.setAccommodatablePopulation(allocPop);
            primary.setSuitabilityClass(SuitabilityClass.HIGHLY_SUITABLE);
            primary.setSuitabilityScore(94.0);
            primary.setHazardSafetyStatus(HazardSafetyStatus.SAFE);

            Map<String, Double> contribs = new LinkedHashMap<>();
            contribs.put(RecommendationScoringConfig.SUITABILITY_QUALITY, 0.94);
            contribs.put(RecommendationScoringConfig.TRANSIT_PROXIMITY, 0.90);
            contribs.put(RecommendationScoringConfig.CAPACITY_FIT, 0.85);
            contribs.put(RecommendationScoringConfig.ACCESS_RELIABILITY, 0.80);
            primary.setScoringContributors(contribs);

            rec.setPrimaryDestination(primary);

            RecommendedDestinationDto alt = new RecommendedDestinationDto("SITE-ALT", "Alternative Shelter", 0.75, 2);
            alt.setDistanceKilometers(6.0);
            alt.setAvailableCapacity(300);
            rec.addAlternativeDestination(alt);
        }

        return rec;
    }

    @Nested
    @DisplayName("7.12 — Decision Aggregation & Structure")
    class DecisionAggregationTests {

        @Test
        @DisplayName("Aggregates WHO, WHERE, WHY, PRIORITY, STATUS, EVIDENCE, ALTERNATIVES, and VALIDATION into unified decision")
        void testCompleteDecisionAggregation() {
            RelocationRequestDto req = new RelocationRequestDto("Sitamarhi");
            req.setHabitationId("HAB-01");
            req.setVulnerablePopulation(200L);

            RelocationRecommendationDto mockRec = buildSampleRecommendation(
                    "HAB-01", "Sonbarsa Flood Village", 0.88, PriorityLevel.IMMEDIATE,
                    RecommendationStatus.RECOMMENDED, 200L, 200L,
                    "SITE-01", "Sitamarhi Flood Shelter", 0.8950, 2.5
            );
            when(recommendationService.recommendForRequest(any())).thenReturn(mockRec);

            RelocationDecisionExplanationDto decision = explainabilityService.explainRequest(req);

            // 1. Identity
            assertNotNull(decision);
            assertEquals("HAB-01", decision.getHabitationId());
            assertEquals("Sonbarsa Flood Village", decision.getHabitationName());
            assertEquals("Sitamarhi", decision.getDistrict());

            // 2. Priority
            assertNotNull(decision.getPriorityResult());
            assertEquals(PriorityLevel.IMMEDIATE, decision.getPriorityResult().getPriorityLevel());
            assertEquals(0.88, decision.getPriorityResult().getPriorityScore());

            // 3. Recommendation
            assertNotNull(decision.getRecommendationResult());
            assertEquals(RecommendationStatus.RECOMMENDED, decision.getRecommendationResult().getStatus());
            assertNotNull(decision.getRecommendationResult().getPrimaryDestination());
            assertEquals("SITE-01", decision.getRecommendationResult().getPrimaryDestination().getSiteId());

            // 4. Explanation (WHO, WHERE, WHY)
            assertNotNull(decision.getDecisionRationale());
            assertTrue(decision.getDecisionRationale().getWhoStatement().contains("Sonbarsa Flood Village"));
            assertTrue(decision.getDecisionRationale().getWhereStatement().contains("Sitamarhi Flood Shelter"));
            assertTrue(decision.getDecisionRationale().getWhyStatement().contains("IMMEDIATE priority"));

            // 5. Evidence Lists
            assertNotNull(decision.getPriorityEvidence());
            assertEquals(6, decision.getPriorityEvidence().size());
            assertNotNull(decision.getDestinationEvidence());
            assertEquals(4, decision.getDestinationEvidence().size());

            // 6. Alternatives & Validation
            assertFalse(decision.getRecommendationResult().getAlternativeDestinations().isEmpty());
            assertTrue(decision.isValid());
            assertTrue(decision.getValidationNotes().isEmpty());
        }
    }

    @Nested
    @DisplayName("7.13 — Comprehensive Decision Validation (All 9 Rules)")
    class DecisionValidationTests {

        @Test
        @DisplayName("Rule 1: Priority level and score mismatch triggers validation failure")
        void testRule1PriorityMismatch() {
            RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();
            RiskExplanationDto risk = new RiskExplanationDto();
            risk.setPriorityLevel(PriorityLevel.IMMEDIATE);
            risk.setPriorityScore(0.90);
            exp.setRiskExplanation(risk);

            RelocationPriorityResultDto p = new RelocationPriorityResultDto("HAB-01", 0.50, PriorityLevel.SHORT_TERM);

            List<String> notes = validationEngine.validateExplanation(exp, p, null);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.stream().anyMatch(n -> n.contains("Priority level mismatch")));
            assertTrue(notes.stream().anyMatch(n -> n.contains("Priority score mismatch")));
        }

        @Test
        @DisplayName("Rule 2: Destination ID mismatch triggers validation failure")
        void testRule2DestinationMismatch() {
            RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();
            RelocationExplanationDto reloc = new RelocationExplanationDto();
            reloc.setDestinationId("SITE-EXPLANATION");
            exp.setRelocationExplanation(reloc);

            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.RECOMMENDED);
            rec.setPrimaryDestination(new RecommendedDestinationDto("SITE-RECOMMENDATION", "Shelter", 0.8, 1));

            List<String> notes = validationEngine.validateExplanation(exp, null, rec);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.stream().anyMatch(n -> n.contains("Destination ID mismatch")));
        }

        @Test
        @DisplayName("Rule 3: Feasibility contradiction triggers validation failure")
        void testRule3FeasibilityContradiction() {
            RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();
            RelocationExplanationDto reloc = new RelocationExplanationDto();
            reloc.setFeasible(true); // Claims feasible
            exp.setRelocationExplanation(reloc);

            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.NO_FEASIBLE_DESTINATION);
            rec.setFeasible(false); // Source is unfeasible

            List<String> notes = validationEngine.validateExplanation(exp, null, rec);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.stream().anyMatch(n -> n.contains("Feasibility contradiction") || n.contains("Feasibility flag mismatch")));
        }

        @Test
        @DisplayName("Rule 4: Capacity arithmetic inconsistency triggers validation failure")
        void testRule4CapacityArithmetic() {
            RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();
            CapacityExplanationDto cap = new CapacityExplanationDto();
            cap.setRequiredPopulation(500L);
            cap.setAllocatedPopulation(300L);
            cap.setUnallocatedPopulation(100L); // 300 + 100 != 500
            exp.setCapacityExplanation(cap);

            List<String> notes = validationEngine.validateExplanation(exp, null, null);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.stream().anyMatch(n -> n.contains("Capacity arithmetic inconsistency")));
        }

        @Test
        @DisplayName("Rule 5: Source and Destination identity match triggers validation failure")
        void testRule5DestinationIdentity() {
            RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();
            RelocationRecommendationDto rec = new RelocationRecommendationDto("SITE-SAME", RecommendationStatus.RECOMMENDED);
            rec.setPrimaryDestination(new RecommendedDestinationDto("SITE-SAME", "Same Shelter", 0.8, 1));

            List<String> notes = validationEngine.validateExplanation(exp, null, rec);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.stream().anyMatch(n -> n.contains("Invalid destination identity")));
        }

        @Test
        @DisplayName("Rule 8: Contributor score outside [0, 1] triggers validation failure")
        void testRule8ScoreBounds() {
            RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();
            DecisionContributorDto outOfBounds = new DecisionContributorDto(
                    "RISK_SEVERITY", "Risk", "PRIORITY", 1.5, 1.5, 0.3, 0.45, "Invalid score"
            );
            exp.setPriorityEvidence(Collections.singletonList(outOfBounds));

            List<String> notes = validationEngine.validateExplanation(exp, null, null);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.stream().anyMatch(n -> n.contains("out of [0.0, 1.0] bounds")));
        }

        @Test
        @DisplayName("Rule 9: NO_FEASIBLE_DESTINATION integrity violation triggers validation failure")
        void testRule9NoFeasibleDestinationIntegrity() {
            RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();
            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.NO_FEASIBLE_DESTINATION);
            rec.setAllocatedPopulation(150L); // Violates zero allocation rule for failure status

            List<String> notes = validationEngine.validateExplanation(exp, null, rec);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.stream().anyMatch(n -> n.contains("Integrity violation")));
        }
    }

    @Nested
    @DisplayName("7.14 — Consolidated Decision API Endpoints")
    class DecisionApiTests {

        @Test
        @DisplayName("POST /api/v1/relocation/decision returns 200 OK with complete consolidated decision")
        void testPostDecisionEndpoint() {
            BatchRelocationDecisionExplanationDto mockBatch = new BatchRelocationDecisionExplanationDto();
            RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();
            exp.setHabitationId("HAB-01");
            exp.setValid(true);
            mockBatch.addExplanation(exp);
            mockBatch.setSummary("1 consolidated decision produced");

            when(recommendationService.recommendBatchForRequests(any())).thenReturn(new BatchRelocationRecommendationDto());

            ResponseEntity<ApiResponse<BatchRelocationDecisionExplanationDto>> response =
                    controller.explainDecision(Collections.singletonList(new RelocationRequestDto("Sitamarhi")));

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
        }

        @Test
        @DisplayName("GET /api/v1/relocation/decision returns 200 OK for district query")
        void testGetDecisionEndpoint() {
            ResponseEntity<ApiResponse<BatchRelocationDecisionExplanationDto>> response =
                    controller.getDecisionExplanations("Sitamarhi", 25.0, "MARGINAL", 250L);

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
        }
    }
}
