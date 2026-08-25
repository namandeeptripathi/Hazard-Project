package com.hazard;

import com.hazard.domain.hazard.NormalizationDirection;
import com.hazard.domain.hazard.NormalizationMethod;
import com.hazard.dto.normalization.NormalizedHazardMetric;
import com.hazard.service.normalization.HazardMetricNormConfig;
import com.hazard.service.normalization.HazardNormalizationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HazardNormalizationEngineTests {

    private HazardNormalizationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new HazardNormalizationEngine();
    }

    @Test
    @DisplayName("1. Value at Reference Minimum Normalizes to Exactly 0.0000")
    void testValueAtMinimum() {
        HazardMetricNormConfig config = new HazardMetricNormConfig(
                "TEST_METRIC", "Test", "mm", 0.0, 100.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING, "Unit test"
        );

        NormalizedHazardMetric result = engine.normalize(0.0, config);

        assertNotNull(result);
        assertEquals(0.0, result.getRawValue());
        assertEquals(0.0000, result.getNormalizedValue());
        assertFalse(result.isClamped());
    }

    @Test
    @DisplayName("2. Value at Reference Maximum Normalizes to Exactly 1.0000")
    void testValueAtMaximum() {
        HazardMetricNormConfig config = new HazardMetricNormConfig(
                "TEST_METRIC", "Test", "mm", 0.0, 100.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING, "Unit test"
        );

        NormalizedHazardMetric result = engine.normalize(100.0, config);

        assertNotNull(result);
        assertEquals(100.0, result.getRawValue());
        assertEquals(1.0000, result.getNormalizedValue());
        assertFalse(result.isClamped());
    }

    @Test
    @DisplayName("3. Midpoint Normalization Produces Exactly 0.5000")
    void testMidpointNormalization() {
        HazardMetricNormConfig config = new HazardMetricNormConfig(
                "TEST_METRIC", "Test", "mm", 0.0, 100.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING, "Unit test"
        );

        NormalizedHazardMetric result = engine.normalize(50.0, config);

        assertNotNull(result);
        assertEquals(50.0, result.getRawValue());
        assertEquals(0.5000, result.getNormalizedValue());
        assertFalse(result.isClamped());
    }

    @Test
    @DisplayName("4. Value Below Minimum Clamps to 0.0000 and Sets Clamped Flag")
    void testBelowMinimumClamping() {
        HazardMetricNormConfig config = new HazardMetricNormConfig(
                "TEST_METRIC", "Test", "mm", 10.0, 100.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING, "Unit test"
        );

        NormalizedHazardMetric result = engine.normalize(2.0, config);

        assertNotNull(result);
        assertEquals(2.0, result.getRawValue());
        assertEquals(0.0000, result.getNormalizedValue());
        assertTrue(result.isClamped());
    }

    @Test
    @DisplayName("5. Value Above Maximum Clamps to 1.0000 and Sets Clamped Flag")
    void testAboveMaximumClamping() {
        HazardMetricNormConfig config = new HazardMetricNormConfig(
                "TEST_METRIC", "Test", "mm", 0.0, 100.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING, "Unit test"
        );

        NormalizedHazardMetric result = engine.normalize(175.0, config);

        assertNotNull(result);
        assertEquals(175.0, result.getRawValue());
        assertEquals(1.0000, result.getNormalizedValue());
        assertTrue(result.isClamped());
    }

    @Test
    @DisplayName("6. Min == Max Handled Gracefully Without NaN or Infinity")
    void testMinEqualsMaxSafety() {
        HazardMetricNormConfig config = new HazardMetricNormConfig(
                "CONST_METRIC", "Const", "mm", 50.0, 50.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING, "Unit test"
        );

        NormalizedHazardMetric result = engine.normalize(50.0, config);

        assertNotNull(result);
        assertEquals(50.0, result.getRawValue());
        assertEquals(0.0000, result.getNormalizedValue());
        assertFalse(Double.isNaN(result.getNormalizedValue()));
        assertFalse(Double.isInfinite(result.getNormalizedValue()));
    }

    @Test
    @DisplayName("7. Null Input Value Returns Null")
    void testNullValueHandling() {
        HazardMetricNormConfig config = new HazardMetricNormConfig(
                "TEST_METRIC", "Test", "mm", 0.0, 100.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING, "Unit test"
        );

        assertNull(engine.normalize(null, config));
    }

    @Test
    @DisplayName("8. Inverse Normalization Direction (DECREASING)")
    void testDecreasingDirection() {
        HazardMetricNormConfig config = new HazardMetricNormConfig(
                "ELEVATION_METRIC", "Elevation", "m", 0.0, 1000.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.DECREASING, "Lower elevation is more prone"
        );

        NormalizedHazardMetric minResult = engine.normalize(0.0, config);
        assertEquals(1.0000, minResult.getNormalizedValue());

        NormalizedHazardMetric maxResult = engine.normalize(1000.0, config);
        assertEquals(0.0000, maxResult.getNormalizedValue());

        NormalizedHazardMetric midResult = engine.normalize(500.0, config);
        assertEquals(0.5000, midResult.getNormalizedValue());
    }

    @Test
    @DisplayName("9. Standard Registered Metric Normalization (DAILY_RAINFALL_MM)")
    void testRegisteredMetricNormalization() {
        // Daily rainfall 101.7 mm with ref max 150.0 mm -> 101.7 / 150.0 = 0.6780
        NormalizedHazardMetric result = engine.normalizeByName(101.7, "DAILY_RAINFALL_MM");

        assertNotNull(result);
        assertEquals("DAILY_RAINFALL_MM", result.getMetricName());
        assertEquals(101.7, result.getRawValue());
        assertEquals(0.6780, result.getNormalizedValue());
        assertEquals(0.0, result.getReferenceMin());
        assertEquals(150.0, result.getReferenceMax());
        assertFalse(result.isClamped());
    }
}
