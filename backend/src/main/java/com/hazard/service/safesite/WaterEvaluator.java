package com.hazard.service.safesite;

import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.safesite.WaterAccessStatus;
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
 * Stage 5.8 — Water Intelligence & Accessibility Evaluator.
 *
 * Evaluates the availability and proximity of useful water supply infrastructure (e.g. potable water plants,
 * drinking water stations, water distribution depots) for candidate safe sites.
 *
 * Critical Distinction:
 * - Stage 5.3 (Hazard Safety) handles water/flood hazard exposure.
 * - Stage 5.8 (Water Intelligence) handles useful emergency water supply.
 * - Natural canals, waterways, rivers, and drainage channels are strictly excluded from being counted as
 *   potable safe-site water sources.
 *
 * Evaluation Rules:
 * - NEAR: Distance to nearest useful water facility <= nearWaterDistanceMeters (default: <= 1000m / 1.0km).
 * - MODERATE: Intermediate distance (1.0km < distance < 5.0km).
 * - FAR: Distance to nearest useful water facility >= farWaterDistanceMeters (default: >= 5000m / 5.0km).
 * - UNKNOWN: Useful water facility data unavailable, or coordinates missing/invalid.
 */
@Component
public class WaterEvaluator {

    private static final Logger log = LoggerFactory.getLogger(WaterEvaluator.class);

    private final InfrastructureDataProvider dataProvider;
    private final WaterEvaluationConfig config;

    public WaterEvaluator(InfrastructureDataProvider dataProvider, WaterEvaluationConfig config) {
        this.dataProvider = dataProvider;
        this.config = config != null ? config : new WaterEvaluationConfig();
    }

    /**
     * Evaluates water accessibility for an individual candidate safe site against available useful water facilities.
     * Mutates the site DTO with waterDistanceMeters, waterDistanceKilometers, waterAccessStatus,
     * nearestWaterSiteId, nearestWaterSiteName, and waterReason.
     */
    public void evaluateWaterAccess(CandidateSafeSiteDto site) {
        if (site == null) {
            return;
        }

        // Fetch configured facilities from data provider and filter for useful water infrastructure
        List<InfrastructureAssetDto> allFacilities = dataProvider != null
                ? dataProvider.getAllRegionalFacilities()
                : Collections.emptyList();

        List<InfrastructureAssetDto> usefulWaterFacilities = allFacilities.stream()
                .filter(WaterEvaluator::isUsefulWaterSupplyFacility)
                .collect(Collectors.toList());

        evaluateWaterAccess(site, usefulWaterFacilities);
    }

