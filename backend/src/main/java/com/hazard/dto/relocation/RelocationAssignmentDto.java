package com.hazard.dto.relocation;

import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.safesite.CandidateSiteCategory;
import com.hazard.domain.safesite.SuitabilityClass;
import com.hazard.dto.safesite.CandidateSafeSiteDto;

import java.time.LocalDateTime;

/**
 * Stage 6.1 — Relocation Assignment DTO.
 *
 * Represents an individual paired evacuation assignment from a vulnerable origin habitation
 * to an optimal, evaluated destination safe shelter site.
 */
public class RelocationAssignmentDto {

    private String assignmentId;

    // Origin Habitation Info
    private String habitationId;
    private String habitationName;
    private String originDistrict;
    private Double originLatitude;
    private Double originLongitude;
    private Long vulnerablePopulation;
    private Long allocatedPopulation;
    private Long unallocatedPopulation;

    // Destination Safe Site Info
    private String destinationSiteId;
    private String destinationSiteName;
    private CandidateSiteCategory destinationCategory;
    private String destinationDistrict;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private SuitabilityClass destinationSuitabilityClass;
    private Double destinationSuitabilityScore;
    private Integer destinationRank;

    // Transit & Geodesic Metrics
    private Double transitDistanceMeters;
    private Double transitDistanceKilometers;
    private Double estimatedTravelTimeMinutes; // Rough transit estimate (assuming emergency transit speeds)

    // Assignment Status & Explainability
    private RelocationStatus status;
    private RelocationUrgency urgency;
    private String allocationReason;
    private LocalDateTime timestamp;

    public RelocationAssignmentDto() {
        this.status = RelocationStatus.PENDING;
        this.urgency = RelocationUrgency.MODERATE;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Helper constructor to initialize a paired assignment between a habitation and a candidate safe site.
     */
    public RelocationAssignmentDto(VulnerableHabitationDto habitation, CandidateSafeSiteDto safeSite,
                                 long allocatedCount, double distanceMeters, RelocationStatus status, String reason) {
        this();
        if (habitation != null) {
            this.habitationId = habitation.getHabitationId();
            this.habitationName = habitation.getHabitationName();
            this.originDistrict = habitation.getDistrict();
            this.originLatitude = habitation.getLatitude();
            this.originLongitude = habitation.getLongitude();
            this.vulnerablePopulation = habitation.getVulnerablePopulation();
            this.urgency = habitation.getUrgency();
            this.unallocatedPopulation = Math.max(0, (this.vulnerablePopulation != null ? this.vulnerablePopulation : 0) - allocatedCount);
        }

        if (safeSite != null) {
            this.destinationSiteId = safeSite.getSiteId();
            this.destinationSiteName = safeSite.getSiteName();
            this.destinationCategory = safeSite.getCategory();
            this.destinationDistrict = safeSite.getDistrict();
            this.destinationLatitude = safeSite.getLatitude();
            this.destinationLongitude = safeSite.getLongitude();
            this.destinationSuitabilityClass = safeSite.getSuitabilityClass();
            this.destinationSuitabilityScore = safeSite.getSuitabilityScore();
            this.destinationRank = safeSite.getRank();
        }

        this.allocatedPopulation = allocatedCount;
        this.transitDistanceMeters = distanceMeters;
        this.transitDistanceKilometers = Math.round((distanceMeters / 1000.0) * 100.0) / 100.0;
        this.status = status;
        this.allocationReason = reason;
        this.assignmentId = "ASN-" + (habitation != null ? habitation.getHabitationId() : "NONE") + "-" + (safeSite != null ? safeSite.getSiteId() : "UNASSIGNED");
    }

    // --- Getters and Setters ---

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

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

    public String getOriginDistrict() {
        return originDistrict;
    }

    public void setOriginDistrict(String originDistrict) {
        this.originDistrict = originDistrict;
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

    public String getDestinationSiteId() {
        return destinationSiteId;
    }

    public void setDestinationSiteId(String destinationSiteId) {
        this.destinationSiteId = destinationSiteId;
    }

    public String getDestinationSiteName() {
        return destinationSiteName;
    }

    public void setDestinationSiteName(String destinationSiteName) {
        this.destinationSiteName = destinationSiteName;
    }

    public CandidateSiteCategory getDestinationCategory() {
        return destinationCategory;
    }

    public void setDestinationCategory(CandidateSiteCategory destinationCategory) {
        this.destinationCategory = destinationCategory;
    }

    public String getDestinationDistrict() {
        return destinationDistrict;
    }

    public void setDestinationDistrict(String destinationDistrict) {
        this.destinationDistrict = destinationDistrict;
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

    public SuitabilityClass getDestinationSuitabilityClass() {
        return destinationSuitabilityClass;
    }

    public void setDestinationSuitabilityClass(SuitabilityClass destinationSuitabilityClass) {
        this.destinationSuitabilityClass = destinationSuitabilityClass;
    }

    public Double getDestinationSuitabilityScore() {
        return destinationSuitabilityScore;
    }

    public void setDestinationSuitabilityScore(Double destinationSuitabilityScore) {
        this.destinationSuitabilityScore = destinationSuitabilityScore;
    }

    public Integer getDestinationRank() {
        return destinationRank;
    }

    public void setDestinationRank(Integer destinationRank) {
        this.destinationRank = destinationRank;
    }

    public Double getTransitDistanceMeters() {
        return transitDistanceMeters;
    }

    public void setTransitDistanceMeters(Double transitDistanceMeters) {
        this.transitDistanceMeters = transitDistanceMeters;
        if (transitDistanceMeters != null) {
            this.transitDistanceKilometers = Math.round((transitDistanceMeters / 1000.0) * 100.0) / 100.0;
        }
    }

    public Double getTransitDistanceKilometers() {
        return transitDistanceKilometers;
    }

    public void setTransitDistanceKilometers(Double transitDistanceKilometers) {
        this.transitDistanceKilometers = transitDistanceKilometers;
    }

    public Double getEstimatedTravelTimeMinutes() {
        return estimatedTravelTimeMinutes;
    }

    public void setEstimatedTravelTimeMinutes(Double estimatedTravelTimeMinutes) {
        this.estimatedTravelTimeMinutes = estimatedTravelTimeMinutes;
    }

    public RelocationStatus getStatus() {
        return status;
    }

    public void setStatus(RelocationStatus status) {
        this.status = status;
    }

    public RelocationUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RelocationUrgency urgency) {
        this.urgency = urgency;
    }

    public String getAllocationReason() {
        return allocationReason;
    }

    public void setAllocationReason(String allocationReason) {
        this.allocationReason = allocationReason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
