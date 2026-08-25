package com.hazard.domain.risk;

/**
 * Top-Level Risk Component Pillars in Stage 4.7.
 */
public enum RiskComponentType {
    HAZARD("Hazard Severity & Probability", "#38BDF8"),
    EXPOSURE("Combined Exposure (Pop, Settlement, Infrastructure)", "#FB923C"),
    VULNERABILITY("Vulnerability & Susceptibility", "#A855F7"),
    HISTORICAL("Historical Disaster Evidence", "#F59E0B");

    private final String displayName;
    private final String colorHex;

    RiskComponentType(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }
}
