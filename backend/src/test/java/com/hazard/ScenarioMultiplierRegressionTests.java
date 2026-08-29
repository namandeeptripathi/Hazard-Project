package com.hazard;

import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.domain.scenario.ScenarioType;
import com.hazard.dto.scenario.ScenarioCreateRequestDto;
import com.hazard.dto.scenario.ScenarioDto;
import com.hazard.dto.scenario.ScenarioExecutionRequestDto;
import com.hazard.dto.scenario.ScenarioSimulationContextDto;
import com.hazard.dto.scenario.ScenarioSimulationResultDto;
import com.hazard.repository.scenario.ScenarioRepository;
import com.hazard.service.risk.RiskCalculationEngine;
import com.hazard.service.scenario.ScenarioExecutionService;
import com.hazard.service.scenario.ScenarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 9B — Focused Regression Tests for Multiplier Arithmetic,
 * Clamp Correctness, Negative Scenarios, and Cross-Factor Independence.
 *
 * These tests verify that:
 * 1. The compound multiplier is NOT erroneously clamped to [0.0, 1.0].
 * 2. The [0.0, 1.0] clamp is applied only to the simulated pillar INPUT SCORE,
 *    not to the multiplier itself.
 * 3. Negative perturbations correctly reduce multipliers below 1.0.
 * 4. Population and hazard multipliers are mathematically independent.
 * 5. Baseline execution produces exact zero delta.
 */
@SpringBootTest
@Transactional
class ScenarioMultiplierRegressionTests {

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private ScenarioExecutionService scenarioExecutionService;

    @BeforeEach
    void setUp() {
        scenarioRepository.resetToBaselineOnly();
    }

    // =========================================================================
    // 1. SINGLE-FACTOR MULTIPLIER ARITHMETIC
    // =========================================================================

    @Test
    @DisplayName("Rainfall +20%: multiplier must be exactly 1.20, simHazard must equal baseHazard × 1.20")
    void testRainfall20_multiplierAndHazardScore() {
        ScenarioDto s = scenarioService.createScenario(new ScenarioCreateRequestDto(
                "Rain +20%", "RAINFALL_CHANGE", "Test", 20.0, 0.0, 0.0
        ));
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                s.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();
        assertEquals(1.20, ctx.getEffectiveHazardMultiplier(), 0.0001,
                "Multiplier must be 1.20 for +20% rainfall");

        double expectedSimHazard = RiskCalculationEngine.round4(
                ctx.getBaselineHazardScore() * 1.20
        );
        // Clamp to [0,1] as the implementation does
        expectedSimHazard = Math.min(1.0, Math.max(0.0, expectedSimHazard));
        assertEquals(expectedSimHazard, ctx.getSimulatedHazardScore(), 0.0001,
                "Simulated hazard score must equal baseHazard × 1.20, clamped to [0,1]");

        // Population multiplier must remain 1.0 (independent)
        assertEquals(1.0, ctx.getEffectivePopulationMultiplier(), 0.0001,
                "Population multiplier must be 1.0 when only rainfall changes");
    }

    @Test
    @DisplayName("Hazard +10%: multiplier must be exactly 1.10, simHazard must equal baseHazard × 1.10")
    void testHazard10_multiplierAndHazardScore() {
        ScenarioDto s = scenarioService.createScenario(new ScenarioCreateRequestDto(
                "Haz +10%", "HAZARD_INTENSITY", "Test", 0.0, 10.0, 0.0
        ));
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                s.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();
        assertEquals(1.10, ctx.getEffectiveHazardMultiplier(), 0.0001);

        double expected = Math.min(1.0, Math.max(0.0,
                RiskCalculationEngine.round4(ctx.getBaselineHazardScore() * 1.10)));
        assertEquals(expected, ctx.getSimulatedHazardScore(), 0.0001);
    }

    // =========================================================================
    // 2. COMPOUND MULTIPLIER: Rain +20% + Hazard +10% = 1.32
    // =========================================================================

