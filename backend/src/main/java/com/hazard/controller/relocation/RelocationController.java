package com.hazard.controller.relocation;

import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.relocation.*;
import com.hazard.dto.relocation.explain.BatchRelocationDecisionExplanationDto;
import com.hazard.dto.relocation.explain.RelocationDecisionExplanationDto;
import com.hazard.service.relocation.RelocationExplainabilityService;
import com.hazard.service.relocation.RelocationPlanningService;
import com.hazard.service.relocation.RelocationPriorityService;
import com.hazard.service.relocation.RelocationRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller for Stage 6 — Relocation Intelligence and Stage 7A — Priority Engine.
 *
 * Exposes endpoints for end-to-end automated emergency evacuation shelter planning:
 * - Feasibility Filtering (Hazard Safety, Suitability, Distance, Capacity gates)
 * - Deterministic Feasible Safe Site Ranking
 * - Capacity-Aware Population Allocation & Deficit Accounting
 * - Explanatory Relocation Assignment Plans
 * - Priority Scoring, Classification, and Ranking (Stage 7A)
 */
@RestController
@RequestMapping("/api/v1/relocation")
@CrossOrigin(origins = "*")
@Tag(
        name = "Relocation Intelligence (Stage 6, 7A, 7B & 7C)",
        description = "Automated emergency shelter relocation planning, multi-gate feasibility filtering, deterministic ranking, capacity-aware population allocation, priority engine, recommendation engine, and explainability engine"
)
public class RelocationController {

    private final RelocationPlanningService relocationPlanningService;
    private final RelocationPriorityService relocationPriorityService;
    private final RelocationRecommendationService relocationRecommendationService;
    private final RelocationExplainabilityService relocationExplainabilityService;

    public RelocationController(RelocationPlanningService relocationPlanningService,
                                RelocationPriorityService relocationPriorityService,
                                RelocationRecommendationService relocationRecommendationService,
                                RelocationExplainabilityService relocationExplainabilityService) {
        this.relocationPlanningService = relocationPlanningService;
        this.relocationPriorityService = relocationPriorityService;
        this.relocationRecommendationService = relocationRecommendationService;
        this.relocationExplainabilityService = relocationExplainabilityService;
    }

    /**
     * POST /api/v1/relocation/plan
     * Generates a deterministic, capacity-aware relocation plan for a vulnerable habitation.
     */
    @PostMapping(value = {"/plan", ""})
    @Operation(
            summary = "Generate Capacity-Aware Relocation Plan",
            description = "Evaluates candidate safe shelters, filters by feasibility, ranks destinations deterministically, and allocates vulnerable populations to shelter capacities with transparent deficit reporting."
    )
    public ResponseEntity<ApiResponse<RelocationPlanDto>> generateRelocationPlan(
            @RequestBody RelocationRequestDto request
    ) {
        if (request == null) {
            request = new RelocationRequestDto();
        }

        RelocationPlanDto plan = relocationPlanningService.planRelocation(request);
        String message = plan.getPlanSummary() != null ? plan.getPlanSummary() : "Relocation plan generated successfully";
        return ResponseEntity.ok(ApiResponse.ok(plan, message));
    }

