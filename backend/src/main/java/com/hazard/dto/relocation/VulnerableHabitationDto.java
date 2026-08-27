package com.hazard.dto.relocation;

import com.hazard.domain.population.OsmSettlement;
import com.hazard.domain.population.PopulatedPlace;
import com.hazard.domain.relocation.RelocationStatus;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.risk.RiskTier;
import com.hazard.dto.exposure.SettlementExposureDto;

import java.time.LocalDateTime;

/**
 * Stage 6.1 — Vulnerable Habitation / Evacuation Origin DTO.
 *
 * Represents an exposed village, town, city, or residential cluster that requires emergency
 * relocation planning and shelter site allocation.
 */
public class VulnerableHabitationDto {

    private String habitationId;
    private String habitationName;
    private String habitationType;        // city, town, village, hamlet, residential
    private String district;
    private String state;
    private Double latitude;
    private Double longitude;

    private Long totalPopulation;
    private Long vulnerablePopulation;   // Number of people requiring relocation
    private boolean isEstimatedPopulation;

    // Hazard & Risk Context from Stages 3, 4, 5
    private String hazardIdentifier;
    private String hazardType;            // FLOOD, EXTREME_RAINFALL, MULTI_HAZARD
    private Double hazardSeverityScore;   // [0.0000, 1.0000]
    private Double riskScore;             // [0.0000, 1.0000]
    private RiskTier riskTier;
    private boolean redZone;

    // Relocation Intelligence Status
    private RelocationUrgency urgency;
    private RelocationStatus relocationStatus;
    private String assignedSiteId;
    private String statusReason;
    private LocalDateTime timestamp;

    public VulnerableHabitationDto() {
        this.state = "Bihar";
        this.urgency = RelocationUrgency.MODERATE;
        this.relocationStatus = RelocationStatus.PENDING;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Factory: builds a VulnerableHabitationDto from a Stage 4.2 SettlementExposureDto.
     */
    public static VulnerableHabitationDto fromSettlementExposure(SettlementExposureDto settlement) {
        if (settlement == null) {
            return null;
        }

        VulnerableHabitationDto dto = new VulnerableHabitationDto();
        dto.setHabitationId(settlement.getSettlementId() != null ? "HAB-" + settlement.getSettlementId() : null);
        dto.setHabitationName(settlement.getSettlementName());
        dto.setHabitationType(settlement.getSettlementType());
        dto.setDistrict(settlement.getDistrictName());
        dto.setState(settlement.getState() != null ? settlement.getState() : "Bihar");
        dto.setLatitude(settlement.getLatitude());
        dto.setLongitude(settlement.getLongitude());

        dto.setTotalPopulation(settlement.getTotalPopulation());
        // Default vulnerable population equals total population unless estimated otherwise
        dto.setVulnerablePopulation(settlement.getTotalPopulation() != null ? settlement.getTotalPopulation() : 0L);
        dto.setEstimatedPopulation(settlement.isEstimatedPopulation());

        dto.setHazardIdentifier(settlement.getHazardIdentifier());
        dto.setHazardType(settlement.getHazardType());
        dto.setHazardSeverityScore(settlement.getHazardSeverityScore());
        dto.setRiskScore(settlement.getSettlementExposureScore());

        // Infer urgency from exposure category
        if (settlement.getExposureCategory() != null) {
            switch (settlement.getExposureCategory()) {
                case VERY_HIGH -> dto.setUrgency(RelocationUrgency.CRITICAL);
                case HIGH -> dto.setUrgency(RelocationUrgency.HIGH);
                case MODERATE -> dto.setUrgency(RelocationUrgency.MODERATE);
                case LOW -> dto.setUrgency(RelocationUrgency.LOW);
            }
        }

        return dto;
    }

    /**
     * Factory: builds a VulnerableHabitationDto from a PopulatedPlace entity.
     */
    public static VulnerableHabitationDto fromPopulatedPlace(PopulatedPlace place) {
        if (place == null) {
            return null;
        }

        VulnerableHabitationDto dto = new VulnerableHabitationDto();
        dto.setHabitationId(place.getId() != null ? "HAB-PP-" + place.getId() : null);
        dto.setHabitationName(place.getName() != null ? place.getName() : place.getNameEn());
        dto.setHabitationType(place.getPlace() != null ? place.getPlace() : "residential");
        dto.setDistrict(place.getAdm2Name());
        dto.setState(place.getAdm1Name() != null ? place.getAdm1Name() : "Bihar");

        if (place.getGeom() != null) {
            org.locationtech.jts.geom.Point centroid = place.getGeom().getCentroid();
            dto.setLatitude(centroid.getY());
            dto.setLongitude(centroid.getX());
        }

        dto.setTotalPopulation(place.getPopulation());
        dto.setVulnerablePopulation(place.getPopulation() != null ? place.getPopulation() : 0L);
        return dto;
    }

    /**
     * Factory: builds a VulnerableHabitationDto from an OsmSettlement entity.
     */
    public static VulnerableHabitationDto fromOsmSettlement(OsmSettlement settlement) {
        if (settlement == null) {
            return null;
        }

        VulnerableHabitationDto dto = new VulnerableHabitationDto();
        dto.setHabitationId(settlement.getId() != null ? "HAB-OSM-" + settlement.getId() : null);
        dto.setHabitationName(settlement.getName() != null ? settlement.getName() : settlement.getNameEn());
        dto.setHabitationType(settlement.getPlace() != null ? settlement.getPlace() : "village");

        if (settlement.getGeom() != null) {
            dto.setLatitude(settlement.getGeom().getY());
            dto.setLongitude(settlement.getGeom().getX());
        }

        dto.setTotalPopulation(settlement.getPopulation());
        dto.setVulnerablePopulation(settlement.getPopulation() != null ? settlement.getPopulation() : 0L);
        return dto;
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

    public String getHabitationType() {
        return habitationType;
    }

    public void setHabitationType(String habitationType) {
        this.habitationType = habitationType;
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

    public Long getTotalPopulation() {
        return totalPopulation;
    }

    public void setTotalPopulation(Long totalPopulation) {
        this.totalPopulation = totalPopulation;
    }

    public Long getVulnerablePopulation() {
        return vulnerablePopulation;
    }

    public void setVulnerablePopulation(Long vulnerablePopulation) {
        this.vulnerablePopulation = vulnerablePopulation;
    }

    public boolean isEstimatedPopulation() {
        return isEstimatedPopulation;
    }

    public void setEstimatedPopulation(boolean estimatedPopulation) {
        isEstimatedPopulation = estimatedPopulation;
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

    public Double getHazardSeverityScore() {
        return hazardSeverityScore;
    }

    public void setHazardSeverityScore(Double hazardSeverityScore) {
        this.hazardSeverityScore = hazardSeverityScore;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public RiskTier getRiskTier() {
        return riskTier;
    }

    public void setRiskTier(RiskTier riskTier) {
        this.riskTier = riskTier;
    }

    public boolean isRedZone() {
        return redZone;
    }

    public void setRedZone(boolean redZone) {
        this.redZone = redZone;
    }

    public RelocationUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RelocationUrgency urgency) {
        this.urgency = urgency;
    }

    public RelocationStatus getRelocationStatus() {
        return relocationStatus;
    }

    public void setRelocationStatus(RelocationStatus relocationStatus) {
        this.relocationStatus = relocationStatus;
    }

    public String getAssignedSiteId() {
        return assignedSiteId;
    }

    public void setAssignedSiteId(String assignedSiteId) {
        this.assignedSiteId = assignedSiteId;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
