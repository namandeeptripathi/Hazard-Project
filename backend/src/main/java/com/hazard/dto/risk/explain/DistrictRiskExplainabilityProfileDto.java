package com.hazard.dto.risk.explain;

import com.hazard.domain.risk.RiskTier;
import com.hazard.dto.risk.contributor.DetailedRiskContributorDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Master Explainability Profile for Stage 4.10 — Explainable Risk.
 * Unifies final scores, multi-level human summaries, empirical evidence items with provenance,
 * calculation traces, sensitivity analysis, data completeness, and model limitations.
 */
public class DistrictRiskExplainabilityProfileDto {

    private String geographicUnit;
    private String geographicId;
    private Integer districtId;
    private String districtName;
    private String state;

    private Double riskScore;            // Normalized in [0.0000, 1.0000]
    private Double riskScore100;         // Human score in [0.0, 100.0]
    private RiskTier riskTier;

    private String configurationId;
    private String configurationVersion;
    private String configurationName;
    private String calculationVersion;
    private String explanationVersion = "explain-v1";

    private ExplanationSummaryDto summary;
    private List<DetailedRiskContributorDto> primaryDrivers = new ArrayList<>();
    private List<DetailedRiskContributorDto> secondaryFactors = new ArrayList<>();
    private List<ExplainableEvidenceItemDto> evidenceItems = new ArrayList<>();
    private CalculationTraceDto calculationTrace;
    private List<ComponentSensitivityDto> sensitivityAnalysis = new ArrayList<>();
    private DataQualityExplanationDto dataQuality;
    private List<String> modelLimitations = new ArrayList<>();

    private LocalDateTime generatedTimestamp;

    public DistrictRiskExplainabilityProfileDto() {
        this.geographicUnit = "DISTRICT";
        this.state = "Bihar";
        this.explanationVersion = "explain-v1";
        this.generatedTimestamp = LocalDateTime.now();
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

    public String getExplanationVersion() { return explanationVersion; }
    public void setExplanationVersion(String explanationVersion) { this.explanationVersion = explanationVersion; }

    public ExplanationSummaryDto getSummary() { return summary; }
    public void setSummary(ExplanationSummaryDto summary) { this.summary = summary; }

    public List<DetailedRiskContributorDto> getPrimaryDrivers() { return primaryDrivers; }
    public void setPrimaryDrivers(List<DetailedRiskContributorDto> primaryDrivers) { this.primaryDrivers = primaryDrivers != null ? primaryDrivers : new ArrayList<>(); }

    public List<DetailedRiskContributorDto> getSecondaryFactors() { return secondaryFactors; }
    public void setSecondaryFactors(List<DetailedRiskContributorDto> secondaryFactors) { this.secondaryFactors = secondaryFactors != null ? secondaryFactors : new ArrayList<>(); }

    public List<ExplainableEvidenceItemDto> getEvidenceItems() { return evidenceItems; }
    public void setEvidenceItems(List<ExplainableEvidenceItemDto> evidenceItems) { this.evidenceItems = evidenceItems != null ? evidenceItems : new ArrayList<>(); }

    public CalculationTraceDto getCalculationTrace() { return calculationTrace; }
    public void setCalculationTrace(CalculationTraceDto calculationTrace) { this.calculationTrace = calculationTrace; }

    public List<ComponentSensitivityDto> getSensitivityAnalysis() { return sensitivityAnalysis; }
    public void setSensitivityAnalysis(List<ComponentSensitivityDto> sensitivityAnalysis) { this.sensitivityAnalysis = sensitivityAnalysis != null ? sensitivityAnalysis : new ArrayList<>(); }

    public DataQualityExplanationDto getDataQuality() { return dataQuality; }
    public void setDataQuality(DataQualityExplanationDto dataQuality) { this.dataQuality = dataQuality; }

    public List<String> getModelLimitations() { return modelLimitations; }
    public void setModelLimitations(List<String> modelLimitations) { this.modelLimitations = modelLimitations != null ? modelLimitations : new ArrayList<>(); }

    public LocalDateTime getGeneratedTimestamp() { return generatedTimestamp; }
    public void setGeneratedTimestamp(LocalDateTime generatedTimestamp) { this.generatedTimestamp = generatedTimestamp; }
}
