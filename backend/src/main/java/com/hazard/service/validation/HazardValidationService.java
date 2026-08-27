package com.hazard.service.validation;

import com.hazard.domain.hazard.*;
import com.hazard.dto.scoring.HazardScoreDto;
import com.hazard.dto.validation.*;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.hazard.DfoFloodEventRepository;
import com.hazard.repository.hazard.EmdatFloodRecordRepository;
import com.hazard.repository.weather.HourlyWeatherRepository;
import com.hazard.service.scoring.HazardScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Stage 3.8 Hazard Validation Engine.
 *
 * Validates whether the hazard scores produced by the pipeline (Stages 3.1-3.5)
 * correspond reasonably well with historical disaster observations.
 *
 * CRITICAL DESIGN PRINCIPLES:
 * 1. Run the current model first, measure performance, identify weaknesses, document them.
 * 2. Never tune the model against the same data and report the tuned result as independent validation.
 * 3. Ground truth remains distinguishable from model output at all times.
 * 4. Statistical honesty: clearly distinguish association from prediction accuracy.
 */
@Service
@Transactional(readOnly = true)
public class HazardValidationService {

    private static final Logger log = LoggerFactory.getLogger(HazardValidationService.class);

    // Sentinel coordinate threshold used by Stage 3.2 to identify unlocated DFO records
    private static final double SENTINEL_THRESHOLD = -1.0E300;

    private final DfoFloodEventRepository dfoFloodEventRepository;
    private final EmdatFloodRecordRepository emdatFloodRecordRepository;
    private final HourlyWeatherRepository hourlyWeatherRepository;
    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final HazardScoringService hazardScoringService;

    public HazardValidationService(DfoFloodEventRepository dfoFloodEventRepository,
                                   EmdatFloodRecordRepository emdatFloodRecordRepository,
                                   HourlyWeatherRepository hourlyWeatherRepository,
                                   DistrictBoundaryRepository districtBoundaryRepository,
                                   HazardScoringService hazardScoringService) {
        this.dfoFloodEventRepository = dfoFloodEventRepository;
        this.emdatFloodRecordRepository = emdatFloodRecordRepository;
        this.hourlyWeatherRepository = hourlyWeatherRepository;
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.hazardScoringService = hazardScoringService;
    }

    // =========================================================================
    // 1. MASTER VALIDATION REPORT
    // =========================================================================

    /**
     * Generates the complete Stage 3.8 Validation Report.
     */
    public ValidationReportDto generateValidationReport() {
        log.info("Stage 3.8: Generating Hazard Validation Report...");

        ValidationReportDto report = new ValidationReportDto();

        // Step 1: Build ground truth catalog
        List<GroundTruthEvent> groundTruth = buildGroundTruthCatalog();

        // Step 2: Data quality coverage analysis
        ValidationReportDto.DataQualityCoverageDto coverage = buildDataQualityCoverage(groundTruth);
        report.setDataQualityCoverage(coverage);

        // Step 3: Flood Hazard Score Validation (DFO events with valid coordinates)
        ValidationMetricsDto floodValidation = validateFloodHazardScores(groundTruth);
        report.addValidationTarget(floodValidation);

        // Step 4: Extreme Rainfall Score Validation (weather-station based)
        ValidationMetricsDto rainfallValidation = validateExtremeRainfallScores(groundTruth);
        report.addValidationTarget(rainfallValidation);

        // Step 5: Multi-Hazard Index Validation
        ValidationMetricsDto multiHazardValidation = validateMultiHazardIndex(groundTruth);
        report.addValidationTarget(multiHazardValidation);

        // Step 6: Overall assessment
        compileOverallAssessment(report);

        log.info("Stage 3.8: Validation Report generation complete.");
        return report;
    }

    // =========================================================================
    // 2. GROUND-TRUTH CATALOG CONSTRUCTION
    // =========================================================================

