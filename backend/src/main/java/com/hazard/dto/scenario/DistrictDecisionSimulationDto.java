package com.hazard.dto.scenario;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.scenario.RedZoneTransitionType;
import com.hazard.dto.relocation.RelocationPlanDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Stage 9D — District-level Decision (Priority & Relocation) Simulation DTO.
 */
@Schema(description = "District-level simulated priority escalation and relocation outcome")
public class DistrictDecisionSimulationDto {

    @Schema(description = "District database ID", example = "10")
    private Integer districtId;

    @Schema(description = "District administrative name", example = "Sitamarhi")
    private String districtName;

    @Schema(description = "District GID2 code", example = "IND.4.34_1")
    private String gid2;

    @Schema(description = "State name", example = "Bihar")
    private String state;

    // --- Risk & Red-Zone Shift ---
    @Schema(description = "Baseline risk score [0.0000, 1.0000]", example = "0.5770")
    private Double baselineRiskScore;

    @Schema(description = "Baseline risk score on 0-100 scale", example = "57.7")
    private Double baselineRiskScore100;

    @Schema(description = "Baseline risk tier classification", example = "HIGH")
    private RiskTier baselineRiskTier;

    @Schema(description = "Baseline Red-Zone boolean", example = "false")
    private boolean baselineRedZone;

    @Schema(description = "Simulated risk score [0.0000, 1.0000]", example = "0.6230")
    private Double simulatedRiskScore;

    @Schema(description = "Simulated risk score on 0-100 scale", example = "62.3")
    private Double simulatedRiskScore100;

    @Schema(description = "Simulated risk tier classification", example = "VERY_HIGH")
    private RiskTier simulatedRiskTier;

    @Schema(description = "Simulated Red-Zone boolean", example = "true")
    private boolean simulatedRedZone;

    @Schema(description = "Red-Zone status transition type", example = "ENTERED_RED_ZONE")
    private RedZoneTransitionType redZoneTransitionType;

    // --- Priority Shift ---
    @Schema(description = "Baseline composite priority score [0.0000, 1.0000]", example = "0.4520")
    private Double baselinePriorityScore;

    @Schema(description = "Baseline priority level tier", example = "SHORT_TERM")
    private PriorityLevel baselinePriorityLevel;

    @Schema(description = "Simulated composite priority score [0.0000, 1.0000]", example = "0.7810")
    private Double simulatedPriorityScore;

    @Schema(description = "Simulated priority level tier", example = "IMMEDIATE")
    private PriorityLevel simulatedPriorityLevel;

    @Schema(description = "Priority score delta [simulated - baseline]", example = "0.3290")
    private Double deltaPriorityScore;

    @Schema(description = "Priority shift direction (INCREASED, DECREASED, UNCHANGED)", example = "INCREASED")
    private String priorityShiftDirection;

    @Schema(description = "Detailed Stage 7A priority result")
    private RelocationPriorityResultDto simulatedPriorityResult;

    // --- Relocation Planning Outcome ---
    @Schema(description = "Vulnerable population requiring relocation", example = "350")
    private Long vulnerablePopulation;

    @Schema(description = "Baseline allocated population count", example = "250")
    private Long baselineAllocatedPopulation;

    @Schema(description = "Baseline unallocated deficit population count", example = "0")
    private Long baselineUnallocatedPopulation;

    @Schema(description = "Baseline relocation overall status", example = "FULLY_ALLOCATED")
    private RelocationStatus baselineRelocationStatus;

    @Schema(description = "Simulated allocated population count", example = "250")
    private Long simulatedAllocatedPopulation;

    @Schema(description = "Simulated unallocated deficit population count", example = "100")
    private Long simulatedUnallocatedPopulation;

    @Schema(description = "Simulated relocation overall status", example = "PARTIALLY_ALLOCATED")
    private RelocationStatus simulatedRelocationStatus;

    @Schema(description = "Count of feasible candidate safe sites discovered", example = "5")
    private Integer feasibleCandidateSitesCount;

    @Schema(description = "Detailed Stage 6 relocation plan")
    private RelocationPlanDto simulatedRelocationPlan;

    @Schema(description = "Human-readable summary of the decision shift")
    private String summary;

