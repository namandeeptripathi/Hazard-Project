package com.hazard.domain.risk;

/**
 * 5-Tier Categorical Classification for Disaster Risk in Stage 4.7.
 */
public enum RiskTier {
    LOW("Low Disaster Risk", 0.00, 0.20, "#4CAF50", "Minimal hazard exposure with high coping capacity and low vulnerability"),
    MODERATE("Moderate Disaster Risk", 0.20, 0.40, "#FFC107", "Standard baseline risk; manageable hazard intensity and adequate infrastructure"),
    HIGH("High Disaster Risk", 0.40, 0.60, "#FF9800", "Elevated risk; significant exposed population or infrastructure in active hazard zone"),
    VERY_HIGH("Very High Disaster Risk", 0.60, 0.80, "#F44336", "Severe risk; high hazard severity coupled with dense population and elevated vulnerability"),
    CRITICAL("Critical Disaster Risk", 0.80, 1.00, "#9C27B0", "Catastrophic compound risk; severe hazard footprint intersecting dense, highly vulnerable communities with chronic recurrence");

    private final String displayName;
    private final double minScore;
    private final double maxScore;
    private final String colorHex;
    private final String description;

    RiskTier(String displayName, double minScore, double maxScore, String colorHex, String description) {
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
     * Classifies a normalized risk score in [0.0, 1.0] into a RiskTier.
     */
    public static RiskTier fromScore(double score) {
        if (score >= 0.80) return CRITICAL;
        if (score >= 0.60) return VERY_HIGH;
        if (score >= 0.40) return HIGH;
        if (score >= 0.20) return MODERATE;
        return LOW;
    }
}
