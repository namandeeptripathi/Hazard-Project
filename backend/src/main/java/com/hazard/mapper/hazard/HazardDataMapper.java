package com.hazard.mapper.hazard;

import com.hazard.domain.hazard.DfoFloodEvent;
import com.hazard.domain.hazard.EmdatFloodRecord;
import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.weather.HourlyWeather;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.GeoJsonGeometryDto;
import com.hazard.dto.hazard.IntegratedHazardEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data Mapper responsible for transforming heterogeneous persistence entities
 * (DFO flood events, EM-DAT macro disaster impact records, and Open-Meteo weather observations)
 * into unified, application-level Hazard Intelligence domain DTOs and standard GeoJSON structures.
 */
@Component
public class HazardDataMapper {

    /**
     * Maps a Dartmouth Flood Observatory (DFO) entity into a unified IntegratedHazardEvent.
     */
    public IntegratedHazardEvent fromDfo(DfoFloodEvent dfo) {
        if (dfo == null) {
            return null;
        }
        IntegratedHazardEvent event = new IntegratedHazardEvent();
        event.setId("DFO-" + dfo.getId());
        event.setSourceRecordId(dfo.getId());
        event.setHazardType(HazardType.FLOOD);
        event.setDataSource("DFO");
        event.setLocationName(dfo.getDetailedLocation());
        event.setCountry(dfo.getCountry() != null ? dfo.getCountry() : "India");

        if (dfo.getGeom() != null) {
            event.setLongitude(dfo.getGeom().getX());
            event.setLatitude(dfo.getGeom().getY());
        } else {
            event.setLongitude(dfo.getCentroidX());
            event.setLatitude(dfo.getCentroidY());
        }

        event.setStartDate(dfo.getBeganDate());
        event.setEndDate(dfo.getEndedDate());
        event.setSeverity(dfo.getSeverity());
        event.setMagnitude(dfo.getMagnitude());
        event.setDurationDays(dfo.getDurationDays());
        event.setDisplacedPopulation(dfo.getDisplaced());
        event.setFatalities(dfo.getDeaths());
        event.setAffectedAreaSqKm(dfo.getAffectedSqkm());
        event.setEconomicDamageUsd(dfo.getDamageUsd());
        event.setExternalReference(dfo.getGlideNo());

        Map<String, Object> meta = new LinkedHashMap<>();
        if (dfo.getRegisterNo() != null) meta.put("registerNo", dfo.getRegisterNo());
        if (dfo.getAnnualDfo() != null) meta.put("annualDfo", dfo.getAnnualDfo());
        if (dfo.getGlideNo() != null) meta.put("glideNo", dfo.getGlideNo());
        if (dfo.getRivers() != null) meta.put("rivers", dfo.getRivers());
        if (dfo.getMainCause() != null) meta.put("mainCause", dfo.getMainCause());
        if (dfo.getMatchedBy() != null) meta.put("matchedBy", dfo.getMatchedBy());
        event.setMetadata(meta);

        return event;
    }

    /**
     * Maps an EM-DAT macro impact flood record into a unified IntegratedHazardEvent.
     */
    public IntegratedHazardEvent fromEmdat(EmdatFloodRecord emdat) {
        if (emdat == null) {
            return null;
        }
        IntegratedHazardEvent event = new IntegratedHazardEvent();
        event.setId("EMDAT-" + emdat.getId());
        event.setSourceRecordId(emdat.getId());
        event.setHazardType(HazardType.FLOOD);
        event.setDataSource("EM_DAT");

        String loc = emdat.getCountry();
        if (emdat.getSpatialGranularity() != null && !emdat.getSpatialGranularity().isEmpty()) {
            loc += " (" + emdat.getSpatialGranularity() + ")";
        }
        event.setLocationName(loc);
        event.setCountry(emdat.getCountry());

        if (emdat.getYear() != null) {
            event.setStartDate(LocalDate.of(emdat.getYear(), 1, 1));
            event.setEndDate(LocalDate.of(emdat.getYear(), 12, 31));
        }

        if (emdat.getTotalDeaths() != null) {
            event.setFatalities(emdat.getTotalDeaths().doubleValue());
        }
        if (emdat.getTotalAffected() != null) {
            event.setDisplacedPopulation(emdat.getTotalAffected().doubleValue());
        }
        if (emdat.getTotalDamageUsdAdjusted() != null) {
            event.setEconomicDamageUsd(emdat.getTotalDamageUsdAdjusted());
        } else if (emdat.getTotalDamageUsdOriginal() != null) {
            event.setEconomicDamageUsd(emdat.getTotalDamageUsdOriginal());
        }
        event.setExternalReference(emdat.getIso());

        Map<String, Object> meta = new LinkedHashMap<>();
        if (emdat.getYear() != null) meta.put("year", emdat.getYear());
        if (emdat.getDisasterGroup() != null) meta.put("disasterGroup", emdat.getDisasterGroup());
        if (emdat.getDisasterSubgroup() != null) meta.put("disasterSubgroup", emdat.getDisasterSubgroup());
        if (emdat.getDisasterType() != null) meta.put("disasterType", emdat.getDisasterType());
        if (emdat.getDisasterSubtype() != null) meta.put("disasterSubtype", emdat.getDisasterSubtype());
        if (emdat.getTotalEvents() != null) meta.put("totalEvents", emdat.getTotalEvents());
        if (emdat.getCpi() != null) meta.put("cpi", emdat.getCpi());
        if (emdat.getSpatialGranularity() != null) meta.put("spatialGranularity", emdat.getSpatialGranularity());
        if (emdat.getNotes() != null) meta.put("notes", emdat.getNotes());
        event.setMetadata(meta);

        return event;
    }

