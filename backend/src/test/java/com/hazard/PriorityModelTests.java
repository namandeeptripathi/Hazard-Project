package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.exception.InvalidHazardParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7A.1 — Priority Model Tests.
 * Tests PriorityLevel enum values, thresholds, ordering, classification, and parsing.
 */
class PriorityModelTests {

    @Nested
    @DisplayName("PriorityLevel Enum Constants")
    class EnumConstantTests {

        @Test
        @DisplayName("Should have exactly 4 priority levels")
        void shouldHaveFourLevels() {
            assertEquals(4, PriorityLevel.values().length);
        }

        @Test
        @DisplayName("IMMEDIATE should be priority order 1")
        void immediateIsOrder1() {
            assertEquals(1, PriorityLevel.IMMEDIATE.getPriorityOrder());
            assertEquals("Immediate Priority", PriorityLevel.IMMEDIATE.getDisplayName());
            assertEquals("#B71C1C", PriorityLevel.IMMEDIATE.getColorHex());
            assertEquals(0.70, PriorityLevel.IMMEDIATE.getMinScoreThreshold());
        }

        @Test
        @DisplayName("SHORT_TERM should be priority order 2")
        void shortTermIsOrder2() {
            assertEquals(2, PriorityLevel.SHORT_TERM.getPriorityOrder());
            assertEquals(0.40, PriorityLevel.SHORT_TERM.getMinScoreThreshold());
        }

        @Test
        @DisplayName("MEDIUM_TERM should be priority order 3")
        void mediumTermIsOrder3() {
            assertEquals(3, PriorityLevel.MEDIUM_TERM.getPriorityOrder());
            assertEquals(0.15, PriorityLevel.MEDIUM_TERM.getMinScoreThreshold());
        }

        @Test
        @DisplayName("MONITORING should be priority order 4")
        void monitoringIsOrder4() {
            assertEquals(4, PriorityLevel.MONITORING.getPriorityOrder());
            assertEquals(0.00, PriorityLevel.MONITORING.getMinScoreThreshold());
        }

        @Test
        @DisplayName("All levels should have non-null display names, colors, descriptions")
        void allFieldsNonNull() {
            for (PriorityLevel level : PriorityLevel.values()) {
                assertNotNull(level.getDisplayName(), level.name() + " displayName");
                assertNotNull(level.getColorHex(), level.name() + " colorHex");
                assertNotNull(level.getDescription(), level.name() + " description");
                assertTrue(level.getPriorityOrder() >= 1 && level.getPriorityOrder() <= 4);
            }
        }
    }

    @Nested
    @DisplayName("Priority Ordering")
    class OrderingTests {

        @Test
        @DisplayName("IMMEDIATE should be higher priority than SHORT_TERM")
        void immediateHigherThanShortTerm() {
            assertTrue(PriorityLevel.IMMEDIATE.isHigherPriorityThan(PriorityLevel.SHORT_TERM));
            assertFalse(PriorityLevel.SHORT_TERM.isHigherPriorityThan(PriorityLevel.IMMEDIATE));
        }

        @Test
        @DisplayName("SHORT_TERM should be higher priority than MEDIUM_TERM")
        void shortTermHigherThanMediumTerm() {
            assertTrue(PriorityLevel.SHORT_TERM.isHigherPriorityThan(PriorityLevel.MEDIUM_TERM));
        }

        @Test
        @DisplayName("MEDIUM_TERM should be higher priority than MONITORING")
        void mediumTermHigherThanMonitoring() {
            assertTrue(PriorityLevel.MEDIUM_TERM.isHigherPriorityThan(PriorityLevel.MONITORING));
        }

        @Test
        @DisplayName("Same level should NOT be higher priority than itself")
        void sameNotHigher() {
            assertFalse(PriorityLevel.IMMEDIATE.isHigherPriorityThan(PriorityLevel.IMMEDIATE));
        }

        @Test
        @DisplayName("Any level is higher priority than null")
        void higherThanNull() {
            assertTrue(PriorityLevel.MONITORING.isHigherPriorityThan(null));
        }
    }

    @Nested
    @DisplayName("isActionable()")
    class ActionableTests {

        @Test
        @DisplayName("IMMEDIATE and SHORT_TERM should be actionable")
        void immediateAndShortTermAreActionable() {
            assertTrue(PriorityLevel.IMMEDIATE.isActionable());
            assertTrue(PriorityLevel.SHORT_TERM.isActionable());
        }

        @Test
        @DisplayName("MEDIUM_TERM and MONITORING should NOT be actionable")
        void mediumTermAndMonitoringNotActionable() {
            assertFalse(PriorityLevel.MEDIUM_TERM.isActionable());
            assertFalse(PriorityLevel.MONITORING.isActionable());
        }
    }

    @Nested
    @DisplayName("fromScore() Classification")
    class FromScoreTests {

