package com.hazard.dto.relocation.explain;

import com.hazard.domain.relocation.PriorityLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7C.2 — Risk Explanation DTO.
 *
 * Encapsulates the structured explanation of why a source habitation is prioritized at its
 * assigned PriorityLevel and priority score, detailing primary risk drivers and contributor items.
 */
public class RiskExplanationDto {

    private String habitationId;
    private String habitationName;
    private Double priorityScore;
    private PriorityLevel priorityLevel;
    private String riskCategory;           // "CRITICAL_IMMEDIATE", "ELEVATED_SHORT_TERM", "MODERATE_PLANNED", "MONITORING_LOW"
    private String riskNarrative;          // 2-3 sentence cohesive narrative explaining the priority
    private String urgencyContext;         // Context regarding operational timeline and emergency level
    private List<String> primaryRiskDrivers = new ArrayList<>(); // Top 2-3 contributing factors
    private List<DecisionContributorDto> contributors = new ArrayList<>(); // Detailed 6-contributor breakdown

    public RiskExplanationDto() {
        this.primaryRiskDrivers = new ArrayList<>();
        this.contributors = new ArrayList<>();
    }

    // --- Getters & Setters ---

    public String getHabitationId() {
        return habitationId;
    }

    public void setHabitationId(String habitationId) {
        this.habitationId = habitationId;
    }

    public String getHabitationName() {
        return habitationName;
    }

    public void setHabitationName(String habitationName) {
        this.habitationName = habitationName;
    }

    public Double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(Double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public String getRiskNarrative() {
        return riskNarrative;
    }

    public void setRiskNarrative(String riskNarrative) {
        this.riskNarrative = riskNarrative;
    }

    public String getUrgencyContext() {
        return urgencyContext;
    }

    public void setUrgencyContext(String urgencyContext) {
        this.urgencyContext = urgencyContext;
    }

    public List<String> getPrimaryRiskDrivers() {
        return primaryRiskDrivers;
    }

    public void setPrimaryRiskDrivers(List<String> primaryRiskDrivers) {
        this.primaryRiskDrivers = primaryRiskDrivers != null ? primaryRiskDrivers : new ArrayList<>();
    }

    public void addPrimaryRiskDriver(String driver) {
        if (this.primaryRiskDrivers == null) {
            this.primaryRiskDrivers = new ArrayList<>();
        }
        this.primaryRiskDrivers.add(driver);
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
