package com.hazard.domain.hazard;

/**
 * Empirical evidence and match confidence for multi-hazard coincidence.
 * Distinguishes concrete spatial and temporal evidence from probabilistic assumptions.
 */
public enum MultiHazardConfidence {
    FULL_MATCH("Full Match", "Both spatial coincidence and temporal overlap verified by empirical observations"),
    SPATIAL_ONLY("Spatial Match Only", "Spatial coincidence confirmed within district/buffer, but time periods do not overlap"),
    TEMPORAL_ONLY("Temporal Match Only", "Temporal overlap confirmed, but geographic locations are disjoint"),
    SINGLE_HAZARD_CONTEXT("Single Hazard Context", "Observation represents an isolated single-hazard occurrence"),
    UNLOCATED_EXCLUDED("Unlocated Excluded", "Tabular records lacking discrete coordinates are excluded from spatial matching");

    private final String displayName;
    private final String description;

    MultiHazardConfidence(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
