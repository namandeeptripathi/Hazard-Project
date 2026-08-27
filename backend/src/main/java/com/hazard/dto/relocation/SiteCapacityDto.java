package com.hazard.dto.relocation;

import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.dto.safesite.CandidateSafeSiteDto;

/**
 * Stage 6.1 — Safe Site Shelter Capacity State DTO.
 *
 * Tracks total shelter capacity, currently allocated evacuee load, remaining capacity,
 * and occupancy percentage for a candidate safe site.
 */
public class SiteCapacityDto {

    private String siteId;
    private String siteName;
    private String district;
    private CandidateSiteCategory category;

    private Integer totalCapacity;       // Nullable if not specified in source data
    private Integer allocatedOccupancy;  // Number of people assigned
    private Integer availableCapacity;   // totalCapacity - allocatedOccupancy (or null if unbounded)
    private Double occupancyRate;        // [0.0000, 1.0000]
    private Double occupancyPercentage;  // [0.0, 100.0]
    private boolean isFull;
    private boolean hasAvailableCapacity;

    public SiteCapacityDto() {
        this.allocatedOccupancy = 0;
        this.hasAvailableCapacity = true;
    }

    public SiteCapacityDto(String siteId, String siteName, String district, CandidateSiteCategory category, Integer totalCapacity) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.district = district;
        this.category = category;
        this.totalCapacity = totalCapacity;
        this.allocatedOccupancy = 0;
        recalculate();
    }

    /**
     * Factory: builds a SiteCapacityDto from an existing CandidateSafeSiteDto.
     */
    public static SiteCapacityDto fromCandidateSafeSite(CandidateSafeSiteDto site) {
        if (site == null) {
            return null;
        }

        SiteCapacityDto dto = new SiteCapacityDto();
        dto.setSiteId(site.getSiteId());
        dto.setSiteName(site.getSiteName());
        dto.setDistrict(site.getDistrict());
        dto.setCategory(site.getCategory());
        dto.setTotalCapacity(site.getCapacity());
        dto.setAllocatedOccupancy(site.getAllocatedOccupancy() != null ? site.getAllocatedOccupancy() : 0);
        dto.recalculate();
        return dto;
    }

    /**
     * Recalculates available capacity, occupancy rate, and boolean fullness flags.
     */
    public void recalculate() {
        if (totalCapacity == null) {
            this.availableCapacity = null;
            this.occupancyRate = 0.0;
            this.occupancyPercentage = 0.0;
            this.isFull = false;
            this.hasAvailableCapacity = true;
            return;
        }

        int occ = (allocatedOccupancy != null) ? allocatedOccupancy : 0;
        this.availableCapacity = Math.max(0, totalCapacity - occ);

        if (totalCapacity > 0) {
            this.occupancyRate = Math.min(1.0, (double) occ / totalCapacity);
            this.occupancyPercentage = Math.round(this.occupancyRate * 10000.0) / 100.0;
        } else {
            this.occupancyRate = 1.0;
            this.occupancyPercentage = 100.0;
        }

        this.isFull = (this.availableCapacity <= 0);
        this.hasAvailableCapacity = (this.availableCapacity > 0);
    }

    /**
     * Allocates a given number of people to this site, updating capacity metrics.
     * Returns the actual number of people successfully accommodated.
     */
    public int allocate(int peopleCount) {
        if (peopleCount <= 0) {
            return 0;
        }

        if (totalCapacity == null) {
            // Unbounded capacity
            this.allocatedOccupancy = (this.allocatedOccupancy != null ? this.allocatedOccupancy : 0) + peopleCount;
            recalculate();
            return peopleCount;
        }

        int currentOcc = (this.allocatedOccupancy != null) ? this.allocatedOccupancy : 0;
        int remaining = Math.max(0, totalCapacity - currentOcc);
        int toAllocate = Math.min(remaining, peopleCount);

        this.allocatedOccupancy = currentOcc + toAllocate;
        recalculate();
        return toAllocate;
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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public CandidateSiteCategory getCategory() {
        return category;
    }

    public void setCategory(CandidateSiteCategory category) {
        this.category = category;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
        recalculate();
    }

    public Integer getAllocatedOccupancy() {
        return allocatedOccupancy;
    }

    public void setAllocatedOccupancy(Integer allocatedOccupancy) {
        this.allocatedOccupancy = allocatedOccupancy;
        recalculate();
    }

    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public Double getOccupancyRate() {
        return occupancyRate;
    }

    public Double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public boolean isFull() {
        return isFull;
    }

    public boolean hasAvailableCapacity() {
        return hasAvailableCapacity;
    }
}
