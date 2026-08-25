package com.hazard;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import com.hazard.service.exposure.InfrastructureExposureEngine;
import com.hazard.service.exposure.PopulationExposureConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InfrastructureExposureEngine distance decay, criticality multipliers, and scoring.
 */
class InfrastructureExposureEngineTests {

    private PopulationExposureConfig config;
    private InfrastructureExposureEngine engine;

    @BeforeEach
    void setUp() {
        config = new PopulationExposureConfig();
        engine = new InfrastructureExposureEngine(config);
    }

    @Test
    @DisplayName("4.3.1: Criticality Multipliers in Scoring")
    void testCriticalityMultipliers() {
        // Base hazard: 0.60, Epicenter (0m), Full length (1.0)
        // VERY_HIGH criticality multiplier = 1.25 -> 0.60 * 1.0 * 1.25 = 0.7500 -> VERY_HIGH (>= 0.70)
        var res1 = engine.calculateInfrastructureScore(0.60, 0.0, 5000.0, InfrastructureCriticality.VERY_HIGH, 1.0);
        assertEquals(0.7500, res1.infrastructureExposureScore(), 0.0001);
        assertEquals(ExposureCategory.VERY_HIGH, res1.exposureCategory());

        // HIGH criticality multiplier = 1.15 -> 0.60 * 1.0 * 1.15 = 0.6900 -> HIGH (0.40 - 0.70)
        var res2 = engine.calculateInfrastructureScore(0.60, 0.0, 5000.0, InfrastructureCriticality.HIGH, 1.0);
        assertEquals(0.6900, res2.infrastructureExposureScore(), 0.0001);
        assertEquals(ExposureCategory.HIGH, res2.exposureCategory());

        // MODERATE criticality multiplier = 1.00 -> 0.60 * 1.0 * 1.00 = 0.6000 -> HIGH (0.40 - 0.70)
        var res3 = engine.calculateInfrastructureScore(0.60, 0.0, 5000.0, InfrastructureCriticality.MODERATE, 1.0);
        assertEquals(0.6000, res3.infrastructureExposureScore(), 0.0001);
        assertEquals(ExposureCategory.HIGH, res3.exposureCategory());

        // LOW criticality multiplier = 0.80 -> 0.60 * 1.0 * 0.80 = 0.4800 -> HIGH
        var res4 = engine.calculateInfrastructureScore(0.60, 0.0, 5000.0, InfrastructureCriticality.LOW, 1.0);
        assertEquals(0.4800, res4.infrastructureExposureScore(), 0.0001);
        assertEquals(ExposureCategory.HIGH, res4.exposureCategory());
    }

    @Test
    @DisplayName("4.3.2: Linear Infrastructure Length Ratio & Distance Decay")
    void testLinearLengthRatioAndDistanceDecay() {
        // Midpoint distance (2500m / 5000m -> P = 0.55), Half length affected (lenRatio = 0.5)
        // Hazard: 0.80, High Criticality (1.15)
        // Raw = 0.80 * 0.55 * 1.15 * 0.5 = 0.2530 -> MODERATE (0.15 - 0.40)
        var res = engine.calculateInfrastructureScore(0.80, 2500.0, 5000.0, InfrastructureCriticality.HIGH, 0.5);
        assertEquals(0.2530, res.infrastructureExposureScore(), 0.0001);
        assertEquals(ExposureCategory.MODERATE, res.exposureCategory());
    }

    @Test
    @DisplayName("4.3.3: Boundary Clamping & Null Handling")
    void testBoundaryClampingAndNulls() {
        // Null hazard defaults to 0.5000, null criticality to 1.0, null length to 1.0
        var res = engine.calculateInfrastructureScore(null, null, null, null, null);
        assertEquals(0.5000, res.infrastructureExposureScore(), 0.0001);
        assertEquals(ExposureCategory.HIGH, res.exposureCategory());

        // Score capped at 1.0000
        var resMax = engine.calculateInfrastructureScore(1.0, 0.0, 5000.0, InfrastructureCriticality.VERY_HIGH, 1.0);
        assertEquals(1.0000, resMax.infrastructureExposureScore(), 0.0001);
        assertEquals(ExposureCategory.VERY_HIGH, resMax.exposureCategory());
    }
}
