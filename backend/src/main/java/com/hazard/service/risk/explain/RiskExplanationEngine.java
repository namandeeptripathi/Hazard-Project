package com.hazard.service.risk.explain;

import com.hazard.domain.risk.config.RiskConfigurationProfile;
import com.hazard.domain.risk.explain.EvidenceType;
import com.hazard.domain.risk.explain.SensitivityImpactTier;
import com.hazard.dto.historical.DistrictHistoricalSummaryDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.ExposureSubBreakdownDto;
import com.hazard.dto.risk.RiskComponentDetailDto;
import com.hazard.dto.risk.contributor.DetailedRiskContributorDto;
import com.hazard.dto.risk.contributor.DistrictRiskContributorsProfileDto;
import com.hazard.dto.risk.explain.*;
import com.hazard.dto.vulnerability.DistrictVulnerabilityScoreDto;
import com.hazard.dto.vulnerability.IndicatorContributionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure Mathematical and Textual Explainability Engine for Stage 4.10 — Explainable Risk.
 * Produces multi-level human summaries, empirical evidence catalogs with provenance,
 * calculation traces, formula displays, one-at-a-time sensitivity analyses, and model limitations.
 */
@Component
public class RiskExplanationEngine {

    private static final Logger log = LoggerFactory.getLogger(RiskExplanationEngine.class);
    private static final String EXPLANATION_VERSION = "explain-v1";
    private static final double SENSITIVITY_DELTA = 0.10;

    public DistrictRiskExplainabilityProfileDto buildExplainabilityProfile(
            DistrictRiskContributorsProfileDto contribProfile,
            DistrictRiskScoreDto riskDto,
            DistrictVulnerabilityScoreDto vulnDto,
            DistrictHistoricalSummaryDto histDto,
            RiskConfigurationProfile configProfile) {

        if (riskDto == null) {
            throw new IllegalArgumentException("Risk calculation result cannot be null");
        }

        DistrictRiskExplainabilityProfileDto profile = new DistrictRiskExplainabilityProfileDto();
        profile.setDistrictName(riskDto.getDistrictName());
        profile.setDistrictId(riskDto.getDistrictId());
        profile.setState(riskDto.getState());
        profile.setGeographicId(riskDto.getGeographicId());
        profile.setRiskScore(riskDto.getRiskScore());
        profile.setRiskScore100(riskDto.getRiskScore100());
        profile.setRiskTier(riskDto.getRiskTier());
        profile.setConfigurationId(riskDto.getConfigurationId());
        profile.setConfigurationVersion(riskDto.getConfigurationVersion());
        profile.setConfigurationName(riskDto.getConfigurationName());
        profile.setCalculationVersion(riskDto.getCalculationVersion());
        profile.setExplanationVersion(EXPLANATION_VERSION);

        List<DetailedRiskContributorDto> allContributors = contribProfile != null ? contribProfile.getAllContributors() : Collections.emptyList();

        // 1. Primary Drivers vs Secondary Factors
        List<DetailedRiskContributorDto> topLevel = allContributors.stream()
                .filter(c -> c.getLevel() == 1)
                .sorted(Comparator.comparing(DetailedRiskContributorDto::getContribution).reversed())
                .collect(Collectors.toList());

        List<DetailedRiskContributorDto> primary = new ArrayList<>();
        List<DetailedRiskContributorDto> secondary = new ArrayList<>();

        for (int i = 0; i < topLevel.size(); i++) {
            DetailedRiskContributorDto c = topLevel.get(i);
            if (i < 2 || (c.getContributionPercent() != null && c.getContributionPercent() >= 15.0)) {
                primary.add(c);
            } else {
                secondary.add(c);
            }
        }
        profile.setPrimaryDrivers(primary);
        profile.setSecondaryFactors(secondary);

        // 2. Multi-Level Human Summaries
        profile.setSummary(buildSummaries(riskDto, primary, secondary, histDto));

        // 3. Evidence Catalog with Provenance
        profile.setEvidenceItems(buildEvidenceCatalog(riskDto, vulnDto, histDto, configProfile));

        // 4. Calculation Trace & Formula Display
        profile.setCalculationTrace(buildCalculationTrace(riskDto, topLevel));

        // 5. Model Sensitivity Analysis ("What Would Change This Score?")
        profile.setSensitivityAnalysis(buildSensitivityAnalysis(riskDto, topLevel));

        // 6. Data Quality Explanation
        profile.setDataQuality(buildDataQualityExplanation(riskDto));

        // 7. Model Limitations
        profile.setModelLimitations(buildModelLimitations());

        return profile;
    }

