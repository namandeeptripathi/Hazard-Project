package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import com.hazard.domain.safesite.*;
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
 * Stage 5.9 — Unit and Integration Tests for Supporting Infrastructure Intelligence.
 */
@ExtendWith(MockitoExtension.class)
class InfrastructureIntelligenceTests {

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
        thresholds.setNearInfrastructureDistanceMeters(2000.0);
        thresholds.setFarInfrastructureDistanceMeters(10000.0);
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
                    () -> InfrastructureAccessStatus.fromString("SUPER_CLOSE")
            );
            assertTrue(ex.getMessage().contains("Allowed values: NEAR, MODERATE, FAR, UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("2. Supporting Category Filtering Tests")
    class SupportingCategoryFilteringTests {

        @Test
        @DisplayName("Test 4: Allowed Supporting Categories (EDU, GOV, EMERGENCY, HEALTH, COMM) qualify")
        void testAllowedSupportingCategories() {
            InfrastructureAssetDto edu = createAsset("1", "School", InfrastructureCategory.EDUCATION, "Patna", 85.0, 25.0);
            InfrastructureAssetDto gov = createAsset("2", "Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.0, 25.0);
            InfrastructureAssetDto emg = createAsset("3", "Fire Station", InfrastructureCategory.EMERGENCY_SERVICES, "Patna", 85.0, 25.0);
            InfrastructureAssetDto health = createAsset("4", "Clinic", InfrastructureCategory.HEALTHCARE, "Patna", 85.0, 25.0);
            InfrastructureAssetDto comm = createAsset("5", "Tower", InfrastructureCategory.COMMUNICATION, "Patna", 85.0, 25.0);

            assertTrue(CandidateSafeSiteService.isUsefulSupportingInfrastructure(edu));
            assertTrue(CandidateSafeSiteService.isUsefulSupportingInfrastructure(gov));
            assertTrue(CandidateSafeSiteService.isUsefulSupportingInfrastructure(emg));
            assertTrue(CandidateSafeSiteService.isUsefulSupportingInfrastructure(health));
            assertTrue(CandidateSafeSiteService.isUsefulSupportingInfrastructure(comm));
        }

        @Test
        @DisplayName("Test 5: Excluded / Hazardous Categories (POWER, TRANSPORT, WATER, OTHER) do NOT qualify")
        void testExcludedCategoriesDoNotQualify() {
            InfrastructureAssetDto power = createAsset("1", "Grid Substation", InfrastructureCategory.POWER, "Patna", 85.0, 25.0);
            InfrastructureAssetDto trans = createAsset("2", "Bridge", InfrastructureCategory.TRANSPORT, "Patna", 85.0, 25.0);
            InfrastructureAssetDto water = createAsset("3", "Canal", InfrastructureCategory.WATER, "Patna", 85.0, 25.0);
            InfrastructureAssetDto other = createAsset("4", "Depot", InfrastructureCategory.OTHER_CRITICAL, "Patna", 85.0, 25.0);

            assertFalse(CandidateSafeSiteService.isUsefulSupportingInfrastructure(power));
            assertFalse(CandidateSafeSiteService.isUsefulSupportingInfrastructure(trans));
            assertFalse(CandidateSafeSiteService.isUsefulSupportingInfrastructure(water));
            assertFalse(CandidateSafeSiteService.isUsefulSupportingInfrastructure(other));
        }
    }

    @Nested
    @DisplayName("3. Infrastructure Proximity Threshold & Calculation Tests")
    class InfrastructureProximityThresholdTests {

        @Test
        @DisplayName("Test 6: Candidate located directly at supporting facility (0.0 m) -> NEAR")
        void testCandidateDirectlyOnSupportingFacility() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-001", "Patna Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.1200, 25.6000);
            CandidateSafeSiteDto site = createCandidateSite("FAC-GOV-001", "Patna Secretariat", 85.1200, 25.6000);

            candidateSafeSiteService.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.NEAR, site.getInfrastructureAccessStatus());
            assertEquals(0.0, site.getInfrastructureDistanceMeters());
            assertEquals(0.0, site.getInfrastructureDistanceKilometers());
            assertEquals("FAC-GOV-001", site.getNearestInfrastructureSiteId());
            assertEquals("Patna Secretariat", site.getNearestInfrastructureSiteName());
            assertEquals("GOVERNMENT", site.getNearestInfrastructureCategory());
            assertTrue(site.getInfrastructureReason().contains("configured supporting facility"));
        }

        @Test
        @DisplayName("Test 7: Candidate close to supporting infrastructure (<= 2000m) -> NEAR")
        void testCandidateCloseToSupportingInfrastructure() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-001", "Patna Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.1200, 25.6000);
            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-001", "Relief Center", 85.1300, 25.6050);

            candidateSafeSiteService.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.NEAR, site.getInfrastructureAccessStatus());
            assertNotNull(site.getInfrastructureDistanceMeters());
            assertTrue(site.getInfrastructureDistanceMeters() <= 2000.0);
            assertEquals("FAC-GOV-001", site.getNearestInfrastructureSiteId());
            assertEquals("Patna Secretariat", site.getNearestInfrastructureSiteName());
            assertEquals("GOVERNMENT", site.getNearestInfrastructureCategory());
            assertTrue(site.getInfrastructureReason().contains("close access to supporting infrastructure"));
        }

        @Test
        @DisplayName("Test 8: Candidate at intermediate infrastructure distance (5.0 km, between 2.0km and 10.0km) -> MODERATE")
        void testCandidateModerateInfrastructureDistance() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-001", "Patna Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.1200, 25.6000);
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Outskirt Site", 85.1200, 25.6500);

            candidateSafeSiteService.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.MODERATE, site.getInfrastructureAccessStatus());
            assertTrue(site.getInfrastructureDistanceMeters() > 2000.0 && site.getInfrastructureDistanceMeters() < 10000.0);
            assertEquals("FAC-GOV-001", site.getNearestInfrastructureSiteId());
            assertTrue(site.getInfrastructureReason().contains("moderate supporting infrastructure proximity"));
        }

        @Test
        @DisplayName("Test 9: Candidate far from supporting infrastructure (>= 10000m) -> FAR")
        void testCandidateFarInfrastructureDistance() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-001", "Patna Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.1200, 25.6000);
            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Shelter", 85.5030, 26.5950);

            candidateSafeSiteService.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.FAR, site.getInfrastructureAccessStatus());
            assertTrue(site.getInfrastructureDistanceMeters() >= 10000.0);
            assertTrue(site.getInfrastructureDistanceKilometers() >= 10.0);
            assertEquals("FAC-GOV-001", site.getNearestInfrastructureSiteId());
            assertTrue(site.getInfrastructureReason().contains("relatively distant"));
        }

        @Test
        @DisplayName("Test 10: Selection of nearest supporting facility selects true minimum")
        void testNearestFacilitySelection() {
            InfrastructureAssetDto govPatna = createAsset("FAC-GOV-001", "Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.1200, 25.6000);
            InfrastructureAssetDto eduSita = createAsset("FAC-EDU-003", "Sitamarhi High School", InfrastructureCategory.EDUCATION, "Sitamarhi", 85.5000, 26.5900);

            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Flood Shelter", 85.5010, 26.5910);

            candidateSafeSiteService.evaluateInfrastructureAccess(site, List.of(govPatna, eduSita));

            assertEquals("FAC-EDU-003", site.getNearestInfrastructureSiteId());
            assertEquals("Sitamarhi High School", site.getNearestInfrastructureSiteName());
            assertEquals("EDUCATION", site.getNearestInfrastructureCategory());
            assertEquals(InfrastructureAccessStatus.NEAR, site.getInfrastructureAccessStatus());
            assertTrue(site.getInfrastructureDistanceMeters() < 500.0);
        }

        @Test
        @DisplayName("Test 11: Distance calculation matches canonical SettlementExposureService.haversineDistanceMeters")
        void testDistanceCalculationMatchesHaversine() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-004", "Muzaffarpur Collectorate", InfrastructureCategory.GOVERNMENT, "Muzaffarpur", 85.3910, 26.1210);
            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-004", "Muzaffarpur Shelter", 85.3850, 26.1150);

            candidateSafeSiteService.evaluateInfrastructureAccess(site, List.of(gov));

            double expected = SettlementExposureService.haversineDistanceMeters(26.1150, 85.3850, 26.1210, 85.3910);
            double expectedRounded = Math.round(expected * 10.0) / 10.0;

            assertEquals(expectedRounded, site.getInfrastructureDistanceMeters(), 0.1);
        }

        @Test
        @DisplayName("Test 12: Empty supporting facilities list evaluates to UNKNOWN")
        void testEmptyFacilitiesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1200, 25.6000);

            candidateSafeSiteService.evaluateInfrastructureAccess(site, Collections.emptyList());

            assertEquals(InfrastructureAccessStatus.UNKNOWN, site.getInfrastructureAccessStatus());
            assertNull(site.getInfrastructureDistanceMeters());
            assertNull(site.getInfrastructureDistanceKilometers());
            assertNull(site.getNearestInfrastructureSiteId());
            assertNull(site.getNearestInfrastructureSiteName());
            assertNull(site.getNearestInfrastructureCategory());
            assertTrue(site.getInfrastructureReason().contains("No useful supporting infrastructure facilities available"));
        }

        @Test
        @DisplayName("Test 13: Missing coordinates -> UNKNOWN")
        void testMissingCoordinatesEvaluatesToUnknown() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-001", "Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.1200, 25.6000);
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-COORDS");

            candidateSafeSiteService.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.UNKNOWN, site.getInfrastructureAccessStatus());
            assertNull(site.getInfrastructureDistanceMeters());
            assertTrue(site.getInfrastructureReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 14: Out-of-bounds coordinates -> UNKNOWN")
        void testOutOfBoundsCoordinatesEvaluatesToUnknown() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-001", "Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.1200, 25.6000);
            CandidateSafeSiteDto site = createCandidateSite("FAC-INVALID", "Invalid Coordinates", 200.0, 95.0);

            candidateSafeSiteService.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(InfrastructureAccessStatus.UNKNOWN, site.getInfrastructureAccessStatus());
            assertNull(site.getInfrastructureDistanceMeters());
            assertTrue(site.getInfrastructureReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 15: Multi-dimensional orthogonal independence across all 7 criteria")
        void testMultiDimensionalIndependence() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-001", "Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.1200, 25.6000);
            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1200, 25.6000);
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.FAR);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setWaterAccessStatus(WaterAccessStatus.NEAR);

            candidateSafeSiteService.evaluateInfrastructureAccess(site, List.of(gov));

            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
            assertEquals(RoadAccessStatus.UNKNOWN, site.getRoadAccessStatus());
            assertEquals(WaterAccessStatus.NEAR, site.getWaterAccessStatus());
            assertEquals(InfrastructureAccessStatus.NEAR, site.getInfrastructureAccessStatus());
        }
    }

    @Nested
    @DisplayName("4. Service Infrastructure Filtering & GeoJSON Tests")
    class ServiceInfrastructureFilteringTests {

        @Test
        @DisplayName("Test 16: Filter candidate sites by infrastructureAccessStatus = NEAR")
        void testFilterInfrastructureNear() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-001", "Patna Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.120, 25.600);
            InfrastructureAssetDto edu = createAsset("FAC-EDU-001", "NIT Patna", InfrastructureCategory.EDUCATION, "Patna", 85.121, 25.601);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(gov, edu));

            List<CandidateSafeSiteDto> results = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, "NEAR");

            assertEquals(2, results.size());
            assertEquals(InfrastructureAccessStatus.NEAR, results.get(0).getInfrastructureAccessStatus());
            assertEquals(InfrastructureAccessStatus.NEAR, results.get(1).getInfrastructureAccessStatus());
        }

        @Test
        @DisplayName("Test 17: GeoJSON export contains all 7 supporting infrastructure properties")
        void testGeoJsonInfrastructurePropertiesEnrichment() {
            InfrastructureAssetDto gov = createAsset("FAC-GOV-001", "Patna Secretariat", InfrastructureCategory.GOVERNMENT, "Patna", 85.120, 25.600);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(gov));

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
            assertEquals("Patna Secretariat", props.get("nearestInfrastructureSiteName"));
            assertEquals("GOVERNMENT", props.get("nearestInfrastructureCategory"));
            assertEquals(0.0, props.get("infrastructureDistanceMeters"));
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

    private InfrastructureAssetDto createAsset(String id, String name, InfrastructureCategory category, String district, double lon, double lat) {
        InfrastructureAssetDto asset = new InfrastructureAssetDto();
        asset.setAssetId(id);
        asset.setAssetName(name);
        asset.setCategory(category);
        asset.setDistrictName(district);
        asset.setLongitude(lon);
        asset.setLatitude(lat);
        asset.setCriticality(InfrastructureCriticality.HIGH);
        return asset;
    }
}
