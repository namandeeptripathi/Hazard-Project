package com.hazard.service.relocation.explain;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.explain.DecisionContributorDto;
import com.hazard.dto.relocation.explain.RiskExplanationDto;
import com.hazard.service.relocation.PriorityScoringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Stage 7C.2 — Risk & Priority Explanation Engine.
 *
 * Translates Stage 7A priority assessment outputs into a structured risk explanation,
 * computing granular weighted contributions, sorting risk drivers by impact, and producing
 * auditable, deterministic narrative text.
 */
@Component("relocationRiskExplanationEngine")
public class RiskExplanationEngine {

    private final PriorityScoringConfig priorityConfig;

    @Autowired
    public RiskExplanationEngine(PriorityScoringConfig priorityConfig) {
        this.priorityConfig = priorityConfig;
    }

    public RiskExplanationEngine() {
        this(new PriorityScoringConfig());
    }

    /**
     * Generates a RiskExplanationDto from a RelocationPriorityResultDto.
     */
    public RiskExplanationDto explainRisk(RelocationPriorityResultDto priorityResult) {
        RiskExplanationDto explanation = new RiskExplanationDto();

        if (priorityResult == null) {
            explanation.setPriorityScore(0.0);
            explanation.setPriorityLevel(PriorityLevel.MONITORING);
            explanation.setRiskCategory("MONITORING_LOW");
            explanation.setRiskNarrative("No priority assessment data available; default monitoring status applied.");
            explanation.setUrgencyContext("No emergency action required.");
            return explanation;
        }

        explanation.setHabitationId(priorityResult.getHabitationId());
        explanation.setHabitationName(priorityResult.getHabitationName() != null ? priorityResult.getHabitationName() : priorityResult.getHabitationId());
        explanation.setPriorityScore(priorityResult.getPriorityScore());
        explanation.setPriorityLevel(priorityResult.getPriorityLevel());

        double totalScore = (priorityResult.getPriorityScore() != null) ? priorityResult.getPriorityScore() : 0.0;
        PriorityLevel level = (priorityResult.getPriorityLevel() != null) ? priorityResult.getPriorityLevel() : PriorityLevel.MONITORING;

        // Categorize Risk
        explanation.setRiskCategory(mapRiskCategory(level));

        // Build Structured Contributors List (7C.5)
        List<DecisionContributorDto> contributors = buildPriorityContributors(priorityResult, totalScore);
        explanation.setContributors(contributors);

        // Identify Primary Risk Drivers (Top 2-3 contributors by weighted impact)
        List<DecisionContributorDto> sortedDrivers = new ArrayList<>(contributors);
        sortedDrivers.sort(Comparator.comparing(DecisionContributorDto::getWeightedImpact, Comparator.nullsLast(Comparator.reverseOrder())));

        for (int i = 0; i < Math.min(3, sortedDrivers.size()); i++) {
            DecisionContributorDto d = sortedDrivers.get(i);
            if (d.getWeightedImpact() != null && d.getWeightedImpact() > 0.02) {
                explanation.addPrimaryRiskDriver(String.format(
                        "%s (%.1f%% score contribution): %s",
                        d.getDisplayName(),
                        d.getImpactPercentage() != null ? d.getImpactPercentage() : 0.0,
                        d.getInterpretation()
                ));
            }
        }

        if (explanation.getPrimaryRiskDrivers().isEmpty()) {
            explanation.addPrimaryRiskDriver("Baseline low risk across all monitored dimensions.");
        }

        // Generate Risk Narrative
        explanation.setRiskNarrative(generateRiskNarrative(priorityResult, level, sortedDrivers));

        // Generate Urgency Context
        explanation.setUrgencyContext(generateUrgencyContext(priorityResult, level));

        return explanation;
    }

