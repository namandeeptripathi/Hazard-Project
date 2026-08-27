package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.RelocationRequestDto;
import com.hazard.dto.relocation.explain.BatchRelocationDecisionExplanationDto;
import com.hazard.dto.relocation.explain.RelocationDecisionExplanationDto;
import com.hazard.service.relocation.RelocationExplainabilityService;
import com.hazard.service.relocation.RelocationRecommendationService;
import com.hazard.service.relocation.explain.RelocationExplainabilityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Stage 7C — Relocation Explainability Service Integration Tests.
 */
class RelocationExplainabilityServiceTests {

    private RelocationExplainabilityService service;
    private RelocationRecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = mock(RelocationRecommendationService.class);
        service = new RelocationExplainabilityService(recommendationService, new RelocationExplainabilityEngine());
    }

    @Nested
    @DisplayName("explainRequest()")
    class ExplainRequestTests {

        @Test
        @DisplayName("Successfully explains single request through complete pipeline")
        void testSingleRequestExplanation() {
            RelocationRequestDto req = new RelocationRequestDto("Sitamarhi");
            req.setVulnerablePopulation(200L);

            RelocationRecommendationDto mockRec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.RECOMMENDED);
            mockRec.setHabitationName("Village Alpha");
            mockRec.setDistrict("Sitamarhi");
            mockRec.setPriorityScore(0.85);
            mockRec.setPriorityLevel(PriorityLevel.IMMEDIATE);
            mockRec.setVulnerablePopulation(200L);
            mockRec.setAllocatedPopulation(200L);
            mockRec.setUnallocatedPopulation(0L);

            RecommendedDestinationDto primary = new RecommendedDestinationDto("SITE-01", "Central Shelter", 0.90, 1);
            primary.setDistrict("Sitamarhi");
            primary.setDistanceKilometers(3.2);
            primary.setAvailableCapacity(500);
            mockRec.setPrimaryDestination(primary);

            when(recommendationService.recommendForRequest(any())).thenReturn(mockRec);

            RelocationDecisionExplanationDto exp = service.explainRequest(req);

            assertNotNull(exp);
            assertEquals("HAB-01", exp.getHabitationId());
            assertEquals("Village Alpha", exp.getHabitationName());
            assertNotNull(exp.getDecisionRationale());
            assertNotNull(exp.getRiskExplanation());
            assertNotNull(exp.getRelocationExplanation());
            assertNotNull(exp.getCapacityExplanation());
            assertTrue(exp.isValid());
        }

        @Test
        @DisplayName("Null request returns safe default explanation")
        void testNullRequest() {
            RelocationDecisionExplanationDto exp = service.explainRequest(null);

            assertNotNull(exp);
            assertNotNull(exp.getRiskExplanation());
        }
    }

    @Nested
    @DisplayName("explainBatchRequests()")
    class ExplainBatchRequestsTests {

        @Test
        @DisplayName("Processes multiple requests and aggregates batch results correctly")
        void testBatchRequests() {
            RelocationRequestDto req1 = new RelocationRequestDto("Sitamarhi");
            RelocationRequestDto req2 = new RelocationRequestDto("Patna");

            RelocationRecommendationDto rec1 = new RelocationRecommendationDto("H1", RecommendationStatus.RECOMMENDED);
            rec1.setPriorityScore(0.85);
            rec1.setPriorityLevel(PriorityLevel.IMMEDIATE);
            rec1.setVulnerablePopulation(100L);
            rec1.setAllocatedPopulation(100L);
            rec1.setUnallocatedPopulation(0L);
            rec1.setPrimaryDestination(new RecommendedDestinationDto("S1", "Shelter 1", 0.9, 1));

            RelocationRecommendationDto rec2 = new RelocationRecommendationDto("H2", RecommendationStatus.RECOMMENDED);
            rec2.setPriorityScore(0.55);
            rec2.setPriorityLevel(PriorityLevel.SHORT_TERM);
            rec2.setVulnerablePopulation(200L);
            rec2.setAllocatedPopulation(200L);
            rec2.setUnallocatedPopulation(0L);
            rec2.setPrimaryDestination(new RecommendedDestinationDto("S2", "Shelter 2", 0.8, 1));

            when(recommendationService.recommendForRequest(req1)).thenReturn(rec1);
            when(recommendationService.recommendForRequest(req2)).thenReturn(rec2);

            BatchRelocationDecisionExplanationDto batch = service.explainBatchRequests(Arrays.asList(req1, req2));

            assertNotNull(batch);
            assertEquals(2, batch.getTotalCases());
            assertEquals(1, batch.getImmediateCases());
            assertEquals(1, batch.getShortTermCases());
            assertEquals(2, batch.getValidExplanations());
            assertNotNull(batch.getSummary());
        }

        @Test
        @DisplayName("Empty requests list returns empty batch result")
        void testEmptyBatch() {
            BatchRelocationDecisionExplanationDto batch = service.explainBatchRequests(Collections.emptyList());

            assertEquals(0, batch.getTotalCases());
            assertTrue(batch.getExplanations().isEmpty());
        }
    }
}
