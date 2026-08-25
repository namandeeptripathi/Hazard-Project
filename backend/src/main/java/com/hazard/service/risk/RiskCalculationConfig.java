package com.hazard.service.risk;

import com.hazard.domain.risk.RiskComponentType;
import com.hazard.domain.risk.RiskTier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration bean for Stage 4.7 — Risk Calculation.
 * Manages top-level 4-pillar risk weights, exposure sub-weights, and tier classification thresholds.
 */
@Component
public class RiskCalculationConfig {

    private final Map<RiskComponentType, Double> defaultRiskComponentWeights = new LinkedHashMap<>();
    private final Map<String, Double> defaultExposureSubWeights = new LinkedHashMap<>();
    private final double minimumDataCompleteness = 0.50; // At least 2 of 4 components required
    private final String calculationVersion = "v1.0";

    public RiskCalculationConfig() {
        // Top-Level 4-Pillar Risk Weights
        defaultRiskComponentWeights.put(RiskComponentType.HAZARD, 0.35);        // 35%
        defaultRiskComponentWeights.put(RiskComponentType.EXPOSURE, 0.30);      // 30%
        defaultRiskComponentWeights.put(RiskComponentType.VULNERABILITY, 0.25); // 25%
        defaultRiskComponentWeights.put(RiskComponentType.HISTORICAL, 0.10);    // 10%

        // Exposure Sub-Component Weights
        defaultExposureSubWeights.put("POPULATION", 0.40);      // 40%
        defaultExposureSubWeights.put("SETTLEMENT", 0.25);      // 25%
        defaultExposureSubWeights.put("INFRASTRUCTURE", 0.35);  // 35%
    }

    public Map<RiskComponentType, Double> getDefaultRiskComponentWeights() {
        return Collections.unmodifiableMap(defaultRiskComponentWeights);
    }

    public Map<String, Double> getDefaultExposureSubWeights() {
        return Collections.unmodifiableMap(defaultExposureSubWeights);
    }

    public double getMinimumDataCompleteness() {
        return minimumDataCompleteness;
    }

    public String getCalculationVersion() {
        return calculationVersion;
    }

    public void validateComponentWeights(Map<RiskComponentType, Double> weights) {
        if (weights == null || weights.isEmpty()) {
            throw new IllegalArgumentException("Risk component weight configuration cannot be null or empty");
        }
        double sum = 0.0;
        for (Map.Entry<RiskComponentType, Double> entry : weights.entrySet()) {
            Double w = entry.getValue();
            if (w == null || Double.isNaN(w) || Double.isInfinite(w) || w < 0.0) {
                throw new IllegalArgumentException("Invalid weight for component " + entry.getKey() + ": " + w + " (must be non-negative and finite)");
            }
            sum += w;
        }
        if (sum <= 0.0) {
            throw new IllegalArgumentException("Total active risk component weight sum must be strictly greater than 0.0");
        }
    }
}
