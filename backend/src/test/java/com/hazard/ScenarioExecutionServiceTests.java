package com.hazard;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.domain.scenario.ScenarioType;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.scenario.ScenarioCreateRequestDto;
import com.hazard.dto.scenario.ScenarioDto;
import com.hazard.dto.scenario.ScenarioExecutionRequestDto;
import com.hazard.dto.scenario.ScenarioSimulationResultDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.scenario.ScenarioRepository;
import com.hazard.service.risk.RiskCalculationEngine;
import com.hazard.service.risk.RiskCalculationService;
import com.hazard.service.risk.config.RiskConfigurationService;
import com.hazard.service.scenario.ScenarioExecutionService;
import com.hazard.service.scenario.ScenarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 9B — Unit and Integration Service Tests for Disaster Scenario Simulation Execution.
 */
@SpringBootTest
@Transactional
class ScenarioExecutionServiceTests {

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private ScenarioExecutionService scenarioExecutionService;

    @Autowired
    private RiskCalculationService riskCalculationService;

    @Autowired
    private DistrictBoundaryRepository districtBoundaryRepository;

    @BeforeEach
    void setUp() {
        scenarioRepository.resetToBaselineOnly();
    }

    // =========================================================================
    // 1. BASELINE SCENARIO EXECUTION TESTS
    // =========================================================================

    @Nested
    @DisplayName("1. Baseline Scenario Execution & Zero-Perturbation Consistency")
    class BaselineExecutionTests {

        @Test
        @DisplayName("Executing SCEN-BASELINE produces 0.0 delta and exact match with production baseline risk")
        void testBaselineExecutionProducesExactMatch() {
            DistrictRiskScoreDto baselineActual = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);

            ScenarioExecutionRequestDto req = new ScenarioExecutionRequestDto("Sitamarhi");
            ScenarioSimulationResultDto result = scenarioExecutionService.executeScenario(
                    ScenarioDefinition.BASELINE_SCENARIO_ID, req);

            assertNotNull(result);
            assertEquals(ScenarioDefinition.BASELINE_SCENARIO_ID, result.getScenarioId());
            assertEquals("Baseline Scenario", result.getScenarioName());
            assertEquals(ScenarioType.BASELINE, result.getScenarioType());
            assertEquals("Sitamarhi", result.getDistrictName());
            assertEquals(0.0, result.getAppliedRainfallChange());
            assertEquals(0.0, result.getAppliedHazardIntensityChange());
            assertEquals(0.0, result.getAppliedPopulationExposureChange());

            // Deltas must be exactly zero
            assertEquals(0.0, result.getDeltaRiskScore(), 0.0001);
            assertEquals(0.0, result.getDeltaRiskScore100(), 0.0001);
            assertEquals("UNCHANGED", result.getRiskDirection());

