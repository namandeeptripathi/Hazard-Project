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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc Integration Tests for HistoricalDisasterController REST Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class HistoricalDisasterControllerTests {

    private static final Logger log = LoggerFactory.getLogger(HistoricalDisasterControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("API 4.6.1: GET /api/v1/historical/district/Sitamarhi — District Historical Summary")
    void testGetDistrictHistoricalSummary() throws Exception {
        mockMvc.perform(get("/api/v1/historical/district/Sitamarhi"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.totalHistoricalEvents").isNumber())
                .andExpect(jsonPath("$.data.eventsPerYear").isNumber())
                .andExpect(jsonPath("$.data.severityStatistics").exists())
                .andExpect(jsonPath("$.data.recurrenceStatistics.status").value("EMPIRICAL_ONLY"))
                .andExpect(jsonPath("$.data.temporalPatterns").exists())
                .andExpect(jsonPath("$.meta.stage").value("4.6"))
                .andExpect(jsonPath("$.meta.substage").value("Historical Disaster Intelligence"));

        log.info("✅ GET /api/v1/historical/district/Sitamarhi — 200 OK");
    }

    @Test
    @DisplayName("API 4.6.2: GET /api/v1/historical/district/Sitamarhi/timeline — Historical Timeline")
    void testGetDistrictTimeline() throws Exception {
        mockMvc.perform(get("/api/v1/historical/district/Sitamarhi/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.meta.stage").value("4.6"));

        log.info("✅ GET /api/v1/historical/district/Sitamarhi/timeline — 200 OK");
    }

    @Test
    @DisplayName("API 4.6.3: GET /api/v1/historical/district/Sitamarhi/hazard/FLOOD — Hazard Filter")
    void testGetHistoricalByHazardType() throws Exception {
        mockMvc.perform(get("/api/v1/historical/district/Sitamarhi/hazard/FLOOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.hazardType").value("FLOOD"));

        log.info("✅ GET /api/v1/historical/district/Sitamarhi/hazard/FLOOD — 200 OK");
    }

    @Test
    @DisplayName("API 4.6.4: GET /api/v1/historical/all — All 38 Districts Historical Summaries")
    void testGetAllDistrictsHistoricalSummaries() throws Exception {
        mockMvc.perform(get("/api/v1/historical/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(38))
                .andExpect(jsonPath("$.meta.totalDistricts").value(38));

        log.info("✅ GET /api/v1/historical/all — 200 OK with 38 districts");
    }

    @Test
    @DisplayName("API 4.6.5: GET /api/v1/historical/hotspots — Historical Hotspots Ranking")
    void testGetHistoricalHotspots() throws Exception {
        mockMvc.perform(get("/api/v1/historical/hotspots?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].hotspotIndex").isNumber())
                .andExpect(jsonPath("$.data[0].hotspotTier").exists());

        log.info("✅ GET /api/v1/historical/hotspots — 200 OK");
    }

    @Test
    @DisplayName("API 4.6.6: GET /api/v1/historical/geojson — Historical Points GeoJSON")
    void testGetHistoricalGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/historical/geojson"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features").isArray());

        log.info("✅ GET /api/v1/historical/geojson — 200 OK");
    }

    @Test
    @DisplayName("API 4.6.7: GET /api/v1/historical/config — Configuration Parameters")
    void testGetHistoricalConfig() throws Exception {
        mockMvc.perform(get("/api/v1/historical/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.supportedHazardTypes").isArray())
                .andExpect(jsonPath("$.data.calculationVersion").value("v1.0"));

        log.info("✅ GET /api/v1/historical/config — 200 OK");
    }

    @Test
    @DisplayName("API 4.6.8: POST /api/v1/historical/analyze — Custom Query POST")
    void testAnalyzeHistoricalDisasters() throws Exception {
        String jsonPayload = """
                {
                    "districtName": "Sitamarhi",
                    "timeWindow": "ALL_HISTORY",
                    "hazardType": "FLOOD"
                }
                """;

        mockMvc.perform(post("/api/v1/historical/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtName").value("Sitamarhi"));

        log.info("✅ POST /api/v1/historical/analyze — 200 OK");
    }

    @Test
    @DisplayName("API 4.6.9: Error Handling — Unknown District")
    void testErrorHandling() throws Exception {
        mockMvc.perform(get("/api/v1/historical/district/UnknownDistrict999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        log.info("✅ Error handling verified (404 status code)");
    }
}
