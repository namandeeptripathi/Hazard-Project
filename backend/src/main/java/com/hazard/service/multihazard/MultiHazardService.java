package com.hazard.service.multihazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.MultiHazardConfidence;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.domain.hazard.SpatialRelationship;
import com.hazard.domain.hazard.TemporalRelationship;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.GeoJsonGeometryDto;
import com.hazard.dto.multihazard.HazardParticipationDto;
import com.hazard.dto.multihazard.MultiHazardObservation;
import com.hazard.dto.multihazard.MultiHazardSummaryDto;
import com.hazard.dto.scoring.HazardScoreDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.service.scoring.HazardScoringService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Master Domain Service for Sub-Stage 3.5 Multi-Hazard Integration.
 * Evaluates spatial and temporal coincidence across distinct hazard types,
 * performs multi-criteria cross-hazard aggregation, and exposes GeoJSON vector feeds.
 */
@Service
@Transactional(readOnly = true)
public class MultiHazardService {

    public static final int DEFAULT_LIMIT = 50;
    public static final int MAX_LIMIT = 1000;

    private final HazardScoringService hazardScoringService;
    private final MultiHazardSpatialTemporalMatcher spatialTemporalMatcher;
    private final MultiHazardAggregationEngine aggregationEngine;
    private final MultiHazardConfig defaultConfig;

    public MultiHazardService(HazardScoringService hazardScoringService,
                              MultiHazardSpatialTemporalMatcher spatialTemporalMatcher,
                              MultiHazardAggregationEngine aggregationEngine) {
        this.hazardScoringService = hazardScoringService;
        this.spatialTemporalMatcher = spatialTemporalMatcher;
        this.aggregationEngine = aggregationEngine;
        this.defaultConfig = MultiHazardConfig.createDefault();
    }

    /**
     * Retrieves all synthesized multi-hazard observations with optional filtering.
     */
    public List<MultiHazardObservation> getAllMultiHazardObservations(String district, SeverityTier severity,
                                                                      HazardType dominantHazard, Integer limit) {
        int safeLimit = sanitizeLimit(limit);
        List<MultiHazardObservation> list = synthesizeMultiHazardObservations(
                defaultConfig.getSpatialProximityRadiusMeters(),
                defaultConfig.getTemporalBufferDays()
        );

        if (district != null && !district.trim().isEmpty()) {
            String targetDist = district.trim().toUpperCase();
            list = list.stream()
                    .filter(m -> m.getAssociatedDistrict() != null && m.getAssociatedDistrict().toUpperCase().contains(targetDist))
                    .collect(Collectors.toList());
        }

        if (severity != null) {
            list = list.stream()
                    .filter(m -> m.getSeverityTier() == severity)
                    .collect(Collectors.toList());
        }

        if (dominantHazard != null) {
            list = list.stream()
                    .filter(m -> m.getDominantHazard() == dominantHazard)
                    .collect(Collectors.toList());
        }

        return list.stream().limit(safeLimit).collect(Collectors.toList());
    }