    /**
     * GET /api/v1/relocation/plan
     * Convenience query endpoint to generate a relocation plan via query parameters.
     */
    @GetMapping(value = {"/plan", ""})
    @Operation(
            summary = "Query Relocation Plan by District/Hazard",
            description = "Quickly computes a relocation plan using query parameters for target district, hazard event, transit distance radius, and minimum suitability class."
    )
    public ResponseEntity<ApiResponse<RelocationPlanDto>> getRelocationPlan(
            @Parameter(description = "Target administrative district name (e.g. Sitamarhi, Patna)", example = "Sitamarhi")
            @RequestParam(name = "district", required = false, defaultValue = "Sitamarhi") String district,

            @Parameter(description = "Optional Stage 3 hazard event identifier", example = "DFO_4123")
            @RequestParam(name = "hazardId", required = false) String hazardId,

            @Parameter(description = "Optional hazard category (FLOOD, EXTREME_RAINFALL, etc.)", example = "FLOOD")
            @RequestParam(name = "hazardType", required = false) String hazardType,

            @Parameter(description = "Maximum transit distance radius in kilometers (default: 25.0 km)", example = "25.0")
            @RequestParam(name = "maxDistanceKm", required = false, defaultValue = "25.0") Double maxDistanceKm,

            @Parameter(description = "Minimum acceptable suitability tier (HIGHLY_SUITABLE, SUITABLE, MARGINAL)", example = "MARGINAL")
            @RequestParam(name = "minSuitability", required = false, defaultValue = "MARGINAL") String minSuitability,

            @Parameter(description = "Vulnerable population count requiring relocation (default: 250)", example = "250")
            @RequestParam(name = "population", required = false) Long population
    ) {
        RelocationRequestDto request = new RelocationRequestDto();
        request.setDistrict(district);
        request.setHazardId(hazardId);
        request.setHazardType(hazardType);
        request.setMaxTransitDistanceKm(maxDistanceKm);
        if (minSuitability != null && !minSuitability.trim().isEmpty()) {
            request.setMinSuitabilityClass(SuitabilityClass.fromString(minSuitability));
        }
        request.setVulnerablePopulation(population);

        RelocationPlanDto plan = relocationPlanningService.planRelocation(request);
        String message = plan.getPlanSummary() != null ? plan.getPlanSummary() : "Relocation plan generated successfully";
        return ResponseEntity.ok(ApiResponse.ok(plan, message));
    }

    // =====================================================================================
    // Stage 7A — Priority Engine Endpoints
    // =====================================================================================

    /**
     * POST /api/v1/relocation/priority
     * Generates relocation plans for multiple requests, then scores and ranks them by priority.
     */
    @PostMapping("/priority")
    @Operation(
            summary = "Score and Rank Relocation Cases by Priority (Stage 7A)",
            description = "Accepts multiple relocation requests, runs the Stage 6 pipeline for each, " +
                    "then computes priority scores, classifies priority levels, and ranks all cases deterministically."
    )
    public ResponseEntity<ApiResponse<PriorityRankingResultDto>> rankRelocationPriorities(
            @RequestBody List<RelocationRequestDto> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            PriorityRankingResultDto emptyResult = new PriorityRankingResultDto();
            emptyResult.setRankingSummary("No relocation requests provided.");
            return ResponseEntity.ok(ApiResponse.ok(emptyResult, "No relocation requests provided."));
        }

        List<RelocationPlanDto> plans = new ArrayList<>(requests.size());
        List<VulnerableHabitationDto> habitations = new ArrayList<>(requests.size());

        for (RelocationRequestDto request : requests) {
            RelocationPlanDto plan = relocationPlanningService.planRelocation(
                    request != null ? request : new RelocationRequestDto()
            );
            plans.add(plan);

            VulnerableHabitationDto hab = relocationPlanningService.resolveVulnerableHabitation(
                    request != null ? request : new RelocationRequestDto()
            );
            habitations.add(hab);
        }

