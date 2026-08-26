package com.hazard.service.safesite;

import org.springframework.stereotype.Component;

/**
 * Configuration parameters and configurable thresholds for Stage 5.4 Terrain / Slope Intelligence.
 */
@Component
public class TerrainEvaluationConfig {

    /**
     * Maximum slope in degrees considered favorable (flat to gentle terrain).
     * Slopes <= 5.0° provide optimal stability and unimpeded accessibility for emergency evacuation.
     */
    private double maxFavorableSlopeDegrees = 5.0;

    /**
     * Minimum slope in degrees considered unfavorable (steep terrain).
     * Slopes >= 15.0° present heightened risk of slope failure, fast runoff, and severe accessibility barriers.
     */
    private double minUnfavorableSlopeDegrees = 15.0;

    public TerrainEvaluationConfig() {
    }

    public TerrainEvaluationConfig(double maxFavorableSlopeDegrees, double minUnfavorableSlopeDegrees) {
        this.maxFavorableSlopeDegrees = maxFavorableSlopeDegrees;
        this.minUnfavorableSlopeDegrees = minUnfavorableSlopeDegrees;
    }

    public double getMaxFavorableSlopeDegrees() {
        return maxFavorableSlopeDegrees;
    }

    public void setMaxFavorableSlopeDegrees(double maxFavorableSlopeDegrees) {
        this.maxFavorableSlopeDegrees = maxFavorableSlopeDegrees;
    }

    public double getMinUnfavorableSlopeDegrees() {
        return minUnfavorableSlopeDegrees;
    }

    public void setMinUnfavorableSlopeDegrees(double minUnfavorableSlopeDegrees) {
        this.minUnfavorableSlopeDegrees = minUnfavorableSlopeDegrees;
    }
}
