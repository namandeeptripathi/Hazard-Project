package com.hazard.domain.safesite;

import com.hazard.exception.InvalidHazardParameterException;

/**
 * Stage 5.6 — Road Accessibility / Proximity Status Classification.
 *
 * Represents the proximity of a candidate safe site to the available road network.
 *
 * Values:
 * - NEAR: Candidate site is in close proximity to an available road (distance <= nearRoadDistanceMeters).
 * - MODERATE: Candidate site has intermediate proximity to the road network (nearRoadDistanceMeters < distance < farRoadDistanceMeters).
 * - FAR: Candidate site is relatively distant from the nearest accessible road (distance >= farRoadDistanceMeters).
 * - UNKNOWN: Road network data is unavailable, or geographic coordinates are missing/invalid.
 */
public enum RoadAccessStatus {
    NEAR("Near Road Network", "Candidate is close to an available road"),
    MODERATE("Moderate Road Proximity", "Candidate has intermediate road proximity"),
    FAR("Far from Road Network", "Candidate is relatively distant from the available road network"),
    UNKNOWN("Unknown Road Access", "Road network data or candidate coordinates are unavailable");

    private final String displayName;
    private final String description;

    RoadAccessStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNear() {
        return this == NEAR;
    }

    public boolean isModerate() {
        return this == MODERATE;
    }

    public boolean isFar() {
        return this == FAR;
    }

    public boolean isKnown() {
        return this != UNKNOWN;
    }

    /**
     * Parses a string into a RoadAccessStatus.
     * Throws InvalidHazardParameterException with allowed values if invalid.
     */
    public static RoadAccessStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return UNKNOWN;
        }
        String normalized = text.trim().toUpperCase();
        try {
            return RoadAccessStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new InvalidHazardParameterException(
                    "Invalid roadAccessStatus filter: '" + text + "'. Allowed values: NEAR, MODERATE, FAR, UNKNOWN");
        }
    }
}
