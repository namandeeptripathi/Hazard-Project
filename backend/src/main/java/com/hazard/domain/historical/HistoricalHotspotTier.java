package com.hazard.domain.historical;

/**
 * Categorical Classification Tiers for Empirical Historical Hotspots in Stage 4.6.
 */
public enum HistoricalHotspotTier {
    LOW("Low Historical Recurrence", 0.00, 0.25, "#4CAF50"),
    MODERATE("Moderate Historical Recurrence", 0.25, 0.50, "#FFC107"),
    HIGH("High Historical Hotspot", 0.50, 0.75, "#FF9800"),
    SEVERE_HOTSPOT("Severe Chronic Hotspot", 0.75, 1.00, "#F44336");

    private final String displayName;
    private final double minIndex;
    private final double maxIndex;
    private final String colorHex;

    HistoricalHotspotTier(String displayName, double minIndex, double maxIndex, String colorHex) {
        this.displayName = displayName;
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinIndex() {
        return minIndex;
    }

    public double getMaxIndex() {
        return maxIndex;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static HistoricalHotspotTier fromIndex(double index) {
        if (index >= 0.75) return SEVERE_HOTSPOT;
        if (index >= 0.50) return HIGH;
        if (index >= 0.25) return MODERATE;
        return LOW;
    }
}
