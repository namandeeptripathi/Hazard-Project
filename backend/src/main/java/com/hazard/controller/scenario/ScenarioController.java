package com.hazard.controller.scenario;

import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.scenario.*;
import com.hazard.service.scenario.ScenarioComparisonService;
import com.hazard.service.scenario.ScenarioDecisionService;
import com.hazard.service.scenario.ScenarioExecutionService;
import com.hazard.service.scenario.ScenarioRedZoneService;
import com.hazard.service.scenario.ScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for:
 * - Stage 9A — Scenario Creation & Definition Layer
 * - Stage 9B — Scenario Execution Layer
 * - Stage 9C — Dynamic Red-Zone Recalculation Layer
 * - Stage 9D — Priority & Relocation Recalculation Layer
 * - Stage 9E — Before/After Scenario Comparison Layer
 *
 * Exposes endpoints to define, validate, list, retrieve, execute, and compare what-if disaster simulation scenarios:
 * - Baseline scenario (0% rainfall change, 0% hazard intensity change, 0% population exposure shift)
 * - Rainfall change scenarios (+/- %)
 * - Hazard intensity scaling scenarios (+/- %)
 * - Population exposure expansion scenarios (+/- %)
 * - Multi-factor compound scenarios
 * - Scenario risk execution without mutating production/stored data
 * - Dynamic Red-Zone recalculation & transition shifts (NO -> YES, YES -> NO, UNCHANGED)
 * - Dynamic Priority escalation & Relocation deficit recalculation (Stage 9D)
 * - Before vs After scenario comparison across Risk, Red-Zone, Priority, and Relocation (Stage 9E)
 */
@RestController
@RequestMapping({"/api/v1/scenarios", "/api/v1/scenario"})
@CrossOrigin(origins = "*")
@Tag(
        name = "Scenario Simulation & Comparison (Stage 9A, 9B, 9C, 9D & 9E)",
        description = "Defines, validates, executes disaster parameter scenarios, and recalculates Red-Zone shifts, evacuation priorities, relocation plans, and Before vs After comparative metrics without mutating production state."
)
public class ScenarioController {

    private final ScenarioService scenarioService;
    private final ScenarioExecutionService scenarioExecutionService;
    private final ScenarioRedZoneService scenarioRedZoneService;
    private final ScenarioDecisionService scenarioDecisionService;
    private final ScenarioComparisonService scenarioComparisonService;

    public ScenarioController(ScenarioService scenarioService,
                              ScenarioExecutionService scenarioExecutionService,
                              ScenarioRedZoneService scenarioRedZoneService,
                              ScenarioDecisionService scenarioDecisionService,
                              ScenarioComparisonService scenarioComparisonService) {
        this.scenarioService = scenarioService;
        this.scenarioExecutionService = scenarioExecutionService;
        this.scenarioRedZoneService = scenarioRedZoneService;
        this.scenarioDecisionService = scenarioDecisionService;
        this.scenarioComparisonService = scenarioComparisonService;
    }

    // =========================================================================
    // STAGE 9A — SCENARIO DEFINITION APIS
    // =========================================================================

