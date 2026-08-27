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
import com.hazard.repository.population.OsmSettlementRepository;
import com.hazard.repository.population.PopulatedPlaceRepository;
import com.hazard.service.hazard.HazardIntegrationService;
import com.hazard.service.multihazard.MultiHazardService;
import com.hazard.service.scoring.HazardScoringService;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Master Service for Stage 4.1 Population Exposure.
 *
 * Coordinates spatial overlays between Stage 3 hazard footprints and population datasets
 * (populated places, settlement nodes, residential footprints), calculating exposed population,
 * exposure ratios, normalized scores [0.0000, 1.0000], and categorical exposure tiers.
 */
@Service
@Transactional(readOnly = true)
public class PopulationExposureService {

    private static final Logger log = LoggerFactory.getLogger(PopulationExposureService.class);

    private final PopulatedPlaceRepository populatedPlaceRepository;
    private final OsmSettlementRepository osmSettlementRepository;
    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final HazardIntegrationService hazardIntegrationService;
    private final HazardScoringService hazardScoringService;
    private final MultiHazardService multiHazardService;
    private final PopulationExposureConfig config;
    private final PopulationExposureEngine engine;

    public PopulationExposureService(PopulatedPlaceRepository populatedPlaceRepository,
                                     OsmSettlementRepository osmSettlementRepository,
                                     DistrictBoundaryRepository districtBoundaryRepository,
                                     HazardIntegrationService hazardIntegrationService,
                                     HazardScoringService hazardScoringService,
                                     MultiHazardService multiHazardService,
                                     PopulationExposureConfig config,
                                     PopulationExposureEngine engine) {
        this.populatedPlaceRepository = populatedPlaceRepository;
        this.osmSettlementRepository = osmSettlementRepository;
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.hazardIntegrationService = hazardIntegrationService;
        this.hazardScoringService = hazardScoringService;
        this.multiHazardService = multiHazardService;
        this.config = config;
        this.engine = engine;
    }

    // =========================================================================
    // 1. DISTRICT-LEVEL POPULATION EXPOSURE
    // =========================================================================

    /**
     * Evaluates population exposure for a specific Bihar administrative district based on
     * its total settlement population and active Stage 3 hazard intensity / footprint.
     */
    public PopulationExposureResultDto analyzeDistrictPopulationExposure(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }

        String targetDistrict = districtName.trim();
        DistrictBoundary boundary = districtBoundaryRepository.findByName2IgnoreCase(targetDistrict)
                .orElseThrow(() -> new HazardNotFoundException("Administrative district not found: " + targetDistrict));

        // 1. Retrieve all populated places in the district
        List<PopulatedPlace> districtPlaces = populatedPlaceRepository.findPlacesInDistrictSpatial(boundary.getName2());

        // 2. Compute total district baseline population
        long explicitPopSum = 0L;
        long estimatedPopSum = 0L;
        for (PopulatedPlace p : districtPlaces) {
            if (p.getPopulation() != null && p.getPopulation() > 0) {
                explicitPopSum += p.getPopulation();
            } else {
                estimatedPopSum += config.resolveArchetypePopulation(p.getPlace(), p.getLanduse());
            }
        }
        long totalDistrictPop = explicitPopSum + estimatedPopSum;
        if (totalDistrictPop == 0) {
            totalDistrictPop = 50000L; // Safety non-zero baseline if district has unmapped places
        }

