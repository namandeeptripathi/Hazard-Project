package com.hazard.service.relocation;

import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RelocationFeasibilityResultDto;
import com.hazard.dto.relocation.RelocationRequestDto;
import com.hazard.dto.relocation.SiteFeasibilityEvaluationDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.exposure.SettlementExposureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stage 6.2 — Relocation Feasibility Service.
 *
 * Deterministically evaluates whether candidate safe sites are feasible relocation destinations
 * for a specific vulnerable habitation by applying four sequential feasibility gates:
 * 1. Safety Gate: Rejects sites exposed to active disaster hazards (AT_RISK or UNSUITABLE).
 * 2. Suitability Gate: Enforces minimum multi-criteria suitability tier (e.g. >= SUITABLE).
 * 3. Capacity Gate: Ensures available shelter capacity is non-zero and sufficient for the evacuee population.
 * 4. Distance Gate: Enforces maximum allowable geodesic transit distance when specified.
 */
@Service
public class RelocationFeasibilityService {

    private static final Logger log = LoggerFactory.getLogger(RelocationFeasibilityService.class);

    /**
     * Evaluates feasibility of candidate safe sites using a RelocationRequestDto configuration.
     */
    public RelocationFeasibilityResultDto evaluateFeasibility(VulnerableHabitationDto habitation,
                                                            List<CandidateSafeSiteDto> candidateSites,
                                                            RelocationRequestDto request) {
        Double maxDistance = request != null ? request.getMaxTransitDistanceKm() : null;
        SuitabilityClass minSuitability = request != null ? request.getMinSuitabilityClass() : null;
        return evaluateFeasibility(habitation, candidateSites, maxDistance, minSuitability);
    }

    /**
     * Core feasibility evaluation method.
     *
     * @param habitation the vulnerable origin habitation
     * @param candidateSites list of candidate safe sites
     * @param maxTransitDistanceKm optional maximum allowable transit radius in km (null for unbounded)
     * @param minSuitabilityClass optional minimum acceptable suitability tier (null for any non-unsuitable)
     * @return RelocationFeasibilityResultDto containing filtered feasible sites and explainability evaluations
     */
    public RelocationFeasibilityResultDto evaluateFeasibility(VulnerableHabitationDto habitation,
                                                            List<CandidateSafeSiteDto> candidateSites,
                                                            Double maxTransitDistanceKm,
                                                            SuitabilityClass minSuitabilityClass) {
        if (habitation == null) {
            log.warn("Cannot evaluate relocation feasibility for null habitation");
            RelocationFeasibilityResultDto emptyResult = new RelocationFeasibilityResultDto();
            emptyResult.setSummary("Null habitation provided; feasibility evaluation skipped.");
            return emptyResult;
        }

        RelocationFeasibilityResultDto result = new RelocationFeasibilityResultDto(habitation);
        result.setMaxTransitDistanceKm(maxTransitDistanceKm);
        result.setMinSuitabilityClass(minSuitabilityClass != null ? minSuitabilityClass.name() : null);

        if (candidateSites == null || candidateSites.isEmpty()) {
            log.info("No candidate safe sites provided for habitation {}", habitation.getHabitationId());
            result.setSummary("No candidate safe sites available to evaluate for habitation " + habitation.getHabitationName());
            return result;
        }

        long requiredPopulation = resolveRequiredPopulation(habitation);

        for (CandidateSafeSiteDto site : candidateSites) {
            SiteFeasibilityEvaluationDto eval = evaluateSingleSite(habitation, site, requiredPopulation, maxTransitDistanceKm, minSuitabilityClass);
            result.addEvaluation(eval);
        }

        String summary = String.format(
                "Feasibility evaluation for %s (Pop: %d): %d of %d candidate sites are feasible (Rejected: %d)",
                habitation.getHabitationName(),
                requiredPopulation,
                result.getFeasibleCandidatesCount(),
                result.getTotalCandidatesEvaluated(),
                result.getRejectedCandidatesCount()
        );
        result.setSummary(summary);
        log.info(summary);

        return result;
    }

