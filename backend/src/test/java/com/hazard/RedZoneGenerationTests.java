package com.hazard;

import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.risk.ZoneLevel;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.GeoJsonGeometryDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.RedZoneDto;
import com.hazard.dto.risk.RiskContributorDto;
import com.hazard.service.risk.RedZoneService;
import com.hazard.service.risk.RiskCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Stage 5.1 — Unit tests for Dynamic Red-Zone Generation.
 * Tests ZoneLevel classification, RedZoneDto mapping, RedZoneService logic,
 * GeoJSON enrichment, and red-zone identification.
 */
@ExtendWith(MockitoExtension.class)
class RedZoneGenerationTests {

    @Mock
    private RiskCalculationService riskCalculationService;

    private RedZoneService redZoneService;

    @BeforeEach
    void setUp() {
        redZoneService = new RedZoneService(riskCalculationService);
    }

    // =========================================================================
    // 1. ZoneLevel Enum Classification Tests
    // =========================================================================

    @Nested
    @DisplayName("5.1.1: ZoneLevel Classification from RiskTier")
    class ZoneLevelClassificationTests {

        @Test
        @DisplayName("LOW RiskTier maps to LOW ZoneLevel")
        void testLowTierMapsToLowZone() {
            assertEquals(ZoneLevel.LOW, ZoneLevel.fromRiskTier(RiskTier.LOW));
            assertFalse(ZoneLevel.LOW.isRedZone());
        }

        @Test
        @DisplayName("MODERATE RiskTier maps to MODERATE ZoneLevel")
        void testModerateTierMapsToModerateZone() {
            assertEquals(ZoneLevel.MODERATE, ZoneLevel.fromRiskTier(RiskTier.MODERATE));
            assertFalse(ZoneLevel.MODERATE.isRedZone());
        }

        @Test
        @DisplayName("HIGH RiskTier maps to HIGH ZoneLevel")
        void testHighTierMapsToHighZone() {
            assertEquals(ZoneLevel.HIGH, ZoneLevel.fromRiskTier(RiskTier.HIGH));
            assertFalse(ZoneLevel.HIGH.isRedZone());
        }

        @Test
        @DisplayName("VERY_HIGH RiskTier maps to CRITICAL ZoneLevel (Red Zone)")
        void testVeryHighTierMapsToCriticalZone() {
            assertEquals(ZoneLevel.CRITICAL, ZoneLevel.fromRiskTier(RiskTier.VERY_HIGH));
            assertTrue(ZoneLevel.CRITICAL.isRedZone());
        }

        @Test
        @DisplayName("CRITICAL RiskTier maps to CRITICAL ZoneLevel (Red Zone)")
        void testCriticalTierMapsToCriticalZone() {
            assertEquals(ZoneLevel.CRITICAL, ZoneLevel.fromRiskTier(RiskTier.CRITICAL));
            assertTrue(ZoneLevel.CRITICAL.isRedZone());
        }

        @Test
        @DisplayName("Null RiskTier maps to UNKNOWN ZoneLevel (not LOW)")
        void testNullTierMapsToUnknown() {
            assertEquals(ZoneLevel.UNKNOWN, ZoneLevel.fromRiskTier(null));
            assertFalse(ZoneLevel.UNKNOWN.isRedZone());
        }

        @Test
        @DisplayName("UNKNOWN zone level is never a Red Zone")
        void testUnknownIsNeverRedZone() {
            assertFalse(ZoneLevel.UNKNOWN.isRedZone());
            assertNotEquals(ZoneLevel.CRITICAL, ZoneLevel.UNKNOWN);
        }

        @Test
        @DisplayName("All 5 RiskTier values are mapped (exhaustive coverage)")
        void testAllRiskTiersMapped() {
            for (RiskTier tier : RiskTier.values()) {
                ZoneLevel zone = ZoneLevel.fromRiskTier(tier);
                assertNotNull(zone, "ZoneLevel should not be null for RiskTier: " + tier);
            }
        }
    }

    // =========================================================================
    // 2. RedZoneDto Factory Method Tests
    // =========================================================================

    @Nested
    @DisplayName("5.1.2: RedZoneDto Mapping from DistrictRiskScoreDto")
    class RedZoneDtoTests {

