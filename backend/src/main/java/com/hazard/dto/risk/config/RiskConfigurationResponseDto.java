package com.hazard.dto.risk.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazard.domain.risk.config.RiskConfigStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standard response DTO detailing a Risk Configuration Profile, including normalized effective weights.
 */
public class RiskConfigurationResponseDto {

    private String configId;
    private String version;
    private String name;
    private String description;
    private RiskConfigStatus status;

    private Map<String, Double> configuredTopLevelWeights = new LinkedHashMap<>();
    private Map<String, Double> normalizedTopLevelWeights = new LinkedHashMap<>();

    private Map<String, Double> configuredExposureWeights = new LinkedHashMap<>();
    private Map<String, Double> normalizedExposureWeights = new LinkedHashMap<>();

    private Map<String, Double> thresholds = new LinkedHashMap<>();

    private int minimumComponents;
    private boolean isImmutable;
    private boolean isPreset;

    private String calculationVersion = "v1.0";
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RiskConfigurationResponseDto() {}

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

    public Map<String, Double> getConfiguredTopLevelWeights() { return configuredTopLevelWeights; }
    public void setConfiguredTopLevelWeights(Map<String, Double> configuredTopLevelWeights) { this.configuredTopLevelWeights = configuredTopLevelWeights; }

    @JsonProperty("riskComponentWeights")
    public Map<String, Double> getRiskComponentWeights() { return configuredTopLevelWeights; }

    public Map<String, Double> getNormalizedTopLevelWeights() { return normalizedTopLevelWeights; }
    public void setNormalizedTopLevelWeights(Map<String, Double> normalizedTopLevelWeights) { this.normalizedTopLevelWeights = normalizedTopLevelWeights; }

    public Map<String, Double> getConfiguredExposureWeights() { return configuredExposureWeights; }
    public void setConfiguredExposureWeights(Map<String, Double> configuredExposureWeights) { this.configuredExposureWeights = configuredExposureWeights; }

    @JsonProperty("exposureSubWeights")
    public Map<String, Double> getExposureSubWeights() { return configuredExposureWeights; }

    public Map<String, Double> getNormalizedExposureWeights() { return normalizedExposureWeights; }
    public void setNormalizedExposureWeights(Map<String, Double> normalizedExposureWeights) { this.normalizedExposureWeights = normalizedExposureWeights; }

    public Map<String, Double> getThresholds() { return thresholds; }
    public void setThresholds(Map<String, Double> thresholds) { this.thresholds = thresholds; }

    public int getMinimumComponents() { return minimumComponents; }
    public void setMinimumComponents(int minimumComponents) { this.minimumComponents = minimumComponents; }

    public boolean isImmutable() { return isImmutable; }
    public void setImmutable(boolean immutable) { isImmutable = immutable; }

    public boolean isPreset() { return isPreset; }
    public void setPreset(boolean preset) { isPreset = preset; }

    public String getCalculationVersion() { return calculationVersion; }
    public void setCalculationVersion(String calculationVersion) { this.calculationVersion = calculationVersion; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
