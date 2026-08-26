package com.hazard.domain.safesite;

/**
 * Stage 5.5 — Distance Status for Candidate Safe Sites.
 *
 * Categorizes the geodesic proximity between a candidate safe site and
 * the relevant active high-risk / red-zone disaster area:
 *
 * - NEAR: Site is in close geographic proximity (<= nearDistanceKm, default <= 5.0 km).
 * - MODERATE: Site is at an intermediate transit distance (> nearDistanceKm and < farDistanceKm, default 5.0 - 20.0 km).
 * - FAR: Site is distant from the affected risk zone (>= farDistanceKm, default >= 20.0 km).
 * - UNKNOWN: Geographic distance could not be determined due to missing coordinates or lack of risk geometry.
 *
 * NOTE: Distance status is purely a spatial proximity metric and is independent of
 * hazard safety (Stage 5.3) and terrain suitability (Stage 5.4).
 */
public enum DistanceStatus {

    NEAR("Near", "#2196F3", "Candidate site is in close geographic proximity to the high-risk disaster area"),
    MODERATE("Moderate", "#FF9800", "Candidate site is at an intermediate transit distance from the high-risk disaster area"),
    FAR("Far", "#9C27B0", "Candidate site is distant from the high-risk disaster area"),
    UNKNOWN("Unknown", "#9E9E9E", "Geographic distance could not be determined due to missing coordinates or lack of risk geometry");

    private final String displayName;
    private final String colorHex;
    private final String description;

    DistanceStatus(String displayName, String colorHex, String description) {
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

    public boolean isNear() {
        return this == NEAR;
    }

    public boolean isModerate() {
        return this == MODERATE;
    }

    public boolean isFar() {
        return this == FAR;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * Case-insensitive parser supporting aliases.
     */
    public static DistanceStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (DistanceStatus status : values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }
        return switch (normalized) {
            case "CLOSE", "PROXIMATE", "SHORT" -> NEAR;
            case "MEDIUM", "INTERMEDIATE", "MID" -> MODERATE;
            case "DISTANT", "REMOTE", "LONG" -> FAR;
            case "N/A", "NONE", "UNRESOLVED" -> UNKNOWN;
            default -> null;
        };
    }
}
