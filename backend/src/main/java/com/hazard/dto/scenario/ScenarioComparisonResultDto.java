package com.hazard.dto.scenario;

import com.hazard.domain.scenario.ScenarioType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 9E — Master Before vs After Scenario Comparison DTO.
 *
 * Encapsulates aggregate changes and district-level before/after comparisons across:
 * 1. Risk shifts
 * 2. Red-Zone status transitions
 * 3. Priority escalations
 * 4. Relocation demand and shelter deficit changes
 */
@Schema(description = "Before vs After disaster scenario comparison outcome")
public class ScenarioComparisonResultDto {

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

    // 1. Risk Aggregates
    @Schema(description = "Count of districts where simulated risk increased", example = "12")
    private int districtsWithIncreasedRiskCount;

    @Schema(description = "Count of districts where simulated risk decreased", example = "0")
    private int districtsWithDecreasedRiskCount;

    @Schema(description = "Count of districts where simulated risk remained unchanged", example = "26")
    private int districtsWithUnchangedRiskCount;

    @Schema(description = "Average risk score delta across evaluated districts on 0-100 scale", example = "3.4")
    private double averageRiskDelta100;

    // 2. Red-Zone Aggregates
    @Schema(description = "Baseline Red-Zone district count", example = "0")
    private int baselineRedZoneCount;

    @Schema(description = "Simulated Red-Zone district count", example = "3")
    private int simulatedRedZoneCount;

    @Schema(description = "Net Red-Zone district change [simulated - baseline]", example = "+3")
    private int netRedZoneChange;

    @Schema(description = "Count of districts newly entering Red-Zone status", example = "3")
    private int enteredRedZoneCount;

    @Schema(description = "Count of districts leaving Red-Zone status", example = "0")
    private int leftRedZoneCount;

    @Schema(description = "Count of districts retaining Red-Zone status", example = "0")
    private int retainedRedZoneCount;

    @Schema(description = "Count of districts remaining safely outside Red-Zone status", example = "35")
    private int unchangedNonRedZoneCount;

    // 3. Priority Aggregates
    @Schema(description = "Count of districts classified as IMMEDIATE priority under baseline", example = "1")
    private int baselineImmediatePriorityCount;

    @Schema(description = "Count of districts classified as IMMEDIATE priority under simulation", example = "5")
    private int simulatedImmediatePriorityCount;

    @Schema(description = "Net change in IMMEDIATE priority districts [simulated - baseline]", example = "+4")
    private int netImmediatePriorityChange;

    @Schema(description = "Count of districts whose priority escalated upward", example = "8")
    private int priorityEscalatedCount;

    @Schema(description = "Count of districts whose priority de-escalated downward", example = "0")
    private int priorityDeEscalatedCount;

    @Schema(description = "Count of districts whose priority tier remained unchanged", example = "30")
    private int priorityUnchangedCount;

    // 4. Relocation Aggregates
    @Schema(description = "Total baseline vulnerable population requiring relocation", example = "9500")
    private long totalBaselineVulnerablePopulation;

    @Schema(description = "Total simulated vulnerable population requiring relocation", example = "12350")
    private long totalSimulatedVulnerablePopulation;

    @Schema(description = "Net change in total vulnerable population demand", example = "2850")
    private long netVulnerablePopulationChange;

    @Schema(description = "Total baseline allocated population", example = "8200")
    private long totalBaselineAllocatedPopulation;

    @Schema(description = "Total simulated allocated population", example = "8200")
    private long totalSimulatedAllocatedPopulation;

    @Schema(description = "Net change in allocated population", example = "0")
    private long netAllocatedPopulationChange;

    @Schema(description = "Total baseline unallocated deficit population", example = "1300")
    private long totalBaselineUnallocatedPopulation;

    @Schema(description = "Total simulated unallocated deficit population", example = "4150")
    private long totalSimulatedUnallocatedPopulation;

    @Schema(description = "Net change in unallocated shelter deficit population", example = "2850")
    private long netUnallocatedDeficitChange;

