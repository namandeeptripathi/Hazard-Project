package com.hazard.service.layer;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.hazard.HazardLayerCategory;
import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.domain.hydro.HydroRiver;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.GeoJsonGeometryDto;
import com.hazard.dto.hazard.IntegratedHazardEvent;
import com.hazard.dto.layer.HazardLayerCatalogDto;
import com.hazard.dto.layer.HazardLayerMetadataDto;
import com.hazard.dto.multihazard.MultiHazardObservation;
import com.hazard.dto.processing.ProcessedHazardObservation;
import com.hazard.dto.scoring.HazardScoreDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.hydro.HydroRiverRepository;
import com.hazard.service.hazard.HazardIntegrationService;
import com.hazard.service.multihazard.MultiHazardService;
import com.hazard.service.processing.HazardProcessingService;
import com.hazard.service.scoring.HazardScoringService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Master Service for Stage 3.6 Map-Ready GIS Hazard Layers.
 * Translates Stage 3 hazard intelligence into standardized RFC 7946 GeoJSON FeatureCollections,
 * provides layer metadata catalogs, and applies spatial, severity, and temporal filters.
 */
@Service
@Transactional(readOnly = true)
public class HazardLayerService {

    public static final int DEFAULT_LIMIT = 50;
    public static final int MAX_LIMIT = 1000;

    // Layer Identifiers
    public static final String LAYER_FLOOD_EVENTS = "FLOOD_EVENTS";
    public static final String LAYER_EXTREME_RAINFALL_EVENTS = "EXTREME_RAINFALL_EVENTS";
    public static final String LAYER_FLOOD_HAZARD_SCORES = "FLOOD_HAZARD_SCORES";
    public static final String LAYER_EXTREME_RAINFALL_SCORES = "EXTREME_RAINFALL_SCORES";
    public static final String LAYER_MULTI_HAZARD_INDEX = "MULTI_HAZARD_INDEX";
    public static final String LAYER_DISTRICT_HAZARD_SUMMARIES = "DISTRICT_HAZARD_SUMMARIES";
    public static final String LAYER_DISTRICT_BOUNDARIES = "DISTRICT_BOUNDARIES";
    public static final String LAYER_RIVERS_REFERENCE = "RIVERS_REFERENCE";

    private final HazardIntegrationService hazardIntegrationService;
    private final HazardProcessingService hazardProcessingService;
    private final HazardScoringService hazardScoringService;
    private final MultiHazardService multiHazardService;
    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final HydroRiverRepository hydroRiverRepository;

    private final Map<String, HazardLayerMetadataDto> layerCatalogMap = new LinkedHashMap<>();

    public HazardLayerService(HazardIntegrationService hazardIntegrationService,
                              HazardProcessingService hazardProcessingService,
                              HazardScoringService hazardScoringService,
                              MultiHazardService multiHazardService,
                              DistrictBoundaryRepository districtBoundaryRepository,
                              HydroRiverRepository hydroRiverRepository) {
        this.hazardIntegrationService = hazardIntegrationService;
        this.hazardProcessingService = hazardProcessingService;
        this.hazardScoringService = hazardScoringService;
        this.multiHazardService = multiHazardService;
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.hydroRiverRepository = hydroRiverRepository;

        initializeLayerCatalog();
    }

