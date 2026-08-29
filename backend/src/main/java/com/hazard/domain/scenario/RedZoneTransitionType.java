package com.hazard.domain.scenario;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Stage 9C — Enum representing Red-Zone transition status for a district under scenario simulation.
 *
 * Classifies the shift between baseline Red-Zone state and simulated Red-Zone state:
 * - UNCHANGED_NON_RED_ZONE (NO -> NO): District remains outside the Red Zone.
 * - ENTERED_RED_ZONE (NO -> YES): District crossed the >=0.60 threshold and newly entered the Red Zone.
 * - LEFT_RED_ZONE (YES -> NO): District mitigated/dropped below the 0.60 threshold and exited the Red Zone.
 * - RETAINED_RED_ZONE (YES -> YES): District was already in the Red Zone and remains in the Red Zone.
 */
@Schema(description = "Red-Zone status transition between baseline and simulated disaster conditions")
public enum RedZoneTransitionType {

    UNCHANGED_NON_RED_ZONE("Unchanged (Non-Red Zone)", "District remains outside the Red Zone threshold (<0.60)", false, false, "#4CAF50"),
    ENTERED_RED_ZONE("Newly Entered Red Zone", "District crossed into the Red Zone (>=0.60) under simulated conditions", false, true, "#F44336"),
    LEFT_RED_ZONE("Exited Red Zone", "District dropped below the Red Zone threshold (<0.60) under simulated conditions", true, false, "#2196F3"),
    RETAINED_RED_ZONE("Retained Red Zone", "District was and remains in the Red Zone (>=0.60)", true, true, "#9C27B0");

    private final String displayName;
    private final String description;
    private final boolean baselineIsRedZone;
    private final boolean simulatedIsRedZone;
    private final String badgeColorHex;

    RedZoneTransitionType(String displayName,
                          String description,
                          boolean baselineIsRedZone,
                          boolean simulatedIsRedZone,
                          String badgeColorHex) {
        this.displayName = displayName;
        this.description = description;
        this.baselineIsRedZone = baselineIsRedZone;
        this.simulatedIsRedZone = simulatedIsRedZone;
        this.badgeColorHex = badgeColorHex;
    }

    /**
     * Resolves the transition type from baseline and simulated Red-Zone booleans.
     */
    public static RedZoneTransitionType from(boolean baselineIsRed, boolean simulatedIsRed) {
        if (!baselineIsRed && !simulatedIsRed) {
            return UNCHANGED_NON_RED_ZONE;
        } else if (!baselineIsRed && simulatedIsRed) {
            return ENTERED_RED_ZONE;
        } else if (baselineIsRed && !simulatedIsRed) {
            return LEFT_RED_ZONE;
        } else {
            return RETAINED_RED_ZONE;
        }
    }

    public boolean isChanged() {
        return baselineIsRedZone != simulatedIsRedZone;
    }

    public boolean isNewlyEntered() {
        return this == ENTERED_RED_ZONE;
    }

    public boolean isLeft() {
        return this == LEFT_RED_ZONE;
    }

    public boolean isRetained() {
        return this == RETAINED_RED_ZONE;
    }

    public boolean isBaselineIsRedZone() {
        return baselineIsRedZone;
    }

    public boolean isSimulatedIsRedZone() {
        return simulatedIsRedZone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getBadgeColorHex() {
        return badgeColorHex;
    }
}
