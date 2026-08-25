package com.hazard.service.risk.explain;

import com.hazard.domain.historical.HistoricalTimeWindow;
import com.hazard.domain.risk.config.RiskConfigurationProfile;
import com.hazard.dto.historical.DistrictHistoricalSummaryDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.config.RiskScenarioAnalysisRequestDto;
import com.hazard.dto.risk.config.RiskScenarioAnalysisResultDto;
import com.hazard.dto.risk.contributor.DistrictRiskContributorsProfileDto;
import com.hazard.dto.risk.explain.*;
import com.hazard.dto.vulnerability.DistrictVulnerabilityScoreDto;
import com.hazard.service.historical.HistoricalDisasterService;
import com.hazard.service.risk.RiskCalculationService;
import com.hazard.service.risk.config.RiskConfigurationService;
import com.hazard.service.risk.contributor.RiskContributorService;
import com.hazard.service.vulnerability.VulnerabilityScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Domain Service for Stage 4.10 — Explainable Risk.
 * Coordinates retrieval of Stage 4.9 Contributor Profiles, Stage 4.7 Risk Calculations,
 * Stage 4.8 Active Configurations, Stage 4.5 Vulnerability Reports, and Stage 4.6 Historical Intelligence
 * to produce full explainability reports for emergency managers.
 */
@Service
@Transactional(readOnly = true)
public class RiskExplanationService {

    private static final Logger log = LoggerFactory.getLogger(RiskExplanationService.class);

    private final RiskCalculationService riskCalculationService;
    private final RiskContributorService riskContributorService;
    private final RiskConfigurationService riskConfigurationService;
    private final VulnerabilityScoringService vulnerabilityScoringService;
    private final HistoricalDisasterService historicalDisasterService;
    private final RiskExplanationEngine engine;

    public RiskExplanationService(RiskCalculationService riskCalculationService,
                                  RiskContributorService riskContributorService,
                                  RiskConfigurationService riskConfigurationService,
                                  VulnerabilityScoringService vulnerabilityScoringService,
                                  HistoricalDisasterService historicalDisasterService,
                                  RiskExplanationEngine engine) {
        this.riskCalculationService = riskCalculationService;
        this.riskContributorService = riskContributorService;
        this.riskConfigurationService = riskConfigurationService;
        this.vulnerabilityScoringService = vulnerabilityScoringService;
        this.historicalDisasterService = historicalDisasterService;
        this.engine = engine;
    }

    public DistrictRiskExplainabilityProfileDto getDistrictExplainabilityProfile(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }

        String target = districtName.trim();
        DistrictRiskScoreDto riskDto = riskCalculationService.getDistrictRiskScore(target, null);
        DistrictRiskContributorsProfileDto contribProfile = riskContributorService.getDistrictContributorsProfile(target, 10);

        DistrictVulnerabilityScoreDto vulnDto = null;
        try {
            vulnDto = vulnerabilityScoringService.getDistrictVulnerabilityScore(target);
        } catch (Exception e) {
            log.debug("Vulnerability lookup for {}: {}", target, e.getMessage());
        }

        DistrictHistoricalSummaryDto histDto = null;
        try {
            histDto = historicalDisasterService.getDistrictHistoricalSummary(target, HistoricalTimeWindow.ALL_HISTORY, null, null, null);
        } catch (Exception e) {
            log.debug("Historical lookup for {}: {}", target, e.getMessage());
        }

        RiskConfigurationProfile configProfile = riskConfigurationService.getActiveConfiguration();

        return engine.buildExplainabilityProfile(contribProfile, riskDto, vulnDto, histDto, configProfile);
    }

    public ExplanationSummaryDto getExplanationSummary(String districtName) {
        return getDistrictExplainabilityProfile(districtName).getSummary();
    }

    public List<ExplainableEvidenceItemDto> getEvidenceItems(String districtName) {
        return getDistrictExplainabilityProfile(districtName).getEvidenceItems();
    }

    public CalculationTraceDto getCalculationTrace(String districtName) {
        return getDistrictExplainabilityProfile(districtName).getCalculationTrace();
    }

    public List<ComponentSensitivityDto> getSensitivityAnalysis(String districtName) {
        return getDistrictExplainabilityProfile(districtName).getSensitivityAnalysis();
    }

    public RiskScenarioAnalysisResultDto analyzeScenarioExplainability(RiskScenarioAnalysisRequestDto request) {
        return riskCalculationService.runScenarioAnalysis(request);
    }
}
