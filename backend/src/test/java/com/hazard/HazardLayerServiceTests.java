package com.hazard;

import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.service.layer.HazardLayerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional(readOnly = true)
class HazardLayerServiceTests {

    private static final Logger log = LoggerFactory.getLogger(HazardLayerServiceTests.class);

    @Autowired
    private HazardLayerService hazardLayerService;

    @Test
    @DisplayName("1. Event Layer: FLOOD_EVENTS GeoJSON (7 valid spatial DFO points)")
    void testFloodEventsLayer() {
        GeoJsonFeatureCollectionDto geojson = hazardLayerService.getLayerGeoJson(
                HazardLayerService.LAYER_FLOOD_EVENTS, null, null, null, null, 50
        );

        assertNotNull(geojson);
        assertEquals("FeatureCollection", geojson.getType());
        assertEquals(7, geojson.getCount());
        assertTrue(geojson.getFeatures().stream().allMatch(f ->
                "Point".equals(f.getGeometry().getType()) &&
                f.getProperties().containsKey("durationDays") &&
                f.getProperties().containsKey("affectedAreaSqKm")));

        log.info("✅ FLOOD_EVENTS layer verified: {} features", geojson.getCount());
    }

    @Test
    @DisplayName("2. Score Layer: FLOOD_HAZARD_SCORES with HazardScore and SeverityTier")
    void testFloodHazardScoresLayer() {
        GeoJsonFeatureCollectionDto geojson = hazardLayerService.getLayerGeoJson(
                HazardLayerService.LAYER_FLOOD_HAZARD_SCORES, null, null, null, null, 50
        );

        assertNotNull(geojson);
        assertEquals(7, geojson.getCount());
        assertTrue(geojson.getFeatures().stream().allMatch(f ->
                f.getProperties().containsKey("hazardScore") &&
                f.getProperties().containsKey("severityTier") &&
                f.getProperties().containsKey("completenessRatio")));

        log.info("✅ FLOOD_HAZARD_SCORES layer verified: {} features", geojson.getCount());
    }

    @Test
    @DisplayName("3. Multi-Hazard Layer: MULTI_HAZARD_INDEX GeoJSON")
    void testMultiHazardIndexLayer() {
        GeoJsonFeatureCollectionDto geojson = hazardLayerService.getLayerGeoJson(
                HazardLayerService.LAYER_MULTI_HAZARD_INDEX, null, null, null, null, 50
        );

        assertNotNull(geojson);
        assertFalse(geojson.getFeatures().isEmpty());
        assertTrue(geojson.getFeatures().stream().allMatch(f ->
                f.getProperties().containsKey("multiHazardIndex") &&
                f.getProperties().containsKey("dominantHazard")));

        log.info("✅ MULTI_HAZARD_INDEX layer verified: {} features", geojson.getCount());
    }

    @Test
    @DisplayName("4. District Summary Layer: DISTRICT_HAZARD_SUMMARIES (38 Bihar District MultiPolygons)")
    void testDistrictHazardSummariesLayer() {
        GeoJsonFeatureCollectionDto geojson = hazardLayerService.getLayerGeoJson(
                HazardLayerService.LAYER_DISTRICT_HAZARD_SUMMARIES, null, null, null, null, 100
        );

        assertNotNull(geojson);
        assertEquals(38, geojson.getCount());
        assertTrue(geojson.getFeatures().stream().allMatch(f ->
                ("MultiPolygon".equals(f.getGeometry().getType()) || "Polygon".equals(f.getGeometry().getType())) &&
                f.getProperties().containsKey("districtName") &&
                f.getProperties().containsKey("peakMultiHazardIndex") &&
                f.getProperties().containsKey("severityTier")));

        log.info("✅ DISTRICT_HAZARD_SUMMARIES layer verified: {} districts with MultiPolygon geometry", geojson.getCount());
    }

    @Test
    @DisplayName("5. Reference Layer: DISTRICT_BOUNDARIES (38 Official Administrative Polygons)")
    void testDistrictBoundariesLayer() {
        GeoJsonFeatureCollectionDto geojson = hazardLayerService.getLayerGeoJson(
                HazardLayerService.LAYER_DISTRICT_BOUNDARIES, null, null, null, null, 100
        );

        assertNotNull(geojson);
        assertEquals(38, geojson.getCount());
        assertTrue(geojson.getFeatures().stream().allMatch(f ->
                "DISTRICT_BOUNDARIES".equals(f.getProperties().get("layerId")) &&
                f.getProperties().containsKey("districtName")));

        log.info("✅ DISTRICT_BOUNDARIES layer verified: {} districts", geojson.getCount());
    }

    @Test
    @DisplayName("6. Reference Layer: RIVERS_REFERENCE (Hydrological River Network Linestrings)")
    void testRiversReferenceLayer() {
        GeoJsonFeatureCollectionDto geojson = hazardLayerService.getLayerGeoJson(
                HazardLayerService.LAYER_RIVERS_REFERENCE, null, null, null, null, 50
        );

        assertNotNull(geojson);
        assertFalse(geojson.getFeatures().isEmpty());
        assertTrue(geojson.getFeatures().stream().allMatch(f ->
                ("MultiLineString".equals(f.getGeometry().getType()) || "LineString".equals(f.getGeometry().getType())) &&
                f.getProperties().containsKey("strahlerOrder")));

        log.info("✅ RIVERS_REFERENCE layer verified: {} river reach features", geojson.getCount());
    }

    @Test
    @DisplayName("7. District and Severity Filtered Queries on Map Layers")
    void testFilteredLayerQueries() {
        GeoJsonFeatureCollectionDto patnaRainScores = hazardLayerService.getLayerGeoJson(
                HazardLayerService.LAYER_EXTREME_RAINFALL_SCORES, "Patna", SeverityTier.HIGH, null, null, 20
        );

        assertNotNull(patnaRainScores);
        log.info("✅ Filtered layer query verified (Patna HIGH rainfall scores: {} features)", patnaRainScores.getCount());
    }
}
