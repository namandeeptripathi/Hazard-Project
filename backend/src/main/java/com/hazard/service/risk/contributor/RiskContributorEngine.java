package com.hazard.service.risk.contributor;

import com.hazard.domain.risk.contributor.ContributorDataAvailability;
import com.hazard.domain.risk.contributor.ContributorDirection;
import com.hazard.domain.risk.contributor.ContributorImportance;
import com.hazard.dto.historical.DistrictHistoricalSummaryDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.ExposureSubBreakdownDto;
import com.hazard.dto.risk.RiskComponentDetailDto;
import com.hazard.dto.risk.contributor.ContributorTreeNodeDto;
import com.hazard.dto.risk.contributor.DetailedRiskContributorDto;
import com.hazard.dto.risk.contributor.DistrictRiskContributorsProfileDto;
import com.hazard.dto.risk.contributor.RiskExplanationDto;
import com.hazard.dto.vulnerability.DistrictVulnerabilityScoreDto;
import com.hazard.dto.vulnerability.IndicatorContributionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure Mathematical and Explainability Engine for Stage 4.9 — Risk Contributors.
 * Decomposes final disaster risk scores into ranked hierarchical contributors,
 * contribution percentages, multi-level tree structures, and dynamic natural-language narratives.
 */
@Component
public class RiskContributorEngine {

    private static final Logger log = LoggerFactory.getLogger(RiskContributorEngine.class);

