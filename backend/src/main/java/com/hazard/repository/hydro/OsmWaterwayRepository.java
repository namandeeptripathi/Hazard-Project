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
}
