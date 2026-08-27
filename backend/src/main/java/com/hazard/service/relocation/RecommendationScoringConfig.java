package com.hazard.service.relocation;

import com.hazard.domain.safesite.HealthcareAccessStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.WaterAccessStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage 7B.1 & 7B.4 — Destination Recommendation Scoring Configuration.
 *
 * Centralizes all scoring weights, normalization bounds, and ordinal parameters
 * for evaluating and ranking feasible candidate safe site destinations:
 * <ul>
 *   <li>SUITABILITY_QUALITY (35%): Candidate multi-criteria suitability score and tier classification.</li>
 *   <li>TRANSIT_PROXIMITY (30%): Geodesic distance decay relative to maximum acceptable transit radius.</li>
 *   <li>CAPACITY_FIT (20%): Shelter headroom and capacity sufficiency for the evacuee population.</li>
 *   <li>ACCESS_RELIABILITY (15%): Road, water, and healthcare accessibility reliability.</li>
 * </ul>
 */
@Component
public class RecommendationScoringConfig {

    // --- Scoring Contributor Keys ---
    public static final String SUITABILITY_QUALITY = "SUITABILITY_QUALITY";
    public static final String TRANSIT_PROXIMITY = "TRANSIT_PROXIMITY";
    public static final String CAPACITY_FIT = "CAPACITY_FIT";
    public static final String ACCESS_RELIABILITY = "ACCESS_RELIABILITY";

    // --- Weights (sum = 1.00) ---
    private final Map<String, Double> contributorWeights = new LinkedHashMap<>();

    // --- Distance Decay Parameters ---
    private final double defaultMaxTransitRadiusKm = 50.0;

    // --- Access Ordinal Scores ---
    private final Map<RoadAccessStatus, Double> roadAccessScores = new LinkedHashMap<>();
    private final Map<WaterAccessStatus, Double> waterAccessScores = new LinkedHashMap<>();
    private final Map<HealthcareAccessStatus, Double> healthcareAccessScores = new LinkedHashMap<>();

    public RecommendationScoringConfig() {
        // Contributor Weights (sum = 1.00)
        contributorWeights.put(SUITABILITY_QUALITY, 0.35);
        contributorWeights.put(TRANSIT_PROXIMITY, 0.30);
        contributorWeights.put(CAPACITY_FIT, 0.20);
        contributorWeights.put(ACCESS_RELIABILITY, 0.15);

        // Road Access Scores
        roadAccessScores.put(RoadAccessStatus.NEAR, 1.0);
        roadAccessScores.put(RoadAccessStatus.MODERATE, 0.6);
        roadAccessScores.put(RoadAccessStatus.FAR, 0.2);
        roadAccessScores.put(RoadAccessStatus.UNKNOWN, 0.4);

        // Water Access Scores
        waterAccessScores.put(WaterAccessStatus.NEAR, 1.0);
        waterAccessScores.put(WaterAccessStatus.MODERATE, 0.6);
        waterAccessScores.put(WaterAccessStatus.FAR, 0.2);
        waterAccessScores.put(WaterAccessStatus.UNKNOWN, 0.4);

        // Healthcare Access Scores
        healthcareAccessScores.put(HealthcareAccessStatus.NEAR, 1.0);
        healthcareAccessScores.put(HealthcareAccessStatus.MODERATE, 0.6);
        healthcareAccessScores.put(HealthcareAccessStatus.FAR, 0.2);
        healthcareAccessScores.put(HealthcareAccessStatus.UNKNOWN, 0.4);
    }

    public Map<String, Double> getContributorWeights() {
        return Collections.unmodifiableMap(contributorWeights);
    }

    public double getWeight(String contributor) {
        return contributorWeights.getOrDefault(contributor, 0.0);
    }

    public double getDefaultMaxTransitRadiusKm() {
        return defaultMaxTransitRadiusKm;
    }

    public double getRoadAccessScore(RoadAccessStatus status) {
        if (status == null) return 0.4;
        return roadAccessScores.getOrDefault(status, 0.4);
    }

    public double getWaterAccessScore(WaterAccessStatus status) {
        if (status == null) return 0.4;
        return waterAccessScores.getOrDefault(status, 0.4);
    }

    public double getHealthcareAccessScore(HealthcareAccessStatus status) {
        if (status == null) return 0.4;
        return healthcareAccessScores.getOrDefault(status, 0.4);
    }

    /**
     * Validates that contributor weights sum to > 0.0 and contain no invalid values.
     */
    public void validateWeights() {
        if (contributorWeights.isEmpty()) {
            throw new IllegalArgumentException("Destination scoring contributor weights cannot be empty");
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
            throw new IllegalArgumentException("Total destination scoring weight sum must be strictly greater than 0.0");
        }
    }
}
