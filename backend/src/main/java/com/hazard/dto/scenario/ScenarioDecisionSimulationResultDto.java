package com.hazard.dto.scenario;

import com.hazard.domain.scenario.ScenarioType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 9D — Master Response DTO encapsulating simulated Priority & Relocation decision outcomes across districts.
 */
@Schema(description = "Simulated Priority escalation and Relocation planning outcome under scenario conditions")
public class ScenarioDecisionSimulationResultDto {

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

    // General Aggregate
    @Schema(description = "Total administrative districts evaluated", example = "38")
    private int totalDistrictsEvaluated;

    // Red-Zone Shifts
    @Schema(description = "Baseline Red-Zone district count", example = "0")
    private int baselineRedZoneCount;

    @Schema(description = "Simulated Red-Zone district count", example = "3")
    private int simulatedRedZoneCount;

    @Schema(description = "Net Red-Zone district change", example = "+3")
    private int netRedZoneChange;

    // Priority Distribution & Shifts
    @Schema(description = "Count of districts classified as IMMEDIATE priority under simulation", example = "5")
    private int immediatePriorityCount;

    @Schema(description = "Count of districts classified as SHORT_TERM priority under simulation", example = "12")
    private int shortTermPriorityCount;

    @Schema(description = "Count of districts classified as MEDIUM_TERM priority under simulation", example = "15")
    private int mediumTermPriorityCount;

    @Schema(description = "Count of districts classified as MONITORING priority under simulation", example = "6")
    private int monitoringPriorityCount;

    @Schema(description = "Count of districts whose priority level escalated upward", example = "8")
    private int priorityShiftUpCount;

    @Schema(description = "Count of districts whose priority level de-escalated downward", example = "0")
    private int priorityShiftDownCount;

    @Schema(description = "Count of districts whose priority level remained unchanged", example = "30")
    private int priorityUnchangedCount;

    // Relocation Aggregates
    @Schema(description = "Total vulnerable population evaluated across districts", example = "9500")
    private long totalVulnerablePopulation;

    @Schema(description = "Total population successfully allocated to safe shelters", example = "8200")
    private long totalAllocatedPopulation;

    @Schema(description = "Total population remaining in shelter deficit", example = "1300")
    private long totalUnallocatedPopulation;

    @Schema(description = "Overall state-wide shelter capacity deficit percentage", example = "13.7")
    private double overallCapacityDeficitPercentage;

    // Detailed District Decision Results
    @Schema(description = "Detailed decision outcome per district")
    private List<DistrictDecisionSimulationDto> districtResults = new ArrayList<>();

    @Schema(description = "Timestamp when decision simulation was computed")
    private LocalDateTime simulatedAt;

    @Schema(description = "Human-readable summary of the decision simulation outcome")
    private String summary;

    public ScenarioDecisionSimulationResultDto() {
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

    public int getImmediatePriorityCount() {
        return immediatePriorityCount;
    }

    public void setImmediatePriorityCount(int immediatePriorityCount) {
        this.immediatePriorityCount = immediatePriorityCount;
    }

    public int getShortTermPriorityCount() {
        return shortTermPriorityCount;
    }

    public void setShortTermPriorityCount(int shortTermPriorityCount) {
        this.shortTermPriorityCount = shortTermPriorityCount;
    }

    public int getMediumTermPriorityCount() {
        return mediumTermPriorityCount;
    }

    public void setMediumTermPriorityCount(int mediumTermPriorityCount) {
        this.mediumTermPriorityCount = mediumTermPriorityCount;
    }

    public int getMonitoringPriorityCount() {
        return monitoringPriorityCount;
    }

    public void setMonitoringPriorityCount(int monitoringPriorityCount) {
        this.monitoringPriorityCount = monitoringPriorityCount;
    }

    public int getPriorityShiftUpCount() {
        return priorityShiftUpCount;
    }

    public void setPriorityShiftUpCount(int priorityShiftUpCount) {
        this.priorityShiftUpCount = priorityShiftUpCount;
    }

    public int getPriorityShiftDownCount() {
        return priorityShiftDownCount;
    }

    public void setPriorityShiftDownCount(int priorityShiftDownCount) {
        this.priorityShiftDownCount = priorityShiftDownCount;
    }

    public int getPriorityUnchangedCount() {
        return priorityUnchangedCount;
    }

    public void setPriorityUnchangedCount(int priorityUnchangedCount) {
        this.priorityUnchangedCount = priorityUnchangedCount;
    }

    public long getTotalVulnerablePopulation() {
        return totalVulnerablePopulation;
    }

    public void setTotalVulnerablePopulation(long totalVulnerablePopulation) {
        this.totalVulnerablePopulation = totalVulnerablePopulation;
    }

    public long getTotalAllocatedPopulation() {
        return totalAllocatedPopulation;
    }

    public void setTotalAllocatedPopulation(long totalAllocatedPopulation) {
        this.totalAllocatedPopulation = totalAllocatedPopulation;
    }

    public long getTotalUnallocatedPopulation() {
        return totalUnallocatedPopulation;
    }

    public void setTotalUnallocatedPopulation(long totalUnallocatedPopulation) {
        this.totalUnallocatedPopulation = totalUnallocatedPopulation;
    }

    public double getOverallCapacityDeficitPercentage() {
        return overallCapacityDeficitPercentage;
    }

    public void setOverallCapacityDeficitPercentage(double overallCapacityDeficitPercentage) {
        this.overallCapacityDeficitPercentage = overallCapacityDeficitPercentage;
    }

    public List<DistrictDecisionSimulationDto> getDistrictResults() {
        return districtResults;
    }

    public void setDistrictResults(List<DistrictDecisionSimulationDto> districtResults) {
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
