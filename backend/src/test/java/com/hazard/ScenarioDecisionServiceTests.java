package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.scenario.RedZoneTransitionType;
import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.domain.scenario.ScenarioType;
import com.hazard.dto.relocation.RelocationPlanDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.scenario.*;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.scenario.ScenarioRepository;
import com.hazard.service.risk.RiskCalculationService;
import com.hazard.service.scenario.ScenarioDecisionService;
import com.hazard.service.scenario.ScenarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 9D — Unit and Integration Service Tests for Priority & Relocation Recalculation.
 */
@SpringBootTest
@Transactional
class ScenarioDecisionServiceTests {

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private ScenarioDecisionService scenarioDecisionService;

    @Autowired
    private RiskCalculationService riskCalculationService;

    @Autowired
    private DistrictBoundaryRepository districtBoundaryRepository;

    @BeforeEach
    void setUp() {
        scenarioRepository.resetToBaselineOnly();
    }

    // =========================================================================
    // 1. BASELINE SCENARIO CONSISTENCY TESTS
    // =========================================================================

    @Nested
    @DisplayName("1. Baseline Scenario Consistency")
    class BaselineConsistencyTests {

        @Test
        @DisplayName("Executing SCEN-BASELINE produces zero priority delta and identical relocation outcome")
        void testBaselineDecisionConsistency() {
            DistrictDecisionSimulationDto result = scenarioDecisionService.recalculateDistrictDecision(
                    ScenarioDefinition.BASELINE_SCENARIO_ID, "Sitamarhi"
            );

            assertNotNull(result);
            assertEquals("Sitamarhi", result.getDistrictName());
            assertEquals(0.0, result.getDeltaPriorityScore(), 0.0001);
            assertEquals(result.getBaselinePriorityScore(), result.getSimulatedPriorityScore(), 0.0001);
            assertEquals(result.getBaselinePriorityLevel(), result.getSimulatedPriorityLevel());
            assertEquals("UNCHANGED", result.getPriorityShiftDirection());

            assertEquals(result.getBaselineAllocatedPopulation(), result.getSimulatedAllocatedPopulation());
            assertEquals(result.getBaselineUnallocatedPopulation(), result.getSimulatedUnallocatedPopulation());
            assertEquals(result.getBaselineRelocationStatus(), result.getSimulatedRelocationStatus());
            assertEquals(RedZoneTransitionType.UNCHANGED_NON_RED_ZONE, result.getRedZoneTransitionType());
        }
    }

    // =========================================================================
    // 2. SCENARIO CHANGING PRIORITY & RED ZONE TESTS
    // =========================================================================

    @Nested
    @DisplayName("2. Priority Escalation & Red-Zone Transitions")
    class PriorityEscalationTests {

        @Test
        @DisplayName("Extreme catastrophe scenario (+60% rain, +40% hazard) escalates priority score")
        void testCatastropheEscalatesPriority() {
            ScenarioDto catastrophe = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Catastrophe +60%", "MULTI_FACTOR", "Testing priority surge", 60.0, 40.0, 30.0
            ));

            DistrictDecisionSimulationDto result = scenarioDecisionService.recalculateDistrictDecision(
                    catastrophe.getScenarioId(), "Patna"
            );

