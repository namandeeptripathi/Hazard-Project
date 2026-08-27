package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import com.hazard.domain.safesite.DistanceStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.HealthcareAccessStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.TerrainStatus;
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
 * Stage 5.7 — Unit and Integration Tests for Healthcare Intelligence & Proximity Evaluation.
 */
@ExtendWith(MockitoExtension.class)
class HealthcareIntelligenceTests {

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
        thresholds.setNearHealthcareDistanceMeters(5000.0);
        thresholds.setFarHealthcareDistanceMeters(20000.0);
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
    @DisplayName("1. HealthcareAccessStatus Enum Tests")
    class HealthcareAccessStatusEnumTests {

        @Test
        @DisplayName("Test 1: Enum Values and Helpers")
        void testEnumValuesAndHelpers() {
            assertEquals(4, HealthcareAccessStatus.values().length);
            assertTrue(HealthcareAccessStatus.NEAR.isNear());
            assertFalse(HealthcareAccessStatus.NEAR.isModerate());
            assertFalse(HealthcareAccessStatus.NEAR.isFar());
            assertTrue(HealthcareAccessStatus.NEAR.isKnown());

            assertTrue(HealthcareAccessStatus.MODERATE.isModerate());
            assertFalse(HealthcareAccessStatus.MODERATE.isNear());
            assertTrue(HealthcareAccessStatus.MODERATE.isKnown());

            assertTrue(HealthcareAccessStatus.FAR.isFar());
            assertFalse(HealthcareAccessStatus.FAR.isNear());
            assertTrue(HealthcareAccessStatus.FAR.isKnown());

            assertFalse(HealthcareAccessStatus.UNKNOWN.isKnown());
            assertFalse(HealthcareAccessStatus.UNKNOWN.isNear());
        }

        @Test
        @DisplayName("Test 2: fromString Valid and Case-Insensitive Parsing")
        void testFromString() {
            assertEquals(HealthcareAccessStatus.NEAR, HealthcareAccessStatus.fromString("near"));
            assertEquals(HealthcareAccessStatus.NEAR, HealthcareAccessStatus.fromString("NEAR"));
            assertEquals(HealthcareAccessStatus.MODERATE, HealthcareAccessStatus.fromString("moderate"));
            assertEquals(HealthcareAccessStatus.FAR, HealthcareAccessStatus.fromString("FAR"));
            assertEquals(HealthcareAccessStatus.UNKNOWN, HealthcareAccessStatus.fromString("UNKNOWN"));
            assertEquals(HealthcareAccessStatus.UNKNOWN, HealthcareAccessStatus.fromString(null));
            assertEquals(HealthcareAccessStatus.UNKNOWN, HealthcareAccessStatus.fromString("   "));
        }

