package com.hazard.service.hazard;

import com.hazard.domain.hazard.DfoFloodEvent;
import com.hazard.domain.hazard.EmdatFloodRecord;
import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.weather.HourlyWeather;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.HazardSummaryDto;
import com.hazard.dto.hazard.IntegratedHazardEvent;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.mapper.hazard.HazardDataMapper;
import com.hazard.repository.hazard.DfoFloodEventRepository;
import com.hazard.repository.hazard.EmdatFloodRecordRepository;
import com.hazard.repository.weather.HourlyWeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain Service responsible for integrating, querying, and serving
 * multi-source hazard data across the Hazard Intelligence subsystem.
 */
@Service
@Transactional(readOnly = true)
public class HazardIntegrationService {

    public static final double DEFAULT_EXTREME_RAINFALL_THRESHOLD_MM = 10.0;
    public static final int DEFAULT_LIMIT = 50;
    public static final int MAX_LIMIT = 1000;

    private final DfoFloodEventRepository dfoFloodEventRepository;
    private final EmdatFloodRecordRepository emdatFloodRecordRepository;
    private final HourlyWeatherRepository hourlyWeatherRepository;
    private final HazardDataMapper hazardDataMapper;

    public HazardIntegrationService(DfoFloodEventRepository dfoFloodEventRepository,
                                   EmdatFloodRecordRepository emdatFloodRecordRepository,
                                   HourlyWeatherRepository hourlyWeatherRepository,
                                   HazardDataMapper hazardDataMapper) {
        this.dfoFloodEventRepository = dfoFloodEventRepository;
        this.emdatFloodRecordRepository = emdatFloodRecordRepository;
        this.hourlyWeatherRepository = hourlyWeatherRepository;
        this.hazardDataMapper = hazardDataMapper;
    }

    /**
     * Retrieves a single integrated hazard event by its unified ID (e.g., "DFO-1", "EMDAT-5", "WEATHER-PATNA-100").
     */
    public IntegratedHazardEvent getHazardById(String unifiedId) {
        if (unifiedId == null || unifiedId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Hazard ID cannot be null or empty");
        }

        String idTrimmed = unifiedId.trim();

        if (idTrimmed.toUpperCase().startsWith("DFO-")) {
            int numericId = parseNumericId(idTrimmed.substring(4), idTrimmed);
            DfoFloodEvent dfo = dfoFloodEventRepository.findById(numericId)
                    .orElseThrow(() -> new HazardNotFoundException("DFO Flood Event not found with ID: " + idTrimmed));
            return hazardDataMapper.fromDfo(dfo);
        }

        if (idTrimmed.toUpperCase().startsWith("EMDAT-")) {
            int numericId = parseNumericId(idTrimmed.substring(6), idTrimmed);
            EmdatFloodRecord emdat = emdatFloodRecordRepository.findById(numericId)
                    .orElseThrow(() -> new HazardNotFoundException("EM-DAT Flood Record not found with ID: " + idTrimmed));
            return hazardDataMapper.fromEmdat(emdat);
        }

        if (idTrimmed.toUpperCase().startsWith("WEATHER-")) {
            // ID format: WEATHER-{STATION}-{ID}
            String[] parts = idTrimmed.split("-");
            if (parts.length >= 3) {
                int numericId = parseNumericId(parts[parts.length - 1], idTrimmed);
                HourlyWeather weather = hourlyWeatherRepository.findById(numericId)
                        .orElseThrow(() -> new HazardNotFoundException("Weather observation not found with ID: " + idTrimmed));
                return hazardDataMapper.fromWeather(weather);
            }
        }

        // Fallback: Try numeric ID directly against DFO, then EM-DAT
        try {
            int num = Integer.parseInt(idTrimmed);
            Optional<DfoFloodEvent> dfoOpt = dfoFloodEventRepository.findById(num);
            if (dfoOpt.isPresent()) {
                return hazardDataMapper.fromDfo(dfoOpt.get());
            }
            Optional<EmdatFloodRecord> emdatOpt = emdatFloodRecordRepository.findById(num);
            if (emdatOpt.isPresent()) {
                return hazardDataMapper.fromEmdat(emdatOpt.get());
            }
        } catch (NumberFormatException ignored) {
        }

        throw new HazardNotFoundException("Hazard event not found with ID: " + unifiedId);
    }