    public DistrictDecisionSimulationDto() {
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getGid2() {
        return gid2;
    }

    public void setGid2(String gid2) {
        this.gid2 = gid2;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getBaselineRiskScore() {
        return baselineRiskScore;
    }

    public void setBaselineRiskScore(Double baselineRiskScore) {
        this.baselineRiskScore = baselineRiskScore;
    }

    public Double getBaselineRiskScore100() {
        return baselineRiskScore100;
    }

    public void setBaselineRiskScore100(Double baselineRiskScore100) {
        this.baselineRiskScore100 = baselineRiskScore100;
    }

    public RiskTier getBaselineRiskTier() {
        return baselineRiskTier;
    }

    public void setBaselineRiskTier(RiskTier baselineRiskTier) {
        this.baselineRiskTier = baselineRiskTier;
    }

    public boolean isBaselineRedZone() {
        return baselineRedZone;
    }

    public void setBaselineRedZone(boolean baselineRedZone) {
        this.baselineRedZone = baselineRedZone;
    }

    public Double getSimulatedRiskScore() {
        return simulatedRiskScore;
    }

    public void setSimulatedRiskScore(Double simulatedRiskScore) {
        this.simulatedRiskScore = simulatedRiskScore;
    }

    public Double getSimulatedRiskScore100() {
        return simulatedRiskScore100;
    }

    public void setSimulatedRiskScore100(Double simulatedRiskScore100) {
        this.simulatedRiskScore100 = simulatedRiskScore100;
    }

    public RiskTier getSimulatedRiskTier() {
        return simulatedRiskTier;
    }

    public void setSimulatedRiskTier(RiskTier simulatedRiskTier) {
        this.simulatedRiskTier = simulatedRiskTier;
    }

    public boolean isSimulatedRedZone() {
        return simulatedRedZone;
    }

    public void setSimulatedRedZone(boolean simulatedRedZone) {
        this.simulatedRedZone = simulatedRedZone;
    }

    public RedZoneTransitionType getRedZoneTransitionType() {
        return redZoneTransitionType;
    }

    public void setRedZoneTransitionType(RedZoneTransitionType redZoneTransitionType) {
        this.redZoneTransitionType = redZoneTransitionType;
    }

    public Double getBaselinePriorityScore() {
        return baselinePriorityScore;
    }

    public void setBaselinePriorityScore(Double baselinePriorityScore) {
        this.baselinePriorityScore = baselinePriorityScore;
    }

    public PriorityLevel getBaselinePriorityLevel() {
        return baselinePriorityLevel;
    }

    public void setBaselinePriorityLevel(PriorityLevel baselinePriorityLevel) {
        this.baselinePriorityLevel = baselinePriorityLevel;
    }

    public Double getSimulatedPriorityScore() {
        return simulatedPriorityScore;
    }

    public void setSimulatedPriorityScore(Double simulatedPriorityScore) {
        this.simulatedPriorityScore = simulatedPriorityScore;
    }

    public PriorityLevel getSimulatedPriorityLevel() {
        return simulatedPriorityLevel;
    }

    public void setSimulatedPriorityLevel(PriorityLevel simulatedPriorityLevel) {
        this.simulatedPriorityLevel = simulatedPriorityLevel;
    }

    public Double getDeltaPriorityScore() {
        return deltaPriorityScore;
    }

    public void setDeltaPriorityScore(Double deltaPriorityScore) {
        this.deltaPriorityScore = deltaPriorityScore;
    }

    public String getPriorityShiftDirection() {
        return priorityShiftDirection;
    }

    public void setPriorityShiftDirection(String priorityShiftDirection) {
        this.priorityShiftDirection = priorityShiftDirection;
    }

    public RelocationPriorityResultDto getSimulatedPriorityResult() {
        return simulatedPriorityResult;
    }

    public void setSimulatedPriorityResult(RelocationPriorityResultDto simulatedPriorityResult) {
        this.simulatedPriorityResult = simulatedPriorityResult;
    }

    public Long getVulnerablePopulation() {
        return vulnerablePopulation;
    }

    public void setVulnerablePopulation(Long vulnerablePopulation) {
        this.vulnerablePopulation = vulnerablePopulation;
    }

    public Long getBaselineAllocatedPopulation() {
        return baselineAllocatedPopulation;
    }

    public void setBaselineAllocatedPopulation(Long baselineAllocatedPopulation) {
        this.baselineAllocatedPopulation = baselineAllocatedPopulation;
    }

    public Long getBaselineUnallocatedPopulation() {
        return baselineUnallocatedPopulation;
    }

    public void setBaselineUnallocatedPopulation(Long baselineUnallocatedPopulation) {
        this.baselineUnallocatedPopulation = baselineUnallocatedPopulation;
    }

    public RelocationStatus getBaselineRelocationStatus() {
        return baselineRelocationStatus;
    }

    public void setBaselineRelocationStatus(RelocationStatus baselineRelocationStatus) {
        this.baselineRelocationStatus = baselineRelocationStatus;
    }

    public Long getSimulatedAllocatedPopulation() {
        return simulatedAllocatedPopulation;
    }

    public void setSimulatedAllocatedPopulation(Long simulatedAllocatedPopulation) {
        this.simulatedAllocatedPopulation = simulatedAllocatedPopulation;
    }

    public Long getSimulatedUnallocatedPopulation() {
        return simulatedUnallocatedPopulation;
    }

    public void setSimulatedUnallocatedPopulation(Long simulatedUnallocatedPopulation) {
        this.simulatedUnallocatedPopulation = simulatedUnallocatedPopulation;
    }

    public RelocationStatus getSimulatedRelocationStatus() {
        return simulatedRelocationStatus;
    }

    public void setSimulatedRelocationStatus(RelocationStatus simulatedRelocationStatus) {
        this.simulatedRelocationStatus = simulatedRelocationStatus;
    }

    public Integer getFeasibleCandidateSitesCount() {
        return feasibleCandidateSitesCount;
    }

    public void setFeasibleCandidateSitesCount(Integer feasibleCandidateSitesCount) {
        this.feasibleCandidateSitesCount = feasibleCandidateSitesCount;
    }

    public RelocationPlanDto getSimulatedRelocationPlan() {
        return simulatedRelocationPlan;
    }

    public void setSimulatedRelocationPlan(RelocationPlanDto simulatedRelocationPlan) {
        this.simulatedRelocationPlan = simulatedRelocationPlan;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
