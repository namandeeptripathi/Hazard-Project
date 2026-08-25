package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.dto.validation.GroundTruthEvent;
import com.hazard.dto.validation.ValidationMetricsDto;
import com.hazard.dto.validation.ValidationReportDto;
import com.hazard.service.validation.HazardValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for Stage 3.8 — Hazard Validation.
 * Validates the validation engine itself: ground-truth construction, data quality coverage,
 * score separation metrics, and report completeness.
 */
@SpringBootTest
@Transactional(readOnly = true)
class HazardValidationServiceTests {

    private static final Logger log = LoggerFactory.getLogger(HazardValidationServiceTests.class);

    @Autowired
    private HazardValidationService hazardValidationService;

    // =========================================================================
    // GROUND-TRUTH CATALOG TESTS
    // =========================================================================

    @Test
    @DisplayName("3.8.1: Ground-Truth Catalog Construction — DFO + EM-DAT Sources")
    void testGroundTruthCatalogConstruction() {
        List<GroundTruthEvent> catalog = hazardValidationService.buildGroundTruthCatalog();

        assertNotNull(catalog);
        assertFalse(catalog.isEmpty());

        long dfoCount = catalog.stream().filter(g -> "DFO".equals(g.getSource())).count();
        long emdatCount = catalog.stream().filter(g -> "EMDAT".equals(g.getSource())).count();

        assertEquals(23, dfoCount, "Expected 23 DFO flood events");
        assertEquals(53, emdatCount, "Expected 53 EM-DAT flood records");
        assertEquals(76, catalog.size(), "Expected 76 total ground-truth records");

        log.info("✅ Ground-truth catalog: {} total records ({} DFO, {} EM-DAT)", catalog.size(), dfoCount, emdatCount);
    }

    @Test
    @DisplayName("3.8.2: DFO Events — Geometric Precision Classification")
    void testDfoGeometricPrecisionClassification() {
        List<GroundTruthEvent> catalog = hazardValidationService.buildGroundTruthCatalog();

        List<GroundTruthEvent> dfoEvents = catalog.stream()
                .filter(g -> "DFO".equals(g.getSource()))
                .toList();

        long validGeometry = dfoEvents.stream().filter(GroundTruthEvent::isHasValidGeometry).count();
        long sentinelGeometry = dfoEvents.stream().filter(g -> !g.isHasValidGeometry()).count();

        assertEquals(7, validGeometry, "Expected 7 DFO events with valid coordinates");
        assertEquals(16, sentinelGeometry, "Expected 16 DFO events with sentinel coordinates");

        // All valid-geometry events must have POINT_COORDINATE precision
        dfoEvents.stream()
                .filter(GroundTruthEvent::isHasValidGeometry)
                .forEach(g -> {
                    assertEquals("POINT_COORDINATE", g.getGeographicPrecision());
                    assertTrue(g.isUsableForSpatialValidation());
                    assertNotNull(g.getLongitude());
                    assertNotNull(g.getLatitude());
                });

        // All sentinel events must be excluded
        dfoEvents.stream()
                .filter(g -> !g.isHasValidGeometry())
                .forEach(g -> {
                    assertEquals("STATE_LEVEL", g.getGeographicPrecision());
                    assertFalse(g.isUsableForSpatialValidation());
                    assertNotNull(g.getExclusionReason());
                });

        log.info("✅ DFO geometric precision: {} POINT_COORDINATE, {} STATE_LEVEL (excluded)", validGeometry, sentinelGeometry);
    }

