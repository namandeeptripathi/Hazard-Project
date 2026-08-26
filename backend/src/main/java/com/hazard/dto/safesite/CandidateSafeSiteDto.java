package com.hazard.dto.safesite;

import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.DistanceStatus;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.HealthcareAccessStatus;
import com.hazard.domain.safesite.InfrastructureAccessStatus;
import com.hazard.domain.safesite.RoadAccessStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.domain.safesite.TerrainStatus;
import com.hazard.domain.safesite.WaterAccessStatus;
import com.hazard.dto.infrastructure.InfrastructureAssetDto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Stages 5.2 - 5.10 — Candidate Safe-Site DTO.
 *
 * Represents a normalized public/institutional location evaluated for emergency
 * safe-site suitability across seven independent spatial dimensions:
 * 1. Stage 5.2: Normalized candidate site metadata
 * 2. Stage 5.3: Spatial hazard safety exposure (SAFE, AT_RISK, UNKNOWN)
 * 3. Stage 5.4: Terrain/slope feasibility (FAVORABLE, UNFAVORABLE, UNKNOWN)
 * 4. Stage 5.5: Geographic distance intelligence (NEAR, MODERATE, FAR, UNKNOWN)
 * 5. Stage 5.6: Road accessibility / proximity (NEAR, MODERATE, FAR, UNKNOWN)
 * 6. Stage 5.7: Healthcare support availability / proximity (NEAR, MODERATE, FAR, UNKNOWN)
 * 7. Stage 5.8: Water supply availability / proximity (NEAR, MODERATE, FAR, UNKNOWN)
 * 8. Stage 5.9: Supporting infrastructure proximity (NEAR, MODERATE, FAR, UNKNOWN)
 * 9. Stage 5.10: Multi-criteria Site Suitability Intelligence (score, classification, factor breakdown)
 * 10. Stage 5.11: Candidate Safe-Site Ranking (deterministic hierarchical ranking and explainable rank reason)
 */
public class CandidateSafeSiteDto {

    // Stage 5.2 Base Candidate Metadata Fields
    private String siteId;
    private String siteName;
    private CandidateSiteCategory category;
    private String categoryDisplayName;
    private String subType;
    private String district;
    private String state;
    private Double latitude;
    private Double longitude;
    private Integer capacity; // Nullable; not fabricated if unavailable in source data
    private String source;
    private String status;
    private String colorHex;
    private LocalDateTime timestamp;

    // Stage 5.3 Hazard Safety Assessment Fields
    private HazardSafetyStatus hazardSafetyStatus;
    private String hazardSafetyReason;
    private String riskZone;
    private Double riskScore;

    // Stage 5.4 Terrain & Slope Intelligence Fields
    private Double elevationMeters;
    private Double slopeDegrees;
    private TerrainStatus terrainStatus;
    private String terrainReason;

    // Stage 5.5 Geographic Distance Intelligence Fields
    private Double distanceMeters;
    private Double distanceKilometers;
    private DistanceStatus distanceStatus;
    private String distanceReason;

    // Stage 5.6 Road Accessibility Fields
    private Double roadDistanceMeters;
    private Double roadDistanceKilometers;
    private RoadAccessStatus roadAccessStatus;
    private String roadAccessReason;

    // Stage 5.7 Healthcare Accessibility Fields
    private Double healthcareDistanceMeters;
    private Double healthcareDistanceKilometers;
    private HealthcareAccessStatus healthcareAccessStatus;
    private String nearestHealthcareSiteId;
    private String nearestHealthcareSiteName;
    private String healthcareReason;

    // Stage 5.8 Water Accessibility Fields
    private Double waterDistanceMeters;
    private Double waterDistanceKilometers;
    private WaterAccessStatus waterAccessStatus;
    private String nearestWaterSiteId;
    private String nearestWaterSiteName;
    private String waterReason;

