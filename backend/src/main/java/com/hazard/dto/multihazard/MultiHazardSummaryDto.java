package com.hazard.dto.multihazard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Executive Catalog Summary DTO for Stage 3.5 Multi-Hazard Integration.
 */
public class MultiHazardSummaryDto {

    private String description = "Stage 3.5 Multi-Hazard Integration & Spatial-Temporal Coincidence Catalog";
    private String scoringFramework = "Multi-Hazard Weighted Composite Index [0.0000, 1.0000]";
    private String canonicalCrs = "EPSG:4326 (WGS 84)";

    private long totalMultiHazardObservations;
    private long fullMatchCount;
    private long spatialOnlyCount;
    private long temporalOnlyCount;
    private long singleHazardCount;

    private Map<String, Long> severityTierDistribution = new LinkedHashMap<>();
    private Map<String, Long> dominantHazardDistribution = new LinkedHashMap<>();
    private Map<String, Double> configuredHazardWeights = new LinkedHashMap<>();
    private List<String> activeDistricts = new ArrayList<>();

    public MultiHazardSummaryDto() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScoringFramework() {
        return scoringFramework;
    }

    public void setScoringFramework(String scoringFramework) {
        this.scoringFramework = scoringFramework;
    }

    public String getCanonicalCrs() {
        return canonicalCrs;
    }

    public void setCanonicalCrs(String canonicalCrs) {
        this.canonicalCrs = canonicalCrs;
    }

    public long getTotalMultiHazardObservations() {
        return totalMultiHazardObservations;
    }

    public void setTotalMultiHazardObservations(long totalMultiHazardObservations) {
        this.totalMultiHazardObservations = totalMultiHazardObservations;
    }

    public long getFullMatchCount() {
        return fullMatchCount;
    }

    public void setFullMatchCount(long fullMatchCount) {
        this.fullMatchCount = fullMatchCount;
    }

    public long getSpatialOnlyCount() {
        return spatialOnlyCount;
    }

    public void setSpatialOnlyCount(long spatialOnlyCount) {
        this.spatialOnlyCount = spatialOnlyCount;
    }

    public long getTemporalOnlyCount() {
        return temporalOnlyCount;
    }

    public void setTemporalOnlyCount(long temporalOnlyCount) {
        this.temporalOnlyCount = temporalOnlyCount;
    }

    public long getSingleHazardCount() {
        return singleHazardCount;
    }

    public void setSingleHazardCount(long singleHazardCount) {
        this.singleHazardCount = singleHazardCount;
    }

    public Map<String, Long> getSeverityTierDistribution() {
        return severityTierDistribution;
    }

    public void setSeverityTierDistribution(Map<String, Long> severityTierDistribution) {
        this.severityTierDistribution = severityTierDistribution != null ? severityTierDistribution : new LinkedHashMap<>();
    }

    public Map<String, Long> getDominantHazardDistribution() {
        return dominantHazardDistribution;
    }

    public void setDominantHazardDistribution(Map<String, Long> dominantHazardDistribution) {
        this.dominantHazardDistribution = dominantHazardDistribution != null ? dominantHazardDistribution : new LinkedHashMap<>();
    }

    public Map<String, Double> getConfiguredHazardWeights() {
        return configuredHazardWeights;
    }

    public void setConfiguredHazardWeights(Map<String, Double> configuredHazardWeights) {
        this.configuredHazardWeights = configuredHazardWeights != null ? configuredHazardWeights : new LinkedHashMap<>();
    }

    public List<String> getActiveDistricts() {
        return activeDistricts;
    }

    public void setActiveDistricts(List<String> activeDistricts) {
        this.activeDistricts = activeDistricts != null ? activeDistricts : new ArrayList<>();
    }
}
