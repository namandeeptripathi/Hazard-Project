package com.hazard.dto.exposure;

/**
 * Summary DTO of an individual populated settlement or residential footprint
 * that intersects a hazard impact area.
 */
public class SettlementExposureSummaryDto {

    private Integer id;
    private String name;
    private String placeType;       // city, town, village, hamlet, residential
    private String districtName;
    private Long population;
    private boolean isEstimatedPopulation;
    private Double longitude;
    private Double latitude;
    private Double distanceMeters;   // Distance to hazard centroid (if applicable)

    public SettlementExposureSummaryDto() {}

    public SettlementExposureSummaryDto(Integer id, String name, String placeType, String districtName,
                                        Long population, boolean isEstimatedPopulation,
                                        Double longitude, Double latitude, Double distanceMeters) {
        this.id = id;
        this.name = name;
        this.placeType = placeType;
        this.districtName = districtName;
        this.population = population;
        this.isEstimatedPopulation = isEstimatedPopulation;
        this.longitude = longitude;
        this.latitude = latitude;
        this.distanceMeters = distanceMeters;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public boolean isEstimatedPopulation() {
        return isEstimatedPopulation;
    }

    public void setEstimatedPopulation(boolean estimatedPopulation) {
        isEstimatedPopulation = estimatedPopulation;
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

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }
}
