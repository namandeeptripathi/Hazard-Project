package com.hazard.controller.exposure;

import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.exposure.DistrictSettlementExposureSummaryDto;
import com.hazard.dto.exposure.GeometryExposureRequestDto;
import com.hazard.dto.exposure.SettlementExposureAnalysisResultDto;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.service.exposure.SettlementExposureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Stage 4.2 — Settlement Exposure.
 *
 * Exposes endpoints to identify which specific villages, towns, cities, and residential settlement
 * clusters are inside or affected by predicted or observed hazard areas.
 */
@RestController
@RequestMapping("/api/v1/exposure/settlements")
@CrossOrigin(origins = "*")
@Tag(name = "Settlement Exposure (Stage 4.2)",
     description = "Identifies exposed settlements, distance to hazard epicenters, exposure scores, and categorical tiers")
public class SettlementExposureController {

    private final SettlementExposureService settlementExposureService;

    public SettlementExposureController(SettlementExposureService settlementExposureService) {
        this.settlementExposureService = settlementExposureService;
    }

    /**
     * GET /api/v1/exposure/settlements/hazard-event/{hazardId}
     * Returns individual settlements exposed to a specific Stage 3 hazard event.
     */
    @GetMapping("/hazard-event/{hazardId}")
    @Operation(
            summary = "Hazard Event Settlement Exposure Overlay",
            description = "Identifies all populated settlements within the impact buffer of a Stage 3 hazard observation (e.g. DFO-3) " +
                    "with distance decay and exposure score calculations."
    )
    public ResponseEntity<ApiResponse<SettlementExposureAnalysisResultDto>> getHazardEventSettlementExposure(
            @Parameter(description = "Stage 3 Hazard ID (e.g. DFO-3, DFO-1)", example = "DFO-3")
            @PathVariable("hazardId") String hazardId,
            @Parameter(description = "Optional buffer radius in meters (default: 5000m / 5km)", example = "5000")
            @RequestParam(name = "bufferMeters", required = false) Double bufferMeters) {

        SettlementExposureAnalysisResultDto result = settlementExposureService.getExposedSettlementsForHazardEvent(hazardId, bufferMeters);
        ApiResponse<SettlementExposureAnalysisResultDto> response = ApiResponse.ok(result,
                "Settlement exposure evaluated successfully for hazard event: " + hazardId);
        response.addMeta("stage", "4.2");
        response.addMeta("substage", "Settlement Exposure");
        response.addMeta("exposedSettlementsCount", result.getExposedSettlementsCount());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/exposure/settlements/district/{districtName}
     * Returns settlement exposure summary and list for a specific administrative district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(
            summary = "District Settlement Exposure Analysis",
            description = "Returns settlement-level exposure scores and categorical distribution " +
                    "for all settlements residing in a specific administrative district."
    )
    public ResponseEntity<ApiResponse<DistrictSettlementExposureSummaryDto>> getDistrictSettlementExposure(
            @Parameter(description = "Name of administrative district e.g. Patna, Sitamarhi, Muzaffarpur", example = "Sitamarhi")
            @PathVariable("districtName") String districtName) {

        DistrictSettlementExposureSummaryDto summary = settlementExposureService.getDistrictSettlementExposure(districtName);
        ApiResponse<DistrictSettlementExposureSummaryDto> response = ApiResponse.ok(summary,
                "District settlement exposure evaluated successfully for: " + districtName);
        response.addMeta("stage", "4.2");
        response.addMeta("totalSettlements", summary.getTotalSettlementsEvaluated());
        response.addMeta("exposedSettlements", summary.getExposedSettlementsCount());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/exposure/settlements/all
     * Returns settlement exposure summaries across all 38 districts of Bihar.
     */
    @GetMapping("/all")
    @Operation(
            summary = "All Districts Settlement Exposure Summary",
            description = "Returns aggregated settlement exposure metrics across all 38 administrative districts of Bihar."
    )
    public ResponseEntity<ApiResponse<List<DistrictSettlementExposureSummaryDto>>> getAllDistrictsSettlementExposureSummary() {
        List<DistrictSettlementExposureSummaryDto> list = settlementExposureService.getAllDistrictsSettlementExposureSummary();
        ApiResponse<List<DistrictSettlementExposureSummaryDto>> response = ApiResponse.ok(list,
                "All districts settlement exposure summary retrieved successfully");
        response.addMeta("stage", "4.2");
        response.addMeta("totalDistricts", list.size());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/exposure/settlements/analyze-geometry
     * Analyzes settlement exposure against a custom GeoJSON or WKT polygon.
     */
    @PostMapping("/analyze-geometry")
    @Operation(
            summary = "Custom Geometry Settlement Exposure Overlay",
            description = "Performs PostGIS spatial intersection between an arbitrary hazard polygon (in WKT format) " +
                    "and settlement footprints to identify affected villages, towns, and cities."
    )
    public ResponseEntity<ApiResponse<SettlementExposureAnalysisResultDto>> analyzeCustomGeometrySettlementExposure(
            @RequestBody GeometryExposureRequestDto request) {

        SettlementExposureAnalysisResultDto result = settlementExposureService.analyzeSettlementsForCustomGeometry(request);
        ApiResponse<SettlementExposureAnalysisResultDto> response = ApiResponse.ok(result,
                "Custom geometry settlement exposure analysis completed successfully");
        response.addMeta("stage", "4.2");
        response.addMeta("exposedSettlementsCount", result.getExposedSettlementsCount());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/exposure/settlements/geojson
     * Returns an RFC 7946 GeoJSON FeatureCollection of Point features representing exposed settlements.
     */
    @GetMapping("/geojson")
    @Operation(
            summary = "Settlement Exposure GeoJSON Point Layer",
            description = "Returns RFC 7946 GeoJSON Point features for exposed settlements with category color codes " +
                    "and exposure scores for interactive GIS map visualization."
    )
    public ResponseEntity<GeoJsonFeatureCollectionDto> getSettlementExposureGeoJson(
            @Parameter(description = "Optional district name filter", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String district,
            @Parameter(description = "Optional hazard event ID filter (e.g. DFO-3)", example = "DFO-3")
            @RequestParam(name = "hazardId", required = false) String hazardId) {

        GeoJsonFeatureCollectionDto featureCollection = settlementExposureService.generateSettlementExposureGeoJson(district, hazardId);
        return ResponseEntity.ok(featureCollection);
    }
}
