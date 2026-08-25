package com.hazard.domain.hazard;

/**
 * Functional category of map-ready hazard GIS layers.
 */
public enum HazardLayerCategory {
    EVENT_LAYER("Event Observation Layer", "Point-based observations of discrete hazard events"),
    HAZARD_SCORE_LAYER("Single-Hazard Score Layer", "Normalized and weighted single-hazard intensity score points"),
    MULTI_HAZARD_LAYER("Multi-Hazard Index Layer", "Cross-hazard synthesized multi-hazard index points and dominance"),
    DISTRICT_SUMMARY_LAYER("District Hazard Summary Layer", "Choropleth district boundary polygons with aggregated hazard indicators"),
    REFERENCE_LAYER("Reference Spatial Layer", "Static geospatial baseline boundaries, waterways, and river networks");

    private final String displayName;
    private final String description;

    HazardLayerCategory(String displayName, String description) {
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