        @Test
        @DisplayName("Score 1.0 → IMMEDIATE")
        void maxScore() {
            assertEquals(PriorityLevel.IMMEDIATE, PriorityLevel.fromScore(1.0));
        }

        @Test
        @DisplayName("Score 0.70 (boundary) → IMMEDIATE")
        void immediateBottomBoundary() {
            assertEquals(PriorityLevel.IMMEDIATE, PriorityLevel.fromScore(0.70));
        }

        @Test
        @DisplayName("Score 0.85 → IMMEDIATE")
        void midImmediate() {
            assertEquals(PriorityLevel.IMMEDIATE, PriorityLevel.fromScore(0.85));
        }

        @Test
        @DisplayName("Score 0.699 → SHORT_TERM (just below IMMEDIATE threshold)")
        void justBelowImmediate() {
            assertEquals(PriorityLevel.SHORT_TERM, PriorityLevel.fromScore(0.699));
        }

        @Test
        @DisplayName("Score 0.40 (boundary) → SHORT_TERM")
        void shortTermBottomBoundary() {
            assertEquals(PriorityLevel.SHORT_TERM, PriorityLevel.fromScore(0.40));
        }

        @Test
        @DisplayName("Score 0.55 → SHORT_TERM")
        void midShortTerm() {
            assertEquals(PriorityLevel.SHORT_TERM, PriorityLevel.fromScore(0.55));
        }

        @Test
        @DisplayName("Score 0.399 → MEDIUM_TERM (just below SHORT_TERM threshold)")
        void justBelowShortTerm() {
            assertEquals(PriorityLevel.MEDIUM_TERM, PriorityLevel.fromScore(0.399));
        }

        @Test
        @DisplayName("Score 0.15 (boundary) → MEDIUM_TERM")
        void mediumTermBottomBoundary() {
            assertEquals(PriorityLevel.MEDIUM_TERM, PriorityLevel.fromScore(0.15));
        }

        @Test
        @DisplayName("Score 0.25 → MEDIUM_TERM")
        void midMediumTerm() {
            assertEquals(PriorityLevel.MEDIUM_TERM, PriorityLevel.fromScore(0.25));
        }

        @Test
        @DisplayName("Score 0.149 → MONITORING (just below MEDIUM_TERM threshold)")
        void justBelowMediumTerm() {
            assertEquals(PriorityLevel.MONITORING, PriorityLevel.fromScore(0.149));
        }

        @Test
        @DisplayName("Score 0.0 → MONITORING")
        void zeroScore() {
            assertEquals(PriorityLevel.MONITORING, PriorityLevel.fromScore(0.0));
        }

        @Test
        @DisplayName("Negative score → MONITORING")
        void negativeScore() {
            assertEquals(PriorityLevel.MONITORING, PriorityLevel.fromScore(-0.5));
        }

        @Test
        @DisplayName("NaN score → MONITORING")
        void nanScore() {
            assertEquals(PriorityLevel.MONITORING, PriorityLevel.fromScore(Double.NaN));
        }

        @Test
        @DisplayName("Score > 1.0 → IMMEDIATE")
        void overflowScore() {
            assertEquals(PriorityLevel.IMMEDIATE, PriorityLevel.fromScore(1.5));
        }
    }

    @Nested
    @DisplayName("fromString() Parsing")
    class FromStringTests {

        @Test
        @DisplayName("Should parse valid uppercase names")
        void validUppercase() {
            assertEquals(PriorityLevel.IMMEDIATE, PriorityLevel.fromString("IMMEDIATE"));
            assertEquals(PriorityLevel.SHORT_TERM, PriorityLevel.fromString("SHORT_TERM"));
            assertEquals(PriorityLevel.MEDIUM_TERM, PriorityLevel.fromString("MEDIUM_TERM"));
            assertEquals(PriorityLevel.MONITORING, PriorityLevel.fromString("MONITORING"));
        }

        @Test
        @DisplayName("Should parse case-insensitively")
        void caseInsensitive() {
            assertEquals(PriorityLevel.IMMEDIATE, PriorityLevel.fromString("immediate"));
            assertEquals(PriorityLevel.SHORT_TERM, PriorityLevel.fromString("short_term"));
        }

        @Test
        @DisplayName("Should parse hyphenated form")
        void hyphenated() {
            assertEquals(PriorityLevel.SHORT_TERM, PriorityLevel.fromString("short-term"));
            assertEquals(PriorityLevel.MEDIUM_TERM, PriorityLevel.fromString("medium-term"));
        }

        @Test
        @DisplayName("Null or empty → null")
        void nullOrEmpty() {
            assertNull(PriorityLevel.fromString(null));
            assertNull(PriorityLevel.fromString(""));
            assertNull(PriorityLevel.fromString("   "));
        }

        @Test
        @DisplayName("Invalid string → InvalidHazardParameterException")
        void invalidString() {
            assertThrows(InvalidHazardParameterException.class, () -> PriorityLevel.fromString("INVALID"));
        }
    }
}
