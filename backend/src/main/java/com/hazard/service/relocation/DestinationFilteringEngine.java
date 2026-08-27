package com.hazard.service.relocation;

import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.exposure.SettlementExposureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stage 7B.3 — Destination Feasibility Filtering Engine.
 *
 * Evaluates candidate safe sites against Stage 6 hard feasibility constraints
 * and produces a list of evaluated RecommendedDestinationDtos with pass/fail gates:
 * 1. Identity Gate: Destination must not be identical to origin habitation.
 * 2. Safety Gate: Destination must NOT be AT_RISK or UNSUITABLE.
 * 3. Suitability Gate: Destination must meet or exceed minimum suitability tier.
 * 4. Capacity Gate: Destination available shelter capacity must be > 0 and sufficient for required population.
 * 5. Distance Gate: Geodesic transit distance must not exceed maximum allowable radius.
 */
@Component
public class DestinationFilteringEngine {

    private static final Logger log = LoggerFactory.getLogger(DestinationFilteringEngine.class);

    /**
     * Filters and converts a list of candidate safe sites for a given origin habitation.
     *
     * @param habitation          the origin vulnerable habitation
     * @param candidateSites      the raw candidate safe sites
     * @param maxTransitDistanceKm optional maximum allowable transit distance in km
     * @param minSuitabilityClass optional minimum acceptable suitability tier
     * @return list of evaluated RecommendedDestinationDtos (both feasible and rejected)
     */
    public List<RecommendedDestinationDto> evaluateCandidates(VulnerableHabitationDto habitation,
                                                             List<CandidateSafeSiteDto> candidateSites,
                                                             Double maxTransitDistanceKm,
                                                             SuitabilityClass minSuitabilityClass) {
        if (candidateSites == null || candidateSites.isEmpty()) {
            return Collections.emptyList();
        }

        long requiredPopulation = resolveRequiredPopulation(habitation);
        List<RecommendedDestinationDto> evaluations = new ArrayList<>(candidateSites.size());

        for (CandidateSafeSiteDto site : candidateSites) {
            RecommendedDestinationDto eval = evaluateSingleCandidate(
                    habitation, site, requiredPopulation, maxTransitDistanceKm, minSuitabilityClass
            );
            evaluations.add(eval);
        }

        return evaluations;
    }

