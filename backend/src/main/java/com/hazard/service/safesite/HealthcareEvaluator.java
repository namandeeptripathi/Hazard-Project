package com.hazard.service.safesite;

import com.hazard.domain.safesite.HealthcareAccessStatus;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.exposure.InfrastructureDataProvider;
import com.hazard.service.exposure.SettlementExposureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Stage 5.7 — Healthcare Intelligence & Accessibility Evaluator.
 *
 * Evaluates the availability and proximity of medical/hospital support for each candidate safe site
 * using the configured regional healthcare facilities from Stage 5.2.
 *
 * Evaluation Rules:
 * - NEAR: Distance to nearest healthcare facility <= nearHealthcareDistanceMeters (default: <= 5.0km / 5000m),
 *   or candidate site is itself a healthcare facility (0.0m).
 * - MODERATE: Intermediate distance (5.0km < distance < 20.0km).
 * - FAR: Distance to nearest healthcare facility >= farHealthcareDistanceMeters (default: >= 20.0km / 20000m).
 * - UNKNOWN: Healthcare facility data unavailable, or coordinates missing/invalid.
 *
 * Distance Metric:
 * - Uses great-circle straight-line geodesic distance via SettlementExposureService.haversineDistanceMeters.
 * - Does NOT calculate road driving time or routing.
 */
@Component
public class HealthcareEvaluator {

    private static final Logger log = LoggerFactory.getLogger(HealthcareEvaluator.class);

    private final InfrastructureDataProvider dataProvider;
    private final HealthcareEvaluationConfig config;

    public HealthcareEvaluator(InfrastructureDataProvider dataProvider, HealthcareEvaluationConfig config) {
        this.dataProvider = dataProvider;
        this.config = config != null ? config : new HealthcareEvaluationConfig();
    }

    /**
     * Evaluates healthcare accessibility for an individual candidate safe site.
     * Mutates the site DTO with healthcareDistanceMeters, healthcareDistanceKilometers,
     * healthcareAccessStatus, nearestHealthcareSiteId, nearestHealthcareSiteName, and healthcareReason.
     */
    public void evaluateHealthcareAccess(CandidateSafeSiteDto site) {
        if (site == null) {
            return;
        }

        Double lat = site.getLatitude();
        Double lon = site.getLongitude();

        // 1. Validate geographic coordinates
        if (lat == null || lon == null || lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            site.setHealthcareDistanceMeters(null);
            site.setHealthcareDistanceKilometers(null);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.UNKNOWN);
            site.setNearestHealthcareSiteId(null);
            site.setNearestHealthcareSiteName(null);
            site.setHealthcareReason("Missing or invalid geographic coordinates; healthcare accessibility cannot be evaluated.");
            return;
        }

        // 2. Fetch available healthcare facilities
        List<InfrastructureAssetDto> healthcareFacilities = dataProvider != null
                ? dataProvider.getHealthcareFacilities()
                : Collections.emptyList();

        if (healthcareFacilities == null || healthcareFacilities.isEmpty()) {
            site.setHealthcareDistanceMeters(null);
            site.setHealthcareDistanceKilometers(null);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.UNKNOWN);
            site.setNearestHealthcareSiteId(null);
            site.setNearestHealthcareSiteName(null);
            site.setHealthcareReason("No healthcare facilities available in the infrastructure dataset; healthcare accessibility cannot be evaluated.");
            return;
        }

        // 3. Find nearest healthcare facility using canonical great-circle Haversine formula
        InfrastructureAssetDto nearestFacility = null;
        double minDistanceMeters = Double.MAX_VALUE;

        for (InfrastructureAssetDto facility : healthcareFacilities) {
            if (facility.getLatitude() == null || facility.getLongitude() == null) {
                continue;
            }

            double dist = SettlementExposureService.haversineDistanceMeters(
                    lat, lon, facility.getLatitude(), facility.getLongitude());

            if (dist < minDistanceMeters) {
                minDistanceMeters = dist;
                nearestFacility = facility;
            }
        }

        if (nearestFacility == null) {
            site.setHealthcareDistanceMeters(null);
            site.setHealthcareDistanceKilometers(null);
            site.setHealthcareAccessStatus(HealthcareAccessStatus.UNKNOWN);
            site.setNearestHealthcareSiteId(null);
            site.setNearestHealthcareSiteName(null);
            site.setHealthcareReason("No valid healthcare facilities with coordinates found; healthcare accessibility cannot be evaluated.");
            return;
        }

        // 4. Apply distance rounding and threshold classification
        double roundedMeters = Math.round(minDistanceMeters * 10.0) / 10.0;
        double distanceKm = Math.round((minDistanceMeters / 1000.0) * 100.0) / 100.0;

        site.setHealthcareDistanceMeters(roundedMeters);
        site.setHealthcareDistanceKilometers(distanceKm);
        site.setNearestHealthcareSiteId(nearestFacility.getAssetId());
        site.setNearestHealthcareSiteName(nearestFacility.getAssetName());

        double nearLimit = config.getNearHealthcareDistanceMeters();
        double farLimit = config.getFarHealthcareDistanceMeters();

        if (roundedMeters <= nearLimit) {
            site.setHealthcareAccessStatus(HealthcareAccessStatus.NEAR);
            if (roundedMeters == 0.0) {
                site.setHealthcareReason("Candidate site is itself a healthcare facility ("
                        + nearestFacility.getAssetName() + ") with immediate on-site medical access (0.0 m).");
            } else {
                site.setHealthcareReason("Candidate site has close healthcare access, approximately " + roundedMeters
                        + " m (" + String.format("%.2f", distanceKm) + " km <= " + String.format("%.1f", nearLimit / 1000.0)
                        + " km threshold) to " + nearestFacility.getAssetName() + ".");
            }
        } else if (roundedMeters >= farLimit) {
            site.setHealthcareAccessStatus(HealthcareAccessStatus.FAR);
            site.setHealthcareReason("Candidate site is relatively distant from available healthcare facilities, approximately "
                    + roundedMeters + " m (" + String.format("%.2f", distanceKm) + " km >= " + String.format("%.1f", farLimit / 1000.0)
                    + " km threshold) from nearest facility: " + nearestFacility.getAssetName() + ".");
        } else {
            site.setHealthcareAccessStatus(HealthcareAccessStatus.MODERATE);
            site.setHealthcareReason("Candidate site has moderate healthcare proximity, approximately " + roundedMeters
                    + " m (" + String.format("%.2f", distanceKm) + " km) to " + nearestFacility.getAssetName()
                    + ", between " + (int) (nearLimit / 1000.0) + "km and " + (int) (farLimit / 1000.0) + "km.");
        }
    }
}
