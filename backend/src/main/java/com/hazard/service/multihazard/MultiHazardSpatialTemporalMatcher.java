package com.hazard.service.multihazard;

import com.hazard.domain.hazard.MultiHazardConfidence;
import com.hazard.domain.hazard.SpatialRelationship;
import com.hazard.domain.hazard.TemporalRelationship;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Pure Spatial and Temporal Coincidence Evaluator for Multi-Hazard Events.
 * Implements geodesic distance matching (WGS 84), administrative district containment,
 * and chronological overlap classification.
 */
@Component
public class MultiHazardSpatialTemporalMatcher {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    /**
     * Evaluates the spatial relationship between two hazard occurrences.
     */
    public SpatialRelationship evaluateSpatialRelationship(Double lon1, Double lat1, String district1,
                                                          Double lon2, Double lat2, String district2,
                                                          double proximityRadiusMeters) {
        if (lon1 == null || lat1 == null || lon2 == null || lat2 == null) {
            if (district1 != null && district2 != null && district1.trim().equalsIgnoreCase(district2.trim())) {
                return SpatialRelationship.DISTRICT_CONTAINMENT;
            }
            return SpatialRelationship.DISJOINT;
        }

        double distanceMeters = calculateHaversineDistanceMeters(lon1, lat1, lon2, lat2);

        if (distanceMeters <= 100.0) {
            return SpatialRelationship.EXACT_POINT;
        } else if (distanceMeters <= proximityRadiusMeters) {
            return SpatialRelationship.PROXIMITY;
        } else if (district1 != null && district2 != null && district1.trim().equalsIgnoreCase(district2.trim())) {
            return SpatialRelationship.DISTRICT_CONTAINMENT;
        }

        return SpatialRelationship.DISJOINT;
    }

    /**
     * Evaluates the temporal relationship between two hazard occurrences.
     */
    public TemporalRelationship evaluateTemporalRelationship(LocalDate start1, LocalDate end1, LocalDateTime time1,
                                                            LocalDate start2, LocalDate end2, LocalDateTime time2,
                                                            int temporalBufferDays) {
        LocalDate s1 = start1 != null ? start1 : (time1 != null ? time1.toLocalDate() : null);
        LocalDate e1 = end1 != null ? end1 : s1;
        LocalDate s2 = start2 != null ? start2 : (time2 != null ? time2.toLocalDate() : null);
        LocalDate e2 = end2 != null ? end2 : s2;

        if (s1 == null || e1 == null || s2 == null || e2 == null) {
            return TemporalRelationship.DISJOINT_TIME;
        }

        // Direct interval overlap check
        if (!s1.isAfter(e2) && !s2.isAfter(e1)) {
            if (s1.equals(s2) && e1.equals(e2) && s1.equals(e1)) {
                return TemporalRelationship.SAME_DAY;
            }
            return TemporalRelationship.EXACT_OVERLAP;
        }

        // Check proximate window gap
        long gapDays;
        if (e1.isBefore(s2)) {
            gapDays = ChronoUnit.DAYS.between(e1, s2);
        } else {
            gapDays = ChronoUnit.DAYS.between(e2, s1);
        }

        if (gapDays <= temporalBufferDays) {
            return TemporalRelationship.PROXIMATE_WINDOW;
        }

        return TemporalRelationship.DISJOINT_TIME;
    }

    /**
     * Determines the overall match confidence classification based on spatial and temporal evidence.
     */
    public MultiHazardConfidence evaluateConfidence(SpatialRelationship spatial, TemporalRelationship temporal) {
        if (spatial != SpatialRelationship.DISJOINT && temporal != TemporalRelationship.DISJOINT_TIME) {
            return MultiHazardConfidence.FULL_MATCH;
        } else if (spatial != SpatialRelationship.DISJOINT) {
            return MultiHazardConfidence.SPATIAL_ONLY;
        } else if (temporal != TemporalRelationship.DISJOINT_TIME) {
            return MultiHazardConfidence.TEMPORAL_ONLY;
        }
        return MultiHazardConfidence.SINGLE_HAZARD_CONTEXT;
    }

    /**
     * Calculates great-circle distance between two WGS 84 points using the Haversine formula.
     */
    public double calculateHaversineDistanceMeters(double lon1, double lat1, double lon2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
