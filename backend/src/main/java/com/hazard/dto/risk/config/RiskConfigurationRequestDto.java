package com.hazard.dto.risk.config;

/**
 * Request DTO for creating or updating a Risk Configuration Profile.
 */
public class RiskConfigurationRequestDto {

    private String name;
    private String description;
    private String author;

    // Top-Level 4-Pillar Weights
    private Double hazardWeight;
    private Double exposureWeight;
    private Double vulnerabilityWeight;
    private Double historicalWeight;

    // Exposure Sub-Weights
    private Double populationWeight;
    private Double settlementWeight;
    private Double infrastructureWeight;

    // 5-Tier Thresholds
    private Double thresholdLowMax;
    private Double thresholdModerateMax;
    private Double thresholdHighMax;
    private Double thresholdVeryHighMax;
    private Double thresholdCriticalMin;

    private Integer minimumComponents;
    private Boolean activateImmediately;

    public RiskConfigurationRequestDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Double getHazardWeight() { return hazardWeight; }
    public void setHazardWeight(Double hazardWeight) { this.hazardWeight = hazardWeight; }

    public Double getExposureWeight() { return exposureWeight; }
    public void setExposureWeight(Double exposureWeight) { this.exposureWeight = exposureWeight; }

    public Double getVulnerabilityWeight() { return vulnerabilityWeight; }
    public void setVulnerabilityWeight(Double vulnerabilityWeight) { this.vulnerabilityWeight = vulnerabilityWeight; }

    public Double getHistoricalWeight() { return historicalWeight; }
    public void setHistoricalWeight(Double historicalWeight) { this.historicalWeight = historicalWeight; }

    public Double getPopulationWeight() { return populationWeight; }
    public void setPopulationWeight(Double populationWeight) { this.populationWeight = populationWeight; }

    public Double getSettlementWeight() { return settlementWeight; }
    public void setSettlementWeight(Double settlementWeight) { this.settlementWeight = settlementWeight; }

    public Double getInfrastructureWeight() { return infrastructureWeight; }
    public void setInfrastructureWeight(Double infrastructureWeight) { this.infrastructureWeight = infrastructureWeight; }

    public Double getThresholdLowMax() { return thresholdLowMax; }
    public void setThresholdLowMax(Double thresholdLowMax) { this.thresholdLowMax = thresholdLowMax; }

    public Double getThresholdModerateMax() { return thresholdModerateMax; }
    public void setThresholdModerateMax(Double thresholdModerateMax) { this.thresholdModerateMax = thresholdModerateMax; }

    public Double getThresholdHighMax() { return thresholdHighMax; }
    public void setThresholdHighMax(Double thresholdHighMax) { this.thresholdHighMax = thresholdHighMax; }

    public Double getThresholdVeryHighMax() { return thresholdVeryHighMax; }
    public void setThresholdVeryHighMax(Double thresholdVeryHighMax) { this.thresholdVeryHighMax = thresholdVeryHighMax; }

    public Double getThresholdCriticalMin() { return thresholdCriticalMin; }
    public void setThresholdCriticalMin(Double thresholdCriticalMin) { this.thresholdCriticalMin = thresholdCriticalMin; }

    public Integer getMinimumComponents() { return minimumComponents; }
    public void setMinimumComponents(Integer minimumComponents) { this.minimumComponents = minimumComponents; }

    public Boolean getActivateImmediately() { return activateImmediately; }
    public void setActivateImmediately(Boolean activateImmediately) { this.activateImmediately = activateImmediately; }
}
