package com.hazard;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.population.OsmSettlement;
import com.hazard.domain.population.PopulatedPlace;
import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.risk.ZoneLevel;
import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.exposure.SettlementExposureDto;
import com.hazard.dto.relocation.*;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.InvalidHazardParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for Stage 6.1 Relocation Intelligence Data Models, Enums, and DTOs.
 */
class RelocationDataModelTests {

    private static final GeometryFactory GF = new GeometryFactory();

    @Nested
    @DisplayName("6.1.1: RelocationStatus Enum Tests")
    class RelocationStatusEnumTests {

        @Test
        @DisplayName("RelocationStatus boolean helpers return expected values")
        void testEnumBooleans() {
            assertTrue(RelocationStatus.ALLOCATED.isAllocated());
            assertTrue(RelocationStatus.ALLOCATED.isAnyAllocated());
            assertFalse(RelocationStatus.ALLOCATED.isPartiallyAllocated());
            assertFalse(RelocationStatus.ALLOCATED.isUnallocated());
            assertFalse(RelocationStatus.ALLOCATED.isPending());

            assertTrue(RelocationStatus.PARTIALLY_ALLOCATED.isPartiallyAllocated());
            assertTrue(RelocationStatus.PARTIALLY_ALLOCATED.isAnyAllocated());
            assertFalse(RelocationStatus.PARTIALLY_ALLOCATED.isAllocated());
            assertFalse(RelocationStatus.PARTIALLY_ALLOCATED.isUnallocated());

            assertTrue(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED.isUnallocated());
            assertFalse(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED.isAnyAllocated());

            assertTrue(RelocationStatus.UNALLOCATED_NO_SAFE_SITE.isUnallocated());
            assertFalse(RelocationStatus.UNALLOCATED_NO_SAFE_SITE.isAnyAllocated());

            assertTrue(RelocationStatus.PENDING.isPending());
            assertFalse(RelocationStatus.PENDING.isAnyAllocated());
        }

        @Test
        @DisplayName("fromString parses standard names and aliases case-insensitively")
        void testFromStringAndAliases() {
            assertEquals(RelocationStatus.ALLOCATED, RelocationStatus.fromString("ALLOCATED"));
            assertEquals(RelocationStatus.ALLOCATED, RelocationStatus.fromString("allocated"));
            assertEquals(RelocationStatus.ALLOCATED, RelocationStatus.fromString("FULLY_ALLOCATED"));
            assertEquals(RelocationStatus.ALLOCATED, RelocationStatus.fromString("FULL"));
            assertEquals(RelocationStatus.ALLOCATED, RelocationStatus.fromString("SUCCESS"));

            assertEquals(RelocationStatus.PARTIALLY_ALLOCATED, RelocationStatus.fromString("PARTIALLY_ALLOCATED"));
            assertEquals(RelocationStatus.PARTIALLY_ALLOCATED, RelocationStatus.fromString("partial"));

            assertEquals(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED, RelocationStatus.fromString("UNALLOCATED_CAPACITY_EXCEEDED"));
            assertEquals(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED, RelocationStatus.fromString("OVER_CAPACITY"));
            assertEquals(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED, RelocationStatus.fromString("capacity-exceeded"));

            assertEquals(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, RelocationStatus.fromString("UNALLOCATED_NO_SAFE_SITE"));
            assertEquals(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, RelocationStatus.fromString("NO_SITE"));

            assertEquals(RelocationStatus.PENDING, RelocationStatus.fromString("PENDING"));
            assertNull(RelocationStatus.fromString(null));
            assertNull(RelocationStatus.fromString("   "));
        }

        @Test
        @DisplayName("fromString throws InvalidHazardParameterException for unknown status")
        void testInvalidThrows() {
            assertThrows(InvalidHazardParameterException.class, () -> RelocationStatus.fromString("INVALID_STATUS"));
        }
    }

    @Nested
    @DisplayName("6.1.2: RelocationUrgency Enum Tests")
    class RelocationUrgencyEnumTests {

