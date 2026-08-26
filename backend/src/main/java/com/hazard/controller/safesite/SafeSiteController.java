package com.hazard.controller.safesite;

import com.hazard.domain.safesite.HealthcareAccessStatus;
import com.hazard.domain.safesite.InfrastructureAccessStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.domain.safesite.WaterAccessStatus;
import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.service.safesite.CandidateSafeSiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Stages 5.2 - 5.11 — Safe-Site Identification, Multi-Criteria Intelligence, Suitability & Ranking.
 *
 * Exposes endpoints for discovering candidate evacuation locations (schools, government buildings,
 * emergency shelters, hospitals), assessing spatial hazard safety (SAFE, AT_RISK, UNKNOWN),
 * evaluating terrain slope suitability (FAVORABLE, UNFAVORABLE, UNKNOWN), calculating
 * geographic distance to active disaster areas (NEAR, MODERATE, FAR, UNKNOWN),
 * evaluating road accessibility proximity (NEAR, MODERATE, FAR, UNKNOWN),
 * evaluating healthcare support availability/proximity (NEAR, MODERATE, FAR, UNKNOWN),
 * evaluating useful water infrastructure availability/proximity (NEAR, MODERATE, FAR, UNKNOWN),
 * evaluating supporting institutional infrastructure availability/proximity (NEAR, MODERATE, FAR, UNKNOWN),
 * computing explainable multi-criteria site suitability intelligence (HIGHLY_SUITABLE, SUITABLE, MARGINAL, UNSUITABLE, UNKNOWN), and
 * performing deterministic hierarchical candidate safe-site ranking (Stage 5.11).
 */
@RestController
@RequestMapping("/api/v1/safe-sites")
@CrossOrigin(origins = "*")
@Tag(name = "Safe Sites & Multi-Criteria Intelligence (Stages 5.2 - 5.11)",
     description = "Discovers emergency safe sites, evaluates spatial disaster exposure, terrain/slope intelligence, geographic distance, road accessibility, healthcare proximity, water supply, supporting infrastructure, site suitability, and hierarchical ranking")
public class SafeSiteController {

    private final CandidateSafeSiteService candidateSafeSiteService;

    public SafeSiteController(CandidateSafeSiteService candidateSafeSiteService) {
        this.candidateSafeSiteService = candidateSafeSiteService;
    }

