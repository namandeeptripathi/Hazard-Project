package com.hazard;

import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.risk.contributor.ContributorImportance;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.ExposureSubBreakdownDto;
import com.hazard.dto.risk.RiskComponentDetailDto;
import com.hazard.dto.risk.contributor.ContributorTreeNodeDto;
import com.hazard.dto.risk.contributor.DetailedRiskContributorDto;
import com.hazard.dto.risk.contributor.DistrictRiskContributorsProfileDto;
import com.hazard.service.risk.contributor.RiskContributorEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for Stage 4.9 — Risk Contributor Engine.
 */
@DisplayName("Stage 4.9: Risk Contributor Engine Tests")
public class RiskContributorEngineTests {

    private RiskContributorEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RiskContributorEngine();
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

    @Test
    @DisplayName("Decompose risk into ranked contributors with percentage shares")
    void testBuildContributorsProfile() {
        DistrictRiskScoreDto risk = createMockRiskDto();
        DistrictRiskContributorsProfileDto profile = engine.buildContributorsProfile(risk, null, null, 5);

        assertNotNull(profile);
        assertEquals("Sitamarhi", profile.getDistrictName());
        assertEquals(0.4228, profile.getRiskScore(), 0.001);
        assertEquals(RiskTier.HIGH, profile.getRiskTier());

        assertFalse(profile.getTopContributors().isEmpty());
        DetailedRiskContributorDto topDriver = profile.getTopContributors().get(0);
        assertEquals("HAZARD", topDriver.getId());
        assertEquals(1, topDriver.getRank());
        assertTrue(topDriver.getContributionPercent() > 40.0);
        assertEquals(ContributorImportance.DOMINANT, topDriver.getImportance());
    }

    @Test
    @DisplayName("Mathematical consistency validation passes for 4-pillar sum")
    void testMathematicalConsistency() {
        DistrictRiskScoreDto risk = createMockRiskDto();
        DistrictRiskContributorsProfileDto profile = engine.buildContributorsProfile(risk, null, null, 10);

        Map<String, Object> check = profile.getMathematicalCheck();
        assertNotNull(check);
        assertEquals(true, check.get("isContributionConsistent"));
        assertEquals(true, check.get("isPercentageConsistent"));
        assertEquals("MATHEMATICALLY_CONSISTENT", check.get("validationStatus"));
    }

    @Test
    @DisplayName("Hierarchical contributor tree contains root and 4 pillar branches")
    void testHierarchicalTree() {
        DistrictRiskScoreDto risk = createMockRiskDto();
        DistrictRiskContributorsProfileDto profile = engine.buildContributorsProfile(risk, null, null, 10);

        ContributorTreeNodeDto tree = profile.getContributorTree();
        assertNotNull(tree);
        assertEquals("TOTAL_RISK", tree.getId());
        assertEquals(4, tree.getChildren().size());

        // Verify exposure branch has 3 sub-children
        ContributorTreeNodeDto expBranch = tree.getChildren().stream()
                .filter(c -> "EXPOSURE".equals(c.getId()))
                .findFirst().orElse(null);

        assertNotNull(expBranch);
        assertEquals(3, expBranch.getChildren().size());
    }

    @Test
    @DisplayName("Dynamic natural-language explanation generates coherent narrative")
    void testExplanationGeneration() {
        DistrictRiskScoreDto risk = createMockRiskDto();
        DistrictRiskContributorsProfileDto profile = engine.buildContributorsProfile(risk, null, null, 5);

        assertNotNull(profile.getExplanation());
        assertTrue(profile.getExplanation().getSummaryHeadline().contains("Sitamarhi"));
        assertTrue(profile.getExplanation().getSummaryHeadline().contains("HIGH"));
        assertNotNull(profile.getExplanation().getNarrative());
        assertFalse(profile.getExplanation().getPrimaryDrivers().isEmpty());
    }

    @Test
    @DisplayName("Zero risk score handled safely without divide-by-zero")
    void testZeroRiskHandling() {
        DistrictRiskScoreDto risk = new DistrictRiskScoreDto();
        risk.setDistrictName("SafeDistrict");
        risk.setRiskScore(0.0);
        risk.setRiskScore100(0.0);
        risk.setRiskTier(RiskTier.LOW);

        DistrictRiskContributorsProfileDto profile = engine.buildContributorsProfile(risk, null, null, 5);
        assertNotNull(profile);
        assertEquals(0.0, profile.getRiskScore());
    }
}
