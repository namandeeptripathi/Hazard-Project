package com.hazard.service.safesite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Stage 5.9 — Configurable Supporting Infrastructure Accessibility & Proximity Parameters.
 *
 * Configurable thresholds:
 * - nearInfrastructureDistanceMeters: Maximum distance in meters to nearest useful supporting facility to be classified as NEAR (default: 2000.0m = 2.0km).
 * - farInfrastructureDistanceMeters: Minimum distance in meters from nearest useful supporting facility to be classified as FAR (default: 10000.0m = 10.0km).
 * - Intermediate range (nearInfrastructureDistanceMeters < distance < farInfrastructureDistanceMeters) evaluates to MODERATE.
 *
 * Note: These are configurable application thresholds for emergency response triage and safe-site evaluation,
 * not scientifically validated standards.
 */
@Component
@ConfigurationProperties(prefix = "hazard.safesite.infrastructure")
public class InfrastructureEvaluationConfig {

    /**
     * Maximum distance in meters to qualify as NEAR supporting infrastructure proximity (<= 2.0km).
     */
    private double nearInfrastructureDistanceMeters = 2000.0;

    /**
     * Minimum distance in meters to qualify as FAR supporting infrastructure proximity (>= 10.0km).
     */
    private double farInfrastructureDistanceMeters = 10000.0;

    public InfrastructureEvaluationConfig() {
    }

    public InfrastructureEvaluationConfig(double nearInfrastructureDistanceMeters, double farInfrastructureDistanceMeters) {
        this.nearInfrastructureDistanceMeters = nearInfrastructureDistanceMeters;
        this.farInfrastructureDistanceMeters = farInfrastructureDistanceMeters;
    }

    public double getNearInfrastructureDistanceMeters() {
        return nearInfrastructureDistanceMeters;
    }

    public void setNearInfrastructureDistanceMeters(double nearInfrastructureDistanceMeters) {
        this.nearInfrastructureDistanceMeters = nearInfrastructureDistanceMeters;
    }

    public double getFarInfrastructureDistanceMeters() {
        return farInfrastructureDistanceMeters;
    }

    public void setFarInfrastructureDistanceMeters(double farInfrastructureDistanceMeters) {
        this.farInfrastructureDistanceMeters = farInfrastructureDistanceMeters;
    }
}
