package com.hazard.domain.historical;

/**
 * Data Quality and Archive Completeness for Historical Disaster Records.
 */
public enum HistoricalDataQualityStatus {
    DATA_COMPLETE("Complete historical record with dates, coordinates, and severity metrics"),
    DATA_PARTIAL("Sufficient historical records available; some non-critical attributes derived"),
    LIMITED_HISTORY("Sparse historical record (< 3 recorded events in time window)"),
    INSUFFICIENT_HISTORY("No qualifying historical disaster records found in time window");

    private final String description;

    HistoricalDataQualityStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
