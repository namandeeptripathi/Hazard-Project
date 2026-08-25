package com.hazard.dto.historical;

import java.util.List;
import java.util.Map;

/**
 * DTO exposing active historical intelligence configuration, default time windows,
 * and high-severity classification thresholds.
 */
public class HistoricalConfigDto {

    private String defaultTimeWindow;
    private List<String> supportedHazardTypes;
    private double highSeverityThresholdDfo;
    private double highSeverityThresholdWeatherMm;
    private double defaultHistoricalPeriodYears;
    private Map<String, Double> hotspotWeightComponents;
    private String calculationVersion;

    public HistoricalConfigDto() {}

    public String getDefaultTimeWindow() {
        return defaultTimeWindow;
    }

    public void setDefaultTimeWindow(String defaultTimeWindow) {
        this.defaultTimeWindow = defaultTimeWindow;
    }

    public List<String> getSupportedHazardTypes() {
        return supportedHazardTypes;
    }

    public void setSupportedHazardTypes(List<String> supportedHazardTypes) {
        this.supportedHazardTypes = supportedHazardTypes;
    }

    public double getHighSeverityThresholdDfo() {
        return highSeverityThresholdDfo;
    }

    public void setHighSeverityThresholdDfo(double highSeverityThresholdDfo) {
        this.highSeverityThresholdDfo = highSeverityThresholdDfo;
    }

    public double getHighSeverityThresholdWeatherMm() {
        return highSeverityThresholdWeatherMm;
    }

    public void setHighSeverityThresholdWeatherMm(double highSeverityThresholdWeatherMm) {
        this.highSeverityThresholdWeatherMm = highSeverityThresholdWeatherMm;
    }

    public double getDefaultHistoricalPeriodYears() {
        return defaultHistoricalPeriodYears;
    }

    public void setDefaultHistoricalPeriodYears(double defaultHistoricalPeriodYears) {
        this.defaultHistoricalPeriodYears = defaultHistoricalPeriodYears;
    }

    public Map<String, Double> getHotspotWeightComponents() {
        return hotspotWeightComponents;
    }

    public void setHotspotWeightComponents(Map<String, Double> hotspotWeightComponents) {
        this.hotspotWeightComponents = hotspotWeightComponents;
    }

    public String getCalculationVersion() {
        return calculationVersion;
    }

    public void setCalculationVersion(String calculationVersion) {
        this.calculationVersion = calculationVersion;
    }
}
