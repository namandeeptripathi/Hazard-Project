package com.hazard.dto.historical;

/**
 * Statistical aggregation of recorded disaster severity across historical events.
 */
public class SeverityStatisticsDto {

    private Double minimumSeverity;
    private Double maximumSeverity;
    private Double averageSeverity;
    private Double medianSeverity;
    private Double latestSeverity;
    private int highSeverityEventCount;
    private String severityScaleDescription;

    public SeverityStatisticsDto() {}

    public Double getMinimumSeverity() {
        return minimumSeverity;
    }

    public void setMinimumSeverity(Double minimumSeverity) {
        this.minimumSeverity = minimumSeverity;
    }

    public Double getMaximumSeverity() {
        return maximumSeverity;
    }

    public void setMaximumSeverity(Double maximumSeverity) {
        this.maximumSeverity = maximumSeverity;
    }

    public Double getAverageSeverity() {
        return averageSeverity;
    }

    public void setAverageSeverity(Double averageSeverity) {
        this.averageSeverity = averageSeverity;
    }

    public Double getMedianSeverity() {
        return medianSeverity;
    }

    public void setMedianSeverity(Double medianSeverity) {
        this.medianSeverity = medianSeverity;
    }

    public Double getLatestSeverity() {
        return latestSeverity;
    }

    public void setLatestSeverity(Double latestSeverity) {
        this.latestSeverity = latestSeverity;
    }

    public int getHighSeverityEventCount() {
        return highSeverityEventCount;
    }

    public void setHighSeverityEventCount(int highSeverityEventCount) {
        this.highSeverityEventCount = highSeverityEventCount;
    }

    public String getSeverityScaleDescription() {
        return severityScaleDescription;
    }

    public void setSeverityScaleDescription(String severityScaleDescription) {
        this.severityScaleDescription = severityScaleDescription;
    }
}
