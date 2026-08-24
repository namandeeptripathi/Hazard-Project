package com.hazard.service.weather;

import com.hazard.domain.weather.HourlyWeather;
import com.hazard.repository.weather.HourlyWeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Domain service managing hourly meteorological observations and rainfall monitoring windows.
 */
@Service
@Transactional(readOnly = true)
public class WeatherDataService {

    private final HourlyWeatherRepository hourlyWeatherRepository;

    public WeatherDataService(HourlyWeatherRepository hourlyWeatherRepository) {
        this.hourlyWeatherRepository = hourlyWeatherRepository;
    }

    /**
     * Retrieves contiguous hourly weather observations for a station within a time window.
     */
    public List<HourlyWeather> getHistoricalWeather(String stationName, LocalDateTime start, LocalDateTime end) {
        if (stationName == null || stationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time (" + start + ") cannot be after end time (" + end + ")");
        }
        return hourlyWeatherRepository.findByStationNameAndObservationTimeBetweenOrderByObservationTimeAsc(
                stationName.trim(), start, end
        );
    }

    /**
     * Retrieves the latest recorded observation for a given weather station.
     */
    public Optional<HourlyWeather> getLatestObservation(String stationName) {
        if (stationName == null || stationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        return hourlyWeatherRepository.findTopByStationNameOrderByObservationTimeDesc(stationName.trim());
    }

    /**
     * Lists all distinct weather monitoring stations available in the database.
     */
    public List<String> getAvailableStations() {
        return hourlyWeatherRepository.findDistinctStationNames();
    }

    /**
     * Spatial query: Resolves the closest weather station observation to a coordinate at a given timestamp.
     */
    public Optional<HourlyWeather> getObservationNearLocation(double longitude, double latitude,
                                                              double radiusMeters, LocalDateTime timestamp) {
        validateCoordinates(longitude, latitude);
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Search radius must be positive. Provided: " + radiusMeters);
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Observation timestamp cannot be null");
        }
        return hourlyWeatherRepository.findNearestStationObservation(longitude, latitude, radiusMeters, timestamp);
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
