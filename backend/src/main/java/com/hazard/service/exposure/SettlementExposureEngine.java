package com.hazard.service.exposure;

import com.hazard.domain.exposure.ExposureCategory;
import org.springframework.stereotype.Component;

/**
 * Calculation and classification engine for Stage 4.2 Settlement Exposure.
 * Derives normalized [0.0000, 1.0000] settlement exposure scores based on
 * Stage 3 hazard intensity and spatial proximity decay.
 */
@Component
public class SettlementExposureEngine {

    private final PopulationExposureConfig config;

    public SettlementExposureEngine(PopulationExposureConfig config) {
        this.config = config;
    }

    /**
     * Encapsulates calculated settlement exposure metrics.
     */
    public record SettlementScoreResult(
            double proximityFactor,
            double settlementExposureScore,
            ExposureCategory exposureCategory
    ) {}

    /**
     * Calculates the spatial proximity decay factor in [0.1000, 1.0000].
     * Closer to hazard epicenter -> higher factor (1.0). At boundary -> 0.1.
     */
    public double calculateProximityFactor(Double distanceMeters, Double bufferRadiusMeters) {
        if (distanceMeters == null || distanceMeters <= 0.0) {
            return 1.0;
        }
        if (bufferRadiusMeters == null || bufferRadiusMeters <= 0.0) {
            return 1.0;
        }
        if (distanceMeters >= bufferRadiusMeters) {
            return 0.1000;
        }
        double ratio = distanceMeters / bufferRadiusMeters;
        double decay = 1.0 - (0.9 * ratio);
        return round4(Math.min(1.0, Math.max(0.1, decay)));
    }

    /**
     * Calculates the composite settlement exposure score and categorical classification.
     */
    public SettlementScoreResult calculateSettlementScore(Double hazardSeverityScore, Double distanceMeters, Double bufferRadiusMeters) {
        double safeHazard = (hazardSeverityScore != null && hazardSeverityScore > 0.0)
                ? Math.min(1.0, hazardSeverityScore)
                : 0.5000; // Baseline median severity if unranked

        double proximity = calculateProximityFactor(distanceMeters, bufferRadiusMeters);
        double rawScore = safeHazard * proximity;
        double score = round4(Math.min(1.0, Math.max(0.0, rawScore)));

        ExposureCategory category = config.classifyExposurePercentage(score * 100.0);

        return new SettlementScoreResult(proximity, score, category);
    }

    /**
     * Utility method to round doubles to 4 decimal places.
     */
    public static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
