package com.hazard.domain.hazard;

/**
 * Standard Categorical Severity Tiers for Normalized Hazard Scores [0.0000, 1.0000].
 * Thresholds:
 * - LOW: [0.0000, 0.2499]
 * - MODERATE: [0.2500, 0.4999]
 * - HIGH: [0.5000, 0.7499]
 * - SEVERE: [0.7500, 1.0000]
 */
public enum SeverityTier {
    LOW("Low", 0.0000, 0.2499, "#4CAF50", "Low relative hazard intensity; minor operational concern"),
    MODERATE("Moderate", 0.2500, 0.4999, "#FFC107", "Moderate relative hazard intensity; elevated monitoring recommended"),
    HIGH("High", 0.5000, 0.7499, "#FF9800", "High relative hazard intensity; significant physical impact potential"),
    SEVERE("Severe", 0.7500, 1.0000, "#F44336", "Severe relative hazard intensity; critical extreme event conditions");

    private final String displayName;
    private final double minScore;
    private final double maxScore;
    private final String colorHex;
    private final String description;

    SeverityTier(String displayName, double minScore, double maxScore, String colorHex, String description) {
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.colorHex = colorHex;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determines the SeverityTier for a given numerical hazard score in [0.0000, 1.0000].
     */
    public static SeverityTier fromScore(Double score) {
        if (score == null) {
            return null;
        }
        double clamped = Math.min(1.0, Math.max(0.0, score));
        if (clamped < 0.2500) {
            return LOW;
        } else if (clamped < 0.5000) {
            return MODERATE;
        } else if (clamped < 0.7500) {
            return HIGH;
        } else {
            return SEVERE;
        }
    }

    public static SeverityTier fromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("Severity tier name cannot be null or empty");
        }
        for (SeverityTier tier : values()) {
            if (tier.name().equalsIgnoreCase(str.trim()) || tier.displayName.equalsIgnoreCase(str.trim())) {
                return tier;
            }
        }
        throw new IllegalArgumentException("Unknown severity tier: " + str);
    }
}