    private List<DecisionContributorDto> buildPriorityContributors(RelocationPriorityResultDto result, double totalScore) {
        List<DecisionContributorDto> list = new ArrayList<>();
        Map<String, Double> contributorMap = result.getScoringContributors();

        // 1. Risk Severity (30%)
        double normRisk = (contributorMap != null && contributorMap.containsKey(PriorityScoringConfig.RISK_SEVERITY))
                ? contributorMap.get(PriorityScoringConfig.RISK_SEVERITY)
                : (result.getRiskScore() != null ? Math.min(1.0, result.getRiskScore()) : 0.0);
        double wRisk = priorityConfig.getWeight(PriorityScoringConfig.RISK_SEVERITY);
        list.add(createContributor(
                PriorityScoringConfig.RISK_SEVERITY, "Multi-Hazard Risk Severity", "PRIORITY",
                result.getRiskScore() != null ? String.format("%.3f", result.getRiskScore()) : "0.0",
                normRisk, wRisk, totalScore,
                interpretRiskSeverity(normRisk)
        ));

        // 2. Hazard Severity (15%)
        double normHazard = (contributorMap != null && contributorMap.containsKey(PriorityScoringConfig.HAZARD_SEVERITY))
                ? contributorMap.get(PriorityScoringConfig.HAZARD_SEVERITY)
                : (result.getHazardSeverityScore() != null ? Math.min(1.0, result.getHazardSeverityScore()) : 0.0);
        double wHazard = priorityConfig.getWeight(PriorityScoringConfig.HAZARD_SEVERITY);
        list.add(createContributor(
                PriorityScoringConfig.HAZARD_SEVERITY, "Hazard Intensity & Footprint", "PRIORITY",
                result.getHazardSeverityScore() != null ? String.format("%.3f", result.getHazardSeverityScore()) : "0.0",
                normHazard, wHazard, totalScore,
                interpretHazardSeverity(normHazard, result.isRedZone())
        ));

        // 3. Population Exposure (20%)
        double normPop = (contributorMap != null && contributorMap.containsKey(PriorityScoringConfig.POPULATION_EXPOSURE))
                ? contributorMap.get(PriorityScoringConfig.POPULATION_EXPOSURE)
                : 0.0;
        double wPop = priorityConfig.getWeight(PriorityScoringConfig.POPULATION_EXPOSURE);
        list.add(createContributor(
                PriorityScoringConfig.POPULATION_EXPOSURE, "Vulnerable Population Exposure", "PRIORITY",
                result.getVulnerablePopulation() != null ? result.getVulnerablePopulation() + " people" : "0 people",
                normPop, wPop, totalScore,
                interpretPopulationExposure(result.getVulnerablePopulation())
        ));

        // 4. Capacity Deficit (15%)
        double normDeficit = (contributorMap != null && contributorMap.containsKey(PriorityScoringConfig.CAPACITY_DEFICIT))
                ? contributorMap.get(PriorityScoringConfig.CAPACITY_DEFICIT)
                : 0.0;
        double wDeficit = priorityConfig.getWeight(PriorityScoringConfig.CAPACITY_DEFICIT);
        list.add(createContributor(
                PriorityScoringConfig.CAPACITY_DEFICIT, "Shelter Capacity Deficit", "PRIORITY",
                result.getUnallocatedPopulation() != null ? result.getUnallocatedPopulation() + " unallocated" : "0 unallocated",
                normDeficit, wDeficit, totalScore,
                interpretCapacityDeficit(normDeficit, result.getUnallocatedPopulation())
        ));

        // 5. Allocation Failure (10%)
        double normAlloc = (contributorMap != null && contributorMap.containsKey(PriorityScoringConfig.ALLOCATION_FAILURE))
                ? contributorMap.get(PriorityScoringConfig.ALLOCATION_FAILURE)
                : 0.0;
        double wAlloc = priorityConfig.getWeight(PriorityScoringConfig.ALLOCATION_FAILURE);
        list.add(createContributor(
                PriorityScoringConfig.ALLOCATION_FAILURE, "Allocation Failure Penalty", "PRIORITY",
                result.getOverallStatus() != null ? result.getOverallStatus() : "NONE",
                normAlloc, wAlloc, totalScore,
                interpretAllocationStatus(result.getOverallStatus())
        ));

        // 6. Urgency (10%)
        double normUrgency = (contributorMap != null && contributorMap.containsKey(PriorityScoringConfig.URGENCY))
                ? contributorMap.get(PriorityScoringConfig.URGENCY)
                : 0.0;
        double wUrgency = priorityConfig.getWeight(PriorityScoringConfig.URGENCY);
        list.add(createContributor(
                PriorityScoringConfig.URGENCY, "Operational Relocation Urgency", "PRIORITY",
                result.getUrgency() != null ? result.getUrgency().name() : "MODERATE",
                normUrgency, wUrgency, totalScore,
                interpretUrgency(result.getUrgency())
        ));

        return list;
    }

    private DecisionContributorDto createContributor(String key, String displayName, String category,
                                                     Object rawVal, double normScore, double weight,
                                                     double totalScore, String interpretation) {
        double weightedImpact = Math.round(normScore * weight * 10000.0) / 10000.0;
        double pct = (totalScore > 0.0) ? Math.round((weightedImpact / totalScore) * 1000.0) / 10.0 : 0.0;
        DecisionContributorDto dto = new DecisionContributorDto(
                key, displayName, category, rawVal, normScore, weight, weightedImpact, interpretation
        );
        dto.setImpactPercentage(pct);
        return dto;
    }

    private String mapRiskCategory(PriorityLevel level) {
        return switch (level) {
            case IMMEDIATE -> "CRITICAL_IMMEDIATE";
            case SHORT_TERM -> "ELEVATED_SHORT_TERM";
            case MEDIUM_TERM -> "MODERATE_PLANNED";
            case MONITORING -> "MONITORING_LOW";
        };
    }

