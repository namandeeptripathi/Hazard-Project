package com.hazard.dto.normalization;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.dto.processing.ProcessingMetadata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Normalized Hazard Observation representation on a common [0.00, 1.00] scale.
 * Holds multiple normalized metrics alongside spatial coordinates and processing metadata.
 */
public class NormalizedHazardObservation {

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
    private QualityStatus qualityStatus;
    private Map<String, NormalizedHazardMetric> normalizedMetrics = new LinkedHashMap<>();
    private ProcessingMetadata processingMetadata;

    public NormalizedHazardObservation() {
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

    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(QualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public Map<String, NormalizedHazardMetric> getNormalizedMetrics() {
        return normalizedMetrics;
    }

    public void setNormalizedMetrics(Map<String, NormalizedHazardMetric> normalizedMetrics) {
        this.normalizedMetrics = normalizedMetrics != null ? normalizedMetrics : new LinkedHashMap<>();
    }

    public void addNormalizedMetric(String key, NormalizedHazardMetric metric) {
        this.normalizedMetrics.put(key, metric);
    }

    public ProcessingMetadata getProcessingMetadata() {
        return processingMetadata;
    }

    public void setProcessingMetadata(ProcessingMetadata processingMetadata) {
        this.processingMetadata = processingMetadata;
    }
}
