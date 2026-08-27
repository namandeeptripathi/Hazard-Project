package com.hazard;

import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RelocationFeasibilityResultDto;
import com.hazard.dto.relocation.RelocationRequestDto;
import com.hazard.dto.relocation.SiteFeasibilityEvaluationDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.relocation.RelocationFeasibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for Stage 6.2 — Relocation Intelligence: Feasibility Filtering.
 */
class RelocationFeasibilityTests {

    private RelocationFeasibilityService feasibilityService;

    @BeforeEach
    void setUp() {
        feasibilityService = new RelocationFeasibilityService();
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
        hab.setRedZone(true);
        return hab;
    }

    private CandidateSafeSiteDto createCandidateSite(String id, String name, double lat, double lon,
                                                     HazardSafetyStatus safety, SuitabilityClass suitability,
                                                     Integer capacity, Integer occupied) {
        CandidateSafeSiteDto site = new CandidateSafeSiteDto();
        site.setSiteId(id);
        site.setSiteName(name);
        site.setCategory(CandidateSiteCategory.EMERGENCY_SHELTER);
        site.setDistrict("Sitamarhi");
        site.setLatitude(lat);
        site.setLongitude(lon);
        site.setHazardSafetyStatus(safety);
        site.setSuitabilityClass(suitability);
        site.setSuitabilityScore(suitability == SuitabilityClass.HIGHLY_SUITABLE ? 95.0 :
                                 suitability == SuitabilityClass.SUITABLE ? 80.0 :
                                 suitability == SuitabilityClass.MARGINAL ? 55.0 : 25.0);
        site.setCapacity(capacity);
        site.setAllocatedOccupancy(occupied != null ? occupied : 0);
        return site;
    }

    @Test
    @DisplayName("1. Safe + suitable + enough capacity within distance -> feasible")
    void testSafeSuitableEnoughCapacityFeasible() {
        // Habitation in Sitamarhi (26.5950, 85.5030), Pop = 300
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 300);

