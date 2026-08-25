package com.hazard;

import com.hazard.domain.hazard.QualityStatus;
import com.hazard.dto.processing.ProcessedHazardObservation;
import com.hazard.service.processing.HazardDataCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class HazardDataCleanerTests {

    private HazardDataCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new HazardDataCleaner();
    }

    @Test
    @DisplayName("1. Sentinel Coordinates Cleaning (-Double.MAX_VALUE -> null & UNLOCATED)")
    void testCleanSentinelCoordinates() {
        ProcessedHazardObservation obs = new ProcessedHazardObservation();
        obs.setLongitude(-Double.MAX_VALUE);
        obs.setLatitude(-Double.MAX_VALUE);

        cleaner.cleanCoordinates(obs);

        assertNull(obs.getLongitude());
        assertNull(obs.getLatitude());
        assertEquals(QualityStatus.UNLOCATED, obs.getQualityStatus());
        assertTrue(obs.getProcessingMetadata().getAnomaliesDetected().stream()
                .anyMatch(a -> a.contains("SENTINEL_COORDINATES_DETECTED")));
        assertTrue(obs.getProcessingMetadata().getCleaningActions().stream()
                .anyMatch(a -> a.contains("CLEANED_SENTINEL_COORDINATES_TO_NULL")));
    }

    @Test
    @DisplayName("2. Valid Coordinates Preserved & Marked Valid")
    void testValidCoordinates() {
        ProcessedHazardObservation obs = new ProcessedHazardObservation();
        obs.setLongitude(85.1376);
        obs.setLatitude(25.5941);
        obs.setStartDate(LocalDate.of(2024, 7, 1));

        cleaner.cleanCoordinates(obs);
        cleaner.evaluateFinalQualityStatus(obs);

        assertEquals(85.1376, obs.getLongitude());
        assertEquals(25.5941, obs.getLatitude());
        assertEquals(QualityStatus.VALID, obs.getQualityStatus());
        assertTrue(obs.getProcessingMetadata().getAnomaliesDetected().isEmpty());
    }

    @Test
    @DisplayName("3. Negative Precipitation Sanitized to Zero")
    void testNegativePrecipitationCleaning() {
        ProcessedHazardObservation obs = new ProcessedHazardObservation();
        obs.setPrecipitationMm(-12.5);

        cleaner.cleanNumericMetrics(obs);

        assertEquals(0.0, obs.getPrecipitationMm());
        assertTrue(obs.getProcessingMetadata().getAnomaliesDetected().stream()
                .anyMatch(a -> a.contains("NEGATIVE_PRECIPITATION_DETECTED")));
        assertTrue(obs.getProcessingMetadata().getCleaningActions().stream()
                .anyMatch(a -> a.contains("FLOORED_NEGATIVE_PRECIPITATION_TO_ZERO")));
    }

    @Test
    @DisplayName("4. Negative Fatalities & Displaced Sanitized to Zero")
    void testNegativeImpactCleaning() {
        ProcessedHazardObservation obs = new ProcessedHazardObservation();
        obs.setFatalities(-5.0);
        obs.setDisplacedPopulation(-100.0);
        obs.setAffectedAreaSqKm(-50.0);

        cleaner.cleanNumericMetrics(obs);

        assertEquals(0.0, obs.getFatalities());
        assertEquals(0.0, obs.getDisplacedPopulation());
        assertEquals(0.0, obs.getAffectedAreaSqKm());
        assertEquals(3, obs.getProcessingMetadata().getCleaningActions().size());
    }

    @Test
    @DisplayName("5. Date Span Duration Calculation & Reconciliation")
    void testDateDurationCalculation() {
        ProcessedHazardObservation obs = new ProcessedHazardObservation();
        obs.setStartDate(LocalDate.of(2008, 8, 18));
        obs.setEndDate(LocalDate.of(2008, 9, 24));
        obs.setDurationDays(38.0);

        cleaner.cleanAndDeriveTemporalMetrics(obs);

        assertEquals(38.0, obs.getDerivedMetrics().get("calculatedDurationDays"));
        assertEquals(38.0, obs.getDurationDays());

        // Test missing duration inference
        ProcessedHazardObservation obs2 = new ProcessedHazardObservation();
        obs2.setStartDate(LocalDate.of(2010, 7, 12));
        obs2.setEndDate(LocalDate.of(2010, 7, 14));
        obs2.setDurationDays(null);

        cleaner.cleanAndDeriveTemporalMetrics(obs2);

        assertEquals(3.0, obs2.getDurationDays());
        assertEquals(3.0, obs2.getDerivedMetrics().get("calculatedDurationDays"));
    }

    @Test
    @DisplayName("6. Quality Status Enum Parsing & Validation")
    void testQualityStatusEnum() {
        assertEquals(QualityStatus.VALID, QualityStatus.fromString("VALID"));
        assertEquals(QualityStatus.VALID, QualityStatus.fromString("valid"));
        assertEquals(QualityStatus.UNLOCATED, QualityStatus.fromString("UNLOCATED"));
        assertEquals(QualityStatus.PARTIAL, QualityStatus.fromString("PARTIAL"));
        assertEquals(QualityStatus.INVALID, QualityStatus.fromString("INVALID"));

        assertThrows(IllegalArgumentException.class, () -> QualityStatus.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> QualityStatus.fromString("CORRUPT"));
    }
}
