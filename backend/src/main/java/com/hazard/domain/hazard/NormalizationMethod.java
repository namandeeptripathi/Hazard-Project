package com.hazard.domain.hazard;

/**
 * Mathematical algorithm utilized for metric normalization.
 * - MIN_MAX: Linear min-max scaling with deterministic bounds clamping.
 * - LOG_MIN_MAX: Logarithmic scaling for wide multi-order magnitude metrics.
 * - STEP_CATEGORICAL: Discrete threshold category mapping.
 */
public enum NormalizationMethod {
    MIN_MAX("Linear Min-Max", "Linear scaling: (value - min) / (max - min) with [0.0, 1.0] clamping"),
    LOG_MIN_MAX("Logarithmic Min-Max", "Logarithmic scaling: (log(value) - log(min)) / (log(max) - log(min))"),
    STEP_CATEGORICAL("Categorical Step", "Discrete threshold step mapping");

    private final String displayName;
    private final String description;

    NormalizationMethod(String displayName, String description) {
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
