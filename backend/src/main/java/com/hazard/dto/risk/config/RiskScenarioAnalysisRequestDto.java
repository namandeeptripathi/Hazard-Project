package com.hazard.dto.risk.config;

import java.util.Map;

/**
 * Request payload for what-if scenario simulations with temporary weight overrides.
 */
public class RiskScenarioAnalysisRequestDto {

    private String districtName;
    private String baseConfigurationId;
    private Map<String, Double> overrideWeights;         // e.g. {"hazard": 0.50, "exposure": 0.25, ...}
    private Map<String, Double> overrideExposureWeights; // e.g. {"population": 0.50, ...}
    private String scenarioName;

    public RiskScenarioAnalysisRequestDto() {
        this.scenarioName = "Custom What-If Scenario";
    }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getBaseConfigurationId() { return baseConfigurationId; }
    public void setBaseConfigurationId(String baseConfigurationId) { this.baseConfigurationId = baseConfigurationId; }

    public Map<String, Double> getOverrideWeights() { return overrideWeights; }
    public void setOverrideWeights(Map<String, Double> overrideWeights) { this.overrideWeights = overrideWeights; }

    public Map<String, Double> getOverrideExposureWeights() { return overrideExposureWeights; }
    public void setOverrideExposureWeights(Map<String, Double> overrideExposureWeights) { this.overrideExposureWeights = overrideExposureWeights; }

    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }
}
