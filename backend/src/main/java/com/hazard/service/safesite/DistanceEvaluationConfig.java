package com.hazard.service.safesite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Stage 5.5 — Distance Evaluation Configuration.
 *
 * Configurable geodesic distance thresholds for proximity categorization:
 * - nearDistanceKm: Maximum distance (in km) for NEAR classification (default: 5.0 km).
 * - farDistanceKm: Minimum distance (in km) for FAR classification (default: 20.0 km).
 * - Values between nearDistanceKm and farDistanceKm are classified as MODERATE.
 *
 * NOTE: These are configurable operational planning thresholds and are not claimed
 * to be universally validated physical constants.
 */
@Component
@ConfigurationProperties(prefix = "hazard.safe-site.distance")
public class DistanceEvaluationConfig {

    /**
     * Maximum distance in kilometers for NEAR classification (inclusive: <= nearDistanceKm).
     * Sites within this radius are considered readily accessible for local evacuation.
     */
    private double nearDistanceKm = 5.0;

    /**
     * Minimum distance in kilometers for FAR classification (inclusive: >= farDistanceKm).
     * Sites beyond this radius require extended transit from the disaster zone.
     */
    private double farDistanceKm = 20.0;

    public DistanceEvaluationConfig() {
    }

    public DistanceEvaluationConfig(double nearDistanceKm, double farDistanceKm) {
        this.nearDistanceKm = nearDistanceKm;
        this.farDistanceKm = farDistanceKm;
    }

    public double getNearDistanceKm() {
        return nearDistanceKm;
    }

    public void setNearDistanceKm(double nearDistanceKm) {
        this.nearDistanceKm = nearDistanceKm;
    }

    public double getFarDistanceKm() {
        return farDistanceKm;
    }

    public void setFarDistanceKm(double farDistanceKm) {
        this.farDistanceKm = farDistanceKm;
    }
}
