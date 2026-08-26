package com.hazard.service.safesite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Stage 5.6 — Configurable Road Accessibility & Proximity Parameters.
 *
 * Configurable thresholds:
 * - nearRoadDistanceMeters: Maximum distance in meters for a site to be considered NEAR an available road (default: 500.0m = 0.5km).
 * - farRoadDistanceMeters: Minimum distance in meters for a site to be considered FAR from the road network (default: 2000.0m = 2.0km).
 * - Intermediate range (nearRoadDistanceMeters < distance < farRoadDistanceMeters) evaluates to MODERATE.
 *
 * Note: These are configurable application thresholds for triage and pilot planning,
 * not scientifically validated engineering standards.
 */
@Component
@ConfigurationProperties(prefix = "hazard.safesite.road")
public class RoadAccessEvaluationConfig {

    /**
     * Maximum distance in meters to qualify as NEAR road proximity (<= 500m).
     */
    private double nearRoadDistanceMeters = 500.0;

    /**
     * Minimum distance in meters to qualify as FAR road proximity (>= 2000m).
     */
    private double farRoadDistanceMeters = 2000.0;

    public double getNearRoadDistanceMeters() {
        return nearRoadDistanceMeters;
    }

    public void setNearRoadDistanceMeters(double nearRoadDistanceMeters) {
        this.nearRoadDistanceMeters = nearRoadDistanceMeters;
    }

    public double getFarRoadDistanceMeters() {
        return farRoadDistanceMeters;
    }

    public void setFarRoadDistanceMeters(double farRoadDistanceMeters) {
        this.farRoadDistanceMeters = farRoadDistanceMeters;
    }
}
