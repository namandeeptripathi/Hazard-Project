package com.hazard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.dto.scenario.ScenarioCreateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Stage 9A — REST Controller Integration Tests for Scenario Creation and Management.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ScenarioControllerTests {

    private static final Logger log = LoggerFactory.getLogger(ScenarioControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. GET /api/v1/scenarios/baseline — Returns 200 OK with baseline parameters (all 0%)")
    void testGetBaselineScenario() throws Exception {
        mockMvc.perform(get("/api/v1/scenarios/baseline"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(ScenarioDefinition.BASELINE_SCENARIO_ID))
                .andExpect(jsonPath("$.data.scenarioName").value("Baseline Scenario"))
                .andExpect(jsonPath("$.data.scenarioType").value("BASELINE"))
                .andExpect(jsonPath("$.data.rainfallChange").value(0.0))
                .andExpect(jsonPath("$.data.hazardIntensityChange").value(0.0))
                .andExpect(jsonPath("$.data.populationExposureChange").value(0.0))
                .andExpect(jsonPath("$.data.baseline").value(true))
                .andExpect(jsonPath("$.meta.stage").value("9A"));

        log.info("✅ GET /api/v1/scenarios/baseline — 200 OK");
    }

    @Test
    @DisplayName("2. GET /api/v1/scenarios/types — Returns 200 OK with all supported scenario types")
    void testGetScenarioTypes() throws Exception {
        mockMvc.perform(get("/api/v1/scenarios/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.meta.stage").value("9A"));

        log.info("✅ GET /api/v1/scenarios/types — 200 OK");
    }

    @Test
    @DisplayName("3. POST /api/v1/scenarios — Valid Rainfall Change Scenario returns 201 Created")
    void testCreateRainfallScenario() throws Exception {
        ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                "Monsoon Heavy Precipitation +25%",
                "RAINFALL_CHANGE",
                "Testing acute rainfall perturbation across Gandak-Kosi basins",
                25.0,
                0.0,
                0.0
        );

        mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(startsWith("SCEN-RAIN-")))
                .andExpect(jsonPath("$.data.scenarioName").value("Monsoon Heavy Precipitation +25%"))
                .andExpect(jsonPath("$.data.scenarioType").value("RAINFALL_CHANGE"))
                .andExpect(jsonPath("$.data.rainfallChange").value(25.0))
                .andExpect(jsonPath("$.data.hazardIntensityChange").value(0.0))
                .andExpect(jsonPath("$.data.populationExposureChange").value(0.0))
                .andExpect(jsonPath("$.data.baseline").value(false))
                .andExpect(jsonPath("$.meta.stage").value("9A"));

        log.info("✅ POST /api/v1/scenarios (Rainfall) — 201 Created");
    }

    @Test
    @DisplayName("4. POST /api/v1/scenarios — Valid Hazard Intensity Scenario returns 201 Created")
    void testCreateHazardIntensityScenario() throws Exception {
        ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                "Embankment Breach Flood Spike +20%",
                "HAZARD_INTENSITY",
                "Simulates direct flood depth and velocity surge",
                0.0,
                20.0,
                0.0
        );

        mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(startsWith("SCEN-HAZ-")))
                .andExpect(jsonPath("$.data.hazardIntensityChange").value(20.0));

        log.info("✅ POST /api/v1/scenarios (Hazard Intensity) — 201 Created");
    }

    @Test
    @DisplayName("5. POST /api/v1/scenarios — Valid Population Exposure Scenario returns 201 Created")
    void testCreatePopulationExposureScenario() throws Exception {
        ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                "Rapid Settlement Growth +35%",
                "POPULATION_EXPOSURE",
                "Simulates population exposure expansion in vulnerable river floodplains",
                0.0,
                0.0,
                35.0
        );

        mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(startsWith("SCEN-POP-")))
                .andExpect(jsonPath("$.data.populationExposureChange").value(35.0));

        log.info("✅ POST /api/v1/scenarios (Population Exposure) — 201 Created");
    }

    @Test
    @DisplayName("6. POST /api/v1/scenarios — Invalid scenario type returns 400 Bad Request")
    void testCreateScenarioInvalidType() throws Exception {
        ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                "Invalid Type Scenario",
                "NON_EXISTENT_SCENARIO_TYPE",
                "Description",
                10.0,
                0.0,
                0.0
        );

        mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid scenario type")));

        log.info("✅ POST /api/v1/scenarios (Invalid Type) — 400 Bad Request");
    }

    @Test
    @DisplayName("7. POST /api/v1/scenarios — Missing scenario name returns 400 Bad Request")
    void testCreateScenarioMissingName() throws Exception {
        ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                "",
                "RAINFALL_CHANGE",
                "Description",
                10.0,
                0.0,
                0.0
        );

        mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("Scenario name is required")));

        log.info("✅ POST /api/v1/scenarios (Missing Name) — 400 Bad Request");
    }

    @Test
    @DisplayName("8. POST /api/v1/scenarios — Parameter change < -100% returns 400 Bad Request")
    void testCreateScenarioExcessiveNegativeChange() throws Exception {
        ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                "Sub-zero Rainfall",
                "RAINFALL_CHANGE",
                "Description",
                -150.0,
                0.0,
                0.0
        );

        mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("cannot be less than -100.0%")));

        log.info("✅ POST /api/v1/scenarios (Negative < -100%) — 400 Bad Request");
    }

    @Test
    @DisplayName("9. GET /api/v1/scenarios — Returns all scenarios and supports ?type= filter")
    void testGetAllScenariosAndFilter() throws Exception {
        // Create one rainfall scenario
        ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                "Filter Test Rain",
                "RAINFALL_CHANGE",
                "Desc",
                15.0, 0.0, 0.0
        );

        mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Test GET all
        mockMvc.perform(get("/api/v1/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));

        // Test GET filter by type
        mockMvc.perform(get("/api/v1/scenarios").param("type", "RAINFALL_CHANGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].scenarioType").value("RAINFALL_CHANGE"));

        log.info("✅ GET /api/v1/scenarios — 200 OK with filtering");
    }

    @Test
    @DisplayName("10. GET /api/v1/scenarios/{id} & DELETE /api/v1/scenarios/{id} — Full Lifecycle")
    void testGetAndDeleteScenarioLifecycle() throws Exception {
        ScenarioCreateRequestDto request = new ScenarioCreateRequestDto(
                "Lifecycle Scenario",
                "RAINFALL_CHANGE",
                "Desc",
                10.0, 0.0, 0.0
        );

        MvcResult result = mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String scenarioId = objectMapper.readTree(responseBody).path("data").path("scenarioId").asText();

        // GET by ID
        mockMvc.perform(get("/api/v1/scenarios/" + scenarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scenarioId").value(scenarioId))
                .andExpect(jsonPath("$.data.scenarioName").value("Lifecycle Scenario"));

        // DELETE
        mockMvc.perform(delete("/api/v1/scenarios/" + scenarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deleted").value(true));

        // GET after DELETE -> 404
        mockMvc.perform(get("/api/v1/scenarios/" + scenarioId))
                .andExpect(status().isNotFound());

        // Attempt DELETE baseline -> 400 Bad Request
        mockMvc.perform(delete("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        log.info("✅ GET & DELETE lifecycle verified");
    }
}
