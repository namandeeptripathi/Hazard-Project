package com.hazard.controller.hazard;

import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.facade.DistrictHazardOverviewDto;
import com.hazard.dto.facade.HazardSystemHealthDto;
import com.hazard.service.facade.HazardApiFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Hazard Subsystem Health and Consolidated District Overview.
 */
@RestController
@RequestMapping("/api/v1/hazards")
@CrossOrigin(origins = "*")
@Tag(name = "System & Health", description = "Subsystem health checks and consolidated district hazard overviews")
public class HazardHealthController {

    private final HazardApiFacade hazardApiFacade;

    public HazardHealthController(HazardApiFacade hazardApiFacade) {
        this.hazardApiFacade = hazardApiFacade;
    }

    /**
     * GET /api/v1/hazards/health
     * Returns the operational health status and active Stage 3 capabilities.
     */
    @GetMapping("/health")
    @Operation(summary = "Hazard Intelligence Subsystem Health Check",
            description = "Returns operational readiness, database connectivity, and active Stage 3 capability status.")
    public ResponseEntity<ApiResponse<HazardSystemHealthDto>> getHealthStatus() {
        HazardSystemHealthDto health = hazardApiFacade.getSystemHealthOverview();
        return ResponseEntity.ok(ApiResponse.ok(health, "Hazard Intelligence Subsystem is fully operational"));
    }

    /**
     * GET /api/v1/hazards/overview/district/{districtName}
     * Returns a consolidated multi-stage hazard intelligence profile for an administrative district.
     */
    @GetMapping("/overview/district/{districtName}")
    @Operation(summary = "Consolidated District Hazard Profile",
            description = "Aggregates flood scores, rainfall scores, multi-hazard index, severity tier, and river networks for a district.")
    public ResponseEntity<ApiResponse<DistrictHazardOverviewDto>> getDistrictHazardOverview(
            @Parameter(description = "Bihar Administrative District Name (e.g. Patna, Sitamarhi)", example = "Patna")
            @PathVariable("districtName") String districtName) {
        DistrictHazardOverviewDto overview = hazardApiFacade.getDistrictHazardIntelligence(districtName);
        return ResponseEntity.ok(ApiResponse.ok(overview, "District hazard overview compiled successfully for " + districtName));
    }
}
