package com.hazard.dto.relocation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 6.4 — Relocation Site Ranking Result DTO.
 *
 * Encapsulates the prioritized, ranked list of feasible candidate safe sites
 * for a specific vulnerable habitation.
 */
public class RelocationRankingResultDto {

    private String habitationId;
    private String habitationName;
    private String district;
    private Long vulnerablePopulation;

    private int totalFeasibleSites;
    private List<RankedRelocationSiteDto> rankedSites = new ArrayList<>();

    private String rankingSummary;
    private LocalDateTime timestamp;

    public RelocationRankingResultDto() {
        this.timestamp = LocalDateTime.now();
    }

    public RelocationRankingResultDto(VulnerableHabitationDto habitation) {
        this();
        if (habitation != null) {
            this.habitationId = habitation.getHabitationId();
            this.habitationName = habitation.getHabitationName();
            this.district = habitation.getDistrict();
            this.vulnerablePopulation = habitation.getVulnerablePopulation();
        }
    }

    public void addRankedSite(RankedRelocationSiteDto site) {
        if (site == null) return;
        this.rankedSites.add(site);
        this.totalFeasibleSites = this.rankedSites.size();
    }

    public boolean hasRankedSites() {
        return !rankedSites.isEmpty();
    }

    public RankedRelocationSiteDto getTopRankedSite() {
        return (rankedSites != null && !rankedSites.isEmpty()) ? rankedSites.get(0) : null;
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

    public int getTotalFeasibleSites() {
        return totalFeasibleSites;
    }

    public void setTotalFeasibleSites(int totalFeasibleSites) {
        this.totalFeasibleSites = totalFeasibleSites;
    }

    public List<RankedRelocationSiteDto> getRankedSites() {
        return rankedSites;
    }

    public void setRankedSites(List<RankedRelocationSiteDto> rankedSites) {
        this.rankedSites = rankedSites != null ? rankedSites : new ArrayList<>();
        this.totalFeasibleSites = this.rankedSites.size();
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
