package com.hazard.service.exposure;

import com.hazard.domain.hydro.OsmWaterway;
import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.repository.hydro.OsmWaterwayRepository;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Master Infrastructure Data Provider SPI.
 * Aggregates real PostGIS water infrastructure, hydraulic assets, and regional critical lifelines across Bihar.
 */
@Component
public class InfrastructureDataProvider {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureDataProvider.class);

    private final OsmWaterwayRepository osmWaterwayRepository;
    private final List<InfrastructureAssetDto> configuredRegionalFacilities = new ArrayList<>();

    public InfrastructureDataProvider(OsmWaterwayRepository osmWaterwayRepository) {
        this.osmWaterwayRepository = osmWaterwayRepository;
        initConfiguredRegionalFacilities();
    }

    /**
     * Initializes configured regional pilot facilities across Bihar pilot basins.
     * These are reference facilities stored in the application dataset as candidate infrastructure for the MVP.
     */
    private void initConfiguredRegionalFacilities() {
        // HEALTHCARE (High / Very High Criticality)
        addFacility("FAC-MED-001", "Patna Medical College & Hospital (PMCH)", InfrastructureCategory.HEALTHCARE, "tertiary_hospital", "Patna", 85.1580, 25.6208, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-MED-002", "AIIMS Patna", InfrastructureCategory.HEALTHCARE, "super_specialty_hospital", "Patna", 85.0440, 25.5615, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-MED-003", "Nalanda Medical College & Hospital (NMCH)", InfrastructureCategory.HEALTHCARE, "tertiary_hospital", "Patna", 85.1972, 25.6022, InfrastructureCriticality.HIGH);
        addFacility("FAC-MED-004", "Sri Krishna Medical College & Hospital (SKMCH)", InfrastructureCategory.HEALTHCARE, "tertiary_hospital", "Muzaffarpur", 85.3910, 26.1520, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-MED-005", "Darbhanga Medical College & Hospital (DMCH)", InfrastructureCategory.HEALTHCARE, "tertiary_hospital", "Darbhanga", 85.8970, 26.1480, InfrastructureCriticality.HIGH);
        addFacility("FAC-MED-006", "Jawaharlal Nehru Medical College (JLNMCH)", InfrastructureCategory.HEALTHCARE, "tertiary_hospital", "Bhagalpur", 87.0120, 25.2440, InfrastructureCriticality.HIGH);
        addFacility("FAC-MED-007", "Sitamarhi Sadar District Hospital", InfrastructureCategory.HEALTHCARE, "district_hospital", "Sitamarhi", 85.4980, 26.5920, InfrastructureCriticality.HIGH);
        addFacility("FAC-MED-008", "Anugrah Narayan Magadh Medical College", InfrastructureCategory.HEALTHCARE, "tertiary_hospital", "Gaya", 84.9750, 24.7890, InfrastructureCriticality.HIGH);

        // EMERGENCY SERVICES & DISASTER LIFELINES (Very High Criticality)
        addFacility("FAC-EMG-001", "State Emergency Operations Center (SEOC)", InfrastructureCategory.EMERGENCY_SERVICES, "disaster_management_hq", "Patna", 85.1320, 25.6090, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-EMG-002", "SDRF Bihar Battalion HQ", InfrastructureCategory.EMERGENCY_SERVICES, "disaster_response_base", "Patna", 85.2410, 25.5920, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-EMG-003", "Sitamarhi Central Flood Shelter", InfrastructureCategory.EMERGENCY_SERVICES, "flood_relief_shelter", "Sitamarhi", 85.5030, 26.5950, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-EMG-004", "Muzaffarpur Disaster Control Center", InfrastructureCategory.EMERGENCY_SERVICES, "emergency_control_room", "Muzaffarpur", 85.3850, 26.1210, InfrastructureCriticality.HIGH);
        addFacility("FAC-EMG-005", "Patna Central Fire Station", InfrastructureCategory.EMERGENCY_SERVICES, "fire_station", "Patna", 85.1410, 25.6130, InfrastructureCriticality.VERY_HIGH);

        // TRANSPORT & STRATEGIC BRIDGES (High Criticality)
        addFacility("FAC-TRN-001", "Mahatma Gandhi Setu (Ganga Bridge)", InfrastructureCategory.TRANSPORT, "major_river_bridge", "Patna", 85.2150, 25.6170, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-TRN-002", "Digha-Sonpur Bridge (J.P. Setu)", InfrastructureCategory.TRANSPORT, "rail_road_bridge", "Patna", 85.1070, 25.6510, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-TRN-003", "Rajendra Setu (Mokama Ganga Bridge)", InfrastructureCategory.TRANSPORT, "rail_road_bridge", "Patna", 85.9810, 25.4050, InfrastructureCriticality.HIGH);
        addFacility("FAC-TRN-004", "Vikramshila Setu (Bhagalpur Ganga Bridge)", InfrastructureCategory.TRANSPORT, "major_river_bridge", "Bhagalpur", 87.0250, 25.2750, InfrastructureCriticality.HIGH);
        addFacility("FAC-TRN-005", "Jayaprakash Narayan International Airport", InfrastructureCategory.TRANSPORT, "international_airport", "Patna", 85.0880, 25.5910, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-TRN-006", "Patna Junction Railway Station", InfrastructureCategory.TRANSPORT, "central_railway_hub", "Patna", 85.1370, 25.6020, InfrastructureCriticality.HIGH);
        addFacility("FAC-TRN-007", "Muzaffarpur Junction Railway Station", InfrastructureCategory.TRANSPORT, "railway_junction", "Muzaffarpur", 85.3810, 26.1230, InfrastructureCriticality.HIGH);

        // POWER & ENERGY (High Criticality)
        addFacility("FAC-PWR-001", "Barauni Thermal Power Station", InfrastructureCategory.POWER, "thermal_power_plant", "Begusarai", 86.0120, 25.3950, InfrastructureCriticality.VERY_HIGH);
        addFacility("FAC-PWR-002", "Kanti Thermal Power Plant (MTPS)", InfrastructureCategory.POWER, "thermal_power_plant", "Muzaffarpur", 85.3050, 26.2050, InfrastructureCriticality.HIGH);
        addFacility("FAC-PWR-003", "Khagaul 400kV Grid Substation", InfrastructureCategory.POWER, "transmission_substation", "Patna", 85.0350, 25.5780, InfrastructureCriticality.HIGH);
        addFacility("FAC-PWR-004", "Muzaffarpur 220kV Grid Substation", InfrastructureCategory.POWER, "transmission_substation", "Muzaffarpur", 85.3520, 26.1080, InfrastructureCriticality.HIGH);

        // GOVERNMENT & ADMINISTRATIVE CENTERS (Moderate Criticality)
        addFacility("FAC-GOV-001", "Bihar State Secretariat (Old & New)", InfrastructureCategory.GOVERNMENT, "state_secretariat", "Patna", 85.1220, 25.6060, InfrastructureCriticality.HIGH);
        addFacility("FAC-GOV-002", "Patna District Collectorate", InfrastructureCategory.GOVERNMENT, "district_collectorate", "Patna", 85.1430, 25.6200, InfrastructureCriticality.MODERATE);
        addFacility("FAC-GOV-003", "Sitamarhi Collectorate & District HQ", InfrastructureCategory.GOVERNMENT, "district_collectorate", "Sitamarhi", 85.4950, 26.5890, InfrastructureCriticality.MODERATE);
        addFacility("FAC-GOV-004", "Muzaffarpur Collectorate", InfrastructureCategory.GOVERNMENT, "district_collectorate", "Muzaffarpur", 85.3930, 26.1240, InfrastructureCriticality.MODERATE);

        // EDUCATION & CAMPUSES (Moderate Criticality)
        addFacility("FAC-EDU-001", "National Institute of Technology Patna (NITP)", InfrastructureCategory.EDUCATION, "engineering_institute", "Patna", 85.1720, 25.6210, InfrastructureCriticality.MODERATE);
        addFacility("FAC-EDU-002", "Patna University Main Campus", InfrastructureCategory.EDUCATION, "university_campus", "Patna", 85.1680, 25.6190, InfrastructureCriticality.MODERATE);
        addFacility("FAC-EDU-003", "Babasaheb Bhimrao Ambedkar Bihar University", InfrastructureCategory.EDUCATION, "university_campus", "Muzaffarpur", 85.3620, 26.1150, InfrastructureCriticality.MODERATE);

        log.info("Initialized {} configured regional critical facilities for Bihar pilot basins", configuredRegionalFacilities.size());
    }

    private void addFacility(String id, String name, InfrastructureCategory cat, String subType, String district, double lon, double lat, InfrastructureCriticality crit) {
        InfrastructureAssetDto dto = new InfrastructureAssetDto();
        dto.setAssetId(id);
        dto.setAssetName(name);
        dto.setCategory(cat);
        dto.setSubType(subType);
        dto.setDistrictName(district);
        dto.setLongitude(lon);
        dto.setLatitude(lat);
        dto.setGeometryType("Point");
        dto.setLineInfrastructure(false);
        dto.setCriticality(crit);
        dto.setCriticalitySource("CONFIGURED_MAPPING");
        dto.setDataProvenance("CONFIGURED_REGIONAL_FACILITIES");
        configuredRegionalFacilities.add(dto);
    }

    /**
     * Retrieves all water infrastructure from PostGIS matching a radial buffer around a coordinate.
     */
    public List<InfrastructureAssetDto> getWaterInfrastructureInPointBuffer(double lon, double lat, double bufferMeters) {
        List<OsmWaterway> waterways = osmWaterwayRepository.findInfrastructureWithinBufferOfPoint(lon, lat, bufferMeters);
        return waterways.stream().map(this::mapWaterwayToDto).collect(Collectors.toList());
    }

    /**
     * Retrieves all water infrastructure from PostGIS intersecting a WKT polygon.
     */
    public List<InfrastructureAssetDto> getWaterInfrastructureInWktPolygon(String wkt) {
        List<OsmWaterway> waterways = osmWaterwayRepository.findInfrastructureIntersectingGeometryWkt(wkt);
        return waterways.stream().map(this::mapWaterwayToDto).collect(Collectors.toList());
    }

    /**
     * Retrieves all water infrastructure in an administrative district.
     */
    public List<InfrastructureAssetDto> getWaterInfrastructureInDistrict(String districtName) {
        List<OsmWaterway> waterways = osmWaterwayRepository.findInfrastructureInDistrictSpatial(districtName);
        return waterways.stream().map(this::mapWaterwayToDto).collect(Collectors.toList());
    }

    /**
     * Retrieves configured regional facilities intersecting a radial buffer.
     */
    public List<InfrastructureAssetDto> getRegionalFacilitiesInPointBuffer(double lon, double lat, double bufferMeters) {
        List<InfrastructureAssetDto> list = new ArrayList<>();
        for (InfrastructureAssetDto f : configuredRegionalFacilities) {
            if (f.getLongitude() == null || f.getLatitude() == null) continue;
            double dist = SettlementExposureService.haversineDistanceMeters(lat, lon, f.getLatitude(), f.getLongitude());
            if (dist <= bufferMeters) {
                InfrastructureAssetDto copy = copyFacilityDto(f);
                copy.setDistanceMeters(dist);
                list.add(copy);
            }
        }
        return list;
    }

    /**
     * Retrieves configured regional facilities in a district.
     */
    public List<InfrastructureAssetDto> getRegionalFacilitiesInDistrict(String districtName) {
        return configuredRegionalFacilities.stream()
                .filter(f -> f.getDistrictName() != null && f.getDistrictName().equalsIgnoreCase(districtName))
                .map(this::copyFacilityDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all configured regional facilities across all districts.
     */
    public List<InfrastructureAssetDto> getAllRegionalFacilities() {
        return configuredRegionalFacilities.stream()
                .map(this::copyFacilityDto)
                .collect(Collectors.toList());
    }

    /**
     * Stage 5.7: Retrieves all configured healthcare facilities (hospitals, medical centers).
     */
    public List<InfrastructureAssetDto> getHealthcareFacilities() {
        return configuredRegionalFacilities.stream()
                .filter(f -> f.getCategory() == InfrastructureCategory.HEALTHCARE)
                .map(this::copyFacilityDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps an OsmWaterway PostGIS entity to an InfrastructureAssetDto.
     */
    public InfrastructureAssetDto mapWaterwayToDto(OsmWaterway w) {
        InfrastructureAssetDto dto = new InfrastructureAssetDto();
        dto.setAssetId("INFRA-WATER-" + w.getId());
        String name = w.getName() != null && !w.getName().trim().isEmpty()
                ? w.getName()
                : (w.getWaterway() != null ? "Waterway: " + w.getWaterway() : "Waterbody: " + (w.getWater() != null ? w.getWater() : "Hydraulic Structure"));
        dto.setAssetName(name);
        dto.setCategory(InfrastructureCategory.WATER);
        dto.setSubType(w.getWaterway() != null ? w.getWaterway() : (w.getWater() != null ? w.getWater() : "canal"));
        dto.setCriticality(InfrastructureCategory.WATER.getDefaultCriticality());
        dto.setCriticalitySource("CONFIGURED_MAPPING");
        dto.setDataProvenance("POSTGIS_HYDRO_OSM");

        if (w.getGeom() != null) {
            Geometry g = w.getGeom();
            dto.setGeometryType(g.getGeometryType());
            dto.setLongitude(g.getCentroid().getX());
            dto.setLatitude(g.getCentroid().getY());

            if (g instanceof LineString || g instanceof MultiLineString) {
                dto.setLineInfrastructure(true);
                // Approximate length in km from degree coordinates for Bihar latitude (approx 111km/deg)
                double degLength = g.getLength();
                double kmLength = Math.round(degLength * 111.32 * 100.0) / 100.0;
                dto.setTotalLengthKm(kmLength > 0.0 ? kmLength : 0.5);
                dto.setAffectedLengthKm(dto.getTotalLengthKm());
                dto.setAffectedPercentage(100.0);
            } else {
                dto.setLineInfrastructure(false);
            }
        }

        return dto;
    }

    private InfrastructureAssetDto copyFacilityDto(InfrastructureAssetDto src) {
        InfrastructureAssetDto d = new InfrastructureAssetDto();
        d.setAssetId(src.getAssetId());
        d.setAssetName(src.getAssetName());
        d.setCategory(src.getCategory());
        d.setSubType(src.getSubType());
        d.setDistrictName(src.getDistrictName());
        d.setLongitude(src.getLongitude());
        d.setLatitude(src.getLatitude());
        d.setGeometryType(src.getGeometryType());
        d.setLineInfrastructure(src.isLineInfrastructure());
        d.setTotalLengthKm(src.getTotalLengthKm());
        d.setAffectedLengthKm(src.getAffectedLengthKm());
        d.setAffectedPercentage(src.getAffectedPercentage());
        d.setCriticality(src.getCriticality());
        d.setCriticalitySource(src.getCriticalitySource());
        d.setDataProvenance(src.getDataProvenance());
        return d;
    }
}
