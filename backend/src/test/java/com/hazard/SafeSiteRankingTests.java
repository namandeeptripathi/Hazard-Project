package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.safesite.*;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Comprehensive Unit and Pipeline Integration Tests for Stage 5.11 — Candidate Safe-Site Ranking.
 */
@ExtendWith(MockitoExtension.class)
class SafeSiteRankingTests {

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
    private InfrastructureEvaluator infrastructureEvaluator;

    private SuitabilityEvaluationConfig suitabilityConfig;
    private SuitabilityEvaluator suitabilityEvaluator;
    private SafeSiteRankingEvaluator safeSiteRankingEvaluator;
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        suitabilityConfig = new SuitabilityEvaluationConfig();
        suitabilityEvaluator = new SuitabilityEvaluator(suitabilityConfig);
        safeSiteRankingEvaluator = new SafeSiteRankingEvaluator();
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

    private CandidateSafeSiteDto createCandidate(String siteId, String name, String district,
                                                 SuitabilityClass suitabilityClass, Double score, Double completeness,
                                                 HazardSafetyStatus hazardStatus) {
        CandidateSafeSiteDto site = new CandidateSafeSiteDto();
        site.setSiteId(siteId);
        site.setSiteName(name);
        site.setCategory(CandidateSiteCategory.EMERGENCY_SHELTER);
        site.setCategoryDisplayName("Emergency Shelter");
        site.setDistrict(district);
        site.setLatitude(25.6000);
        site.setLongitude(85.1000);
        site.setSuitabilityClass(suitabilityClass);
        site.setSuitabilityScore(score);
        site.setDataCompletenessPercentage(completeness);
        site.setHazardSafetyStatus(hazardStatus != null ? hazardStatus : HazardSafetyStatus.SAFE);
        return site;
    }

    // =========================================================================
    // 1. Primary Tier Ordering Tests
    // =========================================================================

    @Nested
    @DisplayName("1. Primary Tier Ordering Tests")
    class PrimaryTierOrderingTests {

