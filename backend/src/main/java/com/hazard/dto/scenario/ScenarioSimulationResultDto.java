package com.hazard.dto.scenario;

import com.hazard.domain.scenario.ScenarioType;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Stage 9B — Response DTO encapsulating the outcome of a disaster scenario simulation.
 * Contains both baseline reference scores and temporary simulated risk results.
 */
@Schema(description = "Disaster scenario simulation execution outcome")
public class ScenarioSimulationResultDto {

    @Schema(description = "Scenario identifier", example = "SCEN-RAIN-101")
    private String scenarioId;

    @Schema(description = "Scenario name", example = "Monsoon Extreme Rainfall +20%")
    private String scenarioName;

    @Schema(description = "Scenario type code", example = "RAINFALL_CHANGE")
    private ScenarioType scenarioType;

    @Schema(description = "Display name of the scenario type", example = "Rainfall Change Scenario")
    private String scenarioTypeDisplayName;

    @Schema(description = "Simulated administrative district", example = "Sitamarhi")
    private String districtName;

    @Schema(description = "Scenario operational description")
    private String description;

    // Applied parameter perturbations
    @Schema(description = "Precipitation change percentage (+/- %)", example = "20.0")
    private double appliedRainfallChange;

    @Schema(description = "Hazard intensity scaling change percentage (+/- %)", example = "0.0")
    private double appliedHazardIntensityChange;

    @Schema(description = "Population exposure scaling change percentage (+/- %)", example = "0.0")
    private double appliedPopulationExposureChange;

    // Temporary Context
    @Schema(description = "Temporary in-memory input comparison context")
    private ScenarioSimulationContextDto simulationContext;

    // Risk Scores
    @Schema(description = "Simulated temporary risk calculation outcome")
    private DistrictRiskScoreDto simulatedRisk;

    @Schema(description = "Baseline reference risk calculation outcome")
    private DistrictRiskScoreDto baselineRisk;

    @Schema(description = "Risk score delta on normalized scale [-1.0, 1.0]", example = "0.0420")
    private Double deltaRiskScore;

    @Schema(description = "Risk score delta on 0-100 scale [-100.0, 100.0]", example = "4.2")
    private Double deltaRiskScore100;

    @Schema(description = "Risk shift direction: UNCHANGED, INCREASED, DECREASED", example = "INCREASED")
    private String riskDirection;

    @Schema(description = "Timestamp when simulation was executed")
    private LocalDateTime simulatedAt;

    @Schema(description = "Human-readable synthesis explaining the simulation results")
    private String summary;

    public ScenarioSimulationResultDto() {
        this.simulatedAt = LocalDateTime.now();
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

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAppliedRainfallChange() {
        return appliedRainfallChange;
    }

    public void setAppliedRainfallChange(double appliedRainfallChange) {
        this.appliedRainfallChange = appliedRainfallChange;
    }

    public double getAppliedHazardIntensityChange() {
        return appliedHazardIntensityChange;
    }

    public void setAppliedHazardIntensityChange(double appliedHazardIntensityChange) {
        this.appliedHazardIntensityChange = appliedHazardIntensityChange;
    }

    public double getAppliedPopulationExposureChange() {
        return appliedPopulationExposureChange;
    }

    public void setAppliedPopulationExposureChange(double appliedPopulationExposureChange) {
        this.appliedPopulationExposureChange = appliedPopulationExposureChange;
    }

    public ScenarioSimulationContextDto getSimulationContext() {
        return simulationContext;
    }

    public void setSimulationContext(ScenarioSimulationContextDto simulationContext) {
        this.simulationContext = simulationContext;
    }

    public DistrictRiskScoreDto getSimulatedRisk() {
        return simulatedRisk;
    }

    public void setSimulatedRisk(DistrictRiskScoreDto simulatedRisk) {
        this.simulatedRisk = simulatedRisk;
    }

    public DistrictRiskScoreDto getBaselineRisk() {
        return baselineRisk;
    }

    public void setBaselineRisk(DistrictRiskScoreDto baselineRisk) {
        this.baselineRisk = baselineRisk;
    }

    public Double getDeltaRiskScore() {
        return deltaRiskScore;
    }

    public void setDeltaRiskScore(Double deltaRiskScore) {
        this.deltaRiskScore = deltaRiskScore;
    }

    public Double getDeltaRiskScore100() {
        return deltaRiskScore100;
    }

    public void setDeltaRiskScore100(Double deltaRiskScore100) {
        this.deltaRiskScore100 = deltaRiskScore100;
    }

    public String getRiskDirection() {
        return riskDirection;
    }

    public void setRiskDirection(String riskDirection) {
        this.riskDirection = riskDirection;
    }

    public LocalDateTime getSimulatedAt() {
        return simulatedAt;
    }

    public void setSimulatedAt(LocalDateTime simulatedAt) {
        this.simulatedAt = simulatedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
