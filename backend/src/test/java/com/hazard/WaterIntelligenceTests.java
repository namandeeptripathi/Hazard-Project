package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import com.hazard.domain.safesite.DistanceStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.HealthcareAccessStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.TerrainStatus;
import com.hazard.domain.safesite.WaterAccessStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.service.exposure.InfrastructureDataProvider;
import com.hazard.service.exposure.SettlementExposureService;
import com.hazard.service.risk.RedZoneService;
import com.hazard.service.safesite.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Stage 5.8 — Unit and Integration Tests for Water Intelligence & Accessibility Evaluation.
 */
@ExtendWith(MockitoExtension.class)
class WaterIntelligenceTests {

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
    private RoadAccessibilityEvaluator roadAccessibilityEvaluator;

    @Mock
    private HealthcareEvaluator healthcareEvaluator;

    @Mock
    private InfrastructureEvaluator infrastructureEvaluator;

    @Mock
    private SuitabilityEvaluator suitabilityEvaluator;

    private WaterEvaluationConfig waterConfig;
    private WaterEvaluator waterEvaluator;
    private SafeSiteRankingEvaluator safeSiteRankingEvaluator = new SafeSiteRankingEvaluator();
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        waterConfig = new WaterEvaluationConfig(1000.0, 5000.0);
        waterEvaluator = new WaterEvaluator(dataProvider, waterConfig);
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
    @DisplayName("1. WaterAccessStatus Enum Tests")
    class WaterAccessStatusEnumTests {

        @Test
        @DisplayName("Test 1: Enum Values and Helper Predicates")
        void testEnumValuesAndHelpers() {
            assertEquals(4, WaterAccessStatus.values().length);
            assertTrue(WaterAccessStatus.NEAR.isNear());
            assertFalse(WaterAccessStatus.NEAR.isModerate());
            assertFalse(WaterAccessStatus.NEAR.isFar());
            assertTrue(WaterAccessStatus.NEAR.isKnown());

            assertTrue(WaterAccessStatus.MODERATE.isModerate());
            assertFalse(WaterAccessStatus.MODERATE.isNear());
            assertTrue(WaterAccessStatus.MODERATE.isKnown());

            assertTrue(WaterAccessStatus.FAR.isFar());
            assertFalse(WaterAccessStatus.FAR.isNear());
            assertTrue(WaterAccessStatus.FAR.isKnown());

            assertFalse(WaterAccessStatus.UNKNOWN.isKnown());
            assertFalse(WaterAccessStatus.UNKNOWN.isNear());
        }

        @Test
        @DisplayName("Test 2: fromString Valid and Case-Insensitive Parsing")
        void testFromString() {
            assertEquals(WaterAccessStatus.NEAR, WaterAccessStatus.fromString("near"));
            assertEquals(WaterAccessStatus.NEAR, WaterAccessStatus.fromString("NEAR"));
            assertEquals(WaterAccessStatus.MODERATE, WaterAccessStatus.fromString("moderate"));
            assertEquals(WaterAccessStatus.FAR, WaterAccessStatus.fromString("FAR"));
            assertEquals(WaterAccessStatus.UNKNOWN, WaterAccessStatus.fromString("UNKNOWN"));
            assertEquals(WaterAccessStatus.UNKNOWN, WaterAccessStatus.fromString(null));
            assertEquals(WaterAccessStatus.UNKNOWN, WaterAccessStatus.fromString("   "));
        }

