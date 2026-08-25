package com.hazard.controller.hazard;

import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.layer.HazardLayerCatalogDto;
import com.hazard.dto.layer.HazardLayerMetadataDto;
import com.hazard.service.layer.HazardLayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST Controller for Stage 3.6 Map-Ready Hazard GIS Layers.
 * Exposes layer catalogs, layer metadata, and RFC 7946 GeoJSON FeatureCollection feeds
 * for browser web mapping clients and GIS visualization tooling.
 */
@RestController
@RequestMapping("/api/v1/hazards/layers")
@CrossOrigin(origins = "*")
@Tag(name = "Hazard Layers", description = "Map-ready RFC 7946 GeoJSON vector and choropleth layers (Stage 3.6)")
public class HazardLayerController {

    private final HazardLayerService hazardLayerService;

    public HazardLayerController(HazardLayerService hazardLayerService) {
        this.hazardLayerService = hazardLayerService;
    }

    /**
     * GET /api/v1/hazards/layers
     * Returns the full catalog of available hazard GIS layers and their metadata.
     */
    @GetMapping
    @Operation(summary = "List available map layer catalog",
            description = "Returns metadata for all 8 available map-ready GIS layers across event, score, multi-hazard, and reference categories.")
    public ResponseEntity<HazardLayerCatalogDto> getLayerCatalog() {
        HazardLayerCatalogDto catalog = hazardLayerService.getLayerCatalog();
        return ResponseEntity.ok(catalog);
    }

    /**
     * GET /api/v1/hazards/layers/{layerId}/metadata
     * Retrieves metadata for a specific hazard layer.
     */
    @GetMapping("/{layerId}/metadata")
    @Operation(summary = "Get layer metadata by ID",
            description = "Retrieves technical metadata, supported filters, geometry types, and data sources for a map layer.")
    public ResponseEntity<HazardLayerMetadataDto> getLayerMetadata(
            @Parameter(description = "Layer ID (e.g. FLOOD_HAZARD_SCORES, MULTI_HAZARD_INDEX, DISTRICT_HAZARD_SUMMARIES)", example = "FLOOD_HAZARD_SCORES")
            @PathVariable("layerId") String layerId) {
        HazardLayerMetadataDto metadata = hazardLayerService.getLayerMetadata(layerId);
        return ResponseEntity.ok(metadata);
    }

    /**
     * GET /api/v1/hazards/layers/{layerId}
     * Delivers map-ready RFC 7946 GeoJSON for the requested layer with optional filtering.
     */
    @GetMapping("/{layerId}")
    @Operation(summary = "Get map-ready GeoJSON layer",
            description = "Delivers map-ready RFC 7946 GeoJSON FeatureCollections for web mapping clients (Leaflet, MapLibre GL, OpenLayers).")
    public ResponseEntity<GeoJsonFeatureCollectionDto> getLayerGeoJson(
            @Parameter(description = "Layer ID (e.g. FLOOD_EVENTS, FLOOD_HAZARD_SCORES, MULTI_HAZARD_INDEX, DISTRICT_HAZARD_SUMMARIES)", example = "DISTRICT_HAZARD_SUMMARIES")
            @PathVariable("layerId") String layerId,
            @Parameter(description = "District filter", example = "Patna")
            @RequestParam(name = "district", required = false) String district,
            @Parameter(description = "Severity tier filter (LOW, MODERATE, HIGH, SEVERE)", example = "HIGH")
            @RequestParam(name = "severity", required = false) String severityStr,
            @Parameter(description = "Start date filter", example = "2020-01-01")
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date filter", example = "2024-12-31")
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Maximum features to return (1-1000)", example = "50")
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {

        SeverityTier severity = (severityStr != null && !severityStr.trim().isEmpty()) ? SeverityTier.fromString(severityStr) : null;
        GeoJsonFeatureCollectionDto geojson = hazardLayerService.getLayerGeoJson(layerId, district, severity, from, to, limit);
        return ResponseEntity.ok(geojson);
    }
}
