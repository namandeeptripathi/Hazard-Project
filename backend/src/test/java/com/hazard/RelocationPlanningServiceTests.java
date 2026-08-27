package com.hazard;

import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RelocationPlanDto;
import com.hazard.dto.relocation.RelocationRequestDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.service.relocation.RelocationAllocationService;
import com.hazard.service.relocation.RelocationFeasibilityService;
import com.hazard.service.relocation.RelocationPlanningService;
import com.hazard.service.relocation.RelocationRankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for RelocationPlanningService Orchestration.
 */
class RelocationPlanningServiceTests {

    private RelocationFeasibilityService feasibilityService;
    private RelocationRankingService rankingService;
    private RelocationAllocationService allocationService;
    private RelocationPlanningService planningService;

    @BeforeEach
    void setUp() {
        feasibilityService = new RelocationFeasibilityService();
        rankingService = new RelocationRankingService(feasibilityService);
        allocationService = new RelocationAllocationService(rankingService);
        planningService = new RelocationPlanningService(feasibilityService, rankingService, allocationService);
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
        hab.setTotalPopulation(pop);
        hab.setUrgency(RelocationUrgency.HIGH);
        return hab;
    }

    @Test
    @DisplayName("1. Plan generation with directly supplied habitation resolves cleanly")
    void testPlanWithDirectHabitation() {
        VulnerableHabitationDto hab = createHabitation("HAB-001", "Rampur Basti", 26.5950, 85.5030, 100);

        RelocationRequestDto request = new RelocationRequestDto();
        request.setHabitation(hab);
        request.setMaxTransitDistanceKm(25.0);
        request.setMinSuitabilityClass(SuitabilityClass.MARGINAL);

        RelocationPlanDto plan = planningService.planRelocation(request);

        assertNotNull(plan);
        assertEquals(100L, plan.getTotalVulnerablePopulation());
        assertTrue(plan.validateInvariants());
    }

    @Test
    @DisplayName("2. Request with negative transit distance throws InvalidHazardParameterException")
    void testNegativeTransitDistanceThrows() {
        RelocationRequestDto request = new RelocationRequestDto();
        request.setDistrict("Sitamarhi");
        request.setMaxTransitDistanceKm(-5.0);

        assertThrows(InvalidHazardParameterException.class, () -> planningService.planRelocation(request));
    }

    @Test
    @DisplayName("3. Null request throws InvalidHazardParameterException")
    void testNullRequestThrows() {
        assertThrows(InvalidHazardParameterException.class, () -> planningService.planRelocation(null));
    }

    @Test
    @DisplayName("4. Request with origin coordinates builds correct VulnerableHabitationDto")
    void testResolveHabitationFromCoordinates() {
        RelocationRequestDto request = new RelocationRequestDto();
        request.setDistrict("Patna");
        request.setOriginLatitude(25.6000);
        request.setOriginLongitude(85.1000);
        request.setVulnerablePopulation(120L);

        VulnerableHabitationDto hab = planningService.resolveVulnerableHabitation(request);

        assertNotNull(hab);
        assertEquals("Patna", hab.getDistrict());
        assertEquals(25.6000, hab.getLatitude());
        assertEquals(85.1000, hab.getLongitude());
        assertEquals(120L, hab.getVulnerablePopulation());
    }
}
