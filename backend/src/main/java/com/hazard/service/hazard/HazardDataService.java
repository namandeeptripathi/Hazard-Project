package com.hazard.service.hazard;

import com.hazard.domain.hazard.DfoFloodEvent;
import com.hazard.domain.hazard.EmdatFloodRecord;
import com.hazard.repository.hazard.DfoFloodEventRepository;
import com.hazard.repository.hazard.EmdatFloodRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Domain service managing historical disaster event retrieval and spatial hazard analysis.
 */
@Service
@Transactional(readOnly = true)
public class HazardDataService {

    private final DfoFloodEventRepository dfoFloodEventRepository;
    private final EmdatFloodRecordRepository emdatFloodRecordRepository;

    public HazardDataService(DfoFloodEventRepository dfoFloodEventRepository,
                             EmdatFloodRecordRepository emdatFloodRecordRepository) {
        this.dfoFloodEventRepository = dfoFloodEventRepository;
        this.emdatFloodRecordRepository = emdatFloodRecordRepository;
    }

    /**
     * Retrieves all historical flood events in Bihar recorded by Dartmouth Flood Observatory.
     */
    public List<DfoFloodEvent> getAllHistoricalFloodEvents() {
        return dfoFloodEventRepository.findAll();
    }

    /**
     * Retrieves flood events that began within a specific date window.
     */
    public List<DfoFloodEvent> getFloodEventsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }
        return dfoFloodEventRepository.findByBeganDateBetweenOrderByBeganDateDesc(startDate, endDate);
    }

    /**
     * Retrieves historical flood events ordered by severity index.
     */
    public List<DfoFloodEvent> getSevereFloodEvents() {
        return dfoFloodEventRepository.findAllByOrderBySeverityDesc();
    }

    /**
     * Retrieves historical flood events ordered by displaced population count.
     */
    public List<DfoFloodEvent> getFloodEventsByDisplacement() {
        return dfoFloodEventRepository.findAllByOrderByDisplacedDesc();
    }

    /**
     * Spatial query: Retrieves historical flood events whose geometry intersects a specific Bihar district.
     */
    public List<DfoFloodEvent> getFloodEventsInDistrict(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }
        return dfoFloodEventRepository.findEventsInDistrict(districtName.trim());
    }

    /**
     * Spatial proximity query: Retrieves flood events within a geographic radius of a point.
     */
    public List<DfoFloodEvent> getFloodEventsNearLocation(double longitude, double latitude, double radiusMeters) {
        validateCoordinates(longitude, latitude);
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Search radius must be positive. Provided: " + radiusMeters);
        }
        return dfoFloodEventRepository.findEventsNearPoint(longitude, latitude, radiusMeters);
    }

    /**
     * Retrieves national EM-DAT macro-impact flood records for a given year window.
     */
    public List<EmdatFloodRecord> getEmdatRecordsForYearRange(int startYear, int endYear) {
        if (startYear > endYear) {
            throw new IllegalArgumentException("Start year (" + startYear + ") cannot be after end year (" + endYear + ")");
        }
        return emdatFloodRecordRepository.findByYearBetweenOrderByYearDesc(startYear, endYear);
    }

    /**
     * Retrieves all EM-DAT historical flood disaster records.
     */
    public List<EmdatFloodRecord> getAllEmdatRecords() {
        return emdatFloodRecordRepository.findAll();
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
