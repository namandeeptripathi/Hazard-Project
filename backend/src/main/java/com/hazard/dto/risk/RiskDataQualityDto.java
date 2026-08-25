package com.hazard.dto.risk;

import com.hazard.domain.risk.RiskDataCompletenessStatus;

/**
 * Data Completeness and Reliability DTO across the 4 risk pillars.
 */
public class RiskDataQualityDto {

    private RiskDataCompletenessStatus status;
    private int configuredComponents;
    private int availableComponents;
    private int unavailableComponents;
    private Double completenessRatio;
    private Double completenessPercentage;

    public RiskDataQualityDto() {
        this.configuredComponents = 4;
    }

    public RiskDataCompletenessStatus getStatus() {
        return status;
    }

    public void setStatus(RiskDataCompletenessStatus status) {
        this.status = status;
    }

    public int getConfiguredComponents() {
        return configuredComponents;
    }

    public void setConfiguredComponents(int configuredComponents) {
        this.configuredComponents = configuredComponents;
    }

    public int getAvailableComponents() {
        return availableComponents;
    }

    public void setAvailableComponents(int availableComponents) {
        this.availableComponents = availableComponents;
    }

    public int getUnavailableComponents() {
        return unavailableComponents;
    }

    public void setUnavailableComponents(int unavailableComponents) {
        this.unavailableComponents = unavailableComponents;
    }

    public Double getCompletenessRatio() {
        return completenessRatio;
    }

    public void setCompletenessRatio(Double completenessRatio) {
        this.completenessRatio = completenessRatio;
    }

    public Double getCompletenessPercentage() {
        return completenessPercentage;
    }

    public void setCompletenessPercentage(Double completenessPercentage) {
        this.completenessPercentage = completenessPercentage;
    }
}
