package com.hazard.dto.facade;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.SeverityTier;

import java.util.ArrayList;
import java.util.List;

/**
 * Consolidated District-Level Hazard Intelligence Overview DTO.
 * Assembles multi-stage hazard metrics into a unified summary profile for an administrative district.
 */
public class DistrictHazardOverviewDto {

    private Integer districtId;
    private String districtName;
    private String state = "Bihar";
    private String country = "India";
    private boolean hasActiveWeatherStation;

    private int recordedFloodCount;
    private int recordedExtremeRainfallCount;

    private Double floodHazardScore;
    private Double rainfallHazardScore;
    private Double multiHazardIndex;
    private SeverityTier severityTier;
    private HazardType dominantHazard;

    private List<String> intersectingMajorRivers = new ArrayList<>();
    private String summaryExplanation;

    public DistrictHazardOverviewDto() {
    }

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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isHasActiveWeatherStation() {
        return hasActiveWeatherStation;
    }

    public void setHasActiveWeatherStation(boolean hasActiveWeatherStation) {
        this.hasActiveWeatherStation = hasActiveWeatherStation;
    }

    public int getRecordedFloodCount() {
        return recordedFloodCount;
    }

    public void setRecordedFloodCount(int recordedFloodCount) {
        this.recordedFloodCount = recordedFloodCount;
    }

    public int getRecordedExtremeRainfallCount() {
        return recordedExtremeRainfallCount;
    }

    public void setRecordedExtremeRainfallCount(int recordedExtremeRainfallCount) {
        this.recordedExtremeRainfallCount = recordedExtremeRainfallCount;
    }

    public Double getFloodHazardScore() {
        return floodHazardScore;
    }

    public void setFloodHazardScore(Double floodHazardScore) {
        this.floodHazardScore = floodHazardScore;
    }

    public Double getRainfallHazardScore() {
        return rainfallHazardScore;
    }

    public void setRainfallHazardScore(Double rainfallHazardScore) {
        this.rainfallHazardScore = rainfallHazardScore;
    }

    public Double getMultiHazardIndex() {
        return multiHazardIndex;
    }

    public void setMultiHazardIndex(Double multiHazardIndex) {
        this.multiHazardIndex = multiHazardIndex;
    }

    public SeverityTier getSeverityTier() {
        return severityTier;
    }

    public void setSeverityTier(SeverityTier severityTier) {
        this.severityTier = severityTier;
    }

    public HazardType getDominantHazard() {
        return dominantHazard;
    }

    public void setDominantHazard(HazardType dominantHazard) {
        this.dominantHazard = dominantHazard;
    }

    public List<String> getIntersectingMajorRivers() {
        return intersectingMajorRivers;
    }

    public void setIntersectingMajorRivers(List<String> intersectingMajorRivers) {
        this.intersectingMajorRivers = intersectingMajorRivers != null ? intersectingMajorRivers : new ArrayList<>();
    }

    public String getSummaryExplanation() {
        return summaryExplanation;
    }

    public void setSummaryExplanation(String summaryExplanation) {
        this.summaryExplanation = summaryExplanation;
    }
}
