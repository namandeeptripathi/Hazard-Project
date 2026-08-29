package com.hazard.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Stage 9A — Request payload for defining a disaster simulation scenario.
 */
@Schema(description = "Scenario creation request specifying parameter perturbations")
public class ScenarioCreateRequestDto {

    @Schema(description = "User-friendly name of the scenario", example = "Monsoon Extreme Rainfall +20%")
    private String scenarioName;

    @Schema(description = "Type of scenario: BASELINE, RAINFALL_CHANGE, HAZARD_INTENSITY, POPULATION_EXPOSURE, MULTI_FACTOR", example = "RAINFALL_CHANGE")
    private String scenarioType;

    @Schema(description = "Detailed description and operational objective of the scenario", example = "Evaluates flood inundation under a 20% increase in monsoon rainfall.")
    private String description;

    @Schema(description = "Precipitation change in percentage (+/- %)", example = "20.0")
    private Double rainfallChange;

    @Schema(description = "Hazard intensity scaling change in percentage (+/- %)", example = "0.0")
    private Double hazardIntensityChange;

    @Schema(description = "Population exposure scaling change in percentage (+/- %)", example = "0.0")
    private Double populationExposureChange;

    public ScenarioCreateRequestDto() {
    }

    public ScenarioCreateRequestDto(String scenarioName,
                                    String scenarioType,
                                    String description,
                                    Double rainfallChange,
                                    Double hazardIntensityChange,
                                    Double populationExposureChange) {
        this.scenarioName = scenarioName;
        this.scenarioType = scenarioType;
        this.description = description;
        this.rainfallChange = rainfallChange;
        this.hazardIntensityChange = hazardIntensityChange;
        this.populationExposureChange = populationExposureChange;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(String scenarioType) {
        this.scenarioType = scenarioType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRainfallChange() {
        return rainfallChange;
    }

    public void setRainfallChange(Double rainfallChange) {
        this.rainfallChange = rainfallChange;
    }

    public Double getHazardIntensityChange() {
        return hazardIntensityChange;
    }

    public void setHazardIntensityChange(Double hazardIntensityChange) {
        this.hazardIntensityChange = hazardIntensityChange;
    }

    public Double getPopulationExposureChange() {
        return populationExposureChange;
    }

    public void setPopulationExposureChange(Double populationExposureChange) {
        this.populationExposureChange = populationExposureChange;
    }
}
