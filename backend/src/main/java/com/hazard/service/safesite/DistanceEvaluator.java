package com.hazard.service.safesite;

import com.hazard.domain.risk.ZoneLevel;
import com.hazard.domain.safesite.DistanceStatus;
import com.hazard.dto.risk.RedZoneDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.boundaries.DistrictDistanceProjection;
import com.hazard.service.exposure.SettlementExposureService;
import com.hazard.service.risk.RedZoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stage 5.5 — Distance Intelligence Evaluator.
 *
 * Evaluates the geodesic proximity (NEAR, MODERATE, FAR, UNKNOWN) between each
 * candidate safe site and the relevant active high-risk / red-zone disaster geometries.
 *
 * Principles:
 * 1. Evidence-based & Spatial Accuracy: Uses PostGIS geodesic ST_Distance on geography
 *    to measure the true minimum distance from the candidate Point to the nearest
 *    high-risk district MultiPolygon boundary (returning 0.0m if inside).
 * 2. Scope Boundary: Evaluates geodesic/geographic distance only. Does NOT calculate
 *    road distance, driving duration, or routing (Stage 5.6).
 * 3. Reuse: Uses SettlementExposureService.haversineDistanceMeters for point-to-point calculations.
 * 4. Orthogonal: Distance is an independent dimension from Stage 5.3 hazard safety and
 *    Stage 5.4 terrain feasibility.
 */
@Component
public class DistanceEvaluator {

    private static final Logger log = LoggerFactory.getLogger(DistanceEvaluator.class);

    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final RedZoneService redZoneService;
    private final DistanceEvaluationConfig config;

    public DistanceEvaluator(DistrictBoundaryRepository districtBoundaryRepository,
                             RedZoneService redZoneService,
                             DistanceEvaluationConfig config) {
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.redZoneService = redZoneService;
        this.config = config;
    }

    /**
     * Evaluates geographic distance for a candidate safe site against active Red Zones / high-risk areas.
     */
    public void evaluateDistance(CandidateSafeSiteDto site) {
        evaluateDistance(site, null);
    }

    /**
     * Evaluates geographic distance for a candidate safe site against specified target districts or active Red Zones.
     *
     * @param site The candidate safe site DTO to mutate
     * @param explicitTargetDistricts Optional list of target district names to measure distance against
     */
    public void evaluateDistance(CandidateSafeSiteDto site, List<String> explicitTargetDistricts) {
        if (site == null) {
            return;
        }

        Double lat = site.getLatitude();
        Double lon = site.getLongitude();

        // 1. Validate geographic coordinates
        if (lat == null || lon == null || lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            site.setDistanceMeters(null);
            site.setDistanceKilometers(null);
            site.setDistanceStatus(DistanceStatus.UNKNOWN);
            site.setDistanceReason("Missing or invalid geographic coordinates; distance evaluation cannot be performed.");
            return;
        }

        // 2. Case A: Site already has explicit distanceMeters (e.g. from custom test fixture or data provider)
        if (site.getDistanceMeters() != null) {
            applyDistanceClassification(site, site.getDistanceMeters(), "the relevant high-risk area");
            return;
        }

        // 3. Resolve target high-risk / red-zone districts
        List<String> targetDistricts = explicitTargetDistricts;
        if (targetDistricts == null || targetDistricts.isEmpty()) {
            targetDistricts = resolveActiveHighRiskDistricts();
        }

        List<String> upperTargetDistricts = targetDistricts.stream()
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        if (upperTargetDistricts.isEmpty()) {
            site.setDistanceMeters(null);
            site.setDistanceKilometers(null);
            site.setDistanceStatus(DistanceStatus.UNKNOWN);
            site.setDistanceReason("No active high-risk or red-zone disaster areas identified to measure proximity.");
            return;
        }

        // 4. Query PostGIS for geodesic distance to the closest high-risk district polygon
        try {
            Optional<DistrictDistanceProjection> projectionOpt = districtBoundaryRepository
                    .findNearestDistrictDistance(lon, lat, upperTargetDistricts);

            if (projectionOpt.isPresent() && projectionOpt.get().getDistanceMeters() != null) {
                DistrictDistanceProjection proj = projectionOpt.get();
                String districtName = proj.getDistrictName();
                Double distanceM = proj.getDistanceMeters();
                applyDistanceClassification(site, distanceM, districtName);
            } else {
                site.setDistanceMeters(null);
                site.setDistanceKilometers(null);
                site.setDistanceStatus(DistanceStatus.UNKNOWN);
                site.setDistanceReason("Distance calculation could not be completed for the candidate location.");
            }
        } catch (Exception e) {
            log.warn("PostGIS distance calculation failed for site {} at ({}, {}): {}",
                    site.getSiteId(), lon, lat, e.getMessage());
            site.setDistanceMeters(null);
            site.setDistanceKilometers(null);
            site.setDistanceStatus(DistanceStatus.UNKNOWN);
            site.setDistanceReason("Spatial distance query failed: " + e.getMessage());
        }
    }

