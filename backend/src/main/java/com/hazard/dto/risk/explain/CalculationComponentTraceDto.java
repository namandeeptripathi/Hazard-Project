package com.hazard.dto.risk.explain;

/**
 * Detailed trace item for an individual risk calculation component.
 */
public class CalculationComponentTraceDto {

    private String componentId;
    private String componentName;
    private Double rawScore;
    private Double normalizedScore;
    private Double configuredWeight;
    private Double effectiveWeight;
    private Double contribution;
    private String contributionFormula;
    private Double contributionPercent;

    public CalculationComponentTraceDto() {}

    public CalculationComponentTraceDto(String componentId, String componentName, Double rawScore,
                                        Double normalizedScore, Double configuredWeight, Double effectiveWeight,
                                        Double contribution, String contributionFormula, Double contributionPercent) {
        this.componentId = componentId;
        this.componentName = componentName;
        this.rawScore = rawScore;
        this.normalizedScore = normalizedScore;
        this.configuredWeight = configuredWeight;
        this.effectiveWeight = effectiveWeight;
        this.contribution = contribution;
        this.contributionFormula = contributionFormula;
        this.contributionPercent = contributionPercent;
    }

    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public Double getRawScore() { return rawScore; }
    public void setRawScore(Double rawScore) { this.rawScore = rawScore; }

    public Double getNormalizedScore() { return normalizedScore; }
    public void setNormalizedScore(Double normalizedScore) { this.normalizedScore = normalizedScore; }

    public Double getConfiguredWeight() { return configuredWeight; }
    public void setConfiguredWeight(Double configuredWeight) { this.configuredWeight = configuredWeight; }

    public Double getEffectiveWeight() { return effectiveWeight; }
    public void setEffectiveWeight(Double effectiveWeight) { this.effectiveWeight = effectiveWeight; }

    public Double getContribution() { return contribution; }
    public void setContribution(Double contribution) { this.contribution = contribution; }

    public String getContributionFormula() { return contributionFormula; }
    public void setContributionFormula(String contributionFormula) { this.contributionFormula = contributionFormula; }

    public Double getContributionPercent() { return contributionPercent; }
    public void setContributionPercent(Double contributionPercent) { this.contributionPercent = contributionPercent; }
}
