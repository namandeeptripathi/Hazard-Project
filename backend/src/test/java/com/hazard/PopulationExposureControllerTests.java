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
 * MockMvc Integration Tests for PopulationExposureController REST Endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class PopulationExposureControllerTests {

    private static final Logger log = LoggerFactory.getLogger(PopulationExposureControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("API 4.1.1: GET /api/v1/exposure/population/district/Patna — District Exposure")
    void testGetDistrictPopulationExposure() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/population/district/Patna"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.districtName").value("Patna"))
                .andExpect(jsonPath("$.data.totalPopulation", greaterThan(1000000)))
                .andExpect(jsonPath("$.data.exposurePercentage").isNumber())
                .andExpect(jsonPath("$.data.exposureScore").isNumber())
                .andExpect(jsonPath("$.data.exposureCategory").exists())
                .andExpect(jsonPath("$.data.affectedSettlementsSummary").isArray())
                .andExpect(jsonPath("$.meta.stage").value("4.1"));

        log.info("✅ GET /api/v1/exposure/population/district/Patna — 200 OK");
    }

    @Test
    @DisplayName("API 4.1.2: GET /api/v1/exposure/population/all-districts — All 38 Districts Summary")
    void testGetAllDistrictsPopulationExposure() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/population/all-districts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(38))
                .andExpect(jsonPath("$.meta.totalDistricts").value(38));

        log.info("✅ GET /api/v1/exposure/population/all-districts — 200 OK with 38 districts");
    }

    @Test
    @DisplayName("API 4.1.3: GET /api/v1/exposure/population/hazard-event/DFO-3 — Event Buffer Overlay")
    void testGetHazardEventPopulationExposure() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/population/hazard-event/DFO-3?bufferMeters=5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hazardIdentifier").value("DFO-3"))
                .andExpect(jsonPath("$.data.exposedPopulation", greaterThan(0)))
                .andExpect(jsonPath("$.data.exposureCategory").exists());

        log.info("✅ GET /api/v1/exposure/population/hazard-event/DFO-3 — 200 OK");
    }

    @Test
    @DisplayName("API 4.1.4: POST /api/v1/exposure/population/analyze-geometry — Custom Polygon Overlay")
    void testAnalyzeCustomGeometry() throws Exception {
        String requestBody = """
                {
                    "wktGeometry": "POLYGON((85.0 25.5, 85.5 25.5, 85.5 26.0, 85.0 26.0, 85.0 25.5))",
                    "hazardIdentifier": "CUSTOM-ZONE-1",
                    "hazardType": "FLOOD",
                    "associatedDistrict": "Patna"
                }
                """;

        mockMvc.perform(post("/api/v1/exposure/population/analyze-geometry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hazardIdentifier").value("CUSTOM-ZONE-1"))
                .andExpect(jsonPath("$.data.intersectingSettlementsCount", greaterThan(0)));

        log.info("✅ POST /api/v1/exposure/population/analyze-geometry — 200 OK");
    }

    @Test
    @DisplayName("API 4.1.5: GET /api/v1/exposure/population/geojson — GeoJSON Layer")
    void testGetPopulationExposureGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/population/geojson"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features.length()").value(38))
                .andExpect(jsonPath("$.features[0].properties.layerId").value("POPULATION_EXPOSURE"));

        log.info("✅ GET /api/v1/exposure/population/geojson — 200 OK");
    }

    @Test
    @DisplayName("API 4.1.6: GET /api/v1/exposure/population/config — Configuration Parameters")
    void testGetExposureConfiguration() throws Exception {
        mockMvc.perform(get("/api/v1/exposure/population/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lowThresholdPercent").value(15.0))
                .andExpect(jsonPath("$.data.moderateThresholdPercent").value(40.0))
                .andExpect(jsonPath("$.data.highThresholdPercent").value(70.0));

        log.info("✅ GET /api/v1/exposure/population/config — 200 OK");
    }

    @Test
    @DisplayName("API 4.1.7: Error Handling — Unknown District 404 & Invalid Geometry 400")
    void testErrorHandling() throws Exception {
        // Unknown district -> 404
        mockMvc.perform(get("/api/v1/exposure/population/district/UnknownDistrictXYZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Invalid geometry (Point instead of Polygon) -> 400
        String badRequestBody = """
                {
                    "wktGeometry": "POINT(85.0 25.5)",
                    "hazardIdentifier": "BAD-GEO"
                }
                """;
        mockMvc.perform(post("/api/v1/exposure/population/analyze-geometry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        log.info("✅ Error handling verified (404 for unknown district, 400 for invalid geometry)");
    }
}
