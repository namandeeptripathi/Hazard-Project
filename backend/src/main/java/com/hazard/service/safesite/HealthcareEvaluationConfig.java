package com.hazard.service.safesite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Stage 5.7 — Configurable Healthcare Accessibility & Proximity Parameters.
 *
 * Configurable thresholds:
 * - nearHealthcareDistanceMeters: Maximum distance in meters to nearest healthcare facility to be classified as NEAR (default: 5000.0m = 5.0km).
 * - farHealthcareDistanceMeters: Minimum distance in meters from nearest healthcare facility to be classified as FAR (default: 20000.0m = 20.0km).
 * - Intermediate range (nearHealthcareDistanceMeters < distance < farHealthcareDistanceMeters) evaluates to MODERATE.
 *
 * Note: These are configurable application thresholds for emergency response triage and safe-site evaluation,
 * not scientifically validated medical care standards.
 */
@Component
@ConfigurationProperties(prefix = "hazard.safesite.healthcare")
public class HealthcareEvaluationConfig {

    /**
     * Maximum distance in meters to qualify as NEAR healthcare proximity (<= 5.0km).
     */
    private double nearHealthcareDistanceMeters = 5000.0;

    /**
     * Minimum distance in meters to qualify as FAR healthcare proximity (>= 20.0km).
     */
    private double farHealthcareDistanceMeters = 20000.0;

    public HealthcareEvaluationConfig() {
    }

    public HealthcareEvaluationConfig(double nearHealthcareDistanceMeters, double farHealthcareDistanceMeters) {
        this.nearHealthcareDistanceMeters = nearHealthcareDistanceMeters;
        this.farHealthcareDistanceMeters = farHealthcareDistanceMeters;
    }

    public double getNearHealthcareDistanceMeters() {
        return nearHealthcareDistanceMeters;
    }

    public void setNearHealthcareDistanceMeters(double nearHealthcareDistanceMeters) {
        this.nearHealthcareDistanceMeters = nearHealthcareDistanceMeters;
    }

    public double getFarHealthcareDistanceMeters() {
        return farHealthcareDistanceMeters;
    }

    public void setFarHealthcareDistanceMeters(double farHealthcareDistanceMeters) {
        this.farHealthcareDistanceMeters = farHealthcareDistanceMeters;
    }
}
