package com.hazard.domain.risk;

/**
 * Stage 5.1 — Zone Classification for Dynamic Red-Zone Generation.
 *
 * Maps the existing 5-tier RiskTier into user-facing zone levels.
 * VERY_HIGH and CRITICAL risk tiers both qualify as Red Zones (CRITICAL zone level).
 * Null or missing risk data maps to UNKNOWN (never a Red Zone).
 */
public enum ZoneLevel {
    UNKNOWN("Unknown Risk Zone", "#9E9E9E", "Risk data unavailable or invalid — classification not possible"),
    LOW("Low Risk Zone", "#4CAF50", "Minimal risk — no immediate action required"),
    MODERATE("Moderate Risk Zone", "#FFC107", "Elevated baseline risk — monitoring recommended"),
    HIGH("High Risk Zone", "#FF9800", "Significant risk — preparedness measures advised"),
    CRITICAL("Critical Risk Zone (Red Zone)", "#F44336", "Severe/catastrophic risk — immediate attention required");

    private final String displayName;
    private final String colorHex;
    private final String description;

    ZoneLevel(String displayName, String colorHex, String description) {
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.description = description;
    }

    /**
     * Converts an existing RiskTier into a ZoneLevel.
     * VERY_HIGH and CRITICAL risk tiers both map to the CRITICAL (Red Zone) level.
     * Null tier maps to UNKNOWN (never a Red Zone).
     */
    public static ZoneLevel fromRiskTier(RiskTier tier) {
        if (tier == null) {
            return UNKNOWN;
        }
        return switch (tier) {
            case LOW -> LOW;
            case MODERATE -> MODERATE;
            case HIGH -> HIGH;
            case VERY_HIGH, CRITICAL -> CRITICAL;
        };
    }

    /**
     * Determines if this zone level qualifies as a Red Zone.
     */
    public boolean isRedZone() {
        return this == CRITICAL;
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
}
