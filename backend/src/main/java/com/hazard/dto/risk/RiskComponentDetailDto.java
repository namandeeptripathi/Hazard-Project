package com.hazard.dto.risk;

import com.hazard.domain.risk.RiskComponentType;

import java.util.Map;

/**
 * Detailed representation of an individual top-level risk component.
 */
public class RiskComponentDetailDto {

    private RiskComponentType componentType;
    private String componentName;
    private Double score;             // Normalized score in [0.0000, 1.0000]
    private Double score100;          // Human-friendly score in [0.0, 100.0]
    private Double configuredWeight;  // Configured weight share (e.g. 0.35)
    private Double effectiveWeight;   // Active normalized weight share
    private Double contribution;      // score * effectiveWeight
    private String colorHex;
    private String status;            // AVAILABLE, ESTIMATED, UNAVAILABLE
    private String sourceSummary;
    private Map<String, Object> subDetails;

    public RiskComponentDetailDto() {}

    public RiskComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(RiskComponentType componentType) {
        this.componentType = componentType;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getScore100() {
        return score100;
    }

    public void setScore100(Double score100) {
        this.score100 = score100;
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

    public Double getContribution() {
        return contribution;
    }

    public void setContribution(Double contribution) {
        this.contribution = contribution;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceSummary() {
        return sourceSummary;
    }

    public void setSourceSummary(String sourceSummary) {
        this.sourceSummary = sourceSummary;
    }

    public Map<String, Object> getSubDetails() {
        return subDetails;
    }

    public void setSubDetails(Map<String, Object> subDetails) {
        this.subDetails = subDetails;
    }
}