        @Test
        @DisplayName("Test 3: fromString Invalid Parameter Throws Exception")
        void testInvalidFromStringThrows() {
            InvalidHazardParameterException ex = assertThrows(
                    InvalidHazardParameterException.class,
                    () -> HealthcareAccessStatus.fromString("SUPER_CLOSE")
            );
            assertTrue(ex.getMessage().contains("Allowed values: NEAR, MODERATE, FAR, UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("2. Healthcare Proximity Threshold & Calculation Tests")
    class HealthcareProximityThresholdTests {

        @Test
        @DisplayName("Test 4: Candidate is itself a healthcare facility (0.0 m) -> NEAR")
        void testCandidateIsHealthcareFacility() {
            InfrastructureAssetDto hospital = createHealthcareFacility("FAC-MED-001", "PMCH", 85.1580, 25.6208);
            when(dataProvider.getHealthcareFacilities()).thenReturn(List.of(hospital));

            CandidateSafeSiteDto site = createCandidateSite("FAC-MED-001", "PMCH", 85.1580, 25.6208);
            candidateSafeSiteService.evaluateHealthcareAccess(site);

            assertEquals(HealthcareAccessStatus.NEAR, site.getHealthcareAccessStatus());
            assertEquals(0.0, site.getHealthcareDistanceMeters());
            assertEquals(0.0, site.getHealthcareDistanceKilometers());
            assertEquals("FAC-MED-001", site.getNearestHealthcareSiteId());
            assertEquals("PMCH", site.getNearestHealthcareSiteName());
            assertTrue(site.getHealthcareReason().contains("itself a configured healthcare facility"));
        }

        @Test
        @DisplayName("Test 5: Candidate close to healthcare facility (2.5 km <= 5.0 km) -> NEAR")
        void testCandidateCloseToHealthcare() {
            InfrastructureAssetDto hospital = createHealthcareFacility("FAC-MED-001", "PMCH", 85.1580, 25.6208);
            when(dataProvider.getHealthcareFacilities()).thenReturn(List.of(hospital));

            CandidateSafeSiteDto site = createCandidateSite("FAC-EDU-001", "NIT Patna", 85.1720, 25.6210);
            candidateSafeSiteService.evaluateHealthcareAccess(site);

            assertEquals(HealthcareAccessStatus.NEAR, site.getHealthcareAccessStatus());
            assertNotNull(site.getHealthcareDistanceMeters());
            assertTrue(site.getHealthcareDistanceMeters() <= 5000.0);
            assertEquals("FAC-MED-001", site.getNearestHealthcareSiteId());
            assertEquals("PMCH", site.getNearestHealthcareSiteName());
            assertTrue(site.getHealthcareReason().contains("close healthcare proximity"));
        }

        @Test
        @DisplayName("Test 6: Candidate at intermediate distance (12.0 km, between 5.0km and 20.0km) -> MODERATE")
        void testModerateHealthcareDistance() {
            InfrastructureAssetDto hospital = createHealthcareFacility("FAC-MED-001", "PMCH", 85.1580, 25.6208);
            when(dataProvider.getHealthcareFacilities()).thenReturn(List.of(hospital));

            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Intermediate Facility", 85.1580, 25.7308);
            candidateSafeSiteService.evaluateHealthcareAccess(site);

            assertEquals(HealthcareAccessStatus.MODERATE, site.getHealthcareAccessStatus());
            assertTrue(site.getHealthcareDistanceMeters() > 5000.0 && site.getHealthcareDistanceMeters() < 20000.0);
            assertEquals("FAC-MED-001", site.getNearestHealthcareSiteId());
            assertTrue(site.getHealthcareReason().contains("moderate healthcare proximity"));
        }

        @Test
        @DisplayName("Test 7: Candidate far from healthcare facility (>= 20.0 km) -> FAR")
        void testFarHealthcareDistance() {
            InfrastructureAssetDto hospital = createHealthcareFacility("FAC-MED-001", "PMCH", 85.1580, 25.6208);
            when(dataProvider.getHealthcareFacilities()).thenReturn(List.of(hospital));

            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Shelter", 85.5030, 26.5950);
            candidateSafeSiteService.evaluateHealthcareAccess(site);

            assertEquals(HealthcareAccessStatus.FAR, site.getHealthcareAccessStatus());
            assertTrue(site.getHealthcareDistanceMeters() >= 20000.0);
            assertTrue(site.getHealthcareDistanceKilometers() >= 20.0);
            assertEquals("FAC-MED-001", site.getNearestHealthcareSiteId());
            assertTrue(site.getHealthcareReason().contains("relatively distant"));
        }

        @Test
        @DisplayName("Test 8: Nearest healthcare facility selection selects the true minimum distance")
        void testNearestHealthcareSelection() {
            InfrastructureAssetDto patnaMed = createHealthcareFacility("FAC-MED-001", "PMCH", 85.1580, 25.6208);
            InfrastructureAssetDto sitamarhiMed = createHealthcareFacility("FAC-MED-007", "Sitamarhi Hospital", 85.4980, 26.5920);
            InfrastructureAssetDto gayaMed = createHealthcareFacility("FAC-MED-008", "Gaya Medical College", 84.9750, 24.7890);

            when(dataProvider.getHealthcareFacilities()).thenReturn(List.of(patnaMed, sitamarhiMed, gayaMed));

            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-003", "Sitamarhi Flood Shelter", 85.5030, 26.5950);
            candidateSafeSiteService.evaluateHealthcareAccess(site);

            assertEquals("FAC-MED-007", site.getNearestHealthcareSiteId());
            assertEquals("Sitamarhi Hospital", site.getNearestHealthcareSiteName());
            assertEquals(HealthcareAccessStatus.NEAR, site.getHealthcareAccessStatus());
            assertTrue(site.getHealthcareDistanceMeters() < 1000.0);
        }

        @Test
        @DisplayName("Test 9: Distance calculation matches canonical SettlementExposureService.haversineDistanceMeters")
        void testDistanceCalculationMatchesHaversine() {
            InfrastructureAssetDto hospital = createHealthcareFacility("FAC-MED-004", "SKMCH", 85.3910, 26.1520);
            when(dataProvider.getHealthcareFacilities()).thenReturn(List.of(hospital));

            double siteLon = 85.3850;
            double siteLat = 26.1210;
            CandidateSafeSiteDto site = createCandidateSite("FAC-EMG-004", "Muzaffarpur Control Center", siteLon, siteLat);
            candidateSafeSiteService.evaluateHealthcareAccess(site);

            double expectedMeters = SettlementExposureService.haversineDistanceMeters(siteLat, siteLon, 26.1520, 85.3910);
            double expectedRounded = Math.round(expectedMeters * 10.0) / 10.0;

            assertEquals(expectedRounded, site.getHealthcareDistanceMeters(), 0.1);
        }

        @Test
        @DisplayName("Test 10: Missing healthcare facilities in dataset -> UNKNOWN")
        void testMissingHealthcareFacilitiesEvaluatesToUnknown() {
            when(dataProvider.getHealthcareFacilities()).thenReturn(Collections.emptyList());

            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.158, 25.6208);
            candidateSafeSiteService.evaluateHealthcareAccess(site);

            assertEquals(HealthcareAccessStatus.UNKNOWN, site.getHealthcareAccessStatus());
            assertNull(site.getHealthcareDistanceMeters());
            assertNull(site.getHealthcareDistanceKilometers());
            assertNull(site.getNearestHealthcareSiteId());
            assertNull(site.getNearestHealthcareSiteName());
            assertTrue(site.getHealthcareReason().contains("No healthcare facilities found"));
        }

        @Test
        @DisplayName("Test 11: Missing/null coordinates -> UNKNOWN")
        void testMissingCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-COORDS");
            site.setLatitude(null);
            site.setLongitude(null);

            candidateSafeSiteService.evaluateHealthcareAccess(site);

            assertEquals(HealthcareAccessStatus.UNKNOWN, site.getHealthcareAccessStatus());
            assertNull(site.getHealthcareDistanceMeters());
            assertTrue(site.getHealthcareReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 12: Out of bounds coordinates -> UNKNOWN")
        void testOutOfBoundsCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = createCandidateSite("FAC-INVALID", "Invalid Coordinates", 200.0, 95.0);

            candidateSafeSiteService.evaluateHealthcareAccess(site);

            assertEquals(HealthcareAccessStatus.UNKNOWN, site.getHealthcareAccessStatus());
            assertNull(site.getHealthcareDistanceMeters());
            assertTrue(site.getHealthcareReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 13: Multi-dimensional orthogonal independence across all 5 criteria")
        void testMultiDimensionalIndependence() {
            InfrastructureAssetDto hospital = createHealthcareFacility("FAC-MED-001", "PMCH", 85.1580, 25.6208);
            when(dataProvider.getHealthcareFacilities()).thenReturn(List.of(hospital));

            CandidateSafeSiteDto site = createCandidateSite("FAC-TEST-001", "Test Site", 85.1580, 25.6208);
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.FAR);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);

            candidateSafeSiteService.evaluateHealthcareAccess(site);

            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
            assertEquals(RoadAccessStatus.UNKNOWN, site.getRoadAccessStatus());
            assertEquals(HealthcareAccessStatus.NEAR, site.getHealthcareAccessStatus());
        }
    }

    @Nested
    @DisplayName("3. Service Healthcare Filtering & GeoJSON Tests")
    class ServiceHealthcareFilteringTests {

        @Test
        @DisplayName("Test 14: Filter candidate sites by healthcareAccessStatus = NEAR")
        void testFilterHealthcareNear() {
            InfrastructureAssetDto med = createHealthcareFacility("FAC-MED-001", "PMCH", 85.158, 25.6208);
            InfrastructureAssetDto edu = createAsset("FAC-EDU-001", "NIT Patna", InfrastructureCategory.EDUCATION, "Patna", 85.172, 25.621);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(med, edu));

            List<CandidateSafeSiteDto> results = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, "NEAR");

            assertEquals(2, results.size());
            assertEquals(HealthcareAccessStatus.NEAR, results.get(0).getHealthcareAccessStatus());
            assertEquals(HealthcareAccessStatus.NEAR, results.get(1).getHealthcareAccessStatus());
        }

        @Test
        @DisplayName("Test 15: Invalid healthcareAccessStatus filter throws HTTP 400 parameter exception")
        void testInvalidHealthcareAccessStatusThrows() {
            InfrastructureAssetDto med = createHealthcareFacility("FAC-MED-001", "PMCH", 85.158, 25.6208);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(med));

            InvalidHazardParameterException ex = assertThrows(
                    InvalidHazardParameterException.class,
                    () -> candidateSafeSiteService.getCandidateSites(
                            null, null, false, null, null, null, null, "SUPER_HOSPITAL")
            );
            assertTrue(ex.getMessage().contains("Invalid healthcareAccessStatus filter: 'SUPER_HOSPITAL'"));
        }

