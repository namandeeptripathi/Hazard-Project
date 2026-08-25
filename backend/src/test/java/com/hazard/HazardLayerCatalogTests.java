package com.hazard;

import com.hazard.domain.hazard.HazardLayerCategory;
import com.hazard.domain.hazard.HazardType;
import com.hazard.dto.layer.HazardLayerCatalogDto;
import com.hazard.dto.layer.HazardLayerMetadataDto;
import com.hazard.service.layer.HazardLayerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HazardLayerCatalogTests {

    @Autowired
    private HazardLayerService hazardLayerService;

    @Test
    @DisplayName("1. Layer Catalog Exposes All 8 Standard Map Layers")
    void testLayerCatalogContainsAllLayers() {
        HazardLayerCatalogDto catalog = hazardLayerService.getLayerCatalog();

        assertNotNull(catalog);
        assertEquals(8, catalog.getTotalAvailableLayers());
        assertEquals(8, catalog.getLayers().size());
        assertEquals("EPSG:4326 (WGS 84)", catalog.getCanonicalCrs());

        List<String> layerIds = catalog.getLayers().stream().map(HazardLayerMetadataDto::getLayerId).toList();
        assertTrue(layerIds.contains(HazardLayerService.LAYER_FLOOD_EVENTS));
        assertTrue(layerIds.contains(HazardLayerService.LAYER_EXTREME_RAINFALL_EVENTS));
        assertTrue(layerIds.contains(HazardLayerService.LAYER_FLOOD_HAZARD_SCORES));
        assertTrue(layerIds.contains(HazardLayerService.LAYER_EXTREME_RAINFALL_SCORES));
        assertTrue(layerIds.contains(HazardLayerService.LAYER_MULTI_HAZARD_INDEX));
        assertTrue(layerIds.contains(HazardLayerService.LAYER_DISTRICT_HAZARD_SUMMARIES));
        assertTrue(layerIds.contains(HazardLayerService.LAYER_DISTRICT_BOUNDARIES));
        assertTrue(layerIds.contains(HazardLayerService.LAYER_RIVERS_REFERENCE));
    }

    @Test
    @DisplayName("2. Layer Metadata Attributes & Categories Consistency")
    void testLayerMetadataAttributes() {
        HazardLayerMetadataDto floodScoreLayer = hazardLayerService.getLayerMetadata(HazardLayerService.LAYER_FLOOD_HAZARD_SCORES);
        assertNotNull(floodScoreLayer);
        assertEquals(HazardLayerCategory.HAZARD_SCORE_LAYER, floodScoreLayer.getCategory());
        assertEquals("Point", floodScoreLayer.getGeometryType());
        assertEquals(HazardType.FLOOD, floodScoreLayer.getHazardType());
        assertTrue(floodScoreLayer.isHasScore());
        assertTrue(floodScoreLayer.isHasSeverityTier());

        HazardLayerMetadataDto districtLayer = hazardLayerService.getLayerMetadata(HazardLayerService.LAYER_DISTRICT_HAZARD_SUMMARIES);
        assertNotNull(districtLayer);
        assertEquals(HazardLayerCategory.DISTRICT_SUMMARY_LAYER, districtLayer.getCategory());
        assertEquals("MultiPolygon", districtLayer.getGeometryType());
        assertTrue(districtLayer.isHasScore());
    }

    @Test
    @DisplayName("3. Unknown Layer ID Rejection with Exception")
    void testUnknownLayerRejection() {
        assertThrows(RuntimeException.class, () ->
                hazardLayerService.getLayerMetadata("NON_EXISTENT_LAYER")
        );
    }
}