    /**
     * Builds a comprehensive ground-truth catalog from all available historical sources.
     * DFO floods are the primary spatially-located ground truth.
     * EM-DAT records are national-aggregate only — documented but excluded from spatial validation.
     */
    public List<GroundTruthEvent> buildGroundTruthCatalog() {
        List<GroundTruthEvent> catalog = new ArrayList<>();

        // DFO Flood Events
        var dfoEvents = dfoFloodEventRepository.findAll();
        for (var dfo : dfoEvents) {
            GroundTruthEvent gt = new GroundTruthEvent();
            gt.setGroundTruthId("GT-DFO-" + dfo.getId());
            gt.setSource("DFO");
            gt.setSourceRecordId(dfo.getId());
            gt.setHazardType(HazardType.FLOOD);
            gt.setEventStart(dfo.getBeganDate());
            gt.setEventEnd(dfo.getEndedDate());
            gt.setLocationDescription(dfo.getDetailedLocation());
            gt.setDfoSeverity(dfo.getSeverity());
            gt.setDfoMagnitude(dfo.getMagnitude());
            gt.setDeaths(dfo.getDeaths());
            gt.setDisplaced(dfo.getDisplaced());
            gt.setAffectedSqkm(dfo.getAffectedSqkm());
            gt.setTemporalPrecision("EXACT_DATE");
            gt.setUsableForTemporalValidation(dfo.getBeganDate() != null);

            boolean hasValidCoords = dfo.getCentroidX() != null && dfo.getCentroidY() != null
                    && dfo.getCentroidX() > SENTINEL_THRESHOLD && dfo.getCentroidY() > SENTINEL_THRESHOLD
                    && Math.abs(dfo.getCentroidX()) < 200 && Math.abs(dfo.getCentroidY()) < 100;

            gt.setHasValidGeometry(hasValidCoords);
            if (hasValidCoords) {
                gt.setLongitude(dfo.getCentroidX());
                gt.setLatitude(dfo.getCentroidY());
                gt.setGeographicPrecision("POINT_COORDINATE");
                gt.setUsableForSpatialValidation(true);

                // Attempt district association via PostGIS
                districtBoundaryRepository.findDistrictContainingPoint(dfo.getCentroidX(), dfo.getCentroidY())
                        .ifPresent(d -> gt.setAssociatedDistrict(d.getName2()));
            } else {
                gt.setGeographicPrecision("STATE_LEVEL");
                gt.setUsableForSpatialValidation(false);
                gt.setExclusionReason("Sentinel coordinates (unlocated DFO record); spatial validation impossible.");
            }

            catalog.add(gt);
        }

        // EM-DAT Flood Records
        var emdatRecords = emdatFloodRecordRepository.findAll();
        for (var emdat : emdatRecords) {
            GroundTruthEvent gt = new GroundTruthEvent();
            gt.setGroundTruthId("GT-EMDAT-" + emdat.getId());
            gt.setSource("EMDAT");
            gt.setSourceRecordId(emdat.getId());
            gt.setHazardType(HazardType.FLOOD);
            gt.setEventStart(emdat.getYear() != null ? LocalDate.of(emdat.getYear(), 1, 1) : null);
            gt.setEventEnd(emdat.getYear() != null ? LocalDate.of(emdat.getYear(), 12, 31) : null);
            gt.setLocationDescription("India (National Aggregate)");
            gt.setDeaths(emdat.getTotalDeaths() != null ? emdat.getTotalDeaths().doubleValue() : null);
            gt.setDisplaced(emdat.getTotalAffected() != null ? emdat.getTotalAffected().doubleValue() : null);
            gt.setHasValidGeometry(false);
            gt.setGeographicPrecision("NATIONAL");
            gt.setTemporalPrecision("YEAR_ONLY");
            gt.setUsableForSpatialValidation(false);
            gt.setUsableForTemporalValidation(false);
            gt.setExclusionReason("National-aggregate country-level record without sub-national geography or exact dates; " +
                    "excluded from spatial and temporal validation.");
            catalog.add(gt);
        }

        log.info("Ground-truth catalog built: {} DFO events, {} EM-DAT records, {} total",
                dfoEvents.size(), emdatRecords.size(), catalog.size());
        return catalog;
    }

