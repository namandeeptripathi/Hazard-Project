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
class HazardNormalizationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. GET /api/v1/hazards/normalized - List Normalized Observations")
    void testGetAllNormalizedHazards() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/normalized")
                        .param("limit", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(15)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].normalizedMetrics", notNullValue()));
    }

    @Test
    @DisplayName("2. GET /api/v1/hazards/normalized/{id} - Normalized DFO Event")
    void testGetNormalizedHazardById() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/normalized/DFO-3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("DFO-3")))
                .andExpect(jsonPath("$.qualityStatus", is("VALID")))
                .andExpect(jsonPath("$.associatedDistrict", is("Sitamarhi")))
                .andExpect(jsonPath("$.normalizedMetrics.FLOOD_SEVERITY_INDEX.rawValue", is(1.5)))
                .andExpect(jsonPath("$.normalizedMetrics.FLOOD_SEVERITY_INDEX.normalizedValue", is(0.5)));
    }

    @Test
    @DisplayName("3. GET /api/v1/hazards/normalized/metric/{metricName} - Filter by Metric Name")
    void testGetNormalizedHazardsByMetric() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/normalized/metric/FLOOD_DURATION_DAYS")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].normalizedMetrics.FLOOD_DURATION_DAYS", notNullValue()));
    }

    @Test
    @DisplayName("4. GET /api/v1/hazards/normalized/district/{districtName} - Filter by District")
    void testGetNormalizedHazardsInDistrict() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/normalized/district/Sitamarhi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].associatedDistrict", is("Sitamarhi")));
    }

    @Test
    @DisplayName("5. GET /api/v1/hazards/normalized/rainfall/daily - Normalized Daily Rainfall")
    void testGetNormalizedDailyRainfall() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/normalized/rainfall/daily")
                        .param("stationName", "Patna")
                        .param("startDate", "2020-06-25")
                        .param("endDate", "2020-07-05")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(11)))
                .andExpect(jsonPath("$[4].date", is("2020-06-29")))
                .andExpect(jsonPath("$[4].rawDailyTotalMm", is(101.7)))
                .andExpect(jsonPath("$[4].normalizedDailyTotal.normalizedValue", is(0.678)))
                .andExpect(jsonPath("$[4].normalizedPeakHourly.normalizedValue", is(0.83)));
    }

    @Test
    @DisplayName("6. GET /api/v1/hazards/normalized/rainfall/rolling - Normalized Rolling Rainfall")
    void testGetNormalizedRollingRainfall() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/normalized/rainfall/rolling")
                        .param("stationName", "Patna")
                        .param("targetTime", "2020-06-29T18:00:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationName", is("Patna")))
                .andExpect(jsonPath("$.rolling24h.normalizedValue", notNullValue()))
                .andExpect(jsonPath("$.rolling24h.normalizedValue", greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.rolling24h.normalizedValue", lessThanOrEqualTo(1.0)));
    }

    @Test
    @DisplayName("7. GET /api/v1/hazards/normalized/summary - Executive Normalization Summary")
    void testGetNormalizationSummary() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/normalized/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalCrs", is("EPSG:4326 (WGS 84)")))
                .andExpect(jsonPath("$.totalConfiguredMetrics", greaterThanOrEqualTo(12)))
                .andExpect(jsonPath("$.configuredMetrics", not(empty())))
                .andExpect(jsonPath("$.activeStations", hasItems("Bhagalpur", "Muzaffarpur", "Patna")));
    }

    @Test
    @DisplayName("8. GET /api/v1/hazards/normalized/geojson - GeoJSON with Normalized Properties")
    void testGetNormalizedHazardsGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/normalized/geojson")
                        .param("type", "FLOOD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.count", is(7)))
                .andExpect(jsonPath("$.features", hasSize(7)))
                .andExpect(jsonPath("$.features[0].properties.normalizedMetrics", notNullValue()));
    }
}
