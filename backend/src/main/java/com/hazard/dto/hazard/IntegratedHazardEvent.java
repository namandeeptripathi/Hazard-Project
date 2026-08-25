package com.hazard.dto.hazard;

import com.hazard.domain.hazard.HazardType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified Application-Level Hazard Domain Model.
 * Represents an integrated hazard event or hazard observation across sources
 * (DFO Historical Floods, EM-DAT Macro Impact Records, Open-Meteo Extreme Rainfall Observations).
 */
public class IntegratedHazardEvent {

    private String id;
    private Object sourceRecordId;
    private HazardType hazardType;
    private String dataSource;
    private String locationName;
    private String country;
    private Double longitude;
    private Double latitude;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime timestamp;
    private Double severity;
    private Double magnitude;
    private Double durationDays;
    private Double displacedPopulation;
    private Double fatalities;
    private Double affectedAreaSqKm;
    private Double economicDamageUsd;
    private Double precipitationMm;
    private String externalReference;
    private Map<String, Object> metadata;

    public IntegratedHazardEvent() {
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public Double getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Double durationDays) {
        this.durationDays = durationDays;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