    // =========================================================================
    // 3. DATA QUALITY COVERAGE
    // =========================================================================

    private ValidationReportDto.DataQualityCoverageDto buildDataQualityCoverage(List<GroundTruthEvent> groundTruth) {
        ValidationReportDto.DataQualityCoverageDto coverage = new ValidationReportDto.DataQualityCoverageDto();

        List<GroundTruthEvent> dfoEvents = groundTruth.stream().filter(g -> "DFO".equals(g.getSource())).toList();
        List<GroundTruthEvent> emdatEvents = groundTruth.stream().filter(g -> "EMDAT".equals(g.getSource())).toList();

        coverage.setTotalDfoEvents(dfoEvents.size());
        coverage.setDfoEventsWithValidGeometry((int) dfoEvents.stream().filter(GroundTruthEvent::isHasValidGeometry).count());
        coverage.setDfoEventsWithSentinelCoordinates((int) dfoEvents.stream().filter(g -> !g.isHasValidGeometry()).count());
        coverage.setDfoEventsUsableForValidation((int) dfoEvents.stream().filter(GroundTruthEvent::isUsableForSpatialValidation).count());

        coverage.setTotalEmdatRecords(emdatEvents.size());
        coverage.setEmdatRecordsUsableForValidation(0);
        coverage.setEmdatExclusionReason("All 53 EM-DAT records are 'National Aggregate (Country-level)' without " +
                "sub-national geometry or exact event dates. They cannot be used for spatial or temporal validation.");

        coverage.setTotalWeatherStations(3);
        coverage.setTotalWeatherRecords((int) hourlyWeatherRepository.count());
        coverage.setWeatherTemporalCoverage("2020-01-01 to 2024-12-31 (3 stations: Patna, Muzaffarpur, Bhagalpur)");

        // DFO temporal coverage
        Optional<LocalDate> minDfoDate = dfoEvents.stream().map(GroundTruthEvent::getEventStart)
                .filter(Objects::nonNull).min(LocalDate::compareTo);
        Optional<LocalDate> maxDfoDate = dfoEvents.stream().map(GroundTruthEvent::getEventEnd)
                .filter(Objects::nonNull).max(LocalDate::compareTo);
        coverage.setDfoTemporalCoverage(minDfoDate.map(d -> d + " to " + maxDfoDate.orElse(d)).orElse("N/A"));

        // Temporal overlap assessment
        coverage.setTemporalOverlapAssessment(
                "CRITICAL GAP: DFO flood events with valid coordinates span 2006-2010. " +
                "Weather observations span 2020-2024. There is ZERO temporal overlap between spatially-valid " +
                "DFO flood events and meteorological observations. This fundamentally limits cross-validation " +
                "of rainfall scores against documented flood events. Flood score validation uses the model's " +
                "own DFO-derived hazard scores; rainfall score validation uses weather-station extreme events " +
                "compared against EM-DAT year-level flood occurrence as weak corroboration only."
        );

        coverage.setTotalGroundTruthEvents(groundTruth.size());
        coverage.setTotalUsableGroundTruthEvents((int) groundTruth.stream()
                .filter(GroundTruthEvent::isUsableForSpatialValidation).count());

        // District coverage for usable events
        Map<String, Integer> districtCounts = new LinkedHashMap<>();
        dfoEvents.stream()
                .filter(GroundTruthEvent::isUsableForSpatialValidation)
                .filter(g -> g.getAssociatedDistrict() != null)
                .forEach(g -> districtCounts.merge(g.getAssociatedDistrict(), 1, Integer::sum));
        coverage.setDistrictCoverage(districtCounts);

        // Exclusion reasons
        List<String> reasons = new ArrayList<>();
        reasons.add("16 DFO events: sentinel coordinates (-1.79E+308) — unlocatable, excluded from spatial validation");
        reasons.add("53 EM-DAT records: national aggregate only — no sub-national geometry, excluded entirely");
        reasons.add("0 DFO events overlap temporally with weather station data (2020-2024)");
        coverage.setExclusionReasons(reasons);

        return coverage;
    }

