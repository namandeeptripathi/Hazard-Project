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
class HazardProcessingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. GET /api/v1/hazards/processed - List Processed Observations")
    void testGetAllProcessedHazards() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/processed")
                        .param("limit", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(15)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].qualityStatus", notNullValue()))
                .andExpect(jsonPath("$[0].processingMetadata", notNullValue()));
    }

    @Test
    @DisplayName("2. GET /api/v1/hazards/processed/{id} - Located (VALID) vs Unlocated (UNLOCATED)")
    void testGetProcessedHazardById() throws Exception {
        // 1. Located DFO Event (VALID)
        mockMvc.perform(get("/api/v1/hazards/processed/DFO-3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("DFO-3")))
                .andExpect(jsonPath("$.qualityStatus", is("VALID")))
                .andExpect(jsonPath("$.associatedDistrict", is("Sitamarhi")))
                .andExpect(jsonPath("$.isWithinBiharBoundary", is(true)))
                .andExpect(jsonPath("$.longitude", is(85.5)))
                .andExpect(jsonPath("$.latitude", is(26.5)));

        // 2. Unlocated DFO Event with cleaned sentinel coordinates (UNLOCATED)
        mockMvc.perform(get("/api/v1/hazards/processed/DFO-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("DFO-8")))
                .andExpect(jsonPath("$.qualityStatus", is("UNLOCATED")))
                .andExpect(jsonPath("$.longitude", nullValue()))
                .andExpect(jsonPath("$.latitude", nullValue()))
                .andExpect(jsonPath("$.isWithinBiharBoundary", is(false)));

        // 3. Tabular EM-DAT Record (UNLOCATED)
        mockMvc.perform(get("/api/v1/hazards/processed/EMDAT-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("EMDAT-1")))
                .andExpect(jsonPath("$.qualityStatus", is("UNLOCATED")))
                .andExpect(jsonPath("$.derivedMetrics.averageDeathsPerEvent", notNullValue()));
    }

    @Test
    @DisplayName("3. GET /api/v1/hazards/processed/quality/{status} - Filter by Quality Status")
    void testGetProcessedHazardsByQuality() throws Exception {
        // VALID
        mockMvc.perform(get("/api/v1/hazards/processed/quality/VALID")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].qualityStatus", is("VALID")));

        // UNLOCATED
        mockMvc.perform(get("/api/v1/hazards/processed/quality/UNLOCATED")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].qualityStatus", is("UNLOCATED")));
    }

    @Test
    @DisplayName("4. GET /api/v1/hazards/processed/district/{districtName} - District Filter")
    void testGetProcessedHazardsInDistrict() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/processed/district/Sitamarhi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].associatedDistrict", is("Sitamarhi")));
    }

    @Test
    @DisplayName("5. GET /api/v1/hazards/processed/rainfall/daily - Daily Rainfall Summary")
    void testGetDailyRainfallSummaries() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/processed/rainfall/daily")
                        .param("stationName", "Patna")
                        .param("startDate", "2020-06-25")
                        .param("endDate", "2020-07-05")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(11)))
                .andExpect(jsonPath("$[4].date", is("2020-06-29")))
                .andExpect(jsonPath("$[4].dailyTotalMm", is(101.7)))
                .andExpect(jsonPath("$[4].peakHourlyMm", is(41.5)))
                .andExpect(jsonPath("$[4].rainyHours", is(21)))
                .andExpect(jsonPath("$[4].exceedsHeavyThreshold", is(true)));
    }

    @Test
    @DisplayName("6. GET /api/v1/hazards/processed/rainfall/rolling - Rolling Rainfall Metrics")
    void testGetRollingRainfallMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/processed/rainfall/rolling")
                        .param("stationName", "Patna")
                        .param("targetTime", "2020-06-29T18:00:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationName", is("Patna")))
                .andExpect(jsonPath("$.currentHourlyMm", is(0.2)))
                .andExpect(jsonPath("$.rolling3hMm", is(0.4)))
                .andExpect(jsonPath("$.rolling6hMm", is(0.8)))
                .andExpect(jsonPath("$.rolling24hMm", notNullValue()))
                .andExpect(jsonPath("$.heavyRainfall", is(true)));
    }

    @Test
    @DisplayName("7. GET /api/v1/hazards/processed/quality-summary - Quality Summary Report")
    void testGetProcessingQualitySummary() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/processed/quality-summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalCrs", is("EPSG:4326 (WGS 84)")))
                .andExpect(jsonPath("$.totalProcessedRecords", is(131620)))
                .andExpect(jsonPath("$.dfoTotal", is(23)))
                .andExpect(jsonPath("$.dfoValid", is(7)))
                .andExpect(jsonPath("$.dfoUnlocated", is(16)))
                .andExpect(jsonPath("$.emdatTotal", is(53)))
                .andExpect(jsonPath("$.emdatUnlocated", is(53)))
                .andExpect(jsonPath("$.validRecordsCount", is(131551)))
                .andExpect(jsonPath("$.unlocatedRecordsCount", is(69)))
                .andExpect(jsonPath("$.anomaliesCleanedCount", is(16)))
                .andExpect(jsonPath("$.cleaningRulesApplied", not(empty())));
    }

    @Test
    @DisplayName("8. GET /api/v1/hazards/processed/geojson - Processed GeoJSON Vector Layer")
    void testGetProcessedHazardsGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/processed/geojson")
                        .param("type", "FLOOD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.count", is(7)))
                .andExpect(jsonPath("$.features", hasSize(7)))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].properties.qualityStatus", is("VALID")));
    }
}
