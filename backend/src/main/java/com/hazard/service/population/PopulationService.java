package com.hazard.service.population;

import com.hazard.domain.population.OsmSettlement;
import com.hazard.domain.population.PopulatedPlace;
import com.hazard.repository.population.OsmSettlementRepository;
import com.hazard.repository.population.PopulatedPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Domain service managing settlement exposure, residential footprints, and demographic access.
 */
@Service
@Transactional(readOnly = true)
public class PopulationService {

    private final PopulatedPlaceRepository populatedPlaceRepository;
    private final OsmSettlementRepository osmSettlementRepository;

    public PopulationService(PopulatedPlaceRepository populatedPlaceRepository,
                             OsmSettlementRepository osmSettlementRepository) {
        this.populatedPlaceRepository = populatedPlaceRepository;
        this.osmSettlementRepository = osmSettlementRepository;
    }

    /**
     * Retrieves mapped residential town footprints intersecting a specific Bihar district.
     */
    public List<PopulatedPlace> getTownsInDistrict(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }
        return populatedPlaceRepository.findTownsInDistrict(districtName.trim());
    }

    /**
     * Proximity query: Retrieves residential footprints near a given coordinate.
     */
    public List<PopulatedPlace> getPopulatedPlacesNearLocation(double longitude, double latitude,
                                                               double radiusMeters, int maxResults) {
        validateCoordinates(longitude, latitude);
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Search radius must be positive. Provided: " + radiusMeters);
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("maxResults must be > 0. Provided: " + maxResults);
        }
        return populatedPlaceRepository.findPopulatedPlacesNearPoint(longitude, latitude, radiusMeters, maxResults);
    }

    /**
     * Retrieves named settlement nodes (villages, towns, cities) intersecting a district.
     */
    public List<OsmSettlement> getSettlementsInDistrict(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }
        return osmSettlementRepository.findSettlementsInDistrict(districtName.trim());
    }

    /**
     * Proximity query: Retrieves nearest settlement nodes to a coordinate.
     */
    public List<OsmSettlement> getSettlementsNearLocation(double longitude, double latitude,
                                                          double radiusMeters, int maxResults) {
        validateCoordinates(longitude, latitude);
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Search radius must be positive. Provided: " + radiusMeters);
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("maxResults must be > 0. Provided: " + maxResults);
        }
        return osmSettlementRepository.findSettlementsNearPoint(longitude, latitude, radiusMeters, maxResults);
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
