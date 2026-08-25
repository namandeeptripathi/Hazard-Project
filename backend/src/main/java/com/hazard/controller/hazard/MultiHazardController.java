package com.hazard.controller.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.multihazard.MultiHazardObservation;
import com.hazard.dto.multihazard.MultiHazardSummaryDto;
import com.hazard.service.multihazard.MultiHazardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Sub-Stage 3.5 Multi-Hazard Handling.
 * Exposes cross-hazard coincidence observations, Multi-Hazard Index calculations,
 * dominance classifications, and GeoJSON vector feeds.
 */
@RestController
@RequestMapping("/api/v1/hazards/multi-hazard")
@CrossOrigin(origins = "*")
@Tag(name = "Multi-Hazard Intelligence", description = "Cross-hazard spatial and temporal coincidence indices (Stage 3.5)")
public class MultiHazardController {

    private final MultiHazardService multiHazardService;

    public MultiHazardController(MultiHazardService multiHazardService) {
        this.multiHazardService = multiHazardService;
    }

    /**
     * GET /api/v1/hazards/multi-hazard
     * Lists multi-hazard observations with optional filtering.
     */
    @GetMapping
    @Operation(summary = "List multi-hazard observations",
            description = "Retrieves synthesized multi-hazard observations with combined index, severity tier, and dominance.")
    public ResponseEntity<List<MultiHazardObservation>> getAllMultiHazardObservations(
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "severity", required = false) String severityStr,
            @RequestParam(name = "dominantHazard", required = false) String dominantHazardStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        SeverityTier severity = (severityStr != null && !severityStr.trim().isEmpty()) ? SeverityTier.fromString(severityStr) : null;
        HazardType dominant = (dominantHazardStr != null && !dominantHazardStr.trim().isEmpty()) ? HazardType.fromString(dominantHazardStr) : null;

        List<MultiHazardObservation> list = multiHazardService.getAllMultiHazardObservations(district, severity, dominant, limit);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/v1/hazards/multi-hazard/{id}
     * Retrieves a single multi-hazard observation by unified ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get multi-hazard observation by ID",
            description = "Retrieves an individual multi-hazard observation with participating hazard breakdown.")
    public ResponseEntity<MultiHazardObservation> getMultiHazardObservationById(
            @Parameter(description = "Multi-hazard ID", example = "MULTI-DFO-3")
            @PathVariable("id") String id) {
        MultiHazardObservation obs = multiHazardService.getMultiHazardObservationById(id);
        return ResponseEntity.ok(obs);
    }

    /**
     * GET /api/v1/hazards/multi-hazard/district/{districtName}
     * Retrieves multi-hazard observations for a specific administrative district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(summary = "Get multi-hazard observations in district",
            description = "Retrieves multi-hazard observations for a specific Bihar administrative district.")
    public ResponseEntity<List<MultiHazardObservation>> getMultiHazardObservationsInDistrict(
            @Parameter(description = "District name", example = "Sitamarhi")
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "severity", required = false) String severityStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        SeverityTier severity = (severityStr != null && !severityStr.trim().isEmpty()) ? SeverityTier.fromString(severityStr) : null;
        List<MultiHazardObservation> list = multiHazardService.getMultiHazardObservationsInDistrict(districtName, severity, limit);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/v1/hazards/multi-hazard/summary
     * Catalog summary of multi-hazard coincidence metrics and tier distributions.
     */
    @GetMapping("/summary")
    @Operation(summary = "Multi-hazard catalog summary",
            description = "Executive catalog summary of full match counts, tier distributions, and active weighting schemes.")
    public ResponseEntity<MultiHazardSummaryDto> getMultiHazardSummary() {
        MultiHazardSummaryDto summary = multiHazardService.getMultiHazardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/v1/hazards/multi-hazard/geojson
     * Delivers multi-hazard observations as an RFC 7946 GeoJSON FeatureCollection.
     */
    @GetMapping("/geojson")
    @Operation(summary = "Multi-hazard GeoJSON vector feed",
            description = "Delivers synthesized multi-hazard points as RFC 7946 GeoJSON with multiHazardIndex and dominance.")
    public ResponseEntity<GeoJsonFeatureCollectionDto> getMultiHazardGeoJson(
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "severity", required = false) String severityStr,
            @RequestParam(name = "limit", required = false, defaultValue = "100") Integer limit) {
        SeverityTier severity = (severityStr != null && !severityStr.trim().isEmpty()) ? SeverityTier.fromString(severityStr) : null;
        GeoJsonFeatureCollectionDto geojson = multiHazardService.getMultiHazardGeoJson(district, severity, limit);
        return ResponseEntity.ok(geojson);
    }
}
