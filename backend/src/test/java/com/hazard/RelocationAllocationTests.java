package com.hazard;

import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.*;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.relocation.RelocationAllocationService;
import com.hazard.service.relocation.RelocationFeasibilityService;
import com.hazard.service.relocation.RelocationRankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for Stage 6.5 — Relocation Intelligence: Capacity-Aware Allocation.
 */
class RelocationAllocationTests {

    private RelocationFeasibilityService feasibilityService;
    private RelocationRankingService rankingService;
    private RelocationAllocationService allocationService;

    @BeforeEach
    void setUp() {
        feasibilityService = new RelocationFeasibilityService();
        rankingService = new RelocationRankingService(feasibilityService);
        allocationService = new RelocationAllocationService(rankingService);
    }

    private VulnerableHabitationDto createHabitation(String id, String name, double lat, double lon, long pop) {
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId(id);
        hab.setHabitationName(name);
        hab.setDistrict("Sitamarhi");
        hab.setState("Bihar");
        hab.setLatitude(lat);
        hab.setLongitude(lon);
        hab.setVulnerablePopulation(pop);
        hab.setTotalPopulation(pop);
        hab.setUrgency(RelocationUrgency.CRITICAL);
        return hab;
    }

    private RankedRelocationSiteDto createRankedSite(int rank, String id, String name,
                                                     SuitabilityClass suitability, double score,
                                                     double distanceMeters, Integer capacity, Integer occupied) {
        SiteFeasibilityEvaluationDto eval = new SiteFeasibilityEvaluationDto();
        eval.setSiteId(id);
        eval.setSiteName(name);
        eval.setCategory("EMERGENCY_SHELTER");
        eval.setDistrict("Sitamarhi");
        eval.setDestinationLatitude(26.6500);
        eval.setDestinationLongitude(85.5200);
        eval.setSuitabilityClass(suitability);
        eval.setSuitabilityScore(score);
        eval.setTransitDistanceMeters(distanceMeters);
        eval.setDistanceAvailable(true);
        eval.setTotalCapacity(capacity);
        eval.setAllocatedOccupancy(occupied != null ? occupied : 0);
        eval.setAvailableCapacity(capacity != null ? Math.max(0, capacity - (occupied != null ? occupied : 0)) : null);

        RankedRelocationSiteDto ranked = new RankedRelocationSiteDto(rank, eval, 10);
        ranked.setRank(rank);
        return ranked;
    }

