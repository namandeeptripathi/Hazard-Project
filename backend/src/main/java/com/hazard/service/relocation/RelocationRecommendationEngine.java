package com.hazard.service.relocation;

import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.*;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stage 7B.6 & 7B.7 — Relocation Recommendation Generation & Validation Engine.
 *
 * Coordinates the full Stage 7B decision pipeline:
 * Input (Habitation + Priority + Candidates)
 *   ↓
 * Validation & Source Check (7B.7)
 *   ↓
 * Hard Feasibility Filtering (7B.3)
 *   ↓
 * Destination Scoring (7B.4)
 *   ↓
 * Destination Ranking (7B.5)
 *   ↓
 * Recommendation Synthesis (Primary Destination + Alternatives) (7B.6)
 */
@Component
public class RelocationRecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(RelocationRecommendationEngine.class);

    private final DestinationFilteringEngine filteringEngine;
    private final DestinationScoringEngine scoringEngine;
    private final DestinationRankingEngine rankingEngine;

    @Autowired
    public RelocationRecommendationEngine(DestinationFilteringEngine filteringEngine,
                                         DestinationScoringEngine scoringEngine,
                                         DestinationRankingEngine rankingEngine) {
        this.filteringEngine = filteringEngine;
        this.scoringEngine = scoringEngine;
        this.rankingEngine = rankingEngine;
    }

    public RelocationRecommendationEngine() {
        this.filteringEngine = new DestinationFilteringEngine();
        this.scoringEngine = new DestinationScoringEngine();
        this.rankingEngine = new DestinationRankingEngine();
    }

    /**
     * Generates a complete RelocationRecommendationDto for a prioritized relocation case.
     *
     * @param habitation          the origin vulnerable habitation
     * @param candidateSites      the raw candidate safe sites available in the region
     * @param priorityResult      the Stage 7A priority assessment (optional)
     * @param maxTransitDistanceKm optional maximum allowable transit distance in km
     * @param minSuitabilityClass optional minimum acceptable suitability tier
     * @return structured, validated RelocationRecommendationDto
     */
    public RelocationRecommendationDto generateRecommendation(VulnerableHabitationDto habitation,
                                                              List<CandidateSafeSiteDto> candidateSites,
                                                              RelocationPriorityResultDto priorityResult,
                                                              Double maxTransitDistanceKm,
                                                              com.hazard.domain.safesite.SuitabilityClass minSuitabilityClass) {
        RelocationRecommendationDto rec = new RelocationRecommendationDto();
        rec.setTimestamp(LocalDateTime.now());
        rec.setRecommendationId("REC-" + (habitation != null && habitation.getHabitationId() != null
                ? habitation.getHabitationId()
                : "HAB") + "-" + System.currentTimeMillis());

        // 1. Validate Origin Habitation
        if (habitation == null) {
            rec.setStatus(RecommendationStatus.INVALID_SOURCE);
            rec.setRecommendationSummary("Invalid source: Origin vulnerable habitation reference is null.");
            return rec;
        }

        // Populate origin context
        rec.setHabitationId(habitation.getHabitationId());
        rec.setHabitationName(habitation.getHabitationName() != null ? habitation.getHabitationName() : habitation.getHabitationId());
        rec.setDistrict(habitation.getDistrict());
        rec.setState(habitation.getState() != null ? habitation.getState() : "Bihar");
        rec.setOriginLatitude(habitation.getLatitude());
        rec.setOriginLongitude(habitation.getLongitude());
        rec.setUrgency(habitation.getUrgency());

        long requiredPopulation = resolveRequiredPopulation(habitation);
        rec.setVulnerablePopulation(requiredPopulation);

        // Populate Stage 7A Priority Context if present
        if (priorityResult != null) {
            rec.setPriorityScore(priorityResult.getPriorityScore());
            rec.setPriorityLevel(priorityResult.getPriorityLevel());
            rec.setPriorityRank(priorityResult.getPriorityRank());
            rec.setPlanId(priorityResult.getPlanId());
        }

        // Edge Case: Population is 0 or negative
        if (requiredPopulation <= 0) {
            rec.setStatus(RecommendationStatus.RECOMMENDED);
            rec.setAllocatedPopulation(0L);
            rec.setUnallocatedPopulation(0L);
            rec.setCapacityFitRatePercentage(100.0);
            rec.setRecommendationSummary(String.format(
                    "Habitation '%s' requires no relocation (vulnerable population: %d).",
                    rec.getHabitationName(), requiredPopulation
            ));
            return rec;
        }

        // 2. Validate Candidate Sites Existence
        if (candidateSites == null || candidateSites.isEmpty()) {
            rec.setStatus(RecommendationStatus.NO_FEASIBLE_DESTINATION);
            rec.setTotalCandidatesEvaluated(0);
            rec.setTotalFeasibleCandidates(0);
            rec.setAllocatedPopulation(0L);
            rec.setUnallocatedPopulation(requiredPopulation);
            rec.setCapacityFitRatePercentage(0.0);
            rec.setRecommendationSummary(String.format(
                    "No destination safe sites available in regional catalog for habitation '%s' (Population: %d).",
                    rec.getHabitationName(), requiredPopulation
            ));
            return rec;
        }

        rec.setTotalCandidatesEvaluated(candidateSites.size());

        // 3. Filter Candidates against Hard Feasibility Gates (7B.3)
        List<RecommendedDestinationDto> evaluatedCandidates = filteringEngine.evaluateCandidates(
                habitation, candidateSites, maxTransitDistanceKm, minSuitabilityClass
        );

        // 4. Score Feasible Candidates (7B.4)
        for (int i = 0; i < evaluatedCandidates.size(); i++) {
            RecommendedDestinationDto eval = evaluatedCandidates.get(i);
            CandidateSafeSiteDto rawSite = (i < candidateSites.size()) ? candidateSites.get(i) : null;
            scoringEngine.scoreDestination(habitation, eval, rawSite, maxTransitDistanceKm);
        }

        // 5. Rank Candidates Deterministically (7B.5)
        List<RecommendedDestinationDto> rankedCandidates = rankingEngine.rankDestinations(evaluatedCandidates);

        // Partition feasible vs unfeasible
        List<RecommendedDestinationDto> feasibleCandidates = new ArrayList<>();
        for (RecommendedDestinationDto d : rankedCandidates) {
            if (d.isFeasible()) {
                feasibleCandidates.add(d);
            }
        }

        rec.setTotalFeasibleCandidates(feasibleCandidates.size());

        // 6. Handle No Feasible Candidates
        if (feasibleCandidates.isEmpty()) {
            rec.setStatus(RecommendationStatus.NO_FEASIBLE_DESTINATION);
            rec.setAllocatedPopulation(0L);
            rec.setUnallocatedPopulation(requiredPopulation);
            rec.setCapacityFitRatePercentage(0.0);

            // Find primary rejection reason
            String firstReason = !rankedCandidates.isEmpty() && rankedCandidates.get(0).getRejectionReason() != null
                    ? rankedCandidates.get(0).getRejectionReason()
                    : "No safe site met safety, suitability, distance, and capacity requirements";

            rec.setRecommendationSummary(String.format(
                    "No feasible destination found for '%s' (Pop: %d): %d candidate(s) evaluated, all rejected. Reason: %s",
                    rec.getHabitationName(), requiredPopulation, rankedCandidates.size(), firstReason
            ));
            return rec;
        }

        // 7. Select Primary & Alternative Destinations
        RecommendedDestinationDto primary = feasibleCandidates.get(0);
        rec.setPrimaryDestination(primary);

        // Alternatives (ranks 2..N)
        for (int i = 1; i < feasibleCandidates.size(); i++) {
            rec.addAlternativeDestination(feasibleCandidates.get(i));
        }

        // 8. Determine Population Allocation & Status
        long allocated;
        if (primary.getAvailableCapacity() != null) {
            allocated = Math.min(requiredPopulation, (long) primary.getAvailableCapacity());
        } else {
            allocated = requiredPopulation; // Unbounded accommodates all
        }

        long unallocated = Math.max(0L, requiredPopulation - allocated);
        rec.setAllocatedPopulation(allocated);
        rec.setUnallocatedPopulation(unallocated);

        double fitRate = (requiredPopulation > 0)
                ? Math.round(((double) allocated / requiredPopulation) * 10000.0) / 100.0
                : 100.0;
        rec.setCapacityFitRatePercentage(fitRate);

        if (unallocated == 0) {
            rec.setStatus(RecommendationStatus.RECOMMENDED);
            rec.setRecommendationSummary(String.format(
                    "Recommended destination for %s (Pop: %d): '%s' [%s] in %s. " +
                            "Distance: %s, Score: %.4f, Suitability: %s, Remaining Shelter Capacity: %s.",
                    rec.getHabitationName(),
                    requiredPopulation,
                    primary.getSiteName(),
                    primary.getSiteId(),
                    primary.getDistrict() != null ? primary.getDistrict() : rec.getDistrict(),
                    primary.getDistanceKilometers() != null ? String.format("%.2f km", primary.getDistanceKilometers()) : "N/A",
                    primary.getDestinationScore() != null ? primary.getDestinationScore() : 0.0,
                    primary.getSuitabilityClass() != null ? primary.getSuitabilityClass().name() : "N/A",
                    primary.getAvailableCapacity() != null ? primary.getAvailableCapacity().toString() : "Unbounded"
            ));
        } else {
            rec.setStatus(RecommendationStatus.CAPACITY_DEFICIT);
            rec.setRecommendationSummary(String.format(
                    "Capacity deficit for %s: '%s' [%s] can accommodate %d of %d evacuees (%d unallocated). " +
                            "Destination Score: %.4f.",
                    rec.getHabitationName(),
                    primary.getSiteName(),
                    primary.getSiteId(),
                    allocated,
                    requiredPopulation,
                    unallocated,
                    primary.getDestinationScore() != null ? primary.getDestinationScore() : 0.0
            ));
        }

        log.info(rec.getRecommendationSummary());
        return rec;
    }

    private long resolveRequiredPopulation(VulnerableHabitationDto habitation) {
        if (habitation == null) return 0L;
        if (habitation.getVulnerablePopulation() != null && habitation.getVulnerablePopulation() > 0) {
            return habitation.getVulnerablePopulation();
        }
        if (habitation.getTotalPopulation() != null && habitation.getTotalPopulation() > 0) {
            return habitation.getTotalPopulation();
        }
        return 0L;
    }
}