    /**
     * Evaluates a single candidate safe site against all hard feasibility gates.
     */
    public RecommendedDestinationDto evaluateSingleCandidate(VulnerableHabitationDto habitation,
                                                            CandidateSafeSiteDto site,
                                                            long requiredPopulation,
                                                            Double maxTransitDistanceKm,
                                                            SuitabilityClass minSuitabilityClass) {
        RecommendedDestinationDto dest = new RecommendedDestinationDto();

        if (site == null) {
            dest.setFeasible(false);
            dest.setRejectionReasonCode("REJECTED_NULL_CANDIDATE");
            dest.setRejectionReason("Candidate safe site reference is null.");
            return dest;
        }

        // Populate base metadata
        dest.setSiteId(site.getSiteId());
        dest.setSiteName(site.getSiteName());
        dest.setCategory(site.getCategory());
        dest.setCategoryDisplayName(site.getCategoryDisplayName());
        dest.setDistrict(site.getDistrict());
        dest.setState(site.getState() != null ? site.getState() : "Bihar");
        dest.setLatitude(site.getLatitude());
        dest.setLongitude(site.getLongitude());
        dest.setHazardSafetyStatus(site.getHazardSafetyStatus());
        dest.setSuitabilityClass(site.getSuitabilityClass());
        dest.setSuitabilityScore(site.getSuitabilityScore());
        dest.setTotalCapacity(site.getCapacity());
        dest.setAllocatedOccupancy(site.getAllocatedOccupancy());
        dest.setAvailableCapacity(site.getAvailableCapacity());

        // Calculate distance if coordinates are valid
        boolean validOrigin = habitation != null
                && habitation.getLatitude() != null && habitation.getLongitude() != null
                && habitation.getLatitude() >= -90.0 && habitation.getLatitude() <= 90.0
                && habitation.getLongitude() >= -180.0 && habitation.getLongitude() <= 180.0;

        boolean validSite = site.getLatitude() != null && site.getLongitude() != null
                && site.getLatitude() >= -90.0 && site.getLatitude() <= 90.0
                && site.getLongitude() >= -180.0 && site.getLongitude() <= 180.0;

        Double distanceMeters = null;
        if (validOrigin && validSite) {
            distanceMeters = SettlementExposureService.haversineDistanceMeters(
                    habitation.getLatitude(), habitation.getLongitude(),
                    site.getLatitude(), site.getLongitude()
            );
            dest.setDistanceMeters(distanceMeters);
            dest.setDistanceKilometers(distanceMeters / 1000.0);
        } else {
            dest.setDistanceMeters(null);
            dest.setDistanceKilometers(null);
        }

        // ---------------------------------------------------------------------
        // GATE 1: IDENTITY GATE (Source != Destination)
        // ---------------------------------------------------------------------
        if (habitation != null && habitation.getHabitationId() != null
                && site.getSiteId() != null
                && habitation.getHabitationId().trim().equalsIgnoreCase(site.getSiteId().trim())) {
            dest.setFeasible(false);
            dest.setRejectionReasonCode("REJECTED_IDENTICAL_ORIGIN_DESTINATION");
            dest.setRejectionReason("Candidate safe site is identical to origin vulnerable habitation.");
            return dest;
        }

        // ---------------------------------------------------------------------
        // GATE 2: SAFETY GATE
        // ---------------------------------------------------------------------
        boolean isAtRisk = (site.getHazardSafetyStatus() == HazardSafetyStatus.AT_RISK);
        boolean isUnsuitableSafety = (site.getSuitabilityClass() == SuitabilityClass.UNSUITABLE);

        if (isAtRisk || isUnsuitableSafety) {
            dest.setFeasible(false);
            dest.setRejectionReasonCode("REJECTED_UNSAFE");
            String reasonDetail = site.getHazardSafetyReason() != null
                    ? site.getHazardSafetyReason()
                    : "Site is exposed to disaster hazard footprint";
            dest.setRejectionReason("Site fails hazard safety gate: " + reasonDetail);
            return dest;
        }

        // ---------------------------------------------------------------------
        // GATE 3: SUITABILITY GATE
        // ---------------------------------------------------------------------
        if (minSuitabilityClass != null) {
            SuitabilityClass siteClass = site.getSuitabilityClass();
            if (siteClass == null || !siteClass.isAtLeast(minSuitabilityClass)) {
                dest.setFeasible(false);
                dest.setRejectionReasonCode("REJECTED_SUITABILITY_BELOW_MINIMUM");
                dest.setRejectionReason(String.format(
                        "Site suitability tier '%s' is below required minimum threshold '%s'",
                        siteClass != null ? siteClass.name() : "UNKNOWN",
                        minSuitabilityClass.name()
                ));
                return dest;
            }
        }

        // ---------------------------------------------------------------------
        // GATE 4: CAPACITY GATE
        // ---------------------------------------------------------------------
        Integer availCap = site.getAvailableCapacity();
        if (site.getCapacity() != null) {
            if (availCap != null && availCap <= 0) {
                dest.setFeasible(false);
                dest.setRejectionReasonCode("REJECTED_ZERO_AVAILABLE_CAPACITY");
                dest.setRejectionReason(String.format(
                        "Candidate safe site has zero available shelter capacity (total: %d, allocated: %d)",
                        site.getCapacity(),
                        site.getAllocatedOccupancy() != null ? site.getAllocatedOccupancy() : 0
                ));
                return dest;
            } else if (availCap != null && availCap < requiredPopulation) {
                dest.setFeasible(false);
                dest.setRejectionReasonCode("REJECTED_INSUFFICIENT_CAPACITY");
                dest.setRejectionReason(String.format(
                        "Available shelter capacity (%d) is insufficient for required vulnerable population (%d)",
                        availCap,
                        requiredPopulation
                ));
                return dest;
            }
        }

        // ---------------------------------------------------------------------
        // GATE 5: DISTANCE GATE
        // ---------------------------------------------------------------------
        if (maxTransitDistanceKm != null && maxTransitDistanceKm > 0.0) {
            if (distanceMeters != null) {
                double distanceKm = distanceMeters / 1000.0;
                if (distanceKm > maxTransitDistanceKm) {
                    dest.setFeasible(false);
                    dest.setRejectionReasonCode("REJECTED_DISTANCE_EXCEEDED");
                    dest.setRejectionReason(String.format(
                            "Transit distance (%.2f km) exceeds maximum allowable radius (%.2f km)",
                            distanceKm,
                            maxTransitDistanceKm
                    ));
                    return dest;
                }
            } else {
                dest.setFeasible(false);
                dest.setRejectionReasonCode("REJECTED_MISSING_COORDINATES");
                dest.setRejectionReason("Missing origin or destination coordinates to verify transit distance.");
                return dest;
            }
        }

        // ---------------------------------------------------------------------
        // ALL HARD GATES PASSED: FEASIBLE
        // ---------------------------------------------------------------------
        dest.setFeasible(true);
        dest.setRejectionReasonCode(null);
        dest.setRejectionReason(null);

        // Accommodatable population
        if (availCap != null) {
            dest.setAccommodatablePopulation(Math.min(requiredPopulation, (long) availCap));
        } else {
            dest.setAccommodatablePopulation(requiredPopulation);
        }

        return dest;
    }

    private long resolveRequiredPopulation(VulnerableHabitationDto habitation) {
        if (habitation == null) return 0L;
        if (habitation.getVulnerablePopulation() != null && habitation.getVulnerablePopulation() > 0) {
            return habitation.getVulnerablePopulation();
        }
        if (habitation.getTotalPopulation() != null && habitation.getTotalPopulation() > 0) {
            return habitation.getTotalPopulation();
        }
        return 0L;
    }
}
