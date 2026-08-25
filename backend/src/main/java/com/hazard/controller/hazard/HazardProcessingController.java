package com.hazard.controller.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.processing.DailyRainfallSummary;
import com.hazard.dto.processing.ProcessedHazardObservation;
import com.hazard.dto.processing.ProcessingQualitySummaryDto;
import com.hazard.dto.processing.RollingRainfallMetrics;
import com.hazard.service.processing.HazardProcessingService;
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
 * REST Controller for Sub-Stage 3.2 Hazard Processing.
 * Exposes analysis-ready hazard observations, daily rainfall aggregations,
 * rolling accumulation metrics, and data quality reports.
 */
@RestController
@RequestMapping("/api/v1/hazards/processed")
@CrossOrigin(origins = "*")
@Tag(name = "Hazard Processing", description = "Analysis-ready processed hazards and temporal aggregations (Stage 3.2)")
public class HazardProcessingController {

    private final HazardProcessingService hazardProcessingService;

    public HazardProcessingController(HazardProcessingService hazardProcessingService) {
        this.hazardProcessingService = hazardProcessingService;
    }

    /**
     * GET /api/v1/hazards/processed
     * Lists analysis-ready processed hazard observations.
     */
    @GetMapping
    @Operation(summary = "List processed hazard observations",
            description = "Retrieves cleaned, spatially associated hazard records with optional type, quality, and district filters.")
    public ResponseEntity<List<ProcessedHazardObservation>> getAllProcessedHazards(
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "quality", required = false) String qualityStr,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        QualityStatus quality = (qualityStr != null && !qualityStr.trim().isEmpty()) ? QualityStatus.fromString(qualityStr) : null;
        List<ProcessedHazardObservation> list = hazardProcessingService.getAllProcessedHazards(type, quality, district, limit);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/v1/hazards/processed/{id}
     * Retrieves a single processed hazard observation by unified ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get processed hazard by ID",
            description = "Retrieves an individual processed hazard observation by unified ID.")
    public ResponseEntity<ProcessedHazardObservation> getProcessedHazardById(
            @Parameter(description = "Unified hazard ID", example = "DFO-3")
            @PathVariable("id") String id) {
        ProcessedHazardObservation obs = hazardProcessingService.getProcessedHazardById(id);
        return ResponseEntity.ok(obs);
    }

    /**
     * GET /api/v1/hazards/processed/quality/{status}
     * Filters processed hazard observations by quality status (VALID, UNLOCATED, PARTIAL, INVALID).
     */
    @GetMapping("/quality/{status}")
    @Operation(summary = "Filter processed hazards by quality status",
            description = "Retrieves processed hazards matching a quality status (VALID, UNLOCATED, PARTIAL, INVALID).")
    public ResponseEntity<List<ProcessedHazardObservation>> getProcessedHazardsByQuality(
            @Parameter(description = "Quality status", example = "VALID")
            @PathVariable("status") String statusStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        QualityStatus quality = QualityStatus.fromString(statusStr);
        List<ProcessedHazardObservation> list = hazardProcessingService.getAllProcessedHazards(null, quality, null, limit);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/v1/hazards/processed/district/{districtName}
     * Retrieves processed hazard observations for a specific administrative district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(summary = "Get processed hazards in district",
            description = "Retrieves processed hazards located within a specific administrative district.")
    public ResponseEntity<List<ProcessedHazardObservation>> getProcessedHazardsInDistrict(
            @Parameter(description = "District name", example = "Patna")
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        List<ProcessedHazardObservation> list = hazardProcessingService.getProcessedHazardsInDistrict(districtName, type, limit);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/v1/hazards/processed/rainfall/daily
     * Retrieves aggregated daily rainfall summaries for a weather station.
     */
    @GetMapping("/rainfall/daily")
    @Operation(summary = "Daily meteorological rainfall aggregation",
            description = "Computes diurnal total accumulation, peak hourly precipitation, and storm duration.")
    public ResponseEntity<List<DailyRainfallSummary>> getDailyRainfallSummaries(
            @Parameter(description = "Weather station name", example = "Patna")
            @RequestParam("stationName") String stationName,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyRainfallSummary> summaries = hazardProcessingService.getDailyRainfallSummaries(stationName, startDate, endDate);
        return ResponseEntity.ok(summaries);
    }

    /**
     * GET /api/v1/hazards/processed/rainfall/rolling
     * Retrieves multi-window rolling rainfall accumulation metrics (3h, 6h, 12h, 24h) at a target timestamp.
     */
    @GetMapping("/rainfall/rolling")
    @Operation(summary = "Multi-window rolling rainfall accumulation",
            description = "Computes rolling 3h, 6h, 12h, and 24h rainfall accumulation preceding a target timestamp.")
    public ResponseEntity<RollingRainfallMetrics> getRollingRainfallMetrics(
            @Parameter(description = "Weather station name", example = "Patna")
            @RequestParam("stationName") String stationName,
            @RequestParam("targetTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetTime) {
        RollingRainfallMetrics metrics = hazardProcessingService.getRollingRainfallMetrics(stationName, targetTime);
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/v1/hazards/processed/quality-summary
     * Executive processing and data quality summary detailing cleaning actions and valid counts.
     */
    @GetMapping("/quality-summary")
    @Operation(summary = "Processing and data quality summary",
            description = "Provides counts of valid, unlocated, and cleaned records, alongside applied data sanitization rules.")
    public ResponseEntity<ProcessingQualitySummaryDto> getProcessingQualitySummary() {
        ProcessingQualitySummaryDto summary = hazardProcessingService.getProcessingQualitySummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/v1/hazards/processed/geojson
     * Standard GeoJSON FeatureCollection of verified, spatially located processed hazards.
     */
    @GetMapping("/geojson")
    @Operation(summary = "Processed hazards GeoJSON vector feed",
            description = "Delivers spatially valid processed hazard observations as RFC 7946 GeoJSON.")
    public ResponseEntity<GeoJsonFeatureCollectionDto> getProcessedHazardsGeoJson(
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "limit", required = false, defaultValue = "100") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        GeoJsonFeatureCollectionDto geojson = hazardProcessingService.getProcessedHazardsGeoJson(type, district, limit);
        return ResponseEntity.ok(geojson);
    }
}
