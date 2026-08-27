package com.hazard.service.exposure;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.exposure.PopulationDataSource;
import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.population.PopulatedPlace;
import com.hazard.dto.exposure.*;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.GeoJsonGeometryDto;
import com.hazard.dto.hazard.IntegratedHazardEvent;
import com.hazard.dto.multihazard.MultiHazardObservation;
import com.hazard.dto.scoring.HazardScoreDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.population.PopulatedPlaceRepository;
import com.hazard.service.hazard.HazardIntegrationService;
import com.hazard.service.multihazard.MultiHazardService;
import com.hazard.service.scoring.HazardScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Master Service for Stage 4.2 Settlement Exposure.
 *
 * Identifies which specific villages, towns, cities, and residential settlement clusters
 * are inside or affected by predicted or observed hazard areas, quantifying settlement-level
 * exposure scores, distances from hazard sources, and categorical exposure tiers.
 */
@Service
@Transactional(readOnly = true)
public class SettlementExposureService {

    private static final Logger log = LoggerFactory.getLogger(SettlementExposureService.class);

    private final PopulatedPlaceRepository populatedPlaceRepository;
    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final HazardIntegrationService hazardIntegrationService;
    private final HazardScoringService hazardScoringService;
    private final MultiHazardService multiHazardService;
    private final PopulationExposureConfig config;
    private final SettlementExposureEngine settlementEngine;

    public SettlementExposureService(PopulatedPlaceRepository populatedPlaceRepository,
                                     DistrictBoundaryRepository districtBoundaryRepository,
                                     HazardIntegrationService hazardIntegrationService,
                                     HazardScoringService hazardScoringService,
                                     MultiHazardService multiHazardService,
                                     PopulationExposureConfig config,
                                     SettlementExposureEngine settlementEngine) {
        this.populatedPlaceRepository = populatedPlaceRepository;
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.hazardIntegrationService = hazardIntegrationService;
        this.hazardScoringService = hazardScoringService;
        this.multiHazardService = multiHazardService;
        this.config = config;
        this.settlementEngine = settlementEngine;
    }

    // =========================================================================
    // 1. HAZARD EVENT SETTLEMENT EXPOSURE (DFO Floods, Weather Stations)
    // =========================================================================