    // =========================================================================
    // MULTI-LEVEL HUMAN SUMMARIES
    // =========================================================================

    public ExplanationSummaryDto buildSummaries(
            DistrictRiskScoreDto risk,
            List<DetailedRiskContributorDto> primary,
            List<DetailedRiskContributorDto> secondary,
            DistrictHistoricalSummaryDto hist) {

        ExplanationSummaryDto summary = new ExplanationSummaryDto();

        String dominant = !primary.isEmpty() ? primary.get(0).getName() : "Hazard";
        summary.setDominantDriver(dominant);

        String tier = risk.getRiskTier() != null ? risk.getRiskTier().name() : "UNKNOWN";
        double score100 = risk.getRiskScore100() != null ? risk.getRiskScore100() : 0.0;

        // Level 1: Executive Summary (1 concise sentence)
        summary.setExecutiveSummary(String.format(
                "District %s is evaluated at %s modeled disaster risk (%.1f/100), primarily driven by %s.",
                risk.getDistrictName(), tier, score100, dominant
        ));

        // Level 2: Short Summary (2–4 concise sentences)
        StringBuilder shortB = new StringBuilder();
        shortB.append(String.format("District %s has an overall disaster risk score of %.1f (%s Tier). ",
                risk.getDistrictName(), score100, tier));

        if (!primary.isEmpty()) {
            String primaryNames = primary.stream()
                    .map(d -> String.format("%s (%.1f%%)", d.getName(), d.getContributionPercent()))
                    .collect(Collectors.joining(" and "));
            shortB.append(String.format("The primary drivers are %s. ", primaryNames));
        }

        if (hist != null && hist.getTotalHistoricalEvents() > 0) {
            shortB.append(String.format("Archival records substantiate %d past flood disaster occurrences in this region. ",
                    hist.getTotalHistoricalEvents()));
        }

        int avail = risk.getDataQuality() != null ? risk.getDataQuality().getAvailableComponents() : 4;
        int conf = risk.getDataQuality() != null ? risk.getDataQuality().getConfiguredComponents() : 4;
        shortB.append(String.format("The evaluation incorporates %d of %d major risk pillars.", avail, conf));

        summary.setShortSummary(shortB.toString().trim());

        // Level 3: Detailed Narrative
        StringBuilder detailedB = new StringBuilder();
        detailedB.append(String.format("District %s has been evaluated using a multi-criteria weighted linear risk model, yielding a final compound score of %.1f out of 100 (%s Risk Tier). ",
                risk.getDistrictName(), score100, tier));
        detailedB.append(String.format("The most influential factor in this score is %s, contributing %.4f (%.1f%% share of total risk). ",
                dominant,
                !primary.isEmpty() ? primary.get(0).getContribution() : 0.0,
                !primary.isEmpty() ? primary.get(0).getContributionPercent() : 0.0));

        if (primary.size() > 1) {
            detailedB.append(String.format("This is augmented by %s (contributing %.4f, %.1f%% share). ",
                    primary.get(1).getName(), primary.get(1).getContribution(), primary.get(1).getContributionPercent()));
        }

        if (!secondary.isEmpty()) {
            String secNames = secondary.stream()
                    .map(d -> String.format("%s (%.1f%%)", d.getName(), d.getContributionPercent()))
                    .collect(Collectors.joining(", "));
            detailedB.append(String.format("Secondary contributing factors include %s. ", secNames));
        }

        detailedB.append("This score is a decision-support metric synthesizing spatial hazard intensity, demographic exposure, socioeconomic vulnerability, and archival disaster history.");
        summary.setDetailedNarrative(detailedB.toString().trim());

        // Bullet Lists
        summary.setPrimaryDriversList(primary.stream()
                .map(d -> String.format("%s: accounts for %.1f%% of modeled risk (effective weight: %s, normalized score: %.4f)",
                        d.getName(), d.getContributionPercent(),
                        d.getEffectiveWeight() != null ? Math.round(d.getEffectiveWeight() * 100) + "%" : "-",
                        d.getNormalizedScore()))
                .collect(Collectors.toList()));

        summary.setSecondaryFactorsList(secondary.stream()
                .map(d -> String.format("%s: accounts for %.1f%% of modeled risk", d.getName(), d.getContributionPercent()))
                .collect(Collectors.toList()));

        if (hist != null) {
            summary.setHistoricalContextSummary(String.format(
                    "Historical hotspot index of %.2f with %d recorded events (recurrence frequency: %.3f events/year).",
                    hist.getHistoricalHotspotIndex(), hist.getTotalHistoricalEvents(), hist.getEventsPerYear()
            ));
        }

        return summary;
    }

