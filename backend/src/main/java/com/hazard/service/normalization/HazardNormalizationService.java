package com.hazard.service.normalization;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.GeoJsonGeometryDto;
import com.hazard.dto.normalization.*;
import com.hazard.dto.processing.DailyRainfallSummary;
import com.hazard.dto.processing.ProcessedHazardObservation;
import com.hazard.dto.processing.RollingRainfallMetrics;
import com.hazard.service.processing.HazardProcessingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Master Domain Service for Sub-Stage 3.3 Hazard Normalization.
 * Translates processed hazard observations into standardized [0.00, 1.00] scale metrics.
 */
@Service
@Transactional(readOnly = true)
public class HazardNormalizationService {

    public static final int DEFAULT_LIMIT = 50;
    public static final int MAX_LIMIT = 1000;

    private final HazardProcessingService hazardProcessingService;
    private final HazardNormalizationEngine normalizationEngine;

    public HazardNormalizationService(HazardProcessingService hazardProcessingService,
                                     HazardNormalizationEngine normalizationEngine) {
        this.hazardProcessingService = hazardProcessingService;
        this.normalizationEngine = normalizationEngine;
    }

    /**
     * Retrieves and normalizes a single hazard observation by unified ID.
     */
    public NormalizedHazardObservation getNormalizedHazardById(String unifiedId) {
        ProcessedHazardObservation processed = hazardProcessingService.getProcessedHazardById(unifiedId);
        return normalizeObservation(processed);
    }

    /**
     * Retrieves all normalized hazard observations with optional filtering.
     */
    public List<NormalizedHazardObservation> getAllNormalizedHazards(HazardType type, QualityStatus quality,
                                                                     String district, String metricName, Integer limit) {
        int safeLimit = sanitizeLimit(limit);

        // If metricName is specified and type is null, infer type if it's flood-specific or rainfall-specific
        HazardType resolvedType = type;
        if (resolvedType == null && metricName != null && !metricName.trim().isEmpty()) {
            String m = metricName.trim().toUpperCase();
            if (m.startsWith("FLOOD_")) {
                resolvedType = HazardType.FLOOD;
            } else if (m.contains("RAINFALL") || m.contains("PRECIPITATION")) {
                resolvedType = HazardType.EXTREME_RAINFALL;
            }
        }

        int fetchLimit = (metricName != null || quality != null || district != null) ? Math.max(safeLimit * 5, 200) : safeLimit;
        List<ProcessedHazardObservation> processedList = hazardProcessingService.getAllProcessedHazards(resolvedType, quality, district, fetchLimit);

        List<NormalizedHazardObservation> normalizedList = processedList.stream()
                .map(this::normalizeObservation)
                .collect(Collectors.toList());

        if (metricName != null && !metricName.trim().isEmpty()) {
            String targetMetric = metricName.trim().toUpperCase();
            normalizedList = normalizedList.stream()
                    .filter(n -> n.getNormalizedMetrics().containsKey(targetMetric))
                    .collect(Collectors.toList());
        }

        return normalizedList.stream().limit(safeLimit).collect(Collectors.toList());
    }

