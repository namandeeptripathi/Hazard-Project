package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.NormalizationDirection;
import com.hazard.domain.hazard.NormalizationMethod;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.normalization.NormalizedHazardMetric;
import com.hazard.service.scoring.HazardScoringConfig;
import com.hazard.service.scoring.HazardScoringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HazardScoringEngineTests {

    private HazardScoringEngine scoringEngine;

    @BeforeEach
    void setUp() {
        scoringEngine = new HazardScoringEngine();
    }

    @Test
    @DisplayName("1. Full Scoring: All Metrics Present Produces Exact Weighted Sum & 100% Completeness")
    void testFullScoringWithAllMetrics() {
        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put("METRIC_A", 0.50);
        weights.put("METRIC_B", 0.30);
        weights.put("METRIC_C", 0.20);
        HazardScoringConfig config = new HazardScoringConfig(HazardType.FLOOD, weights, "Test config");

        Map<String, NormalizedHazardMetric> metrics = new LinkedHashMap<>();
        metrics.put("METRIC_A", createMetric("METRIC_A", 0.80)); // 0.80 * 0.50 = 0.40
        metrics.put("METRIC_B", createMetric("METRIC_B", 0.60)); // 0.60 * 0.30 = 0.18
        metrics.put("METRIC_C", createMetric("METRIC_C", 0.50)); // 0.50 * 0.20 = 0.10
        // Expected sum: 0.40 + 0.18 + 0.10 = 0.6800 -> HIGH

        HazardScoringEngine.ScoringResult result = scoringEngine.calculateScore(metrics, config);

        assertNotNull(result);
        assertEquals(0.6800, result.hazardScore(), 0.0001);
        assertEquals(SeverityTier.HIGH, result.severityTier());
        assertEquals(1.00, result.completenessRatio());
        assertEquals(3, result.contributions().size());
    }

    @Test
    @DisplayName("2. Missing Metric Handling: Recalculates Effective Weights Dynamically")
    void testMissingMetricEffectiveWeightRecalculation() {
        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put("METRIC_A", 0.50);
        weights.put("METRIC_B", 0.30);
        weights.put("METRIC_C", 0.20);
        HazardScoringConfig config = new HazardScoringConfig(HazardType.FLOOD, weights, "Test config");

        // Only METRIC_A and METRIC_B present (available weight sum = 0.80)
        Map<String, NormalizedHazardMetric> metrics = new LinkedHashMap<>();
        metrics.put("METRIC_A", createMetric("METRIC_A", 0.80)); // eff_w = 0.50 / 0.80 = 0.625 -> 0.80 * 0.625 = 0.5000
        metrics.put("METRIC_B", createMetric("METRIC_B", 0.40)); // eff_w = 0.30 / 0.80 = 0.375 -> 0.40 * 0.375 = 0.1500
        // Expected sum: 0.5000 + 0.1500 = 0.6500 -> HIGH

        HazardScoringEngine.ScoringResult result = scoringEngine.calculateScore(metrics, config);

        assertNotNull(result);
        assertEquals(0.6500, result.hazardScore(), 0.0001);
        assertEquals(SeverityTier.HIGH, result.severityTier());
        assertEquals(0.67, result.completenessRatio(), 0.01);
        assertEquals(2, result.contributions().size());
        assertEquals(0.6250, result.contributions().get(0).getEffectiveWeight(), 0.0001);
        assertEquals(0.3750, result.contributions().get(1).getEffectiveWeight(), 0.0001);
    }

    @Test
    @DisplayName("3. Zero Eligible Metrics Produces Null Score and 0% Completeness")
    void testZeroEligibleMetrics() {
        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put("METRIC_A", 0.60);
        weights.put("METRIC_B", 0.40);
        HazardScoringConfig config = new HazardScoringConfig(HazardType.FLOOD, weights, "Test config");

        Map<String, NormalizedHazardMetric> metrics = new LinkedHashMap<>();
        metrics.put("UNRELATED_METRIC", createMetric("UNRELATED_METRIC", 0.90));

        HazardScoringEngine.ScoringResult result = scoringEngine.calculateScore(metrics, config);

        assertNotNull(result);
        assertNull(result.hazardScore());
        assertNull(result.severityTier());
        assertEquals(0.0, result.completenessRatio());
        assertTrue(result.contributions().isEmpty());
    }

    @Test
    @DisplayName("4. Severity Tier Boundary Mappings (LOW, MODERATE, HIGH, SEVERE)")
    void testSeverityTierBoundaries() {
        // LOW: [0.0000, 0.2499]
        assertEquals(SeverityTier.LOW, SeverityTier.fromScore(0.0000));
        assertEquals(SeverityTier.LOW, SeverityTier.fromScore(0.1500));
        assertEquals(SeverityTier.LOW, SeverityTier.fromScore(0.2499));

        // MODERATE: [0.2500, 0.4999]
        assertEquals(SeverityTier.MODERATE, SeverityTier.fromScore(0.2500));
        assertEquals(SeverityTier.MODERATE, SeverityTier.fromScore(0.3750));
        assertEquals(SeverityTier.MODERATE, SeverityTier.fromScore(0.4999));

        // HIGH: [0.5000, 0.7499]
        assertEquals(SeverityTier.HIGH, SeverityTier.fromScore(0.5000));
        assertEquals(SeverityTier.HIGH, SeverityTier.fromScore(0.6250));
        assertEquals(SeverityTier.HIGH, SeverityTier.fromScore(0.7499));

        // SEVERE: [0.7500, 1.0000]
        assertEquals(SeverityTier.SEVERE, SeverityTier.fromScore(0.7500));
        assertEquals(SeverityTier.SEVERE, SeverityTier.fromScore(0.8800));
        assertEquals(SeverityTier.SEVERE, SeverityTier.fromScore(1.0000));
    }

    @Test
    @DisplayName("5. Invalid Weights Configuration Fails Fast with Clear Exception")
    void testInvalidWeightsSumRejection() {
        Map<String, Double> badWeights = new LinkedHashMap<>();
        badWeights.put("METRIC_A", 0.50);
        badWeights.put("METRIC_B", 0.30); // Sum = 0.80 != 1.00

        assertThrows(IllegalStateException.class, () ->
                new HazardScoringConfig(HazardType.FLOOD, badWeights, "Bad weights")
        );
    }

    @Test
    @DisplayName("6. Registered Standard Scoring Configurations Sum to Exactly 1.0000")
    void testRegisteredStandardConfigs() {
        for (HazardScoringConfig config : HazardScoringConfig.getAllConfigs()) {
            double sum = config.getMetricWeights().values().stream().mapToDouble(Double::doubleValue).sum();
            assertEquals(1.0000, sum, 0.0001, "Weights for " + config.getHazardType() + " must sum to 1.0000");
        }
    }

    private NormalizedHazardMetric createMetric(String name, double normVal) {
        return new NormalizedHazardMetric(
                name, name, "units", normVal, normVal, 0.0, 100.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING, false, "Unit test"
        );
    }
}