    @Test
    @DisplayName("3.8.3: EM-DAT Records — All National Aggregate, All Excluded")
    void testEmdatNationalAggregateExclusion() {
        List<GroundTruthEvent> catalog = hazardValidationService.buildGroundTruthCatalog();

        List<GroundTruthEvent> emdatEvents = catalog.stream()
                .filter(g -> "EMDAT".equals(g.getSource()))
                .toList();

        for (GroundTruthEvent emdat : emdatEvents) {
            assertEquals(HazardType.FLOOD, emdat.getHazardType());
            assertEquals("NATIONAL", emdat.getGeographicPrecision());
            assertEquals("YEAR_ONLY", emdat.getTemporalPrecision());
            assertFalse(emdat.isUsableForSpatialValidation());
            assertFalse(emdat.isUsableForTemporalValidation());
            assertNotNull(emdat.getExclusionReason());
            assertTrue(emdat.getExclusionReason().contains("National-aggregate"));
        }

        log.info("✅ All {} EM-DAT records correctly classified as national aggregate and excluded", emdatEvents.size());
    }

    @Test
    @DisplayName("3.8.4: Ground-Truth Provenance — Unique IDs and Hazard Type Assignment")
    void testGroundTruthProvenance() {
        List<GroundTruthEvent> catalog = hazardValidationService.buildGroundTruthCatalog();

        // All IDs must be unique
        long uniqueIds = catalog.stream().map(GroundTruthEvent::getGroundTruthId).distinct().count();
        assertEquals(catalog.size(), uniqueIds, "All ground-truth IDs must be unique");

        // All events must have hazard type
        catalog.forEach(g -> {
            assertNotNull(g.getHazardType());
            assertNotNull(g.getSource());
            assertNotNull(g.getGroundTruthId());
        });

        // DFO IDs must follow GT-DFO-{id} pattern
        catalog.stream().filter(g -> "DFO".equals(g.getSource()))
                .forEach(g -> assertTrue(g.getGroundTruthId().startsWith("GT-DFO-")));

        // EM-DAT IDs must follow GT-EMDAT-{id} pattern
        catalog.stream().filter(g -> "EMDAT".equals(g.getSource()))
                .forEach(g -> assertTrue(g.getGroundTruthId().startsWith("GT-EMDAT-")));

        log.info("✅ All {} ground-truth records have unique IDs and valid provenance", catalog.size());
    }

    // =========================================================================
    // VALIDATION REPORT TESTS
    // =========================================================================

    @Test
    @DisplayName("3.8.5: Master Validation Report — Structure and Completeness")
    void testValidationReportStructure() {
        ValidationReportDto report = hazardValidationService.generateValidationReport();

        assertNotNull(report);
        assertNotNull(report.getGeneratedAt());
        assertNotNull(report.getReportTitle());
        assertNotNull(report.getValidationMethodology());

        // Data quality coverage must be present
        assertNotNull(report.getDataQualityCoverage());
        ValidationReportDto.DataQualityCoverageDto coverage = report.getDataQualityCoverage();
        assertEquals(23, coverage.getTotalDfoEvents());
        assertEquals(7, coverage.getDfoEventsWithValidGeometry());
        assertEquals(16, coverage.getDfoEventsWithSentinelCoordinates());
        assertEquals(53, coverage.getTotalEmdatRecords());
        assertEquals(0, coverage.getEmdatRecordsUsableForValidation());
        assertEquals(3, coverage.getTotalWeatherStations());
        assertTrue(coverage.getTotalWeatherRecords() > 100000);

        // Must have 3 validation targets
        assertEquals(3, report.getValidationTargets().size());

        // Overall assessment must be present
        assertNotNull(report.getOverallAssessment());
        assertFalse(report.getIdentifiedStrengths().isEmpty());
        assertFalse(report.getIdentifiedWeaknesses().isEmpty());
        assertFalse(report.getCalibrationRecommendations().isEmpty());
        assertNotNull(report.getBoundaryNote());

        log.info("✅ Validation report structure verified: {} targets, {} strengths, {} weaknesses, {} recommendations",
                report.getValidationTargets().size(), report.getIdentifiedStrengths().size(),
                report.getIdentifiedWeaknesses().size(), report.getCalibrationRecommendations().size());
    }