    /**
     * Convenience method: returns only the filtered list of feasible CandidateSafeSiteDtos.
     */
    public List<CandidateSafeSiteDto> filterFeasibleSites(VulnerableHabitationDto habitation,
                                                         List<CandidateSafeSiteDto> candidateSites,
                                                         Double maxTransitDistanceKm,
                                                         SuitabilityClass minSuitabilityClass) {
        RelocationFeasibilityResultDto result = evaluateFeasibility(habitation, candidateSites, maxTransitDistanceKm, minSuitabilityClass);
        return result.getFeasibleSites();
    }

    /**
     * Evaluates a single candidate site against all 4 feasibility gates in deterministic order.
     */
    public SiteFeasibilityEvaluationDto evaluateSingleSite(VulnerableHabitationDto habitation,
                                                          CandidateSafeSiteDto site,
                                                          long requiredPopulation,
                                                          Double maxTransitDistanceKm,
                                                          SuitabilityClass minSuitabilityClass) {
        SiteFeasibilityEvaluationDto eval = new SiteFeasibilityEvaluationDto(site);
        eval.setRequiredPopulation(requiredPopulation);
        eval.setMaxAllowableDistanceKm(maxTransitDistanceKm);

        if (site == null) {
            eval.setFeasible(false);
            eval.setRejectionReasonCode("REJECTED_NULL_SITE");
            eval.setExplanation("Candidate safe site reference is null.");
            return eval;
        }

        // Record coordinates explicitly
        if (habitation != null) {
            eval.setOriginLatitude(habitation.getLatitude());
            eval.setOriginLongitude(habitation.getLongitude());
        }
        eval.setDestinationLatitude(site.getLatitude());
        eval.setDestinationLongitude(site.getLongitude());

        // Validate coordinate bounds: lat in [-90.0, 90.0], lon in [-180.0, 180.0]
        boolean validOrigin = habitation != null
                && habitation.getLatitude() != null && habitation.getLongitude() != null
                && habitation.getLatitude() >= -90.0 && habitation.getLatitude() <= 90.0
                && habitation.getLongitude() >= -180.0 && habitation.getLongitude() <= 180.0;

        boolean validSite = site.getLatitude() != null && site.getLongitude() != null
                && site.getLatitude() >= -90.0 && site.getLatitude() <= 90.0
                && site.getLongitude() >= -180.0 && site.getLongitude() <= 180.0;

        Double transitDistanceMeters = null;
        if (validOrigin && validSite) {
            transitDistanceMeters = SettlementExposureService.haversineDistanceMeters(
                    habitation.getLatitude(), habitation.getLongitude(),
                    site.getLatitude(), site.getLongitude()
            );
            eval.setTransitDistanceMeters(transitDistanceMeters);
            eval.setDistanceAvailable(true);
        } else {
            eval.setTransitDistanceMeters(null);
            eval.setDistanceAvailable(false);
        }

        // ---------------------------------------------------------------------
        // CHECK 1: SAFETY GATE
        // ---------------------------------------------------------------------
        boolean isAtRisk = (site.getHazardSafetyStatus() == HazardSafetyStatus.AT_RISK);
        boolean isUnsuitableSafety = (site.getSuitabilityClass() == SuitabilityClass.UNSUITABLE);

        if (isAtRisk || isUnsuitableSafety) {
            eval.setSafetyPassed(false);
            eval.setSuitabilityPassed(false);
            eval.setCapacityPassed(true);
            eval.setDistancePassed(true);
            eval.setFeasible(false);
            eval.setRejectionReasonCode("REJECTED_UNSAFE");
            String reasonDetail = site.getHazardSafetyReason() != null ? site.getHazardSafetyReason() : "exposed to disaster hazard footprint";
            eval.setExplanation("Site fails hazard safety gate: classified as " + (site.getHazardSafetyStatus() != null ? site.getHazardSafetyStatus().name() : "UNSAFE") + " (" + reasonDetail + ")");
            return eval;
        }
        eval.setSafetyPassed(true);

        // ---------------------------------------------------------------------
        // CHECK 2: SUITABILITY GATE
        // ---------------------------------------------------------------------
        if (minSuitabilityClass != null) {
            SuitabilityClass siteClass = site.getSuitabilityClass();
            if (siteClass == null || !siteClass.isAtLeast(minSuitabilityClass)) {
                eval.setSuitabilityPassed(false);
                eval.setCapacityPassed(true);
                eval.setDistancePassed(true);
                eval.setFeasible(false);
                eval.setRejectionReasonCode("REJECTED_SUITABILITY_BELOW_MINIMUM");
                eval.setExplanation(String.format(
                        "Site suitability tier '%s' is below required minimum threshold '%s'",
                        siteClass != null ? siteClass.name() : "UNKNOWN",
                        minSuitabilityClass.name()
                ));
                return eval;
            }
        }
        eval.setSuitabilityPassed(true);

        // ---------------------------------------------------------------------
        // CHECK 3: CAPACITY GATE
        // ---------------------------------------------------------------------
        Integer availableCapacity = site.getAvailableCapacity();
        if (site.getCapacity() != null) {
            if (availableCapacity != null && availableCapacity <= 0) {
                eval.setCapacityPassed(false);
                eval.setDistancePassed(true);
                eval.setFeasible(false);
                eval.setRejectionReasonCode("REJECTED_ZERO_AVAILABLE_CAPACITY");
                eval.setExplanation(String.format(
                        "Candidate safe site has zero available shelter capacity (total: %d, allocated: %d)",
                        site.getCapacity(),
                        site.getAllocatedOccupancy() != null ? site.getAllocatedOccupancy() : 0
                ));
                return eval;
            } else if (availableCapacity != null && availableCapacity < requiredPopulation) {
                eval.setCapacityPassed(false);
                eval.setDistancePassed(true);
                eval.setFeasible(false);
                eval.setRejectionReasonCode("REJECTED_INSUFFICIENT_CAPACITY");
                eval.setExplanation(String.format(
                        "Available shelter capacity (%d) is insufficient for required vulnerable population (%d)",
                        availableCapacity,
                        requiredPopulation
                ));
                return eval;
            }
        } else if (requiredPopulation > 0 && site.getAllocatedOccupancy() != null && site.getCapacity() != null && site.getAllocatedOccupancy() >= site.getCapacity()) {
            eval.setCapacityPassed(false);
            eval.setDistancePassed(true);
            eval.setFeasible(false);
            eval.setRejectionReasonCode("REJECTED_ZERO_AVAILABLE_CAPACITY");
            eval.setExplanation("Candidate safe site capacity is fully saturated.");
            return eval;
        }
        eval.setCapacityPassed(true);

        // ---------------------------------------------------------------------
        // CHECK 4: DISTANCE GATE (OPTIONAL)
        // ---------------------------------------------------------------------
        if (maxTransitDistanceKm != null && maxTransitDistanceKm > 0.0) {
            if (eval.isDistanceAvailable() && transitDistanceMeters != null) {
                double distanceKm = transitDistanceMeters / 1000.0;
                if (distanceKm > maxTransitDistanceKm) {
                    eval.setDistancePassed(false);
                    eval.setFeasible(false);
                    eval.setRejectionReasonCode("REJECTED_DISTANCE_EXCEEDED");
                    eval.setExplanation(String.format(
                            "Transit distance (%.2f km) exceeds maximum allowable radius (%.2f km)",
                            distanceKm,
                            maxTransitDistanceKm
                    ));
                    return eval;
                }
            } else {
                // Missing or invalid coordinates when distance constraint is strictly required
                eval.setDistancePassed(false);
                eval.setFeasible(false);
                eval.setRejectionReasonCode("REJECTED_MISSING_COORDINATES");
                eval.setExplanation("Missing or invalid geographic coordinates; unable to verify transit distance constraint (" + maxTransitDistanceKm + " km)");
                return eval;
            }
        }
        eval.setDistancePassed(true);

        // ---------------------------------------------------------------------
        // ALL CHECKS PASSED: FEASIBLE
        // ---------------------------------------------------------------------
        eval.setFeasible(true);
        eval.setRejectionReasonCode(null);
        eval.setExplanation("Candidate safe site satisfies all safety, suitability, capacity, and distance feasibility constraints.");
        return eval;
    }

    private long resolveRequiredPopulation(VulnerableHabitationDto habitation) {
        if (habitation.getVulnerablePopulation() != null && habitation.getVulnerablePopulation() > 0) {
            return habitation.getVulnerablePopulation();
        }
        if (habitation.getTotalPopulation() != null && habitation.getTotalPopulation() > 0) {
            return habitation.getTotalPopulation();
        }
        return 0L;
    }
}
