package com.hazard.service.relocation;

import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.*;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7B — Relocation Recommendation Orchestration Service.
 *
 * Coordinates the full Stage 6 -> Stage 7A -> Stage 7B pipeline:
 * 1. Resolves vulnerable habitation and spatial safe sites (Stage 6).
 * 2. Computes priority assessment and ranking (Stage 7A).
 * 3. Generates the optimal recommended destination and fallback options (Stage 7B).
 */
@Service
public class RelocationRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RelocationRecommendationService.class);

    private final RelocationPlanningService planningService;
    private final RelocationPriorityService priorityService;
    private final RelocationRecommendationEngine recommendationEngine;

    @Autowired
    public RelocationRecommendationService(RelocationPlanningService planningService,
                                           RelocationPriorityService priorityService,
                                           RelocationRecommendationEngine recommendationEngine) {
        this.planningService = planningService;
        this.priorityService = priorityService;
        this.recommendationEngine = recommendationEngine;
    }

    /**
     * Constructor for isolated unit testing.
     */
    public RelocationRecommendationService(RelocationPlanningService planningService,
                                           RelocationPriorityService priorityService) {
        this(planningService, priorityService, new RelocationRecommendationEngine());
    }

    public RelocationRecommendationService() {
        this(null, new RelocationPriorityService(), new RelocationRecommendationEngine());
    }

    /**
     * Generates a recommendation for a single client request.
     */
    public RelocationRecommendationDto recommendForRequest(RelocationRequestDto request) {
        if (request == null) {
            RelocationRecommendationDto invalid = new RelocationRecommendationDto();
            invalid.setStatus(RecommendationStatus.INVALID_SOURCE);
            invalid.setRecommendationSummary("Relocation request cannot be null.");
            return invalid;
        }

        // 1. Resolve Habitation
        VulnerableHabitationDto habitation = (planningService != null)
                ? planningService.resolveVulnerableHabitation(request)
                : request.getHabitation();

        if (habitation == null) {
            habitation = createFallbackHabitation(request);
        }

        // 2. Resolve Candidate Safe Sites
        List<CandidateSafeSiteDto> candidateSites = (planningService != null)
                ? planningService.resolveCandidateSafeSites(habitation, request)
                : new ArrayList<>();

        // 3. Score Priority (Stage 7A)
        RelocationPlanDto plan = (planningService != null)
                ? planningService.planRelocation(request)
                : null;
        RelocationPriorityResultDto priorityResult = priorityService.scoreSingle(plan, habitation);

        // 4. Generate Recommendation (Stage 7B)
        return recommendationEngine.generateRecommendation(
                habitation, candidateSites, priorityResult,
                request.getMaxTransitDistanceKm(), request.getMinSuitabilityClass()
        );
    }

    /**
     * Generates recommendations for a batch of client requests.
     */
    public BatchRelocationRecommendationDto recommendBatchForRequests(List<RelocationRequestDto> requests) {
        BatchRelocationRecommendationDto batchResult = new BatchRelocationRecommendationDto();

        if (requests == null || requests.isEmpty()) {
            batchResult.setSummary("No relocation requests provided for recommendation generation.");
            batchResult.recomputeStatistics();
            return batchResult;
        }

        for (RelocationRequestDto req : requests) {
            try {
                RelocationRecommendationDto rec = recommendForRequest(req);
                batchResult.addRecommendation(rec);
            } catch (Exception e) {
                log.error("Error generating recommendation for request {}: {}", req, e.getMessage(), e);
                RelocationRecommendationDto errRec = new RelocationRecommendationDto();
                errRec.setStatus(RecommendationStatus.INVALID_SOURCE);
                errRec.setRecommendationSummary("Error evaluating request: " + e.getMessage());
                batchResult.addRecommendation(errRec);
            }
        }

        batchResult.recomputeStatistics();
        String summary = String.format(
                "Processed %d relocation recommendation case(s): %d recommended, %d no feasible destination, %d capacity deficit.",
                batchResult.getTotalCases(),
                batchResult.getSuccessfulRecommendations(),
                batchResult.getNoFeasibleRecommendations(),
                batchResult.getCapacityDeficitRecommendations()
        );
        batchResult.setSummary(summary);
        log.info(summary);

        return batchResult;
    }

    /**
     * Generates a recommendation directly from an existing habitation, candidate safe sites, and priority result.
     */
    public RelocationRecommendationDto recommendForCase(VulnerableHabitationDto habitation,
                                                        List<CandidateSafeSiteDto> candidateSites,
                                                        RelocationPriorityResultDto priorityResult,
                                                        Double maxTransitDistanceKm,
                                                        com.hazard.domain.safesite.SuitabilityClass minSuitabilityClass) {
        return recommendationEngine.generateRecommendation(
                habitation, candidateSites, priorityResult, maxTransitDistanceKm, minSuitabilityClass
        );
    }

    private VulnerableHabitationDto createFallbackHabitation(RelocationRequestDto request) {
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId(request.getHabitationId() != null ? request.getHabitationId() : "HAB-REQ");
        hab.setHabitationName("Origin Habitation");
        hab.setDistrict(request.getDistrict() != null ? request.getDistrict() : "Sitamarhi");
        hab.setState("Bihar");
        hab.setLatitude(request.getOriginLatitude());
        hab.setLongitude(request.getOriginLongitude());
        hab.setVulnerablePopulation(request.getVulnerablePopulation() != null ? request.getVulnerablePopulation() : 250L);
        return hab;
    }
}
