package com.hazard.domain.relocation;

import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.risk.ZoneLevel;
import com.hazard.exception.InvalidHazardParameterException;

/**
 * Stage 6.1 — Relocation Priority & Urgency Classification.
 *
 * Prioritizes evacuees and habitations based on active disaster hazard severity and risk level:
 * - CRITICAL (Level 1): Immediate emergency evacuation required (critical red-zone / extreme hazard).
 * - HIGH (Level 2): High-risk disaster area requiring expedited relocation.
 * - MODERATE (Level 3): Moderate risk or secondary buffer evacuation.
 * - LOW (Level 4): Low risk or precautionary relocation.
 */
public enum RelocationUrgency {
    CRITICAL("Critical Urgency", 1, "#B71C1C", "Immediate emergency evacuation required: located within critical disaster red-zone"),
    HIGH("High Urgency", 2, "#E65100", "High hazard exposure requiring prioritized evacuation"),
    MODERATE("Moderate Urgency", 3, "#F57C00", "Moderate hazard exposure or secondary evacuation zone"),
    LOW("Low Urgency", 4, "#388E3C", "Low hazard exposure or precautionary relocation");

    private final String displayName;
    private final int priorityLevel; // 1 = highest urgency
    private final String colorHex;
    private final String description;

    RelocationUrgency(String displayName, int priorityLevel, String colorHex, String description) {
        this.displayName = displayName;
        this.priorityLevel = priorityLevel;
        this.colorHex = colorHex;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCritical() {
        return this == CRITICAL;
    }

    public boolean isHighOrCritical() {
        return this == CRITICAL || this == HIGH;
    }

    /**
     * Derives RelocationUrgency from a RiskTier.
     */
    public static RelocationUrgency fromRiskTier(RiskTier tier) {
        if (tier == null) {
            return MODERATE;
        }
        return switch (tier) {
            case CRITICAL, VERY_HIGH -> CRITICAL;
            case HIGH -> HIGH;
            case MODERATE -> MODERATE;
            case LOW -> LOW;
        };
    }

    /**
     * Derives RelocationUrgency from a ZoneLevel.
     */
    public static RelocationUrgency fromZoneLevel(ZoneLevel level) {
        if (level == null) {
            return MODERATE;
        }
        return switch (level) {
            case CRITICAL -> CRITICAL;
            case HIGH -> HIGH;
            case MODERATE -> MODERATE;
            case LOW -> LOW;
            case UNKNOWN -> MODERATE;
        };
    }

    /**
     * Parses a string representation case-insensitively into a RelocationUrgency.
     */
    public static RelocationUrgency fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (RelocationUrgency urgency : values()) {
            if (urgency.name().equals(clean)) {
                return urgency;
            }
        }
        throw new InvalidHazardParameterException("Invalid relocationUrgency '" + text + "'. Allowed values: CRITICAL, HIGH, MODERATE, LOW");
    }
}
