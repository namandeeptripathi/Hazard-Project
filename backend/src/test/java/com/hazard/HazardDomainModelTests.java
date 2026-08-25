package com.hazard;

import com.hazard.domain.hazard.DfoFloodEvent;
import com.hazard.domain.hazard.EmdatFloodRecord;
import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.weather.HourlyWeather;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.HazardSummaryDto;
import com.hazard.dto.hazard.IntegratedHazardEvent;
import com.hazard.mapper.hazard.HazardDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HazardDomainModelTests {

    private HazardDataMapper mapper;
    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        mapper = new HazardDataMapper();
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    @Test
    @DisplayName("1. HazardType: Parse Supported Types & Throw on Unsupported")
    void testHazardTypeParsing() {
        assertEquals(HazardType.FLOOD, HazardType.fromString("FLOOD"));
        assertEquals(HazardType.FLOOD, HazardType.fromString("flood"));
        assertEquals(HazardType.FLOOD, HazardType.fromString("Flood"));
        assertEquals(HazardType.EXTREME_RAINFALL, HazardType.fromString("EXTREME_RAINFALL"));
        assertEquals(HazardType.EXTREME_RAINFALL, HazardType.fromString("extreme-rainfall"));
        assertEquals(HazardType.EXTREME_RAINFALL, HazardType.fromString("Extreme Rainfall"));
        assertEquals(HazardType.OTHER, HazardType.fromString("OTHER"));

        assertThrows(IllegalArgumentException.class, () -> HazardType.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> HazardType.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> HazardType.fromString("VOLCANO"));
    }

    @Test
    @DisplayName("2. HazardDataMapper: Map DfoFloodEvent to IntegratedHazardEvent")
    void testMapDfoFloodEvent() {
        DfoFloodEvent dfo = new DfoFloodEvent();
        dfo.setId(42);
        dfo.setBeganDate(LocalDate.of(2008, 8, 18));
        dfo.setEndedDate(LocalDate.of(2008, 9, 24));
        dfo.setCountry("India");
        dfo.setDetailedLocation("Bihar, Kosi basin");
        dfo.setRivers("Kosi, Ganges");
        dfo.setSeverity(2.0);
        dfo.setMagnitude(8.2);
        dfo.setDurationDays(37.0);
        dfo.setDisplaced(1000000.0);
        dfo.setDeaths(500.0);
        dfo.setAffectedSqkm(150000.0);
        dfo.setDamageUsd(45000000.0);
        dfo.setGlideNo("FL-2008-000139-IND");
        Point pt = geometryFactory.createPoint(new Coordinate(86.5, 26.2));
        dfo.setGeom(pt);

        IntegratedHazardEvent event = mapper.fromDfo(dfo);

        assertNotNull(event);
        assertEquals("DFO-42", event.getId());
        assertEquals(42, event.getSourceRecordId());
        assertEquals(HazardType.FLOOD, event.getHazardType());
        assertEquals("DFO", event.getDataSource());
        assertEquals("Bihar, Kosi basin", event.getLocationName());
        assertEquals(86.5, event.getLongitude());
        assertEquals(26.2, event.getLatitude());
        assertEquals(LocalDate.of(2008, 8, 18), event.getStartDate());
        assertEquals(LocalDate.of(2008, 9, 24), event.getEndDate());
        assertEquals(2.0, event.getSeverity());
        assertEquals(8.2, event.getMagnitude());
        assertEquals(1000000.0, event.getDisplacedPopulation());
        assertEquals(500.0, event.getFatalities());
        assertEquals("FL-2008-000139-IND", event.getExternalReference());
        assertEquals("Kosi, Ganges", event.getMetadata().get("rivers"));
    }

    @Test
    @DisplayName("3. HazardDataMapper: Map EmdatFloodRecord to IntegratedHazardEvent")
    void testMapEmdatFloodRecord() {
        EmdatFloodRecord emdat = new EmdatFloodRecord();
        emdat.setId(10);
        emdat.setYear(2007);
        emdat.setCountry("India");
        emdat.setIso("IND");
        emdat.setDisasterType("Flood");
        emdat.setDisasterSubtype("Riverine flood");
        emdat.setTotalEvents(3);
        emdat.setTotalDeaths(1200);
        emdat.setTotalAffected(15000000L);
        emdat.setTotalDamageUsdAdjusted(850000000.0);
        emdat.setSpatialGranularity("National");

        IntegratedHazardEvent event = mapper.fromEmdat(emdat);

        assertNotNull(event);
        assertEquals("EMDAT-10", event.getId());
        assertEquals(10, event.getSourceRecordId());
        assertEquals(HazardType.FLOOD, event.getHazardType());
        assertEquals("EM_DAT", event.getDataSource());
        assertEquals("India (National)", event.getLocationName());
        assertNull(event.getLongitude());
        assertNull(event.getLatitude());
        assertEquals(LocalDate.of(2007, 1, 1), event.getStartDate());
        assertEquals(LocalDate.of(2007, 12, 31), event.getEndDate());
        assertEquals(1200.0, event.getFatalities());
        assertEquals(15000000.0, event.getDisplacedPopulation());
        assertEquals(850000000.0, event.getEconomicDamageUsd());
        assertEquals("IND", event.getExternalReference());
        assertEquals(3, event.getMetadata().get("totalEvents"));
    }

    @Test
    @DisplayName("4. HazardDataMapper: Map HourlyWeather to IntegratedHazardEvent")
    void testMapHourlyWeather() {
        HourlyWeather weather = new HourlyWeather();
        weather.setId(999);
        weather.setStationName("Patna");
        weather.setObservationTime(LocalDateTime.of(2024, 7, 15, 14, 0));
        weather.setPrecipitationMm(28.5);
        weather.setRainMm(28.5);
        weather.setSurfacePressureHpa(998.2);
        weather.setLongitude(85.1376);
        weather.setLatitude(25.5941);
        Point pt = geometryFactory.createPoint(new Coordinate(85.1376, 25.5941));
        weather.setGeom(pt);

        IntegratedHazardEvent event = mapper.fromWeather(weather);

        assertNotNull(event);
        assertEquals("WEATHER-PATNA-999", event.getId());
        assertEquals(999, event.getSourceRecordId());
        assertEquals(HazardType.EXTREME_RAINFALL, event.getHazardType());
        assertEquals("OPEN_METEO", event.getDataSource());
        assertEquals("Patna Weather Station", event.getLocationName());
        assertEquals(85.1376, event.getLongitude());
        assertEquals(25.5941, event.getLatitude());
        assertEquals(28.5, event.getPrecipitationMm());
        assertEquals(2.0, event.getSeverity()); // > 15mm is Heavy (2.0)
        assertEquals(LocalDate.of(2024, 7, 15), event.getStartDate());
        assertEquals(LocalDateTime.of(2024, 7, 15, 14, 0), event.getTimestamp());
    }

    @Test
    @DisplayName("5. HazardDataMapper: Convert to GeoJSON Feature & FeatureCollection")
    void testGeoJsonConversion() {
        IntegratedHazardEvent spatialEvent = new IntegratedHazardEvent();
        spatialEvent.setId("DFO-1");
        spatialEvent.setHazardType(HazardType.FLOOD);
        spatialEvent.setDataSource("DFO");
        spatialEvent.setLocationName("Patna");
        spatialEvent.setLongitude(85.1376);
        spatialEvent.setLatitude(25.5941);
        spatialEvent.setSeverity(1.5);

        IntegratedHazardEvent nonSpatialEvent = new IntegratedHazardEvent();
        nonSpatialEvent.setId("EMDAT-1");
        nonSpatialEvent.setHazardType(HazardType.FLOOD);
        nonSpatialEvent.setDataSource("EM_DAT");
        nonSpatialEvent.setLongitude(null);
        nonSpatialEvent.setLatitude(null);

        GeoJsonFeatureDto feature = mapper.toGeoJsonFeature(spatialEvent);
        assertNotNull(feature);
        assertEquals("Feature", feature.getType());
        assertEquals("DFO-1", feature.getId());
        assertEquals("Point", feature.getGeometry().getType());
        assertArrayEquals(new double[]{85.1376, 25.5941}, (double[]) feature.getGeometry().getCoordinates());

        assertNull(mapper.toGeoJsonFeature(nonSpatialEvent));

        GeoJsonFeatureCollectionDto fc = mapper.toGeoJsonFeatureCollection(List.of(spatialEvent, nonSpatialEvent));
        assertNotNull(fc);
        assertEquals("FeatureCollection", fc.getType());
        assertEquals(1, fc.getCount());
        assertEquals(1, fc.getFeatures().size());
        assertEquals("DFO-1", fc.getFeatures().get(0).getId());
    }
}
