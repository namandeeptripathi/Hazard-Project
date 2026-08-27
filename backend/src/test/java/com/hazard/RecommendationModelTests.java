package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.BatchRelocationRecommendationDto;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.exception.InvalidHazardParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7B.1 — Recommendation Model & DTO Tests.
 */
class RecommendationModelTests {

    @Nested
    @DisplayName("RecommendationStatus Enum Tests")
    class RecommendationStatusTests {

        @Test
        @DisplayName("Should have exactly 4 recommendation status tiers")
        void shouldHaveFourStatuses() {
            assertEquals(4, RecommendationStatus.values().length);
        }

        @Test
        @DisplayName("RECOMMENDED should be actionable with correct attributes")
        void testRecommendedStatus() {
            assertTrue(RecommendationStatus.RECOMMENDED.isActionable());
            assertEquals("Recommended", RecommendationStatus.RECOMMENDED.getDisplayName());
            assertEquals("#2E7D32", RecommendationStatus.RECOMMENDED.getColorHex());
            assertNotNull(RecommendationStatus.RECOMMENDED.getDescription());
        }

        @Test
        @DisplayName("Non-actionable statuses should return false for isActionable()")
        void testNonActionableStatuses() {
            assertFalse(RecommendationStatus.NO_FEASIBLE_DESTINATION.isActionable());
            assertFalse(RecommendationStatus.CAPACITY_DEFICIT.isActionable());
            assertFalse(RecommendationStatus.INVALID_SOURCE.isActionable());
        }

        @Test
        @DisplayName("fromString() parses valid strings case-insensitively")
        void testFromStringValid() {
            assertEquals(RecommendationStatus.RECOMMENDED, RecommendationStatus.fromString("RECOMMENDED"));
            assertEquals(RecommendationStatus.RECOMMENDED, RecommendationStatus.fromString("recommended"));
            assertEquals(RecommendationStatus.NO_FEASIBLE_DESTINATION, RecommendationStatus.fromString("NO_FEASIBLE_DESTINATION"));
            assertEquals(RecommendationStatus.NO_FEASIBLE_DESTINATION, RecommendationStatus.fromString("no-feasible-destination"));
            assertEquals(RecommendationStatus.CAPACITY_DEFICIT, RecommendationStatus.fromString("CAPACITY_DEFICIT"));
            assertEquals(RecommendationStatus.INVALID_SOURCE, RecommendationStatus.fromString("invalid_source"));
        }

        @Test
        @DisplayName("fromString() returns null for empty or throws exception for invalid text")
        void testFromStringInvalid() {
            assertNull(RecommendationStatus.fromString(null));
            assertNull(RecommendationStatus.fromString(""));
            assertThrows(InvalidHazardParameterException.class, () -> RecommendationStatus.fromString("INVALID_FOO_STATUS"));
        }
    }

    @Nested
    @DisplayName("RecommendedDestinationDto Tests")
    class RecommendedDestinationDtoTests {

        @Test
        @DisplayName("Should construct with parameterized constructor and default fields")
        void testDtoConstruction() {
            RecommendedDestinationDto dest = new RecommendedDestinationDto("SITE-01", "Community Center", 0.85, 1);

            assertEquals("SITE-01", dest.getSiteId());
            assertEquals("Community Center", dest.getSiteName());
            assertEquals(0.85, dest.getDestinationScore());
            assertEquals(1, dest.getDestinationRank());
            assertTrue(dest.isFeasible());
            assertNotNull(dest.getScoringContributors());
        }

        @Test
        @DisplayName("Category setting updates categoryDisplayName")
        void testCategoryDisplayName() {
            RecommendedDestinationDto dest = new RecommendedDestinationDto();
            dest.setCategory(CandidateSiteCategory.EDUCATION);

            assertEquals(CandidateSiteCategory.EDUCATION, dest.getCategory());
            assertEquals(CandidateSiteCategory.EDUCATION.getDisplayName(), dest.getCategoryDisplayName());
        }

        @Test
        @DisplayName("Scoring contributors helper stores values accurately")
        void testScoringContributors() {
            RecommendedDestinationDto dest = new RecommendedDestinationDto();
            dest.addScoringContributor("SUITABILITY_QUALITY", 0.90);
            dest.addScoringContributor("TRANSIT_PROXIMITY", 0.75);

            assertEquals(2, dest.getScoringContributors().size());
            assertEquals(0.90, dest.getScoringContributors().get("SUITABILITY_QUALITY"));
            assertEquals(0.75, dest.getScoringContributors().get("TRANSIT_PROXIMITY"));
        }
    }

    @Nested
    @DisplayName("RelocationRecommendationDto Tests")
    class RelocationRecommendationDtoTests {

        @Test
        @DisplayName("Should initialize properly with defaults")
        void testRecommendationDtoDefaults() {
            RelocationRecommendationDto rec = new RelocationRecommendationDto("HAB-01", RecommendationStatus.RECOMMENDED);

            assertEquals("HAB-01", rec.getHabitationId());
            assertEquals(RecommendationStatus.RECOMMENDED, rec.getStatus());
            assertTrue(rec.isFeasible());
            assertNotNull(rec.getTimestamp());
            assertNotNull(rec.getAlternativeDestinations());
            assertEquals(0, rec.getAlternativeDestinations().size());
        }

        @Test
        @DisplayName("Adding alternative destination appends correctly")
        void testAlternativeDestinations() {
            RelocationRecommendationDto rec = new RelocationRecommendationDto();
            RecommendedDestinationDto alt1 = new RecommendedDestinationDto("SITE-ALT1", "School", 0.70, 2);
            RecommendedDestinationDto alt2 = new RecommendedDestinationDto("SITE-ALT2", "Hospital", 0.60, 3);

            rec.addAlternativeDestination(alt1);
            rec.addAlternativeDestination(alt2);

            assertEquals(2, rec.getAlternativeDestinations().size());
            assertEquals("SITE-ALT1", rec.getAlternativeDestinations().get(0).getSiteId());
            assertEquals("SITE-ALT2", rec.getAlternativeDestinations().get(1).getSiteId());
        }
    }

    @Nested
    @DisplayName("BatchRelocationRecommendationDto Tests")
    class BatchRelocationRecommendationDtoTests {

        @Test
        @DisplayName("recomputeStatistics counts status types accurately")
        void testBatchStatistics() {
            BatchRelocationRecommendationDto batch = new BatchRelocationRecommendationDto();

            RelocationRecommendationDto r1 = new RelocationRecommendationDto("H1", RecommendationStatus.RECOMMENDED);
            RelocationRecommendationDto r2 = new RelocationRecommendationDto("H2", RecommendationStatus.RECOMMENDED);
            RelocationRecommendationDto r3 = new RelocationRecommendationDto("H3", RecommendationStatus.NO_FEASIBLE_DESTINATION);
            RelocationRecommendationDto r4 = new RelocationRecommendationDto("H4", RecommendationStatus.CAPACITY_DEFICIT);
            RelocationRecommendationDto r5 = new RelocationRecommendationDto("H5", RecommendationStatus.INVALID_SOURCE);

            batch.addRecommendation(r1);
            batch.addRecommendation(r2);
            batch.addRecommendation(r3);
            batch.addRecommendation(r4);
            batch.addRecommendation(r5);

            assertEquals(5, batch.getTotalCases());
            assertEquals(2, batch.getSuccessfulRecommendations());
            assertEquals(1, batch.getNoFeasibleRecommendations());
            assertEquals(1, batch.getCapacityDeficitRecommendations());
            assertEquals(1, batch.getInvalidSourceRecommendations());
        }
    }
}
