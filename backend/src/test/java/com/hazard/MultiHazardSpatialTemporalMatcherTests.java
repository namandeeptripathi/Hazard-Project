package com.hazard;

import com.hazard.domain.hazard.MultiHazardConfidence;
import com.hazard.domain.hazard.SpatialRelationship;
import com.hazard.domain.hazard.TemporalRelationship;
import com.hazard.service.multihazard.MultiHazardSpatialTemporalMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MultiHazardSpatialTemporalMatcherTests {

    private MultiHazardSpatialTemporalMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new MultiHazardSpatialTemporalMatcher();
    }

    @Test
    @DisplayName("1. Spatial Matching: Exact Point (<100m) & Proximity (<=25km)")
    void testSpatialMatching() {
        // Exact same coordinate (Patna station)
        SpatialRelationship exact = matcher.evaluateSpatialRelationship(
                85.1376, 25.5941, "Patna",
                85.1376, 25.5941, "Patna",
                25000.0
        );
        assertEquals(SpatialRelationship.EXACT_POINT, exact);

        // Nearby coordinate within 10 km
        SpatialRelationship prox = matcher.evaluateSpatialRelationship(
                85.1376, 25.5941, "Patna",
                85.2000, 25.6000, "Patna",
                25000.0
        );
        assertEquals(SpatialRelationship.PROXIMITY, prox);

        // Disjoint coordinates (>100 km) in different districts
        SpatialRelationship disjoint = matcher.evaluateSpatialRelationship(
                85.1376, 25.5941, "Patna",
                87.0000, 25.2500, "Bhagalpur",
                25000.0
        );
        assertEquals(SpatialRelationship.DISJOINT, disjoint);
    }

    @Test
    @DisplayName("2. Spatial Matching: District Containment Fallback")
    void testDistrictContainmentFallback() {
        // Missing coordinates but matching district
        SpatialRelationship districtMatch = matcher.evaluateSpatialRelationship(
                null, null, "Sitamarhi",
                null, null, "Sitamarhi",
                25000.0
        );
        assertEquals(SpatialRelationship.DISTRICT_CONTAINMENT, districtMatch);

        // Missing coordinates and mismatched district
        SpatialRelationship disjoint = matcher.evaluateSpatialRelationship(
                null, null, "Patna",
                null, null, "Gaya",
                25000.0
        );
        assertEquals(SpatialRelationship.DISJOINT, disjoint);
    }

    @Test
    @DisplayName("3. Temporal Matching: Overlap, Same Day, Proximate Window & Disjoint")
    void testTemporalMatching() {
        // Direct overlap: 2020-06-28..2020-07-02 and 2020-06-29
        TemporalRelationship overlap = matcher.evaluateTemporalRelationship(
                LocalDate.of(2020, 6, 28), LocalDate.of(2020, 7, 2), null,
                LocalDate.of(2020, 6, 29), LocalDate.of(2020, 6, 29), null,
                3
        );
        assertEquals(TemporalRelationship.EXACT_OVERLAP, overlap);

        // Same day
        TemporalRelationship sameDay = matcher.evaluateTemporalRelationship(
                LocalDate.of(2020, 6, 29), LocalDate.of(2020, 6, 29), null,
                LocalDate.of(2020, 6, 29), LocalDate.of(2020, 6, 29), null,
                3
        );
        assertEquals(TemporalRelationship.SAME_DAY, sameDay);

        // Proximate window (2 days gap, buffer is 3 days)
        TemporalRelationship prox = matcher.evaluateTemporalRelationship(
                LocalDate.of(2020, 6, 20), LocalDate.of(2020, 6, 25), null,
                LocalDate.of(2020, 6, 27), LocalDate.of(2020, 6, 28), null,
                3
        );
        assertEquals(TemporalRelationship.PROXIMATE_WINDOW, prox);

        // Disjoint (1 year gap)
        TemporalRelationship disjoint = matcher.evaluateTemporalRelationship(
                LocalDate.of(2008, 8, 1), LocalDate.of(2008, 8, 10), null,
                LocalDate.of(2020, 6, 29), LocalDate.of(2020, 6, 29), null,
                3
        );
        assertEquals(TemporalRelationship.DISJOINT_TIME, disjoint);
    }

    @Test
    @DisplayName("4. Confidence Evaluation: FULL_MATCH, SPATIAL_ONLY, TEMPORAL_ONLY")
    void testConfidenceEvaluation() {
        assertEquals(MultiHazardConfidence.FULL_MATCH,
                matcher.evaluateConfidence(SpatialRelationship.PROXIMITY, TemporalRelationship.EXACT_OVERLAP));

        assertEquals(MultiHazardConfidence.SPATIAL_ONLY,
                matcher.evaluateConfidence(SpatialRelationship.DISTRICT_CONTAINMENT, TemporalRelationship.DISJOINT_TIME));

        assertEquals(MultiHazardConfidence.TEMPORAL_ONLY,
                matcher.evaluateConfidence(SpatialRelationship.DISJOINT, TemporalRelationship.SAME_DAY));

        assertEquals(MultiHazardConfidence.SINGLE_HAZARD_CONTEXT,
                matcher.evaluateConfidence(SpatialRelationship.DISJOINT, TemporalRelationship.DISJOINT_TIME));
    }
}
