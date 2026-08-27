package com.hazard.dto.relocation;

import com.hazard.dto.safesite.CandidateSafeSiteDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 6.2 — Feasibility Result DTO for a Vulnerable Habitation.
 *
 * Encapsulates the filtered list of feasible candidate safe sites along with detailed
 * evaluation breakdowns explaining why each evaluated site was accepted or rejected.
 */
public class RelocationFeasibilityResultDto {

    private String habitationId;
    private String habitationName;
    private String district;
    private Long vulnerablePopulation;

    // Constraint Settings Applied
    private Double maxTransitDistanceKm;
    private String minSuitabilityClass;

    // Counts & KPIs
    private int totalCandidatesEvaluated;
    private int feasibleCandidatesCount;
    private int rejectedCandidatesCount;

    // Feasible Sites List
    private List<CandidateSafeSiteDto> feasibleSites = new ArrayList<>();

    // Detailed Evaluation Records for Explainability
    private List<SiteFeasibilityEvaluationDto> evaluations = new ArrayList<>();

    private String summary;
    private LocalDateTime timestamp;

    public RelocationFeasibilityResultDto() {
        this.timestamp = LocalDateTime.now();
    }

    public RelocationFeasibilityResultDto(VulnerableHabitationDto habitation) {
        this();
        if (habitation != null) {
            this.habitationId = habitation.getHabitationId();
            this.habitationName = habitation.getHabitationName();
            this.district = habitation.getDistrict();
            this.vulnerablePopulation = habitation.getVulnerablePopulation();
        }
    }

    /**
     * Adds an evaluation record and updates counts and feasible list.
     */
    public void addEvaluation(SiteFeasibilityEvaluationDto eval) {
        if (eval == null) return;
        this.evaluations.add(eval);
        this.totalCandidatesEvaluated = this.evaluations.size();

        if (eval.isFeasible()) {
            if (eval.getSite() != null) {
                this.feasibleSites.add(eval.getSite());
            }
            this.feasibleCandidatesCount = this.feasibleSites.size();
        }
        this.rejectedCandidatesCount = this.totalCandidatesEvaluated - this.feasibleCandidatesCount;
    }

    public boolean hasFeasibleSites() {
        return !feasibleSites.isEmpty();
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

    public Long getVulnerablePopulation() {
        return vulnerablePopulation;
    }

    public void setVulnerablePopulation(Long vulnerablePopulation) {
        this.vulnerablePopulation = vulnerablePopulation;
    }

    public Double getMaxTransitDistanceKm() {
        return maxTransitDistanceKm;
    }

    public void setMaxTransitDistanceKm(Double maxTransitDistanceKm) {
        this.maxTransitDistanceKm = maxTransitDistanceKm;
    }

    public String getMinSuitabilityClass() {
        return minSuitabilityClass;
    }

    public void setMinSuitabilityClass(String minSuitabilityClass) {
        this.minSuitabilityClass = minSuitabilityClass;
    }

    public int getTotalCandidatesEvaluated() {
        return totalCandidatesEvaluated;
    }

    public void setTotalCandidatesEvaluated(int totalCandidatesEvaluated) {
        this.totalCandidatesEvaluated = totalCandidatesEvaluated;
    }

    public int getFeasibleCandidatesCount() {
        return feasibleCandidatesCount;
    }

    public void setFeasibleCandidatesCount(int feasibleCandidatesCount) {
        this.feasibleCandidatesCount = feasibleCandidatesCount;
    }

    public int getRejectedCandidatesCount() {
        return rejectedCandidatesCount;
    }

    public void setRejectedCandidatesCount(int rejectedCandidatesCount) {
        this.rejectedCandidatesCount = rejectedCandidatesCount;
    }

    public List<CandidateSafeSiteDto> getFeasibleSites() {
        return feasibleSites;
    }

    public void setFeasibleSites(List<CandidateSafeSiteDto> feasibleSites) {
        this.feasibleSites = feasibleSites != null ? feasibleSites : new ArrayList<>();
        this.feasibleCandidatesCount = this.feasibleSites.size();
    }

    public List<SiteFeasibilityEvaluationDto> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<SiteFeasibilityEvaluationDto> evaluations) {
        this.evaluations = evaluations != null ? evaluations : new ArrayList<>();
        this.totalCandidatesEvaluated = this.evaluations.size();
        this.feasibleCandidatesCount = (int) this.evaluations.stream().filter(SiteFeasibilityEvaluationDto::isFeasible).count();
        this.rejectedCandidatesCount = this.totalCandidatesEvaluated - this.feasibleCandidatesCount;
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