    public DistrictRiskContributorsProfileDto buildContributorsProfile(
            DistrictRiskScoreDto riskDto,
            DistrictVulnerabilityScoreDto vulnDto,
            DistrictHistoricalSummaryDto histDto,
            Integer limit) {

        if (riskDto == null) {
            throw new IllegalArgumentException("Risk calculation result cannot be null");
        }

        DistrictRiskContributorsProfileDto profile = new DistrictRiskContributorsProfileDto();
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

        if (riskDto.getDataQuality() != null) {
            profile.setDataQualityStatus(riskDto.getDataQuality().getStatus().name());
            profile.setDataCompletenessPercentage(riskDto.getDataQuality().getCompletenessPercentage());
        }

        double totalRisk = Math.max(0.0001, riskDto.getRiskScore() != null ? riskDto.getRiskScore() : 0.0);

        // 1. Build List of All Contributors (Top Pillars + Sub-components + Key Indicators)
        List<DetailedRiskContributorDto> allContributors = new ArrayList<>();

        // Level 1: 4 Top Pillars
        Map<String, RiskComponentDetailDto> components = riskDto.getComponents();
        if (components != null) {
            if (components.containsKey("HAZARD")) {
                RiskComponentDetailDto h = components.get("HAZARD");
                double hContrib = h.getContribution() != null ? h.getContribution() : 0.0;
                double hPct = round1((hContrib / totalRisk) * 100.0);
                allContributors.add(new DetailedRiskContributorDto(
                        "HAZARD", "Hazard Severity & Intensity", "HAZARD", 1,
                        h.getScore(), h.getScore(), h.getConfiguredWeight(), h.getEffectiveWeight(),
                        hContrib, hPct, 0, ContributorImportance.fromPercentage(hPct),
                        "Stage 3 Multi-Hazard Intelligence",
                        "Physical hazard intensity and spatial inundation footprint in district"
                ));
            }

            if (components.containsKey("EXPOSURE")) {
                RiskComponentDetailDto e = components.get("EXPOSURE");
                double eContrib = e.getContribution() != null ? e.getContribution() : 0.0;
                double ePct = round1((eContrib / totalRisk) * 100.0);
                allContributors.add(new DetailedRiskContributorDto(
                        "EXPOSURE", "Combined Exposure", "EXPOSURE", 1,
                        e.getScore(), e.getScore(), e.getConfiguredWeight(), e.getEffectiveWeight(),
                        eContrib, ePct, 0, ContributorImportance.fromPercentage(ePct),
                        "Stages 4.1–4.3 Exposure Synthesis",
                        "Combined physical exposure of population, settlements, and lifeline infrastructure"
                ));
            }

            if (components.containsKey("VULNERABILITY")) {
                RiskComponentDetailDto v = components.get("VULNERABILITY");
                double vContrib = v.getContribution() != null ? v.getContribution() : 0.0;
                double vPct = round1((vContrib / totalRisk) * 100.0);
                allContributors.add(new DetailedRiskContributorDto(
                        "VULNERABILITY", "Vulnerability & Coping Deficit", "VULNERABILITY", 1,
                        v.getScore(), v.getScore(), v.getConfiguredWeight(), v.getEffectiveWeight(),
                        vContrib, vPct, 0, ContributorImportance.fromPercentage(vPct),
                        "Stage 4.5 Composite Vulnerability Engine",
                        "Socioeconomic, housing, drainage, and institutional susceptibility factors"
                ));
            }

            if (components.containsKey("HISTORICAL")) {
                RiskComponentDetailDto t = components.get("HISTORICAL");
                double tContrib = t.getContribution() != null ? t.getContribution() : 0.0;
                double tPct = round1((tContrib / totalRisk) * 100.0);
                allContributors.add(new DetailedRiskContributorDto(
                        "HISTORICAL", "Historical Disaster Evidence", "HISTORICAL", 1,
                        t.getScore(), t.getScore(), t.getConfiguredWeight(), t.getEffectiveWeight(),
                        tContrib, tPct, 0, ContributorImportance.fromPercentage(tPct),
                        "Stage 4.6 Historical Disaster Intelligence",
                        "Empirical archival disaster recurrence, frequency, and hotspot intensity"
                ));
            }
        }

        // Level 2: Exposure Sub-Breakdown
        ExposureSubBreakdownDto expBreakdown = riskDto.getExposureSubBreakdown();
        if (expBreakdown != null && components != null && components.containsKey("EXPOSURE")) {
            RiskComponentDetailDto expComp = components.get("EXPOSURE");
            double expEffWeight = expComp.getEffectiveWeight() != null ? expComp.getEffectiveWeight() : 0.30;

            if (expBreakdown.getPopulationExposureScore() != null) {
                double popRawW = expBreakdown.getPopulationConfiguredWeight() != null ? expBreakdown.getPopulationConfiguredWeight() : 0.40;
                double popContrib = round4(expEffWeight * popRawW * expBreakdown.getPopulationExposureScore());
                double popPct = round1((popContrib / totalRisk) * 100.0);
                allContributors.add(new DetailedRiskContributorDto(
                        "EXPOSURE_POPULATION", "Population Exposure", "EXPOSURE", 2,
                        expBreakdown.getExposedPopulation() != null ? expBreakdown.getExposedPopulation().doubleValue() : null,
                        expBreakdown.getPopulationExposureScore(), popRawW, round4(expEffWeight * popRawW),
                        popContrib, popPct, 0, ContributorImportance.fromPercentage(popPct),
                        "Stage 4.1 Population Exposure",
                        String.format("%s people physically inside hazard zone (%s%% of district population)",
                                expBreakdown.getExposedPopulation() != null ? expBreakdown.getExposedPopulation() : "N/A",
                                expBreakdown.getExposedPopulationPercentage() != null ? expBreakdown.getExposedPopulationPercentage() : "0")
                ));
            }

            if (expBreakdown.getSettlementExposureScore() != null) {
                double setRawW = expBreakdown.getSettlementConfiguredWeight() != null ? expBreakdown.getSettlementConfiguredWeight() : 0.25;
                double setContrib = round4(expEffWeight * setRawW * expBreakdown.getSettlementExposureScore());
                double setPct = round1((setContrib / totalRisk) * 100.0);
                allContributors.add(new DetailedRiskContributorDto(
                        "EXPOSURE_SETTLEMENTS", "Settlement Habitation Exposure", "EXPOSURE", 2,
                        expBreakdown.getSettlementsExposedCount() != null ? expBreakdown.getSettlementsExposedCount().doubleValue() : null,
                        expBreakdown.getSettlementExposureScore(), setRawW, round4(expEffWeight * setRawW),
                        setContrib, setPct, 0, ContributorImportance.fromPercentage(setPct),
                        "Stage 4.2 Settlement Exposure",
                        String.format("%s settlement units in inundated or high-hazard zones",
                                expBreakdown.getSettlementsExposedCount() != null ? expBreakdown.getSettlementsExposedCount() : "N/A")
                ));
            }

            if (expBreakdown.getInfrastructureExposureScore() != null) {
                double infRawW = expBreakdown.getInfrastructureConfiguredWeight() != null ? expBreakdown.getInfrastructureConfiguredWeight() : 0.35;
                double infContrib = round4(expEffWeight * infRawW * expBreakdown.getInfrastructureExposureScore());
                double infPct = round1((infContrib / totalRisk) * 100.0);
                allContributors.add(new DetailedRiskContributorDto(
                        "EXPOSURE_INFRASTRUCTURE", "Critical Lifeline Infrastructure Exposure", "EXPOSURE", 2,
                        expBreakdown.getInfrastructureAssetsExposedCount() != null ? expBreakdown.getInfrastructureAssetsExposedCount().doubleValue() : null,
                        expBreakdown.getInfrastructureExposureScore(), infRawW, round4(expEffWeight * infRawW),
                        infContrib, infPct, 0, ContributorImportance.fromPercentage(infPct),
                        "Stage 4.3 Infrastructure Exposure",
                        String.format("%s critical assets (hospitals, bridges, embankments) exposed",
                                expBreakdown.getInfrastructureAssetsExposedCount() != null ? expBreakdown.getInfrastructureAssetsExposedCount() : "N/A")
                ));
            }
        }

        // Level 3: Top Vulnerability Indicators (from Stage 4.5)
        if (vulnDto != null && vulnDto.getTopContributors() != null && components != null && components.containsKey("VULNERABILITY")) {
            RiskComponentDetailDto vComp = components.get("VULNERABILITY");
            double vEffWeight = vComp.getEffectiveWeight() != null ? vComp.getEffectiveWeight() : 0.25;

            for (IndicatorContributionDto ind : vulnDto.getTopContributors()) {
                double indContrib = round4(vEffWeight * (ind.getContribution() != null ? ind.getContribution() : 0.0));
                double indPct = round1((indContrib / totalRisk) * 100.0);
                String catDisplayName = ind.getCategory() != null ? ind.getCategory().getDisplayName() : "Vulnerability";
                allContributors.add(new DetailedRiskContributorDto(
                        "VULN_" + (ind.getIndicatorId() != null ? ind.getIndicatorId() : "IND"),
                        ind.getIndicatorName(), "VULNERABILITY", 3,
                        ind.getRawValue(), ind.getNormalizedValue(), ind.getConfiguredWeight(),
                        round4(vEffWeight * (ind.getEffectiveWeight() != null ? ind.getEffectiveWeight() : 0.10)),
                        indContrib, indPct, 0, ContributorImportance.fromPercentage(indPct),
                        "Stage 4.4 Vulnerability Indicators",
                        catDisplayName + ": " + ind.getIndicatorName()
                ));
            }
        }

        // 2. Deterministic Ranking
        allContributors.sort((a, b) -> {
            int cmpContrib = Double.compare(b.getContribution() != null ? b.getContribution() : 0.0,
                    a.getContribution() != null ? a.getContribution() : 0.0);
            if (cmpContrib != 0) return cmpContrib;
            int cmpScore = Double.compare(b.getNormalizedScore() != null ? b.getNormalizedScore() : 0.0,
                    a.getNormalizedScore() != null ? a.getNormalizedScore() : 0.0);
            if (cmpScore != 0) return cmpScore;
            return a.getId().compareTo(b.getId());
        });

        // Assign Ranks
        for (int i = 0; i < allContributors.size(); i++) {
            allContributors.get(i).setRank(i + 1);
        }

        int maxLimit = (limit != null && limit > 0) ? limit : 5;
        List<DetailedRiskContributorDto> topContributors = allContributors.stream()
                .limit(maxLimit)
                .collect(Collectors.toList());

        profile.setAllContributors(allContributors);
        profile.setTopContributors(topContributors);

        // 3. Hierarchical Contributor Tree
        profile.setContributorTree(buildContributorTree(riskDto, allContributors));

        // 4. Natural-Language Explanation
        profile.setExplanation(buildExplanation(riskDto, vulnDto, histDto, allContributors));

        // 5. Mathematical Check Metadata
        profile.setMathematicalCheck(performMathematicalValidation(riskDto, allContributors));

        return profile;
    }

