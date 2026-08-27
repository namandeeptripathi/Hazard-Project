package com.hazard;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.risk.ZoneLevel;
import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.risk.RedZoneDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.HazardNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Stage 5.2 — Unit and Integration Tests for Candidate Safe-Site Identification.
 */
@ExtendWith(MockitoExtension.class)
class CandidateSafeSiteTests {

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
        candidateSafeSiteService = new CandidateSafeSiteService(
                dataProvider,
                redZoneService,
                districtBoundaryRepository,
                terrainService,
                riskCalculationService,
                thresholds
        );
    }

    private List<InfrastructureAssetDto> createMockFacilities() {
        List<InfrastructureAssetDto> list = new ArrayList<>();

        // 1. EDUCATION (Valid candidate safe site)
        InfrastructureAssetDto edu = new InfrastructureAssetDto();
        edu.setAssetId("FAC-EDU-001");
        edu.setAssetName("National Institute of Technology Patna (NITP)");
        edu.setCategory(InfrastructureCategory.EDUCATION);
        edu.setSubType("engineering_institute");
        edu.setDistrictName("Patna");
        edu.setLongitude(85.1720);
        edu.setLatitude(25.6210);
        edu.setDataProvenance("CONFIGURED_REGIONAL_FACILITIES");
        list.add(edu);

        // 2. GOVERNMENT (Valid candidate safe site)
        InfrastructureAssetDto gov = new InfrastructureAssetDto();
        gov.setAssetId("FAC-GOV-003");
        gov.setAssetName("Sitamarhi Collectorate & District HQ");
        gov.setCategory(InfrastructureCategory.GOVERNMENT);
        gov.setSubType("district_collectorate");
        gov.setDistrictName("Sitamarhi");
        gov.setLongitude(85.4950);
        gov.setLatitude(26.5890);
        gov.setDataProvenance("CONFIGURED_REGIONAL_FACILITIES");
        list.add(gov);

        // 3. EMERGENCY_SERVICES (Valid candidate safe site)
        InfrastructureAssetDto emg = new InfrastructureAssetDto();
        emg.setAssetId("FAC-EMG-003");
        emg.setAssetName("Sitamarhi Central Flood Shelter");
        emg.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
        emg.setSubType("flood_relief_shelter");
        emg.setDistrictName("Sitamarhi");
        emg.setLongitude(85.5030);
        emg.setLatitude(26.5950);
        emg.setDataProvenance("CONFIGURED_REGIONAL_FACILITIES");
        list.add(emg);

        // 4. HEALTHCARE (Valid candidate safe site)
        InfrastructureAssetDto med = new InfrastructureAssetDto();
        med.setAssetId("FAC-MED-007");
        med.setAssetName("Sitamarhi Sadar District Hospital");
        med.setCategory(InfrastructureCategory.HEALTHCARE);
        med.setSubType("district_hospital");
        med.setDistrictName("Sitamarhi");
        med.setLongitude(85.4980);
        med.setLatitude(26.5920);
        med.setDataProvenance("CONFIGURED_REGIONAL_FACILITIES");
        list.add(med);

        // 5. POWER (Unusable -> MUST BE FILTERED OUT)
        InfrastructureAssetDto pwr = new InfrastructureAssetDto();
        pwr.setAssetId("FAC-PWR-001");
        pwr.setAssetName("Barauni Thermal Power Station");
        pwr.setCategory(InfrastructureCategory.POWER);
        pwr.setSubType("thermal_power_plant");
        pwr.setDistrictName("Begusarai");
        pwr.setLongitude(86.0120);
        pwr.setLatitude(25.3950);
        list.add(pwr);

        // 6. TRANSPORT (Unusable -> MUST BE FILTERED OUT)
        InfrastructureAssetDto trn = new InfrastructureAssetDto();
        trn.setAssetId("FAC-TRN-001");
        trn.setAssetName("Mahatma Gandhi Setu (Ganga Bridge)");
        trn.setCategory(InfrastructureCategory.TRANSPORT);
        trn.setSubType("major_river_bridge");
        trn.setDistrictName("Patna");
        trn.setLongitude(85.2150);
        trn.setLatitude(25.6170);
        list.add(trn);

        // 7. WATER (Unusable -> MUST BE FILTERED OUT)
        InfrastructureAssetDto wtr = new InfrastructureAssetDto();
        wtr.setAssetId("INFRA-WATER-101");
        wtr.setAssetName("Triveni Canal Drainage Channel");
        wtr.setCategory(InfrastructureCategory.WATER);
        wtr.setSubType("canal");
        wtr.setDistrictName("West Champaran");
        wtr.setLongitude(84.1000);
        wtr.setLatitude(27.1000);
        list.add(wtr);

        return list;
    }

    // =========================================================================
    // 1. Candidate Category & Mapping Tests
    // =========================================================================

    @Nested
    @DisplayName("5.2.1: Candidate Category Mapping & Filtering")
    class CategoryMappingTests {

        @Test
        @DisplayName("InfrastructureCategory.EDUCATION maps to CandidateSiteCategory.EDUCATION")
        void testEducationMapping() {
            CandidateSiteCategory cat = CandidateSiteCategory.fromInfrastructureCategory(InfrastructureCategory.EDUCATION);
            assertEquals(CandidateSiteCategory.EDUCATION, cat);
            assertTrue(CandidateSiteCategory.isCandidateCategory(InfrastructureCategory.EDUCATION));
        }

        @Test
        @DisplayName("InfrastructureCategory.GOVERNMENT maps to CandidateSiteCategory.GOVERNMENT_BUILDING")
        void testGovernmentMapping() {
            CandidateSiteCategory cat = CandidateSiteCategory.fromInfrastructureCategory(InfrastructureCategory.GOVERNMENT);
            assertEquals(CandidateSiteCategory.GOVERNMENT_BUILDING, cat);
            assertTrue(CandidateSiteCategory.isCandidateCategory(InfrastructureCategory.GOVERNMENT));
        }

        @Test
        @DisplayName("InfrastructureCategory.EMERGENCY_SERVICES maps to CandidateSiteCategory.EMERGENCY_SHELTER")
        void testEmergencyServicesMapping() {
            CandidateSiteCategory cat = CandidateSiteCategory.fromInfrastructureCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            assertEquals(CandidateSiteCategory.EMERGENCY_SHELTER, cat);
            assertTrue(CandidateSiteCategory.isCandidateCategory(InfrastructureCategory.EMERGENCY_SERVICES));
        }

        @Test
        @DisplayName("InfrastructureCategory.HEALTHCARE maps to CandidateSiteCategory.HEALTHCARE")
        void testHealthcareMapping() {
            CandidateSiteCategory cat = CandidateSiteCategory.fromInfrastructureCategory(InfrastructureCategory.HEALTHCARE);
            assertEquals(CandidateSiteCategory.HEALTHCARE, cat);
            assertTrue(CandidateSiteCategory.isCandidateCategory(InfrastructureCategory.HEALTHCARE));
        }

        @Test
        @DisplayName("Unusable categories (POWER, TRANSPORT, WATER, OTHER_CRITICAL) return null")
        void testUnusableCategoriesExcluded() {
            assertNull(CandidateSiteCategory.fromInfrastructureCategory(InfrastructureCategory.POWER));
            assertNull(CandidateSiteCategory.fromInfrastructureCategory(InfrastructureCategory.TRANSPORT));
            assertNull(CandidateSiteCategory.fromInfrastructureCategory(InfrastructureCategory.WATER));
            assertNull(CandidateSiteCategory.fromInfrastructureCategory(InfrastructureCategory.OTHER_CRITICAL));
            assertNull(CandidateSiteCategory.fromInfrastructureCategory(null));

            assertFalse(CandidateSiteCategory.isCandidateCategory(InfrastructureCategory.POWER));
            assertFalse(CandidateSiteCategory.isCandidateCategory(InfrastructureCategory.TRANSPORT));
            assertFalse(CandidateSiteCategory.isCandidateCategory(InfrastructureCategory.WATER));
        }

        @Test
        @DisplayName("fromString resolves category names and common aliases")
        void testFromStringResolution() {
            assertEquals(CandidateSiteCategory.EDUCATION, CandidateSiteCategory.fromString("EDUCATION"));
            assertEquals(CandidateSiteCategory.EDUCATION, CandidateSiteCategory.fromString("school"));
            assertEquals(CandidateSiteCategory.GOVERNMENT_BUILDING, CandidateSiteCategory.fromString("GOVERNMENT_BUILDING"));
            assertEquals(CandidateSiteCategory.GOVERNMENT_BUILDING, CandidateSiteCategory.fromString("government"));
            assertEquals(CandidateSiteCategory.EMERGENCY_SHELTER, CandidateSiteCategory.fromString("EMERGENCY_SHELTER"));
            assertEquals(CandidateSiteCategory.EMERGENCY_SHELTER, CandidateSiteCategory.fromString("shelter"));
            assertEquals(CandidateSiteCategory.HEALTHCARE, CandidateSiteCategory.fromString("HEALTHCARE"));
            assertEquals(CandidateSiteCategory.HEALTHCARE, CandidateSiteCategory.fromString("hospital"));
            assertNull(CandidateSiteCategory.fromString("invalid_category_xyz"));
            assertNull(CandidateSiteCategory.fromString(null));
        }
    }

    // =========================================================================
    // 2. Candidate Safe Site DTO Tests
    // =========================================================================

    @Nested
    @DisplayName("5.2.2: CandidateSafeSiteDto Factory & Integrity")
    class CandidateDtoTests {

        @Test
        @DisplayName("CandidateSafeSiteDto preserves coordinates, identifiers, and metadata")
        void testDtoMappingPreservesData() {
            InfrastructureAssetDto asset = new InfrastructureAssetDto();
            asset.setAssetId("FAC-EMG-003");
            asset.setAssetName("Sitamarhi Central Flood Shelter");
            asset.setCategory(InfrastructureCategory.EMERGENCY_SERVICES);
            asset.setSubType("flood_relief_shelter");
            asset.setDistrictName("Sitamarhi");
            asset.setState("Bihar");
            asset.setLongitude(85.5030);
            asset.setLatitude(26.5950);
            asset.setDataProvenance("CONFIGURED_REGIONAL_FACILITIES");

            CandidateSafeSiteDto dto = CandidateSafeSiteDto.fromInfrastructureAsset(asset);

            assertNotNull(dto);
            assertEquals("FAC-EMG-003", dto.getSiteId());
            assertEquals("Sitamarhi Central Flood Shelter", dto.getSiteName());
            assertEquals(CandidateSiteCategory.EMERGENCY_SHELTER, dto.getCategory());
            assertEquals("Emergency Shelters & Relief Centers", dto.getCategoryDisplayName());
            assertEquals("flood_relief_shelter", dto.getSubType());
            assertEquals("Sitamarhi", dto.getDistrict());
            assertEquals("Bihar", dto.getState());
            assertEquals(85.5030, dto.getLongitude());
            assertEquals(26.5950, dto.getLatitude());
            assertNull(dto.getCapacity(), "Capacity must be null (not fabricated)");
            assertEquals("CANDIDATE", dto.getStatus());
            assertEquals("CONFIGURED_REGIONAL_FACILITIES", dto.getSource());
            assertEquals("#F44336", dto.getColorHex());
        }

        @Test
        @DisplayName("Excluded infrastructure asset returns null DTO")
        void testExcludedAssetReturnsNullDto() {
            InfrastructureAssetDto powerPlant = new InfrastructureAssetDto();
            powerPlant.setAssetId("FAC-PWR-001");
            powerPlant.setCategory(InfrastructureCategory.POWER);

            CandidateSafeSiteDto dto = CandidateSafeSiteDto.fromInfrastructureAsset(powerPlant);
            assertNull(dto, "Unusable category should not produce a candidate safe site");
        }

        @Test
        @DisplayName("Null asset returns null DTO")
        void testNullAssetReturnsNullDto() {
            assertNull(CandidateSafeSiteDto.fromInfrastructureAsset(null));
        }
    }

    // =========================================================================
    // 3. CandidateSafeSiteService Retrieval & Filtering Tests
    // =========================================================================

    @Nested
    @DisplayName("5.2.3: CandidateSafeSiteService Query & Filtering")
    class ServiceRetrievalTests {

        @Test
        @DisplayName("getAllCandidateSites retrieves and filters out power, transport, and water")
        void testGetAllCandidateSites() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            List<CandidateSafeSiteDto> candidates = candidateSafeSiteService.getAllCandidateSites();

            // Total 7 in mock: 4 valid (EDU, GOV, EMG, MED) and 3 invalid (PWR, TRN, WTR)
            assertEquals(4, candidates.size());
            assertTrue(candidates.stream().noneMatch(c -> c.getSiteId().contains("PWR")));
            assertTrue(candidates.stream().noneMatch(c -> c.getSiteId().contains("TRN")));
            assertTrue(candidates.stream().noneMatch(c -> c.getSiteId().contains("WATER")));
        }

        @Test
        @DisplayName("getCandidateSites filters by district (case-insensitive)")
        void testFilterByDistrict() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            List<CandidateSafeSiteDto> sitamarhiSites = candidateSafeSiteService.getCandidateSites("sitamarhi", null, false);
            assertEquals(3, sitamarhiSites.size());
            assertTrue(sitamarhiSites.stream().allMatch(s -> "Sitamarhi".equalsIgnoreCase(s.getDistrict())));

            List<CandidateSafeSiteDto> patnaSites = candidateSafeSiteService.getCandidateSites("PATNA", null, false);
            assertEquals(1, patnaSites.size());
            assertEquals("FAC-EDU-001", patnaSites.get(0).getSiteId());
        }

        @Test
        @DisplayName("getCandidateSites filters by category")
        void testFilterByCategory() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            List<CandidateSafeSiteDto> shelterSites = candidateSafeSiteService.getCandidateSites(null, "EMERGENCY_SHELTER", false);
            assertEquals(1, shelterSites.size());
            assertEquals("FAC-EMG-003", shelterSites.get(0).getSiteId());

            List<CandidateSafeSiteDto> eduSites = candidateSafeSiteService.getCandidateSites(null, "school", false);
            assertEquals(1, eduSites.size());
            assertEquals("FAC-EDU-001", eduSites.get(0).getSiteId());
        }

        @Test
        @DisplayName("getCandidateSites throws InvalidHazardParameterException for unknown category")
        void testInvalidCategoryThrowsException() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            assertThrows(InvalidHazardParameterException.class, () ->
                    candidateSafeSiteService.getCandidateSites(null, "INVALID_CAT", false)
            );
        }

        @Test
        @DisplayName("getCandidateSites with redZoneOnly=true filters by active Red-Zone districts")
        void testRedZoneCrossFiltering() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            // Mock Stage 5.1 RedZoneService: Sitamarhi is in Red Zone, Patna is not
            RedZoneDto sitamarhiRed = new RedZoneDto();
            sitamarhiRed.setDistrictName("Sitamarhi");
            sitamarhiRed.setZoneLevel(ZoneLevel.CRITICAL);
            sitamarhiRed.setRedZone(true);

            when(redZoneService.getRedZonesOnly()).thenReturn(List.of(sitamarhiRed));

            List<CandidateSafeSiteDto> redZoneSites = candidateSafeSiteService.getCandidateSites(null, null, true);

            assertEquals(3, redZoneSites.size());
            assertTrue(redZoneSites.stream().allMatch(s -> "Sitamarhi".equalsIgnoreCase(s.getDistrict())));
            verify(redZoneService, atLeastOnce()).getRedZonesOnly();
        }

        @Test
        @DisplayName("getCandidateSites returns empty list for district with no facilities safely")
        void testEmptyDistrictResults() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            List<CandidateSafeSiteDto> emptySites = candidateSafeSiteService.getCandidateSites("Araria", null, false);
            assertNotNull(emptySites);
            assertTrue(emptySites.isEmpty());
        }

        @Test
        @DisplayName("getCandidateSiteById retrieves matching facility")
        void testGetById() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            CandidateSafeSiteDto site = candidateSafeSiteService.getCandidateSiteById("FAC-EMG-003");
            assertNotNull(site);
            assertEquals("Sitamarhi Central Flood Shelter", site.getSiteName());
        }

        @Test
        @DisplayName("getCandidateSiteById throws HazardNotFoundException for non-existent ID")
        void testGetByIdNotFound() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            assertThrows(HazardNotFoundException.class, () ->
                    candidateSafeSiteService.getCandidateSiteById("NON-EXISTENT-999")
            );
        }

        @Test
        @DisplayName("getCandidateSiteById throws IllegalArgumentException for null/empty ID")
        void testGetByIdNullThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    candidateSafeSiteService.getCandidateSiteById(null)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    candidateSafeSiteService.getCandidateSiteById("   ")
            );
        }
    }

    // =========================================================================
    // 4. GeoJSON Output Tests
    // =========================================================================

    @Nested
    @DisplayName("5.2.4: GeoJSON Generation & Coordinates Preservation")
    class GeoJsonTests {

        @Test
        @DisplayName("generateCandidateSitesGeoJson produces RFC 7946 compliant FeatureCollection")
        void testGeoJsonOutput() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson(null, null, false);

            assertNotNull(geojson);
            assertEquals("FeatureCollection", geojson.getType());
            assertEquals(4, geojson.getCount());
            assertEquals(4, geojson.getFeatures().size());

            var firstFeature = geojson.getFeatures().get(0);
            assertNotNull(firstFeature.getId());
            assertTrue(firstFeature.getId().startsWith("SAFE-SITE-"));
            assertNotNull(firstFeature.getGeometry());
            assertEquals("Point", firstFeature.getGeometry().getType());

            var props = firstFeature.getProperties();
            assertNotNull(props.get("siteId"));
            assertNotNull(props.get("siteName"));
            assertNotNull(props.get("category"));
            assertNotNull(props.get("categoryDisplayName"));
            assertNotNull(props.get("district"));
            assertEquals("CANDIDATE", props.get("status"));
            assertEquals("CANDIDATE_SAFE_SITES", props.get("layerId"));
            assertNotNull(props.get("colorHex"));
        }

        @Test
        @DisplayName("GeoJSON generation respects district and category filters")
        void testGeoJsonFiltering() {
            when(dataProvider.getAllRegionalFacilities()).thenReturn(createMockFacilities());

            GeoJsonFeatureCollectionDto geojson = candidateSafeSiteService.generateCandidateSitesGeoJson("Sitamarhi", "GOVERNMENT", false);

            assertEquals(1, geojson.getCount());
            assertEquals("FAC-GOV-003", geojson.getFeatures().get(0).getProperties().get("siteId"));
        }
    }
}
