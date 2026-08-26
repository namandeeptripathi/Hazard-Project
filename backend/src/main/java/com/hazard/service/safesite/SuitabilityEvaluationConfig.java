package com.hazard.service.safesite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Stage 5.10 — Configurable Site Suitability Evaluation Parameters & Weights.
 *
 * Configurable parameters:
 * - Dimension Weights (defaults sum to 1.00 / 100%):
 *   - hazardSafetyWeight: 0.30 (30%)
 *   - terrainWeight: 0.15 (15%)
 *   - distanceWeight: 0.15 (15%)
 *   - roadsWeight: 0.10 (10%)
 *   - healthcareWeight: 0.10 (10%)
 *   - waterWeight: 0.10 (10%)
 *   - infrastructureWeight: 0.10 (10%)
 *
 * - Suitability Classification Score Thresholds:
 *   - highlySuitableMinScore: Minimum score for HIGHLY_SUITABLE (default: 90.0)
 *   - suitableMinScore: Minimum score for SUITABLE (default: 70.0)
 *   - marginalMinScore: Minimum score for MARGINAL (default: 40.0)
 *   - Below marginalMinScore evaluates to UNSUITABLE.
 *
 * - Dimension Value Scoring (0 to 100 scale):
 *   - optimalScore: 100.0 (SAFE, FAVORABLE, NEAR)
 *   - moderateScore: 60.0 (MODERATE)
 *   - farScore: 20.0 (FAR)
 *   - poorScore: 0.0 (AT_RISK, UNFAVORABLE)
 *
 * Note: These are configurable application thresholds and weights for emergency safe-site evaluation,
 * not hardcoded scientific constants.
 */
@Component
@ConfigurationProperties(prefix = "hazard.safesite.suitability")
public class SuitabilityEvaluationConfig {

    // Dimension Weights
    private double hazardSafetyWeight = 0.30;
    private double terrainWeight = 0.15;
    private double distanceWeight = 0.15;
    private double roadsWeight = 0.10;
    private double healthcareWeight = 0.10;
    private double waterWeight = 0.10;
    private double infrastructureWeight = 0.10;

    // Score Band Thresholds
    private double highlySuitableMinScore = 90.0;
    private double suitableMinScore = 70.0;
    private double marginalMinScore = 40.0;

    // Status-to-Numeric Value Scores
    private double optimalScore = 100.0;
    private double moderateScore = 60.0;
    private double farScore = 20.0;
    private double poorScore = 0.0;

    public SuitabilityEvaluationConfig() {
    }

    public SuitabilityEvaluationConfig(double hazardSafetyWeight, double terrainWeight, double distanceWeight,
                                      double roadsWeight, double healthcareWeight, double waterWeight,
                                      double infrastructureWeight) {
        this.hazardSafetyWeight = hazardSafetyWeight;
        this.terrainWeight = terrainWeight;
        this.distanceWeight = distanceWeight;
        this.roadsWeight = roadsWeight;
        this.healthcareWeight = healthcareWeight;
        this.waterWeight = waterWeight;
        this.infrastructureWeight = infrastructureWeight;
    }

    public double getHazardSafetyWeight() {
        return hazardSafetyWeight;
    }

    public void setHazardSafetyWeight(double hazardSafetyWeight) {
        this.hazardSafetyWeight = hazardSafetyWeight;
    }

    public double getTerrainWeight() {
        return terrainWeight;
    }

    public void setTerrainWeight(double terrainWeight) {
        this.terrainWeight = terrainWeight;
    }

    public double getDistanceWeight() {
        return distanceWeight;
    }

    public void setDistanceWeight(double distanceWeight) {
        this.distanceWeight = distanceWeight;
    }

    public double getRoadsWeight() {
        return roadsWeight;
    }

    public void setRoadsWeight(double roadsWeight) {
        this.roadsWeight = roadsWeight;
    }

    public double getHealthcareWeight() {
        return healthcareWeight;
    }

    public void setHealthcareWeight(double healthcareWeight) {
        this.healthcareWeight = healthcareWeight;
    }

    public double getWaterWeight() {
        return waterWeight;
    }

    public void setWaterWeight(double waterWeight) {
        this.waterWeight = waterWeight;
    }

    public double getInfrastructureWeight() {
        return infrastructureWeight;
    }

    public void setInfrastructureWeight(double infrastructureWeight) {
        this.infrastructureWeight = infrastructureWeight;
    }

    public double getHighlySuitableMinScore() {
        return highlySuitableMinScore;
    }

    public void setHighlySuitableMinScore(double highlySuitableMinScore) {
        this.highlySuitableMinScore = highlySuitableMinScore;
    }

    public double getSuitableMinScore() {
        return suitableMinScore;
    }

    public void setSuitableMinScore(double suitableMinScore) {
        this.suitableMinScore = suitableMinScore;
    }

    public double getMarginalMinScore() {
        return marginalMinScore;
    }

    public void setMarginalMinScore(double marginalMinScore) {
        this.marginalMinScore = marginalMinScore;
    }

    public double getOptimalScore() {
        return optimalScore;
    }

    public void setOptimalScore(double optimalScore) {
        this.optimalScore = optimalScore;
    }

    public double getModerateScore() {
        return moderateScore;
    }

    public void setModerateScore(double moderateScore) {
        this.moderateScore = moderateScore;
    }

    public double getFarScore() {
        return farScore;
    }

    public void setFarScore(double farScore) {
        this.farScore = farScore;
    }

    public double getPoorScore() {
        return poorScore;
    }

    public void setPoorScore(double poorScore) {
        this.poorScore = poorScore;
    }
}
