package com.hazard.service.safesite;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.safesite.InfrastructureAccessStatus;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.exposure.InfrastructureDataProvider;
import com.hazard.service.exposure.SettlementExposureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stage 5.9 — Supporting Infrastructure Intelligence & Proximity Evaluator.
 *
 * Evaluates the availability and proximity of useful supporting infrastructure (e.g. government administrative
 * centers, educational campuses, emergency service centers, healthcare facilities, communication hubs) for candidate safe sites.
 *
 * Category Semantics:
 * - Included Useful Supporting Categories: EDUCATION, GOVERNMENT, EMERGENCY_SERVICES, HEALTHCARE, COMMUNICATION.
 * - Excluded / Hazardous Categories: POWER (substations, power plants), TRANSPORT (bridges, airports, railway junctions),
 *   WATER (dams, canals, drains), OTHER_CRITICAL (fuel depots).
 *
 * Proximity Thresholds:
 * - NEAR: Distance to nearest useful supporting facility <= nearInfrastructureDistanceMeters (default: <= 2000m / 2.0km).
 * - MODERATE: Intermediate distance (2.0km < distance < 10.0km).
 * - FAR: Distance to nearest useful supporting facility >= farInfrastructureDistanceMeters (default: >= 10000m / 10.0km).
 * - UNKNOWN: Supporting infrastructure data unavailable, or coordinates missing/invalid.
 */
