package com.hazard.dto.risk.contributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Natural-language and structured explanation metadata detailing the primary drivers of disaster risk.
 */
public class RiskExplanationDto {

    private String summaryHeadline;
    private String narrative;
    private String dominantPillar;
    private List<String> primaryDrivers = new ArrayList<>();
    private List<String> exposureHighlights = new ArrayList<>();
    private List<String> vulnerabilityHighlights = new ArrayList<>();
    private List<String> historicalEvidenceHighlights = new ArrayList<>();
    private String dataCompletenessNote;
    private String disclaimer = "Notice: Risk contributors represent mathematical sensitivity and exposure factors in the multi-criteria model; they are descriptive decision-support metrics, not physical causality guarantees.";

    public RiskExplanationDto() {}

    public String getSummaryHeadline() { return summaryHeadline; }
    public void setSummaryHeadline(String summaryHeadline) { this.summaryHeadline = summaryHeadline; }

    public String getNarrative() { return narrative; }
    public void setNarrative(String narrative) { this.narrative = narrative; }

    public String getDominantPillar() { return dominantPillar; }
    public void setDominantPillar(String dominantPillar) { this.dominantPillar = dominantPillar; }

    public List<String> getPrimaryDrivers() { return primaryDrivers; }
    public void setPrimaryDrivers(List<String> primaryDrivers) { this.primaryDrivers = primaryDrivers != null ? primaryDrivers : new ArrayList<>(); }

    public List<String> getExposureHighlights() { return exposureHighlights; }
    public void setExposureHighlights(List<String> exposureHighlights) { this.exposureHighlights = exposureHighlights != null ? exposureHighlights : new ArrayList<>(); }

    public List<String> getVulnerabilityHighlights() { return vulnerabilityHighlights; }
    public void setVulnerabilityHighlights(List<String> vulnerabilityHighlights) { this.vulnerabilityHighlights = vulnerabilityHighlights != null ? vulnerabilityHighlights : new ArrayList<>(); }

    public List<String> getHistoricalEvidenceHighlights() { return historicalEvidenceHighlights; }
    public void setHistoricalEvidenceHighlights(List<String> historicalEvidenceHighlights) { this.historicalEvidenceHighlights = historicalEvidenceHighlights != null ? historicalEvidenceHighlights : new ArrayList<>(); }

    public String getDataCompletenessNote() { return dataCompletenessNote; }
    public void setDataCompletenessNote(String dataCompletenessNote) { this.dataCompletenessNote = dataCompletenessNote; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
