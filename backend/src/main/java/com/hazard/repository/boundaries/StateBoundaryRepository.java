package com.hazard.repository.boundaries;

import com.hazard.domain.boundaries.StateBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access Repository for State Boundary (boundaries.state_boundaries)
 */
@Repository
public interface StateBoundaryRepository extends JpaRepository<StateBoundary, Integer> {

    Optional<StateBoundary> findByName1IgnoreCase(String name1);

    @Query(value = "SELECT * FROM boundaries.state_boundaries s " +
                   "WHERE ST_Contains(s.geom, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)) " +
                   "LIMIT 1", nativeQuery = true)
    Optional<StateBoundary> findStateContainingPoint(@Param("longitude") double longitude,
                                                     @Param("latitude") double latitude);
}