    private void initializeLayerCatalog() {
        registerLayer(new HazardLayerMetadataDto(
                LAYER_FLOOD_EVENTS,
                "Historical Flood Events",
                HazardLayerCategory.EVENT_LAYER,
                "Point",
                HazardType.FLOOD,
                "Discrete spatial point observations of historical flood events from Dartmouth Flood Observatory (DFO)",
                List.of("district", "from", "to", "limit"),
                false,
                false,
                "Dartmouth Flood Observatory (DFO)",
                "/api/v1/hazards/layers/" + LAYER_FLOOD_EVENTS
        ));

        registerLayer(new HazardLayerMetadataDto(
                LAYER_EXTREME_RAINFALL_EVENTS,
                "Extreme Rainfall Events",
                HazardLayerCategory.EVENT_LAYER,
                "Point",
                HazardType.EXTREME_RAINFALL,
                "Meteorological extreme precipitation observations from Open-Meteo weather stations",
                List.of("district", "from", "to", "limit"),
                false,
                false,
                "Open-Meteo Historical Weather",
                "/api/v1/hazards/layers/" + LAYER_EXTREME_RAINFALL_EVENTS
        ));

        registerLayer(new HazardLayerMetadataDto(
                LAYER_FLOOD_HAZARD_SCORES,
                "Flood Hazard Scores",
                HazardLayerCategory.HAZARD_SCORE_LAYER,
                "Point",
                HazardType.FLOOD,
                "Single-hazard Flood Hazard Scores [0.0000, 1.0000] and categorical severity tiers",
                List.of("district", "severity", "limit"),
                true,
                true,
                "Stage 3.4 Flood Scoring Service",
                "/api/v1/hazards/layers/" + LAYER_FLOOD_HAZARD_SCORES
        ));

        registerLayer(new HazardLayerMetadataDto(
                LAYER_EXTREME_RAINFALL_SCORES,
                "Extreme Rainfall Scores",
                HazardLayerCategory.HAZARD_SCORE_LAYER,
                "Point",
                HazardType.EXTREME_RAINFALL,
                "Single-hazard Extreme Rainfall Hazard Scores [0.0000, 1.0000] and categorical severity tiers",
                List.of("district", "severity", "limit"),
                true,
                true,
                "Stage 3.4 Rainfall Scoring Service",
                "/api/v1/hazards/layers/" + LAYER_EXTREME_RAINFALL_SCORES
        ));

        registerLayer(new HazardLayerMetadataDto(
                LAYER_MULTI_HAZARD_INDEX,
                "Multi-Hazard Composite Index",
                HazardLayerCategory.MULTI_HAZARD_LAYER,
                "Point",
                null,
                "Cross-hazard synthesized Multi-Hazard Index [0.0000, 1.0000] with dominant hazard classification",
                List.of("district", "severity", "limit"),
                true,
                true,
                "Stage 3.5 Multi-Hazard Integration Engine",
                "/api/v1/hazards/layers/" + LAYER_MULTI_HAZARD_INDEX
        ));

        registerLayer(new HazardLayerMetadataDto(
                LAYER_DISTRICT_HAZARD_SUMMARIES,
                "District Hazard Summaries (Choropleth)",
                HazardLayerCategory.DISTRICT_SUMMARY_LAYER,
                "MultiPolygon",
                null,
                "Administrative boundary polygons for all 38 Bihar districts enriched with aggregated flood and rainfall hazard scores",
                List.of("district", "limit"),
                true,
                true,
                "PostGIS District Boundaries & Stage 3 Hazard Intelligence",
                "/api/v1/hazards/layers/" + LAYER_DISTRICT_HAZARD_SUMMARIES
        ));

        registerLayer(new HazardLayerMetadataDto(
                LAYER_DISTRICT_BOUNDARIES,
                "District Administrative Boundaries",
                HazardLayerCategory.REFERENCE_LAYER,
                "MultiPolygon",
                null,
                "Official Survey of India administrative district boundaries for Bihar (38 districts)",
                List.of("district", "limit"),
                false,
                false,
                "Survey of India / GADM Boundaries",
                "/api/v1/hazards/layers/" + LAYER_DISTRICT_BOUNDARIES
        ));

        registerLayer(new HazardLayerMetadataDto(
                LAYER_RIVERS_REFERENCE,
                "Major River Network",
                HazardLayerCategory.REFERENCE_LAYER,
                "MultiLineString",
                null,
                "HydroRIVERS hydrological river reaches and drainage network for Bihar floodplains",
                List.of("district", "limit"),
                false,
                false,
                "HydroSHEDS / HydroRIVERS",
                "/api/v1/hazards/layers/" + LAYER_RIVERS_REFERENCE
        ));
    }

    private void registerLayer(HazardLayerMetadataDto metadata) {
        layerCatalogMap.put(metadata.getLayerId().toUpperCase(), metadata);
    }

    /**
     * Returns the full catalog of available map layers.
     */
    public HazardLayerCatalogDto getLayerCatalog() {
        return new HazardLayerCatalogDto(new ArrayList<>(layerCatalogMap.values()));
    }

    /**
     * Retrieves metadata for a specific layer.
     */
    public HazardLayerMetadataDto getLayerMetadata(String layerId) {
        if (layerId == null || layerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Layer ID cannot be null or empty");
        }
        HazardLayerMetadataDto metadata = layerCatalogMap.get(layerId.trim().toUpperCase());
        if (metadata == null) {
            throw new HazardNotFoundException("Hazard layer not found with ID: " + layerId);
        }
        return metadata;
    }

