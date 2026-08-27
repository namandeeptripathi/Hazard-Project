package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.explain.*;
import com.hazard.service.relocation.explain.ExplanationValidationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7C.8 — Explanation Validation Engine Tests.
 */
class ExplanationValidationEngineTests {

    private ExplanationValidationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ExplanationValidationEngine();
    }

    private RelocationDecisionExplanationDto createValidExplanation() {
        RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();

        RiskExplanationDto risk = new RiskExplanationDto();
        risk.setPriorityLevel(PriorityLevel.IMMEDIATE);
        risk.setPriorityScore(0.85);
        exp.setRiskExplanation(risk);

        RelocationExplanationDto reloc = new RelocationExplanationDto();
        reloc.setDestinationId("SITE-01");
        reloc.setFeasible(true);
        exp.setRelocationExplanation(reloc);

        CapacityExplanationDto cap = new CapacityExplanationDto();
        cap.setRequiredPopulation(200L);
        cap.setAllocatedPopulation(200L);
        cap.setUnallocatedPopulation(0L);
        exp.setCapacityExplanation(cap);

        DecisionContributorDto c1 = new DecisionContributorDto("KEY1", "Name1", "PRIORITY", 0.8, 0.8, 0.3, 0.24, "Desc");
        exp.setPriorityEvidence(Collections.singletonList(c1));

        return exp;
    }

    @Nested
    @DisplayName("Validation Consistency Tests")
    class ConsistencyTests {

        @Test
        @DisplayName("Completely matching explanation passes validation cleanly")
        void testValidExplanationPasses() {
            RelocationDecisionExplanationDto exp = createValidExplanation();

            RelocationPriorityResultDto priority = new RelocationPriorityResultDto("HAB-01", 0.85, PriorityLevel.IMMEDIATE);

            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.RECOMMENDED);
            rec.setPrimaryDestination(new RecommendedDestinationDto("SITE-01", "Shelter", 0.9, 1));
            rec.setVulnerablePopulation(200L);
            rec.setAllocatedPopulation(200L);
            rec.setUnallocatedPopulation(0L);

            List<String> notes = engine.validateExplanation(exp, priority, rec);

            assertTrue(notes.isEmpty());
            assertTrue(exp.isValid());
        }

        @Test
        @DisplayName("Mismatched priority level fails validation and adds note")
        void testPriorityLevelMismatchFails() {
            RelocationDecisionExplanationDto exp = createValidExplanation();
            // Source says SHORT_TERM, explanation says IMMEDIATE
            RelocationPriorityResultDto priority = new RelocationPriorityResultDto("HAB-01", 0.85, PriorityLevel.SHORT_TERM);

            List<String> notes = engine.validateExplanation(exp, priority, null);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.get(0).contains("Priority level mismatch"));
        }

        @Test
        @DisplayName("Mismatched destination ID fails validation and adds note")
        void testDestinationMismatchFails() {
            RelocationDecisionExplanationDto exp = createValidExplanation();

            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.RECOMMENDED);
            rec.setPrimaryDestination(new RecommendedDestinationDto("SITE-DIFFERENT", "Other Shelter", 0.8, 1));

            List<String> notes = engine.validateExplanation(exp, null, rec);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.get(0).contains("Destination ID mismatch"));
        }

        @Test
        @DisplayName("Capacity arithmetic inconsistency fails validation and adds note")
        void testCapacityArithmeticInconsistencyFails() {
            RelocationDecisionExplanationDto exp = createValidExplanation();
            // 150 allocated + 0 unallocated != 200 required
            exp.getCapacityExplanation().setAllocatedPopulation(150L);
            exp.getCapacityExplanation().setUnallocatedPopulation(0L);

            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.RECOMMENDED);

            List<String> notes = engine.validateExplanation(exp, null, rec);

            assertFalse(notes.isEmpty());
            assertFalse(exp.isValid());
            assertTrue(notes.get(0).contains("Capacity arithmetic inconsistency"));
        }
    }
}
