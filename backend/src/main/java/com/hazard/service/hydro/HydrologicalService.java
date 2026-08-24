package com.hazard.service.hydro;

import com.hazard.domain.hydro.HydroRiver;
import com.hazard.domain.hydro.OsmWaterway;
import com.hazard.repository.hydro.HydroRiverRepository;
import com.hazard.repository.hydro.OsmWaterwayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Domain service managing river network routing, discharge analysis, and waterbody proximity.
 */
@Service
@Transactional(readOnly = true)
public class HydrologicalService {

    private final HydroRiverRepository hydroRiverRepository;
    private final OsmWaterwayRepository osmWaterwayRepository;

    public HydrologicalService(HydroRiverRepository hydroRiverRepository,
                               OsmWaterwayRepository osmWaterwayRepository) {
        this.hydroRiverRepository = hydroRiverRepository;
        this.osmWaterwayRepository = osmWaterwayRepository;
    }

    /**
     * Retrieves river reaches intersecting a district that satisfy a minimum Strahler stream order.
     */
    public List<HydroRiver> getRiversInDistrict(String districtName, int minStrahler) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }
        if (minStrahler < 1) {
            throw new IllegalArgumentException("Minimum Strahler order must be >= 1. Provided: " + minStrahler);
        }
        return hydroRiverRepository.findRiversInDistrict(districtName.trim(), minStrahler);
    }

    /**
     * Proximity query: Retrieves river network reaches near a given coordinate.
     */
    public List<HydroRiver> getRiversNearLocation(double longitude, double latitude, double radiusMeters, int maxResults) {
        validateCoordinates(longitude, latitude);
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Search radius must be positive. Provided: " + radiusMeters);
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("maxResults must be > 0. Provided: " + maxResults);
        }
        return hydroRiverRepository.findRiversNearPoint(longitude, latitude, radiusMeters, maxResults);
    }

    /**
     * Proximity query: Retrieves local OSM waterways (canals, ditches, streams) near a coordinate.
     */
    public List<OsmWaterway> getWaterwaysNearLocation(double longitude, double latitude, double radiusMeters, int maxResults) {
        validateCoordinates(longitude, latitude);
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Search radius must be positive. Provided: " + radiusMeters);
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("maxResults must be > 0. Provided: " + maxResults);
        }
        return osmWaterwayRepository.findWaterwaysNearPoint(longitude, latitude, radiusMeters, maxResults);
    }

    /**
     * Retrieves major river reaches with Strahler order >= minStrahler.
     */
    public List<HydroRiver> getMajorRiversByStrahlerOrder(int minStrahler) {
        if (minStrahler < 1) {
            throw new IllegalArgumentException("Minimum Strahler order must be >= 1. Provided: " + minStrahler);
        }
        return hydroRiverRepository.findByOrdStraGreaterThanEqualOrderByDisAvCmsDesc(minStrahler);
    }

    /**
     * Looks up a specific river reach by its global HydroRIVERS identifier.
     */
    public Optional<HydroRiver> getRiverByHyrivId(Long hyrivId) {
        if (hyrivId == null) {
            throw new IllegalArgumentException("hyrivId cannot be null");
        }
        return hydroRiverRepository.findByHyrivId(hyrivId);
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
