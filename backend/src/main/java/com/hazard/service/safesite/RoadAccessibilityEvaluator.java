package com.hazard.service.safesite;

import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stage 5.6 — Road Accessibility / Proximity Evaluator.
 *
 * Evaluates candidate safe sites against available road network infrastructure.
 *
 * Rules:
 * - NEAR: Distance to road network <= nearRoadDistanceMeters (default: 500m / 0.5km).
 * - MODERATE: Intermediate distance (500m < distance < 2000m).
 * - FAR: Distance to road network >= farRoadDistanceMeters (default: 2000m / 2.0km).
 * - UNKNOWN: Road network data unavailable, or coordinates missing/invalid.
 *
 * Note: When road network vector data is not present in the platform database,
 * this evaluator cleanly and defensively defaults to UNKNOWN with explicit provenance metadata.
 */
@Component
public class RoadAccessibilityEvaluator {

    private static final Logger log = LoggerFactory.getLogger(RoadAccessibilityEvaluator.class);

    private final RoadAccessEvaluationConfig config;

    public RoadAccessibilityEvaluator(RoadAccessEvaluationConfig config) {
        this.config = config != null ? config : new RoadAccessEvaluationConfig();
    }

    /**
     * Evaluates road accessibility for an individual candidate safe site.
     * Mutates the site DTO to populate roadDistanceMeters, roadDistanceKilometers,
     * roadAccessStatus, and roadAccessReason.
     */
    public void evaluateRoadAccessibility(CandidateSafeSiteDto site) {
        if (site == null) {
            return;
        }

        Double lat = site.getLatitude();
        Double lon = site.getLongitude();

        // 1. Validate geographic coordinates
        if (lat == null || lon == null || lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            site.setRoadDistanceMeters(null);
            site.setRoadDistanceKilometers(null);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setRoadAccessReason("Missing or invalid geographic coordinates; road accessibility cannot be evaluated.");
            return;
        }

        // 2. Case A: Site already has explicit road distance (e.g., from test fixture or external provider)
        if (site.getRoadDistanceMeters() != null) {
            applyRoadDistanceClassification(site, site.getRoadDistanceMeters());
            return;
        }

        // 3. Case B: Production state — No road network dataset currently ingested in database
        site.setRoadDistanceMeters(null);
        site.setRoadDistanceKilometers(null);
        site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
        site.setRoadAccessReason("Road-network data is not currently available in the project dataset; road accessibility is integrated structurally but returns UNKNOWN.");
    }

    /**
     * Evaluates road accessibility with an explicit distance in meters.
     */
    public void evaluateRoadAccessibility(CandidateSafeSiteDto site, Double explicitDistanceMeters) {
        if (site == null) {
            return;
        }

        Double lat = site.getLatitude();
        Double lon = site.getLongitude();

        if (lat == null || lon == null || lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            site.setRoadDistanceMeters(null);
            site.setRoadDistanceKilometers(null);
            site.setRoadAccessStatus(RoadAccessStatus.UNKNOWN);
            site.setRoadAccessReason("Missing or invalid geographic coordinates; road accessibility cannot be evaluated.");
            return;
        }

        if (explicitDistanceMeters != null) {
            applyRoadDistanceClassification(site, explicitDistanceMeters);
        } else {
            evaluateRoadAccessibility(site);
        }
    }

    /**
     * Applies distance rounding, threshold categorization, and descriptive reason.
     */
    public void applyRoadDistanceClassification(CandidateSafeSiteDto site, double distanceMeters) {
        double roundedMeters = Math.round(distanceMeters * 10.0) / 10.0;
        double distanceKm = Math.round((distanceMeters / 1000.0) * 100.0) / 100.0;

        site.setRoadDistanceMeters(roundedMeters);
        site.setRoadDistanceKilometers(distanceKm);

        double nearLimit = config.getNearRoadDistanceMeters();
        double farLimit = config.getFarRoadDistanceMeters();

        if (roundedMeters <= nearLimit) {
            site.setRoadAccessStatus(RoadAccessStatus.NEAR);
            if (roundedMeters == 0.0) {
                site.setRoadAccessReason("Candidate site is located directly on or adjacent to an accessible road (0.0 m).");
            } else {
                site.setRoadAccessReason("Candidate site has close road access, approximately " + roundedMeters
                        + " m (" + String.format("%.2f", distanceKm) + " km <= " + String.format("%.1f", nearLimit / 1000.0) + " km threshold).");
            }
        } else if (roundedMeters >= farLimit) {
            site.setRoadAccessStatus(RoadAccessStatus.FAR);
            site.setRoadAccessReason("Candidate site is relatively distant from the nearest accessible road, approximately "
                    + roundedMeters + " m (" + String.format("%.2f", distanceKm) + " km >= " + String.format("%.1f", farLimit / 1000.0) + " km threshold).");
        } else {
            site.setRoadAccessStatus(RoadAccessStatus.MODERATE);
            site.setRoadAccessReason("Candidate site has moderate road proximity, approximately " + roundedMeters
                    + " m (" + String.format("%.2f", distanceKm) + " km), between " + (int) nearLimit + "m and " + (int) farLimit + "m.");
        }
    }
}
