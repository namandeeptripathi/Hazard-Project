package com.hazard.controller.historical;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.historical.HistoricalTimeWindow;
import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.historical.*;
import com.hazard.service.historical.HistoricalDisasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Stage 4.6 — Historical Disaster Intelligence.
 *
 * Exposes endpoints for empirical disaster frequency, recurrence intervals,
 * severity statistics, seasonal distributions, chronological timelines, and hotspot mappings.
 */
@RestController
@RequestMapping("/api/v1/historical")
@CrossOrigin(origins = "*")
@Tag(name = "Historical Disaster Intelligence (Stage 4.6)",
     description = "Evaluates empirical disaster frequency, recurrence intervals, severity statistics, timelines, and hotspots")
public class HistoricalDisasterController {

    private final HistoricalDisasterService historicalDisasterService;

    public HistoricalDisasterController(HistoricalDisasterService historicalDisasterService) {
        this.historicalDisasterService = historicalDisasterService;
    }

    /**
     * GET /api/v1/historical/district/{districtName}
     * Returns comprehensive historical disaster intelligence for an administrative district.
     */
    @GetMapping("/district/{districtName}")
    @Operation(
            summary = "District Historical Disaster Intelligence",
            description = "Returns empirical event count, events per year, recurrence intervals, severity metrics, and seasonal patterns."
    )
    public ResponseEntity<ApiResponse<DistrictHistoricalSummaryDto>> getDistrictHistoricalSummary(
            @Parameter(description = "Name of administrative district e.g. Sitamarhi, Patna, Muzaffarpur", example = "Sitamarhi")
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "window", required = false) HistoricalTimeWindow window,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "hazardType", required = false) HazardType hazardType) {

        DistrictHistoricalSummaryDto summary = historicalDisasterService.getDistrictHistoricalSummary(districtName, window, startDate, endDate, hazardType);
        ApiResponse<DistrictHistoricalSummaryDto> response = ApiResponse.ok(summary,
                "Historical disaster intelligence evaluated successfully for: " + districtName);
        response.addMeta("stage", "4.6");
        response.addMeta("substage", "Historical Disaster Intelligence");
        response.addMeta("totalHistoricalEvents", summary.getTotalHistoricalEvents());
        response.addMeta("eventsPerYear", summary.getEventsPerYear());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/historical/district/{districtName}/timeline
     * Returns chronological timeline of historical disaster events in the district.
     */
    @GetMapping("/district/{districtName}/timeline")
    @Operation(
            summary = "District Historical Disaster Timeline",
            description = "Returns historical disaster events ordered chronologically descending."
    )
    public ResponseEntity<ApiResponse<List<HistoricalEventDto>>> getDistrictTimeline(
            @PathVariable("districtName") String districtName,
            @RequestParam(name = "hazardType", required = false) HazardType hazardType) {

        List<HistoricalEventDto> timeline = historicalDisasterService.getDistrictTimeline(districtName, hazardType);
        ApiResponse<List<HistoricalEventDto>> response = ApiResponse.ok(timeline,
                "Historical disaster timeline retrieved successfully for: " + districtName);
        response.addMeta("stage", "4.6");
        response.addMeta("count", timeline.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/historical/district/{districtName}/hazard/{hazardType}
     * Returns historical statistics filtered by a specific hazard type.
     */
    @GetMapping("/district/{districtName}/hazard/{hazardType}")
    @Operation(
            summary = "Historical Intelligence by Hazard Type",
            description = "Returns historical disaster metrics filtered by hazard type (e.g. FLOOD, EXTREME_RAINFALL)."
    )
    public ResponseEntity<ApiResponse<DistrictHistoricalSummaryDto>> getHistoricalByHazardType(
            @PathVariable("districtName") String districtName,
            @PathVariable("hazardType") HazardType hazardType) {

        DistrictHistoricalSummaryDto summary = historicalDisasterService.getDistrictHistoricalSummary(
                districtName, HistoricalTimeWindow.ALL_HISTORY, null, null, hazardType);
        ApiResponse<DistrictHistoricalSummaryDto> response = ApiResponse.ok(summary,
                "Historical disaster intelligence for " + hazardType + " retrieved successfully for: " + districtName);
        response.addMeta("stage", "4.6");
        response.addMeta("hazardType", hazardType.name());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/historical/all
     * Returns historical disaster summaries across all 38 districts of Bihar.
     */
    @GetMapping("/all")
    @Operation(
            summary = "All Districts Historical Summaries",
            description = "Returns empirical historical disaster summaries across all 38 administrative districts of Bihar."
    )
    public ResponseEntity<ApiResponse<List<DistrictHistoricalSummaryDto>>> getAllDistrictsHistoricalSummaries(
            @RequestParam(name = "window", required = false) HistoricalTimeWindow window) {

        List<DistrictHistoricalSummaryDto> list = historicalDisasterService.getAllDistrictsHistoricalSummaries(window);
        ApiResponse<List<DistrictHistoricalSummaryDto>> response = ApiResponse.ok(list,
                "All districts historical disaster summaries retrieved successfully");
        response.addMeta("stage", "4.6");
        response.addMeta("totalDistricts", list.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/historical/hotspots
     * Returns ranked list of empirical historical disaster hotspots.
     */
    @GetMapping("/hotspots")
    @Operation(
            summary = "Empirical Historical Hotspots",
            description = "Ranks districts by historical disaster frequency, severity, and empirical recurrence."
    )
    public ResponseEntity<ApiResponse<List<HistoricalHotspotDto>>> getHistoricalHotspots(
            @RequestParam(name = "window", required = false) HistoricalTimeWindow window,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {

        List<HistoricalHotspotDto> hotspots = historicalDisasterService.getHistoricalHotspots(window, limit);
        ApiResponse<List<HistoricalHotspotDto>> response = ApiResponse.ok(hotspots,
                "Empirical historical disaster hotspots retrieved successfully");
        response.addMeta("stage", "4.6");
        response.addMeta("count", hotspots.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/historical/geojson
     * Returns RFC 7946 GeoJSON FeatureCollection of historical disaster points.
     */
    @GetMapping("/geojson")
    @Operation(
            summary = "Historical Disaster Points GeoJSON",
            description = "Returns RFC 7946 GeoJSON Point features for historical disaster events with severity and impact properties."
    )
    public ResponseEntity<GeoJsonFeatureCollectionDto> getHistoricalGeoJson(
            @RequestParam(name = "window", required = false) HistoricalTimeWindow window) {

        GeoJsonFeatureCollectionDto geojson = historicalDisasterService.generateHistoricalGeoJson(window);
        return ResponseEntity.ok(geojson);
    }

    /**
     * GET /api/v1/historical/config
     * Returns active historical analysis configuration and thresholds.
     */
    @GetMapping("/config")
    @Operation(
            summary = "Historical Analysis Configuration",
            description = "Returns supported hazard types, default time windows, and hotspot calculation parameters."
    )
    public ResponseEntity<ApiResponse<HistoricalConfigDto>> getHistoricalConfig() {
        HistoricalConfigDto configDto = historicalDisasterService.getHistoricalConfig();
        ApiResponse<HistoricalConfigDto> response = ApiResponse.ok(configDto,
                "Historical analysis configuration retrieved successfully");
        response.addMeta("stage", "4.6");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/historical/analyze
     * Custom historical disaster analysis with flexible filters.
     */
    @PostMapping("/analyze")
    @Operation(
            summary = "Custom Historical Disaster Query",
            description = "Analyzes historical disasters with custom date ranges, hazard types, and severity thresholds."
    )
    public ResponseEntity<ApiResponse<DistrictHistoricalSummaryDto>> analyzeHistoricalDisasters(
            @RequestBody HistoricalAnalysisRequestDto request) {

        String district = request.getDistrictName() != null ? request.getDistrictName() : "Sitamarhi";
        DistrictHistoricalSummaryDto summary = historicalDisasterService.getDistrictHistoricalSummary(
                district,
                request.getTimeWindow(),
                request.getStartDate(),
                request.getEndDate(),
                request.getHazardType()
        );
        ApiResponse<DistrictHistoricalSummaryDto> response = ApiResponse.ok(summary,
                "Custom historical analysis completed successfully");
        response.addMeta("stage", "4.6");
        return ResponseEntity.ok(response);
    }
}
