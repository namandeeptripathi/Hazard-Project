package com.hazard;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.relocation.RelocationRecommendationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 7B.6 & 7B.7 — Relocation Recommendation Engine Tests.
 */
class RelocationRecommendationEngineTests {

    private RelocationRecommendationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RelocationRecommendationEngine();
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
        hab.setUrgency(RelocationUrgency.HIGH);
        return hab;
    }

    private CandidateSafeSiteDto createSite(String id, String name, double lat, double lon, Integer cap,
                                            SuitabilityClass sClass, HazardSafetyStatus safety) {
        CandidateSafeSiteDto site = new CandidateSafeSiteDto();
        site.setSiteId(id);
        site.setSiteName(name);
        site.setDistrict("Sitamarhi");
        site.setState("Bihar");
        site.setLatitude(lat);
        site.setLongitude(lon);
        site.setCapacity(cap);
        site.setAllocatedOccupancy(0);
        site.setAvailableCapacity(cap);
        site.setSuitabilityClass(sClass);
        site.setSuitabilityScore(sClass == SuitabilityClass.HIGHLY_SUITABLE ? 92.0 : 78.0);
        site.setHazardSafetyStatus(safety);
        return site;
    }

    @Nested
    @DisplayName("Successful Recommendation")
    class SuccessfulRecommendationTests {

        @Test
        @DisplayName("Selects best feasible site as primary and remaining feasible sites as alternatives")
        void testSuccessfulRecommendationGeneration() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", "Sonbarsa Village", 26.5950, 85.5030, 200L);

            CandidateSafeSiteDto best = createSite("SITE-BEST", "Central High School", 26.6000, 85.5100, 500,
                    SuitabilityClass.HIGHLY_SUITABLE, HazardSafetyStatus.SAFE);
            CandidateSafeSiteDto alt = createSite("SITE-ALT", "Community Hall", 26.6200, 85.5300, 300,
                    SuitabilityClass.SUITABLE, HazardSafetyStatus.SAFE);

            RelocationPriorityResultDto priority = new RelocationPriorityResultDto("HAB-01", 0.85, PriorityLevel.IMMEDIATE);
            priority.setPriorityRank(1);
            priority.setPlanId("PLAN-001");

            RelocationRecommendationDto rec = engine.generateRecommendation(
                    hab, Arrays.asList(alt, best), priority, 50.0, SuitabilityClass.MARGINAL
            );

            assertNotNull(rec);
            assertEquals(RecommendationStatus.RECOMMENDED, rec.getStatus());
            assertTrue(rec.isFeasible());

            // Primary destination
            assertNotNull(rec.getPrimaryDestination());
            assertEquals("SITE-BEST", rec.getPrimaryDestination().getSiteId());
            assertEquals(1, rec.getPrimaryDestination().getDestinationRank());

            // Alternative destinations
            assertEquals(1, rec.getAlternativeDestinations().size());
            assertEquals("SITE-ALT", rec.getAlternativeDestinations().get(0).getSiteId());
            assertEquals(2, rec.getAlternativeDestinations().get(0).getDestinationRank());

            // Population metrics
            assertEquals(200L, rec.getAllocatedPopulation());
            assertEquals(0L, rec.getUnallocatedPopulation());
            assertEquals(100.0, rec.getCapacityFitRatePercentage());

            // Priority propagation
            assertEquals(PriorityLevel.IMMEDIATE, rec.getPriorityLevel());
            assertEquals(0.85, rec.getPriorityScore());
            assertEquals(1, rec.getPriorityRank());

            // Summary text
            assertNotNull(rec.getRecommendationSummary());
            assertTrue(rec.getRecommendationSummary().contains("Central High School"));
        }
    }

    @Nested
    @DisplayName("No Feasible Destination")
    class NoFeasibleDestinationTests {

        @Test
        @DisplayName("Returns NO_FEASIBLE_DESTINATION when all candidate sites are unsafe or far away")
        void testNoFeasibleDestination() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", "Sonbarsa Village", 26.5950, 85.5030, 200L);

            CandidateSafeSiteDto unsafe = createSite("S1", "Flood Prone Hall", 26.6000, 85.5100, 500,
                    SuitabilityClass.SUITABLE, HazardSafetyStatus.AT_RISK);

            RelocationRecommendationDto rec = engine.generateRecommendation(
                    hab, Collections.singletonList(unsafe), null, 50.0, SuitabilityClass.MARGINAL
            );

            assertEquals(RecommendationStatus.NO_FEASIBLE_DESTINATION, rec.getStatus());
            assertFalse(rec.isFeasible());
            assertNull(rec.getPrimaryDestination());
            assertEquals(0L, rec.getAllocatedPopulation());
            assertEquals(200L, rec.getUnallocatedPopulation());
            assertTrue(rec.getRecommendationSummary().contains("No feasible destination found"));
        }

        @Test
        @DisplayName("Returns NO_FEASIBLE_DESTINATION when candidate safe sites list is empty")
        void testEmptyCandidateList() {
            VulnerableHabitationDto hab = createHabitation("HAB-01", "Sonbarsa Village", 26.5950, 85.5030, 200L);

            RelocationRecommendationDto rec = engine.generateRecommendation(
                    hab, Collections.emptyList(), null, 50.0, SuitabilityClass.MARGINAL
            );

            assertEquals(RecommendationStatus.NO_FEASIBLE_DESTINATION, rec.getStatus());
            assertFalse(rec.isFeasible());
            assertEquals(0, rec.getTotalCandidatesEvaluated());
        }
    }

    @Nested
    @DisplayName("Invalid Source Context")
    class InvalidSourceTests {

        @Test
        @DisplayName("Returns INVALID_SOURCE when origin habitation is null")
        void testNullHabitation() {
            RelocationRecommendationDto rec = engine.generateRecommendation(
                    null, Collections.emptyList(), null, 50.0, null
            );

            assertEquals(RecommendationStatus.INVALID_SOURCE, rec.getStatus());
            assertFalse(rec.isFeasible());
        }
    }

    @Nested
    @DisplayName("Zero Population Edge Case")
    class ZeroPopulationTests {

        @Test
        @DisplayName("Zero population requires no relocation and returns RECOMMENDED status with 0 allocation")
        void testZeroPopulation() {
            VulnerableHabitationDto hab = createHabitation("HAB-ZERO", "Empty Settlement", 26.5950, 85.5030, 0L);

            RelocationRecommendationDto rec = engine.generateRecommendation(
                    hab, Collections.emptyList(), null, 50.0, null
            );

            assertEquals(RecommendationStatus.RECOMMENDED, rec.getStatus());
            assertEquals(0L, rec.getAllocatedPopulation());
            assertEquals(0L, rec.getUnallocatedPopulation());
            assertEquals(100.0, rec.getCapacityFitRatePercentage());
        }
    }
}
