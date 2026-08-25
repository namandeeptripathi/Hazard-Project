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
class HazardHealthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. GET /api/v1/hazards/health - Hazard Subsystem Health Status")
    void testGetHealthStatus() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("UP")))
                .andExpect(jsonPath("$.data.subsystem", is("Hazard Intelligence (Stage 3)")))
                .andExpect(jsonPath("$.data.canonicalCrs", is("EPSG:4326 (WGS 84)")))
                .andExpect(jsonPath("$.data.stage2BaseRecordCount", is(159005)))
                .andExpect(jsonPath("$.data.activeCapabilities", hasSize(8)));
    }

    @Test
    @DisplayName("2. GET /api/v1/hazards/overview/district/{districtName} - Consolidated District Profile")
    void testGetDistrictHazardOverview() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/overview/district/Patna")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.districtName", is("Patna")))
                .andExpect(jsonPath("$.data.hasActiveWeatherStation", is(true)))
                .andExpect(jsonPath("$.data.multiHazardIndex", notNullValue()))
                .andExpect(jsonPath("$.data.intersectingMajorRivers", not(empty())));
    }

    @Test
    @DisplayName("3. GET /api/v1/hazards/overview/district/{unknown} - Returns Standardized 404 Error Envelope")
    void testGetUnknownDistrictOverview() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/overview/district/UnknownDistrict999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("UnknownDistrict999")));
    }
}
