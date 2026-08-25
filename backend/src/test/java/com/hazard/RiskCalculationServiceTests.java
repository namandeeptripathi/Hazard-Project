package com.hazard;

import com.hazard.domain.risk.RiskDataCompletenessStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.RiskConfigDto;
import com.hazard.dto.risk.RiskContributorsSummaryDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.service.risk.RiskCalculationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for RiskCalculationService with real PostGIS database.
 */
@SpringBootTest
@Transactional(readOnly = true)
class RiskCalculationServiceTests {

    private static final Logger log = LoggerFactory.getLogger(RiskCalculationServiceTests.class);

    @Autowired
    private RiskCalculationService riskCalculationService;

    @Test
    @DisplayName("4.7.8: District Risk Calculation — Sitamarhi")
    void testDistrictRiskScoreSitamarhi() {
        DistrictRiskScoreDto score = riskCalculationService.getDistrictRiskScore("Sitamarhi", null);

        assertNotNull(score);
        assertEquals("Sitamarhi", score.getDistrictName());
        assertTrue(score.getRiskScore() >= 0.0 && score.getRiskScore() <= 1.0);
        assertTrue(score.getRiskScore100() >= 0.0 && score.getRiskScore100() <= 100.0);
        assertNotNull(score.getRiskTier());
        assertNotNull(score.getComponents());
        assertFalse(score.getComponents().isEmpty());
        assertNotNull(score.getExposureSubBreakdown());
        assertNotNull(score.getDataQuality());
        assertEquals(RiskDataCompletenessStatus.DATA_COMPLETE, score.getDataQuality().getStatus());
        assertNotNull(score.getExplanation());

        log.info("✅ Sitamarhi Final Risk: Score={}/100, Tier={}, Hazard={}, Exposure={}, Vuln={}, Hist={}",
                score.getRiskScore100(), score.getRiskTier(),
                score.getComponents().get("HAZARD").getScore(),
                score.getComponents().get("EXPOSURE").getScore(),
                score.getComponents().get("VULNERABILITY").getScore(),
                score.getComponents().get("HISTORICAL").getScore());
    }

    @Test
    @DisplayName("4.7.9: District Risk Calculation — Patna (Urban Exposure)")
    void testDistrictRiskScorePatna() {
        DistrictRiskScoreDto score = riskCalculationService.getDistrictRiskScore("Patna", null);

        assertNotNull(score);
        assertEquals("Patna", score.getDistrictName());
        assertTrue(score.getRiskScore100() > 0.0);
        assertNotNull(score.getExposureSubBreakdown().getPopulationExposureScore());

        log.info("✅ Patna Final Risk: Score={}/100, Tier={}, ExposedPop={}",
                score.getRiskScore100(), score.getRiskTier(),
                score.getExposureSubBreakdown().getExposedPopulation());
    }

    @Test
    @DisplayName("4.7.10: District Risk Contributors & Explainability")
    void testGetDistrictRiskContributors() {
        RiskContributorsSummaryDto summary = riskCalculationService.getDistrictRiskContributors("Sitamarhi");

        assertNotNull(summary);
        assertEquals("Sitamarhi", summary.getGeographicId());
        assertNotNull(summary.getDominantPillar());
        assertNotNull(summary.getTopDrivers());
        assertFalse(summary.getTopDrivers().isEmpty());
        assertNotNull(summary.getExposureBreakdown());
        assertNotNull(summary.getPrimaryVulnerabilityDriver());

        log.info("✅ Sitamarhi Risk Contributors: DominantPillar={}, TopDriver={}, VulnDriver={}",
                summary.getDominantPillar(), summary.getTopDrivers().get(0).getName(), summary.getPrimaryVulnerabilityDriver());
    }

    @Test
    @DisplayName("4.7.11: All 38 Districts Risk Scores Aggregation")
    void testGetAllDistrictsRiskScores() {
        List<DistrictRiskScoreDto> list = riskCalculationService.getAllDistrictsRiskScores();

        assertNotNull(list);
        assertEquals(38, list.size(), "Should calculate risk for all 38 Bihar districts");
    }

    @Test
    @DisplayName("4.7.12: District Risk GeoJSON Choropleth Generation")
    void testGenerateRiskGeoJson() {
        GeoJsonFeatureCollectionDto geojson = riskCalculationService.generateRiskGeoJson();

        assertNotNull(geojson);
        assertEquals("FeatureCollection", geojson.getType());
        assertEquals(38, geojson.getFeatures().size(), "38 polygon district features expected");

        var first = geojson.getFeatures().get(0);
        assertTrue(first.getGeometry().getType().contains("Polygon"));
        assertNotNull(first.getProperties().get("riskScore"));
        assertNotNull(first.getProperties().get("riskScore100"));
        assertNotNull(first.getProperties().get("riskTier"));
        assertNotNull(first.getProperties().get("colorHex"));

        log.info("✅ Risk GeoJSON verified with 38 district polygon features");
    }

    @Test
    @DisplayName("4.7.13: Risk Configuration Parameters")
    void testGetRiskConfig() {
        RiskConfigDto cfg = riskCalculationService.getRiskConfig();

        assertNotNull(cfg);
        assertEquals(4, cfg.getRiskComponentWeights().size());
        assertEquals(3, cfg.getExposureSubWeights().size());
        assertEquals("v1.0", cfg.getCalculationVersion());
    }

    @Test
    @DisplayName("4.7.14: Error Handling — Unknown District")
    void testErrorHandling() {
        assertThrows(HazardNotFoundException.class, () ->
                riskCalculationService.getDistrictRiskScore("UnknownDistrict999", null)
        );
    }
}
