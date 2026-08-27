package com.hazard;

import com.hazard.controller.relocation.RelocationController;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.relocation.BatchRelocationRecommendationDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.RelocationRequestDto;
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

/**
 * Stage 7B — Recommendation Controller Tests.
 * Verifies POST and GET /recommendation endpoints.
 */
class RecommendationControllerTests {

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
    @DisplayName("POST /api/v1/relocation/recommendation")
    class PostRecommendationTests {

        @Test
        @DisplayName("Empty request list returns 200 OK with empty result")
        void testEmptyRequestList() {
            ResponseEntity<ApiResponse<BatchRelocationRecommendationDto>> response =
                    controller.generateRecommendations(Collections.emptyList());

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals(0, response.getBody().getData().getTotalCases());
        }

        @Test
        @DisplayName("Valid request list calls recommendation service and returns 200 OK")
        void testValidRequestList() {
            BatchRelocationRecommendationDto mockBatch = new BatchRelocationRecommendationDto();
            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.RECOMMENDED);
            mockBatch.addRecommendation(rec);
            mockBatch.setSummary("1 recommendation generated");

            when(recommendationService.recommendBatchForRequests(any())).thenReturn(mockBatch);

            RelocationRequestDto req = new RelocationRequestDto("Sitamarhi");
            ResponseEntity<ApiResponse<BatchRelocationRecommendationDto>> response =
                    controller.generateRecommendations(Collections.singletonList(req));

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalCases());
            verify(recommendationService, times(1)).recommendBatchForRequests(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/relocation/recommendation")
    class GetRecommendationTests {

        @Test
        @DisplayName("Single district query param calls recommendation service")
        void testSingleDistrictGet() {
            BatchRelocationRecommendationDto mockBatch = new BatchRelocationRecommendationDto();
            mockBatch.setSummary("Sitamarhi recommendations");
            when(recommendationService.recommendBatchForRequests(any())).thenReturn(mockBatch);

            ResponseEntity<ApiResponse<BatchRelocationRecommendationDto>> response =
                    controller.getRelocationRecommendations("Sitamarhi", 25.0, "MARGINAL", 200L);

            assertEquals(200, response.getStatusCode().value());
            verify(recommendationService, times(1)).recommendBatchForRequests(any());
        }

        @Test
        @DisplayName("Comma-separated districts query param builds multiple requests")
        void testMultipleDistrictsGet() {
            BatchRelocationRecommendationDto mockBatch = new BatchRelocationRecommendationDto();
            when(recommendationService.recommendBatchForRequests(any())).thenReturn(mockBatch);

            ResponseEntity<ApiResponse<BatchRelocationRecommendationDto>> response =
                    controller.getRelocationRecommendations("Sitamarhi,Patna,Darbhanga", 30.0, "SUITABLE", 300L);

            assertEquals(200, response.getStatusCode().value());
            verify(recommendationService, times(1)).recommendBatchForRequests(argThat(list -> list.size() == 3));
        }
    }
}