            assertNotNull(result);
            assertTrue(result.getSimulatedPriorityScore() >= result.getBaselinePriorityScore());
            assertTrue(result.getDeltaPriorityScore() >= 0.0);
            assertEquals("INCREASED", result.getPriorityShiftDirection());
            assertNotNull(result.getSimulatedPriorityResult());
            assertTrue(result.getSimulatedPriorityResult().getPriorityScore() > 0.0);
        }

        @Test
        @DisplayName("Mitigation scenario (-50% rain, -50% hazard) reduces priority score")
        void testMitigationDeEscalatesPriority() {
            ScenarioDto mitigation = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Mitigation -50%", "MULTI_FACTOR", "Testing priority decrease", -50.0, -50.0, 0.0
            ));

            DistrictDecisionSimulationDto result = scenarioDecisionService.recalculateDistrictDecision(
                    mitigation.getScenarioId(), "Sitamarhi"
            );

            assertNotNull(result);
            assertTrue(result.getSimulatedPriorityScore() <= result.getBaselinePriorityScore());
            assertTrue(result.getDeltaPriorityScore() <= 0.0);
            assertTrue("DECREASED".equals(result.getPriorityShiftDirection()) || "UNCHANGED".equals(result.getPriorityShiftDirection()));
        }
    }

    // =========================================================================
    // 3. RELOCATION CAPACITY & POPULATION DEFICIT TESTS
    // =========================================================================

    @Nested
    @DisplayName("3. Relocation Capacity & Deficit Reporting")
    class RelocationCapacityTests {

        @Test
        @DisplayName("Population surge (+50%) increases relocation demand and reports capacity allocation")
        void testPopulationSurgeAffectsRelocation() {
            ScenarioDto popSurge = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Population Surge +50%", "POPULATION_EXPOSURE", "Testing relocation deficit", 0.0, 0.0, 50.0
            ));

            DistrictDecisionSimulationDto result = scenarioDecisionService.recalculateDistrictDecision(
                    popSurge.getScenarioId(), "Sitamarhi"
            );

            assertNotNull(result);
            assertNotNull(result.getSimulatedRelocationPlan());
            assertTrue(result.getVulnerablePopulation() > 0);
            assertNotNull(result.getSimulatedAllocatedPopulation());
            assertNotNull(result.getSimulatedUnallocatedPopulation());
            assertEquals(result.getVulnerablePopulation(),
                    result.getSimulatedAllocatedPopulation() + result.getSimulatedUnallocatedPopulation());
        }
    }

    // =========================================================================
    // 4. ZERO MUTATION & DETERMINISM TESTS
    // =========================================================================

    @Nested
    @DisplayName("4. Zero Mutation & Determinism")
    class ZeroMutationAndDeterminismTests {

        @Test
        @DisplayName("CRITICAL: Executing Stage 9D decision does NOT mutate stored baseline or database state")
        void testZeroMutationOfBaselineAndDatabase() {
            DistrictRiskScoreDto baselineRiskBefore = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);
            long districtCountBefore = districtBoundaryRepository.count();

            ScenarioDto extremeScenario = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Extreme Catastrophe", "MULTI_FACTOR", "Testing zero mutation", 50.0, 50.0, 50.0
            ));

            DistrictDecisionSimulationDto simResult = scenarioDecisionService.recalculateDistrictDecision(
                    extremeScenario.getScenarioId(), "Sitamarhi"
            );

            assertNotNull(simResult);

            DistrictRiskScoreDto baselineRiskAfter = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);
            long districtCountAfter = districtBoundaryRepository.count();

            assertEquals(baselineRiskBefore.getRiskScore(), baselineRiskAfter.getRiskScore(), 0.00001);
            assertEquals(baselineRiskBefore.getRiskScore100(), baselineRiskAfter.getRiskScore100(), 0.00001);
            assertEquals(districtCountBefore, districtCountAfter);
        }

        @Test
        @DisplayName("Repeated executions across scenarios are deterministic and independent")
        void testRepeatedExecutionDeterminism() {
            ScenarioDto scenA = scenarioService.createScenario(new ScenarioCreateRequestDto("A", "RAINFALL_CHANGE", "Desc", 20.0, 0.0, 0.0));
            ScenarioDto scenB = scenarioService.createScenario(new ScenarioCreateRequestDto("B", "POPULATION_EXPOSURE", "Desc", 0.0, 0.0, 30.0));

            DistrictDecisionSimulationDto resA1 = scenarioDecisionService.recalculateDistrictDecision(scenA.getScenarioId(), "Sitamarhi");
            DistrictDecisionSimulationDto resB = scenarioDecisionService.recalculateDistrictDecision(scenB.getScenarioId(), "Sitamarhi");
            DistrictDecisionSimulationDto resA2 = scenarioDecisionService.recalculateDistrictDecision(scenA.getScenarioId(), "Sitamarhi");

            assertEquals(resA1.getSimulatedPriorityScore(), resA2.getSimulatedPriorityScore(), 0.00001);
            assertEquals(resA1.getSimulatedPriorityLevel(), resA2.getSimulatedPriorityLevel());
            assertEquals(resA1.getSimulatedAllocatedPopulation(), resA2.getSimulatedAllocatedPopulation());
        }
    }

    // =========================================================================
    // 5. VALIDATION & ERROR HANDLING TESTS
    // =========================================================================

    @Nested
    @DisplayName("5. Validation & Error Handling")
    class ValidationAndErrorHandlingTests {

        @Test
        @DisplayName("Executing decision for non-existent scenario throws HazardNotFoundException (404)")
        void testMissingScenarioThrows() {
            assertThrows(HazardNotFoundException.class, () ->
                    scenarioDecisionService.recalculateDistrictDecision("NON-EXISTENT-SCEN-999", "Sitamarhi")
            );
        }

        @Test
        @DisplayName("Executing decision for non-existent district throws HazardNotFoundException (404)")
        void testMissingDistrictThrows() {
            assertThrows(HazardNotFoundException.class, () ->
                    scenarioDecisionService.recalculateDistrictDecision(ScenarioDefinition.BASELINE_SCENARIO_ID, "AtlantisDistrict")
            );
        }

        @Test
        @DisplayName("Executing decision with null or blank scenarioId throws InvalidHazardParameterException (400)")
        void testBlankScenarioIdThrows() {
            assertThrows(InvalidHazardParameterException.class, () ->
                    scenarioDecisionService.recalculateDistrictDecision(null, "Sitamarhi")
            );
            assertThrows(InvalidHazardParameterException.class, () ->
                    scenarioDecisionService.recalculateDistrictDecision("   ", "Sitamarhi")
            );
        }
    }
}
