package com.hazard;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.risk.ZoneLevel;
import com.hazard.domain.scenario.RedZoneTransitionType;
import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.domain.scenario.ScenarioType;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.RedZoneDto;
import com.hazard.dto.scenario.*;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.scenario.ScenarioRepository;
import com.hazard.service.risk.RedZoneService;
import com.hazard.service.risk.RiskCalculationService;
import com.hazard.service.scenario.ScenarioRedZoneService;
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
 * Stage 9C — Unit and Integration Service Tests for Dynamic Red-Zone Recalculation.
 */
@SpringBootTest
@Transactional
class ScenarioRedZoneServiceTests {

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private ScenarioRedZoneService scenarioRedZoneService;

    @Autowired
    private RedZoneService redZoneService;

    @Autowired
    private RiskCalculationService riskCalculationService;

    @Autowired
    private DistrictBoundaryRepository districtBoundaryRepository;

    @BeforeEach
    void setUp() {
        scenarioRepository.resetToBaselineOnly();
    }

    // =========================================================================
    // 1. BASELINE CONSISTENCY TESTS
    // =========================================================================

    @Nested
    @DisplayName("1. Baseline Red-Zone Consistency")
    class BaselineConsistencyTests {

        @Test
        @DisplayName("Executing SCEN-BASELINE produces 100% agreement with standard Stage 5.1 RedZoneService")
        void testBaselineScenarioConsistency() {
            ScenarioRedZoneSimulationResultDto simResult = scenarioRedZoneService.recalculateRedZonesAllDistricts(
                    ScenarioDefinition.BASELINE_SCENARIO_ID);

            assertNotNull(simResult);
            assertEquals(ScenarioDefinition.BASELINE_SCENARIO_ID, simResult.getScenarioId());
            assertEquals(ScenarioType.BASELINE, simResult.getScenarioType());

            // Counts
            assertEquals(simResult.getBaselineRedZoneCount(), simResult.getSimulatedRedZoneCount());
            assertEquals(0, simResult.getNetRedZoneChange());
            assertEquals(0, simResult.getNewlyEnteredRedZoneCount());
            assertEquals(0, simResult.getLeftRedZoneCount());
            assertEquals(simResult.getBaselineRedZoneCount(), simResult.getRetainedRedZoneCount());
            assertEquals(simResult.getTotalDistrictsEvaluated() - simResult.getBaselineRedZoneCount(), simResult.getUnchangedNonRedZoneCount());

            // Every district must have NO shift
            for (DistrictRedZoneSimulationDto d : simResult.getDistrictResults()) {
                assertEquals(d.isBaselineRedZone(), d.isSimulatedRedZone());
                assertTrue(
                        d.getTransitionType() == RedZoneTransitionType.UNCHANGED_NON_RED_ZONE ||
                        d.getTransitionType() == RedZoneTransitionType.RETAINED_RED_ZONE
                );
            }
        }
    }

    // =========================================================================
    // 2. RED-ZONE TRANSITION TESTS (NO -> YES, YES -> NO, UNCHANGED)
    // =========================================================================

    @Nested
    @DisplayName("2. Red-Zone Transition Shifts")
    class RedZoneTransitionTests {

        @Test
        @DisplayName("Extreme elevated scenario pushes non-red district into Red Zone (NO -> YES: ENTERED_RED_ZONE)")
        void testNewlyEnteredRedZoneTransition() {
            // Create a +60% multi-factor catastrophe scenario
            ScenarioDto catastrophe = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Super Catastrophe +60%", "MULTI_FACTOR", "Testing Red Zone expansion", 60.0, 40.0, 30.0
            ));

            DistrictRedZoneSimulationDto districtDto = scenarioRedZoneService.recalculateDistrictRedZone(
                    catastrophe.getScenarioId(), "Patna"
            );