    // =========================================================================
    // HIERARCHICAL CONTRIBUTOR TREE
    // =========================================================================

    public ContributorTreeNodeDto buildContributorTree(DistrictRiskScoreDto risk, List<DetailedRiskContributorDto> contributors) {
        double totalRisk = risk.getRiskScore() != null ? risk.getRiskScore() : 0.0;

        ContributorTreeNodeDto root = new ContributorTreeNodeDto(
                "TOTAL_RISK", "Final Disaster Risk", 0,
                round4(totalRisk), 1.0, round4(totalRisk), 100.0,
                ContributorImportance.DOMINANT, risk.getRiskTier().getColorHex()
        );

        Map<String, DetailedRiskContributorDto> contribMap = contributors.stream()
                .collect(Collectors.toMap(DetailedRiskContributorDto::getId, c -> c, (c1, c2) -> c1));

        // 1. Hazard Branch
        DetailedRiskContributorDto h = contribMap.get("HAZARD");
        if (h != null) {
            ContributorTreeNodeDto hNode = new ContributorTreeNodeDto(
                    "HAZARD", "Hazard Severity & Intensity", 1,
                    h.getNormalizedScore(), h.getEffectiveWeight(), h.getContribution(), h.getContributionPercent(),
                    h.getImportance(), "#38bdf8"
            );
            root.addChild(hNode);
        }

        // 2. Exposure Branch
        DetailedRiskContributorDto e = contribMap.get("EXPOSURE");
        if (e != null) {
            ContributorTreeNodeDto eNode = new ContributorTreeNodeDto(
                    "EXPOSURE", "Combined Exposure", 1,
                    e.getNormalizedScore(), e.getEffectiveWeight(), e.getContribution(), e.getContributionPercent(),
                    e.getImportance(), "#fb923c"
            );

            DetailedRiskContributorDto pop = contribMap.get("EXPOSURE_POPULATION");
            if (pop != null) {
                eNode.addChild(new ContributorTreeNodeDto(
                        pop.getId(), pop.getName(), 2, pop.getNormalizedScore(), pop.getEffectiveWeight(),
                        pop.getContribution(), pop.getContributionPercent(), pop.getImportance(), "#fb923c"
                ));
            }
            DetailedRiskContributorDto settle = contribMap.get("EXPOSURE_SETTLEMENTS");
            if (settle != null) {
                eNode.addChild(new ContributorTreeNodeDto(
                        settle.getId(), settle.getName(), 2, settle.getNormalizedScore(), settle.getEffectiveWeight(),
                        settle.getContribution(), settle.getContributionPercent(), settle.getImportance(), "#f97316"
                ));
            }
            DetailedRiskContributorDto infra = contribMap.get("EXPOSURE_INFRASTRUCTURE");
            if (infra != null) {
                eNode.addChild(new ContributorTreeNodeDto(
                        infra.getId(), infra.getName(), 2, infra.getNormalizedScore(), infra.getEffectiveWeight(),
                        infra.getContribution(), infra.getContributionPercent(), infra.getImportance(), "#ea580c"
                ));
            }
            root.addChild(eNode);
        }

        // 3. Vulnerability Branch
        DetailedRiskContributorDto v = contribMap.get("VULNERABILITY");
        if (v != null) {
            ContributorTreeNodeDto vNode = new ContributorTreeNodeDto(
                    "VULNERABILITY", "Vulnerability & Deprivation", 1,
                    v.getNormalizedScore(), v.getEffectiveWeight(), v.getContribution(), v.getContributionPercent(),
                    v.getImportance(), "#a855f7"
            );

            contributors.stream()
                    .filter(c -> c.getLevel() == 3 && "VULNERABILITY".equals(c.getParentPillar()))
                    .limit(3)
                    .forEach(ind -> vNode.addChild(new ContributorTreeNodeDto(
                            ind.getId(), ind.getName(), 2, ind.getNormalizedScore(), ind.getEffectiveWeight(),
                            ind.getContribution(), ind.getContributionPercent(), ind.getImportance(), "#c084fc"
                    )));

            root.addChild(vNode);
        }

        // 4. Historical Branch
        DetailedRiskContributorDto t = contribMap.get("HISTORICAL");
        if (t != null) {
            ContributorTreeNodeDto tNode = new ContributorTreeNodeDto(
                    "HISTORICAL", "Historical Disaster Evidence", 1,
                    t.getNormalizedScore(), t.getEffectiveWeight(), t.getContribution(), t.getContributionPercent(),
                    t.getImportance(), "#f59e0b"
            );
            root.addChild(tNode);
        }

        return root;
    }

