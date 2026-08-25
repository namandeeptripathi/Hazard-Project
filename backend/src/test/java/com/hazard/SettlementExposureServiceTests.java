package com.hazard;

import com.hazard.dto.exposure.DistrictSettlementExposureSummaryDto;
import com.hazard.dto.exposure.GeometryExposureRequestDto;
import com.hazard.dto.exposure.SettlementExposureAnalysisResultDto;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.service.exposure.SettlementExposureService;
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
 * Integration Tests for SettlementExposureService with real PostGIS database.
 */
@SpringBootTest
@Transactional(readOnly = true)
class SettlementExposureServiceTests {

    private static final Logger log = LoggerFactory.getLogger(SettlementExposureServiceTests.class);

    @Autowired
    private SettlementExposureService settlementExposureService;

    @Test
    @DisplayName("4.2.4: Hazard Event Settlement Exposure — DFO-3 Flood Event")
    void testHazardEventSettlementExposureDfo3() {
        SettlementExposureAnalysisResultDto result = settlementExposureService.getExposedSettlementsForHazardEvent("DFO-3", 5000.0);

        assertNotNull(result);
        assertEquals("DFO-3", result.getHazardIdentifier());
        assertTrue(result.getExposedSettlementsCount() > 0, "DFO-3 buffer should expose settlements");
        assertFalse(result.getExposedSettlements().isEmpty());

        var first = result.getExposedSettlements().get(0);
        assertNotNull(first.getSettlementId());
        assertNotNull(first.getSettlementName());
        assertNotNull(first.getSettlementType());
        assertNotNull(first.getSettlementExposureScore());
        assertNotNull(first.getExposureCategory());
        assertNotNull(first.getDistanceMeters());
        assertTrue(first.getDistanceMeters() <= 5000.0, "Distance must be within the 5km buffer");

        log.info("✅ DFO-3 Settlement Exposure verified: Count={}, AvgScore={}, First={} ({}m, score={})",
                result.getExposedSettlementsCount(), result.getAverageSettlementExposureScore(),
                first.getSettlementName(), first.getDistanceMeters(), first.getSettlementExposureScore());
    }

    @Test
    @DisplayName("4.2.5: District Settlement Exposure — Sitamarhi")
    void testDistrictSettlementExposureSitamarhi() {
        DistrictSettlementExposureSummaryDto summary = settlementExposureService.getDistrictSettlementExposure("Sitamarhi");

        assertNotNull(summary);
        assertEquals("Sitamarhi", summary.getDistrictName());
        assertTrue(summary.getTotalSettlementsEvaluated() > 0, "Sitamarhi should have settlement records");
        assertNotNull(summary.getCategoryCounts());
        assertFalse(summary.getSettlements().isEmpty());

        log.info("✅ Sitamarhi Settlement Exposure: Total={}, DominantHazard={}, PeakIndex={}",
                summary.getTotalSettlementsEvaluated(), summary.getDominantHazard(), summary.getPeakHazardIndex());
    }

    @Test
    @DisplayName("4.2.6: District Settlement Exposure — Patna")
    void testDistrictSettlementExposurePatna() {
        DistrictSettlementExposureSummaryDto summary = settlementExposureService.getDistrictSettlementExposure("Patna");

        assertNotNull(summary);
        assertEquals("Patna", summary.getDistrictName());
        assertTrue(summary.getTotalSettlementsEvaluated() > 1000, "Patna should have >1000 settlement footprints");

        log.info("✅ Patna Settlement Exposure: Total evaluated={}", summary.getTotalSettlementsEvaluated());
    }

    @Test
    @DisplayName("4.2.7: Custom Geometry WKT Settlement Overlay")
    void testCustomGeometrySettlementExposure() {
        String wkt = "POLYGON((85.0 25.5, 85.5 25.5, 85.5 26.0, 85.0 26.0, 85.0 25.5))";
        GeometryExposureRequestDto req = new GeometryExposureRequestDto(wkt, "TEST-ZONE-1", "FLOOD");
        req.setAssociatedDistrict("Patna");

        SettlementExposureAnalysisResultDto result = settlementExposureService.analyzeSettlementsForCustomGeometry(req);

        assertNotNull(result);
        assertEquals("TEST-ZONE-1", result.getHazardIdentifier());
        assertTrue(result.getExposedSettlementsCount() > 0);
        assertNotNull(result.getAverageSettlementExposureScore());

        log.info("✅ Custom Geometry Settlements Overlay verified: {} settlements exposed", result.getExposedSettlementsCount());
    }

    @Test
    @DisplayName("4.2.8: All 38 Districts Settlement Exposure Summary")
    void testAllDistrictsSettlementExposureSummary() {
        List<DistrictSettlementExposureSummaryDto> summaries = settlementExposureService.getAllDistrictsSettlementExposureSummary();

        assertNotNull(summaries);
        assertEquals(38, summaries.size(), "All 38 Bihar districts must be summarized");

        for (DistrictSettlementExposureSummaryDto s : summaries) {
            assertNotNull(s.getDistrictName());
            assertTrue(s.getTotalSettlementsEvaluated() > 0, "District " + s.getDistrictName() + " should have evaluated settlements");
            assertNotNull(s.getCategoryCounts());
        }

        log.info("✅ All 38 districts settlement exposure summarized successfully");
    }

    @Test
    @DisplayName("4.2.9: Settlement Exposure GeoJSON Point Layer")
    void testGenerateSettlementExposureGeoJson() {
        GeoJsonFeatureCollectionDto geoJson = settlementExposureService.generateSettlementExposureGeoJson("Sitamarhi", null);

        assertNotNull(geoJson);
        assertEquals("FeatureCollection", geoJson.getType());
        assertFalse(geoJson.getFeatures().isEmpty());

        var first = geoJson.getFeatures().get(0);
        assertEquals("Point", first.getGeometry().getType());
        assertTrue(first.getProperties().containsKey("settlementName"));
        assertTrue(first.getProperties().containsKey("settlementType"));
        assertTrue(first.getProperties().containsKey("settlementExposureScore"));
        assertTrue(first.getProperties().containsKey("exposureCategory"));
        assertTrue(first.getProperties().containsKey("colorHex"));

        log.info("✅ Settlement Exposure GeoJSON Point layer verified: {} features", geoJson.getFeatures().size());
    }

    @Test
    @DisplayName("4.2.10: Error Handling — Unknown District & Invalid Geometry")
    void testErrorHandling() {
        assertThrows(HazardNotFoundException.class, () ->
                settlementExposureService.getDistrictSettlementExposure("NonExistentDistrict404")
        );

        assertThrows(HazardNotFoundException.class, () ->
                settlementExposureService.getExposedSettlementsForHazardEvent("DFO-99999", null)
        );

        assertThrows(IllegalArgumentException.class, () ->
                settlementExposureService.analyzeSettlementsForCustomGeometry(new GeometryExposureRequestDto("POINT(0 0)", "BAD", "FLOOD"))
        );
    }
}
