package com.hazard.service.relocation;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.dto.relocation.RelocationPlanDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage 7A.3 — Priority Scoring Engine.
 *
 * Computes a deterministic, composite priority score for a single relocation case
 * using a transparent weighted scoring approach. All contributors are normalized to
 * [0.0, 1.0] before weighting to prevent any single factor from dominating due to scale.
 *
 * <p>Final score formula:
 * <pre>
 *   priorityScore = Σ(weight_i × normalizedContributor_i)
 *   where i ∈ {riskSeverity, hazardSeverity, populationExposure, capacityDeficit, allocationFailure, urgency}
 * </pre>
 *
 * <p>Score range: [0.0, 1.0] (clamped)
 */
@Component
public class PriorityScoringEngine {

    private static final Logger log = LoggerFactory.getLogger(PriorityScoringEngine.class);

    private final PriorityScoringConfig config;
    private final PriorityClassificationEngine classificationEngine;

    public PriorityScoringEngine(PriorityScoringConfig config, PriorityClassificationEngine classificationEngine) {
        this.config = config;
        this.classificationEngine = classificationEngine;
    }

    /**
     * Default constructor for isolated unit testing.
     */
    public PriorityScoringEngine() {
        this.config = new PriorityScoringConfig();
        this.classificationEngine = new PriorityClassificationEngine(this.config);
    }

    /**
     * Scores and classifies a single relocation case.
     *
     * @param plan        the relocation plan from Stage 6
     * @param habitation  the vulnerable habitation from Stage 6
     * @return a fully populated RelocationPriorityResultDto with score, level, and contributor breakdown
     */
    public RelocationPriorityResultDto score(RelocationPlanDto plan, VulnerableHabitationDto habitation) {
        Map<String, Double> contributors = new LinkedHashMap<>();

        // --- Normalize each contributor to [0.0, 1.0] ---

        // 1. Risk Severity
        double riskSeverity = normalizeRiskSeverity(habitation);
        contributors.put(PriorityScoringConfig.RISK_SEVERITY, riskSeverity);

        // 2. Hazard Severity
        double hazardSeverity = normalizeHazardSeverity(habitation);
        contributors.put(PriorityScoringConfig.HAZARD_SEVERITY, hazardSeverity);

        // 3. Population Exposure (log-scaled)
        double populationExposure = normalizePopulationExposure(habitation);
        contributors.put(PriorityScoringConfig.POPULATION_EXPOSURE, populationExposure);

        // 4. Capacity Deficit Rate
        double capacityDeficit = normalizeCapacityDeficit(plan, habitation);
        contributors.put(PriorityScoringConfig.CAPACITY_DEFICIT, capacityDeficit);

        // 5. Allocation Failure Penalty
        double allocationFailure = normalizeAllocationFailure(plan);
        contributors.put(PriorityScoringConfig.ALLOCATION_FAILURE, allocationFailure);

        // 6. Urgency
        double urgency = normalizeUrgency(habitation);
        contributors.put(PriorityScoringConfig.URGENCY, urgency);

        // --- Compute weighted sum ---
        double rawScore = 0.0;
        for (Map.Entry<String, Double> entry : contributors.entrySet()) {
            double weight = config.getWeight(entry.getKey());
            rawScore += weight * entry.getValue();
        }

        // Clamp to [0.0, 1.0]
        double priorityScore = clamp(rawScore, 0.0, 1.0);

        // Round to 4 decimal places for determinism
        priorityScore = Math.round(priorityScore * 10000.0) / 10000.0;

        // --- Classify ---
        PriorityLevel level = classificationEngine.classify(priorityScore);

        // --- Build result DTO ---
        RelocationPriorityResultDto result = new RelocationPriorityResultDto();
        result.setPriorityScore(priorityScore);
        result.setPriorityLevel(level);
        result.setScoringContributors(contributors);

        // Preserve identifiers from habitation
        if (habitation != null) {
            result.setHabitationId(habitation.getHabitationId());
            result.setHabitationName(habitation.getHabitationName());
            result.setDistrict(habitation.getDistrict());
            result.setState(habitation.getState());
            result.setVulnerablePopulation(habitation.getVulnerablePopulation());
            result.setUrgency(habitation.getUrgency());
            result.setRiskScore(habitation.getRiskScore());
            result.setHazardSeverityScore(habitation.getHazardSeverityScore());
            result.setRedZone(habitation.isRedZone());
        }

        // Preserve plan-level context
        if (plan != null) {
            result.setPlanId(plan.getPlanId());
            result.setUnallocatedPopulation(plan.getTotalUnallocatedPopulation());
            result.setAllocationRatePercentage(plan.getAllocationRatePercentage());
            result.setOverallStatus(plan.getOverallStatus() != null ? plan.getOverallStatus().name() : null);
        }

        return result;
    }

