package com.hazard.service.relocation;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.population.PopulatedPlace;
import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.exposure.SettlementExposureAnalysisResultDto;
import com.hazard.dto.exposure.SettlementExposureDto;
import com.hazard.dto.relocation.*;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.population.PopulatedPlaceRepository;
import com.hazard.service.exposure.SettlementExposureService;
import com.hazard.service.safesite.CandidateSafeSiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stage 6.7 — Relocation Planning Orchestration Service.
 *
 * Coordinates the end-to-end Relocation Intelligence pipeline:
 * 1. Resolves/retrieves the target vulnerable habitation (from request, DB, or hazard exposure).
 * 2. Retrieves spatial candidate safe sites for the geographic region.
 * 3. Applies multi-gate Feasibility Filtering (Stage 6.2/6.3).
 * 4. Ranks feasible candidate safe sites deterministically (Stage 6.4).
 * 5. Performs capacity-aware population allocation and deficit accounting (Stage 6.5/6.6).
 * 6. Returns a fully structured, explainable RelocationPlanDto.
 */
@Service
public class RelocationPlanningService {

    private static final Logger log = LoggerFactory.getLogger(RelocationPlanningService.class);

    private final RelocationFeasibilityService feasibilityService;
    private final RelocationRankingService rankingService;
    private final RelocationAllocationService allocationService;
    private final CandidateSafeSiteService candidateSafeSiteService;
    private final PopulatedPlaceRepository populatedPlaceRepository;
    private final SettlementExposureService settlementExposureService;
    private final DistrictBoundaryRepository districtBoundaryRepository;

    @Autowired
    public RelocationPlanningService(RelocationFeasibilityService feasibilityService,
                                     RelocationRankingService rankingService,
                                     RelocationAllocationService allocationService,
                                     CandidateSafeSiteService candidateSafeSiteService,
                                     PopulatedPlaceRepository populatedPlaceRepository,
                                     SettlementExposureService settlementExposureService,
                                     DistrictBoundaryRepository districtBoundaryRepository) {
        this.feasibilityService = feasibilityService;
        this.rankingService = rankingService;
        this.allocationService = allocationService;
        this.candidateSafeSiteService = candidateSafeSiteService;
        this.populatedPlaceRepository = populatedPlaceRepository;
        this.settlementExposureService = settlementExposureService;
        this.districtBoundaryRepository = districtBoundaryRepository;
    }

    /**
     * Simplified constructor for isolated unit testing.
     */
    public RelocationPlanningService(RelocationFeasibilityService feasibilityService,
                                     RelocationRankingService rankingService,
                                     RelocationAllocationService allocationService) {
        this(feasibilityService, rankingService, allocationService, null, null, null, null);
    }

    /**
     * Orchestrates the complete relocation planning workflow for a client request.
     *
     * @param request the relocation planning request
     * @return RelocationPlanDto detailing assignments, capacity utilization, and deficit breakdown
     */
    public RelocationPlanDto planRelocation(RelocationRequestDto request) {
        if (request == null) {
            throw new InvalidHazardParameterException("Relocation request cannot be null");
        }

        validateRequest(request);

        // 1. Resolve target vulnerable habitation
        VulnerableHabitationDto habitation = resolveVulnerableHabitation(request);

        // 2. Retrieve candidate safe sites
        List<com.hazard.dto.safesite.CandidateSafeSiteDto> candidateSites = resolveCandidateSafeSites(habitation, request);

        // 3. Execute Feasibility Filtering (Stage 6.2 & 6.3)
        RelocationFeasibilityResultDto feasibilityResult = feasibilityService.evaluateFeasibility(
                habitation, candidateSites, request.getMaxTransitDistanceKm(), request.getMinSuitabilityClass()
        );

        // 4. Execute Deterministic Ranking (Stage 6.4)
        RelocationRankingResultDto rankingResult = rankingService.rankFeasibleSites(feasibilityResult);

        // 5. Execute Capacity-Aware Allocation & Deficit Reporting (Stage 6.5 & 6.6)
        RelocationPlanDto plan = allocationService.allocatePopulation(habitation, rankingResult);

        // 6. Enrich Plan with Request Metadata
        plan.setStrategy(request.getAllocationStrategy() != null ? request.getAllocationStrategy() : "NEAREST_SUITABLE");
        if (request.getDistrict() != null && (plan.getDistrict() == null || plan.getDistrict().isEmpty())) {
            plan.setDistrict(request.getDistrict());
        }
        if (request.getHazardId() != null) {
            plan.setHazardIdentifier(request.getHazardId());
        }
        if (request.getHazardType() != null) {
            plan.setHazardType(request.getHazardType());
        }

        log.info("Relocation planning complete for {} -> Status: {}, Allocated: {}/{}, Deficit: {}",
                habitation.getHabitationName(), plan.getOverallStatus(),
                plan.getTotalAllocatedPopulation(), plan.getTotalVulnerablePopulation(),
                plan.getTotalUnallocatedPopulation());

        return plan;
    }

