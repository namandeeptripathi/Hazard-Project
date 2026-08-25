package com.hazard.dto.processing;

import com.hazard.domain.hazard.QualityStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Processing audit trail and metadata for an analysis-ready hazard observation.
 */
public class ProcessingMetadata {

    private LocalDateTime processedAt = LocalDateTime.now();
    private String pipelineVersion = "3.2.0";
    private QualityStatus qualityStatus = QualityStatus.VALID;
    private String spatialResolutionStatus;
    private List<String> cleaningActions = new ArrayList<>();
    private List<String> anomaliesDetected = new ArrayList<>();
    private List<String> derivedMetricsComputed = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public ProcessingMetadata() {
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getPipelineVersion() {
        return pipelineVersion;
    }

    public void setPipelineVersion(String pipelineVersion) {
        this.pipelineVersion = pipelineVersion;
    }

    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(QualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public String getSpatialResolutionStatus() {
        return spatialResolutionStatus;
    }

    public void setSpatialResolutionStatus(String spatialResolutionStatus) {
        this.spatialResolutionStatus = spatialResolutionStatus;
    }

    public List<String> getCleaningActions() {
        return cleaningActions;
    }

    public void setCleaningActions(List<String> cleaningActions) {
        this.cleaningActions = cleaningActions != null ? cleaningActions : new ArrayList<>();
    }

    public void addCleaningAction(String action) {
        this.cleaningActions.add(action);
    }

    public List<String> getAnomaliesDetected() {
        return anomaliesDetected;
    }

    public void setAnomaliesDetected(List<String> anomaliesDetected) {
        this.anomaliesDetected = anomaliesDetected != null ? anomaliesDetected : new ArrayList<>();
    }

    public void addAnomaly(String anomaly) {
        this.anomaliesDetected.add(anomaly);
    }

    public List<String> getDerivedMetricsComputed() {
        return derivedMetricsComputed;
    }

    public void setDerivedMetricsComputed(List<String> derivedMetricsComputed) {
        this.derivedMetricsComputed = derivedMetricsComputed != null ? derivedMetricsComputed : new ArrayList<>();
    }

    public void addDerivedMetric(String metricName) {
        this.derivedMetricsComputed.add(metricName);
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
}
