package com.hazard.domain.risk.contributor;

/**
 * Importance classification for risk contributors based on their percentage share of total risk.
 */
public enum ContributorImportance {
    DOMINANT("Accounts for >= 25% of total risk contribution", "#f44336"),
    MAJOR("Accounts for 15% - < 25% of total risk contribution", "#ff9800"),
    MODERATE("Accounts for 5% - < 15% of total risk contribution", "#ffc107"),
    MINOR("Accounts for < 5% of total risk contribution", "#94a3b8");

    private final String description;
    private final String colorHex;

    ContributorImportance(String description, String colorHex) {
        this.description = description;
        this.colorHex = colorHex;
    }

    public String getDescription() {
        return description;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static ContributorImportance fromPercentage(double percent) {
        if (percent >= 25.0) return DOMINANT;
        if (percent >= 15.0) return MAJOR;
        if (percent >= 5.0) return MODERATE;
        return MINOR;
    }
}
