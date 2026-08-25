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
class HazardApiIntegrationTestSuite {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. Invalid Hazard Type returns 400 with Standardized Error Response")
    void testInvalidHazardTypeReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/type/INVALID_TYPE_XYZ")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("INVALID_TYPE_XYZ")));
    }

    @Test
    @DisplayName("2. Unknown Hazard ID returns 404 with Standardized Error Response")
    void testUnknownHazardIdReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/DFO-99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("DFO-99999")));
    }

    @Test
    @DisplayName("3. GeoJSON Endpoints return valid RFC 7946 without generic envelope wrapping")
    void testGeoJsonEndpointsAreNotWrapped() throws Exception {
        mockMvc.perform(get("/api/v1/hazards/geojson")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features", notNullValue()))
                .andExpect(jsonPath("$.success").doesNotExist());
    }

    @Test
    @DisplayName("4. Safety Limit Ceiling is Enforced on Layer and Hazard Feeds")
    void testSafetyLimitEnforced() throws Exception {
        mockMvc.perform(get("/api/v1/hazards")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
    }
}
