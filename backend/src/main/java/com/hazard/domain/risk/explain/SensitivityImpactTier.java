package com.hazard.domain.risk.explain;

/**
 * Leverage tier for model sensitivity analysis indicating how strongly a component influences final risk.
 */
public enum SensitivityImpactTier {
    HIGH_LEVERAGE("High score leverage: absolute impact >= 0.035 risk points per 0.10 perturbation", "#f44336"),
    MODERATE_LEVERAGE("Moderate score leverage: absolute impact between 0.015 and 0.035 risk points", "#ff9800"),
    LOW_LEVERAGE("Low score leverage: absolute impact < 0.015 risk points", "#94a3b8");

    private final String description;
    private final String colorHex;

    SensitivityImpactTier(String description, String colorHex) {
        this.description = description;
        this.colorHex = colorHex;
    }

    public String getDescription() {
        return description;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static SensitivityImpactTier fromAbsoluteImpact(double impact) {
        if (impact >= 0.035) return HIGH_LEVERAGE;
        if (impact >= 0.015) return MODERATE_LEVERAGE;
        return LOW_LEVERAGE;
    }
}
