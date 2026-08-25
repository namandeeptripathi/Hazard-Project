package com.hazard.dto.normalization;

import com.hazard.domain.hazard.QualityStatus;

import java.time.LocalDateTime;

/**
 * Multi-Window Normalized Rolling Rainfall Metrics for a station at a timestamp.
 */
public class NormalizedRollingRainfall {

    private String stationName;
    private String associatedDistrict;
    private LocalDateTime timestamp;
    private NormalizedHazardMetric currentHourly;
    private NormalizedHazardMetric rolling3h;
    private NormalizedHazardMetric rolling6h;
    private NormalizedHazardMetric rolling12h;
    private NormalizedHazardMetric rolling24h;
    private boolean isHeavyRainfall;
    private boolean isVeryHeavyRainfall;
    private QualityStatus qualityStatus = QualityStatus.VALID;

    public NormalizedRollingRainfall() {
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

    public NormalizedHazardMetric getCurrentHourly() {
        return currentHourly;
    }

    public void setCurrentHourly(NormalizedHazardMetric currentHourly) {
        this.currentHourly = currentHourly;
    }

    public NormalizedHazardMetric getRolling3h() {
        return rolling3h;
    }

    public void setRolling3h(NormalizedHazardMetric rolling3h) {
        this.rolling3h = rolling3h;
    }

    public NormalizedHazardMetric getRolling6h() {
        return rolling6h;
    }

    public void setRolling6h(NormalizedHazardMetric rolling6h) {
        this.rolling6h = rolling6h;
    }

    public NormalizedHazardMetric getRolling12h() {
        return rolling12h;
    }

    public void setRolling12h(NormalizedHazardMetric rolling12h) {
        this.rolling12h = rolling12h;
    }

    public NormalizedHazardMetric getRolling24h() {
        return rolling24h;
    }

    public void setRolling24h(NormalizedHazardMetric rolling24h) {
        this.rolling24h = rolling24h;
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
}
