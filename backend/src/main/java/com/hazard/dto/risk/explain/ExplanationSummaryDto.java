package com.hazard.dto.risk.explain;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-level human explanations for disaster risk.
 */
public class ExplanationSummaryDto {

    private String executiveSummary;       // Level 1: 1 concise sentence
    private String shortSummary;           // Level 2: 2–4 sentences
    private String detailedNarrative;      // Level 3: Full paragraph
    private String dominantDriver;
    private List<String> primaryDriversList = new ArrayList<>();
    private List<String> secondaryFactorsList = new ArrayList<>();
    private String historicalContextSummary;
    private String mitigatingFactorsNote = "Protective or mitigating factors are not currently modeled in this baseline.";

    public ExplanationSummaryDto() {}

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public String getShortSummary() { return shortSummary; }
    public void setShortSummary(String shortSummary) { this.shortSummary = shortSummary; }

    public String getDetailedNarrative() { return detailedNarrative; }
    public void setDetailedNarrative(String detailedNarrative) { this.detailedNarrative = detailedNarrative; }

    public String getDominantDriver() { return dominantDriver; }
    public void setDominantDriver(String dominantDriver) { this.dominantDriver = dominantDriver; }

    public List<String> getPrimaryDriversList() { return primaryDriversList; }
    public void setPrimaryDriversList(List<String> primaryDriversList) { this.primaryDriversList = primaryDriversList != null ? primaryDriversList : new ArrayList<>(); }

    public List<String> getSecondaryFactorsList() { return secondaryFactorsList; }
    public void setSecondaryFactorsList(List<String> secondaryFactorsList) { this.secondaryFactorsList = secondaryFactorsList != null ? secondaryFactorsList : new ArrayList<>(); }

    public String getHistoricalContextSummary() { return historicalContextSummary; }
    public void setHistoricalContextSummary(String historicalContextSummary) { this.historicalContextSummary = historicalContextSummary; }

    public String getMitigatingFactorsNote() { return mitigatingFactorsNote; }
    public void setMitigatingFactorsNote(String mitigatingFactorsNote) { this.mitigatingFactorsNote = mitigatingFactorsNote; }
}
