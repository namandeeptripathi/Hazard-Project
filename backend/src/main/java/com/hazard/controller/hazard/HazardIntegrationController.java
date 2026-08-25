package com.hazard.controller.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.HazardSummaryDto;
import com.hazard.dto.hazard.IntegratedHazardEvent;
import com.hazard.service.hazard.HazardIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller providing HTTP APIs for Stage 3.1 Hazard Intelligence Integration.
 * Exposes unified hazard queries, spatial filtering, temporal queries, and GeoJSON feeds.
 */
@RestController
@RequestMapping("/api/v1/hazards")
@CrossOrigin(origins = "*")
@Tag(name = "Hazard Integration", description = "Integrated multi-source hazard observations (Stage 3.1)")
public class HazardIntegrationController {

    private final HazardIntegrationService hazardIntegrationService;

    public HazardIntegrationController(HazardIntegrationService hazardIntegrationService) {
        this.hazardIntegrationService = hazardIntegrationService;
    }

    /**
     * GET /api/v1/hazards
     * Retrieves integrated hazard events with optional type and limit filters.
     */
    @GetMapping
    @Operation(summary = "List integrated hazard events",
            description = "Retrieves unified hazard records from DFO, EM-DAT, and Open-Meteo with optional type filter.")
    public ResponseEntity<List<IntegratedHazardEvent>> getAllHazards(
            @Parameter(description = "Hazard type filter (FLOOD, EXTREME_RAINFALL, OTHER)", example = "FLOOD")
            @RequestParam(name = "type", required = false) String typeStr,
            @Parameter(description = "Maximum records to return (1-1000)", example = "50")
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        List<IntegratedHazardEvent> hazards = hazardIntegrationService.getAllIntegratedHazards(type, limit);
        return ResponseEntity.ok(hazards);
    }

    /**
     * GET /api/v1/hazards/{id}
     * Retrieves a single integrated hazard event by unified ID (e.g. "DFO-1", "EMDAT-5", "WEATHER-PATNA-10").
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get integrated hazard event by unified ID",
            description = "Retrieves a single hazard observation by unified ID prefix (DFO, EMDAT, WEATHER).")
    public ResponseEntity<IntegratedHazardEvent> getHazardById(
            @Parameter(description = "Unified hazard ID", example = "DFO-3")
            @PathVariable("id") String id) {
        IntegratedHazardEvent hazard = hazardIntegrationService.getHazardById(id);
        return ResponseEntity.ok(hazard);
    }

    /**
     * GET /api/v1/hazards/type/{type}
     * Retrieves hazard events filtered strictly by HazardType (FLOOD, EXTREME_RAINFALL, OTHER).
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get hazard events by type",
            description = "Retrieves hazard events matching a specific hazard domain type.")
    public ResponseEntity<List<IntegratedHazardEvent>> getHazardsByType(
            @Parameter(description = "Hazard type", example = "FLOOD")
            @PathVariable("type") String typeStr,
            @Parameter(description = "Maximum records to return", example = "50")
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = HazardType.fromString(typeStr);
        List<IntegratedHazardEvent> hazards = hazardIntegrationService.getHazardsByType(type, limit);
        return ResponseEntity.ok(hazards);
    }

    /**
     * GET /api/v1/hazards/district/{districtName}
     * Retrieves hazard events that spatially intersect a specific Bihar administrative district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(summary = "Get hazard events in administrative district",
            description = "Retrieves hazard events located within a Bihar district polygon via PostGIS ST_Contains.")
    public ResponseEntity<List<IntegratedHazardEvent>> getHazardsInDistrict(
            @Parameter(description = "District name", example = "Sitamarhi")
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        List<IntegratedHazardEvent> hazards = hazardIntegrationService.getHazardsInDistrict(districtName, type, limit);
        return ResponseEntity.ok(hazards);
    }

    /**
     * GET /api/v1/hazards/nearby
     * Spatial proximity query: retrieves hazard events within radiusMeters of a WGS 84 point.
     */
    @GetMapping("/nearby")
    @Operation(summary = "Spatial proximity query",
            description = "Finds hazard events within radius meters of a point using PostGIS ST_DWithin.")
    public ResponseEntity<List<IntegratedHazardEvent>> getHazardsNearby(
            @Parameter(description = "Longitude in WGS 84", example = "85.38")
            @RequestParam("longitude") double longitude,
            @Parameter(description = "Latitude in WGS 84", example = "26.12")
            @RequestParam("latitude") double latitude,
            @Parameter(description = "Proximity radius in meters", example = "25000")
            @RequestParam(name = "radiusMeters", defaultValue = "25000") double radiusMeters,
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        List<IntegratedHazardEvent> hazards = hazardIntegrationService.getHazardsNearLocation(
                longitude, latitude, radiusMeters, type, limit
        );
        return ResponseEntity.ok(hazards);
    }

