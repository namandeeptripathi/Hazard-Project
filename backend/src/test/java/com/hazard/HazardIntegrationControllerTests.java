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
class HazardIntegrationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. GET /api/v1/hazards - List Integrated Hazards")
    void testGetAllHazards() throws Exception {
        mockMvc.perform(get("/api/v1/hazards")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].hazardType", notNullValue()))
                .andExpect(jsonPath("$[0].dataSource", notNullValue()));
    }

    @Test
    @DisplayName("2. GET /api/v1/hazards/{id} - Retrieve DFO and EM-DAT Hazard by ID")
    void testGetHazardById() throws Exception {
        // DFO hazard
        mockMvc.perform(get("/api/v1/hazards/DFO-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("DFO-1")))
                .andExpect(jsonPath("$.hazardType", is("FLOOD")))
                .andExpect(jsonPath("$.dataSource", is("DFO")))
                .andExpect(jsonPath("$.longitude", notNullValue()))
                .andExpect(jsonPath("$.latitude", notNullValue()));

        // EM-DAT hazard
        mockMvc.perform(get("/api/v1/hazards/EMDAT-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("EMDAT-1")))
                .andExpect(jsonPath("$.hazardType", is("FLOOD")))
                .andExpect(jsonPath("$.dataSource", is("EM_DAT")));

        // Non-existent hazard (404)
        mockMvc.perform(get("/api/v1/hazards/DFO-99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("3. GET /api/v1/hazards/type/{type} - Filter by Hazard Type")
    void testGetHazardsByType() throws Exception {
        // FLOOD type
        mockMvc.perform(get("/api/v1/hazards/type/FLOOD")
                        .param("limit", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)))
                .andExpect(jsonPath("$[0].hazardType", is("FLOOD")));

        // EXTREME_RAINFALL type
        mockMvc.perform(get("/api/v1/hazards/type/EXTREME_RAINFALL")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].hazardType", is("EXTREME_RAINFALL")));

        // Invalid type (400)
        mockMvc.perform(get("/api/v1/hazards/type/VOLCANO")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Unsupported hazard type")));
    }

    @Test
    @DisplayName("4. GET /api/v1/hazards/district/{districtName} - Spatial District Query")
    void testGetHazardsInDistrict() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/district/Sitamarhi")
                        .param("type", "FLOOD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].hazardType", is("FLOOD")));
    }

    @Test
    @DisplayName("5. GET /api/v1/hazards/nearby - Proximity Query")
    void testGetHazardsNearby() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/nearby")
                        .param("longitude", "85.39")
                        .param("latitude", "26.12")
                        .param("radiusMeters", "50000")
                        .param("type", "FLOOD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // Invalid latitude (400)
        mockMvc.perform(get("/api/v1/hazards/nearby")
                        .param("longitude", "85.39")
                        .param("latitude", "150.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("6. GET /api/v1/hazards/bbox - Bounding Box Query")
    void testGetHazardsInBoundingBox() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/bbox")
                        .param("minLon", "84.5")
                        .param("minLat", "25.5")
                        .param("maxLon", "86.5")
                        .param("maxLat", "27.0")
                        .param("type", "FLOOD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // Inverted bbox minLon > maxLon (400)
        mockMvc.perform(get("/api/v1/hazards/bbox")
                        .param("minLon", "87.0")
                        .param("minLat", "25.5")
                        .param("maxLon", "86.0")
                        .param("maxLat", "27.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("7. GET /api/v1/hazards/time-range - Temporal Window Query")
    void testGetHazardsInTimeRange() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/time-range")
                        .param("startDate", "2000-01-01")
                        .param("endDate", "2010-12-31")
                        .param("type", "FLOOD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("8. GET /api/v1/hazards/rainfall/extreme - Meteorological Hazard Query")
    void testGetExtremeRainfallHazards() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/rainfall/extreme")
                        .param("thresholdMm", "15.0")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].hazardType", is("EXTREME_RAINFALL")))
                .andExpect(jsonPath("$[0].precipitationMm", greaterThanOrEqualTo(15.0)));
    }

    @Test
    @DisplayName("9. GET /api/v1/hazards/geojson - GeoJSON FeatureCollection Layer")
    void testGetHazardsGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/geojson")
                        .param("type", "FLOOD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features", not(empty())))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].properties.hazardType", is("FLOOD")));
    }

    @Test
    @DisplayName("10. GET /api/v1/hazards/summary - Executive Dataset Catalog Summary")
    void testGetHazardSummary() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalCrs", is("EPSG:4326 (WGS 84)")))
                .andExpect(jsonPath("$.dfoFloodEventsCount", is(23)))
                .andExpect(jsonPath("$.emdatFloodRecordsCount", is(53)))
                .andExpect(jsonPath("$.weatherObservationsCount", is(131544)))
                .andExpect(jsonPath("$.totalIntegratedRecords", is(131620)))
                .andExpect(jsonPath("$.activeHazardTypes", hasItems("FLOOD", "EXTREME_RAINFALL", "OTHER")))
                .andExpect(jsonPath("$.availableWeatherStations", hasItems("Bhagalpur", "Muzaffarpur", "Patna")));
    }
}
