package com.hazard.service.exposure;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;
import com.hazard.dto.exposure.GeometryExposureRequestDto;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.GeoJsonGeometryDto;
import com.hazard.dto.hazard.IntegratedHazardEvent;
import com.hazard.dto.infrastructure.DistrictInfrastructureExposureSummaryDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.infrastructure.InfrastructureExposureAnalysisResultDto;
import com.hazard.dto.infrastructure.InfrastructureExposureConfigDto;
import com.hazard.dto.multihazard.MultiHazardObservation;
import com.hazard.dto.scoring.HazardScoreDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.service.hazard.HazardIntegrationService;
import com.hazard.service.multihazard.MultiHazardService;
import com.hazard.service.scoring.HazardScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Master Domain Service for Stage 4.3 — Infrastructure Exposure.
 *
 * Identifies which critical infrastructure assets (hospitals, schools, roads, bridges,
 * emergency facilities, power nodes, dams, canals, water networks) are exposed to predicted or observed
 * hazard areas, quantifying asset exposure scores, distance decay, and categorical tiers.
 */
@Service
@Transactional(readOnly = true)
public class InfrastructureExposureService {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureExposureService.class);

    private final InfrastructureDataProvider dataProvider;
    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final HazardIntegrationService hazardIntegrationService;
    private final HazardScoringService hazardScoringService;
    private final MultiHazardService multiHazardService;
    private final PopulationExposureConfig config;
    private final InfrastructureExposureEngine engine;

    public InfrastructureExposureService(InfrastructureDataProvider dataProvider,
                                         DistrictBoundaryRepository districtBoundaryRepository,
                                         HazardIntegrationService hazardIntegrationService,
                                         HazardScoringService hazardScoringService,
                                         MultiHazardService multiHazardService,
                                         PopulationExposureConfig config,
                                         InfrastructureExposureEngine engine) {
        this.dataProvider = dataProvider;
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.hazardIntegrationService = hazardIntegrationService;
        this.hazardScoringService = hazardScoringService;
        this.multiHazardService = multiHazardService;
        this.config = config;
        this.engine = engine;
    }

    // =========================================================================
    // 1. HAZARD EVENT INFRASTRUCTURE EXPOSURE (DFO Floods, Weather Stations)
    // =========================================================================

    public InfrastructureExposureAnalysisResultDto getExposedInfrastructureForHazardEvent(String hazardId, Double customBufferMeters) {
        if (hazardId == null || hazardId.trim().isEmpty()) {
            throw new IllegalArgumentException("Hazard identifier cannot be null or empty");
        }

        IntegratedHazardEvent event = hazardIntegrationService.getHazardById(hazardId.trim());
        if (event == null) {
            throw new HazardNotFoundException("Hazard event not found: " + hazardId);
        }

        if (event.getLongitude() == null || event.getLatitude() == null) {
            throw new IllegalArgumentException("Hazard event has no valid geographic coordinates: " + hazardId);
        }

        double bufferMeters = (customBufferMeters != null && customBufferMeters > 0.0)
                ? customBufferMeters
                : config.getDefaultHazardBufferMeters();

        // 1. Water infrastructure from PostGIS
        List<InfrastructureAssetDto> waterAssets = dataProvider.getWaterInfrastructureInPointBuffer(
                event.getLongitude(), event.getLatitude(), bufferMeters
        );

        // 2. Verified regional facilities
        List<InfrastructureAssetDto> facilityAssets = dataProvider.getRegionalFacilitiesInPointBuffer(
                event.getLongitude(), event.getLatitude(), bufferMeters
        );

        List<InfrastructureAssetDto> combined = new ArrayList<>(waterAssets);
        combined.addAll(facilityAssets);

        double hazardSeverity = (event.getSeverity() != null && event.getSeverity() > 0.0)
                ? Math.min(1.0, event.getSeverity() / 2.5)
                : 0.6000;

        String districtName = event.getLocationName();
        if (districtName == null || districtName.trim().isEmpty()) {
            districtName = districtBoundaryRepository.findDistrictContainingPoint(event.getLongitude(), event.getLatitude())
                    .map(DistrictBoundary::getName2)
                    .orElse("Bihar Regional");
        }

        InfrastructureExposureAnalysisResultDto result = new InfrastructureExposureAnalysisResultDto();
        result.setGeographicUnit("Hazard Event Buffer: " + event.getId() + " (" + (int)(bufferMeters / 1000) + "km radius)");
        result.setHazardIdentifier(event.getId());
        result.setHazardType(event.getHazardType() != null ? event.getHazardType().name() : "FLOOD");
        result.setHazardSeverityScore(InfrastructureExposureEngine.round4(hazardSeverity));
        result.setTotalAssetsEvaluated(combined.size());
        result.setCalculationMethod("Radial PostGIS ST_Buffer & distance-decay with criticality multiplier");

        double scoreSum = 0.0;
        List<InfrastructureAssetDto> evaluatedAssets = new ArrayList<>();
        Set<String> processedIds = new HashSet<>();

        for (InfrastructureAssetDto asset : combined) {
            if (asset.getAssetId() != null && !processedIds.add(asset.getAssetId())) {
                continue;
            }

            Double distance = asset.getDistanceMeters();
            if (distance == null && asset.getLatitude() != null && asset.getLongitude() != null) {
                distance = SettlementExposureService.haversineDistanceMeters(event.getLatitude(), event.getLongitude(), asset.getLatitude(), asset.getLongitude());
            }

            Double lenRatio = asset.isLineInfrastructure() ? 1.0 : 1.0;
            var scoreRes = engine.calculateInfrastructureScore(hazardSeverity, distance, bufferMeters, asset.getCriticality(), lenRatio);

            scoreSum += scoreRes.infrastructureExposureScore();
            result.incrementCategoryCount(asset.getCategory());
            result.incrementSeverityCount(scoreRes.exposureCategory());
            result.incrementCriticalityCount(asset.getCriticality());

            asset.setDistrictName(asset.getDistrictName() != null ? asset.getDistrictName() : districtName);
            asset.setHazardIdentifier(event.getId());
            asset.setHazardType(result.getHazardType());
            asset.setHazardSeverityScore(InfrastructureExposureEngine.round4(hazardSeverity));
            asset.setDistanceMeters(distance);
            asset.setInfrastructureExposureScore(scoreRes.infrastructureExposureScore());
            asset.setExposureCategory(scoreRes.exposureCategory());
            asset.setExplanation(String.format("%s (%s - %s) is %.1f km from %s hazard center with exposure score %.4f (%s)",
                    asset.getAssetName(), asset.getCategory().getDisplayName(), asset.getCriticality().getDisplayName(),
                    distance != null ? distance / 1000.0 : 0.0,
                    event.getId(), asset.getInfrastructureExposureScore(), asset.getExposureCategory().getDisplayName()));

            evaluatedAssets.add(asset);
        }

        result.setExposedAssets(evaluatedAssets);
        result.setExposedAssetsCount(evaluatedAssets.size());
        result.setInfrastructureExposurePercentage(evaluatedAssets.isEmpty() ? 0.0 : 100.0);
        result.setAverageExposureScore(evaluatedAssets.isEmpty() ? 0.0 : InfrastructureExposureEngine.round4(scoreSum / evaluatedAssets.size()));

        result.setExplanation(String.format("Hazard event %s intersects %d critical infrastructure assets within %.1f km buffer. " +
                "Average infrastructure exposure score is %.4f.",
                event.getId(), evaluatedAssets.size(), bufferMeters / 1000.0, result.getAverageExposureScore()));

        result.addMetadata("bufferRadiusMeters", bufferMeters);
        result.addMetadata("eventCentroidLongitude", event.getLongitude());
        result.addMetadata("eventCentroidLatitude", event.getLatitude());

        return result;
    }

    // =========================================================================
    // 2. DISTRICT-LEVEL INFRASTRUCTURE EXPOSURE
    // =========================================================================

    public DistrictInfrastructureExposureSummaryDto getDistrictInfrastructureExposure(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            throw new IllegalArgumentException("District name cannot be null or empty");
        }

        String targetDistrict = districtName.trim();
        DistrictBoundary boundary = districtBoundaryRepository.findByName2IgnoreCase(targetDistrict)
                .orElseThrow(() -> new HazardNotFoundException("Administrative district not found: " + targetDistrict));

        List<InfrastructureAssetDto> waterAssets = dataProvider.getWaterInfrastructureInDistrict(boundary.getName2());
        List<InfrastructureAssetDto> facilityAssets = dataProvider.getRegionalFacilitiesInDistrict(boundary.getName2());

        List<InfrastructureAssetDto> combined = new ArrayList<>(waterAssets);
        combined.addAll(facilityAssets);

        // Consume Stage 3 Hazard Intelligence for district
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

        DistrictInfrastructureExposureSummaryDto summary = new DistrictInfrastructureExposureSummaryDto();
        summary.setDistrictId(boundary.getId());
        summary.setDistrictName(boundary.getName2());
        summary.setState(boundary.getName1());
        summary.setGid2(boundary.getGid2());
        summary.setTotalAssetsEvaluated(combined.size());
        summary.setDominantHazard(dominantHazard);
        summary.setPeakHazardIndex(InfrastructureExposureEngine.round4(peakHazardScore));

        List<InfrastructureAssetDto> evaluatedAssets = new ArrayList<>();
        Set<String> processedIds = new HashSet<>();

        for (InfrastructureAssetDto asset : combined) {
            if (asset.getAssetId() != null && !processedIds.add(asset.getAssetId())) {
                continue;
            }

            var scoreRes = engine.calculateInfrastructureScore(peakHazardScore, 0.0, 0.0, asset.getCriticality(), 1.0);
            summary.incrementCategoryCount(asset.getCategory());
            summary.incrementSeverityCount(scoreRes.exposureCategory());
            summary.incrementCriticalityCount(asset.getCriticality());

            asset.setDistrictName(boundary.getName2());
            asset.setHazardIdentifier("DISTRICT-HAZARD-" + boundary.getName2().toUpperCase());
            asset.setHazardType(dominantHazard);
            asset.setHazardSeverityScore(InfrastructureExposureEngine.round4(peakHazardScore));
            asset.setInfrastructureExposureScore(scoreRes.infrastructureExposureScore());
            asset.setExposureCategory(scoreRes.exposureCategory());
            asset.setExplanation(String.format("%s in %s district has exposure score %.4f (%s)",
                    asset.getAssetName(), boundary.getName2(), asset.getInfrastructureExposureScore(), asset.getExposureCategory().getDisplayName()));

            evaluatedAssets.add(asset);
        }

        summary.setExposedAssets(evaluatedAssets);
        summary.setExposedAssetsCount(peakHazardScore > 0 ? evaluatedAssets.size() : 0);
        summary.setInfrastructureExposurePercentage(combined.isEmpty() ? 0.0 : (peakHazardScore > 0 ? 100.0 : 0.0));

        return summary;
    }

    // =========================================================================
    // 3. ALL DISTRICTS INFRASTRUCTURE EXPOSURE SUMMARY
    // =========================================================================

    public List<DistrictInfrastructureExposureSummaryDto> getAllDistrictsInfrastructureExposureSummary() {
        List<DistrictBoundary> districts = districtBoundaryRepository.findAllByOrderByName2Asc();
        List<DistrictInfrastructureExposureSummaryDto> resultList = new ArrayList<>();

        for (DistrictBoundary db : districts) {
            try {
                DistrictInfrastructureExposureSummaryDto summary = getDistrictInfrastructureExposure(db.getName2());
                resultList.add(summary);
            } catch (Exception e) {
                log.warn("Error evaluating infrastructure exposure for district {}: {}", db.getName2(), e.getMessage());
            }
        }

        return resultList;
    }

    // =========================================================================
    // 4. CUSTOM GEOMETRY INFRASTRUCTURE EXPOSURE (WKT Polygon)
    // =========================================================================

    public InfrastructureExposureAnalysisResultDto analyzeInfrastructureForCustomGeometry(GeometryExposureRequestDto request) {
        if (request == null || request.getWktGeometry() == null || request.getWktGeometry().trim().isEmpty()) {
            throw new IllegalArgumentException("WKT geometry string cannot be null or empty");
        }

        String wkt = request.getWktGeometry().trim();
        if (!wkt.toUpperCase().startsWith("POLYGON") && !wkt.toUpperCase().startsWith("MULTIPOLYGON")) {
            throw new IllegalArgumentException("Provided geometry must be a POLYGON or MULTIPOLYGON in WKT format");
        }

        List<InfrastructureAssetDto> waterAssets = dataProvider.getWaterInfrastructureInWktPolygon(wkt);

        double hazardSeverity = 0.7500; // Standard severe assumption for custom polygon zone

        InfrastructureExposureAnalysisResultDto result = new InfrastructureExposureAnalysisResultDto();
        result.setGeographicUnit("Custom Geometry Impact Zone");
        result.setHazardIdentifier(request.getHazardIdentifier() != null ? request.getHazardIdentifier() : "CUSTOM-ZONE");
        result.setHazardType(request.getHazardType() != null ? request.getHazardType() : "CUSTOM_HAZARD");
        result.setHazardSeverityScore(hazardSeverity);
        result.setTotalAssetsEvaluated(waterAssets.size());
        result.setCalculationMethod("Direct PostGIS ST_Intersects overlay with custom WKT polygon");

        double scoreSum = 0.0;
        List<InfrastructureAssetDto> evaluatedAssets = new ArrayList<>();
        Set<String> processedIds = new HashSet<>();

        for (InfrastructureAssetDto asset : waterAssets) {
            if (asset.getAssetId() != null && !processedIds.add(asset.getAssetId())) {
                continue;
            }

            var scoreRes = engine.calculateInfrastructureScore(hazardSeverity, 0.0, 0.0, asset.getCriticality(), 1.0);
            scoreSum += scoreRes.infrastructureExposureScore();
            result.incrementCategoryCount(asset.getCategory());
            result.incrementSeverityCount(scoreRes.exposureCategory());
            result.incrementCriticalityCount(asset.getCriticality());

            asset.setDistrictName(asset.getDistrictName() != null ? asset.getDistrictName() : request.getAssociatedDistrict());
            asset.setHazardIdentifier(result.getHazardIdentifier());
            asset.setHazardType(result.getHazardType());
            asset.setHazardSeverityScore(hazardSeverity);
            asset.setInfrastructureExposureScore(scoreRes.infrastructureExposureScore());
            asset.setExposureCategory(scoreRes.exposureCategory());
            asset.setExplanation(String.format("%s intersects custom hazard footprint with exposure score %.4f (%s)",
                    asset.getAssetName(), asset.getInfrastructureExposureScore(), asset.getExposureCategory().getDisplayName()));

            evaluatedAssets.add(asset);
        }

        result.setExposedAssets(evaluatedAssets);
        result.setExposedAssetsCount(evaluatedAssets.size());
        result.setInfrastructureExposurePercentage(evaluatedAssets.isEmpty() ? 0.0 : 100.0);
        result.setAverageExposureScore(evaluatedAssets.isEmpty() ? 0.0 : InfrastructureExposureEngine.round4(scoreSum / evaluatedAssets.size()));

        result.setExplanation(String.format("Custom hazard polygon intersects %d infrastructure assets with average exposure score %.4f.",
                evaluatedAssets.size(), result.getAverageExposureScore()));

        return result;
    }

    // =========================================================================
    // 5. GEOJSON MULTI-GEOMETRY EXPORT
    // =========================================================================

    public GeoJsonFeatureCollectionDto generateInfrastructureExposureGeoJson(String district, String hazardEventId) {
        List<InfrastructureAssetDto> assets;

        if (hazardEventId != null && !hazardEventId.trim().isEmpty()) {
            InfrastructureExposureAnalysisResultDto res = getExposedInfrastructureForHazardEvent(hazardEventId, null);
            assets = res.getExposedAssets();
        } else if (district != null && !district.trim().isEmpty()) {
            DistrictInfrastructureExposureSummaryDto summary = getDistrictInfrastructureExposure(district);
            assets = summary.getExposedAssets();
        } else {
            DistrictInfrastructureExposureSummaryDto summary = getDistrictInfrastructureExposure("Patna");
            assets = summary.getExposedAssets();
        }

        List<GeoJsonFeatureDto> features = new ArrayList<>();
        for (InfrastructureAssetDto a : assets) {
            if (a.getLongitude() == null || a.getLatitude() == null) continue;

            // Geometry type preservation (Point representation for centroid)
            GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(a.getLongitude(), a.getLatitude());
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("assetId", a.getAssetId());
            props.put("assetName", a.getAssetName());
            props.put("category", a.getCategory() != null ? a.getCategory().name() : InfrastructureCategory.OTHER_CRITICAL.name());
            props.put("categoryDisplayName", a.getCategory() != null ? a.getCategory().getDisplayName() : "Critical Infrastructure");
            props.put("subType", a.getSubType());
            props.put("districtName", a.getDistrictName());
            props.put("criticality", a.getCriticality() != null ? a.getCriticality().name() : InfrastructureCriticality.MODERATE.name());
            props.put("criticalityDisplayName", a.getCriticality() != null ? a.getCriticality().getDisplayName() : "Moderate");
            props.put("hazardIdentifier", a.getHazardIdentifier());
            props.put("hazardType", a.getHazardType());
            props.put("distanceMeters", a.getDistanceMeters());
            props.put("infrastructureExposureScore", a.getInfrastructureExposureScore());
            props.put("exposureCategory", a.getExposureCategory() != null ? a.getExposureCategory().name() : ExposureCategory.LOW.name());
            props.put("exposureCategoryDisplayName", a.getExposureCategory() != null ? a.getExposureCategory().getDisplayName() : "Low");
            props.put("colorHex", a.getCategory() != null ? a.getCategory().getColorHex() : "#2196F3");
            props.put("layerId", "INFRASTRUCTURE_EXPOSURE");

            features.add(new GeoJsonFeatureDto("INFRA-" + a.getAssetId(), geom, props));
        }

        return new GeoJsonFeatureCollectionDto(features);
    }

    // =========================================================================
    // 6. CONFIGURATION DTO
    // =========================================================================

    public InfrastructureExposureConfigDto getInfrastructureExposureConfig() {
        InfrastructureExposureConfigDto dto = new InfrastructureExposureConfigDto();
        dto.setLowThresholdPercent(config.getLowThresholdPercent());
        dto.setModerateThresholdPercent(config.getModerateThresholdPercent());
        dto.setHighThresholdPercent(config.getHighThresholdPercent());
        dto.setDefaultHazardBufferMeters(config.getDefaultHazardBufferMeters());

        for (InfrastructureCriticality crit : InfrastructureCriticality.values()) {
            dto.getCriticalityMultipliers().put(crit.name(), crit.getWeightMultiplier());
        }
        for (InfrastructureCategory cat : InfrastructureCategory.values()) {
            dto.getDefaultCategoryCriticality().put(cat.name(), cat.getDefaultCriticality().name());
        }
        return dto;
    }
}
