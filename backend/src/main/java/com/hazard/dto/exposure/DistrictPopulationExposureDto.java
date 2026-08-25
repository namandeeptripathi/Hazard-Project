package com.hazard.dto.exposure;

import com.hazard.domain.exposure.ExposureCategory;

/**
 * District-level aggregated population exposure summary for multi-district comparisons
 * and choropleth thematic mapping.
 */
public class DistrictPopulationExposureDto {

    private Integer districtId;
    private String districtName;
    private String state;
    private String gid2;

    private Long totalPopulation;
    private Long exposedPopulation;
    private Double exposurePercentage;
    private Double exposureScore;
    private ExposureCategory exposureCategory;

    private int totalSettlementsCount;
    private int exposedSettlementsCount;
    private Double peakHazardIndex;
    private String dominantHazard;

    public DistrictPopulationExposureDto() {}

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
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

    public String getGid2() {
        return gid2;
    }

    public void setGid2(String gid2) {
        this.gid2 = gid2;
    }

    public Long getTotalPopulation() {
        return totalPopulation;
    }

    public void setTotalPopulation(Long totalPopulation) {
        this.totalPopulation = totalPopulation;
    }

    public Long getExposedPopulation() {
        return exposedPopulation;
    }

    public void setExposedPopulation(Long exposedPopulation) {
        this.exposedPopulation = exposedPopulation;
    }

    public Double getExposurePercentage() {
        return exposurePercentage;
    }

    public void setExposurePercentage(Double exposurePercentage) {
        this.exposurePercentage = exposurePercentage;
    }

    public Double getExposureScore() {
        return exposureScore;
    }

    public void setExposureScore(Double exposureScore) {
        this.exposureScore = exposureScore;
    }

    public ExposureCategory getExposureCategory() {
        return exposureCategory;
    }

    public void setExposureCategory(ExposureCategory exposureCategory) {
        this.exposureCategory = exposureCategory;
    }

    public int getTotalSettlementsCount() {
        return totalSettlementsCount;
    }

    public void setTotalSettlementsCount(int totalSettlementsCount) {
        this.totalSettlementsCount = totalSettlementsCount;
    }

    public int getExposedSettlementsCount() {
        return exposedSettlementsCount;
    }

    public void setExposedSettlementsCount(int exposedSettlementsCount) {
        this.exposedSettlementsCount = exposedSettlementsCount;
    }

    public Double getPeakHazardIndex() {
        return peakHazardIndex;
    }

    public void setPeakHazardIndex(Double peakHazardIndex) {
        this.peakHazardIndex = peakHazardIndex;
    }

    public String getDominantHazard() {
        return dominantHazard;
    }

    public void setDominantHazard(String dominantHazard) {
        this.dominantHazard = dominantHazard;
    }
}