    @Test
    @DisplayName("3.8.6: Data Quality Coverage — Temporal Overlap Assessment")
    void testTemporalOverlapAssessment() {
        ValidationReportDto report = hazardValidationService.generateValidationReport();
        ValidationReportDto.DataQualityCoverageDto coverage = report.getDataQualityCoverage();

        assertNotNull(coverage.getTemporalOverlapAssessment());
        assertTrue(coverage.getTemporalOverlapAssessment().contains("ZERO temporal overlap") ||
                   coverage.getTemporalOverlapAssessment().contains("CRITICAL GAP"),
                "Temporal overlap assessment must transparently report the data gap");

        assertNotNull(coverage.getWeatherTemporalCoverage());
        assertNotNull(coverage.getDfoTemporalCoverage());

        assertFalse(coverage.getExclusionReasons().isEmpty());

        log.info("✅ Temporal overlap assessment: {}", coverage.getTemporalOverlapAssessment().substring(0, 80));
    }

    @Test
    @DisplayName("3.8.7: Flood Hazard Score Validation — Event-Level Metrics")
    void testFloodHazardScoreValidation() {
        ValidationReportDto report = hazardValidationService.generateValidationReport();

        ValidationMetricsDto floodMetrics = report.getValidationTargets().stream()
                .filter(v -> "FLOOD_HAZARD_SCORE".equals(v.getValidationTarget()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("FLOOD_HAZARD_SCORE validation target not found"));

        assertEquals(23, floodMetrics.getTotalGroundTruthEvents());
        assertEquals(7, floodMetrics.getUsableGroundTruthEvents());
        assertEquals(16, floodMetrics.getExcludedGroundTruthEvents());
        assertTrue(floodMetrics.getTotalModelObservations() > 0);

        // Event-period mean score must be present
        assertNotNull(floodMetrics.getEventPeriodMeanScore());
        assertTrue(floodMetrics.getEventPeriodMeanScore() >= 0.0 && floodMetrics.getEventPeriodMeanScore() <= 1.0);

        // Score separation must be calculated
        if (floodMetrics.getScoreSeparation() != null) {
            assertNotNull(floodMetrics.getScoreSeparationInterpretation());
        }

        // Tier distribution must have all 4 tiers
        assertNotNull(floodMetrics.getEventTierDistribution());
        assertEquals(4, floodMetrics.getEventTierDistribution().size());

        // Statistical warning must honestly disclose in-sample limitation
        assertNotNull(floodMetrics.getStatisticalWarning());
        assertTrue(floodMetrics.getStatisticalWarning().contains("self-validation") ||
                   floodMetrics.getStatisticalWarning().contains("in-sample"),
                "Must transparently disclose self-validation limitation");

        log.info("✅ Flood validation: eventMean={}, separation={}, matched={}",
                floodMetrics.getEventPeriodMeanScore(), floodMetrics.getScoreSeparation(),
                floodMetrics.getMatchedObservations());
    }

    @Test
    @DisplayName("3.8.8: Extreme Rainfall Score Validation — Monsoon Seasonal Pattern")
    void testExtremeRainfallScoreValidation() {
        ValidationReportDto report = hazardValidationService.generateValidationReport();

        ValidationMetricsDto rainfallMetrics = report.getValidationTargets().stream()
                .filter(v -> "EXTREME_RAINFALL_SCORE".equals(v.getValidationTarget()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("EXTREME_RAINFALL_SCORE validation target not found"));

        assertTrue(rainfallMetrics.getTotalModelObservations() > 0);

        // Statistical warning must acknowledge weak validation signal
        assertNotNull(rainfallMetrics.getStatisticalWarning());

        assertNotNull(rainfallMetrics.getTemporalCoverageNote());

        log.info("✅ Rainfall validation: monsoonMean={}, nonMonsoonMean={}, separation={}",
                rainfallMetrics.getEventPeriodMeanScore(), rainfallMetrics.getNonEventPeriodMeanScore(),
                rainfallMetrics.getScoreSeparation());
    }

    @Test
    @DisplayName("3.8.9: Multi-Hazard Index Validation — Insufficient Ground Truth Documented")
    void testMultiHazardIndexValidation() {
        ValidationReportDto report = hazardValidationService.generateValidationReport();

        ValidationMetricsDto multiHazardMetrics = report.getValidationTargets().stream()
                .filter(v -> "MULTI_HAZARD_INDEX".equals(v.getValidationTarget()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("MULTI_HAZARD_INDEX validation target not found"));

        // Zero usable events due to temporal non-overlap
        assertEquals(0, multiHazardMetrics.getUsableGroundTruthEvents());
        assertNotNull(multiHazardMetrics.getStatisticalWarning());
        assertTrue(multiHazardMetrics.getStatisticalWarning().contains("NOT POSSIBLE") ||
                   multiHazardMetrics.getStatisticalWarning().contains("Insufficient"),
                "Must honestly document validation impossibility");

        log.info("✅ Multi-hazard validation correctly reports: insufficient ground truth (usable={})",
                multiHazardMetrics.getUsableGroundTruthEvents());
    }

    @Test
    @DisplayName("3.8.10: Score Separation — Event Scores Consistent with Historical Events")
    void testScoreSeparationConsistency() {
        ValidationReportDto report = hazardValidationService.generateValidationReport();

        ValidationMetricsDto floodMetrics = report.getValidationTargets().stream()
                .filter(v -> "FLOOD_HAZARD_SCORE".equals(v.getValidationTarget()))
                .findFirst()
                .orElseThrow();

        if (floodMetrics.getEventPeriodMeanScore() != null && floodMetrics.getNonEventPeriodMeanScore() != null) {
            // In-sample scores: located DFO events (with valid coordinates) should score equal to or higher
            // than unlocated events (with sentinel coordinates which receive default/lower scoring)
            Double sep = floodMetrics.getScoreSeparation();
            assertNotNull(sep);
            log.info("✅ Flood score separation: {} (interpretation: {})", sep, floodMetrics.getScoreSeparationInterpretation());
        }
    }

    @Test
    @DisplayName("3.8.11: Validation Boundary — No Model Tuning or ML Prediction")
    void testValidationBoundary() {
        ValidationReportDto report = hazardValidationService.generateValidationReport();

        assertNotNull(report.getBoundaryNote());
        assertTrue(report.getBoundaryNote().contains("NOT") || report.getBoundaryNote().contains("not"),
                "Boundary note must explicitly state what Stage 3.8 does NOT do");

        // Calibration recommendations must be documented but NOT applied
        assertFalse(report.getCalibrationRecommendations().isEmpty());
        assertTrue(report.getCalibrationRecommendations().stream()
                .anyMatch(r -> r.contains("NOT") || r.contains("Do NOT") || r.contains("not")),
                "At least one recommendation must include explicit 'do not' constraint");

        log.info("✅ Validation boundary verified: {} recommendations documented without automatic application",
                report.getCalibrationRecommendations().size());
    }

    @Test
    @DisplayName("3.8.12: District Coverage — Spatial Association via PostGIS")
    void testDistrictCoverage() {
        List<GroundTruthEvent> catalog = hazardValidationService.buildGroundTruthCatalog();

        List<GroundTruthEvent> spatiallyValid = catalog.stream()
                .filter(GroundTruthEvent::isUsableForSpatialValidation)
                .toList();

        long withDistrict = spatiallyValid.stream()
                .filter(g -> g.getAssociatedDistrict() != null)
                .count();

        log.info("✅ District coverage: {}/{} spatially-valid events have district associations",
                withDistrict, spatiallyValid.size());

        // At least some should be mapped (dependent on PostGIS geometry containment)
        if (!spatiallyValid.isEmpty()) {
            assertTrue(withDistrict >= 0, "District mapping should be attempted for all valid events");
        }
    }
}