    // =========================================================================
    // EVIDENCE CATALOG WITH PROVENANCE
    // =========================================================================

    public List<ExplainableEvidenceItemDto> buildEvidenceCatalog(
            DistrictRiskScoreDto risk,
            DistrictVulnerabilityScoreDto vuln,
            DistrictHistoricalSummaryDto hist,
            RiskConfigurationProfile config) {

        List<ExplainableEvidenceItemDto> items = new ArrayList<>();

        // 1. Hazard Evidence
        Map<String, RiskComponentDetailDto> comps = risk.getComponents();
        if (comps != null && comps.containsKey("HAZARD")) {
            RiskComponentDetailDto h = comps.get("HAZARD");
            items.add(new ExplainableEvidenceItemDto(
                    "EVID_HAZARD_SEVERITY", EvidenceType.HAZARD_EVIDENCE, "Spatial Hazard Intensity Score",
                    h.getScore(), String.format("%.2f / 1.00", h.getScore()), "normalized",
                    "Stage 3 Hazard Intelligence", "Multi-Hazard Spatial Inundation Grid & Catalog",
                    "Current Simulation / Real-Time", risk.getDistrictName() + " Basin Footprint",
                    "Normalized flood inundation footprint and rainfall hazard intensity intersecting district geometry"
            ));
        }

        // 2. Population Evidence
        ExposureSubBreakdownDto exp = risk.getExposureSubBreakdown();
        if (exp != null && exp.getExposedPopulation() != null) {
            items.add(new ExplainableEvidenceItemDto(
                    "EVID_POP_EXPOSED", EvidenceType.POPULATION_EVIDENCE, "Exposed Inundation Population",
                    exp.getExposedPopulation(), String.format("%,d citizens (%.1f%% of district)", exp.getExposedPopulation(), exp.getExposedPopulationPercentage()), "people",
                    "Stage 4.1 Population Exposure", "Census of India Demographic Layer & GADM Boundaries",
                    "Decadal Census Baseline", risk.getDistrictName() + " District",
                    "Number of residents located physically inside the modeled flood inundation zone"
            ));
        }

        // 3. Settlement Evidence
        if (exp != null && exp.getSettlementsExposedCount() != null) {
            items.add(new ExplainableEvidenceItemDto(
                    "EVID_SETTLE_EXPOSED", EvidenceType.SETTLEMENT_EVIDENCE, "Exposed Habitation Clusters",
                    exp.getSettlementsExposedCount(), String.format("%,d settlements", exp.getSettlementsExposedCount()), "settlement units",
                    "Stage 4.2 Settlement Exposure", "Survey of India Habitation Nodes & OpenStreetMap",
                    "Contemporary Spatial Vector", risk.getDistrictName() + " Habitations",
                    "Number of village polygons and urban settlement clusters within or bordering the hazard perimeter"
            ));
        }

        // 4. Infrastructure Evidence
        if (exp != null && exp.getInfrastructureAssetsExposedCount() != null) {
            items.add(new ExplainableEvidenceItemDto(
                    "EVID_INFRA_EXPOSED", EvidenceType.INFRASTRUCTURE_EVIDENCE, "Exposed Critical Lifelines",
                    exp.getInfrastructureAssetsExposedCount(), String.format("%,d critical facilities", exp.getInfrastructureAssetsExposedCount()), "critical assets",
                    "Stage 4.3 Infrastructure Exposure", "OpenStreetMap & Bihar State Infrastructure Directory",
                    "Infrastructure Master Catalog", risk.getDistrictName() + " Facilities",
                    "Hospitals, bridges, culverts, schools, and embankments directly exposed to inundation"
            ));
        }

        // 5. Vulnerability Evidence
        if (vuln != null && vuln.getTopContributors() != null && !vuln.getTopContributors().isEmpty()) {
            IndicatorContributionDto topInd = vuln.getTopContributors().get(0);
            items.add(new ExplainableEvidenceItemDto(
                    "EVID_VULN_TOP_DRIVER", EvidenceType.VULNERABILITY_EVIDENCE, "Primary Vulnerability Deficit",
                    topInd.getNormalizedValue(), String.format("%s (score: %.2f)", topInd.getIndicatorName(), topInd.getNormalizedValue()), "indicator score",
                    "Stage 4.5 Vulnerability Scoring", "SECC, NFHS-5 & State Development Indicators",
                    "Socioeconomic Survey Baseline", risk.getDistrictName(),
                    "Top contributing vulnerability factor: " + (topInd.getCategory() != null ? topInd.getCategory().getDisplayName() : "Vulnerability")
            ));
        }

        // 6. Historical Evidence
        if (hist != null) {
            items.add(new ExplainableEvidenceItemDto(
                    "EVID_HIST_EVENTS", EvidenceType.HISTORICAL_EVIDENCE, "Archival Disaster Occurrences",
                    hist.getTotalHistoricalEvents(), String.format("%d recorded events (%.3f events/yr)", hist.getTotalHistoricalEvents(), hist.getEventsPerYear()), "disaster events",
                    "Stage 4.6 Historical Disaster Intelligence", "Dartmouth Flood Observatory (DFO) & EM-DAT",
                    "Historical Multi-Decadal Archival Record", risk.getDistrictName() + " Basin",
                    "Empirical historical flood and extreme rainfall events documented in global and national disaster archives"
            ));
        }

        // 7. Configuration Evidence
        items.add(new ExplainableEvidenceItemDto(
                "EVID_CONFIG_VERSION", EvidenceType.CONFIGURATION_EVIDENCE, "Active Risk Configuration Profile",
                risk.getConfigurationId() != null ? risk.getConfigurationId() : "risk-v1",
                String.format("%s (Version %s)", risk.getConfigurationId() != null ? risk.getConfigurationId() : "risk-v1",
                        risk.getConfigurationVersion() != null ? risk.getConfigurationVersion() : "1.0"), "config profile",
                "Stage 4.8 Configurable Risk Weights", "State Disaster Risk Management Configuration Engine",
                "Runtime Active Configuration", "System-Wide Policy",
                "Approved policy weights and classification thresholds governing multi-criteria score computation"
        ));

        // 8. Data Quality Evidence
        if (risk.getDataQuality() != null) {
            items.add(new ExplainableEvidenceItemDto(
                    "EVID_DATA_QUALITY", EvidenceType.DATA_QUALITY_EVIDENCE, "Input Data Completeness",
                    risk.getDataQuality().getCompletenessPercentage(),
                    String.format("%.1f%% (%d/%d pillars available)", risk.getDataQuality().getCompletenessPercentage(),
                            risk.getDataQuality().getAvailableComponents(), risk.getDataQuality().getConfiguredComponents()), "percentage",
                    "Stage 4.7 Risk Calculation Engine", "Data Quality & Completeness Verifier",
                    "Evaluation Timestamp", risk.getDistrictName(),
                    "Completeness status: " + risk.getDataQuality().getStatus().name()
            ));
        }

        return items;
    }

