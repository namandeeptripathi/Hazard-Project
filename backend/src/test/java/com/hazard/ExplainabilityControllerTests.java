package com.hazard;

import com.hazard.controller.relocation.RelocationController;
import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.relocation.RelocationRequestDto;
import com.hazard.dto.relocation.explain.BatchRelocationDecisionExplanationDto;
import com.hazard.dto.relocation.explain.RelocationDecisionExplanationDto;
import com.hazard.service.relocation.RelocationExplainabilityService;
import com.hazard.service.relocation.RelocationPlanningService;
import com.hazard.service.relocation.RelocationPriorityService;
import com.hazard.service.relocation.RelocationRecommendationService;
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

/**
 * Stage 7C — Explainability Controller Tests.
 * Verifies POST and GET /explain endpoints.
 */
class ExplainabilityControllerTests {

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
        controller = new RelocationController(
                planningService, priorityService, recommendationService, explainabilityService
        );
    }

    @Nested
    @DisplayName("POST /api/v1/relocation/explain")
    class PostExplainTests {

        @Test
        @DisplayName("Empty request list returns 200 OK with empty batch explanation")
        void testEmptyRequestList() {
            ResponseEntity<ApiResponse<BatchRelocationDecisionExplanationDto>> response =
                    controller.explainDecision(Collections.emptyList());

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals(0, response.getBody().getData().getTotalCases());
        }

        @Test
        @DisplayName("Valid request list calls explainability service and returns 200 OK")
        void testValidRequestList() {
            BatchRelocationDecisionExplanationDto mockBatch = new BatchRelocationDecisionExplanationDto();
            RelocationDecisionExplanationDto exp = new RelocationDecisionExplanationDto();
            exp.setHabitationId("HAB-01");
            mockBatch.addExplanation(exp);
            mockBatch.setSummary("1 decision explanation generated");

            when(explainabilityService.explainBatchRequests(any())).thenReturn(mockBatch);

            RelocationRequestDto req = new RelocationRequestDto("Sitamarhi");
            ResponseEntity<ApiResponse<BatchRelocationDecisionExplanationDto>> response =
                    controller.explainDecision(Collections.singletonList(req));

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalCases());
            verify(explainabilityService, times(1)).explainBatchRequests(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/relocation/explain")
    class GetExplainTests {

        @Test
        @DisplayName("Single district query param calls explainability service")
        void testSingleDistrictGet() {
            BatchRelocationDecisionExplanationDto mockBatch = new BatchRelocationDecisionExplanationDto();
            mockBatch.setSummary("Sitamarhi explanations");
            when(explainabilityService.explainBatchRequests(any())).thenReturn(mockBatch);

            ResponseEntity<ApiResponse<BatchRelocationDecisionExplanationDto>> response =
                    controller.getDecisionExplanations("Sitamarhi", 25.0, "MARGINAL", 200L);

            assertEquals(200, response.getStatusCode().value());
            verify(explainabilityService, times(1)).explainBatchRequests(any());
        }

        @Test
        @DisplayName("Comma-separated districts query param builds multiple requests")
        void testMultipleDistrictsGet() {
            BatchRelocationDecisionExplanationDto mockBatch = new BatchRelocationDecisionExplanationDto();
            when(explainabilityService.explainBatchRequests(any())).thenReturn(mockBatch);

            ResponseEntity<ApiResponse<BatchRelocationDecisionExplanationDto>> response =
                    controller.getDecisionExplanations("Sitamarhi,Patna,Darbhanga", 30.0, "SUITABLE", 300L);

            assertEquals(200, response.getStatusCode().value());
            verify(explainabilityService, times(1)).explainBatchRequests(argThat(list -> list.size() == 3));
        }
    }
}
