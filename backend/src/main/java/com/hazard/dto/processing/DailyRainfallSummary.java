package com.hazard.dto.processing;

import com.hazard.domain.hazard.QualityStatus;

import java.time.LocalDate;

/**
 * Aggregated Daily Meteorological Observation for a station.
 */
public class DailyRainfallSummary {

    private String stationName;
    private LocalDate date;
    private String associatedDistrict;
    private Double longitude;
    private Double latitude;
    private Double dailyTotalMm;
    private Double peakHourlyMm;
    private int rainyHours;
    private int heavyRainHours;
    private int veryHeavyRainHours;
    private boolean exceedsHeavyThreshold;
    private QualityStatus qualityStatus = QualityStatus.VALID;

    public DailyRainfallSummary() {
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

    public Double getDailyTotalMm() {
        return dailyTotalMm;
    }

    public void setDailyTotalMm(Double dailyTotalMm) {
        this.dailyTotalMm = dailyTotalMm;
    }

    public Double getPeakHourlyMm() {
        return peakHourlyMm;
    }

    public void setPeakHourlyMm(Double peakHourlyMm) {
        this.peakHourlyMm = peakHourlyMm;
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
}
