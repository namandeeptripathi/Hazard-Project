package com.hazard.dto.normalization;

import java.util.ArrayList;
import java.util.List;

/**
 * Catalog Summary DTO for Stage 3.3 Hazard Normalization.
 */
public class NormalizationSummaryDto {

    private String description = "Stage 3.3 Hazard Metric Normalization Catalog";
    private String normalizationScale = "[0.00, 1.00] standard continuous relative intensity";
    private String canonicalCrs = "EPSG:4326 (WGS 84)";
    private int totalConfiguredMetrics;
    private List<NormalizedHazardMetricConfigDto> configuredMetrics = new ArrayList<>();
    private long totalEligibleObservations;
    private long normalizedObservationsCount;
    private String temporalCoverage;
    private List<String> activeStations = new ArrayList<>();
    private List<String> supportedDistricts = new ArrayList<>();

    public NormalizationSummaryDto() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNormalizationScale() {
        return normalizationScale;
    }

    public void setNormalizationScale(String normalizationScale) {
        this.normalizationScale = normalizationScale;
    }

    public String getCanonicalCrs() {
        return canonicalCrs;
    }

    public void setCanonicalCrs(String canonicalCrs) {
        this.canonicalCrs = canonicalCrs;
    }

    public int getTotalConfiguredMetrics() {
        return totalConfiguredMetrics;
    }

    public void setTotalConfiguredMetrics(int totalConfiguredMetrics) {
        this.totalConfiguredMetrics = totalConfiguredMetrics;
    }

    public List<NormalizedHazardMetricConfigDto> getConfiguredMetrics() {
        return configuredMetrics;
    }

    public void setConfiguredMetrics(List<NormalizedHazardMetricConfigDto> configuredMetrics) {
        this.configuredMetrics = configuredMetrics != null ? configuredMetrics : new ArrayList<>();
        this.totalConfiguredMetrics = this.configuredMetrics.size();
    }

    public long getTotalEligibleObservations() {
        return totalEligibleObservations;
    }

    public void setTotalEligibleObservations(long totalEligibleObservations) {
        this.totalEligibleObservations = totalEligibleObservations;
    }

    public long getNormalizedObservationsCount() {
        return normalizedObservationsCount;
    }

    public void setNormalizedObservationsCount(long normalizedObservationsCount) {
        this.normalizedObservationsCount = normalizedObservationsCount;
    }

    public String getTemporalCoverage() {
        return temporalCoverage;
    }

    public void setTemporalCoverage(String temporalCoverage) {
        this.temporalCoverage = temporalCoverage;
    }

    public List<String> getActiveStations() {
        return activeStations;
    }

    public void setActiveStations(List<String> activeStations) {
        this.activeStations = activeStations != null ? activeStations : new ArrayList<>();
    }

    public List<String> getSupportedDistricts() {
        return supportedDistricts;
    }

    public void setSupportedDistricts(List<String> supportedDistricts) {
        this.supportedDistricts = supportedDistricts != null ? supportedDistricts : new ArrayList<>();
    }

    /**
     * Sub-DTO describing the configuration of a specific normalized metric.
     */
    public static class NormalizedHazardMetricConfigDto {
        private String metricName;
        private String metricLabel;
        private String units;
        private double referenceMin;
        private double referenceMax;
        private String method;
        private String direction;
        private String referenceRationale;

        public NormalizedHazardMetricConfigDto() {
        }

        public NormalizedHazardMetricConfigDto(String metricName, String metricLabel, String units,
                                              double referenceMin, double referenceMax,
                                              String method, String direction, String referenceRationale) {
            this.metricName = metricName;
            this.metricLabel = metricLabel;
            this.units = units;
            this.referenceMin = referenceMin;
            this.referenceMax = referenceMax;
            this.method = method;
            this.direction = direction;
            this.referenceRationale = referenceRationale;
        }

        public String getMetricName() {
            return metricName;
        }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }

        public String getMetricLabel() {
            return metricLabel;
        }

        public void setMetricLabel(String metricLabel) {
            this.metricLabel = metricLabel;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public double getReferenceMin() {
            return referenceMin;
        }

        public void setReferenceMin(double referenceMin) {
            this.referenceMin = referenceMin;
        }

        public double getReferenceMax() {
            return referenceMax;
        }

        public void setReferenceMax(double referenceMax) {
            this.referenceMax = referenceMax;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getReferenceRationale() {
            return referenceRationale;
        }

        public void setReferenceRationale(String referenceRationale) {
            this.referenceRationale = referenceRationale;
        }
    }
}
