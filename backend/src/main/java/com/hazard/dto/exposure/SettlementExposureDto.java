package com.hazard.dto.exposure;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.exposure.PopulationDataSource;

/**
 * Encapsulates the exposure profile of an individual populated settlement
 * (village, town, city, residential cluster) intersecting a hazard area.
 */
public class SettlementExposureDto {

    private Integer settlementId;
    private String settlementName;
    private String settlementType;        // city, town, village, hamlet, residential
    private String districtName;
    private String state;
    private Double longitude;
    private Double latitude;

    private Long totalPopulation;
    private boolean isEstimatedPopulation;
    private PopulationDataSource populationProvenance;

    private String hazardIdentifier;
    private String hazardType;
    private Double hazardSeverityScore;

    private boolean isIntersecting;
    private Double distanceMeters;         // Distance to hazard epicenter / centroid (if point hazard)
    private Double settlementExposureScore; // [0.0000, 1.0000]
    private ExposureCategory exposureCategory;
    private String exposureStatus;         // EXPOSED, UNAFFECTED
    private String explanation;

    public SettlementExposureDto() {
        this.isIntersecting = true;
        this.exposureStatus = "EXPOSED";
    }

    public Integer getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(Integer settlementId) {
        this.settlementId = settlementId;
    }

    public String getSettlementName() {
        return settlementName;
    }

    public void setSettlementName(String settlementName) {
        this.settlementName = settlementName;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String settlementType) {
        this.settlementType = settlementType;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public Long getTotalPopulation() {
        return totalPopulation;
    }

    public void setTotalPopulation(Long totalPopulation) {
        this.totalPopulation = totalPopulation;
    }

    public boolean isEstimatedPopulation() {
        return isEstimatedPopulation;
    }

    public void setEstimatedPopulation(boolean estimatedPopulation) {
        isEstimatedPopulation = estimatedPopulation;
    }

    public PopulationDataSource getPopulationProvenance() {
        return populationProvenance;
    }

    public void setPopulationProvenance(PopulationDataSource populationProvenance) {
        this.populationProvenance = populationProvenance;
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

    public Double getSettlementExposureScore() {
        return settlementExposureScore;
    }

    public void setSettlementExposureScore(Double settlementExposureScore) {
        this.settlementExposureScore = settlementExposureScore;
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

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
