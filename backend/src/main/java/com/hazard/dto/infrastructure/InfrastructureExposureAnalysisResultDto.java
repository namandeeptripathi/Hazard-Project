package com.hazard.dto.infrastructure;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result container for hazard-event or custom-geometry infrastructure exposure queries,
 * including summary metadata, category breakdowns, and list of affected infrastructure assets.
 */
public class InfrastructureExposureAnalysisResultDto {

    private String geographicUnit;
    private String hazardIdentifier;
    private String hazardType;
    private Double hazardSeverityScore;

    private int totalAssetsEvaluated;
    private int exposedAssetsCount;
    private Double infrastructureExposurePercentage;
    private Double averageExposureScore;

    private Map<String, Integer> categoryBreakdown = new LinkedHashMap<>();
    private Map<String, Integer> severityBreakdown = new LinkedHashMap<>();
    private Map<String, Integer> criticalityBreakdown = new LinkedHashMap<>();

    private LocalDateTime timestamp;
    private String calculationMethod;
    private String explanation;

    private List<InfrastructureAssetDto> exposedAssets = new ArrayList<>();
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public InfrastructureExposureAnalysisResultDto() {
        this.timestamp = LocalDateTime.now();
        for (InfrastructureCategory cat : InfrastructureCategory.values()) {
            categoryBreakdown.put(cat.name(), 0);
        }
        for (ExposureCategory exp : ExposureCategory.values()) {
            severityBreakdown.put(exp.name(), 0);
        }
        for (InfrastructureCriticality crit : InfrastructureCriticality.values()) {
            criticalityBreakdown.put(crit.name(), 0);
        }
    }

    public String getGeographicUnit() {
        return geographicUnit;
    }

    public void setGeographicUnit(String geographicUnit) {
        this.geographicUnit = geographicUnit;
    }

    public String getHazardIdentifier() {
        return hazardIdentifier;
    }

    public void setHazardIdentifier(String hazardIdentifier) {
        this.hazardIdentifier = hazardIdentifier;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public Double getHazardSeverityScore() {
        return hazardSeverityScore;
    }

    public void setHazardSeverityScore(Double hazardSeverityScore) {
        this.hazardSeverityScore = hazardSeverityScore;
    }

    public int getTotalAssetsEvaluated() {
        return totalAssetsEvaluated;
    }

    public void setTotalAssetsEvaluated(int totalAssetsEvaluated) {
        this.totalAssetsEvaluated = totalAssetsEvaluated;
    }

    public int getExposedAssetsCount() {
        return exposedAssetsCount;
    }

    public void setExposedAssetsCount(int exposedAssetsCount) {
        this.exposedAssetsCount = exposedAssetsCount;
    }

    public Double getInfrastructureExposurePercentage() {
        return infrastructureExposurePercentage;
    }

    public void setInfrastructureExposurePercentage(Double infrastructureExposurePercentage) {
        this.infrastructureExposurePercentage = infrastructureExposurePercentage;
    }

    public Double getAverageExposureScore() {
        return averageExposureScore;
    }

    public void setAverageExposureScore(Double averageExposureScore) {
        this.averageExposureScore = averageExposureScore;
    }

    public Map<String, Integer> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(Map<String, Integer> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown != null ? categoryBreakdown : new LinkedHashMap<>();
    }

    public void incrementCategoryCount(InfrastructureCategory category) {
        if (category != null) {
            this.categoryBreakdown.merge(category.name(), 1, Integer::sum);
        }
    }

    public Map<String, Integer> getSeverityBreakdown() {
        return severityBreakdown;
    }

    public void setSeverityBreakdown(Map<String, Integer> severityBreakdown) {
        this.severityBreakdown = severityBreakdown != null ? severityBreakdown : new LinkedHashMap<>();
    }

    public void incrementSeverityCount(ExposureCategory severity) {
        if (severity != null) {
            this.severityBreakdown.merge(severity.name(), 1, Integer::sum);
        }
    }

    public Map<String, Integer> getCriticalityBreakdown() {
        return criticalityBreakdown;
    }

    public void setCriticalityBreakdown(Map<String, Integer> criticalityBreakdown) {
        this.criticalityBreakdown = criticalityBreakdown != null ? criticalityBreakdown : new LinkedHashMap<>();
    }

    public void incrementCriticalityCount(InfrastructureCriticality criticality) {
        if (criticality != null) {
            this.criticalityBreakdown.merge(criticality.name(), 1, Integer::sum);
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(String calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<InfrastructureAssetDto> getExposedAssets() {
        return exposedAssets;
    }

    public void setExposedAssets(List<InfrastructureAssetDto> exposedAssets) {
        this.exposedAssets = exposedAssets != null ? exposedAssets : new ArrayList<>();
    }

    public void addExposedAsset(InfrastructureAssetDto asset) {
        this.exposedAssets.add(asset);
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new LinkedHashMap<>();
    }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}
