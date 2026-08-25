package com.hazard.controller.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.normalization.NormalizationSummaryDto;
import com.hazard.dto.normalization.NormalizedDailyRainfall;
import com.hazard.dto.normalization.NormalizedHazardObservation;
import com.hazard.dto.normalization.NormalizedRollingRainfall;
import com.hazard.service.normalization.HazardNormalizationService;
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
 * REST Controller for Sub-Stage 3.3 Hazard Normalization.
 * Exposes standardized [0.00, 1.00] normalized hazard indicators, daily rainfall,
 * rolling accumulation, and normalization catalog summaries.
 */
@RestController
@RequestMapping("/api/v1/hazards/normalized")
@CrossOrigin(origins = "*")
@Tag(name = "Hazard Normalization", description = "Min-max normalized hazard indicators in [0, 1] (Stage 3.3)")
public class HazardNormalizationController {

    private final HazardNormalizationService hazardNormalizationService;

    public HazardNormalizationController(HazardNormalizationService hazardNormalizationService) {
        this.hazardNormalizationService = hazardNormalizationService;
    }

    /**
     * GET /api/v1/hazards/normalized
     * Lists normalized hazard observations with optional filtering.
     */
    @GetMapping
    @Operation(summary = "List normalized hazard observations",
            description = "Retrieves normalized hazard indicators scaled to [0.0000, 1.0000] with optional filters.")
    public ResponseEntity<List<NormalizedHazardObservation>> getAllNormalizedHazards(
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "quality", required = false) String qualityStr,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "metric", required = false) String metricName,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        QualityStatus quality = (qualityStr != null && !qualityStr.trim().isEmpty()) ? QualityStatus.fromString(qualityStr) : null;
        List<NormalizedHazardObservation> list = hazardNormalizationService.getAllNormalizedHazards(
                type, quality, district, metricName, limit
        );
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/v1/hazards/normalized/{id}
     * Retrieves a single normalized hazard observation by unified ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get normalized hazard by ID",
            description = "Retrieves a single normalized hazard observation with its metric vectors.")
    public ResponseEntity<NormalizedHazardObservation> getNormalizedHazardById(
            @Parameter(description = "Unified hazard ID", example = "DFO-3")
            @PathVariable("id") String id) {
        NormalizedHazardObservation obs = hazardNormalizationService.getNormalizedHazardById(id);
        return ResponseEntity.ok(obs);
    }

    /**
     * GET /api/v1/hazards/normalized/metric/{metricName}
     * Filters normalized hazard observations that contain a specific normalized metric.
     */
    @GetMapping("/metric/{metricName}")
    @Operation(summary = "Filter by specific normalized metric",
            description = "Retrieves hazard observations containing a specific normalized metric indicator.")
    public ResponseEntity<List<NormalizedHazardObservation>> getNormalizedHazardsByMetric(
            @Parameter(description = "Metric name (e.g. FLOOD_SEVERITY_INDEX, HOURLY_PRECIPITATION_MM)", example = "FLOOD_SEVERITY_INDEX")
            @PathVariable("metricName") String metricName,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        List<NormalizedHazardObservation> list = hazardNormalizationService.getAllNormalizedHazards(
                null, null, null, metricName, limit
        );
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/v1/hazards/normalized/district/{districtName}
     * Retrieves normalized hazard observations for a specific administrative district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(summary = "Get normalized hazards in district",
            description = "Retrieves normalized hazard indicators for observations within an administrative district.")
    public ResponseEntity<List<NormalizedHazardObservation>> getNormalizedHazardsInDistrict(
            @Parameter(description = "District name", example = "Sitamarhi")
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        List<NormalizedHazardObservation> list = hazardNormalizationService.getAllNormalizedHazards(
                type, null, districtName, null, limit
        );
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/v1/hazards/normalized/rainfall/daily
     * Retrieves normalized daily rainfall summaries for a weather station.
     */
    @GetMapping("/rainfall/daily")
    @Operation(summary = "Normalized daily rainfall feeds",
            description = "Retrieves normalized daily total rainfall and peak hourly precipitation metrics [0, 1].")
    public ResponseEntity<List<NormalizedDailyRainfall>> getNormalizedDailyRainfall(
            @Parameter(description = "Weather station name", example = "Patna")
            @RequestParam("stationName") String stationName,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<NormalizedDailyRainfall> list = hazardNormalizationService.getNormalizedDailyRainfall(stationName, startDate, endDate);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/v1/hazards/normalized/rainfall/rolling
     * Retrieves normalized multi-window rolling rainfall metrics (3h, 6h, 12h, 24h) at a target timestamp.
     */
    @GetMapping("/rainfall/rolling")
    @Operation(summary = "Normalized rolling rainfall metrics",
            description = "Retrieves normalized rolling 3h, 6h, 12h, and 24h rainfall accumulation metrics [0, 1].")
    public ResponseEntity<NormalizedRollingRainfall> getNormalizedRollingRainfall(
            @Parameter(description = "Weather station name", example = "Patna")
            @RequestParam("stationName") String stationName,
            @RequestParam("targetTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetTime) {
        NormalizedRollingRainfall metrics = hazardNormalizationService.getNormalizedRollingRainfall(stationName, targetTime);
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/v1/hazards/normalized/summary
     * Catalog summary describing all configured normalization reference ranges, directions, and metrics.
     */
    @GetMapping("/summary")
    @Operation(summary = "Normalization catalog summary",
            description = "Catalog summary describing all 12 scientific normalization reference ranges and clamping limits.")
    public ResponseEntity<NormalizationSummaryDto> getNormalizationSummary() {
        NormalizationSummaryDto summary = hazardNormalizationService.getNormalizationSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/v1/hazards/normalized/geojson
     * Delivers verified, spatially located normalized hazards as an RFC 7946 GeoJSON FeatureCollection.
     */
    @GetMapping("/geojson")
    @Operation(summary = "Normalized hazards GeoJSON vector feed",
            description = "Delivers spatially valid normalized hazard observations as standard RFC 7946 GeoJSON.")
    public ResponseEntity<GeoJsonFeatureCollectionDto> getNormalizedHazardsGeoJson(
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "limit", required = false, defaultValue = "100") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        GeoJsonFeatureCollectionDto geojson = hazardNormalizationService.getNormalizedHazardsGeoJson(type, district, limit);
        return ResponseEntity.ok(geojson);
    }
}
