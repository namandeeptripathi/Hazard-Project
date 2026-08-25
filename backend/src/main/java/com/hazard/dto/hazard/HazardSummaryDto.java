package com.hazard.dto.hazard;

import com.hazard.domain.hazard.HazardType;

import java.util.List;

/**
 * Catalog summary DTO providing high-level statistics and metadata
 * of all integrated hazard and meteorological datasets.
 */
public class HazardSummaryDto {

    private String description;
    private String canonicalCrs;
    private String coverageRegion;
    private String temporalRange;
    private long totalIntegratedRecords;
    private long dfoFloodEventsCount;
    private long emdatFloodRecordsCount;
    private long weatherObservationsCount;
    private List<HazardType> activeHazardTypes;
    private List<String> availableWeatherStations;
    private List<String> supportedQueryTypes;

    public HazardSummaryDto() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCanonicalCrs() {
        return canonicalCrs;
    }

    public void setCanonicalCrs(String canonicalCrs) {
        this.canonicalCrs = canonicalCrs;
    }

    public String getCoverageRegion() {
        return coverageRegion;
    }

    public void setCoverageRegion(String coverageRegion) {
        this.coverageRegion = coverageRegion;
    }

    public String getTemporalRange() {
        return temporalRange;
    }

    public void setTemporalRange(String temporalRange) {
        this.temporalRange = temporalRange;
    }

    public long getTotalIntegratedRecords() {
        return totalIntegratedRecords;
    }

    public void setTotalIntegratedRecords(long totalIntegratedRecords) {
        this.totalIntegratedRecords = totalIntegratedRecords;
    }

    public long getDfoFloodEventsCount() {
        return dfoFloodEventsCount;
    }

    public void setDfoFloodEventsCount(long dfoFloodEventsCount) {
        this.dfoFloodEventsCount = dfoFloodEventsCount;
    }

    public long getEmdatFloodRecordsCount() {
        return emdatFloodRecordsCount;
    }

    public void setEmdatFloodRecordsCount(long emdatFloodRecordsCount) {
        this.emdatFloodRecordsCount = emdatFloodRecordsCount;
    }

    public long getWeatherObservationsCount() {
        return weatherObservationsCount;
    }

    public void setWeatherObservationsCount(long weatherObservationsCount) {
        this.weatherObservationsCount = weatherObservationsCount;
    }

    public List<HazardType> getActiveHazardTypes() {
        return activeHazardTypes;
    }

    public void setActiveHazardTypes(List<HazardType> activeHazardTypes) {
        this.activeHazardTypes = activeHazardTypes;
    }

    public List<String> getAvailableWeatherStations() {
        return availableWeatherStations;
    }

    public void setAvailableWeatherStations(List<String> availableWeatherStations) {
        this.availableWeatherStations = availableWeatherStations;
    }

    public List<String> getSupportedQueryTypes() {
        return supportedQueryTypes;
    }

    public void setSupportedQueryTypes(List<String> supportedQueryTypes) {
        this.supportedQueryTypes = supportedQueryTypes;
    }
}