            assertNotNull(districtDto);
            // Patna with +60% will exceed 0.60 threshold
            assertTrue(districtDto.getSimulatedRiskScore() >= 0.60);
            assertTrue(districtDto.isSimulatedRedZone());
            assertTrue(districtDto.getDeltaRiskScore() > 0.0);
            assertTrue(districtDto.getTransitionType() == RedZoneTransitionType.ENTERED_RED_ZONE ||
                       districtDto.getTransitionType() == RedZoneTransitionType.RETAINED_RED_ZONE);
        }

        @Test
        @DisplayName("Severe negative scenario allows red-zone district to drop below threshold (YES -> NO: LEFT_RED_ZONE)")
        void testLeftRedZoneTransition() {
            // Create massive mitigation scenario (-70% rainfall, -70% hazard)
            ScenarioDto massiveMitigation = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Massive Mitigation -70%", "MULTI_FACTOR", "Testing Red Zone exit", -70.0, -70.0, 0.0
            ));

            DistrictRedZoneSimulationDto districtSim = scenarioRedZoneService.recalculateDistrictRedZone(
                    massiveMitigation.getScenarioId(), "Patna"
            );

            assertNotNull(districtSim);
            assertTrue(districtSim.getSimulatedRiskScore() < 0.60);
            assertFalse(districtSim.isSimulatedRedZone());
            assertTrue(districtSim.getDeltaRiskScore() < 0.0);
            assertTrue(districtSim.getTransitionType() == RedZoneTransitionType.LEFT_RED_ZONE ||
                       districtSim.getTransitionType() == RedZoneTransitionType.UNCHANGED_NON_RED_ZONE);
        }

        @Test
        @DisplayName("Low-risk district in mild scenario remains UNCHANGED_NON_RED_ZONE")
        void testUnchangedNonRedZone() {
            ScenarioDto mild = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Mild +5%", "RAINFALL_CHANGE", "Mild test", 5.0, 0.0, 0.0
            ));

            DistrictRedZoneSimulationDto districtSim = scenarioRedZoneService.recalculateDistrictRedZone(
                    mild.getScenarioId(), "Sitamarhi"
            );

            assertNotNull(districtSim);
            assertFalse(districtSim.isBaselineRedZone());
            assertFalse(districtSim.isSimulatedRedZone());
            assertEquals(RedZoneTransitionType.UNCHANGED_NON_RED_ZONE, districtSim.getTransitionType());
        }
    }

    // =========================================================================
    // 3. THRESHOLD BOUNDARY BEHAVIOR TESTS
    // =========================================================================

    @Nested
    @DisplayName("3. Exact Threshold Boundary Verification")
    class ThresholdBoundaryTests {

        @Test
        @DisplayName("RiskTier and ZoneLevel classify >=0.60 as Red Zone, <0.60 as Non-Red Zone")
        void testThresholdBoundaryExactValues() {
            // Score = 0.6000 -> VERY_HIGH -> CRITICAL Zone -> Red Zone TRUE
            RiskTier tierAtThreshold = RiskTier.fromScore(0.6000);
            assertEquals(RiskTier.VERY_HIGH, tierAtThreshold);
            ZoneLevel zoneAtThreshold = ZoneLevel.fromRiskTier(tierAtThreshold);
            assertEquals(ZoneLevel.CRITICAL, zoneAtThreshold);
            assertTrue(zoneAtThreshold.isRedZone());

            // Score = 0.5999 -> HIGH -> HIGH Zone -> Red Zone FALSE
            RiskTier tierBelowThreshold = RiskTier.fromScore(0.5999);
            assertEquals(RiskTier.HIGH, tierBelowThreshold);
            ZoneLevel zoneBelowThreshold = ZoneLevel.fromRiskTier(tierBelowThreshold);
            assertEquals(ZoneLevel.HIGH, zoneBelowThreshold);
            assertFalse(zoneBelowThreshold.isRedZone());

            // Transition enum helpers
            assertEquals(RedZoneTransitionType.UNCHANGED_NON_RED_ZONE, RedZoneTransitionType.from(false, false));
            assertEquals(RedZoneTransitionType.ENTERED_RED_ZONE, RedZoneTransitionType.from(false, true));
            assertEquals(RedZoneTransitionType.LEFT_RED_ZONE, RedZoneTransitionType.from(true, false));
            assertEquals(RedZoneTransitionType.RETAINED_RED_ZONE, RedZoneTransitionType.from(true, true));
        }
    }

    // =========================================================================
    // 4. MATHEMATICAL AGGREGATION INTEGRITY TESTS
    // =========================================================================

    @Nested
    @DisplayName("4. State-Wide Aggregation Integrity")
    class AggregationIntegrityTests {

        @Test
        @DisplayName("All-district aggregate counts sum up correctly")
        void testAggregateCountsSumUp() {
            ScenarioDto scenario = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Test Catastrophe +35%", "MULTI_FACTOR", "Testing counts", 35.0, 20.0, 10.0
            ));

            ScenarioRedZoneSimulationResultDto result = scenarioRedZoneService.recalculateRedZonesAllDistricts(
                    scenario.getScenarioId());

            assertNotNull(result);
            int total = result.getTotalDistrictsEvaluated();
            assertTrue(total > 0);

            // 1. Total must equal sum of all 4 transition types
            assertEquals(total,
                    result.getNewlyEnteredRedZoneCount() +
                    result.getLeftRedZoneCount() +
                    result.getRetainedRedZoneCount() +
                    result.getUnchangedNonRedZoneCount()
            );

            // 2. Baseline Red count must equal retained + left
            assertEquals(result.getBaselineRedZoneCount(),
                    result.getRetainedRedZoneCount() + result.getLeftRedZoneCount()
            );

            // 3. Simulated Red count must equal retained + newly entered
            assertEquals(result.getSimulatedRedZoneCount(),
                    result.getRetainedRedZoneCount() + result.getNewlyEnteredRedZoneCount()
            );

            // 4. Net change
            assertEquals(result.getSimulatedRedZoneCount() - result.getBaselineRedZoneCount(),
                    result.getNetRedZoneChange()
            );

            // 5. List sizes match counts
            assertEquals(result.getNewlyEnteredRedZoneCount(), result.getNewlyEnteredDistricts().size());
            assertEquals(result.getLeftRedZoneCount(), result.getLeftRedZoneDistricts().size());
            assertEquals(result.getRetainedRedZoneCount(), result.getRetainedRedZoneDistricts().size());
            assertEquals(result.getUnchangedNonRedZoneCount(), result.getUnchangedNonRedZoneDistricts().size());
            assertEquals(total, result.getDistrictResults().size());
        }
    }

    // =========================================================================
    // 5. ZERO MUTATION & DETERMINISM TESTS
    // =========================================================================

    @Nested
    @DisplayName("5. Zero Mutation & Determinism")
    class ZeroMutationAndDeterminismTests {

        @Test
        @DisplayName("CRITICAL: Recalculating Red Zones does NOT mutate stored baseline or database state")
        void testZeroMutationOfBaselineAndDatabase() {
            // Capture state before
            DistrictRiskScoreDto baselineRiskBefore = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);
            long districtCountBefore = districtBoundaryRepository.count();

            // Run extreme Red-Zone simulation
            ScenarioDto extremeScenario = scenarioService.createScenario(new ScenarioCreateRequestDto(
                    "Extreme Red Shift", "MULTI_FACTOR", "Desc", 50.0, 50.0, 50.0
            ));
            DistrictRedZoneSimulationDto simResult = scenarioRedZoneService.recalculateDistrictRedZone(
                    extremeScenario.getScenarioId(), "Sitamarhi");

            assertNotNull(simResult);

            // Capture state after
            DistrictRiskScoreDto baselineRiskAfter = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);
            long districtCountAfter = districtBoundaryRepository.count();

            // Assert total equality
            assertEquals(baselineRiskBefore.getRiskScore(), baselineRiskAfter.getRiskScore(), 0.00001);
            assertEquals(baselineRiskBefore.getRiskScore100(), baselineRiskAfter.getRiskScore100(), 0.00001);
            assertEquals(baselineRiskBefore.getRiskTier(), baselineRiskAfter.getRiskTier());
            assertEquals(districtCountBefore, districtCountAfter);
        }

        @Test
        @DisplayName("Repeated executions across scenarios are independent and deterministic")
        void testRepeatedExecutionDeterminism() {
            ScenarioDto scenA = scenarioService.createScenario(new ScenarioCreateRequestDto("A", "RAINFALL_CHANGE", "Desc", 30.0, 0.0, 0.0));
            ScenarioDto scenB = scenarioService.createScenario(new ScenarioCreateRequestDto("B", "POPULATION_EXPOSURE", "Desc", 0.0, 0.0, 40.0));

            DistrictRedZoneSimulationDto resA1 = scenarioRedZoneService.recalculateDistrictRedZone(scenA.getScenarioId(), "Patna");
            DistrictRedZoneSimulationDto resB = scenarioRedZoneService.recalculateDistrictRedZone(scenB.getScenarioId(), "Patna");
            DistrictRedZoneSimulationDto resA2 = scenarioRedZoneService.recalculateDistrictRedZone(scenA.getScenarioId(), "Patna");

            assertEquals(resA1.getSimulatedRiskScore(), resA2.getSimulatedRiskScore(), 0.00001);
            assertEquals(resA1.getTransitionType(), resA2.getTransitionType());
            assertNotEquals(resA1.getSimulatedRiskScore(), resB.getSimulatedRiskScore());
        }
    }

    // =========================================================================
    // 6. VALIDATION & ERROR HANDLING TESTS
    // =========================================================================

    @Nested
    @DisplayName("6. Validation & Error Handling")
    class ValidationAndErrorHandlingTests {

        @Test
        @DisplayName("Recalculating Red Zones for non-existent scenario throws HazardNotFoundException (404)")
        void testMissingScenarioThrows() {
            assertThrows(HazardNotFoundException.class, () ->
                    scenarioRedZoneService.recalculateDistrictRedZone("NON-EXISTENT-SCEN-999", "Sitamarhi")
            );
        }

        @Test
        @DisplayName("Recalculating Red Zones with null or blank scenarioId throws InvalidHazardParameterException (400)")
        void testBlankScenarioIdThrows() {
            assertThrows(InvalidHazardParameterException.class, () ->
                    scenarioRedZoneService.recalculateDistrictRedZone(null, "Sitamarhi")
            );
            assertThrows(InvalidHazardParameterException.class, () ->
                    scenarioRedZoneService.recalculateDistrictRedZone("   ", "Sitamarhi")
            );
        }

        @Test
        @DisplayName("Recalculating for invalid district throws HazardNotFoundException (404)")
        void testMissingDistrictThrows() {
            assertThrows(HazardNotFoundException.class, () ->
                    scenarioRedZoneService.recalculateDistrictRedZone(ScenarioDefinition.BASELINE_SCENARIO_ID, "NonExistentDistrict")
            );
        }
    }
}
