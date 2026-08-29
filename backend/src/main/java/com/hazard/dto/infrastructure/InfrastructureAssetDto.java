package com.hazard.dto.infrastructure;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.infrastructure.InfrastructureCategory;
import com.hazard.domain.infrastructure.InfrastructureCriticality;

/**
 * Detailed exposure record for an individual infrastructure asset
 * (e.g. hospital, dam, canal, bridge, school, emergency shelter).
 */
public class InfrastructureAssetDto {

    private String assetId;
    private String assetName;
    private InfrastructureCategory category;
    private String subType;
    private String districtName;
    private String state;

    private String geometryType; // Point, LineString, MultiLineString, Polygon, MultiPolygon
    private Double longitude;    // Centroid longitude
    private Double latitude;     // Centroid latitude

    private boolean isLineInfrastructure;
    private Double totalLengthKm;
    private Double affectedLengthKm;
    private Double affectedPercentage;

    private InfrastructureCriticality criticality;
    private String criticalitySource; // SOURCE_PROVIDED, CONFIGURED_MAPPING

    private String hazardIdentifier;
    private String hazardType;
    private Double hazardSeverityScore;

    private boolean isIntersecting;
    private Double distanceMeters;
    private Double infrastructureExposureScore; // [0.0000, 1.0000]
    private ExposureCategory exposureCategory;
    private String exposureStatus; // EXPOSED, UNAFFECTED

    private String dataProvenance;
    private String explanation;

    public InfrastructureAssetDto() {
        this.isIntersecting = true;
        this.exposureStatus = "EXPOSED";
        this.state = "Bihar";
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public InfrastructureCategory getCategory() {
        return category;
    }

    public void setCategory(InfrastructureCategory category) {
        this.category = category;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getDistrict() {
        return districtName;
    }

    public void setDistrict(String district) {
        this.districtName = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public boolean isLineInfrastructure() {
        return isLineInfrastructure;
    }

    public void setLineInfrastructure(boolean lineInfrastructure) {
        isLineInfrastructure = lineInfrastructure;
    }

    public Double getTotalLengthKm() {
        return totalLengthKm;
    }

    public void setTotalLengthKm(Double totalLengthKm) {
        this.totalLengthKm = totalLengthKm;
    }

    public Double getAffectedLengthKm() {
        return affectedLengthKm;
    }

    public void setAffectedLengthKm(Double affectedLengthKm) {
        this.affectedLengthKm = affectedLengthKm;
    }

    public Double getAffectedPercentage() {
        return affectedPercentage;
    }

    public void setAffectedPercentage(Double affectedPercentage) {
        this.affectedPercentage = affectedPercentage;
    }

    public InfrastructureCriticality getCriticality() {
        return criticality;
    }

    public void setCriticality(InfrastructureCriticality criticality) {
        this.criticality = criticality;
    }

    public String getCriticalitySource() {
        return criticalitySource;
    }

    public void setCriticalitySource(String criticalitySource) {
        this.criticalitySource = criticalitySource;
    }

    public String getHazardIdentifier() {
        return hazardIdentifier;
    }

    public void setHazardIdentifier(String hazardIdentifier) {
        this.hazardIdentifier = hazardIdentifier;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public Double getHazardSeverityScore() {
        return hazardSeverityScore;
    }

    public void setHazardSeverityScore(Double hazardSeverityScore) {
        this.hazardSeverityScore = hazardSeverityScore;
    }

    public boolean isIntersecting() {
        return isIntersecting;
    }

    public void setIntersecting(boolean intersecting) {
        isIntersecting = intersecting;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Double getInfrastructureExposureScore() {
        return infrastructureExposureScore;
    }

    public void setInfrastructureExposureScore(Double infrastructureExposureScore) {
        this.infrastructureExposureScore = infrastructureExposureScore;
    }

    public ExposureCategory getExposureCategory() {
        return exposureCategory;
    }

    public void setExposureCategory(ExposureCategory exposureCategory) {
        this.exposureCategory = exposureCategory;
    }

    public String getExposureStatus() {
        return exposureStatus;
    }

    public void setExposureStatus(String exposureStatus) {
        this.exposureStatus = exposureStatus;
    }

    public String getDataProvenance() {
        return dataProvenance;
    }

    public void setDataProvenance(String dataProvenance) {
        this.dataProvenance = dataProvenance;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
