package com.hazard.controller.risk;

import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.risk.config.RiskConfigDiffDto;
import com.hazard.dto.risk.config.RiskConfigurationRequestDto;
import com.hazard.dto.risk.config.RiskConfigurationResponseDto;
import com.hazard.service.risk.config.RiskConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Stage 4.8 — Configurable Risk Weights.
 * Exposes endpoints for managing, activating, versioning, diffing, and auditing risk configuration profiles.
 */
@RestController
@RequestMapping("/api/v1/risk/config")
@CrossOrigin(origins = "*")
@Tag(name = "Risk Configuration Management (Stage 4.8)",
     description = "Manages versioned, auditable, and dynamically configurable risk weights, exposure sub-weights, and tier thresholds")
public class RiskConfigurationController {

    private final RiskConfigurationService riskConfigurationService;

    public RiskConfigurationController(RiskConfigurationService riskConfigurationService) {
        this.riskConfigurationService = riskConfigurationService;
    }

    /**
     * GET /api/v1/risk/config
     * Returns the currently active production risk configuration profile.
     */
    @GetMapping
    @Operation(
            summary = "Get Active Risk Configuration",
            description = "Returns the currently active production risk configuration profile including configured and normalized weights."
    )
    public ResponseEntity<ApiResponse<RiskConfigurationResponseDto>> getActiveConfiguration() {
        RiskConfigurationResponseDto dto = riskConfigurationService.getActiveConfigurationDto();
        ApiResponse<RiskConfigurationResponseDto> response = ApiResponse.ok(dto, "Active risk configuration retrieved successfully");
        response.addMeta("stage", "4.8");
        response.addMeta("substage", "Configurable Risk Weights");
        response.addMeta("configId", dto.getConfigId());
        response.addMeta("version", dto.getVersion());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/config/all
     * Returns all stored risk configuration profiles (active, inactive, presets).
     */
    @GetMapping("/all")
    @Operation(
            summary = "Get All Risk Configurations",
            description = "Lists all available risk configuration profiles and versions."
    )
    public ResponseEntity<ApiResponse<List<RiskConfigurationResponseDto>>> getAllConfigurations() {
        List<RiskConfigurationResponseDto> list = riskConfigurationService.getAllConfigurations();
        ApiResponse<List<RiskConfigurationResponseDto>> response = ApiResponse.ok(list, "All risk configurations retrieved successfully");
        response.addMeta("stage", "4.8");
        response.addMeta("count", list.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/config/presets
     * Returns standard preset configuration templates.
     */
    @GetMapping("/presets")
    @Operation(
            summary = "Get Configuration Presets",
            description = "Returns domain presets such as DEFAULT, HAZARD_FOCUSED, POPULATION_FOCUSED, and INFRASTRUCTURE_FOCUSED."
    )
    public ResponseEntity<ApiResponse<List<RiskConfigurationResponseDto>>> getPresets() {
        List<RiskConfigurationResponseDto> presets = riskConfigurationService.getPresets();
        ApiResponse<List<RiskConfigurationResponseDto>> response = ApiResponse.ok(presets, "Configuration presets retrieved successfully");
        response.addMeta("stage", "4.8");
        response.addMeta("count", presets.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/config/{configId}
     * Returns details for a specific configuration profile.
     */
    @GetMapping("/{configId}")
    @Operation(
            summary = "Get Configuration By ID",
            description = "Returns a specific risk configuration profile by its ID (e.g. risk-v1, risk-preset-hazard)."
    )
    public ResponseEntity<ApiResponse<RiskConfigurationResponseDto>> getConfigurationById(
            @PathVariable("configId") String configId) {

        RiskConfigurationResponseDto dto = riskConfigurationService.getConfigurationDtoById(configId);
        ApiResponse<RiskConfigurationResponseDto> response = ApiResponse.ok(dto, "Risk configuration retrieved successfully: " + configId);
        response.addMeta("stage", "4.8");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/risk/config
     * Creates a new risk configuration profile.
     */
    @PostMapping
    @Operation(
            summary = "Create Risk Configuration",
            description = "Creates and validates a new risk configuration profile with custom weights and thresholds."
    )
    public ResponseEntity<ApiResponse<RiskConfigurationResponseDto>> createConfiguration(
            @RequestBody RiskConfigurationRequestDto request) {

        RiskConfigurationResponseDto dto = riskConfigurationService.createConfiguration(request);
        ApiResponse<RiskConfigurationResponseDto> response = ApiResponse.ok(dto, "New risk configuration created successfully: " + dto.getConfigId());
        response.addMeta("stage", "4.8");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/risk/config/{configId}
     * Updates an existing draft profile or branches immutable/active configurations into a new version.
     */
    @PutMapping("/{configId}")
    @Operation(
            summary = "Update or Branch Configuration",
            description = "Updates a draft profile or automatically branches immutable configurations into a new version."
    )
    public ResponseEntity<ApiResponse<RiskConfigurationResponseDto>> updateConfiguration(
            @PathVariable("configId") String configId,
            @RequestBody RiskConfigurationRequestDto request) {

        RiskConfigurationResponseDto dto = riskConfigurationService.updateConfiguration(configId, request);
        ApiResponse<RiskConfigurationResponseDto> response = ApiResponse.ok(dto, "Risk configuration processed successfully: " + dto.getConfigId());
        response.addMeta("stage", "4.8");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/risk/config/{configId}/activate
     * Transactionally activates a configuration profile as the primary production risk model.
     */
    @PostMapping("/{configId}/activate")
    @Operation(
            summary = "Activate Configuration Profile",
            description = "Activates a configuration profile for production risk calculations and deactivates prior configurations."
    )
    public ResponseEntity<ApiResponse<RiskConfigurationResponseDto>> activateConfiguration(
            @PathVariable("configId") String configId,
            @RequestParam(name = "actor", required = false) String actor) {

        RiskConfigurationResponseDto dto = riskConfigurationService.activateConfiguration(configId, actor);
        ApiResponse<RiskConfigurationResponseDto> response = ApiResponse.ok(dto, "Configuration profile activated successfully: " + configId);
        response.addMeta("stage", "4.8");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/risk/config/{configId}/deactivate
     * Deactivates a configuration profile with automatic safe fallback to default baseline.
     */
    @PostMapping("/{configId}/deactivate")
    @Operation(
            summary = "Deactivate Configuration Profile",
            description = "Deactivates a configuration profile, falling back safely to the default baseline if no active configuration remains."
    )
    public ResponseEntity<ApiResponse<RiskConfigurationResponseDto>> deactivateConfiguration(
            @PathVariable("configId") String configId,
            @RequestParam(name = "actor", required = false) String actor) {

        RiskConfigurationResponseDto dto = riskConfigurationService.deactivateConfiguration(configId, actor);
        ApiResponse<RiskConfigurationResponseDto> response = ApiResponse.ok(dto, "Configuration profile deactivated: " + configId);
        response.addMeta("stage", "4.8");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/config/diff
     * Compares two configuration versions and returns structured differences.
     */
    @GetMapping("/diff")
    @Operation(
            summary = "Compare Configuration Versions (Diff)",
            description = "Compares two configuration versions and returns structured numeric and delta differences."
    )
    public ResponseEntity<ApiResponse<RiskConfigDiffDto>> compareConfigurations(
            @Parameter(description = "Base Configuration ID", example = "risk-v1")
            @RequestParam("base") String baseConfigId,
            @Parameter(description = "Target Configuration ID", example = "risk-preset-hazard")
            @RequestParam("target") String targetConfigId) {

        RiskConfigDiffDto diff = riskConfigurationService.compareConfigurations(baseConfigId, targetConfigId);
        ApiResponse<RiskConfigDiffDto> response = ApiResponse.ok(diff, "Configuration diff computed successfully");
        response.addMeta("stage", "4.8");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/risk/config/audit-logs
     * Returns configuration lifecycle audit history.
     */
    @GetMapping("/audit-logs")
    @Operation(
            summary = "Get Configuration Audit Trail",
            description = "Returns audit log entries of configuration creation, activation, and modification events."
    )
    public ResponseEntity<ApiResponse<List<com.hazard.domain.risk.config.RiskConfigAuditEntry>>> getAuditLogs() {
        var logs = riskConfigurationService.getAuditLogs();
        ApiResponse<List<com.hazard.domain.risk.config.RiskConfigAuditEntry>> response = ApiResponse.ok(logs, "Audit logs retrieved successfully");
        response.addMeta("stage", "4.8");
        response.addMeta("count", logs.size());
        return ResponseEntity.ok(response);
    }
}