            // Scores match baseline exactly
            assertEquals(baselineActual.getRiskScore(), result.getSimulatedRisk().getRiskScore(), 0.0001);
            assertEquals(baselineActual.getRiskScore100(), result.getSimulatedRisk().getRiskScore100(), 0.0001);
            assertEquals(baselineActual.getRiskTier(), result.getSimulatedRisk().getRiskTier());
        }
    }

    // =========================================================================
    // 2. SCENARIO PERTURBATION EXECUTION BY TYPE TESTS
    // =========================================================================

    @Nested
    @DisplayName("2. Perturbation Scenario Execution by Type")
    class ScenarioPerturbationExecutionTests {

        @Test
        @DisplayName("RAINFALL_CHANGE (+20%) increases simulated hazard score and composite risk score")
        void testRainfallIncreaseExecution() {
            ScenarioDto scenario = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Monsoon Surge +20%", "RAINFALL_CHANGE", "Rainfall perturbation test", 20.0, 0.0, 0.0
            ));

            DistrictRiskScoreDto baseline = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);
            ScenarioSimulationResultDto result = scenarioExecutionService.executeScenario(
                    scenario.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
            );

            assertNotNull(result);
            assertEquals(20.0, result.getAppliedRainfallChange());
            assertEquals(1.20, result.getSimulationContext().getEffectiveHazardMultiplier(), 0.001);

            // Simulated hazard score must be higher than baseline
            assertTrue(result.getSimulationContext().getSimulatedHazardScore() > result.getSimulationContext().getBaselineHazardScore());

            // Simulated risk score must increase
            assertTrue(result.getSimulatedRisk().getRiskScore() > baseline.getRiskScore());
            assertTrue(result.getDeltaRiskScore() > 0);
            assertEquals("INCREASED", result.getRiskDirection());
        }

        @Test
        @DisplayName("HAZARD_INTENSITY (+15%) scales simulated hazard score and composite risk score")
        void testHazardIntensityExecution() {
            ScenarioDto scenario = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Embankment Breach Spike +15%", "HAZARD_INTENSITY", "Hazard intensity scaling test", 0.0, 15.0, 0.0
            ));

            DistrictRiskScoreDto baseline = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);
            ScenarioSimulationResultDto result = scenarioExecutionService.executeScenario(
                    scenario.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
            );

            assertNotNull(result);
            assertEquals(15.0, result.getAppliedHazardIntensityChange());
            assertEquals(1.15, result.getSimulationContext().getEffectiveHazardMultiplier(), 0.001);
            assertTrue(result.getSimulatedRisk().getRiskScore() > baseline.getRiskScore());
            assertEquals("INCREASED", result.getRiskDirection());
        }

        @Test
        @DisplayName("POPULATION_EXPOSURE (+30%) scales exposed population count and increases composite risk")
        void testPopulationExposureExecution() {
            ScenarioDto scenario = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Floodplain Encroachment +30%", "POPULATION_EXPOSURE", "Pop exposure expansion test", 0.0, 0.0, 30.0
            ));

            DistrictRiskScoreDto baseline = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);
            ScenarioSimulationResultDto result = scenarioExecutionService.executeScenario(
                    scenario.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
            );

            assertNotNull(result);
            assertEquals(30.0, result.getAppliedPopulationExposureChange());
            assertEquals(1.30, result.getSimulationContext().getEffectivePopulationMultiplier(), 0.001);

            // Exposed population count scaled by 1.30
            long basePop = result.getSimulationContext().getBaselineExposedPopulation();
            long simPop = result.getSimulationContext().getSimulatedExposedPopulation();
            assertEquals(Math.round(basePop * 1.30), simPop);

            // Risk increased
            assertTrue(result.getSimulatedRisk().getRiskScore() > baseline.getRiskScore());
            assertEquals("INCREASED", result.getRiskDirection());
        }

        @Test
        @DisplayName("MULTI_FACTOR (Rain +20%, Hazard +10%, Pop +15%) compounds all parameter deltas")
        void testMultiFactorExecution() {
            ScenarioDto scenario = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Compound Catastrophe", "MULTI_FACTOR", "Multi-factor compound test", 20.0, 10.0, 15.0
            ));

            ScenarioSimulationResultDto result = scenarioExecutionService.executeScenario(
                    scenario.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
            );

            assertNotNull(result);
            assertEquals(20.0, result.getAppliedRainfallChange());
            assertEquals(10.0, result.getAppliedHazardIntensityChange());
            assertEquals(15.0, result.getAppliedPopulationExposureChange());

            // Hazard multiplier = 1.20 * 1.10 = 1.32
            assertEquals(1.32, result.getSimulationContext().getEffectiveHazardMultiplier(), 0.001);
            assertEquals(1.15, result.getSimulationContext().getEffectivePopulationMultiplier(), 0.001);

            assertTrue(result.getDeltaRiskScore() > 0.0);
            assertEquals("INCREASED", result.getRiskDirection());
        }
    }

    // =========================================================================
    // 3. IMMUTABILITY & ZERO PRODUCTION MUTATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("3. Zero-Mutation Immutability & Determinism Guarantees")
    class ZeroMutationAndDeterminismTests {

        @Test
        @DisplayName("CRITICAL: Executing extreme scenario does NOT mutate stored baseline or database state")
        void testZeroMutationOfBaselineAndDatabase() {
            // 1. Capture baseline risk and database state BEFORE execution
            DistrictRiskScoreDto baselineBefore = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);
            ScenarioDto baselineScenarioBefore = scenarioService.getBaselineScenario();
            long districtCountBefore = districtBoundaryRepository.count();
            long scenarioCountBefore = scenarioRepository.count();

            // 2. Create and execute an extreme perturbation scenario (+50% rain, +50% hazard, +50% pop)
            ScenarioDto extremeScenario = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Extreme 50% Shift", "MULTI_FACTOR", "Testing immutability", 50.0, 50.0, 50.0
            ));

            ScenarioSimulationResultDto simResult = scenarioExecutionService.executeScenario(
                    extremeScenario.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
            );

            assertNotNull(simResult);
            assertTrue(simResult.getDeltaRiskScore() > 0.0);

            // 3. Capture baseline risk and database state AFTER execution
            DistrictRiskScoreDto baselineAfter = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);
            ScenarioDto baselineScenarioAfter = scenarioService.getBaselineScenario();
            long districtCountAfter = districtBoundaryRepository.count();

            // 4. Assert total equality / zero-mutation
            assertEquals(baselineBefore.getRiskScore(), baselineAfter.getRiskScore(), 0.00001, "Production risk score was mutated!");
            assertEquals(baselineBefore.getRiskScore100(), baselineAfter.getRiskScore100(), 0.00001, "Production riskScore100 was mutated!");
            assertEquals(baselineBefore.getRiskTier(), baselineAfter.getRiskTier(), "Production risk tier was mutated!");

            assertEquals(baselineScenarioBefore.getRainfallChange(), baselineScenarioAfter.getRainfallChange(), 0.00001);
            assertEquals(baselineScenarioBefore.getHazardIntensityChange(), baselineScenarioAfter.getHazardIntensityChange(), 0.00001);
            assertEquals(baselineScenarioBefore.getPopulationExposureChange(), baselineScenarioAfter.getPopulationExposureChange(), 0.00001);

            assertEquals(districtCountBefore, districtCountAfter, "District boundaries table was mutated!");
        }

        @Test
        @DisplayName("Multiple executions across different scenarios are independent and deterministic")
        void testExecutionDeterminismAndIndependence() {
            ScenarioDto scenA = scenarioService.createScenario(new ScenarioCreateRequestDto("Scenario A", "RAINFALL_CHANGE", "Desc", 25.0, 0.0, 0.0));
            ScenarioDto scenB = scenarioService.createScenario(new ScenarioCreateRequestDto("Scenario B", "POPULATION_EXPOSURE", "Desc", 0.0, 0.0, 40.0));

            // Execute A, then B, then A again
            ScenarioSimulationResultDto resultA1 = scenarioExecutionService.executeScenario(scenA.getScenarioId(), new ScenarioExecutionRequestDto("Patna"));
            ScenarioSimulationResultDto resultB = scenarioExecutionService.executeScenario(scenB.getScenarioId(), new ScenarioExecutionRequestDto("Patna"));
            ScenarioSimulationResultDto resultA2 = scenarioExecutionService.executeScenario(scenA.getScenarioId(), new ScenarioExecutionRequestDto("Patna"));

            // A1 and A2 must be identical
            assertEquals(resultA1.getSimulatedRisk().getRiskScore(), resultA2.getSimulatedRisk().getRiskScore(), 0.00001);
            assertEquals(resultA1.getDeltaRiskScore(), resultA2.getDeltaRiskScore(), 0.00001);

            // B must be independent
            assertNotEquals(resultA1.getSimulatedRisk().getRiskScore(), resultB.getSimulatedRisk().getRiskScore());
        }
    }

    // =========================================================================
    // 4. VALIDATION & ERROR HANDLING TESTS
    // =========================================================================

    @Nested
    @DisplayName("4. Validation & Error Handling")
    class ValidationAndErrorHandlingTests {

        @Test
        @DisplayName("Executing non-existent scenario throws HazardNotFoundException (404)")
        void testMissingScenarioThrows() {
            assertThrows(HazardNotFoundException.class, () ->
                    scenarioExecutionService.executeScenario("NON-EXISTENT-SCEN-999", new ScenarioExecutionRequestDto("Sitamarhi"))
            );
        }

        @Test
        @DisplayName("Executing for non-existent district throws HazardNotFoundException (404)")
        void testMissingDistrictThrows() {
            assertThrows(HazardNotFoundException.class, () ->
                    scenarioExecutionService.executeScenario(ScenarioDefinition.BASELINE_SCENARIO_ID, new ScenarioExecutionRequestDto("AtlantisDistrict"))
            );
        }

        @Test
        @DisplayName("Executing with null or blank scenarioId throws InvalidHazardParameterException (400)")
        void testBlankScenarioIdThrows() {
            assertThrows(InvalidHazardParameterException.class, () ->
                    scenarioExecutionService.executeScenario(null, new ScenarioExecutionRequestDto("Sitamarhi"))
            );
            assertThrows(InvalidHazardParameterException.class, () ->
                    scenarioExecutionService.executeScenario("   ", new ScenarioExecutionRequestDto("Sitamarhi"))
            );
        }

        @Test
        @DisplayName("executeScenarioAllDistricts executes across all 38 districts of Bihar")
        void testExecuteAllDistricts() {
            List<ScenarioSimulationResultDto> allResults = scenarioExecutionService.executeScenarioAllDistricts(ScenarioDefinition.BASELINE_SCENARIO_ID);

            assertNotNull(allResults);
            assertEquals(38, allResults.size());
            assertTrue(allResults.stream().allMatch(r -> ScenarioDefinition.BASELINE_SCENARIO_ID.equals(r.getScenarioId())));
        }
    }
}
