package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.explain.DecisionRationaleDto;
import com.hazard.service.relocation.explain.DecisionRationaleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7C.1 — Decision Rationale Engine Tests.
 */
class DecisionRationaleEngineTests {

    private DecisionRationaleEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DecisionRationaleEngine();
    }

    @Nested
    @DisplayName("Successful Recommendation Rationale")
    class SuccessfulRationaleTests {

        @Test
        @DisplayName("Synthesizes comprehensive WHO, WHERE, WHY and immediate deployment guidance")
        void testSuccessfulRationale() {
            RelocationPriorityResultDto priority = new RelocationPriorityResultDto("HAB-01", 0.85, PriorityLevel.IMMEDIATE);
            priority.setHabitationName("Sonbarsa Village");
            priority.setVulnerablePopulation(200L);

            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.RECOMMENDED);
            rec.setHabitationName("Sonbarsa Village");
            rec.setVulnerablePopulation(200L);
            rec.setTotalFeasibleCandidates(3);

            RecommendedDestinationDto primary = new RecommendedDestinationDto("SITE-01", "District High School", 0.9100, 1);
            primary.setDistrict("Sitamarhi");
            primary.setDistanceKilometers(2.5);
            primary.setAvailableCapacity(500);
            primary.setSuitabilityScore(95.0);
            rec.setPrimaryDestination(primary);

            DecisionRationaleDto rationale = engine.synthesizeRationale(priority, rec);

            assertNotNull(rationale);
            assertTrue(rationale.getWhoStatement().contains("Sonbarsa Village"));
            assertTrue(rationale.getWhoStatement().contains("Immediate Priority"));
            assertTrue(rationale.getWhereStatement().contains("District High School"));
            assertTrue(rationale.getWhereStatement().contains("2.50 km"));
            assertTrue(rationale.getWhyStatement().contains("IMMEDIATE priority"));
            assertTrue(rationale.getActionabilityGuidance().contains("DEPLOY IMMEDIATELY"));

            assertFalse(rationale.getKeyStrengths().isEmpty());
            assertFalse(rationale.getKeyRisksOrDeficits().isEmpty());
        }
    }

    @Nested
    @DisplayName("No Feasible Destination Rationale")
    class NoFeasibleRationaleTests {

        @Test
        @DisplayName("Synthesizes escalation guidance and deficit warnings when no destination is feasible")
        void testNoFeasibleRationale() {
            RelocationPriorityResultDto priority = new RelocationPriorityResultDto("HAB-01", 0.90, PriorityLevel.IMMEDIATE);
            priority.setHabitationName("Isolated Valley");
            priority.setVulnerablePopulation(300L);

            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.NO_FEASIBLE_DESTINATION);
            rec.setHabitationName("Isolated Valley");
            rec.setVulnerablePopulation(300L);
            rec.setTotalCandidatesEvaluated(4);
            rec.setPrimaryDestination(null);

            DecisionRationaleDto rationale = engine.synthesizeRationale(priority, rec);

            assertTrue(rationale.getWhereStatement().contains("No feasible emergency safe shelter"));
            assertTrue(rationale.getActionabilityGuidance().contains("ESCALATE"));
            assertTrue(rationale.getKeyRisksOrDeficits().stream().anyMatch(r -> r.contains("inter-district shelter requisition")));
        }
    }
}
