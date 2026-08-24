package com.hazard.repository.terrain;

import com.hazard.domain.terrain.DemTile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Repository for DEM Metadata Catalog & Tile Footprints (terrain.dem_tiles)
 */
@Repository
public interface DemTileRepository extends JpaRepository<DemTile, Integer> {

    Optional<DemTile> findByTileName(String tileName);

    /**
     * Identifies the DEM tile footprint covering a specific geographic coordinate.
     */
    @Query(value = "SELECT * FROM terrain.dem_tiles t " +
                   "WHERE ST_Intersects(t.geom, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)) " +
                   "LIMIT 1", nativeQuery = true)
    Optional<DemTile> findTileContainingPoint(@Param("longitude") double longitude,
                                              @Param("latitude") double latitude);

    /**
     * Finds DEM tile footprints intersecting a given Bihar district.
     */
    @Query(value = "SELECT t.* FROM terrain.dem_tiles t " +
                   "JOIN boundaries.district_boundaries d ON ST_Intersects(t.geom, d.geom) " +
                   "WHERE UPPER(d.name_2) = UPPER(:districtName) " +
                   "ORDER BY t.tile_name ASC", nativeQuery = true)
    List<DemTile> findTilesIntersectingDistrict(@Param("districtName") String districtName);
}
