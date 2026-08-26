package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import com.hazard.domain.safesite.*;
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
 * Stage 5.9 — Unit and Integration Tests for Supporting Infrastructure Intelligence.
 */
@ExtendWith(MockitoExtension.class)
class InfrastructureIntelligenceTests {

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
    private WaterEvaluator waterEvaluator;

    @Mock
    private SuitabilityEvaluator suitabilityEvaluator;

    private InfrastructureEvaluationConfig infrastructureConfig;
    private InfrastructureEvaluator infrastructureEvaluator;
    private SafeSiteRankingEvaluator safeSiteRankingEvaluator = new SafeSiteRankingEvaluator();
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        infrastructureConfig = new InfrastructureEvaluationConfig(2000.0, 10000.0);
        infrastructureEvaluator = new InfrastructureEvaluator(dataProvider, infrastructureConfig);
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
    @DisplayName("1. InfrastructureAccessStatus Enum Tests")
    class InfrastructureAccessStatusEnumTests {

        @Test
        @DisplayName("Test 1: Enum Values and Helper Predicates")
        void testEnumValuesAndHelpers() {
            assertEquals(4, InfrastructureAccessStatus.values().length);
            assertTrue(InfrastructureAccessStatus.NEAR.isNear());
            assertFalse(InfrastructureAccessStatus.NEAR.isModerate());
            assertFalse(InfrastructureAccessStatus.NEAR.isFar());
            assertTrue(InfrastructureAccessStatus.NEAR.isKnown());

            assertTrue(InfrastructureAccessStatus.MODERATE.isModerate());
            assertFalse(InfrastructureAccessStatus.MODERATE.isNear());
            assertTrue(InfrastructureAccessStatus.MODERATE.isKnown());

            assertTrue(InfrastructureAccessStatus.FAR.isFar());
            assertFalse(InfrastructureAccessStatus.FAR.isNear());
            assertTrue(InfrastructureAccessStatus.FAR.isKnown());

            assertFalse(InfrastructureAccessStatus.UNKNOWN.isKnown());
            assertFalse(InfrastructureAccessStatus.UNKNOWN.isNear());
        }

        @Test
        @DisplayName("Test 2: fromString Valid and Case-Insensitive Parsing")
        void testFromString() {
            assertEquals(InfrastructureAccessStatus.NEAR, InfrastructureAccessStatus.fromString("near"));
            assertEquals(InfrastructureAccessStatus.NEAR, InfrastructureAccessStatus.fromString("NEAR"));
            assertEquals(InfrastructureAccessStatus.MODERATE, InfrastructureAccessStatus.fromString("moderate"));
            assertEquals(InfrastructureAccessStatus.FAR, InfrastructureAccessStatus.fromString("FAR"));
            assertEquals(InfrastructureAccessStatus.UNKNOWN, InfrastructureAccessStatus.fromString("UNKNOWN"));
            assertEquals(InfrastructureAccessStatus.UNKNOWN, InfrastructureAccessStatus.fromString(null));
            assertEquals(InfrastructureAccessStatus.UNKNOWN, InfrastructureAccessStatus.fromString("   "));
        }

