package com.hazard.dto.scoring;

import com.hazard.domain.hazard.QualityStatus;
import com.hazard.domain.hazard.SeverityTier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Scored Daily Meteorological Hazard Observation.
 */
public class DailyRainfallScoreDto {

    private String stationName;
    private LocalDate date;
    private String associatedDistrict;
    private Double longitude;
    private Double latitude;
    private Double rawDailyTotalMm;
    private Double rawPeakHourlyMm;
    private Double rainfallHazardScore;
    private SeverityTier severityTier;
    private int rainyHours;
    private int heavyRainHours;
    private int veryHeavyRainHours;
    private boolean exceedsHeavyThreshold;
    private QualityStatus qualityStatus;
    private List<MetricContributionDto> metricContributions = new ArrayList<>();

    public DailyRainfallScoreDto() {
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getAssociatedDistrict() {
        return associatedDistrict;
    }

    public void setAssociatedDistrict(String associatedDistrict) {
        this.associatedDistrict = associatedDistrict;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getRawDailyTotalMm() {
        return rawDailyTotalMm;
    }

    public void setRawDailyTotalMm(Double rawDailyTotalMm) {
        this.rawDailyTotalMm = rawDailyTotalMm;
    }

    public Double getRawPeakHourlyMm() {
        return rawPeakHourlyMm;
    }

    public void setRawPeakHourlyMm(Double rawPeakHourlyMm) {
        this.rawPeakHourlyMm = rawPeakHourlyMm;
    }

    public Double getRainfallHazardScore() {
        return rainfallHazardScore;
    }

    public void setRainfallHazardScore(Double rainfallHazardScore) {
        this.rainfallHazardScore = rainfallHazardScore;
    }

    public SeverityTier getSeverityTier() {
        return severityTier;
    }

    public void setSeverityTier(SeverityTier severityTier) {
        this.severityTier = severityTier;
    }

    public int getRainyHours() {
        return rainyHours;
    }

    public void setRainyHours(int rainyHours) {
        this.rainyHours = rainyHours;
    }

    public int getHeavyRainHours() {
        return heavyRainHours;
    }

    public void setHeavyRainHours(int heavyRainHours) {
        this.heavyRainHours = heavyRainHours;
    }

    public int getVeryHeavyRainHours() {
        return veryHeavyRainHours;
    }

    public void setVeryHeavyRainHours(int veryHeavyRainHours) {
        this.veryHeavyRainHours = veryHeavyRainHours;
    }

    public boolean isExceedsHeavyThreshold() {
        return exceedsHeavyThreshold;
    }

    public void setExceedsHeavyThreshold(boolean exceedsHeavyThreshold) {
        this.exceedsHeavyThreshold = exceedsHeavyThreshold;
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
