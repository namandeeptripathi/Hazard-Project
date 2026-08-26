package com.hazard.domain.safesite;

/**
 * Stage 5.4 — Terrain / Slope Status for Candidate Safe Sites.
 *
 * Categorizes terrain suitability for emergency evacuation facilities:
 * - FAVORABLE: Flat to gentle slope (<= 5.0°), low risk of slope failure or rapid runoff
 * - UNFAVORABLE: Steep terrain (>= 15.0°), landslide/instability or severe accessibility impediment
 * - UNKNOWN: Elevation or slope raster data not currently available for coordinates
 */
public enum TerrainStatus {
    FAVORABLE("Favorable", "#4CAF50", "Site terrain/slope characteristics are flat or gentle, favorable for emergency shelter operations"),
    UNFAVORABLE("Unfavorable", "#F44336", "Site terrain/slope is steep or unfavorable, posing landslide, waterlogging, or accessibility risk"),
    UNKNOWN("Unknown", "#9E9E9E", "Terrain elevation or slope raster data is not currently available for this location");

    private final String displayName;
    private final String colorHex;
    private final String description;

    TerrainStatus(String displayName, String colorHex, String description) {
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

    public boolean isFavorable() {
        return this == FAVORABLE;
    }

    public boolean isUnfavorable() {
        return this == UNFAVORABLE;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * Resolves TerrainStatus case-insensitively with alias support.
     */
    public static TerrainStatus fromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        String clean = str.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        try {
            return TerrainStatus.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
