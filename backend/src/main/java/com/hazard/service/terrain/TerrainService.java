package com.hazard.service.terrain;

import com.hazard.domain.terrain.DemTile;
import com.hazard.repository.terrain.DemTileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Domain service managing Digital Elevation Model (DEM) metadata catalog and tile footprint lookup.
 * NOTE: Actual raster pixels remain in external GeoTIFF files on disk.
 */
@Service
@Transactional(readOnly = true)
public class TerrainService {

    private final DemTileRepository demTileRepository;

    public TerrainService(DemTileRepository demTileRepository) {
        this.demTileRepository = demTileRepository;
    }

    /**
     * Lists all registered DEM tile metadata records in the catalog.
     */
    public List<DemTile> getAllAvailableDemTiles() {
        return demTileRepository.findAll();
    }

    /**
     * Finds a DEM tile metadata entry by its unique tile name.
     */
    public Optional<DemTile> getDemTileByName(String tileName) {
        if (tileName == null || tileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tile name cannot be null or empty");
        }
        return demTileRepository.findByTileName(tileName.trim());
    }

    /**
     * Spatial query: Resolves the DEM raster tile footprint covering a geographic coordinate.
     */
    public Optional<DemTile> getDemTileForCoordinate(double longitude, double latitude) {
        validateCoordinates(longitude, latitude);
        return demTileRepository.findTileContainingPoint(longitude, latitude);
    }

    /**
     * Finds all DEM tile footprints intersecting a given Bihar district.
     */
    public List<DemTile> getDemTilesIntersectingDistrict(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }
        return demTileRepository.findTilesIntersectingDistrict(districtName.trim());
    }

    private void validateCoordinates(double longitude, double latitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90.0 and 90.0 degrees. Provided: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180.0 and 180.0 degrees. Provided: " + longitude);
        }
    }
}
