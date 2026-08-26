package com.hazard.service.safesite;

import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.DistanceStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.HealthcareAccessStatus;
import com.hazard.domain.safesite.InfrastructureAccessStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.domain.safesite.TerrainStatus;
import com.hazard.domain.safesite.WaterAccessStatus;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.hazard.GeoJsonFeatureDto;
import com.hazard.dto.hazard.GeoJsonGeometryDto;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;
import com.hazard.dto.risk.RedZoneDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.service.exposure.InfrastructureDataProvider;
import com.hazard.service.risk.RedZoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stages 5.2 - 5.10 — Candidate Safe-Site Identification & Multi-Criteria Spatial Intelligence Service.
 *
 * Discovers and filters public/institutional locations that can potentially serve
 * as emergency safe sites (schools, government buildings, emergency shelters, hospitals),
 * evaluates spatial hazard safety exposure (SAFE, AT_RISK, UNKNOWN), evaluates
 * terrain slope feasibility (FAVORABLE, UNFAVORABLE, UNKNOWN), evaluates
 * geographic geodesic distance to active high-risk disaster zones (NEAR, MODERATE, FAR, UNKNOWN),
 * evaluates road accessibility proximity (NEAR, MODERATE, FAR, UNKNOWN), evaluates
 * healthcare support availability/proximity (NEAR, MODERATE, FAR, UNKNOWN), evaluates
 * useful water infrastructure availability/proximity (NEAR, MODERATE, FAR, UNKNOWN),
 * evaluates supporting institutional infrastructure availability/proximity (NEAR, MODERATE, FAR, UNKNOWN), and
 * computes explainable multi-criteria site suitability intelligence (score, classification, factor breakdown).
 *
 * Excludes hazardous and non-shelter infrastructure (power stations, bridges, waterways).
 * Site ranking belongs to Stage 5.11.
 */
@Service
@Transactional(readOnly = true)
public class CandidateSafeSiteService {

    private static final Logger log = LoggerFactory.getLogger(CandidateSafeSiteService.class);

    private final InfrastructureDataProvider dataProvider;
    private final RedZoneService redZoneService;
    private final HazardSafetyEvaluator hazardSafetyEvaluator;
    private final TerrainEvaluator terrainEvaluator;
    private final DistanceEvaluator distanceEvaluator;
    private final RoadAccessibilityEvaluator roadAccessibilityEvaluator;
    private final HealthcareEvaluator healthcareEvaluator;
    private final WaterEvaluator waterEvaluator;
    private final InfrastructureEvaluator infrastructureEvaluator;
    private final SuitabilityEvaluator suitabilityEvaluator;
    private final SafeSiteRankingEvaluator safeSiteRankingEvaluator;

    public CandidateSafeSiteService(InfrastructureDataProvider dataProvider,
                                   RedZoneService redZoneService,
                                   HazardSafetyEvaluator hazardSafetyEvaluator,
                                   TerrainEvaluator terrainEvaluator,
                                   DistanceEvaluator distanceEvaluator,
                                   RoadAccessibilityEvaluator roadAccessibilityEvaluator,
                                   HealthcareEvaluator healthcareEvaluator,
                                   WaterEvaluator waterEvaluator,
                                   InfrastructureEvaluator infrastructureEvaluator,
                                   SuitabilityEvaluator suitabilityEvaluator,
                                   SafeSiteRankingEvaluator safeSiteRankingEvaluator) {
        this.dataProvider = dataProvider;
        this.redZoneService = redZoneService;
        this.hazardSafetyEvaluator = hazardSafetyEvaluator;
        this.terrainEvaluator = terrainEvaluator;
        this.distanceEvaluator = distanceEvaluator;
        this.roadAccessibilityEvaluator = roadAccessibilityEvaluator;
        this.healthcareEvaluator = healthcareEvaluator;
        this.waterEvaluator = waterEvaluator;
        this.infrastructureEvaluator = infrastructureEvaluator;
        this.suitabilityEvaluator = suitabilityEvaluator;
        this.safeSiteRankingEvaluator = safeSiteRankingEvaluator;
    }

