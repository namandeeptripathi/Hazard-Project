package com.hazard.dto.processing;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Analysis-Ready Hazard Observation.
 * Represents a fully cleaned, validated, spatially associated, and derived hazard observation
 * ready for downstream normalization, scoring, and prediction engines (Stage 3.3+).
 */
public class ProcessedHazardObservation {

    private String id;
    private Object sourceRecordId;
    private HazardType hazardType;
    private String dataSource;
    private String locationName;
    private String associatedDistrict;
    private Boolean isWithinBiharBoundary;
    private Double longitude;
    private Double latitude;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime timestamp;
    private Double durationDays;
    private Double severity;
    private Double magnitude;
    private Double displacedPopulation;
    private Double fatalities;
    private Double affectedAreaSqKm;
    private Double economicDamageUsd;
    private Double precipitationMm;
    private String externalReference;
    private QualityStatus qualityStatus;
    private Map<String, Object> rawAttributes = new LinkedHashMap<>();
    private Map<String, Object> derivedMetrics = new LinkedHashMap<>();
    private ProcessingMetadata processingMetadata = new ProcessingMetadata();

    public ProcessedHazardObservation() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getSourceRecordId() {
        return sourceRecordId;
    }

    public void setSourceRecordId(Object sourceRecordId) {
        this.sourceRecordId = sourceRecordId;
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

    public String getAssociatedDistrict() {
        return associatedDistrict;
    }

    public void setAssociatedDistrict(String associatedDistrict) {
        this.associatedDistrict = associatedDistrict;
    }

    public Boolean getIsWithinBiharBoundary() {
        return isWithinBiharBoundary;
    }

    public void setIsWithinBiharBoundary(Boolean withinBiharBoundary) {
        isWithinBiharBoundary = withinBiharBoundary;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
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

    public Double getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Double durationDays) {
        this.durationDays = durationDays;
    }

    public Double getSeverity() {
        return severity;
    }

    public void setSeverity(Double severity) {
        this.severity = severity;
    }

    public Double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Double magnitude) {
        this.magnitude = magnitude;
    }

    public Double getDisplacedPopulation() {
        return displacedPopulation;
    }

    public void setDisplacedPopulation(Double displacedPopulation) {
        this.displacedPopulation = displacedPopulation;
    }

    public Double getFatalities() {
        return fatalities;
    }

    public void setFatalities(Double fatalities) {
        this.fatalities = fatalities;
    }

    public Double getAffectedAreaSqKm() {
        return affectedAreaSqKm;
    }

    public void setAffectedAreaSqKm(Double affectedAreaSqKm) {
        this.affectedAreaSqKm = affectedAreaSqKm;
    }

    public Double getEconomicDamageUsd() {
        return economicDamageUsd;
    }

    public void setEconomicDamageUsd(Double economicDamageUsd) {
        this.economicDamageUsd = economicDamageUsd;
    }

    public Double getPrecipitationMm() {
        return precipitationMm;
    }

    public void setPrecipitationMm(Double precipitationMm) {
        this.precipitationMm = precipitationMm;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(QualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
        if (this.processingMetadata != null) {
            this.processingMetadata.setQualityStatus(qualityStatus);
        }
    }

    public Map<String, Object> getRawAttributes() {
        return rawAttributes;
    }

    public void setRawAttributes(Map<String, Object> rawAttributes) {
        this.rawAttributes = rawAttributes != null ? rawAttributes : new LinkedHashMap<>();
    }

    public Map<String, Object> getDerivedMetrics() {
        return derivedMetrics;
    }

    public void setDerivedMetrics(Map<String, Object> derivedMetrics) {
        this.derivedMetrics = derivedMetrics != null ? derivedMetrics : new LinkedHashMap<>();
    }

    public ProcessingMetadata getProcessingMetadata() {
        return processingMetadata;
    }

    public void setProcessingMetadata(ProcessingMetadata processingMetadata) {
        this.processingMetadata = processingMetadata;
    }
}