    // =========================================================================
    // CALCULATION TRACE & FORMULA DISPLAY
    // =========================================================================

    public CalculationTraceDto buildCalculationTrace(DistrictRiskScoreDto risk, List<DetailedRiskContributorDto> topLevel) {
        CalculationTraceDto trace = new CalculationTraceDto();

        List<CalculationComponentTraceDto> compTraces = new ArrayList<>();
        StringBuilder formulaParam = new StringBuilder();
        StringBuilder formulaNamed = new StringBuilder();

        double sumContribs = 0.0;

        for (int i = 0; i < topLevel.size(); i++) {
            DetailedRiskContributorDto c = topLevel.get(i);
            double score = c.getNormalizedScore() != null ? c.getNormalizedScore() : 0.0;
            double effW = c.getEffectiveWeight() != null ? c.getEffectiveWeight() : 0.0;
            double contrib = c.getContribution() != null ? c.getContribution() : 0.0;
            sumContribs += contrib;

            String compFormula = String.format("(%.4f × %.2f)", score, effW);
            compTraces.add(new CalculationComponentTraceDto(
                    c.getId(), c.getName(), score, score, c.getConfiguredWeight(),
                    effW, contrib, compFormula, c.getContributionPercent()
            ));

            if (i > 0) {
                formulaNamed.append(" + ");
                formulaParam.append(" + ");
            }
            formulaNamed.append(String.format("(%s × %.2f)", c.getId(), effW));
            formulaParam.append(String.format("(%.4f × %.2f = %.4f)", score, effW, contrib));
        }

        trace.setFormulaString("Risk = " + formulaNamed.toString());
        trace.setParameterizedFormulaString(String.format("%s = %.4f → %.1f / 100",
                formulaParam.toString(), sumContribs, round1(sumContribs * 100.0)));
        trace.setComponents(compTraces);
        trace.setSumOfContributions(round4(sumContribs));
        trace.setFinalNormalizedScore(risk.getRiskScore());
        trace.setFinalDisplayScore100(risk.getRiskScore100());

        double diff = Math.abs(sumContribs - (risk.getRiskScore() != null ? risk.getRiskScore() : 0.0));
        trace.setReconciled(diff <= 0.005);

        return trace;
    }

