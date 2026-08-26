package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.TerrainStatus;
import com.hazard.domain.terrain.DemTile;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.service.exposure.InfrastructureDataProvider;
import com.hazard.service.risk.RedZoneService;
import com.hazard.service.safesite.*;
import com.hazard.service.terrain.TerrainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Stage 5.4 — Unit and Integration Tests for Terrain / Slope Intelligence.
 */
@ExtendWith(MockitoExtension.class)
class TerrainIntelligenceTests {

    @Mock
    private TerrainService terrainService;

    @Mock
    private InfrastructureDataProvider dataProvider;

    @Mock
    private RedZoneService redZoneService;

    @Mock
    private HazardSafetyEvaluator hazardSafetyEvaluator;

    @Mock
    private DistanceEvaluator distanceEvaluator;

    @Mock
    private com.hazard.service.safesite.RoadAccessibilityEvaluator roadAccessibilityEvaluator;

    @Mock
    private com.hazard.service.safesite.HealthcareEvaluator healthcareEvaluator;

    @Mock
    private com.hazard.service.safesite.WaterEvaluator waterEvaluator;

    @Mock
    private com.hazard.service.safesite.InfrastructureEvaluator infrastructureEvaluator;

    @Mock
    private com.hazard.service.safesite.SuitabilityEvaluator suitabilityEvaluator;