    /**
     * GET /api/v1/hazards/bbox
     * Spatial bounding box query: retrieves hazard events within [minLon, minLat, maxLon, maxLat].
     */
    @GetMapping("/bbox")
    @Operation(summary = "Spatial bounding box query",
            description = "Retrieves hazard events within a spatial bounding box in WGS 84 coordinates.")
    public ResponseEntity<List<IntegratedHazardEvent>> getHazardsInBoundingBox(
            @RequestParam("minLon") double minLon,
            @RequestParam("minLat") double minLat,
            @RequestParam("maxLon") double maxLon,
            @RequestParam("maxLat") double maxLat,
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        List<IntegratedHazardEvent> hazards = hazardIntegrationService.getHazardsInBoundingBox(
                minLon, minLat, maxLon, maxLat, type, limit
        );
        return ResponseEntity.ok(hazards);
    }

    /**
     * GET /api/v1/hazards/time-range
     * Retrieves hazard events that occurred within a specific date window.
     */
    @GetMapping("/time-range")
    @Operation(summary = "Temporal range query",
            description = "Retrieves hazard events occurring within a specified calendar date window.")
    public ResponseEntity<List<IntegratedHazardEvent>> getHazardsInTimeRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        List<IntegratedHazardEvent> hazards = hazardIntegrationService.getHazardsInTimeRange(
                startDate, endDate, type, limit
        );
        return ResponseEntity.ok(hazards);
    }

    /**
     * GET /api/v1/hazards/rainfall/extreme
     * Retrieves extreme meteorological rainfall observation records exceeding a precipitation threshold.
     */
    @GetMapping("/rainfall/extreme")
    @Operation(summary = "Extreme rainfall threshold extraction",
            description = "Extracts precipitation observations exceeding an intensity threshold (mm/hr).")
    public ResponseEntity<List<IntegratedHazardEvent>> getExtremeRainfallHazards(
            @RequestParam(name = "thresholdMm", defaultValue = "10.0") double thresholdMm,
            @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        List<IntegratedHazardEvent> hazards = hazardIntegrationService.getExtremeRainfallHazards(
                thresholdMm, start, end, limit
        );
        return ResponseEntity.ok(hazards);
    }

    /**
     * GET /api/v1/hazards/geojson
     * Delivers integrated hazard events as a standard GeoJSON FeatureCollection (RFC 7946).
     */
    @GetMapping("/geojson")
    @Operation(summary = "Integrated hazards GeoJSON vector feed",
            description = "Delivers integrated hazard point observations as standard RFC 7946 GeoJSON.")
    public ResponseEntity<GeoJsonFeatureCollectionDto> getHazardsGeoJson(
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "limit", required = false, defaultValue = "100") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        GeoJsonFeatureCollectionDto geojson = hazardIntegrationService.getHazardsGeoJson(type, district, limit);
        return ResponseEntity.ok(geojson);
    }

    /**
     * GET /api/v1/hazards/summary
     * Catalog summary metadata detailing dataset row counts, active hazard types, CRS, and temporal bounds.
     */
    @GetMapping("/summary")
    @Operation(summary = "Integrated hazard catalog summary",
            description = "Returns catalog metadata including total row counts, data sources, CRS, and date spans.")
    public ResponseEntity<HazardSummaryDto> getHazardSummary() {
        HazardSummaryDto summary = hazardIntegrationService.getHazardSummary();
        return ResponseEntity.ok(summary);
    }
}