    /**
     * Generates map-ready GeoJSON for the requested layer with optional filtering.
     */
    public GeoJsonFeatureCollectionDto getLayerGeoJson(String layerId, String district, SeverityTier severity,
                                                      LocalDate from, LocalDate to, Integer limit) {
        int safeLimit = sanitizeLimit(limit);
        String upperId = layerId != null ? layerId.trim().toUpperCase() : "";

        return switch (upperId) {
            case LAYER_FLOOD_EVENTS -> buildFloodEventsLayer(district, from, to, safeLimit);
            case LAYER_EXTREME_RAINFALL_EVENTS -> buildExtremeRainfallEventsLayer(district, from, to, safeLimit);
            case LAYER_FLOOD_HAZARD_SCORES -> buildFloodHazardScoresLayer(district, severity, safeLimit);
            case LAYER_EXTREME_RAINFALL_SCORES -> buildExtremeRainfallScoresLayer(district, severity, safeLimit);
            case LAYER_MULTI_HAZARD_INDEX -> buildMultiHazardIndexLayer(district, severity, safeLimit);
            case LAYER_DISTRICT_HAZARD_SUMMARIES -> buildDistrictHazardSummariesLayer(district, safeLimit);
            case LAYER_DISTRICT_BOUNDARIES -> buildDistrictBoundariesLayer(district, safeLimit);
            case LAYER_RIVERS_REFERENCE -> buildRiversReferenceLayer(district, safeLimit);
            default -> throw new HazardNotFoundException("Unknown hazard layer ID: " + layerId +
                    ". Available layers: " + String.join(", ", layerCatalogMap.keySet()));
        };
    }

    // =========================================================================
    // LAYER BUILDERS
    // =========================================================================