    private TerrainEvaluationConfig config;
    private TerrainEvaluator terrainEvaluator;
    private SafeSiteRankingEvaluator safeSiteRankingEvaluator = new SafeSiteRankingEvaluator();
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        config = new TerrainEvaluationConfig(5.0, 15.0);
        terrainEvaluator = new TerrainEvaluator(terrainService, config);
        candidateSafeSiteService = new CandidateSafeSiteService(
                dataProvider, redZoneService, hazardSafetyEvaluator, terrainEvaluator, distanceEvaluator, roadAccessibilityEvaluator, healthcareEvaluator, waterEvaluator, infrastructureEvaluator, suitabilityEvaluator, safeSiteRankingEvaluator);
    }

    private DemTile createMockDemTile(String name, double minElev, double maxElev) {
        DemTile tile = new DemTile();
        tile.setTileName(name);
        tile.setMinElevationM(minElev);
        tile.setMaxElevationM(maxElev);
        tile.setResolutionMeters(30.87);
        return tile;
    }

    // =========================================================================
    // 1. TerrainStatus Enum Tests
    // =========================================================================

    @Nested
    @DisplayName("5.4.1: TerrainStatus Enum & Parsing")
    class TerrainStatusEnumTests {

        @Test
        @DisplayName("TerrainStatus boolean helper methods behave correctly")
        void testEnumBooleans() {
            assertTrue(TerrainStatus.FAVORABLE.isFavorable());
            assertFalse(TerrainStatus.FAVORABLE.isUnfavorable());
            assertFalse(TerrainStatus.FAVORABLE.isUnknown());

            assertTrue(TerrainStatus.UNFAVORABLE.isUnfavorable());
            assertFalse(TerrainStatus.UNFAVORABLE.isFavorable());
            assertFalse(TerrainStatus.UNFAVORABLE.isUnknown());

            assertTrue(TerrainStatus.UNKNOWN.isUnknown());
            assertFalse(TerrainStatus.UNKNOWN.isFavorable());
            assertFalse(TerrainStatus.UNKNOWN.isUnfavorable());
        }

        @Test
        @DisplayName("fromString parses case-insensitively with aliases")
        void testFromString() {
            assertEquals(TerrainStatus.FAVORABLE, TerrainStatus.fromString("FAVORABLE"));
            assertEquals(TerrainStatus.FAVORABLE, TerrainStatus.fromString("favorable"));
            assertEquals(TerrainStatus.UNFAVORABLE, TerrainStatus.fromString("UNFAVORABLE"));
            assertEquals(TerrainStatus.UNFAVORABLE, TerrainStatus.fromString("unfavorable"));
            assertEquals(TerrainStatus.UNKNOWN, TerrainStatus.fromString("UNKNOWN"));
            assertEquals(TerrainStatus.UNKNOWN, TerrainStatus.fromString("unknown"));
            assertNull(TerrainStatus.fromString("INVALID_STATUS"));
            assertNull(TerrainStatus.fromString(null));
        }
    }

    // =========================================================================
    // 2. TerrainEvaluator Slope & Elevation Evaluation Tests
    // =========================================================================

    @Nested
    @DisplayName("5.4.2: TerrainEvaluator Evidence-Based Assessment")
    class EvaluatorTests {

        @Test
        @DisplayName("Test 1 & 2: Gentle terrain slope (<= 5.0°) evaluates to FAVORABLE")
        void testFavorableSlope() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-EDU-001");
            site.setLatitude(25.6210);
            site.setLongitude(85.1720);
            site.setElevationMeters(58.4);
            site.setSlopeDegrees(2.1);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertEquals(58.4, site.getElevationMeters());
            assertEquals(2.1, site.getSlopeDegrees());
            assertNotNull(site.getTerrainReason());
            assertTrue(site.getTerrainReason().contains("favorable"));
            assertTrue(site.getTerrainReason().contains("2.1°"));
        }

        @Test
        @DisplayName("Test 3: Steep terrain slope (>= 15.0°) evaluates to UNFAVORABLE")
        void testUnfavorableSlope() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-STEEP-001");
            site.setLatitude(25.1000);
            site.setLongitude(85.3000);
            site.setElevationMeters(180.0);
            site.setSlopeDegrees(18.5);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.UNFAVORABLE, site.getTerrainStatus());
            assertEquals(18.5, site.getSlopeDegrees());
            assertNotNull(site.getTerrainReason());
            assertTrue(site.getTerrainReason().contains("unfavorable"));
            assertTrue(site.getTerrainReason().contains("18.5°"));
        }

        @Test
        @DisplayName("Threshold Boundary: Exactly 5.0° evaluates to FAVORABLE")
        void testExactFavorableBoundary() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-BOUND-50");
            site.setLatitude(25.5000);
            site.setLongitude(85.2000);
            site.setSlopeDegrees(5.0);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertTrue(site.getTerrainReason().contains("favorable"));
            assertTrue(site.getTerrainReason().contains("5.0°"));
        }

        @Test
        @DisplayName("Intermediate Range: 5.01° evaluates to UNKNOWN")
        void testIntermediateSlopeLowEdge() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-INTER-501");
            site.setLatitude(25.5000);
            site.setLongitude(85.2000);
            site.setSlopeDegrees(5.01);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.UNKNOWN, site.getTerrainStatus());
            assertTrue(site.getTerrainReason().contains("intermediate"));
            assertTrue(site.getTerrainReason().contains("5.01°"));
        }

        @Test
        @DisplayName("Intermediate Range: 10.0° evaluates to UNKNOWN")
        void testIntermediateSlopeMid() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-INTER-100");
            site.setLatitude(25.5000);
            site.setLongitude(85.2000);
            site.setSlopeDegrees(10.0);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.UNKNOWN, site.getTerrainStatus());
            assertTrue(site.getTerrainReason().contains("intermediate"));
            assertTrue(site.getTerrainReason().contains("10.00°") || site.getTerrainReason().contains("10.0"));
        }

        @Test
        @DisplayName("Intermediate Range: 14.99° evaluates to UNKNOWN")
        void testIntermediateSlopeHighEdge() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-INTER-1499");
            site.setLatitude(25.5000);
            site.setLongitude(85.2000);
            site.setSlopeDegrees(14.99);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.UNKNOWN, site.getTerrainStatus());
            assertTrue(site.getTerrainReason().contains("intermediate"));
            assertTrue(site.getTerrainReason().contains("14.99°"));
        }

        @Test
        @DisplayName("Threshold Boundary: Exactly 15.0° evaluates to UNFAVORABLE")
        void testExactUnfavorableBoundary() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-BOUND-150");
            site.setLatitude(25.5000);
            site.setLongitude(85.2000);
            site.setSlopeDegrees(15.0);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.UNFAVORABLE, site.getTerrainStatus());
            assertTrue(site.getTerrainReason().contains("unfavorable"));
            assertTrue(site.getTerrainReason().contains("15.0°"));
        }

        @Test
        @DisplayName("Test 5: Missing elevation but available slope -> slope evaluated, elevation remains null")
        void testMissingElevationWithAvailableSlope() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-ELEV");
            site.setLatitude(25.5000);
            site.setLongitude(85.2000);
            site.setElevationMeters(null);
            site.setSlopeDegrees(3.0);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.FAVORABLE, site.getTerrainStatus());
            assertNull(site.getElevationMeters());
            assertEquals(3.0, site.getSlopeDegrees());
        }

        @Test
        @DisplayName("Test 6: Elevation available but slope unavailable -> slope remains null, status is UNKNOWN")
        void testElevationAvailableWithoutSlope() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-ELEV-ONLY");
            site.setLatitude(25.5000);
            site.setLongitude(85.2000);
            site.setElevationMeters(52.0);
            site.setSlopeDegrees(null);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.UNKNOWN, site.getTerrainStatus());
            assertEquals(52.0, site.getElevationMeters());
            assertNull(site.getSlopeDegrees());
            assertTrue(site.getTerrainReason().contains("slope data is not available"));
        }

        @Test
        @DisplayName("Test 7: Missing or invalid coordinates -> UNKNOWN (never FAVORABLE)")
        void testInvalidCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-BAD-COORD");
            site.setLatitude(null);
            site.setLongitude(null);

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.UNKNOWN, site.getTerrainStatus());
            assertTrue(site.getTerrainReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Test 4: Default production state with DEM tile footprint -> UNKNOWN without fabricated numbers")
        void testDemTileFootprintCoveredLocation() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-EMG-003");
            site.setLatitude(26.5950);
            site.setLongitude(85.5030);

            when(terrainService.getDemTileForCoordinate(85.5030, 26.5950))
                    .thenReturn(Optional.of(createMockDemTile("copernicus_dsm_cog_10_n26_00_e085_00_dem_clean", 38.88, 361.85)));

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.UNKNOWN, site.getTerrainStatus());
            assertNull(site.getElevationMeters());
            assertNull(site.getSlopeDegrees());
            assertNotNull(site.getTerrainReason());
            assertTrue(site.getTerrainReason().contains("copernicus_dsm_cog_10_n26_00_e085_00_dem_clean"));
            assertTrue(site.getTerrainReason().contains("not currently ingested"));
        }

        @Test
        @DisplayName("Default production state without DEM tile footprint -> UNKNOWN")
        void testUnmappedTerrainLocation() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-UNMAPPED");
            site.setLatitude(24.0000);
            site.setLongitude(83.0000);

            when(terrainService.getDemTileForCoordinate(83.0000, 24.0000))
                    .thenReturn(Optional.empty());

            terrainEvaluator.evaluateTerrain(site);

            assertEquals(TerrainStatus.UNKNOWN, site.getTerrainStatus());
            assertNull(site.getElevationMeters());
            assertNull(site.getSlopeDegrees());
            assertTrue(site.getTerrainReason().contains("not currently available"));
        }

        @Test
        @DisplayName("Test 10: Orthogonality — Hazard Safety and Terrain are separate dimensions")
        void testOrthogonalityWithHazardSafety() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-TEST-ORTHO");
            site.setLatitude(25.5000);
            site.setLongitude(85.2000);
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setSlopeDegrees(22.0); // Steep terrain

            terrainEvaluator.evaluateTerrain(site);

            // Hazard safety remains SAFE while Terrain is UNFAVORABLE
            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals(TerrainStatus.UNFAVORABLE, site.getTerrainStatus());
        }
    }

    // =========================================================================
    // 3. CandidateSafeSiteService Filtering by terrainStatus
    // =========================================================================

    @Nested
    @DisplayName("5.4.3: CandidateSafeSiteService Terrain Status Filtering")
    class ServiceTerrainFilteringTests {

        private List<InfrastructureAssetDto> createTestFacilities() {
            List<InfrastructureAssetDto> list = new ArrayList<>();

            InfrastructureAssetDto fac1 = new InfrastructureAssetDto();
            fac1.setAssetId("FAC-1");
            fac1.setAssetName("Flat School");
            fac1.setCategory(InfrastructureCategory.EDUCATION);
            fac1.setDistrictName("Patna");
            fac1.setLongitude(85.1720);
            fac1.setLatitude(25.6210);
            list.add(fac1);

            InfrastructureAssetDto fac2 = new InfrastructureAssetDto();
            fac2.setAssetId("FAC-2");
            fac2.setAssetName("Hilly Hospital");
            fac2.setCategory(InfrastructureCategory.HEALTHCARE);
            fac2.setDistrictName("Gaya");
            fac2.setLongitude(84.9750);
            fac2.setLatitude(24.7890);
            list.add(fac2);

            return list;
        }

        @Test
        @DisplayName("Test 8: getCandidateSites filters by terrainStatus=UNKNOWN")
        void testFilterTerrainUnknown() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());
            when(terrainService.getDemTileForCoordinate(anyDouble(), anyDouble()))
                    .thenReturn(Optional.empty());

            List<CandidateSafeSiteDto> unknownSites = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, "UNKNOWN");

            assertEquals(2, unknownSites.size());
            assertEquals(TerrainStatus.UNKNOWN, unknownSites.get(0).getTerrainStatus());
        }

        @Test
        @DisplayName("Test 9: Invalid terrainStatus filter throws InvalidHazardParameterException (HTTP 400)")
        void testInvalidTerrainFilterThrows() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());

            assertThrows(InvalidHazardParameterException.class, () ->
                    candidateSafeSiteService.getCandidateSites(null, null, false, null, "FLAT_TERRAIN")
            );
        }

        @Test
        @DisplayName("Test 11: GeoJSON enrichment includes elevationMeters, slopeDegrees, terrainStatus, and terrainReason")
        void testGeoJsonTerrainEnrichment() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());
            when(terrainService.getDemTileForCoordinate(anyDouble(), anyDouble()))
                    .thenReturn(Optional.of(createMockDemTile("copernicus_dsm_cog_10_n25_00_e085_00_dem_clean", 32.17, 390.25)));

            GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                    null, null, false, null, null);

            assertNotNull(geojson);
            assertEquals(2, geojson.getCount());

            var props = geojson.getFeatures().get(0).getProperties();
            assertTrue(props.containsKey("elevationMeters"));
            assertTrue(props.containsKey("slopeDegrees"));
            assertEquals("UNKNOWN", props.get("terrainStatus"));
            assertNotNull(props.get("terrainReason"));
            assertTrue(((String) props.get("terrainReason")).contains("copernicus_dsm_cog_10_n25_00_e085_00_dem_clean"));
        }
    }
}
