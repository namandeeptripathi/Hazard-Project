package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.safesite.DistanceStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.TerrainStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.service.exposure.InfrastructureDataProvider;
import com.hazard.service.risk.RedZoneService;
import com.hazard.service.safesite.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Stage 5.6 — Unit and Integration Tests for Roads Intelligence & Road Accessibility.
 */
@ExtendWith(MockitoExtension.class)
class RoadsIntelligenceTests {

    @Mock
    private InfrastructureDataProvider dataProvider;

    @Mock
    private RedZoneService redZoneService;

    @Mock
    private HazardSafetyEvaluator hazardSafetyEvaluator;

    @Mock
    private TerrainEvaluator terrainEvaluator;

    @Mock
    private DistanceEvaluator distanceEvaluator;

    @Mock
    private HealthcareEvaluator healthcareEvaluator;

    @Mock
    private WaterEvaluator waterEvaluator;

    @Mock
    private InfrastructureEvaluator infrastructureEvaluator;

    @Mock
    private SuitabilityEvaluator suitabilityEvaluator;

    private RoadAccessEvaluationConfig roadConfig;
    private RoadAccessibilityEvaluator roadAccessibilityEvaluator;
    private SafeSiteRankingEvaluator safeSiteRankingEvaluator = new SafeSiteRankingEvaluator();
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        roadConfig = new RoadAccessEvaluationConfig();
        roadAccessibilityEvaluator = new RoadAccessibilityEvaluator(roadConfig);
        candidateSafeSiteService = new CandidateSafeSiteService(
                dataProvider,
                redZoneService,
                hazardSafetyEvaluator,
                terrainEvaluator,
                distanceEvaluator,
                roadAccessibilityEvaluator,
                healthcareEvaluator,
                waterEvaluator,
                infrastructureEvaluator,
                suitabilityEvaluator,
                safeSiteRankingEvaluator
        );
    }

    @Nested
    @DisplayName("1. RoadAccessStatus Enum Tests")
    class RoadAccessStatusEnumTests {

        @Test
        @DisplayName("Test 1: Enum Values and Helpers")
        void testEnumValuesAndHelpers() {
            assertEquals(4, RoadAccessStatus.values().length);
            assertTrue(RoadAccessStatus.NEAR.isNear());
            assertFalse(RoadAccessStatus.NEAR.isModerate());
            assertFalse(RoadAccessStatus.NEAR.isFar());
            assertTrue(RoadAccessStatus.NEAR.isKnown());

            assertTrue(RoadAccessStatus.MODERATE.isModerate());
            assertFalse(RoadAccessStatus.MODERATE.isNear());
            assertTrue(RoadAccessStatus.MODERATE.isKnown());

            assertTrue(RoadAccessStatus.FAR.isFar());
            assertFalse(RoadAccessStatus.FAR.isNear());
            assertTrue(RoadAccessStatus.FAR.isKnown());

            assertFalse(RoadAccessStatus.UNKNOWN.isKnown());
            assertFalse(RoadAccessStatus.UNKNOWN.isNear());
        }

        @Test
        @DisplayName("Test 2: fromString Valid and Case-Insensitive Parsing")
        void testFromString() {
            assertEquals(RoadAccessStatus.NEAR, RoadAccessStatus.fromString("near"));
            assertEquals(RoadAccessStatus.NEAR, RoadAccessStatus.fromString("NEAR"));
            assertEquals(RoadAccessStatus.MODERATE, RoadAccessStatus.fromString("moderate"));
            assertEquals(RoadAccessStatus.FAR, RoadAccessStatus.fromString("FAR"));
            assertEquals(RoadAccessStatus.UNKNOWN, RoadAccessStatus.fromString("UNKNOWN"));
            assertEquals(RoadAccessStatus.UNKNOWN, RoadAccessStatus.fromString(null));
            assertEquals(RoadAccessStatus.UNKNOWN, RoadAccessStatus.fromString("   "));
        }

        @Test
        @DisplayName("Test 3: fromString Invalid Parameter Throws Exception")
        void testInvalidFromStringThrows() {
            InvalidHazardParameterException ex = assertThrows(
                    InvalidHazardParameterException.class,
                    () -> RoadAccessStatus.fromString("INVALID_STATUS")
            );
            assertTrue(ex.getMessage().contains("Allowed values: NEAR, MODERATE, FAR, UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("2. Road Proximity Threshold Evaluation Tests")
    class RoadAccessThresholdTests {

        @Test
        @DisplayName("Test 4: Candidate directly on road (0.0 m) -> NEAR")
        void testZeroDistanceNear() {
            CandidateSafeSiteDto site = createCandidateSite(85.158, 25.6208);
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 0.0);

            assertEquals(RoadAccessStatus.NEAR, site.getRoadAccessStatus());
            assertEquals(0.0, site.getRoadDistanceMeters());
            assertEquals(0.0, site.getRoadDistanceKilometers());
            assertTrue(site.getRoadAccessReason().contains("directly on or adjacent to an accessible road"));
        }

        @Test
        @DisplayName("Test 5: Candidate close to road (250.0 m <= 500m threshold) -> NEAR")
        void testWithinNearThreshold() {
            CandidateSafeSiteDto site = createCandidateSite(85.158, 25.6208);
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 250.0);

            assertEquals(RoadAccessStatus.NEAR, site.getRoadAccessStatus());
            assertEquals(250.0, site.getRoadDistanceMeters());
            assertEquals(0.25, site.getRoadDistanceKilometers());
            assertTrue(site.getRoadAccessReason().contains("close road access"));
        }

        @Test
        @DisplayName("Test 6: Candidate exactly at near threshold (500.0 m) -> NEAR")
        void testExactNearThreshold() {
            CandidateSafeSiteDto site = createCandidateSite(85.158, 25.6208);
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 500.0);

            assertEquals(RoadAccessStatus.NEAR, site.getRoadAccessStatus());
            assertEquals(500.0, site.getRoadDistanceMeters());
            assertEquals(0.5, site.getRoadDistanceKilometers());
        }

        @Test
        @DisplayName("Test 7: Candidate just above near threshold (500.1 m) -> MODERATE")
        void testModerateLowEdge() {
            CandidateSafeSiteDto site = createCandidateSite(85.158, 25.6208);
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 500.1);

            assertEquals(RoadAccessStatus.MODERATE, site.getRoadAccessStatus());
            assertEquals(500.1, site.getRoadDistanceMeters());
            assertEquals(0.5, site.getRoadDistanceKilometers());
            assertTrue(site.getRoadAccessReason().contains("moderate road proximity"));
        }

        @Test
        @DisplayName("Test 8: Candidate at intermediate distance (1200.0 m) -> MODERATE")
        void testModerateMiddle() {
            CandidateSafeSiteDto site = createCandidateSite(85.158, 25.6208);
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 1200.0);

            assertEquals(RoadAccessStatus.MODERATE, site.getRoadAccessStatus());
            assertEquals(1200.0, site.getRoadDistanceMeters());
            assertEquals(1.2, site.getRoadDistanceKilometers());
        }

        @Test
        @DisplayName("Test 9: Candidate just below far threshold (1999.9 m) -> MODERATE")
        void testModerateHighEdge() {
            CandidateSafeSiteDto site = createCandidateSite(85.158, 25.6208);
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 1999.9);

            assertEquals(RoadAccessStatus.MODERATE, site.getRoadAccessStatus());
            assertEquals(1999.9, site.getRoadDistanceMeters());
            assertEquals(2.0, site.getRoadDistanceKilometers());
        }

        @Test
        @DisplayName("Test 10: Candidate exactly at far threshold (2000.0 m) -> FAR")
        void testExactFarThreshold() {
            CandidateSafeSiteDto site = createCandidateSite(85.158, 25.6208);
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 2000.0);

            assertEquals(RoadAccessStatus.FAR, site.getRoadAccessStatus());
            assertEquals(2000.0, site.getRoadDistanceMeters());
            assertEquals(2.0, site.getRoadDistanceKilometers());
            assertTrue(site.getRoadAccessReason().contains("relatively distant"));
        }

        @Test
        @DisplayName("Test 11: Candidate far from road network (5000.0 m) -> FAR")
        void testFarDistance() {
            CandidateSafeSiteDto site = createCandidateSite(85.158, 25.6208);
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 5000.0);

            assertEquals(RoadAccessStatus.FAR, site.getRoadAccessStatus());
            assertEquals(5000.0, site.getRoadDistanceMeters());
            assertEquals(5.0, site.getRoadDistanceKilometers());
        }

        @Test
        @DisplayName("Test 12: Missing road network dataset in platform state -> UNKNOWN with descriptive reason")
        void testMissingRoadDataEvaluatesToUnknown() {
            CandidateSafeSiteDto site = createCandidateSite(85.158, 25.6208);
            // Default evaluateRoadAccessibility without explicit distance (production state)
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site);

            assertEquals(RoadAccessStatus.UNKNOWN, site.getRoadAccessStatus());
            assertNull(site.getRoadDistanceMeters());
            assertNull(site.getRoadDistanceKilometers());
            assertEquals("Road-network data is not currently available in the project dataset; road accessibility is integrated structurally but returns UNKNOWN.", site.getRoadAccessReason());
        }

        @Test
        @DisplayName("Test 13: Missing/null coordinates -> UNKNOWN")
        void testMissingCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-COORDS");
            site.setLatitude(null);
            site.setLongitude(null);

            roadAccessibilityEvaluator.evaluateRoadAccessibility(site);

            assertEquals(RoadAccessStatus.UNKNOWN, site.getRoadAccessStatus());
            assertNull(site.getRoadDistanceMeters());
            assertNull(site.getRoadDistanceKilometers());
            assertTrue(site.getRoadAccessReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 14: Out of bounds coordinates -> UNKNOWN")
        void testOutOfBoundsCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = createCandidateSite(200.0, 95.0);

            roadAccessibilityEvaluator.evaluateRoadAccessibility(site);

            assertEquals(RoadAccessStatus.UNKNOWN, site.getRoadAccessStatus());
            assertNull(site.getRoadDistanceMeters());
            assertTrue(site.getRoadAccessReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 15: Multi-dimensional orthogonal independence across all 4 criteria")
        void testMultiDimensionalIndependence() {
            CandidateSafeSiteDto site = createCandidateSite(85.503, 26.595);
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.FAR);

            // Evaluate road access as NEAR
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 150.0);

            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
            assertEquals(RoadAccessStatus.NEAR, site.getRoadAccessStatus());

            // Re-evaluate road access as FAR
            roadAccessibilityEvaluator.evaluateRoadAccessibility(site, 3500.0);

            // Other dimensions must remain unaffected
            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
            assertEquals(RoadAccessStatus.FAR, site.getRoadAccessStatus());
        }
    }

    @Nested
    @DisplayName("3. Service Road Access Filtering & GeoJSON Tests")
    class ServiceRoadFilteringTests {

        @Test
        @DisplayName("Test 16: Filter candidate sites by roadAccessStatus = UNKNOWN")
        void testFilterRoadAccessUnknown() {
            InfrastructureAssetDto asset1 = createAsset("FAC-1", "Hospital 1", InfrastructureCategory.HEALTHCARE, "Patna", 85.15, 25.62);
            InfrastructureAssetDto asset2 = createAsset("FAC-2", "Shelter 1", InfrastructureCategory.EMERGENCY_SERVICES, "Sitamarhi", 85.50, 26.59);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(asset1, asset2));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(List.of("Patna"));

            List<CandidateSafeSiteDto> results = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, "UNKNOWN");

            assertEquals(2, results.size());
            assertEquals(RoadAccessStatus.UNKNOWN, results.get(0).getRoadAccessStatus());
            assertEquals(RoadAccessStatus.UNKNOWN, results.get(1).getRoadAccessStatus());
        }

        @Test
        @DisplayName("Test 17: Filter candidate sites by roadAccessStatus = NEAR (empty when default is UNKNOWN)")
        void testFilterRoadAccessNear() {
            InfrastructureAssetDto asset1 = createAsset("FAC-1", "Hospital 1", InfrastructureCategory.HEALTHCARE, "Patna", 85.15, 25.62);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(asset1));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(List.of("Patna"));

            List<CandidateSafeSiteDto> results = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, "NEAR");

            assertEquals(0, results.size());
        }

        @Test
        @DisplayName("Test 18: Invalid roadAccessStatus filter throws HTTP 400 parameter exception")
        void testInvalidRoadAccessStatusThrows() {
            InfrastructureAssetDto asset1 = createAsset("FAC-1", "Hospital 1", InfrastructureCategory.HEALTHCARE, "Patna", 85.15, 25.62);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(asset1));

            InvalidHazardParameterException ex = assertThrows(
                    InvalidHazardParameterException.class,
                    () -> candidateSafeSiteService.getCandidateSites(
                            null, null, false, null, null, null, "FLYING_CAR")
            );
            assertTrue(ex.getMessage().contains("Invalid roadAccessStatus filter: 'FLYING_CAR'"));
        }

        @Test
        @DisplayName("Test 19: GeoJSON export contains road properties")
        void testGeoJsonRoadPropertiesEnrichment() {
            InfrastructureAssetDto asset1 = createAsset("FAC-1", "Shelter 1", InfrastructureCategory.EMERGENCY_SERVICES, "Patna", 85.15, 25.62);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(asset1));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(List.of("Patna"));

            GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                    null, null, false, null, null, null, null);

            assertNotNull(geojson);
            assertEquals(1, geojson.getFeatures().size());
            var feature = geojson.getFeatures().get(0);
            assertEquals("Point", feature.getGeometry().getType());
            assertTrue(feature.getProperties().containsKey("roadAccessStatus"));
            assertTrue(feature.getProperties().containsKey("roadAccessReason"));
            assertEquals("UNKNOWN", feature.getProperties().get("roadAccessStatus"));
            assertNull(feature.getProperties().get("roadDistanceMeters"));
            assertNull(feature.getProperties().get("roadDistanceKilometers"));
        }
    }

    // Helper methods
    private CandidateSafeSiteDto createCandidateSite(Double lon, Double lat) {
        CandidateSafeSiteDto site = new CandidateSafeSiteDto();
        site.setSiteId("FAC-TEST-001");
        site.setSiteName("Test Safe Site");
        site.setDistrict("Patna");
        site.setLongitude(lon);
        site.setLatitude(lat);
        return site;
    }

    private InfrastructureAssetDto createAsset(String id, String name, InfrastructureCategory category, String district, double lon, double lat) {
        InfrastructureAssetDto asset = new InfrastructureAssetDto();
        asset.setAssetId(id);
        asset.setAssetName(name);
        asset.setCategory(category);
        asset.setDistrictName(district);
        asset.setLongitude(lon);
        asset.setLatitude(lat);
        return asset;
    }
}
