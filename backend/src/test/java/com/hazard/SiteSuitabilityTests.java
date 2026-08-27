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
 * Comprehensive Unit and Pipeline Integration Tests for Stage 5.10 — Site Suitability Intelligence.
 */
@ExtendWith(MockitoExtension.class)
class SiteSuitabilityTests {

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

    private SafeSiteThresholds config;
    private CandidateSafeSiteService candidateSafeSiteService;

    @BeforeEach
    void setUp() {
        config = new SafeSiteThresholds();
        candidateSafeSiteService = new CandidateSafeSiteService(
                dataProvider,
                redZoneService,
                districtBoundaryRepository,
                terrainService,
                riskCalculationService,
                config
        );
    }

    private CandidateSafeSiteDto createBaselineCandidate(String siteId, String name, String district) {
        CandidateSafeSiteDto site = new CandidateSafeSiteDto();
        site.setSiteId(siteId);
        site.setSiteName(name);
        site.setCategory(CandidateSiteCategory.EMERGENCY_SHELTER);
        site.setCategoryDisplayName("Emergency Shelter");
        site.setDistrict(district);
        site.setLatitude(25.6000);
        site.setLongitude(85.1000);
        return site;
    }

    // =========================================================================
    // 1. SuitabilityClass Enum Tests
    // =========================================================================

    @Nested
    @DisplayName("1. SuitabilityClass Enum Tests")
    class SuitabilityClassEnumTests {

        @Test
        @DisplayName("Test 1.1: Enum Values, Colors and Predicates")
        void testEnumValuesAndPredicates() {
            assertThat(SuitabilityClass.HIGHLY_SUITABLE.getDisplayName()).isEqualTo("Highly Suitable");
            assertThat(SuitabilityClass.HIGHLY_SUITABLE.getColorHex()).isEqualTo("#2E7D32");
            assertThat(SuitabilityClass.HIGHLY_SUITABLE.isHighlySuitable()).isTrue();
            assertThat(SuitabilityClass.HIGHLY_SUITABLE.isKnown()).isTrue();

            assertThat(SuitabilityClass.SUITABLE.getDisplayName()).isEqualTo("Suitable");
            assertThat(SuitabilityClass.SUITABLE.getColorHex()).isEqualTo("#4CAF50");
            assertThat(SuitabilityClass.SUITABLE.isSuitable()).isTrue();
            assertThat(SuitabilityClass.SUITABLE.isKnown()).isTrue();

            assertThat(SuitabilityClass.MARGINAL.getDisplayName()).isEqualTo("Marginal");
            assertThat(SuitabilityClass.MARGINAL.getColorHex()).isEqualTo("#FF9800");
            assertThat(SuitabilityClass.MARGINAL.isMarginal()).isTrue();
            assertThat(SuitabilityClass.MARGINAL.isKnown()).isTrue();

            assertThat(SuitabilityClass.UNSUITABLE.getDisplayName()).isEqualTo("Unsuitable");
            assertThat(SuitabilityClass.UNSUITABLE.getColorHex()).isEqualTo("#F44336");
            assertThat(SuitabilityClass.UNSUITABLE.isUnsuitable()).isTrue();
            assertThat(SuitabilityClass.UNSUITABLE.isKnown()).isTrue();

            assertThat(SuitabilityClass.UNKNOWN.getDisplayName()).isEqualTo("Unknown");
            assertThat(SuitabilityClass.UNKNOWN.getColorHex()).isEqualTo("#9E9E9E");
            assertThat(SuitabilityClass.UNKNOWN.isKnown()).isFalse();
        }

        @Test
        @DisplayName("Test 1.2: fromString Case-Insensitive Parsing & Fallbacks")
        void testFromString() {
            assertThat(SuitabilityClass.fromString("highly_suitable")).isEqualTo(SuitabilityClass.HIGHLY_SUITABLE);
            assertThat(SuitabilityClass.fromString("HIGHLY-SUITABLE")).isEqualTo(SuitabilityClass.HIGHLY_SUITABLE);
            assertThat(SuitabilityClass.fromString("Suitable")).isEqualTo(SuitabilityClass.SUITABLE);
            assertThat(SuitabilityClass.fromString("MARGINAL")).isEqualTo(SuitabilityClass.MARGINAL);
            assertThat(SuitabilityClass.fromString("unsuitable")).isEqualTo(SuitabilityClass.UNSUITABLE);
            assertThat(SuitabilityClass.fromString(null)).isEqualTo(SuitabilityClass.UNKNOWN);
            assertThat(SuitabilityClass.fromString("")).isEqualTo(SuitabilityClass.UNKNOWN);
        }

