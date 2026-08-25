package com.hazard.service.processing;

import com.hazard.domain.hazard.QualityStatus;
import com.hazard.dto.processing.ProcessedHazardObservation;
import com.hazard.dto.processing.ProcessingMetadata;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Deterministic Data Cleaner and Quality Assessor for Hazard Observations.
 * Performs validation, anomaly detection, coordinate bounds cleaning,
 * numeric sanitization, and duration reconciliation.
 */
@Component
public class HazardDataCleaner {

    /**
     * Cleans and validates geographic coordinates.
     * Sanitizes sentinel/NoData coordinates (e.g. -1.797e+308) to null and updates processing metadata.
     */
    public void cleanCoordinates(ProcessedHazardObservation observation) {
        Double lon = observation.getLongitude();
        Double lat = observation.getLatitude();
        ProcessingMetadata meta = observation.getProcessingMetadata();

        if (lon == null && lat == null) {
            meta.setSpatialResolutionStatus("UNLOCATED_TABULAR");
            observation.setQualityStatus(QualityStatus.UNLOCATED);
            return;
        }

        if (lon != null && (lon < -180.0 || lon > 180.0) || (lat != null && (lat < -90.0 || lat > 90.0))) {
            meta.addAnomaly("SENTINEL_COORDINATES_DETECTED (lon=" + lon + ", lat=" + lat + ")");
            meta.addCleaningAction("CLEANED_SENTINEL_COORDINATES_TO_NULL");
            meta.setSpatialResolutionStatus("UNLOCATED_SENTINEL_CLEANED");

            observation.setLongitude(null);
            observation.setLatitude(null);
            observation.setQualityStatus(QualityStatus.UNLOCATED);
        } else if (lon != null && lat != null) {
            meta.setSpatialResolutionStatus("VALID_COORDINATES");
        } else {
            meta.addWarning("PARTIAL_COORDINATES (one axis missing)");
            observation.setQualityStatus(QualityStatus.PARTIAL);
        }
    }

    /**
     * Cleans and sanitizes numerical hazard metrics (precipitation, deaths, displaced, affected area).
     */
    public void cleanNumericMetrics(ProcessedHazardObservation observation) {
        ProcessingMetadata meta = observation.getProcessingMetadata();

        // 1. Precipitation floor (>= 0.0)
        if (observation.getPrecipitationMm() != null) {
            if (observation.getPrecipitationMm() < 0.0) {
                meta.addAnomaly("NEGATIVE_PRECIPITATION_DETECTED (" + observation.getPrecipitationMm() + " mm)");
                meta.addCleaningAction("FLOORED_NEGATIVE_PRECIPITATION_TO_ZERO");
                observation.setPrecipitationMm(0.0);
            }
        }

        // 2. Fatalities floor
        if (observation.getFatalities() != null && observation.getFatalities() < 0.0) {
            meta.addAnomaly("NEGATIVE_FATALITIES_DETECTED (" + observation.getFatalities() + ")");
            meta.addCleaningAction("FLOORED_NEGATIVE_FATALITIES_TO_ZERO");
            observation.setFatalities(0.0);
        }

        // 3. Displaced population floor
        if (observation.getDisplacedPopulation() != null && observation.getDisplacedPopulation() < 0.0) {
            meta.addAnomaly("NEGATIVE_DISPLACED_POPULATION_DETECTED (" + observation.getDisplacedPopulation() + ")");
            meta.addCleaningAction("FLOORED_NEGATIVE_DISPLACED_POPULATION_TO_ZERO");
            observation.setDisplacedPopulation(0.0);
        }

        // 4. Affected Area floor
        if (observation.getAffectedAreaSqKm() != null && observation.getAffectedAreaSqKm() < 0.0) {
            meta.addAnomaly("NEGATIVE_AFFECTED_AREA_DETECTED (" + observation.getAffectedAreaSqKm() + " sq km)");
            meta.addCleaningAction("FLOORED_NEGATIVE_AFFECTED_AREA_TO_ZERO");
            observation.setAffectedAreaSqKm(0.0);
        }
    }

    /**
     * Validates temporal dates and derives/reconciles event duration.
     */
    public void cleanAndDeriveTemporalMetrics(ProcessedHazardObservation observation) {
        LocalDate start = observation.getStartDate();
        LocalDate end = observation.getEndDate();
        ProcessingMetadata meta = observation.getProcessingMetadata();

        if (start != null && end != null) {
            if (start.isAfter(end)) {
                meta.addAnomaly("INVERTED_DATE_RANGE (start=" + start + ", end=" + end + ")");
                meta.addWarning("Event start date is after end date");
            } else {
                long days = ChronoUnit.DAYS.between(start, end) + 1;
                observation.getDerivedMetrics().put("calculatedDurationDays", (double) days);
                meta.addDerivedMetric("calculatedDurationDays");

                // Check consistency with source duration
                if (observation.getDurationDays() != null) {
                    double diff = Math.abs(observation.getDurationDays() - days);
                    if (diff > 1.0) {
                        meta.addWarning("DURATION_DISCREPANCY (source=" + observation.getDurationDays() + " days, calculated=" + days + " days)");
                    }
                } else {
                    observation.setDurationDays((double) days);
                    meta.addCleaningAction("INFERRED_DURATION_FROM_DATE_SPAN");
                }
            }
        }
    }

    /**
     * Determines the final quality status of the processed observation.
     */
    public void evaluateFinalQualityStatus(ProcessedHazardObservation observation) {
        ProcessingMetadata meta = observation.getProcessingMetadata();

        if (observation.getQualityStatus() == QualityStatus.UNLOCATED) {
            // Remains unlocated (e.g. EM-DAT tabular record or cleaned sentinel)
            meta.setQualityStatus(QualityStatus.UNLOCATED);
            return;
        }

        if (!meta.getAnomaliesDetected().isEmpty() && observation.getLongitude() == null) {
            observation.setQualityStatus(QualityStatus.UNLOCATED);
            meta.setQualityStatus(QualityStatus.UNLOCATED);
            return;
        }

        if (observation.getLongitude() != null && observation.getLatitude() != null) {
            if (observation.getStartDate() != null || observation.getTimestamp() != null) {
                observation.setQualityStatus(QualityStatus.VALID);
                meta.setQualityStatus(QualityStatus.VALID);
            } else {
                observation.setQualityStatus(QualityStatus.PARTIAL);
                meta.setQualityStatus(QualityStatus.PARTIAL);
            }
        } else {
            observation.setQualityStatus(QualityStatus.UNLOCATED);
            meta.setQualityStatus(QualityStatus.UNLOCATED);
        }
    }
}
