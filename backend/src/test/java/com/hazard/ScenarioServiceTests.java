package com.hazard;

import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.domain.scenario.ScenarioType;
import com.hazard.dto.scenario.ScenarioCreateRequestDto;
import com.hazard.dto.scenario.ScenarioDto;
import com.hazard.dto.scenario.ScenarioTypeInfoDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.scenario.ScenarioRepository;
import com.hazard.service.scenario.ScenarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 9A — Unit and Service Tests for Scenario Creation and Validation.
 */
class ScenarioServiceTests {

    private ScenarioRepository scenarioRepository;
    private ScenarioService scenarioService;

    @BeforeEach
    void setUp() {
        scenarioRepository = new ScenarioRepository();
        scenarioService = new ScenarioService(scenarioRepository);
    }

    // =========================================================================
    // 1. BASELINE SCENARIO TESTS
    // =========================================================================

    @Nested
    @DisplayName("1. Baseline Scenario Representation & Integrity")
    class BaselineScenarioTests {

        @Test
        @DisplayName("getBaselineScenario returns unperturbed reference parameters (all deltas = 0.0)")
        void testGetBaselineScenario() {
            ScenarioDto baseline = scenarioService.getBaselineScenario();

            assertNotNull(baseline);
            assertEquals(ScenarioDefinition.BASELINE_SCENARIO_ID, baseline.getScenarioId());
            assertEquals("Baseline Scenario", baseline.getScenarioName());
            assertEquals(ScenarioType.BASELINE, baseline.getScenarioType());
            assertEquals(0.0, baseline.getRainfallChange(), 0.0001);
            assertEquals(0.0, baseline.getHazardIntensityChange(), 0.0001);
            assertEquals(0.0, baseline.getPopulationExposureChange(), 0.0001);
            assertTrue(baseline.isBaseline());
        }

        @Test
        @DisplayName("createScenario with BASELINE type and 0 values returns baseline scenario")
        void testCreateBaselineType() {
            ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                    "Standard Baseline",
                    "BASELINE",
                    "Baseline request",
                    0.0,
                    0.0,
                    0.0
            );

            ScenarioDto result = scenarioService.createScenario(request);
            assertNotNull(result);
            assertTrue(result.isBaseline());
            assertEquals(0.0, result.getRainfallChange());
        }

        @Test
        @DisplayName("createScenario with BASELINE type and non-zero parameters throws InvalidHazardParameterException")
        void testCreateBaselineWithNonZeroThrows() {
            ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                    "Invalid Baseline",
                    "BASELINE",
                    "Should fail",
                    10.0,
                    0.0,
                    0.0
            );