        @Test
        @DisplayName("Priority levels and boolean helpers operate correctly")
        void testEnumPriorities() {
            assertEquals(1, RelocationUrgency.CRITICAL.getPriorityLevel());
            assertEquals(2, RelocationUrgency.HIGH.getPriorityLevel());
            assertEquals(3, RelocationUrgency.MODERATE.getPriorityLevel());
            assertEquals(4, RelocationUrgency.LOW.getPriorityLevel());

            assertTrue(RelocationUrgency.CRITICAL.isCritical());
            assertTrue(RelocationUrgency.CRITICAL.isHighOrCritical());
            assertFalse(RelocationUrgency.HIGH.isCritical());
            assertTrue(RelocationUrgency.HIGH.isHighOrCritical());
            assertFalse(RelocationUrgency.MODERATE.isHighOrCritical());
            assertFalse(RelocationUrgency.LOW.isHighOrCritical());
        }

        @Test
        @DisplayName("Derivation from RiskTier correctly prioritizes high risk tiers")
        void testFromRiskTier() {
            assertEquals(RelocationUrgency.CRITICAL, RelocationUrgency.fromRiskTier(RiskTier.CRITICAL));
            assertEquals(RelocationUrgency.CRITICAL, RelocationUrgency.fromRiskTier(RiskTier.VERY_HIGH));
            assertEquals(RelocationUrgency.HIGH, RelocationUrgency.fromRiskTier(RiskTier.HIGH));
            assertEquals(RelocationUrgency.MODERATE, RelocationUrgency.fromRiskTier(RiskTier.MODERATE));
            assertEquals(RelocationUrgency.LOW, RelocationUrgency.fromRiskTier(RiskTier.LOW));
            assertEquals(RelocationUrgency.MODERATE, RelocationUrgency.fromRiskTier(null));
        }

        @Test
        @DisplayName("Derivation from ZoneLevel correctly prioritizes Red Zones")
        void testFromZoneLevel() {
            assertEquals(RelocationUrgency.CRITICAL, RelocationUrgency.fromZoneLevel(ZoneLevel.CRITICAL));
            assertEquals(RelocationUrgency.HIGH, RelocationUrgency.fromZoneLevel(ZoneLevel.HIGH));
            assertEquals(RelocationUrgency.MODERATE, RelocationUrgency.fromZoneLevel(ZoneLevel.MODERATE));
            assertEquals(RelocationUrgency.LOW, RelocationUrgency.fromZoneLevel(ZoneLevel.LOW));
            assertEquals(RelocationUrgency.MODERATE, RelocationUrgency.fromZoneLevel(ZoneLevel.UNKNOWN));
            assertEquals(RelocationUrgency.MODERATE, RelocationUrgency.fromZoneLevel(null));
        }

        @Test
        @DisplayName("fromString parses case-insensitively and throws on invalid")
        void testFromString() {
            assertEquals(RelocationUrgency.CRITICAL, RelocationUrgency.fromString("CRITICAL"));
            assertEquals(RelocationUrgency.HIGH, RelocationUrgency.fromString("high"));
            assertNull(RelocationUrgency.fromString(null));
            assertThrows(InvalidHazardParameterException.class, () -> RelocationUrgency.fromString("EXTREME"));
        }
    }

    @Nested
    @DisplayName("6.1.3: CandidateSafeSite Capacity State Tests")
    class CandidateSafeSiteCapacityTests {

        @Test
        @DisplayName("Raw candidate site defaults to null capacity and unbounded availability")
        void testDefaultNullCapacity() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            assertNull(site.getCapacity());
            assertNull(site.getAvailableCapacity());
            assertFalse(site.isFull());
            assertTrue(site.hasAvailableCapacity());
            assertEquals(0.0, site.getOccupancyRate());
            assertEquals(0.0, site.getOccupancyPercentage());
        }

        @Test
        @DisplayName("Explicit capacity and occupancy calculations behave correctly")
        void testExplicitCapacityAndOccupancy() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setCapacity(500);
            site.setAllocatedOccupancy(200);

