package com.hazard.dto.relocation;

import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.safesite.CandidateSafeSiteDto;

/**
 * Stage 6.2 — Granular Evaluation Record for a Candidate Safe Site's Feasibility.
 *
 * Explains deterministically whether a candidate safe site passed or failed each of the 4 feasibility checks:
 * 1. Safety Gate (must not be AT_RISK or UNSUITABLE)
 * 2. Suitability Gate (must meet or exceed minSuitabilityClass)
 * 3. Capacity Gate (availableCapacity > 0 and availableCapacity >= requiredPopulation, or unbounded)
 * 4. Distance Gate (transit distance <= maxTransitDistanceKm, if specified)
 */
public class SiteFeasibilityEvaluationDto {

    private String siteId;
    private String siteName;
    private String category;
    private String district;

    // Origin Coordinates
    private Double originLatitude;
    private Double originLongitude;

    // Destination Coordinates
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Double latitude;   // Backward-compatible alias for destinationLatitude
    private Double longitude;  // Backward-compatible alias for destinationLongitude

    private boolean feasible;
    private String rejectionReasonCode;   // null if feasible, else e.g. "REJECTED_UNSAFE", "REJECTED_SUITABILITY_BELOW_MINIMUM", "REJECTED_INSUFFICIENT_CAPACITY", "REJECTED_DISTANCE_EXCEEDED", "REJECTED_MISSING_COORDINATES"
    private String explanation;

    // Granular Check Results
    private boolean safetyPassed;
    private HazardSafetyStatus hazardSafetyStatus;

    private boolean suitabilityPassed;
    private SuitabilityClass suitabilityClass;
    private Double suitabilityScore;

    private boolean capacityPassed;
    private Integer totalCapacity;
    private Integer allocatedOccupancy;
    private Integer availableCapacity;
    private Long requiredPopulation;

    private boolean distancePassed;
    private boolean distanceAvailable;
    private Double transitDistanceMeters;
    private Double transitDistanceKilometers;
    private Double maxAllowableDistanceKm;

    // Stage 6.4 Ranking Fields
    private Integer rank;
    private String rankingReason;

    private CandidateSafeSiteDto site;

    public SiteFeasibilityEvaluationDto() {
    }

    public SiteFeasibilityEvaluationDto(CandidateSafeSiteDto site) {
        this.site = site;
        if (site != null) {
            this.siteId = site.getSiteId();
            this.siteName = site.getSiteName();
            this.category = site.getCategory() != null ? site.getCategory().name() : null;
            this.district = site.getDistrict();
            this.destinationLatitude = site.getLatitude();
            this.destinationLongitude = site.getLongitude();
            this.latitude = site.getLatitude();
            this.longitude = site.getLongitude();
            this.hazardSafetyStatus = site.getHazardSafetyStatus();
            this.suitabilityClass = site.getSuitabilityClass();
            this.suitabilityScore = site.getSuitabilityScore();
            this.totalCapacity = site.getCapacity();
            this.allocatedOccupancy = site.getAllocatedOccupancy();
            this.availableCapacity = site.getAvailableCapacity();
        }
    }

    // --- Getters and Setters ---

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

    public Double getDestinationLatitude() {
        return destinationLatitude != null ? destinationLatitude : latitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
        this.latitude = destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude != null ? destinationLongitude : longitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
        this.longitude = destinationLongitude;
    }

    public Double getLatitude() {
        return getDestinationLatitude();
    }

    public void setLatitude(Double latitude) {
        setDestinationLatitude(latitude);
    }

    public Double getLongitude() {
        return getDestinationLongitude();
    }

    public void setLongitude(Double longitude) {
        setDestinationLongitude(longitude);
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

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public boolean isSafetyPassed() {
        return safetyPassed;
    }

    public void setSafetyPassed(boolean safetyPassed) {
        this.safetyPassed = safetyPassed;
    }

    public HazardSafetyStatus getHazardSafetyStatus() {
        return hazardSafetyStatus;
    }

    public void setHazardSafetyStatus(HazardSafetyStatus hazardSafetyStatus) {
        this.hazardSafetyStatus = hazardSafetyStatus;
    }

    public boolean isSuitabilityPassed() {
        return suitabilityPassed;
    }

    public void setSuitabilityPassed(boolean suitabilityPassed) {
        this.suitabilityPassed = suitabilityPassed;
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

    public boolean isCapacityPassed() {
        return capacityPassed;
    }

    public void setCapacityPassed(boolean capacityPassed) {
        this.capacityPassed = capacityPassed;
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

    public Long getRequiredPopulation() {
        return requiredPopulation;
    }

    public void setRequiredPopulation(Long requiredPopulation) {
        this.requiredPopulation = requiredPopulation;
    }

    public boolean isDistancePassed() {
        return distancePassed;
    }

    public void setDistancePassed(boolean distancePassed) {
        this.distancePassed = distancePassed;
    }

    public boolean isDistanceAvailable() {
        return distanceAvailable;
    }

    public void setDistanceAvailable(boolean distanceAvailable) {
        this.distanceAvailable = distanceAvailable;
    }

    public Double getTransitDistanceMeters() {
        return transitDistanceMeters;
    }

    public void setTransitDistanceMeters(Double transitDistanceMeters) {
        this.transitDistanceMeters = transitDistanceMeters;
        if (transitDistanceMeters != null) {
            this.transitDistanceKilometers = Math.round((transitDistanceMeters / 1000.0) * 100.0) / 100.0;
            this.distanceAvailable = true;
        } else {
            this.transitDistanceKilometers = null;
            this.distanceAvailable = false;
        }
    }

    public Double getTransitDistanceKilometers() {
        return transitDistanceKilometers;
    }

    public void setTransitDistanceKilometers(Double transitDistanceKilometers) {
        this.transitDistanceKilometers = transitDistanceKilometers;
        if (transitDistanceKilometers != null) {
            this.distanceAvailable = true;
        }
    }

    public Double getDistanceMeters() {
        return getTransitDistanceMeters();
    }

    public void setDistanceMeters(Double distanceMeters) {
        setTransitDistanceMeters(distanceMeters);
    }

    public Double getDistanceKilometers() {
        return getTransitDistanceKilometers();
    }

    public void setDistanceKilometers(Double distanceKilometers) {
        setTransitDistanceKilometers(distanceKilometers);
    }

    public Double getMaxAllowableDistanceKm() {
        return maxAllowableDistanceKm;
    }

    public void setMaxAllowableDistanceKm(Double maxAllowableDistanceKm) {
        this.maxAllowableDistanceKm = maxAllowableDistanceKm;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
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
