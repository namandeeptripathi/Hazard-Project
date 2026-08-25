package com.hazard.dto.risk;

import com.hazard.domain.risk.RiskTier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Master Disaster Risk Score DTO for a geographic administrative district,
 * synthesizing Hazard, Exposure, Vulnerability, and Historical Evidence.
 */
public class DistrictRiskScoreDto {

    private String geographicUnit;
    private String geographicId;
    private Integer districtId;
    private String districtName;
    private String state;
    private String gid2;

    private Double riskScore;        // Normalized score in [0.0000, 1.0000]
    private Double riskScore100;     // Human display score in [0.0, 100.0]
    private RiskTier riskTier;

    private Map<String, RiskComponentDetailDto> components = new LinkedHashMap<>();
    private ExposureSubBreakdownDto exposureSubBreakdown;
    private RiskDataQualityDto dataQuality;
    private List<RiskContributorDto> topContributors = new ArrayList<>();

    private String configurationId;
    private String configurationVersion;
    private String configurationName;

    private String calculationVersion;
    private LocalDateTime timestamp;
    private String explanation;

    public DistrictRiskScoreDto() {
        this.geographicUnit = "DISTRICT";
        this.state = "Bihar";
        this.configurationId = "risk-v1";
        this.configurationVersion = "1.0";
        this.configurationName = "Default Baseline Risk Model";
        this.calculationVersion = "v1.0";
        this.timestamp = LocalDateTime.now();
    }

    public String getGeographicUnit() {
        return geographicUnit;
    }

    public void setGeographicUnit(String geographicUnit) {
        this.geographicUnit = geographicUnit;
    }

    public String getGeographicId() {
        return geographicId;
    }

    public void setGeographicId(String geographicId) {
        this.geographicId = geographicId;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGid2() {
        return gid2;
    }

    public void setGid2(String gid2) {
        this.gid2 = gid2;
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

    public Map<String, RiskComponentDetailDto> getComponents() {
        return components;
    }

    public void setComponents(Map<String, RiskComponentDetailDto> components) {
        this.components = components != null ? components : new LinkedHashMap<>();
    }

    public ExposureSubBreakdownDto getExposureSubBreakdown() {
        return exposureSubBreakdown;
    }

    public void setExposureSubBreakdown(ExposureSubBreakdownDto exposureSubBreakdown) {
        this.exposureSubBreakdown = exposureSubBreakdown;
    }

    public RiskDataQualityDto getDataQuality() {
        return dataQuality;
    }

    public void setDataQuality(RiskDataQualityDto dataQuality) {
        this.dataQuality = dataQuality;
    }

    public List<RiskContributorDto> getTopContributors() {
        return topContributors;
    }

    public void setTopContributors(List<RiskContributorDto> topContributors) {
        this.topContributors = topContributors != null ? topContributors : new ArrayList<>();
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public String getConfigurationVersion() {
        return configurationVersion;
    }

    public void setConfigurationVersion(String configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getCalculationVersion() {
        return calculationVersion;
    }

    public void setCalculationVersion(String calculationVersion) {
        this.calculationVersion = calculationVersion;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
