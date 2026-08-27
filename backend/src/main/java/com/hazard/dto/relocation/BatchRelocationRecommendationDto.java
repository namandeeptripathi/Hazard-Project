package com.hazard.dto.relocation;

import com.hazard.domain.relocation.RecommendationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7B.1 — Batch Relocation Recommendation DTO.
 *
 * Encapsulates the complete set of relocation recommendations for multiple prioritized cases
 * along with macro-level operational statistics (successful vs unfeasible recommendations).
 */
public class BatchRelocationRecommendationDto {

    private int totalCases;
    private int successfulRecommendations;
    private int noFeasibleRecommendations;
    private int capacityDeficitRecommendations;
    private int invalidSourceRecommendations;

    private List<RelocationRecommendationDto> recommendations = new ArrayList<>();
    private String summary;
    private LocalDateTime timestamp;

    public BatchRelocationRecommendationDto() {
        this.timestamp = LocalDateTime.now();
        this.recommendations = new ArrayList<>();
    }

    /**
     * Recalculates summary statistics across all recommendations.
     */
    public void recomputeStatistics() {
        this.successfulRecommendations = 0;
        this.noFeasibleRecommendations = 0;
        this.capacityDeficitRecommendations = 0;
        this.invalidSourceRecommendations = 0;

        if (recommendations != null) {
            for (RelocationRecommendationDto rec : recommendations) {
                if (rec == null || rec.getStatus() == null) continue;
                switch (rec.getStatus()) {
                    case RECOMMENDED -> this.successfulRecommendations++;
                    case NO_FEASIBLE_DESTINATION -> this.noFeasibleRecommendations++;
                    case CAPACITY_DEFICIT -> this.capacityDeficitRecommendations++;
                    case INVALID_SOURCE -> this.invalidSourceRecommendations++;
                }
            }
            this.totalCases = recommendations.size();
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

    public int getSuccessfulRecommendations() {
        return successfulRecommendations;
    }

    public void setSuccessfulRecommendations(int successfulRecommendations) {
        this.successfulRecommendations = successfulRecommendations;
    }

    public int getNoFeasibleRecommendations() {
        return noFeasibleRecommendations;
    }

    public void setNoFeasibleRecommendations(int noFeasibleRecommendations) {
        this.noFeasibleRecommendations = noFeasibleRecommendations;
    }

    public int getCapacityDeficitRecommendations() {
        return capacityDeficitRecommendations;
    }

    public void setCapacityDeficitRecommendations(int capacityDeficitRecommendations) {
        this.capacityDeficitRecommendations = capacityDeficitRecommendations;
    }

    public int getInvalidSourceRecommendations() {
        return invalidSourceRecommendations;
    }

    public void setInvalidSourceRecommendations(int invalidSourceRecommendations) {
        this.invalidSourceRecommendations = invalidSourceRecommendations;
    }

    public List<RelocationRecommendationDto> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RelocationRecommendationDto> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
        recomputeStatistics();
    }

    public void addRecommendation(RelocationRecommendationDto recommendation) {
        if (this.recommendations == null) {
            this.recommendations = new ArrayList<>();
        }
        this.recommendations.add(recommendation);
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
