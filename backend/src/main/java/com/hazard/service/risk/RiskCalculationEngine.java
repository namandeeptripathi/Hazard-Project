package com.hazard.service.risk;

import com.hazard.domain.risk.RiskComponentType;
import com.hazard.domain.risk.RiskDataCompletenessStatus;
import com.hazard.domain.risk.RiskTier;
import com.hazard.dto.risk.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure calculation and aggregation engine for Stage 4.7 — Risk Calculation.
 * Synthesizes Hazard (Stage 3), Exposure (4.1-4.3), Vulnerability (4.5), and Historical Evidence (4.6)
 * into final disaster risk scores [0.0000, 1.0000] and [0, 100], component contributions, and ranked drivers.
 */
@Component
public class RiskCalculationEngine {

    private final RiskCalculationConfig config;

    public RiskCalculationEngine(RiskCalculationConfig config) {
        this.config = config;
    }

    /**
     * Aggregates population, settlement, and infrastructure exposure scores into a combined exposure score.
     */
    public ExposureSubBreakdownDto calculateCombinedExposure(Double popScore,
                                                             Double settleScore,
                                                             Double infraScore,
                                                             Long exposedPop,
                                                             Double exposedPopPct,
                                                             Integer settleCount,
                                                             Integer infraCount,
                                                             Map<String, Double> customExposureWeights) {
        Map<String, Double> weights = (customExposureWeights != null && !customExposureWeights.isEmpty())
                ? customExposureWeights
                : config.getDefaultExposureSubWeights();

        double wPop = weights.getOrDefault("POPULATION", 0.40);
        double wSettle = weights.getOrDefault("SETTLEMENT", 0.25);
        double wInfra = weights.getOrDefault("INFRASTRUCTURE", 0.35);

        double activeSum = 0.0;
        double weightedValSum = 0.0;

        if (popScore != null) {
            activeSum += wPop;
            weightedValSum += (wPop * popScore);
        }
        if (settleScore != null) {
            activeSum += wSettle;
            weightedValSum += (wSettle * settleScore);
        }
        if (infraScore != null) {
            activeSum += wInfra;
            weightedValSum += (wInfra * infraScore);
        }

        if (activeSum <= 0.0) {
            activeSum = 1.0;
        }

        double combinedExposure = round4(Math.min(1.0, Math.max(0.0, weightedValSum / activeSum)));

        ExposureSubBreakdownDto dto = new ExposureSubBreakdownDto();
        dto.setPopulationExposureScore(popScore != null ? round4(popScore) : 0.0);
        dto.setPopulationConfiguredWeight(wPop);
        dto.setPopulationContribution(popScore != null ? round4((wPop / activeSum) * popScore) : 0.0);
        dto.setExposedPopulation(exposedPop != null ? exposedPop : 0L);
        dto.setExposedPopulationPercentage(exposedPopPct != null ? round1(exposedPopPct) : 0.0);

        dto.setSettlementExposureScore(settleScore != null ? round4(settleScore) : 0.0);
        dto.setSettlementConfiguredWeight(wSettle);
        dto.setSettlementContribution(settleScore != null ? round4((wSettle / activeSum) * settleScore) : 0.0);
        dto.setSettlementsExposedCount(settleCount != null ? settleCount : 0);

        dto.setInfrastructureExposureScore(infraScore != null ? round4(infraScore) : 0.0);
        dto.setInfrastructureConfiguredWeight(wInfra);
        dto.setInfrastructureContribution(infraScore != null ? round4((wInfra / activeSum) * infraScore) : 0.0);
        dto.setInfrastructureAssetsExposedCount(infraCount != null ? infraCount : 0);

        dto.setCombinedExposureScore(combinedExposure);
        dto.setCombinedExposureScore100(round1(combinedExposure * 100.0));

        return dto;
    }

