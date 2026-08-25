package com.hazard.domain.exposure;

/**
 * Identifies the provenance and methodology used to derive population figures.
 */
public enum PopulationDataSource {
    DIRECT_CENSUS_OSM("Direct Census / OSM Attribute", "Explicit population attribute from census or OSM records"),
    RESIDENTIAL_FOOTPRINT_ESTIMATE("Residential Footprint Density", "Derived from residential polygon footprint area and housing density constants"),
    SETTLEMENT_ARCHETYPE("Settlement Archetype Default", "Estimated from settlement tier archetype defaults (city, town, village, hamlet)"),
    HYBRID_COMPOSITE("Hybrid Composite Model", "Combined exact counts and spatial density estimation across intersecting features");

    private final String displayName;
    private final String description;

    PopulationDataSource(String displayName, String description) {
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