        @Test
        @DisplayName("Test 1.1: Primary Ordering Follows HIGHLY_SUITABLE -> SUITABLE -> MARGINAL -> UNSUITABLE -> UNKNOWN")
        void testTierOrderingPriority() {
            CandidateSafeSiteDto siteUnknown = createCandidate("S-UNK", "Unknown Site", "Patna",
                    SuitabilityClass.UNKNOWN, null, 0.0, HazardSafetyStatus.UNKNOWN);
            CandidateSafeSiteDto siteUnsuitable = createCandidate("S-UNS", "Unsuitable Site", "Patna",
                    SuitabilityClass.UNSUITABLE, 20.0, 57.1, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto siteMarginal = createCandidate("S-MAR", "Marginal Site", "Patna",
                    SuitabilityClass.MARGINAL, 55.0, 71.4, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto siteSuitable = createCandidate("S-SUI", "Suitable Site", "Patna",
                    SuitabilityClass.SUITABLE, 78.0, 85.7, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto siteHighlySuitable = createCandidate("S-HI", "Highly Suitable Site", "Patna",
                    SuitabilityClass.HIGHLY_SUITABLE, 95.0, 100.0, HazardSafetyStatus.SAFE);

            // Pass in reverse/scrambled order
            List<CandidateSafeSiteDto> input = Arrays.asList(siteUnknown, siteUnsuitable, siteMarginal, siteSuitable, siteHighlySuitable);
            List<CandidateSafeSiteDto> ranked = safeSiteRankingEvaluator.rankCandidateSites(input);

            assertThat(ranked).hasSize(5);
            assertThat(ranked.get(0).getSiteId()).isEqualTo("S-HI");
            assertThat(ranked.get(0).getRank()).isEqualTo(1);
            assertThat(ranked.get(0).getSuitabilityClass()).isEqualTo(SuitabilityClass.HIGHLY_SUITABLE);

            assertThat(ranked.get(1).getSiteId()).isEqualTo("S-SUI");
            assertThat(ranked.get(1).getRank()).isEqualTo(2);
            assertThat(ranked.get(1).getSuitabilityClass()).isEqualTo(SuitabilityClass.SUITABLE);

            assertThat(ranked.get(2).getSiteId()).isEqualTo("S-MAR");
            assertThat(ranked.get(2).getRank()).isEqualTo(3);
            assertThat(ranked.get(2).getSuitabilityClass()).isEqualTo(SuitabilityClass.MARGINAL);

            assertThat(ranked.get(3).getSiteId()).isEqualTo("S-UNS");
            assertThat(ranked.get(3).getRank()).isEqualTo(4);
            assertThat(ranked.get(3).getSuitabilityClass()).isEqualTo(SuitabilityClass.UNSUITABLE);

            assertThat(ranked.get(4).getSiteId()).isEqualTo("S-UNK");
            assertThat(ranked.get(4).getRank()).isEqualTo(5);
            assertThat(ranked.get(4).getSuitabilityClass()).isEqualTo(SuitabilityClass.UNKNOWN);
        }
    }

    // =========================================================================
    // 2. Secondary Score Ordering Tests
    // =========================================================================

    @Nested
    @DisplayName("2. Secondary Score Ordering Tests")
    class ScoreOrderingTests {

        @Test
        @DisplayName("Test 2.1: Within Same Tier, Higher Suitability Score Ranks Higher")
        void testScoreOrderingWithinTier() {
            CandidateSafeSiteDto siteA = createCandidate("S-A", "Site A", "Muzaffarpur",
                    SuitabilityClass.SUITABLE, 72.0, 71.4, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto siteB = createCandidate("S-B", "Site B", "Muzaffarpur",
                    SuitabilityClass.SUITABLE, 88.5, 71.4, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto siteC = createCandidate("S-C", "Site C", "Muzaffarpur",
                    SuitabilityClass.SUITABLE, 81.0, 71.4, HazardSafetyStatus.SAFE);

            List<CandidateSafeSiteDto> ranked = safeSiteRankingEvaluator.rankCandidateSites(Arrays.asList(siteA, siteB, siteC));

            assertThat(ranked.get(0).getSiteId()).isEqualTo("S-B");
            assertThat(ranked.get(0).getSuitabilityScore()).isEqualTo(88.5);
            assertThat(ranked.get(0).getRank()).isEqualTo(1);

            assertThat(ranked.get(1).getSiteId()).isEqualTo("S-C");
            assertThat(ranked.get(1).getSuitabilityScore()).isEqualTo(81.0);
            assertThat(ranked.get(1).getRank()).isEqualTo(2);

            assertThat(ranked.get(2).getSiteId()).isEqualTo("S-A");
            assertThat(ranked.get(2).getSuitabilityScore()).isEqualTo(72.0);
            assertThat(ranked.get(2).getRank()).isEqualTo(3);
        }
    }

    // =========================================================================
    // 3. Tertiary Completeness Tie-Breaker Tests
    // =========================================================================

    @Nested
    @DisplayName("3. Completeness Tie-Breaker Tests")
    class CompletenessTieBreakerTests {

        @Test
        @DisplayName("Test 3.1: On Equal Score in Same Tier, Higher Data Completeness Ranks First")
        void testCompletenessTieBreaker() {
            CandidateSafeSiteDto siteLowComp = createCandidate("S-LOW", "Low Completeness", "Patna",
                    SuitabilityClass.SUITABLE, 80.0, 57.1, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto siteHighComp = createCandidate("S-HIGH", "High Completeness", "Patna",
                    SuitabilityClass.SUITABLE, 80.0, 100.0, HazardSafetyStatus.SAFE);

            List<CandidateSafeSiteDto> ranked = safeSiteRankingEvaluator.rankCandidateSites(Arrays.asList(siteLowComp, siteHighComp));

            assertThat(ranked.get(0).getSiteId()).isEqualTo("S-HIGH");
            assertThat(ranked.get(0).getDataCompletenessPercentage()).isEqualTo(100.0);
            assertThat(ranked.get(0).getRank()).isEqualTo(1);

            assertThat(ranked.get(1).getSiteId()).isEqualTo("S-LOW");
            assertThat(ranked.get(1).getDataCompletenessPercentage()).isEqualTo(57.1);
            assertThat(ranked.get(1).getRank()).isEqualTo(2);
        }
    }

    // =========================================================================
    // 4. Deterministic SiteId Tie-Breaker Tests
    // =========================================================================

    @Nested
    @DisplayName("4. Deterministic SiteId Tie-Breaker Tests")
    class DeterministicTieBreakerTests {

        @Test
        @DisplayName("Test 4.1: Identical Tier, Score and Completeness Fallback to SiteId Alphabetical")
        void testSiteIdTieBreaker() {
            CandidateSafeSiteDto siteZ = createCandidate("FAC-ZZZ-999", "Site Z", "Patna",
                    SuitabilityClass.HIGHLY_SUITABLE, 95.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto siteA = createCandidate("FAC-AAA-001", "Site A", "Patna",
                    SuitabilityClass.HIGHLY_SUITABLE, 95.0, 100.0, HazardSafetyStatus.SAFE);

            List<CandidateSafeSiteDto> ranked = safeSiteRankingEvaluator.rankCandidateSites(Arrays.asList(siteZ, siteA));

            assertThat(ranked.get(0).getSiteId()).isEqualTo("FAC-AAA-001");
            assertThat(ranked.get(0).getRank()).isEqualTo(1);

            assertThat(ranked.get(1).getSiteId()).isEqualTo("FAC-ZZZ-999");
            assertThat(ranked.get(1).getRank()).isEqualTo(2);
        }
    }

    // =========================================================================
    // 5. AT_RISK Sites Ranking Tests
    // =========================================================================

    @Nested
    @DisplayName("5. AT_RISK Sites Ranking Tests")
    class AtRiskSitesRankingTests {

        @Test
        @DisplayName("Test 5.1: AT_RISK Sites Rank in UNSUITABLE Tier by Non-Hazard Diagnostic Score")
        void testAtRiskSitesRankInUnsuitableTier() {
            CandidateSafeSiteDto siteSafeMarginal = createCandidate("S-SAFE-MAR", "Safe Marginal Site", "Sitamarhi",
                    SuitabilityClass.MARGINAL, 45.0, 57.1, HazardSafetyStatus.SAFE);

            CandidateSafeSiteDto siteAtRiskHighDiag = createCandidate("S-RISK-HI", "PMCH At Risk", "Patna",
                    SuitabilityClass.UNSUITABLE, 100.0, 57.1, HazardSafetyStatus.AT_RISK);

            CandidateSafeSiteDto siteAtRiskLowDiag = createCandidate("S-RISK-LO", "Gaya At Risk", "Gaya",
                    SuitabilityClass.UNSUITABLE, 40.0, 57.1, HazardSafetyStatus.AT_RISK);

            List<CandidateSafeSiteDto> ranked = safeSiteRankingEvaluator.rankCandidateSites(
                    Arrays.asList(siteAtRiskHighDiag, siteSafeMarginal, siteAtRiskLowDiag));

            // MARGINAL precedes UNSUITABLE
            assertThat(ranked.get(0).getSiteId()).isEqualTo("S-SAFE-MAR");
            assertThat(ranked.get(0).getRank()).isEqualTo(1);

            // Within UNSUITABLE, higher diagnostic score ranks higher
            assertThat(ranked.get(1).getSiteId()).isEqualTo("S-RISK-HI");
            assertThat(ranked.get(1).getSuitabilityScore()).isEqualTo(100.0);
            assertThat(ranked.get(1).getRank()).isEqualTo(2);
            assertThat(ranked.get(1).getRankingReason()).contains("active hazard exposure override (AT_RISK)");

            assertThat(ranked.get(2).getSiteId()).isEqualTo("S-RISK-LO");
            assertThat(ranked.get(2).getSuitabilityScore()).isEqualTo(40.0);
            assertThat(ranked.get(2).getRank()).isEqualTo(3);
        }
    }

    // =========================================================================
    // 6. RankingReason & Explainability Tests
    // =========================================================================

    @Nested
    @DisplayName("6. Ranking Reason Tests")
    class RankingReasonTests {

        @Test
        @DisplayName("Test 6.1: Ranking Reason Format for Highly Suitable, Suitable, Marginal, and Unsuitable Sites")
        void testRankingReasonFormats() {
            CandidateSafeSiteDto siteHi = createCandidate("S-HI", "Highly Suitable", "Patna",
                    SuitabilityClass.HIGHLY_SUITABLE, 95.5, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto siteSui = createCandidate("S-SUI", "Suitable", "Patna",
                    SuitabilityClass.SUITABLE, 81.5, 57.1, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto siteRisk = createCandidate("S-RISK", "At Risk", "Patna",
                    SuitabilityClass.UNSUITABLE, 100.0, 57.1, HazardSafetyStatus.AT_RISK);

            List<CandidateSafeSiteDto> ranked = safeSiteRankingEvaluator.rankCandidateSites(Arrays.asList(siteHi, siteSui, siteRisk));

            assertThat(ranked.get(0).getRankingReason()).contains("Rank #1 of 3: Highly suitable safe site with top-tier suitability score (95.5/100) and 100.0% data completeness.");
            assertThat(ranked.get(1).getRankingReason()).contains("Rank #2 of 3: Suitable candidate safe site with suitability score 81.5/100 and 57.1% data completeness.");
            assertThat(ranked.get(2).getRankingReason()).contains("Rank #3 of 3: Unsuitable safe site due to active hazard exposure override (AT_RISK); diagnostic non-hazard score is 100.0/100.");
        }
    }

    // =========================================================================
    // 7. Pipeline & Top-N Query Filter Tests
    // =========================================================================

    @Nested
    @DisplayName("7. Pipeline & Top-N Filter Tests")
    class PipelineAndTopFilterTests {

        @Test
        @DisplayName("Test 7.1: Pipeline Evaluates and Ranks All Candidate Sites")
        void testPipelineRanksAllSites() {
            InfrastructureAssetDto facility1 = new InfrastructureAssetDto();
            facility1.setAssetId("FAC-001");
            facility1.setAssetName("Center 1");
            facility1.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            facility1.setDistrictName("Patna");
            facility1.setLatitude(25.6000);
            facility1.setLongitude(85.1000);

            InfrastructureAssetDto facility2 = new InfrastructureAssetDto();
            facility2.setAssetId("FAC-002");
            facility2.setAssetName("Center 2");
            facility2.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            facility2.setDistrictName("Sitamarhi");
            facility2.setLatitude(26.5900);
            facility2.setLongitude(85.5000);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(Arrays.asList(facility1, facility2));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(Collections.emptyList());

            List<CandidateSafeSiteDto> sites = candidateSafeSiteService.getAllCandidateSites();

            assertThat(sites).hasSize(2);
            assertThat(sites.get(0).getRank()).isEqualTo(1);
            assertThat(sites.get(0).getRankingReason()).isNotNull();
            assertThat(sites.get(1).getRank()).isEqualTo(2);
            assertThat(sites.get(1).getRankingReason()).isNotNull();
        }

        @Test
        @DisplayName("Test 7.2: getCandidateSites with top Parameter Limits Ranked Results")
        void testTopParameterLimitsResults() {
            InfrastructureAssetDto f1 = new InfrastructureAssetDto();
            f1.setAssetId("FAC-001");
            f1.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            f1.setLatitude(25.6);
            f1.setLongitude(85.1);

            InfrastructureAssetDto f2 = new InfrastructureAssetDto();
            f2.setAssetId("FAC-002");
            f2.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            f2.setLatitude(25.7);
            f2.setLongitude(85.2);

            InfrastructureAssetDto f3 = new InfrastructureAssetDto();
            f3.setAssetId("FAC-003");
            f3.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            f3.setLatitude(25.8);
            f3.setLongitude(85.3);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(Arrays.asList(f1, f2, f3));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(Collections.emptyList());

            List<CandidateSafeSiteDto> top2 = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, null, null, 2);

            assertThat(top2).hasSize(2);
            assertThat(top2.get(0).getRank()).isEqualTo(1);
            assertThat(top2.get(1).getRank()).isEqualTo(2);
        }

        @Test
        @DisplayName("Test 7.3: Invalid top <= 0 Parameter Throws InvalidHazardParameterException")
        void testInvalidTopParameter() {
            assertThatThrownBy(() -> candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, null, null, 0))
                    .isInstanceOf(InvalidHazardParameterException.class)
                    .hasMessageContaining("Parameter 'top' must be a positive integer greater than 0");

            assertThatThrownBy(() -> candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, null, null, -5))
                    .isInstanceOf(InvalidHazardParameterException.class)
                    .hasMessageContaining("Parameter 'top' must be a positive integer greater than 0");
        }

        @Test
        @DisplayName("Test 7.4: GeoJSON Export Contains Stage 5.11 Rank and RankingReason Properties")
        void testGeoJsonRankProperties() {
            InfrastructureAssetDto f = new InfrastructureAssetDto();
            f.setAssetId("FAC-001");
            f.setAssetName("Emergency Hub");
            f.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            f.setLatitude(25.6);
            f.setLongitude(85.1);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(Collections.singletonList(f));
            when(distanceEvaluator.resolveActiveHighRiskDistricts()).thenReturn(Collections.emptyList());

            GeoJsonFeatureCollectionDto geoJson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                    null, null, false, null, null, null, null, null, null, null, null, null);

            assertThat(geoJson.getFeatures()).hasSize(1);
            Map<String, Object> props = geoJson.getFeatures().get(0).getProperties();
            assertThat(props).containsKey("rank");
            assertThat(props).containsKey("rankingReason");
            assertThat(props.get("rank")).isEqualTo(1);
        }
    }
}
