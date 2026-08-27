package com.hazard.dto.relocation.explain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7C.6 — Batch Relocation Decision Explanation DTO.
 *
 * Container for multiple case explanations with aggregated macro statistics.
 */
public class BatchRelocationDecisionExplanationDto {

    private int totalCases;
    private int immediateCases;
    private int shortTermCases;
    private int mediumTermCases;
    private int monitoringCases;
    private int validExplanations;
    private int invalidExplanations;

    private List<RelocationDecisionExplanationDto> explanations = new ArrayList<>();
    private String summary;
    private LocalDateTime timestamp;

    public BatchRelocationDecisionExplanationDto() {
        this.timestamp = LocalDateTime.now();
        this.explanations = new ArrayList<>();
    }

    /**
     * Recalculates macro statistics across all case explanations.
     */
    public void recomputeStatistics() {
        this.immediateCases = 0;
        this.shortTermCases = 0;
        this.mediumTermCases = 0;
        this.monitoringCases = 0;
        this.validExplanations = 0;
        this.invalidExplanations = 0;

        if (explanations != null) {
            for (RelocationDecisionExplanationDto exp : explanations) {
                if (exp == null) continue;
                if (exp.isValid()) {
                    this.validExplanations++;
                } else {
                    this.invalidExplanations++;
                }

                if (exp.getPriorityResult() != null && exp.getPriorityResult().getPriorityLevel() != null) {
                    switch (exp.getPriorityResult().getPriorityLevel()) {
                        case IMMEDIATE -> this.immediateCases++;
                        case SHORT_TERM -> this.shortTermCases++;
                        case MEDIUM_TERM -> this.mediumTermCases++;
                        case MONITORING -> this.monitoringCases++;
                    }
                }
            }
            this.totalCases = explanations.size();
        } else {
            this.totalCases = 0;
        }
    }

    // --- Getters & Setters ---

    public int getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(int totalCases) {
        this.totalCases = totalCases;
    }

    public int getImmediateCases() {
        return immediateCases;
    }

    public void setImmediateCases(int immediateCases) {
        this.immediateCases = immediateCases;
    }

    public int getShortTermCases() {
        return shortTermCases;
    }

    public void setShortTermCases(int shortTermCases) {
        this.shortTermCases = shortTermCases;
    }

    public int getMediumTermCases() {
        return mediumTermCases;
    }

    public void setMediumTermCases(int mediumTermCases) {
        this.mediumTermCases = mediumTermCases;
    }

    public int getMonitoringCases() {
        return monitoringCases;
    }

    public void setMonitoringCases(int monitoringCases) {
        this.monitoringCases = monitoringCases;
    }

    public int getValidExplanations() {
        return validExplanations;
    }

    public void setValidExplanations(int validExplanations) {
        this.validExplanations = validExplanations;
    }

    public int getInvalidExplanations() {
        return invalidExplanations;
    }

    public void setInvalidExplanations(int invalidExplanations) {
        this.invalidExplanations = invalidExplanations;
    }

    public List<RelocationDecisionExplanationDto> getExplanations() {
        return explanations;
    }

    public void setExplanations(List<RelocationDecisionExplanationDto> explanations) {
        this.explanations = explanations != null ? explanations : new ArrayList<>();
        recomputeStatistics();
    }

    public void addExplanation(RelocationDecisionExplanationDto explanation) {
        if (this.explanations == null) {
            this.explanations = new ArrayList<>();
        }
        this.explanations.add(explanation);
        recomputeStatistics();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