    @Test
    @DisplayName("Rain +20% + Hazard +10%: compound multiplier must be 1.32, NOT clamped to 1.0")
    void testCompoundMultiplier_notClampedToOne() {
        ScenarioDto s = scenarioService.createScenario(new ScenarioCreateRequestDto(
                "Compound Test", "MULTI_FACTOR", "Test", 20.0, 10.0, 0.0
        ));
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                s.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();

        // CRITICAL: The multiplier must be 1.32, not 1.0
        assertEquals(1.32, ctx.getEffectiveHazardMultiplier(), 0.0001,
                "Compound multiplier 1.20 × 1.10 must be 1.32, NOT clamped to 1.0");

        // The simulated hazard score must reflect the full +32% increase
        double expectedSimHazard = Math.min(1.0, Math.max(0.0,
                RiskCalculationEngine.round4(ctx.getBaselineHazardScore() * 1.32)));
        assertEquals(expectedSimHazard, ctx.getSimulatedHazardScore(), 0.0001,
                "Simulated hazard score must be baseHazard × 1.32, clamped only at pillar boundary [0,1]");

        // The simulated hazard must be strictly greater than baseline
        assertTrue(ctx.getSimulatedHazardScore() > ctx.getBaselineHazardScore(),
                "Compound scenario must increase hazard score");

        // Population multiplier must remain 1.0
        assertEquals(1.0, ctx.getEffectivePopulationMultiplier(), 0.0001);
    }

    // =========================================================================
    // 3. NEGATIVE PERTURBATION
    // =========================================================================

    @Test
    @DisplayName("Rainfall -20%: multiplier must be 0.80, simHazard must decrease")
    void testNegativeRainfall_multiplierBelow1() {
        ScenarioDto s = scenarioService.createScenario(new ScenarioCreateRequestDto(
                "Rain -20%", "RAINFALL_CHANGE", "Test", -20.0, 0.0, 0.0
        ));
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                s.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();
        assertEquals(0.80, ctx.getEffectiveHazardMultiplier(), 0.0001,
                "Negative rainfall -20% must produce multiplier 0.80");

        assertTrue(ctx.getSimulatedHazardScore() < ctx.getBaselineHazardScore(),
                "Negative rainfall must decrease hazard score");
        assertEquals("DECREASED", r.getRiskDirection());
    }

    @Test
    @DisplayName("Hazard -50%: multiplier must be 0.50, simHazard must halve")
    void testNegativeHazard50_multiplierHalves() {
        ScenarioDto s = scenarioService.createScenario(new ScenarioCreateRequestDto(
                "Haz -50%", "HAZARD_INTENSITY", "Test", 0.0, -50.0, 0.0
        ));
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                s.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();
        assertEquals(0.50, ctx.getEffectiveHazardMultiplier(), 0.0001,
                "Hazard -50% must produce multiplier 0.50");

        double expected = RiskCalculationEngine.round4(ctx.getBaselineHazardScore() * 0.50);
        assertEquals(expected, ctx.getSimulatedHazardScore(), 0.0001,
                "Simulated hazard must be exactly half of baseline");
    }

    @Test
    @DisplayName("Population -30%: multiplier must be 0.70, simPop must decrease")
    void testNegativePopulation30_multiplierReduces() {
        ScenarioDto s = scenarioService.createScenario(new ScenarioCreateRequestDto(
                "Pop -30%", "POPULATION_EXPOSURE", "Test", 0.0, 0.0, -30.0
        ));
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                s.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();
        assertEquals(0.70, ctx.getEffectivePopulationMultiplier(), 0.0001,
                "Population -30% must produce multiplier 0.70");

        long expectedPop = Math.round(ctx.getBaselineExposedPopulation() * 0.70);
        assertEquals(expectedPop, ctx.getSimulatedExposedPopulation(),
                "Simulated exposed population must be baseline × 0.70");

        // Hazard multiplier must remain 1.0
        assertEquals(1.0, ctx.getEffectiveHazardMultiplier(), 0.0001,
                "Hazard multiplier must not be affected by population changes");
    }

    // =========================================================================
    // 4. MIXED POSITIVE/NEGATIVE COMPOUND
    // =========================================================================

    @Test
    @DisplayName("Rain -20% + Hazard +10%: compound multiplier must be 0.88")
    void testMixedCompound_negativeRainPositiveHazard() {
        ScenarioDto s = scenarioService.createScenario(new ScenarioCreateRequestDto(
                "Mixed Compound", "MULTI_FACTOR", "Test", -20.0, 10.0, 0.0
        ));
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                s.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();
        // 0.80 × 1.10 = 0.88
        assertEquals(0.88, ctx.getEffectiveHazardMultiplier(), 0.0001,
                "Compound 0.80 × 1.10 must be 0.88");

        assertTrue(ctx.getSimulatedHazardScore() < ctx.getBaselineHazardScore(),
                "Net negative compound must decrease hazard score");
    }

