package com.hazard.domain.hazard;

/**
 * Controlled Hazard Type enumeration for the Hazard Intelligence subsystem.
 * Represents activated natural hazard phenomena supported by the underlying datasets:
 * - FLOOD: Historical flood inundation, riverine, and flash floods (DS-001 DFO & EM-DAT)
 * - EXTREME_RAINFALL: High-intensity meteorological precipitation events (DS-002 Open-Meteo)
 * - OTHER: Extensible fallback for future hazard types (Landslide, Earthquake, Cyclone)
 */
public enum HazardType {
    FLOOD("Flood", "Inundation, riverine flooding, or flash flood events"),
    EXTREME_RAINFALL("Extreme Rainfall", "High-intensity meteorological precipitation observations"),
    OTHER("Other", "General or unclassified natural hazard phenomenon");

    private final String displayName;
    private final String description;

    HazardType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Parses a string representation into a valid HazardType (case-insensitive, supports hyphens and spaces).
     *
     * @param text string input
     * @return matching HazardType
     * @throws IllegalArgumentException if text is null, empty, or unsupported
     */
    public static HazardType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Hazard type cannot be null or empty");
        }
        String normalized = text.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (HazardType type : HazardType.values()) {
            if (type.name().equalsIgnoreCase(normalized) || type.displayName.equalsIgnoreCase(text.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException(
                "Unsupported hazard type: '" + text + "'. Supported types: FLOOD, EXTREME_RAINFALL, OTHER"
        );
    }
}