    // =========================================================================
    // DYNAMIC EXPLANATION BUILDER
    // =========================================================================

    public RiskExplanationDto buildExplanation(
            DistrictRiskScoreDto risk,
            DistrictVulnerabilityScoreDto vuln,
            DistrictHistoricalSummaryDto hist,
            List<DetailedRiskContributorDto> contributors) {

        RiskExplanationDto exp = new RiskExplanationDto();

        List<DetailedRiskContributorDto> topLevel = contributors.stream()
                .filter(c -> c.getLevel() == 1)
                .sorted(Comparator.comparing(DetailedRiskContributorDto::getContribution).reversed())
                .collect(Collectors.toList());

        String dominantPillar = !topLevel.isEmpty() ? topLevel.get(0).getName() : "Hazard";
        exp.setDominantPillar(dominantPillar);

        String tierName = risk.getRiskTier().name();
        double score100 = risk.getRiskScore100() != null ? risk.getRiskScore100() : 0.0;

        // 1. Headline
        exp.setSummaryHeadline(String.format("District %s has %s Disaster Risk (%.1f/100), primarily driven by %s.",
                risk.getDistrictName(), tierName, score100, dominantPillar));

        // 2. Primary Drivers Bullet Points
        List<String> drivers = new ArrayList<>();
        for (int i = 0; i < Math.min(3, topLevel.size()); i++) {
            DetailedRiskContributorDto d = topLevel.get(i);
            drivers.add(String.format("%s accounts for %.1f%% of overall risk (contribution: %.4f)",
                    d.getName(), d.getContributionPercent(), d.getContribution()));
        }
        exp.setPrimaryDrivers(drivers);

        // 3. Exposure Highlights
        ExposureSubBreakdownDto expBreakdown = risk.getExposureSubBreakdown();
        if (expBreakdown != null) {
            List<String> expHighlights = new ArrayList<>();
            if (expBreakdown.getExposedPopulation() != null && expBreakdown.getExposedPopulation() > 0) {
                expHighlights.add(String.format("Exposed Population: %,d people (%.1f%% of district)",
                        expBreakdown.getExposedPopulation(), expBreakdown.getExposedPopulationPercentage()));
            }
            if (expBreakdown.getSettlementsExposedCount() != null) {
                expHighlights.add(String.format("Exposed Settlements: %,d habitation units in hazard zone",
                        expBreakdown.getSettlementsExposedCount()));
            }
            if (expBreakdown.getInfrastructureAssetsExposedCount() != null) {
                expHighlights.add(String.format("Lifeline Infrastructure: %,d critical assets exposed",
                        expBreakdown.getInfrastructureAssetsExposedCount()));
            }
            exp.setExposureHighlights(expHighlights);
        }

        // 4. Vulnerability Highlights
        if (vuln != null && vuln.getTopContributors() != null && !vuln.getTopContributors().isEmpty()) {
            List<String> vHighlights = new ArrayList<>();
            vuln.getTopContributors().stream().limit(3).forEach(ind -> {
                String catDisplayName = ind.getCategory() != null ? ind.getCategory().getDisplayName() : "Vulnerability";
                vHighlights.add(String.format("Vulnerability Driver: %s (%s, score: %.2f)",
                        ind.getIndicatorName(), catDisplayName, ind.getNormalizedValue()));
            });
            exp.setVulnerabilityHighlights(vHighlights);
        }

        // 5. Historical Evidence Highlights
        if (hist != null) {
            List<String> hHighlights = new ArrayList<>();
            hHighlights.add(String.format("Historical Disaster Hotspot Index: %.2f (%s)",
                    hist.getHistoricalHotspotIndex(), hist.getHotspotTier()));
            hHighlights.add(String.format("Recorded Disaster Events: %d events (recurrence frequency: %.3f events/year)",
                    hist.getTotalHistoricalEvents(), hist.getEventsPerYear()));
            exp.setHistoricalEvidenceHighlights(hHighlights);
        }

        // 6. Narrative
        StringBuilder narrative = new StringBuilder();
        narrative.append(String.format("District %s represents an overall compound disaster risk score of %.1f on a 0–100 scale, categorizing it under %s risk. ",
                risk.getDistrictName(), score100, tierName));
        narrative.append(String.format("The single largest contributor to this risk is %s (accounting for %.1f%% of total risk). ",
                dominantPillar, !topLevel.isEmpty() ? topLevel.get(0).getContributionPercent() : 0.0));

        if (topLevel.size() > 1) {
            narrative.append(String.format("This is further amplified by %s (%.1f%% share). ",
                    topLevel.get(1).getName(), topLevel.get(1).getContributionPercent()));
        }

        if (hist != null && hist.getTotalHistoricalEvents() > 0) {
            narrative.append(String.format("Empirical evidence from the archival disaster record confirms %d past disaster occurrences in this basin.",
                    hist.getTotalHistoricalEvents()));
        }

        exp.setNarrative(narrative.toString().trim());

        if (risk.getDataQuality() != null) {
            exp.setDataCompletenessNote(String.format("Data Completeness: %d/%d pillars evaluated (%.1f%% complete, status: %s).",
                    risk.getDataQuality().getAvailableComponents(), risk.getDataQuality().getConfiguredComponents(),
                    risk.getDataQuality().getCompletenessPercentage(), risk.getDataQuality().getStatus()));
        }

        return exp;
    }