        // 3. Query active Stage 3 hazard intelligence for the district
        List<MultiHazardObservation> multiHazards = multiHazardService.getMultiHazardObservationsInDistrict(boundary.getName2(), null, 100);
        List<HazardScoreDto> floodScores = hazardScoringService.getHazardScoresByType(HazardType.FLOOD, null, 100).stream()
                .filter(s -> s.getAssociatedDistrict() != null && s.getAssociatedDistrict().equalsIgnoreCase(boundary.getName2()))
                .toList();
        List<HazardScoreDto> rainScores = hazardScoringService.getHazardScoresByType(HazardType.EXTREME_RAINFALL, null, 100).stream()
                .filter(s -> s.getAssociatedDistrict() != null && s.getAssociatedDistrict().equalsIgnoreCase(boundary.getName2()))
                .toList();

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
        } else if (!floodScores.isEmpty()) {
            peakHazardScore = floodScores.stream().mapToDouble(HazardScoreDto::getHazardScore).max().orElse(0.0);
            dominantHazard = HazardType.FLOOD.name();
        } else if (!rainScores.isEmpty()) {
            peakHazardScore = rainScores.stream().mapToDouble(HazardScoreDto::getHazardScore).max().orElse(0.0);
            dominantHazard = HazardType.EXTREME_RAINFALL.name();
        }

        // 4. Calculate exposed population proportional to hazard intensity & spatial presence
        long exposedPop = 0L;
        List<SettlementExposureSummaryDto> settlementSummaries = new ArrayList<>();

        if (peakHazardScore > 0.0) {
            // High hazard ratio directly scales exposure across populated places
            double exposureFraction = Math.min(1.0, Math.max(0.0, peakHazardScore));
            exposedPop = Math.round(totalDistrictPop * exposureFraction);

            // Populate top affected settlements
            for (PopulatedPlace p : districtPlaces.stream().limit(15).toList()) {
                long pPop = (p.getPopulation() != null && p.getPopulation() > 0)
                        ? p.getPopulation()
                        : config.resolveArchetypePopulation(p.getPlace(), p.getLanduse());
                boolean isEst = p.getPopulation() == null || p.getPopulation() <= 0;

                Double lon = null;
                Double lat = null;
                if (p.getGeom() != null) {
                    lon = p.getGeom().getCentroid().getX();
                    lat = p.getGeom().getCentroid().getY();
                }

                settlementSummaries.add(new SettlementExposureSummaryDto(
                        p.getId(),
                        p.getName() != null ? p.getName() : "Unnamed Settlement (" + (p.getPlace() != null ? p.getPlace() : "residential") + ")",
                        p.getPlace() != null ? p.getPlace() : (p.getLanduse() != null ? p.getLanduse() : "residential"),
                        boundary.getName2(),
                        pPop,
                        isEst,
                        lon,
                        lat,
                        null
                ));
            }
        }

        // 5. Compute metrics via engine
        var calc = engine.calculateExposure(totalDistrictPop, exposedPop);

        PopulationExposureResultDto result = new PopulationExposureResultDto();
        result.setGeographicUnit("District: " + boundary.getName2());
        result.setDistrictName(boundary.getName2());
        result.setHazardIdentifier("DISTRICT-HAZARD-" + boundary.getName2().toUpperCase());
        result.setHazardType(dominantHazard);
        result.setTotalPopulation(calc.totalPopulation());
        result.setExposedPopulation(calc.exposedPopulation());
        result.setUnexposedPopulation(calc.unexposedPopulation());
        result.setExposurePercentage(calc.exposurePercentage());
        result.setExposureScore(calc.exposureScore());
        result.setExposureCategory(calc.exposureCategory());

        result.setIntersectingSettlementsCount(districtPlaces.size());
        result.setExplicitPopulationCount(explicitPopSum);
        result.setEstimatedPopulationCount(estimatedPopSum);
        result.setDataSourceProvenance(explicitPopSum > 0 ? PopulationDataSource.HYBRID_COMPOSITE : PopulationDataSource.SETTLEMENT_ARCHETYPE);

        result.setCalculationMethod("District-scale spatial aggregation with Stage 3 Multi-Hazard intensity weighting");
        result.setExplanation(String.format(
                "District %s has a total baseline population of %,d across %d settlements. " +
                "With an active peak hazard index of %.4f (%s), %,d persons (%.2f%%) are classified under %s population exposure.",
                boundary.getName2(), calc.totalPopulation(), districtPlaces.size(),
                peakHazardScore, dominantHazard, calc.exposedPopulation(), calc.exposurePercentage(), calc.exposureCategory().getDisplayName()
        ));

        result.setAffectedSettlementsSummary(settlementSummaries);
        result.addMetadata("districtId", boundary.getId());
        result.addMetadata("gid2", boundary.getGid2());
        result.addMetadata("peakHazardIndex", peakHazardScore);
        result.addMetadata("dominantHazard", dominantHazard);

        return result;
    }

    // =========================================================================
    // 2. STAGE 3 HAZARD EVENT POPULATION EXPOSURE
    // =========================================================================

    /**
     * Evaluates population exposure for a specific Stage 3 hazard observation (e.g. DFO-3, WEAT-123)
     * by constructing a spatial buffer around the event centroid and intersecting populated places.
     */
    public PopulationExposureResultDto analyzeHazardEventExposure(String hazardId, Double customBufferMeters) {
        if (hazardId == null || hazardId.trim().isEmpty()) {
            throw new IllegalArgumentException("Hazard identifier cannot be null or empty");
        }

        IntegratedHazardEvent event = hazardIntegrationService.getHazardById(hazardId.trim());
        if (event == null) {
            throw new HazardNotFoundException("Hazard event not found: " + hazardId);
        }

        if (event.getLongitude() == null || event.getLatitude() == null) {
            throw new IllegalArgumentException("Hazard event has no valid coordinates for spatial exposure analysis: " + hazardId);
        }

        double bufferMeters = (customBufferMeters != null && customBufferMeters > 0.0)
                ? customBufferMeters
                : config.getDefaultHazardBufferMeters();

        // 1. Query intersecting populated places within buffer
        List<PopulatedPlace> intersectingPlaces = populatedPlaceRepository.findPlacesWithinBufferOfPoint(
                event.getLongitude(), event.getLatitude(), bufferMeters
        );

        // 2. Calculate exposed population inside the buffer
        long explicitExposed = 0L;
        long estimatedExposed = 0L;
        List<SettlementExposureSummaryDto> settlementSummaries = new ArrayList<>();

        // 3. Resolve baseline total population from the associated district or a regional reference
        String districtName = event.getLocationName();
        if (districtName == null || districtName.trim().isEmpty()) {
            districtName = districtBoundaryRepository.findDistrictContainingPoint(event.getLongitude(), event.getLatitude())
                    .map(DistrictBoundary::getName2)
                    .orElse(null);
        }

        for (PopulatedPlace p : intersectingPlaces) {
            long pPop = (p.getPopulation() != null && p.getPopulation() > 0)
                    ? p.getPopulation()
                    : config.resolveArchetypePopulation(p.getPlace(), p.getLanduse());
            boolean isEst = p.getPopulation() == null || p.getPopulation() <= 0;

            if (isEst) {
                estimatedExposed += pPop;
            } else {
                explicitExposed += pPop;
            }

            Double lon = null;
            Double lat = null;
            if (p.getGeom() != null) {
                lon = p.getGeom().getCentroid().getX();
                lat = p.getGeom().getCentroid().getY();
            }

            settlementSummaries.add(new SettlementExposureSummaryDto(
                    p.getId(),
                    p.getName() != null ? p.getName() : "Unnamed Settlement (" + (p.getPlace() != null ? p.getPlace() : "residential") + ")",
                    p.getPlace() != null ? p.getPlace() : (p.getLanduse() != null ? p.getLanduse() : "residential"),
                    p.getAdm2Name() != null ? p.getAdm2Name() : districtName,
                    pPop,
                    isEst,
                    lon,
                    lat,
                    null
            ));
        }
        long totalExposed = explicitExposed + estimatedExposed;

        long baselineDistrictPop = 0L;
        if (districtName != null && !districtName.trim().isEmpty()) {
            List<PopulatedPlace> districtPlaces = populatedPlaceRepository.findPlacesInDistrictSpatial(districtName);
            for (PopulatedPlace dp : districtPlaces) {
                baselineDistrictPop += (dp.getPopulation() != null && dp.getPopulation() > 0)
                        ? dp.getPopulation()
                        : config.resolveArchetypePopulation(dp.getPlace(), dp.getLanduse());
            }
        }
        if (baselineDistrictPop <= totalExposed) {
            baselineDistrictPop = Math.max(totalExposed * 2L, 50000L);
        }

        // 4. Calculate metrics
        var calc = engine.calculateExposure(baselineDistrictPop, totalExposed);

        PopulationExposureResultDto result = new PopulationExposureResultDto();
        result.setGeographicUnit("Hazard Event Buffer: " + event.getId() + " (" + (int)(bufferMeters / 1000) + "km radius)");
        result.setDistrictName(districtName);
        result.setHazardIdentifier(event.getId());
        result.setHazardType(event.getHazardType() != null ? event.getHazardType().name() : "HAZARD_EVENT");
        result.setTotalPopulation(calc.totalPopulation());
        result.setExposedPopulation(calc.exposedPopulation());
        result.setUnexposedPopulation(calc.unexposedPopulation());
        result.setExposurePercentage(calc.exposurePercentage());
        result.setExposureScore(calc.exposureScore());
        result.setExposureCategory(calc.exposureCategory());

        result.setIntersectingSettlementsCount(intersectingPlaces.size());
        result.setExplicitPopulationCount(explicitExposed);
        result.setEstimatedPopulationCount(estimatedExposed);
        result.setDataSourceProvenance(explicitExposed > 0 ? PopulationDataSource.HYBRID_COMPOSITE : PopulationDataSource.SETTLEMENT_ARCHETYPE);

        result.setCalculationMethod("Radial PostGIS ST_Buffer & ST_Intersects spatial overlay in EPSG:4326");
        result.setExplanation(String.format(
                "Hazard event %s located at [%.4f, %.4f] within a %.1f km impact radius intersects %d populated settlements, " +
                "exposing an estimated %,d persons (%.2f%% of district baseline) classified as %s exposure.",
                event.getId(), event.getLongitude(), event.getLatitude(),
                bufferMeters / 1000.0, intersectingPlaces.size(), calc.exposedPopulation(),
                calc.exposurePercentage(), calc.exposureCategory().getDisplayName()
        ));

        result.setAffectedSettlementsSummary(settlementSummaries);
        result.addMetadata("eventCentroidLongitude", event.getLongitude());
        result.addMetadata("eventCentroidLatitude", event.getLatitude());
        result.addMetadata("bufferRadiusMeters", bufferMeters);
        result.addMetadata("eventDataSource", event.getDataSource());

        return result;
    }

    // =========================================================================
    // 3. CUSTOM GEOMETRY POPULATION EXPOSURE (WKT / GeoJSON)
    // =========================================================================

    /**
     * Evaluates population exposure for any arbitrary hazard polygon supplied as WKT.
     */
    public PopulationExposureResultDto analyzeCustomGeometryExposure(GeometryExposureRequestDto request) {
        if (request == null || request.getWktGeometry() == null || request.getWktGeometry().trim().isEmpty()) {
            throw new IllegalArgumentException("WKT geometry string cannot be null or empty");
        }

        String wkt = request.getWktGeometry().trim();
        if (!wkt.toUpperCase().startsWith("POLYGON") && !wkt.toUpperCase().startsWith("MULTIPOLYGON")) {
            throw new IllegalArgumentException("Provided geometry must be a POLYGON or MULTIPOLYGON in WKT format");
        }

        // 1. Query intersecting populated places
        List<PopulatedPlace> intersectingPlaces = populatedPlaceRepository.findPlacesIntersectingGeometryWkt(wkt);

        long explicitExposed = 0L;
        long estimatedExposed = 0L;
        List<SettlementExposureSummaryDto> settlementSummaries = new ArrayList<>();

        for (PopulatedPlace p : intersectingPlaces) {
            long pPop = (p.getPopulation() != null && p.getPopulation() > 0)
                    ? p.getPopulation()
                    : config.resolveArchetypePopulation(p.getPlace(), p.getLanduse());
            boolean isEst = p.getPopulation() == null || p.getPopulation() <= 0;

            if (isEst) {
                estimatedExposed += pPop;
            } else {
                explicitExposed += pPop;
            }

            Double lon = null;
            Double lat = null;
            if (p.getGeom() != null) {
                lon = p.getGeom().getCentroid().getX();
                lat = p.getGeom().getCentroid().getY();
            }

            settlementSummaries.add(new SettlementExposureSummaryDto(
                    p.getId(),
                    p.getName() != null ? p.getName() : "Unnamed Settlement (" + (p.getPlace() != null ? p.getPlace() : "residential") + ")",
                    p.getPlace() != null ? p.getPlace() : (p.getLanduse() != null ? p.getLanduse() : "residential"),
                    p.getAdm2Name() != null ? p.getAdm2Name() : request.getAssociatedDistrict(),
                    pPop,
                    isEst,
                    lon,
                    lat,
                    null
            ));
        }
        long totalExposed = explicitExposed + estimatedExposed;

        // 2. Baseline population
        long totalBaselinePop = 0L;
        if (request.getAssociatedDistrict() != null && !request.getAssociatedDistrict().trim().isEmpty()) {
            List<PopulatedPlace> districtPlaces = populatedPlaceRepository.findPlacesInDistrictSpatial(request.getAssociatedDistrict().trim());
            for (PopulatedPlace dp : districtPlaces) {
                totalBaselinePop += (dp.getPopulation() != null && dp.getPopulation() > 0)
                        ? dp.getPopulation()
                        : config.resolveArchetypePopulation(dp.getPlace(), dp.getLanduse());
            }
        }
        if (totalBaselinePop <= totalExposed) {
            totalBaselinePop = Math.max(totalExposed * 2L, 50000L);
        }

        var calc = engine.calculateExposure(totalBaselinePop, totalExposed);

        PopulationExposureResultDto result = new PopulationExposureResultDto();
        result.setGeographicUnit("Custom Geometry Hazard Footprint");
        result.setDistrictName(request.getAssociatedDistrict());
        result.setHazardIdentifier(request.getHazardIdentifier() != null ? request.getHazardIdentifier() : "CUSTOM-GEOMETRY");
        result.setHazardType(request.getHazardType() != null ? request.getHazardType() : "CUSTOM_HAZARD");
        result.setTotalPopulation(calc.totalPopulation());
        result.setExposedPopulation(calc.exposedPopulation());
        result.setUnexposedPopulation(calc.unexposedPopulation());
        result.setExposurePercentage(calc.exposurePercentage());
        result.setExposureScore(calc.exposureScore());
        result.setExposureCategory(calc.exposureCategory());

        result.setIntersectingSettlementsCount(intersectingPlaces.size());
        result.setExplicitPopulationCount(explicitExposed);
        result.setEstimatedPopulationCount(estimatedExposed);
        result.setDataSourceProvenance(explicitExposed > 0 ? PopulationDataSource.HYBRID_COMPOSITE : PopulationDataSource.SETTLEMENT_ARCHETYPE);

        result.setCalculationMethod("Direct PostGIS ST_Intersects overlay between custom WKT polygon and population layer");
        result.setExplanation(String.format(
                "Custom hazard geometry intersects %d populated settlement footprints, exposing an estimated %,d persons (%.2f%%) classified as %s exposure.",
                intersectingPlaces.size(), calc.exposedPopulation(), calc.exposurePercentage(), calc.exposureCategory().getDisplayName()
        ));

        result.setAffectedSettlementsSummary(settlementSummaries);
        result.addMetadata("wktGeometryPreview", wkt.length() > 60 ? wkt.substring(0, 60) + "..." : wkt);

        return result;
    }

    // =========================================================================
    // 4. ALL DISTRICTS POPULATION EXPOSURE AGGREGATION
    // =========================================================================

    /**
     * Computes population exposure for all 38 districts of Bihar.
     */
    public List<DistrictPopulationExposureDto> analyzeAllDistrictsPopulationExposure() {
        List<DistrictBoundary> districts = districtBoundaryRepository.findAllByOrderByName2Asc();
        List<DistrictPopulationExposureDto> resultList = new ArrayList<>();

        for (DistrictBoundary db : districts) {
            try {
                PopulationExposureResultDto distExp = analyzeDistrictPopulationExposure(db.getName2());

                DistrictPopulationExposureDto dto = new DistrictPopulationExposureDto();
                dto.setDistrictId(db.getId());
                dto.setDistrictName(db.getName2());
                dto.setState(db.getName1());
                dto.setGid2(db.getGid2());
                dto.setTotalPopulation(distExp.getTotalPopulation());
                dto.setExposedPopulation(distExp.getExposedPopulation());
                dto.setExposurePercentage(distExp.getExposurePercentage());
                dto.setExposureScore(distExp.getExposureScore());
                dto.setExposureCategory(distExp.getExposureCategory());
                dto.setTotalSettlementsCount(distExp.getIntersectingSettlementsCount());
                dto.setExposedSettlementsCount(distExp.getAffectedSettlementsSummary().size());

                Object peakIdx = distExp.getMetadata().get("peakHazardIndex");
                if (peakIdx instanceof Number num) {
                    dto.setPeakHazardIndex(num.doubleValue());
                }
                dto.setDominantHazard(distExp.getHazardType());

                resultList.add(dto);
            } catch (Exception e) {
                log.warn("Error computing population exposure for district {}: {}", db.getName2(), e.getMessage());
            }
        }

        return resultList;
    }

    // =========================================================================
    // 5. GEOJSON VECTOR CHOROPLETH EXPORT
    // =========================================================================

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of all district boundaries
     * enriched with Population Exposure properties for direct Leaflet / Mapbox GIS rendering.
     */
    public GeoJsonFeatureCollectionDto generatePopulationExposureGeoJson(String districtFilter) {
        List<DistrictBoundary> districts = districtBoundaryRepository.findAllByOrderByName2Asc();
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (DistrictBoundary db : districts) {
            if (districtFilter != null && !districtFilter.trim().isEmpty()) {
                if (db.getName2() == null || !db.getName2().equalsIgnoreCase(districtFilter.trim())) {
                    continue;
                }
            }

            PopulationExposureResultDto exp = analyzeDistrictPopulationExposure(db.getName2());
            GeoJsonGeometryDto geom = GeoJsonGeometryDto.fromJtsGeometry(db.getGeom());

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("districtId", db.getId());
            props.put("districtName", db.getName2());
            props.put("state", db.getName1());
            props.put("gid2", db.getGid2());
            props.put("layerId", "POPULATION_EXPOSURE");

            props.put("totalPopulation", exp.getTotalPopulation());
            props.put("exposedPopulation", exp.getExposedPopulation());
            props.put("exposurePercentage", exp.getExposurePercentage());
            props.put("exposureScore", exp.getExposureScore());
            props.put("exposureCategory", exp.getExposureCategory() != null ? exp.getExposureCategory().name() : ExposureCategory.LOW.name());
            props.put("categoryDisplayName", exp.getExposureCategory() != null ? exp.getExposureCategory().getDisplayName() : "Low");
            props.put("colorHex", exp.getExposureCategory() != null ? exp.getExposureCategory().getColorHex() : "#4CAF50");
            props.put("settlementsCount", exp.getIntersectingSettlementsCount());
            props.put("dominantHazard", exp.getHazardType());

            features.add(new GeoJsonFeatureDto("POP-EXPOSURE-" + db.getId(), geom, props));
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    /**
     * Returns the active configuration parameters.
     */
    public PopulationExposureConfigDto getConfiguration() {
        return config.toDto();
    }
}
