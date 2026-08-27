package com.hazard.service.relocation;

import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage 7A.1 — Priority Scoring Configuration.
 *
 * Centralizes all scoring weights, classification thresholds, and normalization parameters
 * for the Priority Engine. Follows the established pattern of {@link com.hazard.service.risk.RiskCalculationConfig}.
 *
 * <p>Scoring Contributors and Weights (sum = 1.00):
 * <ul>
 *   <li>RISK_SEVERITY: 0.30 — Composite risk score from Stage 4</li>
 *   <li>HAZARD_SEVERITY: 0.15 — Raw hazard intensity from Stage 3</li>
 *   <li>POPULATION_EXPOSURE: 0.20 — Log-scaled vulnerable population</li>
 *   <li>CAPACITY_DEFICIT: 0.15 — Fraction of population without shelter</li>
 *   <li>ALLOCATION_FAILURE: 0.10 — Ordinal penalty from allocation status</li>
 *   <li>URGENCY: 0.10 — Ordinal mapping from RelocationUrgency</li>
 * </ul>
 */
@Component
public class PriorityScoringConfig {

    // --- Scoring Contributor Identifiers ---
    public static final String RISK_SEVERITY = "RISK_SEVERITY";
    public static final String HAZARD_SEVERITY = "HAZARD_SEVERITY";
    public static final String POPULATION_EXPOSURE = "POPULATION_EXPOSURE";
    public static final String CAPACITY_DEFICIT = "CAPACITY_DEFICIT";
    public static final String ALLOCATION_FAILURE = "ALLOCATION_FAILURE";
    public static final String URGENCY = "URGENCY";

    // --- Weights ---
    private final Map<String, Double> contributorWeights = new LinkedHashMap<>();

    // --- Classification Thresholds ---
    private final double immediateThreshold = 0.70;
    private final double shortTermThreshold = 0.40;
    private final double mediumTermThreshold = 0.15;

    // --- Normalization Parameters ---
    private final long populationNormalizationCap = 100_000L;

    // --- Allocation Failure Ordinal Scores ---
    private final Map<RelocationStatus, Double> allocationFailureScores = new LinkedHashMap<>();

    // --- Urgency Ordinal Scores ---
    private final Map<RelocationUrgency, Double> urgencyScores = new LinkedHashMap<>();

    public PriorityScoringConfig() {
        // Contributor Weights (sum = 1.00)
        contributorWeights.put(RISK_SEVERITY, 0.30);
        contributorWeights.put(HAZARD_SEVERITY, 0.15);
        contributorWeights.put(POPULATION_EXPOSURE, 0.20);
        contributorWeights.put(CAPACITY_DEFICIT, 0.15);
        contributorWeights.put(ALLOCATION_FAILURE, 0.10);
        contributorWeights.put(URGENCY, 0.10);

        // Allocation Failure Ordinal Mapping
        allocationFailureScores.put(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, 1.0);
        allocationFailureScores.put(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED, 0.8);
        allocationFailureScores.put(RelocationStatus.PARTIALLY_ALLOCATED, 0.4);
        allocationFailureScores.put(RelocationStatus.PENDING, 0.2);
        allocationFailureScores.put(RelocationStatus.ALLOCATED, 0.0);

        // Urgency Ordinal Mapping
        urgencyScores.put(RelocationUrgency.CRITICAL, 1.0);
        urgencyScores.put(RelocationUrgency.HIGH, 0.67);
        urgencyScores.put(RelocationUrgency.MODERATE, 0.33);
        urgencyScores.put(RelocationUrgency.LOW, 0.0);
    }

    public Map<String, Double> getContributorWeights() {
        return Collections.unmodifiableMap(contributorWeights);
    }

    public double getWeight(String contributor) {
        return contributorWeights.getOrDefault(contributor, 0.0);
    }

    public double getImmediateThreshold() {
        return immediateThreshold;
    }

    public double getShortTermThreshold() {
        return shortTermThreshold;
    }

    public double getMediumTermThreshold() {
        return mediumTermThreshold;
    }

    public long getPopulationNormalizationCap() {
        return populationNormalizationCap;
    }

    public double getAllocationFailureScore(RelocationStatus status) {
        if (status == null) {
            return allocationFailureScores.getOrDefault(RelocationStatus.PENDING, 0.2);
        }
        return allocationFailureScores.getOrDefault(status, 0.2);
    }

    public double getUrgencyScore(RelocationUrgency urgency) {
        if (urgency == null) {
            return urgencyScores.getOrDefault(RelocationUrgency.MODERATE, 0.33);
        }
        return urgencyScores.getOrDefault(urgency, 0.33);
    }

    public Map<RelocationStatus, Double> getAllocationFailureScores() {
        return Collections.unmodifiableMap(allocationFailureScores);
    }

    public Map<RelocationUrgency, Double> getUrgencyScores() {
        return Collections.unmodifiableMap(urgencyScores);
    }

    /**
     * Validates that contributor weights sum to a positive value and contain no invalid entries.
     */
    public void validateWeights() {
        if (contributorWeights.isEmpty()) {
            throw new IllegalArgumentException("Priority scoring contributor weights cannot be empty");
        }
        double sum = 0.0;
        for (Map.Entry<String, Double> entry : contributorWeights.entrySet()) {
            Double w = entry.getValue();
            if (w == null || Double.isNaN(w) || Double.isInfinite(w) || w < 0.0) {
                throw new IllegalArgumentException(
                        "Invalid weight for contributor " + entry.getKey() + ": " + w + " (must be non-negative and finite)");
            }
            sum += w;
        }
        if (sum <= 0.0) {
            throw new IllegalArgumentException("Total priority scoring weight sum must be strictly greater than 0.0");
        }
    }
}
