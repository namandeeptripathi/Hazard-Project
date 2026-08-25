package com.hazard;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Controller-level integration tests for Stage 4.9 — Risk Contributors REST APIs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Stage 4.9: Risk Contributor Controller Tests")
public class RiskContributorControllerTests {

    private static final Logger log = LoggerFactory.getLogger(RiskContributorControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/risk/contributors/district/Sitamarhi — 200 OK with full profile")
    void testGetDistrictContributorsProfile() throws Exception {
        mockMvc.perform(get("/api/v1/risk/contributors/district/Sitamarhi")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.districtName", is("Sitamarhi")))
                .andExpect(jsonPath("$.data.riskScore", notNullValue()))
                .andExpect(jsonPath("$.data.riskTier", notNullValue()))
                .andExpect(jsonPath("$.data.topContributors", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.contributorTree", notNullValue()))
                .andExpect(jsonPath("$.data.explanation", notNullValue()))
                .andExpect(jsonPath("$.data.mathematicalCheck.isContributionConsistent", is(true)));

        log.info("✅ GET /api/v1/risk/contributors/district/Sitamarhi — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/contributors/district/Sitamarhi/top — 200 OK with ranked drivers")
    void testGetTopContributors() throws Exception {
        mockMvc.perform(get("/api/v1/risk/contributors/district/Sitamarhi/top")
                        .param("limit", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].rank", is(1)))
                .andExpect(jsonPath("$.data[0].contributionPercent", notNullValue()));

        log.info("✅ GET /api/v1/risk/contributors/district/Sitamarhi/top — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/contributors/district/Sitamarhi/tree — 200 OK with tree root")
    void testGetContributorTree() throws Exception {
        mockMvc.perform(get("/api/v1/risk/contributors/district/Sitamarhi/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is("TOTAL_RISK")))
                .andExpect(jsonPath("$.data.children", hasSize(4)));

        log.info("✅ GET /api/v1/risk/contributors/district/Sitamarhi/tree — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/contributors/district/Sitamarhi/explanation — 200 OK with narrative")
    void testGetRiskExplanation() throws Exception {
        mockMvc.perform(get("/api/v1/risk/contributors/district/Sitamarhi/explanation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.summaryHeadline", notNullValue()))
                .andExpect(jsonPath("$.data.narrative", notNullValue()))
                .andExpect(jsonPath("$.data.primaryDrivers", hasSize(greaterThanOrEqualTo(1))));

        log.info("✅ GET /api/v1/risk/contributors/district/Sitamarhi/explanation — 200 OK");
    }

    @Test
    @DisplayName("POST /api/v1/risk/contributors/analyze — 200 OK with scenario contributors")
    void testAnalyzeScenarioContributors() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("districtName", "Sitamarhi");
        req.put("scenarioName", "Severe Flood Vulnerability Scenario");

        Map<String, Double> overrides = new HashMap<>();
        overrides.put("HAZARD", 0.50);
        overrides.put("EXPOSURE", 0.30);
        overrides.put("VULNERABILITY", 0.10);
        overrides.put("HISTORICAL", 0.10);
        req.put("overrideWeights", overrides);

        mockMvc.perform(post("/api/v1/risk/contributors/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.meta.mode", is("SCENARIO_ANALYSIS")))
                .andExpect(jsonPath("$.data.deltaRiskScore", notNullValue()));

        log.info("✅ POST /api/v1/risk/contributors/analyze — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/contributors/district/UnknownDistrict999 — 404 Not Found")
    void testUnknownDistrict() throws Exception {
        mockMvc.perform(get("/api/v1/risk/contributors/district/UnknownDistrict999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));

        log.info("✅ Error handling verified (404 status code)");
    }
}