    /**
     * Retrieves integrated hazard events filtered by type and limit.
     */
    public List<IntegratedHazardEvent> getAllIntegratedHazards(HazardType type, Integer limit) {
        int safeLimit = sanitizeLimit(limit);

        if (type == null) {
            List<IntegratedHazardEvent> results = new ArrayList<>();
            // DFO events
            results.addAll(dfoFloodEventRepository.findAll().stream()
                    .map(hazardDataMapper::fromDfo)
                    .toList());
            // EM-DAT records
            results.addAll(emdatFloodRecordRepository.findAll().stream()
                    .map(hazardDataMapper::fromEmdat)
                    .toList());
            // Extreme rainfall events
            results.addAll(hourlyWeatherRepository.findExtremeRainfallEvents(DEFAULT_EXTREME_RAINFALL_THRESHOLD_MM, safeLimit).stream()
                    .map(hazardDataMapper::fromWeather)
                    .toList());

            return results.stream().limit(safeLimit).collect(Collectors.toList());
        }

        return switch (type) {
            case FLOOD -> {
                List<IntegratedHazardEvent> floods = new ArrayList<>();
                floods.addAll(dfoFloodEventRepository.findAll().stream().map(hazardDataMapper::fromDfo).toList());
                floods.addAll(emdatFloodRecordRepository.findAll().stream().map(hazardDataMapper::fromEmdat).toList());
                yield floods.stream().limit(safeLimit).collect(Collectors.toList());
            }
            case EXTREME_RAINFALL -> hourlyWeatherRepository.findExtremeRainfallEvents(DEFAULT_EXTREME_RAINFALL_THRESHOLD_MM, safeLimit)
                    .stream()
                    .map(hazardDataMapper::fromWeather)
                    .collect(Collectors.toList());
            case OTHER -> Collections.emptyList();
        };
    }

    /**
     * Retrieves hazards of a specific hazard type.
     */
    public List<IntegratedHazardEvent> getHazardsByType(HazardType type, Integer limit) {
        if (type == null) {
            throw new InvalidHazardParameterException("Hazard type cannot be null");
        }
        return getAllIntegratedHazards(type, limit);
    }

