package com.hazard.repository.hazard;

import com.hazard.domain.hazard.DfoFloodEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Repository for DFO Flood Events (hazard.dfo_flood_events)
 */
@Repository
public interface DfoFloodEventRepository extends JpaRepository<DfoFloodEvent, Integer> {

    List<DfoFloodEvent> findByBeganDateBetweenOrderByBeganDateDesc(LocalDate startDate, LocalDate endDate);

    List<DfoFloodEvent> findAllByOrderBySeverityDesc();

    List<DfoFloodEvent> findAllByOrderByDisplacedDesc();

    List<DfoFloodEvent> findAllByOrderByAffectedSqkmDesc();

    /**
     * Spatial query to find historical flood events intersecting a specific Bihar district.
     */
    @Query(value = "SELECT f.* FROM hazard.dfo_flood_events f " +
                   "JOIN boundaries.district_boundaries d ON ST_Intersects(f.geom, d.geom) " +
                   "WHERE UPPER(d.name_2) = UPPER(:districtName) " +
                   "ORDER BY f.began_date DESC", nativeQuery = true)
    List<DfoFloodEvent> findEventsInDistrict(@Param("districtName") String districtName);

    /**
     * Spatial proximity query: find flood event centroids within distance of a given point.
     */
    @Query(value = "SELECT * FROM hazard.dfo_flood_events f " +
                   "WHERE ST_DWithin(f.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography, :distanceMeters) " +
                   "ORDER BY ST_Distance(f.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography)", nativeQuery = true)
    List<DfoFloodEvent> findEventsNearPoint(@Param("longitude") double longitude,
                                            @Param("latitude") double latitude,
                                            @Param("distanceMeters") double distanceMeters);

    /**
     * Spatial bounding box query: find flood event centroids within a geographic bounding box [minLon, minLat, maxLon, maxLat].
     */
    @Query(value = "SELECT * FROM hazard.dfo_flood_events f " +
                   "WHERE ST_Intersects(f.geom, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)) " +
                   "ORDER BY f.began_date DESC", nativeQuery = true)
    List<DfoFloodEvent> findEventsInBoundingBox(@Param("minLon") double minLon,
                                                @Param("minLat") double minLat,
                                                @Param("maxLon") double maxLon,
                                                @Param("maxLat") double maxLat);
}
