package com.hazard.service.safesite;

import com.hazard.domain.safesite.DistanceStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.HealthcareAccessStatus;
import com.hazard.domain.safesite.InfrastructureAccessStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.domain.safesite.TerrainStatus;
import com.hazard.domain.safesite.WaterAccessStatus;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage 5.10 — Site Suitability Intelligence Evaluator.
 *
 * Combines the seven independent spatial dimensions (Stages 5.3–5.9) into an explainable,
 * normalized multi-criteria suitability assessment for each candidate safe site:
 * 1. Stage 5.3: Hazard Safety Status (SAFE, AT_RISK, UNKNOWN) — Weight: 30%
 * 2. Stage 5.4: Terrain / Slope Feasibility (FAVORABLE, UNFAVORABLE, UNKNOWN) — Weight: 15%
 * 3. Stage 5.5: Geographic Distance Intelligence (NEAR, MODERATE, FAR, UNKNOWN) — Weight: 15%
 * 4. Stage 5.6: Road Accessibility (NEAR, MODERATE, FAR, UNKNOWN) — Weight: 10%
 * 5. Stage 5.7: Healthcare Proximity (NEAR, MODERATE, FAR, UNKNOWN) — Weight: 10%
 * 6. Stage 5.8: Water Accessibility (NEAR, MODERATE, FAR, UNKNOWN) — Weight: 10%
 * 7. Stage 5.9: Supporting Infrastructure Proximity (NEAR, MODERATE, FAR, UNKNOWN) — Weight: 10%
 *
 * Safety Gate Rule:
 * - If hazardSafetyStatus == AT_RISK, the site is immediately classified as UNSUITABLE (score 0.0),
 *   overriding all other dimensions.
 *
 * UNKNOWN Normalization Rule:
 * - Dimensions with UNKNOWN status are excluded from scoring. The weighted score is normalized
 *   strictly against known dimensions.
 * - If all 7 dimensions are UNKNOWN, suitabilityClass is UNKNOWN with null score.
 *
 * Output:
 * - suitabilityScore, suitabilityClass, knownFactorCount, unknownFactorCount,
 *   dataCompletenessPercentage, suitabilityReason, and suitabilityFactors breakdown map.
 */
@Component
public class SuitabilityEvaluator {

    private static final Logger log = LoggerFactory.getLogger(SuitabilityEvaluator.class);

    private final SuitabilityEvaluationConfig config;

    public SuitabilityEvaluator(SuitabilityEvaluationConfig config) {
        this.config = config != null ? config : new SuitabilityEvaluationConfig();
    }