        @Test
        @DisplayName("Test 3: fromString Invalid Parameter Throws Exception")
        void testInvalidFromStringThrows() {
            InvalidHazardParameterException ex = assertThrows(
                    InvalidHazardParameterException.class,
                    () -> InfrastructureAccessStatus.fromString("SUPER_HUB")
            );
            assertTrue(ex.getMessage().contains("Allowed values: NEAR, MODERATE, FAR, UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("2. Infrastructure Proximity Threshold & Calculation Tests")
    class InfrastructureProximityThresholdTests {

        @Test
        @DisplayName("Test 4: Candidate with nearby supporting infrastructure (<= 2000m) -> NEAR")
        void testCandidateWithNearbySupportingInfrastructure() {
            InfrastructureAssetDto gov = createFacility("FAC-GOV-002", "Patna District Collectorate", InfrastructureCategory.GOVERNMENT, 85.1430, 25.6200);
            CandidateSafeSiteDto site = createCandidateSite("FAC-EDU-001", "NIT Patna", 85.1500, 25.6200);

            infrastructureEvaluator.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.NEAR, site.getInfrastructureAccessStatus());
            assertNotNull(site.getInfrastructureDistanceMeters());
            assertTrue(site.getInfrastructureDistanceMeters() <= 2000.0);
            assertEquals("FAC-GOV-002", site.getNearestInfrastructureSiteId());
            assertEquals("Patna District Collectorate", site.getNearestInfrastructureSiteName());
            assertEquals("GOVERNMENT", site.getNearestInfrastructureCategory());
            assertTrue(site.getInfrastructureReason().contains("close access to supporting infrastructure"));
        }

        @Test
        @DisplayName("Test 5: Candidate located directly at supporting facility (0.0 m) -> NEAR")
        void testCandidateDirectlyOnSupportingFacility() {
            InfrastructureAssetDto gov = createFacility("FAC-GOV-001", "State Secretariat", InfrastructureCategory.GOVERNMENT, 85.1220, 25.6060);
            CandidateSafeSiteDto site = createCandidateSite("FAC-GOV-001", "State Secretariat", 85.1220, 25.6060);

            infrastructureEvaluator.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.NEAR, site.getInfrastructureAccessStatus());
            assertEquals(0.0, site.getInfrastructureDistanceMeters());
            assertEquals(0.0, site.getInfrastructureDistanceKilometers());
            assertEquals("FAC-GOV-001", site.getNearestInfrastructureSiteId());
            assertEquals("GOVERNMENT", site.getNearestInfrastructureCategory());
            assertTrue(site.getInfrastructureReason().contains("direct on-site infrastructure access"));
        }

        @Test
        @DisplayName("Test 6: Candidate at intermediate distance (5.5 km, between 2.0km and 10.0km) -> MODERATE")
        void testCandidateModerateDistance() {
            InfrastructureAssetDto gov = createFacility("FAC-GOV-001", "State Secretariat", InfrastructureCategory.GOVERNMENT, 85.1220, 25.6060);
            // ~5.5 km away
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Suburban Relief Center", 85.1768, 25.6060);

            infrastructureEvaluator.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.MODERATE, site.getInfrastructureAccessStatus());
            assertTrue(site.getInfrastructureDistanceMeters() > 2000.0 && site.getInfrastructureDistanceMeters() < 10000.0);
            assertEquals("FAC-GOV-001", site.getNearestInfrastructureSiteId());
            assertTrue(site.getInfrastructureReason().contains("moderate supporting infrastructure proximity"));
        }

        @Test
        @DisplayName("Test 7: Candidate far from supporting infrastructure (>= 10.0km) -> FAR")
        void testCandidateFarDistance() {
            InfrastructureAssetDto gov = createFacility("FAC-GOV-001", "State Secretariat", InfrastructureCategory.GOVERNMENT, 85.1220, 25.6060);
            // ~100 km away
            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Shelter", 85.5030, 26.5950);

            infrastructureEvaluator.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.FAR, site.getInfrastructureAccessStatus());
            assertTrue(site.getInfrastructureDistanceMeters() >= 10000.0);
            assertTrue(site.getInfrastructureDistanceKilometers() >= 10.0);
            assertEquals("FAC-GOV-001", site.getNearestInfrastructureSiteId());
            assertTrue(site.getInfrastructureReason().contains("relatively distant"));
        }

        @Test
        @DisplayName("Test 8: Nearest supporting facility is correctly selected among multiple options")
        void testNearestSupportingFacilitySelection() {
            InfrastructureAssetDto patnaGov = createFacility("FAC-GOV-001", "State Secretariat", InfrastructureCategory.GOVERNMENT, 85.1220, 25.6060);
            InfrastructureAssetDto muzaffarpurEdu = createFacility("FAC-EDU-003", "Muzaffarpur University", InfrastructureCategory.EDUCATION, 85.3620, 26.1150);
            InfrastructureAssetDto sitamarhiGov = createFacility("FAC-GOV-003", "Sitamarhi Collectorate", InfrastructureCategory.GOVERNMENT, 85.4950, 26.5890);

            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Flood Shelter", 85.5030, 26.5950);

            infrastructureEvaluator.evaluateInfrastructureAccess(site, List.of(patnaGov, muzaffarpurEdu, sitamarhiGov));

            assertEquals("FAC-GOV-003", site.getNearestInfrastructureSiteId());
            assertEquals("Sitamarhi Collectorate", site.getNearestInfrastructureSiteName());
            assertEquals("GOVERNMENT", site.getNearestInfrastructureCategory());
            assertEquals(InfrastructureAccessStatus.NEAR, site.getInfrastructureAccessStatus());
            assertTrue(site.getInfrastructureDistanceMeters() < 2000.0);
        }

