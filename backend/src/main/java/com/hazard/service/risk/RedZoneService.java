package com.hazard.service.risk;

import com.hazard.domain.risk.ZoneLevel;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.RedZoneDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Stage 5.1 — Dynamic Red-Zone Generation Service.
 *
 * This service is a thin classification layer on top of the existing
 * RiskCalculationService. It does NOT duplicate any risk calculation logic.
 * It takes the existing Stage 4 risk outputs, classifies districts into
 * zone levels, identifies red zones, and enriches GeoJSON output.
 */
@Service
@Transactional(readOnly = true)
public class RedZoneService {

    private static final Logger log = LoggerFactory.getLogger(RedZoneService.class);

    private final RiskCalculationService riskCalculationService;

    public RedZoneService(RiskCalculationService riskCalculationService) {
        this.riskCalculationService = riskCalculationService;
    }

    /**
     * Returns all districts classified into zone levels, sorted by risk score descending.
     * Dynamically derived from current risk data — when risk changes, zones change.
     */
    public List<RedZoneDto> getAllRiskZones() {
        List<DistrictRiskScoreDto> riskScores = riskCalculationService.getAllDistrictsRiskScores();
        return riskScores.stream()
                .map(RedZoneDto::fromDistrictRiskScore)
                .sorted(Comparator.comparingDouble((RedZoneDto z) ->
                        z.getRiskScore() != null ? z.getRiskScore() : 0.0).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns only RED-ZONE districts (VERY_HIGH and CRITICAL risk tiers),
     * sorted by risk score descending. These are the districts requiring
     * immediate attention and emergency response prioritization.
     */
    public List<RedZoneDto> getRedZonesOnly() {
        return getAllRiskZones().stream()
                .filter(RedZoneDto::isRedZone)
                .collect(Collectors.toList());
    }

    /**
     * Returns districts matching or exceeding the specified minimum zone level.
     * Useful for filtering: e.g., "show me all districts at HIGH or above."
     */
    public List<RedZoneDto> getZonesByMinimumLevel(ZoneLevel minLevel) {
        if (minLevel == null) {
            return getAllRiskZones();
        }
        return getAllRiskZones().stream()
                .filter(z -> z.getZoneLevel() != null && z.getZoneLevel().ordinal() >= minLevel.ordinal())
                .collect(Collectors.toList());
    }

    /**
     * Returns a zone-level summary: count of districts per zone level.
     */
    public Map<ZoneLevel, Long> getZoneLevelSummary() {
        return getAllRiskZones().stream()
                .collect(Collectors.groupingBy(RedZoneDto::getZoneLevel, Collectors.counting()));
    }

    /**
     * Generates an enriched GeoJSON FeatureCollection with zone classification properties.
     * Reuses the existing RiskCalculationService.generateRiskGeoJson() output and
     * adds zoneLevel, isRedZone, and zoneColorHex properties to each feature.
     */
    public GeoJsonFeatureCollectionDto generateRedZoneGeoJson() {
        GeoJsonFeatureCollectionDto baseGeoJson = riskCalculationService.generateRiskGeoJson();

        for (GeoJsonFeatureDto feature : baseGeoJson.getFeatures()) {
            Map<String, Object> props = feature.getProperties();

            // Extract the existing riskTier property set by Stage 4
            // Missing or invalid riskTier → null → UNKNOWN zone (never silently treated as LOW)
            com.hazard.domain.risk.RiskTier riskTier = null;
            if (props.containsKey("riskTier") && props.get("riskTier") != null) {
                try {
                    riskTier = com.hazard.domain.risk.RiskTier.valueOf(props.get("riskTier").toString());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid riskTier value '{}' in GeoJSON feature, classifying as UNKNOWN", props.get("riskTier"));
                    riskTier = null;
                }
            }

            ZoneLevel zoneLevel = ZoneLevel.fromRiskTier(riskTier);

            // Enrich the feature properties with zone classification
            props.put("zoneLevel", zoneLevel.name());
            props.put("zoneLevelDisplay", zoneLevel.getDisplayName());
            props.put("isRedZone", zoneLevel.isRedZone());
            props.put("zoneColorHex", zoneLevel.getColorHex());
            props.put("zoneDescription", zoneLevel.getDescription());
            props.put("layerId", "DISTRICT_RED_ZONE");
        }

        return baseGeoJson;
    }

    /**
     * Generates GeoJSON containing only red-zone districts.
     */
    public GeoJsonFeatureCollectionDto generateRedZoneOnlyGeoJson() {
        GeoJsonFeatureCollectionDto fullGeoJson = generateRedZoneGeoJson();

        List<GeoJsonFeatureDto> redFeatures = fullGeoJson.getFeatures().stream()
                .filter(f -> {
                    Object isRed = f.getProperties().get("isRedZone");
                    return Boolean.TRUE.equals(isRed);
                })
                .collect(Collectors.toList());

        return new GeoJsonFeatureCollectionDto(redFeatures);
    }
}
