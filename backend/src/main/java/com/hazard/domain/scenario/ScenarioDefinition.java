package com.hazard.domain.scenario;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Stage 9A — Domain Entity representing a Disaster Simulation Scenario Definition.
 *
 * Defines the parameters for hypothetical what-if scenario simulations:
 * - rainfallChange: Percentage delta relative to baseline (e.g. +20.0 = +20% precipitation).
 * - hazardIntensityChange: Percentage delta relative to baseline (e.g. +15.0 = +15% hazard intensity).
 * - populationExposureChange: Percentage delta relative to baseline (e.g. +30.0 = +30% exposed population).
 *
 * Baseline scenario represents zero perturbation and never alters underlying stored/project data.
 */
public class ScenarioDefinition {

    public static final String BASELINE_SCENARIO_ID = "SCEN-BASELINE";

    private String scenarioId;
    private String scenarioName;
    private ScenarioType scenarioType;
    private String description;

    // Simulation parameter adjustments (percentages, 0.0 = 0% delta)
    private double rainfallChange;
    private double hazardIntensityChange;
    private double populationExposureChange;

    private boolean isBaseline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ScenarioDefinition() {
        this.scenarioType = ScenarioType.BASELINE;
        this.rainfallChange = 0.0;
        this.hazardIntensityChange = 0.0;
        this.populationExposureChange = 0.0;
        this.isBaseline = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ScenarioDefinition(String scenarioId,
                              String scenarioName,
                              ScenarioType scenarioType,
                              String description,
                              double rainfallChange,
                              double hazardIntensityChange,
                              double populationExposureChange,
                              boolean isBaseline) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.scenarioType = scenarioType != null ? scenarioType : ScenarioType.BASELINE;
        this.description = description;
        this.rainfallChange = rainfallChange;
        this.hazardIntensityChange = hazardIntensityChange;
        this.populationExposureChange = populationExposureChange;
        this.isBaseline = isBaseline;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Creates an official immutable baseline reference scenario.
     * All parameter deltas are guaranteed to be exactly 0.0.
     */
    public static ScenarioDefinition createBaseline() {
        return new ScenarioDefinition(
                BASELINE_SCENARIO_ID,
                "Baseline Scenario",
                ScenarioType.BASELINE,
                "Default unperturbed baseline reference conditions (0% rainfall change, 0% hazard intensity change, 0% population exposure shift).",
                0.0,
                0.0,
                0.0,
                true
        );
    }

    // Getters and Setters
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
        return isBaseline || (scenarioType == ScenarioType.BASELINE);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioDefinition that = (ScenarioDefinition) o;
        return Objects.equals(scenarioId, that.scenarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId);
    }

    @Override
    public String toString() {
        return "ScenarioDefinition{" +
                "scenarioId='" + scenarioId + '\'' +
                ", scenarioName='" + scenarioName + '\'' +
                ", scenarioType=" + scenarioType +
                ", rainfallChange=" + rainfallChange + "%" +
                ", hazardIntensityChange=" + hazardIntensityChange + "%" +
                ", populationExposureChange=" + populationExposureChange + "%" +
                ", isBaseline=" + isBaseline +
                '}';
    }
}