    /**
     * Retrieves hazard events occurring in or intersecting a specific Bihar administrative district.
     */
    public List<IntegratedHazardEvent> getHazardsInDistrict(String districtName, HazardType type, Integer limit) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new InvalidHazardParameterException("District name cannot be null or empty");
        }
        int safeLimit = sanitizeLimit(limit);
        String cleanDistrict = districtName.trim();

        List<IntegratedHazardEvent> results = new ArrayList<>();

        if (type == null || type == HazardType.FLOOD) {
            List<DfoFloodEvent> dfoEvents = dfoFloodEventRepository.findEventsInDistrict(cleanDistrict);
            results.addAll(dfoEvents.stream().map(hazardDataMapper::fromDfo).toList());
        }

        if (type == null || type == HazardType.EXTREME_RAINFALL) {
            List<HourlyWeather> weatherEvents = hourlyWeatherRepository.findExtremeRainfallInDistrict(
                    cleanDistrict, DEFAULT_EXTREME_RAINFALL_THRESHOLD_MM, safeLimit
            );
            results.addAll(weatherEvents.stream().map(hazardDataMapper::fromWeather).toList());
        }

        return results.stream().limit(safeLimit).collect(Collectors.toList());
    }

    /**
     * Spatial proximity query: retrieves hazard events within a radial distance (meters) of a point.
     */
    public List<IntegratedHazardEvent> getHazardsNearLocation(double longitude, double latitude, double radiusMeters,
                                                             HazardType type, Integer limit) {
        validateCoordinates(longitude, latitude);
        if (radiusMeters <= 0) {
            throw new InvalidHazardParameterException("Search radius must be positive. Provided: " + radiusMeters);
        }
        int safeLimit = sanitizeLimit(limit);

        List<IntegratedHazardEvent> results = new ArrayList<>();

        if (type == null || type == HazardType.FLOOD) {
            List<DfoFloodEvent> dfoEvents = dfoFloodEventRepository.findEventsNearPoint(longitude, latitude, radiusMeters);
            results.addAll(dfoEvents.stream().map(hazardDataMapper::fromDfo).toList());
        }

        if (type == null || type == HazardType.EXTREME_RAINFALL) {
            List<HourlyWeather> weatherEvents = hourlyWeatherRepository.findExtremeRainfallNearPoint(
                    longitude, latitude, radiusMeters, DEFAULT_EXTREME_RAINFALL_THRESHOLD_MM, safeLimit
            );
            results.addAll(weatherEvents.stream().map(hazardDataMapper::fromWeather).toList());
        }

        return results.stream().limit(safeLimit).collect(Collectors.toList());
    }

    /**
     * Spatial bounding box query: retrieves hazard events within [minLon, minLat, maxLon, maxLat].
     */
    public List<IntegratedHazardEvent> getHazardsInBoundingBox(double minLon, double minLat, double maxLon, double maxLat,
                                                              HazardType type, Integer limit) {
        validateCoordinates(minLon, minLat);
        validateCoordinates(maxLon, maxLat);
        if (minLon > maxLon) {
            throw new InvalidHazardParameterException("minLon (" + minLon + ") cannot be greater than maxLon (" + maxLon + ")");
        }
        if (minLat > maxLat) {
            throw new InvalidHazardParameterException("minLat (" + minLat + ") cannot be greater than maxLat (" + maxLat + ")");
        }
        int safeLimit = sanitizeLimit(limit);

        List<IntegratedHazardEvent> results = new ArrayList<>();

        if (type == null || type == HazardType.FLOOD) {
            List<DfoFloodEvent> dfoEvents = dfoFloodEventRepository.findEventsInBoundingBox(minLon, minLat, maxLon, maxLat);
            results.addAll(dfoEvents.stream().map(hazardDataMapper::fromDfo).toList());
        }

        if (type == null || type == HazardType.EXTREME_RAINFALL) {
            List<HourlyWeather> weatherEvents = hourlyWeatherRepository.findExtremeRainfallInBoundingBox(
                    minLon, minLat, maxLon, maxLat, DEFAULT_EXTREME_RAINFALL_THRESHOLD_MM, safeLimit
            );
            results.addAll(weatherEvents.stream().map(hazardDataMapper::fromWeather).toList());
        }

        return results.stream().limit(safeLimit).collect(Collectors.toList());
    }

    /**
     * Retrieves hazard events that occurred within a specific date range.
     */
    public List<IntegratedHazardEvent> getHazardsInTimeRange(LocalDate startDate, LocalDate endDate, HazardType type, Integer limit) {
        if (startDate == null || endDate == null) {
            throw new InvalidHazardParameterException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidHazardParameterException("Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }
        int safeLimit = sanitizeLimit(limit);

        List<IntegratedHazardEvent> results = new ArrayList<>();

        if (type == null || type == HazardType.FLOOD) {
            List<DfoFloodEvent> dfoEvents = dfoFloodEventRepository.findByBeganDateBetweenOrderByBeganDateDesc(startDate, endDate);
            results.addAll(dfoEvents.stream().map(hazardDataMapper::fromDfo).toList());

            List<EmdatFloodRecord> emdatRecords = emdatFloodRecordRepository.findByYearBetweenOrderByYearDesc(
                    startDate.getYear(), endDate.getYear()
            );
            results.addAll(emdatRecords.stream().map(hazardDataMapper::fromEmdat).toList());
        }

        if (type == null || type == HazardType.EXTREME_RAINFALL) {
            LocalDateTime startDt = startDate.atStartOfDay();
            LocalDateTime endDt = endDate.atTime(23, 59, 59);
            List<HourlyWeather> weatherEvents = hourlyWeatherRepository.findExtremeRainfallInTimeRange(
                    DEFAULT_EXTREME_RAINFALL_THRESHOLD_MM, startDt, endDt, safeLimit
            );
            results.addAll(weatherEvents.stream().map(hazardDataMapper::fromWeather).toList());
        }

        return results.stream().limit(safeLimit).collect(Collectors.toList());
    }

    /**
     * Retrieves high-intensity extreme rainfall observations.
     */
    public List<IntegratedHazardEvent> getExtremeRainfallHazards(Double thresholdMm, LocalDateTime start, LocalDateTime end, Integer limit) {
        double threshold = (thresholdMm != null && thresholdMm > 0) ? thresholdMm : DEFAULT_EXTREME_RAINFALL_THRESHOLD_MM;
        int safeLimit = sanitizeLimit(limit);

        List<HourlyWeather> observations;
        if (start != null && end != null) {
            if (start.isAfter(end)) {
                throw new InvalidHazardParameterException("Start time (" + start + ") cannot be after end time (" + end + ")");
            }
            observations = hourlyWeatherRepository.findExtremeRainfallInTimeRange(threshold, start, end, safeLimit);
        } else {
            observations = hourlyWeatherRepository.findExtremeRainfallEvents(threshold, safeLimit);
        }

        return observations.stream()
                .map(hazardDataMapper::fromWeather)
                .collect(Collectors.toList());
    }

    /**
     * Compiles an executive catalog summary and metadata report of all integrated hazard data.
     */
    public HazardSummaryDto getHazardSummary() {
        HazardSummaryDto summary = new HazardSummaryDto();
        summary.setDescription("Stage 3.1 Integrated Hazard Intelligence Dataset Catalog");
        summary.setCanonicalCrs("EPSG:4326 (WGS 84)");
        summary.setCoverageRegion("Bihar, India (38 Districts, 53 Sub-districts)");
        summary.setTemporalRange("1968 to 2024");

        long dfoCount = dfoFloodEventRepository.count();
        long emdatCount = emdatFloodRecordRepository.count();
        long weatherCount = hourlyWeatherRepository.count();

        summary.setDfoFloodEventsCount(dfoCount);
        summary.setEmdatFloodRecordsCount(emdatCount);
        summary.setWeatherObservationsCount(weatherCount);
        summary.setTotalIntegratedRecords(dfoCount + emdatCount + weatherCount);

        summary.setActiveHazardTypes(List.of(HazardType.FLOOD, HazardType.EXTREME_RAINFALL, HazardType.OTHER));
        summary.setAvailableWeatherStations(hourlyWeatherRepository.findDistinctStationNames());
        summary.setSupportedQueryTypes(List.of(
                "By ID (e.g. DFO-1, EMDAT-2, WEATHER-PATNA-3)",
                "By Hazard Type (FLOOD, EXTREME_RAINFALL)",
                "By District Intersection (ST_Intersects)",
                "By Coordinate Proximity (ST_DWithin)",
                "By Bounding Box (ST_MakeEnvelope)",
                "By Temporal Date Window",
                "By Precipitation Threshold (mm/hr)",
                "Standard GeoJSON Vector Layer (RFC 7946)"
        ));

        return summary;
    }

    /**
     * Converts integrated hazards into a standard GeoJSON FeatureCollection ready for map visualization.
     */
    public GeoJsonFeatureCollectionDto getHazardsGeoJson(HazardType type, String districtName, Integer limit) {
        List<IntegratedHazardEvent> events;
        if (districtName != null && !districtName.trim().isEmpty()) {
            events = getHazardsInDistrict(districtName.trim(), type, limit);
        } else {
            events = getAllIntegratedHazards(type, limit);
        }
        return hazardDataMapper.toGeoJsonFeatureCollection(events);
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private void validateCoordinates(double longitude, double latitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new InvalidHazardParameterException("Latitude must be between -90.0 and 90.0 degrees. Provided: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new InvalidHazardParameterException("Longitude must be between -180.0 and 180.0 degrees. Provided: " + longitude);
        }
    }

    private int parseNumericId(String numStr, String fullId) {
        try {
            return Integer.parseInt(numStr);
        } catch (NumberFormatException ex) {
            throw new InvalidHazardParameterException("Invalid numeric identifier format in ID '" + fullId + "'");
        }
    }
}