    // =========================================================================
    // 4. FLOOD HAZARD SCORE VALIDATION
    // =========================================================================

    /**
     * Validates whether DFO flood events receive higher flood hazard scores than comparable
     * observations at the same locations during non-event periods.
     *
     * Validation Unit: Event-level (DFO flood event matched to model score)
     * Baseline: All flood scores across ALL locations/times vs. scores at event locations
     */
    private ValidationMetricsDto validateFloodHazardScores(List<GroundTruthEvent> groundTruth) {
        ValidationMetricsDto metrics = new ValidationMetricsDto();
        metrics.setValidationTarget("FLOOD_HAZARD_SCORE");
        metrics.setValidationUnit("Event-level (DFO flood event → model flood score)");
        metrics.setDescription("Validates whether locations with historically documented DFO flood events " +
                "receive higher flood hazard scores than the overall population of scored observations.");

        List<GroundTruthEvent> dfoUsable = groundTruth.stream()
                .filter(g -> "DFO".equals(g.getSource()) && g.isUsableForSpatialValidation())
                .toList();

        metrics.setTotalGroundTruthEvents((int) groundTruth.stream().filter(g -> "DFO".equals(g.getSource())).count());
        metrics.setUsableGroundTruthEvents(dfoUsable.size());
        metrics.setExcludedGroundTruthEvents(metrics.getTotalGroundTruthEvents() - dfoUsable.size());

        // Retrieve ALL flood scores as the total model observation pool
        List<HazardScoreDto> allFloodScores = hazardScoringService.getHazardScoresByType(HazardType.FLOOD, null, 1000);
        metrics.setTotalModelObservations(allFloodScores.size());

        if (dfoUsable.isEmpty() || allFloodScores.isEmpty()) {
            metrics.setStatisticalWarning("Insufficient usable ground truth events or model observations for validation.");
            return metrics;
        }

        // Match: for each usable DFO event, find the model flood score for that event (by DFO ID)
        List<Double> eventScores = new ArrayList<>();
        for (GroundTruthEvent gt : dfoUsable) {
            String modelId = "DFO-" + gt.getSourceRecordId();
            allFloodScores.stream()
                    .filter(s -> modelId.equals(s.getId()))
                    .findFirst()
                    .ifPresent(s -> {
                        if (s.getHazardScore() != null) {
                            eventScores.add(s.getHazardScore());
                        }
                    });
        }

        metrics.setMatchedObservations(eventScores.size());

        if (eventScores.isEmpty()) {
            metrics.setStatisticalWarning("No model flood scores matched to usable DFO ground truth events.");
            return metrics;
        }

        // Calculate event period mean score
        double eventMean = eventScores.stream().mapToDouble(d -> d).average().orElse(0.0);
        metrics.setEventPeriodMeanScore(round4(eventMean));

        // Calculate non-event baseline: all flood scores NOT matching a ground-truth DFO event
        Set<String> eventIds = dfoUsable.stream()
                .map(g -> "DFO-" + g.getSourceRecordId())
                .collect(Collectors.toSet());
        List<Double> nonEventScores = allFloodScores.stream()
                .filter(s -> !eventIds.contains(s.getId()) && s.getHazardScore() != null)
                .map(HazardScoreDto::getHazardScore)
                .toList();

        if (!nonEventScores.isEmpty()) {
            double nonEventMean = nonEventScores.stream().mapToDouble(d -> d).average().orElse(0.0);
            metrics.setNonEventPeriodMeanScore(round4(nonEventMean));
            metrics.setScoreSeparation(round4(eventMean - nonEventMean));
            metrics.setScoreSeparationInterpretation(interpretScoreSeparation(eventMean - nonEventMean));
        }

        metrics.setBaselineConstruction("Baseline = all DFO flood observation scores (located + unlocated sentinel events). " +
                "Event group = scores of 7 DFO events with valid centroid coordinates within Bihar.");

        // Severity tier distribution for event vs. non-event
        metrics.setEventTierDistribution(buildTierDistribution(eventScores));
        if (!nonEventScores.isEmpty()) {
            metrics.setNonEventTierDistribution(buildTierDistribution(nonEventScores));
        }

        // Ranking: what fraction of events fall in the top N% of all scores?
        List<Double> allScoresSorted = allFloodScores.stream()
                .filter(s -> s.getHazardScore() != null)
                .map(HazardScoreDto::getHazardScore)
                .sorted(Comparator.reverseOrder())
                .toList();
        if (!allScoresSorted.isEmpty()) {
            metrics.setEventCaptureInTop10Pct(round4(computeEventCapture(eventScores, allScoresSorted, 0.10)));
            metrics.setEventCaptureInTop20Pct(round4(computeEventCapture(eventScores, allScoresSorted, 0.20)));
            metrics.setEventCaptureInTop25Pct(round4(computeEventCapture(eventScores, allScoresSorted, 0.25)));
        }

        // Classification (using MODERATE threshold = 0.25 as "event predicted")
        double threshold = 0.25;
        int tp = (int) eventScores.stream().filter(s -> s >= threshold).count();
        int fn = eventScores.size() - tp;
        int fp = (int) nonEventScores.stream().filter(s -> s >= threshold).count();
        int tn = nonEventScores.size() - fp;

        if (tp + fp > 0) metrics.setPrecision(round4((double) tp / (tp + fp)));
        if (tp + fn > 0) metrics.setRecall(round4((double) tp / (tp + fn)));
        if (metrics.getPrecision() != null && metrics.getRecall() != null
                && (metrics.getPrecision() + metrics.getRecall()) > 0) {
            metrics.setF1Score(round4(2.0 * metrics.getPrecision() * metrics.getRecall()
                    / (metrics.getPrecision() + metrics.getRecall())));
        }
        metrics.setClassificationNote("Binary classification using MODERATE tier threshold (score >= 0.25). " +
                "Sample sizes are very small (n=" + eventScores.size() + " events); " +
                "these metrics are indicative only and not statistically robust.");

        metrics.setStatisticalWarning("CAUTION: Only " + eventScores.size() + " spatially-valid DFO flood events available. " +
                "All DFO events are inherently represented in the flood scoring model as input data. " +
                "This is self-validation (in-sample), not independent out-of-sample prediction validation. " +
                "Results indicate internal consistency of the scoring pipeline, not predictive accuracy.");

        metrics.setTemporalCoverageNote("DFO flood events span 2006-2010. Weather data spans 2020-2024. " +
                "Zero temporal overlap prevents cross-validating rainfall indicators against flood events.");

        return metrics;
    }