    // --- Normalization Methods ---

    /**
     * Normalizes risk severity score. Already in [0.0, 1.0] from Stage 4.
     * Missing values default to 0.0 (conservative — does not inflate priority).
     */
    public double normalizeRiskSeverity(VulnerableHabitationDto habitation) {
        if (habitation == null || habitation.getRiskScore() == null) {
            return 0.0;
        }
        return clamp(habitation.getRiskScore(), 0.0, 1.0);
    }

    /**
     * Normalizes hazard severity score. Already in [0.0, 1.0] from Stage 3.
     */
    public double normalizeHazardSeverity(VulnerableHabitationDto habitation) {
        if (habitation == null || habitation.getHazardSeverityScore() == null) {
            return 0.0;
        }
        return clamp(habitation.getHazardSeverityScore(), 0.0, 1.0);
    }

    /**
     * Normalizes vulnerable population using log-scale to prevent extreme populations
     * from dominating the score.
     *
     * Formula: min(1.0, log10(population + 1) / log10(CAP + 1))
     * where CAP = 100,000 (configurable via PriorityScoringConfig).
     */
    public double normalizePopulationExposure(VulnerableHabitationDto habitation) {
        if (habitation == null || habitation.getVulnerablePopulation() == null
                || habitation.getVulnerablePopulation() <= 0) {
            return 0.0;
        }
        long pop = habitation.getVulnerablePopulation();
        long cap = config.getPopulationNormalizationCap();
        double normalized = Math.log10(pop + 1.0) / Math.log10(cap + 1.0);
        return Math.min(1.0, normalized);
    }

    /**
     * Normalizes capacity deficit as a fraction of vulnerable population.
     * Formula: unallocatedPopulation / max(1, vulnerablePopulation)
     */
    public double normalizeCapacityDeficit(RelocationPlanDto plan, VulnerableHabitationDto habitation) {
        if (plan == null || habitation == null) {
            return 0.0;
        }

        long unallocated = (plan.getTotalUnallocatedPopulation() != null)
                ? plan.getTotalUnallocatedPopulation() : 0L;
        long vulnerable = (habitation.getVulnerablePopulation() != null && habitation.getVulnerablePopulation() > 0)
                ? habitation.getVulnerablePopulation() : 1L;

        double deficit = (double) unallocated / (double) vulnerable;
        return clamp(deficit, 0.0, 1.0);
    }

    /**
     * Maps RelocationStatus to an ordinal failure score.
     */
    public double normalizeAllocationFailure(RelocationPlanDto plan) {
        if (plan == null || plan.getOverallStatus() == null) {
            return config.getAllocationFailureScore(null);
        }
        return config.getAllocationFailureScore(plan.getOverallStatus());
    }

    /**
     * Maps RelocationUrgency to an ordinal urgency score.
     */
    public double normalizeUrgency(VulnerableHabitationDto habitation) {
        if (habitation == null || habitation.getUrgency() == null) {
            return config.getUrgencyScore(null);
        }
        return config.getUrgencyScore(habitation.getUrgency());
    }

    /**
     * Clamps a value to [min, max].
     */
    static double clamp(double value, double min, double max) {
        if (Double.isNaN(value)) return min;
        return Math.max(min, Math.min(max, value));
    }
}
