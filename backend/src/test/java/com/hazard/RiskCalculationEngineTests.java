package com.hazard;

import com.hazard.domain.risk.RiskComponentType;
import com.hazard.domain.risk.RiskDataCompletenessStatus;
import com.hazard.domain.risk.RiskTier;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.ExposureSubBreakdownDto;
import com.hazard.service.risk.RiskCalculationConfig;
import com.hazard.service.risk.RiskCalculationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RiskCalculationEngine (4-pillar weighted risk calculation, exposure sub-aggregation,
 * missing-data redistribution, 5-tier classification, and ranked contributors).
 */
class RiskCalculationEngineTests {

    private RiskCalculationConfig config;
    private RiskCalculationEngine engine;

    @BeforeEach
    void setUp() {
        config = new RiskCalculationConfig();
        engine = new RiskCalculationEngine(config);
    }

    @Test
    @DisplayName("4.7.1: Combined Exposure Aggregation (Pop 40%, Settle 25%, Infra 35%)")
    void testCalculateCombinedExposure() {
        // Pop=0.80, Settle=0.60, Infra=0.70
        // Combined = (0.40 * 0.80) + (0.25 * 0.60) + (0.35 * 0.70) = 0.32 + 0.15 + 0.245 = 0.7150
        ExposureSubBreakdownDto exp = engine.calculateCombinedExposure(0.80, 0.60, 0.70, 500000L, 75.0, 120, 25, null);

        assertNotNull(exp);
        assertEquals(0.7150, exp.getCombinedExposureScore(), 0.0001);
        assertEquals(71.5, exp.getCombinedExposureScore100(), 0.1);
        assertEquals(0.3200, exp.getPopulationContribution(), 0.0001);
        assertEquals(0.1500, exp.getSettlementContribution(), 0.0001);
        assertEquals(0.2450, exp.getInfrastructureContribution(), 0.0001);
    }

    @Test
    @DisplayName("4.7.2: Standard 4-Pillar Disaster Risk Calculation (35% H, 30% E, 25% V, 10% T)")
    void testStandardRiskCalculation() {
        // H = 0.80, E = 0.70, V = 0.60, T = 0.50
        // R = (0.35 * 0.80) + (0.30 * 0.70) + (0.25 * 0.60) + (0.10 * 0.50)
        // R = 0.280 + 0.210 + 0.150 + 0.050 = 0.6900 (VERY_HIGH)
        DistrictRiskScoreDto result = engine.calculateDistrictRisk(
                "Sitamarhi", 0.80, 0.70, 0.60, 0.50, null, null, null);

        assertNotNull(result);
        assertEquals(0.6900, result.getRiskScore(), 0.0001);
        assertEquals(69.0, result.getRiskScore100(), 0.1);
        assertEquals(RiskTier.VERY_HIGH, result.getRiskTier());
        assertEquals(RiskDataCompletenessStatus.DATA_COMPLETE, result.getDataQuality().getStatus());
        assertEquals(4, result.getDataQuality().getAvailableComponents());

        // Verify component contributions
        assertEquals(0.2800, result.getComponents().get("HAZARD").getContribution(), 0.0001);
        assertEquals(0.2100, result.getComponents().get("EXPOSURE").getContribution(), 0.0001);
        assertEquals(0.1500, result.getComponents().get("VULNERABILITY").getContribution(), 0.0001);
        assertEquals(0.0500, result.getComponents().get("HISTORICAL").getContribution(), 0.0001);
    }

    @Test
    @DisplayName("4.7.3: Active-Weight Redistribution for Missing Components")
    void testMissingComponentRedistribution() {
        // Suppose Historical is missing (null)
        // Active components: H=0.80 (w=0.35), E=0.70 (w=0.30), V=0.60 (w=0.25). Total active W = 0.90
        // Eff H = 0.35/0.90 = 0.3889, Eff E = 0.30/0.90 = 0.3333, Eff V = 0.25/0.90 = 0.2778
        // Weighted sum = (0.35*0.80 + 0.30*0.70 + 0.25*0.60) / 0.90 = (0.28 + 0.21 + 0.15) / 0.90 = 0.64 / 0.90 = 0.7111
        DistrictRiskScoreDto result = engine.calculateDistrictRisk(
                "Patna", 0.80, 0.70, 0.60, null, null, null, null);

        assertNotNull(result);
        assertEquals(0.7111, result.getRiskScore(), 0.001);
        assertEquals(71.1, result.getRiskScore100(), 0.2);
        assertEquals(RiskTier.VERY_HIGH, result.getRiskTier());
        assertEquals(RiskDataCompletenessStatus.DATA_PARTIAL, result.getDataQuality().getStatus());
        assertEquals(3, result.getDataQuality().getAvailableComponents());
        assertNull(result.getComponents().get("HISTORICAL"));
    }

    @Test
    @DisplayName("4.7.4: 5-Tier Classification Boundaries")
    void testRiskTierBoundaries() {
        assertEquals(RiskTier.LOW, RiskTier.fromScore(0.19));
        assertEquals(RiskTier.MODERATE, RiskTier.fromScore(0.20));
        assertEquals(RiskTier.MODERATE, RiskTier.fromScore(0.39));
        assertEquals(RiskTier.HIGH, RiskTier.fromScore(0.40));
        assertEquals(RiskTier.HIGH, RiskTier.fromScore(0.59));
        assertEquals(RiskTier.VERY_HIGH, RiskTier.fromScore(0.60));
        assertEquals(RiskTier.VERY_HIGH, RiskTier.fromScore(0.79));
        assertEquals(RiskTier.CRITICAL, RiskTier.fromScore(0.80));
        assertEquals(RiskTier.CRITICAL, RiskTier.fromScore(0.95));
    }

    @Test
    @DisplayName("4.7.5: Ranked Top Risk Contributors")
    void testTopContributorsRanking() {
        // H=0.90 (contrib=0.315), E=0.40 (contrib=0.120), V=0.20 (contrib=0.050), T=0.80 (contrib=0.080)
        DistrictRiskScoreDto result = engine.calculateDistrictRisk(
                "Supaul", 0.90, 0.40, 0.20, 0.80, null, null, null);

        assertFalse(result.getTopContributors().isEmpty());
        assertEquals("Hazard Severity & Intensity", result.getTopContributors().get(0).getName());
        assertEquals(0.3150, result.getTopContributors().get(0).getContribution(), 0.0001);
    }

    @Test
    @DisplayName("4.7.6: Insufficient Data Handling (< 2 pillars)")
    void testInsufficientData() {
        // Only 1 component available
        DistrictRiskScoreDto result = engine.calculateDistrictRisk(
                "SparseDistrict", 0.80, null, null, null, null, null, null);

        assertEquals(0.0, result.getRiskScore());
        assertEquals(RiskDataCompletenessStatus.INSUFFICIENT_DATA, result.getDataQuality().getStatus());
        assertTrue(result.getExplanation().contains("Insufficient data"));
    }

    @Test
    @DisplayName("4.7.7: Weight Validation (Invalid negative weights)")
    void testInvalidWeights() {
        Map<RiskComponentType, Double> invalidWeights = new HashMap<>();
        invalidWeights.put(RiskComponentType.HAZARD, -0.5);

        assertThrows(IllegalArgumentException.class, () ->
                config.validateComponentWeights(invalidWeights)
        );
    }
}
