package com.hazard.dto.historical;

/**
 * Empirical recurrence statistics derived strictly from historical archive intervals.
 * Explicitly disclaims predictive return periods (e.g., 100-year flood).
 */
public class RecurrenceStatisticsDto {

    private Double averageHistoricalGapYears;
    private Double minimumHistoricalGapYears;
    private Double maximumHistoricalGapYears;
    private int totalIntervalsEvaluated;
    private String status; // "EMPIRICAL_ONLY"
    private String disclaimer;

    public RecurrenceStatisticsDto() {
        this.status = "EMPIRICAL_ONLY";
        this.disclaimer = "Empirical average gap between recorded historical events. Does not represent a modeled predictive return period.";
    }

    public Double getAverageHistoricalGapYears() {
        return averageHistoricalGapYears;
    }

    public void setAverageHistoricalGapYears(Double averageHistoricalGapYears) {
        this.averageHistoricalGapYears = averageHistoricalGapYears;
    }

    public Double getMinimumHistoricalGapYears() {
        return minimumHistoricalGapYears;
    }

    public void setMinimumHistoricalGapYears(Double minimumHistoricalGapYears) {
        this.minimumHistoricalGapYears = minimumHistoricalGapYears;
    }

    public Double getMaximumHistoricalGapYears() {
        return maximumHistoricalGapYears;
    }

    public void setMaximumHistoricalGapYears(Double maximumHistoricalGapYears) {
        this.maximumHistoricalGapYears = maximumHistoricalGapYears;
    }

    public int getTotalIntervalsEvaluated() {
        return totalIntervalsEvaluated;
    }

    public void setTotalIntervalsEvaluated(int totalIntervalsEvaluated) {
        this.totalIntervalsEvaluated = totalIntervalsEvaluated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
