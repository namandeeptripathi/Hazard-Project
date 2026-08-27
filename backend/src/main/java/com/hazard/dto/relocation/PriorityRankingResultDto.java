package com.hazard.dto.relocation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7A.4 — Priority Ranking Result DTO.
 *
 * Encapsulates the fully ranked list of relocation priority results along with
 * summary statistics for each priority tier.
 */
public class PriorityRankingResultDto {

    private int totalCases;
    private List<RelocationPriorityResultDto> rankedPriorities = new ArrayList<>();

    // Tier Distribution Counts
    private int immediateCount;
    private int shortTermCount;
    private int mediumTermCount;
    private int monitoringCount;

    private String rankingSummary;
    private LocalDateTime timestamp;

    public PriorityRankingResultDto() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Recalculates tier distribution counts from the ranked priorities list.
     */
    public void recomputeTierCounts() {
        this.immediateCount = 0;
        this.shortTermCount = 0;
        this.mediumTermCount = 0;
        this.monitoringCount = 0;

        if (rankedPriorities != null) {
            for (RelocationPriorityResultDto result : rankedPriorities) {
                if (result == null || result.getPriorityLevel() == null) continue;
                switch (result.getPriorityLevel()) {
                    case IMMEDIATE -> this.immediateCount++;
                    case SHORT_TERM -> this.shortTermCount++;
                    case MEDIUM_TERM -> this.mediumTermCount++;
                    case MONITORING -> this.monitoringCount++;
                }
            }
        }
        this.totalCases = (rankedPriorities != null) ? rankedPriorities.size() : 0;
    }

    // --- Getters and Setters ---

    public int getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(int totalCases) {
        this.totalCases = totalCases;
    }

    public List<RelocationPriorityResultDto> getRankedPriorities() {
        return rankedPriorities;
    }

    public void setRankedPriorities(List<RelocationPriorityResultDto> rankedPriorities) {
        this.rankedPriorities = rankedPriorities != null ? rankedPriorities : new ArrayList<>();
        recomputeTierCounts();
    }

    public int getImmediateCount() {
        return immediateCount;
    }

    public void setImmediateCount(int immediateCount) {
        this.immediateCount = immediateCount;
    }

    public int getShortTermCount() {
        return shortTermCount;
    }

    public void setShortTermCount(int shortTermCount) {
        this.shortTermCount = shortTermCount;
    }

    public int getMediumTermCount() {
        return mediumTermCount;
    }

    public void setMediumTermCount(int mediumTermCount) {
        this.mediumTermCount = mediumTermCount;
    }

    public int getMonitoringCount() {
        return monitoringCount;
    }

    public void setMonitoringCount(int monitoringCount) {
        this.monitoringCount = monitoringCount;
    }

    public String getRankingSummary() {
        return rankingSummary;
    }

    public void setRankingSummary(String rankingSummary) {
        this.rankingSummary = rankingSummary;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
