package com.hazard.controller.relocation;

import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.relocation.RelocationPlanDto;
import com.hazard.dto.relocation.RelocationRequestDto;
import com.hazard.service.relocation.RelocationPlanningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Stage 6 — Relocation Intelligence.
 *
 * Exposes endpoints for end-to-end automated emergency evacuation shelter planning:
 * - Feasibility Filtering (Hazard Safety, Suitability, Distance, Capacity gates)
 * - Deterministic Feasible Safe Site Ranking
 * - Capacity-Aware Population Allocation & Deficit Accounting
 * - Explanatory Relocation Assignment Plans
 */
@RestController
@RequestMapping("/api/v1/relocation")
@CrossOrigin(origins = "*")
@Tag(
        name = "Relocation Intelligence (Stage 6)",
        description = "Automated emergency shelter relocation planning, multi-gate feasibility filtering, deterministic ranking, and capacity-aware population allocation"
)
public class RelocationController {

    private final RelocationPlanningService relocationPlanningService;

    public RelocationController(RelocationPlanningService relocationPlanningService) {
        this.relocationPlanningService = relocationPlanningService;
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
}
