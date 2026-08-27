package com.hazard.domain.relocation;

import com.hazard.exception.InvalidHazardParameterException;

/**
 * Stage 6.1 — Relocation and Shelter Allocation Status.
 *
 * Represents the allocation state of a vulnerable habitation or individual evacuee group:
 * - ALLOCATED: Fully assigned to a safe shelter site with sufficient capacity.
 * - PARTIALLY_ALLOCATED: Assigned to a safe site that reached capacity, leaving a deficit.
 * - UNALLOCATED_CAPACITY_EXCEEDED: No reachable safe site has available shelter capacity.
 * - UNALLOCATED_NO_SAFE_SITE: No candidate site within transit threshold meets minimum safety/suitability gates.
 * - PENDING: Awaiting relocation feasibility and optimization evaluation.
 */
public enum RelocationStatus {
    ALLOCATED("Fully Allocated", "#2E7D32", "Entire vulnerable population assigned to an optimal safe site with sufficient capacity", true),
    PARTIALLY_ALLOCATED("Partially Allocated", "#FF9800", "Site reached maximum capacity; partial population assigned, remainder requires secondary allocation", true),
    UNALLOCATED_CAPACITY_EXCEEDED("Unallocated (Capacity Exceeded)", "#F44336", "No reachable safe site within distance threshold has available shelter capacity", false),
    UNALLOCATED_NO_SAFE_SITE("Unallocated (No Safe Site)", "#D32F2F", "No candidate site within proximity passes hazard safety and suitability criteria", false),
    PENDING("Pending Evaluation", "#9E9E9E", "Awaiting relocation evaluation and optimization", false);

    private final String displayName;
    private final String colorHex;
    private final String description;
    private final boolean allocated;

    RelocationStatus(String displayName, String colorHex, String description, boolean allocated) {
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.description = description;
        this.allocated = allocated;
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

    public boolean isAllocated() {
        return this == ALLOCATED;
    }

    public boolean isPartiallyAllocated() {
        return this == PARTIALLY_ALLOCATED;
    }

    public boolean isAnyAllocated() {
        return allocated;
    }

    public boolean isUnallocated() {
        return this == UNALLOCATED_CAPACITY_EXCEEDED || this == UNALLOCATED_NO_SAFE_SITE;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Parses a string representation case-insensitively into a RelocationStatus.
     */
    public static RelocationStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (RelocationStatus status : values()) {
            if (status.name().equals(clean)) {
                return status;
            }
        }
        // Friendly alias mappings
        if ("FULL".equals(clean) || "FULLY_ALLOCATED".equals(clean) || "SUCCESS".equals(clean)) {
            return ALLOCATED;
        }
        if ("PARTIAL".equals(clean)) {
            return PARTIALLY_ALLOCATED;
        }
        if ("OVER_CAPACITY".equals(clean) || "CAPACITY_EXCEEDED".equals(clean)) {
            return UNALLOCATED_CAPACITY_EXCEEDED;
        }
        if ("NO_SITE".equals(clean) || "UNSAFE".equals(clean)) {
            return UNALLOCATED_NO_SAFE_SITE;
        }

        throw new InvalidHazardParameterException("Invalid relocationStatus '" + text + "'. Allowed values: ALLOCATED, PARTIALLY_ALLOCATED, UNALLOCATED_CAPACITY_EXCEEDED, UNALLOCATED_NO_SAFE_SITE, PENDING");
    }
}
