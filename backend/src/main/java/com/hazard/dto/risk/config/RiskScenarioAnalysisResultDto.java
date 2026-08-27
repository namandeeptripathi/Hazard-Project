package com.hazard.dto.risk.config;

import com.hazard.dto.risk.DistrictRiskScoreDto;

/**
 * Result DTO for what-if scenario simulations, showing baseline vs. scenario comparison and delta difference.
 */
public class RiskScenarioAnalysisResultDto {

    private String districtName;
    private String scenarioName;
    private String mode = "SCENARIO_ANALYSIS";
    private boolean productionConfigurationUnchanged = true;

    private DistrictRiskScoreDto baselineRisk;
    private DistrictRiskScoreDto scenarioRisk;

    private Double deltaRiskScore;    // scenario - baseline in [0, 1]
    private Double deltaRiskScore100; // scenario - baseline in [0, 100]
    private String riskDirection;     // INCREASED, DECREASED, UNCHANGED
    private String explanation;

    public RiskScenarioAnalysisResultDto() {}

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public boolean isProductionConfigurationUnchanged() { return productionConfigurationUnchanged; }
    public void setProductionConfigurationUnchanged(boolean productionConfigurationUnchanged) { this.productionConfigurationUnchanged = productionConfigurationUnchanged; }

    public DistrictRiskScoreDto getBaselineRisk() { return baselineRisk; }
    public void setBaselineRisk(DistrictRiskScoreDto baselineRisk) { this.baselineRisk = baselineRisk; }

    public DistrictRiskScoreDto getScenarioRisk() { return scenarioRisk; }
    public void setScenarioRisk(DistrictRiskScoreDto scenarioRisk) { this.scenarioRisk = scenarioRisk; }

    public Double getDeltaRiskScore() { return deltaRiskScore; }
    public void setDeltaRiskScore(Double deltaRiskScore) { this.deltaRiskScore = deltaRiskScore; }

    public Double getDeltaRiskScore100() { return deltaRiskScore100; }
    public void setDeltaRiskScore100(Double deltaRiskScore100) { this.deltaRiskScore100 = deltaRiskScore100; }

    public String getRiskDirection() { return riskDirection; }
    public void setRiskDirection(String riskDirection) { this.riskDirection = riskDirection; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
