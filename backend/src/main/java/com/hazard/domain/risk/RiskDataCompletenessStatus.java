package com.hazard.domain.risk;

/**
 * Data Quality and Completeness Status across the 4 Risk Pillars.
 */
public enum RiskDataCompletenessStatus {
    DATA_COMPLETE("All 4 risk components (Hazard, Exposure, Vulnerability, Historical) are available and verified"),
    DATA_PARTIAL("Sufficient risk component coverage is available (at least 3 components)"),
    INSUFFICIENT_DATA("Fewer than required components available; risk calculation unreliable");

    private final String description;

    RiskDataCompletenessStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
