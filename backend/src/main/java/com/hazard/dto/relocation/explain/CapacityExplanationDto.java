package com.hazard.dto.relocation.explain;

/**
 * Stage 7C.4 — Capacity Explanation DTO.
 *
 * Encapsulates the structured explanation of shelter capacity, population accommodation,
 * sufficiency tier, and headroom metrics.
 */
public class CapacityExplanationDto {

    private Long requiredPopulation;
    private Integer destinationCapacity;
    private Integer availableCapacity;
    private Long allocatedPopulation;
    private Long unallocatedPopulation;
    private Double capacityFitPercentage;

    private String capacitySufficiencyStatus; // "SUFFICIENT_HEADROOM", "EXACT_MATCH", "UNBOUNDED", "PARTIAL_DEFICIT", "ZERO_CAPACITY", "NO_DESTINATION"
    private String capacityNarrative;         // Narrative detailing population vs capacity balance
    private String headroomMetric;            // Headroom ratio or buffer description (e.g. "+300 surplus beds (150% buffer)")

    public CapacityExplanationDto() {
    }

    // --- Getters & Setters ---

    public Long getRequiredPopulation() {
        return requiredPopulation;
    }

    public void setRequiredPopulation(Long requiredPopulation) {
        this.requiredPopulation = requiredPopulation;
    }

    public Integer getDestinationCapacity() {
        return destinationCapacity;
    }

    public void setDestinationCapacity(Integer destinationCapacity) {
        this.destinationCapacity = destinationCapacity;
    }

    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
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

    public Double getCapacityFitPercentage() {
        return capacityFitPercentage;
    }

    public void setCapacityFitPercentage(Double capacityFitPercentage) {
        this.capacityFitPercentage = capacityFitPercentage;
    }

    public String getCapacitySufficiencyStatus() {
        return capacitySufficiencyStatus;
    }

    public void setCapacitySufficiencyStatus(String capacitySufficiencyStatus) {
        this.capacitySufficiencyStatus = capacitySufficiencyStatus;
    }

    public String getCapacityNarrative() {
        return capacityNarrative;
    }

    public void setCapacityNarrative(String capacityNarrative) {
        this.capacityNarrative = capacityNarrative;
    }

    public String getHeadroomMetric() {
        return headroomMetric;
    }

    public void setHeadroomMetric(String headroomMetric) {
        this.headroomMetric = headroomMetric;
    }
}
