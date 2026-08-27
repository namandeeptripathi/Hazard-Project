package com.hazard.service.relocation;

import com.hazard.dto.relocation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7A — Relocation Priority Service.
 *
 * Orchestrates the Priority Engine pipeline:
 * 1. Scoring: computes composite priority score per relocation case.
 * 2. Classification: maps score to PriorityLevel tier.
 * 3. Ranking: deterministically orders all cases by priority.
 *
 * <p>This service consumes Stage 6 outputs (RelocationPlanDto + VulnerableHabitationDto)
 * and produces fully ranked, classified priority results for downstream consumption
 * by Stage 7B (Recommendation) and Stage 7C (Explainability).
 */
@Service
public class RelocationPriorityService {

    private static final Logger log = LoggerFactory.getLogger(RelocationPriorityService.class);

    private final PriorityScoringEngine scoringEngine;
    private final PriorityRankingEngine rankingEngine;

    @Autowired
    public RelocationPriorityService(PriorityScoringEngine scoringEngine,
                                     PriorityRankingEngine rankingEngine) {
        this.scoringEngine = scoringEngine;
        this.rankingEngine = rankingEngine;
    }

    /**
     * Default constructor for isolated unit testing.
     */
    public RelocationPriorityService() {
        PriorityScoringConfig config = new PriorityScoringConfig();
        PriorityClassificationEngine classificationEngine = new PriorityClassificationEngine(config);
        this.scoringEngine = new PriorityScoringEngine(config, classificationEngine);
        this.rankingEngine = new PriorityRankingEngine();
    }

    /**
     * Scores and classifies a single relocation case.
     *
     * @param plan        the Stage 6 relocation plan
     * @param habitation  the source vulnerable habitation
     * @return scored and classified priority result
     */
    public RelocationPriorityResultDto scoreSingle(RelocationPlanDto plan, VulnerableHabitationDto habitation) {
        if (plan == null && habitation == null) {
            log.warn("Both plan and habitation are null; returning default priority result");
            RelocationPriorityResultDto result = new RelocationPriorityResultDto();
            result.setPriorityScore(0.0);
            result.setPriorityLevel(com.hazard.domain.relocation.PriorityLevel.MONITORING);
            return result;
        }
        return scoringEngine.score(plan, habitation);
    }

    /**
     * Scores, classifies, and ranks multiple relocation cases.
     *
     * <p>Each pair of (plan, habitation) at the same index represents one relocation case.
     * If the lists have different lengths, only the shorter length is processed.
     *
     * @param plans        the Stage 6 relocation plans
     * @param habitations  the source vulnerable habitations (paired by index)
     * @return fully ranked priority result with tier distribution
     */
    public PriorityRankingResultDto scoreAndRankAll(List<RelocationPlanDto> plans,
                                                    List<VulnerableHabitationDto> habitations) {
        if (plans == null || habitations == null || plans.isEmpty() || habitations.isEmpty()) {
            log.warn("Empty or null input provided to scoreAndRankAll");
            PriorityRankingResultDto empty = new PriorityRankingResultDto();
            empty.setRankingSummary("No relocation cases provided for priority scoring and ranking.");
            return empty;
        }

        int count = Math.min(plans.size(), habitations.size());
        List<RelocationPriorityResultDto> scoredResults = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            RelocationPlanDto plan = plans.get(i);
            VulnerableHabitationDto hab = habitations.get(i);

            try {
                RelocationPriorityResultDto result = scoringEngine.score(plan, hab);
                scoredResults.add(result);
            } catch (Exception e) {
                log.error("Error scoring relocation case {}: {}", i, e.getMessage(), e);
                // Create a default low-priority entry so ranking is still complete
                RelocationPriorityResultDto fallback = new RelocationPriorityResultDto();
                fallback.setPriorityScore(0.0);
                fallback.setPriorityLevel(com.hazard.domain.relocation.PriorityLevel.MONITORING);
                if (hab != null) {
                    fallback.setHabitationId(hab.getHabitationId());
                    fallback.setHabitationName(hab.getHabitationName());
                    fallback.setDistrict(hab.getDistrict());
                }
                scoredResults.add(fallback);
            }
        }

        return rankingEngine.rank(scoredResults);
    }

    /**
     * Convenience method: scores, classifies, and ranks a list of relocation plans
     * where each plan's habitation context is extracted from the plan's first assignment
     * or unallocated habitation.
     *
     * @param plans the Stage 6 relocation plans
     * @return fully ranked priority result
     */
    public PriorityRankingResultDto scoreAndRankPlans(List<RelocationPlanDto> plans) {
        if (plans == null || plans.isEmpty()) {
            PriorityRankingResultDto empty = new PriorityRankingResultDto();
            empty.setRankingSummary("No relocation plans provided for priority ranking.");
            return empty;
        }

        List<VulnerableHabitationDto> habitations = new ArrayList<>(plans.size());
        for (RelocationPlanDto plan : plans) {
            habitations.add(extractHabitation(plan));
        }

        return scoreAndRankAll(plans, habitations);
    }

    /**
     * Extracts a representative VulnerableHabitationDto from a RelocationPlanDto
     * by examining its assignments and unallocated habitations.
     */
    private VulnerableHabitationDto extractHabitation(RelocationPlanDto plan) {
        if (plan == null) {
            return new VulnerableHabitationDto();
        }

        // Try first assignment
        if (plan.getAssignments() != null && !plan.getAssignments().isEmpty()) {
            RelocationAssignmentDto assignment = plan.getAssignments().get(0);
            VulnerableHabitationDto hab = new VulnerableHabitationDto();
            hab.setHabitationId(assignment.getHabitationId());
            hab.setHabitationName(assignment.getHabitationName());
            hab.setDistrict(assignment.getOriginDistrict());
            hab.setVulnerablePopulation(assignment.getVulnerablePopulation());
            hab.setUrgency(assignment.getUrgency());
            return hab;
        }

        // Try first unallocated habitation
        if (plan.getUnallocatedHabitations() != null && !plan.getUnallocatedHabitations().isEmpty()) {
            return plan.getUnallocatedHabitations().get(0);
        }

        // Fallback: create minimal habitation from plan metadata
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setDistrict(plan.getDistrict());
        hab.setVulnerablePopulation(plan.getTotalVulnerablePopulation());
        return hab;
    }
}
