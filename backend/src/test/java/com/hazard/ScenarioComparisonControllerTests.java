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
 * Stage 9E — REST Controller Integration Tests for Before/After Comparison Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ScenarioComparisonControllerTests {

    private static final Logger log = LoggerFactory.getLogger(ScenarioComparisonControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. POST /api/v1/scenarios/SCEN-BASELINE/compare?district=Sitamarhi — 200 OK with baseline comparison")
    void testBaselineComparisonExecution() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/compare")
                        .param("district", "Sitamarhi")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(ScenarioDefinition.BASELINE_SCENARIO_ID))
                .andExpect(jsonPath("$.data.totalDistrictsEvaluated").value(1))
                .andExpect(jsonPath("$.data.districtComparisons[0].districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.districtComparisons[0].riskDirection").value("UNCHANGED"))
                .andExpect(jsonPath("$.data.districtComparisons[0].priorityShiftDirection").value("UNCHANGED"))
                .andExpect(jsonPath("$.meta.stage").value("9E"))
                .andExpect(jsonPath("$.meta.substage").value("Before/After Comparison"));

        log.info("✅ POST /api/v1/scenarios/SCEN-BASELINE/compare — 200 OK");
    }

    @Test
    @DisplayName("2. POST /api/v1/scenarios/{id}/compare — Custom Catastrophe scenario returns 200 OK")
    void testCustomScenarioComparisonExecution() throws Exception {
        // 1. Create custom scenario
        ScenarioCreateRequestDto createReq = new ScenarioCreateRequestDto(
                "Catastrophe Surge +50%", "MULTI_FACTOR", "Testing comparison endpoint", 50.0, 30.0, 20.0
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String scenarioId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("scenarioId").asText();

        // 2. Execute comparison for Sitamarhi
        ScenarioExecutionRequestDto execReq = new ScenarioExecutionRequestDto("Sitamarhi");
        mockMvc.perform(post("/api/v1/scenarios/" + scenarioId + "/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(execReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(scenarioId))
                .andExpect(jsonPath("$.data.districtComparisons[0].districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.districtComparisons[0].simulatedRiskScore").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.meta.stage").value("9E"));

        log.info("✅ POST /api/v1/scenarios/{id}/compare — 200 OK");
    }

    @Test
    @DisplayName("3. GET /api/v1/scenarios/{id}/compare?district=Sitamarhi — Query GET returns 200 OK")
    void testSingleDistrictComparisonQuery() throws Exception {
        mockMvc.perform(get("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/compare")
                        .param("district", "Sitamarhi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDistrictsEvaluated").value(1))
                .andExpect(jsonPath("$.data.districtComparisons[0].districtName").value("Sitamarhi"));

        log.info("✅ GET /api/v1/scenarios/{id}/compare?district=Sitamarhi — 200 OK");
    }

    @Test
    @DisplayName("4. POST /api/v1/scenarios/NON-EXISTENT/compare — Returns 404 Not Found")
    void testMissingScenarioReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/NON-EXISTENT-SCENARIO-XYZ/compare")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        log.info("✅ POST /api/v1/scenarios/NON-EXISTENT/compare — 404 Not Found verified");
    }
}
