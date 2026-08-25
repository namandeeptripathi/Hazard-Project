package com.hazard.dto.exposure;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO exposing the active configuration parameters and classification thresholds
 * for the Population Exposure Engine.
 */
public class PopulationExposureConfigDto {

    private double lowThresholdPercent;
    private double moderateThresholdPercent;
    private double highThresholdPercent;
    private double defaultHazardBufferMeters;
    private double residentialDensityPersonsPerHectare;

    private Map<String, Long> settlementArchetypeDefaultPopulations = new LinkedHashMap<>();
    private Map<String, String> categoryColorCodes = new LinkedHashMap<>();

    public PopulationExposureConfigDto() {}

    public double getLowThresholdPercent() {
        return lowThresholdPercent;
    }

    public void setLowThresholdPercent(double lowThresholdPercent) {
        this.lowThresholdPercent = lowThresholdPercent;
    }

    public double getModerateThresholdPercent() {
        return moderateThresholdPercent;
    }

    public void setModerateThresholdPercent(double moderateThresholdPercent) {
        this.moderateThresholdPercent = moderateThresholdPercent;
    }

    public double getHighThresholdPercent() {
        return highThresholdPercent;
    }

    public void setHighThresholdPercent(double highThresholdPercent) {
        this.highThresholdPercent = highThresholdPercent;
    }

    public double getDefaultHazardBufferMeters() {
        return defaultHazardBufferMeters;
    }

    public void setDefaultHazardBufferMeters(double defaultHazardBufferMeters) {
        this.defaultHazardBufferMeters = defaultHazardBufferMeters;
    }

    public double getResidentialDensityPersonsPerHectare() {
        return residentialDensityPersonsPerHectare;
    }

    public void setResidentialDensityPersonsPerHectare(double residentialDensityPersonsPerHectare) {
        this.residentialDensityPersonsPerHectare = residentialDensityPersonsPerHectare;
    }

    public Map<String, Long> getSettlementArchetypeDefaultPopulations() {
        return settlementArchetypeDefaultPopulations;
    }

    public void setSettlementArchetypeDefaultPopulations(Map<String, Long> settlementArchetypeDefaultPopulations) {
        this.settlementArchetypeDefaultPopulations = settlementArchetypeDefaultPopulations;
    }

    public Map<String, String> getCategoryColorCodes() {
        return categoryColorCodes;
    }

    public void setCategoryColorCodes(Map<String, String> categoryColorCodes) {
        this.categoryColorCodes = categoryColorCodes;
    }
}
