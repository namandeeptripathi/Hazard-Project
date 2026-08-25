package com.hazard.service.scoring;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.GeoJsonGeometryDto;
import com.hazard.dto.normalization.NormalizedDailyRainfall;
import com.hazard.dto.normalization.NormalizedHazardMetric;
import com.hazard.dto.normalization.NormalizedHazardObservation;
import com.hazard.dto.normalization.NormalizedRollingRainfall;
import com.hazard.dto.scoring.*;
import com.hazard.service.normalization.HazardNormalizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Master Domain Service for Sub-Stage 3.4 Single-Hazard Scoring.
 * Translates normalized hazard indicators into weighted single-hazard scores and severity tiers.
 */
@Service
@Transactional(readOnly = true)
public class HazardScoringService {

    public static final int DEFAULT_LIMIT = 50;
    public static final int MAX_LIMIT = 1000;

    private final HazardNormalizationService hazardNormalizationService;
    private final HazardScoringEngine scoringEngine;

    public HazardScoringService(HazardNormalizationService hazardNormalizationService,
                                HazardScoringEngine scoringEngine) {
        this.hazardNormalizationService = hazardNormalizationService;
        this.scoringEngine = scoringEngine;
    }

    /**
     * Retrieves and scores a single hazard observation by unified ID.
     */
    public HazardScoreDto getHazardScoreById(String unifiedId) {
        NormalizedHazardObservation normalized = hazardNormalizationService.getNormalizedHazardById(unifiedId);
        return scoreObservation(normalized);
    }

    /**
     * Retrieves all single-hazard scored observations with optional filtering.
     */
    public List<HazardScoreDto> getAllHazardScores(HazardType type, String district, SeverityTier severity,
                                                   QualityStatus quality, Integer limit) {
        int safeLimit = sanitizeLimit(limit);
        int fetchLimit = (severity != null || district != null || quality != null) ? Math.max(safeLimit * 5, 250) : safeLimit;

        List<NormalizedHazardObservation> normalizedList = hazardNormalizationService.getAllNormalizedHazards(
                type, quality, district, null, fetchLimit
        );

        List<HazardScoreDto> scoredList = normalizedList.stream()
                .map(this::scoreObservation)
                .filter(s -> s.getHazardScore() != null)
                .collect(Collectors.toList());

        if (severity != null) {
            scoredList = scoredList.stream()
                    .filter(s -> s.getSeverityTier() == severity)
                    .collect(Collectors.toList());
        }

        return scoredList.stream().limit(safeLimit).collect(Collectors.toList());
    }

    /**
     * Retrieves single-hazard scores for a specific hazard type.
     */
    public List<HazardScoreDto> getHazardScoresByType(HazardType type, String district, Integer limit) {
        if (type == null) {
            throw new IllegalArgumentException("Hazard type cannot be null");
        }
        return getAllHazardScores(type, district, null, null, limit);
    }

