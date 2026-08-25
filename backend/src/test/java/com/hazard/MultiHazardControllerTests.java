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
class MultiHazardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. GET /api/v1/hazards/multi-hazard - List Multi-Hazard Observations")
    void testGetAllMultiHazardObservations() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/multi-hazard")
                        .param("limit", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(15))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].multiHazardIndex", notNullValue()))
                .andExpect(jsonPath("$[0].severityTier", notNullValue()))
                .andExpect(jsonPath("$[0].dominantHazard", notNullValue()));
    }

    @Test
    @DisplayName("2. GET /api/v1/hazards/multi-hazard/{id} - Single Multi-Hazard Observation")
    void testGetMultiHazardObservationById() throws Exception {
        // Fetch first ID dynamically from list
        String sampleId = "MULTI-DFO-3";

        mockMvc.perform(get("/api/v1/hazards/multi-hazard/" + sampleId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sampleId)))
                .andExpect(jsonPath("$.associatedDistrict", is("Sitamarhi")))
                .andExpect(jsonPath("$.multiHazardIndex", notNullValue()))
                .andExpect(jsonPath("$.severityTier", notNullValue()))
                .andExpect(jsonPath("$.scoringMethod", is("MULTI_HAZARD_WEIGHTED_COMPOSITE_INDEX")));
    }

    @Test
    @DisplayName("3. GET /api/v1/hazards/multi-hazard/district/{districtName} - District Filter")
    void testGetMultiHazardObservationsInDistrict() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/multi-hazard/district/Sitamarhi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].associatedDistrict", is("Sitamarhi")));
    }

    @Test
    @DisplayName("4. GET /api/v1/hazards/multi-hazard/summary - Executive Summary")
    void testGetMultiHazardSummary() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/multi-hazard/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalCrs", is("EPSG:4326 (WGS 84)")))
                .andExpect(jsonPath("$.totalMultiHazardObservations", greaterThan(0)))
                .andExpect(jsonPath("$.configuredHazardWeights.FLOOD", is(0.50)))
                .andExpect(jsonPath("$.configuredHazardWeights.EXTREME_RAINFALL", is(0.50)));
    }

    @Test
    @DisplayName("5. GET /api/v1/hazards/multi-hazard/geojson - GeoJSON FeatureCollection")
    void testGetMultiHazardGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/multi-hazard/geojson")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features", not(empty())))
                .andExpect(jsonPath("$.features[0].properties.multiHazardIndex", notNullValue()))
                .andExpect(jsonPath("$.features[0].properties.severityTier", notNullValue()))
                .andExpect(jsonPath("$.features[0].properties.dominantHazard", notNullValue()));
    }
}
