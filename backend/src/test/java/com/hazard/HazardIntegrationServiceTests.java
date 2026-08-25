package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.HazardSummaryDto;
import com.hazard.dto.hazard.IntegratedHazardEvent;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.service.hazard.HazardIntegrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional(readOnly = true)
class HazardIntegrationServiceTests {

    private static final Logger log = LoggerFactory.getLogger(HazardIntegrationServiceTests.class);

    @Autowired
    private HazardIntegrationService hazardIntegrationService;

    @Test
    @DisplayName("1. Retrieve Hazard By Unified ID: DFO, EM-DAT, and Weather")
    void testGetHazardById() {
        // 1. DFO Lookup
        IntegratedHazardEvent dfoEvent = hazardIntegrationService.getHazardById("DFO-1");
        assertNotNull(dfoEvent);
        assertEquals("DFO-1", dfoEvent.getId());
        assertEquals(HazardType.FLOOD, dfoEvent.getHazardType());
        assertEquals("DFO", dfoEvent.getDataSource());
        assertNotNull(dfoEvent.getLongitude());
        assertNotNull(dfoEvent.getLatitude());

        // 2. EM-DAT Lookup
        IntegratedHazardEvent emdatRecord = hazardIntegrationService.getHazardById("EMDAT-1");
        assertNotNull(emdatRecord);
        assertEquals("EMDAT-1", emdatRecord.getId());
        assertEquals(HazardType.FLOOD, emdatRecord.getHazardType());
        assertEquals("EM_DAT", emdatRecord.getDataSource());
        assertNull(emdatRecord.getLongitude());

        // 3. Not Found
        assertThrows(HazardNotFoundException.class, () -> hazardIntegrationService.getHazardById("DFO-99999"));
        assertThrows(HazardNotFoundException.class, () -> hazardIntegrationService.getHazardById("EMDAT-99999"));
        assertThrows(InvalidHazardParameterException.class, () -> hazardIntegrationService.getHazardById(""));

        log.info("✅ HazardIntegrationService ID lookup verified for DFO, EM-DAT, and error cases");
    }

    @Test
    @DisplayName("2. Retrieve Hazards By Type: FLOOD and EXTREME_RAINFALL")
    void testGetHazardsByType() {
        List<IntegratedHazardEvent> floodHazards = hazardIntegrationService.getHazardsByType(HazardType.FLOOD, 50);
        assertFalse(floodHazards.isEmpty());
        assertTrue(floodHazards.stream().allMatch(h -> h.getHazardType() == HazardType.FLOOD));

        List<IntegratedHazardEvent> rainHazards = hazardIntegrationService.getHazardsByType(HazardType.EXTREME_RAINFALL, 20);
        assertFalse(rainHazards.isEmpty());
        assertTrue(rainHazards.stream().allMatch(h -> h.getHazardType() == HazardType.EXTREME_RAINFALL));
        assertTrue(rainHazards.stream().allMatch(h -> h.getPrecipitationMm() >= 10.0));

        log.info("✅ Hazards by type verified: Floods ({}) and Extreme Rainfall ({})", floodHazards.size(), rainHazards.size());
    }

    @Test
    @DisplayName("3. District Spatial Intersection: Sitamarhi and Patna")
    void testGetHazardsInDistrict() {
        List<IntegratedHazardEvent> sitamarhiFloods = hazardIntegrationService.getHazardsInDistrict("Sitamarhi", HazardType.FLOOD, 50);
        assertFalse(sitamarhiFloods.isEmpty());
        assertTrue(sitamarhiFloods.stream().anyMatch(h -> h.getLocationName().toLowerCase().contains("sitamarhi")));

        List<IntegratedHazardEvent> patnaHazards = hazardIntegrationService.getHazardsInDistrict("Patna", null, 50);
        assertFalse(patnaHazards.isEmpty());

        // Validation test
        assertThrows(InvalidHazardParameterException.class, () ->
                hazardIntegrationService.getHazardsInDistrict(null, HazardType.FLOOD, 10));

        log.info("✅ District spatial intersection verified for Sitamarhi and Patna");
    }

    @Test
    @DisplayName("4. Coordinate Proximity Search (ST_DWithin)")
    void testGetHazardsNearLocation() {
        // Point in Muzaffarpur (85.39, 26.12)
        List<IntegratedHazardEvent> nearby = hazardIntegrationService.getHazardsNearLocation(
                85.39, 26.12, 50000.0, HazardType.FLOOD, 10
        );
        assertFalse(nearby.isEmpty());

        // Validation test: invalid coordinates
        assertThrows(InvalidHazardParameterException.class, () ->
                hazardIntegrationService.getHazardsNearLocation(200.0, 26.12, 1000.0, null, 10));
        assertThrows(InvalidHazardParameterException.class, () ->
                hazardIntegrationService.getHazardsNearLocation(85.39, 26.12, -100.0, null, 10));

        log.info("✅ Spatial proximity query verified near Muzaffarpur coordinate");
    }