    /**
     * Returns all candidate safe sites across Bihar, filtering out unusable infrastructure categories,
     * evaluating spatial hazard safety, terrain feasibility, geographic distance, road accessibility,
     * healthcare availability, water accessibility, supporting infrastructure, multi-criteria suitability,
     * and deterministic hierarchical ranking for each candidate.
     */
    public List<CandidateSafeSiteDto> getAllCandidateSites() {
        List<InfrastructureAssetDto> allFacilities = dataProvider.getAllRegionalFacilities();
        List<String> activeHighRiskDistricts = distanceEvaluator.resolveActiveHighRiskDistricts();
        List<CandidateSafeSiteDto> evaluated = allFacilities.stream()
                .map(CandidateSafeSiteDto::fromInfrastructureAsset)
                .filter(Objects::nonNull)
                .peek(hazardSafetyEvaluator::evaluateHazardSafety)
                .peek(terrainEvaluator::evaluateTerrain)
                .peek(c -> distanceEvaluator.evaluateDistance(c, activeHighRiskDistricts))
                .peek(roadAccessibilityEvaluator::evaluateRoadAccessibility)
                .peek(healthcareEvaluator::evaluateHealthcareAccess)
                .peek(waterEvaluator::evaluateWaterAccess)
                .peek(infrastructureEvaluator::evaluateInfrastructureAccess)
                .peek(suitabilityEvaluator::evaluateSuitability)
                .collect(Collectors.toList());

        return safeSiteRankingEvaluator.rankCandidateSites(evaluated);
    }

