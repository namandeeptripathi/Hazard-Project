package com.hazard.service.multihazard;

import com.hazard.domain.hazard.HazardType;

import java.util.*;

/**
 * Configuration Registry for Cross-Hazard Multi-Hazard Aggregation Weights and Matching Thresholds.
 * Enforces strict validation that multi-hazard weights sum to 1.0000 (+/- 1e-4).
 */
public class MultiHazardConfig {

    private static final double WEIGHT_SUM_TOLERANCE = 1e-4;
    public static final double DEFAULT_SPATIAL_PROXIMITY_RADIUS_METERS = 25000.0; // 25 km
    public static final int DEFAULT_TEMPORAL_BUFFER_DAYS = 3; // 3 days

    private final Map<HazardType, Double> hazardWeights;
    private final double spatialProximityRadiusMeters;
    private final int temporalBufferDays;
    private final String description;

    public MultiHazardConfig(Map<HazardType, Double> hazardWeights,
                             double spatialProximityRadiusMeters,
                             int temporalBufferDays,
                             String description) {
        if (hazardWeights == null || hazardWeights.isEmpty()) {
            throw new IllegalArgumentException("Hazard weights cannot be null or empty in multi-hazard config");
        }

        double sum = 0.0;
        for (Map.Entry<HazardType, Double> entry : hazardWeights.entrySet()) {
            if (entry.getValue() == null || entry.getValue() < 0.0) {
                throw new IllegalArgumentException("Hazard weight for " + entry.getKey() + " must be non-negative");
            }
            sum += entry.getValue();
        }

        if (Math.abs(sum - 1.0) > WEIGHT_SUM_TOLERANCE) {
            throw new IllegalStateException(String.format(
                    "Invalid multi-hazard configuration: weights sum to %.4f (expected 1.0000)", sum
            ));
        }

        this.hazardWeights = Collections.unmodifiableMap(new LinkedHashMap<>(hazardWeights));
        this.spatialProximityRadiusMeters = spatialProximityRadiusMeters;
        this.temporalBufferDays = temporalBufferDays;
        this.description = description;
    }

    public Map<HazardType, Double> getHazardWeights() {
        return hazardWeights;
    }

    public double getSpatialProximityRadiusMeters() {
        return spatialProximityRadiusMeters;
    }

    public int getTemporalBufferDays() {
        return temporalBufferDays;
    }

    public String getDescription() {
        return description;
    }

    // =========================================================================
    // DEFAULT STANDARD MULTI-HAZARD CONFIGURATION
    // =========================================================================

    public static MultiHazardConfig createDefault() {
        Map<HazardType, Double> defaultWeights = new LinkedHashMap<>();
        defaultWeights.put(HazardType.FLOOD, 0.50);
        defaultWeights.put(HazardType.EXTREME_RAINFALL, 0.50);

        return new MultiHazardConfig(
                defaultWeights,
                DEFAULT_SPATIAL_PROXIMITY_RADIUS_METERS,
                DEFAULT_TEMPORAL_BUFFER_DAYS,
                "MVP Balanced Multi-Hazard Index combining flood inundation severity and extreme rainfall accumulation (50% Flood + 50% Extreme Rainfall)"
        );
    }
}
