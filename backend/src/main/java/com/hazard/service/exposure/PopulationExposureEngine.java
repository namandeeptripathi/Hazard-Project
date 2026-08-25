package com.hazard.service.exposure;

import com.hazard.domain.exposure.ExposureCategory;
import org.springframework.stereotype.Component;

/**
 * Pure calculation engine for Stage 4.1 Population Exposure.
 * Computes exposure ratios, normalized [0.0000, 1.0000] scores, and categorical tiers.
 */
@Component
public class PopulationExposureEngine {

    private final PopulationExposureConfig config;

    public PopulationExposureEngine(PopulationExposureConfig config) {
        this.config = config;
    }

    /**
     * Encapsulates calculated exposure metrics.
     */
    public record ExposureCalculationResult(
            long totalPopulation,
            long exposedPopulation,
            long unexposedPopulation,
            double exposurePercentage,
            double exposureScore,
            ExposureCategory exposureCategory
    ) {}

    /**
     * Calculates population exposure metrics with safety boundaries and 4-decimal rounding.
     */
    public ExposureCalculationResult calculateExposure(long totalPopulation, long exposedPopulation) {
        long safeTotal = Math.max(0L, totalPopulation);
        long safeExposed = Math.min(safeTotal, Math.max(0L, exposedPopulation));
        long unexposed = Math.max(0L, safeTotal - safeExposed);

        double percentage = 0.0;
        double score = 0.0;

        if (safeTotal > 0) {
            percentage = ((double) safeExposed / safeTotal) * 100.0;
            percentage = round4(percentage);
            score = round4(Math.min(1.0, Math.max(0.0, (double) safeExposed / safeTotal)));
        }

        ExposureCategory category = config.classifyExposurePercentage(percentage);

        return new ExposureCalculationResult(
                safeTotal,
                safeExposed,
                unexposed,
                percentage,
                score,
                category
        );
    }

    /**
     * Calculates normalized score directly from an exposure percentage in [0.0, 100.0].
     */
    public double calculateNormalizedScore(double percentage) {
        double clamped = Math.min(100.0, Math.max(0.0, percentage));
        return round4(clamped / 100.0);
    }

    /**
     * Utility method to round doubles to 4 decimal places.
     */
    public static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
