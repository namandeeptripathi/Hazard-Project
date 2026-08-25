package com.hazard;

import com.hazard.domain.risk.config.RiskConfigurationProfile;
import com.hazard.service.risk.config.RiskConfigurationRepository;
import com.hazard.service.risk.config.RiskConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Stage 4.8 — Risk Configuration Validation Engine.
 */
@DisplayName("Stage 4.8: Risk Configuration Validation Tests")
public class RiskConfigurationValidationTests {

    private RiskConfigurationService service;

    @BeforeEach
    void setUp() {
        RiskConfigurationRepository repository = new RiskConfigurationRepository();
        service = new RiskConfigurationService(repository);
    }

    @Test
    @DisplayName("Valid profile passes all business validations")
    void testValidProfile() {
        RiskConfigurationProfile p = RiskConfigurationProfile.createDefaultBaseline();
        assertDoesNotThrow(() -> service.validateProfile(p));
    }

    @Test
    @DisplayName("Negative top-level weight is rejected")
    void testNegativeTopLevelWeightRejected() {
        RiskConfigurationProfile p = RiskConfigurationProfile.createDefaultBaseline();
        p.setHazardWeight(-0.10);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validateProfile(p));
        assertTrue(ex.getMessage().contains("non-negative"));
    }

    @Test
    @DisplayName("Zero total top-level weight sum is rejected")
    void testZeroTotalWeightRejected() {
        RiskConfigurationProfile p = RiskConfigurationProfile.createDefaultBaseline();
        p.setHazardWeight(0.0);
        p.setExposureWeight(0.0);
        p.setVulnerabilityWeight(0.0);
        p.setHistoricalWeight(0.0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validateProfile(p));
        assertTrue(ex.getMessage().contains("strictly greater than 0.0"));
    }

    @Test
    @DisplayName("Negative exposure sub-weight is rejected")
    void testNegativeExposureSubWeightRejected() {
        RiskConfigurationProfile p = RiskConfigurationProfile.createDefaultBaseline();
        p.setPopulationWeight(-0.20);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validateProfile(p));
        assertTrue(ex.getMessage().contains("non-negative"));
    }

    @Test
    @DisplayName("Non-monotonic risk thresholds are rejected")
    void testNonMonotonicThresholdsRejected() {
        RiskConfigurationProfile p = RiskConfigurationProfile.createDefaultBaseline();
        p.setThresholdLowMax(0.50);
        p.setThresholdModerateMax(0.40); // reversed!
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validateProfile(p));
        assertTrue(ex.getMessage().contains("strictly monotonic"));
    }

    @Test
    @DisplayName("Thresholds outside (0, 1] range are rejected")
    void testThresholdOutsideRangeRejected() {
        RiskConfigurationProfile p = RiskConfigurationProfile.createDefaultBaseline();
        p.setThresholdVeryHighMax(1.50);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validateProfile(p));
        assertTrue(ex.getMessage().contains("strictly monotonic"));
    }

    @Test
    @DisplayName("Invalid minimum components count is rejected")
    void testInvalidMinimumComponents() {
        RiskConfigurationProfile p = RiskConfigurationProfile.createDefaultBaseline();
        p.setMinimumComponents(0);
        assertThrows(IllegalArgumentException.class, () -> service.validateProfile(p));

        p.setMinimumComponents(5);
        assertThrows(IllegalArgumentException.class, () -> service.validateProfile(p));
    }
}