        @Test
        @DisplayName("Test 1.3: fromString Invalid Throws InvalidHazardParameterException")
        void testFromStringInvalid() {
            assertThatThrownBy(() -> SuitabilityClass.fromString("PERFECT"))
                    .isInstanceOf(InvalidHazardParameterException.class)
                    .hasMessageContaining("Allowed values: HIGHLY_SUITABLE, SUITABLE, MARGINAL, UNSUITABLE, UNKNOWN");
        }
    }

    // =========================================================================
    // 2. SuitabilityEvaluationConfig Tests
    // =========================================================================

    @Nested
    @DisplayName("2. SuitabilityEvaluationConfig Tests")
    class SuitabilityEvaluationConfigTests {

        @Test
        @DisplayName("Test 2.1: Default Weights Sum to 1.00")
        void testDefaultWeights() {
            double totalWeight = config.getHazardSafetyWeight()
                    + config.getTerrainWeight()
                    + config.getDistanceWeight()
                    + config.getRoadsWeight()
                    + config.getHealthcareWeight()
                    + config.getWaterWeight()
                    + config.getInfrastructureWeight();

            assertThat(totalWeight).isCloseTo(1.00, org.assertj.core.data.Offset.offset(0.0001));
            assertThat(config.getHazardSafetyWeight()).isEqualTo(0.30);
            assertThat(config.getTerrainWeight()).isEqualTo(0.15);
            assertThat(config.getDistanceWeight()).isEqualTo(0.15);
            assertThat(config.getRoadsWeight()).isEqualTo(0.10);
            assertThat(config.getHealthcareWeight()).isEqualTo(0.10);
            assertThat(config.getWaterWeight()).isEqualTo(0.10);
            assertThat(config.getInfrastructureWeight()).isEqualTo(0.10);
        }

        @Test
        @DisplayName("Test 2.2: Default Classification Score Bands")
        void testDefaultScoreBands() {
            assertThat(config.getHighlySuitableMinScore()).isEqualTo(90.0);
            assertThat(config.getSuitableMinScore()).isEqualTo(70.0);
            assertThat(config.getMarginalMinScore()).isEqualTo(40.0);
        }
    }

    // =========================================================================
    // 3. Safety Gate Override Tests
    // =========================================================================

    @Nested
    @DisplayName("3. Hard Safety Gate Override Tests")
    class SafetyGateTests {

