package com.hazard.domain.safesite;

import com.hazard.exception.InvalidHazardParameterException;

/**
 * Stage 5.8 — Water Accessibility / Proximity Status Classification.
 *
 * Represents the proximity of a candidate safe site to useful emergency water infrastructure
 * (e.g. potable water supply, water treatment plants, emergency drinking water distribution stations).
 *
 * Note: Natural canals, waterways, rivers, and drainage channels are excluded as they represent
 * drainage and flood hazard exposure rather than potable emergency water supply.
 *
 * Values:
 * - NEAR: Candidate site is in close proximity to useful water supply (distance <= nearWaterDistanceMeters, default: <= 1.0km).
 * - MODERATE: Candidate site has intermediate proximity to useful water supply (nearWaterDistanceMeters < distance < farWaterDistanceMeters).
 * - FAR: Candidate site is relatively distant from useful water supply (distance >= farWaterDistanceMeters, default: >= 5.0km).
 * - UNKNOWN: Usable water infrastructure data or candidate coordinates are missing/unavailable.
 */
public enum WaterAccessStatus {
    NEAR("Near Water Facility", "Candidate has close proximity to useful emergency water supply"),
    MODERATE("Moderate Water Proximity", "Candidate has intermediate proximity to useful water supply"),
    FAR("Far from Water Facility", "Candidate is relatively distant from useful emergency water facilities"),
    UNKNOWN("Unknown Water Access", "Useful water infrastructure data or candidate coordinates are unavailable");

    private final String displayName;
    private final String description;

    WaterAccessStatus(String displayName, String description) {
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
     * Parses a string into a WaterAccessStatus.
     * Throws InvalidHazardParameterException with allowed values if invalid.
     */
    public static WaterAccessStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return UNKNOWN;
        }
        String normalized = text.trim().toUpperCase();
        try {
            return WaterAccessStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new InvalidHazardParameterException(
                    "Invalid waterAccessStatus filter: '" + text + "'. Allowed values: NEAR, MODERATE, FAR, UNKNOWN");
        }
    }
}
