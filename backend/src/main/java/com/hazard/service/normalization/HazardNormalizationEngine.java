package com.hazard.service.normalization;

import com.hazard.domain.hazard.NormalizationDirection;
import com.hazard.domain.hazard.NormalizationMethod;
import com.hazard.dto.normalization.NormalizedHazardMetric;
import org.springframework.stereotype.Component;

/**
 * Pure Mathematical Normalization Engine for Hazard Indicators.
 * Executes deterministic min-max scaling with boundary clamping to [0.00, 1.00],
 * direction handling, logarithmic transformations, and min==max safety protections.
 */
@Component
public class HazardNormalizationEngine {

    /**
     * Normalizes a raw numeric metric value according to its configuration.
     *
     * @param rawValue raw observed numeric value (null-safe)
     * @param config normalization configuration containing reference min, max, direction, and method
     * @return NormalizedHazardMetric with value bounded in [0.0000, 1.0000], or null if rawValue is null
     */
    public NormalizedHazardMetric normalize(Double rawValue, HazardMetricNormConfig config) {
        if (rawValue == null) {
            return null;
        }
        if (config == null) {
            throw new IllegalArgumentException("Normalization configuration cannot be null");
        }

        double min = config.getReferenceMin();
        double max = config.getReferenceMax();
        NormalizationDirection direction = config.getDirection() != null ? config.getDirection() : NormalizationDirection.INCREASING;
        NormalizationMethod method = config.getMethod() != null ? config.getMethod() : NormalizationMethod.MIN_MAX;

        // 1. Protection against division by zero (min == max)
        if (Double.compare(min, max) == 0) {
            return new NormalizedHazardMetric(
                    config.getMetricName(), config.getMetricLabel(), config.getUnits(),
                    rawValue, 0.0, min, max, method, direction, false, config.getReferenceRationale()
            );
        }

        double unclamped;
        if (method == NormalizationMethod.LOG_MIN_MAX) {
            double safeVal = Math.max(1e-6, rawValue);
            double safeMin = Math.max(1e-6, min);
            double safeMax = Math.max(1e-6, max);
            double logVal = Math.log10(safeVal);
            double logMin = Math.log10(safeMin);
            double logMax = Math.log10(safeMax);

            if (direction == NormalizationDirection.INCREASING) {
                unclamped = (logVal - logMin) / (logMax - logMin);
            } else {
                unclamped = (logMax - logVal) / (logMax - logMin);
            }
        } else {
            // Standard Linear MIN_MAX
            if (direction == NormalizationDirection.INCREASING) {
                unclamped = (rawValue - min) / (max - min);
            } else {
                unclamped = (max - rawValue) / (max - min);
            }
        }

        // 2. Boundary handling & Clamping to [0.0000, 1.0000]
        boolean clamped = (unclamped < 0.0 || unclamped > 1.0);
        double clampedVal = Math.min(1.0, Math.max(0.0, unclamped));
        double roundedVal = Math.round(clampedVal * 10000.0) / 10000.0;

        return new NormalizedHazardMetric(
                config.getMetricName(),
                config.getMetricLabel(),
                config.getUnits(),
                rawValue,
                roundedVal,
                min,
                max,
                method,
                direction,
                clamped,
                config.getReferenceRationale()
        );
    }

    /**
     * Normalizes a metric by its registered standard name in the configuration registry.
     */
    public NormalizedHazardMetric normalizeByName(Double rawValue, String metricName) {
        if (rawValue == null || metricName == null) {
            return null;
        }
        HazardMetricNormConfig config = HazardMetricNormConfig.getConfig(metricName)
                .orElseThrow(() -> new IllegalArgumentException("Unregistered normalization metric name: " + metricName));
        return normalize(rawValue, config);
    }
}
