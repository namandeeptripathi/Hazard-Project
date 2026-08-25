package com.hazard.domain.risk.config;

import com.hazard.domain.risk.RiskComponentType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Domain model representing a complete, versioned Risk Configuration Profile.
 * Encapsulates top-level 4-pillar risk weights, exposure sub-weights, tier classification thresholds,
 * minimum component requirements, and immutability flags.
 */
public class RiskConfigurationProfile {

    private String configId;           // e.g. "risk-v1", "risk-v2"
    private String version;            // e.g. "1.0", "2.0"
    private String name;               // e.g. "Default Baseline", "Monsoon Flood Focus"
    private String description;
    private RiskConfigStatus status;   // ACTIVE, INACTIVE, DRAFT, ARCHIVED

    // Top-Level 4-Pillar Weights
    private double hazardWeight;
    private double exposureWeight;
    private double vulnerabilityWeight;
    private double historicalWeight;

    // Exposure Sub-Weights
    private double populationWeight;
    private double settlementWeight;
    private double infrastructureWeight;

    // 5-Tier Classification Thresholds (Upper bounds for lower tiers)
    private double thresholdLowMax;
    private double thresholdModerateMax;
    private double thresholdHighMax;
    private double thresholdVeryHighMax;
    private double thresholdCriticalMin;

    private int minimumComponents;
    private boolean isImmutable;
    private boolean isPreset;

    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RiskConfigurationProfile() {
        this.status = RiskConfigStatus.INACTIVE;
        this.hazardWeight = 0.35;
        this.exposureWeight = 0.30;
        this.vulnerabilityWeight = 0.25;
        this.historicalWeight = 0.10;

        this.populationWeight = 0.40;
        this.settlementWeight = 0.25;
        this.infrastructureWeight = 0.35;

        this.thresholdLowMax = 0.20;
        this.thresholdModerateMax = 0.40;
        this.thresholdHighMax = 0.60;
        this.thresholdVeryHighMax = 0.80;
        this.thresholdCriticalMin = 0.80;

        this.minimumComponents = 2;
        this.isImmutable = false;
        this.isPreset = false;
        this.author = "SYSTEM";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static RiskConfigurationProfile createDefaultBaseline() {
        RiskConfigurationProfile p = new RiskConfigurationProfile();
        p.setConfigId("risk-v1");
        p.setVersion("1.0");
        p.setName("Default Baseline Risk Model");
        p.setDescription("Official national disaster risk weighting: Hazard (35%), Exposure (30%), Vulnerability (25%), Historical Evidence (10%)");
        p.setStatus(RiskConfigStatus.ACTIVE);
        p.setImmutable(true);
        p.setPreset(true);
        p.setAuthor("NDMA-SYSTEM");
        return p;
    }

    public Map<RiskComponentType, Double> toTopLevelWeightMap() {
        Map<RiskComponentType, Double> map = new LinkedHashMap<>();
        map.put(RiskComponentType.HAZARD, hazardWeight);
        map.put(RiskComponentType.EXPOSURE, exposureWeight);
        map.put(RiskComponentType.VULNERABILITY, vulnerabilityWeight);
        map.put(RiskComponentType.HISTORICAL, historicalWeight);
        return Collections.unmodifiableMap(map);
    }

    public Map<String, Double> toExposureSubWeightMap() {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("POPULATION", populationWeight);
        map.put("SETTLEMENT", settlementWeight);
        map.put("INFRASTRUCTURE", infrastructureWeight);
        return Collections.unmodifiableMap(map);
    }

    public double getTopLevelWeightSum() {
        return hazardWeight + exposureWeight + vulnerabilityWeight + historicalWeight;
    }

    public double getExposureSubWeightSum() {
        return populationWeight + settlementWeight + infrastructureWeight;
    }

    // Getters and Setters
    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RiskConfigStatus getStatus() { return status; }
    public void setStatus(RiskConfigStatus status) { this.status = status; }

    public double getHazardWeight() { return hazardWeight; }
    public void setHazardWeight(double hazardWeight) { this.hazardWeight = hazardWeight; }

    public double getExposureWeight() { return exposureWeight; }
    public void setExposureWeight(double exposureWeight) { this.exposureWeight = exposureWeight; }

    public double getVulnerabilityWeight() { return vulnerabilityWeight; }
    public void setVulnerabilityWeight(double vulnerabilityWeight) { this.vulnerabilityWeight = vulnerabilityWeight; }

    public double getHistoricalWeight() { return historicalWeight; }
    public void setHistoricalWeight(double historicalWeight) { this.historicalWeight = historicalWeight; }

    public double getPopulationWeight() { return populationWeight; }
    public void setPopulationWeight(double populationWeight) { this.populationWeight = populationWeight; }

    public double getSettlementWeight() { return settlementWeight; }
    public void setSettlementWeight(double settlementWeight) { this.settlementWeight = settlementWeight; }

    public double getInfrastructureWeight() { return infrastructureWeight; }
    public void setInfrastructureWeight(double infrastructureWeight) { this.infrastructureWeight = infrastructureWeight; }

    public double getThresholdLowMax() { return thresholdLowMax; }
    public void setThresholdLowMax(double thresholdLowMax) { this.thresholdLowMax = thresholdLowMax; }

    public double getThresholdModerateMax() { return thresholdModerateMax; }
    public void setThresholdModerateMax(double thresholdModerateMax) { this.thresholdModerateMax = thresholdModerateMax; }

    public double getThresholdHighMax() { return thresholdHighMax; }
    public void setThresholdHighMax(double thresholdHighMax) { this.thresholdHighMax = thresholdHighMax; }

    public double getThresholdVeryHighMax() { return thresholdVeryHighMax; }
    public void setThresholdVeryHighMax(double thresholdVeryHighMax) { this.thresholdVeryHighMax = thresholdVeryHighMax; }

    public double getThresholdCriticalMin() { return thresholdCriticalMin; }
    public void setThresholdCriticalMin(double thresholdCriticalMin) { this.thresholdCriticalMin = thresholdCriticalMin; }

    public int getMinimumComponents() { return minimumComponents; }
    public void setMinimumComponents(int minimumComponents) { this.minimumComponents = minimumComponents; }

    public boolean isImmutable() { return isImmutable; }
    public void setImmutable(boolean immutable) { isImmutable = immutable; }

    public boolean isPreset() { return isPreset; }
    public void setPreset(boolean preset) { isPreset = preset; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
