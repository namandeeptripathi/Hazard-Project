package com.hazard.dto.relocation;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.domain.relocation.RelocationUrgency;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7B.1 — Relocation Recommendation DTO.
 *
 * Represents the complete destination recommendation for a single prioritized relocation case.
 * Combines origin habitation details, Stage 7A priority classification, primary recommended destination,
 * fallback alternative destinations, population accommodation metrics, and transparent status summary.
 */
public class RelocationRecommendationDto {

    // Identity
    private String recommendationId;
    private String planId;

    // Origin Habitation Context
    private String habitationId;
    private String habitationName;
    private String district;
    private String state;
    private Double originLatitude;
    private Double originLongitude;
    private Long vulnerablePopulation;
    private RelocationUrgency urgency;

    // Stage 7A Priority Context
    private Double priorityScore;
    private PriorityLevel priorityLevel;
    private Integer priorityRank;

    // Recommendation Outcomes
    private RecommendationStatus status;
    private boolean feasible;
    private RecommendedDestinationDto primaryDestination;
    private List<RecommendedDestinationDto> alternativeDestinations = new ArrayList<>();

    // Candidate Counts & Accommodation Statistics
    private int totalCandidatesEvaluated;
    private int totalFeasibleCandidates;
    private Long allocatedPopulation;
    private Long unallocatedPopulation;
    private Double capacityFitRatePercentage;

    // Operational Summary
    private String recommendationSummary;
    private LocalDateTime timestamp;

    public RelocationRecommendationDto() {
        this.timestamp = LocalDateTime.now();
        this.alternativeDestinations = new ArrayList<>();
    }

    public RelocationRecommendationDto(String habitationId, RecommendationStatus status) {
        this();
        this.habitationId = habitationId;
        this.status = status;
        this.feasible = (status == RecommendationStatus.RECOMMENDED);
    }

    // --- Getters & Setters ---

    public String getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(String recommendationId) {
        this.recommendationId = recommendationId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
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

    public Double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(Double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public Double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(Double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public Long getVulnerablePopulation() {
        return vulnerablePopulation;
    }

    public void setVulnerablePopulation(Long vulnerablePopulation) {
        this.vulnerablePopulation = vulnerablePopulation;
    }

    public RelocationUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RelocationUrgency urgency) {
        this.urgency = urgency;
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

    public RecommendationStatus getStatus() {
        return status;
    }

    public void setStatus(RecommendationStatus status) {
        this.status = status;
        this.feasible = (status == RecommendationStatus.RECOMMENDED);
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    public RecommendedDestinationDto getPrimaryDestination() {
        return primaryDestination;
    }

    public void setPrimaryDestination(RecommendedDestinationDto primaryDestination) {
        this.primaryDestination = primaryDestination;
    }

    public List<RecommendedDestinationDto> getAlternativeDestinations() {
        return alternativeDestinations;
    }

    public void setAlternativeDestinations(List<RecommendedDestinationDto> alternativeDestinations) {
        this.alternativeDestinations = alternativeDestinations != null ? alternativeDestinations : new ArrayList<>();
    }

    public void addAlternativeDestination(RecommendedDestinationDto alternative) {
        if (this.alternativeDestinations == null) {
            this.alternativeDestinations = new ArrayList<>();
        }
        this.alternativeDestinations.add(alternative);
    }

    public int getTotalCandidatesEvaluated() {
        return totalCandidatesEvaluated;
    }

    public void setTotalCandidatesEvaluated(int totalCandidatesEvaluated) {
        this.totalCandidatesEvaluated = totalCandidatesEvaluated;
    }

    public int getTotalFeasibleCandidates() {
        return totalFeasibleCandidates;
    }

    public void setTotalFeasibleCandidates(int totalFeasibleCandidates) {
        this.totalFeasibleCandidates = totalFeasibleCandidates;
    }

    public Long getAllocatedPopulation() {
        return allocatedPopulation;
    }

    public void setAllocatedPopulation(Long allocatedPopulation) {
        this.allocatedPopulation = allocatedPopulation;
    }

    public Long getUnallocatedPopulation() {
        return unallocatedPopulation;
    }

    public void setUnallocatedPopulation(Long unallocatedPopulation) {
        this.unallocatedPopulation = unallocatedPopulation;
    }

    public Double getCapacityFitRatePercentage() {
        return capacityFitRatePercentage;
    }

    public void setCapacityFitRatePercentage(Double capacityFitRatePercentage) {
        this.capacityFitRatePercentage = capacityFitRatePercentage;
    }

    public String getRecommendationSummary() {
        return recommendationSummary;
    }

    public void setRecommendationSummary(String recommendationSummary) {
        this.recommendationSummary = recommendationSummary;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
