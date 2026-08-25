package com.hazard.dto.risk.explain;

import com.hazard.domain.risk.explain.SensitivityImpactTier;

/**
 * Model sensitivity record for one-at-a-time component perturbation.
 */
public class ComponentSensitivityDto {

    private String componentId;
    private String componentName;
    private Double baselineComponentScore;
    private Double effectiveWeight;
    private Double baselineRiskScore;
    private Double plusDeltaRiskScore;
    private Double minusDeltaRiskScore;
    private Double absoluteLeverageImpact;
    private SensitivityImpactTier leverageTier;
    private int leverageRank;
    private String interpretation;

    public ComponentSensitivityDto() {}

    public ComponentSensitivityDto(String componentId, String componentName, Double baselineComponentScore,
                                   Double effectiveWeight, Double baselineRiskScore, Double plusDeltaRiskScore,
                                   Double minusDeltaRiskScore, Double absoluteLeverageImpact,
                                   SensitivityImpactTier leverageTier, int leverageRank, String interpretation) {
        this.componentId = componentId;
        this.componentName = componentName;
        this.baselineComponentScore = baselineComponentScore;
        this.effectiveWeight = effectiveWeight;
        this.baselineRiskScore = baselineRiskScore;
        this.plusDeltaRiskScore = plusDeltaRiskScore;
        this.minusDeltaRiskScore = minusDeltaRiskScore;
        this.absoluteLeverageImpact = absoluteLeverageImpact;
        this.leverageTier = leverageTier;
        this.leverageRank = leverageRank;
        this.interpretation = interpretation;
    }

    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public Double getBaselineComponentScore() { return baselineComponentScore; }
    public void setBaselineComponentScore(Double baselineComponentScore) { this.baselineComponentScore = baselineComponentScore; }

    public Double getEffectiveWeight() { return effectiveWeight; }
    public void setEffectiveWeight(Double effectiveWeight) { this.effectiveWeight = effectiveWeight; }

    public Double getBaselineRiskScore() { return baselineRiskScore; }
    public void setBaselineRiskScore(Double baselineRiskScore) { this.baselineRiskScore = baselineRiskScore; }

    public Double getPlusDeltaRiskScore() { return plusDeltaRiskScore; }
    public void setPlusDeltaRiskScore(Double plusDeltaRiskScore) { this.plusDeltaRiskScore = plusDeltaRiskScore; }

    public Double getMinusDeltaRiskScore() { return minusDeltaRiskScore; }
    public void setMinusDeltaRiskScore(Double minusDeltaRiskScore) { this.minusDeltaRiskScore = minusDeltaRiskScore; }

    public Double getAbsoluteLeverageImpact() { return absoluteLeverageImpact; }
    public void setAbsoluteLeverageImpact(Double absoluteLeverageImpact) { this.absoluteLeverageImpact = absoluteLeverageImpact; }

    public SensitivityImpactTier getLeverageTier() { return leverageTier; }
    public void setLeverageTier(SensitivityImpactTier leverageTier) { this.leverageTier = leverageTier; }

    public int getLeverageRank() { return leverageRank; }
    public void setLeverageRank(int leverageRank) { this.leverageRank = leverageRank; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }
}