    /**
     * GET /api/v1/safe-sites
     * Returns candidate safe sites with optional multi-criteria filtering across all spatial dimensions, suitability class, and top N rank limit.
     */
    @GetMapping
    @Operation(
            summary = "Discover & Rank Candidate Safe Sites with Multi-Criteria Intelligence",
            description = "Returns normalized, ranked candidate safe sites evaluated for hazard safety, terrain feasibility, geodesic distance, road accessibility, healthcare support, water accessibility, supporting infrastructure, suitability classification, and deterministic hierarchical ranking. Supports multi-criteria query filtering and top-N ranking limit."
    )
    public ResponseEntity<ApiResponse<List<CandidateSafeSiteDto>>> getCandidateSafeSites(
            @Parameter(description = "Optional district name filter e.g. Sitamarhi, Patna, Muzaffarpur", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String district,
            @Parameter(description = "Optional category filter: EDUCATION, GOVERNMENT, EMERGENCY_SHELTER, HEALTHCARE", example = "EMERGENCY_SHELTER")
            @RequestParam(name = "category", required = false) String category,
            @Parameter(description = "If true, restricts results to sites in active Stage 5.1 Red-Zone districts", example = "false")
            @RequestParam(name = "redZoneOnly", required = false, defaultValue = "false") boolean redZoneOnly,
            @Parameter(description = "Optional hazard safety status filter: SAFE, AT_RISK, UNKNOWN", example = "SAFE")
            @RequestParam(name = "hazardSafety", required = false) String hazardSafety,
            @Parameter(description = "Optional terrain status filter: FAVORABLE, UNFAVORABLE, UNKNOWN", example = "FAVORABLE")
            @RequestParam(name = "terrainStatus", required = false) String terrainStatus,
            @Parameter(description = "Optional distance status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "distanceStatus", required = false) String distanceStatus,
            @Parameter(description = "Optional road access status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "roadAccessStatus", required = false) String roadAccessStatus,
            @Parameter(description = "Optional healthcare access status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "healthcareAccessStatus", required = false) String healthcareAccessStatus,
            @Parameter(description = "Optional water access status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "waterAccessStatus", required = false) String waterAccessStatus,
            @Parameter(description = "Optional infrastructure access status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "infrastructureAccessStatus", required = false) String infrastructureAccessStatus,
            @Parameter(description = "Optional suitability class filter: HIGHLY_SUITABLE, SUITABLE, MARGINAL, UNSUITABLE, UNKNOWN", example = "HIGHLY_SUITABLE")
            @RequestParam(name = "suitabilityClass", required = false) String suitabilityClass,
            @Parameter(description = "Optional limit on number of ranked sites returned (positive integer)", example = "5")
            @RequestParam(name = "limit", required = false) Integer limit,
            @Parameter(description = "Optional top N ranking limit (positive integer)", example = "5")
            @RequestParam(name = "top", required = false) Integer top) {

        // Validate filters if provided to ensure early 400 rejection
        if (roadAccessStatus != null && !roadAccessStatus.trim().isEmpty()) {
            RoadAccessStatus.fromString(roadAccessStatus);
        }
        if (healthcareAccessStatus != null && !healthcareAccessStatus.trim().isEmpty()) {
            HealthcareAccessStatus.fromString(healthcareAccessStatus);
        }
        if (waterAccessStatus != null && !waterAccessStatus.trim().isEmpty()) {
            WaterAccessStatus.fromString(waterAccessStatus);
        }
        if (infrastructureAccessStatus != null && !infrastructureAccessStatus.trim().isEmpty()) {
            InfrastructureAccessStatus.fromString(infrastructureAccessStatus);
        }
        if (suitabilityClass != null && !suitabilityClass.trim().isEmpty()) {
            SuitabilityClass.fromString(suitabilityClass);
        }
        if (limit != null && limit <= 0) {
            throw new InvalidHazardParameterException("Parameter 'limit' must be a positive integer greater than 0.");
        }
        if (top != null && top <= 0) {
            throw new InvalidHazardParameterException("Parameter 'top' must be a positive integer greater than 0.");
        }

        Integer effectiveLimit = limit != null ? limit : top;

        List<CandidateSafeSiteDto> sites = candidateSafeSiteService.getCandidateSites(
                district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus,
                roadAccessStatus, healthcareAccessStatus, waterAccessStatus, infrastructureAccessStatus, suitabilityClass, effectiveLimit);
        ApiResponse<List<CandidateSafeSiteDto>> response = ApiResponse.ok(sites,
                "Candidate safe sites retrieved successfully");
        response.addMeta("stage", "5.11");
        response.addMeta("substage", "Candidate Safe-Site Ranking");
        response.addMeta("totalCandidates", sites.size());
        if (district != null && !district.trim().isEmpty()) {
            response.addMeta("district", district.trim());
        }
        if (category != null && !category.trim().isEmpty()) {
            response.addMeta("categoryFilter", category.trim());
        }
        if (hazardSafety != null && !hazardSafety.trim().isEmpty()) {
            response.addMeta("hazardSafetyFilter", hazardSafety.trim().toUpperCase());
        }
        if (terrainStatus != null && !terrainStatus.trim().isEmpty()) {
            response.addMeta("terrainStatusFilter", terrainStatus.trim().toUpperCase());
        }
        if (distanceStatus != null && !distanceStatus.trim().isEmpty()) {
            response.addMeta("distanceStatusFilter", distanceStatus.trim().toUpperCase());
        }
        if (roadAccessStatus != null && !roadAccessStatus.trim().isEmpty()) {
            response.addMeta("roadAccessStatusFilter", roadAccessStatus.trim().toUpperCase());
        }
        if (healthcareAccessStatus != null && !healthcareAccessStatus.trim().isEmpty()) {
            response.addMeta("healthcareAccessStatusFilter", healthcareAccessStatus.trim().toUpperCase());
        }
        if (waterAccessStatus != null && !waterAccessStatus.trim().isEmpty()) {
            response.addMeta("waterAccessStatusFilter", waterAccessStatus.trim().toUpperCase());
        }
        if (infrastructureAccessStatus != null && !infrastructureAccessStatus.trim().isEmpty()) {
            response.addMeta("infrastructureAccessStatusFilter", infrastructureAccessStatus.trim().toUpperCase());
        }
        if (suitabilityClass != null && !suitabilityClass.trim().isEmpty()) {
            response.addMeta("suitabilityClassFilter", suitabilityClass.trim().toUpperCase());
        }
        if (limit != null) {
            response.addMeta("limitFilter", limit);
        }
        if (top != null) {
            response.addMeta("topFilter", top);
        }
        response.addMeta("redZoneOnly", redZoneOnly);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/safe-sites/ranked
     * Alias endpoint returning candidate safe sites in ranked order.
     */
    @GetMapping("/ranked")
    @Operation(
            summary = "Get Ranked Candidate Safe Sites (Stage 5.11)",
            description = "Returns candidate safe sites ordered by suitability ranking (HIGHLY_SUITABLE -> SUITABLE -> MARGINAL -> UNSUITABLE -> UNKNOWN, score DESC, completeness DESC, siteId ASC) with optional limit."
    )
    public ResponseEntity<ApiResponse<List<CandidateSafeSiteDto>>> getRankedCandidateSites(
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "redZoneOnly", required = false, defaultValue = "false") boolean redZoneOnly,
            @RequestParam(name = "hazardSafety", required = false) String hazardSafety,
            @RequestParam(name = "terrainStatus", required = false) String terrainStatus,
            @RequestParam(name = "distanceStatus", required = false) String distanceStatus,
            @RequestParam(name = "roadAccessStatus", required = false) String roadAccessStatus,
            @RequestParam(name = "healthcareAccessStatus", required = false) String healthcareAccessStatus,
            @RequestParam(name = "waterAccessStatus", required = false) String waterAccessStatus,
            @RequestParam(name = "infrastructureAccessStatus", required = false) String infrastructureAccessStatus,
            @RequestParam(name = "suitabilityClass", required = false) String suitabilityClass,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "top", required = false) Integer top) {

        return getCandidateSafeSites(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus,
                roadAccessStatus, healthcareAccessStatus, waterAccessStatus, infrastructureAccessStatus, suitabilityClass, limit, top);
    }

    /**
     * GET /api/v1/safe-sites/{siteId}
     * Returns a single candidate safe site by ID with its hazard safety, terrain, distance, road, healthcare, water, infrastructure, suitability, and rank assessments.
     */
    @GetMapping("/{siteId}")
    @Operation(
            summary = "Candidate Safe Site Detail with Multi-Criteria Intelligence & Ranking",
            description = "Returns detailed candidate safe site metadata, hazard safety evaluation, terrain feasibility, distance proximity, road accessibility, healthcare support availability, water availability, supporting infrastructure, site suitability intelligence, and assigned rank by unique site ID."
    )
    public ResponseEntity<ApiResponse<CandidateSafeSiteDto>> getCandidateSiteById(
            @Parameter(description = "Site unique identifier (e.g. FAC-EMG-003, FAC-EDU-001)", example = "FAC-EMG-003")
            @PathVariable("siteId") String siteId) {

        CandidateSafeSiteDto site = candidateSafeSiteService.getCandidateSiteById(siteId);
        ApiResponse<CandidateSafeSiteDto> response = ApiResponse.ok(site,
                "Candidate safe site retrieved successfully for: " + siteId);
        response.addMeta("stage", "5.11");
        response.addMeta("substage", "Candidate Safe-Site Ranking");
        response.addMeta("siteId", site.getSiteId());
        response.addMeta("category", site.getCategory() != null ? site.getCategory().name() : "UNKNOWN");
        response.addMeta("hazardSafetyStatus", site.getHazardSafetyStatus() != null ? site.getHazardSafetyStatus().name() : "UNKNOWN");
        response.addMeta("terrainStatus", site.getTerrainStatus() != null ? site.getTerrainStatus().name() : "UNKNOWN");
        response.addMeta("distanceStatus", site.getDistanceStatus() != null ? site.getDistanceStatus().name() : "UNKNOWN");
        response.addMeta("roadAccessStatus", site.getRoadAccessStatus() != null ? site.getRoadAccessStatus().name() : "UNKNOWN");
        response.addMeta("healthcareAccessStatus", site.getHealthcareAccessStatus() != null ? site.getHealthcareAccessStatus().name() : "UNKNOWN");
        response.addMeta("waterAccessStatus", site.getWaterAccessStatus() != null ? site.getWaterAccessStatus().name() : "UNKNOWN");
        response.addMeta("infrastructureAccessStatus", site.getInfrastructureAccessStatus() != null ? site.getInfrastructureAccessStatus().name() : "UNKNOWN");
        response.addMeta("suitabilityClass", site.getSuitabilityClass() != null ? site.getSuitabilityClass().name() : "UNKNOWN");
        response.addMeta("suitabilityScore", site.getSuitabilityScore());
        response.addMeta("rank", site.getRank());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/safe-sites/geojson
     * Returns an RFC 7946 GeoJSON FeatureCollection of candidate safe sites enriched with multi-criteria metadata, suitability, and ranking properties.
     */
    @GetMapping("/geojson")
    @Operation(
            summary = "Candidate Safe Sites GeoJSON Layer with Multi-Criteria & Ranking Properties",
            description = "Returns RFC 7946 GeoJSON FeatureCollection with Point geometry, category colors, hazard safety, terrain properties, distance metrics, road accessibility, healthcare proximity, water availability, supporting infrastructure, suitability properties, and rank positions ready for frontend Leaflet/Mapbox mapping."
    )
    public ResponseEntity<GeoJsonFeatureCollectionDto> getCandidateSitesGeoJson(
            @Parameter(description = "Optional district name filter", example = "Sitamarhi")
            @RequestParam(name = "district", required = false) String district,
            @Parameter(description = "Optional category filter: EDUCATION, GOVERNMENT, EMERGENCY_SHELTER, HEALTHCARE", example = "EDUCATION")
            @RequestParam(name = "category", required = false) String category,
            @Parameter(description = "If true, restricts results to sites in active Stage 5.1 Red-Zone districts", example = "false")
            @RequestParam(name = "redZoneOnly", required = false, defaultValue = "false") boolean redZoneOnly,
            @Parameter(description = "Optional hazard safety status filter: SAFE, AT_RISK, UNKNOWN", example = "SAFE")
            @RequestParam(name = "hazardSafety", required = false) String hazardSafety,
            @Parameter(description = "Optional terrain status filter: FAVORABLE, UNFAVORABLE, UNKNOWN", example = "FAVORABLE")
            @RequestParam(name = "terrainStatus", required = false) String terrainStatus,
            @Parameter(description = "Optional distance status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "distanceStatus", required = false) String distanceStatus,
            @Parameter(description = "Optional road access status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "roadAccessStatus", required = false) String roadAccessStatus,
            @Parameter(description = "Optional healthcare access status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "healthcareAccessStatus", required = false) String healthcareAccessStatus,
            @Parameter(description = "Optional water access status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "waterAccessStatus", required = false) String waterAccessStatus,
            @Parameter(description = "Optional infrastructure access status filter: NEAR, MODERATE, FAR, UNKNOWN", example = "NEAR")
            @RequestParam(name = "infrastructureAccessStatus", required = false) String infrastructureAccessStatus,
            @Parameter(description = "Optional suitability class filter: HIGHLY_SUITABLE, SUITABLE, MARGINAL, UNSUITABLE, UNKNOWN", example = "HIGHLY_SUITABLE")
            @RequestParam(name = "suitabilityClass", required = false) String suitabilityClass,
            @Parameter(description = "Optional top N ranking limit (positive integer)", example = "5")
            @RequestParam(name = "top", required = false) Integer top) {

        // Validate filters if provided
        if (roadAccessStatus != null && !roadAccessStatus.trim().isEmpty()) {
            RoadAccessStatus.fromString(roadAccessStatus);
        }
        if (healthcareAccessStatus != null && !healthcareAccessStatus.trim().isEmpty()) {
            HealthcareAccessStatus.fromString(healthcareAccessStatus);
        }
        if (waterAccessStatus != null && !waterAccessStatus.trim().isEmpty()) {
            WaterAccessStatus.fromString(waterAccessStatus);
        }
        if (infrastructureAccessStatus != null && !infrastructureAccessStatus.trim().isEmpty()) {
            InfrastructureAccessStatus.fromString(infrastructureAccessStatus);
        }
        if (suitabilityClass != null && !suitabilityClass.trim().isEmpty()) {
            SuitabilityClass.fromString(suitabilityClass);
        }
        if (top != null && top <= 0) {
            throw new InvalidHazardParameterException("Parameter 'top' must be a positive integer greater than 0.");
        }

        GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus,
                roadAccessStatus, healthcareAccessStatus, waterAccessStatus, infrastructureAccessStatus, suitabilityClass, top);
        return ResponseEntity.ok(geojson);
    }
}