    /**
     * Resolves the target vulnerable habitation from request parameters or database repositories.
     */
    public VulnerableHabitationDto resolveVulnerableHabitation(RelocationRequestDto request) {
        // Direct Habitation DTO
        if (request.getHabitation() != null) {
            VulnerableHabitationDto hab = request.getHabitation();
            if (request.getDistrict() != null && hab.getDistrict() == null) {
                hab.setDistrict(request.getDistrict());
            }
            return hab;
        }

        // Direct Coordinates & Population in Request
        if (request.getOriginLatitude() != null && request.getOriginLongitude() != null) {
            VulnerableHabitationDto hab = new VulnerableHabitationDto();
            hab.setHabitationId(request.getHabitationId() != null ? request.getHabitationId() : "HAB-REQ-01");
            hab.setHabitationName("Vulnerable Location (" + request.getOriginLatitude() + ", " + request.getOriginLongitude() + ")");
            hab.setDistrict(request.getDistrict() != null ? request.getDistrict() : "Sitamarhi");
            hab.setState("Bihar");
            hab.setLatitude(request.getOriginLatitude());
            hab.setLongitude(request.getOriginLongitude());
            long pop = request.getVulnerablePopulation() != null ? request.getVulnerablePopulation() : 250L;
            hab.setVulnerablePopulation(pop);
            hab.setTotalPopulation(pop);
            hab.setUrgency(RelocationUrgency.HIGH);
            return hab;
        }

        // By Habitation ID (PopulatedPlace lookup)
        if (request.getHabitationId() != null && populatedPlaceRepository != null) {
            try {
                int placeId = Integer.parseInt(request.getHabitationId().replace("HAB-", "").trim());
                Optional<PopulatedPlace> placeOpt = populatedPlaceRepository.findById(placeId);
                if (placeOpt.isPresent()) {
                    return mapPopulatedPlaceToDto(placeOpt.get(), request);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // By Hazard Event Identifier
        if (request.getHazardId() != null && settlementExposureService != null) {
            try {
                SettlementExposureAnalysisResultDto exposure = settlementExposureService.getExposedSettlementsForHazardEvent(
                        request.getHazardId(), 25000.0
                );
                if (exposure != null && exposure.getExposedSettlements() != null && !exposure.getExposedSettlements().isEmpty()) {
                    SettlementExposureDto topSettlement = exposure.getExposedSettlements().get(0);
                    return mapSettlementExposureToDto(topSettlement, request);
                }
            } catch (Exception e) {
                log.warn("Could not derive exposed settlement for hazardId {}: {}", request.getHazardId(), e.getMessage());
            }
        }

        // By District Name
        if (request.getDistrict() != null && populatedPlaceRepository != null) {
            List<PopulatedPlace> places = populatedPlaceRepository.findByAdm2NameIgnoreCase(request.getDistrict().trim());
            if (places != null && !places.isEmpty()) {
                return mapPopulatedPlaceToDto(places.get(0), request);
            }
        }

        // Fallback default habitation for valid district request
        String district = request.getDistrict() != null ? request.getDistrict() : "Sitamarhi";
        VulnerableHabitationDto defaultHab = new VulnerableHabitationDto();
        defaultHab.setHabitationId("HAB-" + district.toUpperCase() + "-01");
        defaultHab.setHabitationName(district + " Central Habitation");
        defaultHab.setDistrict(district);
        defaultHab.setState("Bihar");
        defaultHab.setLatitude(26.5950);
        defaultHab.setLongitude(85.5030);
        long pop = request.getVulnerablePopulation() != null ? request.getVulnerablePopulation() : 250L;
        defaultHab.setVulnerablePopulation(pop);
        defaultHab.setTotalPopulation(pop);
        defaultHab.setUrgency(RelocationUrgency.HIGH);
        return defaultHab;
    }

    /**
     * Discovers and retrieves candidate safe sites for the target habitation and request constraints.
     */
    public List<com.hazard.dto.safesite.CandidateSafeSiteDto> resolveCandidateSafeSites(VulnerableHabitationDto habitation, RelocationRequestDto request) {
        if (candidateSafeSiteService == null) {
            return new ArrayList<>();
        }

        String district = habitation != null && habitation.getDistrict() != null ? habitation.getDistrict() : request.getDistrict();
        List<com.hazard.dto.safesite.CandidateSafeSiteDto> sites;

        if (district != null && !district.trim().isEmpty()) {
            sites = candidateSafeSiteService.getCandidateSites(district.trim(), null, false);
            if (sites == null || sites.isEmpty()) {
                sites = candidateSafeSiteService.getAllCandidateSites();
            }
        } else {
            sites = candidateSafeSiteService.getAllCandidateSites();
        }

        if (sites == null) {
            sites = new ArrayList<>();
        }

        // Apply default capacity if site capacity is unmeasured
        if (request.getDefaultSiteCapacity() != null && request.getDefaultSiteCapacity() > 0) {
            for (com.hazard.dto.safesite.CandidateSafeSiteDto s : sites) {
                if (s.getCapacity() == null) {
                    s.setCapacity(request.getDefaultSiteCapacity());
                }
            }
        }

        return sites;
    }

    private void validateRequest(RelocationRequestDto request) {
        if (request.getMaxTransitDistanceKm() != null && request.getMaxTransitDistanceKm() < 0.0) {
            throw new InvalidHazardParameterException("Maximum transit distance cannot be negative: " + request.getMaxTransitDistanceKm());
        }
        if (request.getDefaultSiteCapacity() != null && request.getDefaultSiteCapacity() < 0) {
            throw new InvalidHazardParameterException("Default site capacity cannot be negative: " + request.getDefaultSiteCapacity());
        }
        if (request.getVulnerablePopulation() != null && request.getVulnerablePopulation() < 0) {
            throw new InvalidHazardParameterException("Vulnerable population cannot be negative: " + request.getVulnerablePopulation());
        }
    }

    private VulnerableHabitationDto mapPopulatedPlaceToDto(PopulatedPlace place, RelocationRequestDto request) {
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId(place.getId() != null ? "HAB-" + place.getId() : "HAB-UNKNOWN");
        hab.setHabitationName(place.getName() != null ? place.getName() : "Settlement #" + place.getId());
        hab.setHabitationType(place.getPlace() != null ? place.getPlace() : "village");
        hab.setDistrict(place.getAdm2Name() != null ? place.getAdm2Name() : request.getDistrict());
        hab.setState("Bihar");
        if (place.getGeom() != null) {
            hab.setLongitude(place.getGeom().getCentroid().getX());
            hab.setLatitude(place.getGeom().getCentroid().getY());
        }
        long pop = (request.getVulnerablePopulation() != null && request.getVulnerablePopulation() > 0)
                ? request.getVulnerablePopulation()
                : (place.getPopulation() != null && place.getPopulation() > 0 ? place.getPopulation() : 300L);
        hab.setVulnerablePopulation(pop);
        hab.setTotalPopulation(pop);
        hab.setUrgency(RelocationUrgency.HIGH);
        return hab;
    }

    private VulnerableHabitationDto mapSettlementExposureToDto(SettlementExposureDto exp, RelocationRequestDto request) {
        VulnerableHabitationDto hab = new VulnerableHabitationDto();
        hab.setHabitationId(exp.getSettlementId() != null ? "HAB-" + exp.getSettlementId() : "HAB-UNKNOWN");
        hab.setHabitationName(exp.getSettlementName());
        hab.setHabitationType(exp.getSettlementType());
        hab.setDistrict(exp.getDistrictName());
        hab.setState(exp.getState() != null ? exp.getState() : "Bihar");
        hab.setLatitude(exp.getLatitude());
        hab.setLongitude(exp.getLongitude());
        long pop = (request.getVulnerablePopulation() != null && request.getVulnerablePopulation() > 0)
                ? request.getVulnerablePopulation()
                : (exp.getTotalPopulation() != null && exp.getTotalPopulation() > 0 ? exp.getTotalPopulation() : 300L);
        hab.setVulnerablePopulation(pop);
        hab.setTotalPopulation(pop);
        hab.setUrgency(RelocationUrgency.CRITICAL);
        hab.setHazardIdentifier(request.getHazardId());
        hab.setHazardType(request.getHazardType());
        return hab;
    }
}
