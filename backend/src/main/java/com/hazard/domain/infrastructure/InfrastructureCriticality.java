package com.hazard.domain.infrastructure;

/**
 * Categorical Criticality Tiers for Infrastructure Assets.
 * Determines the prioritization multiplier in exposure scoring.
 */
public enum InfrastructureCriticality {
    LOW("Low Criticality", 0.80, "Non-essential / secondary asset; disruption has localized impact"),
    MODERATE("Moderate Criticality", 1.00, "Standard public infrastructure; disruption causes general inconvenience"),
    HIGH("High Criticality", 1.15, "Essential service asset (hospitals, bridges, power nodes, dams); disruption causes severe disruption"),
    VERY_HIGH("Very High Criticality", 1.25, "Life-safety / emergency lifeline asset; failure directly endangers human life or disaster response");

    private final String displayName;
    private final double weightMultiplier;
    private final String description;

    InfrastructureCriticality(String displayName, double weightMultiplier, String description) {
        this.displayName = displayName;
        this.weightMultiplier = weightMultiplier;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getWeightMultiplier() {
        return weightMultiplier;
    }

    public String getDescription() {
        return description;
    }
}
