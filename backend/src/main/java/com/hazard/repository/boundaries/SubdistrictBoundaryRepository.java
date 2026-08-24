package com.hazard.repository.boundaries;

import com.hazard.domain.boundaries.SubdistrictBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Repository for Sub-district Boundaries (boundaries.subdistrict_boundaries)
 */
@Repository
public interface SubdistrictBoundaryRepository extends JpaRepository<SubdistrictBoundary, Integer> {

    List<SubdistrictBoundary> findByName2IgnoreCase(String name2);

    Optional<SubdistrictBoundary> findByName3IgnoreCase(String name3);

    @Query(value = "SELECT * FROM boundaries.subdistrict_boundaries s " +
                   "WHERE ST_Contains(s.geom, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)) " +
                   "LIMIT 1", nativeQuery = true)
    Optional<SubdistrictBoundary> findSubdistrictContainingPoint(@Param("longitude") double longitude,
                                                                 @Param("latitude") double latitude);
}
