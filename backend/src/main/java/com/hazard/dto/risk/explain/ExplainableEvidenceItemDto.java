package com.hazard.dto.risk.explain;

import com.hazard.domain.risk.explain.EvidenceType;

/**
 * Structured empirical and modeled evidence supporting a risk component.
 */
public class ExplainableEvidenceItemDto {

    private String evidenceId;
    private EvidenceType type;
    private String title;
    private Object rawValue;
    private String displayValue;
    private String unit;
    private String sourceStage;
    private String provenance;
    private String timePeriod;
    private String geographicScope;
    private String availability;
    private String description;

    public ExplainableEvidenceItemDto() {
        this.availability = "AVAILABLE";
    }

    public ExplainableEvidenceItemDto(String evidenceId, EvidenceType type, String title,
                                      Object rawValue, String displayValue, String unit,
                                      String sourceStage, String provenance, String timePeriod,
                                      String geographicScope, String description) {
        this.evidenceId = evidenceId;
        this.type = type;
        this.title = title;
        this.rawValue = rawValue;
        this.displayValue = displayValue;
        this.unit = unit;
        this.sourceStage = sourceStage;
        this.provenance = provenance;
        this.timePeriod = timePeriod;
        this.geographicScope = geographicScope;
        this.availability = "AVAILABLE";
        this.description = description;
    }

    public String getEvidenceId() { return evidenceId; }
    public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }

    public EvidenceType getType() { return type; }
    public void setType(EvidenceType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Object getRawValue() { return rawValue; }
    public void setRawValue(Object rawValue) { this.rawValue = rawValue; }

    public String getDisplayValue() { return displayValue; }
    public void setDisplayValue(String displayValue) { this.displayValue = displayValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSourceStage() { return sourceStage; }
    public void setSourceStage(String sourceStage) { this.sourceStage = sourceStage; }

    public String getProvenance() { return provenance; }
    public void setProvenance(String provenance) { this.provenance = provenance; }

    public String getTimePeriod() { return timePeriod; }
    public void setTimePeriod(String timePeriod) { this.timePeriod = timePeriod; }

    public String getGeographicScope() { return geographicScope; }
    public void setGeographicScope(String geographicScope) { this.geographicScope = geographicScope; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
