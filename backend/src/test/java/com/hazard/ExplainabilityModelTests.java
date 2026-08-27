package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.dto.relocation.explain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7C.6 — Explainability Model & DTO Tests.
 */
class ExplainabilityModelTests {

    @Nested
    @DisplayName("DecisionContributorDto Tests")
    class DecisionContributorTests {

        @Test
        @DisplayName("Should construct with full parameters and calculate impact direction accurately")
        void testContributorConstruction() {
            DecisionContributorDto highImpact = new DecisionContributorDto(
                    "RISK_SEVERITY", "Multi-Hazard Risk", "PRIORITY",
                    0.85, 0.85, 0.30, 0.255, "Elevated risk index."
            );
            assertEquals("HIGH_IMPACT", highImpact.getImpactDirection());
            assertEquals("RISK_SEVERITY", highImpact.getContributorKey());
            assertEquals("PRIORITY", highImpact.getCategory());

            DecisionContributorDto modImpact = new DecisionContributorDto(
                    "HAZARD_SEVERITY", "Hazard Footprint", "PRIORITY",
                    0.60, 0.60, 0.15, 0.090, "Moderate footprint."
            );
            assertEquals("MODERATE_IMPACT", modImpact.getImpactDirection());

            DecisionContributorDto lowImpact = new DecisionContributorDto(
                    "URGENCY", "Relocation Urgency", "PRIORITY",
                    "LOW", 0.0, 0.10, 0.000, "Low urgency."
            );
            assertEquals("LOW_IMPACT", lowImpact.getImpactDirection());
        }
    }

    @Nested
    @DisplayName("RiskExplanationDto Tests")
    class RiskExplanationTests {

        @Test
        @DisplayName("Should add drivers and contributors cleanly")
        void testRiskExplanationAdders() {
            RiskExplanationDto dto = new RiskExplanationDto();
            dto.addPrimaryRiskDriver("Multi-Hazard Risk (35% score contribution)");
            dto.addContributor(new DecisionContributorDto("KEY1", "Name1", "PRIORITY", 1.0, 1.0, 0.3, 0.3, "Desc1"));

            assertEquals(1, dto.getPrimaryRiskDrivers().size());
            assertEquals(1, dto.getContributors().size());
        }
    }

    @Nested
    @DisplayName("DecisionRationaleDto Tests")
    class DecisionRationaleTests {

        @Test
        @DisplayName("Should manage key strengths and risks collections properly")
        void testRationaleLists() {
            DecisionRationaleDto rationale = new DecisionRationaleDto();
            rationale.addKeyStrength("100% capacity accommodation");
            rationale.addKeyRiskOrDeficit("Active flood surge in origin");

            assertEquals(1, rationale.getKeyStrengths().size());
            assertEquals(1, rationale.getKeyRisksOrDeficits().size());
        }
    }

    @Nested
    @DisplayName("BatchRelocationDecisionExplanationDto Tests")
    class BatchDecisionExplanationTests {

        @Test
        @DisplayName("recomputeStatistics counts valid explanations and priority levels accurately")
        void testBatchStatistics() {
            BatchRelocationDecisionExplanationDto batch = new BatchRelocationDecisionExplanationDto();

            RelocationDecisionExplanationDto exp1 = new RelocationDecisionExplanationDto();
            exp1.setValid(true);
            com.hazard.dto.relocation.RelocationPriorityResultDto p1 = new com.hazard.dto.relocation.RelocationPriorityResultDto("H1", 0.85, PriorityLevel.IMMEDIATE);
            exp1.setPriorityResult(p1);

            RelocationDecisionExplanationDto exp2 = new RelocationDecisionExplanationDto();
            exp2.setValid(false);
            com.hazard.dto.relocation.RelocationPriorityResultDto p2 = new com.hazard.dto.relocation.RelocationPriorityResultDto("H2", 0.55, PriorityLevel.SHORT_TERM);
            exp2.setPriorityResult(p2);

            batch.addExplanation(exp1);
            batch.addExplanation(exp2);

            assertEquals(2, batch.getTotalCases());
            assertEquals(1, batch.getValidExplanations());
            assertEquals(1, batch.getInvalidExplanations());
            assertEquals(1, batch.getImmediateCases());
            assertEquals(1, batch.getShortTermCases());
            assertEquals(0, batch.getMediumTermCases());
            assertEquals(0, batch.getMonitoringCases());
        }
    }
}