@Component
public class InfrastructureEvaluator {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureEvaluator.class);

    private final InfrastructureDataProvider dataProvider;
    private final InfrastructureEvaluationConfig config;

    public InfrastructureEvaluator(InfrastructureDataProvider dataProvider, InfrastructureEvaluationConfig config) {
        this.dataProvider = dataProvider;
        this.config = config != null ? config : new InfrastructureEvaluationConfig();
    }

    /**
     * Evaluates supporting infrastructure accessibility for an individual candidate safe site.
     * Enriches the site DTO with infrastructureDistanceMeters, infrastructureDistanceKilometers,
     * infrastructureAccessStatus, nearestInfrastructureSiteId, nearestInfrastructureSiteName,
     * nearestInfrastructureCategory, and infrastructureReason.
     */
    public void evaluateInfrastructureAccess(CandidateSafeSiteDto site) {
        if (site == null) {
            return;
        }

        List<InfrastructureAssetDto> allFacilities = dataProvider != null
                ? dataProvider.getAllRegionalFacilities()
                : Collections.emptyList();

        List<InfrastructureAssetDto> supportingFacilities = allFacilities.stream()
                .filter(InfrastructureEvaluator::isUsefulSupportingInfrastructure)
                .collect(Collectors.toList());

        evaluateInfrastructureAccess(site, supportingFacilities);
    }

    /**
     * Evaluates supporting infrastructure accessibility with an explicit list of supporting facilities.
     */
    public void evaluateInfrastructureAccess(CandidateSafeSiteDto site, List<InfrastructureAssetDto> usefulSupportingFacilities) {
        if (site == null) {
            return;
        }

        Double lat = site.getLatitude();
        Double lon = site.getLongitude();

        // 1. Validate geographic coordinates
        if (lat == null || lon == null || lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            site.setInfrastructureDistanceMeters(null);
            site.setInfrastructureDistanceKilometers(null);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.UNKNOWN);
            site.setNearestInfrastructureSiteId(null);
            site.setNearestInfrastructureSiteName(null);
            site.setNearestInfrastructureCategory(null);
            site.setInfrastructureReason("Missing or invalid geographic coordinates; supporting infrastructure proximity cannot be evaluated.");
            return;
        }

        // 2. Validate availability of useful supporting facilities
        if (usefulSupportingFacilities == null || usefulSupportingFacilities.isEmpty()) {
            site.setInfrastructureDistanceMeters(null);
            site.setInfrastructureDistanceKilometers(null);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.UNKNOWN);
            site.setNearestInfrastructureSiteId(null);
            site.setNearestInfrastructureSiteName(null);
            site.setNearestInfrastructureCategory(null);
            site.setInfrastructureReason("No useful supporting infrastructure facilities available in the dataset.");
            return;
        }

        // 3. Find nearest useful supporting facility using great-circle Haversine formula
        InfrastructureAssetDto nearestFacility = null;
        double minDistanceMeters = Double.MAX_VALUE;

        for (InfrastructureAssetDto facility : usefulSupportingFacilities) {
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
            site.setInfrastructureDistanceMeters(null);
            site.setInfrastructureDistanceKilometers(null);
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.UNKNOWN);
            site.setNearestInfrastructureSiteId(null);
            site.setNearestInfrastructureSiteName(null);
            site.setNearestInfrastructureCategory(null);
            site.setInfrastructureReason("No valid supporting infrastructure facilities with coordinates found.");
            return;
        }

        // 4. Apply distance rounding and threshold classification
        double roundedMeters = Math.round(minDistanceMeters * 10.0) / 10.0;
        double distanceKm = Math.round((minDistanceMeters / 1000.0) * 100.0) / 100.0;
        String categoryName = nearestFacility.getCategory() != null ? nearestFacility.getCategory().name() : "UNKNOWN";

        site.setInfrastructureDistanceMeters(roundedMeters);
        site.setInfrastructureDistanceKilometers(distanceKm);
        site.setNearestInfrastructureSiteId(nearestFacility.getAssetId());
        site.setNearestInfrastructureSiteName(nearestFacility.getAssetName());
        site.setNearestInfrastructureCategory(categoryName);

        double nearLimit = config.getNearInfrastructureDistanceMeters();
        double farLimit = config.getFarInfrastructureDistanceMeters();

        if (roundedMeters <= nearLimit) {
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.NEAR);
            if (roundedMeters == 0.0) {
                site.setInfrastructureReason("Candidate site is itself a configured supporting facility ("
                        + nearestFacility.getAssetName() + " [" + categoryName + "]) with direct on-site infrastructure access (0.0 m).");
            } else {
                site.setInfrastructureReason("Candidate site has close access to supporting infrastructure ("
                        + nearestFacility.getAssetName() + " [" + categoryName + "]), approximately " + roundedMeters
                        + " m (" + String.format("%.2f", distanceKm) + " km <= " + String.format("%.1f", nearLimit / 1000.0)
                        + " km threshold).");
            }
        } else if (roundedMeters >= farLimit) {
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.FAR);
            site.setInfrastructureReason("Candidate site is relatively distant from useful supporting infrastructure, approximately "
                    + roundedMeters + " m (" + String.format("%.2f", distanceKm) + " km >= " + String.format("%.1f", farLimit / 1000.0)
                    + " km threshold) from nearest facility: " + nearestFacility.getAssetName() + " [" + categoryName + "].");
        } else {
            site.setInfrastructureAccessStatus(InfrastructureAccessStatus.MODERATE);
            site.setInfrastructureReason("Candidate site has moderate supporting infrastructure proximity, approximately "
                    + roundedMeters + " m (" + String.format("%.2f", distanceKm) + " km) to " + nearestFacility.getAssetName()
                    + " [" + categoryName + "], between " + (int) (nearLimit / 1000.0) + "km and " + (int) (farLimit / 1000.0) + "km.");
        }
    }

    /**
     * Determines whether an infrastructure category qualifies as useful supporting infrastructure for emergency response.
     * Excludes power plants, transmission substations, river bridges, airports, railway junctions, canals, dams, and fuel depots.
     */
    public static boolean isUsefulSupportingInfrastructure(InfrastructureAssetDto f) {
        if (f == null || f.getCategory() == null) {
            return false;
        }

        InfrastructureCategory cat = f.getCategory();
        return cat == InfrastructureCategory.EDUCATION
                || cat == InfrastructureCategory.GOVERNMENT
                || cat == InfrastructureCategory.EMERGENCY_SERVICES
                || cat == InfrastructureCategory.HEALTHCARE
                || cat == InfrastructureCategory.COMMUNICATION;
    }
}