    @Test
    @DisplayName("1. Population fits entirely in first-ranked site -> 100% ALLOCATED")
    void testPopulationFitsEntirelyInFirstSite() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 100);

        RankedRelocationSiteDto site1 = createRankedSite(1, "FAC-001", "Primary Shelter",
                SuitabilityClass.HIGHLY_SUITABLE, 95.0, 5000.0, 500, 0); // Available: 500
        RankedRelocationSiteDto site2 = createRankedSite(2, "FAC-002", "Secondary Shelter",
                SuitabilityClass.SUITABLE, 80.0, 8000.0, 300, 0); // Available: 300

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(site1, site2));

        assertNotNull(plan);
        assertEquals(100L, plan.getTotalVulnerablePopulation());
        assertEquals(100L, plan.getTotalAllocatedPopulation());
        assertEquals(0L, plan.getTotalUnallocatedPopulation());
        assertEquals(100.0, plan.getAllocationRatePercentage());
        assertEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
        assertEquals(1, plan.getAssignments().size());

        RelocationAssignmentDto assign1 = plan.getAssignments().get(0);
        assertEquals("FAC-001", assign1.getDestinationSiteId());
        assertEquals(100L, assign1.getAllocatedPopulation());
        assertEquals(0L, assign1.getUnallocatedPopulation());
        assertEquals(RelocationStatus.ALLOCATED, assign1.getStatus());
        assertTrue(plan.getUnallocatedHabitations().isEmpty());
    }

    @Test
    @DisplayName("2. Population is split across multiple sites when first site fills to capacity")
    void testPopulationSplitAcrossMultipleSites() {
        // Population = 150
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 150);

        // Site A has available capacity = 100
        RankedRelocationSiteDto siteA = createRankedSite(1, "FAC-A", "Site A",
                SuitabilityClass.HIGHLY_SUITABLE, 95.0, 4000.0, 100, 0);
        // Site B has available capacity = 80
        RankedRelocationSiteDto siteB = createRankedSite(2, "FAC-B", "Site B",
                SuitabilityClass.SUITABLE, 80.0, 7000.0, 80, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(siteA, siteB));

        assertEquals(150L, plan.getTotalVulnerablePopulation());
        assertEquals(150L, plan.getTotalAllocatedPopulation());
        assertEquals(0L, plan.getTotalUnallocatedPopulation());
        assertEquals(100.0, plan.getAllocationRatePercentage());
        assertEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
        assertEquals(2, plan.getAssignments().size());

        // Site A gets 100 (full)
        RelocationAssignmentDto assignA = plan.getAssignments().get(0);
        assertEquals("FAC-A", assignA.getDestinationSiteId());
        assertEquals(100L, assignA.getAllocatedPopulation());

        // Site B gets remaining 50
        RelocationAssignmentDto assignB = plan.getAssignments().get(1);
        assertEquals("FAC-B", assignB.getDestinationSiteId());
        assertEquals(50L, assignB.getAllocatedPopulation());

        assertTrue(plan.getUnallocatedHabitations().isEmpty());
    }

    @Test
    @DisplayName("3. First site partially fills and remainder goes to next site")
    void testFirstSitePartiallyFillsRemainderToNext() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Flood Area", 26.5950, 85.5030, 120);

        // Site 1: Capacity 100, Occupied 30 -> Available 70
        RankedRelocationSiteDto site1 = createRankedSite(1, "FAC-1", "Semi-Full Shelter",
                SuitabilityClass.HIGHLY_SUITABLE, 92.0, 3000.0, 100, 30);
        // Site 2: Available 200
        RankedRelocationSiteDto site2 = createRankedSite(2, "FAC-2", "Open Hall",
                SuitabilityClass.SUITABLE, 85.0, 5000.0, 200, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(site1, site2));

        assertEquals(120L, plan.getTotalAllocatedPopulation());
        assertEquals(0L, plan.getTotalUnallocatedPopulation());
        assertEquals(2, plan.getAssignments().size());
        assertEquals(70L, plan.getAssignments().get(0).getAllocatedPopulation());
        assertEquals(50L, plan.getAssignments().get(1).getAllocatedPopulation());
        assertEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
    }

    @Test
    @DisplayName("4. Total capacity is insufficient -> correct unallocated deficit recorded")
    void testTotalCapacityInsufficientDeficitRecorded() {
        // Population = 300
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Large Cluster", 26.5950, 85.5030, 300);

        // Site 1: Available 100
        RankedRelocationSiteDto s1 = createRankedSite(1, "FAC-1", "S1", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 4000.0, 100, 0);
        // Site 2: Available 80
        RankedRelocationSiteDto s2 = createRankedSite(2, "FAC-2", "S2", SuitabilityClass.SUITABLE, 80.0, 6000.0, 80, 0);
        // Total available = 180 (< 300)

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1, s2));

        assertEquals(300L, plan.getTotalVulnerablePopulation());
        assertEquals(180L, plan.getTotalAllocatedPopulation());
        assertEquals(120L, plan.getTotalUnallocatedPopulation());
        assertEquals(60.0, plan.getAllocationRatePercentage(), 0.01);
        assertEquals(RelocationStatus.PARTIALLY_ALLOCATED, plan.getOverallStatus());

        // Check deficit record
        assertEquals(1, plan.getUnallocatedHabitations().size());
        VulnerableHabitationDto deficit = plan.getUnallocatedHabitations().get(0);
        assertEquals(120L, deficit.getVulnerablePopulation());
        assertEquals(RelocationStatus.PARTIALLY_ALLOCATED, deficit.getRelocationStatus());
        assertTrue(deficit.getStatusReason().contains("Capacity deficit"));
    }

    @Test
    @DisplayName("5. Zero-capacity site is skipped without receiving allocation")
    void testZeroCapacitySiteSkipped() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 80);

        // Site 1: Full (Capacity 200, Occupied 200 -> Available 0)
        RankedRelocationSiteDto s1 = createRankedSite(1, "FAC-FULL", "Full Hall",
                SuitabilityClass.HIGHLY_SUITABLE, 98.0, 2000.0, 200, 200);

        // Site 2: Available 150
        RankedRelocationSiteDto s2 = createRankedSite(2, "FAC-AVAIL", "Available Hall",
                SuitabilityClass.SUITABLE, 80.0, 5000.0, 150, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1, s2));

        assertEquals(80L, plan.getTotalAllocatedPopulation());
        assertEquals(1, plan.getAssignments().size());
        assertEquals("FAC-AVAIL", plan.getAssignments().get(0).getDestinationSiteId());
        assertEquals(80L, plan.getAssignments().get(0).getAllocatedPopulation());
        assertEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
    }

    @Test
    @DisplayName("6. Exact capacity match accommodates entire population")
    void testExactCapacityMatch() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 150);

        RankedRelocationSiteDto s1 = createRankedSite(1, "FAC-EXACT", "Exact Hall",
                SuitabilityClass.HIGHLY_SUITABLE, 95.0, 5000.0, 150, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1));

        assertEquals(150L, plan.getTotalAllocatedPopulation());
        assertEquals(0L, plan.getTotalUnallocatedPopulation());
        assertEquals(100.0, plan.getAllocationRatePercentage());
        assertEquals(100.0, plan.getCapacityUtilizationPercentage());
        assertEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
    }

    @Test
    @DisplayName("7. Empty ranked sites list -> UNALLOCATED_NO_SAFE_SITE")
    void testEmptyRankedSitesList() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 200);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, Collections.emptyList());

        assertEquals(200L, plan.getTotalVulnerablePopulation());
        assertEquals(0L, plan.getTotalAllocatedPopulation());
        assertEquals(200L, plan.getTotalUnallocatedPopulation());
        assertEquals(0.0, plan.getAllocationRatePercentage());
        assertEquals(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, plan.getOverallStatus());
        assertTrue(plan.getAssignments().isEmpty());
        assertEquals(1, plan.getUnallocatedHabitations().size());
        assertEquals(200L, plan.getUnallocatedHabitations().get(0).getVulnerablePopulation());
    }

    @Test
    @DisplayName("8. Zero or negative population requires no allocation")
    void testZeroOrNegativePopulation() {
        VulnerableHabitationDto zeroHab = createHabitation("HAB-000", "Empty Habitation", 26.5950, 85.5030, 0);
        RankedRelocationSiteDto site = createRankedSite(1, "FAC-1", "S1", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 5000.0, 500, 0);

        RelocationPlanDto zeroPlan = allocationService.allocatePopulation(zeroHab, List.of(site));
        assertEquals(0L, zeroPlan.getTotalAllocatedPopulation());
        assertEquals(0L, zeroPlan.getTotalUnallocatedPopulation());
        assertEquals(RelocationStatus.ALLOCATED, zeroPlan.getOverallStatus());
        assertTrue(zeroPlan.getAssignments().isEmpty());

        VulnerableHabitationDto negHab = createHabitation("HAB-NEG", "Negative Pop", 26.5950, 85.5030, -50);
        RelocationPlanDto negPlan = allocationService.allocatePopulation(negHab, List.of(site));
        assertEquals(0L, negPlan.getTotalAllocatedPopulation());
        assertEquals(RelocationStatus.ALLOCATED, negPlan.getOverallStatus());
    }

    @Test
    @DisplayName("9. Allocation never exceeds individual site available capacity")
    void testAllocationNeverExceedsSiteCapacity() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Large Pop", 26.5950, 85.5030, 500);

        RankedRelocationSiteDto s1 = createRankedSite(1, "FAC-1", "S1", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 3000.0, 100, 0);
        RankedRelocationSiteDto s2 = createRankedSite(2, "FAC-2", "S2", SuitabilityClass.SUITABLE, 85.0, 4000.0, 150, 0);
        RankedRelocationSiteDto s3 = createRankedSite(3, "FAC-3", "S3", SuitabilityClass.MARGINAL, 60.0, 5000.0, 75, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1, s2, s3));

        for (RelocationAssignmentDto a : plan.getAssignments()) {
            if ("FAC-1".equals(a.getDestinationSiteId())) {
                assertTrue(a.getAllocatedPopulation() <= 100L);
            } else if ("FAC-2".equals(a.getDestinationSiteId())) {
                assertTrue(a.getAllocatedPopulation() <= 150L);
            } else if ("FAC-3".equals(a.getDestinationSiteId())) {
                assertTrue(a.getAllocatedPopulation() <= 75L);
            }
        }
    }

    @Test
    @DisplayName("10. Original site capacity and occupancy are protected and remain unchanged")
    void testOriginalSiteStateProtected() {
        RankedRelocationSiteDto site = createRankedSite(1, "FAC-ORIG", "Original Site",
                SuitabilityClass.HIGHLY_SUITABLE, 95.0, 5000.0, 400, 100);

        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur", 26.5950, 85.5030, 250);

        allocationService.allocatePopulation(hab, List.of(site));

        // Verify that original RankedRelocationSiteDto capacity/occupancy values were NOT mutated
        assertEquals(400, site.getTotalCapacity(), "Total capacity must not change");
        assertEquals(100, site.getAllocatedOccupancy(), "Allocated occupancy on source DTO must not change");
        assertEquals(300, site.getAvailableCapacity(), "Available capacity on source DTO must not change");
    }

    @Test
    @DisplayName("11. Determinism: same inputs always produce exact same allocation result")
    void testAllocationIsDeterministic() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur", 26.5950, 85.5030, 175);
        RankedRelocationSiteDto s1 = createRankedSite(1, "FAC-1", "S1", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 3000.0, 100, 0);
        RankedRelocationSiteDto s2 = createRankedSite(2, "FAC-2", "S2", SuitabilityClass.SUITABLE, 80.0, 5000.0, 100, 0);

        RelocationPlanDto plan1 = allocationService.allocatePopulation(hab, List.of(s1, s2));
        RelocationPlanDto plan2 = allocationService.allocatePopulation(hab, List.of(s1, s2));

        assertEquals(plan1.getTotalAllocatedPopulation(), plan2.getTotalAllocatedPopulation());
        assertEquals(plan1.getTotalUnallocatedPopulation(), plan2.getTotalUnallocatedPopulation());
        assertEquals(plan1.getOverallStatus(), plan2.getOverallStatus());
        assertEquals(plan1.getAssignments().size(), plan2.getAssignments().size());
        assertEquals(plan1.getAssignments().get(0).getAllocatedPopulation(), plan2.getAssignments().get(0).getAllocatedPopulation());
        assertEquals(plan1.getAssignments().get(1).getAllocatedPopulation(), plan2.getAssignments().get(1).getAllocatedPopulation());
    }

    @Test
    @DisplayName("12. All sites saturated with zero available capacity -> UNALLOCATED_CAPACITY_EXCEEDED")
    void testAllSitesSaturatedReturnsCapacityExceeded() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur", 26.5950, 85.5030, 200);

        RankedRelocationSiteDto s1 = createRankedSite(1, "FAC-1", "S1", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 3000.0, 100, 100);
        RankedRelocationSiteDto s2 = createRankedSite(2, "FAC-2", "S2", SuitabilityClass.SUITABLE, 80.0, 5000.0, 50, 50);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1, s2));

        assertEquals(0L, plan.getTotalAllocatedPopulation());
        assertEquals(200L, plan.getTotalUnallocatedPopulation());
        assertEquals(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED, plan.getOverallStatus());
        assertTrue(plan.getAssignments().isEmpty());
        assertEquals(1, plan.getUnallocatedHabitations().size());
    }

    @Test
    @DisplayName("13. Unbounded capacity (null capacity) accommodates entire remaining population")
    void testUnboundedCapacityAccommodatesAll() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Large Group", 26.5950, 85.5030, 750);

        // Site with null capacity (unbounded raw open space)
        RankedRelocationSiteDto unboundedSite = createRankedSite(1, "FAC-OPEN", "Regional Public Grounds",
                SuitabilityClass.HIGHLY_SUITABLE, 95.0, 4000.0, null, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(unboundedSite));

        assertEquals(750L, plan.getTotalAllocatedPopulation());
        assertEquals(0L, plan.getTotalUnallocatedPopulation());
        assertEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
        assertEquals(1, plan.getAssignments().size());
        assertEquals(750L, plan.getAssignments().get(0).getAllocatedPopulation());
    }

    @Test
    @DisplayName("14. End-to-end planRelocation pipeline (Feasibility -> Ranking -> Allocation)")
    void testEndToEndPlanRelocation() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 200);

        CandidateSafeSiteDto c1 = new CandidateSafeSiteDto();
        c1.setSiteId("S1");
        c1.setSiteName("School Hall");
        c1.setCategory(CandidateSiteCategory.EDUCATION);
        c1.setDistrict("Sitamarhi");
        c1.setLatitude(26.6500);
        c1.setLongitude(85.5200);
        c1.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
        c1.setSuitabilityClass(SuitabilityClass.HIGHLY_SUITABLE);
        c1.setSuitabilityScore(95.0);
        c1.setCapacity(500);
        c1.setAllocatedOccupancy(0);

        CandidateSafeSiteDto c2 = new CandidateSafeSiteDto();
        c2.setSiteId("S2");
        c2.setSiteName("Community Center");
        c2.setCategory(CandidateSiteCategory.EMERGENCY_SHELTER);
        c2.setDistrict("Sitamarhi");
        c2.setLatitude(26.6600);
        c2.setLongitude(85.5300);
        c2.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
        c2.setSuitabilityClass(SuitabilityClass.SUITABLE);
        c2.setSuitabilityScore(80.0);
        c2.setCapacity(300);
        c2.setAllocatedOccupancy(0);

        RelocationRequestDto req = new RelocationRequestDto();
        req.setMaxTransitDistanceKm(20.0);
        req.setMinSuitabilityClass(SuitabilityClass.SUITABLE);

        RelocationPlanDto plan = allocationService.planRelocation(hab, List.of(c1, c2), req);

        assertEquals(200L, plan.getTotalVulnerablePopulation());
        assertEquals(200L, plan.getTotalAllocatedPopulation());
        assertEquals(0L, plan.getTotalUnallocatedPopulation());
        assertEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
        assertEquals(1, plan.getAssignments().size());
        assertEquals("S1", plan.getAssignments().get(0).getDestinationSiteId());
        assertEquals(200L, plan.getAssignments().get(0).getAllocatedPopulation());
        assertEquals(RelocationStatus.ALLOCATED, plan.getAssignments().get(0).getStatus());
    }
}
