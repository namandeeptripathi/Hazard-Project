package com.hazard.domain.risk.explain;

/**
 * Categorization of empirical and model evidence supporting the disaster risk score.
 */
public enum EvidenceType {
    HAZARD_EVIDENCE("Physical hazard intensity, return periods, and spatial inundation footprints"),
    POPULATION_EVIDENCE("Demographic exposure, vulnerable age groups, and affected population counts"),
    SETTLEMENT_EVIDENCE("Habitation clusters, village polygons, and exposed built-up areas"),
    INFRASTRUCTURE_EVIDENCE("Critical lifeline assets: hospitals, schools, bridges, embankments, and roads"),
    VULNERABILITY_EVIDENCE("Socioeconomic deprivation, housing materials, health access, and drainage deficits"),
    HISTORICAL_EVIDENCE("Archival disaster records, empirical recurrence intervals, and hotspot indices"),
    CONFIGURATION_EVIDENCE("Model parameter weights, normalization rules, and classification thresholds"),
    DATA_QUALITY_EVIDENCE("Spatial resolution, catalog completeness, and data availability assessments");

    private final String description;

    EvidenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
