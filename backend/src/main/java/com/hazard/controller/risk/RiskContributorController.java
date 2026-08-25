package com.hazard.controller.risk;

import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.risk.config.RiskScenarioAnalysisRequestDto;
import com.hazard.dto.risk.config.RiskScenarioAnalysisResultDto;
import com.hazard.dto.risk.contributor.ContributorTreeNodeDto;
import com.hazard.dto.risk.contributor.DetailedRiskContributorDto;
import com.hazard.dto.risk.contributor.DistrictRiskContributorsProfileDto;
import com.hazard.dto.risk.contributor.RiskExplanationDto;
import com.hazard.service.risk.RiskCalculationService;
import com.hazard.service.risk.contributor.RiskContributorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Stage 4.9 — Risk Contributors.
 * Exposes explainability endpoints detailing primary drivers, hierarchical trees, and natural-language summaries.
 */
@RestController
@RequestMapping("/api/v1/risk/contributors")
@CrossOrigin(origins = "*")
@Tag(name = "Risk Contributors & Explainability (Stage 4.9)",
     description = "Decomposes, ranks, and explains the primary drivers of disaster risk")
public class RiskContributorController {

    private final RiskContributorService riskContributorService;
    private final RiskCalculationService riskCalculationService;

    public RiskContributorController(RiskContributorService riskContributorService,
                                     RiskCalculationService riskCalculationService) {
        this.riskContributorService = riskContributorService;
        this.riskCalculationService = riskCalculationService;
    }

    /**
     * GET /api/v1/risk/contributors/district/{districtName}
     * Returns full disaster risk contributors profile with ranked drivers, tree, explanation, and mathematical validation.
     */
    @GetMapping("/district/{districtName}")
    @Operation(
            summary = "District Risk Contributors Profile",
            description = "Returns complete explainability profile including top ranked drivers, hierarchical tree, and dynamic narrative."
    )
    public ResponseEntity<ApiResponse<DistrictRiskContributorsProfileDto>> getDistrictContributors(
            @Parameter(description = "District name e.g. Sitamarhi, Patna, Supaul", example = "Sitamarhi")
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit) {

        DistrictRiskContributorsProfileDto profile = riskContributorService.getDistrictContributorsProfile(districtName, limit);
        ApiResponse<DistrictRiskContributorsProfileDto> response = ApiResponse.ok(profile,
                "Risk contributors profile evaluated successfully for: " + districtName);
        response.addMeta("stage", "4.9");
        response.addMeta("substage", "Risk Contributors");
        response.addMeta("riskScore", profile.getRiskScore());
        response.addMeta("dominantPillar", profile.getExplanation().getDominantPillar());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/contributors/district/{districtName}/top
     * Returns ranked list of top risk contributors.
     */
    @GetMapping("/district/{districtName}/top")
    @Operation(
            summary = "Top Ranked Risk Drivers",
            description = "Returns top N ranked contributors ordered by absolute risk contribution and relative percentage share."
    )
    public ResponseEntity<ApiResponse<List<DetailedRiskContributorDto>>> getTopContributors(
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit) {

        List<DetailedRiskContributorDto> list = riskContributorService.getTopContributors(districtName, limit);
        ApiResponse<List<DetailedRiskContributorDto>> response = ApiResponse.ok(list,
                "Top risk contributors retrieved successfully for: " + districtName);
        response.addMeta("stage", "4.9");
        response.addMeta("count", list.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/contributors/district/{districtName}/tree
     * Returns multi-level hierarchical contributor tree.
     */
    @GetMapping("/district/{districtName}/tree")
    @Operation(
            summary = "Hierarchical Contributor Tree",
            description = "Returns multi-level tree representation of risk decomposition from Root -> Pillars -> Sub-components -> Indicators."
    )
    public ResponseEntity<ApiResponse<ContributorTreeNodeDto>> getContributorTree(
            @PathVariable("districtName") String districtName) {

        ContributorTreeNodeDto tree = riskContributorService.getContributorTree(districtName);
        ApiResponse<ContributorTreeNodeDto> response = ApiResponse.ok(tree,
                "Contributor tree retrieved successfully for: " + districtName);
        response.addMeta("stage", "4.9");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/contributors/district/{districtName}/explanation
     * Returns natural-language narrative and structured primary drivers.
     */
    @GetMapping("/district/{districtName}/explanation")
    @Operation(
            summary = "Natural-Language Risk Explanation",
            description = "Returns dynamic human-readable explanation and structured highlights for district risk profile."
    )
    public ResponseEntity<ApiResponse<RiskExplanationDto>> getRiskExplanation(
            @PathVariable("districtName") String districtName) {

        RiskExplanationDto explanation = riskContributorService.getRiskExplanation(districtName);
        ApiResponse<RiskExplanationDto> response = ApiResponse.ok(explanation,
                "Risk explanation generated successfully for: " + districtName);
        response.addMeta("stage", "4.9");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/risk/contributors/analyze
     * Executes scenario contributor simulation with temporary weight overrides.
     */
    @PostMapping("/analyze")
    @Operation(
            summary = "Scenario Contributor Simulation",
            description = "Evaluates what-if scenario contributors and compares them with production baseline without modifying stored configuration."
    )
    public ResponseEntity<ApiResponse<RiskScenarioAnalysisResultDto>> analyzeScenarioContributors(
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

        RiskScenarioAnalysisResultDto result = riskCalculationService.runScenarioAnalysis(scenarioReq);
        ApiResponse<RiskScenarioAnalysisResultDto> response = ApiResponse.ok(result,
                "Scenario contributors evaluated successfully for: " + districtName);
        response.addMeta("stage", "4.9");
        response.addMeta("mode", "SCENARIO_ANALYSIS");
        return ResponseEntity.ok(response);
    }
}
