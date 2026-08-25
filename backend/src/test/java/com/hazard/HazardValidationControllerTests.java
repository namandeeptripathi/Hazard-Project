package com.hazard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc Integration Tests for Stage 3.8 — Hazard Validation API Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class HazardValidationControllerTests {

    private static final Logger log = LoggerFactory.getLogger(HazardValidationControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("API 3.8.1: GET /api/v1/hazards/validation/report — Full Validation Report")
    void testValidationReportEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/validation/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.reportTitle").exists())
                .andExpect(jsonPath("$.data.generatedAt").exists())
                .andExpect(jsonPath("$.data.validationMethodology").exists())
                .andExpect(jsonPath("$.data.dataQualityCoverage").exists())
                .andExpect(jsonPath("$.data.validationTargets").isArray())
                .andExpect(jsonPath("$.data.validationTargets.length()").value(3))
                .andExpect(jsonPath("$.data.overallAssessment").exists())
                .andExpect(jsonPath("$.data.identifiedStrengths").isArray())
                .andExpect(jsonPath("$.data.identifiedWeaknesses").isArray())
                .andExpect(jsonPath("$.data.calibrationRecommendations").isArray())
                .andExpect(jsonPath("$.data.boundaryNote").exists())
                .andExpect(jsonPath("$.meta.stage").value("3.8"))
                .andExpect(jsonPath("$.meta.validationTargets").value(3));

        log.info("✅ GET /api/v1/hazards/validation/report — 200 OK with full report");
    }

    @Test
    @DisplayName("API 3.8.2: GET /api/v1/hazards/validation/ground-truth — Ground Truth Catalog")
    void testGroundTruthCatalogEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/validation/ground-truth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(76))
                .andExpect(jsonPath("$.meta.totalRecords").value(76))
                .andExpect(jsonPath("$.meta.dfoEvents").value(23))
                .andExpect(jsonPath("$.meta.emdatRecords").value(53));

        log.info("✅ GET /api/v1/hazards/validation/ground-truth — 200 OK with 76 records");
    }

    @Test
    @DisplayName("API 3.8.3: GET /api/v1/hazards/validation/coverage — Data Quality Coverage")
    void testDataQualityCoverageEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/validation/coverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.totalDfoEvents").value(23))
                .andExpect(jsonPath("$.data.dfoEventsWithValidGeometry").value(7))
                .andExpect(jsonPath("$.data.dfoEventsWithSentinelCoordinates").value(16))
                .andExpect(jsonPath("$.data.totalEmdatRecords").value(53))
                .andExpect(jsonPath("$.data.emdatRecordsUsableForValidation").value(0))
                .andExpect(jsonPath("$.data.totalWeatherStations").value(3))
                .andExpect(jsonPath("$.data.temporalOverlapAssessment").exists())
                .andExpect(jsonPath("$.data.exclusionReasons").isArray())
                .andExpect(jsonPath("$.meta.stage").value("3.8"));

        log.info("✅ GET /api/v1/hazards/validation/coverage — 200 OK with coverage data");
    }

    @Test
    @DisplayName("API 3.8.4: Validation Report — Flood Score Target Contains Statistical Warning")
    void testFloodScoreTargetStatisticalWarning() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/validation/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.validationTargets[0].validationTarget").value("FLOOD_HAZARD_SCORE"))
                .andExpect(jsonPath("$.data.validationTargets[0].statisticalWarning").exists())
                .andExpect(jsonPath("$.data.validationTargets[0].eventPeriodMeanScore").exists());

        log.info("✅ Flood score validation target includes statistical warning and mean score");
    }

    @Test
    @DisplayName("API 3.8.5: Validation Report — Multi-Hazard Target Reports Insufficient Data")
    void testMultiHazardTargetInsufficientData() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/validation/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.validationTargets[2].validationTarget").value("MULTI_HAZARD_INDEX"))
                .andExpect(jsonPath("$.data.validationTargets[2].usableGroundTruthEvents").value(0))
                .andExpect(jsonPath("$.data.validationTargets[2].statisticalWarning").exists());

        log.info("✅ Multi-hazard validation target correctly reports zero usable ground truth");
    }
}