    private String generateRiskNarrative(RelocationPriorityResultDto res, PriorityLevel level, List<DecisionContributorDto> drivers) {
        String habName = res.getHabitationName() != null ? res.getHabitationName() : "Origin Habitation";
        String topDriver = !drivers.isEmpty() ? drivers.get(0).getDisplayName() : "Hazard Exposure";

        return switch (level) {
            case IMMEDIATE -> String.format(
                    "%s is classified under IMMEDIATE priority (Composite Priority Score: %.2f/1.00) due to acute life-safety risk driven predominantly by %s.",
                    habName, res.getPriorityScore() != null ? res.getPriorityScore() : 0.0, topDriver
            );
            case SHORT_TERM -> String.format(
                    "%s requires near-term SHORT_TERM relocation intervention (Priority Score: %.2f/1.00), primarily influenced by %s.",
                    habName, res.getPriorityScore() != null ? res.getPriorityScore() : 0.0, topDriver
            );
            case MEDIUM_TERM -> String.format(
                    "%s is designated for MEDIUM_TERM planned relocation (Priority Score: %.2f/1.00) with moderate hazard exposure and manageable shelter requirements.",
                    habName, res.getPriorityScore() != null ? res.getPriorityScore() : 0.0
            );
            case MONITORING -> String.format(
                    "%s is categorized under MONITORING status (Priority Score: %.2f/1.00); current hazard risks are low and shelter accommodation is stable.",
                    habName, res.getPriorityScore() != null ? res.getPriorityScore() : 0.0
            );
        };
    }

    private String generateUrgencyContext(RelocationPriorityResultDto res, PriorityLevel level) {
        return switch (level) {
            case IMMEDIATE -> "Emergency action required immediately. Evacuation mobilization should take precedence within 0-6 hours.";
            case SHORT_TERM -> "Action required within 12-24 hours. Pre-stage logistics and coordinate shelter staging.";
            case MEDIUM_TERM -> "Scheduled relocation within 24-72 hours. Standard emergency operational protocol applies.";
            case MONITORING -> "No immediate evacuation needed. Maintain active weather and flood level monitoring.";
        };
    }

    private String interpretRiskSeverity(double norm) {
        if (norm >= 0.70) return "Severe multi-hazard vulnerability index represents acute threat to life.";
        if (norm >= 0.40) return "Elevated multi-hazard risk requires structured mitigation.";
        if (norm >= 0.15) return "Moderate risk exposure within tolerable thresholds.";
        return "Low risk severity index observed.";
    }

    private String interpretHazardSeverity(double norm, boolean redZone) {
        if (redZone) return "Direct red-zone hazard exposure inside the active flood inundation footprint.";
        if (norm >= 0.70) return "High hazard intensity indicates deep water depth or extreme rainfall.";
        if (norm >= 0.40) return "Moderate hazard exposure in peripheral inundation buffer.";
        return "Minimal direct hazard intensity recorded.";
    }

    private String interpretPopulationExposure(Long pop) {
        if (pop == null || pop <= 0) return "No vulnerable population requiring relocation.";
        if (pop >= 10000) return String.format("High population exposure (%d evacuees) magnifies evacuation complexity.", pop);
        if (pop >= 1000) return String.format("Significant population exposure (%d evacuees) requiring dedicated transport.", pop);
        return String.format("Localized group of %d vulnerable evacuees.", pop);
    }

    private String interpretCapacityDeficit(double normDeficit, Long unalloc) {
        if (normDeficit >= 0.90) return "Severe capacity deficit: almost the entire population cannot be accommodated locally.";
        if (normDeficit > 0.0) return String.format("Partial capacity deficit: %d evacuees remain unallocated.", unalloc != null ? unalloc : 0);
        return "Zero capacity deficit: 100% of evacuees can be accommodated.";
    }

    private String interpretAllocationStatus(String status) {
        if ("UNALLOCATED_NO_SAFE_SITE".equals(status)) return "Critical allocation failure: no feasible safe site available within transit radius.";
        if ("UNALLOCATED_CAPACITY_EXCEEDED".equals(status)) return "Allocation failure: shelter capacity exhausted across candidate shelters.";
        if ("PARTIALLY_ALLOCATED".equals(status)) return "Partial allocation: shelter capacity exhausted before full cohort was housed.";
        if ("ALLOCATED".equals(status)) return "Successful allocation: full vulnerable population assigned to safe shelter.";
        return "Relocation allocation pending evaluation.";
    }

    private String interpretUrgency(com.hazard.domain.relocation.RelocationUrgency urgency) {
        if (urgency == null) return "Standard urgency baseline.";
        return switch (urgency) {
            case CRITICAL -> "Highest operational urgency: immediate life safety hazard declared.";
            case HIGH -> "High operational urgency: rapid mobilization advised.";
            case MODERATE -> "Moderate operational urgency: proactive relocation.";
            case LOW -> "Low operational urgency: precautionary posture.";
        };
    }
}
