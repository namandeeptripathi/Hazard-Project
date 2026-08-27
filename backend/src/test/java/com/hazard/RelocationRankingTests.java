package com.hazard;

import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.*;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.relocation.RelocationFeasibilityService;
import com.hazard.service.relocation.RelocationRankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for Stage 6.4 — Relocation Intelligence: Feasible Site Ranking.
 */
class RelocationRankingTests {

    private RelocationFeasibilityService feasibilityService;
    private RelocationRankingService rankingService;

    @BeforeEach
    void setUp() {
        feasibilityService = new RelocationFeasibilityService();
        rankingService = new RelocationRankingService(feasibilityService);
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

    private SiteFeasibilityEvaluationDto createFeasibleEvaluation(String id, String name,
                                                                  SuitabilityClass suitability, Double score,
                                                                  Double distanceMeters, Integer capacity, Integer occupied) {
        SiteFeasibilityEvaluationDto eval = new SiteFeasibilityEvaluationDto();
        eval.setSiteId(id);
        eval.setSiteName(name);
        eval.setCategory("EMERGENCY_SHELTER");
        eval.setDistrict("Sitamarhi");
        eval.setFeasible(true);
        eval.setSafetyPassed(true);
        eval.setSuitabilityPassed(true);
        eval.setCapacityPassed(true);
        eval.setDistancePassed(true);
        eval.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
        eval.setSuitabilityClass(suitability);
        eval.setSuitabilityScore(score);
        eval.setTransitDistanceMeters(distanceMeters);
        eval.setDistanceAvailable(distanceMeters != null);
        eval.setTotalCapacity(capacity);
        eval.setAllocatedOccupancy(occupied != null ? occupied : 0);
        eval.setAvailableCapacity(capacity != null ? Math.max(0, capacity - (occupied != null ? occupied : 0)) : null);
        return eval;
    }

    @Test
    @DisplayName("1. Higher suitability ranks above lower suitability (Priority 1)")
    void testHigherSuitabilityRanksAboveLower() {
        // Site A: HIGHLY_SUITABLE, but farther (15 km)
        SiteFeasibilityEvaluationDto siteA = createFeasibleEvaluation(
                "FAC-A", "High Suitability School", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 15000.0, 500, 0
        );

        // Site B: SUITABLE, but closer (5 km)
        SiteFeasibilityEvaluationDto siteB = createFeasibleEvaluation(
                "FAC-B", "Moderate Shelter", SuitabilityClass.SUITABLE, 78.0, 5000.0, 500, 0
        );

        RelocationFeasibilityResultDto feasResult = new RelocationFeasibilityResultDto();
        feasResult.setHabitationName("Rampur");
        feasResult.setEvaluations(List.of(siteB, siteA));

        RelocationRankingResultDto rankedResult = rankingService.rankFeasibleSites(feasResult);

        assertEquals(2, rankedResult.getTotalFeasibleSites());
        assertEquals("FAC-A", rankedResult.getRankedSites().get(0).getSiteId());
        assertEquals(1, rankedResult.getRankedSites().get(0).getRank());
        assertEquals("FAC-B", rankedResult.getRankedSites().get(1).getSiteId());
        assertEquals(2, rankedResult.getRankedSites().get(1).getRank());
    }

    @Test
    @DisplayName("1b. Within same suitability tier, higher score ranks above lower score")
    void testWithinSameTierHigherScoreRanksAbove() {
        SiteFeasibilityEvaluationDto siteA = createFeasibleEvaluation(
                "FAC-A", "Score 88 Site", SuitabilityClass.SUITABLE, 88.0, 10000.0, 500, 0
        );
        SiteFeasibilityEvaluationDto siteB = createFeasibleEvaluation(
                "FAC-B", "Score 72 Site", SuitabilityClass.SUITABLE, 72.0, 10000.0, 500, 0
        );

        RelocationFeasibilityResultDto feasResult = new RelocationFeasibilityResultDto();
        feasResult.setEvaluations(List.of(siteB, siteA));

        RelocationRankingResultDto rankedResult = rankingService.rankFeasibleSites(feasResult);

        assertEquals("FAC-A", rankedResult.getRankedSites().get(0).getSiteId());
        assertEquals("FAC-B", rankedResult.getRankedSites().get(1).getSiteId());
    }

    @Test
    @DisplayName("2. Same suitability -> shorter distance ranks higher (Priority 2)")
    void testSameSuitabilityShorterDistanceRanksHigher() {
        // Both HIGHLY_SUITABLE with same score 95.0
        // Site A: 4.2 km
        SiteFeasibilityEvaluationDto siteA = createFeasibleEvaluation(
                "FAC-NEAR", "Near Shelter", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 4200.0, 500, 0
        );

        // Site B: 18.5 km
        SiteFeasibilityEvaluationDto siteB = createFeasibleEvaluation(
                "FAC-FAR", "Far Shelter", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 18500.0, 500, 0
        );

        RelocationFeasibilityResultDto feasResult = new RelocationFeasibilityResultDto();
        feasResult.setEvaluations(List.of(siteB, siteA));

        RelocationRankingResultDto rankedResult = rankingService.rankFeasibleSites(feasResult);

        assertEquals("FAC-NEAR", rankedResult.getRankedSites().get(0).getSiteId());
        assertEquals(1, rankedResult.getRankedSites().get(0).getRank());
        assertEquals("FAC-FAR", rankedResult.getRankedSites().get(1).getSiteId());
        assertEquals(2, rankedResult.getRankedSites().get(1).getRank());
    }

    @Test
    @DisplayName("3. Same suitability and distance -> greater available capacity ranks higher (Priority 3)")
    void testSameSuitabilityAndDistanceGreaterCapacityRanksHigher() {
        // Both SUITABLE (80.0), Distance = 8.0 km
        // Site A: Cap 800, Occ 100 -> Available 700
        SiteFeasibilityEvaluationDto siteA = createFeasibleEvaluation(
                "FAC-BIG", "Large High-Capacity Hall", SuitabilityClass.SUITABLE, 80.0, 8000.0, 800, 100
        );

        // Site B: Cap 400, Occ 200 -> Available 200
        SiteFeasibilityEvaluationDto siteB = createFeasibleEvaluation(
                "FAC-SMALL", "Small Hall", SuitabilityClass.SUITABLE, 80.0, 8000.0, 400, 200
        );

        RelocationFeasibilityResultDto feasResult = new RelocationFeasibilityResultDto();
        feasResult.setEvaluations(List.of(siteB, siteA));

        RelocationRankingResultDto rankedResult = rankingService.rankFeasibleSites(feasResult);

        assertEquals("FAC-BIG", rankedResult.getRankedSites().get(0).getSiteId());
        assertEquals(700, rankedResult.getRankedSites().get(0).getAvailableCapacity());
        assertEquals("FAC-SMALL", rankedResult.getRankedSites().get(1).getSiteId());
        assertEquals(200, rankedResult.getRankedSites().get(1).getAvailableCapacity());
    }

    @Test
    @DisplayName("4. Exact tie across all criteria -> stable site ID determines order (Priority 4)")
    void testExactTieStableIdDeterminesOrder() {
        // Identical suitability, score, distance, and capacity
        SiteFeasibilityEvaluationDto siteB = createFeasibleEvaluation(
                "FAC-002", "Shelter B", SuitabilityClass.HIGHLY_SUITABLE, 90.0, 6000.0, 500, 0
        );
        SiteFeasibilityEvaluationDto siteA = createFeasibleEvaluation(
                "FAC-001", "Shelter A", SuitabilityClass.HIGHLY_SUITABLE, 90.0, 6000.0, 500, 0
        );
        SiteFeasibilityEvaluationDto siteC = createFeasibleEvaluation(
                "FAC-003", "Shelter C", SuitabilityClass.HIGHLY_SUITABLE, 90.0, 6000.0, 500, 0
        );

        RelocationFeasibilityResultDto feasResult = new RelocationFeasibilityResultDto();
        feasResult.setEvaluations(List.of(siteC, siteB, siteA));

        RelocationRankingResultDto rankedResult = rankingService.rankFeasibleSites(feasResult);

        assertEquals(3, rankedResult.getTotalFeasibleSites());
        assertEquals("FAC-001", rankedResult.getRankedSites().get(0).getSiteId());
        assertEquals("FAC-002", rankedResult.getRankedSites().get(1).getSiteId());
        assertEquals("FAC-003", rankedResult.getRankedSites().get(2).getSiteId());
    }

    @Test
    @DisplayName("5. Missing distance is handled explicitly and valid distance preferred")
    void testMissingDistanceHandledDeterministically() {
        // Site A: distance = 10 km (valid)
        SiteFeasibilityEvaluationDto siteWithDist = createFeasibleEvaluation(
                "FAC-DIST", "Shelter With Distance", SuitabilityClass.SUITABLE, 80.0, 10000.0, 500, 0
        );

        // Site B: distance = null (missing coordinates, but allowed when no max distance set)
        SiteFeasibilityEvaluationDto siteNoDist = createFeasibleEvaluation(
                "FAC-NO-DIST", "Shelter Without Distance", SuitabilityClass.SUITABLE, 80.0, null, 500, 0
        );

        RelocationFeasibilityResultDto feasResult = new RelocationFeasibilityResultDto();
        feasResult.setEvaluations(List.of(siteNoDist, siteWithDist));

        RelocationRankingResultDto rankedResult = rankingService.rankFeasibleSites(feasResult);

        // Site with known distance should rank above site with unavailable distance
        assertEquals("FAC-DIST", rankedResult.getRankedSites().get(0).getSiteId());
        assertTrue(rankedResult.getRankedSites().get(0).isDistanceAvailable());
        assertEquals("FAC-NO-DIST", rankedResult.getRankedSites().get(1).getSiteId());
        assertFalse(rankedResult.getRankedSites().get(1).isDistanceAvailable());
        assertNull(rankedResult.getRankedSites().get(1).getDistanceKilometers());
    }

    @Test
    @DisplayName("6. Only feasible candidates are ranked (infeasible filtered out)")
    void testOnlyFeasibleCandidatesAreRanked() {
        SiteFeasibilityEvaluationDto feasible1 = createFeasibleEvaluation(
                "FAC-OK-1", "Safe School", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 5000.0, 500, 0
        );

        // Infeasible site (e.g. unsafe or capacity exceeded)
        SiteFeasibilityEvaluationDto infeasible = createFeasibleEvaluation(
                "FAC-UNSAFE", "Flooded Area Hall", SuitabilityClass.UNSUITABLE, 20.0, 1000.0, 500, 0
        );
        infeasible.setFeasible(false);
        infeasible.setRejectionReasonCode("REJECTED_UNSAFE");

        SiteFeasibilityEvaluationDto feasible2 = createFeasibleEvaluation(
                "FAC-OK-2", "Safe Community Hall", SuitabilityClass.SUITABLE, 75.0, 8000.0, 300, 0
        );

        RelocationFeasibilityResultDto feasResult = new RelocationFeasibilityResultDto();
        feasResult.setEvaluations(List.of(feasible1, infeasible, feasible2));

        RelocationRankingResultDto rankedResult = rankingService.rankFeasibleSites(feasResult);

        // Only 2 feasible sites ranked; infeasible site is completely excluded
        assertEquals(2, rankedResult.getTotalFeasibleSites());
        assertEquals("FAC-OK-1", rankedResult.getRankedSites().get(0).getSiteId());
        assertEquals("FAC-OK-2", rankedResult.getRankedSites().get(1).getSiteId());
        assertFalse(rankedResult.getRankedSites().stream().anyMatch(s -> "FAC-UNSAFE".equals(s.getSiteId())));
    }

    @Test
    @DisplayName("7. Ranking does not modify site capacity or occupancy (read-only)")
    void testRankingDoesNotModifyCapacityOrOccupancy() {
        CandidateSafeSiteDto site = createCandidateSite(
                "FAC-STATIC", "Static Facility", 26.6500, 85.5200,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 95.0, 600, 150
        );

        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 200);

        // Run evaluateAndRank
        RelocationRankingResultDto result = rankingService.evaluateAndRank(
                hab, List.of(site), 25.0, SuitabilityClass.MARGINAL
        );

        assertEquals(1, result.getTotalFeasibleSites());
        assertEquals(600, site.getCapacity(), "Capacity must not be altered");
        assertEquals(150, site.getAllocatedOccupancy(), "Allocated occupancy must not be modified");
        assertEquals(450, site.getAvailableCapacity(), "Available capacity must remain 450");
    }

