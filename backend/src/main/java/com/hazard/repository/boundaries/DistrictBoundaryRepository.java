package com.hazard.repository.boundaries;

import com.hazard.domain.boundaries.DistrictBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Repository for District Boundaries (boundaries.district_boundaries)
 */
@Repository
public interface DistrictBoundaryRepository extends JpaRepository<DistrictBoundary, Integer> {

    Optional<DistrictBoundary> findByName2IgnoreCase(String name2);

    Optional<DistrictBoundary> findByGid2(String gid2);

    List<DistrictBoundary> findAllByOrderByName2Asc();

    /**
     * Point-in-Polygon query to identify the district containing the given WGS 84 coordinate.
     */
    @Query(value = "SELECT * FROM boundaries.district_boundaries d " +
                   "WHERE ST_Contains(d.geom, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)) " +
                   "LIMIT 1", nativeQuery = true)
    Optional<DistrictBoundary> findDistrictContainingPoint(@Param("longitude") double longitude,
                                                           @Param("latitude") double latitude);

    /**
     * Spatial intersection query to identify all districts intersecting a bounding geometry WKT.
     */
    @Query(value = "SELECT * FROM boundaries.district_boundaries d " +
                   "WHERE ST_Intersects(d.geom, ST_SetSRID(ST_GeomFromText(:wktPolygon), 4326)) " +
                   "ORDER BY d.name_2 ASC", nativeQuery = true)
    List<DistrictBoundary> findDistrictsIntersectingGeometry(@Param("wktPolygon") String wktPolygon);

    /**
     * Geodesic distance query to calculate the minimum distance in meters from a WGS 84 point
     * to the nearest district polygon in the specified target district list.
     * Returns 0.0 meters if the point is contained within the district polygon.
     */
    @Query(value = "SELECT d.name_2 AS \"districtName\", " +
                   "ST_Distance(d.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography) AS \"distanceMeters\" " +
                   "FROM boundaries.district_boundaries d " +
                   "WHERE UPPER(d.name_2) IN (:districtNames) " +
                   "ORDER BY \"distanceMeters\" ASC " +
                   "LIMIT 1", nativeQuery = true)
    Optional<DistrictDistanceProjection> findNearestDistrictDistance(@Param("longitude") double longitude,
                                                                     @Param("latitude") double latitude,
                                                                     @Param("districtNames") List<String> districtNames);
}
