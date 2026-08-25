package com.hazard.controller.risk;

import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.risk.config.RiskScenarioAnalysisRequestDto;
import com.hazard.dto.risk.config.RiskScenarioAnalysisResultDto;
import com.hazard.dto.risk.explain.*;
import com.hazard.service.risk.explain.RiskExplanationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Stage 4.10 — Explainable Risk.
 * Exposes multi-level human summaries, empirical evidence items with provenance,
 * step-by-step mathematical calculation traces, model sensitivity analyses, and limitations.
 */
@RestController
@RequestMapping("/api/v1/risk/explanation")
@CrossOrigin(origins = "*")
@Tag(name = "Explainable Risk & Decision Support (Stage 4.10)",
     description = "Transparent human-understandable risk explanations, evidence catalogs, calculation traces, and sensitivity insights")
public class RiskExplanationController {

    private final RiskExplanationService riskExplanationService;

    public RiskExplanationController(RiskExplanationService riskExplanationService) {
        this.riskExplanationService = riskExplanationService;
    }

    /**
     * GET /api/v1/risk/explanation/district/{districtName}
     * Returns complete explainability profile for a district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(
            summary = "Complete District Explainability Profile",
            description = "Returns executive summary, empirical evidence catalog with provenance, calculation trace, sensitivity analysis, data quality, and model limitations."
    )
    public ResponseEntity<ApiResponse<DistrictRiskExplainabilityProfileDto>> getDistrictExplainability(
            @Parameter(description = "District name e.g. Sitamarhi, Patna, Supaul", example = "Sitamarhi")
            @PathVariable("districtName") String districtName) {

        DistrictRiskExplainabilityProfileDto profile = riskExplanationService.getDistrictExplainabilityProfile(districtName);
        ApiResponse<DistrictRiskExplainabilityProfileDto> response = ApiResponse.ok(profile,
                "Explainability profile generated successfully for: " + districtName);
        response.addMeta("stage", "4.10");
        response.addMeta("substage", "Explainable Risk");
        response.addMeta("explanationVersion", profile.getExplanationVersion());
        response.addMeta("dominantDriver", profile.getSummary().getDominantDriver());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/explanation/district/{districtName}/summary
     * Returns executive and short summaries.
     */
    @GetMapping("/district/{districtName}/summary")
    @Operation(
            summary = "Executive & Short Risk Summaries",
            description = "Returns decision-maker-friendly 1-sentence executive summary and 2–4 sentence narrative."
    )
    public ResponseEntity<ApiResponse<ExplanationSummaryDto>> getExplanationSummary(
            @PathVariable("districtName") String districtName) {

        ExplanationSummaryDto summary = riskExplanationService.getExplanationSummary(districtName);
        ApiResponse<ExplanationSummaryDto> response = ApiResponse.ok(summary,
                "Risk summaries generated successfully for: " + districtName);
        response.addMeta("stage", "4.10");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/explanation/district/{districtName}/evidence
     * Returns structured empirical evidence items.
     */
    @GetMapping("/district/{districtName}/evidence")
    @Operation(
            summary = "Structured Evidence Catalog",
            description = "Returns empirical hazard, exposure, vulnerability, and historical evidence items with explicit data provenance."
    )
    public ResponseEntity<ApiResponse<List<ExplainableEvidenceItemDto>>> getEvidenceItems(
            @PathVariable("districtName") String districtName) {

        List<ExplainableEvidenceItemDto> items = riskExplanationService.getEvidenceItems(districtName);
        ApiResponse<List<ExplainableEvidenceItemDto>> response = ApiResponse.ok(items,
                "Evidence items retrieved successfully for: " + districtName);
        response.addMeta("stage", "4.10");
        response.addMeta("evidenceCount", items.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/explanation/district/{districtName}/calculation
     * Returns formula and step-by-step calculation trace.
     */
    @GetMapping("/district/{districtName}/calculation")
    @Operation(
            summary = "Calculation Trace & Formula",
            description = "Returns exact mathematical formula and step-by-step component trace reconciling with final risk score."
    )
    public ResponseEntity<ApiResponse<CalculationTraceDto>> getCalculationTrace(
            @PathVariable("districtName") String districtName) {

        CalculationTraceDto trace = riskExplanationService.getCalculationTrace(districtName);
        ApiResponse<CalculationTraceDto> response = ApiResponse.ok(trace,
                "Calculation trace retrieved successfully for: " + districtName);
        response.addMeta("stage", "4.10");
        response.addMeta("isReconciled", trace.isReconciled());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/explanation/district/{districtName}/sensitivity
     * Returns model sensitivity leverage ranking.
     */
    @GetMapping("/district/{districtName}/sensitivity")
    @Operation(
            summary = "Model Sensitivity Analysis",
            description = "Returns one-at-a-time component perturbation analysis ranking factors by leverage over final risk score."
    )
    public ResponseEntity<ApiResponse<List<ComponentSensitivityDto>>> getSensitivityAnalysis(
            @PathVariable("districtName") String districtName) {

        List<ComponentSensitivityDto> sensitivity = riskExplanationService.getSensitivityAnalysis(districtName);
        ApiResponse<List<ComponentSensitivityDto>> response = ApiResponse.ok(sensitivity,
                "Sensitivity analysis generated successfully for: " + districtName);
        response.addMeta("stage", "4.10");
        response.addMeta("topLeverageComponent", !sensitivity.isEmpty() ? sensitivity.get(0).getComponentName() : "N/A");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/risk/explanation/analyze
     * Supports scenario explainability simulations with temporary weight overrides.
     */
    @PostMapping("/analyze")
    @Operation(
            summary = "Scenario Explainability Simulation",
            description = "Evaluates scenario risk scores and explains parameter-induced shifts without modifying production configuration."
    )
    public ResponseEntity<ApiResponse<RiskScenarioAnalysisResultDto>> analyzeScenarioExplainability(
            @RequestBody Map<String, Object> request) {

        String districtName = request.containsKey("districtName") ? request.get("districtName").toString() : "Sitamarhi";

        RiskScenarioAnalysisRequestDto scenarioReq = new RiskScenarioAnalysisRequestDto();
        scenarioReq.setDistrictName(districtName);
        if (request.containsKey("baseConfigurationId")) {
            scenarioReq.setBaseConfigurationId(request.get("baseConfigurationId").toString());
        }
        if (request.containsKey("scenarioName")) {
            scenarioReq.setScenarioName(request.get("scenarioName").toString());
        }
        if (request.get("overrideWeights") instanceof Map<?, ?> rawMap) {
            Map<String, Double> map = new HashMap<>();
            rawMap.forEach((k, v) -> {
                if (v instanceof Number n) map.put(k.toString(), n.doubleValue());
            });
            scenarioReq.setOverrideWeights(map);
        }

        RiskScenarioAnalysisResultDto result = riskExplanationService.analyzeScenarioExplainability(scenarioReq);
        ApiResponse<RiskScenarioAnalysisResultDto> response = ApiResponse.ok(result,
                "Scenario explainability evaluated successfully for: " + districtName);
        response.addMeta("stage", "4.10");
        response.addMeta("mode", "SCENARIO_ANALYSIS");
        return ResponseEntity.ok(response);
    }
}
