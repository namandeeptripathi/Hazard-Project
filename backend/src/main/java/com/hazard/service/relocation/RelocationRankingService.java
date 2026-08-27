package com.hazard.service.relocation;

import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.*;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stage 6.4 — Relocation Site Ranking Service.
 *
 * Deterministically ranks already-feasible candidate safe sites for a vulnerable habitation
 * from best to worst using a multi-criteria lexicographic priority:
 *
 * Priority Order:
 * 1. Feasibility: Strictly processes only candidates that passed Stage 6.2 feasibility gates.
 * 2. Suitability: Higher SuitabilityClass tier (HIGHLY_SUITABLE > SUITABLE > MARGINAL) and higher score.
 * 3. Distance: Shorter transit distance from the vulnerable habitation (valid distance preferred over unavailable).
 * 4. Available Capacity: Higher remaining available shelter capacity.
 * 5. Stable Tie-Breaker: Deterministic site identifier (alphabetical ascending).
 */
@Service
public class RelocationRankingService {

    private static final Logger log = LoggerFactory.getLogger(RelocationRankingService.class);

    private final RelocationFeasibilityService feasibilityService;

    @Autowired
    public RelocationRankingService(RelocationFeasibilityService feasibilityService) {
        this.feasibilityService = feasibilityService;
    }

    /**
     * Default constructor for isolated unit testing.
     */
    public RelocationRankingService() {
        this.feasibilityService = new RelocationFeasibilityService();
    }

    /**
     * Convenience method: runs feasibility evaluation and ranks the resulting feasible candidate sites.
     */
    public RelocationRankingResultDto evaluateAndRank(VulnerableHabitationDto habitation,
                                                    List<CandidateSafeSiteDto> candidateSites,
                                                    RelocationRequestDto request) {
        RelocationFeasibilityResultDto feasibilityResult = feasibilityService.evaluateFeasibility(
                habitation, candidateSites, request
        );
        return rankFeasibleSites(feasibilityResult);
    }

    /**
     * Convenience method: runs feasibility evaluation with direct parameter constraints and ranks feasible sites.
     */
    public RelocationRankingResultDto evaluateAndRank(VulnerableHabitationDto habitation,
                                                    List<CandidateSafeSiteDto> candidateSites,
                                                    Double maxTransitDistanceKm,
                                                    SuitabilityClass minSuitabilityClass) {
        RelocationFeasibilityResultDto feasibilityResult = feasibilityService.evaluateFeasibility(
                habitation, candidateSites, maxTransitDistanceKm, minSuitabilityClass
        );
        return rankFeasibleSites(feasibilityResult);
    }

    /**
     * Core ranking method: ranks the feasible candidates from an existing RelocationFeasibilityResultDto.
     *
     * @param feasibilityResult the pre-evaluated feasibility result from Stage 6.2/6.3
     * @return RelocationRankingResultDto with ordered, ranked sites and explainable ranking reasons
     */
    public RelocationRankingResultDto rankFeasibleSites(RelocationFeasibilityResultDto feasibilityResult) {
        if (feasibilityResult == null) {
            log.warn("Null feasibility result provided to RelocationRankingService");
            RelocationRankingResultDto empty = new RelocationRankingResultDto();
            empty.setRankingSummary("Null feasibility result provided; ranking skipped.");
            return empty;
        }

        VulnerableHabitationDto habitation = new VulnerableHabitationDto();
        habitation.setHabitationId(feasibilityResult.getHabitationId());
        habitation.setHabitationName(feasibilityResult.getHabitationName());
        habitation.setDistrict(feasibilityResult.getDistrict());
        habitation.setVulnerablePopulation(feasibilityResult.getVulnerablePopulation());

        RelocationRankingResultDto result = new RelocationRankingResultDto(habitation);

        List<SiteFeasibilityEvaluationDto> evaluations = feasibilityResult.getEvaluations();
        if (evaluations == null || evaluations.isEmpty()) {
            result.setRankingSummary("No evaluated candidates available to rank for habitation " + feasibilityResult.getHabitationName());
            return result;
        }

        // Filter: ONLY feasible candidate evaluations are ranked
        List<SiteFeasibilityEvaluationDto> feasibleEvaluations = evaluations.stream()
                .filter(SiteFeasibilityEvaluationDto::isFeasible)
                .collect(Collectors.toList());

        if (feasibleEvaluations.isEmpty()) {
            result.setRankingSummary("Zero feasible safe sites available to rank for habitation " + feasibilityResult.getHabitationName());
            return result;
        }

        // Sort feasible evaluations using deterministic lexicographic comparator
        feasibleEvaluations.sort(FEASIBLE_SITE_COMPARATOR);

        int totalFeasible = feasibleEvaluations.size();
        List<RankedRelocationSiteDto> rankedDtos = new ArrayList<>(totalFeasible);

        for (int i = 0; i < totalFeasible; i++) {
            int rank = i + 1;
            SiteFeasibilityEvaluationDto eval = feasibleEvaluations.get(i);
            eval.setRank(rank);

            RankedRelocationSiteDto rankedDto = new RankedRelocationSiteDto(rank, eval, totalFeasible);
            eval.setRankingReason(rankedDto.getRankingReason());
            rankedDtos.add(rankedDto);
        }

        result.setRankedSites(rankedDtos);

        String summary = String.format(
                "Ranked %d feasible relocation safe sites for %s (Top Destination: %s)",
                totalFeasible,
                feasibilityResult.getHabitationName() != null ? feasibilityResult.getHabitationName() : "Habitation",
                rankedDtos.get(0).getSiteName() + " [" + rankedDtos.get(0).getSiteId() + "]"
        );
        result.setRankingSummary(summary);
        log.info(summary);

        return result;
    }

