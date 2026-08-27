package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.explain.DecisionContributorDto;
import com.hazard.dto.relocation.explain.RiskExplanationDto;
import com.hazard.service.relocation.PriorityScoringConfig;
import com.hazard.service.relocation.explain.RiskExplanationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7C.2 — Risk Explanation Engine Tests.
 */
class RiskExplanationEngineTests {

    private RiskExplanationEngine engine;
    private PriorityScoringConfig config;

    @BeforeEach
    void setUp() {
        config = new PriorityScoringConfig();
        engine = new RiskExplanationEngine(config);
    }

    private RelocationPriorityResultDto createPriorityResult(String id, String name, double score,
                                                              PriorityLevel level, double risk, double hazard,
                                                              long pop, long unalloc, RelocationUrgency urgency, boolean redZone) {
        RelocationPriorityResultDto res = new RelocationPriorityResultDto(id, score, level);
        res.setHabitationName(name);
        res.setRiskScore(risk);
        res.setHazardSeverityScore(hazard);
        res.setVulnerablePopulation(pop);
        res.setUnallocatedPopulation(unalloc);
        res.setUrgency(urgency);
        res.setRedZone(redZone);
        res.setOverallStatus(unalloc > 0 ? "UNALLOCATED_NO_SAFE_SITE" : "ALLOCATED");

        Map<String, Double> contribs = new LinkedHashMap<>();
        contribs.put(PriorityScoringConfig.RISK_SEVERITY, risk);
        contribs.put(PriorityScoringConfig.HAZARD_SEVERITY, hazard);
        contribs.put(PriorityScoringConfig.POPULATION_EXPOSURE, 0.70);
        contribs.put(PriorityScoringConfig.CAPACITY_DEFICIT, unalloc > 0 ? 1.0 : 0.0);
        contribs.put(PriorityScoringConfig.ALLOCATION_FAILURE, unalloc > 0 ? 1.0 : 0.0);
        contribs.put(PriorityScoringConfig.URGENCY, urgency == RelocationUrgency.CRITICAL ? 1.0 : 0.67);
        res.setScoringContributors(contribs);

        return res;
    }

    @Nested
    @DisplayName("Immediate Priority Risk Explanations")
    class ImmediateRiskTests {

        @Test
        @DisplayName("Generates critical risk explanation and primary drivers for IMMEDIATE case")
        void testImmediateRiskExplanation() {
            RelocationPriorityResultDto res = createPriorityResult(
                    "HAB-01", "Sonbarsa Inundated Zone", 0.88, PriorityLevel.IMMEDIATE,
                    0.92, 0.85, 5000L, 5000L, RelocationUrgency.CRITICAL, true
            );

            RiskExplanationDto exp = engine.explainRisk(res);

            assertNotNull(exp);
            assertEquals("CRITICAL_IMMEDIATE", exp.getRiskCategory());
            assertEquals(PriorityLevel.IMMEDIATE, exp.getPriorityLevel());
            assertEquals(0.88, exp.getPriorityScore());
            assertTrue(exp.getRiskNarrative().contains("IMMEDIATE"));
            assertTrue(exp.getUrgencyContext().contains("Emergency action"));

            // Check 6 contributors
            assertEquals(6, exp.getContributors().size());

            // Check primary drivers
            assertFalse(exp.getPrimaryRiskDrivers().isEmpty());
            assertTrue(exp.getPrimaryRiskDrivers().get(0).contains("Multi-Hazard Risk")
                    || exp.getPrimaryRiskDrivers().get(0).contains("Capacity Deficit"));
        }
    }

    @Nested
    @DisplayName("Monitoring Low Risk Explanations")
    class MonitoringRiskTests {

        @Test
        @DisplayName("Generates monitoring narrative for low-risk case")
        void testMonitoringRiskExplanation() {
            RelocationPriorityResultDto res = createPriorityResult(
                    "HAB-SAFE", "Elevated Village", 0.08, PriorityLevel.MONITORING,
                    0.05, 0.05, 100L, 0L, RelocationUrgency.LOW, false
            );

            RiskExplanationDto exp = engine.explainRisk(res);

            assertEquals("MONITORING_LOW", exp.getRiskCategory());
            assertTrue(exp.getRiskNarrative().contains("MONITORING"));
            assertTrue(exp.getUrgencyContext().contains("No immediate evacuation needed"));
        }
    }

    @Nested
    @DisplayName("Null Safety")
    class NullSafetyTests {

        @Test
        @DisplayName("Null priority result returns safe baseline monitoring explanation")
        void testNullResultSafety() {
            RiskExplanationDto exp = engine.explainRisk(null);

            assertNotNull(exp);
            assertEquals(PriorityLevel.MONITORING, exp.getPriorityLevel());
            assertEquals("MONITORING_LOW", exp.getRiskCategory());
            assertEquals(0.0, exp.getPriorityScore());
        }
    }
}
