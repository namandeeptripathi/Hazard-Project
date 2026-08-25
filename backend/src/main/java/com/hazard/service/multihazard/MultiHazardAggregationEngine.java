package com.hazard.service.multihazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.multihazard.HazardParticipationDto;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Pure Mathematical Aggregation Engine for Multi-Hazard Index Computation.
 * Combines participating single-hazard scores using configurable weights,
 * dynamic effective weight recalculation, and dominant hazard identification.
 */
@Component
public class MultiHazardAggregationEngine {

    public record MultiHazardResult(
            Double multiHazardIndex,
            SeverityTier severityTier,
            HazardType dominantHazard,
            Double dominantHazardScore,
            HazardType secondaryHazard,
            Double secondaryHazardScore,
            double completenessRatio,
            String explanation
    ) {}

    /**
     * Aggregates participating single-hazard scores into a unified Multi-Hazard Index.
     */
    public MultiHazardResult aggregate(List<HazardParticipationDto> participatingHazards, MultiHazardConfig config) {
        if (participatingHazards == null || participatingHazards.isEmpty() || config == null) {
            return new MultiHazardResult(null, null, null, null, null, null, 0.0,
                    "Insufficient hazard participation data for multi-hazard aggregation");
        }

        Map<HazardType, Double> configuredWeights = config.getHazardWeights();
        double availableWeightSum = 0.0;
        int matchedCount = 0;

        for (HazardParticipationDto hazard : participatingHazards) {
            if (hazard.getHazardScore() != null) {
                Double configuredW = configuredWeights.get(hazard.getHazardType());
                if (configuredW != null && configuredW > 0.0) {
                    availableWeightSum += configuredW;
                    matchedCount++;
                }
            }
        }

        if (matchedCount == 0 || availableWeightSum <= 0.0) {
            return new MultiHazardResult(null, null, null, null, null, null, 0.0,
                    "No configured participating hazards available for multi-hazard aggregation");
        }

        double rawIndexSum = 0.0;
        StringBuilder explanationBuilder = new StringBuilder();
        explanationBuilder.append("Multi-Hazard Composite Index calculation: ");

        HazardParticipationDto dominant = null;
        HazardParticipationDto secondary = null;

        for (HazardParticipationDto hazard : participatingHazards) {
            if (hazard.getHazardScore() != null) {
                Double configuredW = configuredWeights.getOrDefault(hazard.getHazardType(), 0.0);
                double effectiveW = configuredW / availableWeightSum;
                double contribution = hazard.getHazardScore() * effectiveW;
                rawIndexSum += contribution;

                double roundedContrib = Math.round(contribution * 10000.0) / 10000.0;
                double roundedEffW = Math.round(effectiveW * 10000.0) / 10000.0;

                hazard.setConfiguredWeight(configuredW);
                hazard.setEffectiveWeight(roundedEffW);
                hazard.setWeightedContribution(roundedContrib);

                explanationBuilder.append(String.format("[%s: score=%.4f (%s), eff_w=%.2f, contrib=%.4f] ",
                        hazard.getHazardType().name(), hazard.getHazardScore(),
                        hazard.getSeverityTier() != null ? hazard.getSeverityTier().name() : "N/A",
                        roundedEffW, roundedContrib));

                // Dominance ranking
                if (dominant == null || hazard.getHazardScore() > dominant.getHazardScore()) {
                    secondary = dominant;
                    dominant = hazard;
                } else if (secondary == null || hazard.getHazardScore() > secondary.getHazardScore()) {
                    secondary = hazard;
                }
            }
        }

        double clampedIndex = Math.min(1.0, Math.max(0.0, rawIndexSum));
        double finalIndex = Math.round(clampedIndex * 10000.0) / 10000.0;
        SeverityTier tier = SeverityTier.fromScore(finalIndex);
        double completeness = Math.round(((double) matchedCount / configuredWeights.size()) * 100.0) / 100.0;

        HazardType dominantType = dominant != null ? dominant.getHazardType() : null;
        Double dominantScore = dominant != null ? dominant.getHazardScore() : null;
        HazardType secondaryType = secondary != null ? secondary.getHazardType() : null;
        Double secondaryScore = secondary != null ? secondary.getHazardScore() : null;

        explanationBuilder.append(String.format("-> Final Multi-Hazard Index: %.4f (%s, Dominant: %s=%.4f, Completeness: %.0f%%)",
                finalIndex, tier != null ? tier.name() : "N/A",
                dominantType != null ? dominantType.name() : "None",
                dominantScore != null ? dominantScore : 0.0,
                completeness * 100.0));

        return new MultiHazardResult(
                finalIndex, tier, dominantType, dominantScore, secondaryType, secondaryScore,
                completeness, explanationBuilder.toString()
        );
    }
}
