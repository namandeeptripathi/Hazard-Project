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
class HazardScoringControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. GET /api/v1/hazards/scores - List Scored Observations")
    void testGetAllHazardScores() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/scores")
                        .param("limit", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(15)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].hazardScore", notNullValue()))
                .andExpect(jsonPath("$[0].severityTier", notNullValue()))
                .andExpect(jsonPath("$[0].metricContributions", not(empty())));
    }

    @Test
    @DisplayName("2. GET /api/v1/hazards/scores/{id} - Single Scored Observation")
    void testGetHazardScoreById() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/scores/DFO-3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("DFO-3")))
                .andExpect(jsonPath("$.hazardType", is("FLOOD")))
                .andExpect(jsonPath("$.associatedDistrict", is("Sitamarhi")))
                .andExpect(jsonPath("$.hazardScore", notNullValue()))
                .andExpect(jsonPath("$.severityTier", notNullValue()))
                .andExpect(jsonPath("$.scoringMethod", is("WEIGHTED_MULTI_CRITERIA_HAZARD_INDEX")))
                .andExpect(jsonPath("$.metricContributions", not(empty())));
    }

    @Test
    @DisplayName("3. GET /api/v1/hazards/scores/type/{type} - Type Filter")
    void testGetHazardScoresByType() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/scores/type/FLOOD")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].hazardType", is("FLOOD")));
    }

    @Test
    @DisplayName("4. GET /api/v1/hazards/scores/district/{districtName} - District Filter")
    void testGetHazardScoresInDistrict() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/scores/district/Sitamarhi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].associatedDistrict", is("Sitamarhi")));
    }

    @Test
    @DisplayName("5. GET /api/v1/hazards/scores/rainfall/daily - Scored Daily Rainfall")
    void testGetScoredDailyRainfall() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/scores/rainfall/daily")
                        .param("stationName", "Patna")
                        .param("startDate", "2020-06-25")
                        .param("endDate", "2020-07-05")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(11)))
                .andExpect(jsonPath("$[4].date", is("2020-06-29")))
                .andExpect(jsonPath("$[4].rainfallHazardScore", notNullValue()))
                .andExpect(jsonPath("$[4].severityTier", is("HIGH")))
                .andExpect(jsonPath("$[4].metricContributions", hasSize(2)));
    }

    @Test
    @DisplayName("6. GET /api/v1/hazards/scores/rainfall/rolling - Scored Rolling Rainfall")
    void testGetScoredRollingRainfall() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/scores/rainfall/rolling")
                        .param("stationName", "Patna")
                        .param("targetTime", "2020-06-29T18:00:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationName", is("Patna")))
                .andExpect(jsonPath("$.rollingRainfallScore", notNullValue()))
                .andExpect(jsonPath("$.severityTier", notNullValue()))
                .andExpect(jsonPath("$.metricContributions", not(empty())));
    }

    @Test
    @DisplayName("7. GET /api/v1/hazards/scores/summary - Executive Scoring Summary")
    void testGetHazardScoringSummary() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/scores/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalCrs", is("EPSG:4326 (WGS 84)")))
                .andExpect(jsonPath("$.scoringFramework", notNullValue()))
                .andExpect(jsonPath("$.totalScoredObservations", greaterThan(0)))
                .andExpect(jsonPath("$.activeScoringConfigurations", hasSize(2)));
    }

    @Test
    @DisplayName("8. GET /api/v1/hazards/scores/geojson - GeoJSON with Hazard Score and Tier")
    void testGetHazardScoresGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/scores/geojson")
                        .param("type", "FLOOD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.count", is(7)))
                .andExpect(jsonPath("$.features", hasSize(7)))
                .andExpect(jsonPath("$.features[0].properties.hazardScore", notNullValue()))
                .andExpect(jsonPath("$.features[0].properties.severityTier", notNullValue()));
    }
}
