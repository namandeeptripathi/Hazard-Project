package com.hazard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SpringBootTest & MockMvc Integration Tests for SafeSiteController REST Endpoints (Stages 5.2 - 5.11).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class SafeSiteControllerTests {

    private static final Logger log = LoggerFactory.getLogger(SafeSiteControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("API 5.2 - 5.11.1: GET /api/v1/safe-sites — All Candidate Safe Sites with Multi-Criteria Intelligence & Ranking")
    void testGetAllCandidateSites() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data[0].hazardSafetyStatus").exists())
                .andExpect(jsonPath("$.data[0].terrainStatus").exists())
                .andExpect(jsonPath("$.data[0].distanceStatus").exists())
                .andExpect(jsonPath("$.data[0].roadAccessStatus").exists())
                .andExpect(jsonPath("$.data[0].healthcareAccessStatus").exists())
                .andExpect(jsonPath("$.data[0].waterAccessStatus").exists())
                .andExpect(jsonPath("$.data[0].infrastructureAccessStatus").exists())
                .andExpect(jsonPath("$.data[0].suitabilityClass").exists())
                .andExpect(jsonPath("$.data[0].knownFactorCount").exists())
                .andExpect(jsonPath("$.data[0].unknownFactorCount").exists())
                .andExpect(jsonPath("$.data[0].dataCompletenessPercentage").exists())
                .andExpect(jsonPath("$.data[0].suitabilityReason").exists())
                .andExpect(jsonPath("$.data[0].suitabilityFactors").exists())
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].rankingReason").exists())
                .andExpect(jsonPath("$.meta.stage").value("5.11"))
                .andExpect(jsonPath("$.meta.substage").value("Candidate Safe-Site Ranking"))
                .andExpect(jsonPath("$.meta.totalCandidates").isNumber());

        log.info("✅ GET /api/v1/safe-sites — 200 OK with candidate sites, multi-criteria spatial factors, suitability, and ranking");
    }

    @Test
    @DisplayName("API 5.2 - 5.10.2: GET /api/v1/safe-sites?district=Sitamarhi — Filter by District")
    void testGetCandidateSitesByDistrict() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?district=Sitamarhi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data[*].district", everyItem(equalToIgnoringCase("Sitamarhi"))))
                .andExpect(jsonPath("$.meta.district").value("Sitamarhi"));

        log.info("✅ GET /api/v1/safe-sites?district=Sitamarhi — 200 OK");
    }

    @Test
    @DisplayName("API 5.2 - 5.10.3: GET /api/v1/safe-sites?category=EMERGENCY_SHELTER — Filter by Category")
    void testGetCandidateSitesByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?category=EMERGENCY_SHELTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].category", everyItem(equalTo("EMERGENCY_SHELTER"))));

        log.info("✅ GET /api/v1/safe-sites?category=EMERGENCY_SHELTER — 200 OK");
    }

    @Test
    @DisplayName("API 5.2 - 5.10.4: GET /api/v1/safe-sites?redZoneOnly=true — Cross-filter with Stage 5.1 Red Zones")
    void testGetCandidateSitesRedZoneOnly() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?redZoneOnly=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.meta.redZoneOnly").value(true));

        log.info("✅ GET /api/v1/safe-sites?redZoneOnly=true — 200 OK");
    }

    @Test
    @DisplayName("API 5.3.5: GET /api/v1/safe-sites?hazardSafety=SAFE — Filter by Hazard Safety Status")
    void testGetCandidateSitesByHazardSafety() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?hazardSafety=SAFE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].hazardSafetyStatus", everyItem(equalTo("SAFE"))))
                .andExpect(jsonPath("$.meta.hazardSafetyFilter").value("SAFE"));

        log.info("✅ GET /api/v1/safe-sites?hazardSafety=SAFE — 200 OK");
    }

    @Test
    @DisplayName("API 5.3.6: GET /api/v1/safe-sites?hazardSafety=INVALID — 400 Bad Request")
    void testGetCandidateSitesInvalidHazardSafety() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?hazardSafety=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Allowed values")));

        log.info("✅ GET /api/v1/safe-sites?hazardSafety=INVALID — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.4.7: GET /api/v1/safe-sites?terrainStatus=UNKNOWN — Filter by Terrain Status")
    void testGetCandidateSitesByTerrainStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?terrainStatus=UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].terrainStatus", everyItem(equalTo("UNKNOWN"))))
                .andExpect(jsonPath("$.meta.terrainStatusFilter").value("UNKNOWN"));

        log.info("✅ GET /api/v1/safe-sites?terrainStatus=UNKNOWN — 200 OK");
    }

    @Test
    @DisplayName("API 5.4.8: GET /api/v1/safe-sites?terrainStatus=INVALID — 400 Bad Request")
    void testGetCandidateSitesInvalidTerrainStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?terrainStatus=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Allowed values")));

        log.info("✅ GET /api/v1/safe-sites?terrainStatus=INVALID — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.5.9: GET /api/v1/safe-sites?distanceStatus=NEAR — Filter by Distance Status")
    void testGetCandidateSitesByDistanceStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?distanceStatus=NEAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].distanceStatus", everyItem(equalTo("NEAR"))))
                .andExpect(jsonPath("$.meta.distanceStatusFilter").value("NEAR"));

        log.info("✅ GET /api/v1/safe-sites?distanceStatus=NEAR — 200 OK");
    }

    @Test
    @DisplayName("API 5.5.10: GET /api/v1/safe-sites?distanceStatus=INVALID — 400 Bad Request")
    void testGetCandidateSitesInvalidDistanceStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?distanceStatus=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Allowed values")));

        log.info("✅ GET /api/v1/safe-sites?distanceStatus=INVALID — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.6.11: GET /api/v1/safe-sites?roadAccessStatus=UNKNOWN — Filter by Road Access Status")
    void testGetCandidateSitesByRoadAccessStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?roadAccessStatus=UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].roadAccessStatus", everyItem(equalTo("UNKNOWN"))))
                .andExpect(jsonPath("$.meta.roadAccessStatusFilter").value("UNKNOWN"));

        log.info("✅ GET /api/v1/safe-sites?roadAccessStatus=UNKNOWN — 200 OK");
    }

    @Test
    @DisplayName("API 5.6.12: GET /api/v1/safe-sites?roadAccessStatus=INVALID — 400 Bad Request")
    void testGetCandidateSitesInvalidRoadAccessStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?roadAccessStatus=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Allowed values")));

        log.info("✅ GET /api/v1/safe-sites?roadAccessStatus=INVALID — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.7.13: GET /api/v1/safe-sites?healthcareAccessStatus=NEAR — Filter by Healthcare Access Status")
    void testGetCandidateSitesByHealthcareAccessStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?healthcareAccessStatus=NEAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].healthcareAccessStatus", everyItem(equalTo("NEAR"))))
                .andExpect(jsonPath("$.meta.healthcareAccessStatusFilter").value("NEAR"));

        log.info("✅ GET /api/v1/safe-sites?healthcareAccessStatus=NEAR — 200 OK");
    }

    @Test
    @DisplayName("API 5.7.14: GET /api/v1/safe-sites?healthcareAccessStatus=INVALID — 400 Bad Request")
    void testGetCandidateSitesInvalidHealthcareAccessStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?healthcareAccessStatus=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Allowed values")));

        log.info("✅ GET /api/v1/safe-sites?healthcareAccessStatus=INVALID — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.8.15: GET /api/v1/safe-sites?waterAccessStatus=UNKNOWN — Filter by Water Access Status")
    void testGetCandidateSitesByWaterAccessStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?waterAccessStatus=UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].waterAccessStatus", everyItem(equalTo("UNKNOWN"))))
                .andExpect(jsonPath("$.meta.waterAccessStatusFilter").value("UNKNOWN"));

        log.info("✅ GET /api/v1/safe-sites?waterAccessStatus=UNKNOWN — 200 OK");
    }

    @Test
    @DisplayName("API 5.8.16: GET /api/v1/safe-sites?waterAccessStatus=INVALID — 400 Bad Request")
    void testGetCandidateSitesInvalidWaterAccessStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?waterAccessStatus=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Allowed values")));

        log.info("✅ GET /api/v1/safe-sites?waterAccessStatus=INVALID — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.9.17: GET /api/v1/safe-sites?infrastructureAccessStatus=NEAR — Filter by Infrastructure Access Status")
    void testGetCandidateSitesByInfrastructureAccessStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?infrastructureAccessStatus=NEAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].infrastructureAccessStatus", everyItem(equalTo("NEAR"))))
                .andExpect(jsonPath("$.meta.infrastructureAccessStatusFilter").value("NEAR"));

        log.info("✅ GET /api/v1/safe-sites?infrastructureAccessStatus=NEAR — 200 OK");
    }

    @Test
    @DisplayName("API 5.9.18: GET /api/v1/safe-sites?infrastructureAccessStatus=INVALID — 400 Bad Request")
    void testGetCandidateSitesInvalidInfrastructureAccessStatus() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?infrastructureAccessStatus=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Allowed values")));

        log.info("✅ GET /api/v1/safe-sites?infrastructureAccessStatus=INVALID — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.10.19: GET /api/v1/safe-sites?suitabilityClass=HIGHLY_SUITABLE — Filter by Suitability Class")
    void testGetCandidateSitesBySuitabilityClass() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?suitabilityClass=HIGHLY_SUITABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].suitabilityClass", everyItem(equalTo("HIGHLY_SUITABLE"))))
                .andExpect(jsonPath("$.meta.suitabilityClassFilter").value("HIGHLY_SUITABLE"));

        log.info("✅ GET /api/v1/safe-sites?suitabilityClass=HIGHLY_SUITABLE — 200 OK");
    }

    @Test
    @DisplayName("API 5.10.20: GET /api/v1/safe-sites?suitabilityClass=INVALID — 400 Bad Request")
    void testGetCandidateSitesInvalidSuitabilityClass() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?suitabilityClass=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Allowed values")));

        log.info("✅ GET /api/v1/safe-sites?suitabilityClass=INVALID — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.2 - 5.11.21: GET /api/v1/safe-sites/FAC-EMG-003 — Single Candidate Site Detail with Ranking")
    void testGetCandidateSiteById() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites/FAC-EMG-003"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.siteId").value("FAC-EMG-003"))
                .andExpect(jsonPath("$.data.siteName").value("Sitamarhi Central Flood Shelter"))
                .andExpect(jsonPath("$.data.category").value("EMERGENCY_SHELTER"))
                .andExpect(jsonPath("$.data.district").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.latitude").value(26.5950))
                .andExpect(jsonPath("$.data.longitude").value(85.5030))
                .andExpect(jsonPath("$.data.status").value("CANDIDATE"))
                .andExpect(jsonPath("$.data.hazardSafetyStatus").value("SAFE"))
                .andExpect(jsonPath("$.data.hazardSafetyReason", containsString("Sitamarhi")))
                .andExpect(jsonPath("$.data.terrainStatus").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.distanceStatus").exists())
                .andExpect(jsonPath("$.data.distanceReason").exists())
                .andExpect(jsonPath("$.data.roadAccessStatus").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.healthcareAccessStatus").value("NEAR"))
                .andExpect(jsonPath("$.data.nearestHealthcareSiteId").value("FAC-MED-007"))
                .andExpect(jsonPath("$.data.nearestHealthcareSiteName").value("Sitamarhi Sadar District Hospital"))
                .andExpect(jsonPath("$.data.waterAccessStatus").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.infrastructureAccessStatus").value("NEAR"))
                .andExpect(jsonPath("$.data.suitabilityClass").exists())
                .andExpect(jsonPath("$.data.suitabilityScore").isNumber())
                .andExpect(jsonPath("$.data.knownFactorCount").isNumber())
                .andExpect(jsonPath("$.data.unknownFactorCount").isNumber())
                .andExpect(jsonPath("$.data.dataCompletenessPercentage").isNumber())
                .andExpect(jsonPath("$.data.suitabilityReason").exists())
                .andExpect(jsonPath("$.data.suitabilityFactors").exists())
                .andExpect(jsonPath("$.data.rank").isNumber())
                .andExpect(jsonPath("$.data.rankingReason").exists())
                .andExpect(jsonPath("$.meta.stage").value("5.11"))
                .andExpect(jsonPath("$.meta.substage").value("Candidate Safe-Site Ranking"))
                .andExpect(jsonPath("$.meta.suitabilityClass").exists())
                .andExpect(jsonPath("$.meta.suitabilityScore").isNumber())
                .andExpect(jsonPath("$.meta.rank").isNumber());

        log.info("✅ GET /api/v1/safe-sites/FAC-EMG-003 — 200 OK with full multi-criteria, suitability & rank intelligence");
    }

    @Test
    @DisplayName("API 5.2 - 5.11.22: GET /api/v1/safe-sites/NON-EXISTENT-ID — 404 Not Found")
    void testGetCandidateSiteByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites/NON-EXISTENT-ID"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        log.info("✅ GET /api/v1/safe-sites/NON-EXISTENT-ID — 404 Not Found verified");
    }

    @Test
    @DisplayName("API 5.2 - 5.11.23: GET /api/v1/safe-sites?category=INVALID_CATEGORY — 400 Bad Request")
    void testGetCandidateSitesInvalidCategory() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?category=INVALID_CATEGORY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Allowed categories")));

        log.info("✅ GET /api/v1/safe-sites?category=INVALID_CATEGORY — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.11.24: GET /api/v1/safe-sites?top=3 — Top 3 Ranked Safe Sites")
    void testGetCandidateSitesTopN() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?top=3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[1].rank").value(2))
                .andExpect(jsonPath("$.data[2].rank").value(3))
                .andExpect(jsonPath("$.meta.topFilter").value(3));

        log.info("✅ GET /api/v1/safe-sites?top=3 — 200 OK with top 3 ranked sites");
    }

    @Test
    @DisplayName("API 5.11.25: GET /api/v1/safe-sites?top=0 — 400 Bad Request")
    void testGetCandidateSitesInvalidTop() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?top=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Parameter 'top' must be a positive integer")));

        log.info("✅ GET /api/v1/safe-sites?top=0 — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.11.26: GET /api/v1/safe-sites?limit=3 — Limit 3 Ranked Safe Sites")
    void testGetCandidateSitesLimitN() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?limit=3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[1].rank").value(2))
                .andExpect(jsonPath("$.data[2].rank").value(3))
                .andExpect(jsonPath("$.meta.limitFilter").value(3));

        log.info("✅ GET /api/v1/safe-sites?limit=3 — 200 OK with limited 3 ranked sites");
    }

    @Test
    @DisplayName("API 5.11.27: GET /api/v1/safe-sites?limit=-1 — 400 Bad Request")
    void testGetCandidateSitesInvalidLimit() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites?limit=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Parameter 'limit' must be a positive integer")));

        log.info("✅ GET /api/v1/safe-sites?limit=-1 — 400 Bad Request verified");
    }

    @Test
    @DisplayName("API 5.11.28: GET /api/v1/safe-sites/ranked?limit=5 — Ranked Safe Sites Endpoint")
    void testGetRankedCandidateSitesEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites/ranked?limit=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].rankingReason").exists())
                .andExpect(jsonPath("$.meta.stage").value("5.11"))
                .andExpect(jsonPath("$.meta.substage").value("Candidate Safe-Site Ranking"));

        log.info("✅ GET /api/v1/safe-sites/ranked?limit=5 — 200 OK verified");
    }

    @Test
    @DisplayName("API 5.2 - 5.11.26: GET /api/v1/safe-sites/geojson — RFC 7946 GeoJSON Layer with Multi-Criteria & Ranking Properties")
    void testGetCandidateSitesGeoJson() throws Exception {
        mockMvc.perform(get("/api/v1/safe-sites/geojson"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features.length()", greaterThan(0)))
                .andExpect(jsonPath("$.features[0].geometry.type").value("Point"))
                .andExpect(jsonPath("$.features[0].properties.siteId").exists())
                .andExpect(jsonPath("$.features[0].properties.layerId").value("CANDIDATE_SAFE_SITES"))
                .andExpect(jsonPath("$.features[0].properties.hazardSafetyStatus").exists())
                .andExpect(jsonPath("$.features[0].properties.terrainStatus").exists())
                .andExpect(jsonPath("$.features[0].properties.distanceStatus").exists())
                .andExpect(jsonPath("$.features[0].properties.roadAccessStatus").exists())
                .andExpect(jsonPath("$.features[0].properties.healthcareAccessStatus").exists())
                .andExpect(jsonPath("$.features[0].properties.waterAccessStatus").exists())
                .andExpect(jsonPath("$.features[0].properties.infrastructureAccessStatus").exists())
                .andExpect(jsonPath("$.features[0].properties.suitabilityClass").exists())
                .andExpect(jsonPath("$.features[0].properties.suitabilityScore").exists())
                .andExpect(jsonPath("$.features[0].properties.knownFactorCount").exists())
                .andExpect(jsonPath("$.features[0].properties.unknownFactorCount").exists())
                .andExpect(jsonPath("$.features[0].properties.dataCompletenessPercentage").exists())
                .andExpect(jsonPath("$.features[0].properties.suitabilityReason").exists())
                .andExpect(jsonPath("$.features[0].properties.suitabilityFactors").exists())
                .andExpect(jsonPath("$.features[0].properties.rank").exists())
                .andExpect(jsonPath("$.features[0].properties.rankingReason").exists());

        log.info("✅ GET /api/v1/safe-sites/geojson — 200 OK RFC 7946 GeoJSON FeatureCollection with full multi-criteria & ranking properties");
    }
}
