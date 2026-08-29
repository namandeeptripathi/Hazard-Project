package com.hazard.dto.scenario;

import com.hazard.domain.scenario.ScenarioType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 9C — Master Response DTO encapsulating state-wide / district Red-Zone recalculation results.
 */
@Schema(description = "Dynamic Red-Zone recalculation simulation outcome across districts")
public class ScenarioRedZoneSimulationResultDto {

    @Schema(description = "Scenario identifier", example = "SCEN-RAIN-101")
    private String scenarioId;

    @Schema(description = "Scenario name", example = "Monsoon Extreme Rainfall +20%")
    private String scenarioName;

    @Schema(description = "Scenario type code", example = "RAINFALL_CHANGE")
    private ScenarioType scenarioType;

    @Schema(description = "Scenario type display name", example = "Rainfall Change Scenario")
    private String scenarioTypeDisplayName;

    @Schema(description = "Scenario description")
    private String description;

    // Applied Scenario Perturbations
    @Schema(description = "Applied rainfall change percentage", example = "20.0")
    private double appliedRainfallChange;

    @Schema(description = "Applied hazard intensity change percentage", example = "0.0")
    private double appliedHazardIntensityChange;

    @Schema(description = "Applied population exposure change percentage", example = "0.0")
    private double appliedPopulationExposureChange;

    // Aggregate Counts
    @Schema(description = "Total administrative districts evaluated", example = "38")
    private int totalDistrictsEvaluated;

    @Schema(description = "Total districts classified as Red Zone in baseline", example = "12")
    private int baselineRedZoneCount;

    @Schema(description = "Total districts classified as Red Zone under simulated scenario", example = "15")
    private int simulatedRedZoneCount;

    @Schema(description = "Net change in Red-Zone district count [simulated - baseline]", example = "+3")
    private int netRedZoneChange;

    @Schema(description = "Count of districts newly entering Red-Zone status (NO -> YES)", example = "3")
    private int newlyEnteredRedZoneCount;

    @Schema(description = "Count of districts exiting Red-Zone status (YES -> NO)", example = "0")
    private int leftRedZoneCount;

    @Schema(description = "Count of districts remaining in Red-Zone status (YES -> YES)", example = "12")
    private int retainedRedZoneCount;

    @Schema(description = "Count of districts remaining outside Red-Zone status (NO -> NO)", example = "23")
    private int unchangedNonRedZoneCount;

    // District Name Groupings
    @Schema(description = "Names of districts newly entering Red-Zone status")
    private List<String> newlyEnteredDistricts = new ArrayList<>();

    @Schema(description = "Names of districts exiting Red-Zone status")
    private List<String> leftRedZoneDistricts = new ArrayList<>();

    @Schema(description = "Names of districts persistently in Red-Zone status")
    private List<String> retainedRedZoneDistricts = new ArrayList<>();

    @Schema(description = "Names of districts remaining non-Red Zone")
    private List<String> unchangedNonRedZoneDistricts = new ArrayList<>();

    // Detailed per-district results
    @Schema(description = "Detailed Red-Zone simulation outcome for each evaluated district")
    private List<DistrictRedZoneSimulationDto> districtResults = new ArrayList<>();

    @Schema(description = "Timestamp when Red-Zone simulation was calculated")
    private LocalDateTime simulatedAt;

    @Schema(description = "Human-readable summary of the Red-Zone recalculation outcome")
    private String summary;

    public ScenarioRedZoneSimulationResultDto() {
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

    public int getTotalDistrictsEvaluated() {
        return totalDistrictsEvaluated;
    }

    public void setTotalDistrictsEvaluated(int totalDistrictsEvaluated) {
        this.totalDistrictsEvaluated = totalDistrictsEvaluated;
    }

    public int getBaselineRedZoneCount() {
        return baselineRedZoneCount;
    }

    public void setBaselineRedZoneCount(int baselineRedZoneCount) {
        this.baselineRedZoneCount = baselineRedZoneCount;
    }

    public int getSimulatedRedZoneCount() {
        return simulatedRedZoneCount;
    }

    public void setSimulatedRedZoneCount(int simulatedRedZoneCount) {
        this.simulatedRedZoneCount = simulatedRedZoneCount;
    }

    public int getNetRedZoneChange() {
        return netRedZoneChange;
    }

    public void setNetRedZoneChange(int netRedZoneChange) {
        this.netRedZoneChange = netRedZoneChange;
    }

    public int getNewlyEnteredRedZoneCount() {
        return newlyEnteredRedZoneCount;
    }

    public void setNewlyEnteredRedZoneCount(int newlyEnteredRedZoneCount) {
        this.newlyEnteredRedZoneCount = newlyEnteredRedZoneCount;
    }

    public int getLeftRedZoneCount() {
        return leftRedZoneCount;
    }

    public void setLeftRedZoneCount(int leftRedZoneCount) {
        this.leftRedZoneCount = leftRedZoneCount;
    }

    public int getRetainedRedZoneCount() {
        return retainedRedZoneCount;
    }

    public void setRetainedRedZoneCount(int retainedRedZoneCount) {
        this.retainedRedZoneCount = retainedRedZoneCount;
    }

    public int getUnchangedNonRedZoneCount() {
        return unchangedNonRedZoneCount;
    }

    public void setUnchangedNonRedZoneCount(int unchangedNonRedZoneCount) {
        this.unchangedNonRedZoneCount = unchangedNonRedZoneCount;
    }

    public List<String> getNewlyEnteredDistricts() {
        return newlyEnteredDistricts;
    }

    public void setNewlyEnteredDistricts(List<String> newlyEnteredDistricts) {
        this.newlyEnteredDistricts = newlyEnteredDistricts != null ? newlyEnteredDistricts : new ArrayList<>();
    }

    public List<String> getLeftRedZoneDistricts() {
        return leftRedZoneDistricts;
    }

    public void setLeftRedZoneDistricts(List<String> leftRedZoneDistricts) {
        this.leftRedZoneDistricts = leftRedZoneDistricts != null ? leftRedZoneDistricts : new ArrayList<>();
    }

    public List<String> getRetainedRedZoneDistricts() {
        return retainedRedZoneDistricts;
    }

    public void setRetainedRedZoneDistricts(List<String> retainedRedZoneDistricts) {
        this.retainedRedZoneDistricts = retainedRedZoneDistricts != null ? retainedRedZoneDistricts : new ArrayList<>();
    }

    public List<String> getUnchangedNonRedZoneDistricts() {
        return unchangedNonRedZoneDistricts;
    }

    public void setUnchangedNonRedZoneDistricts(List<String> unchangedNonRedZoneDistricts) {
        this.unchangedNonRedZoneDistricts = unchangedNonRedZoneDistricts != null ? unchangedNonRedZoneDistricts : new ArrayList<>();
    }

    public List<DistrictRedZoneSimulationDto> getDistrictResults() {
        return districtResults;
    }

    public void setDistrictResults(List<DistrictRedZoneSimulationDto> districtResults) {
        this.districtResults = districtResults != null ? districtResults : new ArrayList<>();
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