    /**
     * Evaluates water accessibility with a provided list of useful water facilities.
     */
    public void evaluateWaterAccess(CandidateSafeSiteDto site, List<InfrastructureAssetDto> usefulWaterFacilities) {
        if (site == null) {
            return;
        }

        Double lat = site.getLatitude();
        Double lon = site.getLongitude();

        // 1. Validate geographic coordinates
        if (lat == null || lon == null || lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            site.setWaterDistanceMeters(null);
            site.setWaterDistanceKilometers(null);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setNearestWaterSiteId(null);
            site.setNearestWaterSiteName(null);
            site.setWaterReason("Missing or invalid geographic coordinates; water accessibility cannot be evaluated.");
            return;
        }

        // 2. Check for available useful water facilities (excluding non-potable waterways/drainage)
        if (usefulWaterFacilities == null || usefulWaterFacilities.isEmpty()) {
            site.setWaterDistanceMeters(null);
            site.setWaterDistanceKilometers(null);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setNearestWaterSiteId(null);
            site.setNearestWaterSiteName(null);
            site.setWaterReason("Useful emergency water supply data is not currently available in the project dataset; natural canals, waterways, and drainage features are excluded from potable safe-site water assessment.");
            return;
        }

        // 3. Find nearest useful water facility using great-circle Haversine formula
        InfrastructureAssetDto nearestFacility = null;
        double minDistanceMeters = Double.MAX_VALUE;

        for (InfrastructureAssetDto facility : usefulWaterFacilities) {
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
            site.setWaterDistanceMeters(null);
            site.setWaterDistanceKilometers(null);
            site.setWaterAccessStatus(WaterAccessStatus.UNKNOWN);
            site.setNearestWaterSiteId(null);
            site.setNearestWaterSiteName(null);
            site.setWaterReason("No valid water facilities with coordinates found; water accessibility cannot be evaluated.");
            return;
        }

        // 4. Apply distance rounding and threshold classification
        double roundedMeters = Math.round(minDistanceMeters * 10.0) / 10.0;
        double distanceKm = Math.round((minDistanceMeters / 1000.0) * 100.0) / 100.0;

        site.setWaterDistanceMeters(roundedMeters);
        site.setWaterDistanceKilometers(distanceKm);
        site.setNearestWaterSiteId(nearestFacility.getAssetId());
        site.setNearestWaterSiteName(nearestFacility.getAssetName());

        double nearLimit = config.getNearWaterDistanceMeters();
        double farLimit = config.getFarWaterDistanceMeters();

        if (roundedMeters <= nearLimit) {
            site.setWaterAccessStatus(WaterAccessStatus.NEAR);
            if (roundedMeters == 0.0) {
                site.setWaterReason("Candidate site is located directly on or adjacent to a useful water facility ("
                        + nearestFacility.getAssetName() + ") (0.0 m).");
            } else {
                site.setWaterReason("Candidate site has close access to emergency water facility ("
                        + nearestFacility.getAssetName() + "), approximately " + roundedMeters
                        + " m (" + String.format("%.2f", distanceKm) + " km <= " + String.format("%.1f", nearLimit / 1000.0)
                        + " km threshold).");
            }
        } else if (roundedMeters >= farLimit) {
            site.setWaterAccessStatus(WaterAccessStatus.FAR);
            site.setWaterReason("Candidate site is relatively distant from useful emergency water facilities, approximately "
                    + roundedMeters + " m (" + String.format("%.2f", distanceKm) + " km >= " + String.format("%.1f", farLimit / 1000.0)
                    + " km threshold) from nearest facility: " + nearestFacility.getAssetName() + ".");
        } else {
            site.setWaterAccessStatus(WaterAccessStatus.MODERATE);
            site.setWaterReason("Candidate site has moderate water facility proximity, approximately " + roundedMeters
                    + " m (" + String.format("%.2f", distanceKm) + " km) to " + nearestFacility.getAssetName()
                    + ", between " + (int) (nearLimit / 1000.0) + "km and " + (int) (farLimit / 1000.0) + "km.");
        }
    }

    /**
     * Determines whether an infrastructure asset represents useful potable or emergency water supply.
     * Raw canals, drainage channels, ditches, and dams are excluded as they represent hydraulic/flood features.
     */
    public static boolean isUsefulWaterSupplyFacility(InfrastructureAssetDto f) {
        if (f == null || f.getCategory() != InfrastructureCategory.WATER) {
            return false;
        }

        String subType = f.getSubType() != null ? f.getSubType().trim().toLowerCase() : "";

        // Explicitly exclude non-potable raw hydraulic/drainage/flood features
        if (subType.contains("canal") || subType.contains("drain") || subType.contains("river") ||
            subType.contains("stream") || subType.contains("weir") || subType.contains("dam") ||
            subType.contains("ditch") || subType.contains("retention") || subType.contains("lock")) {
            return false;
        }

        // Include potable water and municipal water supply/treatment infrastructure
        return subType.contains("treatment") || subType.contains("supply") || subType.contains("potable") ||
               subType.contains("purification") || subType.contains("drinking") || subType.contains("depot") ||
               subType.contains("tower") || subType.contains("reservoir_potable");
    }
}