        @Test
        @DisplayName("Test 16: GeoJSON export contains all 6 healthcare properties")
        void testGeoJsonHealthcarePropertiesEnrichment() {
            InfrastructureAssetDto med = createHealthcareFacility("FAC-MED-001", "PMCH", 85.158, 25.6208);
            when(dataProvider.getAllRegionalFacilities()).thenReturn(List.of(med));

            GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                    null, null, false, null, null, null, null, null);

            assertNotNull(geojson);
            assertEquals(1, geojson.getFeatures().size());
            var props = geojson.getFeatures().get(0).getProperties();

            assertTrue(props.containsKey("healthcareDistanceMeters"));
            assertTrue(props.containsKey("healthcareDistanceKilometers"));
            assertTrue(props.containsKey("healthcareAccessStatus"));
            assertTrue(props.containsKey("nearestHealthcareSiteId"));
            assertTrue(props.containsKey("nearestHealthcareSiteName"));
            assertTrue(props.containsKey("healthcareReason"));

            assertEquals("NEAR", props.get("healthcareAccessStatus"));
            assertEquals("FAC-MED-001", props.get("nearestHealthcareSiteId"));
            assertEquals("PMCH", props.get("nearestHealthcareSiteName"));
            assertEquals(0.0, props.get("healthcareDistanceMeters"));
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

    private InfrastructureAssetDto createHealthcareFacility(String id, String name, double lon, double lat) {
        InfrastructureAssetDto asset = new InfrastructureAssetDto();
        asset.setAssetId(id);
        asset.setAssetName(name);
        asset.setCategory(InfrastructureCategory.HEALTHCARE);
        asset.setDistrictName("Patna");
        asset.setLongitude(lon);
        asset.setLatitude(lat);
        asset.setCriticality(InfrastructureCriticality.VERY_HIGH);
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
