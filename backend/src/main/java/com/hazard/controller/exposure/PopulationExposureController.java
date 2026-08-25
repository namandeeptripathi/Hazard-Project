package com.hazard.controller.exposure;

import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.exposure.DistrictPopulationExposureDto;
import com.hazard.dto.exposure.GeometryExposureRequestDto;
import com.hazard.dto.exposure.PopulationExposureConfigDto;
import com.hazard.dto.exposure.PopulationExposureResultDto;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.service.exposure.PopulationExposureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Stage 4.1 — Population Exposure.
 *
 * Exposes endpoints for spatial intersection and population exposure analysis
 * across districts, specific Stage 3 hazard events, custom WKT geometries, and RFC 7946 GeoJSON choropleths.
 */
@RestController
@RequestMapping("/api/v1/exposure/population")
@CrossOrigin(origins = "*")
@Tag(name = "Population Exposure (Stage 4.1)",
     description = "Evaluates exposed population, exposure percentages, and severity categories across hazard zones")
public class PopulationExposureController {

    private final PopulationExposureService populationExposureService;

    public PopulationExposureController(PopulationExposureService populationExposureService) {
        this.populationExposureService = populationExposureService;
    }

    /**
     * GET /api/v1/exposure/population/district/{districtName}
     * Evaluates population exposure for a specific administrative district based on active hazard intensity.
     */
    @GetMapping("/district/{districtName}")
    @Operation(
            summary = "District Population Exposure Analysis",
            description = "Analyzes exposed population, exposure percentage, normalized score [0.0000, 1.0000], " +
                    "and categorical classification for a specific administrative district."
    )
    public ResponseEntity<ApiResponse<PopulationExposureResultDto>> getDistrictPopulationExposure(
            @Parameter(description = "Name of administrative district e.g. Patna, Sitamarhi, Muzaffarpur", example = "Patna")
            @PathVariable("districtName") String districtName) {

        PopulationExposureResultDto result = populationExposureService.analyzeDistrictPopulationExposure(districtName);
        ApiResponse<PopulationExposureResultDto> response = ApiResponse.ok(result,
                "Population exposure analyzed successfully for district: " + districtName);
        response.addMeta("stage", "4.1");
        response.addMeta("substage", "Population Exposure");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/exposure/population/hazard-event/{hazardId}
     * Evaluates population exposure within a spatial buffer around a Stage 3 hazard observation.
     */
    @GetMapping("/hazard-event/{hazardId}")
    @Operation(
            summary = "Hazard Event Population Exposure Overlay",
            description = "Performs radial spatial buffer overlay around a Stage 3 hazard observation (e.g. DFO-3) " +
                    "and intersects populated settlements to compute exposed population."
    )
    public ResponseEntity<ApiResponse<PopulationExposureResultDto>> getHazardEventPopulationExposure(
            @Parameter(description = "Stage 3 Hazard ID (e.g. DFO-3, WEAT-43848)", example = "DFO-3")
            @PathVariable("hazardId") String hazardId,
            @Parameter(description = "Optional buffer radius in meters (default: 5000m / 5km)", example = "5000")
            @RequestParam(name = "bufferMeters", required = false) Double bufferMeters) {

        PopulationExposureResultDto result = populationExposureService.analyzeHazardEventExposure(hazardId, bufferMeters);
        ApiResponse<PopulationExposureResultDto> response = ApiResponse.ok(result,
                "Population exposure analyzed successfully for hazard event: " + hazardId);
        response.addMeta("stage", "4.1");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/exposure/population/analyze-geometry
     * Evaluates population exposure for an arbitrary hazard polygon geometry supplied in WKT format.
     */
    @PostMapping("/analyze-geometry")
    @Operation(
            summary = "Custom Geometry Population Exposure Overlay",
            description = "Performs PostGIS spatial overlay between an arbitrary hazard polygon (in WKT format) " +
                    "and populated settlements to compute exposed population and exposure categories."
    )
    public ResponseEntity<ApiResponse<PopulationExposureResultDto>> analyzeCustomGeometryExposure(
            @RequestBody GeometryExposureRequestDto request) {

        PopulationExposureResultDto result = populationExposureService.analyzeCustomGeometryExposure(request);
        ApiResponse<PopulationExposureResultDto> response = ApiResponse.ok(result,
                "Population exposure analyzed successfully for custom geometry");
        response.addMeta("stage", "4.1");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/exposure/population/all-districts
     * Returns population exposure metrics for all 38 districts of Bihar.
     */
    @GetMapping("/all-districts")
    @Operation(
            summary = "All Districts Population Exposure Summary",
            description = "Returns aggregated population exposure metrics, percentages, and categories " +
                    "for all 38 administrative districts of Bihar."
    )
    public ResponseEntity<ApiResponse<List<DistrictPopulationExposureDto>>> getAllDistrictsPopulationExposure() {
        List<DistrictPopulationExposureDto> list = populationExposureService.analyzeAllDistrictsPopulationExposure();
        ApiResponse<List<DistrictPopulationExposureDto>> response = ApiResponse.ok(list,
                "All districts population exposure summary retrieved successfully");
        response.addMeta("stage", "4.1");
        response.addMeta("totalDistricts", list.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/exposure/population/geojson
     * Returns an RFC 7946 GeoJSON FeatureCollection of district boundaries enriched with exposure metrics.
     */
    @GetMapping("/geojson")
    @Operation(
            summary = "Population Exposure GeoJSON Choropleth Layer",
            description = "Returns RFC 7946 GeoJSON district polygons enriched with exposed population, " +
                    "exposure percentage, and category properties for interactive GIS map visualization."
    )
    public ResponseEntity<GeoJsonFeatureCollectionDto> getPopulationExposureGeoJson(
            @Parameter(description = "Optional district name filter", example = "Patna")
            @RequestParam(name = "district", required = false) String district) {

        GeoJsonFeatureCollectionDto featureCollection = populationExposureService.generatePopulationExposureGeoJson(district);
        return ResponseEntity.ok(featureCollection);
    }

    /**
     * GET /api/v1/exposure/population/config
     * Returns the active classification thresholds and density parameters.
     */
    @GetMapping("/config")
    @Operation(
            summary = "Population Exposure Configuration Parameters",
            description = "Returns the active classification thresholds, default buffer radii, and density parameters."
    )
    public ResponseEntity<ApiResponse<PopulationExposureConfigDto>> getExposureConfiguration() {
        PopulationExposureConfigDto configDto = populationExposureService.getConfiguration();
        ApiResponse<PopulationExposureConfigDto> response = ApiResponse.ok(configDto,
                "Population exposure configuration retrieved successfully");
        response.addMeta("stage", "4.1");
        return ResponseEntity.ok(response);
    }
}