    /**
     * Synthesizes Hazard, Combined Exposure, Vulnerability, and Historical Evidence into final disaster risk.
     */
    public DistrictRiskScoreDto calculateDistrictRisk(String districtName,
                                                      Double hazardScore,
                                                      Double exposureScore,
                                                      Double vulnScore,
                                                      Double histScore,
                                                      ExposureSubBreakdownDto exposureBreakdown,
                                                      Map<RiskComponentType, Double> customWeights,
                                                      Map<String, String> sourceDescriptions) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District identifier cannot be null or empty");
        }

        Map<RiskComponentType, Double> activeWeights = (customWeights != null && !customWeights.isEmpty())
                ? customWeights
                : config.getDefaultRiskComponentWeights();

        config.validateComponentWeights(activeWeights);

        DistrictRiskScoreDto result = new DistrictRiskScoreDto();
        result.setGeographicId(districtName);
        result.setDistrictName(districtName);
        result.setExposureSubBreakdown(exposureBreakdown);

        // 1. Data Completeness Accounting
        int configuredCount = 4;
        int availableCount = 0;
        if (hazardScore != null) availableCount++;
        if (exposureScore != null) availableCount++;
        if (vulnScore != null) availableCount++;
        if (histScore != null) availableCount++;

        double completenessRatio = round4((double) availableCount / configuredCount);
        RiskDataQualityDto dq = new RiskDataQualityDto();
        dq.setConfiguredComponents(configuredCount);
        dq.setAvailableComponents(availableCount);
        dq.setUnavailableComponents(configuredCount - availableCount);
        dq.setCompletenessRatio(completenessRatio);
        dq.setCompletenessPercentage(round1(completenessRatio * 100.0));

        if (availableCount == 4) {
            dq.setStatus(RiskDataCompletenessStatus.DATA_COMPLETE);
        } else if (availableCount >= 2) {
            dq.setStatus(RiskDataCompletenessStatus.DATA_PARTIAL);
        } else {
            dq.setStatus(RiskDataCompletenessStatus.INSUFFICIENT_DATA);
        }
        result.setDataQuality(dq);

        if (availableCount < 2) {
            result.setRiskScore(0.0);
            result.setRiskScore100(0.0);
            result.setRiskTier(RiskTier.LOW);
            result.setExplanation(String.format("Insufficient data: only %d of 4 risk pillars available for district %s", availableCount, districtName));
            return result;
        }

        // 2. Active Weight Sum (Missing Component Redistribution)
        double activeWeightSum = 0.0;
        if (hazardScore != null) activeWeightSum += activeWeights.getOrDefault(RiskComponentType.HAZARD, 0.35);
        if (exposureScore != null) activeWeightSum += activeWeights.getOrDefault(RiskComponentType.EXPOSURE, 0.30);
        if (vulnScore != null) activeWeightSum += activeWeights.getOrDefault(RiskComponentType.VULNERABILITY, 0.25);
        if (histScore != null) activeWeightSum += activeWeights.getOrDefault(RiskComponentType.HISTORICAL, 0.10);

        if (activeWeightSum <= 0.0) {
            activeWeightSum = 1.0;
        }

        // 3. Component Details & Contributions
        Map<String, RiskComponentDetailDto> componentsMap = new LinkedHashMap<>();
        List<RiskContributorDto> contributorList = new ArrayList<>();
        double totalWeightedRisk = 0.0;

        // A. HAZARD PILLAR
        if (hazardScore != null) {
            double rawW = activeWeights.getOrDefault(RiskComponentType.HAZARD, 0.35);
            double effW = round4(rawW / activeWeightSum);
            double contrib = round4(effW * hazardScore);
            totalWeightedRisk += (rawW * hazardScore);

            RiskComponentDetailDto hDto = createComponentDto(RiskComponentType.HAZARD, hazardScore, rawW, effW, contrib,
                    sourceDescriptions != null ? sourceDescriptions.getOrDefault("HAZARD", "Stage 3 Hazard Score") : "Stage 3 Multi-Hazard Intelligence");
            componentsMap.put("HAZARD", hDto);

            contributorList.add(new RiskContributorDto("Hazard Severity & Intensity", "HAZARD", round4(hazardScore), effW, contrib,
                    "Current active multi-hazard intensity footprint in district"));
        }

        // B. EXPOSURE PILLAR
        if (exposureScore != null) {
            double rawW = activeWeights.getOrDefault(RiskComponentType.EXPOSURE, 0.30);
            double effW = round4(rawW / activeWeightSum);
            double contrib = round4(effW * exposureScore);
            totalWeightedRisk += (rawW * exposureScore);

            RiskComponentDetailDto eDto = createComponentDto(RiskComponentType.EXPOSURE, exposureScore, rawW, effW, contrib,
                    "Stages 4.1-4.3 Combined Exposure (Pop, Settlements, Infrastructure)");
            componentsMap.put("EXPOSURE", eDto);

            if (exposureBreakdown != null) {
                if (exposureBreakdown.getPopulationContribution() != null && exposureBreakdown.getPopulationContribution() > 0) {
                    double popContrib = round4(effW * (exposureBreakdown.getPopulationConfiguredWeight() / 1.0) * exposureBreakdown.getPopulationExposureScore());
                    contributorList.add(new RiskContributorDto("Population Exposure", "EXPOSURE", exposureBreakdown.getPopulationExposureScore(), round4(effW * 0.40), popContrib,
                            "Population physically located inside hazard exposure zone"));
                }
                if (exposureBreakdown.getInfrastructureContribution() != null && exposureBreakdown.getInfrastructureContribution() > 0) {
                    double infraContrib = round4(effW * (exposureBreakdown.getInfrastructureConfiguredWeight() / 1.0) * exposureBreakdown.getInfrastructureExposureScore());
                    contributorList.add(new RiskContributorDto("Infrastructure Exposure", "EXPOSURE", exposureBreakdown.getInfrastructureExposureScore(), round4(effW * 0.35), infraContrib,
                            "Critical lifeline and hydraulic assets exposed to hazard"));
                }
            }
        }

        // C. VULNERABILITY PILLAR
        if (vulnScore != null) {
            double rawW = activeWeights.getOrDefault(RiskComponentType.VULNERABILITY, 0.25);
            double effW = round4(rawW / activeWeightSum);
            double contrib = round4(effW * vulnScore);
            totalWeightedRisk += (rawW * vulnScore);

            RiskComponentDetailDto vDto = createComponentDto(RiskComponentType.VULNERABILITY, vulnScore, rawW, effW, contrib,
                    "Stage 4.5 Composite Vulnerability Score (10 Susceptibility Drivers)");
            componentsMap.put("VULNERABILITY", vDto);

            contributorList.add(new RiskContributorDto("Vulnerability & Deprivation", "VULNERABILITY", round4(vulnScore), effW, contrib,
                    "Baseline demographic, accessibility, and structural susceptibility"));
        }

        // D. HISTORICAL EVIDENCE PILLAR
        if (histScore != null) {
            double rawW = activeWeights.getOrDefault(RiskComponentType.HISTORICAL, 0.10);
            double effW = round4(rawW / activeWeightSum);
            double contrib = round4(effW * histScore);
            totalWeightedRisk += (rawW * histScore);

            RiskComponentDetailDto tDto = createComponentDto(RiskComponentType.HISTORICAL, histScore, rawW, effW, contrib,
                    "Stage 4.6 Historical Disaster Hotspot Index (DFO & Weather Archive)");
            componentsMap.put("HISTORICAL", tDto);

            contributorList.add(new RiskContributorDto("Historical Recurrence Hotspot", "HISTORICAL", round4(histScore), effW, contrib,
                    "Recorded empirical disaster recurrence and past severity in archive"));
        }

        result.setComponents(componentsMap);

        // 4. Final Risk Score: R = Sum(w_i * x_i) / Sum(w_i)
        double finalRisk = round4(Math.min(1.0, Math.max(0.0, totalWeightedRisk / activeWeightSum)));
        double finalRisk100 = round1(finalRisk * 100.0);
        RiskTier tier = RiskTier.fromScore(finalRisk);

        result.setRiskScore(finalRisk);
        result.setRiskScore100(finalRisk100);
        result.setRiskTier(tier);

        // Sort contributors descending by contribution
        contributorList.sort(Comparator.comparingDouble(RiskContributorDto::getContribution).reversed());
        result.setTopContributors(contributorList.stream().limit(5).collect(Collectors.toList()));

        // Explanation Summary
        String primaryDriver = contributorList.isEmpty() ? "N/A" : contributorList.get(0).getName();
        result.setExplanation(String.format("District %s has overall disaster risk score %.4f (%.1f/100, %s). " +
                "Primary risk driver is %s (contribution: %.4f). Data completeness: %d/4 pillars (%.0f%%).",
                districtName, finalRisk, finalRisk100, tier.getDisplayName(), primaryDriver,
                contributorList.isEmpty() ? 0.0 : contributorList.get(0).getContribution(),
                availableCount, dq.getCompletenessPercentage()));

        return result;
    }

    private RiskComponentDetailDto createComponentDto(RiskComponentType type, double score, double configW, double effW, double contrib, String sourceSummary) {
        RiskComponentDetailDto dto = new RiskComponentDetailDto();
        dto.setComponentType(type);
        dto.setComponentName(type.getDisplayName());
        dto.setScore(round4(score));
        dto.setScore100(round1(score * 100.0));
        dto.setConfiguredWeight(configW);
        dto.setEffectiveWeight(effW);
        dto.setContribution(contrib);
        dto.setColorHex(type.getColorHex());
        dto.setStatus("AVAILABLE");
        dto.setSourceSummary(sourceSummary);
        return dto;
    }

    public static double round4(double val) {
        return Math.round(val * 10000.0) / 10000.0;
    }

    public static double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
