package com.hazard.service.safesite;

import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Stage 5.11 — Candidate Safe-Site Ranking Evaluator.
 *
 * Ranks candidate safe sites using the multi-criteria suitability results already computed in Stage 5.10.
 *
 * Deterministic Hierarchical Ranking Order:
 * 1. Primary: SuitabilityClass Tier
 *    (HIGHLY_SUITABLE -> SUITABLE -> MARGINAL -> UNSUITABLE -> UNKNOWN)
 * 2. Secondary: Suitability Score DESC (nulls last)
 * 3. Tertiary: Data Completeness Percentage DESC (nulls last)
 * 4. Quaternary: Site ID ASC (lexicographical deterministic tie-breaker)
 *
 * Assigns sequential 1-based integer rank (1, 2, 3... N) and generates an explainable rankingReason.
 * This component does NOT recalculate any prior stage dimensions or suitability scores.
 */
@Component
public class SafeSiteRankingEvaluator {

    private static final Logger log = LoggerFactory.getLogger(SafeSiteRankingEvaluator.class);

    /**
     * Deterministic hierarchical comparator for candidate safe-site ranking.
     */
    public static final Comparator<CandidateSafeSiteDto> RANKING_COMPARATOR = Comparator
            .comparing(CandidateSafeSiteDto::getSuitabilityClass, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(CandidateSafeSiteDto::getSuitabilityScore, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(CandidateSafeSiteDto::getDataCompletenessPercentage, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(CandidateSafeSiteDto::getSiteId, Comparator.nullsLast(Comparator.naturalOrder()));

    /**
     * Ranks a collection of evaluated candidate safe sites.
     *
     * @param sites List of evaluated CandidateSafeSiteDto instances
     * @return Sorted list with 1-based ranks and rankingReason assigned
     */
    public List<CandidateSafeSiteDto> rankCandidateSites(List<CandidateSafeSiteDto> sites) {
        if (sites == null || sites.isEmpty()) {
            return Collections.emptyList();
        }

        List<CandidateSafeSiteDto> sortedSites = new ArrayList<>(sites);
        sortedSites.sort(RANKING_COMPARATOR);

        int totalCount = sortedSites.size();
        for (int i = 0; i < totalCount; i++) {
            CandidateSafeSiteDto site = sortedSites.get(i);
            int rank = i + 1;
            site.setRank(rank);
            site.setRankingReason(generateRankingReason(site, rank, totalCount));
        }

        log.debug("Ranked {} candidate safe sites for Stage 5.11", totalCount);
        return sortedSites;
    }

    /**
     * Generates an explainable human-readable rationale for the assigned candidate rank.
     */
    public String generateRankingReason(CandidateSafeSiteDto site, int rank, int totalCandidates) {
        if (site == null) {
            return "Rank #" + rank + " of " + totalCandidates;
        }

        SuitabilityClass suitabilityClass = site.getSuitabilityClass();
        Double score = site.getSuitabilityScore();
        Double completeness = site.getDataCompletenessPercentage();
        HazardSafetyStatus hazardStatus = site.getHazardSafetyStatus();

        String scoreStr = score != null ? String.format("%.1f", score) : "N/A";
        String completenessStr = completeness != null ? String.format("%.1f", completeness) + "%" : "N/A";

        if (suitabilityClass == null || suitabilityClass == SuitabilityClass.UNKNOWN) {
            return String.format("Rank #%d of %d: Suitability undetermined due to insufficient spatial dimension data.",
                    rank, totalCandidates);
        }

        switch (suitabilityClass) {
            case HIGHLY_SUITABLE:
                return String.format("Rank #%d of %d: Highly suitable safe site with top-tier suitability score (%s/100) and %s data completeness.",
                        rank, totalCandidates, scoreStr, completenessStr);

            case SUITABLE:
                return String.format("Rank #%d of %d: Suitable candidate safe site with suitability score %s/100 and %s data completeness.",
                        rank, totalCandidates, scoreStr, completenessStr);

            case MARGINAL:
                return String.format("Rank #%d of %d: Marginal candidate safe site with suitability score %s/100 and %s data completeness.",
                        rank, totalCandidates, scoreStr, completenessStr);

            case UNSUITABLE:
                if (hazardStatus == HazardSafetyStatus.AT_RISK) {
                    return String.format("Rank #%d of %d: Unsuitable safe site due to active hazard exposure override (AT_RISK); diagnostic non-hazard score is %s/100.",
                            rank, totalCandidates, scoreStr);
                } else {
                    return String.format("Rank #%d of %d: Unsuitable candidate safe site due to low multi-criteria suitability score (%s/100).",
                            rank, totalCandidates, scoreStr);
                }

            default:
                return String.format("Rank #%d of %d: Candidate classified as %s with score %s/100.",
                        rank, totalCandidates, suitabilityClass.name(), scoreStr);
        }
    }
}
