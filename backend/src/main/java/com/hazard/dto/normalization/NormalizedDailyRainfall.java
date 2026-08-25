package com.hazard.dto.normalization;

import com.hazard.domain.hazard.QualityStatus;

import java.time.LocalDate;

/**
 * Normalized Daily Meteorological Observation for a station.
 */
public class NormalizedDailyRainfall {

    private String stationName;
    private LocalDate date;
    private String associatedDistrict;
    private Double longitude;
    private Double latitude;
    private Double rawDailyTotalMm;
    private NormalizedHazardMetric normalizedDailyTotal;
    private Double rawPeakHourlyMm;
    private NormalizedHazardMetric normalizedPeakHourly;
    private int rainyHours;
    private int heavyRainHours;
    private int veryHeavyRainHours;
    private boolean exceedsHeavyThreshold;
    private QualityStatus qualityStatus = QualityStatus.VALID;

    public NormalizedDailyRainfall() {
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

    public NormalizedHazardMetric getNormalizedDailyTotal() {
        return normalizedDailyTotal;
    }

    public void setNormalizedDailyTotal(NormalizedHazardMetric normalizedDailyTotal) {
        this.normalizedDailyTotal = normalizedDailyTotal;
    }

    public Double getRawPeakHourlyMm() {
        return rawPeakHourlyMm;
    }

    public void setRawPeakHourlyMm(Double rawPeakHourlyMm) {
        this.rawPeakHourlyMm = rawPeakHourlyMm;
    }

    public NormalizedHazardMetric getNormalizedPeakHourly() {
        return normalizedPeakHourly;
    }

    public void setNormalizedPeakHourly(NormalizedHazardMetric normalizedPeakHourly) {
        this.normalizedPeakHourly = normalizedPeakHourly;
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
