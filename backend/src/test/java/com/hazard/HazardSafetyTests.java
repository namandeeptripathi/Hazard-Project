package com.hazard;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.service.exposure.InfrastructureDataProvider;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Stage 5.3 — Unit and Integration Tests for Hazard Safety Evaluation.
 */
@ExtendWith(MockitoExtension.class)
class HazardSafetyTests {

    @Mock
    private DistrictBoundaryRepository districtBoundaryRepository;

    @Mock
    private RiskCalculationService riskCalculationService;

    @Mock
    private InfrastructureDataProvider dataProvider;

    @Mock
    private RedZoneService redZoneService;

    @Mock
    private TerrainService terrainService;

    private SafeSiteThresholds thresholds;
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        thresholds = new SafeSiteThresholds();
        candidateSafeSiteService = new CandidateSafeSiteService(
                dataProvider,
                redZoneService,
                districtBoundaryRepository,
                terrainService,
                riskCalculationService,
                thresholds
        );
    }

    private DistrictBoundary createBoundary(String name) {
        DistrictBoundary b = new DistrictBoundary();
        b.setName2(name);
        return b;
    }

    private DistrictRiskScoreDto createRiskScore(String name, double score, RiskTier tier) {
        DistrictRiskScoreDto dto = new DistrictRiskScoreDto();
        dto.setDistrictName(name);
        dto.setRiskScore(score);
        dto.setRiskScore100(Math.round(score * 1000.0) / 10.0);
        dto.setRiskTier(tier);
        return dto;
    }

    // =========================================================================
    // 1. HazardSafetyStatus Enum Tests
    // =========================================================================

    @Nested
    @DisplayName("5.3.1: HazardSafetyStatus Enum & Parsing")
    class HazardSafetyStatusEnumTests {

        @Test
        @DisplayName("HazardSafetyStatus methods behave correctly")
        void testEnumBooleans() {
            assertTrue(HazardSafetyStatus.SAFE.isSafe());
            assertFalse(HazardSafetyStatus.SAFE.isAtRisk());
            assertFalse(HazardSafetyStatus.SAFE.isUnknown());

            assertTrue(HazardSafetyStatus.AT_RISK.isAtRisk());
            assertFalse(HazardSafetyStatus.AT_RISK.isSafe());
            assertFalse(HazardSafetyStatus.AT_RISK.isUnknown());

            assertTrue(HazardSafetyStatus.UNKNOWN.isUnknown());
            assertFalse(HazardSafetyStatus.UNKNOWN.isSafe());
            assertFalse(HazardSafetyStatus.UNKNOWN.isAtRisk());
        }

        @Test
        @DisplayName("fromString parses case-insensitively with hyphens and spaces")
        void testFromString() {
            assertEquals(HazardSafetyStatus.SAFE, HazardSafetyStatus.fromString("SAFE"));
            assertEquals(HazardSafetyStatus.SAFE, HazardSafetyStatus.fromString("safe"));
            assertEquals(HazardSafetyStatus.AT_RISK, HazardSafetyStatus.fromString("AT_RISK"));
            assertEquals(HazardSafetyStatus.AT_RISK, HazardSafetyStatus.fromString("at-risk"));
            assertEquals(HazardSafetyStatus.AT_RISK, HazardSafetyStatus.fromString("at risk"));
            assertEquals(HazardSafetyStatus.UNKNOWN, HazardSafetyStatus.fromString("UNKNOWN"));
            assertEquals(HazardSafetyStatus.UNKNOWN, HazardSafetyStatus.fromString("unknown"));
            assertNull(HazardSafetyStatus.fromString("INVALID"));
            assertNull(HazardSafetyStatus.fromString(null));
        }
    }

    // =========================================================================
    // 2. Spatial Point-in-Polygon & Risk Slicing Tests
    // =========================================================================

    @Nested
    @DisplayName("5.3.2: Spatial Hazard Safety Risk Evaluation")
    class EvaluatorTests {

        @Test
        @DisplayName("Candidate site in CRITICAL risk tier (Red Zone) evaluates to AT_RISK")
        void testCriticalRedZoneSiteIsAtRisk() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-EMG-003");
            site.setSiteName("Sitamarhi Central Flood Shelter");
            site.setDistrict("Sitamarhi");
            site.setLongitude(85.5030);
            site.setLatitude(26.5950);

            when(districtBoundaryRepository.findDistrictContainingPoint(85.5030, 26.5950))
                    .thenReturn(Optional.of(createBoundary("Sitamarhi")));
            when(riskCalculationService.getDistrictRiskScore("Sitamarhi", null))
                    .thenReturn(createRiskScore("Sitamarhi", 0.85, RiskTier.CRITICAL));

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.AT_RISK, site.getHazardSafetyStatus());
            assertEquals("CRITICAL", site.getRiskZone());
            assertEquals(85.0, site.getRiskScore());
            assertNotNull(site.getHazardSafetyReason());
            assertTrue(site.getHazardSafetyReason().contains("Critical Red Zone"));
            assertTrue(site.getHazardSafetyReason().contains("Sitamarhi"));
        }

        @Test
        @DisplayName("Candidate site in VERY_HIGH risk tier (Red Zone) evaluates to AT_RISK")
        void testVeryHighRedZoneSiteIsAtRisk() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-SHELTER-001");
            site.setDistrict("Supaul");
            site.setLongitude(86.6000);
            site.setLatitude(26.1000);

            when(districtBoundaryRepository.findDistrictContainingPoint(86.6000, 26.1000))
                    .thenReturn(Optional.of(createBoundary("Supaul")));
            when(riskCalculationService.getDistrictRiskScore("Supaul", null))
                    .thenReturn(createRiskScore("Supaul", 0.72, RiskTier.VERY_HIGH));

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.AT_RISK, site.getHazardSafetyStatus());
            assertEquals("CRITICAL", site.getRiskZone());
            assertTrue(site.getHazardSafetyReason().contains("Critical Red Zone"));
        }

        @Test
        @DisplayName("Rule 2: Candidate site in HIGH risk tier evaluates to AT_RISK (even outside Red Zone)")
        void testHighRiskSiteIsAtRisk() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-EDU-001");
            site.setDistrict("Patna");
            site.setLongitude(85.1720);
            site.setLatitude(25.6210);

            when(districtBoundaryRepository.findDistrictContainingPoint(85.1720, 25.6210))
                    .thenReturn(Optional.of(createBoundary("Patna")));
            when(riskCalculationService.getDistrictRiskScore("Patna", null))
                    .thenReturn(createRiskScore("Patna", 0.55, RiskTier.HIGH));

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.AT_RISK, site.getHazardSafetyStatus());
            assertEquals("HIGH", site.getRiskZone());
            assertTrue(site.getHazardSafetyReason().contains("High Risk Zone"));
        }

        @Test
        @DisplayName("Candidate site in MODERATE risk tier evaluates to SAFE")
        void testModerateRiskSiteIsSafe() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-MED-008");
            site.setDistrict("Gaya");
            site.setLongitude(84.9750);
            site.setLatitude(24.7890);

            when(districtBoundaryRepository.findDistrictContainingPoint(84.9750, 24.7890))
                    .thenReturn(Optional.of(createBoundary("Gaya")));
            when(riskCalculationService.getDistrictRiskScore("Gaya", null))
                    .thenReturn(createRiskScore("Gaya", 0.32, RiskTier.MODERATE));

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals("MODERATE", site.getRiskZone());
            assertEquals(32.0, site.getRiskScore());
            assertTrue(site.getHazardSafetyReason().contains("Moderate Risk"));
        }

        @Test
        @DisplayName("Candidate site in LOW risk tier evaluates to SAFE")
        void testLowRiskSiteIsSafe() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-SAFE-001");
            site.setDistrict("SafeDistrict");
            site.setLongitude(85.0000);
            site.setLatitude(25.0000);

            when(districtBoundaryRepository.findDistrictContainingPoint(85.0000, 25.0000))
                    .thenReturn(Optional.of(createBoundary("SafeDistrict")));
            when(riskCalculationService.getDistrictRiskScore("SafeDistrict", null))
                    .thenReturn(createRiskScore("SafeDistrict", 0.12, RiskTier.LOW));

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.SAFE, site.getHazardSafetyStatus());
            assertEquals("LOW", site.getRiskZone());
            assertEquals(12.0, site.getRiskScore());
            assertTrue(site.getHazardSafetyReason().contains("Low Risk"));
        }

        @Test
        @DisplayName("Rule 1: Missing or null coordinates evaluates to UNKNOWN (never SAFE)")
        void testMissingCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-COORD");
            site.setLatitude(null);
            site.setLongitude(null);

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.UNKNOWN, site.getHazardSafetyStatus());
            assertEquals("UNKNOWN", site.getRiskZone());
            assertNull(site.getRiskScore());
            assertTrue(site.getHazardSafetyReason().contains("Missing or invalid geographic coordinates"));
        }

        @Test
        @DisplayName("Out of bounds coordinates evaluate to UNKNOWN (never SAFE)")
        void testOutOfBoundsCoordinatesEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-BAD-COORD");
            site.setLatitude(120.0); // Invalid latitude > 90
            site.setLongitude(85.0);

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.UNKNOWN, site.getHazardSafetyStatus());
            assertEquals("UNKNOWN", site.getRiskZone());
            assertNull(site.getRiskScore());
        }

        @Test
        @DisplayName("Rule 1: Coordinates outside mapped boundaries evaluate to UNKNOWN (never SAFE)")
        void testUnmappedBoundaryEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-UNMAPPED");
            site.setLatitude(20.0);
            site.setLongitude(75.0);
            site.setDistrict(null);

            when(districtBoundaryRepository.findDistrictContainingPoint(75.0, 20.0))
                    .thenReturn(Optional.empty());

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.UNKNOWN, site.getHazardSafetyStatus());
            assertEquals("UNKNOWN", site.getRiskZone());
            assertNull(site.getRiskScore());
            assertTrue(site.getHazardSafetyReason().contains("fall outside mapped administrative district boundaries"));
        }

        @Test
        @DisplayName("Rule 1: Missing spatial risk profile evaluates to UNKNOWN (never SAFE)")
        void testMissingRiskDataEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NO-RISK");
            site.setDistrict("UnknownArea");
            site.setLongitude(85.0);
            site.setLatitude(25.0);

            when(districtBoundaryRepository.findDistrictContainingPoint(85.0, 25.0))
                    .thenReturn(Optional.of(createBoundary("UnknownArea")));
            when(riskCalculationService.getDistrictRiskScore("UnknownArea", null))
                    .thenReturn(null);

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.UNKNOWN, site.getHazardSafetyStatus());
            assertEquals("UNKNOWN", site.getRiskZone());
            assertNull(site.getRiskScore());
            assertTrue(site.getHazardSafetyReason().contains("unavailable"));
        }

        @Test
        @DisplayName("Null riskTier in risk score evaluates to UNKNOWN (never SAFE)")
        void testNullRiskTierEvaluatesToUnknown() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-NULL-TIER");
            site.setDistrict("PartialDataArea");
            site.setLongitude(85.0);
            site.setLatitude(25.0);

            DistrictRiskScoreDto partial = createRiskScore("PartialDataArea", 0.0, null);
            partial.setRiskTier(null);

            when(districtBoundaryRepository.findDistrictContainingPoint(85.0, 25.0))
                    .thenReturn(Optional.of(createBoundary("PartialDataArea")));
            when(riskCalculationService.getDistrictRiskScore("PartialDataArea", null))
                    .thenReturn(partial);

            candidateSafeSiteService.evaluateHazardSafety(site);

            assertEquals(HazardSafetyStatus.UNKNOWN, site.getHazardSafetyStatus());
            assertNull(site.getRiskScore());
        }
    }

    // =========================================================================
    // 3. CandidateSafeSiteService Filtering by hazardSafety
    // =========================================================================

    @Nested
    @DisplayName("5.3.3: CandidateSafeSiteService Hazard Safety Filtering")
    class ServiceHazardSafetyFilteringTests {

        private List<InfrastructureAssetDto> createTestFacilities() {
            List<InfrastructureAssetDto> list = new ArrayList<>();

            InfrastructureAssetDto atRiskSite = new InfrastructureAssetDto();
            atRiskSite.setAssetId("FAC-EMG-003");
            atRiskSite.setAssetName("Sitamarhi Central Flood Shelter");
            atRiskSite.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            atRiskSite.setSubType("flood_relief_shelter");
            atRiskSite.setDistrictName("Sitamarhi");
            atRiskSite.setLongitude(85.5030);
            atRiskSite.setLatitude(26.5950);
            list.add(atRiskSite);

            InfrastructureAssetDto safeSite = new InfrastructureAssetDto();
            safeSite.setAssetId("FAC-MED-008");
            safeSite.setAssetName("Anugrah Narayan Magadh Medical College");
            safeSite.setCategory(InfrastructureCategory.HEALTHCARE);
            safeSite.setSubType("tertiary_hospital");
            safeSite.setDistrictName("Gaya");
            safeSite.setLongitude(84.9750);
            safeSite.setLatitude(24.7890);
            list.add(safeSite);

            return list;
        }

        @Test
        @DisplayName("getCandidateSites filters by hazardSafety=AT_RISK")
        void testFilterAtRisk() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());

            when(districtBoundaryRepository.findDistrictContainingPoint(85.5030, 26.5950))
                    .thenReturn(Optional.of(createBoundary("Sitamarhi")));
            when(riskCalculationService.getDistrictRiskScore("Sitamarhi", null))
                    .thenReturn(createRiskScore("Sitamarhi", 0.85, RiskTier.CRITICAL));

            when(districtBoundaryRepository.findDistrictContainingPoint(84.9750, 24.7890))
                    .thenReturn(Optional.of(createBoundary("Gaya")));
            when(riskCalculationService.getDistrictRiskScore("Gaya", null))
                    .thenReturn(createRiskScore("Gaya", 0.25, RiskTier.LOW));

            List<CandidateSafeSiteDto> atRisk = candidateSafeSiteService.getCandidateSites(null, null, false, "AT_RISK");

            assertEquals(1, atRisk.size());
            assertEquals("FAC-EMG-003", atRisk.get(0).getSiteId());
            assertEquals(HazardSafetyStatus.AT_RISK, atRisk.get(0).getHazardSafetyStatus());
        }

        @Test
        @DisplayName("getCandidateSites filters by hazardSafety=SAFE")
        void testFilterSafe() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());

            when(districtBoundaryRepository.findDistrictContainingPoint(85.5030, 26.5950))
                    .thenReturn(Optional.of(createBoundary("Sitamarhi")));
            when(riskCalculationService.getDistrictRiskScore("Sitamarhi", null))
                    .thenReturn(createRiskScore("Sitamarhi", 0.85, RiskTier.CRITICAL));

            when(districtBoundaryRepository.findDistrictContainingPoint(84.9750, 24.7890))
                    .thenReturn(Optional.of(createBoundary("Gaya")));
            when(riskCalculationService.getDistrictRiskScore("Gaya", null))
                    .thenReturn(createRiskScore("Gaya", 0.25, RiskTier.LOW));

            List<CandidateSafeSiteDto> safe = candidateSafeSiteService.getCandidateSites(null, null, false, "SAFE");

            assertEquals(1, safe.size());
            assertEquals("FAC-MED-008", safe.get(0).getSiteId());
            assertEquals(HazardSafetyStatus.SAFE, safe.get(0).getHazardSafetyStatus());
        }

        @Test
        @DisplayName("getCandidateSites throws InvalidHazardParameterException for bad hazardSafety value")
        void testInvalidHazardSafetyThrows() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());

            assertThrows(InvalidHazardParameterException.class, () ->
                    candidateSafeSiteService.getCandidateSites(null, null, false, "SUPER_SAFE")
            );
        }

        @Test
        @DisplayName("GeoJSON generation enriches features with hazardSafetyStatus, reason, riskZone, and riskScore")
        void testGeoJsonHazardSafetyEnrichment() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createTestFacilities());

            when(districtBoundaryRepository.findDistrictContainingPoint(85.5030, 26.5950))
                    .thenReturn(Optional.of(createBoundary("Sitamarhi")));
            when(riskCalculationService.getDistrictRiskScore("Sitamarhi", null))
                    .thenReturn(createRiskScore("Sitamarhi", 0.85, RiskTier.CRITICAL));

            when(districtBoundaryRepository.findDistrictContainingPoint(84.9750, 24.7890))
                    .thenReturn(Optional.of(createBoundary("Gaya")));
            when(riskCalculationService.getDistrictRiskScore("Gaya", null))
                    .thenReturn(createRiskScore("Gaya", 0.25, RiskTier.LOW));

            GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson(null, null, false, null);

            assertNotNull(geojson);
            assertEquals(2, geojson.getCount());

            var atRiskFeature = geojson.getFeatures().stream()
                    .filter(f -> "FAC-EMG-003".equals(f.getProperties().get("siteId")))
                    .findFirst().orElseThrow();
            var safeFeature = geojson.getFeatures().stream()
                    .filter(f -> "FAC-MED-008".equals(f.getProperties().get("siteId")))
                    .findFirst().orElseThrow();

            var firstProps = atRiskFeature.getProperties();
            assertEquals("AT_RISK", firstProps.get("hazardSafetyStatus"));
            assertNotNull(firstProps.get("hazardSafetyReason"));
            assertEquals("CRITICAL", firstProps.get("riskZone"));
            assertEquals(85.0, firstProps.get("riskScore"));

            var secondProps = safeFeature.getProperties();
            assertEquals("SAFE", secondProps.get("hazardSafetyStatus"));
            assertNotNull(secondProps.get("hazardSafetyReason"));
            assertEquals("LOW", secondProps.get("riskZone"));
            assertEquals(25.0, secondProps.get("riskScore"));
        }
    }
}
