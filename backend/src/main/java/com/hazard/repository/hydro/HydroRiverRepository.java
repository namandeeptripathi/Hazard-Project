package com.hazard.repository.hydro;

import com.hazard.domain.hydro.HydroRiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Repository for River Network (hydro.hydrorivers)
 */
@Repository
public interface HydroRiverRepository extends JpaRepository<HydroRiver, Integer> {

    Optional<HydroRiver> findByHyrivId(Long hyrivId);

    List<HydroRiver> findByOrdStraGreaterThanEqualOrderByDisAvCmsDesc(Integer ordStra);

    /**
     * Spatial query to find river reaches intersecting a district with minimum Strahler stream order.
     */
    @Query(value = "SELECT h.* FROM hydro.hydrorivers h " +
                   "JOIN boundaries.district_boundaries d ON ST_Intersects(h.geom, d.geom) " +
                   "WHERE UPPER(d.name_2) = UPPER(:districtName) " +
                   "  AND h.ord_stra >= :minStrahler " +
                   "ORDER BY h.dis_av_cms DESC", nativeQuery = true)
    List<HydroRiver> findRiversInDistrict(@Param("districtName") String districtName,
                                          @Param("minStrahler") int minStrahler);

    /**
     * Spatial proximity query: find river reaches within distance of a point.
     */
    @Query(value = "SELECT * FROM hydro.hydrorivers h " +
                   "WHERE ST_DWithin(h.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography, :distanceMeters) " +
                   "ORDER BY ST_Distance(h.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography) " +
                   "LIMIT :maxResults", nativeQuery = true)
    List<HydroRiver> findRiversNearPoint(@Param("longitude") double longitude,
                                         @Param("latitude") double latitude,
                                         @Param("distanceMeters") double distanceMeters,
                                         @Param("maxResults") int maxResults);
}
