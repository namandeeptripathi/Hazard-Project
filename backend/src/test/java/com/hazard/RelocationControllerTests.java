package com.hazard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RelocationRequestDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Stage 6.7 — Relocation Intelligence API Integration Tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class RelocationControllerTests {

    private static final Logger log = LoggerFactory.getLogger(RelocationControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. POST /api/v1/relocation/plan — Valid request returns 200 OK with complete plan")
    void testPostRelocationPlanValidRequest() throws Exception {
        RelocationRequestDto request = new RelocationRequestDto();
        request.setDistrict("Sitamarhi");
        request.setMaxTransitDistanceKm(25.0);
        request.setMinSuitabilityClass(SuitabilityClass.MARGINAL);
        request.setVulnerablePopulation(200L);

        mockMvc.perform(post("/api/v1/relocation/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.planId").exists())
                .andExpect(jsonPath("$.data.district").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.totalVulnerablePopulation").value(200))
                .andExpect(jsonPath("$.data.totalAllocatedPopulation").isNumber())
                .andExpect(jsonPath("$.data.totalUnallocatedPopulation").isNumber())
                .andExpect(jsonPath("$.data.overallStatus").exists())
                .andExpect(jsonPath("$.data.assignments").isArray());

        log.info("✅ POST /api/v1/relocation/plan — 200 OK");
    }

    @Test
    @DisplayName("2. POST /api/v1/relocation — Alias endpoint works identically")
    void testPostRelocationAliasEndpoint() throws Exception {
        RelocationRequestDto request = new RelocationRequestDto();
        request.setDistrict("Sitamarhi");
        request.setVulnerablePopulation(150L);

        mockMvc.perform(post("/api/v1/relocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalVulnerablePopulation").value(150));

        log.info("✅ POST /api/v1/relocation — 200 OK");
    }

    @Test
    @DisplayName("3. GET /api/v1/relocation/plan — Query parameters endpoint returns 200 OK")
    void testGetRelocationPlanWithQueryParams() throws Exception {
        mockMvc.perform(get("/api/v1/relocation/plan")
                        .param("district", "Sitamarhi")
                        .param("maxDistanceKm", "30.0")
                        .param("minSuitability", "MARGINAL")
                        .param("population", "180"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.district").value("Sitamarhi"))
                .andExpect(jsonPath("$.data.totalVulnerablePopulation").value(180))
                .andExpect(jsonPath("$.data.overallStatus").exists());

        log.info("✅ GET /api/v1/relocation/plan — 200 OK");
    }

    @Test
    @DisplayName("4. No feasible site returns HTTP 200 with UNALLOCATED_NO_SAFE_SITE status (NOT 500 error)")
    void testNoFeasibleSiteReturns200WithNoSafeSiteStatus() throws Exception {
        // Direct Habitation located far outside the reach of any shelter (e.g. coordinates 20.0, 70.0)
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId("HAB-ISOLATED");
        hab.setHabitationName("Isolated Mountain Valley");
        hab.setDistrict("Sitamarhi");
        hab.setState("Bihar");
        hab.setLatitude(20.0000);
        hab.setLongitude(70.0000);
        hab.setVulnerablePopulation(300L);
        hab.setUrgency(RelocationUrgency.CRITICAL);

        RelocationRequestDto request = new RelocationRequestDto();
        request.setHabitation(hab);
        request.setMaxTransitDistanceKm(5.0); // 5 km max radius -> impossible to reach any candidate shelter (>1000 km away)

        mockMvc.perform(post("/api/v1/relocation/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalVulnerablePopulation").value(300))
                .andExpect(jsonPath("$.data.totalAllocatedPopulation").value(0))
                .andExpect(jsonPath("$.data.totalUnallocatedPopulation").value(300))
                .andExpect(jsonPath("$.data.overallStatus").value("UNALLOCATED_NO_SAFE_SITE"))
                .andExpect(jsonPath("$.data.deficitReasonCode").value("NO_FEASIBLE_SITE"))
                .andExpect(jsonPath("$.data.assignments", hasSize(0)))
                .andExpect(jsonPath("$.data.unallocatedHabitations", hasSize(1)));

        log.info("✅ No feasible site -> 200 OK with UNALLOCATED_NO_SAFE_SITE status");
    }

    @Test
    @DisplayName("5. Fully allocated relocation returns HTTP 200 with ALLOCATED and zero deficit")
    void testFullyAllocatedRelocationReturnsZeroDeficit() throws Exception {
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId("HAB-FIT");
        hab.setHabitationName("Small Village");
        hab.setDistrict("Sitamarhi");
        hab.setState("Bihar");
        hab.setLatitude(26.5950);
        hab.setLongitude(85.5030);
        hab.setVulnerablePopulation(50L);
        hab.setUrgency(RelocationUrgency.MODERATE);

        RelocationRequestDto request = new RelocationRequestDto();
        request.setHabitation(hab);
        request.setMaxTransitDistanceKm(50.0);
        request.setMinSuitabilityClass(SuitabilityClass.MARGINAL);

        mockMvc.perform(post("/api/v1/relocation/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalVulnerablePopulation").value(50))
                .andExpect(jsonPath("$.data.totalAllocatedPopulation").value(50))
                .andExpect(jsonPath("$.data.totalUnallocatedPopulation").value(0))
                .andExpect(jsonPath("$.data.overallStatus").value("ALLOCATED"))
                .andExpect(jsonPath("$.data.deficitReasonCode").value("FULLY_ALLOCATED"))
                .andExpect(jsonPath("$.data.allocationRatePercentage").value(100.0));

        log.info("✅ Full allocation -> 200 OK with ALLOCATED status and 0 deficit");
    }

    @Test
    @DisplayName("6. Invalid negative maxTransitDistanceKm returns HTTP 400 Bad Request")
    void testInvalidParameterReturns400BadRequest() throws Exception {
        RelocationRequestDto request = new RelocationRequestDto();
        request.setDistrict("Sitamarhi");
        request.setMaxTransitDistanceKm(-10.0); // Invalid negative distance

        mockMvc.perform(post("/api/v1/relocation/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("cannot be negative")));

        log.info("✅ Negative transit distance -> 400 Bad Request");
    }

    @Test
    @DisplayName("7. Direct coordinates request resolves habitation and produces valid plan")
    void testDirectCoordinatesRequest() throws Exception {
        RelocationRequestDto request = new RelocationRequestDto();
        request.setDistrict("Sitamarhi");
        request.setOriginLatitude(26.5950);
        request.setOriginLongitude(85.5030);
        request.setVulnerablePopulation(100L);
        request.setMaxTransitDistanceKm(25.0);

        mockMvc.perform(post("/api/v1/relocation/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalVulnerablePopulation").value(100))
                .andExpect(jsonPath("$.data.overallStatus").exists());

        log.info("✅ Direct coordinates request -> 200 OK");
    }
}
