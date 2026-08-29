package com.hazard.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Stage 9B — Simulation Context DTO representing the temporary in-memory
 * state comparison between baseline inputs and scenario-adjusted inputs.
 */
@Schema(description = "Temporary in-memory simulation input adjustments and baseline comparison")
public class ScenarioSimulationContextDto {

    @Schema(description = "Target district name", example = "Sitamarhi")
    private String districtName;

    // Applied Scenario Perturbations (%)
    @Schema(description = "Applied rainfall perturbation percentage", example = "20.0")
    private double appliedRainfallChange;

    @Schema(description = "Applied hazard intensity perturbation percentage", example = "0.0")
    private double appliedHazardIntensityChange;

    @Schema(description = "Applied population exposure perturbation percentage", example = "0.0")
    private double appliedPopulationExposureChange;

    // Computed Multipliers
    @Schema(description = "Effective composite hazard scaling multiplier", example = "1.20")
    private double effectiveHazardMultiplier;

    @Schema(description = "Effective population scaling multiplier", example = "1.00")
    private double effectivePopulationMultiplier;

    // 1. Hazard Pillar State
    @Schema(description = "Baseline hazard score [0, 1]", example = "0.6000")
    private Double baselineHazardScore;

    @Schema(description = "Temporary simulated hazard score [0, 1]", example = "0.7200")
    private Double simulatedHazardScore;

    // 2. Exposure Pillar State
    @Schema(description = "Baseline exposed population count", example = "94293")
    private Long baselineExposedPopulation;

    @Schema(description = "Temporary simulated exposed population count", example = "94293")
    private Long simulatedExposedPopulation;

    @Schema(description = "Baseline population exposure score [0, 1]", example = "0.2226")
    private Double baselinePopulationExposureScore;

    @Schema(description = "Temporary simulated population exposure score [0, 1]", example = "0.2226")
    private Double simulatedPopulationExposureScore;

    @Schema(description = "Baseline combined exposure score [0, 1]", example = "0.4500")
    private Double baselineCombinedExposureScore;

    @Schema(description = "Temporary simulated combined exposure score [0, 1]", example = "0.4500")
    private Double simulatedCombinedExposureScore;

    // 3. Vulnerability & Historical Pillars (Unperturbed)
    @Schema(description = "Baseline vulnerability score [0, 1]", example = "0.4850")
    private Double baselineVulnerabilityScore;

    @Schema(description = "Temporary simulated vulnerability score [0, 1]", example = "0.4850")
    private Double simulatedVulnerabilityScore;

    @Schema(description = "Baseline historical hotspot index [0, 1]", example = "0.3500")
    private Double baselineHistoricalScore;

    @Schema(description = "Temporary simulated historical hotspot index [0, 1]", example = "0.3500")
    private Double simulatedHistoricalScore;

    public ScenarioSimulationContextDto() {
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
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

    public double getEffectiveHazardMultiplier() {
        return effectiveHazardMultiplier;
    }

    public void setEffectiveHazardMultiplier(double effectiveHazardMultiplier) {
        this.effectiveHazardMultiplier = effectiveHazardMultiplier;
    }

    public double getEffectivePopulationMultiplier() {
        return effectivePopulationMultiplier;
    }

    public void setEffectivePopulationMultiplier(double effectivePopulationMultiplier) {
        this.effectivePopulationMultiplier = effectivePopulationMultiplier;
    }

    public Double getBaselineHazardScore() {
        return baselineHazardScore;
    }

    public void setBaselineHazardScore(Double baselineHazardScore) {
        this.baselineHazardScore = baselineHazardScore;
    }

    public Double getSimulatedHazardScore() {
        return simulatedHazardScore;
    }

    public void setSimulatedHazardScore(Double simulatedHazardScore) {
        this.simulatedHazardScore = simulatedHazardScore;
    }

    public Long getBaselineExposedPopulation() {
        return baselineExposedPopulation;
    }

    public void setBaselineExposedPopulation(Long baselineExposedPopulation) {
        this.baselineExposedPopulation = baselineExposedPopulation;
    }

    public Long getSimulatedExposedPopulation() {
        return simulatedExposedPopulation;
    }

    public void setSimulatedExposedPopulation(Long simulatedExposedPopulation) {
        this.simulatedExposedPopulation = simulatedExposedPopulation;
    }

    public Double getBaselinePopulationExposureScore() {
        return baselinePopulationExposureScore;
    }

    public void setBaselinePopulationExposureScore(Double baselinePopulationExposureScore) {
        this.baselinePopulationExposureScore = baselinePopulationExposureScore;
    }

    public Double getSimulatedPopulationExposureScore() {
        return simulatedPopulationExposureScore;
    }

    public void setSimulatedPopulationExposureScore(Double simulatedPopulationExposureScore) {
        this.simulatedPopulationExposureScore = simulatedPopulationExposureScore;
    }

    public Double getBaselineCombinedExposureScore() {
        return baselineCombinedExposureScore;
    }

    public void setBaselineCombinedExposureScore(Double baselineCombinedExposureScore) {
        this.baselineCombinedExposureScore = baselineCombinedExposureScore;
    }

    public Double getSimulatedCombinedExposureScore() {
        return simulatedCombinedExposureScore;
    }

    public void setSimulatedCombinedExposureScore(Double simulatedCombinedExposureScore) {
        this.simulatedCombinedExposureScore = simulatedCombinedExposureScore;
    }

    public Double getBaselineVulnerabilityScore() {
        return baselineVulnerabilityScore;
    }

    public void setBaselineVulnerabilityScore(Double baselineVulnerabilityScore) {
        this.baselineVulnerabilityScore = baselineVulnerabilityScore;
    }

    public Double getSimulatedVulnerabilityScore() {
        return simulatedVulnerabilityScore;
    }

    public void setSimulatedVulnerabilityScore(Double simulatedVulnerabilityScore) {
        this.simulatedVulnerabilityScore = simulatedVulnerabilityScore;
    }

    public Double getBaselineHistoricalScore() {
        return baselineHistoricalScore;
    }

    public void setBaselineHistoricalScore(Double baselineHistoricalScore) {
        this.baselineHistoricalScore = baselineHistoricalScore;
    }

    public Double getSimulatedHistoricalScore() {
        return simulatedHistoricalScore;
    }

    public void setSimulatedHistoricalScore(Double simulatedHistoricalScore) {
        this.simulatedHistoricalScore = simulatedHistoricalScore;
    }
}
