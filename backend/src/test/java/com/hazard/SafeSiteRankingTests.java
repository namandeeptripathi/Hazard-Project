package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.safesite.*;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
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
    private DistrictBoundaryRepository districtBoundaryRepository;

    @Mock
    private TerrainService terrainService;

    @Mock
    private RiskCalculationService riskCalculationService;

    private SafeSiteThresholds suitabilityConfig;
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        suitabilityConfig = new SafeSiteThresholds();
        candidateSafeSiteService = new CandidateSafeSiteService(
                dataProvider,
                redZoneService,
                districtBoundaryRepository,
                terrainService,
                riskCalculationService,
                suitabilityConfig
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
    // 1. Primary Ordering by SuitabilityClass Tier
    // =========================================================================

    @Nested
    @DisplayName("1. Primary Ordering by SuitabilityClass Tier")
    class PrimaryTierOrderingTests {

        @Test
        @DisplayName("Test 1.1: Ranking Orders Strictly by Tier Hierarchy")
        void testTierHierarchyOrdering() {
            CandidateSafeSiteDto s1 = createCandidate("S-01", "Marginal Site", "Patna", SuitabilityClass.MARGINAL, 55.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto s2 = createCandidate("S-02", "Highly Suitable Site", "Patna", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto s3 = createCandidate("S-03", "Unsuitable Site", "Patna", SuitabilityClass.UNSUITABLE, 20.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto s4 = createCandidate("S-04", "Suitable Site", "Patna", SuitabilityClass.SUITABLE, 75.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto s5 = createCandidate("S-05", "Unknown Site", "Patna", SuitabilityClass.UNKNOWN, null, 0.0, HazardSafetyStatus.UNKNOWN);

            List<CandidateSafeSiteDto> ranked = candidateSafeSiteService.rankCandidateSites(Arrays.asList(s1, s2, s3, s4, s5));

            assertThat(ranked).hasSize(5);
            assertThat(ranked.get(0).getSiteId()).isEqualTo("S-02"); // HIGHLY_SUITABLE (Rank 1)
            assertThat(ranked.get(0).getRank()).isEqualTo(1);
            assertThat(ranked.get(1).getSiteId()).isEqualTo("S-04"); // SUITABLE (Rank 2)
            assertThat(ranked.get(1).getRank()).isEqualTo(2);
            assertThat(ranked.get(2).getSiteId()).isEqualTo("S-01"); // MARGINAL (Rank 3)
            assertThat(ranked.get(2).getRank()).isEqualTo(3);
            assertThat(ranked.get(3).getSiteId()).isEqualTo("S-03"); // UNSUITABLE (Rank 4)
            assertThat(ranked.get(3).getRank()).isEqualTo(4);
            assertThat(ranked.get(4).getSiteId()).isEqualTo("S-05"); // UNKNOWN (Rank 5)
            assertThat(ranked.get(4).getRank()).isEqualTo(5);
        }
    }

    // =========================================================================
    // 2. Secondary Ordering by Suitability Score DESC
    // =========================================================================

    @Nested
    @DisplayName("2. Secondary Ordering by Suitability Score DESC")
    class ScoreOrderingTests {

        @Test
        @DisplayName("Test 2.1: Within Same Tier, Higher Score Ranks First")
        void testScoreOrderingWithinTier() {
            CandidateSafeSiteDto s1 = createCandidate("S-01", "Suitable 72", "Patna", SuitabilityClass.SUITABLE, 72.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto s2 = createCandidate("S-02", "Suitable 88", "Patna", SuitabilityClass.SUITABLE, 88.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto s3 = createCandidate("S-03", "Suitable 79", "Patna", SuitabilityClass.SUITABLE, 79.0, 100.0, HazardSafetyStatus.SAFE);

            List<CandidateSafeSiteDto> ranked = candidateSafeSiteService.rankCandidateSites(Arrays.asList(s1, s2, s3));

            assertThat(ranked.get(0).getSiteId()).isEqualTo("S-02"); // 88.0 (Rank 1)
            assertThat(ranked.get(1).getSiteId()).isEqualTo("S-03"); // 79.0 (Rank 2)
            assertThat(ranked.get(2).getSiteId()).isEqualTo("S-01"); // 72.0 (Rank 3)
        }
    }

    // =========================================================================
    // 3. Tertiary Ordering by Data Completeness DESC
    // =========================================================================

    @Nested
    @DisplayName("3. Tertiary Ordering by Data Completeness DESC")
    class CompletenessTieBreakerTests {

        @Test
        @DisplayName("Test 3.1: Identical Tier and Score Broken by Completeness Percentage")
        void testCompletenessTieBreaker() {
            CandidateSafeSiteDto s1 = createCandidate("S-01", "Site 50% Completeness", "Patna", SuitabilityClass.SUITABLE, 80.0, 50.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto s2 = createCandidate("S-02", "Site 100% Completeness", "Patna", SuitabilityClass.SUITABLE, 80.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto s3 = createCandidate("S-03", "Site 75% Completeness", "Patna", SuitabilityClass.SUITABLE, 80.0, 75.0, HazardSafetyStatus.SAFE);

            List<CandidateSafeSiteDto> ranked = candidateSafeSiteService.rankCandidateSites(Arrays.asList(s1, s2, s3));

            assertThat(ranked.get(0).getSiteId()).isEqualTo("S-02"); // 100.0%
            assertThat(ranked.get(1).getSiteId()).isEqualTo("S-03"); // 75.0%
            assertThat(ranked.get(2).getSiteId()).isEqualTo("S-01"); // 50.0%
        }
    }

    // =========================================================================
    // 4. Deterministic Lexicographical Tie-Breaker
    // =========================================================================

    @Nested
    @DisplayName("4. Deterministic Lexicographical Tie-Breaker")
    class DeterministicTieBreakerTests {

        @Test
        @DisplayName("Test 4.1: Identical Tier, Score and Completeness Broken Lexicographically by SiteId")
        void testSiteIdTieBreaker() {
            CandidateSafeSiteDto sB = createCandidate("FAC-B", "Site B", "Patna", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto sA = createCandidate("FAC-A", "Site A", "Patna", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto sC = createCandidate("FAC-C", "Site C", "Patna", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 100.0, HazardSafetyStatus.SAFE);

            List<CandidateSafeSiteDto> ranked = candidateSafeSiteService.rankCandidateSites(Arrays.asList(sB, sC, sA));

            assertThat(ranked.get(0).getSiteId()).isEqualTo("FAC-A");
            assertThat(ranked.get(1).getSiteId()).isEqualTo("FAC-B");
            assertThat(ranked.get(2).getSiteId()).isEqualTo("FAC-C");
        }
    }

    // =========================================================================
    // 5. AT_RISK Sites Ranking Behavior
    // =========================================================================

    @Nested
    @DisplayName("5. AT_RISK Sites Ranking Behavior")
    class AtRiskSitesRankingTests {

        @Test
        @DisplayName("Test 5.1: AT_RISK Sites Are in UNSUITABLE Tier and Ranked by Diagnostic Score")
        void testAtRiskSitesRanking() {
            CandidateSafeSiteDto safeSite = createCandidate("S-SAFE", "Safe Marginal", "Patna", SuitabilityClass.MARGINAL, 45.0, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto atRiskHighDiag = createCandidate("S-AR-HIGH", "At Risk High Diag", "Sitamarhi", SuitabilityClass.UNSUITABLE, 90.0, 100.0, HazardSafetyStatus.AT_RISK);
            CandidateSafeSiteDto atRiskLowDiag = createCandidate("S-AR-LOW", "At Risk Low Diag", "Sitamarhi", SuitabilityClass.UNSUITABLE, 40.0, 100.0, HazardSafetyStatus.AT_RISK);

            List<CandidateSafeSiteDto> ranked = candidateSafeSiteService.rankCandidateSites(Arrays.asList(atRiskLowDiag, safeSite, atRiskHighDiag));

            assertThat(ranked.get(0).getSiteId()).isEqualTo("S-SAFE");     // MARGINAL > UNSUITABLE
            assertThat(ranked.get(1).getSiteId()).isEqualTo("S-AR-HIGH");  // UNSUITABLE with 90.0 diagnostic score
            assertThat(ranked.get(2).getSiteId()).isEqualTo("S-AR-LOW");   // UNSUITABLE with 40.0 diagnostic score
        }
    }

    // =========================================================================
    // 6. Explainable Ranking Reason Generation
    // =========================================================================

    @Nested
    @DisplayName("6. Explainable Ranking Reason Generation")
    class RankingReasonTests {

        @Test
        @DisplayName("Test 6.1: Generated Ranking Reasons Are Human-Readable and Clear")
        void testRankingReasonStrings() {
            CandidateSafeSiteDto s1 = createCandidate("S-01", "Top Site", "Patna", SuitabilityClass.HIGHLY_SUITABLE, 96.5, 100.0, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto s2 = createCandidate("S-02", "At-Risk Site", "Sitamarhi", SuitabilityClass.UNSUITABLE, 85.0, 100.0, HazardSafetyStatus.AT_RISK);

            List<CandidateSafeSiteDto> ranked = candidateSafeSiteService.rankCandidateSites(Arrays.asList(s1, s2));

            assertThat(ranked.get(0).getRankingReason())
                    .contains("Rank #1 of 2")
                    .contains("Highly suitable safe site")
                    .contains("96.5/100")
                    .contains("100.0% data completeness");

            assertThat(ranked.get(1).getRankingReason())
                    .contains("Rank #2 of 2")
                    .contains("Unsuitable safe site due to active hazard exposure override (AT_RISK)")
                    .contains("85.0/100");
        }
    }

    // =========================================================================
    // 7. Pipeline Integration & Top N Limit Filtering
    // =========================================================================

    @Nested
    @DisplayName("7. Pipeline Integration & Top N Limit Filtering")
    class PipelineAndTopFilterTests {

        @Test
        @DisplayName("Test 7.1: Pipeline End-to-End Automatically Assigns Ranks and Reasons")
        void testPipelineAssignsRanks() {
            InfrastructureAssetDto f1 = new InfrastructureAssetDto();
            f1.setAssetId("FAC-01");
            f1.setAssetName("Center 1");
            f1.setCategory(InfrastructureCategory.EDUCATION);
            f1.setDistrictName("Patna");
            f1.setLatitude(25.60);
            f1.setLongitude(85.10);

            InfrastructureAssetDto f2 = new InfrastructureAssetDto();
            f2.setAssetId("FAC-02");
            f2.setAssetName("Center 2");
            f2.setCategory(InfrastructureCategory.HEALTHCARE);
            f2.setDistrictName("Patna");
            f2.setLatitude(25.61);
            f2.setLongitude(85.11);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(Arrays.asList(f1, f2));

            List<CandidateSafeSiteDto> result = candidateSafeSiteService.getAllCandidateSites();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getRank()).isEqualTo(1);
            assertThat(result.get(0).getRankingReason()).isNotBlank();
            assertThat(result.get(1).getRank()).isEqualTo(2);
            assertThat(result.get(1).getRankingReason()).isNotBlank();
        }

        @Test
        @DisplayName("Test 7.2: Top N Parameter Limits Ranked Output Correctly")
        void testTopNLimitFiltering() {
            InfrastructureAssetDto f1 = new InfrastructureAssetDto();
            f1.setAssetId("FAC-01");
            f1.setCategory(InfrastructureCategory.EDUCATION);
            f1.setDistrictName("Patna");
            f1.setLatitude(25.60);
            f1.setLongitude(85.10);

            InfrastructureAssetDto f2 = new InfrastructureAssetDto();
            f2.setAssetId("FAC-02");
            f2.setCategory(InfrastructureCategory.EDUCATION);
            f2.setDistrictName("Patna");
            f2.setLatitude(25.61);
            f2.setLongitude(85.11);

            InfrastructureAssetDto f3 = new InfrastructureAssetDto();
            f3.setAssetId("FAC-03");
            f3.setCategory(InfrastructureCategory.EDUCATION);
            f3.setDistrictName("Patna");
            f3.setLatitude(25.62);
            f3.setLongitude(85.12);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(Arrays.asList(f1, f2, f3));

            List<CandidateSafeSiteDto> top2 = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, null, null, 2);

            assertThat(top2).hasSize(2);
            assertThat(top2.get(0).getRank()).isEqualTo(1);
            assertThat(top2.get(1).getRank()).isEqualTo(2);
        }

        @Test
        @DisplayName("Test 7.3: Invalid Top N Parameter (<= 0) Throws HTTP 400 InvalidHazardParameterException")
        void testInvalidTopNThrows() {
            assertThatThrownBy(() -> candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, null, null, 0))
                    .isInstanceOf(InvalidHazardParameterException.class)
                    .hasMessageContaining("Parameter 'top' must be a positive integer greater than 0.");

            assertThatThrownBy(() -> candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, null, null, -5))
                    .isInstanceOf(InvalidHazardParameterException.class)
                    .hasMessageContaining("Parameter 'top' must be a positive integer greater than 0.");
        }

        @Test
        @DisplayName("Test 7.4: GeoJSON Export Contains Rank and RankingReason Properties")
        void testGeoJsonContainsRankProperties() {
            InfrastructureAssetDto f1 = new InfrastructureAssetDto();
            f1.setAssetId("FAC-01");
            f1.setAssetName("Center 1");
            f1.setCategory(InfrastructureCategory.EDUCATION);
            f1.setDistrictName("Patna");
            f1.setLatitude(25.60);
            f1.setLongitude(85.10);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(Collections.singletonList(f1));

            GeoJsonFeatureCollectionDto geoJson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                    null, null, false, null, null, null, null, null, null, null, null, 1);

            assertThat(geoJson.getFeatures()).hasSize(1);
            Map<String, Object> props = geoJson.getFeatures().get(0).getProperties();
            assertThat(props).containsKey("rank");
            assertThat(props).containsKey("rankingReason");
            assertThat(props.get("rank")).isEqualTo(1);
        }
    }
}
