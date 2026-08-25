package com.hazard.domain.hazard;

/**
 * Quality and Processing classification status for hazard observations.
 * - VALID: Fully validated observation with verified coordinates, timestamps, and metrics.
 * - PARTIAL: Valid observation containing partial or approximate fields (e.g. missing optional metrics).
 * - UNLOCATED: Valid tabular observation lacking discrete geographic coordinates (e.g. EM-DAT macro records or unlocated DFO events).
 * - INVALID: Corrupt, irreconcilable, or out-of-bounds observation flagged during processing.
 */
public enum QualityStatus {
    VALID("Valid", "Complete, spatially located, and verified observation"),
    PARTIAL("Partial", "Valid observation with partial or approximated attributes"),
    UNLOCATED("Unlocated", "Valid tabular observation lacking discrete geographic point coordinates"),
    INVALID("Invalid", "Observation failing critical validation constraints");

    private final String displayName;
    private final String description;

    QualityStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static QualityStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Quality status cannot be null or empty");
        }
        String normalized = text.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (QualityStatus status : QualityStatus.values()) {
            if (status.name().equalsIgnoreCase(normalized) || status.displayName.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported quality status: '" + text + "'. Supported: VALID, PARTIAL, UNLOCATED, INVALID");
    }
}