    @Test
    @DisplayName("5. Bounding Box Spatial Search (ST_MakeEnvelope)")
    void testGetHazardsInBoundingBox() {
        // Bounding box covering North Bihar: minLon=84.5, minLat=25.5, maxLon=86.5, maxLat=27.0
        List<IntegratedHazardEvent> bboxHazards = hazardIntegrationService.getHazardsInBoundingBox(
                84.5, 25.5, 86.5, 27.0, HazardType.FLOOD, 50
        );
        assertFalse(bboxHazards.isEmpty());

        // Validation test: inverted bounding box
        assertThrows(InvalidHazardParameterException.class, () ->
                hazardIntegrationService.getHazardsInBoundingBox(86.5, 25.5, 84.5, 27.0, null, 10));

        log.info("✅ Bounding box spatial search verified for North Bihar region");
    }

    @Test
    @DisplayName("6. Temporal Range Filtering")
    void testGetHazardsInTimeRange() {
        LocalDate start = LocalDate.of(2000, 1, 1);
        LocalDate end = LocalDate.of(2010, 12, 31);

        List<IntegratedHazardEvent> timeRangeEvents = hazardIntegrationService.getHazardsInTimeRange(
                start, end, HazardType.FLOOD, 100
        );
        assertFalse(timeRangeEvents.isEmpty());

        // Validation test: inverted dates
        assertThrows(InvalidHazardParameterException.class, () ->
                hazardIntegrationService.getHazardsInTimeRange(end, start, null, 10));

        log.info("✅ Temporal range query verified between 2000 and 2010 ({} events)", timeRangeEvents.size());
    }

    @Test
    @DisplayName("7. Extreme Rainfall Meteorological Event Extraction")
    void testGetExtremeRainfallHazards() {
        List<IntegratedHazardEvent> extremeRain = hazardIntegrationService.getExtremeRainfallHazards(15.0, null, null, 50);
        assertFalse(extremeRain.isEmpty());
        assertTrue(extremeRain.stream().allMatch(e -> e.getPrecipitationMm() >= 15.0));

        // In 2024 July window
        LocalDateTime start = LocalDateTime.of(2024, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 7, 31, 23, 59);
        List<IntegratedHazardEvent> julyRain = hazardIntegrationService.getExtremeRainfallHazards(5.0, start, end, 50);
        assertFalse(julyRain.isEmpty());

        log.info("✅ Extreme rainfall hazard extraction verified (>=15mm: {} events)", extremeRain.size());
    }

    @Test
    @DisplayName("8. Catalog Summary Report Generation")
    void testGetHazardSummary() {
        HazardSummaryDto summary = hazardIntegrationService.getHazardSummary();
        assertNotNull(summary);
        assertEquals("EPSG:4326 (WGS 84)", summary.getCanonicalCrs());
        assertEquals(23, summary.getDfoFloodEventsCount());
        assertEquals(53, summary.getEmdatFloodRecordsCount());
        assertEquals(131544, summary.getWeatherObservationsCount());
        assertEquals(131620, summary.getTotalIntegratedRecords());
        assertTrue(summary.getActiveHazardTypes().contains(HazardType.FLOOD));
        assertTrue(summary.getActiveHazardTypes().contains(HazardType.EXTREME_RAINFALL));
        assertEquals(3, summary.getAvailableWeatherStations().size());

        log.info("✅ Integrated Hazard Catalog Summary verified (Total records: {})", summary.getTotalIntegratedRecords());
    }

    @Test
    @DisplayName("9. Standard GeoJSON FeatureCollection Generation")
    void testGetHazardsGeoJson() {
        GeoJsonFeatureCollectionDto fc = hazardIntegrationService.getHazardsGeoJson(HazardType.FLOOD, null, 50);
        assertNotNull(fc);
        assertEquals("FeatureCollection", fc.getType());
        assertEquals(23, fc.getCount());
        assertFalse(fc.getFeatures().isEmpty());
        assertEquals("Point", fc.getFeatures().get(0).getGeometry().getType());

        log.info("✅ GeoJSON FeatureCollection generation verified ({} features)", fc.getCount());
    }
}