        // Site ~6.3 km away (26.6500, 85.5200), Safe, Highly Suitable, Cap = 500, Occ = 100 (Avail = 400)
        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-001", "Sitamarhi Central Shelter", 26.6500, 85.5200,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 100
        );

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(site), 25.0, SuitabilityClass.SUITABLE
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalCandidatesEvaluated());
        assertEquals(1, result.getFeasibleCandidatesCount());
        assertEquals(0, result.getRejectedCandidatesCount());
        assertEquals(1, result.getFeasibleSites().size());
        assertEquals("FAC-001", result.getFeasibleSites().get(0).getSiteId());

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertTrue(eval.isFeasible());
        assertTrue(eval.isSafetyPassed());
        assertTrue(eval.isSuitabilityPassed());
        assertTrue(eval.isCapacityPassed());
        assertTrue(eval.isDistancePassed());
        assertNull(eval.getRejectionReasonCode());
        assertNotNull(eval.getTransitDistanceKilometers());
        assertTrue(eval.getTransitDistanceKilometers() <= 25.0);
    }

    @Test
    @DisplayName("2. Unsafe site (AT_RISK / UNSUITABLE) -> rejected by safety gate")
    void testUnsafeSiteRejected() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 200);

        // Site 1: AT_RISK
        CandidateSafeSiteDto unsafeSite = createCandidateSite(
                "FAC-UNSAFE-1", "Flooded Community Hall", 26.6000, 85.5050,
                HazardSafetyStatus.AT_RISK, SuitabilityClass.SUITABLE, 500, 0
        );
        unsafeSite.setHazardSafetyReason("Directly inside active flood inundation buffer");

        // Site 2: UNSUITABLE
        CandidateSafeSiteDto unsuitableSite = createCandidateSite(
                "FAC-UNSAFE-2", "Steep Ravine Outpost", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.UNSUITABLE, 500, 0
        );

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(unsafeSite, unsuitableSite), 25.0, SuitabilityClass.MARGINAL
        );

        assertEquals(2, result.getTotalCandidatesEvaluated());
        assertEquals(0, result.getFeasibleCandidatesCount());
        assertEquals(2, result.getRejectedCandidatesCount());
        assertTrue(result.getFeasibleSites().isEmpty());

        SiteFeasibilityEvaluationDto eval1 = result.getEvaluations().get(0);
        assertFalse(eval1.isFeasible());
        assertFalse(eval1.isSafetyPassed());
        assertEquals("REJECTED_UNSAFE", eval1.getRejectionReasonCode());
        assertTrue(eval1.getExplanation().contains("AT_RISK"));

        SiteFeasibilityEvaluationDto eval2 = result.getEvaluations().get(1);
        assertFalse(eval2.isFeasible());
        assertFalse(eval2.isSafetyPassed());
        assertEquals("REJECTED_UNSAFE", eval2.getRejectionReasonCode());
    }

    @Test
    @DisplayName("3. Insufficient capacity -> rejected by capacity gate")
    void testInsufficientCapacityRejected() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Large Settlement", 26.5950, 85.5030, 450);

        // Site: Total 500, Occupied 300 -> Available 200 (< 450 required)
        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-CAP-01", "Small Primary School", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 300
        );

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(site), 25.0, SuitabilityClass.MARGINAL
        );

        assertEquals(1, result.getTotalCandidatesEvaluated());
        assertEquals(0, result.getFeasibleCandidatesCount());
        assertEquals(1, result.getRejectedCandidatesCount());
        assertTrue(result.getFeasibleSites().isEmpty());

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertFalse(eval.isFeasible());
        assertTrue(eval.isSafetyPassed());
        assertTrue(eval.isSuitabilityPassed());
        assertFalse(eval.isCapacityPassed());
        assertEquals("REJECTED_INSUFFICIENT_CAPACITY", eval.getRejectionReasonCode());
        assertTrue(eval.getExplanation().contains("insufficient"));
    }

    @Test
    @DisplayName("4. Suitability below minimum -> rejected by suitability gate")
    void testSuitabilityBelowMinimumRejected() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 100);

        // Site: MARGINAL suitability
        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-MARG-01", "Marginal Panchayat Bhavan", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.MARGINAL, 500, 0
        );

        // Require at least SUITABLE
        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(site), 25.0, SuitabilityClass.SUITABLE
        );

        assertEquals(1, result.getTotalCandidatesEvaluated());
        assertEquals(0, result.getFeasibleCandidatesCount());
        assertEquals(1, result.getRejectedCandidatesCount());
        assertTrue(result.getFeasibleSites().isEmpty());

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertFalse(eval.isFeasible());
        assertTrue(eval.isSafetyPassed());
        assertFalse(eval.isSuitabilityPassed());
        assertEquals("REJECTED_SUITABILITY_BELOW_MINIMUM", eval.getRejectionReasonCode());
        assertTrue(eval.getExplanation().contains("MARGINAL"));
        assertTrue(eval.getExplanation().contains("SUITABLE"));
    }

    @Test
    @DisplayName("5. Maximum distance exceeded -> rejected by distance gate")
    void testMaximumDistanceExceededRejected() {
        // Origin: Sitamarhi (26.5950, 85.5030)
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 100);

        // Destination: Far away in Muzaffarpur (26.1150, 85.3850) -> ~54 km away
        CandidateSafeSiteDto distantSite = createCandidateSite(
                "FAC-DIST-01", "Distant Muzaffarpur Relief Center", 26.1150, 85.3850,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 1000, 0
        );

        // Set max distance constraint to 20.0 km
        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(distantSite), 20.0, SuitabilityClass.MARGINAL
        );

        assertEquals(1, result.getTotalCandidatesEvaluated());
        assertEquals(0, result.getFeasibleCandidatesCount());
        assertEquals(1, result.getRejectedCandidatesCount());
        assertTrue(result.getFeasibleSites().isEmpty());

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertFalse(eval.isFeasible());
        assertTrue(eval.isSafetyPassed());
        assertTrue(eval.isSuitabilityPassed());
        assertTrue(eval.isCapacityPassed());
        assertFalse(eval.isDistancePassed());
        assertEquals("REJECTED_DISTANCE_EXCEEDED", eval.getRejectionReasonCode());
        assertTrue(eval.getTransitDistanceKilometers() > 20.0);
    }

    @Test
    @DisplayName("6. No distance constraint -> distance does not reject a site")
    void testNoDistanceConstraintAllowsDistantSite() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 100);

        // Distant site (~54 km)
        CandidateSafeSiteDto distantSite = createCandidateSite(
                "FAC-DIST-02", "Regional Shelter Center", 26.1150, 85.3850,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 1000, 0
        );

        // Pass null for maxTransitDistanceKm
        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(distantSite), null, SuitabilityClass.MARGINAL
        );

        assertEquals(1, result.getTotalCandidatesEvaluated());
        assertEquals(1, result.getFeasibleCandidatesCount());
        assertEquals(1, result.getFeasibleSites().size());

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertTrue(eval.isFeasible());
        assertTrue(eval.isDistancePassed());
        assertNull(eval.getRejectionReasonCode());
    }

    @Test
    @DisplayName("7. Multiple candidates -> only feasible candidates returned")
    void testMultipleCandidatesFiltering() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 200);

        // Site 1: Feasible (Close, Safe, High Suitability, Cap 500, Occ 0)
        CandidateSafeSiteDto s1 = createCandidateSite("S1", "Feasible School", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 0);

        // Site 2: Unsafe (AT_RISK)
        CandidateSafeSiteDto s2 = createCandidateSite("S2", "Flooded Center", 26.6100, 85.5100,
                HazardSafetyStatus.AT_RISK, SuitabilityClass.HIGHLY_SUITABLE, 500, 0);

        // Site 3: Insufficient Capacity (Cap 200, Occ 150 -> Avail 50 < 200)
        CandidateSafeSiteDto s3 = createCandidateSite("S3", "Full Shelter", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE, 200, 150);

        // Site 4: Below Suitability (MARGINAL when SUITABLE is required)
        CandidateSafeSiteDto s4 = createCandidateSite("S4", "Low Grade Shed", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.MARGINAL, 500, 0);

        // Site 5: Feasible (Close, Safe, Suitable, Cap 300, Occ 50 -> Avail 250 >= 200)
        CandidateSafeSiteDto s5 = createCandidateSite("S5", "Feasible Hospital Hall", 26.6300, 85.5150,
                HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE, 300, 50);

        // Site 6: Distance Exceeded (54 km > 20 km)
        CandidateSafeSiteDto s6 = createCandidateSite("S6", "Too Far Shelter", 26.1150, 85.3850,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 1000, 0);

        List<CandidateSafeSiteDto> allSites = List.of(s1, s2, s3, s4, s5, s6);

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, allSites, 20.0, SuitabilityClass.SUITABLE
        );

        assertEquals(6, result.getTotalCandidatesEvaluated());
        assertEquals(2, result.getFeasibleCandidatesCount());
        assertEquals(4, result.getRejectedCandidatesCount());

        List<String> feasibleIds = result.getFeasibleSites().stream().map(CandidateSafeSiteDto::getSiteId).toList();
        assertEquals(List.of("S1", "S5"), feasibleIds);
    }

    @Test
    @DisplayName("8. No feasible candidates -> empty result without selecting unsafe fallback")
    void testNoFeasibleCandidatesReturnsEmpty() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 500);

        // All sites fail
        CandidateSafeSiteDto unsafe = createCandidateSite("U1", "Unsafe 1", 26.60, 85.50,
                HazardSafetyStatus.AT_RISK, SuitabilityClass.SUITABLE, 1000, 0);
        CandidateSafeSiteDto noCap = createCandidateSite("U2", "No Cap 2", 26.60, 85.50,
                HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE, 100, 100);

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(unsafe, noCap), 25.0, SuitabilityClass.SUITABLE
        );

        assertNotNull(result);
        assertEquals(2, result.getTotalCandidatesEvaluated());
        assertEquals(0, result.getFeasibleCandidatesCount());
        assertEquals(2, result.getRejectedCandidatesCount());
        assertTrue(result.getFeasibleSites().isEmpty());
        assertFalse(result.hasFeasibleSites());
    }

    @Test
    @DisplayName("9. Zero available capacity -> rejected")
    void testZeroAvailableCapacityRejected() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 50);

        // Total 200, Occupied 200 -> Available 0
        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-ZERO-01", "Fully Occupied Hall", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 200, 200
        );

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(site), 25.0, SuitabilityClass.MARGINAL
        );

        assertEquals(1, result.getTotalCandidatesEvaluated());
        assertEquals(0, result.getFeasibleCandidatesCount());
        assertEquals(1, result.getRejectedCandidatesCount());

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertFalse(eval.isFeasible());
        assertFalse(eval.isCapacityPassed());
        assertEquals("REJECTED_ZERO_AVAILABLE_CAPACITY", eval.getRejectionReasonCode());
    }

    @Test
    @DisplayName("10. Edge cases: null/empty candidates and null habitation")
    void testEdgeCases() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Test Hab", 26.5950, 85.5030, 100);

        // Empty candidates list
        RelocationFeasibilityResultDto resEmpty = feasibilityService.evaluateFeasibility(
                hab, Collections.emptyList(), 25.0, SuitabilityClass.MARGINAL
        );
        assertEquals(0, resEmpty.getTotalCandidatesEvaluated());
        assertEquals(0, resEmpty.getFeasibleCandidatesCount());
        assertTrue(resEmpty.getFeasibleSites().isEmpty());

        // Null candidates list
        RelocationFeasibilityResultDto resNullList = feasibilityService.evaluateFeasibility(
                hab, null, 25.0, SuitabilityClass.MARGINAL
        );
        assertEquals(0, resNullList.getTotalCandidatesEvaluated());
        assertTrue(resNullList.getFeasibleSites().isEmpty());

        // Null habitation
        RelocationFeasibilityResultDto resNullHab = feasibilityService.evaluateFeasibility(
                null, List.of(new CandidateSafeSiteDto()), 25.0, SuitabilityClass.MARGINAL
        );
        assertEquals(0, resNullHab.getTotalCandidatesEvaluated());
        assertTrue(resNullHab.getFeasibleSites().isEmpty());
    }

    @Test
    @DisplayName("11. Integration with RelocationRequestDto configuration")
    void testWithRelocationRequestDto() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 150);

        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-REQ-01", "Request Test Shelter", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 0
        );

        RelocationRequestDto request = new RelocationRequestDto();
        request.setMaxTransitDistanceKm(15.0);
        request.setMinSuitabilityClass(SuitabilityClass.SUITABLE);

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(site), request
        );

        assertEquals(1, result.getFeasibleCandidatesCount());
        assertTrue(result.hasFeasibleSites());
        assertEquals(15.0, result.getMaxTransitDistanceKm());
        assertEquals("SUITABLE", result.getMinSuitabilityClass());
    }

    @Test
    @DisplayName("12. Unbounded capacity (null capacity) passes capacity gate")
    void testUnboundedCapacityPasses() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Large Group", 26.5950, 85.5030, 1000);

        // Site with capacity = null (raw OSM source without capacity metadata)
        CandidateSafeSiteDto unmeasuredSite = createCandidateSite(
                "FAC-OSM-01", "Unmeasured Public Grounds", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, null, 0
        );

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(unmeasuredSite), 25.0, SuitabilityClass.MARGINAL
        );

        assertEquals(1, result.getFeasibleCandidatesCount());
        assertTrue(result.getEvaluations().get(0).isCapacityPassed());
        assertTrue(result.getEvaluations().get(0).isFeasible());
    }

    // =========================================================================
    // STAGE 6.3: DISTANCE INFORMATION TESTS
    // =========================================================================

    @Test
    @DisplayName("13. Correct distance calculation for known coordinates using Haversine")
    void testKnownCoordinatesDistanceCalculation() {
        // Origin: PMCH Patna (25.6208, 85.1580)
        VulnerableHabitationDto hab = createHabitation("HAB-PATNA", "Patna Central", 25.6208, 85.1580, 100);

        // Destination: Sitamarhi Shelter (26.5950, 85.5030)
        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-SITA-01", "Sitamarhi Shelter", 26.5950, 85.5030,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 0
        );

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(site), null, SuitabilityClass.MARGINAL
        );

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertTrue(eval.isDistanceAvailable());
        assertNotNull(eval.getDistanceMeters());
        assertNotNull(eval.getDistanceKilometers());

        // Geodesic distance Patna -> Sitamarhi is ~113.1 km (approx 113100 m)
        assertTrue(eval.getDistanceMeters() > 100000.0 && eval.getDistanceMeters() < 120000.0,
                "Distance should be around 113 km, was: " + eval.getDistanceMeters());
        assertEquals(Math.round((eval.getDistanceMeters() / 1000.0) * 100.0) / 100.0, eval.getDistanceKilometers());
    }

    @Test
    @DisplayName("14. Distance in meters and kilometers is consistently formatted")
    void testDistanceMetersAndKilometersConsistency() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 100);

        // Site ~6.35 km away
        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-001", "Nearby Hall", 26.6500, 85.5200,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 0
        );

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(site), null, SuitabilityClass.MARGINAL
        );

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertTrue(eval.isDistanceAvailable());
        assertEquals(eval.getTransitDistanceMeters(), eval.getDistanceMeters());
        assertEquals(eval.getTransitDistanceKilometers(), eval.getDistanceKilometers());
        assertEquals(Math.round((eval.getDistanceMeters() / 1000.0) * 100.0) / 100.0, eval.getDistanceKilometers());
    }

    @Test
    @DisplayName("15. Zero-distance case returns 0.0 meters and 0.0 kilometers")
    void testZeroDistanceCase() {
        // Same coordinates for origin and destination
        VulnerableHabitationDto hab = createHabitation("HAB-SAME", "Colocated Basti", 26.5000, 85.5000, 50);

        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-SAME", "Colocated Community Center", 26.5000, 85.5000,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 0
        );

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(site), 10.0, SuitabilityClass.MARGINAL
        );

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertTrue(eval.isDistanceAvailable());
        assertEquals(0.0, eval.getDistanceMeters(), 0.001);
        assertEquals(0.0, eval.getDistanceKilometers(), 0.001);
        assertTrue(eval.isDistancePassed());
        assertTrue(eval.isFeasible());
    }

    @Test
    @DisplayName("16. Missing origin coordinates represented explicitly as unavailable (null)")
    void testMissingOriginCoordinates() {
        // Habitation with null coordinates
        VulnerableHabitationDto hab = createHabitation("HAB-NO-GEO", "Unmapped Village", 0.0, 0.0, 100);
        hab.setLatitude(null);
        hab.setLongitude(null);

        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-GEO-01", "Mapped Shelter", 26.6100, 85.5100,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 0
        );

        // Case A: No distance limit required -> distance is null, site remains feasible
        RelocationFeasibilityResultDto resultNoLimit = feasibilityService.evaluateFeasibility(
                hab, List.of(site), null, SuitabilityClass.MARGINAL
        );
        SiteFeasibilityEvaluationDto evalNoLimit = resultNoLimit.getEvaluations().get(0);
        assertFalse(evalNoLimit.isDistanceAvailable());
        assertNull(evalNoLimit.getDistanceMeters());
        assertNull(evalNoLimit.getDistanceKilometers());
        assertNull(evalNoLimit.getOriginLatitude());
        assertNull(evalNoLimit.getOriginLongitude());
        assertTrue(evalNoLimit.isDistancePassed());
        assertTrue(evalNoLimit.isFeasible());

        // Case B: Strict max distance required -> distance cannot be verified, rejected with REJECTED_MISSING_COORDINATES
        RelocationFeasibilityResultDto resultWithLimit = feasibilityService.evaluateFeasibility(
                hab, List.of(site), 20.0, SuitabilityClass.MARGINAL
        );
        SiteFeasibilityEvaluationDto evalWithLimit = resultWithLimit.getEvaluations().get(0);
        assertFalse(evalWithLimit.isDistanceAvailable());
        assertFalse(evalWithLimit.isDistancePassed());
        assertFalse(evalWithLimit.isFeasible());
        assertEquals("REJECTED_MISSING_COORDINATES", evalWithLimit.getRejectionReasonCode());
    }

    @Test
    @DisplayName("17. Missing destination coordinates represented explicitly as unavailable (null)")
    void testMissingDestinationCoordinates() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 100);

        // Candidate site with null coordinates
        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-NO-GEO", "Unmapped Hall", 0.0, 0.0,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 0
        );
        site.setLatitude(null);
        site.setLongitude(null);

        // Case A: No distance limit -> distance is null, site feasible
        RelocationFeasibilityResultDto resultNoLimit = feasibilityService.evaluateFeasibility(
                hab, List.of(site), null, SuitabilityClass.MARGINAL
        );
        SiteFeasibilityEvaluationDto evalNoLimit = resultNoLimit.getEvaluations().get(0);
        assertFalse(evalNoLimit.isDistanceAvailable());
        assertNull(evalNoLimit.getDistanceMeters());
        assertNull(evalNoLimit.getDistanceKilometers());
        assertNull(evalNoLimit.getDestinationLatitude());
        assertNull(evalNoLimit.getDestinationLongitude());
        assertTrue(evalNoLimit.isFeasible());

        // Case B: Strict max distance required -> fails with REJECTED_MISSING_COORDINATES
        RelocationFeasibilityResultDto resultWithLimit = feasibilityService.evaluateFeasibility(
                hab, List.of(site), 15.0, SuitabilityClass.MARGINAL
        );
        SiteFeasibilityEvaluationDto evalWithLimit = resultWithLimit.getEvaluations().get(0);
        assertFalse(evalWithLimit.isDistanceAvailable());
        assertFalse(evalWithLimit.isFeasible());
        assertEquals("REJECTED_MISSING_COORDINATES", evalWithLimit.getRejectionReasonCode());
    }

    @Test
    @DisplayName("18. Distance information exposes origin, destination, meters, and kilometers")
    void testDistanceInformationExposedInEvaluationResult() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Origin Place", 26.5950, 85.5030, 100);
        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-DEST", "Destination Shelter", 26.6500, 85.5200,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 500, 0
        );

        RelocationFeasibilityResultDto result = feasibilityService.evaluateFeasibility(
                hab, List.of(site), 25.0, SuitabilityClass.MARGINAL
        );

        SiteFeasibilityEvaluationDto eval = result.getEvaluations().get(0);
        assertEquals(26.5950, eval.getOriginLatitude());
        assertEquals(85.5030, eval.getOriginLongitude());
        assertEquals(26.6500, eval.getDestinationLatitude());
        assertEquals(85.5200, eval.getDestinationLongitude());
        assertEquals(26.6500, eval.getLatitude()); // backward compatible alias
        assertEquals(85.5200, eval.getLongitude()); // backward compatible alias
        assertNotNull(eval.getDistanceMeters());
        assertNotNull(eval.getDistanceKilometers());
        assertTrue(eval.isDistanceAvailable());
        assertTrue(eval.getDistanceKilometers() > 0.0);
    }
}