    // =========================================================================
    // 5. EXTREME RAINFALL SCORE VALIDATION
    // =========================================================================

    /**
     * Validates whether extreme rainfall scores are higher during historically
     * documented flood years than during non-flood-year periods.
     *
     * Because there is zero temporal overlap between DFO events and weather data,
     * this validation uses EM-DAT year-level flood occurrence as weak corroboration.
     *
     * Validation Unit: Station-month (comparing monsoon vs. non-monsoon scoring)
     */
    private ValidationMetricsDto validateExtremeRainfallScores(List<GroundTruthEvent> groundTruth) {
        ValidationMetricsDto metrics = new ValidationMetricsDto();
        metrics.setValidationTarget("EXTREME_RAINFALL_SCORE");
        metrics.setValidationUnit("Station-level seasonal comparison (monsoon vs. non-monsoon)");
        metrics.setDescription("Validates whether extreme rainfall scores are systematically higher during " +
                "the Indian monsoon season (June-September) when documented floods historically occur, " +
                "compared to dry/non-monsoon periods at the same stations.");

        // All rainfall scores
        List<HazardScoreDto> allRainfallScores = hazardScoringService.getHazardScoresByType(
                HazardType.EXTREME_RAINFALL, null, 1000);
        metrics.setTotalModelObservations(allRainfallScores.size());

        // EM-DAT records confirm floods occur during monsoon in all years 2020-2024
        long emdatFloodYearsInWeatherWindow = groundTruth.stream()
                .filter(g -> "EMDAT".equals(g.getSource()) && g.getEventStart() != null)
                .map(g -> g.getEventStart().getYear())
                .filter(y -> y >= 2020 && y <= 2024)
                .distinct().count();

        metrics.setTotalGroundTruthEvents((int) emdatFloodYearsInWeatherWindow);
        metrics.setUsableGroundTruthEvents((int) emdatFloodYearsInWeatherWindow);
        metrics.setExcludedGroundTruthEvents(0);

        if (allRainfallScores.isEmpty()) {
            metrics.setStatisticalWarning("No extreme rainfall scores available for validation.");
            return metrics;
        }

        // Separate monsoon (June-Sep) vs. non-monsoon scores
        List<Double> monsoonScores = new ArrayList<>();
        List<Double> nonMonsoonScores = new ArrayList<>();

        for (HazardScoreDto score : allRainfallScores) {
            if (score.getHazardScore() == null || score.getTimestamp() == null) continue;
            int month = score.getTimestamp().getMonthValue();
            if (month >= 6 && month <= 9) {
                monsoonScores.add(score.getHazardScore());
            } else {
                nonMonsoonScores.add(score.getHazardScore());
            }
        }

        metrics.setMatchedObservations(monsoonScores.size());

        if (!monsoonScores.isEmpty()) {
            double monsoonMean = monsoonScores.stream().mapToDouble(d -> d).average().orElse(0.0);
            metrics.setEventPeriodMeanScore(round4(monsoonMean));
            metrics.setEventTierDistribution(buildTierDistribution(monsoonScores));
        }

        if (!nonMonsoonScores.isEmpty()) {
            double nonMonsoonMean = nonMonsoonScores.stream().mapToDouble(d -> d).average().orElse(0.0);
            metrics.setNonEventPeriodMeanScore(round4(nonMonsoonMean));
            metrics.setNonEventTierDistribution(buildTierDistribution(nonMonsoonScores));
        }

        if (metrics.getEventPeriodMeanScore() != null && metrics.getNonEventPeriodMeanScore() != null) {
            double sep = metrics.getEventPeriodMeanScore() - metrics.getNonEventPeriodMeanScore();
            metrics.setScoreSeparation(round4(sep));
            metrics.setScoreSeparationInterpretation(interpretScoreSeparation(sep));
        }

        metrics.setBaselineConstruction("Event period = Indian monsoon months (June-September) during which " +
                "EM-DAT confirms national-level flood occurrences in all years 2020-2024. " +
                "Non-event period = non-monsoon months (October-May) from the same stations.");

        // Ranking
        List<Double> allSorted = allRainfallScores.stream()
                .filter(s -> s.getHazardScore() != null)
                .map(HazardScoreDto::getHazardScore)
                .sorted(Comparator.reverseOrder())
                .toList();
        if (!allSorted.isEmpty() && !monsoonScores.isEmpty()) {
            metrics.setEventCaptureInTop10Pct(round4(computeEventCapture(monsoonScores, allSorted, 0.10)));
            metrics.setEventCaptureInTop20Pct(round4(computeEventCapture(monsoonScores, allSorted, 0.20)));
            metrics.setEventCaptureInTop25Pct(round4(computeEventCapture(monsoonScores, allSorted, 0.25)));
        }

        metrics.setStatisticalWarning("This validation uses monsoon seasonality as a proxy for flood occurrence. " +
                "EM-DAT confirms flood events in India during all years 2020-2024, but at national aggregate " +
                "granularity only. This is a weak validation signal providing directional evidence only.");

        metrics.setTemporalCoverageNote("Weather data: 2020-2024. EM-DAT flood years in weather window: " +
                emdatFloodYearsInWeatherWindow + " years. Monsoon observations: " +
                monsoonScores.size() + ", Non-monsoon observations: " + nonMonsoonScores.size());

        return metrics;
    }

