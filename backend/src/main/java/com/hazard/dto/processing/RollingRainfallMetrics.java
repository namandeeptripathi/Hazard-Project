package com.hazard.dto.processing;

import java.time.LocalDateTime;

/**
 * Multi-Window Rolling Rainfall Metrics for real-time hazard monitoring.
 */
public class RollingRainfallMetrics {

    private String stationName;
    private String associatedDistrict;
    private LocalDateTime timestamp;
    private Double currentHourlyMm;
    private Double rolling3hMm;
    private Double rolling6hMm;
    private Double rolling12hMm;
    private Double rolling24hMm;
    private boolean isHeavyRainfall;
    private boolean isVeryHeavyRainfall;

    public RollingRainfallMetrics() {
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

    public Double getCurrentHourlyMm() {
        return currentHourlyMm;
    }

    public void setCurrentHourlyMm(Double currentHourlyMm) {
        this.currentHourlyMm = currentHourlyMm;
    }

    public Double getRolling3hMm() {
        return rolling3hMm;
    }

    public void setRolling3hMm(Double rolling3hMm) {
        this.rolling3hMm = rolling3hMm;
    }

    public Double getRolling6hMm() {
        return rolling6hMm;
    }

    public void setRolling6hMm(Double rolling6hMm) {
        this.rolling6hMm = rolling6hMm;
    }

    public Double getRolling12hMm() {
        return rolling12hMm;
    }

    public void setRolling12hMm(Double rolling12hMm) {
        this.rolling12hMm = rolling12hMm;
    }

    public Double getRolling24hMm() {
        return rolling24hMm;
    }

    public void setRolling24hMm(Double rolling24hMm) {
        this.rolling24hMm = rolling24hMm;
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
}
