package com.hazard;

import com.hazard.controller.relocation.RelocationController;
import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.dto.relocation.*;
import com.hazard.service.relocation.RelocationPlanningService;
import com.hazard.service.relocation.RelocationPriorityService;
import com.hazard.service.relocation.RelocationRecommendationService;
import com.hazard.service.relocation.RelocationExplainabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hazard.dto.common.ApiResponse;

/**
 * Stage 7A — Priority Controller Tests.
 * Tests the new priority endpoints on RelocationController.
 */
class PriorityControllerTests {

    private RelocationController controller;
    private RelocationPlanningService planningService;
    private RelocationPriorityService priorityService;
    private RelocationRecommendationService recommendationService;
    private RelocationExplainabilityService explainabilityService;

    @BeforeEach
    void setUp() {
        planningService = mock(RelocationPlanningService.class);
        priorityService = mock(RelocationPriorityService.class);
        recommendationService = mock(RelocationRecommendationService.class);
        explainabilityService = mock(RelocationExplainabilityService.class);
        controller = new RelocationController(planningService, priorityService, recommendationService, explainabilityService);
    }

    @Nested
    @DisplayName("POST /api/v1/relocation/priority")
    class PostPriorityTests {

        @Test
        @DisplayName("Empty request list → returns empty result")
        void emptyRequestList() {
            ResponseEntity<ApiResponse<PriorityRankingResultDto>> response =
                    controller.rankRelocationPriorities(Collections.emptyList());

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
        }

        @Test
        @DisplayName("Null request list → returns empty result")
        void nullRequestList() {
            ResponseEntity<ApiResponse<PriorityRankingResultDto>> response =
                    controller.rankRelocationPriorities(null);

            assertEquals(200, response.getStatusCode().value());
        }

        @Test
        @DisplayName("Valid request → calls planning and priority services")
        void validRequest() {
            RelocationPlanDto mockPlan = new RelocationPlanDto();
            mockPlan.setPlanSummary("Test plan");
            VulnerableHabitationDto mockHab = new VulnerableHabitationDto();

            when(planningService.planRelocation(any())).thenReturn(mockPlan);
            when(planningService.resolveVulnerableHabitation(any())).thenReturn(mockHab);

            PriorityRankingResultDto mockResult = new PriorityRankingResultDto();
            mockResult.setRankingSummary("Test ranking");
            when(priorityService.scoreAndRankAll(any(), any())).thenReturn(mockResult);

            RelocationRequestDto request = new RelocationRequestDto("Sitamarhi");
            ResponseEntity<ApiResponse<PriorityRankingResultDto>> response =
                    controller.rankRelocationPriorities(Collections.singletonList(request));

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            verify(planningService, times(1)).planRelocation(any());
            verify(planningService, times(1)).resolveVulnerableHabitation(any());
            verify(priorityService, times(1)).scoreAndRankAll(any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/relocation/priority")
    class GetPriorityTests {

        @Test
        @DisplayName("Single district → calls services correctly")
        void singleDistrict() {
            RelocationPlanDto mockPlan = new RelocationPlanDto();
            VulnerableHabitationDto mockHab = new VulnerableHabitationDto();

            when(planningService.planRelocation(any())).thenReturn(mockPlan);
            when(planningService.resolveVulnerableHabitation(any())).thenReturn(mockHab);

            PriorityRankingResultDto mockResult = new PriorityRankingResultDto();
            mockResult.setRankingSummary("Single district ranking");
            when(priorityService.scoreAndRankAll(any(), any())).thenReturn(mockResult);

            ResponseEntity<ApiResponse<PriorityRankingResultDto>> response =
                    controller.getRelocationPriority("Sitamarhi", 25.0, null);

            assertEquals(200, response.getStatusCode().value());
            verify(planningService, times(1)).planRelocation(any());
        }

        @Test
        @DisplayName("Multiple comma-separated districts → calls services for each")
        void multipleDistricts() {
            RelocationPlanDto mockPlan = new RelocationPlanDto();
            VulnerableHabitationDto mockHab = new VulnerableHabitationDto();

            when(planningService.planRelocation(any())).thenReturn(mockPlan);
            when(planningService.resolveVulnerableHabitation(any())).thenReturn(mockHab);

            PriorityRankingResultDto mockResult = new PriorityRankingResultDto();
            mockResult.setRankingSummary("Multi-district ranking");
            when(priorityService.scoreAndRankAll(any(), any())).thenReturn(mockResult);

            ResponseEntity<ApiResponse<PriorityRankingResultDto>> response =
                    controller.getRelocationPriority("Sitamarhi,Patna,Darbhanga", 25.0, 500L);

            assertEquals(200, response.getStatusCode().value());
            verify(planningService, times(3)).planRelocation(any());
            verify(planningService, times(3)).resolveVulnerableHabitation(any());
        }
    }

    @Nested
    @DisplayName("Existing Stage 6 Endpoints Compatibility")
    class BackwardCompatibilityTests {

        @Test
        @DisplayName("POST /plan endpoint still works with new controller constructor")
        void postPlanStillWorks() {
            RelocationPlanDto mockPlan = new RelocationPlanDto();
            mockPlan.setPlanSummary("Stage 6 plan");
            when(planningService.planRelocation(any())).thenReturn(mockPlan);

            RelocationRequestDto request = new RelocationRequestDto("Sitamarhi");
            ResponseEntity<ApiResponse<RelocationPlanDto>> response =
                    controller.generateRelocationPlan(request);

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Stage 6 plan", response.getBody().getData().getPlanSummary());
        }

        @Test
        @DisplayName("GET /plan endpoint still works with new controller constructor")
        void getPlanStillWorks() {
            RelocationPlanDto mockPlan = new RelocationPlanDto();
            mockPlan.setPlanSummary("Stage 6 GET plan");
            when(planningService.planRelocation(any())).thenReturn(mockPlan);

            ResponseEntity<ApiResponse<RelocationPlanDto>> response =
                    controller.getRelocationPlan("Sitamarhi", null, null, 25.0, "MARGINAL", null);

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
        }
    }
}
