package com.hazard.dto.relocation.explain;

import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7C.6 — Relocation Decision Explanation Root DTO.
 *
 * Root response object for a single case decision explanation. Combines the high-level
 * executive rationale, domain-specific sub-explanations (Risk, Relocation, Capacity),
 * auditable evidence lists, source results (Stage 7A + 7B), and validation status.
 */
public class RelocationDecisionExplanationDto {

    // Identity & Metadata
    private String explanationId;
    private String habitationId;
    private String habitationName;
    private String district;
    private String state;
    private LocalDateTime timestamp;

    // High-Level Executive Rationale (7C.1)
    private DecisionRationaleDto decisionRationale;

    // Structured Domain Sub-Explanations (7C.2, 7C.3, 7C.4)
    private RiskExplanationDto riskExplanation;
    private RelocationExplanationDto relocationExplanation;
    private CapacityExplanationDto capacityExplanation;

    // Structured Evidence Lists (7C.5)
    private List<DecisionContributorDto> priorityEvidence = new ArrayList<>();
    private List<DecisionContributorDto> destinationEvidence = new ArrayList<>();

    // Underlying Source Results
    private RelocationPriorityResultDto priorityResult;
    private RelocationRecommendationDto recommendationResult;

    // Validation Status (7C.8)
    private boolean valid;
    private List<String> validationNotes = new ArrayList<>();

    public RelocationDecisionExplanationDto() {
        this.timestamp = LocalDateTime.now();
        this.valid = true;
        this.priorityEvidence = new ArrayList<>();
        this.destinationEvidence = new ArrayList<>();
        this.validationNotes = new ArrayList<>();
    }

    // --- Getters & Setters ---

    public String getExplanationId() {
        return explanationId;
    }

    public void setExplanationId(String explanationId) {
        this.explanationId = explanationId;
    }

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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public DecisionRationaleDto getDecisionRationale() {
        return decisionRationale;
    }

    public void setDecisionRationale(DecisionRationaleDto decisionRationale) {
        this.decisionRationale = decisionRationale;
    }

    public RiskExplanationDto getRiskExplanation() {
        return riskExplanation;
    }

    public void setRiskExplanation(RiskExplanationDto riskExplanation) {
        this.riskExplanation = riskExplanation;
    }

    public RelocationExplanationDto getRelocationExplanation() {
        return relocationExplanation;
    }

    public void setRelocationExplanation(RelocationExplanationDto relocationExplanation) {
        this.relocationExplanation = relocationExplanation;
    }

    public CapacityExplanationDto getCapacityExplanation() {
        return capacityExplanation;
    }

    public void setCapacityExplanation(CapacityExplanationDto capacityExplanation) {
        this.capacityExplanation = capacityExplanation;
    }

    public List<DecisionContributorDto> getPriorityEvidence() {
        return priorityEvidence;
    }

    public void setPriorityEvidence(List<DecisionContributorDto> priorityEvidence) {
        this.priorityEvidence = priorityEvidence != null ? priorityEvidence : new ArrayList<>();
    }

    public List<DecisionContributorDto> getDestinationEvidence() {
        return destinationEvidence;
    }

    public void setDestinationEvidence(List<DecisionContributorDto> destinationEvidence) {
        this.destinationEvidence = destinationEvidence != null ? destinationEvidence : new ArrayList<>();
    }

    public RelocationPriorityResultDto getPriorityResult() {
        return priorityResult;
    }

    public void setPriorityResult(RelocationPriorityResultDto priorityResult) {
        this.priorityResult = priorityResult;
    }

    public RelocationRecommendationDto getRecommendationResult() {
        return recommendationResult;
    }

    public void setRecommendationResult(RelocationRecommendationDto recommendationResult) {
        this.recommendationResult = recommendationResult;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getValidationNotes() {
        return validationNotes;
    }

    public void setValidationNotes(List<String> validationNotes) {
        this.validationNotes = validationNotes != null ? validationNotes : new ArrayList<>();
    }

    public void addValidationNote(String note) {
        if (this.validationNotes == null) {
            this.validationNotes = new ArrayList<>();
        }
        this.validationNotes.add(note);
    }
}
