package com.hazard;

import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.domain.scenario.ScenarioType;
import com.hazard.dto.scenario.ScenarioCreateRequestDto;
import com.hazard.dto.scenario.ScenarioDto;
import com.hazard.dto.scenario.ScenarioTypeInfoDto;
import com.hazard.exception.InvalidHazardParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 9A — Unit tests for Scenario Domain Models, Enums, and DTOs.
 */
class ScenarioDomainAndDtoTests {

    @Nested
    @DisplayName("1. ScenarioType Enum & Mapping Tests")
    class ScenarioTypeTests {

        @Test
        @DisplayName("ScenarioType contains required types: BASELINE, RAINFALL_CHANGE, HAZARD_INTENSITY, POPULATION_EXPOSURE")
        void testRequiredScenarioTypesExist() {
            assertNotNull(ScenarioType.BASELINE);
            assertNotNull(ScenarioType.RAINFALL_CHANGE);
            assertNotNull(ScenarioType.HAZARD_INTENSITY);
            assertNotNull(ScenarioType.POPULATION_EXPOSURE);
            assertNotNull(ScenarioType.MULTI_FACTOR);

            assertTrue(ScenarioType.BASELINE.isBaseline());
            assertFalse(ScenarioType.RAINFALL_CHANGE.isBaseline());
            assertFalse(ScenarioType.HAZARD_INTENSITY.isBaseline());
            assertFalse(ScenarioType.POPULATION_EXPOSURE.isBaseline());
        }

        @Test
        @DisplayName("ScenarioType.fromString resolves valid names and aliases case-insensitively")
        void testFromStringResolutions() {
            assertEquals(ScenarioType.BASELINE, ScenarioType.fromString("BASELINE"));
            assertEquals(ScenarioType.BASELINE, ScenarioType.fromString("baseline"));
            assertEquals(ScenarioType.RAINFALL_CHANGE, ScenarioType.fromString("RAINFALL_CHANGE"));
            assertEquals(ScenarioType.RAINFALL_CHANGE, ScenarioType.fromString("rainfall-change"));
            assertEquals(ScenarioType.RAINFALL_CHANGE, ScenarioType.fromString("rain"));
            assertEquals(ScenarioType.RAINFALL_CHANGE, ScenarioType.fromString("rainfall"));
            assertEquals(ScenarioType.HAZARD_INTENSITY, ScenarioType.fromString("HAZARD_INTENSITY"));
            assertEquals(ScenarioType.HAZARD_INTENSITY, ScenarioType.fromString("hazard"));
            assertEquals(ScenarioType.HAZARD_INTENSITY, ScenarioType.fromString("intensity"));
            assertEquals(ScenarioType.POPULATION_EXPOSURE, ScenarioType.fromString("POPULATION_EXPOSURE"));
            assertEquals(ScenarioType.POPULATION_EXPOSURE, ScenarioType.fromString("population"));
            assertEquals(ScenarioType.POPULATION_EXPOSURE, ScenarioType.fromString("pop"));
            assertEquals(ScenarioType.MULTI_FACTOR, ScenarioType.fromString("MULTI_FACTOR"));
            assertEquals(ScenarioType.MULTI_FACTOR, ScenarioType.fromString("compound"));
        }

        @Test
        @DisplayName("ScenarioType.fromString throws InvalidHazardParameterException for unknown or blank type")
        void testFromStringInvalidThrows() {
            assertThrows(InvalidHazardParameterException.class, () -> ScenarioType.fromString(null));
            assertThrows(InvalidHazardParameterException.class, () -> ScenarioType.fromString("   "));
            assertThrows(InvalidHazardParameterException.class, () -> ScenarioType.fromString("UNKNOWN_XYZ_TYPE"));
        }

