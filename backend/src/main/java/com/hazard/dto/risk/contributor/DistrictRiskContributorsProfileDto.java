package com.hazard.dto.risk.contributor;

import com.hazard.domain.risk.RiskTier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Master response DTO for Stage 4.9 — Risk Contributors.
 * Synthesizes final score, ranked contributors, hierarchical tree, dynamic explanation,
 * configuration version, and mathematical verification metadata.
 */
public class DistrictRiskContributorsProfileDto {

    private String geographicUnit;
    private String geographicId;
    private Integer districtId;
    private String districtName;
    private String state;

    private Double riskScore;        // Score in [0.0000, 1.0000]
    private Double riskScore100;     // Human score in [0.0, 100.0]
    private RiskTier riskTier;

    private String configurationId;
    private String configurationVersion;
    private String configurationName;
    private String calculationVersion;

    private String dataQualityStatus;
    private Double dataCompletenessPercentage;

    private List<DetailedRiskContributorDto> topContributors = new ArrayList<>();
    private List<DetailedRiskContributorDto> allContributors = new ArrayList<>();
    private ContributorTreeNodeDto contributorTree;
    private RiskExplanationDto explanation;

    private Map<String, Object> mathematicalCheck = new LinkedHashMap<>();
    private LocalDateTime timestamp;

    public DistrictRiskContributorsProfileDto() {
        this.geographicUnit = "DISTRICT";
        this.state = "Bihar";
        this.timestamp = LocalDateTime.now();
    }

    public String getGeographicUnit() { return geographicUnit; }
    public void setGeographicUnit(String geographicUnit) { this.geographicUnit = geographicUnit; }

    public String getGeographicId() { return geographicId; }
    public void setGeographicId(String geographicId) { this.geographicId = geographicId; }

    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public Double getRiskScore100() { return riskScore100; }
    public void setRiskScore100(Double riskScore100) { this.riskScore100 = riskScore100; }

    public RiskTier getRiskTier() { return riskTier; }
    public void setRiskTier(RiskTier riskTier) { this.riskTier = riskTier; }

    public String getConfigurationId() { return configurationId; }
    public void setConfigurationId(String configurationId) { this.configurationId = configurationId; }

    public String getConfigurationVersion() { return configurationVersion; }
    public void setConfigurationVersion(String configurationVersion) { this.configurationVersion = configurationVersion; }

    public String getConfigurationName() { return configurationName; }
    public void setConfigurationName(String configurationName) { this.configurationName = configurationName; }

    public String getCalculationVersion() { return calculationVersion; }
    public void setCalculationVersion(String calculationVersion) { this.calculationVersion = calculationVersion; }

    public String getDataQualityStatus() { return dataQualityStatus; }
    public void setDataQualityStatus(String dataQualityStatus) { this.dataQualityStatus = dataQualityStatus; }

    public Double getDataCompletenessPercentage() { return dataCompletenessPercentage; }
    public void setDataCompletenessPercentage(Double dataCompletenessPercentage) { this.dataCompletenessPercentage = dataCompletenessPercentage; }

    public List<DetailedRiskContributorDto> getTopContributors() { return topContributors; }
    public void setTopContributors(List<DetailedRiskContributorDto> topContributors) { this.topContributors = topContributors != null ? topContributors : new ArrayList<>(); }

    public List<DetailedRiskContributorDto> getAllContributors() { return allContributors; }
    public void setAllContributors(List<DetailedRiskContributorDto> allContributors) { this.allContributors = allContributors != null ? allContributors : new ArrayList<>(); }

    public ContributorTreeNodeDto getContributorTree() { return contributorTree; }
    public void setContributorTree(ContributorTreeNodeDto contributorTree) { this.contributorTree = contributorTree; }

    public RiskExplanationDto getExplanation() { return explanation; }
    public void setExplanation(RiskExplanationDto explanation) { this.explanation = explanation; }

    public Map<String, Object> getMathematicalCheck() { return mathematicalCheck; }
    public void setMathematicalCheck(Map<String, Object> mathematicalCheck) { this.mathematicalCheck = mathematicalCheck != null ? mathematicalCheck : new LinkedHashMap<>(); }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
