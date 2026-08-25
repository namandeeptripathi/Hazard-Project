package com.hazard.dto.historical;

import com.hazard.domain.hazard.HazardType;

import java.time.LocalDate;
import java.util.Map;

/**
 * Structured Data Transfer Object representing an individual historical disaster event.
 */
public class HistoricalEventDto {

    private String eventId;
    private HazardType hazardType;
    private LocalDate eventDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double durationDays;

    private String locationName;
    private String districtName;
    private Double latitude;
    private Double longitude;

    private Double severity;         // Recorded severity on source scale
    private Double normalizedSeverity; // Normalized severity in [0.0000, 1.0000]
    private Double magnitude;
    private Double affectedSqkm;
    private Double displacedPopulation;
    private Double fatalities;
    private Double damageUsd;
    private String mainCause;

    private String source;
    private String sourceId;
    private String provenance; // DIRECT_SOURCE, SPATIAL_DERIVATION, etc.
    private Map<String, Object> metadata;

    public HistoricalEventDto() {}

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public HazardType getHazardType() {
        return hazardType;
    }

    public void setHazardType(HazardType hazardType) {
        this.hazardType = hazardType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
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

    public Double getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Double durationDays) {
        this.durationDays = durationDays;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
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

    public Double getSeverity() {
        return severity;
    }

    public void setSeverity(Double severity) {
        this.severity = severity;
    }

    public Double getNormalizedSeverity() {
        return normalizedSeverity;
    }

    public void setNormalizedSeverity(Double normalizedSeverity) {
        this.normalizedSeverity = normalizedSeverity;
    }

    public Double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Double magnitude) {
        this.magnitude = magnitude;
    }

    public Double getAffectedSqkm() {
        return affectedSqkm;
    }

    public void setAffectedSqkm(Double affectedSqkm) {
        this.affectedSqkm = affectedSqkm;
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

    public Double getDamageUsd() {
        return damageUsd;
    }

    public void setDamageUsd(Double damageUsd) {
        this.damageUsd = damageUsd;
    }

    public String getMainCause() {
        return mainCause;
    }

    public void setMainCause(String mainCause) {
        this.mainCause = mainCause;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