    /**
     * Maps an Open-Meteo extreme precipitation observation into a unified IntegratedHazardEvent.
     */
    public IntegratedHazardEvent fromWeather(HourlyWeather weather) {
        if (weather == null) {
            return null;
        }
        IntegratedHazardEvent event = new IntegratedHazardEvent();
        String station = weather.getStationName() != null ? weather.getStationName() : "UNKNOWN";
        event.setId("WEATHER-" + station.toUpperCase() + "-" + weather.getId());
        event.setSourceRecordId(weather.getId());
        event.setHazardType(HazardType.EXTREME_RAINFALL);
        event.setDataSource("OPEN_METEO");
        event.setLocationName(station + " Weather Station");
        event.setCountry("India");

        if (weather.getGeom() != null) {
            event.setLongitude(weather.getGeom().getX());
            event.setLatitude(weather.getGeom().getY());
        } else {
            event.setLongitude(weather.getLongitude());
            event.setLatitude(weather.getLatitude());
        }

        event.setTimestamp(weather.getObservationTime());
        if (weather.getObservationTime() != null) {
            event.setStartDate(weather.getObservationTime().toLocalDate());
            event.setEndDate(weather.getObservationTime().toLocalDate());
        }

        event.setPrecipitationMm(weather.getPrecipitationMm());

        // Derived relative severity index: >35 mm/hr = 3.0 (Very Heavy), >15 mm/hr = 2.0 (Heavy), >7.5 mm/hr = 1.0 (Moderate)
        if (weather.getPrecipitationMm() != null) {
            double prec = weather.getPrecipitationMm();
            if (prec >= 35.0) {
                event.setSeverity(3.0);
            } else if (prec >= 15.0) {
                event.setSeverity(2.0);
            } else if (prec >= 7.5) {
                event.setSeverity(1.0);
            } else {
                event.setSeverity(0.5);
            }
        }

        event.setExternalReference("Station: " + station);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("stationName", station);
        if (weather.getRainMm() != null) meta.put("rainMm", weather.getRainMm());
        if (weather.getSnowfallCm() != null) meta.put("snowfallCm", weather.getSnowfallCm());
        if (weather.getCloudCoverPct() != null) meta.put("cloudCoverPct", weather.getCloudCoverPct());
        if (weather.getSurfacePressureHpa() != null) meta.put("surfacePressureHpa", weather.getSurfacePressureHpa());
        event.setMetadata(meta);

        return event;
    }

    /**
     * Converts an IntegratedHazardEvent into a standard GeoJSON Feature (RFC 7946).
     * Returns null if the event has no spatial coordinates.
     */
    public GeoJsonFeatureDto toGeoJsonFeature(IntegratedHazardEvent event) {
        if (event == null || event.getLongitude() == null || event.getLatitude() == null) {
            return null;
        }

        GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(event.getLongitude(), event.getLatitude());

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("id", event.getId());
        props.put("hazardType", event.getHazardType() != null ? event.getHazardType().name() : null);
        props.put("dataSource", event.getDataSource());
        props.put("locationName", event.getLocationName());
        props.put("country", event.getCountry());
        props.put("startDate", event.getStartDate() != null ? event.getStartDate().toString() : null);
        props.put("endDate", event.getEndDate() != null ? event.getEndDate().toString() : null);
        props.put("timestamp", event.getTimestamp() != null ? event.getTimestamp().toString() : null);
        props.put("severity", event.getSeverity());
        props.put("magnitude", event.getMagnitude());
        props.put("displacedPopulation", event.getDisplacedPopulation());
        props.put("fatalities", event.getFatalities());
        props.put("affectedAreaSqKm", event.getAffectedAreaSqKm());
        props.put("economicDamageUsd", event.getEconomicDamageUsd());
        props.put("precipitationMm", event.getPrecipitationMm());
        props.put("externalReference", event.getExternalReference());

        if (event.getMetadata() != null && !event.getMetadata().isEmpty()) {
            props.put("metadata", event.getMetadata());
        }

        return new GeoJsonFeatureDto(event.getId(), geom, props);
    }

    /**
     * Converts a collection of IntegratedHazardEvents into a standard GeoJSON FeatureCollection.
     */
    public GeoJsonFeatureCollectionDto toGeoJsonFeatureCollection(List<IntegratedHazardEvent> events) {
        if (events == null || events.isEmpty()) {
            return new GeoJsonFeatureCollectionDto(Collections.emptyList());
        }
        List<GeoJsonFeatureDto> features = events.stream()
                .map(this::toGeoJsonFeature)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new GeoJsonFeatureCollectionDto(features);
    }
}
