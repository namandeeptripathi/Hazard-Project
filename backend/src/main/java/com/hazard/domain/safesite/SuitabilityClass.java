package com.hazard.domain.safesite;

import com.hazard.exception.InvalidHazardParameterException;

/**
 * Stage 5.10 — Candidate Safe-Site Suitability Classification.
 *
 * Classifies candidate safe sites based on a multi-criteria weighted combination of seven independent
 * spatial dimensions (hazard safety, terrain/slope, risk distance, road access, healthcare, water, supporting infrastructure).
 *
 * Classification Tiers:
 * - HIGHLY_SUITABLE (90.0 - 100.0): Optimal candidate site with strong safety, favorable terrain, and close access to roads, healthcare, water, and supporting services.
 * - SUITABLE (70.0 - 89.99): Good candidate site with acceptable safety, terrain feasibility, and reasonable accessibility.
 * - MARGINAL (40.0 - 69.99): Limited candidate site with significant distance, terrain, or infrastructure constraints.
 * - UNSUITABLE (0.0 - 39.99): Unacceptable candidate site due to direct hazard exposure (hard safety gate: AT_RISK -> UNSUITABLE) or severe multi-dimensional deficiencies.
 * - UNKNOWN: Insufficient spatial dimension data available to determine suitability.
 */
public enum SuitabilityClass {
    HIGHLY_SUITABLE("Highly Suitable", "#2E7D32", "Optimal multi-criteria suitability with high hazard safety, favorable terrain, and strong service accessibility"),
    SUITABLE("Suitable", "#4CAF50", "Good candidate safe site with acceptable overall safety, terrain, and supporting services"),
    MARGINAL("Marginal", "#FF9800", "Limited suitability due to partial constraints, distance, or reduced service accessibility"),
    UNSUITABLE("Unsuitable", "#F44336", "Unacceptable candidate safe site due to hazard exposure (safety gate) or severe terrain/access deficiencies"),
    UNKNOWN("Unknown", "#9E9E9E", "Insufficient dimensional data to assess site suitability");

    private final String displayName;
    private final String colorHex;
    private final String description;

    SuitabilityClass(String displayName, String colorHex, String description) {
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHighlySuitable() {
        return this == HIGHLY_SUITABLE;
    }

    public boolean isSuitable() {
        return this == SUITABLE;
    }

    public boolean isMarginal() {
        return this == MARGINAL;
    }

    public boolean isUnsuitable() {
        return this == UNSUITABLE;
    }

    public boolean isKnown() {
        return this != UNKNOWN;
    }

    public int getTierLevel() {
        return switch (this) {
            case HIGHLY_SUITABLE -> 1;
            case SUITABLE -> 2;
            case MARGINAL -> 3;
            case UNSUITABLE -> 4;
            case UNKNOWN -> 5;
        };
    }

    /**
     * Checks if this suitability class meets or exceeds a specified minimum suitability tier.
     * Tier hierarchy: HIGHLY_SUITABLE (1) > SUITABLE (2) > MARGINAL (3) > UNSUITABLE (4) > UNKNOWN (5).
     */
    public boolean isAtLeast(SuitabilityClass minRequired) {
        if (minRequired == null) {
            return true;
        }
        return this.getTierLevel() <= minRequired.getTierLevel();
    }

    /**
     * Parses a string into a SuitabilityClass.
     * Throws InvalidHazardParameterException with allowed values if invalid.
     */
    public static SuitabilityClass fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return UNKNOWN;
        }
        String normalized = text.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        try {
            return SuitabilityClass.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new InvalidHazardParameterException(
                    "Invalid suitabilityClass filter: '" + text + "'. Allowed values: HIGHLY_SUITABLE, SUITABLE, MARGINAL, UNSUITABLE, UNKNOWN");
        }
    }
}
