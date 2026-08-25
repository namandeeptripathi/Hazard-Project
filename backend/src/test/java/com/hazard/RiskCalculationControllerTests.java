package com.hazard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc Integration Tests for RiskCalculationController REST Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class RiskCalculationControllerTests {

    private static final Logger log = LoggerFactory.getLogger(RiskCalculationControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("API 4.7.1: GET /api/v1/risk/district/Sitamarhi — District Risk Profile")
    void testGetDistrictRiskScore() throws Exception {
        mockMvc.perform(get("/api/v1/risk/district/Sitamarhi"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.riskScore").isNumber())
                .andExpect(jsonPath("$.data.riskScore100").isNumber())
                .andExpect(jsonPath("$.data.riskTier").exists())
                .andExpect(jsonPath("$.data.components.HAZARD").exists())
                .andExpect(jsonPath("$.data.components.EXPOSURE").exists())
                .andExpect(jsonPath("$.data.components.VULNERABILITY").exists())
                .andExpect(jsonPath("$.data.components.HISTORICAL").exists())
                .andExpect(jsonPath("$.meta.stage").value("4.7"))
                .andExpect(jsonPath("$.meta.substage").value("Risk Calculation"));

        log.info("✅ GET /api/v1/risk/district/Sitamarhi — 200 OK");
    }

    @Test
    @DisplayName("API 4.7.2: GET /api/v1/risk/district/Sitamarhi/contributors — Risk Contributors")
    void testGetDistrictRiskContributors() throws Exception {
        mockMvc.perform(get("/api/v1/risk/district/Sitamarhi/contributors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.topDrivers").isArray())
                .andExpect(jsonPath("$.data.exposureBreakdown").exists())
                .andExpect(jsonPath("$.meta.stage").value("4.7"));

        log.info("✅ GET /api/v1/risk/district/Sitamarhi/contributors — 200 OK");
    }

    @Test
    @DisplayName("API 4.7.3: GET /api/v1/risk/all — All 38 Districts Risk Scores")
    void testGetAllDistrictsRiskScores() throws Exception {
        mockMvc.perform(get("/api/v1/risk/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(38))
                .andExpect(jsonPath("$.meta.totalDistricts").value(38));

        log.info("✅ GET /api/v1/risk/all — 200 OK with 38 districts");
    }

    @Test
    @DisplayName("API 4.7.4: GET /api/v1/risk/geojson — Risk Choropleth GeoJSON")
    void testGetRiskGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/risk/geojson"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features.length()").value(38))
                .andExpect(jsonPath("$.features[0].properties.riskScore100").isNumber())
                .andExpect(jsonPath("$.features[0].properties.riskTier").exists());

        log.info("✅ GET /api/v1/risk/geojson — 200 OK with 38 polygon features");
    }

    @Test
    @DisplayName("API 4.7.5: GET /api/v1/risk/config — Risk Configuration")
    void testGetRiskConfig() throws Exception {
        mockMvc.perform(get("/api/v1/risk/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.riskComponentWeights").exists())
                .andExpect(jsonPath("$.data.exposureSubWeights").exists())
                .andExpect(jsonPath("$.data.calculationVersion").value("v1.0"));

        log.info("✅ GET /api/v1/risk/config — 200 OK");
    }

    @Test
    @DisplayName("API 4.7.6: POST /api/v1/risk/analyze — Custom Risk POST")
    void testAnalyzeRisk() throws Exception {
        String jsonPayload = """
                {
                    "districtName": "Sitamarhi"
                }
                """;

        mockMvc.perform(post("/api/v1/risk/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtName").value("Sitamarhi"));

        log.info("✅ POST /api/v1/risk/analyze — 200 OK");
    }

    @Test
    @DisplayName("API 4.7.7: Error Handling — Unknown District")
    void testErrorHandling() throws Exception {
        mockMvc.perform(get("/api/v1/risk/district/UnknownDistrict999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        log.info("✅ Error handling verified (404 status code)");
    }
}