    /**
     * Evaluates site suitability across all seven independent spatial dimensions.
     */
    public void evaluateSuitability(CandidateSafeSiteDto site) {
        if (site == null) {
            return;
        }

        Map<String, Object> factors = new LinkedHashMap<>();

        // 1. Evaluate Dimension 1: Hazard Safety (Weight: 30%)
        HazardSafetyStatus hazardStatus = site.getHazardSafetyStatus();
        boolean hazardKnown = hazardStatus != null && hazardStatus != HazardSafetyStatus.UNKNOWN;
        Double hazardScore = null;
        if (hazardKnown) {
            hazardScore = hazardStatus == HazardSafetyStatus.SAFE ? config.getOptimalScore() : config.getPoorScore();
        }
        factors.put("hazardSafety", buildFactorDetail(
                hazardStatus != null ? hazardStatus.name() : "UNKNOWN",
                hazardScore,
                config.getHazardSafetyWeight(),
                hazardKnown));

        // 2. Evaluate Dimension 2: Terrain / Slope (Weight: 15%)
        TerrainStatus terrainStatus = site.getTerrainStatus();
        boolean terrainKnown = terrainStatus != null && terrainStatus != TerrainStatus.UNKNOWN;
        Double terrainScore = null;
        if (terrainKnown) {
            terrainScore = terrainStatus == TerrainStatus.FAVORABLE ? config.getOptimalScore() : config.getPoorScore();
        }
        factors.put("terrain", buildFactorDetail(
                terrainStatus != null ? terrainStatus.name() : "UNKNOWN",
                terrainScore,
                config.getTerrainWeight(),
                terrainKnown));

        // 3. Evaluate Dimension 3: Geographic Distance (Weight: 15%)
        DistanceStatus distanceStatus = site.getDistanceStatus();
        boolean distanceKnown = distanceStatus != null && distanceStatus != DistanceStatus.UNKNOWN;
        Double distanceScore = null;
        if (distanceKnown) {
            distanceScore = scoreForDistanceStatus(distanceStatus);
        }
        factors.put("distance", buildFactorDetail(
                distanceStatus != null ? distanceStatus.name() : "UNKNOWN",
                distanceScore,
                config.getDistanceWeight(),
                distanceKnown));

        // 4. Evaluate Dimension 4: Road Accessibility (Weight: 10%)
        RoadAccessStatus roadStatus = site.getRoadAccessStatus();
        boolean roadKnown = roadStatus != null && roadStatus != RoadAccessStatus.UNKNOWN;
        Double roadScore = null;
        if (roadKnown) {
            roadScore = scoreForRoadStatus(roadStatus);
        }
        factors.put("roads", buildFactorDetail(
                roadStatus != null ? roadStatus.name() : "UNKNOWN",
                roadScore,
                config.getRoadsWeight(),
                roadKnown));

        // 5. Evaluate Dimension 5: Healthcare Support (Weight: 10%)
        HealthcareAccessStatus healthcareStatus = site.getHealthcareAccessStatus();
        boolean healthcareKnown = healthcareStatus != null && healthcareStatus != HealthcareAccessStatus.UNKNOWN;
        Double healthcareScore = null;
        if (healthcareKnown) {
            healthcareScore = scoreForHealthcareStatus(healthcareStatus);
        }
        factors.put("healthcare", buildFactorDetail(
                healthcareStatus != null ? healthcareStatus.name() : "UNKNOWN",
                healthcareScore,
                config.getHealthcareWeight(),
                healthcareKnown));

        // 6. Evaluate Dimension 6: Water Accessibility (Weight: 10%)
        WaterAccessStatus waterStatus = site.getWaterAccessStatus();
        boolean waterKnown = waterStatus != null && waterStatus != WaterAccessStatus.UNKNOWN;
        Double waterScore = null;
        if (waterKnown) {
            waterScore = scoreForWaterStatus(waterStatus);
        }
        factors.put("water", buildFactorDetail(
                waterStatus != null ? waterStatus.name() : "UNKNOWN",
                waterScore,
                config.getWaterWeight(),
                waterKnown));

        // 7. Evaluate Dimension 7: Supporting Infrastructure (Weight: 10%)
        InfrastructureAccessStatus infraStatus = site.getInfrastructureAccessStatus();
        boolean infraKnown = infraStatus != null && infraStatus != InfrastructureAccessStatus.UNKNOWN;
        Double infraScore = null;
        if (infraKnown) {
            infraScore = scoreForInfrastructureStatus(infraStatus);
        }
        factors.put("infrastructure", buildFactorDetail(
                infraStatus != null ? infraStatus.name() : "UNKNOWN",
                infraScore,
                config.getInfrastructureWeight(),
                infraKnown));

        // Compute Factor Counts & Completeness
        int knownCount = 0;
        if (hazardKnown) knownCount++;
        if (terrainKnown) knownCount++;
        if (distanceKnown) knownCount++;
        if (roadKnown) knownCount++;
        if (healthcareKnown) knownCount++;
        if (waterKnown) knownCount++;
        if (infraKnown) knownCount++;

        int unknownCount = 7 - knownCount;
        double completeness = Math.round((knownCount / 7.0) * 1000.0) / 10.0;

        site.setKnownFactorCount(knownCount);
        site.setUnknownFactorCount(unknownCount);
        site.setDataCompletenessPercentage(completeness);
        site.setSuitabilityFactors(factors);

        // Handle case where ALL dimensions are UNKNOWN
        if (knownCount == 0) {
            site.setSuitabilityScore(null);
            site.setSuitabilityClass(SuitabilityClass.UNKNOWN);
            site.setSuitabilityReason("Insufficient spatial dimension data: All 7 evaluation dimensions are UNKNOWN; site suitability undetermined.");
            return;
        }

        // HARD SAFETY GATE: If hazard safety is AT_RISK, calculate diagnostic score from non-hazard factors and classify as UNSUITABLE
        if (hazardStatus == HazardSafetyStatus.AT_RISK) {
            double nonHazardWeightedSum = 0.0;
            double nonHazardKnownWeightSum = 0.0;

            if (terrainKnown && terrainScore != null) {
                nonHazardWeightedSum += terrainScore * config.getTerrainWeight();
                nonHazardKnownWeightSum += config.getTerrainWeight();
            }
            if (distanceKnown && distanceScore != null) {
                nonHazardWeightedSum += distanceScore * config.getDistanceWeight();
                nonHazardKnownWeightSum += config.getDistanceWeight();
            }
            if (roadKnown && roadScore != null) {
                nonHazardWeightedSum += roadScore * config.getRoadsWeight();
                nonHazardKnownWeightSum += config.getRoadsWeight();
            }
            if (healthcareKnown && healthcareScore != null) {
                nonHazardWeightedSum += healthcareScore * config.getHealthcareWeight();
                nonHazardKnownWeightSum += config.getHealthcareWeight();
            }
            if (waterKnown && waterScore != null) {
                nonHazardWeightedSum += waterScore * config.getWaterWeight();
                nonHazardKnownWeightSum += config.getWaterWeight();
            }
            if (infraKnown && infraScore != null) {
                nonHazardWeightedSum += infraScore * config.getInfrastructureWeight();
                nonHazardKnownWeightSum += config.getInfrastructureWeight();
            }

            Double diagnosticScore = null;
            if (nonHazardKnownWeightSum > 0) {
                double normalized = nonHazardWeightedSum / nonHazardKnownWeightSum;
                diagnosticScore = Math.round(normalized * 100.0) / 100.0;
            }

            site.setSuitabilityScore(diagnosticScore);
            site.setSuitabilityClass(SuitabilityClass.UNSUITABLE);
            site.setSuitabilityReason("Site is classified as UNSUITABLE because it is currently AT_RISK; hazard exposure overrides other suitability factors.");
            return;
        }

        // Weighted normalization over known dimensions
        double weightedSum = 0.0;
        double knownWeightSum = 0.0;

        if (hazardKnown && hazardScore != null) {
            weightedSum += hazardScore * config.getHazardSafetyWeight();
            knownWeightSum += config.getHazardSafetyWeight();
        }
        if (terrainKnown && terrainScore != null) {
            weightedSum += terrainScore * config.getTerrainWeight();
            knownWeightSum += config.getTerrainWeight();
        }
        if (distanceKnown && distanceScore != null) {
            weightedSum += distanceScore * config.getDistanceWeight();
            knownWeightSum += config.getDistanceWeight();
        }
        if (roadKnown && roadScore != null) {
            weightedSum += roadScore * config.getRoadsWeight();
            knownWeightSum += config.getRoadsWeight();
        }
        if (healthcareKnown && healthcareScore != null) {
            weightedSum += healthcareScore * config.getHealthcareWeight();
            knownWeightSum += config.getHealthcareWeight();
        }
        if (waterKnown && waterScore != null) {
            weightedSum += waterScore * config.getWaterWeight();
            knownWeightSum += config.getWaterWeight();
        }
        if (infraKnown && infraScore != null) {
            weightedSum += infraScore * config.getInfrastructureWeight();
            knownWeightSum += config.getInfrastructureWeight();
        }

        double normalizedScore = knownWeightSum > 0 ? (weightedSum / knownWeightSum) : 0.0;
        double roundedScore = Math.round(normalizedScore * 100.0) / 100.0;
        site.setSuitabilityScore(roundedScore);

        // Determine Suitability Classification Tier
        SuitabilityClass suitabilityClass;
        if (roundedScore >= config.getHighlySuitableMinScore()) {
            suitabilityClass = SuitabilityClass.HIGHLY_SUITABLE;
        } else if (roundedScore >= config.getSuitableMinScore()) {
            suitabilityClass = SuitabilityClass.SUITABLE;
        } else if (roundedScore >= config.getMarginalMinScore()) {
            suitabilityClass = SuitabilityClass.MARGINAL;
        } else {
            suitabilityClass = SuitabilityClass.UNSUITABLE;
        }
        site.setSuitabilityClass(suitabilityClass);

        // Construct descriptive suitability explanation
        StringBuilder reason = new StringBuilder();
        reason.append("Candidate site evaluated as ").append(suitabilityClass.name())
                .append(" with suitability score ").append(String.format("%.1f", roundedScore))
                .append("/100 (").append(knownCount).append("/7 dimensions evaluated, ")
                .append(String.format("%.1f", completeness)).append("% data completeness).");

        if (unknownCount > 0) {
            reason.append(" Score normalized over ").append(knownCount).append(" known dimensions; ")
                    .append(unknownCount).append(" dimension(s) had UNKNOWN data.");
        }

        site.setSuitabilityReason(reason.toString());
    }