    /**
     * Queries candidate safe sites with district, category, and red-zone filtering.
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly) {
        return getCandidateSites(district, category, redZoneOnly, null, null, null, null, null, null, null);
    }

    /**
     * Queries candidate safe sites with district, category, red-zone, and hazard-safety filtering.
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly, String hazardSafety) {
        return getCandidateSites(district, category, redZoneOnly, hazardSafety, null, null, null, null, null, null);
    }

    /**
     * Queries candidate safe sites with district, category, red-zone, hazard-safety, and terrain-status filtering.
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly, String hazardSafety, String terrainStatus) {
        return getCandidateSites(district, category, redZoneOnly, hazardSafety, terrainStatus, null, null, null, null, null);
    }

    /**
     * Queries candidate safe sites with Stage 5.5 multi-dimensional filtering.
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly,
                                                        String hazardSafety, String terrainStatus, String distanceStatus) {
        return getCandidateSites(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus, null, null, null, null);
    }

    /**
     * Queries candidate safe sites with Stage 5.6 multi-dimensional filtering.
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly,
                                                        String hazardSafety, String terrainStatus, String distanceStatus,
                                                        String roadAccessStatus) {
        return getCandidateSites(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus, roadAccessStatus, null, null, null);
    }

    /**
     * Queries candidate safe sites with Stage 5.7 multi-dimensional filtering.
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly,
                                                        String hazardSafety, String terrainStatus, String distanceStatus,
                                                        String roadAccessStatus, String healthcareAccessStatus) {
        return getCandidateSites(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus, roadAccessStatus, healthcareAccessStatus, null, null);
    }

    /**
     * Queries candidate safe sites with Stage 5.8 multi-dimensional filtering.
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly,
                                                        String hazardSafety, String terrainStatus, String distanceStatus,
                                                        String roadAccessStatus, String healthcareAccessStatus,
                                                        String waterAccessStatus) {
        return getCandidateSites(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus, roadAccessStatus, healthcareAccessStatus, waterAccessStatus, null);
    }

    /**
     * Queries candidate safe sites with full multi-dimensional criteria filtering (Stages 5.2 - 5.9).
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly,
                                                        String hazardSafety, String terrainStatus, String distanceStatus,
                                                        String roadAccessStatus, String healthcareAccessStatus,
                                                        String waterAccessStatus, String infrastructureAccessStatus) {
        return getCandidateSites(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus,
                roadAccessStatus, healthcareAccessStatus, waterAccessStatus, infrastructureAccessStatus, null);
    }

    /**
     * Queries candidate safe sites with full multi-dimensional criteria filtering (Stages 5.2 - 5.10).
     *
     * @param district Optional district name filter (e.g. "Sitamarhi", "Patna")
     * @param category Optional category name or alias (e.g. "EDUCATION", "GOVERNMENT", "EMERGENCY_SHELTER", "HEALTHCARE")
     * @param redZoneOnly If true, limits results to candidate sites in active Stage 5.1 Red-Zone districts
     * @param hazardSafety Optional hazard safety status filter ("SAFE", "AT_RISK", "UNKNOWN")
     * @param terrainStatus Optional terrain status filter ("FAVORABLE", "UNFAVORABLE", "UNKNOWN")
     * @param distanceStatus Optional distance status filter ("NEAR", "MODERATE", "FAR", "UNKNOWN")
     * @param roadAccessStatus Optional road accessibility status filter ("NEAR", "MODERATE", "FAR", "UNKNOWN")
     * @param healthcareAccessStatus Optional healthcare accessibility status filter ("NEAR", "MODERATE", "FAR", "UNKNOWN")
     * @param waterAccessStatus Optional water accessibility status filter ("NEAR", "MODERATE", "FAR", "UNKNOWN")
     * @param infrastructureAccessStatus Optional infrastructure accessibility status filter ("NEAR", "MODERATE", "FAR", "UNKNOWN")
     * @param suitabilityClass Optional site suitability classification filter ("HIGHLY_SUITABLE", "SUITABLE", "MARGINAL", "UNSUITABLE", "UNKNOWN")
     * @return Filtered list of candidate safe sites
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly,
                                                        String hazardSafety, String terrainStatus, String distanceStatus,
                                                        String roadAccessStatus, String healthcareAccessStatus,
                                                        String waterAccessStatus, String infrastructureAccessStatus,
                                                        String suitabilityClass) {
        List<CandidateSafeSiteDto> candidates = getAllCandidateSites();

        // 1. Filter by district if provided
        if (district != null && !district.trim().isEmpty()) {
            String targetDistrict = district.trim();
            candidates = candidates.stream()
                    .filter(c -> c.getDistrict() != null && c.getDistrict().equalsIgnoreCase(targetDistrict))
                    .collect(Collectors.toList());
        }

        // 2. Filter by category if provided
        if (category != null && !category.trim().isEmpty()) {
            CandidateSiteCategory targetCategory = CandidateSiteCategory.fromString(category);
            if (targetCategory == null) {
                throw new InvalidHazardParameterException(
                        "Invalid safe-site category: '" + category + "'. Allowed categories: EDUCATION, GOVERNMENT, EMERGENCY_SHELTER, HEALTHCARE");
            }
            candidates = candidates.stream()
                    .filter(c -> c.getCategory() == targetCategory)
                    .collect(Collectors.toList());
        }

        // 3. Filter by Red-Zone districts if requested
        if (redZoneOnly) {
            Set<String> redZoneDistricts = redZoneService.getRedZonesOnly().stream()
                    .map(RedZoneDto::getDistrictName)
                    .filter(Objects::nonNull)
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());

            candidates = candidates.stream()
                    .filter(c -> c.getDistrict() != null && redZoneDistricts.contains(c.getDistrict().toUpperCase()))
                    .collect(Collectors.toList());
        }

        // 4. Filter by Hazard Safety Status (Stage 5.3)
        if (hazardSafety != null && !hazardSafety.trim().isEmpty()) {
            HazardSafetyStatus targetStatus = HazardSafetyStatus.fromString(hazardSafety);
            if (targetStatus == null) {
                throw new InvalidHazardParameterException(
                        "Invalid hazardSafety filter: '" + hazardSafety + "'. Allowed values: SAFE, AT_RISK, UNKNOWN");
            }
            candidates = candidates.stream()
                    .filter(c -> c.getHazardSafetyStatus() == targetStatus)
                    .collect(Collectors.toList());
        }

        // 5. Filter by Terrain Status (Stage 5.4)
        if (terrainStatus != null && !terrainStatus.trim().isEmpty()) {
            TerrainStatus targetTerrain = TerrainStatus.fromString(terrainStatus);
            if (targetTerrain == null) {
                throw new InvalidHazardParameterException(
                        "Invalid terrainStatus filter: '" + terrainStatus + "'. Allowed values: FAVORABLE, UNFAVORABLE, UNKNOWN");
            }
            candidates = candidates.stream()
                    .filter(c -> c.getTerrainStatus() == targetTerrain)
                    .collect(Collectors.toList());
        }

        // 6. Filter by Distance Status (Stage 5.5)
        if (distanceStatus != null && !distanceStatus.trim().isEmpty()) {
            DistanceStatus targetDistance = DistanceStatus.fromString(distanceStatus);
            if (targetDistance == null) {
                throw new InvalidHazardParameterException(
                        "Invalid distanceStatus filter: '" + distanceStatus + "'. Allowed values: NEAR, MODERATE, FAR, UNKNOWN");
            }
            candidates = candidates.stream()
                    .filter(c -> c.getDistanceStatus() == targetDistance)
                    .collect(Collectors.toList());
        }

        // 7. Filter by Road Access Status (Stage 5.6)
        if (roadAccessStatus != null && !roadAccessStatus.trim().isEmpty()) {
            RoadAccessStatus targetRoad = RoadAccessStatus.fromString(roadAccessStatus);
            if (targetRoad == null) {
                throw new InvalidHazardParameterException(
                        "Invalid roadAccessStatus filter: '" + roadAccessStatus + "'. Allowed values: NEAR, MODERATE, FAR, UNKNOWN");
            }
            candidates = candidates.stream()
                    .filter(c -> c.getRoadAccessStatus() == targetRoad)
                    .collect(Collectors.toList());
        }

        // 8. Filter by Healthcare Access Status (Stage 5.7)
        if (healthcareAccessStatus != null && !healthcareAccessStatus.trim().isEmpty()) {
            HealthcareAccessStatus targetHealthcare = HealthcareAccessStatus.fromString(healthcareAccessStatus);
            if (targetHealthcare == null) {
                throw new InvalidHazardParameterException(
                        "Invalid healthcareAccessStatus filter: '" + healthcareAccessStatus + "'. Allowed values: NEAR, MODERATE, FAR, UNKNOWN");
            }
            candidates = candidates.stream()
                    .filter(c -> c.getHealthcareAccessStatus() == targetHealthcare)
                    .collect(Collectors.toList());
        }

        // 9. Filter by Water Access Status (Stage 5.8)
        if (waterAccessStatus != null && !waterAccessStatus.trim().isEmpty()) {
            WaterAccessStatus targetWater = WaterAccessStatus.fromString(waterAccessStatus);
            if (targetWater == null) {
                throw new InvalidHazardParameterException(
                        "Invalid waterAccessStatus filter: '" + waterAccessStatus + "'. Allowed values: NEAR, MODERATE, FAR, UNKNOWN");
            }
            candidates = candidates.stream()
                    .filter(c -> c.getWaterAccessStatus() == targetWater)
                    .collect(Collectors.toList());
        }

        // 10. Filter by Supporting Infrastructure Access Status (Stage 5.9)
        if (infrastructureAccessStatus != null && !infrastructureAccessStatus.trim().isEmpty()) {
            InfrastructureAccessStatus targetInfra = InfrastructureAccessStatus.fromString(infrastructureAccessStatus);
            if (targetInfra == null) {
                throw new InvalidHazardParameterException(
                        "Invalid infrastructureAccessStatus filter: '" + infrastructureAccessStatus + "'. Allowed values: NEAR, MODERATE, FAR, UNKNOWN");
            }
            candidates = candidates.stream()
                    .filter(c -> c.getInfrastructureAccessStatus() == targetInfra)
                    .collect(Collectors.toList());
        }

        // 11. Filter by Suitability Class (Stage 5.10)
        if (suitabilityClass != null && !suitabilityClass.trim().isEmpty()) {
            SuitabilityClass targetSuitability = SuitabilityClass.fromString(suitabilityClass);
            candidates = candidates.stream()
                    .filter(c -> c.getSuitabilityClass() == targetSuitability)
                    .collect(Collectors.toList());
        }

        return candidates;
    }

    /**
     * Queries candidate safe sites with full multi-dimensional criteria filtering and optional top N ranking limit (Stage 5.11).
     */
    public List<CandidateSafeSiteDto> getCandidateSites(String district, String category, boolean redZoneOnly,
                                                        String hazardSafety, String terrainStatus, String distanceStatus,
                                                        String roadAccessStatus, String healthcareAccessStatus,
                                                        String waterAccessStatus, String infrastructureAccessStatus,
                                                        String suitabilityClass, Integer top) {
        if (top != null && top <= 0) {
            throw new InvalidHazardParameterException("Parameter 'top' must be a positive integer greater than 0.");
        }
        List<CandidateSafeSiteDto> candidates = getCandidateSites(district, category, redZoneOnly, hazardSafety,
                terrainStatus, distanceStatus, roadAccessStatus, healthcareAccessStatus,
                waterAccessStatus, infrastructureAccessStatus, suitabilityClass);

        if (top != null && top < candidates.size()) {
            return new ArrayList<>(candidates.subList(0, top));
        }
        return candidates;
    }

