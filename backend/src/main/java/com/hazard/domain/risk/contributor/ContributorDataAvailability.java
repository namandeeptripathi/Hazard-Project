package com.hazard.domain.risk.contributor;

/**
 * Data availability status for individual risk contributors.
 */
public enum ContributorDataAvailability {
    AVAILABLE("Direct empirical or observed spatial data available"),
    PARTIAL("Partial spatial data with proxy estimation"),
    ESTIMATED("Modeled or interpolated baseline data"),
    UNAVAILABLE("Data missing or not available for evaluation");

    private final String description;

    ContributorDataAvailability(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
