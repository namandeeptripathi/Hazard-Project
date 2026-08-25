package com.hazard.dto.risk;

import com.hazard.domain.risk.RiskTier;

import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated DTO for explainable risk endpoints detailing primary drivers.
 */
public class RiskContributorsSummaryDto {

    private String geographicUnit;
    private String geographicId;
    private Double riskScore;
    private Double riskScore100;
    private RiskTier riskTier;
    private String dominantPillar;

    private List<RiskContributorDto> topDrivers = new ArrayList<>();
    private ExposureSubBreakdownDto exposureBreakdown;
    private String primaryVulnerabilityDriver;
    private String historicalEvidenceSummary;

    public RiskContributorsSummaryDto() {
        this.geographicUnit = "DISTRICT";
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

    public String getDominantPillar() {
        return dominantPillar;
    }

    public void setDominantPillar(String dominantPillar) {
        this.dominantPillar = dominantPillar;
    }

    public List<RiskContributorDto> getTopDrivers() {
        return topDrivers;
    }

    public void setTopDrivers(List<RiskContributorDto> topDrivers) {
        this.topDrivers = topDrivers != null ? topDrivers : new ArrayList<>();
    }

    public ExposureSubBreakdownDto getExposureBreakdown() {
        return exposureBreakdown;
    }

    public void setExposureBreakdown(ExposureSubBreakdownDto exposureBreakdown) {
        this.exposureBreakdown = exposureBreakdown;
    }

    public String getPrimaryVulnerabilityDriver() {
        return primaryVulnerabilityDriver;
    }

    public void setPrimaryVulnerabilityDriver(String primaryVulnerabilityDriver) {
        this.primaryVulnerabilityDriver = primaryVulnerabilityDriver;
    }

    public String getHistoricalEvidenceSummary() {
        return historicalEvidenceSummary;
    }

    public void setHistoricalEvidenceSummary(String historicalEvidenceSummary) {
        this.historicalEvidenceSummary = historicalEvidenceSummary;
    }
}
