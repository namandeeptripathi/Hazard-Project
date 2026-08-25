package com.hazard.dto.historical;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulates empirical temporal distributions across years, months, and seasons,
 * as well as descriptive historical trend indications.
 */
public class TemporalPatternDto {

    private Map<Integer, Integer> eventsByYear = new LinkedHashMap<>();
    private Map<String, Integer> eventsByMonth = new LinkedHashMap<>();
    private Map<String, Integer> eventsBySeason = new LinkedHashMap<>();
    private String peakDisasterMonth;
    private String primaryDisasterSeason;
    private String descriptiveTrend; // INCREASING, DECREASING, STABLE, INSUFFICIENT_DATA
    private String trendExplanation;

    public TemporalPatternDto() {}

    public Map<Integer, Integer> getEventsByYear() {
        return eventsByYear;
    }

    public void setEventsByYear(Map<Integer, Integer> eventsByYear) {
        this.eventsByYear = eventsByYear;
    }

    public Map<String, Integer> getEventsByMonth() {
        return eventsByMonth;
    }

    public void setEventsByMonth(Map<String, Integer> eventsByMonth) {
        this.eventsByMonth = eventsByMonth;
    }

    public Map<String, Integer> getEventsBySeason() {
        return eventsBySeason;
    }

    public void setEventsBySeason(Map<String, Integer> eventsBySeason) {
        this.eventsBySeason = eventsBySeason;
    }

    public String getPeakDisasterMonth() {
        return peakDisasterMonth;
    }

    public void setPeakDisasterMonth(String peakDisasterMonth) {
        this.peakDisasterMonth = peakDisasterMonth;
    }

    public String getPrimaryDisasterSeason() {
        return primaryDisasterSeason;
    }

    public void setPrimaryDisasterSeason(String primaryDisasterSeason) {
        this.primaryDisasterSeason = primaryDisasterSeason;
    }

    public String getDescriptiveTrend() {
        return descriptiveTrend;
    }

    public void setDescriptiveTrend(String descriptiveTrend) {
        this.descriptiveTrend = descriptiveTrend;
    }

    public String getTrendExplanation() {
        return trendExplanation;
    }

    public void setTrendExplanation(String trendExplanation) {
        this.trendExplanation = trendExplanation;
    }
}
