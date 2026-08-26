package com.hazard.domain.safesite;

import com.hazard.domain.infrastructure.InfrastructureCategory;

/**
 * Stage 5.2 — Supported Candidate Safe-Site Categories.
 *
 * Classifies public/institutional locations that can potentially serve
 * as emergency safe sites or relief shelters. Non-shelter infrastructure
 * (power plants, bridges, rail lines, waterways, fuel depots) are filtered out.
 */
public enum CandidateSiteCategory {
    EDUCATION("Schools & Educational Institutions", "#9C27B0", "Schools, universities, and educational campuses with indoor halls"),
    GOVERNMENT_BUILDING("Government & Administrative Buildings", "#673AB7", "Administrative complexes, collectorates, and civic structures"),
    EMERGENCY_SHELTER("Emergency Shelters & Relief Centers", "#F44336", "Designated disaster relief shelters and emergency response centers"),
    HEALTHCARE("Hospitals & Medical Facilities", "#E91E63", "Hospitals and medical centers with triage and emergency care capacity");

    private final String displayName;
    private final String colorHex;
    private final String description;

    CandidateSiteCategory(String displayName, String colorHex, String description) {
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Maps an InfrastructureCategory into a CandidateSiteCategory.
     * Returns null if the category is not suitable as a candidate safe site
     * (e.g. POWER, TRANSPORT, WATER, OTHER_CRITICAL).
     */
    public static CandidateSiteCategory fromInfrastructureCategory(InfrastructureCategory category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case EDUCATION -> EDUCATION;
            case GOVERNMENT -> GOVERNMENT_BUILDING;
            case EMERGENCY_SERVICES -> EMERGENCY_SHELTER;
            case HEALTHCARE -> HEALTHCARE;
            default -> null; // POWER, TRANSPORT, WATER, OTHER_CRITICAL are excluded
        };
    }

    /**
     * Checks whether an InfrastructureCategory qualifies as a candidate safe site category.
     */
    public static boolean isCandidateCategory(InfrastructureCategory category) {
        return fromInfrastructureCategory(category) != null;
    }

    /**
     * Flexible string-to-enum resolution supporting aliases.
     */
    public static CandidateSiteCategory fromString(String categoryStr) {
        if (categoryStr == null || categoryStr.trim().isEmpty()) {
            return null;
        }
        String clean = categoryStr.trim().toUpperCase().replace("-", "_").replace(" ", "_");

        // Direct match with CandidateSiteCategory enum names
        try {
            return CandidateSiteCategory.valueOf(clean);
        } catch (IllegalArgumentException ignored) {
        }

        // Match with InfrastructureCategory names
        try {
            InfrastructureCategory infraCat = InfrastructureCategory.valueOf(clean);
            return fromInfrastructureCategory(infraCat);
        } catch (IllegalArgumentException ignored) {
        }

        // Common aliases
        if (clean.contains("SCHOOL") || clean.contains("COLLEGE") || clean.contains("UNIVERSITY") || clean.contains("EDU")) {
            return EDUCATION;
        }
        if (clean.contains("GOV") || clean.contains("COLLECTORATE") || clean.contains("ADMIN") || clean.contains("OFFICE")) {
            return GOVERNMENT_BUILDING;
        }
        if (clean.contains("SHELTER") || clean.contains("EMERGENCY") || clean.contains("RELIEF") || clean.contains("SEOC") || clean.contains("SDRF")) {
            return EMERGENCY_SHELTER;
        }
        if (clean.contains("HOSPITAL") || clean.contains("HEALTH") || clean.contains("MEDIC") || clean.contains("CLINIC")) {
            return HEALTHCARE;
        }

        return null;
    }
}
