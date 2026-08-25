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
 * MockMvc Integration Tests for SettlementExposureController REST Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class SettlementExposureControllerTests {

    private static final Logger log = LoggerFactory.getLogger(SettlementExposureControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("API 4.2.1: GET /api/v1/exposure/settlements/hazard-event/DFO-3 — Event Settlement Overlay")
    void testGetHazardEventSettlementExposure() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/settlements/hazard-event/DFO-3?bufferMeters=5000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hazardIdentifier").value("DFO-3"))
                .andExpect(jsonPath("$.data.exposedSettlementsCount", greaterThan(0)))
                .andExpect(jsonPath("$.data.exposedSettlements").isArray())
                .andExpect(jsonPath("$.meta.stage").value("4.2"))
                .andExpect(jsonPath("$.meta.substage").value("Settlement Exposure"));

        log.info("✅ GET /api/v1/exposure/settlements/hazard-event/DFO-3 — 200 OK");
    }

    @Test
    @DisplayName("API 4.2.2: GET /api/v1/exposure/settlements/district/Sitamarhi — District Settlement Exposure")
    void testGetDistrictSettlementExposure() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/settlements/district/Sitamarhi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtName").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.totalSettlementsEvaluated", greaterThan(0)))
                .andExpect(jsonPath("$.data.categoryCounts").exists())
                .andExpect(jsonPath("$.data.settlements").isArray())
                .andExpect(jsonPath("$.meta.stage").value("4.2"));

        log.info("✅ GET /api/v1/exposure/settlements/district/Sitamarhi — 200 OK");
    }

    @Test
    @DisplayName("API 4.2.3: GET /api/v1/exposure/settlements/all — All 38 Districts Settlement Exposure")
    void testGetAllDistrictsSettlementExposure() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/settlements/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(38))
                .andExpect(jsonPath("$.meta.totalDistricts").value(38));

        log.info("✅ GET /api/v1/exposure/settlements/all — 200 OK with 38 districts");
    }

    @Test
    @DisplayName("API 4.2.4: POST /api/v1/exposure/settlements/analyze-geometry — Custom WKT Polygon Overlay")
    void testAnalyzeCustomGeometrySettlements() throws Exception {
        String requestBody = """
                {
                    "wktGeometry": "POLYGON((85.0 25.5, 85.5 25.5, 85.5 26.0, 85.0 26.0, 85.0 25.5))",
                    "hazardIdentifier": "CUSTOM-ZONE-1",
                    "hazardType": "FLOOD",
                    "associatedDistrict": "Patna"
                }
                """;

        mockMvc.perform(post("/api/v1/exposure/settlements/analyze-geometry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hazardIdentifier").value("CUSTOM-ZONE-1"))
                .andExpect(jsonPath("$.data.exposedSettlementsCount", greaterThan(0)))
                .andExpect(jsonPath("$.data.exposedSettlements").isArray());

        log.info("✅ POST /api/v1/exposure/settlements/analyze-geometry — 200 OK");
    }

    @Test
    @DisplayName("API 4.2.5: GET /api/v1/exposure/settlements/geojson — GeoJSON Settlement Points")
    void testGetSettlementExposureGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/settlements/geojson?district=Sitamarhi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features[0].geometry.type").value("Point"))
                .andExpect(jsonPath("$.features[0].properties.layerId").value("SETTLEMENT_EXPOSURE"))
                .andExpect(jsonPath("$.features[0].properties.settlementName").exists());

        log.info("✅ GET /api/v1/exposure/settlements/geojson — 200 OK with Point features");
    }

    @Test
    @DisplayName("API 4.2.6: Error Handling — Unknown District & Invalid Geometry")
    void testErrorHandling() throws Exception {
        // Unknown district -> 404
        mockMvc.perform(get("/api/v1/exposure/settlements/district/UnknownDistrict999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Invalid geometry -> 400
        String badRequestBody = """
                {
                    "wktGeometry": "POINT(85.0 25.5)",
                    "hazardIdentifier": "BAD-GEO"
                }
                """;
        mockMvc.perform(post("/api/v1/exposure/settlements/analyze-geometry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        log.info("✅ Error handling verified (404 and 400 status codes)");
    }
}
