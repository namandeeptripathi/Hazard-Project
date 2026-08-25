package com.hazard.dto.historical;

import com.hazard.domain.historical.HistoricalDataQualityStatus;
import com.hazard.domain.historical.HistoricalHotspotTier;
import com.hazard.domain.historical.HistoricalTimeWindow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive District Historical Disaster Intelligence Report.
 * Contains event frequency, empirical recurrence, severity metrics, temporal patterns,
 * historical hotspot indices, and data quality metadata.
 */
public class DistrictHistoricalSummaryDto {

    private String geographicUnit;
    private String geographicId;
    private Integer districtId;
    private String districtName;
    private String state;

    private HistoricalTimeWindow timeWindow;
    private LocalDate windowStartDate;
    private LocalDate windowEndDate;
    private double windowDurationYears;

    private int totalHistoricalEvents;
    private double eventsPerYear;
    private double historicalHotspotIndex;   // [0.0000, 1.0000]
    private double historicalHotspotScore100; // [0.0, 100.0]
    private HistoricalHotspotTier hotspotTier;

    private SeverityStatisticsDto severityStatistics;
    private RecurrenceStatisticsDto recurrenceStatistics;
    private TemporalPatternDto temporalPatterns;
    private HistoricalEventDto latestEvent;

    private int settlementsHistoricallyAffected;
    private int infrastructureAssetsHistoricallyAffected;

    private HistoricalDataQualityStatus dataQualityStatus;
    private int recordsEvaluated;
    private int recordsWithGeometry;
    private int recordsWithSeverity;

    private List<HistoricalEventDto> events = new ArrayList<>();
    private String summaryExplanation;

    public DistrictHistoricalSummaryDto() {
        this.geographicUnit = "DISTRICT";
        this.state = "Bihar";
    }

    public String getGeographicUnit() {
        return geographicUnit;
    }

    public void setGeographicUnit(String geographicUnit) {
        this.geographicUnit = geographicUnit;
    }

    public String getGeographicId() {
        return geographicId;
    }

    public void setGeographicId(String geographicId) {
        this.geographicId = geographicId;
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

    public HistoricalTimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(HistoricalTimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }

    public LocalDate getWindowStartDate() {
        return windowStartDate;
    }

    public void setWindowStartDate(LocalDate windowStartDate) {
        this.windowStartDate = windowStartDate;
    }

    public LocalDate getWindowEndDate() {
        return windowEndDate;
    }

    public void setWindowEndDate(LocalDate windowEndDate) {
        this.windowEndDate = windowEndDate;
    }

    public double getWindowDurationYears() {
        return windowDurationYears;
    }

    public void setWindowDurationYears(double windowDurationYears) {
        this.windowDurationYears = windowDurationYears;
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

    public double getHistoricalHotspotIndex() {
        return historicalHotspotIndex;
    }

    public void setHistoricalHotspotIndex(double historicalHotspotIndex) {
        this.historicalHotspotIndex = historicalHotspotIndex;
    }

    public double getHistoricalHotspotScore100() {
        return historicalHotspotScore100;
    }

    public void setHistoricalHotspotScore100(double historicalHotspotScore100) {
        this.historicalHotspotScore100 = historicalHotspotScore100;
    }

    public HistoricalHotspotTier getHotspotTier() {
        return hotspotTier;
    }

    public void setHotspotTier(HistoricalHotspotTier hotspotTier) {
        this.hotspotTier = hotspotTier;
    }

    public SeverityStatisticsDto getSeverityStatistics() {
        return severityStatistics;
    }

    public void setSeverityStatistics(SeverityStatisticsDto severityStatistics) {
        this.severityStatistics = severityStatistics;
    }

    public RecurrenceStatisticsDto getRecurrenceStatistics() {
        return recurrenceStatistics;
    }

    public void setRecurrenceStatistics(RecurrenceStatisticsDto recurrenceStatistics) {
        this.recurrenceStatistics = recurrenceStatistics;
    }

    public TemporalPatternDto getTemporalPatterns() {
        return temporalPatterns;
    }

    public void setTemporalPatterns(TemporalPatternDto temporalPatterns) {
        this.temporalPatterns = temporalPatterns;
    }

    public HistoricalEventDto getLatestEvent() {
        return latestEvent;
    }

    public void setLatestEvent(HistoricalEventDto latestEvent) {
        this.latestEvent = latestEvent;
    }

    public int getSettlementsHistoricallyAffected() {
        return settlementsHistoricallyAffected;
    }

    public void setSettlementsHistoricallyAffected(int settlementsHistoricallyAffected) {
        this.settlementsHistoricallyAffected = settlementsHistoricallyAffected;
    }

    public int getInfrastructureAssetsHistoricallyAffected() {
        return infrastructureAssetsHistoricallyAffected;
    }

    public void setInfrastructureAssetsHistoricallyAffected(int infrastructureAssetsHistoricallyAffected) {
        this.infrastructureAssetsHistoricallyAffected = infrastructureAssetsHistoricallyAffected;
    }

    public HistoricalDataQualityStatus getDataQualityStatus() {
        return dataQualityStatus;
    }

    public void setDataQualityStatus(HistoricalDataQualityStatus dataQualityStatus) {
        this.dataQualityStatus = dataQualityStatus;
    }

    public int getRecordsEvaluated() {
        return recordsEvaluated;
    }

    public void setRecordsEvaluated(int recordsEvaluated) {
        this.recordsEvaluated = recordsEvaluated;
    }

    public int getRecordsWithGeometry() {
        return recordsWithGeometry;
    }

    public void setRecordsWithGeometry(int recordsWithGeometry) {
        this.recordsWithGeometry = recordsWithGeometry;
    }

    public int getRecordsWithSeverity() {
        return recordsWithSeverity;
    }

    public void setRecordsWithSeverity(int recordsWithSeverity) {
        this.recordsWithSeverity = recordsWithSeverity;
    }

    public List<HistoricalEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<HistoricalEventDto> events) {
        this.events = events != null ? events : new ArrayList<>();
    }

    public String getSummaryExplanation() {
        return summaryExplanation;
    }

    public void setSummaryExplanation(String summaryExplanation) {
        this.summaryExplanation = summaryExplanation;
    }
}
