package com.hazard.domain.hazard;

/**
 * Geometric and spatial relationship between multiple hazard occurrences.
 */
public enum SpatialRelationship {
    EXACT_POINT("Exact Point Coincidence", "Points coincide within 100 meters"),
    PROXIMITY("Spatial Proximity", "Points fall within spatial proximity radius via PostGIS ST_DWithin"),
    DISTRICT_CONTAINMENT("District Polygon Containment", "Hazards occur within the same administrative district polygon"),
    DISJOINT("Spatially Disjoint", "No spatial intersection or proximity detected");

    private final String displayName;
    private final String description;

    SpatialRelationship(String displayName, String description) {
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
