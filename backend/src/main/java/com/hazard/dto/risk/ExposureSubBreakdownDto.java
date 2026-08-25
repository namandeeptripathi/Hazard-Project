package com.hazard.dto.risk;

/**
 * Detailed exposure sub-aggregation breakdown across Population (4.1), Settlements (4.2), and Infrastructure (4.3).
 */
public class ExposureSubBreakdownDto {

    private Double populationExposureScore;        // [0.0000, 1.0000]
    private Double populationConfiguredWeight;     // e.g. 0.40
    private Double populationContribution;
    private Long exposedPopulation;
    private Double exposedPopulationPercentage;

    private Double settlementExposureScore;        // [0.0000, 1.0000]
    private Double settlementConfiguredWeight;     // e.g. 0.25
    private Double settlementContribution;
    private Integer settlementsExposedCount;

    private Double infrastructureExposureScore;    // [0.0000, 1.0000]
    private Double infrastructureConfiguredWeight; // e.g. 0.35
    private Double infrastructureContribution;
    private Integer infrastructureAssetsExposedCount;

    private Double combinedExposureScore;          // [0.0000, 1.0000]
    private Double combinedExposureScore100;       // [0.0, 100.0]

    public ExposureSubBreakdownDto() {}

    public Double getPopulationExposureScore() {
        return populationExposureScore;
    }

    public void setPopulationExposureScore(Double populationExposureScore) {
        this.populationExposureScore = populationExposureScore;
    }

    public Double getPopulationConfiguredWeight() {
        return populationConfiguredWeight;
    }

    public void setPopulationConfiguredWeight(Double populationConfiguredWeight) {
        this.populationConfiguredWeight = populationConfiguredWeight;
    }

    public Double getPopulationContribution() {
        return populationContribution;
    }

    public void setPopulationContribution(Double populationContribution) {
        this.populationContribution = populationContribution;
    }

    public Long getExposedPopulation() {
        return exposedPopulation;
    }

    public void setExposedPopulation(Long exposedPopulation) {
        this.exposedPopulation = exposedPopulation;
    }

    public Double getExposedPopulationPercentage() {
        return exposedPopulationPercentage;
    }

    public void setExposedPopulationPercentage(Double exposedPopulationPercentage) {
        this.exposedPopulationPercentage = exposedPopulationPercentage;
    }

    public Double getSettlementExposureScore() {
        return settlementExposureScore;
    }

    public void setSettlementExposureScore(Double settlementExposureScore) {
        this.settlementExposureScore = settlementExposureScore;
    }

    public Double getSettlementConfiguredWeight() {
        return settlementConfiguredWeight;
    }

    public void setSettlementConfiguredWeight(Double settlementConfiguredWeight) {
        this.settlementConfiguredWeight = settlementConfiguredWeight;
    }

    public Double getSettlementContribution() {
        return settlementContribution;
    }

    public void setSettlementContribution(Double settlementContribution) {
        this.settlementContribution = settlementContribution;
    }

    public Integer getSettlementsExposedCount() {
        return settlementsExposedCount;
    }

    public void setSettlementsExposedCount(Integer settlementsExposedCount) {
        this.settlementsExposedCount = settlementsExposedCount;
    }

    public Double getInfrastructureExposureScore() {
        return infrastructureExposureScore;
    }

    public void setInfrastructureExposureScore(Double infrastructureExposureScore) {
        this.infrastructureExposureScore = infrastructureExposureScore;
    }

    public Double getInfrastructureConfiguredWeight() {
        return infrastructureConfiguredWeight;
    }

    public void setInfrastructureConfiguredWeight(Double infrastructureConfiguredWeight) {
        this.infrastructureConfiguredWeight = infrastructureConfiguredWeight;
    }

    public Double getInfrastructureContribution() {
        return infrastructureContribution;
    }

    public void setInfrastructureContribution(Double infrastructureContribution) {
        this.infrastructureContribution = infrastructureContribution;
    }

    public Integer getInfrastructureAssetsExposedCount() {
        return infrastructureAssetsExposedCount;
    }

    public void setInfrastructureAssetsExposedCount(Integer infrastructureAssetsExposedCount) {
        this.infrastructureAssetsExposedCount = infrastructureAssetsExposedCount;
    }

    public Double getCombinedExposureScore() {
        return combinedExposureScore;
    }

    public void setCombinedExposureScore(Double combinedExposureScore) {
        this.combinedExposureScore = combinedExposureScore;
    }

    public Double getCombinedExposureScore100() {
        return combinedExposureScore100;
    }

    public void setCombinedExposureScore100(Double combinedExposureScore100) {
        this.combinedExposureScore100 = combinedExposureScore100;
    }
}