    /**
     * Resolves the list of active high-risk and red-zone district names.
     */
    public List<String> resolveActiveHighRiskDistricts() {
        List<RedZoneDto> redZonesList = null;
        try {
            redZonesList = redZoneService.getRedZonesOnly();
        } catch (Exception e) {
            log.warn("Failed to retrieve red zones: {}", e.getMessage());
        }

        if (redZonesList != null && !redZonesList.isEmpty()) {
            return redZonesList.stream()
                    .map(RedZoneDto::getDistrictName)
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .collect(Collectors.toList());
        }

        List<RedZoneDto> highRiskList = null;
        try {
            highRiskList = redZoneService.getZonesByMinimumLevel(ZoneLevel.HIGH);
        } catch (Exception e) {
            log.warn("Failed to retrieve high risk zones: {}", e.getMessage());
        }

        if (highRiskList != null && !highRiskList.isEmpty()) {
            return highRiskList.stream()
                    .map(RedZoneDto::getDistrictName)
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .collect(Collectors.toList());
        }

        return java.util.Collections.emptyList();
    }

    /**
     * Applies distance rounding, threshold categorization, and descriptive reason.
     */
    public void applyDistanceClassification(CandidateSafeSiteDto site, double distanceMeters, String referenceAreaName) {
        double roundedMeters = Math.round(distanceMeters * 10.0) / 10.0;
        double distanceKm = Math.round((distanceMeters / 1000.0) * 100.0) / 100.0;

        site.setDistanceMeters(roundedMeters);
        site.setDistanceKilometers(distanceKm);

        if (roundedMeters <= 0.0) {
            site.setDistanceStatus(DistanceStatus.NEAR);
            site.setDistanceReason(String.format(
                    "Candidate site is located inside the high-risk disaster area (%s). Distance: 0.00 km.",
                    referenceAreaName));
        } else if (distanceKm <= config.getNearDistanceKm()) {
            site.setDistanceStatus(DistanceStatus.NEAR);
            site.setDistanceReason(String.format(
                    "Candidate site is near the high-risk disaster area (%s), approximately %.2f km (<= %.1f km threshold).",
                    referenceAreaName, distanceKm, config.getNearDistanceKm()));
        } else if (distanceKm >= config.getFarDistanceKm()) {
            site.setDistanceStatus(DistanceStatus.FAR);
            site.setDistanceReason(String.format(
                    "Candidate site is far from the high-risk disaster area (%s), approximately %.2f km (>= %.1f km threshold).",
                    referenceAreaName, distanceKm, config.getFarDistanceKm()));
        } else {
            site.setDistanceStatus(DistanceStatus.MODERATE);
            site.setDistanceReason(String.format(
                    "Candidate site is at a moderate distance from the high-risk disaster area (%s), approximately %.2f km.",
                    referenceAreaName, distanceKm));
        }
    }

    /**
     * Helper method reusing existing SettlementExposureService.haversineDistanceMeters
     * for point-to-point great-circle distance.
     */
    public double calculateHaversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        return SettlementExposureService.haversineDistanceMeters(lat1, lon1, lat2, lon2);
    }
}