        @Test
        @DisplayName("Critical-tier district is correctly identified as red zone")
        void testCriticalDistrictIsRedZone() {
            DistrictRiskScoreDto risk = createMockRiskScore("Sitamarhi", 0.85, RiskTier.CRITICAL);
            RedZoneDto dto = RedZoneDto.fromDistrictRiskScore(risk);

            assertNotNull(dto);
            assertEquals("Sitamarhi", dto.getDistrictName());
            assertEquals(0.85, dto.getRiskScore());
            assertEquals(85.0, dto.getRiskScore100());
            assertEquals(RiskTier.CRITICAL, dto.getRiskTier());
            assertEquals(ZoneLevel.CRITICAL, dto.getZoneLevel());
            assertTrue(dto.isRedZone());
        }

        @Test
        @DisplayName("Very-high-tier district is correctly identified as red zone")
        void testVeryHighDistrictIsRedZone() {
            DistrictRiskScoreDto risk = createMockRiskScore("Supaul", 0.72, RiskTier.VERY_HIGH);
            RedZoneDto dto = RedZoneDto.fromDistrictRiskScore(risk);

            assertNotNull(dto);
            assertEquals(ZoneLevel.CRITICAL, dto.getZoneLevel());
            assertTrue(dto.isRedZone());
        }

        @Test
        @DisplayName("High-tier district is NOT a red zone")
        void testHighDistrictIsNotRedZone() {
            DistrictRiskScoreDto risk = createMockRiskScore("Patna", 0.55, RiskTier.HIGH);
            RedZoneDto dto = RedZoneDto.fromDistrictRiskScore(risk);

            assertNotNull(dto);
            assertEquals(ZoneLevel.HIGH, dto.getZoneLevel());
            assertFalse(dto.isRedZone());
        }

        @Test
        @DisplayName("Low-tier district is NOT a red zone")
        void testLowDistrictIsNotRedZone() {
            DistrictRiskScoreDto risk = createMockRiskScore("Gaya", 0.15, RiskTier.LOW);
            RedZoneDto dto = RedZoneDto.fromDistrictRiskScore(risk);

            assertNotNull(dto);
            assertEquals(ZoneLevel.LOW, dto.getZoneLevel());
            assertFalse(dto.isRedZone());
        }

        @Test
        @DisplayName("Null DistrictRiskScoreDto returns null RedZoneDto")
        void testNullInputReturnsNull() {
            assertNull(RedZoneDto.fromDistrictRiskScore(null));
        }

        @Test
        @DisplayName("District with null RiskTier maps to UNKNOWN zone, not LOW")
        void testNullRiskTierInDtoMapsToUnknown() {
            DistrictRiskScoreDto risk = createMockRiskScore("NoTierDistrict", 0.0, null);
            risk.setRiskTier(null);
            RedZoneDto dto = RedZoneDto.fromDistrictRiskScore(risk);

            assertNotNull(dto);
            assertEquals(ZoneLevel.UNKNOWN, dto.getZoneLevel());
            assertFalse(dto.isRedZone());
        }

        @Test
        @DisplayName("Top contributors are preserved from source DTO")
        void testTopContributorsPreserved() {
            DistrictRiskScoreDto risk = createMockRiskScore("Muzaffarpur", 0.65, RiskTier.VERY_HIGH);
            RiskContributorDto contributor = new RiskContributorDto(
                    "Hazard Severity", "HAZARD", 0.90, 0.35, 0.315, "High hazard intensity");
            risk.setTopContributors(List.of(contributor));

            RedZoneDto dto = RedZoneDto.fromDistrictRiskScore(risk);

            assertNotNull(dto.getTopContributors());
            assertEquals(1, dto.getTopContributors().size());
            assertEquals("Hazard Severity", dto.getTopContributors().get(0).getName());
        }
    }

    // =========================================================================
    // 3. RedZoneService Tests
    // =========================================================================

    @Nested
    @DisplayName("5.1.3: RedZoneService Dynamic Zone Generation")
    class RedZoneServiceTests {

