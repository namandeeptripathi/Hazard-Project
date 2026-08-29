package com.hazard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.dto.scenario.ScenarioCreateRequestDto;
import com.hazard.dto.scenario.ScenarioExecutionRequestDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Stage 9B — REST Controller Integration Tests for Scenario Execution Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ScenarioExecutionControllerTests {

    private static final Logger log = LoggerFactory.getLogger(ScenarioExecutionControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. POST /api/v1/scenarios/SCEN-BASELINE/execute — Returns 200 OK with baseline simulation")
    void testExecuteBaselineScenario() throws Exception {
        ScenarioExecutionRequestDto request = new ScenarioExecutionRequestDto("Sitamarhi");

        mockMvc.perform(post("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(ScenarioDefinition.BASELINE_SCENARIO_ID))
                .andExpect(jsonPath("$.data.scenarioName").value("Baseline Scenario"))
                .andExpect(jsonPath("$.data.districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.deltaRiskScore").value(0.0))
                .andExpect(jsonPath("$.data.deltaRiskScore100").value(0.0))
                .andExpect(jsonPath("$.data.riskDirection").value("UNCHANGED"))
                .andExpect(jsonPath("$.data.simulatedRisk.riskScore").exists())
                .andExpect(jsonPath("$.data.baselineRisk.riskScore").exists())
                .andExpect(jsonPath("$.meta.stage").value("9B"));

        log.info("✅ POST /api/v1/scenarios/SCEN-BASELINE/execute — 200 OK");
    }

    @Test
    @DisplayName("2. POST /api/v1/scenarios/{id}/execute — Custom Rainfall Scenario returns 200 OK with simulated increase")
    void testExecuteCustomRainfallScenario() throws Exception {
        // 1. Create a custom rainfall scenario (+25%)
        ScenarioCreateRequestDto createReq = new ScenarioCreateRequestDto(
                "Monsoon Test +25%", "RAINFALL_CHANGE", "Rainfall execution test", 25.0, 0.0, 0.0
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String scenarioId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("scenarioId").asText();

        // 2. Execute the scenario for Sitamarhi
        ScenarioExecutionRequestDto execReq = new ScenarioExecutionRequestDto("Sitamarhi");

        mockMvc.perform(post("/api/v1/scenarios/" + scenarioId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(execReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(scenarioId))
                .andExpect(jsonPath("$.data.districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.appliedRainfallChange").value(25.0))
                .andExpect(jsonPath("$.data.simulationContext.effectiveHazardMultiplier").value(1.25))
                .andExpect(jsonPath("$.data.riskDirection").value("INCREASED"))
                .andExpect(jsonPath("$.meta.stage").value("9B"));

        log.info("✅ POST /api/v1/scenarios/{id}/execute — 200 OK with simulated increase");
    }

    @Test
    @DisplayName("3. GET /api/v1/scenarios/{id}/execute — Query parameter execution returns 200 OK")
    void testGetScenarioExecutionQueryParams() throws Exception {
        mockMvc.perform(get("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/execute")
                        .param("district", "Patna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtName").value("Patna"))
                .andExpect(jsonPath("$.data.scenarioId").value(ScenarioDefinition.BASELINE_SCENARIO_ID));

        log.info("✅ GET /api/v1/scenarios/{id}/execute?district=Patna — 200 OK");
    }

    @Test
    @DisplayName("4. POST /api/v1/scenarios/{id}/execute/all — Batch simulation across 38 districts returns 200 OK")
    void testExecuteAllDistrictsEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/execute/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(38)))
                .andExpect(jsonPath("$.meta.totalDistrictsEvaluated").value(38));

        log.info("✅ POST /api/v1/scenarios/{id}/execute/all — 200 OK");
    }

    @Test
    @DisplayName("5. POST /api/v1/scenarios/NON-EXISTENT/execute — Returns 404 Not Found")
    void testExecuteNonExistentScenarioReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/NON-EXISTENT-SCENARIO-XYZ/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ScenarioExecutionRequestDto("Sitamarhi"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        log.info("✅ POST /api/v1/scenarios/NON-EXISTENT/execute — 404 Not Found verified");
    }

    @Test
    @DisplayName("6. POST /api/v1/scenarios/{id}/execute with invalid district — Returns 404 Not Found")
    void testExecuteInvalidDistrictReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ScenarioExecutionRequestDto("AtlantisCity"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        log.info("✅ POST /api/v1/scenarios/{id}/execute (Invalid District) — 404 Not Found verified");
    }
}
