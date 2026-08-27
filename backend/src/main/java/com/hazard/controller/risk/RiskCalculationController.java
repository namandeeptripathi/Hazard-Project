package com.hazard.controller.risk;

import com.hazard.domain.risk.RiskComponentType;
import com.hazard.domain.risk.ZoneLevel;
import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.RedZoneDto;
import com.hazard.dto.risk.RiskContributorsSummaryDto;
import com.hazard.dto.risk.config.RiskScenarioAnalysisRequestDto;
import com.hazard.dto.risk.config.RiskScenarioAnalysisResultDto;
import com.hazard.service.risk.RedZoneService;
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
 * REST Controller for Stage 4.7, 4.8 & 5.1 — Risk Calculation, Scenario Simulations & Dynamic Red-Zone Generation.
 *
 * Exposes endpoints for final disaster risk synthesis, 4-pillar component decomposition,
 * explainable risk contributors, GeoJSON choropleth layers, What-If scenario simulations,
 * and dynamic red-zone classification.
 */
@RestController
@RequestMapping("/api/v1/risk")
@CrossOrigin(origins = "*")
@Tag(name = "Risk Calculation, Simulation & Red-Zone Generation (Stages 4.7, 4.8 & 5.1)",
     description = "Calculates final disaster risk, executes what-if scenario simulations, and generates dynamic red zones")
public class RiskCalculationController {

    private final RiskCalculationService riskCalculationService;
    private final RedZoneService redZoneService;

    public RiskCalculationController(RiskCalculationService riskCalculationService,
                                     RedZoneService redZoneService) {
        this.riskCalculationService = riskCalculationService;
        this.redZoneService = redZoneService;
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

    // =========================================================================
    // STAGE 5.1 — DYNAMIC RED-ZONE GENERATION
    // =========================================================================

    /**
     * GET /api/v1/risk/zones
     * Returns all districts with dynamic red-zone classification.
     * Optional: ?minLevel=HIGH to filter by minimum zone level.
     */
    @GetMapping("/zones")
    @Operation(
            summary = "All Districts Dynamic Risk Zones (Stage 5.1)",
            description = "Returns all districts classified into zone levels (LOW, MODERATE, HIGH, CRITICAL/RED-ZONE), dynamically derived from current risk data."
    )
    public ResponseEntity<ApiResponse<List<RedZoneDto>>> getAllRiskZones(
            @Parameter(description = "Minimum zone level filter: LOW, MODERATE, HIGH, CRITICAL", example = "HIGH")
            @RequestParam(name = "minLevel", required = false) String minLevel) {

        List<RedZoneDto> zones;
        if (minLevel != null && !minLevel.trim().isEmpty()) {
            try {
                ZoneLevel level = ZoneLevel.valueOf(minLevel.trim().toUpperCase());
                zones = redZoneService.getZonesByMinimumLevel(level);
            } catch (IllegalArgumentException e) {
                throw new com.hazard.exception.InvalidHazardParameterException(
                        "Invalid minLevel value: '" + minLevel + "'. Allowed values: LOW, MODERATE, HIGH, CRITICAL");
            }
        } else {
            zones = redZoneService.getAllRiskZones();
        }

        long redZoneCount = zones.stream().filter(RedZoneDto::isRedZone).count();

        ApiResponse<List<RedZoneDto>> response = ApiResponse.ok(zones,
                "Dynamic risk zone classification generated successfully");
        response.addMeta("stage", "5.1");
        response.addMeta("substage", "Dynamic Red-Zone Generation");
        response.addMeta("totalZones", zones.size());
        response.addMeta("redZoneCount", redZoneCount);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/zones/red
     * Returns only RED-ZONE districts (VERY_HIGH + CRITICAL risk tiers).
     */
    @GetMapping("/zones/red")
    @Operation(
            summary = "Red-Zone Districts Only (Stage 5.1)",
            description = "Returns only districts classified as Red Zones (VERY_HIGH and CRITICAL risk tiers) requiring immediate attention."
    )
    public ResponseEntity<ApiResponse<List<RedZoneDto>>> getRedZonesOnly() {
        List<RedZoneDto> redZones = redZoneService.getRedZonesOnly();

        ApiResponse<List<RedZoneDto>> response = ApiResponse.ok(redZones,
                "Red-zone districts identified successfully");
        response.addMeta("stage", "5.1");
        response.addMeta("substage", "Red-Zone Identification");
        response.addMeta("redZoneCount", redZones.size());
        if (!redZones.isEmpty()) {
            response.addMeta("highestRiskDistrict", redZones.get(0).getDistrictName());
            response.addMeta("highestRiskScore", redZones.get(0).getRiskScore100());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/zones/geojson
     * Returns GeoJSON FeatureCollection enriched with zone-level classification.
     * Optional: ?redOnly=true to include only red-zone features.
     */
    @GetMapping("/zones/geojson")
    @Operation(
            summary = "Red-Zone GeoJSON Layer (Stage 5.1)",
            description = "Returns RFC 7946 GeoJSON FeatureCollection with dynamic zone-level classification, red-zone flags, and zone colors for map rendering."
    )
    public ResponseEntity<GeoJsonFeatureCollectionDto> getRedZoneGeoJson(
            @Parameter(description = "If true, returns only red-zone district features", example = "false")
            @RequestParam(name = "redOnly", required = false, defaultValue = "false") boolean redOnly) {

        GeoJsonFeatureCollectionDto geojson = redOnly
                ? redZoneService.generateRedZoneOnlyGeoJson()
                : redZoneService.generateRedZoneGeoJson();
        return ResponseEntity.ok(geojson);
    }
}
