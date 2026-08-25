package com.hazard;

import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.risk.contributor.ContributorImportance;
import com.hazard.domain.risk.explain.EvidenceType;
import com.hazard.domain.risk.explain.SensitivityImpactTier;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.ExposureSubBreakdownDto;
import com.hazard.dto.risk.RiskComponentDetailDto;
import com.hazard.dto.risk.contributor.DetailedRiskContributorDto;
import com.hazard.dto.risk.contributor.DistrictRiskContributorsProfileDto;
import com.hazard.dto.risk.explain.*;
import com.hazard.service.risk.explain.RiskExplanationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for Stage 4.10 — Risk Explanation Engine.
 */
@DisplayName("Stage 4.10: Risk Explanation Engine Tests")
public class RiskExplanationEngineTests {

    private RiskExplanationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RiskExplanationEngine();
    }

    private DistrictRiskScoreDto createMockRiskDto() {
        DistrictRiskScoreDto dto = new DistrictRiskScoreDto();
        dto.setDistrictName("Sitamarhi");
        dto.setDistrictId(32);
        dto.setRiskScore(0.4228);
        dto.setRiskScore100(42.3);
        dto.setRiskTier(RiskTier.HIGH);

        Map<String, RiskComponentDetailDto> comps = new LinkedHashMap<>();

        RiskComponentDetailDto h = new RiskComponentDetailDto();
        h.setComponentType(com.hazard.domain.risk.RiskComponentType.HAZARD);
        h.setScore(0.60);
        h.setConfiguredWeight(0.35);
        h.setEffectiveWeight(0.35);
        h.setContribution(0.2100);
        comps.put("HAZARD", h);

        RiskComponentDetailDto e = new RiskComponentDetailDto();
        e.setComponentType(com.hazard.domain.risk.RiskComponentType.EXPOSURE);
        e.setScore(0.38);
        e.setConfiguredWeight(0.30);
        e.setEffectiveWeight(0.30);
        e.setContribution(0.1140);
        comps.put("EXPOSURE", e);

        RiskComponentDetailDto v = new RiskComponentDetailDto();
        v.setComponentType(com.hazard.domain.risk.RiskComponentType.VULNERABILITY);
        v.setScore(0.2470);
        v.setConfiguredWeight(0.25);
        v.setEffectiveWeight(0.25);
        v.setContribution(0.0618);
        comps.put("VULNERABILITY", v);

        RiskComponentDetailDto t = new RiskComponentDetailDto();
        t.setComponentType(com.hazard.domain.risk.RiskComponentType.HISTORICAL);
        t.setScore(0.3700);
        t.setConfiguredWeight(0.10);
        t.setEffectiveWeight(0.10);
        t.setContribution(0.0370);
        comps.put("HISTORICAL", t);

        dto.setComponents(comps);

        ExposureSubBreakdownDto exp = new ExposureSubBreakdownDto();
        exp.setPopulationExposureScore(0.2226);
        exp.setPopulationConfiguredWeight(0.40);
        exp.setExposedPopulation(94293L);
        exp.setExposedPopulationPercentage(22.3);

        exp.setSettlementExposureScore(0.5000);
        exp.setSettlementConfiguredWeight(0.25);
        exp.setSettlementsExposedCount(1878);

        exp.setInfrastructureExposureScore(0.4743);
        exp.setInfrastructureConfiguredWeight(0.35);
        exp.setInfrastructureAssetsExposedCount(31);

        dto.setExposureSubBreakdown(exp);
        return dto;
    }

    private DistrictRiskContributorsProfileDto createMockContributorsProfile(DistrictRiskScoreDto risk) {
        DistrictRiskContributorsProfileDto profile = new DistrictRiskContributorsProfileDto();
        profile.setDistrictName(risk.getDistrictName());
        profile.setRiskScore(risk.getRiskScore());
        profile.setRiskScore100(risk.getRiskScore100());
        profile.setRiskTier(risk.getRiskTier());

        List<DetailedRiskContributorDto> list = new ArrayList<>();
        list.add(new DetailedRiskContributorDto("HAZARD", "Hazard Severity & Intensity", "HAZARD", 1,
                0.60, 0.60, 0.35, 0.35, 0.2100, 49.7, 1, ContributorImportance.DOMINANT, "Stage 3", "Hazard footprint"));
        list.add(new DetailedRiskContributorDto("EXPOSURE", "Combined Exposure", "EXPOSURE", 1,
                0.38, 0.38, 0.30, 0.30, 0.1140, 27.0, 2, ContributorImportance.DOMINANT, "Stages 4.1-4.3", "Exposure"));
        list.add(new DetailedRiskContributorDto("VULNERABILITY", "Vulnerability & Coping Deficit", "VULNERABILITY", 1,
                0.247, 0.247, 0.25, 0.25, 0.0618, 14.6, 3, ContributorImportance.MODERATE, "Stage 4.5", "Vulnerability"));
        list.add(new DetailedRiskContributorDto("HISTORICAL", "Historical Disaster Evidence", "HISTORICAL", 1,
                0.37, 0.37, 0.10, 0.10, 0.0370, 8.8, 4, ContributorImportance.MODERATE, "Stage 4.6", "Historical"));

        profile.setAllContributors(list);
        profile.setTopContributors(list);
        return profile;
    }

    @Test
    @DisplayName("Generate complete explainability profile with multi-level summaries")
    void testBuildExplainabilityProfile() {
        DistrictRiskScoreDto risk = createMockRiskDto();
        DistrictRiskContributorsProfileDto contrib = createMockContributorsProfile(risk);

        DistrictRiskExplainabilityProfileDto profile = engine.buildExplainabilityProfile(contrib, risk, null, null, null);

        assertNotNull(profile);
        assertEquals("Sitamarhi", profile.getDistrictName());
        assertEquals("explain-v1", profile.getExplanationVersion());
        assertEquals(0.4228, profile.getRiskScore(), 0.001);

        // Summaries
        assertNotNull(profile.getSummary());
        assertTrue(profile.getSummary().getExecutiveSummary().contains("Sitamarhi"));
        assertTrue(profile.getSummary().getExecutiveSummary().contains("HIGH"));
        assertNotNull(profile.getSummary().getShortSummary());
        assertNotNull(profile.getSummary().getDetailedNarrative());

        // Primary vs Secondary Drivers
        assertFalse(profile.getPrimaryDrivers().isEmpty());
        assertEquals("HAZARD", profile.getPrimaryDrivers().get(0).getId());
    }

    @Test
    @DisplayName("Build structured evidence items with explicit provenance")
    void testBuildEvidenceCatalog() {
        DistrictRiskScoreDto risk = createMockRiskDto();
        List<ExplainableEvidenceItemDto> items = engine.buildEvidenceCatalog(risk, null, null, null);

        assertNotNull(items);
        assertFalse(items.isEmpty());

        // Verify evidence contains Hazard, Population, Settlement, Infrastructure evidence
        boolean hasHazard = items.stream().anyMatch(e -> e.getType() == EvidenceType.HAZARD_EVIDENCE);
        boolean hasPop = items.stream().anyMatch(e -> e.getType() == EvidenceType.POPULATION_EVIDENCE);
        boolean hasInfra = items.stream().anyMatch(e -> e.getType() == EvidenceType.INFRASTRUCTURE_EVIDENCE);

        assertTrue(hasHazard, "Should contain hazard evidence");
        assertTrue(hasPop, "Should contain population evidence");
        assertTrue(hasInfra, "Should contain infrastructure evidence");

        // Verify provenance is not null
        for (ExplainableEvidenceItemDto item : items) {
            assertNotNull(item.getProvenance());
            assertNotNull(item.getSourceStage());
        }
    }

    @Test
    @DisplayName("Build mathematical calculation trace reconciling with final score")
    void testBuildCalculationTrace() {
        DistrictRiskScoreDto risk = createMockRiskDto();
        DistrictRiskContributorsProfileDto contrib = createMockContributorsProfile(risk);

        CalculationTraceDto trace = engine.buildCalculationTrace(risk, contrib.getAllContributors());

        assertNotNull(trace);
        assertTrue(trace.isReconciled());
        assertEquals(0.4228, trace.getSumOfContributions(), 0.001);
        assertNotNull(trace.getFormulaString());
        assertNotNull(trace.getParameterizedFormulaString());
        assertEquals(4, trace.getComponents().size());
    }

    @Test
    @DisplayName("Perform one-at-a-time model sensitivity analysis and rank leverage")
    void testBuildSensitivityAnalysis() {
        DistrictRiskScoreDto risk = createMockRiskDto();
        DistrictRiskContributorsProfileDto contrib = createMockContributorsProfile(risk);

        List<ComponentSensitivityDto> sensitivity = engine.buildSensitivityAnalysis(risk, contrib.getAllContributors());

        assertNotNull(sensitivity);
        assertEquals(4, sensitivity.size());

        // Hazard has highest weight (0.35) -> highest leverage rank #1
        ComponentSensitivityDto topLeverage = sensitivity.get(0);
        assertEquals("HAZARD", topLeverage.getComponentId());
        assertEquals(1, topLeverage.getLeverageRank());
        assertEquals(0.035, topLeverage.getAbsoluteLeverageImpact(), 0.001);
        assertEquals(SensitivityImpactTier.HIGH_LEVERAGE, topLeverage.getLeverageTier());
    }

    @Test
    @DisplayName("Model limitations are explicitly defined and non-empty")
    void testModelLimitations() {
        List<String> limitations = engine.buildModelLimitations();
        assertNotNull(limitations);
        assertTrue(limitations.size() >= 4);
        assertTrue(limitations.stream().anyMatch(l -> l.contains("Decision Support Metric")));
    }
}
