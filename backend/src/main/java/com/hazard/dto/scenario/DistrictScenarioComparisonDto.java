package com.hazard.dto.scenario;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.scenario.RedZoneTransitionType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Stage 9E — District-level Before vs After Scenario Comparison DTO.
 *
 * Encapsulates the multi-dimensional comparison for a single district:
 * 1. Risk change (scores, tiers, deltas)
 * 2. Red-Zone transition (entered, left, retained, unchanged)
 * 3. Priority shift (scores, tiers, escalation)
 * 4. Relocation impact (demand, allocations, deficits)
 */
@Schema(description = "District-level Before vs After scenario comparison outcome")
public class DistrictScenarioComparisonDto {

    @Schema(description = "District database ID", example = "10")
    private Integer districtId;

    @Schema(description = "District administrative name", example = "Sitamarhi")
    private String districtName;

    @Schema(description = "District GID2 code", example = "IND.4.34_1")
    private String gid2;

    @Schema(description = "State name", example = "Bihar")
    private String state;

    // =========================================================================
    // 1. RISK COMPARISON
    // =========================================================================

    @Schema(description = "Baseline risk score [0.0000, 1.0000]", example = "0.2360")
    private Double baselineRiskScore;

    @Schema(description = "Baseline risk score on 0-100 scale", example = "23.6")
    private Double baselineRiskScore100;

    @Schema(description = "Baseline risk tier classification", example = "LOW")
    private RiskTier baselineRiskTier;

    @Schema(description = "Simulated risk score [0.0000, 1.0000]", example = "0.3470")
    private Double simulatedRiskScore;

    @Schema(description = "Simulated risk score on 0-100 scale", example = "34.7")
    private Double simulatedRiskScore100;

    @Schema(description = "Simulated risk tier classification", example = "MODERATE")
    private RiskTier simulatedRiskTier;

    @Schema(description = "Risk score delta [simulated - baseline]", example = "0.1110")
    private Double deltaRiskScore;

    @Schema(description = "Risk score delta on 0-100 scale", example = "11.1")
    private Double deltaRiskScore100;

    @Schema(description = "Risk change direction (INCREASED, DECREASED, UNCHANGED)", example = "INCREASED")
    private String riskDirection;

    // =========================================================================
    // 2. RED-ZONE COMPARISON
    // =========================================================================

    @Schema(description = "Baseline Red-Zone classification boolean", example = "false")
    private boolean baselineRedZone;

    @Schema(description = "Simulated Red-Zone classification boolean", example = "false")
    private boolean simulatedRedZone;

    @Schema(description = "Red-Zone status transition classification", example = "UNCHANGED_NON_RED_ZONE")
    private RedZoneTransitionType redZoneTransitionType;

    @Schema(description = "Indicates whether Red-Zone status changed (entered or left)", example = "false")
    private boolean redZoneChanged;

    // =========================================================================
    // 3. PRIORITY COMPARISON
    // =========================================================================

    @Schema(description = "Baseline composite priority score [0.0000, 1.0000]", example = "0.3540")
    private Double baselinePriorityScore;

    @Schema(description = "Baseline priority level tier", example = "MONITORING")
    private PriorityLevel baselinePriorityLevel;

    @Schema(description = "Simulated composite priority score [0.0000, 1.0000]", example = "0.5820")
    private Double simulatedPriorityScore;

    @Schema(description = "Simulated priority level tier", example = "SHORT_TERM")
    private PriorityLevel simulatedPriorityLevel;

    @Schema(description = "Priority score delta [simulated - baseline]", example = "0.2280")
    private Double deltaPriorityScore;

    @Schema(description = "Priority shift direction (INCREASED, DECREASED, UNCHANGED)", example = "INCREASED")
    private String priorityShiftDirection;

    @Schema(description = "Indicates whether priority escalated upward", example = "true")
    private boolean priorityEscalated;

    // =========================================================================
    // 4. RELOCATION COMPARISON
    // =========================================================================

    @Schema(description = "Baseline vulnerable population requiring shelter", example = "250")
    private Long baselineVulnerablePopulation;

    @Schema(description = "Simulated vulnerable population requiring shelter", example = "350")
    private Long simulatedVulnerablePopulation;

    @Schema(description = "Net change in vulnerable population demand [simulated - baseline]", example = "100")
    private Long deltaVulnerablePopulation;

    @Schema(description = "Baseline allocated population count", example = "250")
    private Long baselineAllocatedPopulation;

    @Schema(description = "Simulated allocated population count", example = "250")
    private Long simulatedAllocatedPopulation;

    @Schema(description = "Net change in allocated population count", example = "0")
    private Long deltaAllocatedPopulation;

    @Schema(description = "Baseline unallocated deficit population count", example = "0")
    private Long baselineUnallocatedPopulation;

