package com.hazard.service.processing;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.dto.processing.ProcessedHazardObservation;
import com.hazard.dto.processing.ProcessingMetadata;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service providing dynamic spatial association between hazard observations
 * and Bihar administrative boundaries using PostGIS ST_Contains / ST_Intersects.
 */
@Service
@Transactional(readOnly = true)
public class SpatialAssociationService {

    private final DistrictBoundaryRepository districtBoundaryRepository;

    public SpatialAssociationService(DistrictBoundaryRepository districtBoundaryRepository) {
        this.districtBoundaryRepository = districtBoundaryRepository;
    }

    /**
     * Resolves and associates the administrative district for a processed hazard observation.
     */
    public void associateDistrict(ProcessedHazardObservation observation) {
        Double lon = observation.getLongitude();
        Double lat = observation.getLatitude();
        ProcessingMetadata meta = observation.getProcessingMetadata();

        if (lon == null || lat == null) {
            observation.setAssociatedDistrict(null);
            observation.setIsWithinBiharBoundary(false);
            return;
        }

        Optional<DistrictBoundary> districtOpt = districtBoundaryRepository.findDistrictContainingPoint(lon, lat);
        if (districtOpt.isPresent()) {
            String districtName = districtOpt.get().getName2();
            observation.setAssociatedDistrict(districtName);
            observation.setIsWithinBiharBoundary(true);
            meta.setSpatialResolutionStatus("CONTAINED_IN_DISTRICT: " + districtName);
            meta.addDerivedMetric("associatedDistrict: " + districtName);
        } else {
            observation.setAssociatedDistrict(null);
            observation.setIsWithinBiharBoundary(false);
            meta.setSpatialResolutionStatus("OUTSIDE_BIHAR_STUDY_AREA");
            meta.addWarning("Coordinates fall outside Bihar state administrative boundary");
        }
    }

    /**
     * Resolves the district boundary name for a given coordinate pair.
     */
    public Optional<String> resolveDistrictName(double longitude, double latitude) {
        return districtBoundaryRepository.findDistrictContainingPoint(longitude, latitude)
                .map(DistrictBoundary::getName2);
    }
}
