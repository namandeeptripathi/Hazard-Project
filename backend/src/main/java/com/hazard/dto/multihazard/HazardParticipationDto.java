package com.hazard.dto.multihazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.SeverityTier;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Details of an individual single-hazard component participating in a multi-hazard observation.
 */
public class HazardParticipationDto {

    private String hazardId;
    private HazardType hazardType;
    private String dataSource;
    private String locationName;
    private Double hazardScore;
    private SeverityTier severityTier;
    private Double configuredWeight;
    private Double effectiveWeight;
    private Double weightedContribution;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime timestamp;

    public HazardParticipationDto() {
    }

    public HazardParticipationDto(String hazardId, HazardType hazardType, String dataSource,
                                  String locationName, Double hazardScore, SeverityTier severityTier,
                                  Double configuredWeight, Double effectiveWeight, Double weightedContribution,
                                  LocalDate startDate, LocalDate endDate, LocalDateTime timestamp) {
        this.hazardId = hazardId;
        this.hazardType = hazardType;
        this.dataSource = dataSource;
        this.locationName = locationName;
        this.hazardScore = hazardScore;
        this.severityTier = severityTier;
        this.configuredWeight = configuredWeight;
        this.effectiveWeight = effectiveWeight;
        this.weightedContribution = weightedContribution;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timestamp = timestamp;
    }

    public String getHazardId() {
        return hazardId;
    }

    public void setHazardId(String hazardId) {
        this.hazardId = hazardId;
    }

    public HazardType getHazardType() {
        return hazardType;
    }

    public void setHazardType(HazardType hazardType) {
        this.hazardType = hazardType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getHazardScore() {
        return hazardScore;
    }

    public void setHazardScore(Double hazardScore) {
        this.hazardScore = hazardScore;
    }

    public SeverityTier getSeverityTier() {
        return severityTier;
    }

    public void setSeverityTier(SeverityTier severityTier) {
        this.severityTier = severityTier;
    }

    public Double getConfiguredWeight() {
        return configuredWeight;
    }

    public void setConfiguredWeight(Double configuredWeight) {
        this.configuredWeight = configuredWeight;
    }

    public Double getEffectiveWeight() {
        return effectiveWeight;
    }

    public void setEffectiveWeight(Double effectiveWeight) {
        this.effectiveWeight = effectiveWeight;
    }

    public Double getWeightedContribution() {
        return weightedContribution;
    }

    public void setWeightedContribution(Double weightedContribution) {
        this.weightedContribution = weightedContribution;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