    // =========================================================================
    // 5. FINAL RISK SCORE UPPER BOUND (NOT MULTIPLIER CLAMP)
    // =========================================================================

    @Test
    @DisplayName("Extreme +200% rainfall: simHazard clamped at 1.0, multiplier stays at 3.0")
    void testExtremeRainfall_pillarClampNotMultiplierClamp() {
        ScenarioDto s = scenarioService.createScenario(new ScenarioCreateRequestDto(
                "Extreme Rain +200%", "RAINFALL_CHANGE", "Test", 200.0, 0.0, 0.0
        ));
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                s.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();

        // Multiplier must be 3.0, NOT clamped to 1.0
        assertEquals(3.0, ctx.getEffectiveHazardMultiplier(), 0.0001,
                "Extreme +200% must produce multiplier 3.0, NOT clamp to 1.0");

        // The simulated hazard score is clamped at [0, 1] — pillar domain
        assertTrue(ctx.getSimulatedHazardScore() <= 1.0,
                "Simulated hazard score must be clamped at pillar ceiling 1.0");
        assertTrue(ctx.getSimulatedHazardScore() >= ctx.getBaselineHazardScore(),
                "Even with clamping, simulated hazard must be >= baseline");

        // Final risk score must also be in [0, 1]
        assertTrue(r.getSimulatedRisk().getRiskScore() <= 1.0);
        assertTrue(r.getSimulatedRisk().getRiskScore() >= 0.0);
    }

    // =========================================================================
    // 6. BASELINE EQUIVALENCE
    // =========================================================================

    @Test
    @DisplayName("Baseline scenario: all multipliers 1.0, all deltas 0.0, exact score match")
    void testBaselineEquivalence() {
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                ScenarioDefinition.BASELINE_SCENARIO_ID, new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();

        assertEquals(1.0, ctx.getEffectiveHazardMultiplier(), 0.0001);
        assertEquals(1.0, ctx.getEffectivePopulationMultiplier(), 0.0001);
        assertEquals(ctx.getBaselineHazardScore(), ctx.getSimulatedHazardScore(), 0.0001);
        assertEquals(ctx.getBaselinePopulationExposureScore(), ctx.getSimulatedPopulationExposureScore(), 0.0001);
        assertEquals(ctx.getBaselineExposedPopulation(), ctx.getSimulatedExposedPopulation());
        assertEquals(ctx.getBaselineVulnerabilityScore(), ctx.getSimulatedVulnerabilityScore(), 0.0001);
        assertEquals(ctx.getBaselineHistoricalScore(), ctx.getSimulatedHistoricalScore(), 0.0001);

        assertEquals(0.0, r.getDeltaRiskScore(), 0.0001);
        assertEquals(0.0, r.getDeltaRiskScore100(), 0.0001);
        assertEquals("UNCHANGED", r.getRiskDirection());

        assertEquals(r.getBaselineRisk().getRiskScore(), r.getSimulatedRisk().getRiskScore(), 0.0001);
    }

    // =========================================================================
    // 7. CROSS-FACTOR INDEPENDENCE
    // =========================================================================

    @Test
    @DisplayName("Population +15% must NOT modify the hazard multiplier")
    void testPopulationDoesNotAffectHazard() {
        ScenarioDto s = scenarioService.createScenario(new ScenarioCreateRequestDto(
                "Pop +15%", "POPULATION_EXPOSURE", "Test", 0.0, 0.0, 15.0
        ));
        ScenarioSimulationResultDto r = scenarioExecutionService.executeScenario(
                s.getScenarioId(), new ScenarioExecutionRequestDto("Sitamarhi")
        );

        ScenarioSimulationContextDto ctx = r.getSimulationContext();
        assertEquals(1.15, ctx.getEffectivePopulationMultiplier(), 0.0001);
        assertEquals(1.0, ctx.getEffectiveHazardMultiplier(), 0.0001,
                "Hazard multiplier must remain 1.0 when only population changes");

        // Hazard score must be unchanged
        assertEquals(ctx.getBaselineHazardScore(), ctx.getSimulatedHazardScore(), 0.0001,
                "Hazard score must not change when only population is perturbed");

        // Population must be scaled
        long expectedPop = Math.round(ctx.getBaselineExposedPopulation() * 1.15);
        assertEquals(expectedPop, ctx.getSimulatedExposedPopulation());
    }
}
