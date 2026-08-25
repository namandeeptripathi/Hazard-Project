package com.hazard.dto.risk.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structured comparison DTO highlighting differences between two configuration versions.
 */
public class RiskConfigDiffDto {

    private String baseConfigId;
    private String baseVersion;
    private String targetConfigId;
    private String targetVersion;

    private Map<String, String> metadataChanges = new LinkedHashMap<>();
    private Map<String, Double[]> topLevelWeightDiffs = new LinkedHashMap<>(); // [baseVal, targetVal, delta]
    private Map<String, Double[]> exposureWeightDiffs = new LinkedHashMap<>(); // [baseVal, targetVal, delta]
    private Map<String, Double[]> thresholdDiffs = new LinkedHashMap<>();      // [baseVal, targetVal, delta]

    public RiskConfigDiffDto() {}

    public String getBaseConfigId() { return baseConfigId; }
    public void setBaseConfigId(String baseConfigId) { this.baseConfigId = baseConfigId; }

    public String getBaseVersion() { return baseVersion; }
    public void setBaseVersion(String baseVersion) { this.baseVersion = baseVersion; }

    public String getTargetConfigId() { return targetConfigId; }
    public void setTargetConfigId(String targetConfigId) { this.targetConfigId = targetConfigId; }

    public String getTargetVersion() { return targetVersion; }
    public void setTargetVersion(String targetVersion) { this.targetVersion = targetVersion; }

    public Map<String, String> getMetadataChanges() { return metadataChanges; }
    public void setMetadataChanges(Map<String, String> metadataChanges) { this.metadataChanges = metadataChanges; }

    public Map<String, Double[]> getTopLevelWeightDiffs() { return topLevelWeightDiffs; }
    public void setTopLevelWeightDiffs(Map<String, Double[]> topLevelWeightDiffs) { this.topLevelWeightDiffs = topLevelWeightDiffs; }

    public Map<String, Double[]> getExposureWeightDiffs() { return exposureWeightDiffs; }
    public void setExposureWeightDiffs(Map<String, Double[]> exposureWeightDiffs) { this.exposureWeightDiffs = exposureWeightDiffs; }

    public Map<String, Double[]> getThresholdDiffs() { return thresholdDiffs; }
    public void setThresholdDiffs(Map<String, Double[]> thresholdDiffs) { this.thresholdDiffs = thresholdDiffs; }
}