        @Test
        @DisplayName("Test 3.1: Candidate with AT_RISK Hazard Safety is UNSUITABLE with Diagnostic Score 100.0 When Other Factors Optimal")
        void testSafetyGateOverrideWithOptimalNonHazardFactors() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-01", "At-Risk Shelter", "Sitamarhi");
            site.setHazardSafetyStatus(HazardSafetyStatus.AT_RISK);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.NEAR);
            site.setRoadAccessStatus(RoadAccessStatus.NEAR);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.NEAR);
            site.setWaterAccessStatus(WaterAccessStatus.NEAR);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.NEAR);

            candidateSafeSiteService.evaluateSuitability(site);

            // Classification MUST remain UNSUITABLE due to safety gate
            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.UNSUITABLE);
            // Diagnostic score MUST NOT be forced to 0.0 when other factors have valid data
            assertThat(site.getSuitabilityScore()).isEqualTo(100.0);
            assertThat(site.getSuitabilityReason()).isEqualTo("Site is classified as UNSUITABLE because it is currently AT_RISK; hazard exposure overrides other suitability factors.");
            assertThat(site.getKnownFactorCount()).isEqualTo(7);
            assertThat(site.getUnknownFactorCount()).isEqualTo(0);
            assertThat(site.getDataCompletenessPercentage()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Test 3.2: AT_RISK Candidate with Mixed Non-Hazard Factors Produces Normalized Diagnostic Score")
        void testSafetyGateWithMixedNonHazardFactors() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-01B", "At-Risk Campus", "Patna");
            site.setHazardSafetyStatus(HazardSafetyStatus.AT_RISK);
            site.setTerrainStatus(TerrainStatus.FAVORABLE); // 100 * 0.15 = 15
            site.setDistanceStatus(DistanceStatus.MODERATE); // 60 * 0.15 = 9
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.NEAR); // 100 * 0.10 = 10
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.FAR); // 20 * 0.10 = 2

            // Non-hazard known weights: 0.15 + 0.15 + 0.10 + 0.10 = 0.50
            // Non-hazard weighted sum: 15 + 9 + 10 + 2 = 36.0
            // Diagnostic score: 36.0 / 0.50 = 72.0
            candidateSafeSiteService.evaluateSuitability(site);

            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.UNSUITABLE);
            assertThat(site.getSuitabilityScore()).isEqualTo(72.0);
            assertThat(site.getSuitabilityReason()).isEqualTo("Site is classified as UNSUITABLE because it is currently AT_RISK; hazard exposure overrides other suitability factors.");
            assertThat(site.getKnownFactorCount()).isEqualTo(5);
            assertThat(site.getUnknownFactorCount()).isEqualTo(2);
            assertThat(site.getDataCompletenessPercentage()).isEqualTo(71.4);
        }

        @Test
        @DisplayName("Test 3.3: AT_RISK Candidate with All Other Factors UNKNOWN Produces Null Diagnostic Score")
        void testSafetyGateWithAllOtherFactorsUnknown() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-01C", "At-Risk Sparse Site", "Gaya");
            site.setHazardSafetyStatus(HazardSafetyStatus.AT_RISK);
            site.setTerrainStatus(TerrainStatus.UNKNOWN);
            site.setDistanceStatus(DistanceStatus.UNKNOWN);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.UNKNOWN);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.UNKNOWN);

            candidateSafeSiteService.evaluateSuitability(site);

            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.UNSUITABLE);
            assertThat(site.getSuitabilityScore()).isNull();
            assertThat(site.getSuitabilityReason()).isEqualTo("Site is classified as UNSUITABLE because it is currently AT_RISK; hazard exposure overrides other suitability factors.");
            assertThat(site.getKnownFactorCount()).isEqualTo(1);
            assertThat(site.getUnknownFactorCount()).isEqualTo(6);
            assertThat(site.getDataCompletenessPercentage()).isEqualTo(14.3);
        }
    }

    // =========================================================================
    // 4. UNKNOWN Normalization Tests
    // =========================================================================

    @Nested
    @DisplayName("4. UNKNOWN Normalization & Data Completeness Tests")
    class UnknownNormalizationTests {

        @Test
        @DisplayName("Test 4.1: All Dimensions UNKNOWN Results in SuitabilityClass UNKNOWN and null Score")
        void testAllDimensionsUnknown() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-02", "Unknown Site", "Patna");
            site.setHazardSafetyStatus(HazardSafetyStatus.UNKNOWN);
            site.setTerrainStatus(TerrainStatus.UNKNOWN);
            site.setDistanceStatus(DistanceStatus.UNKNOWN);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.UNKNOWN);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.UNKNOWN);

            candidateSafeSiteService.evaluateSuitability(site);

            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.UNKNOWN);
            assertThat(site.getSuitabilityScore()).isNull();
            assertThat(site.getKnownFactorCount()).isEqualTo(0);
            assertThat(site.getUnknownFactorCount()).isEqualTo(7);
            assertThat(site.getDataCompletenessPercentage()).isEqualTo(0.0);
            assertThat(site.getSuitabilityReason()).contains("All 7 evaluation dimensions are UNKNOWN");
        }

        @Test
        @DisplayName("Test 4.2: Single Known Dimension (SAFE) Normalizes to 100.0 Score")
        void testSingleKnownDimension() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-03", "Partially Known Site", "Patna");
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.UNKNOWN);
            site.setDistanceStatus(DistanceStatus.UNKNOWN);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.UNKNOWN);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.UNKNOWN);

            candidateSafeSiteService.evaluateSuitability(site);

            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.HIGHLY_SUITABLE);
            assertThat(site.getSuitabilityScore()).isEqualTo(100.0);
            assertThat(site.getKnownFactorCount()).isEqualTo(1);
            assertThat(site.getUnknownFactorCount()).isEqualTo(6);
            assertThat(site.getDataCompletenessPercentage()).isEqualTo(14.3);
            assertThat(site.getSuitabilityReason()).contains("1/7 dimensions evaluated");
        }

        @Test
        @DisplayName("Test 4.3: Mixed Known Dimensions Normalize Accurately Over Known Weights")
        void testMixedKnownDimensions() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-04", "Mixed Site", "Patna");
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.UNFAVORABLE);
            site.setDistanceStatus(DistanceStatus.NEAR);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.UNKNOWN);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.UNKNOWN);

            candidateSafeSiteService.evaluateSuitability(site);

            assertThat(site.getSuitabilityScore()).isEqualTo(75.0);
            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.SUITABLE);
            assertThat(site.getKnownFactorCount()).isEqualTo(3);
            assertThat(site.getUnknownFactorCount()).isEqualTo(4);
            assertThat(site.getDataCompletenessPercentage()).isEqualTo(42.9);
        }
    }

    // =========================================================================
    // 5. Classification Tier Scoring Tests
    // =========================================================================

    @Nested
    @DisplayName("5. Classification Tier Scoring Tests")
    class ClassificationTierTests {

        @Test
        @DisplayName("Test 5.1: Fully Optimal Candidate Site -> HIGHLY_SUITABLE (Score 100.0)")
        void testFullyOptimalCandidate() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-05", "Optimal Campus", "Patna");
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.NEAR);
            site.setRoadAccessStatus(RoadAccessStatus.NEAR);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.NEAR);
            site.setWaterAccessStatus(WaterAccessStatus.NEAR);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.NEAR);

            candidateSafeSiteService.evaluateSuitability(site);

            assertThat(site.getSuitabilityScore()).isEqualTo(100.0);
            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.HIGHLY_SUITABLE);
            assertThat(site.getKnownFactorCount()).isEqualTo(7);
            assertThat(site.getUnknownFactorCount()).isEqualTo(0);
            assertThat(site.getDataCompletenessPercentage()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Test 5.2: Moderate Access Factors Produce SUITABLE Classification")
        void testModerateAccessCandidate() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-06", "Moderate Access Shelter", "Muzaffarpur");
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.MODERATE);
            site.setRoadAccessStatus(RoadAccessStatus.MODERATE);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.MODERATE);
            site.setWaterAccessStatus(WaterAccessStatus.MODERATE);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.MODERATE);

            candidateSafeSiteService.evaluateSuitability(site);

            assertThat(site.getSuitabilityScore()).isEqualTo(78.0);
            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.SUITABLE);
        }

        @Test
        @DisplayName("Test 5.3: Marginal Candidate with Far Services (Score 40 - 69.99)")
        void testMarginalCandidate() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-07", "Remote Shelter", "West Champaran");
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.UNFAVORABLE);
            site.setDistanceStatus(DistanceStatus.FAR);
            site.setRoadAccessStatus(RoadAccessStatus.FAR);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.FAR);
            site.setWaterAccessStatus(WaterAccessStatus.MODERATE);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.FAR);

            candidateSafeSiteService.evaluateSuitability(site);

            assertThat(site.getSuitabilityScore()).isEqualTo(45.0);
            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.MARGINAL);
        }

        @Test
        @DisplayName("Test 5.4: Unsuitable Candidate Due to Multi-Dimensional Deficiencies (Score < 40)")
        void testUnsuitableCandidateDeficiencies() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-08", "Deficient Shelter", "Gaya");
            site.setHazardSafetyStatus(HazardSafetyStatus.UNKNOWN);
            site.setTerrainStatus(TerrainStatus.UNFAVORABLE);
            site.setDistanceStatus(DistanceStatus.FAR);
            site.setRoadAccessStatus(RoadAccessStatus.FAR);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.FAR);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.UNKNOWN);

            candidateSafeSiteService.evaluateSuitability(site);

            assertThat(site.getSuitabilityScore()).isEqualTo(14.0);
            assertThat(site.getSuitabilityClass()).isEqualTo(SuitabilityClass.UNSUITABLE);
        }
    }

    // =========================================================================
    // 6. Factor Breakdown Map Tests
    // =========================================================================

    @Nested
    @DisplayName("6. Factor Breakdown Map Tests")
    class FactorBreakdownTests {

        @Test
        @DisplayName("Test 6.1: suitabilityFactors Map Contains All 7 Dimensions with Correct Schema")
        void testSuitabilityFactorsSchema() {
            CandidateSafeSiteDto site = createBaselineCandidate("S-09", "Schema Test Site", "Patna");
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setTerrainStatus(TerrainStatus.FAVORABLE);
            site.setDistanceStatus(DistanceStatus.NEAR);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.NEAR);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.MODERATE);

            candidateSafeSiteService.evaluateSuitability(site);

            Map<String, Object> factors = site.getSuitabilityFactors();
            assertThat(factors).isNotNull();
            assertThat(factors).containsKeys("hazardSafety", "terrain", "distance", "roads", "healthcare", "water", "infrastructure");

            @SuppressWarnings("unchecked")
            Map<String, Object> hazardDetail = (Map<String, Object>) factors.get("hazardSafety");
            assertThat(hazardDetail.get("status")).isEqualTo("SAFE");
            assertThat(hazardDetail.get("score")).isEqualTo(100.0);
            assertThat(hazardDetail.get("weight")).isEqualTo(0.30);
            assertThat(hazardDetail.get("isKnown")).isEqualTo(true);

            @SuppressWarnings("unchecked")
            Map<String, Object> roadsDetail = (Map<String, Object>) factors.get("roads");
            assertThat(roadsDetail.get("status")).isEqualTo("UNKNOWN");
            assertThat(roadsDetail.get("score")).isNull();
            assertThat(roadsDetail.get("weight")).isEqualTo(0.10);
            assertThat(roadsDetail.get("isKnown")).isEqualTo(false);
        }
    }

    // =========================================================================
    // 7. Pipeline & Service Integration Tests
    // =========================================================================

    @Nested
    @DisplayName("7. Pipeline & Service Integration Tests")
    class PipelineIntegrationTests {

        @Test
        @DisplayName("Test 7.1: Pipeline Evaluates All Candidate Sites with Suitability Intelligence")
        void testPipelineEvaluatesSuitability() {
            InfrastructureAssetDto facility = new InfrastructureAssetDto();
            facility.setAssetId("FAC-EMG-001");
            facility.setAssetName("Patna Central Disaster Center");
            facility.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            facility.setSubType("disaster_management_center");
            facility.setDistrictName("Patna");
            facility.setLatitude(25.6000);
            facility.setLongitude(85.1000);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(Collections.singletonList(facility));

            List<CandidateSafeSiteDto> sites = candidateSafeSiteService.getAllCandidateSites();

            assertThat(sites).hasSize(1);
            CandidateSafeSiteDto evaluated = sites.get(0);
            assertThat(evaluated.getSuitabilityClass()).isNotNull();
            assertThat(evaluated.getKnownFactorCount()).isNotNull();
            assertThat(evaluated.getUnknownFactorCount()).isNotNull();
            assertThat(evaluated.getDataCompletenessPercentage()).isNotNull();
            assertThat(evaluated.getSuitabilityReason()).isNotNull();
            assertThat(evaluated.getSuitabilityFactors()).isNotNull();
        }

        @Test
        @DisplayName("Test 7.2: Query Filtering by suitabilityClass Returns Filtered Results")
        void testFilterBySuitabilityClass() {
            InfrastructureAssetDto f1 = new InfrastructureAssetDto();
            f1.setAssetId("FAC-EMG-001");
            f1.setAssetName("Site 1");
            f1.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            f1.setDistrictName("Patna");
            f1.setLatitude(25.6000);
            f1.setLongitude(85.1000);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(Collections.singletonList(f1));

            // Query matching class
            List<CandidateSafeSiteDto> result = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, null, "HIGHLY_SUITABLE");
            assertThat(result).hasSize(1);

            // Query non-matching class
            List<CandidateSafeSiteDto> emptyResult = candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, null, "UNSUITABLE");
            assertThat(emptyResult).isEmpty();
        }

        @Test
        @DisplayName("Test 7.3: Invalid suitabilityClass Filter Throws InvalidHazardParameterException")
        void testInvalidSuitabilityClassFilter() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> candidateSafeSiteService.getCandidateSites(
                    null, null, false, null, null, null, null, null, null, null, "INVALID_TIER"))
                    .isInstanceOf(InvalidHazardParameterException.class)
                    .hasMessageContaining("Allowed values: HIGHLY_SUITABLE, SUITABLE, MARGINAL, UNSUITABLE, UNKNOWN");
        }

        @Test
        @DisplayName("Test 7.4: GeoJSON Export Contains Stage 5.10 Suitability Properties")
        void testGeoJsonExportSuitabilityProperties() {
            InfrastructureAssetDto facility = new InfrastructureAssetDto();
            facility.setAssetId("FAC-EMG-001");
            facility.setAssetName("Patna Central Disaster Center");
            facility.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            facility.setSubType("disaster_management_center");
            facility.setDistrictName("Patna");
            facility.setLatitude(25.6000);
            facility.setLongitude(85.1000);

            when(dataProvider.getAllRegionalFacilities()).thenReturn(Collections.singletonList(facility));

            GeoJsonFeatureCollectionDto geoJson = candidateSafeSiteService.generateCandidateSitesGeoJson(
                    null, null, false, null, null, null, null, null, null, null, null);

            assertThat(geoJson.getFeatures()).hasSize(1);
            Map<String, Object> props = geoJson.getFeatures().get(0).getProperties();

            assertThat(props).containsKey("suitabilityClass");
            assertThat(props).containsKey("suitabilityScore");
            assertThat(props).containsKey("knownFactorCount");
            assertThat(props).containsKey("unknownFactorCount");
            assertThat(props).containsKey("dataCompletenessPercentage");
            assertThat(props).containsKey("suitabilityReason");
            assertThat(props).containsKey("suitabilityFactors");
        }
    }
}
