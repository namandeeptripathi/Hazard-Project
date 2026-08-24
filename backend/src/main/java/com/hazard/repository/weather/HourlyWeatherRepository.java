package com.hazard.repository.weather;

import com.hazard.domain.weather.HourlyWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Repository for Hourly Meteorological Observations (weather.hourly_weather)
 */
@Repository
public interface HourlyWeatherRepository extends JpaRepository<HourlyWeather, Integer> {

    List<HourlyWeather> findByStationNameOrderByObservationTimeDesc(String stationName);

    List<HourlyWeather> findByStationNameAndObservationTimeBetweenOrderByObservationTimeAsc(String stationName,
                                                                                            LocalDateTime startTime,
                                                                                            LocalDateTime endTime);

    Optional<HourlyWeather> findTopByStationNameOrderByObservationTimeDesc(String stationName);

    @Query(value = "SELECT DISTINCT station_name FROM weather.hourly_weather ORDER BY station_name ASC", nativeQuery = true)
    List<String> findDistinctStationNames();

    /**
     * Proximity weather lookup: find the nearest weather station observation at a given timestamp.
     */
    @Query(value = "SELECT * FROM weather.hourly_weather w " +
                   "WHERE ST_DWithin(w.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography, :distanceMeters) " +
                   "  AND w.observation_time = :observationTime " +
                   "ORDER BY ST_Distance(w.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography) " +
                   "LIMIT 1", nativeQuery = true)
    Optional<HourlyWeather> findNearestStationObservation(@Param("longitude") double longitude,
                                                          @Param("latitude") double latitude,
                                                          @Param("distanceMeters") double distanceMeters,
                                                          @Param("observationTime") LocalDateTime observationTime);
}
