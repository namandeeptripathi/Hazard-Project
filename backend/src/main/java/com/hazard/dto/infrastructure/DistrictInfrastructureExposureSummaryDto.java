package com.hazard.dto.infrastructure;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * District-level aggregated infrastructure exposure summary, detailing total assets evaluated,
 * exposed count, category breakdown, criticality breakdown, and individual asset records.
 */
public class DistrictInfrastructureExposureSummaryDto {

    private Integer districtId;
    private String districtName;
    private String state;
    private String gid2;

    private int totalAssetsEvaluated;
    private int exposedAssetsCount;
    private Double infrastructureExposurePercentage;

    private Map<String, Integer> categoryBreakdown = new LinkedHashMap<>();
    private Map<String, Integer> severityBreakdown = new LinkedHashMap<>();
    private Map<String, Integer> criticalityBreakdown = new LinkedHashMap<>();

    private String dominantHazard;
    private Double peakHazardIndex;

    private List<InfrastructureAssetDto> exposedAssets = new ArrayList<>();

    public DistrictInfrastructureExposureSummaryDto() {
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

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGid2() {
        return gid2;
    }

    public void setGid2(String gid2) {
        this.gid2 = gid2;
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

    public String getDominantHazard() {
        return dominantHazard;
    }

    public void setDominantHazard(String dominantHazard) {
        this.dominantHazard = dominantHazard;
    }

    public Double getPeakHazardIndex() {
        return peakHazardIndex;
    }

    public void setPeakHazardIndex(Double peakHazardIndex) {
        this.peakHazardIndex = peakHazardIndex;
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
}
