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
 * Controller-level integration tests for Stage 4.10 — Explainable Risk REST APIs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Stage 4.10: Risk Explanation Controller Tests")
public class RiskExplanationControllerTests {

    private static final Logger log = LoggerFactory.getLogger(RiskExplanationControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/risk/explanation/district/Sitamarhi — 200 OK with full explainability profile")
    void testGetDistrictExplainabilityProfile() throws Exception {
        mockMvc.perform(get("/api/v1/risk/explanation/district/Sitamarhi")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.districtName", is("Sitamarhi")))
                .andExpect(jsonPath("$.data.explanationVersion", is("explain-v1")))
                .andExpect(jsonPath("$.data.summary.executiveSummary", notNullValue()))
                .andExpect(jsonPath("$.data.primaryDrivers", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.evidenceItems", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.calculationTrace.isReconciled", is(true)))
                .andExpect(jsonPath("$.data.sensitivityAnalysis", hasSize(4)))
                .andExpect(jsonPath("$.data.modelLimitations", hasSize(greaterThanOrEqualTo(1))));

        log.info("✅ GET /api/v1/risk/explanation/district/Sitamarhi — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/explanation/district/Sitamarhi/summary — 200 OK with summaries")
    void testGetExplanationSummary() throws Exception {
        mockMvc.perform(get("/api/v1/risk/explanation/district/Sitamarhi/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.executiveSummary", notNullValue()))
                .andExpect(jsonPath("$.data.shortSummary", notNullValue()))
                .andExpect(jsonPath("$.data.detailedNarrative", notNullValue()));

        log.info("✅ GET /api/v1/risk/explanation/district/Sitamarhi/summary — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/explanation/district/Sitamarhi/evidence — 200 OK with evidence catalog")
    void testGetEvidenceItems() throws Exception {
        mockMvc.perform(get("/api/v1/risk/explanation/district/Sitamarhi/evidence")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$.data[0].evidenceId", notNullValue()))
                .andExpect(jsonPath("$.data[0].provenance", notNullValue()));

        log.info("✅ GET /api/v1/risk/explanation/district/Sitamarhi/evidence — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/explanation/district/Sitamarhi/calculation — 200 OK with trace")
    void testGetCalculationTrace() throws Exception {
        mockMvc.perform(get("/api/v1/risk/explanation/district/Sitamarhi/calculation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.formulaString", notNullValue()))
                .andExpect(jsonPath("$.data.isReconciled", is(true)))
                .andExpect(jsonPath("$.data.components", hasSize(4)));

        log.info("✅ GET /api/v1/risk/explanation/district/Sitamarhi/calculation — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/explanation/district/Sitamarhi/sensitivity — 200 OK with leverage ranking")
    void testGetSensitivityAnalysis() throws Exception {
        mockMvc.perform(get("/api/v1/risk/explanation/district/Sitamarhi/sensitivity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].leverageRank", is(1)))
                .andExpect(jsonPath("$.data[0].absoluteLeverageImpact", notNullValue()));

        log.info("✅ GET /api/v1/risk/explanation/district/Sitamarhi/sensitivity — 200 OK");
    }

    @Test
    @DisplayName("POST /api/v1/risk/explanation/analyze — 200 OK with scenario explainability")
    void testAnalyzeScenarioExplainability() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("districtName", "Sitamarhi");
        req.put("scenarioName", "Severe Inundation Simulation");

        Map<String, Double> overrides = new HashMap<>();
        overrides.put("HAZARD", 0.55);
        overrides.put("EXPOSURE", 0.25);
        overrides.put("VULNERABILITY", 0.10);
        overrides.put("HISTORICAL", 0.10);
        req.put("overrideWeights", overrides);

        mockMvc.perform(post("/api/v1/risk/explanation/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.meta.mode", is("SCENARIO_ANALYSIS")))
                .andExpect(jsonPath("$.data.deltaRiskScore", notNullValue()));

        log.info("✅ POST /api/v1/risk/explanation/analyze — 200 OK");
    }

    @Test
    @DisplayName("GET /api/v1/risk/explanation/district/UnknownDistrict999 — 404 Not Found")
    void testUnknownDistrict() throws Exception {
        mockMvc.perform(get("/api/v1/risk/explanation/district/UnknownDistrict999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));

        log.info("✅ Error handling verified (404 status code)");
    }
}