            assertEquals(500, site.getCapacity());
            assertEquals(200, site.getAllocatedOccupancy());
            assertEquals(300, site.getAvailableCapacity());
            assertEquals(0.40, site.getOccupancyRate(), 0.001);
            assertEquals(40.0, site.getOccupancyPercentage(), 0.001);
            assertFalse(site.isFull());
            assertTrue(site.hasAvailableCapacity());
        }

        @Test
        @DisplayName("Site reaches full capacity when allocatedOccupancy equals or exceeds capacity")
        void testFullCapacityAndAvailabilityHelpers() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setCapacity(300);
            site.setAllocatedOccupancy(300);

            assertEquals(0, site.getAvailableCapacity());
            assertTrue(site.isFull());
            assertFalse(site.hasAvailableCapacity());
            assertEquals(1.0, site.getOccupancyRate(), 0.001);
            assertEquals(100.0, site.getOccupancyPercentage(), 0.001);

            // Over-allocated case clamps available capacity at 0
            site.setAllocatedOccupancy(350);
            assertEquals(0, site.getAvailableCapacity());
            assertTrue(site.isFull());
            assertFalse(site.hasAvailableCapacity());
        }
    }

    @Nested
    @DisplayName("6.1.4: SiteCapacityDto Tests")
    class SiteCapacityDtoTests {

        @Test
        @DisplayName("SiteCapacityDto recalculates available capacity and occupancy rate")
        void testRecalculate() {
            SiteCapacityDto cap = new SiteCapacityDto("FAC-001", "Patna College", "Patna", CandidateSiteCategory.EDUCATION, 1000);
            assertEquals(1000, cap.getAvailableCapacity());
            assertFalse(cap.isFull());
            assertTrue(cap.hasAvailableCapacity());

            cap.setAllocatedOccupancy(250);
            assertEquals(750, cap.getAvailableCapacity());
            assertEquals(0.25, cap.getOccupancyRate(), 0.001);
            assertEquals(25.0, cap.getOccupancyPercentage(), 0.001);
        }

        @Test
        @DisplayName("allocate method increments occupancy and returns actually accommodated count")
        void testAllocatePeople() {
            SiteCapacityDto cap = new SiteCapacityDto("FAC-002", "Relief Hall", "Sitamarhi", CandidateSiteCategory.EMERGENCY_SHELTER, 200);

            int firstAllocated = cap.allocate(150);
            assertEquals(150, firstAllocated);
            assertEquals(150, cap.getAllocatedOccupancy());
            assertEquals(50, cap.getAvailableCapacity());

            // Allocate more than remaining capacity (100 requested, only 50 available)
            int secondAllocated = cap.allocate(100);
            assertEquals(50, secondAllocated);
            assertEquals(200, cap.getAllocatedOccupancy());
            assertEquals(0, cap.getAvailableCapacity());
            assertTrue(cap.isFull());
            assertFalse(cap.hasAvailableCapacity());

            // Further allocation returns 0
            assertEquals(0, cap.allocate(50));
        }

        @Test
        @DisplayName("fromCandidateSafeSite creates a synchronized SiteCapacityDto")
        void testFromCandidateSafeSite() {
            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-HLT-01");
            site.setSiteName("District Hospital");
            site.setDistrict("Gaya");
            site.setCategory(CandidateSiteCategory.HEALTHCARE);
            site.setCapacity(400);
            site.setAllocatedOccupancy(100);

            SiteCapacityDto capDto = SiteCapacityDto.fromCandidateSafeSite(site);
            assertNotNull(capDto);
            assertEquals("FAC-HLT-01", capDto.getSiteId());
            assertEquals("District Hospital", capDto.getSiteName());
            assertEquals("Gaya", capDto.getDistrict());
            assertEquals(CandidateSiteCategory.HEALTHCARE, capDto.getCategory());
            assertEquals(400, capDto.getTotalCapacity());
            assertEquals(100, capDto.getAllocatedOccupancy());
            assertEquals(300, capDto.getAvailableCapacity());
            assertEquals(25.0, capDto.getOccupancyPercentage(), 0.001);
        }
    }

    @Nested
    @DisplayName("6.1.5: VulnerableHabitationDto Factory & Mapping Tests")
    class VulnerableHabitationDtoTests {

        @Test
        @DisplayName("Factory from SettlementExposureDto maps coordinates, population, and hazard context")
        void testFromSettlementExposure() {
            SettlementExposureDto exp = new SettlementExposureDto();
            exp.setSettlementId(42);
            exp.setSettlementName("Rampur Village");
            exp.setSettlementType("village");
            exp.setDistrictName("Sitamarhi");
            exp.setState("Bihar");
            exp.setLatitude(26.5950);
            exp.setLongitude(85.5030);
            exp.setTotalPopulation(1500L);
            exp.setEstimatedPopulation(false);
            exp.setHazardIdentifier("DFO-001");
            exp.setHazardType("FLOOD");
            exp.setHazardSeverityScore(0.85);
            exp.setSettlementExposureScore(0.78);
            exp.setExposureCategory(ExposureCategory.VERY_HIGH);

            VulnerableHabitationDto hab = VulnerableHabitationDto.fromSettlementExposure(exp);

            assertNotNull(hab);
            assertEquals("HAB-42", hab.getHabitationId());
            assertEquals("Rampur Village", hab.getHabitationName());
            assertEquals("village", hab.getHabitationType());
            assertEquals("Sitamarhi", hab.getDistrict());
            assertEquals(26.5950, hab.getLatitude());
            assertEquals(85.5030, hab.getLongitude());
            assertEquals(1500L, hab.getTotalPopulation());
            assertEquals(1500L, hab.getVulnerablePopulation());
            assertEquals(RelocationUrgency.CRITICAL, hab.getUrgency());
            assertEquals(RelocationStatus.PENDING, hab.getRelocationStatus());
            assertEquals("FLOOD", hab.getHazardType());
            assertEquals(0.85, hab.getHazardSeverityScore());
        }

        @Test
        @DisplayName("Factory from PopulatedPlace maps centroid coordinates and administrative metadata")
        void testFromPopulatedPlace() {
            PopulatedPlace place = new PopulatedPlace();
            place.setId(101);
            place.setName("Muzaffarpur Ward 4");
            place.setPlace("town");
            place.setAdm2Name("Muzaffarpur");
            place.setAdm1Name("Bihar");
            place.setPopulation(3200L);

            Point pt = GF.createPoint(new Coordinate(85.3620, 26.1150));
            place.setGeom(pt);

            VulnerableHabitationDto hab = VulnerableHabitationDto.fromPopulatedPlace(place);

            assertNotNull(hab);
            assertEquals("HAB-PP-101", hab.getHabitationId());
            assertEquals("Muzaffarpur Ward 4", hab.getHabitationName());
            assertEquals("town", hab.getHabitationType());
            assertEquals("Muzaffarpur", hab.getDistrict());
            assertEquals(26.1150, hab.getLatitude(), 0.0001);
            assertEquals(85.3620, hab.getLongitude(), 0.0001);
            assertEquals(3200L, hab.getTotalPopulation());
            assertEquals(3200L, hab.getVulnerablePopulation());
        }

        @Test
        @DisplayName("Factory from OsmSettlement maps point coordinates and settlement details")
        void testFromOsmSettlement() {
            OsmSettlement settlement = new OsmSettlement();
            settlement.setId(202);
            settlement.setName("Belaganj");
            settlement.setPlace("village");
            settlement.setPopulation(850L);

            Point pt = GF.createPoint(new Coordinate(84.9800, 24.9500));
            settlement.setGeom(pt);

            VulnerableHabitationDto hab = VulnerableHabitationDto.fromOsmSettlement(settlement);

            assertNotNull(hab);
            assertEquals("HAB-OSM-202", hab.getHabitationId());
            assertEquals("Belaganj", hab.getHabitationName());
            assertEquals("village", hab.getHabitationType());
            assertEquals(24.9500, hab.getLatitude(), 0.0001);
            assertEquals(84.9800, hab.getLongitude(), 0.0001);
            assertEquals(850L, hab.getTotalPopulation());
            assertEquals(850L, hab.getVulnerablePopulation());
        }
    }

    @Nested
    @DisplayName("6.1.6: RelocationAssignmentDto Tests")
    class RelocationAssignmentDtoTests {

        @Test
        @DisplayName("RelocationAssignmentDto initializes pair metrics and transit distance")
        void testConstructorAndTransitDistances() {
            VulnerableHabitationDto hab = new VulnerableHabitationDto();
            hab.setHabitationId("HAB-1");
            hab.setHabitationName("Flooded Basti");
            hab.setDistrict("Sitamarhi");
            hab.setLatitude(26.5950);
            hab.setLongitude(85.5030);
            hab.setVulnerablePopulation(350L);
            hab.setUrgency(RelocationUrgency.CRITICAL);

            CandidateSafeSiteDto site = new CandidateSafeSiteDto();
            site.setSiteId("FAC-EMG-01");
            site.setSiteName("Central Relief Shelter");
            site.setCategory(CandidateSiteCategory.EMERGENCY_SHELTER);
            site.setDistrict("Sitamarhi");
            site.setLatitude(26.6500);
            site.setLongitude(85.5200);
            site.setSuitabilityClass(SuitabilityClass.HIGHLY_SUITABLE);
            site.setSuitabilityScore(94.5);
            site.setRank(1);

            RelocationAssignmentDto assignment = new RelocationAssignmentDto(
                    hab, site, 350L, 6350.0, RelocationStatus.ALLOCATED, "Optimal nearest suitable relief shelter with available capacity"
            );

            assertNotNull(assignment);
            assertEquals("ASN-HAB-1-FAC-EMG-01", assignment.getAssignmentId());
            assertEquals("HAB-1", assignment.getHabitationId());
            assertEquals("Flooded Basti", assignment.getHabitationName());
            assertEquals("FAC-EMG-01", assignment.getDestinationSiteId());
            assertEquals("Central Relief Shelter", assignment.getDestinationSiteName());
            assertEquals(350L, assignment.getAllocatedPopulation());
            assertEquals(0L, assignment.getUnallocatedPopulation());
            assertEquals(6350.0, assignment.getTransitDistanceMeters());
            assertEquals(6.35, assignment.getTransitDistanceKilometers());
            assertEquals(RelocationStatus.ALLOCATED, assignment.getStatus());
            assertEquals(RelocationUrgency.CRITICAL, assignment.getUrgency());
        }
    }

    @Nested
    @DisplayName("6.1.7: RelocationPlanDto Aggregation Tests")
    class RelocationPlanDtoTests {

        @Test
        @DisplayName("recomputeTotals correctly calculates population, allocation percentage, and capacity usage")
        void testRecomputeTotals() {
            RelocationPlanDto plan = new RelocationPlanDto();
            plan.setPlanId("PLAN-SITAMARHI-001");
            plan.setDistrict("Sitamarhi");
            plan.setTotalCapacityAvailable(2000);
            plan.setTotalCapacityUtilized(1200);

            RelocationAssignmentDto a1 = new RelocationAssignmentDto();
            a1.setAllocatedPopulation(500L);

            RelocationAssignmentDto a2 = new RelocationAssignmentDto();
            a2.setAllocatedPopulation(700L);

            VulnerableHabitationDto unalloc = new VulnerableHabitationDto();
            unalloc.setVulnerablePopulation(300L);

            plan.setAssignments(List.of(a1, a2));
            plan.setUnallocatedHabitations(List.of(unalloc));

            plan.recomputeTotals();

            assertEquals(3, plan.getTotalHabitations());
            assertEquals(1200L, plan.getTotalAllocatedPopulation());
            assertEquals(300L, plan.getTotalUnallocatedPopulation());
            assertEquals(1500L, plan.getTotalVulnerablePopulation());
            assertEquals(80.0, plan.getAllocationRatePercentage(), 0.01);
            assertEquals(60.0, plan.getCapacityUtilizationPercentage(), 0.01);
        }
    }

    @Nested
    @DisplayName("6.1.8: RelocationRequestDto Configuration Tests")
    class RelocationRequestDtoTests {

        @Test
        @DisplayName("Default parameters adhere to expected emergency transit standards")
        void testDefaults() {
            RelocationRequestDto req = new RelocationRequestDto();
            assertEquals(25.0, req.getMaxTransitDistanceKm());
            assertEquals(SuitabilityClass.MARGINAL, req.getMinSuitabilityClass());
            assertTrue(req.isPrioritizeRedZones());
            assertEquals(500, req.getDefaultSiteCapacity());
            assertEquals("NEAREST_SUITABLE", req.getAllocationStrategy());
        }

        @Test
        @DisplayName("Parameterized constructor overrides defaults safely")
        void testCustomValues() {
            RelocationRequestDto req = new RelocationRequestDto("Sitamarhi", "FLOOD", 15.0);
            assertEquals("Sitamarhi", req.getDistrict());
            assertEquals("FLOOD", req.getHazardType());
            assertEquals(15.0, req.getMaxTransitDistanceKm());
        }
    }
}
