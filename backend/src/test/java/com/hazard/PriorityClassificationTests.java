package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.service.relocation.PriorityClassificationEngine;
import com.hazard.service.relocation.PriorityScoringConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7A.2 — Priority Classification Tests.
 * Tests every boundary value and edge case for the classification engine.
 */
class PriorityClassificationTests {

    private PriorityClassificationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PriorityClassificationEngine(new PriorityScoringConfig());
    }

    @Nested
    @DisplayName("IMMEDIATE Classification (score >= 0.70)")
    class ImmediateTests {

        @Test
        @DisplayName("Score 1.0 → IMMEDIATE")
        void maxScore() {
            assertEquals(PriorityLevel.IMMEDIATE, engine.classify(1.0));
        }

        @Test
        @DisplayName("Score 0.70 (exact boundary) → IMMEDIATE")
        void exactBoundary() {
            assertEquals(PriorityLevel.IMMEDIATE, engine.classify(0.70));
        }

        @Test
        @DisplayName("Score 0.85 → IMMEDIATE")
        void midRange() {
            assertEquals(PriorityLevel.IMMEDIATE, engine.classify(0.85));
        }

        @Test
        @DisplayName("Score 0.9999 → IMMEDIATE")
        void nearMax() {
            assertEquals(PriorityLevel.IMMEDIATE, engine.classify(0.9999));
        }
    }

    @Nested
    @DisplayName("SHORT_TERM Classification (0.40 <= score < 0.70)")
    class ShortTermTests {

        @Test
        @DisplayName("Score 0.699 → SHORT_TERM (just below IMMEDIATE)")
        void justBelowImmediate() {
            assertEquals(PriorityLevel.SHORT_TERM, engine.classify(0.699));
        }

        @Test
        @DisplayName("Score 0.40 (exact boundary) → SHORT_TERM")
        void exactBoundary() {
            assertEquals(PriorityLevel.SHORT_TERM, engine.classify(0.40));
        }

        @Test
        @DisplayName("Score 0.55 → SHORT_TERM")
        void midRange() {
            assertEquals(PriorityLevel.SHORT_TERM, engine.classify(0.55));
        }
    }

    @Nested
    @DisplayName("MEDIUM_TERM Classification (0.15 <= score < 0.40)")
    class MediumTermTests {

        @Test
        @DisplayName("Score 0.399 → MEDIUM_TERM (just below SHORT_TERM)")
        void justBelowShortTerm() {
            assertEquals(PriorityLevel.MEDIUM_TERM, engine.classify(0.399));
        }

        @Test
        @DisplayName("Score 0.15 (exact boundary) → MEDIUM_TERM")
        void exactBoundary() {
            assertEquals(PriorityLevel.MEDIUM_TERM, engine.classify(0.15));
        }

        @Test
        @DisplayName("Score 0.25 → MEDIUM_TERM")
        void midRange() {
            assertEquals(PriorityLevel.MEDIUM_TERM, engine.classify(0.25));
        }
    }

    @Nested
    @DisplayName("MONITORING Classification (score < 0.15)")
    class MonitoringTests {

        @Test
        @DisplayName("Score 0.149 → MONITORING (just below MEDIUM_TERM)")
        void justBelowMediumTerm() {
            assertEquals(PriorityLevel.MONITORING, engine.classify(0.149));
        }

        @Test
        @DisplayName("Score 0.0 → MONITORING")
        void zeroScore() {
            assertEquals(PriorityLevel.MONITORING, engine.classify(0.0));
        }

        @Test
        @DisplayName("Score 0.001 → MONITORING")
        void tinyScore() {
            assertEquals(PriorityLevel.MONITORING, engine.classify(0.001));
        }

        @Test
        @DisplayName("Score 0.10 → MONITORING")
        void lowScore() {
            assertEquals(PriorityLevel.MONITORING, engine.classify(0.10));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Negative score → MONITORING")
        void negativeScore() {
            assertEquals(PriorityLevel.MONITORING, engine.classify(-0.5));
        }

        @Test
        @DisplayName("NaN → MONITORING")
        void nanScore() {
            assertEquals(PriorityLevel.MONITORING, engine.classify(Double.NaN));
        }

        @Test
        @DisplayName("Score > 1.0 → IMMEDIATE")
        void overflowScore() {
            assertEquals(PriorityLevel.IMMEDIATE, engine.classify(1.5));
        }

        @Test
        @DisplayName("Very small positive → MONITORING")
        void verySmall() {
            assertEquals(PriorityLevel.MONITORING, engine.classify(0.0001));
        }
    }

    @Nested
    @DisplayName("Classification Determinism")
    class DeterminismTests {

        @Test
        @DisplayName("Same score should always produce same classification")
        void deterministic() {
            for (int i = 0; i < 100; i++) {
                assertEquals(PriorityLevel.SHORT_TERM, engine.classify(0.55));
            }
        }

        @Test
        @DisplayName("All boundary values produce consistent results")
        void boundaryConsistency() {
            // Just below each threshold
            assertEquals(PriorityLevel.SHORT_TERM, engine.classify(0.6999999));
            assertEquals(PriorityLevel.MEDIUM_TERM, engine.classify(0.3999999));
            assertEquals(PriorityLevel.MONITORING, engine.classify(0.1499999));

            // At each threshold
            assertEquals(PriorityLevel.IMMEDIATE, engine.classify(0.70));
            assertEquals(PriorityLevel.SHORT_TERM, engine.classify(0.40));
            assertEquals(PriorityLevel.MEDIUM_TERM, engine.classify(0.15));
        }
    }
}
