package com.hazard.dto.infrastructure;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO exposing active configuration, criticality multipliers, and classification thresholds.
 */
public class InfrastructureExposureConfigDto {

    private double lowThresholdPercent;
    private double moderateThresholdPercent;
    private double highThresholdPercent;
    private double defaultHazardBufferMeters;

    private Map<String, Double> criticalityMultipliers = new LinkedHashMap<>();
    private Map<String, String> defaultCategoryCriticality = new LinkedHashMap<>();

    public InfrastructureExposureConfigDto() {}

    public double getLowThresholdPercent() {
        return lowThresholdPercent;
    }

    public void setLowThresholdPercent(double lowThresholdPercent) {
        this.lowThresholdPercent = lowThresholdPercent;
    }

    public double getModerateThresholdPercent() {
        return moderateThresholdPercent;
    }

    public void setModerateThresholdPercent(double moderateThresholdPercent) {
        this.moderateThresholdPercent = moderateThresholdPercent;
    }

    public double getHighThresholdPercent() {
        return highThresholdPercent;
    }

    public void setHighThresholdPercent(double highThresholdPercent) {
        this.highThresholdPercent = highThresholdPercent;
    }

    public double getDefaultHazardBufferMeters() {
        return defaultHazardBufferMeters;
    }

    public void setDefaultHazardBufferMeters(double defaultHazardBufferMeters) {
        this.defaultHazardBufferMeters = defaultHazardBufferMeters;
    }

    public Map<String, Double> getCriticalityMultipliers() {
        return criticalityMultipliers;
    }

    public void setCriticalityMultipliers(Map<String, Double> criticalityMultipliers) {
        this.criticalityMultipliers = criticalityMultipliers;
    }

    public Map<String, String> getDefaultCategoryCriticality() {
        return defaultCategoryCriticality;
    }

    public void setDefaultCategoryCriticality(Map<String, String> defaultCategoryCriticality) {
        this.defaultCategoryCriticality = defaultCategoryCriticality;
    }
}
