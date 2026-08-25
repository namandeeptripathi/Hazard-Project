package com.hazard.domain.hazard;

/**
 * Chronological relationship between multiple hazard event time windows.
 */
public enum TemporalRelationship {
    EXACT_OVERLAP("Exact Temporal Overlap", "Event active date/time intervals directly intersect"),
    SAME_DAY("Same Calendar Day", "Events recorded on the exact same calendar date"),
    PROXIMATE_WINDOW("Proximate Time Window", "Events occur within a configurable temporal buffer (e.g. 3 to 7 days)"),
    DISJOINT_TIME("Temporally Disjoint", "Non-overlapping time windows");

    private final String displayName;
    private final String description;

    TemporalRelationship(String displayName, String description) {
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