    /**
     * Retrieves normalized daily rainfall summaries for a weather station and date range.
     */
    public List<NormalizedDailyRainfall> getNormalizedDailyRainfall(String stationName, LocalDate startDate, LocalDate endDate) {
        List<DailyRainfallSummary> summaries = hazardProcessingService.getDailyRainfallSummaries(stationName, startDate, endDate);

        return summaries.stream().map(s -> {
            NormalizedDailyRainfall norm = new NormalizedDailyRainfall();
            norm.setStationName(s.getStationName());
            norm.setDate(s.getDate());
            norm.setAssociatedDistrict(s.getAssociatedDistrict());
            norm.setLongitude(s.getLongitude());
            norm.setLatitude(s.getLatitude());
            norm.setRawDailyTotalMm(s.getDailyTotalMm());
            norm.setRawPeakHourlyMm(s.getPeakHourlyMm());
            norm.setRainyHours(s.getRainyHours());
            norm.setHeavyRainHours(s.getHeavyRainHours());
            norm.setVeryHeavyRainHours(s.getVeryHeavyRainHours());
            norm.setExceedsHeavyThreshold(s.isExceedsHeavyThreshold());
            norm.setQualityStatus(s.getQualityStatus());

            norm.setNormalizedDailyTotal(normalizationEngine.normalizeByName(s.getDailyTotalMm(), "DAILY_RAINFALL_MM"));
            norm.setNormalizedPeakHourly(normalizationEngine.normalizeByName(s.getPeakHourlyMm(), "PEAK_HOURLY_RAINFALL_MM"));

            return norm;
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves multi-window normalized rolling rainfall metrics for a station at a timestamp.
     */
    public NormalizedRollingRainfall getNormalizedRollingRainfall(String stationName, LocalDateTime targetTime) {
        RollingRainfallMetrics rawMetrics = hazardProcessingService.getRollingRainfallMetrics(stationName, targetTime);

        NormalizedRollingRainfall norm = new NormalizedRollingRainfall();
        norm.setStationName(rawMetrics.getStationName());
        norm.setAssociatedDistrict(rawMetrics.getAssociatedDistrict());
        norm.setTimestamp(rawMetrics.getTimestamp());
        norm.setHeavyRainfall(rawMetrics.isHeavyRainfall());
        norm.setVeryHeavyRainfall(rawMetrics.isVeryHeavyRainfall());

        norm.setCurrentHourly(normalizationEngine.normalizeByName(rawMetrics.getCurrentHourlyMm(), "HOURLY_PRECIPITATION_MM"));
        norm.setRolling3h(normalizationEngine.normalizeByName(rawMetrics.getRolling3hMm(), "ROLLING_3H_RAINFALL_MM"));
        norm.setRolling6h(normalizationEngine.normalizeByName(rawMetrics.getRolling6hMm(), "ROLLING_6H_RAINFALL_MM"));
        norm.setRolling12h(normalizationEngine.normalizeByName(rawMetrics.getRolling12hMm(), "ROLLING_12H_RAINFALL_MM"));
        norm.setRolling24h(normalizationEngine.normalizeByName(rawMetrics.getRolling24hMm(), "ROLLING_24H_RAINFALL_MM"));

        return norm;
    }

    /**
     * Compiles an executive summary report of the normalization framework and active configurations.
     */
    public NormalizationSummaryDto getNormalizationSummary() {
        NormalizationSummaryDto summary = new NormalizationSummaryDto();
        summary.setCanonicalCrs("EPSG:4326 (WGS 84)");
        summary.setNormalizationScale("[0.0000, 1.0000] continuous relative intensity scale");
        summary.setTemporalCoverage("1968 to 2024");

        List<NormalizationSummaryDto.NormalizedHazardMetricConfigDto> configDtos = HazardMetricNormConfig.getAllConfigs().stream()
                .map(c -> new NormalizationSummaryDto.NormalizedHazardMetricConfigDto(
                        c.getMetricName(), c.getMetricLabel(), c.getUnits(),
                        c.getReferenceMin(), c.getReferenceMax(),
                        c.getMethod().name(), c.getDirection().name(),
                        c.getReferenceRationale()
                ))
                .collect(Collectors.toList());

        summary.setConfiguredMetrics(configDtos);
        summary.setTotalConfiguredMetrics(configDtos.size());

        var qualitySummary = hazardProcessingService.getProcessingQualitySummary();
        summary.setTotalEligibleObservations(qualitySummary.getTotalProcessedRecords());
        summary.setNormalizedObservationsCount(qualitySummary.getValidRecordsCount());
        summary.setActiveStations(qualitySummary.getActiveWeatherStations());
        summary.setSupportedDistricts(qualitySummary.getCoveredDistricts());

        return summary;
    }

    /**
     * Delivers normalized hazard observations as a standard GeoJSON FeatureCollection.
     */
    public GeoJsonFeatureCollectionDto getNormalizedHazardsGeoJson(HazardType type, String district, Integer limit) {
        List<NormalizedHazardObservation> observations = getAllNormalizedHazards(type, QualityStatus.VALID, district, null, limit);
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (NormalizedHazardObservation obs : observations) {
            if (obs.getLongitude() != null && obs.getLatitude() != null) {
                GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(obs.getLongitude(), obs.getLatitude());

                Map<String, Object> props = new LinkedHashMap<>();
                props.put("id", obs.getId());
                props.put("hazardType", obs.getHazardType().name());
                props.put("dataSource", obs.getDataSource());
                props.put("locationName", obs.getLocationName());
                props.put("associatedDistrict", obs.getAssociatedDistrict());
                props.put("isWithinBiharBoundary", obs.getIsWithinBiharBoundary());
                props.put("startDate", obs.getStartDate() != null ? obs.getStartDate().toString() : null);
                props.put("endDate", obs.getEndDate() != null ? obs.getEndDate().toString() : null);
                props.put("qualityStatus", obs.getQualityStatus().name());

                // Pack normalized values
                Map<String, Double> normValues = new LinkedHashMap<>();
                for (Map.Entry<String, NormalizedHazardMetric> entry : obs.getNormalizedMetrics().entrySet()) {
                    normValues.put(entry.getKey(), entry.getValue().getNormalizedValue());
                }
                props.put("normalizedMetrics", normValues);

                features.add(new GeoJsonFeatureDto(obs.getId(), geom, props));
            }
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    // =========================================================================
    // HELPER NORMALIZATION LOGIC
    // =========================================================================

    public NormalizedHazardObservation normalizeObservation(ProcessedHazardObservation processed) {
        if (processed == null) {
            return null;
        }

        NormalizedHazardObservation norm = new NormalizedHazardObservation();
        norm.setId(processed.getId());
        norm.setSourceRecordId(processed.getSourceRecordId());
        norm.setHazardType(processed.getHazardType());
        norm.setDataSource(processed.getDataSource());
        norm.setLocationName(processed.getLocationName());
        norm.setAssociatedDistrict(processed.getAssociatedDistrict());
        norm.setIsWithinBiharBoundary(processed.getIsWithinBiharBoundary());
        norm.setLongitude(processed.getLongitude());
        norm.setLatitude(processed.getLatitude());
        norm.setStartDate(processed.getStartDate());
        norm.setEndDate(processed.getEndDate());
        norm.setTimestamp(processed.getTimestamp());
        norm.setQualityStatus(processed.getQualityStatus());
        norm.setProcessingMetadata(processed.getProcessingMetadata());

        // Skip normalization for INVALID observations
        if (processed.getQualityStatus() == QualityStatus.INVALID) {
            return norm;
        }

        // Normalize metrics based on HazardType
        if (processed.getHazardType() == HazardType.FLOOD) {
            // 1. Flood Duration
            if (processed.getDurationDays() != null) {
                NormalizedHazardMetric m = normalizationEngine.normalizeByName(processed.getDurationDays(), "FLOOD_DURATION_DAYS");
                if (m != null) norm.addNormalizedMetric("FLOOD_DURATION_DAYS", m);
            }
            // 2. Flood Affected Area
            if (processed.getAffectedAreaSqKm() != null) {
                NormalizedHazardMetric m = normalizationEngine.normalizeByName(processed.getAffectedAreaSqKm(), "FLOOD_AFFECTED_AREA_SQKM");
                if (m != null) norm.addNormalizedMetric("FLOOD_AFFECTED_AREA_SQKM", m);
            }
            // 3. Flood Displacement Density
            Object densityObj = processed.getDerivedMetrics().get("displacementDensityPerSqKm");
            if (densityObj instanceof Number densityNum) {
                NormalizedHazardMetric m = normalizationEngine.normalizeByName(densityNum.doubleValue(), "FLOOD_DISPLACEMENT_DENSITY");
                if (m != null) norm.addNormalizedMetric("FLOOD_DISPLACEMENT_DENSITY", m);
            }
            // 4. Severity Index
            if (processed.getSeverity() != null) {
                NormalizedHazardMetric m = normalizationEngine.normalizeByName(processed.getSeverity(), "FLOOD_SEVERITY_INDEX");
                if (m != null) norm.addNormalizedMetric("FLOOD_SEVERITY_INDEX", m);
            }
            // 5. Magnitude Index
            if (processed.getMagnitude() != null) {
                NormalizedHazardMetric m = normalizationEngine.normalizeByName(processed.getMagnitude(), "FLOOD_MAGNITUDE_INDEX");
                if (m != null) norm.addNormalizedMetric("FLOOD_MAGNITUDE_INDEX", m);
            }
        } else if (processed.getHazardType() == HazardType.EXTREME_RAINFALL) {
            // Hourly Precipitation
            if (processed.getPrecipitationMm() != null) {
                NormalizedHazardMetric m = normalizationEngine.normalizeByName(processed.getPrecipitationMm(), "HOURLY_PRECIPITATION_MM");
                if (m != null) norm.addNormalizedMetric("HOURLY_PRECIPITATION_MM", m);
            }
        }

        return norm;
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