    /**
     * Identifies all settlements within a spatial buffer of a Stage 3 hazard observation.
     */
    public SettlementExposureAnalysisResultDto getExposedSettlementsForHazardEvent(String hazardId, Double customBufferMeters) {
        if (hazardId == null || hazardId.trim().isEmpty()) {
            throw new IllegalArgumentException("Hazard identifier cannot be null or empty");
        }

        IntegratedHazardEvent event = hazardIntegrationService.getHazardById(hazardId.trim());
        if (event == null) {
            throw new HazardNotFoundException("Hazard event not found: " + hazardId);
        }

        if (event.getLongitude() == null || event.getLatitude() == null) {
            throw new IllegalArgumentException("Hazard event has no valid geographic coordinates for settlement analysis: " + hazardId);
        }

        double bufferMeters = (customBufferMeters != null && customBufferMeters > 0.0)
                ? customBufferMeters
                : config.getDefaultHazardBufferMeters();

        // Query intersecting populated places
        List<PopulatedPlace> intersectingPlaces = populatedPlaceRepository.findPlacesWithinBufferOfPoint(
                event.getLongitude(), event.getLatitude(), bufferMeters
        );

        // Derive hazard severity score from Stage 3 event
        double hazardSeverity = (event.getSeverity() != null && event.getSeverity() > 0.0)
                ? Math.min(1.0, event.getSeverity() / 2.5) // Normalize DFO 1-2.5 severity scale to [0,1]
                : 0.6000;

        String districtName = event.getLocationName();
        if (districtName == null || districtName.trim().isEmpty()) {
            districtName = districtBoundaryRepository.findDistrictContainingPoint(event.getLongitude(), event.getLatitude())
                    .map(DistrictBoundary::getName2)
                    .orElse("Bihar Regional");
        }

        SettlementExposureAnalysisResultDto result = new SettlementExposureAnalysisResultDto();
        result.setGeographicUnit("Hazard Event Buffer: " + event.getId() + " (" + (int)(bufferMeters / 1000) + "km radius)");
        result.setHazardIdentifier(event.getId());
        result.setHazardType(event.getHazardType() != null ? event.getHazardType().name() : "FLOOD");
        result.setHazardSeverityScore(SettlementExposureEngine.round4(hazardSeverity));
        result.setTotalSettlementsEvaluated(intersectingPlaces.size());
        result.setCalculationMethod("Radial PostGIS ST_Buffer & ST_Intersects overlay with distance-decay scoring");

        double totalScoreSum = 0.0;
        List<SettlementExposureDto> settlementDtos = new ArrayList<>();
        Set<Integer> processedIds = new HashSet<>();

        for (PopulatedPlace p : intersectingPlaces) {
            if (p.getId() != null && !processedIds.add(p.getId())) {
                continue; // Prevent duplicate entries
            }

            Double sLon = null;
            Double sLat = null;
            Double distanceMeters = null;

            if (p.getGeom() != null) {
                sLon = p.getGeom().getCentroid().getX();
                sLat = p.getGeom().getCentroid().getY();
                distanceMeters = haversineDistanceMeters(event.getLatitude(), event.getLongitude(), sLat, sLon);
            }

            var scoreRes = settlementEngine.calculateSettlementScore(hazardSeverity, distanceMeters, bufferMeters);
            totalScoreSum += scoreRes.settlementExposureScore();
            result.incrementCategoryCount(scoreRes.exposureCategory());

            long pop = (p.getPopulation() != null && p.getPopulation() > 0)
                    ? p.getPopulation()
                    : config.resolveArchetypePopulation(p.getPlace(), p.getLanduse());
            boolean isEst = p.getPopulation() == null || p.getPopulation() <= 0;

            SettlementExposureDto dto = new SettlementExposureDto();
            dto.setSettlementId(p.getId());
            dto.setSettlementName(p.getName() != null ? p.getName() : "Settlement #" + p.getId() + " (" + (p.getPlace() != null ? p.getPlace() : "residential") + ")");
            dto.setSettlementType(p.getPlace() != null ? p.getPlace() : (p.getLanduse() != null ? p.getLanduse() : "residential"));
            dto.setDistrictName(p.getAdm2Name() != null ? p.getAdm2Name() : districtName);
            dto.setState("Bihar");
            dto.setLongitude(sLon);
            dto.setLatitude(sLat);
            dto.setTotalPopulation(pop);
            dto.setEstimatedPopulation(isEst);
            dto.setPopulationProvenance(isEst ? PopulationDataSource.SETTLEMENT_ARCHETYPE : PopulationDataSource.DIRECT_CENSUS_OSM);
            dto.setHazardIdentifier(event.getId());
            dto.setHazardType(event.getHazardType() != null ? event.getHazardType().name() : "FLOOD");
            dto.setHazardSeverityScore(SettlementExposureEngine.round4(hazardSeverity));
            dto.setDistanceMeters(distanceMeters);
            dto.setSettlementExposureScore(scoreRes.settlementExposureScore());
            dto.setExposureCategory(scoreRes.exposureCategory());
            dto.setExplanation(String.format("%s (%s) is %.1f km from %s hazard center with exposure score %.4f (%s)",
                    dto.getSettlementName(), dto.getSettlementType(),
                    distanceMeters != null ? distanceMeters / 1000.0 : 0.0,
                    event.getId(), dto.getSettlementExposureScore(), dto.getExposureCategory().getDisplayName()));

            settlementDtos.add(dto);
        }

        result.setExposedSettlements(settlementDtos);
        result.setExposedSettlementsCount(settlementDtos.size());
        result.setSettlementExposurePercentage(settlementDtos.isEmpty() ? 0.0 : 100.0);
        result.setAverageSettlementExposureScore(settlementDtos.isEmpty() ? 0.0 : SettlementExposureEngine.round4(totalScoreSum / settlementDtos.size()));

        result.setExplanation(String.format("Hazard event %s intersects %d populated settlements within %.1f km buffer. " +
                "Average settlement exposure score is %.4f.",
                event.getId(), settlementDtos.size(), bufferMeters / 1000.0, result.getAverageSettlementExposureScore()));

        result.addMetadata("bufferRadiusMeters", bufferMeters);
        result.addMetadata("eventCentroidLongitude", event.getLongitude());
        result.addMetadata("eventCentroidLatitude", event.getLatitude());

        return result;
    }

    // =========================================================================
    // 2. DISTRICT-LEVEL SETTLEMENT EXPOSURE
    // =========================================================================

    /**
     * Evaluates settlement exposure across all settlements residing inside an administrative district.
     */
    public DistrictSettlementExposureSummaryDto getDistrictSettlementExposure(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }

        String targetDistrict = districtName.trim();
        DistrictBoundary boundary = districtBoundaryRepository.findByName2IgnoreCase(targetDistrict)
                .orElseThrow(() -> new HazardNotFoundException("Administrative district not found: " + targetDistrict));

        List<PopulatedPlace> districtPlaces = populatedPlaceRepository.findPlacesInDistrictSpatial(boundary.getName2());

        // Stage 3 Hazard Intelligence for district
        List<MultiHazardObservation> multiHazards = multiHazardService.getMultiHazardObservationsInDistrict(boundary.getName2(), null, 100);
        double peakHazardScore = 0.0;
        String dominantHazard = "NONE";

        if (!multiHazards.isEmpty()) {
            MultiHazardObservation peak = multiHazards.stream()
                    .max(Comparator.comparingDouble(m -> m.getMultiHazardIndex() != null ? m.getMultiHazardIndex() : 0.0))
                    .orElse(null);
            if (peak != null && peak.getMultiHazardIndex() != null) {
                peakHazardScore = peak.getMultiHazardIndex();
                dominantHazard = peak.getDominantHazard() != null ? peak.getDominantHazard().name() : "MULTI_HAZARD";
            }
        } else {
            List<HazardScoreDto> floodScores = hazardScoringService.getHazardScoresByType(HazardType.FLOOD, null, 100).stream()
                    .filter(s -> s.getAssociatedDistrict() != null && s.getAssociatedDistrict().equalsIgnoreCase(boundary.getName2()))
                    .toList();
            if (!floodScores.isEmpty()) {
                peakHazardScore = floodScores.stream().mapToDouble(HazardScoreDto::getHazardScore).max().orElse(0.0);
                dominantHazard = HazardType.FLOOD.name();
            }
        }

        DistrictSettlementExposureSummaryDto summary = new DistrictSettlementExposureSummaryDto();
        summary.setDistrictId(boundary.getId());
        summary.setDistrictName(boundary.getName2());
        summary.setState(boundary.getName1());
        summary.setGid2(boundary.getGid2());
        summary.setTotalSettlementsEvaluated(districtPlaces.size());
        summary.setDominantHazard(dominantHazard);
        summary.setPeakHazardIndex(SettlementExposureEngine.round4(peakHazardScore));

        List<SettlementExposureDto> settlementDtos = new ArrayList<>();
        Set<Integer> processedIds = new HashSet<>();

        for (PopulatedPlace p : districtPlaces) {
            if (p.getId() != null && !processedIds.add(p.getId())) {
                continue;
            }

            Double sLon = null;
            Double sLat = null;
            if (p.getGeom() != null) {
                sLon = p.getGeom().getCentroid().getX();
                sLat = p.getGeom().getCentroid().getY();
            }

            var scoreRes = settlementEngine.calculateSettlementScore(peakHazardScore, 0.0, 0.0);
            summary.incrementCategoryCount(scoreRes.exposureCategory());

            long pop = (p.getPopulation() != null && p.getPopulation() > 0)
                    ? p.getPopulation()
                    : config.resolveArchetypePopulation(p.getPlace(), p.getLanduse());
            boolean isEst = p.getPopulation() == null || p.getPopulation() <= 0;

            SettlementExposureDto dto = new SettlementExposureDto();
            dto.setSettlementId(p.getId());
            dto.setSettlementName(p.getName() != null ? p.getName() : "Settlement #" + p.getId() + " (" + (p.getPlace() != null ? p.getPlace() : "residential") + ")");
            dto.setSettlementType(p.getPlace() != null ? p.getPlace() : (p.getLanduse() != null ? p.getLanduse() : "residential"));
            dto.setDistrictName(boundary.getName2());
            dto.setState("Bihar");
            dto.setLongitude(sLon);
            dto.setLatitude(sLat);
            dto.setTotalPopulation(pop);
            dto.setEstimatedPopulation(isEst);
            dto.setPopulationProvenance(isEst ? PopulationDataSource.SETTLEMENT_ARCHETYPE : PopulationDataSource.DIRECT_CENSUS_OSM);
            dto.setHazardIdentifier("DISTRICT-HAZARD-" + boundary.getName2().toUpperCase());
            dto.setHazardType(dominantHazard);
            dto.setHazardSeverityScore(SettlementExposureEngine.round4(peakHazardScore));
            dto.setSettlementExposureScore(scoreRes.settlementExposureScore());
            dto.setExposureCategory(scoreRes.exposureCategory());
            dto.setExplanation(String.format("Settlement %s in district %s has exposure score %.4f (%s)",
                    dto.getSettlementName(), boundary.getName2(), dto.getSettlementExposureScore(), dto.getExposureCategory().getDisplayName()));

            settlementDtos.add(dto);
        }

