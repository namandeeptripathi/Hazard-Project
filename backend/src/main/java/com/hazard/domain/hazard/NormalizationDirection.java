package com.hazard.domain.hazard;

/**
 * Direction of normalization scaling relative to hazard intensity:
 * - INCREASING: Higher raw value corresponds to higher normalized hazard intensity (default).
 * - DECREASING: Lower raw value corresponds to higher normalized hazard intensity (inverse).
 */
public enum NormalizationDirection {
    INCREASING("Increasing", "Higher raw value indicates higher relative hazard intensity"),
    DECREASING("Decreasing", "Lower raw value indicates higher relative hazard intensity");

    private final String displayName;
    private final String description;

    NormalizationDirection(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