        @Test
        @DisplayName("Test 3: fromString Invalid Parameter Throws Exception")
        void testInvalidFromStringThrows() {
            InvalidHazardParameterException ex = assertThrows(
                    InvalidHazardParameterException.class,
                    () -> WaterAccessStatus.fromString("VERY_WET")
            );
            assertTrue(ex.getMessage().contains("Allowed values: NEAR, MODERATE, FAR, UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("2. Water Proximity Threshold & Calculation Tests")
    class WaterProximityThresholdTests {

        @Test
        @DisplayName("Test 4: Candidate with nearby useful water facility (<= 1000m) -> NEAR")
        void testCandidateWithNearbyWaterFacility() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Patna Central Water Treatment Plant", "water_treatment_plant", 85.1600, 25.6200);
            CandidateSafeSiteDto site = createCandidateSite("FAC-EDU-001", "NIT Patna", 85.1620, 25.6205);

            waterEvaluator.evaluateWaterAccess(site, List.of(plant));

            assertEquals(WaterAccessStatus.NEAR, site.getWaterAccessStatus());
            assertNotNull(site.getWaterDistanceMeters());
            assertTrue(site.getWaterDistanceMeters() <= 1000.0);
            assertEquals("FAC-WAT-001", site.getNearestWaterSiteId());
            assertEquals("Patna Central Water Treatment Plant", site.getNearestWaterSiteName());
            assertTrue(site.getWaterReason().contains("close access to emergency water facility"));
        }

        @Test
        @DisplayName("Test 5: Candidate located directly at water facility (0.0 m) -> NEAR")
        void testCandidateDirectlyOnWaterFacility() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Patna Central Water Treatment Plant", "water_treatment_plant", 85.1600, 25.6200);
            CandidateSafeSiteDto site = createCandidateSite("FAC-WAT-001", "Patna Central Water Treatment Plant", 85.1600, 25.6200);

            waterEvaluator.evaluateWaterAccess(site, List.of(plant));

            assertEquals(WaterAccessStatus.NEAR, site.getWaterAccessStatus());
            assertEquals(0.0, site.getWaterDistanceMeters());
            assertEquals(0.0, site.getWaterDistanceKilometers());
            assertTrue(site.getWaterReason().contains("directly on or adjacent to a useful water facility"));
        }

        @Test
        @DisplayName("Test 6: Candidate at intermediate water distance (2.5 km, between 1.0km and 5.0km) -> MODERATE")
        void testCandidateModerateWaterDistance() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Patna Central Water Treatment Plant", "water_treatment_plant", 85.1600, 25.6200);
            // ~2.2 km away
            CandidateSafeSiteDto site = createCandidateSite("FAC-GOV-001", "State Secretariat", 85.1380, 25.6200);

            waterEvaluator.evaluateWaterAccess(site, List.of(plant));

            assertEquals(WaterAccessStatus.MODERATE, site.getWaterAccessStatus());
            assertTrue(site.getWaterDistanceMeters() > 1000.0 && site.getWaterDistanceMeters() < 5000.0);
            assertEquals("FAC-WAT-001", site.getNearestWaterSiteId());
            assertTrue(site.getWaterReason().contains("moderate water facility proximity"));
        }

        @Test
        @DisplayName("Test 7: Candidate far from useful water facility (>= 5000m) -> FAR")
        void testCandidateFarWaterDistance() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Patna Central Water Treatment Plant", "water_treatment_plant", 85.1600, 25.6200);
            // Sitamarhi is over 100 km away
            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Shelter", 85.5030, 26.5950);

            waterEvaluator.evaluateWaterAccess(site, List.of(plant));

            assertEquals(WaterAccessStatus.FAR, site.getWaterAccessStatus());
            assertTrue(site.getWaterDistanceMeters() >= 5000.0);
            assertTrue(site.getWaterDistanceKilometers() >= 5.0);
            assertEquals("FAC-WAT-001", site.getNearestWaterSiteId());
            assertTrue(site.getWaterReason().contains("relatively distant"));
        }

        @Test
        @DisplayName("Test 8: Nearest useful water facility is correctly selected among multiple options")
        void testNearestWaterFacilitySelection() {
            InfrastructureAssetDto patnaPlant = createWaterFacility("FAC-WAT-001", "Patna Water Plant", "water_treatment_plant", 85.1600, 25.6200);
            InfrastructureAssetDto muzaffarpurDepot = createWaterFacility("FAC-WAT-002", "Muzaffarpur Water Depot", "drinking_water_station", 85.3850, 26.1200);
            InfrastructureAssetDto sitamarhiSupply = createWaterFacility("FAC-WAT-003", "Sitamarhi Emergency Supply", "potable_water_supply", 85.4980, 26.5920);

            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Flood Shelter", 85.5030, 26.5950);

            waterEvaluator.evaluateWaterAccess(site, List.of(patnaPlant, muzaffarpurDepot, sitamarhiSupply));

            assertEquals("FAC-WAT-003", site.getNearestWaterSiteId());
            assertEquals("Sitamarhi Emergency Supply", site.getNearestWaterSiteName());
            assertEquals(WaterAccessStatus.NEAR, site.getWaterAccessStatus());
            assertTrue(site.getWaterDistanceMeters() < 1000.0);
        }

