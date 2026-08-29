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
 * Stage 9C — REST Controller Integration Tests for Red-Zone Recalculation Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ScenarioRedZoneControllerTests {

    private static final Logger log = LoggerFactory.getLogger(ScenarioRedZoneControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. POST /api/v1/scenarios/SCEN-BASELINE/red-zone/execute — 200 OK with baseline consistency")
    void testBaselineRedZoneExecution() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/red-zone/execute")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(ScenarioDefinition.BASELINE_SCENARIO_ID))
                .andExpect(jsonPath("$.data.netRedZoneChange").value(0))
                .andExpect(jsonPath("$.data.newlyEnteredRedZoneCount").value(0))
                .andExpect(jsonPath("$.data.leftRedZoneCount").value(0))
                .andExpect(jsonPath("$.meta.stage").value("9C"))
                .andExpect(jsonPath("$.meta.substage").value("Red-Zone Recalculation"));

        log.info("✅ POST /api/v1/scenarios/SCEN-BASELINE/red-zone/execute — 200 OK");
    }

    @Test
    @DisplayName("2. POST /api/v1/scenarios/{id}/red-zone/execute — Custom Catastrophe scenario returns 200 OK with shift")
    void testCustomScenarioRedZoneExecution() throws Exception {
        // 1. Create a custom multi-factor catastrophe scenario
        ScenarioCreateRequestDto createReq = new ScenarioCreateRequestDto(
                "Catastrophe Surge +50%", "MULTI_FACTOR", "Testing Red Zone shift endpoint", 50.0, 30.0, 20.0
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String scenarioId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("scenarioId").asText();

        // 2. Execute Red-Zone recalculation across all districts
        mockMvc.perform(post("/api/v1/scenarios/" + scenarioId + "/red-zone/execute")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioId").value(scenarioId))
                .andExpect(jsonPath("$.data.totalDistrictsEvaluated").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.simulatedRedZoneCount").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.meta.stage").value("9C"));

        log.info("✅ POST /api/v1/scenarios/{id}/red-zone/execute — 200 OK");
    }

    @Test
    @DisplayName("3. GET /api/v1/scenarios/{id}/red-zone/execute?district=Sitamarhi — Single district query returns 200 OK")
    void testSingleDistrictRedZoneQuery() throws Exception {
        mockMvc.perform(get("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/red-zone/execute")
                        .param("district", "Sitamarhi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDistrictsEvaluated").value(1))
                .andExpect(jsonPath("$.data.districtResults[0].districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.districtResults[0].transitionType").value("UNCHANGED_NON_RED_ZONE"));

        log.info("✅ GET /api/v1/scenarios/{id}/red-zone/execute?district=Sitamarhi — 200 OK");
    }

    @Test
    @DisplayName("4. POST /api/v1/scenarios/{id}/red-zone/execute/all — Batch all-districts recalculation returns 200 OK")
    void testBatchAllDistrictsRedZoneEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/" + ScenarioDefinition.BASELINE_SCENARIO_ID + "/red-zone/execute/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtResults").isArray())
                .andExpect(jsonPath("$.data.districtResults", hasSize(38)))
                .andExpect(jsonPath("$.meta.totalDistrictsEvaluated").value(38));

        log.info("✅ POST /api/v1/scenarios/{id}/red-zone/execute/all — 200 OK");
    }

    @Test
    @DisplayName("5. POST /api/v1/scenarios/NON-EXISTENT/red-zone/execute — Returns 404 Not Found")
    void testMissingScenarioReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/NON-EXISTENT-SCENARIO-XYZ/red-zone/execute")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        log.info("✅ POST /api/v1/scenarios/NON-EXISTENT/red-zone/execute — 404 Not Found verified");
    }
}