        @Test
        @DisplayName("ScenarioType has descriptive display names and descriptions")
        void testDisplayNames() {
            for (ScenarioType type : ScenarioType.values()) {
                assertNotNull(type.getDisplayName());
                assertFalse(type.getDisplayName().isEmpty());
                assertNotNull(type.getDescription());
                assertFalse(type.getDescription().isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("2. ScenarioDefinition Domain Model Tests")
    class ScenarioDefinitionTests {

        @Test
        @DisplayName("createBaseline produces valid unperturbed baseline entity")
        void testCreateBaseline() {
            ScenarioDefinition baseline = ScenarioDefinition.createBaseline();

            assertEquals(ScenarioDefinition.BASELINE_SCENARIO_ID, baseline.getScenarioId());
            assertEquals("Baseline Scenario", baseline.getScenarioName());
            assertEquals(ScenarioType.BASELINE, baseline.getScenarioType());
            assertEquals(0.0, baseline.getRainfallChange(), 0.0001);
            assertEquals(0.0, baseline.getHazardIntensityChange(), 0.0001);
            assertEquals(0.0, baseline.getPopulationExposureChange(), 0.0001);
            assertTrue(baseline.isBaseline());
            assertNotNull(baseline.getCreatedAt());
            assertNotNull(baseline.getUpdatedAt());
        }

        @Test
        @DisplayName("ScenarioDefinition holds custom perturbation parameters accurately")
        void testCustomScenarioParameters() {
            ScenarioDefinition scen = new ScenarioDefinition(
                    "SCEN-TEST-001",
                    "Heavy Flood Scenario",
                    ScenarioType.RAINFALL_CHANGE,
                    "Simulates +35% precipitation",
                    35.0,
                    10.0,
                    5.0,
                    false
            );

            assertEquals("SCEN-TEST-001", scen.getScenarioId());
            assertEquals("Heavy Flood Scenario", scen.getScenarioName());
            assertEquals(ScenarioType.RAINFALL_CHANGE, scen.getScenarioType());
            assertEquals(35.0, scen.getRainfallChange(), 0.0001);
            assertEquals(10.0, scen.getHazardIntensityChange(), 0.0001);
            assertEquals(5.0, scen.getPopulationExposureChange(), 0.0001);
            assertFalse(scen.isBaseline());
        }

        @Test
        @DisplayName("ScenarioDefinition equals and hashCode based on scenarioId")
        void testEqualsAndHashCode() {
            ScenarioDefinition s1 = new ScenarioDefinition();
            s1.setScenarioId("SCEN-01");
            ScenarioDefinition s2 = new ScenarioDefinition();
            s2.setScenarioId("SCEN-01");
            ScenarioDefinition s3 = new ScenarioDefinition();
            s3.setScenarioId("SCEN-02");

            assertEquals(s1, s2);
            assertEquals(s1.hashCode(), s2.hashCode());
            assertNotEquals(s1, s3);
        }
    }

    @Nested
    @DisplayName("3. Scenario DTOs Tests")
    class ScenarioDtoTests {

        @Test
        @DisplayName("ScenarioDto.fromDomain converts all domain fields correctly")
        void testFromDomain() {
            ScenarioDefinition domain = new ScenarioDefinition(
                    "SCEN-POP-101",
                    "Urban Influx Scenario",
                    ScenarioType.POPULATION_EXPOSURE,
                    "Simulates +40% demographic influx",
                    0.0,
                    0.0,
                    40.0,
                    false
            );
            domain.setCreatedAt(LocalDateTime.of(2026, 8, 29, 10, 0));
            domain.setUpdatedAt(LocalDateTime.of(2026, 8, 29, 10, 30));

            ScenarioDto dto = ScenarioDto.fromDomain(domain);

            assertNotNull(dto);
            assertEquals("SCEN-POP-101", dto.getScenarioId());
            assertEquals("Urban Influx Scenario", dto.getScenarioName());
            assertEquals(ScenarioType.POPULATION_EXPOSURE, dto.getScenarioType());
            assertEquals("Population Exposure Scenario", dto.getScenarioTypeDisplayName());
            assertEquals("Simulates +40% demographic influx", dto.getDescription());
            assertEquals(0.0, dto.getRainfallChange(), 0.0001);
            assertEquals(0.0, dto.getHazardIntensityChange(), 0.0001);
            assertEquals(40.0, dto.getPopulationExposureChange(), 0.0001);
            assertFalse(dto.isBaseline());
            assertEquals(LocalDateTime.of(2026, 8, 29, 10, 0), dto.getCreatedAt());
            assertEquals(LocalDateTime.of(2026, 8, 29, 10, 30), dto.getUpdatedAt());
        }

        @Test
        @DisplayName("ScenarioDto.fromDomain returns null when domain is null")
        void testFromDomainNull() {
            assertNull(ScenarioDto.fromDomain(null));
        }

        @Test
        @DisplayName("ScenarioTypeInfoDto produces complete metadata")
        void testScenarioTypeInfoDto() {
            ScenarioTypeInfoDto info = ScenarioTypeInfoDto.fromScenarioType(ScenarioType.RAINFALL_CHANGE);

            assertNotNull(info);
            assertEquals("RAINFALL_CHANGE", info.getType());
            assertEquals("Rainfall Change Scenario", info.getDisplayName());
            assertFalse(info.isBaseline());
            assertNotNull(info.getDescription());
        }
    }
}
