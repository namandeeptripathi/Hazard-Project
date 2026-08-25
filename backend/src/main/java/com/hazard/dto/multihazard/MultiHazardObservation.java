package com.hazard.dto.multihazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.MultiHazardConfidence;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.domain.hazard.SpatialRelationship;
import com.hazard.domain.hazard.TemporalRelationship;
import com.hazard.dto.processing.ProcessingMetadata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a synthesized Multi-Hazard Event/Observation.
 * Aggregates coincident single-hazard scores into a unified Multi-Hazard Index [0.0000, 1.0000],
 * identifying dominant hazards, spatial/temporal relationships, and match confidence.
 */
public class MultiHazardObservation {

    private String id;
    private String associatedDistrict;
    private Boolean isWithinBiharBoundary;
    private Double longitude;
    private Double latitude;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<HazardParticipationDto> participatingHazards = new ArrayList<>();
    private SpatialRelationship spatialRelationship;
    private TemporalRelationship temporalRelationship;
    private MultiHazardConfidence confidence;

    private Double multiHazardIndex;
    private SeverityTier severityTier;
    private HazardType dominantHazard;
    private Double dominantHazardScore;
    private HazardType secondaryHazard;
    private Double secondaryHazardScore;

    private double completenessRatio;
    private String scoringMethod = "MULTI_HAZARD_WEIGHTED_COMPOSITE_INDEX";
    private String explanation;
    private ProcessingMetadata processingMetadata;

    public MultiHazardObservation() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<HazardParticipationDto> getParticipatingHazards() {
        return participatingHazards;
    }

    public void setParticipatingHazards(List<HazardParticipationDto> participatingHazards) {
        this.participatingHazards = participatingHazards != null ? participatingHazards : new ArrayList<>();
    }

    public void addParticipatingHazard(HazardParticipationDto hazard) {
        this.participatingHazards.add(hazard);
    }

    public SpatialRelationship getSpatialRelationship() {
        return spatialRelationship;
    }

    public void setSpatialRelationship(SpatialRelationship spatialRelationship) {
        this.spatialRelationship = spatialRelationship;
    }

    public TemporalRelationship getTemporalRelationship() {
        return temporalRelationship;
    }

    public void setTemporalRelationship(TemporalRelationship temporalRelationship) {
        this.temporalRelationship = temporalRelationship;
    }

    public MultiHazardConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(MultiHazardConfidence confidence) {
        this.confidence = confidence;
    }

    public Double getMultiHazardIndex() {
        return multiHazardIndex;
    }

    public void setMultiHazardIndex(Double multiHazardIndex) {
        this.multiHazardIndex = multiHazardIndex;
    }

    public SeverityTier getSeverityTier() {
        return severityTier;
    }

    public void setSeverityTier(SeverityTier severityTier) {
        this.severityTier = severityTier;
    }

    public HazardType getDominantHazard() {
        return dominantHazard;
    }

    public void setDominantHazard(HazardType dominantHazard) {
        this.dominantHazard = dominantHazard;
    }

    public Double getDominantHazardScore() {
        return dominantHazardScore;
    }

    public void setDominantHazardScore(Double dominantHazardScore) {
        this.dominantHazardScore = dominantHazardScore;
    }

    public HazardType getSecondaryHazard() {
        return secondaryHazard;
    }

    public void setSecondaryHazard(HazardType secondaryHazard) {
        this.secondaryHazard = secondaryHazard;
    }

    public Double getSecondaryHazardScore() {
        return secondaryHazardScore;
    }

    public void setSecondaryHazardScore(Double secondaryHazardScore) {
        this.secondaryHazardScore = secondaryHazardScore;
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

    public ProcessingMetadata getProcessingMetadata() {
        return processingMetadata;
    }

    public void setProcessingMetadata(ProcessingMetadata processingMetadata) {
        this.processingMetadata = processingMetadata;
    }
}