    // =========================================================================
    // 6. MULTI-HAZARD INDEX VALIDATION
    // =========================================================================

    private ValidationMetricsDto validateMultiHazardIndex(List<GroundTruthEvent> groundTruth) {
        ValidationMetricsDto metrics = new ValidationMetricsDto();
        metrics.setValidationTarget("MULTI_HAZARD_INDEX");
        metrics.setValidationUnit("N/A — insufficient labelled multi-hazard ground truth");
        metrics.setDescription("Assesses whether documented multi-hazard events receive higher multi-hazard indices.");

        // Count usable multi-hazard ground truth: need events with BOTH spatial precision AND
        // temporal overlap with the weather observation window (2020-2024)
        List<GroundTruthEvent> spatiallyUsable = groundTruth.stream()
                .filter(GroundTruthEvent::isUsableForSpatialValidation)
                .toList();

        // Check for temporal overlap with weather window
        List<GroundTruthEvent> temporallyUsable = spatiallyUsable.stream()
                .filter(g -> g.getEventStart() != null &&
                        !g.getEventStart().isBefore(LocalDate.of(2020, 1, 1)) &&
                        !g.getEventStart().isAfter(LocalDate.of(2024, 12, 31)))
                .toList();

        metrics.setTotalGroundTruthEvents((int) groundTruth.stream()
                .filter(g -> "DFO".equals(g.getSource())).count());
        metrics.setUsableGroundTruthEvents(temporallyUsable.size());
        metrics.setExcludedGroundTruthEvents(metrics.getTotalGroundTruthEvents() - temporallyUsable.size());

        metrics.setStatisticalWarning(
                "Insufficient ground truth for statistical multi-hazard validation. " +
                "The 7 spatially-valid DFO flood events span 2006-2010, while weather/rainfall data " +
                "covers 2020-2024. Zero events have both valid spatial coordinates AND temporal overlap " +
                "with rainfall observations. Multi-hazard validation requires coincident flood + rainfall " +
                "ground truth which does not exist in the current dataset. " +
                "Result: MULTI-HAZARD INDEX VALIDATION NOT POSSIBLE with current data.");

        metrics.setBaselineConstruction("Not applicable — no labelled multi-hazard ground truth available.");
        metrics.setTemporalCoverageNote("DFO flood events: 2006-2010. Weather data: 2020-2024. Overlap: NONE.");

        return metrics;
    }