    // =========================================================================
    // ONE-AT-A-TIME SENSITIVITY ANALYSIS
    // =========================================================================

    public List<ComponentSensitivityDto> buildSensitivityAnalysis(DistrictRiskScoreDto risk, List<DetailedRiskContributorDto> topLevel) {
        List<ComponentSensitivityDto> list = new ArrayList<>();
        double baseRisk = risk.getRiskScore() != null ? risk.getRiskScore() : 0.0;

        for (DetailedRiskContributorDto c : topLevel) {
            double baseScore = c.getNormalizedScore() != null ? c.getNormalizedScore() : 0.0;
            double effW = c.getEffectiveWeight() != null ? c.getEffectiveWeight() : 0.0;

            double scorePlus = Math.min(1.0, baseScore + SENSITIVITY_DELTA);
            double scoreMinus = Math.max(0.0, baseScore - SENSITIVITY_DELTA);

            double riskPlus = round4(baseRisk + effW * (scorePlus - baseScore));
            double riskMinus = round4(baseRisk + effW * (scoreMinus - baseScore));

            double impact = round4(effW * SENSITIVITY_DELTA);
            SensitivityImpactTier tier = SensitivityImpactTier.fromAbsoluteImpact(impact);

            String interpretation = String.format("A ±10%% change in %s results in an immediate ±%.3f shift (%.1f points) in the total disaster risk score.",
                    c.getName(), impact, impact * 100.0);

            list.add(new ComponentSensitivityDto(
                    c.getId(), c.getName(), round4(baseScore), round4(effW),
                    round4(baseRisk), riskPlus, riskMinus, impact, tier, 0, interpretation
            ));
        }

        // Rank by descending leverage
        list.sort(Comparator.comparing(ComponentSensitivityDto::getAbsoluteLeverageImpact).reversed());
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setLeverageRank(i + 1);
        }