            assertThrows(InvalidHazardParameterException.class, () -> scenarioService.createScenario(request));
        }

        @Test
        @DisplayName("Baseline scenario cannot be deleted via deleteScenario")
        void testBaselineCannotBeDeleted() {
            assertThrows(InvalidHazardParameterException.class, () ->
                    scenarioService.deleteScenario(ScenarioDefinition.BASELINE_SCENARIO_ID)
            );

            // Verify baseline still exists in repository
            assertNotNull(scenarioService.getBaselineScenario());
        }
    }

    // =========================================================================
    // 2. SCENARIO CREATION BY TYPE TESTS
    // =========================================================================

    @Nested
    @DisplayName("2. Scenario Creation by Type")
    class ScenarioCreationByTypeTests {

        @Test
        @DisplayName("createScenario creates RAINFALL_CHANGE scenario with +20% precipitation")
        void testCreateRainfallChangeScenario() {
            ScenarioCreateRequestDto req = new ScenarioCreateRequestDto(
                    "Monsoon Rainfall +20%",
                    "RAINFALL_CHANGE",
                    "Simulates 20% increase in precipitation across North Bihar",
                    20.0,
                    0.0,
                    0.0
            );

            ScenarioDto created = scenarioService.createScenario(req);

            assertNotNull(created);
            assertTrue(created.getScenarioId().startsWith("SCEN-RAIN-"));
            assertEquals("Monsoon Rainfall +20%", created.getScenarioName());
            assertEquals(ScenarioType.RAINFALL_CHANGE, created.getScenarioType());
            assertEquals(20.0, created.getRainfallChange(), 0.0001);
            assertEquals(0.0, created.getHazardIntensityChange(), 0.0001);
            assertEquals(0.0, created.getPopulationExposureChange(), 0.0001);
            assertFalse(created.isBaseline());
        }

        @Test
        @DisplayName("createScenario creates HAZARD_INTENSITY scenario with +15% intensity")
        void testCreateHazardIntensityScenario() {
            ScenarioCreateRequestDto req = new ScenarioCreateRequestDto(
                    "Flash Flood Severity Spike +15%",
                    "HAZARD_INTENSITY",
                    "Acute riverine flood crest surge",
                    0.0,
                    15.0,
                    0.0
            );

            ScenarioDto created = scenarioService.createScenario(req);

            assertNotNull(created);
            assertTrue(created.getScenarioId().startsWith("SCEN-HAZ-"));
            assertEquals("Flash Flood Severity Spike +15%", created.getScenarioName());
            assertEquals(ScenarioType.HAZARD_INTENSITY, created.getScenarioType());
            assertEquals(0.0, created.getRainfallChange(), 0.0001);
            assertEquals(15.0, created.getHazardIntensityChange(), 0.0001);
            assertEquals(0.0, created.getPopulationExposureChange(), 0.0001);
            assertFalse(created.isBaseline());
        }

        @Test
        @DisplayName("createScenario creates POPULATION_EXPOSURE scenario with +30% demographic growth")
        void testCreatePopulationExposureScenario() {
            ScenarioCreateRequestDto req = new ScenarioCreateRequestDto(
                    "Rapid Urban Settlement Growth +30%",
                    "POPULATION_EXPOSURE",
                    "Simulates rapid population influx into flood plains",
                    0.0,
                    0.0,
                    30.0
            );

            ScenarioDto created = scenarioService.createScenario(req);

            assertNotNull(created);
            assertTrue(created.getScenarioId().startsWith("SCEN-POP-"));
            assertEquals("Rapid Urban Settlement Growth +30%", created.getScenarioName());
            assertEquals(ScenarioType.POPULATION_EXPOSURE, created.getScenarioType());
            assertEquals(0.0, created.getRainfallChange(), 0.0001);
            assertEquals(0.0, created.getHazardIntensityChange(), 0.0001);
            assertEquals(30.0, created.getPopulationExposureChange(), 0.0001);
            assertFalse(created.isBaseline());
        }

        @Test
        @DisplayName("createScenario creates MULTI_FACTOR compound scenario with simultaneous parameter shifts")
        void testCreateMultiFactorScenario() {
            ScenarioCreateRequestDto req = new ScenarioCreateRequestDto(
                    "Extreme Catastrophic Convergence",
                    "MULTI_FACTOR",
                    "Heavy Monsoon (+25%), Dam Release (+30%), and Population Growth (+15%)",
                    25.0,
                    30.0,
                    15.0
            );

            ScenarioDto created = scenarioService.createScenario(req);

            assertNotNull(created);
            assertTrue(created.getScenarioId().startsWith("SCEN-MULTI-"));
            assertEquals("Extreme Catastrophic Convergence", created.getScenarioName());
            assertEquals(ScenarioType.MULTI_FACTOR, created.getScenarioType());
            assertEquals(25.0, created.getRainfallChange(), 0.0001);
            assertEquals(30.0, created.getHazardIntensityChange(), 0.0001);
            assertEquals(15.0, created.getPopulationExposureChange(), 0.0001);
        }
    }

    // =========================================================================
    // 3. PARAMETER VALIDATION & ERROR HANDLING
    // =========================================================================

    @Nested
    @DisplayName("3. Input Validation & Error Handling")
    class ValidationAndErrorHandlingTests {

        @Test
        @DisplayName("createScenario with null request throws InvalidHazardParameterException")
        void testNullRequestThrows() {
            assertThrows(InvalidHazardParameterException.class, () -> scenarioService.createScenario(null));
        }

        @Test
        @DisplayName("createScenario with blank scenarioName throws InvalidHazardParameterException")
        void testBlankNameThrows() {
            ScenarioCreateRequestDto req = new ScenarioCreateRequestDto(
                    "   ",
                    "RAINFALL_CHANGE",
                    "Description",
                    10.0, 0.0, 0.0
            );
            assertThrows(InvalidHazardParameterException.class, () -> scenarioService.createScenario(req));
        }

        @Test
        @DisplayName("createScenario with null or invalid scenarioType throws InvalidHazardParameterException")
        void testInvalidScenarioTypeThrows() {
            ScenarioCreateRequestDto req1 = new ScenarioCreateRequestDto("Valid Name", null, "Desc", 10.0, 0.0, 0.0);
            assertThrows(InvalidHazardParameterException.class, () -> scenarioService.createScenario(req1));

            ScenarioCreateRequestDto req2 = new ScenarioCreateRequestDto("Valid Name", "INVALID_TYPE_123", "Desc", 10.0, 0.0, 0.0);
            assertThrows(InvalidHazardParameterException.class, () -> scenarioService.createScenario(req2));
        }

        @Test
        @DisplayName("createScenario with negative change < -100.0% throws InvalidHazardParameterException")
        void testExcessiveNegativeChangeThrows() {
            ScenarioCreateRequestDto req = new ScenarioCreateRequestDto(
                    "Impossible Reduction",
                    "RAINFALL_CHANGE",
                    "Desc",
                    -120.0, // Invalid: cannot have less than -100%
                    0.0,
                    0.0
            );
            assertThrows(InvalidHazardParameterException.class, () -> scenarioService.createScenario(req));
        }

        @Test
        @DisplayName("createScenario with change > +1000.0% throws InvalidHazardParameterException")
        void testExcessivePositiveChangeThrows() {
            ScenarioCreateRequestDto req = new ScenarioCreateRequestDto(
                    "Unbounded Increase",
                    "RAINFALL_CHANGE",
                    "Desc",
                    1500.0, // Invalid: exceeds sanity limit
                    0.0,
                    0.0
            );
            assertThrows(InvalidHazardParameterException.class, () -> scenarioService.createScenario(req));
        }

        @Test
        @DisplayName("createScenario with NaN or Infinite numeric values throws InvalidHazardParameterException")
        void testNaNDeltaThrows() {
            ScenarioCreateRequestDto req1 = new ScenarioCreateRequestDto("NaN Test", "RAINFALL_CHANGE", "Desc", Double.NaN, 0.0, 0.0);
            assertThrows(InvalidHazardParameterException.class, () -> scenarioService.createScenario(req1));

            ScenarioCreateRequestDto req2 = new ScenarioCreateRequestDto("Inf Test", "RAINFALL_CHANGE", "Desc", Double.POSITIVE_INFINITY, 0.0, 0.0);
            assertThrows(InvalidHazardParameterException.class, () -> scenarioService.createScenario(req2));
        }

        @Test
        @DisplayName("createScenario with null numeric deltas defaults safely to 0.0")
        void testNullDeltasDefaultToZero() {
            ScenarioCreateRequestDto req = new ScenarioCreateRequestDto(
                    "Null Deltas Test",
                    "RAINFALL_CHANGE",
                    "Desc",
                    null,
                    null,
                    null
            );

            ScenarioDto created = scenarioService.createScenario(req);
            assertNotNull(created);
            assertEquals(0.0, created.getRainfallChange());
            assertEquals(0.0, created.getHazardIntensityChange());
            assertEquals(0.0, created.getPopulationExposureChange());
        }
    }

    // =========================================================================
    // 4. DATA INTEGRITY & BASELINE NON-MUTATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("4. Baseline Non-Mutation & Data Integrity")
    class DataIntegrityTests {

        @Test
        @DisplayName("Creating multiple custom scenarios does NOT mutate baseline reference values")
        void testBaselineRemainsUnmutated() {
            ScenarioDto baselineBefore = scenarioService.getBaselineScenario();

            // Create multiple custom scenarios with varied perturbations
            scenarioService.createScenario(new ScenarioCreateRequestDto("Rain +25%", "RAINFALL_CHANGE", "Desc", 25.0, 0.0, 0.0));
            scenarioService.createScenario(new ScenarioCreateRequestDto("Hazard +50%", "HAZARD_INTENSITY", "Desc", 0.0, 50.0, 0.0));
            scenarioService.createScenario(new ScenarioCreateRequestDto("Pop -10%", "POPULATION_EXPOSURE", "Desc", 0.0, 0.0, -10.0));

            ScenarioDto baselineAfter = scenarioService.getBaselineScenario();

            assertEquals(baselineBefore.getScenarioId(), baselineAfter.getScenarioId());
            assertEquals(0.0, baselineAfter.getRainfallChange(), 0.0001);
            assertEquals(0.0, baselineAfter.getHazardIntensityChange(), 0.0001);
            assertEquals(0.0, baselineAfter.getPopulationExposureChange(), 0.0001);
            assertTrue(baselineAfter.isBaseline());
        }
    }

    // =========================================================================
    // 5. RETRIEVAL & FILTERING TESTS
    // =========================================================================

    @Nested
    @DisplayName("5. Retrieval, Filtering & Deletion")
    class RetrievalAndDeletionTests {

        @Test
        @DisplayName("getAllScenarios returns baseline + all created scenarios")
        void testGetAllScenarios() {
            scenarioService.createScenario(new ScenarioCreateRequestDto("Rain +10%", "RAINFALL_CHANGE", "Desc", 10.0, 0.0, 0.0));
            scenarioService.createScenario(new ScenarioCreateRequestDto("Pop +20%", "POPULATION_EXPOSURE", "Desc", 0.0, 0.0, 20.0));

            List<ScenarioDto> all = scenarioService.getAllScenarios(null);

            // Baseline (1) + 2 created = 3
            assertEquals(3, all.size());
        }

        @Test
        @DisplayName("getAllScenarios filters accurately by type")
        void testFilterByType() {
            scenarioService.createScenario(new ScenarioCreateRequestDto("Rain 1", "RAINFALL_CHANGE", "Desc", 10.0, 0.0, 0.0));
            scenarioService.createScenario(new ScenarioCreateRequestDto("Rain 2", "RAINFALL_CHANGE", "Desc", 20.0, 0.0, 0.0));
            scenarioService.createScenario(new ScenarioCreateRequestDto("Hazard 1", "HAZARD_INTENSITY", "Desc", 0.0, 15.0, 0.0));

            List<ScenarioDto> rainOnly = scenarioService.getAllScenarios("RAINFALL_CHANGE");
            assertEquals(2, rainOnly.size());
            assertTrue(rainOnly.stream().allMatch(s -> s.getScenarioType() == ScenarioType.RAINFALL_CHANGE));

            List<ScenarioDto> hazOnly = scenarioService.getAllScenarios("HAZARD_INTENSITY");
            assertEquals(1, hazOnly.size());
            assertEquals("Hazard 1", hazOnly.get(0).getScenarioName());
        }

        @Test
        @DisplayName("getScenarioById retrieves existing scenario or throws HazardNotFoundException")
        void testGetById() {
            ScenarioDto created = scenarioService.createScenario(
                    new ScenarioCreateRequestDto("Test Scenario", "RAINFALL_CHANGE", "Desc", 15.0, 0.0, 0.0)
            );

            ScenarioDto retrieved = scenarioService.getScenarioById(created.getScenarioId());
            assertNotNull(retrieved);
            assertEquals("Test Scenario", retrieved.getScenarioName());

            assertThrows(HazardNotFoundException.class, () -> scenarioService.getScenarioById("NON_EXISTENT_ID"));
        }

        @Test
        @DisplayName("deleteScenario deletes custom scenario successfully")
        void testDeleteCustomScenario() {
            ScenarioDto created = scenarioService.createScenario(
                    new ScenarioCreateRequestDto("To Delete", "RAINFALL_CHANGE", "Desc", 15.0, 0.0, 0.0)
            );

            boolean deleted = scenarioService.deleteScenario(created.getScenarioId());
            assertTrue(deleted);

            assertThrows(HazardNotFoundException.class, () -> scenarioService.getScenarioById(created.getScenarioId()));
        }

        @Test
        @DisplayName("getScenarioTypes returns full list of available scenario types")
        void testGetScenarioTypes() {
            List<ScenarioTypeInfoDto> types = scenarioService.getScenarioTypes();
            assertNotNull(types);
            assertEquals(ScenarioType.values().length, types.size());
        }
    }
}