    /**
     * Retrieves a single multi-hazard observation by unified ID.
     */
    public MultiHazardObservation getMultiHazardObservationById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Multi-hazard ID cannot be null or empty");
        }

        return getAllMultiHazardObservations(null, null, null, 1000).stream()
                .filter(m -> m.getId().equalsIgnoreCase(id.trim()))
                .findFirst()
                .orElseThrow(() -> new HazardNotFoundException("Multi-hazard observation not found with ID: " + id));
    }

    /**
     * Retrieves multi-hazard observations for a specific administrative district.
     */
    public List<MultiHazardObservation> getMultiHazardObservationsInDistrict(String districtName, SeverityTier severity, Integer limit) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }
        return getAllMultiHazardObservations(districtName.trim(), severity, null, limit);
    }

    /**
     * Compiles an executive summary of multi-hazard coincidence metrics.
     */
    public MultiHazardSummaryDto getMultiHazardSummary() {
        List<MultiHazardObservation> allObservations = getAllMultiHazardObservations(null, null, null, 1000);

        MultiHazardSummaryDto summary = new MultiHazardSummaryDto();
        summary.setCanonicalCrs("EPSG:4326 (WGS 84)");
        summary.setTotalMultiHazardObservations(allObservations.size());

        summary.setFullMatchCount(allObservations.stream().filter(m -> m.getConfidence() == MultiHazardConfidence.FULL_MATCH).count());
        summary.setSpatialOnlyCount(allObservations.stream().filter(m -> m.getConfidence() == MultiHazardConfidence.SPATIAL_ONLY).count());
        summary.setTemporalOnlyCount(allObservations.stream().filter(m -> m.getConfidence() == MultiHazardConfidence.TEMPORAL_ONLY).count());
        summary.setSingleHazardCount(allObservations.stream().filter(m -> m.getConfidence() == MultiHazardConfidence.SINGLE_HAZARD_CONTEXT).count());

        Map<String, Long> tierDist = new LinkedHashMap<>();
        for (SeverityTier tier : SeverityTier.values()) {
            tierDist.put(tier.name(), allObservations.stream().filter(m -> m.getSeverityTier() == tier).count());
        }
        summary.setSeverityTierDistribution(tierDist);

        Map<String, Long> dominantDist = new LinkedHashMap<>();
        for (HazardType type : HazardType.values()) {
            long count = allObservations.stream().filter(m -> m.getDominantHazard() == type).count();
            if (count > 0) dominantDist.put(type.name(), count);
        }
        summary.setDominantHazardDistribution(dominantDist);

        Map<String, Double> weights = new LinkedHashMap<>();
        defaultConfig.getHazardWeights().forEach((k, v) -> weights.put(k.name(), v));
        summary.setConfiguredHazardWeights(weights);

        summary.setActiveDistricts(allObservations.stream()
                .map(MultiHazardObservation::getAssociatedDistrict)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));

        return summary;
    }

    /**
     * Delivers multi-hazard observations as an RFC 7946 GeoJSON FeatureCollection.
     */
    public GeoJsonFeatureCollectionDto getMultiHazardGeoJson(String district, SeverityTier severity, Integer limit) {
        List<MultiHazardObservation> observations = getAllMultiHazardObservations(district, severity, null, limit);
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (MultiHazardObservation obs : observations) {
            if (obs.getLongitude() != null && obs.getLatitude() != null) {
                GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(obs.getLongitude(), obs.getLatitude());

                Map<String, Object> props = new LinkedHashMap<>();
                props.put("id", obs.getId());
                props.put("associatedDistrict", obs.getAssociatedDistrict());
                props.put("isWithinBiharBoundary", obs.getIsWithinBiharBoundary());
                props.put("startDate", obs.getStartDate() != null ? obs.getStartDate().toString() : null);
                props.put("endDate", obs.getEndDate() != null ? obs.getEndDate().toString() : null);
                props.put("spatialRelationship", obs.getSpatialRelationship() != null ? obs.getSpatialRelationship().name() : null);
                props.put("temporalRelationship", obs.getTemporalRelationship() != null ? obs.getTemporalRelationship().name() : null);
                props.put("confidence", obs.getConfidence() != null ? obs.getConfidence().name() : null);
                props.put("multiHazardIndex", obs.getMultiHazardIndex());
                props.put("severityTier", obs.getSeverityTier() != null ? obs.getSeverityTier().name() : null);
                props.put("dominantHazard", obs.getDominantHazard() != null ? obs.getDominantHazard().name() : null);
                props.put("dominantHazardScore", obs.getDominantHazardScore());
                props.put("secondaryHazard", obs.getSecondaryHazard() != null ? obs.getSecondaryHazard().name() : null);
                props.put("completenessRatio", obs.getCompletenessRatio());
                props.put("explanation", obs.getExplanation());

                features.add(new GeoJsonFeatureDto(obs.getId(), geom, props));
            }
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    // =========================================================================
    // MULTI-HAZARD SYNTHESIS ENGINE
    // =========================================================================

    public List<MultiHazardObservation> synthesizeMultiHazardObservations(double proximityRadiusMeters,
                                                                         int temporalBufferDays) {
        List<HazardScoreDto> floodScores = hazardScoringService.getHazardScoresByType(HazardType.FLOOD, null, 100);
        List<HazardScoreDto> rainScores = hazardScoringService.getHazardScoresByType(HazardType.EXTREME_RAINFALL, null, 100);

        List<MultiHazardObservation> multiHazards = new ArrayList<>();
        Set<String> pairedRainIds = new HashSet<>();

        // 1. Evaluate cross-hazard pairs between Flood and Extreme Rainfall
        for (HazardScoreDto flood : floodScores) {
            // Unlocated records excluded from spatial matching
            if (flood.getQualityStatus() == QualityStatus.UNLOCATED || flood.getLongitude() == null) {
                continue;
            }

            boolean matched = false;
            for (HazardScoreDto rain : rainScores) {
                if (rain.getQualityStatus() == QualityStatus.UNLOCATED || rain.getLongitude() == null) {
                    continue;
                }

                SpatialRelationship spatial = spatialTemporalMatcher.evaluateSpatialRelationship(
                        flood.getLongitude(), flood.getLatitude(), flood.getAssociatedDistrict(),
                        rain.getLongitude(), rain.getLatitude(), rain.getAssociatedDistrict(),
                        proximityRadiusMeters
                );

                TemporalRelationship temporal = spatialTemporalMatcher.evaluateTemporalRelationship(
                        flood.getStartDate(), flood.getEndDate(), flood.getTimestamp(),
                        rain.getStartDate(), rain.getEndDate(), rain.getTimestamp(),
                        temporalBufferDays
                );

                // Check coincidence (either full match or spatial coincidence)
                if (spatial != SpatialRelationship.DISJOINT) {
                    MultiHazardConfidence confidence = spatialTemporalMatcher.evaluateConfidence(spatial, temporal);
                    MultiHazardObservation obs = createMultiHazardPair(flood, rain, spatial, temporal, confidence);
                    multiHazards.add(obs);
                    pairedRainIds.add(rain.getId());
                    matched = true;
                }
            }

            // If flood did not pair spatially with any rainfall, record as single-hazard context
            if (!matched) {
                multiHazards.add(createSingleHazardObservation(flood));
            }
        }

        // 2. Add remaining unpaired rain scores as single-hazard context
        for (HazardScoreDto rain : rainScores) {
            if (!pairedRainIds.contains(rain.getId()) && rain.getQualityStatus() != QualityStatus.UNLOCATED) {
                multiHazards.add(createSingleHazardObservation(rain));
            }
        }

        return multiHazards;
    }

    private MultiHazardObservation createMultiHazardPair(HazardScoreDto flood, HazardScoreDto rain,
                                                        SpatialRelationship spatial, TemporalRelationship temporal,
                                                        MultiHazardConfidence confidence) {
        MultiHazardObservation obs = new MultiHazardObservation();
        obs.setId("MULTI-" + flood.getId() + "-" + rain.getId());
        obs.setAssociatedDistrict(flood.getAssociatedDistrict() != null ? flood.getAssociatedDistrict() : rain.getAssociatedDistrict());
        obs.setIsWithinBiharBoundary(Boolean.TRUE.equals(flood.getIsWithinBiharBoundary()) || Boolean.TRUE.equals(rain.getIsWithinBiharBoundary()));
        obs.setLongitude(flood.getLongitude() != null ? flood.getLongitude() : rain.getLongitude());
        obs.setLatitude(flood.getLatitude() != null ? flood.getLatitude() : rain.getLatitude());

        obs.setStartDate(flood.getStartDate());
        obs.setEndDate(flood.getEndDate());

        obs.setSpatialRelationship(spatial);
        obs.setTemporalRelationship(temporal);
        obs.setConfidence(confidence);

        List<HazardParticipationDto> participants = new ArrayList<>();
        participants.add(toParticipation(flood));
        participants.add(toParticipation(rain));
        obs.setParticipatingHazards(participants);

        MultiHazardAggregationEngine.MultiHazardResult result = aggregationEngine.aggregate(participants, defaultConfig);

        obs.setMultiHazardIndex(result.multiHazardIndex());
        obs.setSeverityTier(result.severityTier());
        obs.setDominantHazard(result.dominantHazard());
        obs.setDominantHazardScore(result.dominantHazardScore());
        obs.setSecondaryHazard(result.secondaryHazard());
        obs.setSecondaryHazardScore(result.secondaryHazardScore());
        obs.setCompletenessRatio(result.completenessRatio());
        obs.setExplanation(result.explanation());
        obs.setProcessingMetadata(flood.getProcessingMetadata());

        return obs;
    }

    private MultiHazardObservation createSingleHazardObservation(HazardScoreDto single) {
        MultiHazardObservation obs = new MultiHazardObservation();
        obs.setId("MULTI-" + single.getId());
        obs.setAssociatedDistrict(single.getAssociatedDistrict());
        obs.setIsWithinBiharBoundary(single.getIsWithinBiharBoundary());
        obs.setLongitude(single.getLongitude());
        obs.setLatitude(single.getLatitude());
        obs.setStartDate(single.getStartDate());
        obs.setEndDate(single.getEndDate());

        obs.setSpatialRelationship(SpatialRelationship.EXACT_POINT);
        obs.setTemporalRelationship(TemporalRelationship.EXACT_OVERLAP);
        obs.setConfidence(MultiHazardConfidence.SINGLE_HAZARD_CONTEXT);

        List<HazardParticipationDto> participants = List.of(toParticipation(single));
        obs.setParticipatingHazards(participants);

        MultiHazardAggregationEngine.MultiHazardResult result = aggregationEngine.aggregate(participants, defaultConfig);

        obs.setMultiHazardIndex(result.multiHazardIndex());
        obs.setSeverityTier(result.severityTier());
        obs.setDominantHazard(result.dominantHazard());
        obs.setDominantHazardScore(result.dominantHazardScore());
        obs.setSecondaryHazard(null);
        obs.setSecondaryHazardScore(null);
        obs.setCompletenessRatio(result.completenessRatio());
        obs.setExplanation(result.explanation());
        obs.setProcessingMetadata(single.getProcessingMetadata());

        return obs;
    }

    private HazardParticipationDto toParticipation(HazardScoreDto scoreDto) {
        return new HazardParticipationDto(
                scoreDto.getId(),
                scoreDto.getHazardType(),
                scoreDto.getDataSource(),
                scoreDto.getLocationName(),
                scoreDto.getHazardScore(),
                scoreDto.getSeverityTier(),
                defaultConfig.getHazardWeights().getOrDefault(scoreDto.getHazardType(), 0.50),
                0.50,
                0.0,
                scoreDto.getStartDate(),
                scoreDto.getEndDate(),
                scoreDto.getTimestamp()
        );
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
