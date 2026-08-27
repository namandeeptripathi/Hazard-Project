package com.hazard.dto.relocation;

import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage 7B.1 — Recommended Destination DTO.
 *
 * Represents an evaluated and ranked safe site candidate for an emergency relocation case.
 * Contains destination metadata, safety status, transit distance, capacity metrics,
 * composite suitability score, rank, and contributor breakdown for Stage 7C explainability.
 */
public class RecommendedDestinationDto {

    // Identity & Metadata
    private String siteId;
    private String siteName;
    private CandidateSiteCategory category;
    private String categoryDisplayName;
    private String district;
    private String state;
    private Double latitude;
    private Double longitude;

    // Safety & Suitability
    private HazardSafetyStatus hazardSafetyStatus;
    private SuitabilityClass suitabilityClass;
    private Double suitabilityScore;

    // Distance & Proximity
    private Double distanceMeters;
    private Double distanceKilometers;

    // Shelter Capacity & Allocation Metrics
    private Integer totalCapacity;
    private Integer allocatedOccupancy;
    private Integer availableCapacity;
    private Long accommodatablePopulation; // Number of evacuees this site can take for this case

    // Recommendation Scoring & Ranking
    private Double destinationScore;       // Composite score in [0.0, 1.0]
    private Integer destinationRank;        // 1-based rank (1 = best recommended destination)
    private boolean feasible;
    private String rejectionReasonCode;
    private String rejectionReason;

    // Contributor Breakdown for Stage 7C Explainability
    private Map<String, Double> scoringContributors = new LinkedHashMap<>();

    public RecommendedDestinationDto() {
    }

    public RecommendedDestinationDto(String siteId, String siteName, double destinationScore, int destinationRank) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.destinationScore = destinationScore;
        this.destinationRank = destinationRank;
        this.feasible = true;
    }

    // --- Getters & Setters ---

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

    public CandidateSiteCategory getCategory() {
        return category;
    }

    public void setCategory(CandidateSiteCategory category) {
        this.category = category;
        if (category != null) {
            this.categoryDisplayName = category.getDisplayName();
        }
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public HazardSafetyStatus getHazardSafetyStatus() {
        return hazardSafetyStatus;
    }

    public void setHazardSafetyStatus(HazardSafetyStatus hazardSafetyStatus) {
        this.hazardSafetyStatus = hazardSafetyStatus;
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

    public Long getAccommodatablePopulation() {
        return accommodatablePopulation;
    }

    public void setAccommodatablePopulation(Long accommodatablePopulation) {
        this.accommodatablePopulation = accommodatablePopulation;
    }

    public Double getDestinationScore() {
        return destinationScore;
    }

    public void setDestinationScore(Double destinationScore) {
        this.destinationScore = destinationScore;
    }

    public Integer getDestinationRank() {
        return destinationRank;
    }

    public void setDestinationRank(Integer destinationRank) {
        this.destinationRank = destinationRank;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    public String getRejectionReasonCode() {
        return rejectionReasonCode;
    }

    public void setRejectionReasonCode(String rejectionReasonCode) {
        this.rejectionReasonCode = rejectionReasonCode;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Map<String, Double> getScoringContributors() {
        return scoringContributors;
    }

    public void setScoringContributors(Map<String, Double> scoringContributors) {
        this.scoringContributors = scoringContributors != null ? scoringContributors : new LinkedHashMap<>();
    }

    public void addScoringContributor(String contributor, double score) {
        this.scoringContributors.put(contributor, score);
    }
}
