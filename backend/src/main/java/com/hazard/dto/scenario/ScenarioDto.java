package com.hazard.dto.scenario;

import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.domain.scenario.ScenarioType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Stage 9A — Standard Response DTO representing a Disaster Simulation Scenario Definition.
 */
@Schema(description = "Disaster Simulation Scenario Definition DTO")
public class ScenarioDto {

    @Schema(description = "Unique scenario identifier", example = "SCEN-RAIN-001")
    private String scenarioId;

    @Schema(description = "Human-readable scenario name", example = "Monsoon Extreme Rainfall +20%")
    private String scenarioName;

    @Schema(description = "Scenario type code", example = "RAINFALL_CHANGE")
    private ScenarioType scenarioType;

    @Schema(description = "Display name of the scenario type", example = "Rainfall Change Scenario")
    private String scenarioTypeDisplayName;

    @Schema(description = "Detailed operational description", example = "Evaluates flood inundation under a 20% increase in monsoon rainfall.")
    private String description;

    @Schema(description = "Precipitation change percentage (+/- %)", example = "20.0")
    private double rainfallChange;

    @Schema(description = "Hazard intensity scaling change percentage (+/- %)", example = "0.0")
    private double hazardIntensityChange;

    @Schema(description = "Population exposure scaling change percentage (+/- %)", example = "0.0")
    private double populationExposureChange;

    @Schema(description = "Indicates whether this is the immutable reference baseline scenario", example = "false")
    private boolean isBaseline;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public ScenarioDto() {
    }

    public static ScenarioDto fromDomain(ScenarioDefinition domain) {
        if (domain == null) return null;

        ScenarioDto dto = new ScenarioDto();
        dto.setScenarioId(domain.getScenarioId());
        dto.setScenarioName(domain.getScenarioName());
        dto.setScenarioType(domain.getScenarioType());
        dto.setScenarioTypeDisplayName(domain.getScenarioType() != null ? domain.getScenarioType().getDisplayName() : null);
        dto.setDescription(domain.getDescription());
        dto.setRainfallChange(domain.getRainfallChange());
        dto.setHazardIntensityChange(domain.getHazardIntensityChange());
        dto.setPopulationExposureChange(domain.getPopulationExposureChange());
        dto.setBaseline(domain.isBaseline());
        dto.setCreatedAt(domain.getCreatedAt());
        dto.setUpdatedAt(domain.getUpdatedAt());
        return dto;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(ScenarioType scenarioType) {
        this.scenarioType = scenarioType;
    }

    public String getScenarioTypeDisplayName() {
        return scenarioTypeDisplayName;
    }

    public void setScenarioTypeDisplayName(String scenarioTypeDisplayName) {
        this.scenarioTypeDisplayName = scenarioTypeDisplayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRainfallChange() {
        return rainfallChange;
    }

    public void setRainfallChange(double rainfallChange) {
        this.rainfallChange = rainfallChange;
    }

    public double getHazardIntensityChange() {
        return hazardIntensityChange;
    }

    public void setHazardIntensityChange(double hazardIntensityChange) {
        this.hazardIntensityChange = hazardIntensityChange;
    }

    public double getPopulationExposureChange() {
        return populationExposureChange;
    }

    public void setPopulationExposureChange(double populationExposureChange) {
        this.populationExposureChange = populationExposureChange;
    }

    public boolean isBaseline() {
        return isBaseline;
    }

    public void setBaseline(boolean baseline) {
        isBaseline = baseline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