        summary.setSettlements(settlementDtos);
        summary.setExposedSettlementsCount(peakHazardScore > 0 ? settlementDtos.size() : 0);
        summary.setSettlementExposurePercentage(districtPlaces.isEmpty() ? 0.0 : (peakHazardScore > 0 ? 100.0 : 0.0));

        return summary;
    }

    // =========================================================================
    // 3. ALL DISTRICTS SETTLEMENT EXPOSURE SUMMARY
    // =========================================================================

    /**
     * Evaluates settlement exposure summaries across all 38 districts of Bihar.
     */
    public List<DistrictSettlementExposureSummaryDto> getAllDistrictsSettlementExposureSummary() {
        List<DistrictBoundary> districts = districtBoundaryRepository.findAllByOrderByName2Asc();
        List<DistrictSettlementExposureSummaryDto> resultList = new ArrayList<>();

        for (DistrictBoundary db : districts) {
            try {
                DistrictSettlementExposureSummaryDto summary = getDistrictSettlementExposure(db.getName2());
                resultList.add(summary);
            } catch (Exception e) {
                log.warn("Error evaluating settlement exposure for district {}: {}", db.getName2(), e.getMessage());
            }
        }

        return resultList;
    }

    // =========================================================================
    // 4. CUSTOM GEOMETRY SETTLEMENT EXPOSURE (WKT Polygon)
    // =========================================================================

    /**
     * Evaluates which settlements intersect an arbitrary custom WKT polygon hazard area.
     */
    public SettlementExposureAnalysisResultDto analyzeSettlementsForCustomGeometry(GeometryExposureRequestDto request) {
        if (request == null || request.getWktGeometry() == null || request.getWktGeometry().trim().isEmpty()) {
            throw new IllegalArgumentException("WKT geometry string cannot be null or empty");
        }

        String wkt = request.getWktGeometry().trim();
        if (!wkt.toUpperCase().startsWith("POLYGON") && !wkt.toUpperCase().startsWith("MULTIPOLYGON")) {
            throw new IllegalArgumentException("Provided geometry must be a POLYGON or MULTIPOLYGON in WKT format");
        }

        List<PopulatedPlace> intersectingPlaces = populatedPlaceRepository.findPlacesIntersectingGeometryWkt(wkt);

        double hazardSeverity = 0.7500; // Standard severe assumption for custom polygon zone unless specified

        SettlementExposureAnalysisResultDto result = new SettlementExposureAnalysisResultDto();
        result.setGeographicUnit("Custom Geometry Impact Zone");
        result.setHazardIdentifier(request.getHazardIdentifier() != null ? request.getHazardIdentifier() : "CUSTOM-ZONE");
        result.setHazardType(request.getHazardType() != null ? request.getHazardType() : "CUSTOM_HAZARD");
        result.setHazardSeverityScore(hazardSeverity);
        result.setTotalSettlementsEvaluated(intersectingPlaces.size());
        result.setCalculationMethod("Direct PostGIS ST_Intersects overlay with custom WKT polygon");

        double totalScoreSum = 0.0;
        List<SettlementExposureDto> settlementDtos = new ArrayList<>();
        Set<Integer> processedIds = new HashSet<>();

        for (PopulatedPlace p : intersectingPlaces) {
            if (p.getId() != null && !processedIds.add(p.getId())) {
                continue;
            }

            Double sLon = null;
            Double sLat = null;
            if (p.getGeom() != null) {
                sLon = p.getGeom().getCentroid().getX();
                sLat = p.getGeom().getCentroid().getY();
            }

            var scoreRes = settlementEngine.calculateSettlementScore(hazardSeverity, 0.0, 0.0);
            totalScoreSum += scoreRes.settlementExposureScore();
            result.incrementCategoryCount(scoreRes.exposureCategory());

            long pop = (p.getPopulation() != null && p.getPopulation() > 0)
                    ? p.getPopulation()
                    : config.resolveArchetypePopulation(p.getPlace(), p.getLanduse());
            boolean isEst = p.getPopulation() == null || p.getPopulation() <= 0;

            SettlementExposureDto dto = new SettlementExposureDto();
            dto.setSettlementId(p.getId());
            dto.setSettlementName(p.getName() != null ? p.getName() : "Settlement #" + p.getId() + " (" + (p.getPlace() != null ? p.getPlace() : "residential") + ")");
            dto.setSettlementType(p.getPlace() != null ? p.getPlace() : (p.getLanduse() != null ? p.getLanduse() : "residential"));
            dto.setDistrictName(p.getAdm2Name() != null ? p.getAdm2Name() : request.getAssociatedDistrict());
            dto.setState("Bihar");
            dto.setLongitude(sLon);
            dto.setLatitude(sLat);
            dto.setTotalPopulation(pop);
            dto.setEstimatedPopulation(isEst);
            dto.setPopulationProvenance(isEst ? PopulationDataSource.SETTLEMENT_ARCHETYPE : PopulationDataSource.DIRECT_CENSUS_OSM);
            dto.setHazardIdentifier(result.getHazardIdentifier());
            dto.setHazardType(result.getHazardType());
            dto.setHazardSeverityScore(hazardSeverity);
            dto.setSettlementExposureScore(scoreRes.settlementExposureScore());
            dto.setExposureCategory(scoreRes.exposureCategory());
            dto.setExplanation(String.format("Settlement %s intersects custom hazard footprint with exposure score %.4f (%s)",
                    dto.getSettlementName(), dto.getSettlementExposureScore(), dto.getExposureCategory().getDisplayName()));

            settlementDtos.add(dto);
        }

        result.setExposedSettlements(settlementDtos);
        result.setExposedSettlementsCount(settlementDtos.size());
        result.setSettlementExposurePercentage(settlementDtos.isEmpty() ? 0.0 : 100.0);
        result.setAverageSettlementExposureScore(settlementDtos.isEmpty() ? 0.0 : SettlementExposureEngine.round4(totalScoreSum / settlementDtos.size()));

        result.setExplanation(String.format("Custom hazard polygon intersects %d populated settlement footprints with average exposure score %.4f.",
                settlementDtos.size(), result.getAverageSettlementExposureScore()));

        return result;
    }

    // =========================================================================
    // 5. GEOJSON SETTLEMENT POINT EXPORT
    // =========================================================================

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection containing Point features for each exposed settlement.
     */
    public GeoJsonFeatureCollectionDto generateSettlementExposureGeoJson(String district, String hazardEventId) {
        List<SettlementExposureDto> settlements;

        if (hazardEventId != null && !hazardEventId.trim().isEmpty()) {
            SettlementExposureAnalysisResultDto res = getExposedSettlementsForHazardEvent(hazardEventId, null);
            settlements = res.getExposedSettlements();
        } else if (district != null && !district.trim().isEmpty()) {
            DistrictSettlementExposureSummaryDto summary = getDistrictSettlementExposure(district);
            settlements = summary.getSettlements();
        } else {
            // Default to top 200 exposed settlements across Bihar
            DistrictSettlementExposureSummaryDto summary = getDistrictSettlementExposure("Patna");
            settlements = summary.getSettlements();
        }

        List<GeoJsonFeatureDto> features = new ArrayList<>();
        for (SettlementExposureDto s : settlements) {
            if (s.getLongitude() == null || s.getLatitude() == null) continue;

            GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(s.getLongitude(), s.getLatitude());
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("settlementId", s.getSettlementId());
            props.put("settlementName", s.getSettlementName());
            props.put("settlementType", s.getSettlementType());
            props.put("districtName", s.getDistrictName());
            props.put("population", s.getTotalPopulation());
            props.put("isEstimatedPopulation", s.isEstimatedPopulation());
            props.put("hazardIdentifier", s.getHazardIdentifier());
            props.put("hazardType", s.getHazardType());
            props.put("distanceMeters", s.getDistanceMeters());
            props.put("settlementExposureScore", s.getSettlementExposureScore());
            props.put("exposureCategory", s.getExposureCategory() != null ? s.getExposureCategory().name() : ExposureCategory.LOW.name());
            props.put("categoryDisplayName", s.getExposureCategory() != null ? s.getExposureCategory().getDisplayName() : "Low");
            props.put("colorHex", s.getExposureCategory() != null ? s.getExposureCategory().getColorHex() : "#4CAF50");
            props.put("layerId", "SETTLEMENT_EXPOSURE");

            features.add(new GeoJsonFeatureDto("SETTLEMENT-" + s.getSettlementId(), geom, props));
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    // =========================================================================
    // UTILITIES
    // =========================================================================

    /**
     * Computes the Haversine distance in meters between two WGS 84 coordinates.
     */
    public static double haversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371000.0; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return Math.round((r * c) * 10.0) / 10.0;
    }
}
