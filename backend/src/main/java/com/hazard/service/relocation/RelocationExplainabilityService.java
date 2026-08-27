package com.hazard.service.relocation;

import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.RelocationRequestDto;
import com.hazard.dto.relocation.explain.BatchRelocationDecisionExplanationDto;
import com.hazard.dto.relocation.explain.RelocationDecisionExplanationDto;
import com.hazard.service.relocation.explain.RelocationExplainabilityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stage 7C — Relocation Decision Explainability Service.
 *
 * Coordinates the full Stage 6 -> 7A -> 7B -> 7C pipeline to deliver transparent,
 * auditable, and structured decision explanations.
 */
@Service
public class RelocationExplainabilityService {

    private static final Logger log = LoggerFactory.getLogger(RelocationExplainabilityService.class);

    private final RelocationRecommendationService recommendationService;
    private final RelocationExplainabilityEngine explainabilityEngine;

    @Autowired
    public RelocationExplainabilityService(RelocationRecommendationService recommendationService,
                                           RelocationExplainabilityEngine explainabilityEngine) {
        this.recommendationService = recommendationService;
        this.explainabilityEngine = explainabilityEngine;
    }

    public RelocationExplainabilityService(RelocationRecommendationService recommendationService) {
        this(recommendationService, new RelocationExplainabilityEngine());
    }

    public RelocationExplainabilityService() {
        this(new RelocationRecommendationService(), new RelocationExplainabilityEngine());
    }

    /**
     * Explains an already computed decision from priority and recommendation results.
     */
    public RelocationDecisionExplanationDto explainDecision(RelocationPriorityResultDto priorityResult,
                                                           RelocationRecommendationDto recommendation) {
        return explainabilityEngine.explainDecision(priorityResult, recommendation);
    }

    /**
     * Runs the complete Stage 6 -> 7A -> 7B -> 7C pipeline for a single request and returns the explanation.
     */
    public RelocationDecisionExplanationDto explainRequest(RelocationRequestDto request) {
        if (request == null) {
            return explainabilityEngine.explainDecision(null, null);
        }

        // 1. Generate Recommendation (runs Stage 6 & Stage 7A internally)
        RelocationRecommendationDto recommendation = (recommendationService != null)
                ? recommendationService.recommendForRequest(request)
                : null;

        // 2. Reconstruct Priority Result DTO from recommendation metadata
        RelocationPriorityResultDto priorityResult = extractPriorityResult(recommendation);

        // 3. Generate Complete Explanation
        return explainabilityEngine.explainDecision(priorityResult, recommendation);
    }

    /**
     * Runs the complete pipeline for a batch of requests and returns a BatchRelocationDecisionExplanationDto.
     */
    public BatchRelocationDecisionExplanationDto explainBatchRequests(List<RelocationRequestDto> requests) {
        BatchRelocationDecisionExplanationDto batch = new BatchRelocationDecisionExplanationDto();

        if (requests == null || requests.isEmpty()) {
            batch.setSummary("No relocation requests provided for decision explanation.");
            batch.recomputeStatistics();
            return batch;
        }

        for (RelocationRequestDto req : requests) {
            try {
                RelocationDecisionExplanationDto exp = explainRequest(req);
                batch.addExplanation(exp);
            } catch (Exception e) {
                log.error("Error generating decision explanation for request {}: {}", req, e.getMessage(), e);
                RelocationDecisionExplanationDto errExp = new RelocationDecisionExplanationDto();
                errExp.setHabitationId(req.getHabitationId() != null ? req.getHabitationId() : "HAB-ERR");
                errExp.setValid(false);
                errExp.addValidationNote("Error generating explanation: " + e.getMessage());
                batch.addExplanation(errExp);
            }
        }

        batch.recomputeStatistics();
        String summary = String.format(
                "Processed %d decision explanation(s): %d valid, %d with warnings. Priority breakdown: %d Immediate, %d Short-Term, %d Medium-Term, %d Monitoring.",
                batch.getTotalCases(),
                batch.getValidExplanations(),
                batch.getInvalidExplanations(),
                batch.getImmediateCases(),
                batch.getShortTermCases(),
                batch.getMediumTermCases(),
                batch.getMonitoringCases()
        );
        batch.setSummary(summary);
        log.info(summary);

        return batch;
    }

    private RelocationPriorityResultDto extractPriorityResult(RelocationRecommendationDto rec) {
        if (rec == null) return null;

        RelocationPriorityResultDto p = new RelocationPriorityResultDto();
        p.setHabitationId(rec.getHabitationId());
        p.setHabitationName(rec.getHabitationName());
        p.setDistrict(rec.getDistrict());
        p.setState(rec.getState());
        p.setPlanId(rec.getPlanId());
        p.setPriorityScore(rec.getPriorityScore());
        p.setPriorityLevel(rec.getPriorityLevel());
        p.setPriorityRank(rec.getPriorityRank());
        p.setVulnerablePopulation(rec.getVulnerablePopulation());
        p.setUnallocatedPopulation(rec.getUnallocatedPopulation());
        p.setUrgency(rec.getUrgency());
        return p;
    }
}