        @Test
        @DisplayName("Test 9: Hazardous and non-supporting infrastructure categories are strictly excluded")
        void testHazardousCategoriesExcluded() {
            InfrastructureAssetDto powerPlant = createFacility("FAC-PWR-001", "Power Plant", InfrastructureCategory.POWER, 85.1600, 25.6200);
            InfrastructureAssetDto bridge = createFacility("FAC-TRN-001", "Ganga Bridge", InfrastructureCategory.TRANSPORT, 85.1600, 25.6200);
            InfrastructureAssetDto dam = createFacility("FAC-WAT-001", "Dam", InfrastructureCategory.WATER, 85.1600, 25.6200);
            InfrastructureAssetDto fuelDepot = createFacility("FAC-OTH-001", "Fuel Depot", InfrastructureCategory.OTHER_CRITICAL, 85.1600, 25.6200);

            assertFalse(InfrastructureEvaluator.isUsefulSupportingInfrastructure(powerPlant));
            assertFalse(InfrastructureEvaluator.isUsefulSupportingInfrastructure(bridge));
            assertFalse(InfrastructureEvaluator.isUsefulSupportingInfrastructure(dam));
            assertFalse(InfrastructureEvaluator.isUsefulSupportingInfrastructure(fuelDepot));

            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1600, 25.6200);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(powerPlant, bridge, dam, fuelDepot));

            infrastructureEvaluator.evaluateInfrastructureAccess(site);

