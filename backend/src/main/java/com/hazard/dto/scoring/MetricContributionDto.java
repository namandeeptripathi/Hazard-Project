package com.hazard.dto.scoring;

/**
 * Detailed breakdown of a single indicator's contribution to the composite hazard score.
 */
public class MetricContributionDto {

    private String metricName;
    private String metricLabel;
    private Double rawValue;
    private String units;
    private Double normalizedValue;
    private Double configuredWeight;
    private Double effectiveWeight;
    private Double weightedContribution;
    private boolean clamped;

    public MetricContributionDto() {
    }

    public MetricContributionDto(String metricName, String metricLabel, Double rawValue, String units,
                                 Double normalizedValue, Double configuredWeight, Double effectiveWeight,
                                 Double weightedContribution, boolean clamped) {
        this.metricName = metricName;
        this.metricLabel = metricLabel;
        this.rawValue = rawValue;
        this.units = units;
        this.normalizedValue = normalizedValue;
        this.configuredWeight = configuredWeight;
        this.effectiveWeight = effectiveWeight;
        this.weightedContribution = weightedContribution;
        this.clamped = clamped;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricLabel() {
        return metricLabel;
    }

    public void setMetricLabel(String metricLabel) {
        this.metricLabel = metricLabel;
    }

    public Double getRawValue() {
        return rawValue;
    }

    public void setRawValue(Double rawValue) {
        this.rawValue = rawValue;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Double getNormalizedValue() {
        return normalizedValue;
    }

    public void setNormalizedValue(Double normalizedValue) {
        this.normalizedValue = normalizedValue;
    }

    public Double getConfiguredWeight() {
        return configuredWeight;
    }

    public void setConfiguredWeight(Double configuredWeight) {
        this.configuredWeight = configuredWeight;
    }

    public Double getEffectiveWeight() {
        return effectiveWeight;
    }

    public void setEffectiveWeight(Double effectiveWeight) {
        this.effectiveWeight = effectiveWeight;
    }

    public Double getWeightedContribution() {
        return weightedContribution;
    }

    public void setWeightedContribution(Double weightedContribution) {
        this.weightedContribution = weightedContribution;
    }

    public boolean isClamped() {
        return clamped;
    }

    public void setClamped(boolean clamped) {
        this.clamped = clamped;
    }
}
