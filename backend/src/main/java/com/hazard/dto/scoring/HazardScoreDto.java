package com.hazard.dto.scoring;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.QualityStatus;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.processing.ProcessingMetadata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the calculated single-hazard score, severity tier classification,
 * contributing metric breakdown, provenance metadata, and spatial attributes.
 */
public class HazardScoreDto {

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

    private Double hazardScore;
    private SeverityTier severityTier;
    private double completenessRatio;
    private String scoringMethod = "WEIGHTED_MULTI_CRITERIA_HAZARD_INDEX";
    private String explanation;

    private List<MetricContributionDto> metricContributions = new ArrayList<>();
    private ProcessingMetadata processingMetadata;

    public HazardScoreDto() {
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

    public double getCompletenessRatio() {
        return completenessRatio;
    }

    public void setCompletenessRatio(double completenessRatio) {
        this.completenessRatio = completenessRatio;
    }

    public String getScoringMethod() {
        return scoringMethod;
    }

    public void setScoringMethod(String scoringMethod) {
        this.scoringMethod = scoringMethod;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<MetricContributionDto> getMetricContributions() {
        return metricContributions;
    }

    public void setMetricContributions(List<MetricContributionDto> metricContributions) {
        this.metricContributions = metricContributions != null ? metricContributions : new ArrayList<>();
    }

    public void addMetricContribution(MetricContributionDto contribution) {
        this.metricContributions.add(contribution);
    }

    public ProcessingMetadata getProcessingMetadata() {
        return processingMetadata;
    }

    public void setProcessingMetadata(ProcessingMetadata processingMetadata) {
        this.processingMetadata = processingMetadata;
    }
}
