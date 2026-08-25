package com.hazard.dto.risk;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO exposing active risk weights, exposure sub-weights, and tier classification thresholds.
 */
public class RiskConfigDto {

    private Map<String, Double> riskComponentWeights = new LinkedHashMap<>();
    private Map<String, Double> exposureSubWeights = new LinkedHashMap<>();
    private Map<String, Double> tierThresholds = new LinkedHashMap<>();
    private double minimumDataCompleteness;
    private String calculationVersion;

    public RiskConfigDto() {}

    public Map<String, Double> getRiskComponentWeights() {
        return riskComponentWeights;
    }

    public void setRiskComponentWeights(Map<String, Double> riskComponentWeights) {
        this.riskComponentWeights = riskComponentWeights;
    }

    public Map<String, Double> getExposureSubWeights() {
        return exposureSubWeights;
    }

    public void setExposureSubWeights(Map<String, Double> exposureSubWeights) {
        this.exposureSubWeights = exposureSubWeights;
    }

    public Map<String, Double> getTierThresholds() {
        return tierThresholds;
    }

    public void setTierThresholds(Map<String, Double> tierThresholds) {
        this.tierThresholds = tierThresholds;
    }

    public double getMinimumDataCompleteness() {
        return minimumDataCompleteness;
    }

    public void setMinimumDataCompleteness(double minimumDataCompleteness) {
        this.minimumDataCompleteness = minimumDataCompleteness;
    }

    public String getCalculationVersion() {
        return calculationVersion;
    }

    public void setCalculationVersion(String calculationVersion) {
        this.calculationVersion = calculationVersion;
    }
}
