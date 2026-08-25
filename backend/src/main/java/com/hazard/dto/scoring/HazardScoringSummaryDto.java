package com.hazard.dto.scoring;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Executive Catalog Summary DTO for Stage 3.4 Hazard Scoring.
 */
public class HazardScoringSummaryDto {

    private String description = "Stage 3.4 Single-Hazard Scoring & Severity Classification Catalog";
    private String scoringFramework = "Weighted Multi-Criteria Hazard Index [0.0000, 1.0000]";
    private String canonicalCrs = "EPSG:4326 (WGS 84)";
    private long totalScoredObservations;
    private Map<String, Long> severityTierDistribution = new LinkedHashMap<>();
    private Map<String, Long> hazardTypeDistribution = new LinkedHashMap<>();
    private List<ScoringConfigSummaryDto> activeScoringConfigurations = new ArrayList<>();
    private List<String> activeStations = new ArrayList<>();
    private List<String> coveredDistricts = new ArrayList<>();

    public HazardScoringSummaryDto() {
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

    public long getTotalScoredObservations() {
        return totalScoredObservations;
    }

    public void setTotalScoredObservations(long totalScoredObservations) {
        this.totalScoredObservations = totalScoredObservations;
    }

    public Map<String, Long> getSeverityTierDistribution() {
        return severityTierDistribution;
    }

    public void setSeverityTierDistribution(Map<String, Long> severityTierDistribution) {
        this.severityTierDistribution = severityTierDistribution != null ? severityTierDistribution : new LinkedHashMap<>();
    }

    public Map<String, Long> getHazardTypeDistribution() {
        return hazardTypeDistribution;
    }

    public void setHazardTypeDistribution(Map<String, Long> hazardTypeDistribution) {
        this.hazardTypeDistribution = hazardTypeDistribution != null ? hazardTypeDistribution : new LinkedHashMap<>();
    }

    public List<ScoringConfigSummaryDto> getActiveScoringConfigurations() {
        return activeScoringConfigurations;
    }

    public void setActiveScoringConfigurations(List<ScoringConfigSummaryDto> activeScoringConfigurations) {
        this.activeScoringConfigurations = activeScoringConfigurations != null ? activeScoringConfigurations : new ArrayList<>();
    }

    public List<String> getActiveStations() {
        return activeStations;
    }

    public void setActiveStations(List<String> activeStations) {
        this.activeStations = activeStations != null ? activeStations : new ArrayList<>();
    }

    public List<String> getCoveredDistricts() {
        return coveredDistricts;
    }

    public void setCoveredDistricts(List<String> coveredDistricts) {
        this.coveredDistricts = coveredDistricts != null ? coveredDistricts : new ArrayList<>();
    }

    /**
     * Sub-DTO summarizing an active scoring configuration.
     */
    public static class ScoringConfigSummaryDto {
        private String hazardType;
        private Map<String, Double> metricWeights = new LinkedHashMap<>();
        private double totalWeight;
        private String description;

        public ScoringConfigSummaryDto() {
        }

        public ScoringConfigSummaryDto(String hazardType, Map<String, Double> metricWeights,
                                       double totalWeight, String description) {
            this.hazardType = hazardType;
            this.metricWeights = metricWeights;
            this.totalWeight = totalWeight;
            this.description = description;
        }

        public String getHazardType() {
            return hazardType;
        }

        public void setHazardType(String hazardType) {
            this.hazardType = hazardType;
        }

        public Map<String, Double> getMetricWeights() {
            return metricWeights;
        }

        public void setMetricWeights(Map<String, Double> metricWeights) {
            this.metricWeights = metricWeights != null ? metricWeights : new LinkedHashMap<>();
        }

        public double getTotalWeight() {
            return totalWeight;
        }

        public void setTotalWeight(double totalWeight) {
            this.totalWeight = totalWeight;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
