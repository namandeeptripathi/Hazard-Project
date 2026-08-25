package com.hazard.controller.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.scoring.DailyRainfallScoreDto;
import com.hazard.dto.scoring.HazardScoreDto;
import com.hazard.dto.scoring.HazardScoringSummaryDto;
import com.hazard.dto.scoring.RollingRainfallScoreDto;
import com.hazard.service.scoring.HazardScoringService;
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
 * REST Controller for Sub-Stage 3.4 Hazard Scoring.
 * Exposes single-hazard scores, severity tier classifications,
 * metric contribution breakdowns, and GeoJSON vector feeds.
 */
@RestController
@RequestMapping("/api/v1/hazards/scores")
@CrossOrigin(origins = "*")
@Tag(name = "Hazard Scoring", description = "Single-hazard intensity scores and severity tiers (Stage 3.4)")
public class HazardScoringController {

    private final HazardScoringService hazardScoringService;

    public HazardScoringController(HazardScoringService hazardScoringService) {
        this.hazardScoringService = hazardScoringService;
    }

    /**
     * GET /api/v1/hazards/scores
     * Lists single-hazard scores with optional filtering.
     */
    @GetMapping
    @Operation(summary = "List single-hazard scores",
            description = "Retrieves weighted single-hazard composite scores in [0.0000, 1.0000] and severity tiers.")
    public ResponseEntity<List<HazardScoreDto>> getAllHazardScores(
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "severity", required = false) String severityStr,
            @RequestParam(name = "quality", required = false) String qualityStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        SeverityTier severity = (severityStr != null && !severityStr.trim().isEmpty()) ? SeverityTier.fromString(severityStr) : null;
        QualityStatus quality = (qualityStr != null && !qualityStr.trim().isEmpty()) ? QualityStatus.fromString(qualityStr) : null;

        List<HazardScoreDto> scores = hazardScoringService.getAllHazardScores(type, district, severity, quality, limit);
        return ResponseEntity.ok(scores);
    }

    /**
     * GET /api/v1/hazards/scores/{id}
     * Retrieves a single hazard score observation by unified ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get single-hazard score by ID",
            description = "Retrieves a scored hazard observation with complete metric contribution breakdown and explanation.")
    public ResponseEntity<HazardScoreDto> getHazardScoreById(
            @Parameter(description = "Unified hazard ID", example = "DFO-3")
            @PathVariable("id") String id) {
        HazardScoreDto score = hazardScoringService.getHazardScoreById(id);
        return ResponseEntity.ok(score);
    }

    /**
     * GET /api/v1/hazards/scores/type/{type}
     * Retrieves hazard scores filtered by hazard type (FLOOD, EXTREME_RAINFALL).
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get scores by hazard type",
            description = "Retrieves hazard scores filtered by hazard domain type (FLOOD or EXTREME_RAINFALL).")
    public ResponseEntity<List<HazardScoreDto>> getHazardScoresByType(
            @Parameter(description = "Hazard type", example = "FLOOD")
            @PathVariable("type") String typeStr,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = HazardType.fromString(typeStr);
        List<HazardScoreDto> scores = hazardScoringService.getHazardScoresByType(type, district, limit);
        return ResponseEntity.ok(scores);
    }

    /**
     * GET /api/v1/hazards/scores/district/{districtName}
     * Retrieves single-hazard scores for a specific administrative district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(summary = "Get hazard scores in district",
            description = "Retrieves single-hazard scores located within a specific administrative district.")
    public ResponseEntity<List<HazardScoreDto>> getHazardScoresInDistrict(
            @Parameter(description = "District name", example = "Sitamarhi")
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "severity", required = false) String severityStr,
            @RequestParam(name = "limit", required = false, defaultValue = "50") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        SeverityTier severity = (severityStr != null && !severityStr.trim().isEmpty()) ? SeverityTier.fromString(severityStr) : null;

        List<HazardScoreDto> scores = hazardScoringService.getAllHazardScores(type, districtName, severity, null, limit);
        return ResponseEntity.ok(scores);
    }

    /**
     * GET /api/v1/hazards/scores/rainfall/daily
     * Retrieves scored daily rainfall observations for a station.
     */
    @GetMapping("/rainfall/daily")
    @Operation(summary = "Scored daily rainfall observations",
            description = "Retrieves scored daily total and peak rainfall hazard intensity metrics.")
    public ResponseEntity<List<DailyRainfallScoreDto>> getScoredDailyRainfall(
            @Parameter(description = "Weather station name", example = "Patna")
            @RequestParam("stationName") String stationName,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyRainfallScoreDto> scores = hazardScoringService.getScoredDailyRainfall(stationName, startDate, endDate);
        return ResponseEntity.ok(scores);
    }

    /**
     * GET /api/v1/hazards/scores/rainfall/rolling
     * Retrieves scored multi-window rolling rainfall observation at a target timestamp.
     */
    @GetMapping("/rainfall/rolling")
    @Operation(summary = "Scored rolling rainfall accumulation",
            description = "Retrieves scored rolling accumulation metrics combining 3h, 6h, 12h, and 24h storm windows.")
    public ResponseEntity<RollingRainfallScoreDto> getScoredRollingRainfall(
            @Parameter(description = "Weather station name", example = "Patna")
            @RequestParam("stationName") String stationName,
            @RequestParam("targetTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetTime) {
        RollingRainfallScoreDto score = hazardScoringService.getScoredRollingRainfall(stationName, targetTime);
        return ResponseEntity.ok(score);
    }

    /**
     * GET /api/v1/hazards/scores/summary
     * Catalog summary describing hazard score distributions, severity tiers, and active configurations.
     */
    @GetMapping("/summary")
    @Operation(summary = "Scoring catalog summary",
            description = "Executive catalog summary describing score and severity tier distributions and active weight schemes.")
    public ResponseEntity<HazardScoringSummaryDto> getHazardScoringSummary() {
        HazardScoringSummaryDto summary = hazardScoringService.getHazardScoringSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/v1/hazards/scores/geojson
     * Delivers scored hazard observations as an RFC 7946 GeoJSON FeatureCollection.
     */
    @GetMapping("/geojson")
    @Operation(summary = "Scored hazards GeoJSON vector feed",
            description = "Delivers scored hazard observations as RFC 7946 GeoJSON with hazardScore and severityTier properties.")
    public ResponseEntity<GeoJsonFeatureCollectionDto> getHazardScoresGeoJson(
            @RequestParam(name = "type", required = false) String typeStr,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "limit", required = false, defaultValue = "100") Integer limit) {
        HazardType type = (typeStr != null && !typeStr.trim().isEmpty()) ? HazardType.fromString(typeStr) : null;
        GeoJsonFeatureCollectionDto geojson = hazardScoringService.getHazardScoresGeoJson(type, district, limit);
        return ResponseEntity.ok(geojson);
    }
}
