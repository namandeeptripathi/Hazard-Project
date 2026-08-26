package com.hazard.domain.safesite;

/**
 * Stage 5.3 — Hazard Safety Status for Candidate Safe Sites.
 *
 * Distinguishes whether a candidate evacuation/shelter site is:
 * - SAFE: Outside identified high-risk/red-zone disaster areas (low/moderate risk)
 * - AT_RISK: Exposed to critical red-zone or high-risk disaster hazards
 * - UNKNOWN: Spatial hazard data unavailable or unmapped coordinates
 */
public enum HazardSafetyStatus {
    SAFE("Safe", "#4CAF50", "Candidate location is outside identified high-risk and red-zone disaster areas"),
    AT_RISK("At Risk", "#F44336", "Candidate location is exposed to high or critical disaster risk / red-zone hazard area"),
    UNKNOWN("Unknown", "#9E9E9E", "Insufficient spatial hazard data or unmapped coordinates; hazard safety undetermined");

    private final String displayName;
    private final String colorHex;
    private final String description;

    HazardSafetyStatus(String displayName, String colorHex, String description) {
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

    public boolean isSafe() {
        return this == SAFE;
    }

    public boolean isAtRisk() {
        return this == AT_RISK;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * Resolves HazardSafetyStatus from string case-insensitively.
     */
    public static HazardSafetyStatus fromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        String clean = str.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        try {
            return HazardSafetyStatus.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
