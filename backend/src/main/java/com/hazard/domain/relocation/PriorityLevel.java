package com.hazard.domain.relocation;

import com.hazard.exception.InvalidHazardParameterException;

/**
 * Stage 7A.1 — Relocation Priority Level Classification.
 *
 * Categorizes relocation cases into actionable priority tiers based on
 * the composite priority score produced by the Priority Scoring Engine:
 * - IMMEDIATE: Life-threatening risk requiring emergency action now.
 * - SHORT_TERM: High-risk cases requiring near-term intervention.
 * - MEDIUM_TERM: Moderate-risk cases for planned relocation.
 * - MONITORING: Low-risk, fully-allocated cases requiring periodic reassessment only.
 */
public enum PriorityLevel {

    IMMEDIATE("Immediate Priority", 1, "#B71C1C", 0.70,
            "Life-threatening risk: red-zone exposure, critical hazard severity, or shelter capacity exhausted — requires emergency action"),

    SHORT_TERM("Short-Term Priority", 2, "#E65100", 0.40,
            "High-risk case requiring near-term intervention: elevated hazard or significant unallocated population"),

    MEDIUM_TERM("Medium-Term Priority", 3, "#F57C00", 0.15,
            "Moderate-risk case for planned relocation: partial allocation or moderate hazard exposure"),

    MONITORING("Monitoring", 4, "#388E3C", 0.00,
            "Low-risk, fully-allocated case: requires periodic reassessment but no active intervention");

    private final String displayName;
    private final int priorityOrder;   // 1 = highest priority
    private final String colorHex;
    private final double minScoreThreshold;
    private final String description;

    PriorityLevel(String displayName, int priorityOrder, String colorHex,
                  double minScoreThreshold, String description) {
        this.displayName = displayName;
        this.priorityOrder = priorityOrder;
        this.colorHex = colorHex;
        this.minScoreThreshold = minScoreThreshold;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriorityOrder() {
        return priorityOrder;
    }

    public String getColorHex() {
        return colorHex;
    }

    public double getMinScoreThreshold() {
        return minScoreThreshold;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns true if this level represents a higher priority (lower priorityOrder)
     * than the given level.
     */
    public boolean isHigherPriorityThan(PriorityLevel other) {
        if (other == null) return true;
        return this.priorityOrder < other.priorityOrder;
    }

    /**
     * Returns true if this level is IMMEDIATE or SHORT_TERM (actionable priorities).
     */
    public boolean isActionable() {
        return this == IMMEDIATE || this == SHORT_TERM;
    }

    /**
     * Classifies a priority score into a PriorityLevel using centralized thresholds.
     * Boundary values are handled by >= (e.g. score of exactly 0.70 → IMMEDIATE).
     *
     * @param score the composite priority score in [0.0, 1.0]
     * @return the corresponding PriorityLevel
     */
    public static PriorityLevel fromScore(double score) {
        if (Double.isNaN(score) || score < 0.0) {
            return MONITORING;
        }
        if (score >= IMMEDIATE.minScoreThreshold) return IMMEDIATE;
        if (score >= SHORT_TERM.minScoreThreshold) return SHORT_TERM;
        if (score >= MEDIUM_TERM.minScoreThreshold) return MEDIUM_TERM;
        return MONITORING;
    }

    /**
     * Parses a string representation case-insensitively into a PriorityLevel.
     */
    public static PriorityLevel fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (PriorityLevel level : values()) {
            if (level.name().equals(clean)) {
                return level;
            }
        }
        throw new InvalidHazardParameterException(
                "Invalid priorityLevel '" + text + "'. Allowed values: IMMEDIATE, SHORT_TERM, MEDIUM_TERM, MONITORING");
    }
}
