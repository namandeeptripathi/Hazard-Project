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

    /**
     * Extreme rainfall lookup: find observation records exceeding a precipitation threshold (mm).
     */
    @Query(value = "SELECT * FROM weather.hourly_weather w " +
                   "WHERE w.precipitation_mm >= :thresholdMm " +
                   "ORDER BY w.precipitation_mm DESC, w.observation_time DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<HourlyWeather> findExtremeRainfallEvents(@Param("thresholdMm") double thresholdMm,
                                                  @Param("limit") int limit);

    /**
     * Extreme rainfall lookup within a specific observation time window.
     */
    @Query(value = "SELECT * FROM weather.hourly_weather w " +
                   "WHERE w.precipitation_mm >= :thresholdMm " +
                   "  AND w.observation_time >= :startTime " +
                   "  AND w.observation_time <= :endTime " +
                   "ORDER BY w.precipitation_mm DESC, w.observation_time DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<HourlyWeather> findExtremeRainfallInTimeRange(@Param("thresholdMm") double thresholdMm,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("limit") int limit);

    /**
     * Spatial query: find extreme rainfall events recorded at stations within a specific district.
     */
    @Query(value = "SELECT w.* FROM weather.hourly_weather w " +
                   "JOIN boundaries.district_boundaries d ON ST_Intersects(w.geom, d.geom) " +
                   "WHERE UPPER(d.name_2) = UPPER(:districtName) " +
                   "  AND w.precipitation_mm >= :thresholdMm " +
                   "ORDER BY w.precipitation_mm DESC, w.observation_time DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<HourlyWeather> findExtremeRainfallInDistrict(@Param("districtName") String districtName,
                                                      @Param("thresholdMm") double thresholdMm,
                                                      @Param("limit") int limit);

    /**
     * Spatial proximity query: find extreme rainfall events recorded within radius of a point.
     */
    @Query(value = "SELECT * FROM weather.hourly_weather w " +
                   "WHERE ST_DWithin(w.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography, :distanceMeters) " +
                   "  AND w.precipitation_mm >= :thresholdMm " +
                   "ORDER BY w.precipitation_mm DESC, ST_Distance(w.geom::geography, ST_SetSRID(ST_Point(:longitude, :latitude), 4326)::geography) " +
                   "LIMIT :limit", nativeQuery = true)
    List<HourlyWeather> findExtremeRainfallNearPoint(@Param("longitude") double longitude,
                                                     @Param("latitude") double latitude,
                                                     @Param("distanceMeters") double distanceMeters,
                                                     @Param("thresholdMm") double thresholdMm,
                                                     @Param("limit") int limit);

    /**
     * Spatial bounding box query: find extreme rainfall events within a geographic bounding box [minLon, minLat, maxLon, maxLat].
     */
    @Query(value = "SELECT * FROM weather.hourly_weather w " +
                   "WHERE ST_Intersects(w.geom, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)) " +
                   "  AND w.precipitation_mm >= :thresholdMm " +
                   "ORDER BY w.precipitation_mm DESC, w.observation_time DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<HourlyWeather> findExtremeRainfallInBoundingBox(@Param("minLon") double minLon,
                                                         @Param("minLat") double minLat,
                                                         @Param("maxLon") double maxLon,
                                                         @Param("maxLat") double maxLat,
                                                         @Param("thresholdMm") double thresholdMm,
                                                         @Param("limit") int limit);

    /**
     * Daily rainfall statistics aggregation: aggregates hourly observations into daily sums and peak intensities.
     */
    @Query(value = "SELECT w.station_name, w.observation_time::date AS obs_date, " +
                   "       COALESCE(SUM(w.precipitation_mm), 0.0) AS daily_total_mm, " +
                   "       COALESCE(MAX(w.precipitation_mm), 0.0) AS peak_hourly_mm, " +
                   "       COUNT(*) FILTER (WHERE w.precipitation_mm > 0.0) AS rainy_hours, " +
                   "       COUNT(*) FILTER (WHERE w.precipitation_mm >= 15.0) AS heavy_rain_hours, " +
                   "       COUNT(*) FILTER (WHERE w.precipitation_mm >= 35.0) AS very_heavy_rain_hours, " +
                   "       AVG(w.longitude) AS lon, AVG(w.latitude) AS lat " +
                   "FROM weather.hourly_weather w " +
                   "WHERE UPPER(w.station_name) = UPPER(:stationName) " +
                   "  AND w.observation_time >= :startTime " +
                   "  AND w.observation_time <= :endTime " +
                   "GROUP BY w.station_name, w.observation_time::date " +
                   "ORDER BY obs_date ASC", nativeQuery = true)
    List<Object[]> findDailyRainfallStats(@Param("stationName") String stationName,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * Rolling precipitation accumulation: computes the cumulative rainfall sum over a time window.
     */
    @Query(value = "SELECT COALESCE(SUM(w.precipitation_mm), 0.0) " +
                   "FROM weather.hourly_weather w " +
                   "WHERE UPPER(w.station_name) = UPPER(:stationName) " +
                   "  AND w.observation_time <= :endTime " +
                   "  AND w.observation_time > :startTime", nativeQuery = true)
    Double calculateRollingPrecipitationBetween(@Param("stationName") String stationName,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
}
