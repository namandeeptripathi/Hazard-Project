package com.hazard;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.boundaries.StateBoundary;
import com.hazard.domain.boundaries.SubdistrictBoundary;
import com.hazard.domain.hazard.DfoFloodEvent;
import com.hazard.domain.hazard.EmdatFloodRecord;
import com.hazard.domain.hydro.HydroRiver;
import com.hazard.domain.hydro.OsmWaterway;
import com.hazard.domain.population.OsmSettlement;
import com.hazard.domain.population.PopulatedPlace;
import com.hazard.domain.terrain.DemTile;
import com.hazard.domain.weather.HourlyWeather;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.boundaries.StateBoundaryRepository;
import com.hazard.repository.boundaries.SubdistrictBoundaryRepository;
import com.hazard.repository.hazard.DfoFloodEventRepository;
import com.hazard.repository.hazard.EmdatFloodRecordRepository;
import com.hazard.repository.hydro.HydroRiverRepository;
import com.hazard.repository.hydro.OsmWaterwayRepository;
import com.hazard.repository.population.OsmSettlementRepository;
import com.hazard.repository.population.PopulatedPlaceRepository;
import com.hazard.repository.terrain.DemTileRepository;
import com.hazard.repository.weather.HourlyWeatherRepository;
import com.hazard.service.boundaries.GeographicBoundaryService;
import com.hazard.service.hazard.HazardDataService;
import com.hazard.service.hydro.HydrologicalService;
import com.hazard.service.population.PopulationService;
import com.hazard.service.terrain.TerrainService;
import com.hazard.service.weather.WeatherDataService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional(readOnly = true)
class HazardApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(HazardApplicationTests.class);

    // Repositories
    @Autowired
    private DistrictBoundaryRepository districtBoundaryRepository;
    @Autowired
    private StateBoundaryRepository stateBoundaryRepository;
    @Autowired
    private SubdistrictBoundaryRepository subdistrictBoundaryRepository;
    @Autowired
    private DfoFloodEventRepository dfoFloodEventRepository;
    @Autowired
    private EmdatFloodRecordRepository emdatFloodRecordRepository;
    @Autowired
    private HourlyWeatherRepository hourlyWeatherRepository;
    @Autowired
    private HydroRiverRepository hydroRiverRepository;
    @Autowired
    private OsmWaterwayRepository osmWaterwayRepository;
    @Autowired
    private PopulatedPlaceRepository populatedPlaceRepository;
    @Autowired
    private OsmSettlementRepository osmSettlementRepository;
    @Autowired
    private DemTileRepository demTileRepository;

    // Services
    @Autowired
    private GeographicBoundaryService geographicBoundaryService;
    @Autowired
    private HazardDataService hazardDataService;
    @Autowired
    private WeatherDataService weatherDataService;
    @Autowired
    private HydrologicalService hydrologicalService;
    @Autowired
    private PopulationService populationService;
    @Autowired
    private TerrainService terrainService;

    @Test
    @DisplayName("1. Verify Repositories Load Exact Row Counts (159,005 rows)")
    void testRepositoryCounts() {
        assertEquals(1, stateBoundaryRepository.count());
        assertEquals(38, districtBoundaryRepository.count());
        assertEquals(53, subdistrictBoundaryRepository.count());
        assertEquals(23, dfoFloodEventRepository.count());
        assertEquals(53, emdatFloodRecordRepository.count());
        assertEquals(131544, hourlyWeatherRepository.count());
        assertEquals(6093, hydroRiverRepository.count());
        assertEquals(4401, osmWaterwayRepository.count());
        assertEquals(16208, populatedPlaceRepository.count());
        assertEquals(589, osmSettlementRepository.count());
        assertEquals(2, demTileRepository.count());
        log.info("✅ All 11 Repositories verified with exact table row counts (Total: 159,005 rows)");
    }

    @Test
    @DisplayName("2. GeographicBoundaryService: Point-in-Polygon & Input Validation")
    void testGeographicBoundaryService() {
        // Point in Patna
        Optional<DistrictBoundary> district = geographicBoundaryService.getDistrictByCoordinate(85.1376, 25.5941);
        assertTrue(district.isPresent());
        assertEquals("Patna", district.get().getName2());

        Optional<StateBoundary> state = geographicBoundaryService.getStateByCoordinate(85.1376, 25.5941);
        assertTrue(state.isPresent());
        assertEquals("Bihar", state.get().getName1());

        List<DistrictBoundary> allDistricts = geographicBoundaryService.getAllDistricts();
        assertEquals(38, allDistricts.size());

        // Validation test: invalid latitude
        assertThrows(IllegalArgumentException.class, () ->
                geographicBoundaryService.getDistrictByCoordinate(85.1376, 120.0));
        assertThrows(IllegalArgumentException.class, () ->
                geographicBoundaryService.getDistrictByName(""));
        log.info("✅ GeographicBoundaryService verified with coordinate validation");
    }

    @Test
    @DisplayName("3. HazardDataService: Flood Events, Date Ranges & Validation")
    void testHazardDataService() {
        List<DfoFloodEvent> allEvents = hazardDataService.getAllHistoricalFloodEvents();
        assertEquals(23, allEvents.size());

        List<DfoFloodEvent> events2000s = hazardDataService.getFloodEventsBetween(
                LocalDate.of(2000, 1, 1), LocalDate.of(2010, 12, 31)
        );
        assertEquals(14, events2000s.size(), "DFO events between 2000 and 2010 must equal 14");

        List<DfoFloodEvent> sitamarhiEvents = hazardDataService.getFloodEventsInDistrict("Sitamarhi");
        assertFalse(sitamarhiEvents.isEmpty());

        // Validation test: inverted date range
        assertThrows(IllegalArgumentException.class, () ->
                hazardDataService.getFloodEventsBetween(LocalDate.of(2020, 1, 1), LocalDate.of(2019, 1, 1)));
        assertThrows(IllegalArgumentException.class, () ->
                hazardDataService.getFloodEventsInDistrict(null));
        log.info("✅ HazardDataService verified with temporal and spatial queries");
    }

    @Test
    @DisplayName("4. WeatherDataService: Observations, Station Filter & Validation")
    void testWeatherDataService() {
        LocalDateTime start = LocalDateTime.of(2024, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 7, 7, 23, 0);

        List<HourlyWeather> weather = weatherDataService.getHistoricalWeather("Patna", start, end);
        assertEquals(168, weather.size());

        Optional<HourlyWeather> latest = weatherDataService.getLatestObservation("Patna");
        assertTrue(latest.isPresent());

        List<String> stations = weatherDataService.getAvailableStations();
        assertEquals(List.of("Bhagalpur", "Muzaffarpur", "Patna"), stations);

        // Validation test: inverted time range
        assertThrows(IllegalArgumentException.class, () ->
                weatherDataService.getHistoricalWeather("Patna", end, start));
        assertThrows(IllegalArgumentException.class, () ->
                weatherDataService.getLatestObservation("  "));
        log.info("✅ WeatherDataService verified with hourly time windows");
    }

    @Test
    @DisplayName("5. HydrologicalService: River Reaches, Strahler Filtering & Proximity")
    void testHydrologicalService() {
        List<HydroRiver> majorRivers = hydrologicalService.getRiversInDistrict("Patna", 5);
        assertFalse(majorRivers.isEmpty());
        assertTrue(majorRivers.get(0).getDisAvCms() > 1000.0);

        List<HydroRiver> nearbyReaches = hydrologicalService.getRiversNearLocation(85.1376, 25.5941, 5000.0, 5);
        assertFalse(nearbyReaches.isEmpty());

        List<OsmWaterway> nearbyWaterways = hydrologicalService.getWaterwaysNearLocation(85.1376, 25.5941, 10000.0, 5);
        assertFalse(nearbyWaterways.isEmpty());

        // Validation test: invalid Strahler order
        assertThrows(IllegalArgumentException.class, () ->
                hydrologicalService.getRiversInDistrict("Patna", 0));
        log.info("✅ HydrologicalService verified with stream orders and proximity queries");
    }

    @Test
    @DisplayName("6. PopulationService: Town Footprints, Settlements & Proximity")
    void testPopulationService() {
        List<PopulatedPlace> towns = populationService.getTownsInDistrict("Patna");
        assertFalse(towns.isEmpty());

        List<OsmSettlement> settlements = populationService.getSettlementsInDistrict("Nalanda");
        assertFalse(settlements.isEmpty());

        List<OsmSettlement> nearbySettlements = populationService.getSettlementsNearLocation(85.1376, 25.5941, 20000.0, 5);
        assertFalse(nearbySettlements.isEmpty());

        // Validation test
        assertThrows(IllegalArgumentException.class, () ->
                populationService.getTownsInDistrict(""));
        log.info("✅ PopulationService verified with settlement exposure queries");
    }

    @Test
    @DisplayName("7. TerrainService: DEM Tile Metadata & Spatial Footprints")
    void testTerrainService() {
        List<DemTile> tiles = terrainService.getAllAvailableDemTiles();
        assertEquals(2, tiles.size());

        Optional<DemTile> tile = terrainService.getDemTileForCoordinate(85.1376, 25.5941);
        assertTrue(tile.isPresent());
        assertEquals("copernicus_dsm_cog_10_n25_00_e085_00_dem_clean", tile.get().getTileName());

        List<DemTile> patnaTiles = terrainService.getDemTilesIntersectingDistrict("Patna");
        assertFalse(patnaTiles.isEmpty());

        // Validation test
        assertThrows(IllegalArgumentException.class, () ->
                terrainService.getDemTileByName(null));
        log.info("✅ TerrainService verified with DEM tile catalog resolution");
    }
}
