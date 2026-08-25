package com.hazard.dto.historical;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.historical.HistoricalTimeWindow;

import java.time.LocalDate;

/**
 * Request DTO for custom historical disaster queries.
 */
public class HistoricalAnalysisRequestDto {

    private String districtName;
    private HazardType hazardType;
    private HistoricalTimeWindow timeWindow;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double minSeverity;

    public HistoricalAnalysisRequestDto() {
        this.timeWindow = HistoricalTimeWindow.ALL_HISTORY;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public HazardType getHazardType() {
        return hazardType;
    }

    public void setHazardType(HazardType hazardType) {
        this.hazardType = hazardType;
    }

    public HistoricalTimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(HistoricalTimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getMinSeverity() {
        return minSeverity;
    }

    public void setMinSeverity(Double minSeverity) {
        this.minSeverity = minSeverity;
    }
}
