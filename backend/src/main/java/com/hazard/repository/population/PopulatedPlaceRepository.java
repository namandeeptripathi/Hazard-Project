package com.hazard.repository.population;

import com.hazard.domain.population.PopulatedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Repository for Populated Places & Residential Footprints (population.populated_places)
 */
@Repository
public interface PopulatedPlaceRepository extends JpaRepository<PopulatedPlace, Integer> {

    List<PopulatedPlace> findByAdm2NameIgnoreCase(String adm2Name);

    List<PopulatedPlace> findByAdm2NameIgnoreCaseAndPlace(String adm2Name, String place);

    /**
     * Spatial query to find urban towns and populated settlements within a specific district.
     */
    @Query(value = "SELECT p.* FROM population.populated_places p " +
                   "JOIN boundaries.district_boundaries d ON ST_Intersects(p.geom, d.geom) " +
                   "WHERE UPPER(d.name_2) = UPPER(:districtName) " +
                   "  AND p.place = 'town' " +
                   "ORDER BY p.name ASC", nativeQuery = true)
    List<PopulatedPlace> findTownsInDistrict(@Param("districtName") String districtName);

    /**
     * Proximity query: find populated place footprints within distance of a coordinate.
     */
    @Query(value = "SELECT * FROM population.populated_places p " +
                   "WHERE ST_DWithin(p.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography, :distanceMeters) " +
                   "LIMIT :maxResults", nativeQuery = true)
    List<PopulatedPlace> findPopulatedPlacesNearPoint(@Param("longitude") double longitude,
                                                      @Param("latitude") double latitude,
                                                      @Param("distanceMeters") double distanceMeters,
                                                      @Param("maxResults") int maxResults);
}