    @Test
    @DisplayName("8. Ranking is deterministic across repeated executions and list permutations")
    void testRankingIsDeterministicAcrossPermutations() {
        SiteFeasibilityEvaluationDto s1 = createFeasibleEvaluation("FAC-1", "S1", SuitabilityClass.HIGHLY_SUITABLE, 95.0, 5000.0, 500, 0);
        SiteFeasibilityEvaluationDto s2 = createFeasibleEvaluation("FAC-2", "S2", SuitabilityClass.HIGHLY_SUITABLE, 90.0, 5000.0, 500, 0);
        SiteFeasibilityEvaluationDto s3 = createFeasibleEvaluation("FAC-3", "S3", SuitabilityClass.SUITABLE, 85.0, 3000.0, 500, 0);
        SiteFeasibilityEvaluationDto s4 = createFeasibleEvaluation("FAC-4", "S4", SuitabilityClass.SUITABLE, 85.0, 7000.0, 500, 0);

        List<String> expectedOrder = List.of("FAC-1", "FAC-2", "FAC-3", "FAC-4");

        for (int i = 0; i < 10; i++) {
            List<SiteFeasibilityEvaluationDto> shuffled = new ArrayList<>(List.of(s1, s2, s3, s4));
            Collections.shuffle(shuffled);

            RelocationFeasibilityResultDto feasResult = new RelocationFeasibilityResultDto();
            feasResult.setEvaluations(shuffled);

            RelocationRankingResultDto ranked = rankingService.rankFeasibleSites(feasResult);
            List<String> actualOrder = ranked.getRankedSites().stream().map(RankedRelocationSiteDto::getSiteId).toList();
            assertEquals(expectedOrder, actualOrder, "Ranking order must be strictly deterministic across executions");
        }
    }

