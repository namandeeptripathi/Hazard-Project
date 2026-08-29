package com.hazard.dto.scenario;

import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.risk.ZoneLevel;
import com.hazard.domain.scenario.RedZoneTransitionType;
import com.hazard.dto.risk.RedZoneDto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Stage 9C — District-level Red-Zone simulation and transition outcome DTO.
 */
@Schema(description = "District-level Red-Zone classification and transition outcome under simulation")
public class DistrictRedZoneSimulationDto {

    @Schema(description = "District database ID", example = "10")
    private Integer districtId;

    @Schema(description = "District administrative name", example = "Sitamarhi")
    private String districtName;

    @Schema(description = "District GID2 code", example = "IND.4.34_1")
    private String gid2;

    @Schema(description = "State name", example = "Bihar")
    private String state;

    // Baseline State
    @Schema(description = "Baseline risk score [0.0000, 1.0000]", example = "0.5770")
    private Double baselineRiskScore;

    @Schema(description = "Baseline risk score on 0-100 scale", example = "57.7")
    private Double baselineRiskScore100;

    @Schema(description = "Baseline risk tier classification", example = "HIGH")
    private RiskTier baselineRiskTier;

    @Schema(description = "Baseline zone level classification", example = "HIGH")
    private ZoneLevel baselineZoneLevel;

    @Schema(description = "Whether district is classified as Red Zone in baseline", example = "false")
    private boolean baselineRedZone;

    // Simulated State
    @Schema(description = "Simulated risk score [0.0000, 1.0000]", example = "0.6130")
    private Double simulatedRiskScore;

    @Schema(description = "Simulated risk score on 0-100 scale", example = "61.3")
    private Double simulatedRiskScore100;

    @Schema(description = "Simulated risk tier classification", example = "VERY_HIGH")
    private RiskTier simulatedRiskTier;

    @Schema(description = "Simulated zone level classification", example = "CRITICAL")
    private ZoneLevel simulatedZoneLevel;

    @Schema(description = "Whether district is classified as Red Zone under simulation", example = "true")
    private boolean simulatedRedZone;

    // Deltas and Transitions
    @Schema(description = "Risk score delta [simulated - baseline]", example = "0.0360")
    private Double deltaRiskScore;

    @Schema(description = "Risk score delta on 0-100 scale", example = "3.6")
    private Double deltaRiskScore100;

    @Schema(description = "Red-Zone status transition type", example = "ENTERED_RED_ZONE")
    private RedZoneTransitionType transitionType;

    @Schema(description = "Human-readable transition summary", example = "Shifted from Non-Red Zone (57.7/100, HIGH) to Red Zone (61.3/100, VERY_HIGH)")
    private String transitionDescription;

    // Full classification wrappers
    @Schema(description = "Canonical baseline RedZoneDto")
    private RedZoneDto baselineRedZoneDto;

    @Schema(description = "Canonical simulated RedZoneDto")
    private RedZoneDto simulatedRedZoneDto;

    public DistrictRedZoneSimulationDto() {
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

    public ZoneLevel getBaselineZoneLevel() {
        return baselineZoneLevel;
    }

    public void setBaselineZoneLevel(ZoneLevel baselineZoneLevel) {
        this.baselineZoneLevel = baselineZoneLevel;
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

    public ZoneLevel getSimulatedZoneLevel() {
        return simulatedZoneLevel;
    }

    public void setSimulatedZoneLevel(ZoneLevel simulatedZoneLevel) {
        this.simulatedZoneLevel = simulatedZoneLevel;
    }

    public boolean isSimulatedRedZone() {
        return simulatedRedZone;
    }

    public void setSimulatedRedZone(boolean simulatedRedZone) {
        this.simulatedRedZone = simulatedRedZone;
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

    public RedZoneTransitionType getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(RedZoneTransitionType transitionType) {
        this.transitionType = transitionType;
    }

    public String getTransitionDescription() {
        return transitionDescription;
    }

    public void setTransitionDescription(String transitionDescription) {
        this.transitionDescription = transitionDescription;
    }

    public RedZoneDto getBaselineRedZoneDto() {
        return baselineRedZoneDto;
    }

    public void setBaselineRedZoneDto(RedZoneDto baselineRedZoneDto) {
        this.baselineRedZoneDto = baselineRedZoneDto;
    }

    public RedZoneDto getSimulatedRedZoneDto() {
        return simulatedRedZoneDto;
    }

    public void setSimulatedRedZoneDto(RedZoneDto simulatedRedZoneDto) {
        this.simulatedRedZoneDto = simulatedRedZoneDto;
    }
}
