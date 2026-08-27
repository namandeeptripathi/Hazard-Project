package com.hazard.dto.relocation;

import com.hazard.domain.relocation.RelocationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 6.1 — Relocation Plan DTO.
 *
 * Represents an aggregated emergency evacuation and shelter relocation plan for an administrative district
 * or disaster-impacted region, including summary KPIs, capacity utilization metrics, and individual site assignments.
 */
public class RelocationPlanDto {

    private String planId;
    private String district;
    private String state;
    private String hazardIdentifier;
    private String hazardType;

    // Population Metrics
    private Integer totalHabitations;
    private Long totalVulnerablePopulation;
    private Long totalAllocatedPopulation;
    private Long totalUnallocatedPopulation;
    private Double allocationRatePercentage;

    // Site & Capacity Metrics
    private Integer totalCandidateSitesEvaluated;
    private Integer totalCandidateSitesUtilized;
    private Integer totalCapacityAvailable;
    private Integer totalCapacityUtilized;
    private Double capacityUtilizationPercentage;

    // Granular Assignments & Unallocated Deficits
    private List<RelocationAssignmentDto> assignments = new ArrayList<>();
    private List<VulnerableHabitationDto> unallocatedHabitations = new ArrayList<>();

    // Overall Plan Status & Deficit Accounting (Stage 6.6)
    private RelocationStatus overallStatus = RelocationStatus.PENDING;
    private String deficitReasonCode;  // NO_FEASIBLE_SITE, CAPACITY_EXHAUSTED, PARTIAL_CAPACITY, FULLY_ALLOCATED
    private String deficitExplanation;

    // Metadata
    private String strategy;
    private String planSummary;
    private LocalDateTime generationTimestamp;

    public RelocationPlanDto() {
        this.state = "Bihar";
        this.totalHabitations = 0;
        this.totalVulnerablePopulation = 0L;
        this.totalAllocatedPopulation = 0L;
        this.totalUnallocatedPopulation = 0L;
        this.allocationRatePercentage = 0.0;
        this.totalCandidateSitesEvaluated = 0;
        this.totalCandidateSitesUtilized = 0;
        this.totalCapacityAvailable = 0;
        this.totalCapacityUtilized = 0;
        this.capacityUtilizationPercentage = 0.0;
        this.generationTimestamp = LocalDateTime.now();
    }

    /**
     * Helper to recompute aggregated totals based on current assignments and unallocated lists.
     */
    public void recomputeTotals() {
        long allocatedPop = 0;
        for (RelocationAssignmentDto a : assignments) {
            if (a.getAllocatedPopulation() != null) {
                allocatedPop += a.getAllocatedPopulation();
            }
        }
        this.totalAllocatedPopulation = allocatedPop;

        long unallocatedPop = 0;
        for (VulnerableHabitationDto h : unallocatedHabitations) {
            if (h.getVulnerablePopulation() != null) {
                unallocatedPop += h.getVulnerablePopulation();
            }
        }
        this.totalUnallocatedPopulation = unallocatedPop;

        this.totalVulnerablePopulation = this.totalAllocatedPopulation + this.totalUnallocatedPopulation;
        this.totalHabitations = this.assignments.size() + this.unallocatedHabitations.size();

        if (this.totalVulnerablePopulation > 0) {
            this.allocationRatePercentage = Math.round(((double) this.totalAllocatedPopulation / this.totalVulnerablePopulation * 100.0) * 100.0) / 100.0;
        } else {
            this.allocationRatePercentage = 100.0;
        }

        if (this.totalCapacityAvailable != null && this.totalCapacityAvailable > 0) {
            this.capacityUtilizationPercentage = Math.round(((double) this.totalCapacityUtilized / this.totalCapacityAvailable * 100.0) * 100.0) / 100.0;
        } else {
            this.capacityUtilizationPercentage = 0.0;
        }

        // Determine overall relocation status
        if (this.totalVulnerablePopulation == null || this.totalVulnerablePopulation <= 0) {
            this.overallStatus = RelocationStatus.ALLOCATED;
        } else if (this.totalUnallocatedPopulation <= 0) {
            this.overallStatus = RelocationStatus.ALLOCATED;
        } else if (this.totalAllocatedPopulation > 0) {
            this.overallStatus = RelocationStatus.PARTIALLY_ALLOCATED;
        } else if (this.assignments.isEmpty() && !this.unallocatedHabitations.isEmpty()) {
            this.overallStatus = this.unallocatedHabitations.get(0).getRelocationStatus() != null
                    ? this.unallocatedHabitations.get(0).getRelocationStatus()
                    : RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED;
        } else {
            this.overallStatus = RelocationStatus.UNALLOCATED_CAPACITY_EXCEEDED;
        }
    }

    // --- Getters and Setters ---

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
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

    public String getHazardIdentifier() {
        return hazardIdentifier;
    }

