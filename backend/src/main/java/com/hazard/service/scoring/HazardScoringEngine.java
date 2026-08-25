package com.hazard.service.scoring;

import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.normalization.NormalizedHazardMetric;
import com.hazard.dto.scoring.MetricContributionDto;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Pure Mathematical Scoring Engine for Single-Hazard Aggregation.
 * Executes weighted multi-criteria aggregation with dynamic effective weight recalculation
 * for missing metrics, score clamping to [0.0000, 1.0000], and categorical severity tier assignment.
 */
@Component
public class HazardScoringEngine {

    /**
     * Internal result structure produced by the scoring engine.
     */
    public record ScoringResult(
            Double hazardScore,
            SeverityTier severityTier,
            double completenessRatio,
            List<MetricContributionDto> contributions,
            String explanation
    ) {}

    /**
     * Calculates the single-hazard composite score from available normalized indicators.
     *
     * @param normalizedMetrics map of normalized hazard metrics for the observation
     * @param config scoring configuration specifying metric weights for the hazard type
     * @return ScoringResult containing the calculated score, severity tier, and contribution breakdown
     */
    public ScoringResult calculateScore(Map<String, NormalizedHazardMetric> normalizedMetrics, HazardScoringConfig config) {
        if (config == null || normalizedMetrics == null || normalizedMetrics.isEmpty()) {
            return new ScoringResult(null, null, 0.0, Collections.emptyList(), "Insufficient metric data for scoring");
        }

        Map<String, Double> configuredWeights = config.getMetricWeights();
        List<MetricContributionDto> contributions = new ArrayList<>();

        // 1. Identify available metrics matching configuration
        double availableWeightSum = 0.0;
        int availableCount = 0;

        for (Map.Entry<String, Double> entry : configuredWeights.entrySet()) {
            String metricName = entry.getKey();
            NormalizedHazardMetric metric = normalizedMetrics.get(metricName);
            if (metric != null && metric.getNormalizedValue() != null) {
                availableWeightSum += entry.getValue();
                availableCount++;
            }
        }

        // 2. If no eligible metrics exist, return null score
        if (availableCount == 0 || availableWeightSum <= 0.0) {
            return new ScoringResult(null, null, 0.0, Collections.emptyList(),
                    "No configured indicators available for hazard type: " + config.getHazardType());
        }

        // 3. Compute weighted contributions with effective weight renormalization
        double rawScoreSum = 0.0;
        StringBuilder explanationBuilder = new StringBuilder();
        explanationBuilder.append(config.getHazardType().name()).append(" Hazard Score calculation: ");

        for (Map.Entry<String, Double> entry : configuredWeights.entrySet()) {
            String metricName = entry.getKey();
            Double configuredWeight = entry.getValue();
            NormalizedHazardMetric metric = normalizedMetrics.get(metricName);

            if (metric != null && metric.getNormalizedValue() != null) {
                double effectiveWeight = configuredWeight / availableWeightSum;
                double contribution = metric.getNormalizedValue() * effectiveWeight;
                rawScoreSum += contribution;

                double roundedContrib = Math.round(contribution * 10000.0) / 10000.0;
                double roundedEffWeight = Math.round(effectiveWeight * 10000.0) / 10000.0;

                MetricContributionDto contribDto = new MetricContributionDto(
                        metricName,
                        metric.getMetricLabel() != null ? metric.getMetricLabel() : metricName,
                        metric.getRawValue(),
                        metric.getUnits(),
                        metric.getNormalizedValue(),
                        configuredWeight,
                        roundedEffWeight,
                        roundedContrib,
                        metric.isClamped()
                );
                contributions.add(contribDto);

                explanationBuilder.append(String.format("[%s: norm=%.4f, eff_w=%.2f, contrib=%.4f] ",
                        metricName, metric.getNormalizedValue(), roundedEffWeight, roundedContrib));
            }
        }

        // 4. Clamping and Rounding to [0.0000, 1.0000]
        double clampedScore = Math.min(1.0, Math.max(0.0, rawScoreSum));
        double finalScore = Math.round(clampedScore * 10000.0) / 10000.0;

        SeverityTier tier = SeverityTier.fromScore(finalScore);
        double completeness = Math.round(((double) availableCount / configuredWeights.size()) * 100.0) / 100.0;

        explanationBuilder.append(String.format("-> Final Score: %.4f (%s, completeness: %.0f%%)",
                finalScore, tier.name(), completeness * 100.0));

        return new ScoringResult(finalScore, tier, completeness, contributions, explanationBuilder.toString());
    }
}