        @Test
        @DisplayName("getAllRiskZones returns all districts classified and sorted by risk descending")
        void testGetAllRiskZonesSortedDescending() {
            List<DistrictRiskScoreDto> mockScores = List.of(
                    createMockRiskScore("LowDistrict", 0.15, RiskTier.LOW),
                    createMockRiskScore("CriticalDistrict", 0.90, RiskTier.CRITICAL),
                    createMockRiskScore("HighDistrict", 0.55, RiskTier.HIGH)
            );
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(mockScores);

            List<RedZoneDto> zones = redZoneService.getAllRiskZones();

            assertEquals(3, zones.size());
            // Sorted descending by risk score
            assertEquals("CriticalDistrict", zones.get(0).getDistrictName());
            assertEquals("HighDistrict", zones.get(1).getDistrictName());
            assertEquals("LowDistrict", zones.get(2).getDistrictName());
        }

        @Test
        @DisplayName("getRedZonesOnly returns only VERY_HIGH and CRITICAL districts")
        void testGetRedZonesOnly() {
            List<DistrictRiskScoreDto> mockScores = List.of(
                    createMockRiskScore("Low1", 0.10, RiskTier.LOW),
                    createMockRiskScore("Moderate1", 0.30, RiskTier.MODERATE),
                    createMockRiskScore("High1", 0.50, RiskTier.HIGH),
                    createMockRiskScore("VeryHigh1", 0.70, RiskTier.VERY_HIGH),
                    createMockRiskScore("Critical1", 0.90, RiskTier.CRITICAL)
            );
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(mockScores);

            List<RedZoneDto> redZones = redZoneService.getRedZonesOnly();

            assertEquals(2, redZones.size());
            assertTrue(redZones.stream().allMatch(RedZoneDto::isRedZone));
            assertEquals("Critical1", redZones.get(0).getDistrictName());
            assertEquals("VeryHigh1", redZones.get(1).getDistrictName());
        }

        @Test
        @DisplayName("getRedZonesOnly returns empty list when no red zones exist")
        void testGetRedZonesOnlyReturnsEmptyWhenNone() {
            List<DistrictRiskScoreDto> mockScores = List.of(
                    createMockRiskScore("Safe1", 0.10, RiskTier.LOW),
                    createMockRiskScore("Safe2", 0.30, RiskTier.MODERATE)
            );
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(mockScores);

            List<RedZoneDto> redZones = redZoneService.getRedZonesOnly();

            assertTrue(redZones.isEmpty());
        }

        @Test
        @DisplayName("getZonesByMinimumLevel(HIGH) filters correctly")
        void testGetZonesByMinimumLevelHigh() {
            List<DistrictRiskScoreDto> mockScores = List.of(
                    createMockRiskScore("Low1", 0.10, RiskTier.LOW),
                    createMockRiskScore("High1", 0.50, RiskTier.HIGH),
                    createMockRiskScore("Critical1", 0.90, RiskTier.CRITICAL)
            );
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(mockScores);

            List<RedZoneDto> filtered = redZoneService.getZonesByMinimumLevel(ZoneLevel.HIGH);

            assertEquals(2, filtered.size());
            assertTrue(filtered.stream().allMatch(z ->
                    z.getZoneLevel() == ZoneLevel.HIGH || z.getZoneLevel() == ZoneLevel.CRITICAL));
        }

        @Test
        @DisplayName("getZonesByMinimumLevel(null) returns all zones")
        void testGetZonesByMinimumLevelNullReturnsAll() {
            List<DistrictRiskScoreDto> mockScores = List.of(
                    createMockRiskScore("D1", 0.10, RiskTier.LOW),
                    createMockRiskScore("D2", 0.90, RiskTier.CRITICAL)
            );
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(mockScores);

            List<RedZoneDto> zones = redZoneService.getZonesByMinimumLevel(null);

            assertEquals(2, zones.size());
        }

