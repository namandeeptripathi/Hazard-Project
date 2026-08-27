package com.hazard.service.relocation;

import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.dto.relocation.PriorityRankingResultDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stage 7A.4 — Priority Ranking Engine.
 *
 * Ranks relocation priority results by composite priority score using a deterministic
 * multi-level comparator with stable tie-breaking.
 *
 * <p>Ranking Order (highest priority first):
 * <ol>
 *   <li>Higher priority score (descending)</li>
 *   <li>Higher urgency level — lower priorityLevel integer (CRITICAL=1 before HIGH=2)</li>
 *   <li>Habitation ID alphabetical ascending (stable tie-breaker)</li>
 * </ol>
 *
 * <p>Invariants:
 * <ul>
 *   <li>Source collection is NOT mutated — a new sorted copy is created.</li>
 *   <li>Null entries are filtered out with a warning.</li>
 *   <li>Empty input produces an empty result safely.</li>
 * </ul>
 */
@Component
public class PriorityRankingEngine {

    private static final Logger log = LoggerFactory.getLogger(PriorityRankingEngine.class);

    /**
     * Deterministic comparator for priority ranking.
     */
    public static final Comparator<RelocationPriorityResultDto> PRIORITY_COMPARATOR = (r1, r2) -> {
        if (r1 == r2) return 0;
        if (r1 == null) return 1;
        if (r2 == null) return -1;

        // 1. Priority Score — descending (higher score = higher priority = ranked first)
        double score1 = r1.getPriorityScore() != null ? r1.getPriorityScore() : 0.0;
        double score2 = r2.getPriorityScore() != null ? r2.getPriorityScore() : 0.0;
        int scoreComp = Double.compare(score2, score1);
        if (scoreComp != 0) {
            return scoreComp;
        }

        // 2. Urgency — ascending priorityLevel integer (1=CRITICAL ranks before 4=LOW)
        int urgency1 = r1.getUrgency() != null ? r1.getUrgency().getPriorityLevel() : 99;
        int urgency2 = r2.getUrgency() != null ? r2.getUrgency().getPriorityLevel() : 99;
        int urgencyComp = Integer.compare(urgency1, urgency2);
        if (urgencyComp != 0) {
            return urgencyComp;
        }

        // 3. Habitation ID — alphabetical ascending (stable tie-breaker)
        String id1 = r1.getHabitationId() != null ? r1.getHabitationId() : "";
        String id2 = r2.getHabitationId() != null ? r2.getHabitationId() : "";
        return id1.compareTo(id2);
    };

    /**
     * Ranks a list of priority results by score and assigns 1-based rank numbers.
     *
     * @param results the unranked priority results
     * @return PriorityRankingResultDto with sorted, ranked list and tier distribution
     */
    public PriorityRankingResultDto rank(List<RelocationPriorityResultDto> results) {
        PriorityRankingResultDto rankingResult = new PriorityRankingResultDto();

        if (results == null || results.isEmpty()) {
            rankingResult.setTotalCases(0);
            rankingResult.setRankedPriorities(new ArrayList<>());
            rankingResult.setRankingSummary("No relocation cases provided for priority ranking.");
            return rankingResult;
        }

        // Filter out null entries (do NOT mutate source list)
        List<RelocationPriorityResultDto> filtered = new ArrayList<>();
        int nullCount = 0;
        for (RelocationPriorityResultDto r : results) {
            if (r != null) {
                filtered.add(r);
            } else {
                nullCount++;
            }
        }

        if (nullCount > 0) {
            log.warn("Filtered out {} null entries from priority ranking input", nullCount);
        }

        if (filtered.isEmpty()) {
            rankingResult.setTotalCases(0);
            rankingResult.setRankedPriorities(new ArrayList<>());
            rankingResult.setRankingSummary("All input entries were null; no cases to rank.");
            return rankingResult;
        }

        // Create a new sorted copy (do NOT mutate source)
        List<RelocationPriorityResultDto> sorted = new ArrayList<>(filtered);
        sorted.sort(PRIORITY_COMPARATOR);

        // Assign 1-based ranks
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setPriorityRank(i + 1);
        }

        rankingResult.setRankedPriorities(sorted);
        // recomputeTierCounts is called inside setRankedPriorities

        String topCase = sorted.get(0).getHabitationName() != null
                ? sorted.get(0).getHabitationName()
                : sorted.get(0).getHabitationId();

        String summary = String.format(
                "Ranked %d relocation cases by priority. Top priority: %s (Score: %.4f, Level: %s). " +
                        "Distribution — Immediate: %d, Short-Term: %d, Medium-Term: %d, Monitoring: %d",
                sorted.size(),
                topCase,
                sorted.get(0).getPriorityScore() != null ? sorted.get(0).getPriorityScore() : 0.0,
                sorted.get(0).getPriorityLevel() != null ? sorted.get(0).getPriorityLevel().name() : "UNKNOWN",
                rankingResult.getImmediateCount(),
                rankingResult.getShortTermCount(),
                rankingResult.getMediumTermCount(),
                rankingResult.getMonitoringCount()
        );
        rankingResult.setRankingSummary(summary);
        log.info(summary);

        return rankingResult;
    }
}
