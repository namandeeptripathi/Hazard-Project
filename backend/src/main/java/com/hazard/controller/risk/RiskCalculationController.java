package com.hazard.controller.risk;

import com.hazard.domain.risk.RiskComponentType;
import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.RiskConfigDto;
import com.hazard.dto.risk.RiskContributorsSummaryDto;
import com.hazard.dto.risk.config.RiskScenarioAnalysisRequestDto;
import com.hazard.dto.risk.config.RiskScenarioAnalysisResultDto;
import com.hazard.service.risk.RiskCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Stage 4.7 & 4.8 — Risk Calculation & Scenario Simulations.
 *
 * Exposes endpoints for final disaster risk synthesis, 4-pillar component decomposition,
 * explainable risk contributors, GeoJSON choropleth layers, and What-If scenario simulations.
 */
@RestController
@RequestMapping("/api/v1/risk")
@CrossOrigin(origins = "*")
@Tag(name = "Risk Calculation & Simulation (Stages 4.7 & 4.8)",
     description = "Calculates final disaster risk and executes what-if scenario simulations")
public class RiskCalculationController {

    private final RiskCalculationService riskCalculationService;

    public RiskCalculationController(RiskCalculationService riskCalculationService) {
        this.riskCalculationService = riskCalculationService;
    }

    /**
     * GET /api/v1/risk/district/{districtName}
     * Returns the master disaster risk score and 4-pillar component breakdown for an administrative district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(
            summary = "District Final Disaster Risk Profile",
            description = "Returns final composite disaster risk score [0, 1] and [0, 100], 5-tier classification, 4-pillar component details, and explainability summary."
    )
    public ResponseEntity<ApiResponse<DistrictRiskScoreDto>> getDistrictRiskScore(
            @Parameter(description = "Name of administrative district e.g. Sitamarhi, Patna, Supaul", example = "Sitamarhi")
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "hazardWeight", required = false) Double hazardWeight,
            @RequestParam(name = "exposureWeight", required = false) Double exposureWeight,
            @RequestParam(name = "vulnerabilityWeight", required = false) Double vulnerabilityWeight,
            @RequestParam(name = "historicalWeight", required = false) Double historicalWeight) {

        Map<RiskComponentType, Double> customWeights = null;
        if (hazardWeight != null || exposureWeight != null || vulnerabilityWeight != null || historicalWeight != null) {
            customWeights = new HashMap<>();
            if (hazardWeight != null) customWeights.put(RiskComponentType.HAZARD, hazardWeight);
            if (exposureWeight != null) customWeights.put(RiskComponentType.EXPOSURE, exposureWeight);
            if (vulnerabilityWeight != null) customWeights.put(RiskComponentType.VULNERABILITY, vulnerabilityWeight);
            if (historicalWeight != null) customWeights.put(RiskComponentType.HISTORICAL, historicalWeight);
        }

        DistrictRiskScoreDto scoreDto = riskCalculationService.getDistrictRiskScore(districtName, customWeights);
        ApiResponse<DistrictRiskScoreDto> response = ApiResponse.ok(scoreDto,
                "Final disaster risk score evaluated successfully for: " + districtName);
        response.addMeta("stage", "4.7");
        response.addMeta("substage", "Risk Calculation");
        response.addMeta("riskScore", scoreDto.getRiskScore());
        response.addMeta("riskScore100", scoreDto.getRiskScore100());
        response.addMeta("riskTier", scoreDto.getRiskTier().name());
        response.addMeta("configurationId", scoreDto.getConfigurationId());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/district/{districtName}/contributors
     * Returns top ranked risk contributors, exposure sub-breakdown, and vulnerability drivers for explainability.
     */
    @GetMapping("/district/{districtName}/contributors")
    @Operation(
            summary = "District Risk Contributors & Explainability",
            description = "Returns ranked top risk drivers, exposure sub-contributions, and dominant vulnerability factors."
    )
    public ResponseEntity<ApiResponse<RiskContributorsSummaryDto>> getDistrictRiskContributors(
            @PathVariable("districtName") String districtName) {

        RiskContributorsSummaryDto summary = riskCalculationService.getDistrictRiskContributors(districtName);
        ApiResponse<RiskContributorsSummaryDto> response = ApiResponse.ok(summary,
                "Risk contributors and explainability retrieved successfully for: " + districtName);
        response.addMeta("stage", "4.7");
        response.addMeta("dominantPillar", summary.getDominantPillar());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/all
     * Returns final disaster risk profiles across all 38 administrative districts of Bihar.
     */
    @GetMapping("/all")
    @Operation(
            summary = "All Districts Disaster Risk Scores",
            description = "Returns final disaster risk scores and tiers across all 38 districts of Bihar."
    )
    public ResponseEntity<ApiResponse<List<DistrictRiskScoreDto>>> getAllDistrictsRiskScores() {
        List<DistrictRiskScoreDto> list = riskCalculationService.getAllDistrictsRiskScores();
        ApiResponse<List<DistrictRiskScoreDto>> response = ApiResponse.ok(list,
                "All districts disaster risk scores retrieved successfully");
        response.addMeta("stage", "4.7");
        response.addMeta("totalDistricts", list.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/geojson
     * Returns RFC 7946 GeoJSON FeatureCollection of district risk choropleth polygons.
     */
    @GetMapping("/geojson")
    @Operation(
            summary = "District Risk Choropleth GeoJSON Layer",
            description = "Returns RFC 7946 Polygon features with final risk scores, risk tiers, and 4-pillar component scores."
    )
    public ResponseEntity<GeoJsonFeatureCollectionDto> getRiskGeoJson() {
        GeoJsonFeatureCollectionDto geojson = riskCalculationService.generateRiskGeoJson();
        return ResponseEntity.ok(geojson);
    }

    /**
     * POST /api/v1/risk/analyze
     * Custom risk calculation query with dynamic weights or What-If scenario simulations.
     */
    @PostMapping("/analyze")
    @Operation(
            summary = "Custom Risk Query & Scenario Simulation",
            description = "Evaluates district disaster risk using dynamically provided weights or executes What-If scenarios without altering production settings."
    )
    public ResponseEntity<ApiResponse<Object>> analyzeRisk(
            @RequestBody Map<String, Object> request) {

        String districtName = request.containsKey("districtName") ? request.get("districtName").toString() : "Sitamarhi";

        if (request.containsKey("overrideWeights") || request.containsKey("baseConfigurationId")) {
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
            RiskScenarioAnalysisResultDto scenarioResult = riskCalculationService.runScenarioAnalysis(scenarioReq);
            ApiResponse<Object> response = ApiResponse.ok(scenarioResult, "Scenario simulation executed successfully for: " + districtName);
            response.addMeta("stage", "4.8");
            response.addMeta("mode", "SCENARIO_ANALYSIS");
            return ResponseEntity.ok(response);
        }

        DistrictRiskScoreDto result = riskCalculationService.getDistrictRiskScore(districtName, null);
        ApiResponse<Object> response = ApiResponse.ok(result,
                "Custom disaster risk calculation completed successfully for: " + districtName);
        response.addMeta("stage", "4.7");
        return ResponseEntity.ok(response);
    }
}