        @Test
        @DisplayName("Zone level summary counts districts per zone correctly")
        void testZoneLevelSummary() {
            List<DistrictRiskScoreDto> mockScores = List.of(
                    createMockRiskScore("L1", 0.10, RiskTier.LOW),
                    createMockRiskScore("L2", 0.15, RiskTier.LOW),
                    createMockRiskScore("M1", 0.30, RiskTier.MODERATE),
                    createMockRiskScore("H1", 0.50, RiskTier.HIGH),
                    createMockRiskScore("C1", 0.85, RiskTier.CRITICAL),
                    createMockRiskScore("VH1", 0.70, RiskTier.VERY_HIGH)
            );
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(mockScores);

            Map<ZoneLevel, Long> summary = redZoneService.getZoneLevelSummary();

            assertEquals(2L, summary.getOrDefault(ZoneLevel.LOW, 0L));
            assertEquals(1L, summary.getOrDefault(ZoneLevel.MODERATE, 0L));
            assertEquals(1L, summary.getOrDefault(ZoneLevel.HIGH, 0L));
            // VERY_HIGH + CRITICAL both map to CRITICAL zone
            assertEquals(2L, summary.getOrDefault(ZoneLevel.CRITICAL, 0L));
        }
    }

    // =========================================================================
    // 4. GeoJSON Output Validity Tests
    // =========================================================================

    @Nested
    @DisplayName("5.1.4: GeoJSON Red-Zone Output Validity")
    class GeoJsonOutputTests {

        @Test
        @DisplayName("Enriched GeoJSON contains zone classification properties")
        void testGeoJsonContainsZoneProperties() {
            // Create a mock base GeoJSON from the existing Stage 4 service
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("districtName", "Sitamarhi");
            props.put("riskScore", 0.85);
            props.put("riskTier", "CRITICAL");
            props.put("colorHex", "#9C27B0");

            GeoJsonFeatureDto feature = new GeoJsonFeatureDto(
                    "RISK-DISTRICT-1",
                    GeoJsonGeometryDto.point(85.67, 26.59),
                    props
            );
            GeoJsonFeatureCollectionDto baseGeoJson = new GeoJsonFeatureCollectionDto(List.of(feature));

            when(riskCalculationService.generateRiskGeoJson()).thenReturn(baseGeoJson);

            GeoJsonFeatureCollectionDto result = redZoneService.generateRedZoneGeoJson();

            assertNotNull(result);
            assertEquals("FeatureCollection", result.getType());
            assertEquals(1, result.getFeatures().size());

            Map<String, Object> enrichedProps = result.getFeatures().get(0).getProperties();
            // Original Stage 4 properties preserved
            assertEquals("Sitamarhi", enrichedProps.get("districtName"));
            assertEquals(0.85, enrichedProps.get("riskScore"));

            // New Stage 5.1 zone properties added
            assertEquals("CRITICAL", enrichedProps.get("zoneLevel"));
            assertEquals(true, enrichedProps.get("isRedZone"));
            assertNotNull(enrichedProps.get("zoneColorHex"));
            assertNotNull(enrichedProps.get("zoneLevelDisplay"));
            assertNotNull(enrichedProps.get("zoneDescription"));
            assertEquals("DISTRICT_RED_ZONE", enrichedProps.get("layerId"));
        }

        @Test
        @DisplayName("GeoJSON feature with LOW tier is not marked as red zone")
        void testGeoJsonLowTierNotRedZone() {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("districtName", "Gaya");
            props.put("riskScore", 0.15);
            props.put("riskTier", "LOW");

            GeoJsonFeatureDto feature = new GeoJsonFeatureDto(
                    "RISK-DISTRICT-2",
                    GeoJsonGeometryDto.point(84.99, 24.75),
                    props
            );
            GeoJsonFeatureCollectionDto baseGeoJson = new GeoJsonFeatureCollectionDto(List.of(feature));
            when(riskCalculationService.generateRiskGeoJson()).thenReturn(baseGeoJson);

            GeoJsonFeatureCollectionDto result = redZoneService.generateRedZoneGeoJson();

            Map<String, Object> enrichedProps = result.getFeatures().get(0).getProperties();
            assertEquals("LOW", enrichedProps.get("zoneLevel"));
            assertEquals(false, enrichedProps.get("isRedZone"));
        }

