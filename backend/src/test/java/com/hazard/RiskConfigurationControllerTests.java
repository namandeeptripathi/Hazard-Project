package com.hazard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazard.dto.risk.config.RiskConfigurationRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-level integration tests for Stage 4.8 — Configurable Risk Weights REST APIs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Stage 4.8: Risk Configuration Controller Tests")
public class RiskConfigurationControllerTests {

    private static final Logger log = LoggerFactory.getLogger(RiskConfigurationControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/risk/config — 200 OK with active baseline configuration")
    void testGetActiveConfig() throws Exception {
        mockMvc.perform(get("/api/v1/risk/config")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.configId", notNullValue()))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.configuredTopLevelWeights.HAZARD", is(0.35)))
                .andExpect(jsonPath("$.data.configuredTopLevelWeights.EXPOSURE", is(0.30)))
                .andExpect(jsonPath("$.data.configuredTopLevelWeights.VULNERABILITY", is(0.25)))
                .andExpect(jsonPath("$.data.configuredTopLevelWeights.HISTORICAL", is(0.10)));

        log.info("✅ GET /api/v1/risk/config — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/config/all — 200 OK with list of configurations")
    void testGetAllConfigs() throws Exception {
        mockMvc.perform(get("/api/v1/risk/config/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(4))));

        log.info("✅ GET /api/v1/risk/config/all — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/config/presets — 200 OK with preset list")
    void testGetPresets() throws Exception {
        mockMvc.perform(get("/api/v1/risk/config/presets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));

        log.info("✅ GET /api/v1/risk/config/presets — 200 OK");
    }

    @Test
    @DisplayName("POST /api/v1/risk/config — 201 Created with valid custom weights")
    void testCreateConfig() throws Exception {
        RiskConfigurationRequestDto req = new RiskConfigurationRequestDto();
        req.setName("Urban Infrastructure Vulnerability Profile");
        req.setHazardWeight(0.30);
        req.setExposureWeight(0.35);
        req.setVulnerabilityWeight(0.25);
        req.setHistoricalWeight(0.10);
        req.setAuthor("MOCK-TESTER");

        mockMvc.perform(post("/api/v1/risk/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.configId", startsWith("risk-v")))
                .andExpect(jsonPath("$.data.status", is("INACTIVE")));

        log.info("✅ POST /api/v1/risk/config — 201 Created");
    }

    @Test
    @DisplayName("GET /api/v1/risk/config/diff — 200 OK comparing risk-v1 and risk-preset-hazard")
    void testGetDiff() throws Exception {
        mockMvc.perform(get("/api/v1/risk/config/diff")
                        .param("base", "risk-v1")
                        .param("target", "risk-preset-hazard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.baseConfigId", is("risk-v1")))
                .andExpect(jsonPath("$.data.targetConfigId", is("risk-preset-hazard")))
                .andExpect(jsonPath("$.data.topLevelWeightDiffs.HAZARD", hasSize(3)));

        log.info("✅ GET /api/v1/risk/config/diff — 200 OK");
    }

    @Test
    @DisplayName("POST /api/v1/risk/analyze — 200 OK with what-if scenario override")
    void testScenarioAnalysisEndpoint() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("districtName", "Sitamarhi");
        req.put("scenarioName", "High Severity Precipitation Scenario");

        Map<String, Double> overrides = new HashMap<>();
        overrides.put("HAZARD", 0.50);
        overrides.put("EXPOSURE", 0.25);
        overrides.put("VULNERABILITY", 0.15);
        overrides.put("HISTORICAL", 0.10);
        req.put("overrideWeights", overrides);

        mockMvc.perform(post("/api/v1/risk/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.meta.mode", is("SCENARIO_ANALYSIS")))
                .andExpect(jsonPath("$.data.deltaRiskScore", notNullValue()))
                .andExpect(jsonPath("$.data.productionConfigurationUnchanged", is(true)));

        log.info("✅ POST /api/v1/risk/analyze (Scenario) — 200 OK");
    }
}