        return list;
    }

    // =========================================================================
    // DATA QUALITY & COMPLETENESS EXPLANATION
    // =========================================================================

    public DataQualityExplanationDto buildDataQualityExplanation(DistrictRiskScoreDto risk) {
        DataQualityExplanationDto dto = new DataQualityExplanationDto();

        List<String> allPillars = Arrays.asList("HAZARD", "EXPOSURE", "VULNERABILITY", "HISTORICAL");
        List<String> available = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        if (risk.getComponents() != null) {
            for (String p : allPillars) {
                if (risk.getComponents().containsKey(p) && risk.getComponents().get(p).getScore() != null) {
                    available.add(p);
                } else {
                    missing.add(p);
                }
            }
        } else {
            available.addAll(allPillars);
        }

        dto.setAvailableComponentNames(available);
        dto.setMissingComponentNames(missing);

        if (risk.getDataQuality() != null) {
            dto.setStatus(risk.getDataQuality().getStatus().name());
            dto.setConfiguredComponents(risk.getDataQuality().getConfiguredComponents());
            dto.setAvailableComponents(risk.getDataQuality().getAvailableComponents());
            dto.setCompletenessPercentage(risk.getDataQuality().getCompletenessPercentage());

            if (!missing.isEmpty()) {
                dto.setRedistributionApplied(true);
                dto.setExplanationText(String.format("Risk evaluation used %d of %d major risk components. Missing component(s) [%s] had active weight proportionally redistributed across available pillars.",
                        dto.getAvailableComponents(), dto.getConfiguredComponents(),
                        String.join(", ", missing)));
            } else {
                dto.setRedistributionApplied(false);
                dto.setExplanationText(String.format("Risk evaluation is based on full input data completeness (all %d of %d major risk pillars available and evaluated).",
                        dto.getAvailableComponents(), dto.getConfiguredComponents()));
            }
        } else {
            dto.setStatus("DATA_COMPLETE");
            dto.setConfiguredComponents(4);
            dto.setAvailableComponents(4);
            dto.setCompletenessPercentage(100.0);
            dto.setExplanationText("Full data completeness: all 4 risk pillars evaluated.");
        }

        return dto;
    }

    // =========================================================================
    // MODEL LIMITATIONS & BOUNDARIES
    // =========================================================================

    public List<String> buildModelLimitations() {
        return Arrays.asList(
                "Decision Support Metric: Disaster Risk Score represents relative multi-criteria priority, not a deterministic guarantee of physical disaster occurrence.",
                "Descriptive Archival Records: Historical flood recurrence and frequency represent documented past events and should not be confused with statistical future probabilities.",
                "Policy-Configured Weights: Pillar and sub-component weights reflect authorized disaster management assumptions and can be adjusted through configuration versions.",
                "Resolution Disparities: Demographic, settlement, and infrastructure layers have differing spatial resolutions, mapped to district boundaries via GIS spatial intersection.",
                "No Autonomous Dispatch: This system provides explainable intelligence to human emergency commanders; it does not execute autonomous evacuations or resource dispatches."
        );
    }

    public static double round4(double val) {
        return Math.round(val * 10000.0) / 10000.0;
    }

    public static double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
