package com.hazard.dto.relocation;

import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.safesite.CandidateSafeSiteDto;

/**
 * Stage 6.4 — Ranked Feasible Candidate Safe Site DTO.
 *
 * Represents an individual feasible candidate safe site positioned in the prioritized
 * relocation ranking order (1 = best destination) for a specific vulnerable habitation.
 */
public class RankedRelocationSiteDto {

    private int rank;
    private String siteId;
    private String siteName;
    private String category;
    private String district;

    private Double destinationLatitude;
    private Double destinationLongitude;

    // Suitability metrics
    private SuitabilityClass suitabilityClass;
    private Double suitabilityScore;

    // Distance metrics
    private Double distanceMeters;
    private Double distanceKilometers;
    private boolean distanceAvailable;

    // Capacity metrics (read-only)
    private Integer totalCapacity;
    private Integer allocatedOccupancy;
    private Integer availableCapacity;

    // Explainability
    private String rankingReason;

    private CandidateSafeSiteDto site;

    public RankedRelocationSiteDto() {
    }

    public RankedRelocationSiteDto(int rank, SiteFeasibilityEvaluationDto eval, int totalFeasible) {
        this.rank = rank;
        if (eval != null) {
            this.siteId = eval.getSiteId();
            this.siteName = eval.getSiteName();
            this.category = eval.getCategory();
            this.district = eval.getDistrict();
            this.destinationLatitude = eval.getDestinationLatitude();
            this.destinationLongitude = eval.getDestinationLongitude();
            this.suitabilityClass = eval.getSuitabilityClass();
            this.suitabilityScore = eval.getSuitabilityScore();
            this.distanceMeters = eval.getDistanceMeters();
            this.distanceKilometers = eval.getDistanceKilometers();
            this.distanceAvailable = eval.isDistanceAvailable();
            this.totalCapacity = eval.getTotalCapacity();
            this.allocatedOccupancy = eval.getAllocatedOccupancy();
            this.availableCapacity = eval.getAvailableCapacity();
            this.site = eval.getSite();

            String distStr = eval.isDistanceAvailable() && eval.getDistanceKilometers() != null
                    ? String.format("%.2f km", eval.getDistanceKilometers())
                    : "Distance unavailable";

            String capStr = eval.getAvailableCapacity() != null
                    ? eval.getAvailableCapacity().toString()
                    : "Unbounded";

            this.rankingReason = String.format(
                    "Rank %d of %d: %s (Score: %.1f), Transit Distance: %s, Available Capacity: %s",
                    rank,
                    totalFeasible,
                    eval.getSuitabilityClass() != null ? eval.getSuitabilityClass().name() : "UNKNOWN",
                    eval.getSuitabilityScore() != null ? eval.getSuitabilityScore() : 0.0,
                    distStr,
                    capStr
            );
        }
    }

    // --- Getters and Setters ---

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public SuitabilityClass getSuitabilityClass() {
        return suitabilityClass;
    }

    public void setSuitabilityClass(SuitabilityClass suitabilityClass) {
        this.suitabilityClass = suitabilityClass;
    }

    public Double getSuitabilityScore() {
        return suitabilityScore;
    }

    public void setSuitabilityScore(Double suitabilityScore) {
        this.suitabilityScore = suitabilityScore;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Double getDistanceKilometers() {
        return distanceKilometers;
    }

    public void setDistanceKilometers(Double distanceKilometers) {
        this.distanceKilometers = distanceKilometers;
    }

    public boolean isDistanceAvailable() {
        return distanceAvailable;
    }

    public void setDistanceAvailable(boolean distanceAvailable) {
        this.distanceAvailable = distanceAvailable;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public Integer getAllocatedOccupancy() {
        return allocatedOccupancy;
    }

    public void setAllocatedOccupancy(Integer allocatedOccupancy) {
        this.allocatedOccupancy = allocatedOccupancy;
    }

    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public String getRankingReason() {
        return rankingReason;
    }

    public void setRankingReason(String rankingReason) {
        this.rankingReason = rankingReason;
    }

    public CandidateSafeSiteDto getSite() {
        return site;
    }

    public void setSite(CandidateSafeSiteDto site) {
        this.site = site;
    }
}