    @Schema(description = "Baseline overall shelter capacity deficit percentage", example = "13.7")
    private double baselineCapacityDeficitPercentage;

    @Schema(description = "Simulated overall shelter capacity deficit percentage", example = "33.6")
    private double simulatedCapacityDeficitPercentage;

    // Detailed District Comparisons
    @Schema(description = "Detailed Before vs After comparison per district")
    private List<DistrictScenarioComparisonDto> districtComparisons = new ArrayList<>();

    @Schema(description = "Timestamp when comparison was evaluated")
    private LocalDateTime comparedAt;

    @Schema(description = "Human-readable summary of Before vs After comparison")
    private String summary;

    public ScenarioComparisonResultDto() {
        this.comparedAt = LocalDateTime.now();
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

    public int totalDistrictsEvaluated() {
        return totalDistrictsEvaluated;
    }

    public int getTotalDistrictsEvaluated() {
        return totalDistrictsEvaluated;
    }

    public void setTotalDistrictsEvaluated(int totalDistrictsEvaluated) {
        this.totalDistrictsEvaluated = totalDistrictsEvaluated;
    }

    public int getDistrictsWithIncreasedRiskCount() {
        return districtsWithIncreasedRiskCount;
    }

    public void setDistrictsWithIncreasedRiskCount(int districtsWithIncreasedRiskCount) {
        this.districtsWithIncreasedRiskCount = districtsWithIncreasedRiskCount;
    }

    public int getDistrictsWithDecreasedRiskCount() {
        return districtsWithDecreasedRiskCount;
    }

    public void setDistrictsWithDecreasedRiskCount(int districtsWithDecreasedRiskCount) {
        this.districtsWithDecreasedRiskCount = districtsWithDecreasedRiskCount;
    }

    public int getDistrictsWithUnchangedRiskCount() {
        return districtsWithUnchangedRiskCount;
    }

    public void setDistrictsWithUnchangedRiskCount(int districtsWithUnchangedRiskCount) {
        this.districtsWithUnchangedRiskCount = districtsWithUnchangedRiskCount;
    }

    public double getAverageRiskDelta100() {
        return averageRiskDelta100;
    }

    public void setAverageRiskDelta100(double averageRiskDelta100) {
        this.averageRiskDelta100 = averageRiskDelta100;
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

    public int getEnteredRedZoneCount() {
        return enteredRedZoneCount;
    }

    public void setEnteredRedZoneCount(int enteredRedZoneCount) {
        this.enteredRedZoneCount = enteredRedZoneCount;
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

    public int getBaselineImmediatePriorityCount() {
        return baselineImmediatePriorityCount;
    }

    public void setBaselineImmediatePriorityCount(int baselineImmediatePriorityCount) {
        this.baselineImmediatePriorityCount = baselineImmediatePriorityCount;
    }

    public int getSimulatedImmediatePriorityCount() {
        return simulatedImmediatePriorityCount;
    }

    public void setSimulatedImmediatePriorityCount(int simulatedImmediatePriorityCount) {
        this.simulatedImmediatePriorityCount = simulatedImmediatePriorityCount;
    }

    public int getNetImmediatePriorityChange() {
        return netImmediatePriorityChange;
    }

    public void setNetImmediatePriorityChange(int netImmediatePriorityChange) {
        this.netImmediatePriorityChange = netImmediatePriorityChange;
    }

    public int getPriorityEscalatedCount() {
        return priorityEscalatedCount;
    }

    public void setPriorityEscalatedCount(int priorityEscalatedCount) {
        this.priorityEscalatedCount = priorityEscalatedCount;
    }

    public int getPriorityDeEscalatedCount() {
        return priorityDeEscalatedCount;
    }

    public void setPriorityDeEscalatedCount(int priorityDeEscalatedCount) {
        this.priorityDeEscalatedCount = priorityDeEscalatedCount;
    }

    public int getPriorityUnchangedCount() {
        return priorityUnchangedCount;
    }

    public void setPriorityUnchangedCount(int priorityUnchangedCount) {
        this.priorityUnchangedCount = priorityUnchangedCount;
    }

    public long getTotalBaselineVulnerablePopulation() {
        return totalBaselineVulnerablePopulation;
    }

    public void setTotalBaselineVulnerablePopulation(long totalBaselineVulnerablePopulation) {
        this.totalBaselineVulnerablePopulation = totalBaselineVulnerablePopulation;
    }

    public long getTotalSimulatedVulnerablePopulation() {
        return totalSimulatedVulnerablePopulation;
    }

    public void setTotalSimulatedVulnerablePopulation(long totalSimulatedVulnerablePopulation) {
        this.totalSimulatedVulnerablePopulation = totalSimulatedVulnerablePopulation;
    }

    public long getNetVulnerablePopulationChange() {
        return netVulnerablePopulationChange;
    }

    public void setNetVulnerablePopulationChange(long netVulnerablePopulationChange) {
        this.netVulnerablePopulationChange = netVulnerablePopulationChange;
    }

    public long getTotalBaselineAllocatedPopulation() {
        return totalBaselineAllocatedPopulation;
    }

    public void setTotalBaselineAllocatedPopulation(long totalBaselineAllocatedPopulation) {
        this.totalBaselineAllocatedPopulation = totalBaselineAllocatedPopulation;
    }

    public long getTotalSimulatedAllocatedPopulation() {
        return totalSimulatedAllocatedPopulation;
    }

    public void setTotalSimulatedAllocatedPopulation(long totalSimulatedAllocatedPopulation) {
        this.totalSimulatedAllocatedPopulation = totalSimulatedAllocatedPopulation;
    }

    public long getNetAllocatedPopulationChange() {
        return netAllocatedPopulationChange;
    }

    public void setNetAllocatedPopulationChange(long netAllocatedPopulationChange) {
        this.netAllocatedPopulationChange = netAllocatedPopulationChange;
    }

    public long getTotalBaselineUnallocatedPopulation() {
        return totalBaselineUnallocatedPopulation;
    }

    public void setTotalBaselineUnallocatedPopulation(long totalBaselineUnallocatedPopulation) {
        this.totalBaselineUnallocatedPopulation = totalBaselineUnallocatedPopulation;
    }

    public long getTotalSimulatedUnallocatedPopulation() {
        return totalSimulatedUnallocatedPopulation;
    }

    public void setTotalSimulatedUnallocatedPopulation(long totalSimulatedUnallocatedPopulation) {
        this.totalSimulatedUnallocatedPopulation = totalSimulatedUnallocatedPopulation;
    }

    public long getNetUnallocatedDeficitChange() {
        return netUnallocatedDeficitChange;
    }

    public void setNetUnallocatedDeficitChange(long netUnallocatedDeficitChange) {
        this.netUnallocatedDeficitChange = netUnallocatedDeficitChange;
    }

    public double getBaselineCapacityDeficitPercentage() {
        return baselineCapacityDeficitPercentage;
    }

    public void setBaselineCapacityDeficitPercentage(double baselineCapacityDeficitPercentage) {
        this.baselineCapacityDeficitPercentage = baselineCapacityDeficitPercentage;
    }

    public double getSimulatedCapacityDeficitPercentage() {
        return simulatedCapacityDeficitPercentage;
    }

    public void setSimulatedCapacityDeficitPercentage(double simulatedCapacityDeficitPercentage) {
        this.simulatedCapacityDeficitPercentage = simulatedCapacityDeficitPercentage;
    }

    public List<DistrictScenarioComparisonDto> getDistrictComparisons() {
        return districtComparisons;
    }

    public void setDistrictComparisons(List<DistrictScenarioComparisonDto> districtComparisons) {
        this.districtComparisons = districtComparisons;
    }

    public LocalDateTime getComparedAt() {
        return comparedAt;
    }

    public void setComparedAt(LocalDateTime comparedAt) {
        this.comparedAt = comparedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