    /**
     * Retrieves scored daily rainfall observations for a station and date window.
     */
    public List<DailyRainfallScoreDto> getScoredDailyRainfall(String stationName, LocalDate startDate, LocalDate endDate) {
        List<NormalizedDailyRainfall> normalizedList = hazardNormalizationService.getNormalizedDailyRainfall(stationName, startDate, endDate);

        return normalizedList.stream().map(n -> {
            DailyRainfallScoreDto dto = new DailyRainfallScoreDto();
            dto.setStationName(n.getStationName());
            dto.setDate(n.getDate());
            dto.setAssociatedDistrict(n.getAssociatedDistrict());
            dto.setLongitude(n.getLongitude());
            dto.setLatitude(n.getLatitude());
            dto.setRawDailyTotalMm(n.getRawDailyTotalMm());
            dto.setRawPeakHourlyMm(n.getRawPeakHourlyMm());
            dto.setRainyHours(n.getRainyHours());
            dto.setHeavyRainHours(n.getHeavyRainHours());
            dto.setVeryHeavyRainHours(n.getVeryHeavyRainHours());
            dto.setExceedsHeavyThreshold(n.isExceedsHeavyThreshold());
            dto.setQualityStatus(n.getQualityStatus());

            // Build rainfall score: daily total (0.60) + peak hourly (0.40)
            Map<String, NormalizedHazardMetric> metricsMap = new LinkedHashMap<>();
            if (n.getNormalizedDailyTotal() != null) metricsMap.put("DAILY_RAINFALL_MM", n.getNormalizedDailyTotal());
            if (n.getNormalizedPeakHourly() != null) metricsMap.put("HOURLY_PRECIPITATION_MM", n.getNormalizedPeakHourly());

            Map<String, Double> weights = new LinkedHashMap<>();
            weights.put("DAILY_RAINFALL_MM", 0.60);
            weights.put("HOURLY_PRECIPITATION_MM", 0.40);
            HazardScoringConfig config = new HazardScoringConfig(HazardType.EXTREME_RAINFALL, weights, "Daily rainfall index");

            HazardScoringEngine.ScoringResult result = scoringEngine.calculateScore(metricsMap, config);
            dto.setRainfallHazardScore(result.hazardScore());
            dto.setSeverityTier(result.severityTier());
            dto.setMetricContributions(result.contributions());

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves scored multi-window rolling rainfall metrics for a station at a timestamp.
     */
    public RollingRainfallScoreDto getScoredRollingRainfall(String stationName, LocalDateTime targetTime) {
        NormalizedRollingRainfall normalized = hazardNormalizationService.getNormalizedRollingRainfall(stationName, targetTime);

        RollingRainfallScoreDto dto = new RollingRainfallScoreDto();
        dto.setStationName(normalized.getStationName());
        dto.setAssociatedDistrict(normalized.getAssociatedDistrict());
        dto.setTimestamp(normalized.getTimestamp());
        dto.setHeavyRainfall(normalized.isHeavyRainfall());
        dto.setVeryHeavyRainfall(normalized.isVeryHeavyRainfall());
        dto.setQualityStatus(normalized.getQualityStatus());

        Map<String, NormalizedHazardMetric> metricsMap = new LinkedHashMap<>();
        if (normalized.getCurrentHourly() != null) metricsMap.put("HOURLY_PRECIPITATION_MM", normalized.getCurrentHourly());
        if (normalized.getRolling24h() != null) metricsMap.put("DAILY_RAINFALL_MM", normalized.getRolling24h());
        if (normalized.getRolling6h() != null) metricsMap.put("ROLLING_6H_RAINFALL_MM", normalized.getRolling6h());

        HazardScoringConfig config = HazardScoringConfig.getConfig(HazardType.EXTREME_RAINFALL)
                .orElseThrow(() -> new IllegalStateException("Missing EXTREME_RAINFALL scoring configuration"));

        HazardScoringEngine.ScoringResult result = scoringEngine.calculateScore(metricsMap, config);
        dto.setRollingRainfallScore(result.hazardScore());
        dto.setSeverityTier(result.severityTier());
        dto.setMetricContributions(result.contributions());

        return dto;
    }

    /**
     * Compiles an executive summary report of single-hazard scores and tier distributions.
     */
    public HazardScoringSummaryDto getHazardScoringSummary() {
        HazardScoringSummaryDto summary = new HazardScoringSummaryDto();
        summary.setCanonicalCrs("EPSG:4326 (WGS 84)");

        // Collect all scored observations
        List<HazardScoreDto> allScores = getAllHazardScores(null, null, null, null, 1000);
        summary.setTotalScoredObservations(allScores.size());

        Map<String, Long> tierDist = new LinkedHashMap<>();
        for (SeverityTier tier : SeverityTier.values()) {
            tierDist.put(tier.name(), allScores.stream().filter(s -> s.getSeverityTier() == tier).count());
        }
        summary.setSeverityTierDistribution(tierDist);

        Map<String, Long> typeDist = new LinkedHashMap<>();
        for (HazardType type : HazardType.values()) {
            long count = allScores.stream().filter(s -> s.getHazardType() == type).count();
            if (count > 0) {
                typeDist.put(type.name(), count);
            }
        }
        summary.setHazardTypeDistribution(typeDist);

        List<HazardScoringSummaryDto.ScoringConfigSummaryDto> configs = HazardScoringConfig.getAllConfigs().stream()
                .map(c -> new HazardScoringSummaryDto.ScoringConfigSummaryDto(
                        c.getHazardType().name(),
                        c.getMetricWeights(),
                        1.0000,
                        c.getDescription()
                ))
                .collect(Collectors.toList());
        summary.setActiveScoringConfigurations(configs);

        var normSummary = hazardNormalizationService.getNormalizationSummary();
        summary.setActiveStations(normSummary.getActiveStations());
        summary.setCoveredDistricts(normSummary.getSupportedDistricts());

        return summary;
    }

    /**
     * Delivers scored hazard observations as an RFC 7946 GeoJSON FeatureCollection.
     */
    public GeoJsonFeatureCollectionDto getHazardScoresGeoJson(HazardType type, String district, Integer limit) {
        List<HazardScoreDto> scores = getAllHazardScores(type, district, null, QualityStatus.VALID, limit);
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (HazardScoreDto s : scores) {
            if (s.getLongitude() != null && s.getLatitude() != null) {
                GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(s.getLongitude(), s.getLatitude());

                Map<String, Object> props = new LinkedHashMap<>();
                props.put("id", s.getId());
                props.put("hazardType", s.getHazardType().name());
                props.put("dataSource", s.getDataSource());
                props.put("locationName", s.getLocationName());
                props.put("associatedDistrict", s.getAssociatedDistrict());
                props.put("isWithinBiharBoundary", s.getIsWithinBiharBoundary());
                props.put("startDate", s.getStartDate() != null ? s.getStartDate().toString() : null);
                props.put("endDate", s.getEndDate() != null ? s.getEndDate().toString() : null);
                props.put("qualityStatus", s.getQualityStatus().name());
                props.put("hazardScore", s.getHazardScore());
                props.put("severityTier", s.getSeverityTier() != null ? s.getSeverityTier().name() : null);
                props.put("completenessRatio", s.getCompletenessRatio());
                props.put("explanation", s.getExplanation());

                features.add(new GeoJsonFeatureDto(s.getId(), geom, props));
            }
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    // =========================================================================
    // HELPER SCORING LOGIC
    // =========================================================================

    public HazardScoreDto scoreObservation(NormalizedHazardObservation normalized) {
        if (normalized == null) {
            return null;
        }

        HazardScoreDto dto = new HazardScoreDto();
        dto.setId(normalized.getId());
        dto.setSourceRecordId(normalized.getSourceRecordId());
        dto.setHazardType(normalized.getHazardType());
        dto.setDataSource(normalized.getDataSource());
        dto.setLocationName(normalized.getLocationName());
        dto.setAssociatedDistrict(normalized.getAssociatedDistrict());
        dto.setIsWithinBiharBoundary(normalized.getIsWithinBiharBoundary());
        dto.setLongitude(normalized.getLongitude());
        dto.setLatitude(normalized.getLatitude());
        dto.setStartDate(normalized.getStartDate());
        dto.setEndDate(normalized.getEndDate());
        dto.setTimestamp(normalized.getTimestamp());
        dto.setQualityStatus(normalized.getQualityStatus());
        dto.setProcessingMetadata(normalized.getProcessingMetadata());

        // Skip scoring for INVALID quality
        if (normalized.getQualityStatus() == QualityStatus.INVALID) {
            dto.setExplanation("Observation marked INVALID - scoring bypassed");
            return dto;
        }

        Optional<HazardScoringConfig> configOpt = HazardScoringConfig.getConfig(normalized.getHazardType());
        if (configOpt.isEmpty()) {
            dto.setExplanation("No scoring configuration registered for hazard type: " + normalized.getHazardType());
            return dto;
        }

        HazardScoringEngine.ScoringResult result = scoringEngine.calculateScore(
                normalized.getNormalizedMetrics(), configOpt.get()
        );

        dto.setHazardScore(result.hazardScore());
        dto.setSeverityTier(result.severityTier());
        dto.setCompletenessRatio(result.completenessRatio());
        dto.setMetricContributions(result.contributions());
        dto.setExplanation(result.explanation());

        return dto;
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
