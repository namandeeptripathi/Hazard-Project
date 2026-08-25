package com.hazard.dto.validation;

import com.hazard.domain.hazard.HazardType;

import java.time.LocalDate;

/**
 * Immutable ground-truth event record for hazard validation.
 * Represents a historically documented hazard occurrence with provenance metadata.
 * Ground-truth records are never modified or merged with model outputs.
 */
public class GroundTruthEvent {

    private String groundTruthId;
    private String source;           // "DFO", "EMDAT"
    private Object sourceRecordId;
    private HazardType hazardType;

    private LocalDate eventStart;
    private LocalDate eventEnd;

    private String locationDescription;
    private String associatedDistrict;
    private Double longitude;
    private Double latitude;
    private boolean hasValidGeometry;

    private Double dfoSeverity;
    private Double dfoMagnitude;
    private Double deaths;
    private Double displaced;
    private Double affectedSqkm;

    private String geographicPrecision;  // "POINT_COORDINATE", "DISTRICT_LEVEL", "STATE_LEVEL", "NATIONAL"
    private String temporalPrecision;    // "EXACT_DATE", "YEAR_ONLY"
    private boolean usableForSpatialValidation;
    private boolean usableForTemporalValidation;
    private String exclusionReason;

    public GroundTruthEvent() {}

    // --- Getters and Setters ---

    public String getGroundTruthId() { return groundTruthId; }
    public void setGroundTruthId(String groundTruthId) { this.groundTruthId = groundTruthId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Object getSourceRecordId() { return sourceRecordId; }
    public void setSourceRecordId(Object sourceRecordId) { this.sourceRecordId = sourceRecordId; }

    public HazardType getHazardType() { return hazardType; }
    public void setHazardType(HazardType hazardType) { this.hazardType = hazardType; }

    public LocalDate getEventStart() { return eventStart; }
    public void setEventStart(LocalDate eventStart) { this.eventStart = eventStart; }

    public LocalDate getEventEnd() { return eventEnd; }
    public void setEventEnd(LocalDate eventEnd) { this.eventEnd = eventEnd; }

    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }

    public String getAssociatedDistrict() { return associatedDistrict; }
    public void setAssociatedDistrict(String associatedDistrict) { this.associatedDistrict = associatedDistrict; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public boolean isHasValidGeometry() { return hasValidGeometry; }
    public void setHasValidGeometry(boolean hasValidGeometry) { this.hasValidGeometry = hasValidGeometry; }

    public Double getDfoSeverity() { return dfoSeverity; }
    public void setDfoSeverity(Double dfoSeverity) { this.dfoSeverity = dfoSeverity; }

    public Double getDfoMagnitude() { return dfoMagnitude; }
    public void setDfoMagnitude(Double dfoMagnitude) { this.dfoMagnitude = dfoMagnitude; }

    public Double getDeaths() { return deaths; }
    public void setDeaths(Double deaths) { this.deaths = deaths; }

    public Double getDisplaced() { return displaced; }
    public void setDisplaced(Double displaced) { this.displaced = displaced; }

    public Double getAffectedSqkm() { return affectedSqkm; }
    public void setAffectedSqkm(Double affectedSqkm) { this.affectedSqkm = affectedSqkm; }

    public String getGeographicPrecision() { return geographicPrecision; }
    public void setGeographicPrecision(String geographicPrecision) { this.geographicPrecision = geographicPrecision; }

    public String getTemporalPrecision() { return temporalPrecision; }
    public void setTemporalPrecision(String temporalPrecision) { this.temporalPrecision = temporalPrecision; }

    public boolean isUsableForSpatialValidation() { return usableForSpatialValidation; }
    public void setUsableForSpatialValidation(boolean usableForSpatialValidation) { this.usableForSpatialValidation = usableForSpatialValidation; }

    public boolean isUsableForTemporalValidation() { return usableForTemporalValidation; }
    public void setUsableForTemporalValidation(boolean usableForTemporalValidation) { this.usableForTemporalValidation = usableForTemporalValidation; }

    public String getExclusionReason() { return exclusionReason; }
    public void setExclusionReason(String exclusionReason) { this.exclusionReason = exclusionReason; }
}
