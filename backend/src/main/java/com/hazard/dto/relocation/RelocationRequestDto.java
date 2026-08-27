package com.hazard.dto.relocation;

import com.hazard.domain.safesite.SuitabilityClass;

/**
 * Stage 6.1 — Relocation Plan Request DTO.
 *
 * Encapsulates parameters for generating automated evacuation and shelter allocation recommendations:
 * - district: target administrative district
 * - hazardId: specific Stage 3 hazard event identifier (optional)
 * - hazardType: target hazard category (FLOOD, EXTREME_RAINFALL, etc.)
 * - maxTransitDistanceKm: maximum allowable transit distance between origin and shelter
 * - minSuitabilityClass: minimum acceptable shelter suitability tier (default: MARGINAL)
 * - prioritizeRedZones: whether critical red zones must be prioritized first (default: true)
 * - defaultSiteCapacity: fallback capacity for candidate sites without specified capacity in OSM data
 * - allocationStrategy: optimization strategy (e.g. "NEAREST_SUITABLE", "HIGHEST_SUITABILITY", "CAPACITY_BALANCED")
 */
public class RelocationRequestDto {

    private String district;
    private String hazardId;
    private String hazardType;
    private String habitationId;
    private VulnerableHabitationDto habitation;
    private Double originLatitude;
    private Double originLongitude;
    private Long vulnerablePopulation;

    private Double maxTransitDistanceKm = 25.0; // Default 25 km max emergency transit radius
    private SuitabilityClass minSuitabilityClass = SuitabilityClass.MARGINAL;
    private boolean prioritizeRedZones = true;
    private Integer defaultSiteCapacity = 500;  // Realistic fallback capacity for unmeasured public shelters
    private String allocationStrategy = "NEAREST_SUITABLE";

    public RelocationRequestDto() {
    }

    public RelocationRequestDto(String district) {
        this.district = district;
    }

    public RelocationRequestDto(String district, String hazardType, Double maxTransitDistanceKm) {
        this.district = district;
        this.hazardType = hazardType;
        if (maxTransitDistanceKm != null && maxTransitDistanceKm > 0.0) {
            this.maxTransitDistanceKm = maxTransitDistanceKm;
        }
    }

    // --- Getters and Setters ---

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getHazardId() {
        return hazardId;
    }

    public void setHazardId(String hazardId) {
        this.hazardId = hazardId;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public Double getMaxTransitDistanceKm() {
        return maxTransitDistanceKm;
    }

    public void setMaxTransitDistanceKm(Double maxTransitDistanceKm) {
        this.maxTransitDistanceKm = maxTransitDistanceKm;
    }

    public SuitabilityClass getMinSuitabilityClass() {
        return minSuitabilityClass;
    }

    public void setMinSuitabilityClass(SuitabilityClass minSuitabilityClass) {
        this.minSuitabilityClass = minSuitabilityClass;
    }

    public boolean isPrioritizeRedZones() {
        return prioritizeRedZones;
    }

    public void setPrioritizeRedZones(boolean prioritizeRedZones) {
        this.prioritizeRedZones = prioritizeRedZones;
    }

    public Integer getDefaultSiteCapacity() {
        return defaultSiteCapacity;
    }

    public void setDefaultSiteCapacity(Integer defaultSiteCapacity) {
        this.defaultSiteCapacity = defaultSiteCapacity;
    }

    public String getHabitationId() {
        return habitationId;
    }

    public void setHabitationId(String habitationId) {
        this.habitationId = habitationId;
    }

    public VulnerableHabitationDto getHabitation() {
        return habitation;
    }

    public void setHabitation(VulnerableHabitationDto habitation) {
        this.habitation = habitation;
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

    public String getAllocationStrategy() {
        return allocationStrategy;
    }

    public void setAllocationStrategy(String allocationStrategy) {
        this.allocationStrategy = allocationStrategy;
    }
}
