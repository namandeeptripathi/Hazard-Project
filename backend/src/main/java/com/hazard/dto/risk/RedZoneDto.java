package com.hazard.dto.risk;

import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.risk.ZoneLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 5.1 — Red-Zone classification DTO for a district.
 * All risk data is sourced from the existing DistrictRiskScoreDto;
 * this DTO adds zone-level classification and red-zone identification.
 */
public class RedZoneDto {

    private String districtName;
    private Integer districtId;
    private String gid2;
    private String state;

    private Double riskScore;
    private Double riskScore100;
    private RiskTier riskTier;

    private ZoneLevel zoneLevel;
    private boolean redZone;

    private List<RiskContributorDto> topContributors = new ArrayList<>();
    private String explanation;
    private LocalDateTime timestamp;

    public RedZoneDto() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Factory method: builds a RedZoneDto from an existing DistrictRiskScoreDto.
     * No risk recalculation — purely a classification wrapper.
     */
    public static RedZoneDto fromDistrictRiskScore(DistrictRiskScoreDto risk) {
        if (risk == null) {
            return null;
        }

        RedZoneDto dto = new RedZoneDto();
        dto.setDistrictName(risk.getDistrictName());
        dto.setDistrictId(risk.getDistrictId());
        dto.setGid2(risk.getGid2());
        dto.setState(risk.getState());

        dto.setRiskScore(risk.getRiskScore());
        dto.setRiskScore100(risk.getRiskScore100());
        dto.setRiskTier(risk.getRiskTier());

        ZoneLevel zone = ZoneLevel.fromRiskTier(risk.getRiskTier());
        dto.setZoneLevel(zone);
        dto.setRedZone(zone.isRedZone());

        dto.setTopContributors(risk.getTopContributors());
        dto.setExplanation(risk.getExplanation());

        return dto;
    }

    // Getters and Setters

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
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

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Double getRiskScore100() {
        return riskScore100;
    }

    public void setRiskScore100(Double riskScore100) {
        this.riskScore100 = riskScore100;
    }

    public RiskTier getRiskTier() {
        return riskTier;
    }

    public void setRiskTier(RiskTier riskTier) {
        this.riskTier = riskTier;
    }

    public ZoneLevel getZoneLevel() {
        return zoneLevel;
    }

    public void setZoneLevel(ZoneLevel zoneLevel) {
        this.zoneLevel = zoneLevel;
    }

    public boolean isRedZone() {
        return redZone;
    }

    public void setRedZone(boolean redZone) {
        this.redZone = redZone;
    }

    public List<RiskContributorDto> getTopContributors() {
        return topContributors;
    }

    public void setTopContributors(List<RiskContributorDto> topContributors) {
        this.topContributors = topContributors != null ? topContributors : new ArrayList<>();
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