    /**
     * GET /api/v1/scenarios/baseline
     * Returns the official immutable baseline scenario.
     */
    @GetMapping("/baseline")
    @Operation(
            summary = "Get Baseline Scenario Definition",
            description = "Returns the official unperturbed baseline scenario with 0% changes across all environmental and exposure parameters."
    )
    public ResponseEntity<ApiResponse<ScenarioDto>> getBaselineScenario() {
        ScenarioDto baseline = scenarioService.getBaselineScenario();
        ApiResponse<ScenarioDto> response = ApiResponse.ok(baseline, "Baseline scenario definition retrieved successfully");
        response.addMeta("stage", "9A");
        response.addMeta("substage", "Baseline Scenario");
        response.addMeta("scenarioId", baseline.getScenarioId());
        response.addMeta("isBaseline", true);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/scenarios/types
     * Returns supported scenario simulation types and metadata.
     */
    @GetMapping("/types")
    @Operation(
            summary = "Get Supported Scenario Types",
            description = "Returns metadata and descriptions for all supported scenario categories (BASELINE, RAINFALL_CHANGE, HAZARD_INTENSITY, POPULATION_EXPOSURE, MULTI_FACTOR)."
    )
    public ResponseEntity<ApiResponse<List<ScenarioTypeInfoDto>>> getScenarioTypes() {
        List<ScenarioTypeInfoDto> types = scenarioService.getScenarioTypes();
        ApiResponse<List<ScenarioTypeInfoDto>> response = ApiResponse.ok(types, "Supported scenario types retrieved successfully");
        response.addMeta("stage", "9A");
        response.addMeta("count", types.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/scenarios
     * Returns all registered scenario definitions, optionally filtered by type.
     */
    @GetMapping
    @Operation(
            summary = "Get All Scenario Definitions",
            description = "Lists all registered scenario definitions, optionally filtered by scenario type."
    )
    public ResponseEntity<ApiResponse<List<ScenarioDto>>> getAllScenarios(
            @Parameter(description = "Optional filter by scenario type (e.g. RAINFALL_CHANGE, HAZARD_INTENSITY, POPULATION_EXPOSURE)", example = "RAINFALL_CHANGE")
            @RequestParam(name = "type", required = false) String type) {

        List<ScenarioDto> scenarios = scenarioService.getAllScenarios(type);
        ApiResponse<List<ScenarioDto>> response = ApiResponse.ok(scenarios, "Scenarios retrieved successfully");
        response.addMeta("stage", "9A");
        response.addMeta("count", scenarios.size());
        if (type != null) {
            response.addMeta("filterType", type);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/scenarios/{scenarioId}
     * Retrieves a specific scenario definition by ID.
     */
    @GetMapping("/{scenarioId}")
    @Operation(
            summary = "Get Scenario Definition by ID",
            description = "Retrieves complete parameter specifications for a single scenario definition."
    )
    public ResponseEntity<ApiResponse<ScenarioDto>> getScenarioById(
            @Parameter(description = "Unique scenario identifier (e.g. SCEN-BASELINE, SCEN-RAIN-101)", example = "SCEN-BASELINE")
            @PathVariable("scenarioId") String scenarioId) {

        ScenarioDto scenario = scenarioService.getScenarioById(scenarioId);
        ApiResponse<ScenarioDto> response = ApiResponse.ok(scenario, "Scenario definition retrieved successfully: " + scenarioId);
        response.addMeta("stage", "9A");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("scenarioType", scenario.getScenarioType().name());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/scenarios
     * Creates and validates a new scenario definition.
     */
    @PostMapping
    @Operation(
            summary = "Create Scenario Definition",
            description = "Validates and creates a new disaster simulation scenario definition without triggering risk or relocation recalculations."
    )
    public ResponseEntity<ApiResponse<ScenarioDto>> createScenario(
            @RequestBody ScenarioCreateRequestDto request) {

        ScenarioDto scenario = scenarioService.createScenario(request);
        ApiResponse<ScenarioDto> response = ApiResponse.ok(scenario, "Scenario definition created successfully: " + scenario.getScenarioName());
        response.addMeta("stage", "9A");
        response.addMeta("substage", "Scenario Creation");
        response.addMeta("scenarioId", scenario.getScenarioId());
        response.addMeta("scenarioType", scenario.getScenarioType().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /api/v1/scenarios/{scenarioId}
     * Deletes a user-created scenario definition by ID.
     */
    @DeleteMapping("/{scenarioId}")
    @Operation(
            summary = "Delete Scenario Definition",
            description = "Deletes a custom scenario definition by ID. The baseline reference scenario cannot be deleted."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteScenario(
            @Parameter(description = "Scenario identifier to delete", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId) {

        scenarioService.deleteScenario(scenarioId);
        ApiResponse<Map<String, Object>> response = ApiResponse.ok(
                Map.of("scenarioId", scenarioId, "deleted", true),
                "Scenario definition deleted successfully: " + scenarioId
        );
        response.addMeta("stage", "9A");
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // STAGE 9B — SCENARIO EXECUTION APIS
    // =========================================================================

    /**
     * POST /api/v1/scenarios/{scenarioId}/execute
     * Executes a scenario simulation against the existing risk engine for a district.
     */
    @PostMapping("/{scenarioId}/execute")
    @Operation(
            summary = "Execute Disaster Scenario Simulation (Stage 9B)",
            description = "Executes the specified scenario against the existing risk engine in-memory and returns simulated risk scores and baseline comparisons without altering stored production data."
    )
    public ResponseEntity<ApiResponse<ScenarioSimulationResultDto>> executeScenario(
            @Parameter(description = "Unique scenario identifier to execute", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId,
            @Parameter(description = "Optional query parameter to specify target district", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String districtParam,
            @RequestBody(required = false) ScenarioExecutionRequestDto request) {

        if (request == null) {
            request = new ScenarioExecutionRequestDto();
        }
        if (districtParam != null && !districtParam.trim().isEmpty()) {
            request.setDistrictName(districtParam.trim());
        }

        ScenarioSimulationResultDto result = scenarioExecutionService.executeScenario(scenarioId, request);
        ApiResponse<ScenarioSimulationResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9B");
        response.addMeta("substage", "Scenario Execution");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("districtName", result.getDistrictName());
        response.addMeta("simulatedRiskScore", result.getSimulatedRisk().getRiskScore());
        response.addMeta("simulatedRiskScore100", result.getSimulatedRisk().getRiskScore100());
        response.addMeta("deltaRiskScore100", result.getDeltaRiskScore100());
        response.addMeta("riskDirection", result.getRiskDirection());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/scenarios/{scenarioId}/execute
     * Convenience query endpoint to execute a scenario simulation via GET.
     */
    @GetMapping("/{scenarioId}/execute")
    @Operation(
            summary = "Query Scenario Simulation Execution (Stage 9B)",
            description = "Executes a scenario simulation for a district via query parameters."
    )
    public ResponseEntity<ApiResponse<ScenarioSimulationResultDto>> getScenarioExecution(
            @Parameter(description = "Unique scenario identifier to execute", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId,
            @Parameter(description = "Target administrative district name (e.g. Sitamarhi, Patna)", example = "Sitamarhi")
            @RequestParam(name = "district", required = false, defaultValue = "Sitamarhi") String district) {

        ScenarioExecutionRequestDto req = new ScenarioExecutionRequestDto(district);
        ScenarioSimulationResultDto result = scenarioExecutionService.executeScenario(scenarioId, req);
        ApiResponse<ScenarioSimulationResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9B");
        response.addMeta("substage", "Scenario Execution Query");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("districtName", result.getDistrictName());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/scenarios/{scenarioId}/execute/all
     * Executes a scenario simulation across all 38 districts of Bihar.
     */
    @PostMapping("/{scenarioId}/execute/all")
    @Operation(
            summary = "Execute Scenario Across All Districts (Stage 9B)",
            description = "Executes the scenario across all 38 administrative districts in Bihar and returns simulated risk results for each."
    )
    public ResponseEntity<ApiResponse<List<ScenarioSimulationResultDto>>> executeScenarioAllDistricts(
            @Parameter(description = "Unique scenario identifier to execute", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId) {

        List<ScenarioSimulationResultDto> results = scenarioExecutionService.executeScenarioAllDistricts(scenarioId);
        ApiResponse<List<ScenarioSimulationResultDto>> response = ApiResponse.ok(
                results,
                String.format("Scenario '%s' executed across %d districts successfully", scenarioId, results.size())
        );
        response.addMeta("stage", "9B");
        response.addMeta("substage", "Batch Scenario Execution");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("totalDistrictsEvaluated", results.size());
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // STAGE 9C — DYNAMIC RED-ZONE RECALCULATION APIS
    // =========================================================================

    /**
     * POST /api/v1/scenarios/{scenarioId}/red-zone/execute
     * Recalculates Red-Zone classifications and transitions under scenario conditions.
     */
    @PostMapping("/{scenarioId}/red-zone/execute")
    @Operation(
            summary = "Execute Dynamic Red-Zone Recalculation (Stage 9C)",
            description = "Evaluates simulated Red-Zone classifications and shift transitions (NO->YES, YES->NO, UNCHANGED) using the canonical Red-Zone classification rules without mutating production data."
    )
    public ResponseEntity<ApiResponse<ScenarioRedZoneSimulationResultDto>> executeRedZoneRecalculation(
            @Parameter(description = "Unique scenario identifier to execute", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId,
            @Parameter(description = "Optional query parameter to specify single district", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String districtParam,
            @RequestBody(required = false) ScenarioExecutionRequestDto request) {

        if (request == null) {
            request = new ScenarioExecutionRequestDto();
        }
        if (districtParam != null && !districtParam.trim().isEmpty()) {
            request.setDistrictName(districtParam.trim());
        }

        ScenarioRedZoneSimulationResultDto result = scenarioRedZoneService.recalculateRedZones(scenarioId, request);
        ApiResponse<ScenarioRedZoneSimulationResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9C");
        response.addMeta("substage", "Red-Zone Recalculation");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("totalDistrictsEvaluated", result.getTotalDistrictsEvaluated());
        response.addMeta("baselineRedZones", result.getBaselineRedZoneCount());
        response.addMeta("simulatedRedZones", result.getSimulatedRedZoneCount());
        response.addMeta("netRedZoneChange", result.getNetRedZoneChange());
        response.addMeta("newlyEnteredRedZones", result.getNewlyEnteredRedZoneCount());
        response.addMeta("leftRedZones", result.getLeftRedZoneCount());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/scenarios/{scenarioId}/red-zone/execute
     * Convenience GET endpoint to query Red-Zone recalculation for a district or all districts.
     */
    @GetMapping("/{scenarioId}/red-zone/execute")
    @Operation(
            summary = "Query Dynamic Red-Zone Recalculation (Stage 9C)",
            description = "Queries Red-Zone recalculation for a district or all districts."
    )
    public ResponseEntity<ApiResponse<ScenarioRedZoneSimulationResultDto>> getRedZoneRecalculation(
            @Parameter(description = "Unique scenario identifier to execute", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId,
            @Parameter(description = "Optional target administrative district name", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String district) {

        ScenarioExecutionRequestDto req = new ScenarioExecutionRequestDto(district);
        ScenarioRedZoneSimulationResultDto result = scenarioRedZoneService.recalculateRedZones(scenarioId, req);
        ApiResponse<ScenarioRedZoneSimulationResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9C");
        response.addMeta("substage", "Red-Zone Recalculation Query");
        response.addMeta("scenarioId", scenarioId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/scenarios/{scenarioId}/red-zone/execute/all
     * Batch executes dynamic Red-Zone recalculation across all 38 districts in Bihar.
     */
    @PostMapping("/{scenarioId}/red-zone/execute/all")
    @Operation(
            summary = "Execute Red-Zone Recalculation Across All Districts (Stage 9C)",
            description = "Recalculates Red-Zone classifications across all 38 districts in Bihar under simulated disaster scenario conditions."
    )
    public ResponseEntity<ApiResponse<ScenarioRedZoneSimulationResultDto>> executeRedZoneRecalculationAll(
            @Parameter(description = "Unique scenario identifier to execute", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId) {

        ScenarioRedZoneSimulationResultDto result = scenarioRedZoneService.recalculateRedZonesAllDistricts(scenarioId);
        ApiResponse<ScenarioRedZoneSimulationResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9C");
        response.addMeta("substage", "Batch Red-Zone Recalculation");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("totalDistrictsEvaluated", result.getTotalDistrictsEvaluated());
        response.addMeta("netRedZoneChange", result.getNetRedZoneChange());
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // STAGE 9D — PRIORITY & RELOCATION RECALCULATION APIS
    // =========================================================================

    /**
     * POST /api/v1/scenarios/{scenarioId}/decision/execute
     * Recalculates evacuation Priority and Relocation planning under simulated scenario conditions.
     */
    @PostMapping("/{scenarioId}/decision/execute")
    @Operation(
            summary = "Execute Priority & Relocation Recalculation (Stage 9D)",
            description = "Executes the scenario through Risk (9B) -> Red-Zone (9C) -> Stage 7 Priority Engine -> Stage 6 Relocation Planning Engine, returning simulated priorities, capacity allocations, and deficits without mutating production data."
    )
    public ResponseEntity<ApiResponse<ScenarioDecisionSimulationResultDto>> executeDecisionRecalculation(
            @Parameter(description = "Unique scenario identifier to execute", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId,
            @Parameter(description = "Optional query parameter to specify single district", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String districtParam,
            @RequestBody(required = false) ScenarioExecutionRequestDto request) {

        if (request == null) {
            request = new ScenarioExecutionRequestDto();
        }
        if (districtParam != null && !districtParam.trim().isEmpty()) {
            request.setDistrictName(districtParam.trim());
        }

        ScenarioDecisionSimulationResultDto result = scenarioDecisionService.recalculateDecision(scenarioId, request);
        ApiResponse<ScenarioDecisionSimulationResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9D");
        response.addMeta("substage", "Priority & Relocation Recalculation");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("totalDistrictsEvaluated", result.getTotalDistrictsEvaluated());
        response.addMeta("immediatePriorityDistricts", result.getImmediatePriorityCount());
        response.addMeta("priorityShiftUpCount", result.getPriorityShiftUpCount());
        response.addMeta("totalUnallocatedDeficit", result.getTotalUnallocatedPopulation());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/scenarios/{scenarioId}/decision/execute
     * Convenience GET endpoint to query decision recalculation for a district or all districts.
     */
    @GetMapping("/{scenarioId}/decision/execute")
    @Operation(
            summary = "Query Priority & Relocation Recalculation (Stage 9D)",
            description = "Queries simulated Priority escalation and Relocation planning outcomes for a district or all districts."
    )
    public ResponseEntity<ApiResponse<ScenarioDecisionSimulationResultDto>> getDecisionRecalculation(
            @Parameter(description = "Unique scenario identifier to execute", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId,
            @Parameter(description = "Optional target administrative district name", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String district) {

        ScenarioExecutionRequestDto req = new ScenarioExecutionRequestDto(district);
        ScenarioDecisionSimulationResultDto result = scenarioDecisionService.recalculateDecision(scenarioId, req);
        ApiResponse<ScenarioDecisionSimulationResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9D");
        response.addMeta("substage", "Decision Recalculation Query");
        response.addMeta("scenarioId", scenarioId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/scenarios/{scenarioId}/decision/execute/all
     * Batch executes Priority and Relocation recalculation across all 38 districts in Bihar.
     */
    @PostMapping("/{scenarioId}/decision/execute/all")
    @Operation(
            summary = "Execute Decision Recalculation Across All Districts (Stage 9D)",
            description = "Recalculates Priority and Relocation outcomes across all 38 administrative districts under simulated scenario conditions."
    )
    public ResponseEntity<ApiResponse<ScenarioDecisionSimulationResultDto>> executeDecisionRecalculationAll(
            @Parameter(description = "Unique scenario identifier to execute", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId) {

        ScenarioDecisionSimulationResultDto result = scenarioDecisionService.recalculateDecisionAllDistricts(scenarioId);
        ApiResponse<ScenarioDecisionSimulationResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9D");
        response.addMeta("substage", "Batch Decision Recalculation");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("totalDistrictsEvaluated", result.getTotalDistrictsEvaluated());
        response.addMeta("immediatePriorityDistricts", result.getImmediatePriorityCount());
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // STAGE 9E — BEFORE / AFTER SCENARIO COMPARISON APIS
    // =========================================================================

    /**
     * POST /api/v1/scenarios/{scenarioId}/compare
     * Generates a multi-dimensional Before vs After comparison across Risk, Red-Zone, Priority, and Relocation.
     */
    @PostMapping("/{scenarioId}/compare")
    @Operation(
            summary = "Compare Scenario Before vs After (Stage 9E)",
            description = "Produces a multi-dimensional Before vs After comparison of Risk shifts, Red-Zone transitions, Priority escalations, and Relocation capacity deficits without mutating baseline or production state."
    )
    public ResponseEntity<ApiResponse<ScenarioComparisonResultDto>> compareScenario(
            @Parameter(description = "Unique scenario identifier to compare", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId,
            @Parameter(description = "Optional query parameter to specify single district", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String districtParam,
            @RequestBody(required = false) ScenarioExecutionRequestDto request) {

        if (request == null) {
            request = new ScenarioExecutionRequestDto();
        }
        if (districtParam != null && !districtParam.trim().isEmpty()) {
            request.setDistrictName(districtParam.trim());
        }

        ScenarioComparisonResultDto result = scenarioComparisonService.compareScenario(scenarioId, request);
        ApiResponse<ScenarioComparisonResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9E");
        response.addMeta("substage", "Before/After Comparison");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("totalDistrictsEvaluated", result.getTotalDistrictsEvaluated());
        response.addMeta("districtsWithIncreasedRisk", result.getDistrictsWithIncreasedRiskCount());
        response.addMeta("netRedZoneChange", result.getNetRedZoneChange());
        response.addMeta("priorityEscalatedCount", result.getPriorityEscalatedCount());
        response.addMeta("netUnallocatedDeficitChange", result.getNetUnallocatedDeficitChange());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/scenarios/{scenarioId}/compare
     * Convenience query endpoint to retrieve Before vs After comparison for a district or all districts.
     */
    @GetMapping("/{scenarioId}/compare")
    @Operation(
            summary = "Query Scenario Before vs After Comparison (Stage 9E)",
            description = "Queries Before vs After comparison metrics across Risk, Red-Zone, Priority, and Relocation via GET."
    )
    public ResponseEntity<ApiResponse<ScenarioComparisonResultDto>> getScenarioComparison(
            @Parameter(description = "Unique scenario identifier to compare", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId,
            @Parameter(description = "Optional target administrative district name", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String district) {

        ScenarioExecutionRequestDto req = new ScenarioExecutionRequestDto(district);
        ScenarioComparisonResultDto result = scenarioComparisonService.compareScenario(scenarioId, req);
        ApiResponse<ScenarioComparisonResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9E");
        response.addMeta("substage", "Before/After Comparison Query");
        response.addMeta("scenarioId", scenarioId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/scenarios/{scenarioId}/compare/all
     * Batch executes Before vs After comparison across all 38 districts in Bihar.
     */
    @PostMapping("/{scenarioId}/compare/all")
    @Operation(
            summary = "Compare Scenario Across All Districts (Stage 9E)",
            description = "Batch generates Before vs After comparative metrics across all 38 administrative districts in Bihar."
    )
    public ResponseEntity<ApiResponse<ScenarioComparisonResultDto>> compareScenarioAll(
            @Parameter(description = "Unique scenario identifier to compare", example = "SCEN-RAIN-101")
            @PathVariable("scenarioId") String scenarioId) {

        ScenarioComparisonResultDto result = scenarioComparisonService.compareAllDistrictsScenario(scenarioId);
        ApiResponse<ScenarioComparisonResultDto> response = ApiResponse.ok(result, result.getSummary());
        response.addMeta("stage", "9E");
        response.addMeta("substage", "Batch Before/After Comparison");
        response.addMeta("scenarioId", scenarioId);
        response.addMeta("totalDistrictsEvaluated", result.getTotalDistrictsEvaluated());
        response.addMeta("netRedZoneChange", result.getNetRedZoneChange());
        response.addMeta("priorityEscalatedCount", result.getPriorityEscalatedCount());
        return ResponseEntity.ok(response);
    }
}
