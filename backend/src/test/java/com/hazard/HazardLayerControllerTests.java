package com.hazard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HazardLayerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. GET /api/v1/hazards/layers - List Layer Catalog")
    void testGetLayerCatalog() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/layers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalAvailableLayers", is(8)))
                .andExpect(jsonPath("$.layers", hasSize(8)))
                .andExpect(jsonPath("$.layers[0].layerId", notNullValue()))
                .andExpect(jsonPath("$.layers[0].category", notNullValue()));
    }

    @Test
    @DisplayName("2. GET /api/v1/hazards/layers/{layerId}/metadata - Single Layer Metadata")
    void testGetLayerMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/layers/FLOOD_HAZARD_SCORES/metadata")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.layerId", is("FLOOD_HAZARD_SCORES")))
                .andExpect(jsonPath("$.category", is("HAZARD_SCORE_LAYER")))
                .andExpect(jsonPath("$.geometryType", is("Point")))
                .andExpect(jsonPath("$.hasScore", is(true)))
                .andExpect(jsonPath("$.hasSeverityTier", is(true)));
    }

    @Test
    @DisplayName("3. GET /api/v1/hazards/layers/FLOOD_EVENTS - Flood Events GeoJSON")
    void testGetFloodEventsLayer() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/layers/FLOOD_EVENTS")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.count", is(7)))
                .andExpect(jsonPath("$.features", hasSize(7)))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].properties.hazardType", is("FLOOD")));
    }

    @Test
    @DisplayName("4. GET /api/v1/hazards/layers/DISTRICT_HAZARD_SUMMARIES - District Summaries MultiPolygon GeoJSON")
    void testGetDistrictHazardSummariesLayer() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/layers/DISTRICT_HAZARD_SUMMARIES")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.count", is(38)))
                .andExpect(jsonPath("$.features", hasSize(38)))
                .andExpect(jsonPath("$.features[0].geometry.type", isIn(new String[]{"MultiPolygon", "Polygon"})))
                .andExpect(jsonPath("$.features[0].properties.districtName", notNullValue()))
                .andExpect(jsonPath("$.features[0].properties.peakMultiHazardIndex", notNullValue()));
    }

    @Test
    @DisplayName("5. GET /api/v1/hazards/layers/DISTRICT_BOUNDARIES - Administrative Boundaries GeoJSON")
    void testGetDistrictBoundariesLayer() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/layers/DISTRICT_BOUNDARIES")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.count", is(38)));
    }

    @Test
    @DisplayName("6. GET /api/v1/hazards/layers/RIVERS_REFERENCE - Rivers MultiLineString GeoJSON")
    void testGetRiversReferenceLayer() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/layers/RIVERS_REFERENCE")
                        .param("limit", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features", not(empty())))
                .andExpect(jsonPath("$.features[0].geometry.type", isIn(new String[]{"MultiLineString", "LineString"})));
    }
}
