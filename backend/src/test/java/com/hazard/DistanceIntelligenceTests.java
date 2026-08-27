package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.risk.ZoneLevel;
import com.hazard.domain.safesite.DistanceStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.TerrainStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.risk.RedZoneDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.boundaries.DistrictDistanceProjection;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Stage 5.5 — Unit and Integration Tests for Distance Intelligence.
 */
@ExtendWith(MockitoExtension.class)
class DistanceIntelligenceTests {

    @Mock
    private DistrictBoundaryRepository districtBoundaryRepository;

    @Mock
    private RedZoneService redZoneService;

    @Mock
    private InfrastructureDataProvider dataProvider;

    @Mock
    private TerrainService terrainService;

    @Mock
    private RiskCalculationService riskCalculationService;

    private SafeSiteThresholds thresholds;
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        thresholds = new SafeSiteThresholds();
        thresholds.setNearDistanceKm(5.0);
        thresholds.setFarDistanceKm(20.0);
        candidateSafeSiteService = new CandidateSafeSiteService(
                dataProvider,
                redZoneService,
                districtBoundaryRepository,
                terrainService,
                riskCalculationService,
                thresholds
        );
    }

    private DistrictDistanceProjection createMockProjection(String districtName, double distanceMeters) {
        return new DistrictDistanceProjection() {
            @Override
            public String getDistrictName() {
                return districtName;
            }

            @Override
            public Double getDistanceMeters() {
                return distanceMeters;
            }
        };
    }

    private RedZoneDto createRedZone(String districtName, ZoneLevel level) {
        RedZoneDto rz = new RedZoneDto();
        rz.setDistrictName(districtName);
        rz.setZoneLevel(level);
        rz.setRedZone(level == ZoneLevel.CRITICAL);
        return rz;
    }

    // =========================================================================
    // 1. DistanceStatus Enum Tests
    // =========================================================================

    @Nested
    @DisplayName("5.5.1: DistanceStatus Enum & Helper Methods")
    class DistanceStatusEnumTests {

        @Test
        @DisplayName("DistanceStatus boolean helper methods behave correctly")
        void testEnumBooleans() {
            assertTrue(DistanceStatus.NEAR.isNear());
            assertFalse(DistanceStatus.NEAR.isModerate());
            assertFalse(DistanceStatus.NEAR.isFar());
            assertFalse(DistanceStatus.NEAR.isUnknown());

            assertTrue(DistanceStatus.MODERATE.isModerate());
            assertFalse(DistanceStatus.MODERATE.isNear());
            assertFalse(DistanceStatus.MODERATE.isFar());
            assertFalse(DistanceStatus.MODERATE.isUnknown());

            assertTrue(DistanceStatus.FAR.isFar());
            assertFalse(DistanceStatus.FAR.isNear());
            assertFalse(DistanceStatus.FAR.isModerate());
            assertFalse(DistanceStatus.FAR.isUnknown());

            assertTrue(DistanceStatus.UNKNOWN.isUnknown());
            assertFalse(DistanceStatus.UNKNOWN.isNear());
            assertFalse(DistanceStatus.UNKNOWN.isModerate());
            assertFalse(DistanceStatus.UNKNOWN.isFar());
        }

        @Test
        @DisplayName("fromString parses case-insensitively with aliases")
        void testFromString() {
            assertEquals(DistanceStatus.NEAR, DistanceStatus.fromString("NEAR"));
            assertEquals(DistanceStatus.NEAR, DistanceStatus.fromString("near"));
            assertEquals(DistanceStatus.NEAR, DistanceStatus.fromString("close"));
            assertEquals(DistanceStatus.MODERATE, DistanceStatus.fromString("MODERATE"));
            assertEquals(DistanceStatus.MODERATE, DistanceStatus.fromString("moderate"));
            assertEquals(DistanceStatus.MODERATE, DistanceStatus.fromString("intermediate"));
            assertEquals(DistanceStatus.FAR, DistanceStatus.fromString("FAR"));
            assertEquals(DistanceStatus.FAR, DistanceStatus.fromString("far"));
            assertEquals(DistanceStatus.FAR, DistanceStatus.fromString("remote"));
            assertEquals(DistanceStatus.UNKNOWN, DistanceStatus.fromString("UNKNOWN"));
            assertEquals(DistanceStatus.UNKNOWN, DistanceStatus.fromString("unknown"));
            assertNull(DistanceStatus.fromString("INVALID_STATUS"));
            assertNull(DistanceStatus.fromString(null));
        }
    }

    // =========================================================================
    // 2. Haversine & Geodesic Calculation Tests
    // =========================================================================

    @Nested
    @DisplayName("5.5.2: Distance Calculation Accuracy")
    class DistanceCalculationTests {

        @Test
        @DisplayName("Test 1 & 2: Known Haversine distance between Patna and Sitamarhi")
        void testKnownHaversineDistance() {
            // Patna: (25.6210, 85.1720), Sitamarhi: (26.5950, 85.5030)
            double distanceMeters = SettlementExposureService.haversineDistanceMeters(25.6210, 85.1720, 26.5950, 85.5030);
            double distanceKm = distanceMeters / 1000.0;

            // Great-circle distance between Patna & Sitamarhi center is approx ~113 km
            assertTrue(distanceKm > 105.0 && distanceKm < 120.0, "Expected ~113 km, got " + distanceKm);
            assertTrue(distanceMeters > 105000.0 && distanceMeters < 120000.0);
        }

        @Test
        @DisplayName("Test 3: Zero-distance case (point inside affected district)")
        void testZeroDistanceCase() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-SITAMARHI-INSIDE");
            site.setLatitude(26.5950);
            site.setLongitude(85.5030);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(eq(85.5030), eq(26.5950), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 0.0)));

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.NEAR, site.getDistanceStatus());
            assertEquals(0.0, site.getDistanceMeters());
            assertEquals(0.0, site.getDistanceKilometers());
            assertTrue(site.getDistanceReason().contains("inside the high-risk disaster area"));
            assertTrue(site.getDistanceReason().contains("Sitamarhi"));
        }
    }

    // =========================================================================
    // 3. Distance Threshold Classification & Boundary Tests
    // =========================================================================

    @Nested
    @DisplayName("5.5.3: Proximity Threshold Boundaries")
    class DistanceThresholdTests {

        @Test
        @DisplayName("Test 4: Near distance (3.2 km <= 5.0 km) -> NEAR")
        void testNearDistance() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NEAR-1");
            site.setLatitude(26.6500);
            site.setLongitude(85.5200);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 3200.0)));

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.NEAR, site.getDistanceStatus());
            assertEquals(3200.0, site.getDistanceMeters());
            assertEquals(3.2, site.getDistanceKilometers());
            assertTrue(site.getDistanceReason().contains("near"));
            assertTrue(site.getDistanceReason().contains("3.20 km"));
        }

        @Test
        @DisplayName("Test 7a: Exact 5.0 km boundary -> NEAR")
        void testExactNearThreshold() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-BOUND-5KM");
            site.setLatitude(26.6500);
            site.setLongitude(85.5200);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 5000.0)));

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.NEAR, site.getDistanceStatus());
            assertEquals(5000.0, site.getDistanceMeters());
            assertEquals(5.0, site.getDistanceKilometers());
        }

        @Test
        @DisplayName("Test 7b: 5.01 km -> MODERATE")
        void testModerateLowEdge() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-BOUND-501");
            site.setLatitude(26.6500);
            site.setLongitude(85.5200);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 5010.0)));

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.MODERATE, site.getDistanceStatus());
            assertEquals(5.01, site.getDistanceKilometers());
            assertTrue(site.getDistanceReason().contains("moderate"));
        }

        @Test
        @DisplayName("Test 5: Moderate distance (12.5 km) -> MODERATE")
        void testModerateDistance() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-MOD-12");
            site.setLatitude(26.4000);
            site.setLongitude(85.3000);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 12500.0)));

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.MODERATE, site.getDistanceStatus());
            assertEquals(12500.0, site.getDistanceMeters());
            assertEquals(12.5, site.getDistanceKilometers());
        }

        @Test
        @DisplayName("Test 7c: 19.99 km -> MODERATE")
        void testModerateHighEdge() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-BOUND-1999");
            site.setLatitude(26.4000);
            site.setLongitude(85.3000);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 19990.0)));

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.MODERATE, site.getDistanceStatus());
            assertEquals(19.99, site.getDistanceKilometers());
        }

        @Test
        @DisplayName("Test 7d: Exact 20.0 km boundary -> FAR")
        void testExactFarThreshold() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-BOUND-20KM");
            site.setLatitude(26.4000);
            site.setLongitude(85.3000);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 20000.0)));

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
            assertEquals(20000.0, site.getDistanceMeters());
            assertEquals(20.0, site.getDistanceKilometers());
        }

        @Test
        @DisplayName("Test 6: Far distance (75.8 km >= 20.0 km) -> FAR")
        void testFarDistance() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-FAR-PATNA");
            site.setLatitude(25.6210);
            site.setLongitude(85.1720);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 75785.1)));

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
            assertEquals(75785.1, site.getDistanceMeters());
            assertEquals(75.79, site.getDistanceKilometers());
            assertTrue(site.getDistanceReason().contains("far"));
            assertTrue(site.getDistanceReason().contains("75.79 km"));
        }

        @Test
        @DisplayName("Test 8: Missing or invalid coordinates -> UNKNOWN")
        void testMissingCoordinates() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-BAD-COORD");
            site.setLatitude(null);
            site.setLongitude(null);

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.UNKNOWN, site.getDistanceStatus());
            assertNull(site.getDistanceMeters());
            assertNull(site.getDistanceKilometers());
            assertTrue(site.getDistanceReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 9: Missing risk/affected-area geometry -> UNKNOWN")
        void testNoActiveRiskAreas() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-RISK");
            site.setLatitude(25.6210);
            site.setLongitude(85.1720);

            when(redZoneService.getRedZonesOnly()).thenReturn(Collections.emptyList());

            candidateSafeSiteService.evaluateDistance(site);

            assertEquals(DistanceStatus.UNKNOWN, site.getDistanceStatus());
            assertNull(site.getDistanceMeters());
            assertNull(site.getDistanceKilometers());
            assertTrue(site.getDistanceReason().contains("No active high-risk or red-zone disaster areas"));
        }

        @Test
        @DisplayName("Independence across Hazard Safety, Terrain, and Distance")
        void testMultiDimensionalIndependence() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-INDEPENDENT");
            site.setLatitude(25.6210);
            site.setLongitude(85.1720);
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 75785.1)));

            candidateSafeSiteService.evaluateDistance(site);

            // Distance is FAR while Hazard Safety is SAFE and Terrain is FAVORABLE
            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertEquals(DistanceStatus.FAR, site.getDistanceStatus());
        }
    }

    // =========================================================================
    // 4. CandidateSafeSiteService Filtering & GeoJSON Tests
    // =========================================================================

    @Nested
    @DisplayName("5.5.4: Service Distance Filtering & GeoJSON Enrichment")
    class ServiceDistanceFilteringTests {

        private List<InfrastructureAssetDto> createTestFacilities() {
            List<InfrastructureAssetDto> list = new ArrayList<>();

            InfrastructureAssetDto fac1 = new InfrastructureAssetDto();
            fac1.setAssetId("FAC-1");
            fac1.setAssetName("Close School");
            fac1.setCategory(InfrastructureCategory.EDUCATION);
            fac1.setDistrictName("Sitamarhi");
            fac1.setLongitude(85.5030);
            fac1.setLatitude(26.5950);
            list.add(fac1);

            InfrastructureAssetDto fac2 = new InfrastructureAssetDto();
            fac2.setAssetId("FAC-2");
            fac2.setAssetName("Distant Hospital");
            fac2.setCategory(InfrastructureCategory.HEALTHCARE);
            fac2.setDistrictName("Patna");
            fac2.setLongitude(85.1720);
            fac2.setLatitude(25.6210);
            list.add(fac2);

            return list;
        }

        @Test
        @DisplayName("Test 10: getCandidateSites filters by distanceStatus=NEAR")
        void testFilterDistanceNear() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());
            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(eq(85.5030), eq(26.5950), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 0.0)));
            when(districtBoundaryRepository.findNearestDistrictDistance(eq(85.1720), eq(25.6210), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 75785.1)));

            List<CandidateSafeSiteDto> nearSites = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, "NEAR");

            assertEquals(1, nearSites.size());
            assertEquals("FAC-1", nearSites.get(0).getSiteId());
            assertEquals(DistanceStatus.NEAR, nearSites.get(0).getDistanceStatus());
        }

        @Test
        @DisplayName("Test 11: Invalid distanceStatus filter throws InvalidHazardParameterException (HTTP 400)")
        void testInvalidDistanceFilterThrows() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());

            assertThrows(InvalidHazardParameterException.class, () ->
                    candidateSafeSiteService.getCandidateSites(null, null, false, null, null, "CLOSE_ENOUGH")
            );
        }

        @Test
        @DisplayName("GeoJSON Feature properties contain distanceMeters, distanceKilometers, distanceStatus, and distanceReason")
        void testGeoJsonDistanceEnrichment() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());
            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(createRedZone("Sitamarhi", ZoneLevel.CRITICAL)));
            when(districtBoundaryRepository.findNearestDistrictDistance(eq(85.5030), eq(26.5950), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 1500.0)));
            when(districtBoundaryRepository.findNearestDistrictDistance(eq(85.1720), eq(25.6210), anyList()))
                    .thenReturn(Optional.of(createMockProjection("Sitamarhi", 75785.1)));

            GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                    null, null, false, null, null, null);

            assertNotNull(geojson);
            assertEquals(2, geojson.getCount());

            var props = geojson.getFeatures().get(0).getProperties();
            assertEquals(1500.0, props.get("distanceMeters"));
            assertEquals(1.5, props.get("distanceKilometers"));
            assertEquals("NEAR", props.get("distanceStatus"));
            assertNotNull(props.get("distanceReason"));
        }
    }
}