    // =========================================================================
    // MATHEMATICAL CONSISTENCY VALIDATION
    // =========================================================================

    public Map<String, Object> performMathematicalValidation(DistrictRiskScoreDto risk, List<DetailedRiskContributorDto> contributors) {
        Map<String, Object> map = new LinkedHashMap<>();

        List<DetailedRiskContributorDto> topLevel = contributors.stream()
                .filter(c -> c.getLevel() == 1)
                .collect(Collectors.toList());

        double sumContributions = topLevel.stream()
                .mapToDouble(c -> c.getContribution() != null ? c.getContribution() : 0.0)
                .sum();
        double sumPercentages = topLevel.stream()
                .mapToDouble(c -> c.getContributionPercent() != null ? c.getContributionPercent() : 0.0)
                .sum();

        double riskScore = risk.getRiskScore() != null ? risk.getRiskScore() : 0.0;
        double diffContrib = Math.abs(sumContributions - riskScore);
        double diffPercent = Math.abs(sumPercentages - 100.0);

        boolean isContributionConsistent = diffContrib <= 0.005;
        boolean isPercentageConsistent = diffPercent <= 1.0;

        map.put("riskScoreTarget", round4(riskScore));
        map.put("sumTopLevelContributions", round4(sumContributions));
        map.put("contributionDelta", round4(diffContrib));
        map.put("isContributionConsistent", isContributionConsistent);

        map.put("sumTopLevelPercentages", round1(sumPercentages));
        map.put("percentageDelta", round1(diffPercent));
        map.put("isPercentageConsistent", isPercentageConsistent);

        map.put("validationStatus", (isContributionConsistent && isPercentageConsistent) ? "MATHEMATICALLY_CONSISTENT" : "WARNING_DELTA_TOLERANCE");

        return map;
    }

    public static double round4(double val) {
        return Math.round(val * 10000.0) / 10000.0;
    }

    public static double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
