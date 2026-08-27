package com.hazard.dto.relocation;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RelocationUrgency;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage 7A.1 — Relocation Priority Result DTO.
 *
 * Represents a single relocation case that has been scored, classified, and ranked
 * by the Priority Engine. Preserves relevant identifiers and source relocation information
 * for downstream consumption by Stage 7B (Recommendation) and Stage 7C (Explainability).
 */
public class RelocationPriorityResultDto {

    // --- Identity (preserved from source) ---
    private String habitationId;
    private String habitationName;
    private String district;
    private String state;
    private String planId;

    // --- Priority Scoring ---
    private Double priorityScore;          // [0.0, 1.0]
    private PriorityLevel priorityLevel;
    private Integer priorityRank;          // 1-based, 1 = highest priority

    // --- Scoring Contributors (for Stage 7C Explainability) ---
    private Map<String, Double> scoringContributors = new LinkedHashMap<>();

    // --- Source Context ---
    private Long vulnerablePopulation;
    private Long unallocatedPopulation;
    private Double allocationRatePercentage;
    private String overallStatus;
    private RelocationUrgency urgency;
    private Double riskScore;
    private Double hazardSeverityScore;
    private boolean redZone;

    // --- Metadata ---
    private LocalDateTime timestamp;

    public RelocationPriorityResultDto() {
        this.timestamp = LocalDateTime.now();
    }

    public RelocationPriorityResultDto(String habitationId, double priorityScore, PriorityLevel priorityLevel) {
        this();
        this.habitationId = habitationId;
        this.priorityScore = priorityScore;
        this.priorityLevel = priorityLevel;
    }

    // --- Getters and Setters ---

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

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
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

    public Integer getPriorityRank() {
        return priorityRank;
    }

    public void setPriorityRank(Integer priorityRank) {
        this.priorityRank = priorityRank;
    }

    public Map<String, Double> getScoringContributors() {
        return scoringContributors;
    }

    public void setScoringContributors(Map<String, Double> scoringContributors) {
        this.scoringContributors = scoringContributors != null ? scoringContributors : new LinkedHashMap<>();
    }

    public void addScoringContributor(String name, double value) {
        this.scoringContributors.put(name, value);
    }

    public Long getVulnerablePopulation() {
        return vulnerablePopulation;
    }

    public void setVulnerablePopulation(Long vulnerablePopulation) {
        this.vulnerablePopulation = vulnerablePopulation;
    }

    public Long getUnallocatedPopulation() {
        return unallocatedPopulation;
    }

    public void setUnallocatedPopulation(Long unallocatedPopulation) {
        this.unallocatedPopulation = unallocatedPopulation;
    }

    public Double getAllocationRatePercentage() {
        return allocationRatePercentage;
    }

    public void setAllocationRatePercentage(Double allocationRatePercentage) {
        this.allocationRatePercentage = allocationRatePercentage;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public RelocationUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RelocationUrgency urgency) {
        this.urgency = urgency;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Double getHazardSeverityScore() {
        return hazardSeverityScore;
    }

    public void setHazardSeverityScore(Double hazardSeverityScore) {
        this.hazardSeverityScore = hazardSeverityScore;
    }

    public boolean isRedZone() {
        return redZone;
    }

    public void setRedZone(boolean redZone) {
        this.redZone = redZone;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
