package com.hazard.controller.hazard;

import com.hazard.dto.common.ApiResponse;
import com.hazard.dto.validation.GroundTruthEvent;
import com.hazard.dto.validation.ValidationReportDto;
import com.hazard.service.validation.HazardValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Stage 3.8 — Hazard Validation.
 * Exposes endpoints for ground-truth catalog inspection, data quality coverage analysis,
 * and the master empirical validation report.
 */
@RestController
@RequestMapping("/api/v1/hazards/validation")
@CrossOrigin(origins = "*")
@Tag(name = "Hazard Validation (Stage 3.8)",
     description = "Empirical validation of hazard scores against historical ground-truth disaster events")
public class HazardValidationController {

    private final HazardValidationService hazardValidationService;

    public HazardValidationController(HazardValidationService hazardValidationService) {
        this.hazardValidationService = hazardValidationService;
    }

    /**
     * GET /api/v1/hazards/validation/report
     * Generates and returns the complete Stage 3.8 Hazard Validation Report.
     */
    @GetMapping("/report")
    @Operation(
            summary = "Generate Hazard Validation Report",
            description = "Generates the complete Stage 3.8 empirical validation report, including " +
                    "data quality coverage, flood hazard score validation, extreme rainfall score validation, " +
                    "multi-hazard index assessment, and overall findings with calibration recommendations."
    )
    public ResponseEntity<ApiResponse<ValidationReportDto>> generateValidationReport() {
        ValidationReportDto report = hazardValidationService.generateValidationReport();
        ApiResponse<ValidationReportDto> response = ApiResponse.ok(report,
                "Stage 3.8 Hazard Validation Report generated successfully");
        response.addMeta("stage", "3.8");
        response.addMeta("substage", "Hazard Validation");
        response.addMeta("validationTargets", report.getValidationTargets().size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/hazards/validation/ground-truth
     * Returns the complete ground-truth catalog with provenance and usability metadata.
     */
    @GetMapping("/ground-truth")
    @Operation(
            summary = "Retrieve Ground-Truth Catalog",
            description = "Returns the complete ground-truth catalog constructed from DFO flood events " +
                    "and EM-DAT records, including geometric precision classification, temporal precision, " +
                    "usability flags, and exclusion reasons for each record."
    )
    public ResponseEntity<ApiResponse<List<GroundTruthEvent>>> getGroundTruthCatalog() {
        List<GroundTruthEvent> catalog = hazardValidationService.buildGroundTruthCatalog();
        ApiResponse<List<GroundTruthEvent>> response = ApiResponse.ok(catalog,
                "Ground-truth catalog retrieved successfully");
        response.addMeta("totalRecords", catalog.size());
        response.addMeta("dfoEvents", catalog.stream().filter(g -> "DFO".equals(g.getSource())).count());
        response.addMeta("emdatRecords", catalog.stream().filter(g -> "EMDAT".equals(g.getSource())).count());
        response.addMeta("usableForSpatialValidation",
                catalog.stream().filter(GroundTruthEvent::isUsableForSpatialValidation).count());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/hazards/validation/coverage
     * Returns the data quality coverage analysis only.
     */
    @GetMapping("/coverage")
    @Operation(
            summary = "Data Quality Coverage Report",
            description = "Returns the data quality coverage analysis summarizing the availability " +
                    "and usability of ground-truth datasets (DFO events, EM-DAT records, weather stations), " +
                    "including temporal overlap assessment and exclusion documentation."
    )
    public ResponseEntity<ApiResponse<ValidationReportDto.DataQualityCoverageDto>> getDataQualityCoverage() {
        ValidationReportDto report = hazardValidationService.generateValidationReport();
        ApiResponse<ValidationReportDto.DataQualityCoverageDto> response = ApiResponse.ok(
                report.getDataQualityCoverage(),
                "Data quality coverage analysis completed");
        response.addMeta("stage", "3.8");
        return ResponseEntity.ok(response);
    }
}
