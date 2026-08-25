package com.hazard.service.processing;

import com.hazard.domain.hazard.QualityStatus;
import com.hazard.domain.weather.HourlyWeather;
import com.hazard.dto.processing.DailyRainfallSummary;
import com.hazard.dto.processing.RollingRainfallMetrics;
import com.hazard.repository.weather.HourlyWeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service calculating temporal aggregations, daily rainfall totals,
 * peak hourly intensities, and multi-window rolling rainfall metrics.
 */
@Service
@Transactional(readOnly = true)
public class TemporalRainfallAggregator {

    public static final double IMD_HEAVY_RAINFALL_HOURLY_THRESHOLD_MM = 15.0;
    public static final double IMD_VERY_HEAVY_RAINFALL_HOURLY_THRESHOLD_MM = 35.0;
    public static final double IMD_HEAVY_RAINFALL_DAILY_THRESHOLD_MM = 64.5;
    public static final double IMD_VERY_HEAVY_RAINFALL_DAILY_THRESHOLD_MM = 115.5;

    private final HourlyWeatherRepository hourlyWeatherRepository;
    private final SpatialAssociationService spatialAssociationService;

    public TemporalRainfallAggregator(HourlyWeatherRepository hourlyWeatherRepository,
                                     SpatialAssociationService spatialAssociationService) {
        this.hourlyWeatherRepository = hourlyWeatherRepository;
        this.spatialAssociationService = spatialAssociationService;
    }

    /**
     * Aggregates hourly meteorological data into daily summaries for a given weather station.
     */
    public List<DailyRainfallSummary> getDailySummaries(String stationName, LocalDate startDate, LocalDate endDate) {
        if (stationName == null || stationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(23, 59, 59);

        List<Object[]> rawStats = hourlyWeatherRepository.findDailyRainfallStats(
                stationName.trim(), startDt, endDt
        );

        if (rawStats == null || rawStats.isEmpty()) {
            return Collections.emptyList();
        }

        List<DailyRainfallSummary> summaries = new ArrayList<>();
        for (Object[] row : rawStats) {
            DailyRainfallSummary summary = new DailyRainfallSummary();
            summary.setStationName((String) row[0]);

            if (row[1] instanceof Date sqlDate) {
                summary.setDate(sqlDate.toLocalDate());
            } else if (row[1] instanceof LocalDate ld) {
                summary.setDate(ld);
            }

            double dailyTotal = ((Number) row[2]).doubleValue();
            double peakHourly = ((Number) row[3]).doubleValue();
            int rainyHours = ((Number) row[4]).intValue();
            int heavyHours = ((Number) row[5]).intValue();
            int veryHeavyHours = ((Number) row[6]).intValue();

            summary.setDailyTotalMm(Math.round(dailyTotal * 100.0) / 100.0);
            summary.setPeakHourlyMm(Math.round(peakHourly * 100.0) / 100.0);
            summary.setRainyHours(rainyHours);
            summary.setHeavyRainHours(heavyHours);
            summary.setVeryHeavyRainHours(veryHeavyHours);

            if (row[7] != null && row[8] != null) {
                summary.setLongitude(((Number) row[7]).doubleValue());
                summary.setLatitude(((Number) row[8]).doubleValue());
                spatialAssociationService.resolveDistrictName(summary.getLongitude(), summary.getLatitude())
                        .ifPresent(summary::setAssociatedDistrict);
            } else {
                summary.setAssociatedDistrict(summary.getStationName());
            }

            boolean exceeds = dailyTotal >= IMD_HEAVY_RAINFALL_DAILY_THRESHOLD_MM || peakHourly >= IMD_HEAVY_RAINFALL_HOURLY_THRESHOLD_MM;
            summary.setExceedsHeavyThreshold(exceeds);
            summary.setQualityStatus(QualityStatus.VALID);

            summaries.add(summary);
        }

        return summaries;
    }

    /**
     * Computes rolling rainfall metrics across multiple temporal windows (3h, 6h, 12h, 24h).
     */
    public RollingRainfallMetrics getRollingMetrics(String stationName, LocalDateTime targetTime) {
        if (stationName == null || stationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        if (targetTime == null) {
            throw new IllegalArgumentException("Target observation time cannot be null");
        }

        String cleanStation = stationName.trim();
        RollingRainfallMetrics metrics = new RollingRainfallMetrics();
        metrics.setStationName(cleanStation);
        metrics.setTimestamp(targetTime);

        // 1. Current hourly observation
        Optional<HourlyWeather> latestOpt = hourlyWeatherRepository.findByStationNameAndObservationTimeBetweenOrderByObservationTimeAsc(
                cleanStation, targetTime, targetTime
        ).stream().findFirst();

        double currentMm = latestOpt.map(HourlyWeather::getPrecipitationMm).orElse(0.0);
        metrics.setCurrentHourlyMm(currentMm);

        latestOpt.ifPresent(w -> {
            if (w.getLongitude() != null && w.getLatitude() != null) {
                spatialAssociationService.resolveDistrictName(w.getLongitude(), w.getLatitude())
                        .ifPresent(metrics::setAssociatedDistrict);
            }
        });

        // 2. Rolling sums
        Double r3 = hourlyWeatherRepository.calculateRollingPrecipitationBetween(cleanStation, targetTime.minusHours(3), targetTime);
        Double r6 = hourlyWeatherRepository.calculateRollingPrecipitationBetween(cleanStation, targetTime.minusHours(6), targetTime);
        Double r12 = hourlyWeatherRepository.calculateRollingPrecipitationBetween(cleanStation, targetTime.minusHours(12), targetTime);
        Double r24 = hourlyWeatherRepository.calculateRollingPrecipitationBetween(cleanStation, targetTime.minusHours(24), targetTime);

        metrics.setRolling3hMm(r3 != null ? Math.round(r3 * 100.0) / 100.0 : 0.0);
        metrics.setRolling6hMm(r6 != null ? Math.round(r6 * 100.0) / 100.0 : 0.0);
        metrics.setRolling12hMm(r12 != null ? Math.round(r12 * 100.0) / 100.0 : 0.0);
        metrics.setRolling24hMm(r24 != null ? Math.round(r24 * 100.0) / 100.0 : 0.0);

        metrics.setHeavyRainfall(currentMm >= IMD_HEAVY_RAINFALL_HOURLY_THRESHOLD_MM ||
                metrics.getRolling24hMm() >= IMD_HEAVY_RAINFALL_DAILY_THRESHOLD_MM);
        metrics.setVeryHeavyRainfall(currentMm >= IMD_VERY_HEAVY_RAINFALL_HOURLY_THRESHOLD_MM ||
                metrics.getRolling24hMm() >= IMD_VERY_HEAVY_RAINFALL_DAILY_THRESHOLD_MM);

        return metrics;
    }
}
