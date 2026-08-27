package com.hazard.service.relocation;

import com.hazard.dto.relocation.RecommendedDestinationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Stage 7B.5 — Destination Ranking Engine.
 *
 * Deterministically ranks evaluated candidate safe sites for a relocation case from best to worst:
 * 1. Feasibility Gate: Feasible sites strictly ranked above unfeasible ones.
 * 2. Destination Suitability Score: Higher score ranked first (descending).
 * 3. Suitability Tier: Higher suitability tier (HIGHLY_SUITABLE > SUITABLE > MARGINAL).
 * 4. Transit Distance: Shorter transit distance preferred (ascending).
 * 5. Available Capacity: Larger available shelter headroom preferred (descending).
 * 6. Stable Tie-Breaker: Deterministic site identifier (alphabetical ascending).
 *
 * Guaranteed: Zero mutation of input collections; thread-safe; returns new sorted list.
 */
@Component
public class DestinationRankingEngine {

    private static final Logger log = LoggerFactory.getLogger(DestinationRankingEngine.class);

    public static final Comparator<RecommendedDestinationDto> DESTINATION_COMPARATOR = (d1, d2) -> {
        if (d1 == d2) return 0;
        if (d1 == null) return 1;
        if (d2 == null) return -1;

        // 1. FEASIBILITY (Feasible > Unfeasible)
        if (d1.isFeasible() != d2.isFeasible()) {
            return d1.isFeasible() ? -1 : 1;
        }

        // 2. DESTINATION SCORE (Higher = Better)
        Double score1 = d1.getDestinationScore();
        Double score2 = d2.getDestinationScore();
        if (score1 != null && score2 != null) {
            int comp = Double.compare(score2, score1); // descending
            if (comp != 0) return comp;
        } else if (score1 != null) {
            return -1;
        } else if (score2 != null) {
            return 1;
        }

        // 3. SUITABILITY TIER (Lower tier integer = Better)
        int tier1 = d1.getSuitabilityClass() != null ? d1.getSuitabilityClass().getTierLevel() : 99;
        int tier2 = d2.getSuitabilityClass() != null ? d2.getSuitabilityClass().getTierLevel() : 99;
        if (tier1 != tier2) {
            return Integer.compare(tier1, tier2);
        }

        // 4. TRANSIT DISTANCE (Shorter = Better; Known > Missing)
        Double dist1 = d1.getDistanceMeters();
        Double dist2 = d2.getDistanceMeters();
        if (dist1 != null && dist2 != null) {
            int comp = Double.compare(dist1, dist2); // ascending
            if (comp != 0) return comp;
        } else if (dist1 != null) {
            return -1;
        } else if (dist2 != null) {
            return 1;
        }

        // 5. AVAILABLE CAPACITY (Larger = Better; Unbounded null > Bounded)
        Integer cap1 = d1.getAvailableCapacity();
        Integer cap2 = d2.getAvailableCapacity();
        if (cap1 != null && cap2 != null) {
            int comp = Integer.compare(cap2, cap1); // descending
            if (comp != 0) return comp;
        } else if (cap1 == null && cap2 != null) {
            return -1; // Unbounded preferred
        } else if (cap1 != null && cap2 == null) {
            return 1;
        }

        // 6. STABLE TIE-BREAKER (Site ID alphabetical ascending)
        String id1 = d1.getSiteId() != null ? d1.getSiteId() : "";
        String id2 = d2.getSiteId() != null ? d2.getSiteId() : "";
        return id1.compareTo(id2);
    };

    /**
     * Ranks a collection of evaluated candidate destinations deterministically.
     *
     * @param candidates the evaluated destination candidates
     * @return a new sorted list with 1-based ranks assigned
     */
    public List<RecommendedDestinationDto> rankDestinations(List<RecommendedDestinationDto> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter out null items without mutating source
        List<RecommendedDestinationDto> validList = new ArrayList<>(candidates.size());
        for (RecommendedDestinationDto d : candidates) {
            if (d != null) {
                validList.add(d);
            }
        }

        if (validList.isEmpty()) {
            return Collections.emptyList();
        }

        // Sort new copy
        List<RecommendedDestinationDto> sorted = new ArrayList<>(validList);
        sorted.sort(DESTINATION_COMPARATOR);

        // Assign 1-based sequential ranks
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setDestinationRank(i + 1);
        }

        return sorted;
    }
}