        @Test
        @DisplayName("Red-zone-only GeoJSON filters non-red-zone features")
        void testRedZoneOnlyGeoJsonFiltering() {
            GeoJsonFeatureDto lowFeature = new GeoJsonFeatureDto(
                    "RISK-DISTRICT-1",
                    GeoJsonGeometryDto.point(84.99, 24.75),
                    new LinkedHashMap<>(Map.of("districtName", "SafeDistrict", "riskTier", "LOW"))
            );
            GeoJsonFeatureDto criticalFeature = new GeoJsonFeatureDto(
                    "RISK-DISTRICT-2",
                    GeoJsonGeometryDto.point(85.67, 26.59),
                    new LinkedHashMap<>(Map.of("districtName", "DangerDistrict", "riskTier", "CRITICAL"))
            );
            GeoJsonFeatureCollectionDto baseGeoJson = new GeoJsonFeatureCollectionDto(
                    List.of(lowFeature, criticalFeature));

            when(riskCalculationService.generateRiskGeoJson()).thenReturn(baseGeoJson);

            GeoJsonFeatureCollectionDto result = redZoneService.generateRedZoneOnlyGeoJson();

            assertEquals(1, result.getFeatures().size());
            assertEquals("DangerDistrict", result.getFeatures().get(0).getProperties().get("districtName"));
        }

        @Test
        @DisplayName("GeoJSON geometry is preserved (not modified by zone enrichment)")
        void testGeometryPreserved() {
            GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(85.67, 26.59);
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("riskTier", "HIGH");

            GeoJsonFeatureDto feature = new GeoJsonFeatureDto("F-1", geom, props);
            GeoJsonFeatureCollectionDto baseGeoJson = new GeoJsonFeatureCollectionDto(List.of(feature));
            when(riskCalculationService.generateRiskGeoJson()).thenReturn(baseGeoJson);

            GeoJsonFeatureCollectionDto result = redZoneService.generateRedZoneGeoJson();

            assertNotNull(result.getFeatures().get(0).getGeometry());
            assertEquals("Point", result.getFeatures().get(0).getGeometry().getType());
        }

        @Test
        @DisplayName("GeoJSON feature with missing riskTier maps to UNKNOWN, not LOW")
        void testGeoJsonMissingRiskTierMapsToUnknown() {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("districtName", "NoTierDistrict");
            // No riskTier property at all

            GeoJsonFeatureDto feature = new GeoJsonFeatureDto(
                    "RISK-DISTRICT-X",
                    GeoJsonGeometryDto.point(85.0, 25.0),
                    props
            );
            GeoJsonFeatureCollectionDto baseGeoJson = new GeoJsonFeatureCollectionDto(List.of(feature));
            when(riskCalculationService.generateRiskGeoJson()).thenReturn(baseGeoJson);

            GeoJsonFeatureCollectionDto result = redZoneService.generateRedZoneGeoJson();

            Map<String, Object> enrichedProps = result.getFeatures().get(0).getProperties();
            assertEquals("UNKNOWN", enrichedProps.get("zoneLevel"));
            assertEquals(false, enrichedProps.get("isRedZone"));
        }

        @Test
        @DisplayName("GeoJSON feature with invalid riskTier string maps to UNKNOWN, not LOW")
        void testGeoJsonInvalidRiskTierMapsToUnknown() {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("districtName", "BadTierDistrict");
            props.put("riskTier", "BANANA");

            GeoJsonFeatureDto feature = new GeoJsonFeatureDto(
                    "RISK-DISTRICT-Y",
                    GeoJsonGeometryDto.point(85.0, 25.0),
                    props
            );
            GeoJsonFeatureCollectionDto baseGeoJson = new GeoJsonFeatureCollectionDto(List.of(feature));
            when(riskCalculationService.generateRiskGeoJson()).thenReturn(baseGeoJson);

            GeoJsonFeatureCollectionDto result = redZoneService.generateRedZoneGeoJson();

            Map<String, Object> enrichedProps = result.getFeatures().get(0).getProperties();
            assertEquals("UNKNOWN", enrichedProps.get("zoneLevel"));
            assertEquals(false, enrichedProps.get("isRedZone"));
        }
    }

    // =========================================================================
    // 5. Dynamic Behavior Tests
    // =========================================================================

    @Nested
    @DisplayName("5.1.5: Dynamic Red-Zone Behavior")
    class DynamicBehaviorTests {

