package com.hazard.service.risk.contributor;

import com.hazard.domain.historical.HistoricalTimeWindow;
import com.hazard.dto.historical.DistrictHistoricalSummaryDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.config.RiskScenarioAnalysisRequestDto;
import com.hazard.dto.risk.contributor.ContributorTreeNodeDto;
import com.hazard.dto.risk.contributor.DetailedRiskContributorDto;
import com.hazard.dto.risk.contributor.DistrictRiskContributorsProfileDto;
import com.hazard.dto.risk.contributor.RiskExplanationDto;
import com.hazard.dto.vulnerability.DistrictVulnerabilityScoreDto;
import com.hazard.service.historical.HistoricalDisasterService;
import com.hazard.service.risk.RiskCalculationService;
import com.hazard.service.vulnerability.VulnerabilityScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Domain Service for Stage 4.9 — Risk Contributors.
 * Coordinates retrieval of Stage 4.7 Risk Scores, Stage 4.5 Vulnerability Profiles,
 * and Stage 4.6 Historical Disasters to produce rich explainability profiles.
 */
@Service
@Transactional(readOnly = true)
public class RiskContributorService {

    private static final Logger log = LoggerFactory.getLogger(RiskContributorService.class);

    private final RiskCalculationService riskCalculationService;
    private final VulnerabilityScoringService vulnerabilityScoringService;
    private final HistoricalDisasterService historicalDisasterService;
    private final RiskContributorEngine engine;

    public RiskContributorService(RiskCalculationService riskCalculationService,
                                  VulnerabilityScoringService vulnerabilityScoringService,
                                  HistoricalDisasterService historicalDisasterService,
                                  RiskContributorEngine engine) {
        this.riskCalculationService = riskCalculationService;
        this.vulnerabilityScoringService = vulnerabilityScoringService;
        this.historicalDisasterService = historicalDisasterService;
        this.engine = engine;
    }

    public DistrictRiskContributorsProfileDto getDistrictContributorsProfile(String districtName, Integer limit) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }

        String target = districtName.trim();
        DistrictRiskScoreDto riskDto = riskCalculationService.getDistrictRiskScore(target, null);

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
            log.debug("Historical summary lookup for {}: {}", target, e.getMessage());
        }

        return engine.buildContributorsProfile(riskDto, vulnDto, histDto, limit);
    }

    public List<DetailedRiskContributorDto> getTopContributors(String districtName, Integer limit) {
        return getDistrictContributorsProfile(districtName, limit).getTopContributors();
    }

    public ContributorTreeNodeDto getContributorTree(String districtName) {
        return getDistrictContributorsProfile(districtName, null).getContributorTree();
    }

    public RiskExplanationDto getRiskExplanation(String districtName) {
        return getDistrictContributorsProfile(districtName, null).getExplanation();
    }
}
