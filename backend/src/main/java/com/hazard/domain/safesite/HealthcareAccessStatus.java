package com.hazard.domain.safesite;

import com.hazard.exception.InvalidHazardParameterException;

/**
 * Stage 5.7 — Healthcare Accessibility / Proximity Status Classification.
 *
 * Represents the proximity of a candidate safe site to the nearest available healthcare facility
 * (e.g. tertiary hospital, medical college, district sadar hospital).
 *
 * Values:
 * - NEAR: Candidate site is in close proximity to a healthcare facility (distance <= nearHealthcareDistanceMeters, default: <= 5.0km).
 * - MODERATE: Candidate site has intermediate proximity to healthcare (nearHealthcareDistanceMeters < distance < farHealthcareDistanceMeters).
 * - FAR: Candidate site is relatively distant from the nearest healthcare facility (distance >= farHealthcareDistanceMeters, default: >= 20.0km).
 * - UNKNOWN: Healthcare facility data or candidate coordinates are missing/invalid.
 */
public enum HealthcareAccessStatus {
    NEAR("Near Healthcare Facility", "Candidate has close proximity to medical/hospital support"),
    MODERATE("Moderate Healthcare Proximity", "Candidate has intermediate proximity to medical support"),
    FAR("Far from Healthcare Facility", "Candidate is relatively distant from available healthcare facilities"),
    UNKNOWN("Unknown Healthcare Access", "Healthcare facility data or candidate coordinates are unavailable");

    private final String displayName;
    private final String description;

    HealthcareAccessStatus(String displayName, String description) {
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
     * Parses a string into a HealthcareAccessStatus.
     * Throws InvalidHazardParameterException with allowed values if invalid.
     */
    public static HealthcareAccessStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return UNKNOWN;
        }
        String normalized = text.trim().toUpperCase();
        try {
            return HealthcareAccessStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new InvalidHazardParameterException(
                    "Invalid healthcareAccessStatus filter: '" + text + "'. Allowed values: NEAR, MODERATE, FAR, UNKNOWN");
        }
    }
}
