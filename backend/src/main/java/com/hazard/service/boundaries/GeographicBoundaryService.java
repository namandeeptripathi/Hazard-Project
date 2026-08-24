package com.hazard.service.boundaries;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.boundaries.StateBoundary;
import com.hazard.domain.boundaries.SubdistrictBoundary;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.boundaries.StateBoundaryRepository;
import com.hazard.repository.boundaries.SubdistrictBoundaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service providing administrative boundary spatial resolution and geographic queries.
 */
@Service
@Transactional(readOnly = true)
public class GeographicBoundaryService {

    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final StateBoundaryRepository stateBoundaryRepository;
    private final SubdistrictBoundaryRepository subdistrictBoundaryRepository;

    public GeographicBoundaryService(DistrictBoundaryRepository districtBoundaryRepository,
                                     StateBoundaryRepository stateBoundaryRepository,
                                     SubdistrictBoundaryRepository subdistrictBoundaryRepository) {
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.stateBoundaryRepository = stateBoundaryRepository;
        this.subdistrictBoundaryRepository = subdistrictBoundaryRepository;
    }

    /**
     * Resolves the administrative district polygon containing a specific WGS 84 coordinate.
     */
    public Optional<DistrictBoundary> getDistrictByCoordinate(double longitude, double latitude) {
        validateCoordinates(longitude, latitude);
        return districtBoundaryRepository.findDistrictContainingPoint(longitude, latitude);
    }

    /**
     * Resolves the state boundary containing a specific WGS 84 coordinate.
     */
    public Optional<StateBoundary> getStateByCoordinate(double longitude, double latitude) {
        validateCoordinates(longitude, latitude);
        return stateBoundaryRepository.findStateContainingPoint(longitude, latitude);
    }

    /**
     * Resolves the subdistrict/tehsil boundary containing a specific WGS 84 coordinate.
     */
    public Optional<SubdistrictBoundary> getSubdistrictByCoordinate(double longitude, double latitude) {
        validateCoordinates(longitude, latitude);
        return subdistrictBoundaryRepository.findSubdistrictContainingPoint(longitude, latitude);
    }

    /**
     * Retrieves all 38 Bihar district boundary polygons sorted alphabetically.
     */
    public List<DistrictBoundary> getAllDistricts() {
        return districtBoundaryRepository.findAllByOrderByName2Asc();
    }

    /**
     * Retrieves a district boundary by name (case-insensitive).
     */
    public Optional<DistrictBoundary> getDistrictByName(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }
        return districtBoundaryRepository.findByName2IgnoreCase(districtName.trim());
    }

    /**
     * Retrieves all subdistricts belonging to a specific district name.
     */
    public List<SubdistrictBoundary> getSubdistrictsInDistrict(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }
        return subdistrictBoundaryRepository.findByName2IgnoreCase(districtName.trim());
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
