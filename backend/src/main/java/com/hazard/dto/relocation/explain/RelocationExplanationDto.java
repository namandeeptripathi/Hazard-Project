package com.hazard.dto.relocation.explain;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7C.3 — Relocation Explanation DTO.
 *
 * Encapsulates the structured explanation of why a specific safe site was chosen as the
 * recommended destination, distinguishing hard feasibility gate compliance from soft suitability preferences.
 */
public class RelocationExplanationDto {

    private String destinationId;
    private String destinationName;
    private Double destinationScore;
    private Integer destinationRank;
    private boolean feasible;

    // Gate & Preference Summaries
    private String feasibilityGateSummary;       // Explains passing hard gates (Safety, Suitability, Capacity, Distance)
    private String softPreferenceSummary;         // Explains why this destination won among feasible options
    private String comparativeRankNarrative;      // Narrative describing rank standing among evaluated candidates
    private String alternativeDestinationsSummary;// Summary of available fallback destinations

    // Individual Dimension Narratives
    private String proximityExplanation;
    private String suitabilityExplanation;
    private String capacityFitExplanation;
    private String accessExplanation;

    // Structured 4-contributor breakdown
    private List<DecisionContributorDto> contributors = new ArrayList<>();

    public RelocationExplanationDto() {
        this.contributors = new ArrayList<>();
    }

    // --- Getters & Setters ---

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public Double getDestinationScore() {
        return destinationScore;
    }

    public void setDestinationScore(Double destinationScore) {
        this.destinationScore = destinationScore;
    }

    public Integer getDestinationRank() {
        return destinationRank;
    }

    public void setDestinationRank(Integer destinationRank) {
        this.destinationRank = destinationRank;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    public String getFeasibilityGateSummary() {
        return feasibilityGateSummary;
    }

    public void setFeasibilityGateSummary(String feasibilityGateSummary) {
        this.feasibilityGateSummary = feasibilityGateSummary;
    }

    public String getSoftPreferenceSummary() {
        return softPreferenceSummary;
    }

    public void setSoftPreferenceSummary(String softPreferenceSummary) {
        this.softPreferenceSummary = softPreferenceSummary;
    }

    public String getComparativeRankNarrative() {
        return comparativeRankNarrative;
    }

    public void setComparativeRankNarrative(String comparativeRankNarrative) {
        this.comparativeRankNarrative = comparativeRankNarrative;
    }

    public String getAlternativeDestinationsSummary() {
        return alternativeDestinationsSummary;
    }

    public void setAlternativeDestinationsSummary(String alternativeDestinationsSummary) {
        this.alternativeDestinationsSummary = alternativeDestinationsSummary;
    }

    public String getProximityExplanation() {
        return proximityExplanation;
    }

    public void setProximityExplanation(String proximityExplanation) {
        this.proximityExplanation = proximityExplanation;
    }

    public String getSuitabilityExplanation() {
        return suitabilityExplanation;
    }

    public void setSuitabilityExplanation(String suitabilityExplanation) {
        this.suitabilityExplanation = suitabilityExplanation;
    }

    public String getCapacityFitExplanation() {
        return capacityFitExplanation;
    }

    public void setCapacityFitExplanation(String capacityFitExplanation) {
        this.capacityFitExplanation = capacityFitExplanation;
    }

    public String getAccessExplanation() {
        return accessExplanation;
    }

    public void setAccessExplanation(String accessExplanation) {
        this.accessExplanation = accessExplanation;
    }

    public List<DecisionContributorDto> getContributors() {
        return contributors;
    }

    public void setContributors(List<DecisionContributorDto> contributors) {
        this.contributors = contributors != null ? contributors : new ArrayList<>();
    }

    public void addContributor(DecisionContributorDto contributor) {
        if (this.contributors == null) {
            this.contributors = new ArrayList<>();
        }
        this.contributors.add(contributor);
    }
}
