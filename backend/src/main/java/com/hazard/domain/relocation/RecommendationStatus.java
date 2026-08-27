package com.hazard.domain.relocation;

import com.hazard.exception.InvalidHazardParameterException;

/**
 * Stage 7B.1 — Relocation Recommendation Status.
 *
 * Represents the conclusive feasibility and operational status of a relocation recommendation:
 * - RECOMMENDED: A feasible, high-suitability destination with sufficient shelter capacity has been identified.
 * - NO_FEASIBLE_DESTINATION: Zero candidate safe sites passed the multi-gate feasibility constraints.
 * - CAPACITY_DEFICIT: Candidate safe sites exist but their remaining available capacity cannot fully accommodate the evacuee population.
 * - INVALID_SOURCE: The source habitation context is missing, malformed, or has invalid geographic coordinates.
 */
public enum RecommendationStatus {

    RECOMMENDED("Recommended", true, "#2E7D32",
            "A viable and feasible emergency destination has been identified and allocated."),

    NO_FEASIBLE_DESTINATION("No Feasible Destination", false, "#C62828",
            "No candidate safe sites passed safety, suitability, distance, or access feasibility constraints."),

    CAPACITY_DEFICIT("Capacity Deficit", false, "#E65100",
            "Available safe sites cannot fully accommodate the vulnerable population."),

    INVALID_SOURCE("Invalid Source Context", false, "#424242",
            "Source habitation details or coordinates are missing or invalid.");

    private final String displayName;
    private final boolean actionable;
    private final String colorHex;
    private final String description;

    RecommendationStatus(String displayName, boolean actionable, String colorHex, String description) {
        this.displayName = displayName;
        this.actionable = actionable;
        this.colorHex = colorHex;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActionable() {
        return actionable;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Parses a string representation case-insensitively into a RecommendationStatus.
     */
    public static RecommendationStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (RecommendationStatus status : values()) {
            if (status.name().equals(clean)) {
                return status;
            }
        }
        throw new InvalidHazardParameterException(
                "Invalid recommendationStatus '" + text + "'. Allowed values: RECOMMENDED, NO_FEASIBLE_DESTINATION, CAPACITY_DEFICIT, INVALID_SOURCE");
    }
}
