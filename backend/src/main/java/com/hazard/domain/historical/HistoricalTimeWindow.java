package com.hazard.domain.historical;

/**
 * Configurable Historical Time Window for Disaster Analysis.
 */
public enum HistoricalTimeWindow {
    ALL_HISTORY("All Available Recorded History"),
    LAST_5_YEARS("Last 5 Years (Recent Dynamics)"),
    LAST_10_YEARS("Last 10 Years (Decadal Pattern)"),
    LAST_20_YEARS("Last 20 Years (Long-Term Archive)"),
    CUSTOM("Custom Date Range");

    private final String description;

    HistoricalTimeWindow(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
