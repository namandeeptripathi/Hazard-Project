package com.hazard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI 3 Configuration for Stage 3 Hazard Intelligence API.
 * Defines API metadata, contact info, license, and logical OpenAPI groupings.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Hazard Risk Prediction & Relocation System - Hazard Intelligence API")
                        .description("Stage 3 Hazard Intelligence REST APIs providing integrated multi-source observations, " +
                                "data cleaning and temporal aggregation, scientific indicator normalization, " +
                                "single-hazard scoring, multi-hazard spatial-temporal coincidence indices, " +
                                "and map-ready RFC 7946 GeoJSON GIS layers.")
                        .version("1.0.0 (Stage 3.7)")
                        .contact(new Contact()
                                .name("Smart India Hackathon (SIH) 2026 Team")
                                .url("https://github.com/NamandeepTripathi/Hazard-Project"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("/").description("Local / Embedded Application Server")
                ));
    }

    @Bean
    public GroupedOpenApi hazardAllApi() {
        return GroupedOpenApi.builder()
                .group("1. All Hazard APIs")
                .pathsToMatch("/api/v1/hazards/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hazardLayersApi() {
        return GroupedOpenApi.builder()
                .group("2. Map Layers (GeoJSON)")
                .pathsToMatch("/api/v1/hazards/layers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hazardMultiHazardApi() {
        return GroupedOpenApi.builder()
                .group("3. Multi-Hazard Intelligence")
                .pathsToMatch("/api/v1/hazards/multi-hazard/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hazardScoringApi() {
        return GroupedOpenApi.builder()
                .group("4. Hazard Scoring")
                .pathsToMatch("/api/v1/hazards/scores/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hazardNormalizationApi() {
        return GroupedOpenApi.builder()
                .group("5. Hazard Normalization")
                .pathsToMatch("/api/v1/hazards/normalized/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hazardProcessingApi() {
        return GroupedOpenApi.builder()
                .group("6. Hazard Processing")
                .pathsToMatch("/api/v1/hazards/processing/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hazardIntegrationApi() {
        return GroupedOpenApi.builder()
                .group("7. Hazard Integration")
                .pathsToMatch("/api/v1/hazards", "/api/v1/hazards/{id}", "/api/v1/hazards/type/**",
                        "/api/v1/hazards/district/**", "/api/v1/hazards/spatial/**", "/api/v1/hazards/summary", "/api/v1/hazards/geojson")
                .build();
    }
}
