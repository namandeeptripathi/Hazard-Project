package com.hazard.dto.validation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Master Validation Report DTO aggregating ground-truth coverage, data quality,
 * individual validation target metrics, and overall assessment.
 */
public class ValidationReportDto {

    private String reportTitle = "Stage 3.8 Hazard Intelligence Validation Report";
    private LocalDateTime generatedAt;
    private String validationMethodology = "Event-level score-separation with district-matched ground truth";

    // Data Quality Coverage
    private DataQualityCoverageDto dataQualityCoverage;

    // Individual Validation Targets
    private List<ValidationMetricsDto> validationTargets = new ArrayList<>();

    // Overall assessment
    private String overallAssessment;
    private List<String> identifiedStrengths = new ArrayList<>();
    private List<String> identifiedWeaknesses = new ArrayList<>();
    private List<String> calibrationRecommendations = new ArrayList<>();

    // Conceptual boundary
    private String boundaryNote;

    public ValidationReportDto() {
        this.generatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getValidationMethodology() { return validationMethodology; }
    public void setValidationMethodology(String v) { this.validationMethodology = v; }

    public DataQualityCoverageDto getDataQualityCoverage() { return dataQualityCoverage; }
    public void setDataQualityCoverage(DataQualityCoverageDto d) { this.dataQualityCoverage = d; }

    public List<ValidationMetricsDto> getValidationTargets() { return validationTargets; }
    public void setValidationTargets(List<ValidationMetricsDto> v) { this.validationTargets = v != null ? v : new ArrayList<>(); }
    public void addValidationTarget(ValidationMetricsDto target) { this.validationTargets.add(target); }

    public String getOverallAssessment() { return overallAssessment; }
    public void setOverallAssessment(String overallAssessment) { this.overallAssessment = overallAssessment; }

    public List<String> getIdentifiedStrengths() { return identifiedStrengths; }
    public void setIdentifiedStrengths(List<String> s) { this.identifiedStrengths = s != null ? s : new ArrayList<>(); }

    public List<String> getIdentifiedWeaknesses() { return identifiedWeaknesses; }
    public void setIdentifiedWeaknesses(List<String> w) { this.identifiedWeaknesses = w != null ? w : new ArrayList<>(); }

    public List<String> getCalibrationRecommendations() { return calibrationRecommendations; }
    public void setCalibrationRecommendations(List<String> c) { this.calibrationRecommendations = c != null ? c : new ArrayList<>(); }

    public String getBoundaryNote() { return boundaryNote; }
    public void setBoundaryNote(String boundaryNote) { this.boundaryNote = boundaryNote; }

    /**
     * Sub-DTO: Data Quality Coverage Summary
     */
    public static class DataQualityCoverageDto {
        private int totalDfoEvents;
        private int dfoEventsWithValidGeometry;
        private int dfoEventsWithSentinelCoordinates;
        private int dfoEventsUsableForValidation;
        private int totalEmdatRecords;
        private int emdatRecordsUsableForValidation;
        private String emdatExclusionReason;
        private int totalWeatherStations;
        private int totalWeatherRecords;
        private String weatherTemporalCoverage;
        private String dfoTemporalCoverage;
        private String temporalOverlapAssessment;
        private int totalGroundTruthEvents;
        private int totalUsableGroundTruthEvents;
        private Map<String, Integer> districtCoverage = new LinkedHashMap<>();
        private List<String> exclusionReasons = new ArrayList<>();

        public DataQualityCoverageDto() {}

        public int getTotalDfoEvents() { return totalDfoEvents; }
        public void setTotalDfoEvents(int v) { this.totalDfoEvents = v; }

        public int getDfoEventsWithValidGeometry() { return dfoEventsWithValidGeometry; }
        public void setDfoEventsWithValidGeometry(int v) { this.dfoEventsWithValidGeometry = v; }

        public int getDfoEventsWithSentinelCoordinates() { return dfoEventsWithSentinelCoordinates; }
        public void setDfoEventsWithSentinelCoordinates(int v) { this.dfoEventsWithSentinelCoordinates = v; }

        public int getDfoEventsUsableForValidation() { return dfoEventsUsableForValidation; }
        public void setDfoEventsUsableForValidation(int v) { this.dfoEventsUsableForValidation = v; }

        public int getTotalEmdatRecords() { return totalEmdatRecords; }
        public void setTotalEmdatRecords(int v) { this.totalEmdatRecords = v; }

        public int getEmdatRecordsUsableForValidation() { return emdatRecordsUsableForValidation; }
        public void setEmdatRecordsUsableForValidation(int v) { this.emdatRecordsUsableForValidation = v; }

        public String getEmdatExclusionReason() { return emdatExclusionReason; }
        public void setEmdatExclusionReason(String v) { this.emdatExclusionReason = v; }

        public int getTotalWeatherStations() { return totalWeatherStations; }
        public void setTotalWeatherStations(int v) { this.totalWeatherStations = v; }

        public int getTotalWeatherRecords() { return totalWeatherRecords; }
        public void setTotalWeatherRecords(int v) { this.totalWeatherRecords = v; }

        public String getWeatherTemporalCoverage() { return weatherTemporalCoverage; }
        public void setWeatherTemporalCoverage(String v) { this.weatherTemporalCoverage = v; }

        public String getDfoTemporalCoverage() { return dfoTemporalCoverage; }
        public void setDfoTemporalCoverage(String v) { this.dfoTemporalCoverage = v; }

        public String getTemporalOverlapAssessment() { return temporalOverlapAssessment; }
        public void setTemporalOverlapAssessment(String v) { this.temporalOverlapAssessment = v; }

        public int getTotalGroundTruthEvents() { return totalGroundTruthEvents; }
        public void setTotalGroundTruthEvents(int v) { this.totalGroundTruthEvents = v; }

        public int getTotalUsableGroundTruthEvents() { return totalUsableGroundTruthEvents; }
        public void setTotalUsableGroundTruthEvents(int v) { this.totalUsableGroundTruthEvents = v; }

        public Map<String, Integer> getDistrictCoverage() { return districtCoverage; }
        public void setDistrictCoverage(Map<String, Integer> v) { this.districtCoverage = v != null ? v : new LinkedHashMap<>(); }

        public List<String> getExclusionReasons() { return exclusionReasons; }
        public void setExclusionReasons(List<String> v) { this.exclusionReasons = v != null ? v : new ArrayList<>(); }
    }
}
