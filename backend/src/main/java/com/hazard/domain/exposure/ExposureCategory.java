package com.hazard.domain.exposure;

/**
 * Standard Categorical Classification for Population Exposure.
 *
 * Thresholds (Configurable via PopulationExposureConfig):
 * - LOW: Exposure < 15.0% (Score < 0.2500)
 * - MODERATE: 15.0% <= Exposure < 40.0% (0.2500 <= Score < 0.5000)
 * - HIGH: 40.0% <= Exposure < 70.0% (0.5000 <= Score < 0.7500)
 * - VERY_HIGH: Exposure >= 70.0% (Score >= 0.7500)
 */
public enum ExposureCategory {
    LOW("Low", 0.0, 14.99, "#4CAF50", "Low population exposure; minimal residential impact"),
    MODERATE("Moderate", 15.0, 39.99, "#FFC107", "Moderate population exposure; noticeable community impact"),
    HIGH("High", 40.0, 69.99, "#FF9800", "High population exposure; significant evacuation and protection needs"),
    VERY_HIGH("Very High", 70.0, 100.0, "#F44336", "Critical population exposure; extensive population displacement risk");

    private final String displayName;
    private final double minPercentage;
    private final double maxPercentage;
    private final String colorHex;
    private final String description;

    ExposureCategory(String displayName, double minPercentage, double maxPercentage, String colorHex, String description) {
        this.displayName = displayName;
        this.minPercentage = minPercentage;
        this.maxPercentage = maxPercentage;
        this.colorHex = colorHex;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    public double getMaxPercentage() {
        return maxPercentage;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determines the ExposureCategory based on exposure percentage [0.0, 100.0].
     */
    public static ExposureCategory fromPercentage(Double percentage) {
        if (percentage == null || percentage <= 0.0) {
            return LOW;
        }
        double clamped = Math.min(100.0, Math.max(0.0, percentage));
        if (clamped < 15.0) {
            return LOW;
        } else if (clamped < 40.0) {
            return MODERATE;
        } else if (clamped < 70.0) {
            return HIGH;
        } else {
            return VERY_HIGH;
        }
    }

    /**
     * Determines the ExposureCategory based on normalized exposure score [0.0000, 1.0000].
     */
    public static ExposureCategory fromScore(Double score) {
        if (score == null || score <= 0.0) {
            return LOW;
        }
        return fromPercentage(score * 100.0);
    }
}
