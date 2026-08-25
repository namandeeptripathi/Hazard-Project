package com.hazard.domain.risk.contributor;

/**
 * Directional impact of a contributor on overall disaster risk.
 */
public enum ContributorDirection {
    INCREASES_RISK("Component drives risk higher"),
    DECREASES_RISK("Component provides protective/mitigation effect"),
    NEUTRAL("Component has neutral impact on risk");

    private final String description;

    ContributorDirection(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
