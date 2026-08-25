package com.hazard.dto.validation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Validation metrics result for a single validation target (e.g. FLOOD_SCORE, RAINFALL_SCORE).
 */
public class ValidationMetricsDto {

    private String validationTarget;
    private String validationUnit;
    private String description;

    private int totalGroundTruthEvents;
    private int usableGroundTruthEvents;
    private int excludedGroundTruthEvents;
    private int totalModelObservations;
    private int matchedObservations;

    // Score Separation
    private Double eventPeriodMeanScore;
    private Double nonEventPeriodMeanScore;
    private Double scoreSeparation;
    private String scoreSeparationInterpretation;

    // Severity Tier Distribution for event observations
    private Map<String, Integer> eventTierDistribution = new LinkedHashMap<>();
    private Map<String, Integer> nonEventTierDistribution = new LinkedHashMap<>();

    // Ranking Performance
    private Double eventCaptureInTop10Pct;
    private Double eventCaptureInTop20Pct;
    private Double eventCaptureInTop25Pct;

    // Classification metrics (only where binary classification is meaningful)
    private Double precision;
    private Double recall;
    private Double f1Score;
    private String classificationNote;

    // Statistical notes and warnings
    private String statisticalWarning;
    private String baselineConstruction;
    private String temporalCoverageNote;

    public ValidationMetricsDto() {}

    // --- Getters and Setters ---

    public String getValidationTarget() { return validationTarget; }
    public void setValidationTarget(String validationTarget) { this.validationTarget = validationTarget; }

    public String getValidationUnit() { return validationUnit; }
    public void setValidationUnit(String validationUnit) { this.validationUnit = validationUnit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalGroundTruthEvents() { return totalGroundTruthEvents; }
    public void setTotalGroundTruthEvents(int totalGroundTruthEvents) { this.totalGroundTruthEvents = totalGroundTruthEvents; }

    public int getUsableGroundTruthEvents() { return usableGroundTruthEvents; }
    public void setUsableGroundTruthEvents(int usableGroundTruthEvents) { this.usableGroundTruthEvents = usableGroundTruthEvents; }

    public int getExcludedGroundTruthEvents() { return excludedGroundTruthEvents; }
    public void setExcludedGroundTruthEvents(int excludedGroundTruthEvents) { this.excludedGroundTruthEvents = excludedGroundTruthEvents; }

    public int getTotalModelObservations() { return totalModelObservations; }
    public void setTotalModelObservations(int totalModelObservations) { this.totalModelObservations = totalModelObservations; }

    public int getMatchedObservations() { return matchedObservations; }
    public void setMatchedObservations(int matchedObservations) { this.matchedObservations = matchedObservations; }

    public Double getEventPeriodMeanScore() { return eventPeriodMeanScore; }
    public void setEventPeriodMeanScore(Double eventPeriodMeanScore) { this.eventPeriodMeanScore = eventPeriodMeanScore; }

    public Double getNonEventPeriodMeanScore() { return nonEventPeriodMeanScore; }
    public void setNonEventPeriodMeanScore(Double nonEventPeriodMeanScore) { this.nonEventPeriodMeanScore = nonEventPeriodMeanScore; }

    public Double getScoreSeparation() { return scoreSeparation; }
    public void setScoreSeparation(Double scoreSeparation) { this.scoreSeparation = scoreSeparation; }

    public String getScoreSeparationInterpretation() { return scoreSeparationInterpretation; }
    public void setScoreSeparationInterpretation(String s) { this.scoreSeparationInterpretation = s; }

    public Map<String, Integer> getEventTierDistribution() { return eventTierDistribution; }
    public void setEventTierDistribution(Map<String, Integer> d) { this.eventTierDistribution = d != null ? d : new LinkedHashMap<>(); }

    public Map<String, Integer> getNonEventTierDistribution() { return nonEventTierDistribution; }
    public void setNonEventTierDistribution(Map<String, Integer> d) { this.nonEventTierDistribution = d != null ? d : new LinkedHashMap<>(); }

    public Double getEventCaptureInTop10Pct() { return eventCaptureInTop10Pct; }
    public void setEventCaptureInTop10Pct(Double v) { this.eventCaptureInTop10Pct = v; }

    public Double getEventCaptureInTop20Pct() { return eventCaptureInTop20Pct; }
    public void setEventCaptureInTop20Pct(Double v) { this.eventCaptureInTop20Pct = v; }

    public Double getEventCaptureInTop25Pct() { return eventCaptureInTop25Pct; }
    public void setEventCaptureInTop25Pct(Double v) { this.eventCaptureInTop25Pct = v; }

    public Double getPrecision() { return precision; }
    public void setPrecision(Double precision) { this.precision = precision; }

    public Double getRecall() { return recall; }
    public void setRecall(Double recall) { this.recall = recall; }

    public Double getF1Score() { return f1Score; }
    public void setF1Score(Double f1Score) { this.f1Score = f1Score; }

    public String getClassificationNote() { return classificationNote; }
    public void setClassificationNote(String classificationNote) { this.classificationNote = classificationNote; }

    public String getStatisticalWarning() { return statisticalWarning; }
    public void setStatisticalWarning(String statisticalWarning) { this.statisticalWarning = statisticalWarning; }

    public String getBaselineConstruction() { return baselineConstruction; }
    public void setBaselineConstruction(String baselineConstruction) { this.baselineConstruction = baselineConstruction; }

    public String getTemporalCoverageNote() { return temporalCoverageNote; }
    public void setTemporalCoverageNote(String temporalCoverageNote) { this.temporalCoverageNote = temporalCoverageNote; }
}