    /**
     * Deterministic Lexicographic Comparator for Feasible Safe Site Evaluations:
     * 1. Suitability Tier (HIGHLY_SUITABLE > SUITABLE > MARGINAL)
     * 2. Suitability Score (higher is better)
     * 3. Geodesic Distance (shorter is better; known distance > unavailable distance)
     * 4. Available Shelter Capacity (greater capacity is better; unbounded > bounded)
     * 5. Stable Site Identifier (alphabetical natural order)
     */
    public static final Comparator<SiteFeasibilityEvaluationDto> FEASIBLE_SITE_COMPARATOR = (e1, e2) -> {
        if (e1 == e2) return 0;
        if (e1 == null) return 1;
        if (e2 == null) return -1;

        // 1. SUITABILITY TIER (Lower tierLevel integer = better)
        int tier1 = e1.getSuitabilityClass() != null ? e1.getSuitabilityClass().getTierLevel() : 99;
        int tier2 = e2.getSuitabilityClass() != null ? e2.getSuitabilityClass().getTierLevel() : 99;
        if (tier1 != tier2) {
            return Integer.compare(tier1, tier2);
        }

        // 1b. SUITABILITY SCORE (Higher score = better)
        Double score1 = e1.getSuitabilityScore();
        Double score2 = e2.getSuitabilityScore();
        if (score1 != null && score2 != null) {
            int scoreComp = Double.compare(score2, score1); // descending
            if (scoreComp != 0) {
                return scoreComp;
            }
        } else if (score1 != null) {
            return -1;
        } else if (score2 != null) {
            return 1;
        }

        // 2. DISTANCE (Shorter distance = better; Available distance > Missing distance)
        boolean distAvail1 = e1.isDistanceAvailable() && e1.getDistanceMeters() != null;
        boolean distAvail2 = e2.isDistanceAvailable() && e2.getDistanceMeters() != null;

        if (distAvail1 && distAvail2) {
            int distComp = Double.compare(e1.getDistanceMeters(), e2.getDistanceMeters()); // ascending
            if (distComp != 0) {
                return distComp;
            }
        } else if (distAvail1 && !distAvail2) {
            return -1; // e1 has valid distance, ranks better
        } else if (!distAvail1 && distAvail2) {
            return 1;  // e2 has valid distance, ranks better
        }

        // 3. AVAILABLE CAPACITY (Greater capacity = better; Unbounded null > Bounded)
        Integer cap1 = e1.getAvailableCapacity();
        Integer cap2 = e2.getAvailableCapacity();

        if (cap1 != null && cap2 != null) {
            int capComp = Integer.compare(cap2, cap1); // descending
            if (capComp != 0) {
                return capComp;
            }
        } else if (cap1 == null && cap2 != null) {
            return -1; // Unbounded capacity ranks above bounded
        } else if (cap1 != null && cap2 == null) {
            return 1;  // Unbounded capacity ranks above bounded
        }

        // 4. STABLE TIE-BREAKER (Site ID alphabetical ascending)
        String id1 = e1.getSiteId() != null ? e1.getSiteId() : "";
        String id2 = e2.getSiteId() != null ? e2.getSiteId() : "";
        return id1.compareTo(id2);
    };
}
