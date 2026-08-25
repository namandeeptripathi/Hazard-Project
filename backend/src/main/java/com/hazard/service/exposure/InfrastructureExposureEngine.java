package com.hazard.service.exposure;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import org.springframework.stereotype.Component;

/**
 * Calculation and classification engine for Stage 4.3 Infrastructure Exposure.
 * Derives normalized [0.0000, 1.0000] infrastructure exposure scores based on
 * Stage 3 hazard intensity, proximity decay, asset criticality multiplier, and linear impact ratios.
 */
@Component
public class InfrastructureExposureEngine {

    private final PopulationExposureConfig config;

    public InfrastructureExposureEngine(PopulationExposureConfig config) {
        this.config = config;
    }

    /**
     * Encapsulates calculated infrastructure exposure metrics.
     */
    public record InfrastructureScoreResult(
            double proximityFactor,
            double criticalityFactor,
            double lengthRatio,
            double infrastructureExposureScore,
            ExposureCategory exposureCategory
    ) {}

    /**
     * Calculates spatial proximity decay factor in [0.1000, 1.0000].
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
     * Calculates composite infrastructure exposure score and category classification.
     */
    public InfrastructureScoreResult calculateInfrastructureScore(Double hazardSeverityScore,
                                                                   Double distanceMeters,
                                                                   Double bufferRadiusMeters,
                                                                   InfrastructureCriticality criticality,
                                                                   Double affectedLengthRatio) {
        double safeHazard = (hazardSeverityScore != null && hazardSeverityScore > 0.0)
                ? Math.min(1.0, hazardSeverityScore)
                : 0.5000;

        double proximity = calculateProximityFactor(distanceMeters, bufferRadiusMeters);
        double critFactor = criticality != null ? criticality.getWeightMultiplier() : 1.0000;
        double lenRatio = (affectedLengthRatio != null && affectedLengthRatio > 0.0)
                ? Math.min(1.0, Math.max(0.10, affectedLengthRatio))
                : 1.0000;

        double rawScore = safeHazard * proximity * critFactor * lenRatio;
        double score = round4(Math.min(1.0, Math.max(0.0, rawScore)));

        ExposureCategory category = config.classifyExposurePercentage(score * 100.0);

        return new InfrastructureScoreResult(proximity, critFactor, lenRatio, score, category);
    }

    public static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
