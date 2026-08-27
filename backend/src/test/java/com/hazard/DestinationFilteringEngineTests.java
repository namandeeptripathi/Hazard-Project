package com.hazard;

import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.relocation.DestinationFilteringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7B.3 — Destination Filtering Engine Tests.
 * Verifies all 5 hard feasibility gates (Identity, Safety, Suitability, Capacity, Distance).
 */
class DestinationFilteringEngineTests {

    private DestinationFilteringEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DestinationFilteringEngine();
    }

    private VulnerableHabitationDto createHabitation(String id, double lat, double lon, long pop) {
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId(id);
        hab.setHabitationName("Habitation " + id);
        hab.setDistrict("Sitamarhi");
        hab.setState("Bihar");
        hab.setLatitude(lat);
        hab.setLongitude(lon);
        hab.setVulnerablePopulation(pop);
        return hab;
    }

    private CandidateSafeSiteDto createSite(String id, double lat, double lon, Integer capacity,
                                            HazardSafetyStatus safety, SuitabilityClass suitability) {
        CandidateSafeSiteDto site = new CandidateSafeSiteDto();
        site.setSiteId(id);
        site.setSiteName("Site " + id);
        site.setDistrict("Sitamarhi");
        site.setState("Bihar");
        site.setLatitude(lat);
        site.setLongitude(lon);
        site.setCapacity(capacity);
        site.setAllocatedOccupancy(0);
        site.setAvailableCapacity(capacity);
        site.setHazardSafetyStatus(safety);
        site.setSuitabilityClass(suitability);
        site.setSuitabilityScore(suitability == SuitabilityClass.HIGHLY_SUITABLE ? 90.0 : 75.0);
        return site;
    }

    @Nested
    @DisplayName("Gate 1: Identity Gate")
    class IdentityGateTests {

        @Test
        @DisplayName("Rejects candidate destination identical to origin habitation ID")
        void testIdenticalOriginDestinationRejected() {
            VulnerableHabitationDto hab = createHabitation("SITE-01", 26.5950, 85.5030, 100L);
            CandidateSafeSiteDto site = createSite("SITE-01", 26.5950, 85.5030, 500,
                    HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE);

            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 100L, 50.0, SuitabilityClass.MARGINAL);

            assertFalse(result.isFeasible());
            assertEquals("REJECTED_IDENTICAL_ORIGIN_DESTINATION", result.getRejectionReasonCode());
            assertTrue(result.getRejectionReason().contains("identical"));
        }
    }

    @Nested
    @DisplayName("Gate 2: Safety Gate")
    class SafetyGateTests {

        @Test
        @DisplayName("Rejects site with AT_RISK hazard safety status")
        void testAtRiskSafetyRejected() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 100L);
            CandidateSafeSiteDto site = createSite("SITE-02", 26.6000, 85.5100, 500,
                    HazardSafetyStatus.AT_RISK, SuitabilityClass.SUITABLE);

            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 100L, 50.0, SuitabilityClass.MARGINAL);

            assertFalse(result.isFeasible());
            assertEquals("REJECTED_UNSAFE", result.getRejectionReasonCode());
        }

        @Test
        @DisplayName("Rejects site with UNSUITABLE suitability class due to safety")
        void testUnsuitableClassRejected() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 100L);
            CandidateSafeSiteDto site = createSite("SITE-03", 26.6000, 85.5100, 500,
                    HazardSafetyStatus.SAFE, SuitabilityClass.UNSUITABLE);

            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 100L, 50.0, null);

            assertFalse(result.isFeasible());
            assertEquals("REJECTED_UNSAFE", result.getRejectionReasonCode());
        }
    }

    @Nested
    @DisplayName("Gate 3: Suitability Gate")
    class SuitabilityGateTests {

        @Test
        @DisplayName("Rejects candidate below minSuitabilityClass threshold")
        void testBelowMinSuitabilityRejected() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 100L);
            CandidateSafeSiteDto site = createSite("SITE-04", 26.6000, 85.5100, 500,
                    HazardSafetyStatus.SAFE, SuitabilityClass.MARGINAL);

            // Require SUITABLE (tier 2), but candidate is MARGINAL (tier 3)
            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 100L, 50.0, SuitabilityClass.SUITABLE);

            assertFalse(result.isFeasible());
            assertEquals("REJECTED_SUITABILITY_BELOW_MINIMUM", result.getRejectionReasonCode());
        }

        @Test
        @DisplayName("Passes candidate meeting or exceeding minSuitabilityClass")
        void testMeetingMinSuitabilityPasses() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 100L);
            CandidateSafeSiteDto site = createSite("SITE-05", 26.6000, 85.5100, 500,
                    HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE);

            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 100L, 50.0, SuitabilityClass.SUITABLE);

            assertTrue(result.isFeasible());
            assertNull(result.getRejectionReasonCode());
        }
    }

    @Nested
    @DisplayName("Gate 4: Capacity Gate")
    class CapacityGateTests {

        @Test
        @DisplayName("Rejects candidate with zero available capacity")
        void testZeroAvailableCapacityRejected() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 100L);
            CandidateSafeSiteDto site = createSite("SITE-06", 26.6000, 85.5100, 500,
                    HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE);
            site.setAllocatedOccupancy(500); // Fully occupied
            site.setAvailableCapacity(0);

            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 100L, 50.0, SuitabilityClass.MARGINAL);

            assertFalse(result.isFeasible());
            assertEquals("REJECTED_ZERO_AVAILABLE_CAPACITY", result.getRejectionReasonCode());
        }

        @Test
        @DisplayName("Rejects candidate with insufficient capacity for required population")
        void testInsufficientCapacityRejected() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 300L);
            CandidateSafeSiteDto site = createSite("SITE-07", 26.6000, 85.5100, 200,
                    HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE); // 200 < 300 required

            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 300L, 50.0, SuitabilityClass.MARGINAL);

            assertFalse(result.isFeasible());
            assertEquals("REJECTED_INSUFFICIENT_CAPACITY", result.getRejectionReasonCode());
        }

        @Test
        @DisplayName("Passes candidate with exact capacity boundary (capacity == population)")
        void testExactCapacityPasses() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 250L);
            CandidateSafeSiteDto site = createSite("SITE-08", 26.6000, 85.5100, 250,
                    HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE);

            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 250L, 50.0, SuitabilityClass.MARGINAL);

            assertTrue(result.isFeasible());
            assertEquals(250L, result.getAccommodatablePopulation());
        }

        @Test
        @DisplayName("Passes candidate with unbounded capacity (null)")
        void testUnboundedCapacityPasses() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 1000L);
            CandidateSafeSiteDto site = createSite("SITE-09", 26.6000, 85.5100, null,
                    HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE);
            site.setAvailableCapacity(null);

            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 1000L, 50.0, SuitabilityClass.MARGINAL);

            assertTrue(result.isFeasible());
            assertEquals(1000L, result.getAccommodatablePopulation());
        }
    }

    @Nested
    @DisplayName("Gate 5: Distance Gate")
    class DistanceGateTests {

        @Test
        @DisplayName("Rejects candidate exceeding max allowable transit distance")
        void testDistanceExceededRejected() {
            // Origin at Sitamarhi (~26.59, 85.50), destination far away (~25.59, 85.50 is ~110km)
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 100L);
            CandidateSafeSiteDto site = createSite("SITE-10", 25.5950, 85.5030, 500,
                    HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE);

            // Max distance: 20.0 km
            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 100L, 20.0, SuitabilityClass.MARGINAL);

            assertFalse(result.isFeasible());
            assertEquals("REJECTED_DISTANCE_EXCEEDED", result.getRejectionReasonCode());
        }

        @Test
        @DisplayName("Rejects candidate with missing coordinates when distance constraint specified")
        void testMissingCoordinatesRejected() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 100L);
            CandidateSafeSiteDto site = createSite("SITE-11", 0.0, 0.0, 500,
                    HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE);
            site.setLatitude(null); // Missing coords

            RecommendedDestinationDto result = engine.evaluateSingleCandidate(hab, site, 100L, 25.0, SuitabilityClass.MARGINAL);

            assertFalse(result.isFeasible());
            assertEquals("REJECTED_MISSING_COORDINATES", result.getRejectionReasonCode());
        }
    }

    @Nested
    @DisplayName("Batch Candidate Filtering")
    class BatchFilteringTests {

        @Test
        @DisplayName("evaluateCandidates evaluates all items and preserves count")
        void testEvaluateCandidatesBatch() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 100L);
            List<CandidateSafeSiteDto> sites = Arrays.asList(
                    createSite("S1", 26.6000, 85.5100, 500, HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE),
                    createSite("S2", 26.6000, 85.5100, 500, HazardSafetyStatus.AT_RISK, SuitabilityClass.SUITABLE),
                    createSite("S3", 26.6000, 85.5100, 50, HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE) // insufficient cap
            );

            List<RecommendedDestinationDto> evals = engine.evaluateCandidates(hab, sites, 50.0, SuitabilityClass.MARGINAL);

            assertEquals(3, evals.size());
            assertTrue(evals.get(0).isFeasible());
            assertFalse(evals.get(1).isFeasible());
            assertFalse(evals.get(2).isFeasible());
        }

        @Test
        @DisplayName("Empty or null candidate list returns empty list")
        void testEmptyOrNullCandidates() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", 26.5950, 85.5030, 100L);

            assertTrue(engine.evaluateCandidates(hab, null, 25.0, null).isEmpty());
            assertTrue(engine.evaluateCandidates(hab, Collections.emptyList(), 25.0, null).isEmpty());
        }
    }
}
