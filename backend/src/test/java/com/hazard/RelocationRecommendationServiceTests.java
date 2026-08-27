package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.*;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.relocation.RelocationPlanningService;
import com.hazard.service.relocation.RelocationPriorityService;
import com.hazard.service.relocation.RelocationRecommendationEngine;
import com.hazard.service.relocation.RelocationRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Stage 7B — Relocation Recommendation Service Integration Tests.
 */
class RelocationRecommendationServiceTests {

    private RelocationRecommendationService service;
    private RelocationPlanningService planningService;
    private RelocationPriorityService priorityService;

    @BeforeEach
    void setUp() {
        planningService = mock(RelocationPlanningService.class);
        priorityService = mock(RelocationPriorityService.class);
        service = new RelocationRecommendationService(
                planningService, priorityService, new RelocationRecommendationEngine()
        );
    }

    private CandidateSafeSiteDto createSite(String id, String name, double lat, double lon, int cap) {
        CandidateSafeSiteDto s = new CandidateSafeSiteDto();
        s.setSiteId(id);
        s.setSiteName(name);
        s.setDistrict("Sitamarhi");
        s.setLatitude(lat);
        s.setLongitude(lon);
        s.setCapacity(cap);
        s.setAvailableCapacity(cap);
        s.setAllocatedOccupancy(0);
        s.setSuitabilityClass(SuitabilityClass.HIGHLY_SUITABLE);
        s.setSuitabilityScore(90.0);
        s.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
        return s;
    }

    @Nested
    @DisplayName("recommendForRequest()")
    class RecommendForRequestTests {

        @Test
        @DisplayName("Successfully processes single request through full mock pipeline")
        void testSingleRequestRecommendation() {
            RelocationRequestDto req = new RelocationRequestDto("Sitamarhi");
            req.setVulnerablePopulation(150L);

            VulnerableHabitationDto hab = new VulnerableHabitationDto();
            hab.setHabitationId("HAB-01");
            hab.setHabitationName("Village 1");
            hab.setDistrict("Sitamarhi");
            hab.setLatitude(26.5950);
            hab.setLongitude(85.5030);
            hab.setVulnerablePopulation(150L);

            List<CandidateSafeSiteDto> sites = Collections.singletonList(
                    createSite("SITE-01", "Relief Shelter A", 26.6000, 85.5100, 300)
            );

            RelocationPriorityResultDto priority = new RelocationPriorityResultDto("HAB-01", 0.75, PriorityLevel.IMMEDIATE);

            when(planningService.resolveVulnerableHabitation(any())).thenReturn(hab);
            when(planningService.resolveCandidateSafeSites(any(), any())).thenReturn(sites);
            when(priorityService.scoreSingle(any(), any())).thenReturn(priority);

            RelocationRecommendationDto rec = service.recommendForRequest(req);

            assertNotNull(rec);
            assertEquals(RecommendationStatus.RECOMMENDED, rec.getStatus());
            assertEquals("SITE-01", rec.getPrimaryDestination().getSiteId());
            assertEquals(150L, rec.getAllocatedPopulation());
            assertEquals(0L, rec.getUnallocatedPopulation());
        }

        @Test
        @DisplayName("Null request returns INVALID_SOURCE status")
        void testNullRequest() {
            RelocationRecommendationDto rec = service.recommendForRequest(null);

            assertNotNull(rec);
            assertEquals(RecommendationStatus.INVALID_SOURCE, rec.getStatus());
        }
    }

    @Nested
    @DisplayName("recommendBatchForRequests()")
    class RecommendBatchTests {

        @Test
        @DisplayName("Processes multiple requests and aggregates batch results correctly")
        void testBatchRequests() {
            RelocationRequestDto req1 = new RelocationRequestDto("Sitamarhi");
            req1.setVulnerablePopulation(100L);
            RelocationRequestDto req2 = new RelocationRequestDto("Patna");
            req2.setVulnerablePopulation(200L);

            VulnerableHabitationDto hab1 = new VulnerableHabitationDto();
            hab1.setHabitationId("HAB-1");
            hab1.setLatitude(26.5950);
            hab1.setLongitude(85.5030);
            hab1.setVulnerablePopulation(100L);

            VulnerableHabitationDto hab2 = new VulnerableHabitationDto();
            hab2.setHabitationId("HAB-2");
            hab2.setLatitude(25.5940);
            hab2.setLongitude(85.1370);
            hab2.setVulnerablePopulation(200L);

            List<CandidateSafeSiteDto> sites1 = Collections.singletonList(createSite("SITE-1", "Shelter 1", 26.6000, 85.5100, 300));
            List<CandidateSafeSiteDto> sites2 = Collections.singletonList(createSite("SITE-2", "Shelter 2", 25.6000, 85.1400, 400));

            when(planningService.resolveVulnerableHabitation(req1)).thenReturn(hab1);
            when(planningService.resolveVulnerableHabitation(req2)).thenReturn(hab2);
            when(planningService.resolveCandidateSafeSites(eq(hab1), any())).thenReturn(sites1);
            when(planningService.resolveCandidateSafeSites(eq(hab2), any())).thenReturn(sites2);

            BatchRelocationRecommendationDto batch = service.recommendBatchForRequests(Arrays.asList(req1, req2));

            assertNotNull(batch);
            assertEquals(2, batch.getTotalCases());
            assertEquals(2, batch.getSuccessfulRecommendations());
            assertNotNull(batch.getSummary());
        }

        @Test
        @DisplayName("Empty requests list returns empty batch result")
        void testEmptyBatchRequests() {
            BatchRelocationRecommendationDto batch = service.recommendBatchForRequests(Collections.emptyList());

            assertEquals(0, batch.getTotalCases());
            assertTrue(batch.getRecommendations().isEmpty());
        }
    }
}
