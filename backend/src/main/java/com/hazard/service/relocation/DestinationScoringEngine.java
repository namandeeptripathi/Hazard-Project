package com.hazard.service.relocation;

import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage 7B.4 — Destination Recommendation Scoring Engine.
 *
 * Computes a transparent, deterministic composite suitability score for an evaluated
 * candidate safe site destination across four weighted contributors:
 * 1. SUITABILITY_QUALITY (35%): Stage 5.10 multi-criteria site suitability score/class.
 * 2. TRANSIT_PROXIMITY (30%): Geodesic distance decay relative to maximum radius.
 * 3. CAPACITY_FIT (20%): Shelter headroom and capacity sufficiency for vulnerable evacuees.
 * 4. ACCESS_RELIABILITY (15%): Road, water, and healthcare accessibility reliability.
 *
 * Score is strictly bounded in [0.0, 1.0] and rounded to 4 decimal places for determinism.
 */
@Component
public class DestinationScoringEngine {

    private static final Logger log = LoggerFactory.getLogger(DestinationScoringEngine.class);

    private final RecommendationScoringConfig config;

    @Autowired
    public DestinationScoringEngine(RecommendationScoringConfig config) {
        this.config = config;
    }

    public DestinationScoringEngine() {
        this(new RecommendationScoringConfig());
    }

    /**
     * Computes the composite destination score for an evaluated candidate destination.
     *
     * @param habitation the origin vulnerable habitation
     * @param dest       the evaluated destination DTO
     * @param rawSite    optional raw CandidateSafeSiteDto containing access dimensions
     * @param maxRadiusKm optional maximum transit radius in km
     * @return the composite score in [0.0, 1.0]
     */
    public double scoreDestination(VulnerableHabitationDto habitation,
                                  RecommendedDestinationDto dest,
                                  CandidateSafeSiteDto rawSite,
                                  Double maxRadiusKm) {
        if (dest == null) {
            return 0.0;
        }

        Map<String, Double> contributors = new LinkedHashMap<>();

        // 1. Suitability Quality (35%)
        double suitabilityQuality = normalizeSuitabilityQuality(dest);
        contributors.put(RecommendationScoringConfig.SUITABILITY_QUALITY, suitabilityQuality);

        // 2. Transit Proximity (30%)
        double effectiveRadius = (maxRadiusKm != null && maxRadiusKm > 0.0)
                ? maxRadiusKm
                : config.getDefaultMaxTransitRadiusKm();
        double transitProximity = normalizeTransitProximity(dest, effectiveRadius);
        contributors.put(RecommendationScoringConfig.TRANSIT_PROXIMITY, transitProximity);

        // 3. Capacity Fit (20%)
        long requiredPopulation = resolveRequiredPopulation(habitation);
        double capacityFit = normalizeCapacityFit(dest, requiredPopulation);
        contributors.put(RecommendationScoringConfig.CAPACITY_FIT, capacityFit);

        // 4. Access Reliability (15%)
        double accessReliability = normalizeAccessReliability(rawSite);
        contributors.put(RecommendationScoringConfig.ACCESS_RELIABILITY, accessReliability);

        // Compute weighted composite score
        double rawScore = 0.0;
        for (Map.Entry<String, Double> entry : contributors.entrySet()) {
            double weight = config.getWeight(entry.getKey());
            rawScore += weight * entry.getValue();
        }

        // Clamp to [0.0, 1.0] and round to 4 decimal places
        double finalScore = clamp(rawScore, 0.0, 1.0);
        finalScore = Math.round(finalScore * 10000.0) / 10000.0;

        // Save breakdown and score into destination DTO
        dest.setDestinationScore(finalScore);
        dest.setScoringContributors(contributors);

        return finalScore;
    }

    // --- Normalization Helpers ---

    public double normalizeSuitabilityQuality(RecommendedDestinationDto dest) {
        if (dest == null) return 0.0;

        if (dest.getSuitabilityScore() != null) {
            double score = dest.getSuitabilityScore();
            // If score is on a 0-100 scale, normalize to [0, 1]
            if (score > 1.0) {
                return clamp(score / 100.0, 0.0, 1.0);
            }
            return clamp(score, 0.0, 1.0);
        }

        SuitabilityClass sClass = dest.getSuitabilityClass();
        if (sClass != null) {
            return switch (sClass) {
                case HIGHLY_SUITABLE -> 0.95;
                case SUITABLE -> 0.80;
                case MARGINAL -> 0.50;
                case UNSUITABLE -> 0.10;
                case UNKNOWN -> 0.40;
            };
        }
        return 0.40;
    }

    public double normalizeTransitProximity(RecommendedDestinationDto dest, double maxRadiusKm) {
        if (dest == null || dest.getDistanceKilometers() == null || maxRadiusKm <= 0.0) {
            return 0.20; // Default conservative proximity if distance missing
        }
        double distKm = dest.getDistanceKilometers();
        if (distKm <= 0.0) {
            return 1.0;
        }
        if (distKm >= maxRadiusKm) {
            return 0.0;
        }
        return clamp(1.0 - (distKm / maxRadiusKm), 0.0, 1.0);
    }

    public double normalizeCapacityFit(RecommendedDestinationDto dest, long requiredPopulation) {
        if (dest == null) return 0.0;

        Integer avail = dest.getAvailableCapacity();
        if (avail == null) {
            return 1.0; // Unbounded capacity receives full score
        }
        if (avail <= 0) {
            return 0.0;
        }
        if (requiredPopulation <= 0) {
            return 1.0;
        }

        if (avail >= requiredPopulation) {
            // Sufficiency baseline 0.6 + headroom bonus up to 0.4
            double headroomRatio = (double) avail / (requiredPopulation * 2.0);
            return clamp(0.6 + 0.4 * Math.min(1.0, headroomRatio), 0.0, 1.0);
        } else {
            // Partial capacity fits proportion but penalized
            return clamp(((double) avail / requiredPopulation) * 0.4, 0.0, 0.4);
        }
    }

    public double normalizeAccessReliability(CandidateSafeSiteDto site) {
        if (site == null) {
            return 0.50; // Neutral default
        }

        double roadScore = config.getRoadAccessScore(site.getRoadAccessStatus());
        double waterScore = config.getWaterAccessScore(site.getWaterAccessStatus());
        double healthScore = config.getHealthcareAccessScore(site.getHealthcareAccessStatus());

        return clamp(0.40 * roadScore + 0.30 * waterScore + 0.30 * healthScore, 0.0, 1.0);
    }

    private long resolveRequiredPopulation(VulnerableHabitationDto habitation) {
        if (habitation == null) return 0L;
        if (habitation.getVulnerablePopulation() != null && habitation.getVulnerablePopulation() > 0) {
            return habitation.getVulnerablePopulation();
        }
        if (habitation.getTotalPopulation() != null && habitation.getTotalPopulation() > 0) {
            return habitation.getTotalPopulation();
        }
        return 0L;
    }

    private static double clamp(double value, double min, double max) {
        if (Double.isNaN(value)) return min;
        return Math.max(min, Math.min(max, value));
    }
}