    // Stage 5.9 Supporting Infrastructure Accessibility Fields
    private Double infrastructureDistanceMeters;
    private Double infrastructureDistanceKilometers;
    private InfrastructureAccessStatus infrastructureAccessStatus;
    private String nearestInfrastructureSiteId;
    private String nearestInfrastructureSiteName;
    private String nearestInfrastructureCategory;
    private String infrastructureReason;

    // Stage 5.10 Multi-Criteria Site Suitability Intelligence Fields
    private Double suitabilityScore;
    private SuitabilityClass suitabilityClass;
    private Integer knownFactorCount;
    private Integer unknownFactorCount;
    private Double dataCompletenessPercentage;
    private String suitabilityReason;
    private Map<String, Object> suitabilityFactors;

    // Stage 5.11 Candidate Safe-Site Ranking Fields
    private Integer rank;
    private String rankingReason;

    public CandidateSafeSiteDto() {
        this.status = "CANDIDATE";
        this.state = "Bihar";
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Factory method: converts an InfrastructureAssetDto into a CandidateSafeSiteDto.
     * Returns null if the asset category is not a supported candidate safe site category.
     */
    public static CandidateSafeSiteDto fromInfrastructureAsset(InfrastructureAssetDto asset) {
        if (asset == null) {
            return null;
        }

        CandidateSiteCategory siteCategory = CandidateSiteCategory.fromInfrastructureCategory(asset.getCategory());
        if (siteCategory == null) {
            return null; // Excluded category (e.g. POWER, TRANSPORT, WATER, OTHER_CRITICAL)
        }

        CandidateSafeSiteDto dto = new CandidateSafeSiteDto();
        dto.setSiteId(asset.getAssetId());
        dto.setSiteName(asset.getAssetName());
        dto.setCategory(siteCategory);
        dto.setCategoryDisplayName(siteCategory.getDisplayName());
        dto.setSubType(asset.getSubType());
        dto.setDistrict(asset.getDistrictName());
        dto.setState(asset.getState() != null ? asset.getState() : "Bihar");
        dto.setLatitude(asset.getLatitude());
        dto.setLongitude(asset.getLongitude());
        dto.setCapacity(null); // Explicitly null: raw dataset does not include capacity
        dto.setSource(asset.getDataProvenance() != null ? asset.getDataProvenance() : "CONFIGURED_REGIONAL_FACILITIES");
        dto.setStatus("CANDIDATE");
        dto.setColorHex(siteCategory.getColorHex());
        return dto;
    }

    // Getters and Setters

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public CandidateSiteCategory getCategory() {
        return category;
    }

    public void setCategory(CandidateSiteCategory category) {
        this.category = category;
        if (category != null) {
            this.categoryDisplayName = category.getDisplayName();
            this.colorHex = category.getColorHex();
        }
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public HazardSafetyStatus getHazardSafetyStatus() {
        return hazardSafetyStatus;
    }

    public void setHazardSafetyStatus(HazardSafetyStatus hazardSafetyStatus) {
        this.hazardSafetyStatus = hazardSafetyStatus;
    }

    public String getHazardSafetyReason() {
        return hazardSafetyReason;
    }

    public void setHazardSafetyReason(String hazardSafetyReason) {
        this.hazardSafetyReason = hazardSafetyReason;
    }

    public String getRiskZone() {
        return riskZone;
    }

    public void setRiskZone(String riskZone) {
        this.riskZone = riskZone;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Double getElevationMeters() {
        return elevationMeters;
    }

    public void setElevationMeters(Double elevationMeters) {
        this.elevationMeters = elevationMeters;
    }

    public Double getSlopeDegrees() {
        return slopeDegrees;
    }

    public void setSlopeDegrees(Double slopeDegrees) {
        this.slopeDegrees = slopeDegrees;
    }

    public TerrainStatus getTerrainStatus() {
        return terrainStatus;
    }

    public void setTerrainStatus(TerrainStatus terrainStatus) {
        this.terrainStatus = terrainStatus;
    }

    public String getTerrainReason() {
        return terrainReason;
    }

    public void setTerrainReason(String terrainReason) {
        this.terrainReason = terrainReason;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Double getDistanceKilometers() {
        return distanceKilometers;
    }

    public void setDistanceKilometers(Double distanceKilometers) {
        this.distanceKilometers = distanceKilometers;
    }

    public DistanceStatus getDistanceStatus() {
        return distanceStatus;
    }

    public void setDistanceStatus(DistanceStatus distanceStatus) {
        this.distanceStatus = distanceStatus;
    }

    public String getDistanceReason() {
        return distanceReason;
    }

    public void setDistanceReason(String distanceReason) {
        this.distanceReason = distanceReason;
    }

    public Double getRoadDistanceMeters() {
        return roadDistanceMeters;
    }

    public void setRoadDistanceMeters(Double roadDistanceMeters) {
        this.roadDistanceMeters = roadDistanceMeters;
    }

    public Double getRoadDistanceKilometers() {
        return roadDistanceKilometers;
    }

    public void setRoadDistanceKilometers(Double roadDistanceKilometers) {
        this.roadDistanceKilometers = roadDistanceKilometers;
    }

    public RoadAccessStatus getRoadAccessStatus() {
        return roadAccessStatus;
    }

    public void setRoadAccessStatus(RoadAccessStatus roadAccessStatus) {
        this.roadAccessStatus = roadAccessStatus;
    }

    public String getRoadAccessReason() {
        return roadAccessReason;
    }

    public void setRoadAccessReason(String roadAccessReason) {
        this.roadAccessReason = roadAccessReason;
    }

    public Double getHealthcareDistanceMeters() {
        return healthcareDistanceMeters;
    }

    public void setHealthcareDistanceMeters(Double healthcareDistanceMeters) {
        this.healthcareDistanceMeters = healthcareDistanceMeters;
    }

    public Double getHealthcareDistanceKilometers() {
        return healthcareDistanceKilometers;
    }

    public void setHealthcareDistanceKilometers(Double healthcareDistanceKilometers) {
        this.healthcareDistanceKilometers = healthcareDistanceKilometers;
    }

    public HealthcareAccessStatus getHealthcareAccessStatus() {
        return healthcareAccessStatus;
    }

    public void setHealthcareAccessStatus(HealthcareAccessStatus healthcareAccessStatus) {
        this.healthcareAccessStatus = healthcareAccessStatus;
    }

    public String getNearestHealthcareSiteId() {
        return nearestHealthcareSiteId;
    }

    public void setNearestHealthcareSiteId(String nearestHealthcareSiteId) {
        this.nearestHealthcareSiteId = nearestHealthcareSiteId;
    }

    public String getNearestHealthcareSiteName() {
        return nearestHealthcareSiteName;
    }

    public void setNearestHealthcareSiteName(String nearestHealthcareSiteName) {
        this.nearestHealthcareSiteName = nearestHealthcareSiteName;
    }

    public String getHealthcareReason() {
        return healthcareReason;
    }

    public void setHealthcareReason(String healthcareReason) {
        this.healthcareReason = healthcareReason;
    }

    // Stage 5.8 Water Getters & Setters
    public Double getWaterDistanceMeters() {
        return waterDistanceMeters;
    }

    public void setWaterDistanceMeters(Double waterDistanceMeters) {
        this.waterDistanceMeters = waterDistanceMeters;
    }

    public Double getWaterDistanceKilometers() {
        return waterDistanceKilometers;
    }

    public void setWaterDistanceKilometers(Double waterDistanceKilometers) {
        this.waterDistanceKilometers = waterDistanceKilometers;
    }

    public WaterAccessStatus getWaterAccessStatus() {
        return waterAccessStatus;
    }

    public void setWaterAccessStatus(WaterAccessStatus waterAccessStatus) {
        this.waterAccessStatus = waterAccessStatus;
    }

    public String getNearestWaterSiteId() {
        return nearestWaterSiteId;
    }

    public void setNearestWaterSiteId(String nearestWaterSiteId) {
        this.nearestWaterSiteId = nearestWaterSiteId;
    }

    public String getNearestWaterSiteName() {
        return nearestWaterSiteName;
    }

    public void setNearestWaterSiteName(String nearestWaterSiteName) {
        this.nearestWaterSiteName = nearestWaterSiteName;
    }

    public String getWaterReason() {
        return waterReason;
    }

    public void setWaterReason(String waterReason) {
        this.waterReason = waterReason;
    }

    // Stage 5.9 Supporting Infrastructure Getters & Setters
    public Double getInfrastructureDistanceMeters() {
        return infrastructureDistanceMeters;
    }

    public void setInfrastructureDistanceMeters(Double infrastructureDistanceMeters) {
        this.infrastructureDistanceMeters = infrastructureDistanceMeters;
    }

    public Double getInfrastructureDistanceKilometers() {
        return infrastructureDistanceKilometers;
    }

    public void setInfrastructureDistanceKilometers(Double infrastructureDistanceKilometers) {
        this.infrastructureDistanceKilometers = infrastructureDistanceKilometers;
    }

    public InfrastructureAccessStatus getInfrastructureAccessStatus() {
        return infrastructureAccessStatus;
    }

    public void setInfrastructureAccessStatus(InfrastructureAccessStatus infrastructureAccessStatus) {
        this.infrastructureAccessStatus = infrastructureAccessStatus;
    }

    public String getNearestInfrastructureSiteId() {
        return nearestInfrastructureSiteId;
    }

    public void setNearestInfrastructureSiteId(String nearestInfrastructureSiteId) {
        this.nearestInfrastructureSiteId = nearestInfrastructureSiteId;
    }

    public String getNearestInfrastructureSiteName() {
        return nearestInfrastructureSiteName;
    }

    public void setNearestInfrastructureSiteName(String nearestInfrastructureSiteName) {
        this.nearestInfrastructureSiteName = nearestInfrastructureSiteName;
    }

    public String getNearestInfrastructureCategory() {
        return nearestInfrastructureCategory;
    }

    public void setNearestInfrastructureCategory(String nearestInfrastructureCategory) {
        this.nearestInfrastructureCategory = nearestInfrastructureCategory;
    }

    public String getInfrastructureReason() {
        return infrastructureReason;
    }

    public void setInfrastructureReason(String infrastructureReason) {
        this.infrastructureReason = infrastructureReason;
    }

    // Stage 5.10 Multi-Criteria Site Suitability Getters & Setters
    public Double getSuitabilityScore() {
        return suitabilityScore;
    }

    public void setSuitabilityScore(Double suitabilityScore) {
        this.suitabilityScore = suitabilityScore;
    }

    public SuitabilityClass getSuitabilityClass() {
        return suitabilityClass;
    }

    public void setSuitabilityClass(SuitabilityClass suitabilityClass) {
        this.suitabilityClass = suitabilityClass;
    }

    public Integer getKnownFactorCount() {
        return knownFactorCount;
    }

    public void setKnownFactorCount(Integer knownFactorCount) {
        this.knownFactorCount = knownFactorCount;
    }

    public Integer getUnknownFactorCount() {
        return unknownFactorCount;
    }

    public void setUnknownFactorCount(Integer unknownFactorCount) {
        this.unknownFactorCount = unknownFactorCount;
    }

    public Double getDataCompletenessPercentage() {
        return dataCompletenessPercentage;
    }

    public void setDataCompletenessPercentage(Double dataCompletenessPercentage) {
        this.dataCompletenessPercentage = dataCompletenessPercentage;
    }

    public String getSuitabilityReason() {
        return suitabilityReason;
    }

    public void setSuitabilityReason(String suitabilityReason) {
        this.suitabilityReason = suitabilityReason;
    }

    public Map<String, Object> getSuitabilityFactors() {
        return suitabilityFactors;
    }

    public void setSuitabilityFactors(Map<String, Object> suitabilityFactors) {
        this.suitabilityFactors = suitabilityFactors;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getRankingReason() {
        return rankingReason;
    }

    public void setRankingReason(String rankingReason) {
        this.rankingReason = rankingReason;
    }
}
