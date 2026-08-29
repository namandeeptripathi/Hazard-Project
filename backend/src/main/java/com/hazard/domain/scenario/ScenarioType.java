package com.hazard.domain.scenario;

import com.hazard.exception.InvalidHazardParameterException;

import java.util.Arrays;

/**
 * Stage 9A — Scenario Types for What-If Disaster Simulation Modeling.
 *
 * Defines the supported scenario categories for parameter perturbation:
 * - BASELINE: Zero-change reference benchmark representing current observed conditions.
 * - RAINFALL_CHANGE: Precipitation delta/percentage perturbation.
 * - HAZARD_INTENSITY: Direct hazard severity/intensity scaling.
 * - POPULATION_EXPOSURE: Demographic growth or population displacement shift.
 * - MULTI_FACTOR: Combined multi-dimensional parameter shifts.
 */
public enum ScenarioType {

    BASELINE(
            "Baseline (Reference Model)",
            "Zero-perturbation reference conditions with 0% rainfall change, 0% hazard intensity change, and 0% population exposure shift."
    ),
    RAINFALL_CHANGE(
            "Rainfall Change Scenario",
            "Simulates precipitation anomalies (+/- %) impacting hydrological discharge, surface runoff, and inundation extent."
    ),
    HAZARD_INTENSITY(
            "Hazard Intensity Scenario",
            "Simulates amplification or reduction (+/- %) of acute hazard footprint and severity."
    ),
    POPULATION_EXPOSURE(
            "Population Exposure Scenario",
            "Simulates demographic influx, high-density urbanization, or population vulnerability expansion (+/- %)."
    ),
    MULTI_FACTOR(
            "Multi-Factor Compound Scenario",
            "Simulates simultaneous changes across precipitation, hazard severity, and population exposure."
    );

    private final String displayName;
    private final String description;

    ScenarioType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBaseline() {
        return this == BASELINE;
    }

    /**
     * Case-insensitive resolver for ScenarioType.
     * Throws {@link InvalidHazardParameterException} with actionable messages when an unrecognized type is provided.
     *
     * @param typeStr String representation of scenario type
     * @return Resolved {@link ScenarioType}
     */
    public static ScenarioType fromString(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario type cannot be null or empty. Allowed types: " +
                    Arrays.toString(values()));
        }

        String normalized = typeStr.trim().toUpperCase()
                .replace("-", "_")
                .replace(" ", "_");

        for (ScenarioType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }

        // Check common aliases
        if ("RAIN".equals(normalized) || "RAINFALL".equals(normalized)) {
            return RAINFALL_CHANGE;
        }
        if ("HAZARD".equals(normalized) || "INTENSITY".equals(normalized)) {
            return HAZARD_INTENSITY;
        }
        if ("POPULATION".equals(normalized) || "EXPOSURE".equals(normalized) || "POP".equals(normalized)) {
            return POPULATION_EXPOSURE;
        }
        if ("COMPOUND".equals(normalized) || "MULTI".equals(normalized)) {
            return MULTI_FACTOR;
        }

        throw new InvalidHazardParameterException(String.format(
                "Invalid scenario type '%s'. Allowed scenario types are: %s",
                typeStr, Arrays.toString(values())
        ));
    }
}