    private double scoreForDistanceStatus(DistanceStatus status) {
        if (status == null) return config.getPoorScore();
        switch (status) {
            case NEAR:
                return config.getOptimalScore();
            case MODERATE:
                return config.getModerateScore();
            case FAR:
                return config.getFarScore();
            default:
                return config.getPoorScore();
        }
    }

    private double scoreForRoadStatus(RoadAccessStatus status) {
        if (status == null) return config.getPoorScore();
        switch (status) {
            case NEAR:
                return config.getOptimalScore();
            case MODERATE:
                return config.getModerateScore();
            case FAR:
                return config.getFarScore();
            default:
                return config.getPoorScore();
        }
    }

    private double scoreForHealthcareStatus(HealthcareAccessStatus status) {
        if (status == null) return config.getPoorScore();
        switch (status) {
            case NEAR:
                return config.getOptimalScore();
            case MODERATE:
                return config.getModerateScore();
            case FAR:
                return config.getFarScore();
            default:
                return config.getPoorScore();
        }
    }

    private double scoreForWaterStatus(WaterAccessStatus status) {
        if (status == null) return config.getPoorScore();
        switch (status) {
            case NEAR:
                return config.getOptimalScore();
            case MODERATE:
                return config.getModerateScore();
            case FAR:
                return config.getFarScore();
            default:
                return config.getPoorScore();
        }
    }

    private double scoreForInfrastructureStatus(InfrastructureAccessStatus status) {
        if (status == null) return config.getPoorScore();
        switch (status) {
            case NEAR:
                return config.getOptimalScore();
            case MODERATE:
                return config.getModerateScore();
            case FAR:
                return config.getFarScore();
            default:
                return config.getPoorScore();
        }
    }

    private Map<String, Object> buildFactorDetail(String status, Double score, double weight, boolean isKnown) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("status", status);
        detail.put("score", score);
        detail.put("weight", weight);
        detail.put("isKnown", isKnown);
        return detail;
    }
}
