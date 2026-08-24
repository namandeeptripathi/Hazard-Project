package com.hazard.repository.population;

import com.hazard.domain.population.OsmSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Repository for OSM Settlement Nodes (population.osm_settlements)
 */
@Repository
public interface OsmSettlementRepository extends JpaRepository<OsmSettlement, Integer> {

    Optional<OsmSettlement> findByNameIgnoreCase(String name);

    List<OsmSettlement> findByPlaceIgnoreCase(String place);

    /**
     * Spatial query to find settlement nodes in a specific Bihar district ordered by population.
     */
    @Query(value = "SELECT s.* FROM population.osm_settlements s " +
                   "JOIN boundaries.district_boundaries d ON ST_Intersects(s.geom, d.geom) " +
                   "WHERE UPPER(d.name_2) = UPPER(:districtName) " +
                   "ORDER BY s.population DESC NULLS LAST", nativeQuery = true)
    List<OsmSettlement> findSettlementsInDistrict(@Param("districtName") String districtName);

    /**
     * Proximity query: find closest settlements to a given point.
     */
    @Query(value = "SELECT * FROM population.osm_settlements s " +
                   "WHERE ST_DWithin(s.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography, :distanceMeters) " +
                   "ORDER BY ST_Distance(s.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography) " +
                   "LIMIT :maxResults", nativeQuery = true)
    List<OsmSettlement> findSettlementsNearPoint(@Param("longitude") double longitude,
                                                 @Param("latitude") double latitude,
                                                 @Param("distanceMeters") double distanceMeters,
                                                 @Param("maxResults") int maxResults);
}
