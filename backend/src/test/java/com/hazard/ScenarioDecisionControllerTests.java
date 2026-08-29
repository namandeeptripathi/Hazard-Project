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
 * Stage 9D — REST Controller Integration Tests for Priority & Relocation Recalculation Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ScenarioDecisionControllerTests {

    private static final Logger log = LoggerFactory.getLogger(ScenarioDecisionControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. POST /api/v1/scenarios/SCEN-BASELINE/decision/execute?district=Sitamarhi — 200 OK with baseline decision")
    void testBaselineDecisionExecution() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/decision/execute")
                        .param("district", "Sitamarhi")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(ScenarioDefinition.BASELINE_SCENARIO_ID))
                .andExpect(jsonPath("$.data.totalDistrictsEvaluated").value(1))
                .andExpect(jsonPath("$.data.districtResults[0].districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.districtResults[0].priorityShiftDirection").value("UNCHANGED"))
                .andExpect(jsonPath("$.meta.stage").value("9D"))
                .andExpect(jsonPath("$.meta.substage").value("Priority & Relocation Recalculation"));

        log.info("✅ POST /api/v1/scenarios/SCEN-BASELINE/decision/execute — 200 OK");
    }

    @Test
    @DisplayName("2. POST /api/v1/scenarios/{id}/decision/execute — Custom Catastrophe scenario returns 200 OK")
    void testCustomScenarioDecisionExecution() throws Exception {
        // 1. Create a custom multi-factor catastrophe scenario
        ScenarioCreateRequestDto createReq = new ScenarioCreateRequestDto(
                "Catastrophe Surge +50%", "MULTI_FACTOR", "Testing decision endpoint", 50.0, 30.0, 20.0
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String scenarioId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("scenarioId").asText();

        // 2. Execute decision recalculation for Sitamarhi
        ScenarioExecutionRequestDto execReq = new ScenarioExecutionRequestDto("Sitamarhi");
        mockMvc.perform(post("/api/v1/scenarios/" + scenarioId + "/decision/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(execReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(scenarioId))
                .andExpect(jsonPath("$.data.districtResults[0].districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.districtResults[0].simulatedPriorityScore").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.meta.stage").value("9D"));

        log.info("✅ POST /api/v1/scenarios/{id}/decision/execute — 200 OK");
    }

    @Test
    @DisplayName("3. GET /api/v1/scenarios/{id}/decision/execute?district=Sitamarhi — Query GET returns 200 OK")
    void testSingleDistrictDecisionQuery() throws Exception {
        mockMvc.perform(get("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/decision/execute")
                        .param("district", "Sitamarhi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDistrictsEvaluated").value(1))
                .andExpect(jsonPath("$.data.districtResults[0].districtName").value("Sitamarhi"));

        log.info("✅ GET /api/v1/scenarios/{id}/decision/execute?district=Sitamarhi — 200 OK");
    }

    @Test
    @DisplayName("4. POST /api/v1/scenarios/NON-EXISTENT/decision/execute — Returns 404 Not Found")
    void testMissingScenarioReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/NON-EXISTENT-SCENARIO-XYZ/decision/execute")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        log.info("✅ POST /api/v1/scenarios/NON-EXISTENT/decision/execute — 404 Not Found verified");
    }
}
