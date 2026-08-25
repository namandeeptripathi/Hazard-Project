package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import com.hazard.dto.exposure.GeometryExposureRequestDto;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.DistrictInfrastructureExposureSummaryDto;
import com.hazard.dto.infrastructure.InfrastructureExposureAnalysisResultDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.service.exposure.InfrastructureExposureService;
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
 * Integration Tests for InfrastructureExposureService with real PostGIS database and critical assets.
 */
@SpringBootTest
@Transactional(readOnly = true)
class InfrastructureExposureServiceTests {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureExposureServiceTests.class);

    @Autowired
    private InfrastructureExposureService infrastructureExposureService;

    @Test
    @DisplayName("4.3.4: Hazard Event Infrastructure Exposure — DFO-3 Flood")
    void testHazardEventInfrastructureExposureDfo3() {
        InfrastructureExposureAnalysisResultDto result = infrastructureExposureService.getExposedInfrastructureForHazardEvent("DFO-3", 5000.0);

        assertNotNull(result);
        assertEquals("DFO-3", result.getHazardIdentifier());
        assertTrue(result.getExposedAssetsCount() > 0, "DFO-3 buffer should intersect water/civil infrastructure");
        assertNotNull(result.getCategoryBreakdown());
        assertNotNull(result.getCriticalityBreakdown());
        assertFalse(result.getExposedAssets().isEmpty());

        var first = result.getExposedAssets().get(0);
        assertNotNull(first.getAssetId());
        assertNotNull(first.getAssetName());
        assertNotNull(first.getCategory());
        assertNotNull(first.getCriticality());
        assertNotNull(first.getInfrastructureExposureScore());
        assertNotNull(first.getExposureCategory());

        log.info("✅ DFO-3 Infrastructure Exposure verified: Count={}, AvgScore={}, First={} ({}, {})",
                result.getExposedAssetsCount(), result.getAverageExposureScore(),
                first.getAssetName(), first.getCategory(), first.getCriticality());
    }

    @Test
    @DisplayName("4.3.5: District Infrastructure Exposure — Patna (Urban & Lifeline Hub)")
    void testDistrictInfrastructureExposurePatna() {
        DistrictInfrastructureExposureSummaryDto summary = infrastructureExposureService.getDistrictInfrastructureExposure("Patna");

        assertNotNull(summary);
        assertEquals("Patna", summary.getDistrictName());
        assertTrue(summary.getTotalAssetsEvaluated() > 0, "Patna should have infrastructure records");
        assertNotNull(summary.getCategoryBreakdown());
        assertTrue(summary.getCategoryBreakdown().containsKey("HEALTHCARE"));
        assertTrue(summary.getCategoryBreakdown().containsKey("WATER"));
        assertTrue(summary.getCategoryBreakdown().containsKey("TRANSPORT"));

        log.info("✅ Patna Infrastructure Exposure: Total={}, DominantHazard={}, PeakHazard={}",
                summary.getTotalAssetsEvaluated(), summary.getDominantHazard(), summary.getPeakHazardIndex());
    }

    @Test
    @DisplayName("4.3.6: District Infrastructure Exposure — Sitamarhi")
    void testDistrictInfrastructureExposureSitamarhi() {
        DistrictInfrastructureExposureSummaryDto summary = infrastructureExposureService.getDistrictInfrastructureExposure("Sitamarhi");

        assertNotNull(summary);
        assertEquals("Sitamarhi", summary.getDistrictName());
        assertTrue(summary.getTotalAssetsEvaluated() > 0);

        log.info("✅ Sitamarhi Infrastructure Exposure: Total evaluated={}", summary.getTotalAssetsEvaluated());
    }

    @Test
    @DisplayName("4.3.7: Custom Geometry WKT Infrastructure Overlay")
    void testCustomGeometryInfrastructureExposure() {
        String wkt = "POLYGON((85.0 25.5, 85.5 25.5, 85.5 26.0, 85.0 26.0, 85.0 25.5))";
        GeometryExposureRequestDto req = new GeometryExposureRequestDto(wkt, "TEST-INFRA-ZONE", "FLOOD");
        req.setAssociatedDistrict("Patna");

        InfrastructureExposureAnalysisResultDto result = infrastructureExposureService.analyzeInfrastructureForCustomGeometry(req);

        assertNotNull(result);
        assertEquals("TEST-INFRA-ZONE", result.getHazardIdentifier());
        assertTrue(result.getExposedAssetsCount() > 0);
        assertNotNull(result.getAverageExposureScore());

        log.info("✅ Custom Geometry Infrastructure Overlay verified: {} assets exposed", result.getExposedAssetsCount());
    }

    @Test
    @DisplayName("4.3.8: All 38 Districts Infrastructure Exposure Summary")
    void testAllDistrictsInfrastructureExposureSummary() {
        List<DistrictInfrastructureExposureSummaryDto> list = infrastructureExposureService.getAllDistrictsInfrastructureExposureSummary();

        assertNotNull(list);
        assertEquals(38, list.size(), "All 38 Bihar districts must have infrastructure summaries");

        log.info("✅ All 38 districts infrastructure exposure summarized successfully");
    }

    @Test
    @DisplayName("4.3.9: Infrastructure Exposure GeoJSON Export")
    void testGenerateInfrastructureExposureGeoJson() {
        GeoJsonFeatureCollectionDto geoJson = infrastructureExposureService.generateInfrastructureExposureGeoJson("Patna", null);

        assertNotNull(geoJson);
        assertEquals("FeatureCollection", geoJson.getType());
        assertFalse(geoJson.getFeatures().isEmpty());

        var first = geoJson.getFeatures().get(0);
        assertEquals("Point", first.getGeometry().getType());
        assertTrue(first.getProperties().containsKey("assetId"));
        assertTrue(first.getProperties().containsKey("assetName"));
        assertTrue(first.getProperties().containsKey("category"));
        assertTrue(first.getProperties().containsKey("criticality"));
        assertTrue(first.getProperties().containsKey("infrastructureExposureScore"));
        assertTrue(first.getProperties().containsKey("colorHex"));

        log.info("✅ Infrastructure Exposure GeoJSON layer verified: {} features", geoJson.getFeatures().size());
    }

    @Test
    @DisplayName("4.3.10: Error Handling — Unknown District & Invalid Geometry")
    void testErrorHandling() {
        assertThrows(HazardNotFoundException.class, () ->
                infrastructureExposureService.getDistrictInfrastructureExposure("NonExistentDistrictXYZ")
        );

        assertThrows(HazardNotFoundException.class, () ->
                infrastructureExposureService.getExposedInfrastructureForHazardEvent("DFO-99999", null)
        );

        assertThrows(IllegalArgumentException.class, () ->
                infrastructureExposureService.analyzeInfrastructureForCustomGeometry(new GeometryExposureRequestDto("POINT(0 0)", "BAD", "FLOOD"))
        );
    }
}
