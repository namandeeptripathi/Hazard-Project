package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.dto.relocation.PriorityRankingResultDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.service.relocation.PriorityRankingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7A.4 — Priority Ranking Engine Tests.
 * Tests ordering, tie-breaking, empty input, no mutation, null filtering, and determinism.
 */
class PriorityRankingEngineTests {

    private PriorityRankingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PriorityRankingEngine();
    }

    private RelocationPriorityResultDto createResult(String habitationId, double score,
                                                       PriorityLevel level, RelocationUrgency urgency) {
        RelocationPriorityResultDto result = new RelocationPriorityResultDto();
        result.setHabitationId(habitationId);
        result.setHabitationName("Habitation " + habitationId);
        result.setPriorityScore(score);
        result.setPriorityLevel(level);
        result.setUrgency(urgency);
        return result;
    }

    @Nested
    @DisplayName("Basic Ranking")
    class BasicRankingTests {

        @Test
        @DisplayName("Higher score ranked before lower score")
        void higherScoreFirst() {
            RelocationPriorityResultDto high = createResult("A", 0.85, PriorityLevel.IMMEDIATE, RelocationUrgency.CRITICAL);
            RelocationPriorityResultDto low = createResult("B", 0.35, PriorityLevel.MEDIUM_TERM, RelocationUrgency.MODERATE);

            PriorityRankingResultDto result = engine.rank(Arrays.asList(low, high));

            assertEquals(2, result.getTotalCases());
            assertEquals("A", result.getRankedPriorities().get(0).getHabitationId());
            assertEquals("B", result.getRankedPriorities().get(1).getHabitationId());
        }

        @Test
        @DisplayName("Rank numbers are assigned 1-based sequentially")
        void rankNumbersAssigned() {
            List<RelocationPriorityResultDto> results = Arrays.asList(
                    createResult("C", 0.30, PriorityLevel.MEDIUM_TERM, RelocationUrgency.MODERATE),
                    createResult("A", 0.90, PriorityLevel.IMMEDIATE, RelocationUrgency.CRITICAL),
                    createResult("B", 0.60, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH)
            );

            PriorityRankingResultDto ranked = engine.rank(results);

            assertEquals(1, ranked.getRankedPriorities().get(0).getPriorityRank());
            assertEquals(2, ranked.getRankedPriorities().get(1).getPriorityRank());
            assertEquals(3, ranked.getRankedPriorities().get(2).getPriorityRank());
        }

        @Test
        @DisplayName("Single element list → rank 1")
        void singleElement() {
            RelocationPriorityResultDto single = createResult("X", 0.50, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH);

            PriorityRankingResultDto result = engine.rank(Collections.singletonList(single));

            assertEquals(1, result.getTotalCases());
            assertEquals(1, result.getRankedPriorities().get(0).getPriorityRank());
            assertEquals("X", result.getRankedPriorities().get(0).getHabitationId());
        }
    }

    @Nested
    @DisplayName("Tie-Breaking")
    class TieBreakingTests {

        @Test
        @DisplayName("Same score: higher urgency (CRITICAL) ranks before lower urgency (MODERATE)")
        void tieBreakByUrgency() {
            RelocationPriorityResultDto critical = createResult("B", 0.55, PriorityLevel.SHORT_TERM, RelocationUrgency.CRITICAL);
            RelocationPriorityResultDto moderate = createResult("A", 0.55, PriorityLevel.SHORT_TERM, RelocationUrgency.MODERATE);

            PriorityRankingResultDto result = engine.rank(Arrays.asList(moderate, critical));

            assertEquals("B", result.getRankedPriorities().get(0).getHabitationId(), "CRITICAL urgency should rank first");
            assertEquals("A", result.getRankedPriorities().get(1).getHabitationId());
        }

        @Test
        @DisplayName("Same score and urgency: alphabetical habitationId")
        void tieBreakByHabitationId() {
            RelocationPriorityResultDto a = createResult("AAA", 0.55, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH);
            RelocationPriorityResultDto b = createResult("ZZZ", 0.55, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH);

            PriorityRankingResultDto result = engine.rank(Arrays.asList(b, a));

            assertEquals("AAA", result.getRankedPriorities().get(0).getHabitationId(), "Alphabetically earlier ID should rank first");
            assertEquals("ZZZ", result.getRankedPriorities().get(1).getHabitationId());
        }

        @Test
        @DisplayName("Three-way tie resolved deterministically")
        void threeWayTie() {
            List<RelocationPriorityResultDto> results = Arrays.asList(
                    createResult("C", 0.50, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH),
                    createResult("A", 0.50, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH),
                    createResult("B", 0.50, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH)
            );

            PriorityRankingResultDto ranked = engine.rank(results);

            assertEquals("A", ranked.getRankedPriorities().get(0).getHabitationId());
            assertEquals("B", ranked.getRankedPriorities().get(1).getHabitationId());
            assertEquals("C", ranked.getRankedPriorities().get(2).getHabitationId());
        }
    }

    @Nested
    @DisplayName("Empty and Null Input")
    class EmptyInputTests {

        @Test
        @DisplayName("Null list → empty result")
        void nullList() {
            PriorityRankingResultDto result = engine.rank(null);

            assertEquals(0, result.getTotalCases());
            assertNotNull(result.getRankedPriorities());
            assertTrue(result.getRankedPriorities().isEmpty());
            assertNotNull(result.getRankingSummary());
        }

        @Test
        @DisplayName("Empty list → empty result")
        void emptyList() {
            PriorityRankingResultDto result = engine.rank(Collections.emptyList());

            assertEquals(0, result.getTotalCases());
            assertTrue(result.getRankedPriorities().isEmpty());
        }

        @Test
        @DisplayName("List with only null entries → empty result")
        void allNullEntries() {
            List<RelocationPriorityResultDto> list = new ArrayList<>();
            list.add(null);
            list.add(null);

            PriorityRankingResultDto result = engine.rank(list);

            assertEquals(0, result.getTotalCases());
            assertTrue(result.getRankedPriorities().isEmpty());
        }

        @Test
        @DisplayName("Mixed null and valid entries → null entries filtered out")
        void mixedNullAndValid() {
            List<RelocationPriorityResultDto> list = new ArrayList<>();
            list.add(null);
            list.add(createResult("A", 0.50, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH));
            list.add(null);
            list.add(createResult("B", 0.70, PriorityLevel.IMMEDIATE, RelocationUrgency.CRITICAL));

            PriorityRankingResultDto result = engine.rank(list);

            assertEquals(2, result.getTotalCases());
            assertEquals("B", result.getRankedPriorities().get(0).getHabitationId());
            assertEquals("A", result.getRankedPriorities().get(1).getHabitationId());
        }
    }

    @Nested
    @DisplayName("No Source Mutation")
    class NoMutationTests {

        @Test
        @DisplayName("Source list should not be modified by ranking")
        void sourceNotMutated() {
            List<RelocationPriorityResultDto> original = new ArrayList<>(Arrays.asList(
                    createResult("C", 0.30, PriorityLevel.MEDIUM_TERM, RelocationUrgency.MODERATE),
                    createResult("A", 0.90, PriorityLevel.IMMEDIATE, RelocationUrgency.CRITICAL),
                    createResult("B", 0.60, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH)
            ));

            // Capture original order
            String firstId = original.get(0).getHabitationId();
            String secondId = original.get(1).getHabitationId();
            String thirdId = original.get(2).getHabitationId();

            engine.rank(original);

            // Source list order should be unchanged
            assertEquals(firstId, original.get(0).getHabitationId());
            assertEquals(secondId, original.get(1).getHabitationId());
            assertEquals(thirdId, original.get(2).getHabitationId());
        }
    }

    @Nested
    @DisplayName("Tier Distribution Counts")
    class TierDistributionTests {

        @Test
        @DisplayName("Tier counts are correctly calculated")
        void tierCounts() {
            List<RelocationPriorityResultDto> results = Arrays.asList(
                    createResult("A", 0.90, PriorityLevel.IMMEDIATE, RelocationUrgency.CRITICAL),
                    createResult("B", 0.75, PriorityLevel.IMMEDIATE, RelocationUrgency.HIGH),
                    createResult("C", 0.55, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH),
                    createResult("D", 0.25, PriorityLevel.MEDIUM_TERM, RelocationUrgency.MODERATE),
                    createResult("E", 0.05, PriorityLevel.MONITORING, RelocationUrgency.LOW)
            );

            PriorityRankingResultDto ranked = engine.rank(results);

            assertEquals(5, ranked.getTotalCases());
            assertEquals(2, ranked.getImmediateCount());
            assertEquals(1, ranked.getShortTermCount());
            assertEquals(1, ranked.getMediumTermCount());
            assertEquals(1, ranked.getMonitoringCount());
        }
    }

    @Nested
    @DisplayName("Summary Generation")
    class SummaryTests {

        @Test
        @DisplayName("Summary contains key statistics")
        void summaryContents() {
            List<RelocationPriorityResultDto> results = Arrays.asList(
                    createResult("TopCase", 0.85, PriorityLevel.IMMEDIATE, RelocationUrgency.CRITICAL),
                    createResult("LowCase", 0.20, PriorityLevel.MEDIUM_TERM, RelocationUrgency.LOW)
            );

            PriorityRankingResultDto ranked = engine.rank(results);

            assertNotNull(ranked.getRankingSummary());
            assertTrue(ranked.getRankingSummary().contains("2")); // total cases
            assertTrue(ranked.getRankingSummary().contains("TopCase")); // top case name
        }

        @Test
        @DisplayName("Empty result has informative summary")
        void emptySummary() {
            PriorityRankingResultDto ranked = engine.rank(Collections.emptyList());
            assertNotNull(ranked.getRankingSummary());
            assertTrue(ranked.getRankingSummary().length() > 0);
        }
    }

    @Nested
    @DisplayName("Determinism")
    class DeterminismTests {

        @Test
        @DisplayName("Multiple runs produce identical ordering")
        void deterministicOrdering() {
            List<RelocationPriorityResultDto> results = Arrays.asList(
                    createResult("D", 0.50, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH),
                    createResult("A", 0.80, PriorityLevel.IMMEDIATE, RelocationUrgency.CRITICAL),
                    createResult("C", 0.50, PriorityLevel.SHORT_TERM, RelocationUrgency.HIGH),
                    createResult("B", 0.30, PriorityLevel.MEDIUM_TERM, RelocationUrgency.MODERATE)
            );

            // Run ranking multiple times
            for (int i = 0; i < 20; i++) {
                PriorityRankingResultDto ranked = engine.rank(results);
                assertEquals("A", ranked.getRankedPriorities().get(0).getHabitationId(), "Run " + i);
                assertEquals("C", ranked.getRankedPriorities().get(1).getHabitationId(), "Run " + i);
                assertEquals("D", ranked.getRankedPriorities().get(2).getHabitationId(), "Run " + i);
                assertEquals("B", ranked.getRankedPriorities().get(3).getHabitationId(), "Run " + i);
            }
        }
    }
}
