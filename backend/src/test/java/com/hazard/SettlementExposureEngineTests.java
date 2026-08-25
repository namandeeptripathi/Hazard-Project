package com.hazard;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.service.exposure.PopulationExposureConfig;
import com.hazard.service.exposure.SettlementExposureEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SettlementExposureEngine distance decay and score calculations.
 */
class SettlementExposureEngineTests {

    private PopulationExposureConfig config;
    private SettlementExposureEngine engine;

    @BeforeEach
    void setUp() {
        config = new PopulationExposureConfig();
        engine = new SettlementExposureEngine(config);
    }

    @Test
    @DisplayName("4.2.1: Proximity Factor — Epicenter vs. Boundary Decay")
    void testProximityFactorDecay() {
        // At epicenter (distance = 0) -> factor = 1.0
        assertEquals(1.0000, engine.calculateProximityFactor(0.0, 5000.0), 0.0001);

        // At midpoint (2500m / 5000m) -> factor = 1.0 - 0.9 * 0.5 = 0.55
        assertEquals(0.5500, engine.calculateProximityFactor(2500.0, 5000.0), 0.0001);

        // At outer boundary (5000m / 5000m) -> factor = 0.1000
        assertEquals(0.1000, engine.calculateProximityFactor(5000.0, 5000.0), 0.0001);

        // Beyond boundary (6000m / 5000m) -> clamped to 0.1000
        assertEquals(0.1000, engine.calculateProximityFactor(6000.0, 5000.0), 0.0001);

        // Null / negative cases -> 1.0
        assertEquals(1.0000, engine.calculateProximityFactor(null, 5000.0), 0.0001);
        assertEquals(1.0000, engine.calculateProximityFactor(-100.0, 5000.0), 0.0001);
    }

    @Test
    @DisplayName("4.2.2: Settlement Score & Category Assignment")
    void testSettlementScoreAndCategory() {
        // High hazard (0.80) at epicenter (0m) -> score = 0.80 -> VERY_HIGH (>= 0.70)
        var res1 = engine.calculateSettlementScore(0.8000, 0.0, 5000.0);
        assertEquals(0.8000, res1.settlementExposureScore(), 0.0001);
        assertEquals(ExposureCategory.VERY_HIGH, res1.exposureCategory());

        // High hazard (0.80) at midpoint (2500m) -> score = 0.80 * 0.55 = 0.44 -> HIGH (0.40 - 0.70)
        var res2 = engine.calculateSettlementScore(0.8000, 2500.0, 5000.0);
        assertEquals(0.4400, res2.settlementExposureScore(), 0.0001);
        assertEquals(ExposureCategory.HIGH, res2.exposureCategory());

        // High hazard (0.80) at boundary (5000m) -> score = 0.80 * 0.10 = 0.08 -> LOW (< 0.15)
        var res3 = engine.calculateSettlementScore(0.8000, 5000.0, 5000.0);
        assertEquals(0.0800, res3.settlementExposureScore(), 0.0001);
        assertEquals(ExposureCategory.LOW, res3.exposureCategory());

        // Moderate hazard (0.50) at midpoint (2500m) -> score = 0.50 * 0.55 = 0.275 -> MODERATE (0.15 - 0.40)
        var res4 = engine.calculateSettlementScore(0.5000, 2500.0, 5000.0);
        assertEquals(0.2750, res4.settlementExposureScore(), 0.0001);
        assertEquals(ExposureCategory.MODERATE, res4.exposureCategory());
    }

    @Test
    @DisplayName("4.2.3: Boundary & Edge Cases — Null Hazard Score")
    void testNullHazardScore() {
        // Null hazard defaults to 0.5000
        var res = engine.calculateSettlementScore(null, 0.0, 5000.0);
        assertEquals(0.5000, res.settlementExposureScore(), 0.0001);
        assertEquals(ExposureCategory.HIGH, res.exposureCategory());
    }
}