        @Test
        @DisplayName("Zone classification changes when underlying risk data changes")
        void testZoneChangesWithRiskData() {
            // First call: district is HIGH risk
            List<DistrictRiskScoreDto> highRisk = List.of(
                    createMockRiskScore("TestDistrict", 0.55, RiskTier.HIGH)
            );
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(highRisk);

            List<RedZoneDto> firstResult = redZoneService.getAllRiskZones();
            assertEquals(ZoneLevel.HIGH, firstResult.get(0).getZoneLevel());
            assertFalse(firstResult.get(0).isRedZone());

            // Second call: same district now CRITICAL (risk data changed)
            List<DistrictRiskScoreDto> criticalRisk = List.of(
                    createMockRiskScore("TestDistrict", 0.90, RiskTier.CRITICAL)
            );
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(criticalRisk);

            List<RedZoneDto> secondResult = redZoneService.getAllRiskZones();
            assertEquals(ZoneLevel.CRITICAL, secondResult.get(0).getZoneLevel());
            assertTrue(secondResult.get(0).isRedZone());
        }

        @Test
        @DisplayName("Red zones are always dynamically derived, never cached or hardcoded")
        void testRedZonesNotCached() {
            List<DistrictRiskScoreDto> scores1 = List.of(
                    createMockRiskScore("D1", 0.85, RiskTier.CRITICAL)
            );
            List<DistrictRiskScoreDto> scores2 = List.of(
                    createMockRiskScore("D1", 0.30, RiskTier.MODERATE)
            );

            when(riskCalculationService.getAllDistrictsRiskScores())
                    .thenReturn(scores1)
                    .thenReturn(scores2);

            // First call — red zone
            assertEquals(1, redZoneService.getRedZonesOnly().size());
            // Second call — no longer red zone (risk dropped)
            assertEquals(0, redZoneService.getRedZonesOnly().size());

            // Verify underlying service was called twice (no caching)
            verify(riskCalculationService, times(2)).getAllDistrictsRiskScores();
        }
    }

    // =========================================================================
    // 6. Defensive Behavior Tests (Fixes)
    // =========================================================================

    @Nested
    @DisplayName("5.1.6: Defensive Behavior — UNKNOWN zones and invalid parameters")
    class DefensiveBehaviorTests {

        @Test
        @DisplayName("Districts with null riskTier are not counted as red zones")
        void testNullRiskTierDistrictNotRedZone() {
            DistrictRiskScoreDto nullTier = createMockRiskScore("MissingData", 0.0, null);
            nullTier.setRiskTier(null);
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(List.of(nullTier));

            List<RedZoneDto> redZones = redZoneService.getRedZonesOnly();
            assertTrue(redZones.isEmpty(), "District with null riskTier must not be a red zone");

            List<RedZoneDto> allZones = redZoneService.getAllRiskZones();
            assertEquals(1, allZones.size());
            assertEquals(ZoneLevel.UNKNOWN, allZones.get(0).getZoneLevel());
        }

        @Test
        @DisplayName("UNKNOWN zone is excluded by getZonesByMinimumLevel(LOW)")
        void testUnknownExcludedFromMinLevelLow() {
            DistrictRiskScoreDto nullTier = createMockRiskScore("MissingData", 0.0, null);
            nullTier.setRiskTier(null);
            DistrictRiskScoreDto lowTier = createMockRiskScore("SafeDistrict", 0.10, RiskTier.LOW);
            when(riskCalculationService.getAllDistrictsRiskScores()).thenReturn(List.of(nullTier, lowTier));

            List<RedZoneDto> filtered = redZoneService.getZonesByMinimumLevel(ZoneLevel.LOW);

            // UNKNOWN.ordinal() < LOW.ordinal(), so UNKNOWN is excluded
            assertEquals(1, filtered.size());
            assertEquals("SafeDistrict", filtered.get(0).getDistrictName());
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private DistrictRiskScoreDto createMockRiskScore(String districtName, double riskScore, RiskTier tier) {
        DistrictRiskScoreDto dto = new DistrictRiskScoreDto();
        dto.setDistrictName(districtName);
        dto.setDistrictId(districtName.hashCode() & 0xFF);
        dto.setGid2("IND.7." + (districtName.hashCode() & 0xFF) + "_1");
        dto.setState("Bihar");
        dto.setRiskScore(riskScore);
        dto.setRiskScore100(Math.round(riskScore * 1000.0) / 10.0);
        dto.setRiskTier(tier);
        dto.setExplanation("Test risk explanation for " + districtName);
        return dto;
    }
}
