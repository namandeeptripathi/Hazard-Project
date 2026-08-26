package com.hazard.domain.safesite;

import com.hazard.exception.InvalidHazardParameterException;

/**
 * Stage 5.9 — Supporting Infrastructure Accessibility / Proximity Status Classification.
 *
 * Represents the proximity of a candidate safe site to useful supporting institutional and emergency
 * infrastructure (e.g. government administrative centers, educational campuses, emergency operations centers,
 * healthcare facilities, communication hubs).
 *
 * Note: Hazardous infrastructure (power plants, high-voltage substations, fuel storage) and transport/water
 * lifelines (bridges, airports, railway junctions, canals, dams) are excluded from supporting safe-site infrastructure.
 *
 * Values:
 * - NEAR: Candidate site is in close proximity to useful supporting infrastructure (distance <= nearInfrastructureDistanceMeters, default: <= 2.0km).
 * - MODERATE: Candidate site has intermediate proximity (nearInfrastructureDistanceMeters < distance < farInfrastructureDistanceMeters).
 * - FAR: Candidate site is relatively distant from useful supporting infrastructure (distance >= farInfrastructureDistanceMeters, default: >= 10.0km).
 * - UNKNOWN: Supporting infrastructure data or candidate coordinates are missing/unavailable.
 */
public enum InfrastructureAccessStatus {
    NEAR("Near Supporting Infrastructure", "Candidate has close proximity to useful supporting infrastructure"),
    MODERATE("Moderate Infrastructure Proximity", "Candidate has intermediate proximity to supporting infrastructure"),
    FAR("Far from Supporting Infrastructure", "Candidate is relatively distant from supporting infrastructure"),
    UNKNOWN("Unknown Infrastructure Access", "Supporting infrastructure data or candidate coordinates are unavailable");

    private final String displayName;
    private final String description;

    InfrastructureAccessStatus(String displayName, String description) {
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
     * Parses a string into an InfrastructureAccessStatus.
     * Throws InvalidHazardParameterException with allowed values if invalid.
     */
    public static InfrastructureAccessStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return UNKNOWN;
        }
        String normalized = text.trim().toUpperCase();
        try {
            return InfrastructureAccessStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new InvalidHazardParameterException(
                    "Invalid infrastructureAccessStatus filter: '" + text + "'. Allowed values: NEAR, MODERATE, FAR, UNKNOWN");
        }
    }
}