    /**
     * Retrieves a single candidate safe site by its unique identifier with all spatial dimensions evaluated.
     */
    public CandidateSafeSiteDto getCandidateSiteById(String siteId) {
        if (siteId == null || siteId.trim().isEmpty()) {
            throw new IllegalArgumentException("Site identifier cannot be null or empty");
        }

        String targetId = siteId.trim();
        return getAllCandidateSites().stream()
                .filter(c -> c.getSiteId() != null && c.getSiteId().equalsIgnoreCase(targetId))
                .findFirst()
                .orElseThrow(() -> new HazardNotFoundException("Candidate safe site not found: " + targetId));
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with spatial intelligence metadata.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly) {
        return generateCandidateSitesGeoJson(district, category, redZoneOnly, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with hazard-safety filtering.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly, String hazardSafety) {
        return generateCandidateSitesGeoJson(district, category, redZoneOnly, hazardSafety, null, null, null, null, null, null, null, null);
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with hazard-safety and terrain filtering.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly, String hazardSafety, String terrainStatus) {
        return generateCandidateSitesGeoJson(district, category, redZoneOnly, hazardSafety, terrainStatus, null, null, null, null, null, null, null);
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with Stage 5.5 multi-dimensional filtering.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly,
                                                                     String hazardSafety, String terrainStatus, String distanceStatus) {
        return generateCandidateSitesGeoJson(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus, null, null, null, null, null, null);
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with Stage 5.6 multi-dimensional filtering.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly,
                                                                     String hazardSafety, String terrainStatus, String distanceStatus,
                                                                     String roadAccessStatus) {
        return generateCandidateSitesGeoJson(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus, roadAccessStatus, null, null, null, null, null);
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with Stage 5.7 multi-dimensional filtering.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly,
                                                                     String hazardSafety, String terrainStatus, String distanceStatus,
                                                                     String roadAccessStatus, String healthcareAccessStatus) {
        return generateCandidateSitesGeoJson(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus, roadAccessStatus, healthcareAccessStatus, null, null, null, null);
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with Stage 5.8 multi-dimensional filtering.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly,
                                                                     String hazardSafety, String terrainStatus, String distanceStatus,
                                                                     String roadAccessStatus, String healthcareAccessStatus,
                                                                     String waterAccessStatus) {
        return generateCandidateSitesGeoJson(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus, roadAccessStatus, healthcareAccessStatus, waterAccessStatus, null, null, null);
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with Stage 5.9 multi-dimensional filtering.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly,
                                                                     String hazardSafety, String terrainStatus, String distanceStatus,
                                                                     String roadAccessStatus, String healthcareAccessStatus,
                                                                     String waterAccessStatus, String infrastructureAccessStatus) {
        return generateCandidateSitesGeoJson(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus,
                roadAccessStatus, healthcareAccessStatus, waterAccessStatus, infrastructureAccessStatus, null, null);
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with Stage 5.10 multi-dimensional filtering.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly,
                                                                     String hazardSafety, String terrainStatus, String distanceStatus,
                                                                     String roadAccessStatus, String healthcareAccessStatus,
                                                                     String waterAccessStatus, String infrastructureAccessStatus,
                                                                     String suitabilityClass) {
        return generateCandidateSitesGeoJson(district, category, redZoneOnly, hazardSafety, terrainStatus, distanceStatus,
                roadAccessStatus, healthcareAccessStatus, waterAccessStatus, infrastructureAccessStatus, suitabilityClass, null);
    }

    /**
     * Generates an RFC 7946 GeoJSON FeatureCollection of candidate safe sites with Stage 5.11 full multi-dimensional and top-ranking filtering.
     */
    public GeoJsonFeatureCollectionDto generateCandidateSitesGeoJson(String district, String category, boolean redZoneOnly,
                                                                     String hazardSafety, String terrainStatus, String distanceStatus,
                                                                     String roadAccessStatus, String healthcareAccessStatus,
                                                                     String waterAccessStatus, String infrastructureAccessStatus,
                                                                     String suitabilityClass, Integer top) {
        List<CandidateSafeSiteDto> sites = getCandidateSites(district, category, redZoneOnly, hazardSafety, terrainStatus,
                distanceStatus, roadAccessStatus, healthcareAccessStatus, waterAccessStatus, infrastructureAccessStatus, suitabilityClass, top);
        List<GeoJsonFeatureDto> features = new ArrayList<>();

        for (CandidateSafeSiteDto site : sites) {
            if (site.getLongitude() == null || site.getLatitude() == null) {
                continue;
            }

            GeoJsonGeometryDto geom = GeoJsonGeometryDto.point(site.getLongitude(), site.getLatitude());

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("siteId", site.getSiteId());
            props.put("siteName", site.getSiteName());
            props.put("category", site.getCategory() != null ? site.getCategory().name() : "UNKNOWN");
            props.put("categoryDisplayName", site.getCategoryDisplayName());
            props.put("subType", site.getSubType());
            props.put("district", site.getDistrict());
            props.put("state", site.getState());
            props.put("status", site.getStatus());
            props.put("source", site.getSource());
            props.put("colorHex", site.getColorHex());
            props.put("layerId", "CANDIDATE_SAFE_SITES");

            // Stage 5.3 Hazard Safety Feature Properties
            props.put("hazardSafetyStatus", site.getHazardSafetyStatus() != null ? site.getHazardSafetyStatus().name() : "UNKNOWN");
            props.put("hazardSafetyReason", site.getHazardSafetyReason());
            props.put("riskZone", site.getRiskZone());
            props.put("riskScore", site.getRiskScore());

            // Stage 5.4 Terrain & Slope Feature Properties
            props.put("elevationMeters", site.getElevationMeters());
            props.put("slopeDegrees", site.getSlopeDegrees());
            props.put("terrainStatus", site.getTerrainStatus() != null ? site.getTerrainStatus().name() : "UNKNOWN");
            props.put("terrainReason", site.getTerrainReason());

            // Stage 5.5 Geographic Distance Feature Properties
            props.put("distanceMeters", site.getDistanceMeters());
            props.put("distanceKilometers", site.getDistanceKilometers());
            props.put("distanceStatus", site.getDistanceStatus() != null ? site.getDistanceStatus().name() : "UNKNOWN");
            props.put("distanceReason", site.getDistanceReason());

            // Stage 5.6 Road Accessibility Feature Properties
            props.put("roadDistanceMeters", site.getRoadDistanceMeters());
            props.put("roadDistanceKilometers", site.getRoadDistanceKilometers());
            props.put("roadAccessStatus", site.getRoadAccessStatus() != null ? site.getRoadAccessStatus().name() : "UNKNOWN");
            props.put("roadAccessReason", site.getRoadAccessReason());

            // Stage 5.7 Healthcare Accessibility Feature Properties
            props.put("healthcareDistanceMeters", site.getHealthcareDistanceMeters());
            props.put("healthcareDistanceKilometers", site.getHealthcareDistanceKilometers());
            props.put("healthcareAccessStatus", site.getHealthcareAccessStatus() != null ? site.getHealthcareAccessStatus().name() : "UNKNOWN");
            props.put("nearestHealthcareSiteId", site.getNearestHealthcareSiteId());
            props.put("nearestHealthcareSiteName", site.getNearestHealthcareSiteName());
            props.put("healthcareReason", site.getHealthcareReason());

            // Stage 5.8 Water Accessibility Feature Properties
            props.put("waterDistanceMeters", site.getWaterDistanceMeters());
            props.put("waterDistanceKilometers", site.getWaterDistanceKilometers());
            props.put("waterAccessStatus", site.getWaterAccessStatus() != null ? site.getWaterAccessStatus().name() : "UNKNOWN");
            props.put("nearestWaterSiteId", site.getNearestWaterSiteId());
            props.put("nearestWaterSiteName", site.getNearestWaterSiteName());
            props.put("waterReason", site.getWaterReason());

            // Stage 5.9 Supporting Infrastructure Feature Properties
            props.put("infrastructureDistanceMeters", site.getInfrastructureDistanceMeters());
            props.put("infrastructureDistanceKilometers", site.getInfrastructureDistanceKilometers());
            props.put("infrastructureAccessStatus", site.getInfrastructureAccessStatus() != null ? site.getInfrastructureAccessStatus().name() : "UNKNOWN");
            props.put("nearestInfrastructureSiteId", site.getNearestInfrastructureSiteId());
            props.put("nearestInfrastructureSiteName", site.getNearestInfrastructureSiteName());
            props.put("nearestInfrastructureCategory", site.getNearestInfrastructureCategory());
            props.put("infrastructureReason", site.getInfrastructureReason());

            // Stage 5.10 Site Suitability Feature Properties
            props.put("suitabilityScore", site.getSuitabilityScore());
            props.put("suitabilityClass", site.getSuitabilityClass() != null ? site.getSuitabilityClass().name() : "UNKNOWN");
            props.put("knownFactorCount", site.getKnownFactorCount());
            props.put("unknownFactorCount", site.getUnknownFactorCount());
            props.put("dataCompletenessPercentage", site.getDataCompletenessPercentage());
            props.put("suitabilityReason", site.getSuitabilityReason());
            props.put("suitabilityFactors", site.getSuitabilityFactors());

            // Stage 5.11 Candidate Safe-Site Ranking Feature Properties
            props.put("rank", site.getRank());
            props.put("rankingReason", site.getRankingReason());

            features.add(new GeoJsonFeatureDto("SAFE-SITE-" + site.getSiteId(), geom, props));
        }

        return new GeoJsonFeatureCollectionDto(features);
    }
}