            assertEquals(InfrastructureAccessStatus.UNKNOWN, site.getInfrastructureAccessStatus());
            assertNull(site.getInfrastructureDistanceMeters());
            assertNull(site.getNearestInfrastructureSiteId());
            assertTrue(site.getInfrastructureReason().contains("No useful supporting infrastructure facilities available"));
        }

        @Test
        @DisplayName("Test 10: Missing supporting infrastructure dataset evaluates to UNKNOWN")
        void testMissingDatasetEvaluatesToUnknown() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(Collections.emptyList());

            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1580, 25.6208);
            infrastructureEvaluator.evaluateInfrastructureAccess(site);

            assertEquals(InfrastructureAccessStatus.UNKNOWN, site.getInfrastructureAccessStatus());
            assertNull(site.getInfrastructureDistanceMeters());
            assertNull(site.getInfrastructureDistanceKilometers());
            assertNull(site.getNearestInfrastructureSiteId());
            assertNull(site.getNearestInfrastructureSiteName());
            assertNull(site.getNearestInfrastructureCategory());
            assertTrue(site.getInfrastructureReason().contains("No useful supporting infrastructure facilities available"));
        }

        @Test
        @DisplayName("Test 11: Missing/null candidate coordinates evaluate to UNKNOWN")
        void testMissingCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-COORDS");
            site.setLatitude(null);
            site.setLongitude(null);

            infrastructureEvaluator.evaluateInfrastructureAccess(site);

            assertEquals(InfrastructureAccessStatus.UNKNOWN, site.getInfrastructureAccessStatus());
            assertNull(site.getInfrastructureDistanceMeters());
            assertTrue(site.getInfrastructureReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 12: Out of bounds candidate coordinates evaluate to UNKNOWN")
        void testOutOfBoundsCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = createCandidateSite("FAC-INVALID", "Invalid Coords", 195.0, 95.0);

            infrastructureEvaluator.evaluateInfrastructureAccess(site);

            assertEquals(InfrastructureAccessStatus.UNKNOWN, site.getInfrastructureAccessStatus());
            assertNull(site.getInfrastructureDistanceMeters());
            assertTrue(site.getInfrastructureReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 13: Infrastructure distance calculation matches SettlementExposureService.haversineDistanceMeters")
        void testDistanceCalculationMatchesHaversine() {
            InfrastructureAssetDto edu = createFacility("FAC-EDU-003", "University Campus", InfrastructureCategory.EDUCATION, 85.3620, 26.1150);
            double siteLon = 85.3850;
            double siteLat = 26.1210;
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Safe Site", siteLon, siteLat);

            infrastructureEvaluator.evaluateInfrastructureAccess(site, List.of(edu));

            double expectedMeters = SettlementExposureService.haversineDistanceMeters(siteLat, siteLon, 26.1150, 85.3620);
            double expectedRounded = Math.round(expectedMeters * 10.0) / 10.0;

            assertEquals(expectedRounded, site.getInfrastructureDistanceMeters(), 0.1);
        }

        @Test
        @DisplayName("Test 14: Multi-dimensional orthogonal independence across all 7 criteria")
        void testMultiDimensionalIndependence() {
            InfrastructureAssetDto gov = createFacility("FAC-GOV-001", "State Secretariat", InfrastructureCategory.GOVERNMENT, 85.1220, 25.6060);
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1220, 25.6060);

            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.FAR);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.NEAR);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);

            infrastructureEvaluator.evaluateInfrastructureAccess(site, List.of(gov));

            // All prior 6 dimensions must remain unaltered
            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
            assertEquals(RoadAccessStatus.UNKNOWN, site.getRoadAccessStatus());
            assertEquals(HealthcareAccessStatus.NEAR, site.getHealthcareAccessStatus());
            assertEquals(WaterAccessStatus.UNKNOWN, site.getWaterAccessStatus());
            assertEquals(InfrastructureAccessStatus.NEAR, site.getInfrastructureAccessStatus());
        }
    }

    @Nested
    @DisplayName("3. Service Infrastructure Filtering & GeoJSON Tests")
    class ServiceInfrastructureFilteringTests {

        @Test
        @DisplayName("Test 15: Filter candidate sites by infrastructureAccessStatus = NEAR")
        void testFilterInfrastructureNear() {
            InfrastructureAssetDto gov = createFacility("FAC-GOV-001", "State Secretariat", InfrastructureCategory.GOVERNMENT, 85.122, 25.606);
            InfrastructureAssetDto edu = createFacility("FAC-EDU-001", "NIT Patna", InfrastructureCategory.EDUCATION, 85.172, 25.621);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(gov, edu));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(List.of("Patna"));

            List<CandidateSafeSiteDto> results = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, "NEAR");

            assertEquals(2, results.size());
            assertEquals(InfrastructureAccessStatus.NEAR, results.get(0).getInfrastructureAccessStatus());
            assertEquals(InfrastructureAccessStatus.NEAR, results.get(1).getInfrastructureAccessStatus());
        }

        @Test
        @DisplayName("Test 16: Invalid infrastructureAccessStatus filter throws HTTP 400 parameter exception")
        void testInvalidInfrastructureAccessStatusThrows() {
            InfrastructureAssetDto gov = createFacility("FAC-GOV-001", "State Secretariat", InfrastructureCategory.GOVERNMENT, 85.122, 25.606);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(gov));

            InvalidHazardParameterException ex = assertThrows(
                    InvalidHazardParameterException.class,
                    () -> candidateSafeSiteService.getCandidateSites(
                            null, null, false, null, null, null, null, null, null, "SUPER_INFRA")
            );
            assertTrue(ex.getMessage().contains("Invalid infrastructureAccessStatus filter: 'SUPER_INFRA'"));
        }

        @Test
        @DisplayName("Test 17: GeoJSON export contains all 7 infrastructure properties")
        void testGeoJsonInfrastructurePropertiesEnrichment() {
            InfrastructureAssetDto gov = createFacility("FAC-GOV-001", "State Secretariat", InfrastructureCategory.GOVERNMENT, 85.122, 25.606);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(gov));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(List.of("Patna"));

            GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                    null, null, false, null, null, null, null, null, null, null);

            assertNotNull(geojson);
            assertEquals(1, geojson.getFeatures().size());
            var props = geojson.getFeatures().get(0).getProperties();

            assertTrue(props.containsKey("infrastructureDistanceMeters"));
            assertTrue(props.containsKey("infrastructureDistanceKilometers"));
            assertTrue(props.containsKey("infrastructureAccessStatus"));
            assertTrue(props.containsKey("nearestInfrastructureSiteId"));
            assertTrue(props.containsKey("nearestInfrastructureSiteName"));
            assertTrue(props.containsKey("nearestInfrastructureCategory"));
            assertTrue(props.containsKey("infrastructureReason"));

            assertEquals("NEAR", props.get("infrastructureAccessStatus"));
            assertEquals("FAC-GOV-001", props.get("nearestInfrastructureSiteId"));
            assertEquals("State Secretariat", props.get("nearestInfrastructureSiteName"));
            assertEquals("GOVERNMENT", props.get("nearestInfrastructureCategory"));
            assertEquals(0.0, props.get("infrastructureDistanceMeters"));
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

    private InfrastructureAssetDto createFacility(String id, String name, InfrastructureCategory category, double lon, double lat) {
        InfrastructureAssetDto asset = new InfrastructureAssetDto();
        asset.setAssetId(id);
        asset.setAssetName(name);
        asset.setCategory(category);
        asset.setDistrictName("Patna");
        asset.setLongitude(lon);
        asset.setLatitude(lat);
        asset.setCriticality(InfrastructureCriticality.HIGH);
        return asset;
    }
}
