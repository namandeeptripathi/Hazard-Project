package com.hazard.repository.boundaries;

/**
 * Spring Data JPA Projection for PostGIS geodesic distance queries between
 * candidate points and district polygon boundaries.
 */
public interface DistrictDistanceProjection {

    /**
     * Name of the intersecting or nearest district boundary.
     */
    String getDistrictName();

    /**
     * Minimum geodesic distance in meters from the query point to the district polygon.
     * Returns 0.0 if the point is contained within the district polygon.
     */
    Double getDistanceMeters();
}
