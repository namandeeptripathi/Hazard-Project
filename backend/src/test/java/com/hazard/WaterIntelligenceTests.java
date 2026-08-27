package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import com.hazard.domain.safesite.DistanceStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.TerrainStatus;
import com.hazard.domain.safesite.WaterAccessStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.service.exposure.InfrastructureDataProvider;
import com.hazard.service.exposure.SettlementExposureService;
import com.hazard.service.risk.RedZoneService;
import com.hazard.service.risk.RiskCalculationService;
import com.hazard.service.safesite.CandidateSafeSiteService;
import com.hazard.service.safesite.SafeSiteThresholds;
import com.hazard.service.terrain.TerrainService;
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
    private DistrictBoundaryRepository districtBoundaryRepository;

    @Mock
    private TerrainService terrainService;

    @Mock
    private RiskCalculationService riskCalculationService;

    private SafeSiteThresholds thresholds;
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        thresholds = new SafeSiteThresholds();
        thresholds.setNearWaterDistanceMeters(1000.0);
        thresholds.setFarWaterDistanceMeters(5000.0);
        candidateSafeSiteService = new CandidateSafeSiteService(
                dataProvider,
                redZoneService,
                districtBoundaryRepository,
                terrainService,
                riskCalculationService,
                thresholds
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

            candidateSafeSiteService.evaluateWaterAccess(site, List.of(plant));

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

            candidateSafeSiteService.evaluateWaterAccess(site, List.of(plant));

            assertEquals(WaterAccessStatus.NEAR, site.getWaterAccessStatus());
            assertEquals(0.0, site.getWaterDistanceMeters());
            assertEquals(0.0, site.getWaterDistanceKilometers());
            assertTrue(site.getWaterReason().contains("directly on or adjacent to a useful water facility"));
        }

        @Test
        @DisplayName("Test 6: Candidate at intermediate water distance (2.5 km, between 1.0km and 5.0km) -> MODERATE")
        void testCandidateModerateWaterDistance() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Patna Central Water Treatment Plant", "water_treatment_plant", 85.1600, 25.6200);
            CandidateSafeSiteDto site = createCandidateSite("FAC-GOV-001", "State Secretariat", 85.1380, 25.6200);

            candidateSafeSiteService.evaluateWaterAccess(site, List.of(plant));

            assertEquals(WaterAccessStatus.MODERATE, site.getWaterAccessStatus());
            assertTrue(site.getWaterDistanceMeters() > 1000.0 && site.getWaterDistanceMeters() < 5000.0);
            assertEquals("FAC-WAT-001", site.getNearestWaterSiteId());
            assertTrue(site.getWaterReason().contains("moderate water facility proximity"));
        }

        @Test
        @DisplayName("Test 7: Candidate far from useful water facility (>= 5000m) -> FAR")
        void testCandidateFarWaterDistance() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Patna Central Water Treatment Plant", "water_treatment_plant", 85.1600, 25.6200);
            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Shelter", 85.5030, 26.5950);

            candidateSafeSiteService.evaluateWaterAccess(site, List.of(plant));

            assertEquals(WaterAccessStatus.FAR, site.getWaterAccessStatus());
            assertTrue(site.getWaterDistanceMeters() >= 5000.0);
            assertTrue(site.getWaterDistanceKilometers() >= 5.0);
            assertEquals("FAC-WAT-001", site.getNearestWaterSiteId());
            assertTrue(site.getWaterReason().contains("relatively distant"));
        }

        @Test
        @DisplayName("Test 8: Selection of nearest useful water facility from multiple candidates")
        void testNearestWaterFacilitySelection() {
            InfrastructureAssetDto plant1 = createWaterFacility("FAC-WAT-001", "Patna Plant", "water_treatment", 85.1600, 25.6200);
            InfrastructureAssetDto plant2 = createWaterFacility("FAC-WAT-002", "Sitamarhi Depot", "water_depot", 85.5000, 26.5900);
            InfrastructureAssetDto plant3 = createWaterFacility("FAC-WAT-003", "Gaya Tank", "water_tower", 84.9900, 24.7900);

            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Shelter", 85.5030, 26.5950);

            candidateSafeSiteService.evaluateWaterAccess(site, List.of(plant1, plant2, plant3));

            assertEquals("FAC-WAT-002", site.getNearestWaterSiteId());
            assertEquals("Sitamarhi Depot", site.getNearestWaterSiteName());
            assertEquals(WaterAccessStatus.NEAR, site.getWaterAccessStatus());
            assertTrue(site.getWaterDistanceMeters() < 1000.0);
        }

        @Test
        @DisplayName("Test 9: Distance matches canonical SettlementExposureService.haversineDistanceMeters")
        void testDistanceCalculationMatchesHaversine() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-004", "Muzaffarpur Waterworks", "water_supply_depot", 85.3910, 26.1210);
            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-004", "Muzaffarpur Shelter", 85.3850, 26.1150);

            candidateSafeSiteService.evaluateWaterAccess(site, List.of(plant));

            double expected = SettlementExposureService.haversineDistanceMeters(26.1150, 85.3850, 26.1210, 85.3910);
            double expectedRounded = Math.round(expected * 10.0) / 10.0;

            assertEquals(expectedRounded, site.getWaterDistanceMeters(), 0.1);
        }

        @Test
        @DisplayName("Test 10: Missing water facilities list evaluates to UNKNOWN")
        void testEmptyWaterFacilitiesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1600, 25.6200);

            candidateSafeSiteService.evaluateWaterAccess(site, Collections.emptyList());

            assertEquals(WaterAccessStatus.UNKNOWN, site.getWaterAccessStatus());
            assertNull(site.getWaterDistanceMeters());
            assertNull(site.getWaterDistanceKilometers());
            assertNull(site.getNearestWaterSiteId());
            assertNull(site.getNearestWaterSiteName());
            assertTrue(site.getWaterReason().contains("Useful emergency water supply data is not currently available"));
        }

        @Test
        @DisplayName("Test 11: Missing coordinates -> UNKNOWN")
        void testMissingCoordinatesEvaluatesToUnknown() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Patna Plant", "water_treatment", 85.1600, 25.6200);
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-COORDS");

            candidateSafeSiteService.evaluateWaterAccess(site, List.of(plant));

            assertEquals(WaterAccessStatus.UNKNOWN, site.getWaterAccessStatus());
            assertNull(site.getWaterDistanceMeters());
            assertTrue(site.getWaterReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 12: Out-of-bounds coordinates -> UNKNOWN")
        void testOutOfBoundsCoordinatesEvaluatesToUnknown() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Patna Plant", "water_treatment", 85.1600, 25.6200);
            CandidateSafeSiteDto site = createCandidateSite("FAC-INVALID", "Invalid Coordinates", 200.0, 95.0);

            candidateSafeSiteService.evaluateWaterAccess(site, List.of(plant));

            assertEquals(WaterAccessStatus.UNKNOWN, site.getWaterAccessStatus());
            assertNull(site.getWaterDistanceMeters());
            assertTrue(site.getWaterReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 13: Multi-dimensional orthogonal independence across all 6 criteria")
        void testMultiDimensionalIndependence() {
            InfrastructureAssetDto plant = createWaterFacility("FAC-WAT-001", "Patna Plant", "water_treatment", 85.1600, 25.6200);
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1600, 25.6200);
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.FAR);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);

            candidateSafeSiteService.evaluateWaterAccess(site, List.of(plant));

            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
            assertEquals(RoadAccessStatus.UNKNOWN, site.getRoadAccessStatus());
            assertEquals(WaterAccessStatus.NEAR, site.getWaterAccessStatus());
        }
    }

    @Nested
    @DisplayName("3. Water Supply Facility Filtering Tests")
    class WaterFacilityFilteringTests {

        @Test
        @DisplayName("Test 14: Included potable water infrastructure keywords")
        void testPotableWaterKeywordsIncluded() {
            InfrastructureAssetDto plant = createWaterFacility("1", "Water Plant", "water_treatment_plant", 85.0, 25.0);
            InfrastructureAssetDto supply = createWaterFacility("2", "Supply Depot", "potable_water_supply", 85.0, 25.0);
            InfrastructureAssetDto station = createWaterFacility("3", "Station", "drinking_water_purification", 85.0, 25.0);
            InfrastructureAssetDto tower = createWaterFacility("4", "Tower", "water_tower", 85.0, 25.0);

            assertTrue(CandidateSafeSiteService.isUsefulWaterSupplyFacility(plant));
            assertTrue(CandidateSafeSiteService.isUsefulWaterSupplyFacility(supply));
            assertTrue(CandidateSafeSiteService.isUsefulWaterSupplyFacility(station));
            assertTrue(CandidateSafeSiteService.isUsefulWaterSupplyFacility(tower));
        }

        @Test
        @DisplayName("Test 15: Excluded hydraulic / flood / non-potable waterways")
        void testNonPotableWaterwaysExcluded() {
            InfrastructureAssetDto canal = createWaterFacility("1", "Main Canal", "irrigation_canal", 85.0, 25.0);
            InfrastructureAssetDto drain = createWaterFacility("2", "Drainage Ditch", "storm_drain", 85.0, 25.0);
            InfrastructureAssetDto river = createWaterFacility("3", "River Feature", "river_embankment", 85.0, 25.0);
            InfrastructureAssetDto dam = createWaterFacility("4", "Dam", "retention_dam", 85.0, 25.0);

            assertFalse(CandidateSafeSiteService.isUsefulWaterSupplyFacility(canal));
            assertFalse(CandidateSafeSiteService.isUsefulWaterSupplyFacility(drain));
            assertFalse(CandidateSafeSiteService.isUsefulWaterSupplyFacility(river));
            assertFalse(CandidateSafeSiteService.isUsefulWaterSupplyFacility(dam));
        }
    }

    @Nested
    @DisplayName("4. Service Water Filtering & GeoJSON Tests")
    class ServiceWaterFilteringTests {

        @Test
        @DisplayName("Test 16: Filter candidate sites by waterAccessStatus = NEAR")
        void testFilterWaterNear() {
            InfrastructureAssetDto water = createWaterFacility("FAC-WAT-001", "Patna Plant", "water_treatment", 85.158, 25.6208);
            InfrastructureAssetDto edu = createAsset("FAC-EDU-001", "NIT Patna", InfrastructureCategory.EDUCATION, "Patna", 85.159, 25.621);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(water, edu));

            List<CandidateSafeSiteDto> results = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, "NEAR");

            assertEquals(1, results.size());
            assertEquals(WaterAccessStatus.NEAR, results.get(0).getWaterAccessStatus());
            assertEquals("FAC-EDU-001", results.get(0).getSiteId());
        }

        @Test
        @DisplayName("Test 17: GeoJSON export contains all 6 water properties")
        void testGeoJsonWaterPropertiesEnrichment() {
            InfrastructureAssetDto water = createWaterFacility("FAC-WAT-001", "Patna Plant", "water_treatment", 85.158, 25.6208);
            InfrastructureAssetDto edu = createAsset("FAC-EDU-001", "NIT Patna", InfrastructureCategory.EDUCATION, "Patna", 85.159, 25.621);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(water, edu));

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

            assertEquals("NEAR", props.get("waterAccessStatus"));
            assertEquals("FAC-WAT-001", props.get("nearestWaterSiteId"));
            assertEquals("Patna Plant", props.get("nearestWaterSiteName"));
        }
    }

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
