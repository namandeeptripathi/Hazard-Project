package com.hazard.service.safesite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Stage 5.8 — Configurable Water Accessibility & Proximity Parameters.
 *
 * Configurable thresholds:
 * - nearWaterDistanceMeters: Maximum distance in meters to nearest useful water facility to be classified as NEAR (default: 1000.0m = 1.0km).
 * - farWaterDistanceMeters: Minimum distance in meters from nearest useful water facility to be classified as FAR (default: 5000.0m = 5.0km).
 * - Intermediate range (nearWaterDistanceMeters < distance < farWaterDistanceMeters) evaluates to MODERATE.
 *
 * Note: These are configurable application thresholds for emergency response triage and safe-site evaluation,
 * not scientifically validated public health or water engineering standards.
 */
@Component
@ConfigurationProperties(prefix = "hazard.safesite.water")
public class WaterEvaluationConfig {

    /**
     * Maximum distance in meters to qualify as NEAR water proximity (<= 1.0km).
     */
    private double nearWaterDistanceMeters = 1000.0;

    /**
     * Minimum distance in meters to qualify as FAR water proximity (>= 5.0km).
     */
    private double farWaterDistanceMeters = 5000.0;

    public WaterEvaluationConfig() {
    }

    public WaterEvaluationConfig(double nearWaterDistanceMeters, double farWaterDistanceMeters) {
        this.nearWaterDistanceMeters = nearWaterDistanceMeters;
        this.farWaterDistanceMeters = farWaterDistanceMeters;
    }

    public double getNearWaterDistanceMeters() {
        return nearWaterDistanceMeters;
    }

    public void setNearWaterDistanceMeters(double nearWaterDistanceMeters) {
        this.nearWaterDistanceMeters = nearWaterDistanceMeters;
    }

    public double getFarWaterDistanceMeters() {
        return farWaterDistanceMeters;
    }

    public void setFarWaterDistanceMeters(double farWaterDistanceMeters) {
        this.farWaterDistanceMeters = farWaterDistanceMeters;
    }
}