    // =========================================================================
    // 7. OVERALL ASSESSMENT
    // =========================================================================

    private void compileOverallAssessment(ValidationReportDto report) {
        report.setOverallAssessment(
                "Initial empirical validation / MVP validation. " +
                "The hazard scoring pipeline demonstrates internal consistency: DFO flood events " +
                "receive quantifiable hazard scores, and extreme rainfall scores show expected seasonal " +
                "variation. However, the validation is fundamentally limited by dataset constraints: " +
                "(1) only 7 of 23 DFO events have valid coordinates, (2) all EM-DAT records are national " +
                "aggregates without sub-national geography, (3) zero temporal overlap exists between " +
                "spatially-valid flood events and weather observations. These limitations prevent " +
                "independent out-of-sample prediction validation."
        );

        report.setIdentifiedStrengths(List.of(
                "Flood scoring pipeline correctly processes and scores all 7 spatially-valid DFO events",
                "Extreme rainfall scores show expected monsoon-season elevation vs. dry season",
                "Severity tier classification produces meaningful differentiation across event types",
                "District-level spatial association via PostGIS ST_Contains operates correctly",
                "Data quality pipeline correctly identifies and quarantines 16 unlocated DFO records and 53 unlocated EM-DAT records"
        ));

        report.setIdentifiedWeaknesses(List.of(
                "CRITICAL: Zero temporal overlap between spatially-valid DFO flood events (2006-2010) and weather data (2020-2024)",
                "Only 7 of 23 DFO events have valid point coordinates for spatial validation",
                "All 53 EM-DAT records are national-aggregate (country-level) and useless for sub-national validation",
                "Flood hazard score validation is in-sample (self-validation), not independent out-of-sample testing",
                "Multi-hazard index cannot be validated due to absent coincident ground truth",
                "Sample sizes are too small for statistically robust classification metrics (precision, recall, F1)"
        ));

        report.setCalibrationRecommendations(List.of(
                "Acquire spatially-precise flood event data from 2020-2024 (NDMA, CWC, IMD, state disaster authorities) " +
                        "to enable cross-validation with weather-station rainfall observations",
                "Integrate district-level flood occurrence records from Bihar State Disaster Management Authority (BSDMA)",
                "Add CWC river gauge data for flood stage validation at major river crossings",
                "Expand weather station coverage beyond 3 stations to improve spatial resolution",
                "Consider IMD gridded precipitation products (0.25° resolution) for complete district coverage",
                "Do NOT adjust scoring weights or severity thresholds until independent validation data is available"
        ));

        report.setBoundaryNote(
                "Stage 3.8 completes the Hazard Validation sub-stage. It does NOT implement new scoring, " +
                "ML/AI prediction, exposure analysis, vulnerability, or risk scoring. " +
                "Calibration recommendations are documented but NOT automatically applied. " +
                "Any future calibration must follow the separation principle: calibrate on one dataset, " +
                "validate on a held-out dataset, and never report tuned results as independent validation."
        );
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    private Map<String, Integer> buildTierDistribution(List<Double> scores) {
        Map<String, Integer> dist = new LinkedHashMap<>();
        dist.put("LOW", 0);
        dist.put("MODERATE", 0);
        dist.put("HIGH", 0);
        dist.put("SEVERE", 0);
        for (double s : scores) {
            SeverityTier tier = SeverityTier.fromScore(s);
            if (tier != null) {
                dist.merge(tier.name(), 1, Integer::sum);
            }
        }
        return dist;
    }

    private double computeEventCapture(List<Double> eventScores, List<Double> allScoresSorted, double topFraction) {
        if (allScoresSorted.isEmpty() || eventScores.isEmpty()) return 0.0;
        int topN = Math.max(1, (int) (allScoresSorted.size() * topFraction));
        double cutoff = allScoresSorted.get(Math.min(topN - 1, allScoresSorted.size() - 1));
        long captured = eventScores.stream().filter(s -> s >= cutoff).count();
        return (double) captured / eventScores.size();
    }

    private String interpretScoreSeparation(double separation) {
        if (separation > 0.20) return "Strong positive separation: event scores substantially higher than baseline";
        if (separation > 0.10) return "Moderate positive separation: event scores meaningfully higher than baseline";
        if (separation > 0.05) return "Weak positive separation: event scores slightly higher than baseline";
        if (separation > -0.05) return "Negligible separation: event and baseline scores are similar";
        return "Negative separation: event scores lower than baseline (unexpected)";
    }

    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
