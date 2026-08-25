package com.hazard;

import com.hazard.domain.risk.config.RiskConfigStatus;
import com.hazard.domain.risk.config.RiskConfigurationProfile;
import com.hazard.dto.risk.config.*;
import com.hazard.service.risk.RiskCalculationService;
import com.hazard.service.risk.config.RiskConfigurationRepository;
import com.hazard.service.risk.config.RiskConfigurationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service-level integration tests for Stage 4.8 — Configurable Risk Weights.
 */
@SpringBootTest
@DisplayName("Stage 4.8: Risk Configuration Service Tests")
public class RiskConfigurationServiceTests {

    @Autowired
    private RiskConfigurationService riskConfigurationService;

    @Autowired
    private RiskCalculationService riskCalculationService;

    @Test
    @DisplayName("Active configuration defaults to baseline risk-v1")
    void testActiveConfigurationDefault() {
        RiskConfigurationProfile active = riskConfigurationService.getActiveConfiguration();
        assertNotNull(active);
        assertEquals(RiskConfigStatus.ACTIVE, active.getStatus());
        assertEquals(0.35, active.getHazardWeight(), 0.001);
        assertEquals(0.30, active.getExposureWeight(), 0.001);
        assertEquals(0.25, active.getVulnerabilityWeight(), 0.001);
        assertEquals(0.10, active.getHistoricalWeight(), 0.001);
    }

    @Test
    @DisplayName("Create new configuration profile with version increment")
    void testCreateConfiguration() {
        RiskConfigurationRequestDto req = new RiskConfigurationRequestDto();
        req.setName("Monsoon Flood Focus 2026");
        req.setDescription("High priority on hazard intensity and vulnerable settlements");
        req.setHazardWeight(0.45);
        req.setExposureWeight(0.30);
        req.setVulnerabilityWeight(0.15);
        req.setHistoricalWeight(0.10);
        req.setAuthor("DISASTER-CELL");

        RiskConfigurationResponseDto res = riskConfigurationService.createConfiguration(req);
        assertNotNull(res);
        assertTrue(res.getConfigId().startsWith("risk-v"));
        assertEquals("Monsoon Flood Focus 2026", res.getName());
        assertEquals(RiskConfigStatus.INACTIVE, res.getStatus());
        assertEquals(0.45, res.getConfiguredTopLevelWeights().get("HAZARD"), 0.001);
    }

    @Test
    @DisplayName("Transactional activation ensures single active configuration")
    void testActivateConfiguration() {
        // Create custom configuration
        RiskConfigurationRequestDto req = new RiskConfigurationRequestDto();
        req.setName("Test Active Profile");
        req.setHazardWeight(0.40);
        req.setExposureWeight(0.30);
        req.setVulnerabilityWeight(0.20);
        req.setHistoricalWeight(0.10);

        RiskConfigurationResponseDto created = riskConfigurationService.createConfiguration(req);
        RiskConfigurationResponseDto activated = riskConfigurationService.activateConfiguration(created.getConfigId(), "TESTER");

        assertEquals(RiskConfigStatus.ACTIVE, activated.getStatus());
        assertTrue(activated.isImmutable());

        // Verify active configuration matches newly activated profile
        RiskConfigurationProfile active = riskConfigurationService.getActiveConfiguration();
        assertEquals(created.getConfigId(), active.getConfigId());

        // Reset back to baseline for test isolation
        riskConfigurationService.activateConfiguration("risk-v1", "TEST-TEARDOWN");
    }

    @Test
    @DisplayName("Configuration diff computes numeric and delta differences")
    void testConfigurationDiff() {
        RiskConfigDiffDto diff = riskConfigurationService.compareConfigurations("risk-v1", "risk-preset-hazard");
        assertNotNull(diff);
        assertEquals("risk-v1", diff.getBaseConfigId());
        assertEquals("risk-preset-hazard", diff.getTargetConfigId());

        Double[] hazardDiff = diff.getTopLevelWeightDiffs().get("HAZARD");
        assertNotNull(hazardDiff);
        assertEquals(0.35, hazardDiff[0], 0.001);
        assertEquals(0.50, hazardDiff[1], 0.001);
        assertEquals(0.15, hazardDiff[2], 0.001);
    }

    @Test
    @DisplayName("What-If scenario simulation computes delta without modifying production configuration")
    void testWhatIfScenarioSimulation() {
        RiskScenarioAnalysisRequestDto req = new RiskScenarioAnalysisRequestDto();
        req.setDistrictName("Sitamarhi");
        req.setScenarioName("High Hazard Inundation Scenario");

        Map<String, Double> overrides = new HashMap<>();
        overrides.put("HAZARD", 0.60);
        overrides.put("EXPOSURE", 0.25);
        overrides.put("VULNERABILITY", 0.10);
        overrides.put("HISTORICAL", 0.05);
        req.setOverrideWeights(overrides);

        RiskScenarioAnalysisResultDto result = riskCalculationService.runScenarioAnalysis(req);
        assertNotNull(result);
        assertEquals("Sitamarhi", result.getDistrictName());
        assertTrue(result.isProductionConfigurationUnchanged());
        assertNotNull(result.getBaselineRisk());
        assertNotNull(result.getScenarioRisk());
        assertNotNull(result.getDeltaRiskScore());
        assertNotNull(result.getExplanation());
    }

    @Test
    @DisplayName("Audit trail records configuration lifecycle events")
    void testAuditTrail() {
        List<com.hazard.domain.risk.config.RiskConfigAuditEntry> logs = riskConfigurationService.getAuditLogs();
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
    }
}
