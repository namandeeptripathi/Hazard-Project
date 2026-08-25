package com.hazard.controller.exposure;

import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.exposure.GeometryExposureRequestDto;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.DistrictInfrastructureExposureSummaryDto;
import com.hazard.dto.infrastructure.InfrastructureExposureAnalysisResultDto;
import com.hazard.dto.infrastructure.InfrastructureExposureConfigDto;
import com.hazard.service.exposure.InfrastructureExposureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Stage 4.3 — Infrastructure Exposure.
 *
 * Exposes endpoints to identify which critical infrastructure assets (hospitals, schools,
 * transport lifelines, bridges, emergency centers, power substations, water infrastructure)
 * are exposed to predicted or observed hazard areas.
 */
@RestController
@RequestMapping("/api/v1/exposure/infrastructure")
@CrossOrigin(origins = "*")
@Tag(name = "Infrastructure Exposure (Stage 4.3)",
     description = "Identifies critical infrastructure exposure, distance decay, criticality multipliers, and exposure tiers")
public class InfrastructureExposureController {

    private final InfrastructureExposureService infrastructureExposureService;

    public InfrastructureExposureController(InfrastructureExposureService infrastructureExposureService) {
        this.infrastructureExposureService = infrastructureExposureService;
    }

    /**
     * GET /api/v1/exposure/infrastructure/hazard-event/{hazardId}
     * Returns individual infrastructure assets exposed within the impact buffer of a Stage 3 hazard event.
     */
    @GetMapping("/hazard-event/{hazardId}")
    @Operation(
            summary = "Hazard Event Infrastructure Exposure Overlay",
            description = "Identifies all critical infrastructure assets (hospitals, bridges, emergency shelters, power nodes, dams, canals) " +
                    "within the impact buffer of a Stage 3 hazard observation (e.g. DFO-3)."
    )
    public ResponseEntity<ApiResponse<InfrastructureExposureAnalysisResultDto>> getHazardEventInfrastructureExposure(
            @Parameter(description = "Stage 3 Hazard ID (e.g. DFO-3, DFO-1)", example = "DFO-3")
            @PathVariable("hazardId") String hazardId,
            @Parameter(description = "Optional buffer radius in meters (default: 5000m / 5km)", example = "5000")
            @RequestParam(name = "bufferMeters", required = false) Double bufferMeters) {

        InfrastructureExposureAnalysisResultDto result = infrastructureExposureService.getExposedInfrastructureForHazardEvent(hazardId, bufferMeters);
        ApiResponse<InfrastructureExposureAnalysisResultDto> response = ApiResponse.ok(result,
                "Infrastructure exposure evaluated successfully for hazard event: " + hazardId);
        response.addMeta("stage", "4.3");
        response.addMeta("substage", "Infrastructure Exposure");
        response.addMeta("exposedAssetsCount", result.getExposedAssetsCount());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/exposure/infrastructure/district/{districtName}
     * Returns infrastructure exposure summary and asset list for a specific administrative district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(
            summary = "District Infrastructure Exposure Analysis",
            description = "Returns infrastructure asset exposure scores, category breakdowns, and criticality distributions " +
                    "for an administrative district."
    )
    public ResponseEntity<ApiResponse<DistrictInfrastructureExposureSummaryDto>> getDistrictInfrastructureExposure(
            @Parameter(description = "Name of administrative district e.g. Patna, Sitamarhi, Muzaffarpur", example = "Patna")
            @PathVariable("districtName") String districtName) {

        DistrictInfrastructureExposureSummaryDto summary = infrastructureExposureService.getDistrictInfrastructureExposure(districtName);
        ApiResponse<DistrictInfrastructureExposureSummaryDto> response = ApiResponse.ok(summary,
                "District infrastructure exposure evaluated successfully for: " + districtName);
        response.addMeta("stage", "4.3");
        response.addMeta("totalAssets", summary.getTotalAssetsEvaluated());
        response.addMeta("exposedAssets", summary.getExposedAssetsCount());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/exposure/infrastructure/all
     * Returns infrastructure exposure summaries across all 38 districts of Bihar.
     */
    @GetMapping("/all")
    @Operation(
            summary = "All Districts Infrastructure Exposure Summary",
            description = "Returns aggregated infrastructure exposure metrics across all 38 administrative districts of Bihar."
    )
    public ResponseEntity<ApiResponse<List<DistrictInfrastructureExposureSummaryDto>>> getAllDistrictsInfrastructureExposureSummary() {
        List<DistrictInfrastructureExposureSummaryDto> list = infrastructureExposureService.getAllDistrictsInfrastructureExposureSummary();
        ApiResponse<List<DistrictInfrastructureExposureSummaryDto>> response = ApiResponse.ok(list,
                "All districts infrastructure exposure summary retrieved successfully");
        response.addMeta("stage", "4.3");
        response.addMeta("totalDistricts", list.size());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/exposure/infrastructure/analyze-geometry
     * Analyzes infrastructure exposure against a custom GeoJSON or WKT polygon.
     */
    @PostMapping("/analyze-geometry")
    @Operation(
            summary = "Custom Geometry Infrastructure Exposure Overlay",
            description = "Performs PostGIS spatial intersection between an arbitrary hazard polygon (in WKT format) " +
                    "and infrastructure assets."
    )
    public ResponseEntity<ApiResponse<InfrastructureExposureAnalysisResultDto>> analyzeCustomGeometryInfrastructureExposure(
            @RequestBody GeometryExposureRequestDto request) {

        InfrastructureExposureAnalysisResultDto result = infrastructureExposureService.analyzeInfrastructureForCustomGeometry(request);
        ApiResponse<InfrastructureExposureAnalysisResultDto> response = ApiResponse.ok(result,
                "Custom geometry infrastructure exposure analysis completed successfully");
        response.addMeta("stage", "4.3");
        response.addMeta("exposedAssetsCount", result.getExposedAssetsCount());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/exposure/infrastructure/geojson
     * Returns an RFC 7946 GeoJSON FeatureCollection of exposed infrastructure assets.
     */
    @GetMapping("/geojson")
    @Operation(
            summary = "Infrastructure Exposure GeoJSON Layer",
            description = "Returns RFC 7946 GeoJSON features for exposed infrastructure with category color codes " +
                    "and criticality metadata for interactive GIS map visualization."
    )
    public ResponseEntity<GeoJsonFeatureCollectionDto> getInfrastructureExposureGeoJson(
            @Parameter(description = "Optional district name filter", example = "Patna")
            @RequestParam(name = "district", required = false) String district,
            @Parameter(description = "Optional hazard event ID filter (e.g. DFO-3)", example = "DFO-3")
            @RequestParam(name = "hazardId", required = false) String hazardId) {

        GeoJsonFeatureCollectionDto featureCollection = infrastructureExposureService.generateInfrastructureExposureGeoJson(district, hazardId);
        return ResponseEntity.ok(featureCollection);
    }

    /**
     * GET /api/v1/exposure/infrastructure/config
     * Returns active configuration, criticality weights, and classification thresholds.
     */
    @GetMapping("/config")
    @Operation(
            summary = "Infrastructure Exposure Configuration",
            description = "Returns active criticality multipliers, buffer defaults, and exposure classification thresholds."
    )
    public ResponseEntity<ApiResponse<InfrastructureExposureConfigDto>> getInfrastructureExposureConfig() {
        InfrastructureExposureConfigDto configDto = infrastructureExposureService.getInfrastructureExposureConfig();
        ApiResponse<InfrastructureExposureConfigDto> response = ApiResponse.ok(configDto,
                "Infrastructure exposure configuration retrieved successfully");
        response.addMeta("stage", "4.3");
        return ResponseEntity.ok(response);
    }
}
