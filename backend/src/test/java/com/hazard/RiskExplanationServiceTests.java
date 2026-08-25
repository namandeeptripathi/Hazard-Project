package com.hazard;

import com.hazard.dto.risk.explain.*;
import com.hazard.service.risk.explain.RiskExplanationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for Stage 4.10 — Risk Explanation Service.
 */
@SpringBootTest
@DisplayName("Stage 4.10: Risk Explanation Service Tests")
public class RiskExplanationServiceTests {

    private static final Logger log = LoggerFactory.getLogger(RiskExplanationServiceTests.class);

    @Autowired
    private RiskExplanationService riskExplanationService;

    @Test
    @DisplayName("Generate complete explainability profile for Sitamarhi")
    void testGetSitamarhiExplainabilityProfile() {
        DistrictRiskExplainabilityProfileDto profile = riskExplanationService.getDistrictExplainabilityProfile("Sitamarhi");

        assertNotNull(profile);
        assertEquals("Sitamarhi", profile.getDistrictName());
        assertEquals("explain-v1", profile.getExplanationVersion());
        assertNotNull(profile.getRiskScore());
        assertNotNull(profile.getRiskScore100());

        // Summaries
        assertNotNull(profile.getSummary());
        assertNotNull(profile.getSummary().getExecutiveSummary());
        assertNotNull(profile.getSummary().getShortSummary());

        // Evidence & Trace
        assertFalse(profile.getEvidenceItems().isEmpty());
        assertNotNull(profile.getCalculationTrace());
        assertTrue(profile.getCalculationTrace().isReconciled());

        // Sensitivity & Limitations
        assertFalse(profile.getSensitivityAnalysis().isEmpty());
        assertFalse(profile.getModelLimitations().isEmpty());

        log.info("✅ Sitamarhi Explainability verified: Score={}/100, ExecSummary='{}', TopLeverage={}",
                profile.getRiskScore100(), profile.getSummary().getExecutiveSummary(),
                profile.getSensitivityAnalysis().get(0).getComponentName());
    }

    @Test
    @DisplayName("Generate complete explainability profile for Patna")
    void testGetPatnaExplainabilityProfile() {
        DistrictRiskExplainabilityProfileDto profile = riskExplanationService.getDistrictExplainabilityProfile("Patna");

        assertNotNull(profile);
        assertEquals("Patna", profile.getDistrictName());
        assertFalse(profile.getPrimaryDrivers().isEmpty());
        assertNotNull(profile.getDataQuality());

        log.info("✅ Patna Explainability verified: Score={}/100, DominantDriver={}",
                profile.getRiskScore100(), profile.getSummary().getDominantDriver());
    }

    @Test
    @DisplayName("Retrieve executive and short summaries")
    void testGetExplanationSummary() {
        ExplanationSummaryDto summary = riskExplanationService.getExplanationSummary("Sitamarhi");
        assertNotNull(summary);
        assertNotNull(summary.getExecutiveSummary());
        assertNotNull(summary.getShortSummary());
        assertNotNull(summary.getDetailedNarrative());
    }

    @Test
    @DisplayName("Retrieve evidence catalog with provenance items")
    void testGetEvidenceItems() {
        List<ExplainableEvidenceItemDto> items = riskExplanationService.getEvidenceItems("Sitamarhi");
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items.stream().anyMatch(e -> e.getSourceStage().contains("Stage 3")));
    }

    @Test
    @DisplayName("Retrieve calculation trace with reconciling formula")
    void testGetCalculationTrace() {
        CalculationTraceDto trace = riskExplanationService.getCalculationTrace("Sitamarhi");
        assertNotNull(trace);
        assertTrue(trace.isReconciled());
        assertNotNull(trace.getFormulaString());
    }

    @Test
    @DisplayName("Retrieve model sensitivity ranking")
    void testGetSensitivityAnalysis() {
        List<ComponentSensitivityDto> list = riskExplanationService.getSensitivityAnalysis("Sitamarhi");
        assertNotNull(list);
        assertEquals(4, list.size());
        assertEquals(1, list.get(0).getLeverageRank());
    }
}
