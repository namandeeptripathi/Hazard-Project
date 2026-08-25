package com.hazard;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.service.exposure.PopulationExposureConfig;
import com.hazard.service.exposure.PopulationExposureEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PopulationExposureEngine and PopulationExposureConfig.
 */
class PopulationExposureEngineTests {

    private PopulationExposureConfig config;
    private PopulationExposureEngine engine;

    @BeforeEach
    void setUp() {
        config = new PopulationExposureConfig();
        engine = new PopulationExposureEngine(config);
    }

    @Test
    @DisplayName("4.1.1: Calculate Exposure — Standard Ratio, Score & Category")
    void testStandardExposureCalculation() {
        var result = engine.calculateExposure(100000L, 25000L);

        assertEquals(100000L, result.totalPopulation());
        assertEquals(25000L, result.exposedPopulation());
        assertEquals(75000L, result.unexposedPopulation());
        assertEquals(25.0000, result.exposurePercentage(), 0.0001);
        assertEquals(0.2500, result.exposureScore(), 0.0001);
        assertEquals(ExposureCategory.MODERATE, result.exposureCategory());
    }

    @Test
    @DisplayName("4.1.2: Calculate Exposure — Category Thresholds (LOW, MODERATE, HIGH, VERY_HIGH)")
    void testCategoryThresholds() {
        // LOW (< 15%)
        assertEquals(ExposureCategory.LOW, engine.calculateExposure(100000L, 5000L).exposureCategory());
        assertEquals(ExposureCategory.LOW, engine.calculateExposure(100000L, 14900L).exposureCategory());

        // MODERATE (15% <= pct < 40%)
        assertEquals(ExposureCategory.MODERATE, engine.calculateExposure(100000L, 15000L).exposureCategory());
        assertEquals(ExposureCategory.MODERATE, engine.calculateExposure(100000L, 39900L).exposureCategory());

        // HIGH (40% <= pct < 70%)
        assertEquals(ExposureCategory.HIGH, engine.calculateExposure(100000L, 40000L).exposureCategory());
        assertEquals(ExposureCategory.HIGH, engine.calculateExposure(100000L, 69900L).exposureCategory());

        // VERY_HIGH (>= 70%)
        assertEquals(ExposureCategory.VERY_HIGH, engine.calculateExposure(100000L, 70000L).exposureCategory());
        assertEquals(ExposureCategory.VERY_HIGH, engine.calculateExposure(100000L, 95000L).exposureCategory());
        assertEquals(ExposureCategory.VERY_HIGH, engine.calculateExposure(100000L, 100000L).exposureCategory());
    }

    @Test
    @DisplayName("4.1.3: Boundary Cases — Zero Population & Full Population Exposure")
    void testBoundaryCases() {
        // Zero total population
        var zeroTotal = engine.calculateExposure(0L, 0L);
        assertEquals(0L, zeroTotal.totalPopulation());
        assertEquals(0L, zeroTotal.exposedPopulation());
        assertEquals(0.0, zeroTotal.exposurePercentage());
        assertEquals(0.0, zeroTotal.exposureScore());
        assertEquals(ExposureCategory.LOW, zeroTotal.exposureCategory());

        // Full exposure (100%)
        var full = engine.calculateExposure(50000L, 50000L);
        assertEquals(100.0, full.exposurePercentage());
        assertEquals(1.0, full.exposureScore());
        assertEquals(ExposureCategory.VERY_HIGH, full.exposureCategory());

        // Exposed > Total (must be safely clamped)
        var clamped = engine.calculateExposure(50000L, 80000L);
        assertEquals(50000L, clamped.exposedPopulation());
        assertEquals(100.0, clamped.exposurePercentage());
        assertEquals(1.0, clamped.exposureScore());
    }

    @Test
    @DisplayName("4.1.4: Dynamic Configurable Thresholds")
    void testConfigurableThresholds() {
        // Change thresholds dynamically
        config.setLowThresholdPercent(10.0);
        config.setModerateThresholdPercent(30.0);
        config.setHighThresholdPercent(60.0);

        // 12% is now MODERATE under custom thresholds (since lowThreshold is 10.0)
        assertEquals(ExposureCategory.MODERATE, config.classifyExposurePercentage(12.0));

        // 35% is now HIGH under custom thresholds (since moderateThreshold is 30.0)
        assertEquals(ExposureCategory.HIGH, config.classifyExposurePercentage(35.0));

        // 65% is now VERY_HIGH under custom thresholds (since highThreshold is 60.0)
        assertEquals(ExposureCategory.VERY_HIGH, config.classifyExposurePercentage(65.0));
    }

    @Test
    @DisplayName("4.1.5: Settlement Archetype Fallback Resolution")
    void testSettlementArchetypeFallback() {
        assertEquals(100000L, config.resolveArchetypePopulation("city", null));
        assertEquals(20000L, config.resolveArchetypePopulation("town", null));
        assertEquals(2500L, config.resolveArchetypePopulation("village", null));
        assertEquals(350L, config.resolveArchetypePopulation("hamlet", null));
        assertEquals(1200L, config.resolveArchetypePopulation(null, "residential"));
    }
}
