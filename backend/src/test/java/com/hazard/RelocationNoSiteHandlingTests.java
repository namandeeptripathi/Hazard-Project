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
 * Unit Tests for Stage 6.6 — Relocation Intelligence: No-Site Handling & Allocation Deficit Reporting.
 */
class RelocationNoSiteHandlingTests {

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

    private CandidateSafeSiteDto createCandidateSite(String id, String name, double lat, double lon,
                                                     HazardSafetyStatus safety, SuitabilityClass suitability,
                                                     Double suitabilityScore, Integer capacity, Integer occupied) {
        CandidateSafeSiteDto site = new CandidateSafeSiteDto();
        site.setSiteId(id);
        site.setSiteName(name);
        site.setCategory(CandidateSiteCategory.EMERGENCY_SHELTER);
        site.setDistrict("Sitamarhi");
        site.setLatitude(lat);
        site.setLongitude(lon);
        site.setHazardSafetyStatus(safety);
        site.setSuitabilityClass(suitability);
        site.setSuitabilityScore(suitabilityScore);
        site.setCapacity(capacity);
        site.setAllocatedOccupancy(occupied != null ? occupied : 0);
        return site;
    }

    @Test
    @DisplayName("1. CASE 1: No feasible sites exist -> UNALLOCATED_NO_SAFE_SITE with full deficit")
    void testNoFeasibleSitesReturnsNoSafeSiteStatus() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Inundated Village", 26.5950, 85.5030, 200);

        // 3 Candidate sites, but all are AT_RISK (hazard exposed)
        CandidateSafeSiteDto unsafe1 = createCandidateSite("U1", "Flooded School", 26.6000, 85.5050,
                HazardSafetyStatus.AT_RISK, SuitabilityClass.HIGHLY_SUITABLE, 95.0, 500, 0);
        CandidateSafeSiteDto unsafe2 = createCandidateSite("U2", "Inundated Hall", 26.6100, 85.5100,
                HazardSafetyStatus.AT_RISK, SuitabilityClass.SUITABLE, 80.0, 300, 0);

        RelocationRequestDto req = new RelocationRequestDto();
        req.setMaxTransitDistanceKm(20.0);
        req.setMinSuitabilityClass(SuitabilityClass.MARGINAL);

        RelocationPlanDto plan = allocationService.planRelocation(hab, List.of(unsafe1, unsafe2), req);

        assertNotNull(plan);
        assertEquals(200L, plan.getTotalVulnerablePopulation());
        assertEquals(0L, plan.getTotalAllocatedPopulation());
        assertEquals(200L, plan.getTotalUnallocatedPopulation());
        assertEquals(0.0, plan.getAllocationRatePercentage());
        assertEquals(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, plan.getOverallStatus());
        assertEquals("NO_FEASIBLE_SITE", plan.getDeficitReasonCode());
        assertNotNull(plan.getDeficitExplanation());
        assertTrue(plan.getDeficitExplanation().contains("No feasible safe sites"));
        assertTrue(plan.getAssignments().isEmpty(), "No unsafe fallback assignment may be created");

