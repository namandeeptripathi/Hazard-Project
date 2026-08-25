package com.hazard.repository.hydro;

import com.hazard.domain.hydro.OsmWaterway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Repository for OSM Waterways and Waterbodies (hydro.osm_waterways)
 */
@Repository
public interface OsmWaterwayRepository extends JpaRepository<OsmWaterway, Integer> {

    List<OsmWaterway> findByWaterwayIgnoreCase(String waterway);

    List<OsmWaterway> findByWaterIgnoreCase(String water);

    /**
     * Proximity query to locate waterbodies/canals near a given coordinate.
     */
    @Query(value = "SELECT * FROM hydro.osm_waterways w " +
                   "WHERE ST_DWithin(w.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography, :distanceMeters) " +
                   "LIMIT :maxResults", nativeQuery = true)
    List<OsmWaterway> findWaterwaysNearPoint(@Param("longitude") double longitude,
                                             @Param("latitude") double latitude,
                                             @Param("distanceMeters") double distanceMeters,
                                             @Param("maxResults") int maxResults);

    /**
     * Finds water and drainage infrastructure intersecting an arbitrary WKT polygon.
     */
    @Query(value = "SELECT w.* FROM hydro.osm_waterways w " +
                   "WHERE w.geom IS NOT NULL " +
                   "AND ST_Intersects(w.geom, ST_SetSRID(ST_GeomFromText(:wkt), 4326))", nativeQuery = true)
    List<OsmWaterway> findInfrastructureIntersectingGeometryWkt(@Param("wkt") String wkt);

    /**
     * Finds water and drainage infrastructure within a radial buffer of a point coordinate.
     */
    @Query(value = "SELECT w.* FROM hydro.osm_waterways w " +
                   "WHERE w.geom IS NOT NULL " +
                   "AND ST_Intersects(w.geom, ST_Buffer(ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography, :distanceMeters)::geometry)", nativeQuery = true)
    List<OsmWaterway> findInfrastructureWithinBufferOfPoint(@Param("longitude") double longitude,
                                                            @Param("latitude") double latitude,
                                                            @Param("distanceMeters") double distanceMeters);

    /**
     * Finds all water and drainage infrastructure inside an administrative district boundary.
     */
    @Query(value = "SELECT w.* FROM hydro.osm_waterways w " +
                   "JOIN boundaries.district_boundaries d " +
                   "ON ST_Intersects(w.geom, d.geom) " +
                   "WHERE LOWER(d.name_2) = LOWER(:districtName)", nativeQuery = true)
    List<OsmWaterway> findInfrastructureInDistrictSpatial(@Param("districtName") String districtName);
}