    @Schema(description = "Simulated unallocated deficit population count", example = "100")
    private Long simulatedUnallocatedPopulation;

    @Schema(description = "Net change in unallocated deficit count [simulated - baseline]", example = "100")
    private Long deltaUnallocatedPopulation;

    @Schema(description = "Baseline relocation planning overall status", example = "FULLY_ALLOCATED")
    private RelocationStatus baselineRelocationStatus;

    @Schema(description = "Simulated relocation planning overall status", example = "PARTIALLY_ALLOCATED")
    private RelocationStatus simulatedRelocationStatus;

    @Schema(description = "Relocation demand change direction (INCREASED, DECREASED, UNCHANGED)", example = "INCREASED")
    private String relocationDemandDirection;

    @Schema(description = "Concise human-readable comparison summary")
    private String summary;

    public DistrictScenarioComparisonDto() {
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

    public boolean isBaselineRedZone() {
        return baselineRedZone;
    }

    public void setBaselineRedZone(boolean baselineRedZone) {
        this.baselineRedZone = baselineRedZone;
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

    public boolean isRedZoneChanged() {
        return redZoneChanged;
    }

    public void setRedZoneChanged(boolean redZoneChanged) {
        this.redZoneChanged = redZoneChanged;
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

    public boolean isPriorityEscalated() {
        return priorityEscalated;
    }

    public void setPriorityEscalated(boolean priorityEscalated) {
        this.priorityEscalated = priorityEscalated;
    }

    public Long getBaselineVulnerablePopulation() {
        return baselineVulnerablePopulation;
    }

    public void setBaselineVulnerablePopulation(Long baselineVulnerablePopulation) {
        this.baselineVulnerablePopulation = baselineVulnerablePopulation;
    }

    public Long getSimulatedVulnerablePopulation() {
        return simulatedVulnerablePopulation;
    }

    public void setSimulatedVulnerablePopulation(Long simulatedVulnerablePopulation) {
        this.simulatedVulnerablePopulation = simulatedVulnerablePopulation;
    }

    public Long getDeltaVulnerablePopulation() {
        return deltaVulnerablePopulation;
    }

    public void setDeltaVulnerablePopulation(Long deltaVulnerablePopulation) {
        this.deltaVulnerablePopulation = deltaVulnerablePopulation;
    }

    public Long getBaselineAllocatedPopulation() {
        return baselineAllocatedPopulation;
    }

    public void setBaselineAllocatedPopulation(Long baselineAllocatedPopulation) {
        this.baselineAllocatedPopulation = baselineAllocatedPopulation;
    }

    public Long getSimulatedAllocatedPopulation() {
        return simulatedAllocatedPopulation;
    }

    public void setSimulatedAllocatedPopulation(Long simulatedAllocatedPopulation) {
        this.simulatedAllocatedPopulation = simulatedAllocatedPopulation;
    }

    public Long getDeltaAllocatedPopulation() {
        return deltaAllocatedPopulation;
    }

    public void setDeltaAllocatedPopulation(Long deltaAllocatedPopulation) {
        this.deltaAllocatedPopulation = deltaAllocatedPopulation;
    }

    public Long getBaselineUnallocatedPopulation() {
        return baselineUnallocatedPopulation;
    }

    public void setBaselineUnallocatedPopulation(Long baselineUnallocatedPopulation) {
        this.baselineUnallocatedPopulation = baselineUnallocatedPopulation;
    }

    public Long getSimulatedUnallocatedPopulation() {
        return simulatedUnallocatedPopulation;
    }

    public void setSimulatedUnallocatedPopulation(Long simulatedUnallocatedPopulation) {
        this.simulatedUnallocatedPopulation = simulatedUnallocatedPopulation;
    }

    public Long getDeltaUnallocatedPopulation() {
        return deltaUnallocatedPopulation;
    }

    public void setDeltaUnallocatedPopulation(Long deltaUnallocatedPopulation) {
        this.deltaUnallocatedPopulation = deltaUnallocatedPopulation;
    }

    public RelocationStatus getBaselineRelocationStatus() {
        return baselineRelocationStatus;
    }

    public void setBaselineRelocationStatus(RelocationStatus baselineRelocationStatus) {
        this.baselineRelocationStatus = baselineRelocationStatus;
    }

    public RelocationStatus getSimulatedRelocationStatus() {
        return simulatedRelocationStatus;
    }

    public void setSimulatedRelocationStatus(RelocationStatus simulatedRelocationStatus) {
        this.simulatedRelocationStatus = simulatedRelocationStatus;
    }

    public String getRelocationDemandDirection() {
        return relocationDemandDirection;
    }

    public void setRelocationDemandDirection(String relocationDemandDirection) {
        this.relocationDemandDirection = relocationDemandDirection;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
