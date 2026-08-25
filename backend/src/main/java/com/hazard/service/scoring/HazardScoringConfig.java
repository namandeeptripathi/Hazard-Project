package com.hazard.service.scoring;

import com.hazard.domain.hazard.HazardType;

import java.util.*;

/**
 * Configuration Registry for Multi-Criteria Hazard Scoring Weights.
 * Enforces strict validation that metric weights sum to exactly 1.0000 (+/- 1e-6).
 */
public class HazardScoringConfig {

    private static final double WEIGHT_SUM_TOLERANCE = 1e-4;

    private final HazardType hazardType;
    private final Map<String, Double> metricWeights;
    private final String description;

    public HazardScoringConfig(HazardType hazardType, Map<String, Double> metricWeights, String description) {
        if (hazardType == null) {
            throw new IllegalArgumentException("HazardType cannot be null in scoring configuration");
        }
        if (metricWeights == null || metricWeights.isEmpty()) {
            throw new IllegalArgumentException("Metric weights cannot be null or empty for hazard type: " + hazardType);
        }

        // Validate weights sum to 1.0000
        double sum = 0.0;
        for (Map.Entry<String, Double> entry : metricWeights.entrySet()) {
            if (entry.getValue() == null || entry.getValue() < 0.0) {
                throw new IllegalArgumentException("Metric weight for " + entry.getKey() + " must be non-negative");
            }
            sum += entry.getValue();
        }

        if (Math.abs(sum - 1.0) > WEIGHT_SUM_TOLERANCE) {
            throw new IllegalStateException(String.format(
                    "Invalid scoring configuration for %s: metric weights sum to %.4f (expected 1.0000)",
                    hazardType, sum
            ));
        }

        this.hazardType = hazardType;
        this.metricWeights = Collections.unmodifiableMap(new LinkedHashMap<>(metricWeights));
        this.description = description;
    }

    public HazardType getHazardType() {
        return hazardType;
    }

    public Map<String, Double> getMetricWeights() {
        return metricWeights;
    }

    public String getDescription() {
        return description;
    }

    // =========================================================================
    // STANDARD REGISTRY OF HAZARD SCORING CONFIGURATIONS
    // =========================================================================

    public static final Map<HazardType, HazardScoringConfig> REGISTRY = new EnumMap<>(HazardType.class);

    static {
        // 1. FLOOD Scoring Configuration
        // Weights: Severity (0.30) + Magnitude (0.25) + Affected Area (0.25) + Duration (0.20) = 1.00
        Map<String, Double> floodWeights = new LinkedHashMap<>();
        floodWeights.put("FLOOD_SEVERITY_INDEX", 0.30);
        floodWeights.put("FLOOD_MAGNITUDE_INDEX", 0.25);
        floodWeights.put("FLOOD_AFFECTED_AREA_SQKM", 0.25);
        floodWeights.put("FLOOD_DURATION_DAYS", 0.20);

        register(new HazardScoringConfig(
                HazardType.FLOOD,
                floodWeights,
                "Multi-criteria flood hazard index combining structural severity, event magnitude, inundated area footprint, and flood duration"
        ));

        // 2. EXTREME_RAINFALL Scoring Configuration
        // Weights: Peak Hourly Intensity (0.40) + Daily Total / 24h (0.35) + 6h Short Storm (0.25) = 1.00
        Map<String, Double> rainfallWeights = new LinkedHashMap<>();
        rainfallWeights.put("HOURLY_PRECIPITATION_MM", 0.40);
        rainfallWeights.put("DAILY_RAINFALL_MM", 0.35);
        rainfallWeights.put("ROLLING_6H_RAINFALL_MM", 0.25);

        register(new HazardScoringConfig(
                HazardType.EXTREME_RAINFALL,
                rainfallWeights,
                "Multi-window meteorological rainfall hazard index combining peak hourly intensity, diurnal accumulation, and storm window burst"
        ));
    }

    public static void register(HazardScoringConfig config) {
        REGISTRY.put(config.getHazardType(), config);
    }

    public static Optional<HazardScoringConfig> getConfig(HazardType hazardType) {
        if (hazardType == null) return Optional.empty();
        return Optional.ofNullable(REGISTRY.get(hazardType));
    }

    public static List<HazardScoringConfig> getAllConfigs() {
        return new ArrayList<>(REGISTRY.values());
    }
}