    @Test
    @DisplayName("9. Empty feasible list returns empty result")
    void testEmptyFeasibleListReturnsEmptyResult() {
        // Feasibility result with only infeasible evaluations
        SiteFeasibilityEvaluationDto rejected = createFeasibleEvaluation(
                "FAC-REJ", "Rejected Site", SuitabilityClass.MARGINAL, 50.0, 50000.0, 100, 100
        );
        rejected.setFeasible(false);

        RelocationFeasibilityResultDto feasResult = new RelocationFeasibilityResultDto();
        feasResult.setHabitationName("Flooded Basti");
        feasResult.setEvaluations(List.of(rejected));

        RelocationRankingResultDto rankedResult = rankingService.rankFeasibleSites(feasResult);

        assertNotNull(rankedResult);
        assertEquals(0, rankedResult.getTotalFeasibleSites());
        assertTrue(rankedResult.getRankedSites().isEmpty());
        assertFalse(rankedResult.hasRankedSites());
        assertNull(rankedResult.getTopRankedSite());
        assertTrue(rankedResult.getRankingSummary().contains("Zero feasible"));
    }

    @Test
    @DisplayName("10. End-to-end integration: evaluateAndRank pipeline")
    void testEndToEndEvaluateAndRankPipeline() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 200);

        // 3 Sites:
        // 1. Feasible: Safe, Highly Suitable (95.0), Distance ~6.3 km, Cap 500 -> Rank 1
        CandidateSafeSiteDto s1 = createCandidateSite("S1", "Nearby High Suitable", 26.6500, 85.5200,
                HazardSafetyStatus.SAFE, SuitabilityClass.HIGHLY_SUITABLE, 95.0, 500, 0);

        // 2. Feasible: Safe, Suitable (80.0), Distance ~6.3 km, Cap 300 -> Rank 2
        CandidateSafeSiteDto s2 = createCandidateSite("S2", "Nearby Suitable", 26.6500, 85.5200,
                HazardSafetyStatus.SAFE, SuitabilityClass.SUITABLE, 80.0, 300, 0);

        // 3. Infeasible: Unsafe AT_RISK -> excluded
        CandidateSafeSiteDto s3 = createCandidateSite("S3", "Unsafe Inundated", 26.6000, 85.5050,
                HazardSafetyStatus.AT_RISK, SuitabilityClass.HIGHLY_SUITABLE, 95.0, 1000, 0);

        RelocationRequestDto req = new RelocationRequestDto();
        req.setMaxTransitDistanceKm(20.0);
        req.setMinSuitabilityClass(SuitabilityClass.SUITABLE);

        RelocationRankingResultDto result = rankingService.evaluateAndRank(hab, List.of(s1, s2, s3), req);

        assertEquals(2, result.getTotalFeasibleSites());
        assertEquals("S1", result.getRankedSites().get(0).getSiteId());
        assertEquals(1, result.getRankedSites().get(0).getRank());
        assertEquals("S2", result.getRankedSites().get(1).getSiteId());
        assertEquals(2, result.getRankedSites().get(1).getRank());
        assertNotNull(result.getRankedSites().get(0).getRankingReason());
        assertTrue(result.getRankedSites().get(0).getRankingReason().contains("Rank 1"));
    }
}
