package com.hazard;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.dto.exposure.DistrictPopulationExposureDto;
import com.hazard.dto.exposure.GeometryExposureRequestDto;
import com.hazard.dto.exposure.PopulationExposureResultDto;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.service.exposure.PopulationExposureService;
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
 * Integration Tests for PopulationExposureService using real PostGIS population datasets.
 */
@SpringBootTest
@Transactional(readOnly = true)
class PopulationExposureServiceTests {

    private static final Logger log = LoggerFactory.getLogger(PopulationExposureServiceTests.class);

    @Autowired
    private PopulationExposureService populationExposureService;

    @Test
    @DisplayName("4.1.6: District Population Exposure — Patna")
    void testDistrictPopulationExposurePatna() {
        PopulationExposureResultDto result = populationExposureService.analyzeDistrictPopulationExposure("Patna");

        assertNotNull(result);
        assertEquals("Patna", result.getDistrictName());
        assertTrue(result.getTotalPopulation() > 1000000L, "Patna total population should exceed 1M");
        assertNotNull(result.getExposedPopulation());
        assertTrue(result.getExposedPopulation() >= 0L);
        assertNotNull(result.getExposurePercentage());
        assertTrue(result.getExposurePercentage() >= 0.0 && result.getExposurePercentage() <= 100.0);
        assertNotNull(result.getExposureScore());
        assertTrue(result.getExposureScore() >= 0.0 && result.getExposureScore() <= 1.0);
        assertNotNull(result.getExposureCategory());
        assertTrue(result.getIntersectingSettlementsCount() > 1000, "Patna should have >1000 settlement footprints");
        assertFalse(result.getAffectedSettlementsSummary().isEmpty());

        log.info("✅ Patna Population Exposure verified: Total={}, Exposed={}, Pct={}% ({}), Score={}",
                result.getTotalPopulation(), result.getExposedPopulation(),
                result.getExposurePercentage(), result.getExposureCategory(), result.getExposureScore());
    }

    @Test
    @DisplayName("4.1.7: District Population Exposure — Sitamarhi (Flood Prone District)")
    void testDistrictPopulationExposureSitamarhi() {
        PopulationExposureResultDto result = populationExposureService.analyzeDistrictPopulationExposure("Sitamarhi");

        assertNotNull(result);
        assertEquals("Sitamarhi", result.getDistrictName());
        assertTrue(result.getTotalPopulation() > 0);
        assertNotNull(result.getExposureCategory());

        log.info("✅ Sitamarhi Population Exposure verified: Total={}, Exposed={}, Pct={}% ({})",
                result.getTotalPopulation(), result.getExposedPopulation(),
                result.getExposurePercentage(), result.getExposureCategory());
    }

    @Test
    @DisplayName("4.1.8: Stage 3 Hazard Event Exposure — DFO-3 Flood Event Buffer Overlay")
    void testHazardEventExposureDfo3() {
        PopulationExposureResultDto result = populationExposureService.analyzeHazardEventExposure("DFO-3", 5000.0);

        assertNotNull(result);
        assertEquals("DFO-3", result.getHazardIdentifier());
        assertTrue(result.getIntersectingSettlementsCount() > 0, "5km buffer around DFO-3 should intersect settlements");
        assertTrue(result.getExposedPopulation() > 0, "Exposed population should be > 0");
        assertNotNull(result.getExposureCategory());
        assertFalse(result.getAffectedSettlementsSummary().isEmpty());

        log.info("✅ DFO-3 Event Exposure verified: Intersecting Settlements={}, Exposed Pop={}, Category={}",
                result.getIntersectingSettlementsCount(), result.getExposedPopulation(), result.getExposureCategory());
    }

    @Test
    @DisplayName("4.1.9: Custom Geometry WKT Spatial Overlay")
    void testCustomGeometryExposure() {
        // Bounding box polygon covering central Patna region
        String wkt = "POLYGON((85.0 25.5, 85.5 25.5, 85.5 26.0, 85.0 26.0, 85.0 25.5))";
        GeometryExposureRequestDto request = new GeometryExposureRequestDto(wkt, "TEST-ZONE-1", "FLOOD");
        request.setAssociatedDistrict("Patna");

        PopulationExposureResultDto result = populationExposureService.analyzeCustomGeometryExposure(request);

        assertNotNull(result);
        assertEquals("TEST-ZONE-1", result.getHazardIdentifier());
        assertTrue(result.getIntersectingSettlementsCount() > 0);
        assertTrue(result.getExposedPopulation() > 0);
        assertNotNull(result.getExposureCategory());

        log.info("✅ Custom Geometry Exposure verified: Intersecting Settlements={}, Exposed Pop={}",
                result.getIntersectingSettlementsCount(), result.getExposedPopulation());
    }

    @Test
    @DisplayName("4.1.10: All 38 Districts Exposure Aggregation")
    void testAllDistrictsPopulationExposure() {
        List<DistrictPopulationExposureDto> districts = populationExposureService.analyzeAllDistrictsPopulationExposure();

        assertNotNull(districts);
        assertEquals(38, districts.size(), "All 38 Bihar districts should be evaluated");

        for (DistrictPopulationExposureDto d : districts) {
            assertNotNull(d.getDistrictName());
            assertTrue(d.getTotalPopulation() > 0, "District " + d.getDistrictName() + " should have non-zero baseline population");
            assertNotNull(d.getExposureCategory());
            assertNotNull(d.getExposurePercentage());
        }

        log.info("✅ All 38 Districts Population Exposure aggregated successfully");
    }

    @Test
    @DisplayName("4.1.11: Population Exposure GeoJSON Vector Layer")
    void testGeneratePopulationExposureGeoJson() {
        GeoJsonFeatureCollectionDto geoJson = populationExposureService.generatePopulationExposureGeoJson(null);

        assertNotNull(geoJson);
        assertEquals("FeatureCollection", geoJson.getType());
        assertEquals(38, geoJson.getFeatures().size());

        var firstFeature = geoJson.getFeatures().get(0);
        assertNotNull(firstFeature.getGeometry());
        assertTrue(firstFeature.getProperties().containsKey("totalPopulation"));
        assertTrue(firstFeature.getProperties().containsKey("exposedPopulation"));
        assertTrue(firstFeature.getProperties().containsKey("exposurePercentage"));
        assertTrue(firstFeature.getProperties().containsKey("exposureCategory"));
        assertTrue(firstFeature.getProperties().containsKey("colorHex"));

        log.info("✅ Population Exposure GeoJSON Layer verified with 38 district features");
    }

    @Test
    @DisplayName("4.1.12: Error Handling — Unknown District and Invalid Hazard ID")
    void testErrorHandling() {
        assertThrows(HazardNotFoundException.class, () ->
                populationExposureService.analyzeDistrictPopulationExposure("NonExistentDistrict999")
        );

        assertThrows(HazardNotFoundException.class, () ->
                populationExposureService.analyzeHazardEventExposure("DFO-99999", null)
        );

        assertThrows(IllegalArgumentException.class, () ->
                populationExposureService.analyzeCustomGeometryExposure(new GeometryExposureRequestDto("POINT(0 0)", "BAD", "FLOOD"))
        );
    }
}
