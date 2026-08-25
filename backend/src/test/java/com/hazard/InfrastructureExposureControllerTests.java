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
 * MockMvc Integration Tests for InfrastructureExposureController REST Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class InfrastructureExposureControllerTests {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureExposureControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("API 4.3.1: GET /api/v1/exposure/infrastructure/hazard-event/DFO-3 — Event Infrastructure Overlay")
    void testGetHazardEventInfrastructureExposure() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/infrastructure/hazard-event/DFO-3?bufferMeters=5000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hazardIdentifier").value("DFO-3"))
                .andExpect(jsonPath("$.data.exposedAssetsCount", greaterThan(0)))
                .andExpect(jsonPath("$.data.exposedAssets").isArray())
                .andExpect(jsonPath("$.meta.stage").value("4.3"))
                .andExpect(jsonPath("$.meta.substage").value("Infrastructure Exposure"));

        log.info("✅ GET /api/v1/exposure/infrastructure/hazard-event/DFO-3 — 200 OK");
    }

    @Test
    @DisplayName("API 4.3.2: GET /api/v1/exposure/infrastructure/district/Patna — District Infrastructure Exposure")
    void testGetDistrictInfrastructureExposure() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/infrastructure/district/Patna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtName").value("Patna"))
                .andExpect(jsonPath("$.data.totalAssetsEvaluated", greaterThan(0)))
                .andExpect(jsonPath("$.data.categoryBreakdown").exists())
                .andExpect(jsonPath("$.data.criticalityBreakdown").exists())
                .andExpect(jsonPath("$.data.exposedAssets").isArray())
                .andExpect(jsonPath("$.meta.stage").value("4.3"));

        log.info("✅ GET /api/v1/exposure/infrastructure/district/Patna — 200 OK");
    }

    @Test
    @DisplayName("API 4.3.3: GET /api/v1/exposure/infrastructure/all — All 38 Districts Infrastructure Summary")
    void testGetAllDistrictsInfrastructureExposure() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/infrastructure/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(38))
                .andExpect(jsonPath("$.meta.totalDistricts").value(38));

        log.info("✅ GET /api/v1/exposure/infrastructure/all — 200 OK with 38 districts");
    }

    @Test
    @DisplayName("API 4.3.4: POST /api/v1/exposure/infrastructure/analyze-geometry — Custom WKT Polygon Overlay")
    void testAnalyzeCustomGeometryInfrastructure() throws Exception {
        String requestBody = """
                {
                    "wktGeometry": "POLYGON((85.0 25.5, 85.5 25.5, 85.5 26.0, 85.0 26.0, 85.0 25.5))",
                    "hazardIdentifier": "CUSTOM-INFRA-ZONE",
                    "hazardType": "FLOOD",
                    "associatedDistrict": "Patna"
                }
                """;

        mockMvc.perform(post("/api/v1/exposure/infrastructure/analyze-geometry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hazardIdentifier").value("CUSTOM-INFRA-ZONE"))
                .andExpect(jsonPath("$.data.exposedAssetsCount", greaterThan(0)))
                .andExpect(jsonPath("$.data.exposedAssets").isArray());

        log.info("✅ POST /api/v1/exposure/infrastructure/analyze-geometry — 200 OK");
    }

    @Test
    @DisplayName("API 4.3.5: GET /api/v1/exposure/infrastructure/geojson — GeoJSON Infrastructure Features")
    void testGetInfrastructureExposureGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/infrastructure/geojson?district=Patna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features[0].properties.layerId").value("INFRASTRUCTURE_EXPOSURE"))
                .andExpect(jsonPath("$.features[0].properties.criticality").exists());

        log.info("✅ GET /api/v1/exposure/infrastructure/geojson — 200 OK");
    }

    @Test
    @DisplayName("API 4.3.6: GET /api/v1/exposure/infrastructure/config — Configuration Endpoint")
    void testGetInfrastructureExposureConfig() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/infrastructure/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.criticalityMultipliers").exists())
                .andExpect(jsonPath("$.data.defaultCategoryCriticality").exists());

        log.info("✅ GET /api/v1/exposure/infrastructure/config — 200 OK");
    }

    @Test
    @DisplayName("API 4.3.7: Error Handling — Unknown District & Invalid Geometry")
    void testErrorHandling() throws Exception {
        // Unknown district -> 404
        mockMvc.perform(get("/api/v1/exposure/infrastructure/district/UnknownDistrict999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Invalid geometry -> 400
        String badRequestBody = """
                {
                    "wktGeometry": "POINT(85.0 25.5)",
                    "hazardIdentifier": "BAD-GEO"
                }
                """;
        mockMvc.perform(post("/api/v1/exposure/infrastructure/analyze-geometry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        log.info("✅ Error handling verified (404 and 400 status codes)");
    }
}
