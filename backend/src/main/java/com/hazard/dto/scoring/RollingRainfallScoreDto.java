package com.hazard.dto.scoring;

import com.hazard.domain.hazard.QualityStatus;
import com.hazard.domain.hazard.SeverityTier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Scored Multi-Window Rolling Rainfall Hazard Observation.
 */
public class RollingRainfallScoreDto {

    private String stationName;
    private String associatedDistrict;
    private LocalDateTime timestamp;
    private Double rollingRainfallScore;
    private SeverityTier severityTier;
    private boolean isHeavyRainfall;
    private boolean isVeryHeavyRainfall;
    private QualityStatus qualityStatus;
    private List<MetricContributionDto> metricContributions = new ArrayList<>();

    public RollingRainfallScoreDto() {
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getAssociatedDistrict() {
        return associatedDistrict;
    }

    public void setAssociatedDistrict(String associatedDistrict) {
        this.associatedDistrict = associatedDistrict;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getRollingRainfallScore() {
        return rollingRainfallScore;
    }

    public void setRollingRainfallScore(Double rollingRainfallScore) {
        this.rollingRainfallScore = rollingRainfallScore;
    }

    public SeverityTier getSeverityTier() {
        return severityTier;
    }

    public void setSeverityTier(SeverityTier severityTier) {
        this.severityTier = severityTier;
    }

    public boolean isHeavyRainfall() {
        return isHeavyRainfall;
    }

    public void setHeavyRainfall(boolean heavyRainfall) {
        isHeavyRainfall = heavyRainfall;
    }

    public boolean isVeryHeavyRainfall() {
        return isVeryHeavyRainfall;
    }

    public void setVeryHeavyRainfall(boolean veryHeavyRainfall) {
        isVeryHeavyRainfall = veryHeavyRainfall;
    }

    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(QualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public List<MetricContributionDto> getMetricContributions() {
        return metricContributions;
    }

    public void setMetricContributions(List<MetricContributionDto> metricContributions) {
        this.metricContributions = metricContributions != null ? metricContributions : new ArrayList<>();
    }
}