        PriorityRankingResultDto result = relocationPriorityService.scoreAndRankAll(plans, habitations);
        String message = result.getRankingSummary() != null ? result.getRankingSummary() : "Priority ranking completed";
        return ResponseEntity.ok(ApiResponse.ok(result, message));
    }

    /**
     * GET /api/v1/relocation/priority
     * Convenience endpoint to generate and prioritize relocation plans for a comma-separated list of districts.
     */
    @GetMapping("/priority")
    @Operation(
            summary = "Query Priority Ranking by Districts (Stage 7A)",
            description = "Generates relocation plans for one or more districts and returns a priority-ranked result."
    )
    public ResponseEntity<ApiResponse<PriorityRankingResultDto>> getRelocationPriority(
            @Parameter(description = "Comma-separated list of district names", example = "Sitamarhi,Patna,Darbhanga")
            @RequestParam(name = "districts", required = false, defaultValue = "Sitamarhi") String districts,

            @Parameter(description = "Maximum transit distance radius in kilometers", example = "25.0")
            @RequestParam(name = "maxDistanceKm", required = false, defaultValue = "25.0") Double maxDistanceKm,

            @Parameter(description = "Vulnerable population count per district (default: 250)", example = "250")
            @RequestParam(name = "population", required = false) Long population
    ) {
        String[] districtArray = districts.split(",");
        List<RelocationPlanDto> plans = new ArrayList<>(districtArray.length);
        List<VulnerableHabitationDto> habitations = new ArrayList<>(districtArray.length);

        for (String district : districtArray) {
            String trimmed = district.trim();
            if (trimmed.isEmpty()) continue;

            RelocationRequestDto request = new RelocationRequestDto();
            request.setDistrict(trimmed);
            request.setMaxTransitDistanceKm(maxDistanceKm);
            request.setVulnerablePopulation(population);

            RelocationPlanDto plan = relocationPlanningService.planRelocation(request);
            plans.add(plan);

            VulnerableHabitationDto hab = relocationPlanningService.resolveVulnerableHabitation(request);
            habitations.add(hab);
        }

        PriorityRankingResultDto result = relocationPriorityService.scoreAndRankAll(plans, habitations);
        String message = result.getRankingSummary() != null ? result.getRankingSummary() : "Priority ranking completed";
        return ResponseEntity.ok(ApiResponse.ok(result, message));
    }

    // =====================================================================================
    // Stage 7B — Recommendation Engine Endpoints
    // =====================================================================================

    /**
     * POST /api/v1/relocation/recommendation
     * Generates optimal destination recommendations for one or multiple relocation requests.
     */
    @PostMapping("/recommendation")
    @Operation(
            summary = "Generate Relocation Destination Recommendation (Stage 7B)",
            description = "Accepts one or more relocation requests, evaluates regional safe site candidates against hard feasibility gates, computes multi-factor destination suitability scores, and returns the top recommended destination along with fallback alternatives."
    )
    public ResponseEntity<ApiResponse<BatchRelocationRecommendationDto>> generateRecommendations(
            @RequestBody List<RelocationRequestDto> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            BatchRelocationRecommendationDto emptyResult = new BatchRelocationRecommendationDto();
            emptyResult.setSummary("No relocation requests provided for recommendation.");
            emptyResult.recomputeStatistics();
            return ResponseEntity.ok(ApiResponse.ok(emptyResult, "No relocation requests provided."));
        }

        BatchRelocationRecommendationDto batchResult = relocationRecommendationService.recommendBatchForRequests(requests);
        String message = batchResult.getSummary() != null ? batchResult.getSummary() : "Recommendations generated successfully";
        return ResponseEntity.ok(ApiResponse.ok(batchResult, message));
    }

    /**
     * GET /api/v1/relocation/recommendation
     * Convenience query endpoint to generate destination recommendations for a comma-separated list of districts.
     */
    @GetMapping("/recommendation")
    @Operation(
            summary = "Query Relocation Destination Recommendations by District (Stage 7B)",
            description = "Evaluates and generates destination recommendations for one or more target districts using query parameters."
    )
    public ResponseEntity<ApiResponse<BatchRelocationRecommendationDto>> getRelocationRecommendations(
            @Parameter(description = "Comma-separated list of target district names", example = "Sitamarhi,Patna")
            @RequestParam(name = "districts", required = false, defaultValue = "Sitamarhi") String districts,

            @Parameter(description = "Maximum transit distance radius in kilometers", example = "25.0")
            @RequestParam(name = "maxDistanceKm", required = false, defaultValue = "25.0") Double maxDistanceKm,

            @Parameter(description = "Minimum acceptable suitability tier (HIGHLY_SUITABLE, SUITABLE, MARGINAL)", example = "MARGINAL")
            @RequestParam(name = "minSuitability", required = false, defaultValue = "MARGINAL") String minSuitability,

            @Parameter(description = "Vulnerable population count per district (default: 250)", example = "250")
            @RequestParam(name = "population", required = false) Long population
    ) {
        String[] districtArray = districts.split(",");
        List<RelocationRequestDto> requests = new ArrayList<>(districtArray.length);

        for (String district : districtArray) {
            String trimmed = district.trim();
            if (trimmed.isEmpty()) continue;

            RelocationRequestDto req = new RelocationRequestDto();
            req.setDistrict(trimmed);
            req.setMaxTransitDistanceKm(maxDistanceKm);
            if (minSuitability != null && !minSuitability.trim().isEmpty()) {
                req.setMinSuitabilityClass(SuitabilityClass.fromString(minSuitability));
            }
            req.setVulnerablePopulation(population);
            requests.add(req);
        }

        BatchRelocationRecommendationDto batchResult = relocationRecommendationService.recommendBatchForRequests(requests);
        String message = batchResult.getSummary() != null ? batchResult.getSummary() : "Recommendations generated successfully";
        return ResponseEntity.ok(ApiResponse.ok(batchResult, message));
    }

    // =====================================================================================
    // Stage 7C & 7D — Decision & Explainability Engine Endpoints
    // =====================================================================================

    /**
     * POST /api/v1/relocation/decision (aliases: /explain, /explainability)
     * Generates complete, transparent, and auditable consolidated decisions answering WHO, WHERE, and WHY.
     */
    @PostMapping(value = {"/decision", "/explain", "/explainability"})
    @Operation(
            summary = "Generate Consolidated Relocation Decisions (Stage 7D)",
            description = "Consumes Stage 7A Priority Assessment, Stage 7B Recommendation, and Stage 7C Explainability to deliver a transparent, unified decision answering WHO, WHERE, and WHY with full mathematical evidence and integrity validation."
    )
    public ResponseEntity<ApiResponse<BatchRelocationDecisionExplanationDto>> explainDecision(
            @RequestBody List<RelocationRequestDto> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            BatchRelocationDecisionExplanationDto emptyResult = new BatchRelocationDecisionExplanationDto();
            emptyResult.setSummary("No relocation requests provided for decision explanation.");
            emptyResult.recomputeStatistics();
            return ResponseEntity.ok(ApiResponse.ok(emptyResult, "No relocation requests provided."));
        }

        BatchRelocationDecisionExplanationDto batchResult = relocationExplainabilityService.explainBatchRequests(requests);
        String message = batchResult.getSummary() != null ? batchResult.getSummary() : "Decision explanations generated successfully";
        return ResponseEntity.ok(ApiResponse.ok(batchResult, message));
    }

    /**
     * GET /api/v1/relocation/decision (aliases: /explain, /explainability)
     * Convenience query endpoint to generate consolidated decisions for comma-separated districts.
     */
    @GetMapping(value = {"/decision", "/explain", "/explainability"})
    @Operation(
            summary = "Query Consolidated Decisions by District (Stage 7D)",
            description = "Evaluates, prioritizes, recommends, explains, and validates relocation decisions for one or more districts using query parameters."
    )
    public ResponseEntity<ApiResponse<BatchRelocationDecisionExplanationDto>> getDecisionExplanations(
            @Parameter(description = "Comma-separated list of target district names", example = "Sitamarhi,Patna")
            @RequestParam(name = "districts", required = false, defaultValue = "Sitamarhi") String districts,

            @Parameter(description = "Maximum transit distance radius in kilometers", example = "25.0")
            @RequestParam(name = "maxDistanceKm", required = false, defaultValue = "25.0") Double maxDistanceKm,

            @Parameter(description = "Minimum acceptable suitability tier (HIGHLY_SUITABLE, SUITABLE, MARGINAL)", example = "MARGINAL")
            @RequestParam(name = "minSuitability", required = false, defaultValue = "MARGINAL") String minSuitability,

            @Parameter(description = "Vulnerable population count per district (default: 250)", example = "250")
            @RequestParam(name = "population", required = false) Long population
    ) {
        String[] districtArray = districts.split(",");
        List<RelocationRequestDto> requests = new ArrayList<>(districtArray.length);

        for (String district : districtArray) {
            String trimmed = district.trim();
            if (trimmed.isEmpty()) continue;

            RelocationRequestDto req = new RelocationRequestDto();
            req.setDistrict(trimmed);
            req.setMaxTransitDistanceKm(maxDistanceKm);
            if (minSuitability != null && !minSuitability.trim().isEmpty()) {
                req.setMinSuitabilityClass(SuitabilityClass.fromString(minSuitability));
            }
            req.setVulnerablePopulation(population);
            requests.add(req);
        }

        BatchRelocationDecisionExplanationDto batchResult = relocationExplainabilityService.explainBatchRequests(requests);
        String message = batchResult.getSummary() != null ? batchResult.getSummary() : "Decision explanations generated successfully";
        return ResponseEntity.ok(ApiResponse.ok(batchResult, message));
    }
}
