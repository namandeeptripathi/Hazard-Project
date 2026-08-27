package com.hazard.service.safesite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Stage 5.3 - 5.10 — Consolidated Safe-Site Evaluation Thresholds, Weights & Scoring Parameters.
 *
 * Groups all multi-criteria evaluation parameters with explicit domain units:
 * - Dimension Weights (sum = 1.00 / 100%)
 * - Proximity / Slope Boundary Thresholds
 * - Score Band Classification Thresholds
 * - Status-to-Numeric Value Scores (0 to 100 scale)
 */
@Component
@ConfigurationProperties(prefix = "hazard.safesite.suitability")
public class SafeSiteThresholds {

    // 1. Dimension Weights (Defaults sum to 1.00)
    private double hazardSafetyWeight = 0.30;
    private double terrainWeight = 0.15;
    private double distanceWeight = 0.15;
    private double roadsWeight = 0.10;
    private double healthcareWeight = 0.10;
    private double waterWeight = 0.10;
    private double infrastructureWeight = 0.10;

    // 2. Score Band Thresholds
    private double highlySuitableMinScore = 90.0;
    private double suitableMinScore = 70.0;
    private double marginalMinScore = 40.0;

    // 3. Status-to-Numeric Value Scores
    private double optimalScore = 100.0;
    private double moderateScore = 60.0;
    private double farScore = 20.0;
    private double poorScore = 0.0;

    // 4. Terrain Slope Thresholds (Degrees)
    private double maxFavorableSlopeDegrees = 5.0;
    private double minUnfavorableSlopeDegrees = 15.0;

    // 5. High-Risk Zone Distance Thresholds (Kilometers)
    private double nearDistanceKm = 5.0;
    private double farDistanceKm = 20.0;

    // 6. Facility & Network Proximity Thresholds (Meters)
    private double nearRoadDistanceMeters = 500.0;
    private double farRoadDistanceMeters = 2000.0;

    private double nearHealthcareDistanceMeters = 5000.0;
    private double farHealthcareDistanceMeters = 20000.0;

    private double nearWaterDistanceMeters = 1000.0;
    private double farWaterDistanceMeters = 5000.0;

    private double nearInfrastructureDistanceMeters = 2000.0;
    private double farInfrastructureDistanceMeters = 10000.0;

    public SafeSiteThresholds() {
    }

    public SafeSiteThresholds(double hazardSafetyWeight, double terrainWeight, double distanceWeight,
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

    // Getters and Setters
    public double getHazardSafetyWeight() { return hazardSafetyWeight; }
    public void setHazardSafetyWeight(double hazardSafetyWeight) { this.hazardSafetyWeight = hazardSafetyWeight; }

    public double getTerrainWeight() { return terrainWeight; }
    public void setTerrainWeight(double terrainWeight) { this.terrainWeight = terrainWeight; }

    public double getDistanceWeight() { return distanceWeight; }
    public void setDistanceWeight(double distanceWeight) { this.distanceWeight = distanceWeight; }

    public double getRoadsWeight() { return roadsWeight; }
    public void setRoadsWeight(double roadsWeight) { this.roadsWeight = roadsWeight; }

    public double getHealthcareWeight() { return healthcareWeight; }
    public void setHealthcareWeight(double healthcareWeight) { this.healthcareWeight = healthcareWeight; }

    public double getWaterWeight() { return waterWeight; }
    public void setWaterWeight(double waterWeight) { this.waterWeight = waterWeight; }

    public double getInfrastructureWeight() { return infrastructureWeight; }
    public void setInfrastructureWeight(double infrastructureWeight) { this.infrastructureWeight = infrastructureWeight; }

    public double getHighlySuitableMinScore() { return highlySuitableMinScore; }
    public void setHighlySuitableMinScore(double highlySuitableMinScore) { this.highlySuitableMinScore = highlySuitableMinScore; }

    public double getSuitableMinScore() { return suitableMinScore; }
    public void setSuitableMinScore(double suitableMinScore) { this.suitableMinScore = suitableMinScore; }

    public double getMarginalMinScore() { return marginalMinScore; }
    public void setMarginalMinScore(double marginalMinScore) { this.marginalMinScore = marginalMinScore; }

    public double getOptimalScore() { return optimalScore; }
    public void setOptimalScore(double optimalScore) { this.optimalScore = optimalScore; }

    public double getModerateScore() { return moderateScore; }
    public void setModerateScore(double moderateScore) { this.moderateScore = moderateScore; }

    public double getFarScore() { return farScore; }
    public void setFarScore(double farScore) { this.farScore = farScore; }

    public double getPoorScore() { return poorScore; }
    public void setPoorScore(double poorScore) { this.poorScore = poorScore; }

    public double getMaxFavorableSlopeDegrees() { return maxFavorableSlopeDegrees; }
    public void setMaxFavorableSlopeDegrees(double maxFavorableSlopeDegrees) { this.maxFavorableSlopeDegrees = maxFavorableSlopeDegrees; }

    public double getMinUnfavorableSlopeDegrees() { return minUnfavorableSlopeDegrees; }
    public void setMinUnfavorableSlopeDegrees(double minUnfavorableSlopeDegrees) { this.minUnfavorableSlopeDegrees = minUnfavorableSlopeDegrees; }

    public double getNearDistanceKm() { return nearDistanceKm; }
    public void setNearDistanceKm(double nearDistanceKm) { this.nearDistanceKm = nearDistanceKm; }

    public double getFarDistanceKm() { return farDistanceKm; }
    public void setFarDistanceKm(double farDistanceKm) { this.farDistanceKm = farDistanceKm; }

    public double getNearRoadDistanceMeters() { return nearRoadDistanceMeters; }
    public void setNearRoadDistanceMeters(double nearRoadDistanceMeters) { this.nearRoadDistanceMeters = nearRoadDistanceMeters; }

    public double getFarRoadDistanceMeters() { return farRoadDistanceMeters; }
    public void setFarRoadDistanceMeters(double farRoadDistanceMeters) { this.farRoadDistanceMeters = farRoadDistanceMeters; }

    public double getNearHealthcareDistanceMeters() { return nearHealthcareDistanceMeters; }
    public void setNearHealthcareDistanceMeters(double nearHealthcareDistanceMeters) { this.nearHealthcareDistanceMeters = nearHealthcareDistanceMeters; }

    public double getFarHealthcareDistanceMeters() { return farHealthcareDistanceMeters; }
    public void setFarHealthcareDistanceMeters(double farHealthcareDistanceMeters) { this.farHealthcareDistanceMeters = farHealthcareDistanceMeters; }

    public double getNearWaterDistanceMeters() { return nearWaterDistanceMeters; }
    public void setNearWaterDistanceMeters(double nearWaterDistanceMeters) { this.nearWaterDistanceMeters = nearWaterDistanceMeters; }

    public double getFarWaterDistanceMeters() { return farWaterDistanceMeters; }
    public void setFarWaterDistanceMeters(double farWaterDistanceMeters) { this.farWaterDistanceMeters = farWaterDistanceMeters; }

    public double getNearInfrastructureDistanceMeters() { return nearInfrastructureDistanceMeters; }
    public void setNearInfrastructureDistanceMeters(double nearInfrastructureDistanceMeters) { this.nearInfrastructureDistanceMeters = nearInfrastructureDistanceMeters; }

    public double getFarInfrastructureDistanceMeters() { return farInfrastructureDistanceMeters; }
    public void setFarInfrastructureDistanceMeters(double farInfrastructureDistanceMeters) { this.farInfrastructureDistanceMeters = farInfrastructureDistanceMeters; }
}
