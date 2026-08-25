package com.hazard.dto.historical;

import com.hazard.domain.historical.HistoricalHotspotTier;

/**
 * DTO representing an empirical historical disaster hotspot ranking for a district.
 */
public class HistoricalHotspotDto {

    private String districtName;
    private int totalHistoricalEvents;
    private double eventsPerYear;
    private double averageSeverity;
    private int highSeverityEvents;
    private double empiricalRecurrenceGapYears;
    private double hotspotIndex;       // [0.0000, 1.0000]
    private double hotspotScore100;    // [0.0, 100.0]
    private HistoricalHotspotTier hotspotTier;
    private String latestEventDate;
    private String primaryHazardType;

    public HistoricalHotspotDto() {}

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public int getTotalHistoricalEvents() {
        return totalHistoricalEvents;
    }

    public void setTotalHistoricalEvents(int totalHistoricalEvents) {
        this.totalHistoricalEvents = totalHistoricalEvents;
    }

    public double getEventsPerYear() {
        return eventsPerYear;
    }

    public void setEventsPerYear(double eventsPerYear) {
        this.eventsPerYear = eventsPerYear;
    }

    public double getAverageSeverity() {
        return averageSeverity;
    }

    public void setAverageSeverity(double averageSeverity) {
        this.averageSeverity = averageSeverity;
    }

    public int getHighSeverityEvents() {
        return highSeverityEvents;
    }

    public void setHighSeverityEvents(int highSeverityEvents) {
        this.highSeverityEvents = highSeverityEvents;
    }

    public double getEmpiricalRecurrenceGapYears() {
        return empiricalRecurrenceGapYears;
    }

    public void setEmpiricalRecurrenceGapYears(double empiricalRecurrenceGapYears) {
        this.empiricalRecurrenceGapYears = empiricalRecurrenceGapYears;
    }

    public double getHotspotIndex() {
        return hotspotIndex;
    }

    public void setHotspotIndex(double hotspotIndex) {
        this.hotspotIndex = hotspotIndex;
    }

    public double getHotspotScore100() {
        return hotspotScore100;
    }

    public void setHotspotScore100(double hotspotScore100) {
        this.hotspotScore100 = hotspotScore100;
    }

    public HistoricalHotspotTier getHotspotTier() {
        return hotspotTier;
    }

    public void setHotspotTier(HistoricalHotspotTier hotspotTier) {
        this.hotspotTier = hotspotTier;
    }

    public String getLatestEventDate() {
        return latestEventDate;
    }

    public void setLatestEventDate(String latestEventDate) {
        this.latestEventDate = latestEventDate;
    }

    public String getPrimaryHazardType() {
        return primaryHazardType;
    }

    public void setPrimaryHazardType(String primaryHazardType) {
        this.primaryHazardType = primaryHazardType;
    }
}