    public void setHazardIdentifier(String hazardIdentifier) {
        this.hazardIdentifier = hazardIdentifier;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public Integer getTotalHabitations() {
        return totalHabitations;
    }

    public void setTotalHabitations(Integer totalHabitations) {
        this.totalHabitations = totalHabitations;
    }

    public Long getTotalVulnerablePopulation() {
        return totalVulnerablePopulation;
    }

    public void setTotalVulnerablePopulation(Long totalVulnerablePopulation) {
        this.totalVulnerablePopulation = totalVulnerablePopulation;
    }

    public Long getTotalAllocatedPopulation() {
        return totalAllocatedPopulation;
    }

    public void setTotalAllocatedPopulation(Long totalAllocatedPopulation) {
        this.totalAllocatedPopulation = totalAllocatedPopulation;
    }

    public Long getTotalUnallocatedPopulation() {
        return totalUnallocatedPopulation;
    }

    public void setTotalUnallocatedPopulation(Long totalUnallocatedPopulation) {
        this.totalUnallocatedPopulation = totalUnallocatedPopulation;
    }

    public Double getAllocationRatePercentage() {
        return allocationRatePercentage;
    }

    public void setAllocationRatePercentage(Double allocationRatePercentage) {
        this.allocationRatePercentage = allocationRatePercentage;
    }

    public Integer getTotalCandidateSitesEvaluated() {
        return totalCandidateSitesEvaluated;
    }

    public void setTotalCandidateSitesEvaluated(Integer totalCandidateSitesEvaluated) {
        this.totalCandidateSitesEvaluated = totalCandidateSitesEvaluated;
    }

    public Integer getTotalCandidateSitesUtilized() {
        return totalCandidateSitesUtilized;
    }

    public void setTotalCandidateSitesUtilized(Integer totalCandidateSitesUtilized) {
        this.totalCandidateSitesUtilized = totalCandidateSitesUtilized;
    }

    public Integer getTotalCapacityAvailable() {
        return totalCapacityAvailable;
    }

    public void setTotalCapacityAvailable(Integer totalCapacityAvailable) {
        this.totalCapacityAvailable = totalCapacityAvailable;
    }

    public Integer getTotalCapacityUtilized() {
        return totalCapacityUtilized;
    }

    public void setTotalCapacityUtilized(Integer totalCapacityUtilized) {
        this.totalCapacityUtilized = totalCapacityUtilized;
    }

    public Double getCapacityUtilizationPercentage() {
        return capacityUtilizationPercentage;
    }

    public void setCapacityUtilizationPercentage(Double capacityUtilizationPercentage) {
        this.capacityUtilizationPercentage = capacityUtilizationPercentage;
    }

    public List<RelocationAssignmentDto> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<RelocationAssignmentDto> assignments) {
        this.assignments = assignments;
    }

    public List<VulnerableHabitationDto> getUnallocatedHabitations() {
        return unallocatedHabitations;
    }

    public void setUnallocatedHabitations(List<VulnerableHabitationDto> unallocatedHabitations) {
        this.unallocatedHabitations = unallocatedHabitations;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getPlanSummary() {
        return planSummary;
    }

    public void setPlanSummary(String planSummary) {
        this.planSummary = planSummary;
    }

    public RelocationStatus getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(RelocationStatus overallStatus) {
        this.overallStatus = overallStatus;
    }

    public RelocationStatus getStatus() {
        return getOverallStatus();
    }

    public void setStatus(RelocationStatus status) {
        setOverallStatus(status);
    }

    public String getDeficitReasonCode() {
        return deficitReasonCode;
    }

    public void setDeficitReasonCode(String deficitReasonCode) {
        this.deficitReasonCode = deficitReasonCode;
    }

    public String getDeficitExplanation() {
        return deficitExplanation;
    }

    public void setDeficitExplanation(String deficitExplanation) {
        this.deficitExplanation = deficitExplanation;
    }

    public boolean hasDeficit() {
        return totalUnallocatedPopulation != null && totalUnallocatedPopulation > 0;
    }

    public boolean isFullyAllocated() {
        return totalUnallocatedPopulation != null && totalUnallocatedPopulation == 0 && totalVulnerablePopulation != null;
    }

    public Long getDeficitPopulation() {
        return totalUnallocatedPopulation != null ? totalUnallocatedPopulation : 0L;
    }

    /**
     * Verifies fundamental conservation of population invariants:
     * 1. allocated + unallocated == totalVulnerablePopulation
     * 2. allocated >= 0 and unallocated >= 0
     * 3. allocated <= totalVulnerablePopulation
     */
    public boolean validateInvariants() {
        if (totalAllocatedPopulation == null || totalUnallocatedPopulation == null || totalVulnerablePopulation == null) {
            return false;
        }
        if (totalAllocatedPopulation < 0 || totalUnallocatedPopulation < 0 || totalVulnerablePopulation < 0) {
            return false;
        }
        if (totalAllocatedPopulation + totalUnallocatedPopulation != totalVulnerablePopulation) {
            return false;
        }
        if (totalAllocatedPopulation > totalVulnerablePopulation) {
            return false;
        }
        return true;
    }

    public LocalDateTime getGenerationTimestamp() {
        return generationTimestamp;
    }

    public void setGenerationTimestamp(LocalDateTime generationTimestamp) {
        this.generationTimestamp = generationTimestamp;
    }
}
