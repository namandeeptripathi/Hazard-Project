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
class HazardOpenApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. GET /v3/api-docs - OpenAPI 3 JSON Specification Generates Successfully")
    void testOpenApiDocsEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi", notNullValue()))
                .andExpect(jsonPath("$.info.title", containsString("Hazard Intelligence API")))
                .andExpect(jsonPath("$.info.version", containsString("Stage 3.7")))
                .andExpect(jsonPath("$.paths", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/hazards']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/hazards/layers']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/hazards/scores']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/hazards/multi-hazard']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/hazards/health']", notNullValue()));
    }

    @Test
    @DisplayName("2. GET /swagger-ui/index.html - Swagger UI HTML Documentation Is Reachable")
    void testSwaggerUiEndpoint() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }
}