        @Test
        @DisplayName("Test 9: Raw canals, dams, and drainage channels are excluded from useful water facilities")
        void testRawWaterwaysExcludedFromUsefulWater() {
            InfrastructureAssetDto canal = createWaterFacility("RAW-001", "Main Irrigation Canal", "irrigation_canal", 85.1600, 25.6200);
            InfrastructureAssetDto dam = createWaterFacility("RAW-002", "River Dam", "earth_dam", 85.1600, 25.6200);
            InfrastructureAssetDto drain = createWaterFacility("RAW-003", "Municipal Drain", "storm_drain", 85.1600, 25.6200);
            InfrastructureAssetDto river = createWaterFacility("RAW-004", "River Reach", "river_channel", 85.1600, 25.6200);

            assertFalse(WaterEvaluator.isUsefulWaterSupplyFacility(canal));
            assertFalse(WaterEvaluator.isUsefulWaterSupplyFacility(dam));
            assertFalse(WaterEvaluator.isUsefulWaterSupplyFacility(drain));
            assertFalse(WaterEvaluator.isUsefulWaterSupplyFacility(river));

            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1600, 25.6200);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(canal, dam, drain, river));

            waterEvaluator.evaluateWaterAccess(site);

            assertEquals(WaterAccessStatus.UNKNOWN, site.getWaterAccessStatus());
            assertNull(site.getWaterDistanceMeters());
            assertNull(site.getNearestWaterSiteId());
            assertTrue(site.getWaterReason().contains("Useful emergency water supply data is not currently available"));
        }

        @Test
        @DisplayName("Test 10: Missing water dataset evaluates to UNKNOWN with descriptive provenance reason")
        void testMissingWaterDatasetEvaluatesToUnknown() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(Collections.emptyList());

            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1580, 25.6208);
            waterEvaluator.evaluateWaterAccess(site);

            assertEquals(WaterAccessStatus.UNKNOWN, site.getWaterAccessStatus());
            assertNull(site.getWaterDistanceMeters());
            assertNull(site.getWaterDistanceKilometers());
            assertNull(site.getNearestWaterSiteId());
            assertNull(site.getNearestWaterSiteName());
            assertTrue(site.getWaterReason().contains("Useful emergency water supply data is not currently available"));
        }

        @Test
        @DisplayName("Test 11: Missing/null candidate coordinates evaluate to UNKNOWN")
        void testMissingCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-COORDS");
            site.setLatitude(null);
            site.setLongitude(null);

            waterEvaluator.evaluateWaterAccess(site);

            assertEquals(WaterAccessStatus.UNKNOWN, site.getWaterAccessStatus());
            assertNull(site.getWaterDistanceMeters());
            assertTrue(site.getWaterReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 12: Out of bounds candidate coordinates evaluate to UNKNOWN")
        void testOutOfBoundsCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = createCandidateSite("FAC-INVALID", "Invalid Coords", 195.0, 95.0);

            waterEvaluator.evaluateWaterAccess(site);

            assertEquals(WaterAccessStatus.UNKNOWN, site.getWaterAccessStatus());
            assertNull(site.getWaterDistanceMeters());
            assertTrue(site.getWaterReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 13: Water distance calculation matches SettlementExposureService.haversineDistanceMeters")
        void testDistanceCalculationMatchesHaversine() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Water Plant", "water_treatment_plant", 85.3910, 26.1520);
            double siteLon = 85.3850;
            double siteLat = 26.1210;
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Safe Site", siteLon, siteLat);

            waterEvaluator.evaluateWaterAccess(site, List.of(plant));

            double expectedMeters = SettlementExposureService.haversineDistanceMeters(siteLat, siteLon, 26.1520, 85.3910);
            double expectedRounded = Math.round(expectedMeters * 10.0) / 10.0;

            assertEquals(expectedRounded, site.getWaterDistanceMeters(), 0.1);
        }

        @Test
        @DisplayName("Test 14: Multi-dimensional orthogonal independence across all 6 criteria")
        void testMultiDimensionalIndependence() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Water Plant", "water_treatment_plant", 85.1580, 25.6208);
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1580, 25.6208);

            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.FAR);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.NEAR);

            waterEvaluator.evaluateWaterAccess(site, List.of(plant));

            // All prior 5 dimensions must remain unaltered
            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
            assertEquals(RoadAccessStatus.UNKNOWN, site.getRoadAccessStatus());
            assertEquals(HealthcareAccessStatus.NEAR, site.getHealthcareAccessStatus());
            assertEquals(WaterAccessStatus.NEAR, site.getWaterAccessStatus());
        }
    }

    @Nested
    @DisplayName("3. Service Water Filtering & GeoJSON Tests")
    class ServiceWaterFilteringTests {

        @Test
        @DisplayName("Test 15: Filter candidate sites by waterAccessStatus = UNKNOWN (default dataset behavior)")
        void testFilterWaterUnknown() {
            InfrastructureAssetDto edu = createAsset("FAC-EDU-001", "NIT Patna", InfrastructureCategory.EDUCATION, "Patna", 85.172, 25.621);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(edu));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(List.of("Patna"));

            List<CandidateSafeSiteDto> results = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, "UNKNOWN");

            assertEquals(1, results.size());
            assertEquals(WaterAccessStatus.UNKNOWN, results.get(0).getWaterAccessStatus());
        }

        @Test
        @DisplayName("Test 16: Invalid waterAccessStatus filter throws HTTP 400 parameter exception")
        void testInvalidWaterAccessStatusThrows() {
            InfrastructureAssetDto edu = createAsset("FAC-EDU-001", "NIT Patna", InfrastructureCategory.EDUCATION, "Patna", 85.172, 25.621);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(edu));

            InvalidHazardParameterException ex = assertThrows(
                    InvalidHazardParameterException.class,
                    () -> candidateSafeSiteService.getCandidateSites(
                            null, null, false, null, null, null, null, null, "FLOOD_SAFE")
            );
            assertTrue(ex.getMessage().contains("Invalid waterAccessStatus filter: 'FLOOD_SAFE'"));
        }

        @Test
        @DisplayName("Test 17: GeoJSON export contains all 6 water properties")
        void testGeoJsonWaterPropertiesEnrichment() {
            InfrastructureAssetDto edu = createAsset("FAC-EDU-001", "NIT Patna", InfrastructureCategory.EDUCATION, "Patna", 85.172, 25.621);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(edu));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(List.of("Patna"));

            GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                    null, null, false, null, null, null, null, null, null);

            assertNotNull(geojson);
            assertEquals(1, geojson.getFeatures().size());
            var props = geojson.getFeatures().get(0).getProperties();

            assertTrue(props.containsKey("waterDistanceMeters"));
            assertTrue(props.containsKey("waterDistanceKilometers"));
            assertTrue(props.containsKey("waterAccessStatus"));
            assertTrue(props.containsKey("nearestWaterSiteId"));
            assertTrue(props.containsKey("nearestWaterSiteName"));
            assertTrue(props.containsKey("waterReason"));

            assertEquals("UNKNOWN", props.get("waterAccessStatus"));
            assertTrue(props.get("waterReason").toString().contains("Useful emergency water supply data is not currently available"));
        }
    }

    // Helper methods
    private CandidateSafeSiteDto createCandidateSite(String id, String name, Double lon, Double lat) {
        CandidateSafeSiteDto site = new CandidateSafeSiteDto();
        site.setSiteId(id);
        site.setSiteName(name);
        site.setDistrict("Patna");
        site.setLongitude(lon);
        site.setLatitude(lat);
        return site;
    }

    private InfrastructureAssetDto createWaterFacility(String id, String name, String subType, double lon, double lat) {
        InfrastructureAssetDto asset = new InfrastructureAssetDto();
        asset.setAssetId(id);
        asset.setAssetName(name);
        asset.setCategory(InfrastructureCategory.WATER);
        asset.setSubType(subType);
        asset.setDistrictName("Patna");
        asset.setLongitude(lon);
        asset.setLatitude(lat);
        asset.setCriticality(InfrastructureCriticality.HIGH);
        return asset;
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