    private GeoJsonFeatureCollectionDto buildFloodEventsLayer(String district, LocalDate from, LocalDate to, int limit) {
        List<ProcessedHazardObservation> events = hazardProcessingService.getAllProcessedHazards(HazardType.FLOOD, QualityStatus.VALID, district, limit);
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (ProcessedHazardObservation ev : events) {
            if (ev.getLongitude() == null || ev.getLatitude() == null) continue;

            if (district != null && (ev.getAssociatedDistrict() == null || !ev.getAssociatedDistrict().equalsIgnoreCase(district))) continue;
            if (from != null && ev.getStartDate() != null && ev.getStartDate().isBefore(from)) continue;
            if (to != null && ev.getEndDate() != null && ev.getEndDate().isAfter(to)) continue;

            GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(ev.getLongitude(), ev.getLatitude());
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id", ev.getId());
            props.put("layerId", LAYER_FLOOD_EVENTS);
            props.put("hazardType", HazardType.FLOOD.name());
            props.put("dataSource", ev.getDataSource());
            props.put("locationName", ev.getLocationName());
            props.put("associatedDistrict", ev.getAssociatedDistrict());
            props.put("isWithinBiharBoundary", ev.getIsWithinBiharBoundary());
            props.put("startDate", ev.getStartDate() != null ? ev.getStartDate().toString() : null);
            props.put("endDate", ev.getEndDate() != null ? ev.getEndDate().toString() : null);
            props.put("durationDays", ev.getDurationDays());
            props.put("affectedAreaSqKm", ev.getAffectedAreaSqKm());
            props.put("displacedPopulation", ev.getDisplacedPopulation());
            props.put("fatalities", ev.getFatalities());

            features.add(new GeoJsonFeatureDto(ev.getId(), geom, props));
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    private GeoJsonFeatureCollectionDto buildExtremeRainfallEventsLayer(String district, LocalDate from, LocalDate to, int limit) {
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(23, 59, 59) : null;
        List<IntegratedHazardEvent> events = hazardIntegrationService.getExtremeRainfallHazards(15.0, fromDt, toDt, limit);
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (IntegratedHazardEvent ev : events) {
            if (ev.getLongitude() == null || ev.getLatitude() == null) continue;

            GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(ev.getLongitude(), ev.getLatitude());
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id", ev.getId());
            props.put("layerId", LAYER_EXTREME_RAINFALL_EVENTS);
            props.put("hazardType", HazardType.EXTREME_RAINFALL.name());
            props.put("dataSource", ev.getDataSource());
            props.put("locationName", ev.getLocationName());
            props.put("timestamp", ev.getTimestamp() != null ? ev.getTimestamp().toString() : null);
            props.put("precipitationMm", ev.getPrecipitationMm());
            props.put("severity", ev.getSeverity());

            features.add(new GeoJsonFeatureDto(ev.getId(), geom, props));
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    private GeoJsonFeatureCollectionDto buildFloodHazardScoresLayer(String district, SeverityTier severity, int limit) {
        String sevStr = severity != null ? severity.name() : null;
        List<HazardScoreDto> scores = hazardScoringService.getHazardScoresByType(HazardType.FLOOD, sevStr, limit);
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (HazardScoreDto sc : scores) {
            if (sc.getLongitude() == null || sc.getLatitude() == null) continue;
            if (district != null && (sc.getAssociatedDistrict() == null || !sc.getAssociatedDistrict().equalsIgnoreCase(district))) continue;

            GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(sc.getLongitude(), sc.getLatitude());
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id", sc.getId());
            props.put("layerId", LAYER_FLOOD_HAZARD_SCORES);
            props.put("hazardType", HazardType.FLOOD.name());
            props.put("associatedDistrict", sc.getAssociatedDistrict());
            props.put("isWithinBiharBoundary", sc.getIsWithinBiharBoundary());
            props.put("hazardScore", sc.getHazardScore());
            props.put("severityTier", sc.getSeverityTier() != null ? sc.getSeverityTier().name() : null);
            props.put("completenessRatio", sc.getCompletenessRatio());
            props.put("explanation", sc.getExplanation());

            features.add(new GeoJsonFeatureDto(sc.getId(), geom, props));
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    private GeoJsonFeatureCollectionDto buildExtremeRainfallScoresLayer(String district, SeverityTier severity, int limit) {
        String sevStr = severity != null ? severity.name() : null;
        List<HazardScoreDto> scores = hazardScoringService.getHazardScoresByType(HazardType.EXTREME_RAINFALL, sevStr, limit);
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (HazardScoreDto sc : scores) {
            if (sc.getLongitude() == null || sc.getLatitude() == null) continue;
            if (district != null && (sc.getAssociatedDistrict() == null || !sc.getAssociatedDistrict().equalsIgnoreCase(district))) continue;

            GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(sc.getLongitude(), sc.getLatitude());
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id", sc.getId());
            props.put("layerId", LAYER_EXTREME_RAINFALL_SCORES);
            props.put("hazardType", HazardType.EXTREME_RAINFALL.name());
            props.put("associatedDistrict", sc.getAssociatedDistrict());
            props.put("timestamp", sc.getTimestamp() != null ? sc.getTimestamp().toString() : null);
            props.put("hazardScore", sc.getHazardScore());
            props.put("severityTier", sc.getSeverityTier() != null ? sc.getSeverityTier().name() : null);
            props.put("completenessRatio", sc.getCompletenessRatio());
            props.put("explanation", sc.getExplanation());

            features.add(new GeoJsonFeatureDto(sc.getId(), geom, props));
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    private GeoJsonFeatureCollectionDto buildMultiHazardIndexLayer(String district, SeverityTier severity, int limit) {
        return multiHazardService.getMultiHazardGeoJson(district, severity, limit);
    }

    private GeoJsonFeatureCollectionDto buildDistrictHazardSummariesLayer(String districtFilter, int limit) {
        List<DistrictBoundary> districts = districtBoundaryRepository.findAllByOrderByName2Asc();
        List<MultiHazardObservation> multiHazards = multiHazardService.getAllMultiHazardObservations(null, null, null, 1000);

        // Group multi-hazards by district
        Map<String, List<MultiHazardObservation>> districtMap = multiHazards.stream()
                .filter(m -> m.getAssociatedDistrict() != null)
                .collect(Collectors.groupingBy(m -> m.getAssociatedDistrict().toUpperCase()));

        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (DistrictBoundary db : districts) {
            if (districtFilter != null && !districtFilter.trim().isEmpty()) {
                if (db.getName2() == null || !db.getName2().equalsIgnoreCase(districtFilter.trim())) {
                    continue;
                }
            }

            GeoJsonGeometryDto geom = GeoJsonGeometryDto.fromJtsGeometry(db.getGeom());
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("districtId", db.getId());
            props.put("districtName", db.getName2());
            props.put("state", db.getName1());
            props.put("gid2", db.getGid2());
            props.put("layerId", LAYER_DISTRICT_HAZARD_SUMMARIES);

            List<MultiHazardObservation> obs = districtMap.getOrDefault(db.getName2().toUpperCase(), Collections.emptyList());
            props.put("totalHazardEventsCount", obs.size());

            boolean hasStation = "PATNA".equalsIgnoreCase(db.getName2()) ||
                                 "MUZAFFARPUR".equalsIgnoreCase(db.getName2()) ||
                                 "BHAGALPUR".equalsIgnoreCase(db.getName2());
            props.put("hasActiveWeatherStation", hasStation);

            if (!obs.isEmpty()) {
                double maxIndex = obs.stream().mapToDouble(m -> m.getMultiHazardIndex() != null ? m.getMultiHazardIndex() : 0.0).max().orElse(0.0);
                double roundedMax = Math.round(maxIndex * 10000.0) / 10000.0;
                props.put("peakMultiHazardIndex", roundedMax);
                props.put("severityTier", SeverityTier.fromScore(roundedMax).name());

                MultiHazardObservation peakObs = obs.stream()
                        .max(Comparator.comparingDouble(m -> m.getMultiHazardIndex() != null ? m.getMultiHazardIndex() : 0.0))
                        .orElse(null);
                if (peakObs != null) {
                    props.put("dominantHazard", peakObs.getDominantHazard() != null ? peakObs.getDominantHazard().name() : null);
                    props.put("dominantHazardScore", peakObs.getDominantHazardScore());
                }
            } else {
                props.put("peakMultiHazardIndex", 0.0);
                props.put("severityTier", SeverityTier.LOW.name());
                props.put("dominantHazard", "NONE");
                props.put("dominantHazardScore", 0.0);
            }

            features.add(new GeoJsonFeatureDto("DISTRICT-" + db.getId(), geom, props));
            if (features.size() >= limit) break;
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    private GeoJsonFeatureCollectionDto buildDistrictBoundariesLayer(String districtFilter, int limit) {
        List<DistrictBoundary> districts = districtBoundaryRepository.findAllByOrderByName2Asc();
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (DistrictBoundary db : districts) {
            if (districtFilter != null && !districtFilter.trim().isEmpty()) {
                if (db.getName2() == null || !db.getName2().equalsIgnoreCase(districtFilter.trim())) {
                    continue;
                }
            }

            GeoJsonGeometryDto geom = GeoJsonGeometryDto.fromJtsGeometry(db.getGeom());
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("districtId", db.getId());
            props.put("districtName", db.getName2());
            props.put("state", db.getName1());
            props.put("country", db.getCountry());
            props.put("gid2", db.getGid2());
            props.put("hasc2", db.getHasc2());
            props.put("engtype2", db.getEngtype2());
            props.put("layerId", LAYER_DISTRICT_BOUNDARIES);

            features.add(new GeoJsonFeatureDto("DISTRICT-BOUNDARY-" + db.getId(), geom, props));
            if (features.size() >= limit) break;
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    private GeoJsonFeatureCollectionDto buildRiversReferenceLayer(String district, int limit) {
        List<HydroRiver> rivers;
        if (district != null && !district.trim().isEmpty()) {
            rivers = hydroRiverRepository.findRiversInDistrict(district.trim(), 1);
        } else {
            rivers = hydroRiverRepository.findByOrdStraGreaterThanEqualOrderByDisAvCmsDesc(3);
        }

        List<GeoJsonFeatureDto> features = new ArrayList<>();
        for (HydroRiver river : rivers) {
            if (river.getGeom() == null) continue;

            GeoJsonGeometryDto geom = GeoJsonGeometryDto.fromJtsGeometry(river.getGeom());
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("hyrivId", river.getHyrivId());
            props.put("strahlerOrder", river.getOrdStra());
            props.put("lengthKm", river.getLengthKm());
            props.put("avgDischargeCms", river.getDisAvCms());
            props.put("mainRiverId", river.getMainRiv());
            props.put("basinLevel12Id", river.getHybasL12());
            props.put("layerId", LAYER_RIVERS_REFERENCE);

            features.add(new GeoJsonFeatureDto("RIVER-" + river.getId(), geom, props));
            if (features.size() >= limit) break;
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