        assertEquals(1, plan.getUnallocatedHabitations().size());
        VulnerableHabitationDto deficit = plan.getUnallocatedHabitations().get(0);
        assertEquals(200L, deficit.getVulnerablePopulation());
        assertEquals(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, deficit.getRelocationStatus());
        assertTrue(plan.hasDeficit());
        assertFalse(plan.isFullyAllocated());
    }

    @Test
    @DisplayName("2. CASE 2: Feasible sites exist but have zero available capacity -> UNALLOCATED_CAPACITY_EXCEEDED")
    void testFeasibleSitesZeroCapacityReturnsCapacityExceeded() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 200);

        // 2 Safe & Highly Suitable sites, but 100% full (available capacity = 0)
        RankedRelocationSiteDto fullSite1 = createRankedSite(1, "FULL-1", "Full School",
                SuitabilityClass.HIGHLY_SUITABLE, 95.0, 4000.0, 300, 300);
        RankedRelocationSiteDto fullSite2 = createRankedSite(2, "FULL-2", "Full Hall",
                SuitabilityClass.SUITABLE, 80.0, 6000.0, 200, 200);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(fullSite1, fullSite2));

        assertNotNull(plan);
        assertEquals(200L, plan.getTotalVulnerablePopulation());
        assertEquals(0L, plan.getTotalAllocatedPopulation());
        assertEquals(200L, plan.getTotalUnallocatedPopulation());
        assertEquals(0.0, plan.getAllocationRatePercentage());
        assertEquals(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED, plan.getOverallStatus());
        assertEquals("CAPACITY_EXHAUSTED", plan.getDeficitReasonCode());
        assertNotNull(plan.getDeficitExplanation());
        assertTrue(plan.getDeficitExplanation().contains("zero available"));
        assertTrue(plan.getAssignments().isEmpty(), "No assignments when zero capacity available");

        assertEquals(1, plan.getUnallocatedHabitations().size());
        VulnerableHabitationDto deficit = plan.getUnallocatedHabitations().get(0);
        assertEquals(200L, deficit.getVulnerablePopulation());
        assertEquals(RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED, deficit.getRelocationStatus());
        assertTrue(plan.hasDeficit());
        assertFalse(plan.isFullyAllocated());
    }

    @Test
    @DisplayName("3. CASE 3: Partial capacity -> PARTIALLY_ALLOCATED with exact deficit calculation")
    void testPartialCapacityReturnsExactDeficit() {
        // Population = 300
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Large Cluster", 26.5950, 85.5030, 300);

        // Total available capacity = 100 + 80 = 180
        RankedRelocationSiteDto s1 = createRankedSite(1, "S1", "Site 1", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 3000.0, 100, 0);
        RankedRelocationSiteDto s2 = createRankedSite(2, "S2", "Site 2", SuitabilityClass.SUITABLE, 80.0, 5000.0, 80, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1, s2));

        assertEquals(300L, plan.getTotalVulnerablePopulation());
        assertEquals(180L, plan.getTotalAllocatedPopulation());
        assertEquals(120L, plan.getTotalUnallocatedPopulation());
        assertEquals(60.0, plan.getAllocationRatePercentage(), 0.01);
        assertEquals(RelocationStatus.PARTIALLY_ALLOCATED, plan.getOverallStatus());
        assertEquals("PARTIAL_CAPACITY", plan.getDeficitReasonCode());
        assertTrue(plan.getDeficitExplanation().contains("Capacity deficit: 180 of 300"));

        // Verify valid assignments remain intact
        assertEquals(2, plan.getAssignments().size());
        assertEquals(100L, plan.getAssignments().get(0).getAllocatedPopulation());
        assertEquals(80L, plan.getAssignments().get(1).getAllocatedPopulation());

        // Verify deficit record
        assertEquals(1, plan.getUnallocatedHabitations().size());
        VulnerableHabitationDto deficit = plan.getUnallocatedHabitations().get(0);
        assertEquals(120L, deficit.getVulnerablePopulation());
        assertEquals(RelocationStatus.PARTIALLY_ALLOCATED, deficit.getRelocationStatus());

        assertTrue(plan.hasDeficit());
        assertFalse(plan.isFullyAllocated());
        assertEquals(120L, plan.getDeficitPopulation());
    }

    @Test
    @DisplayName("4. CASE 4: Full allocation -> ALLOCATED with zero deficit and NO false deficit")
    void testFullAllocationZeroDeficit() {
        // Population = 300, Available capacity = 500
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur", 26.5950, 85.5030, 300);
        RankedRelocationSiteDto s1 = createRankedSite(1, "S1", "High Cap Site", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 4000.0, 500, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1));

        assertEquals(300L, plan.getTotalVulnerablePopulation());
        assertEquals(300L, plan.getTotalAllocatedPopulation());
        assertEquals(0L, plan.getTotalUnallocatedPopulation());
        assertEquals(100.0, plan.getAllocationRatePercentage());
        assertEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
        assertEquals("FULLY_ALLOCATED", plan.getDeficitReasonCode());
        assertNull(plan.getDeficitExplanation());
        assertTrue(plan.getUnallocatedHabitations().isEmpty(), "No false deficit record should be reported");
        assertFalse(plan.hasDeficit());
        assertTrue(plan.isFullyAllocated());
        assertEquals(0L, plan.getDeficitPopulation());
    }

    @Test
    @DisplayName("5. Deficit Accounting Invariant: allocated + unallocated == totalVulnerablePopulation")
    void testDeficitAccountingInvariants() {
        // Test varying population loads
        long[] populations = {0, 50, 100, 150, 200, 300, 500, 1000};
        RankedRelocationSiteDto s1 = createRankedSite(1, "S1", "S1", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 3000.0, 100, 0);
        RankedRelocationSiteDto s2 = createRankedSite(2, "S2", "S2", SuitabilityClass.SUITABLE, 80.0, 5000.0, 150, 0);

        for (long pop : populations) {
            VulnerableHabitationDto hab = createHabitation("HAB-" + pop, "Hab " + pop, 26.5950, 85.5030, pop);
            RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1, s2));

            // Core Conservation Invariant
            assertTrue(plan.validateInvariants(), "Plan invariants must be valid for population " + pop);
            assertEquals(pop, plan.getTotalAllocatedPopulation() + plan.getTotalUnallocatedPopulation(),
                    "allocated + unallocated must strictly equal total for pop " + pop);
            assertTrue(plan.getTotalAllocatedPopulation() >= 0);
            assertTrue(plan.getTotalUnallocatedPopulation() >= 0);
            assertTrue(plan.getTotalAllocatedPopulation() <= pop);

            // Assignment capacity invariant
            long sumAssigned = plan.getAssignments().stream().mapToLong(RelocationAssignmentDto::getAllocatedPopulation).sum();
            assertEquals(plan.getTotalAllocatedPopulation(), sumAssigned);
        }
    }

    @Test
    @DisplayName("6. NEVER assign to an unsafe or infeasible fallback site")
    void testNoUnsafeOrInfeasibleFallbackAssignment() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Critical Group", 26.5950, 85.5030, 200);

        // Candidate 1: Unsafe (AT_RISK)
        CandidateSafeSiteDto unsafeSite = createCandidateSite("UNSAFE-1", "Flooded Ground", 26.6000, 85.5050,
                HazardSafetyStatus.AT_RISK, SuitabilityClass.HIGHLY_SUITABLE, 95.0, 1000, 0);

        // Candidate 2: Infeasible (Distance = 100 km > max 15 km)
        CandidateSafeSiteDto farSite = createCandidateSite("FAR-1", "Faraway Hall", 27.5000, 86.5000,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 95.0, 1000, 0);

        RelocationRequestDto req = new RelocationRequestDto();
        req.setMaxTransitDistanceKm(15.0);
        req.setMinSuitabilityClass(SuitabilityClass.MARGINAL);

        RelocationPlanDto plan = allocationService.planRelocation(hab, List.of(unsafeSite, farSite), req);

        // Assert zero unsafe assignments
        assertEquals(0, plan.getAssignments().size());
        assertEquals(0L, plan.getTotalAllocatedPopulation());
        assertEquals(200L, plan.getTotalUnallocatedPopulation());
        assertEquals(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, plan.getOverallStatus());
        assertEquals("NO_FEASIBLE_SITE", plan.getDeficitReasonCode());

        // Verify none of the unsafe site IDs appear in any assignment
        assertFalse(plan.getAssignments().stream().anyMatch(a -> "UNSAFE-1".equals(a.getDestinationSiteId())));
        assertFalse(plan.getAssignments().stream().anyMatch(a -> "FAR-1".equals(a.getDestinationSiteId())));
    }

    @Test
    @DisplayName("7. NEVER claim 100% allocation when a capacity deficit exists")
    void testNoFalse100PercentAllocation() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur", 26.5950, 85.5030, 200);
        // Only 50 available capacity
        RankedRelocationSiteDto s1 = createRankedSite(1, "S1", "Small Hall", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 3000.0, 50, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1));

        assertNotEquals(100.0, plan.getAllocationRatePercentage());
        assertEquals(25.0, plan.getAllocationRatePercentage(), 0.01);
        assertNotEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
        assertEquals(RelocationStatus.PARTIALLY_ALLOCATED, plan.getOverallStatus());
        assertTrue(plan.hasDeficit());
        assertEquals(150L, plan.getDeficitPopulation());
    }

    @Test
    @DisplayName("8. Empty candidate list returns UNALLOCATED_NO_SAFE_SITE cleanly")
    void testEmptyCandidateList() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur", 26.5950, 85.5030, 150);

        RelocationPlanDto planNull = allocationService.allocatePopulation(hab, (List<RankedRelocationSiteDto>) null);
        assertEquals(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, planNull.getOverallStatus());
        assertEquals("NO_FEASIBLE_SITE", planNull.getDeficitReasonCode());
        assertEquals(150L, planNull.getTotalUnallocatedPopulation());

        RelocationPlanDto planEmpty = allocationService.allocatePopulation(hab, Collections.emptyList());
        assertEquals(RelocationStatus.UNALLOCATED_NO_SAFE_SITE, planEmpty.getOverallStatus());
        assertEquals("NO_FEASIBLE_SITE", planEmpty.getDeficitReasonCode());
        assertEquals(150L, planEmpty.getTotalUnallocatedPopulation());
    }

    @Test
    @DisplayName("9. Zero or negative population produces no false deficit")
    void testZeroPopulationProducesNoFalseDeficit() {
        VulnerableHabitationDto hab = createHabitation("HAB-000", "Zero Pop Village", 26.5950, 85.5030, 0);
        RankedRelocationSiteDto s1 = createRankedSite(1, "S1", "Hall", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 3000.0, 500, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1));

        assertEquals(0L, plan.getTotalVulnerablePopulation());
        assertEquals(0L, plan.getTotalAllocatedPopulation());
        assertEquals(0L, plan.getTotalUnallocatedPopulation());
        assertEquals(100.0, plan.getAllocationRatePercentage());
        assertEquals(RelocationStatus.ALLOCATED, plan.getOverallStatus());
        assertEquals("FULLY_ALLOCATED", plan.getDeficitReasonCode());
        assertFalse(plan.hasDeficit());
        assertTrue(plan.isFullyAllocated());
    }

    @Test
    @DisplayName("10. Existing Stage 6.5 assignment properties remain intact in final plan")
    void testAssignmentPropertiesPreservedInFinalPlan() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 150);
        RankedRelocationSiteDto s1 = createRankedSite(1, "FAC-001", "Primary Shelter",
                SuitabilityClass.HIGHLY_SUITABLE, 95.0, 5200.0, 200, 0);

        RelocationPlanDto plan = allocationService.allocatePopulation(hab, List.of(s1));

        assertEquals(1, plan.getAssignments().size());
        RelocationAssignmentDto assign = plan.getAssignments().get(0);
        assertEquals("ASN-HAB-001-FAC-001", assign.getAssignmentId());
        assertEquals("HAB-001", assign.getHabitationId());
        assertEquals("FAC-001", assign.getDestinationSiteId());
        assertEquals("Primary Shelter", assign.getDestinationSiteName());
        assertEquals(1, assign.getDestinationRank());
        assertEquals(5200.0, assign.getTransitDistanceMeters());
        assertEquals(5.2, assign.getTransitDistanceKilometers());
        assertEquals(RelocationUrgency.CRITICAL, assign.getUrgency());
        assertEquals(RelocationStatus.ALLOCATED, assign.getStatus());
        assertNotNull(assign.getAllocationReason());
        assertTrue(assign.getAllocationReason().contains("Allocated 150 people"));
    }
}
