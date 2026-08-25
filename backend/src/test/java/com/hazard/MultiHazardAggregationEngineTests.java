package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.multihazard.HazardParticipationDto;
import com.hazard.service.multihazard.MultiHazardAggregationEngine;
import com.hazard.service.multihazard.MultiHazardConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MultiHazardAggregationEngineTests {

    private MultiHazardAggregationEngine aggregationEngine;
    private MultiHazardConfig defaultConfig;

    @BeforeEach
    void setUp() {
        aggregationEngine = new MultiHazardAggregationEngine();
        defaultConfig = MultiHazardConfig.createDefault();
    }

    @Test
    @DisplayName("1. Multi-Hazard Aggregation: Balanced 50% Flood + 50% Extreme Rainfall")
    void testMultiHazardAggregation() {
        List<HazardParticipationDto> participants = new ArrayList<>();
        // Flood Score = 0.60 (HIGH), Rainfall Score = 0.80 (SEVERE)
        participants.add(new HazardParticipationDto("DFO-1", HazardType.FLOOD, "DFO", "Patna", 0.60, SeverityTier.HIGH, 0.50, 0.50, 0.30, null, null, null));
        participants.add(new HazardParticipationDto("RAIN-1", HazardType.EXTREME_RAINFALL, "OPEN_METEO", "Patna", 0.80, SeverityTier.SEVERE, 0.50, 0.50, 0.40, null, null, null));
        // Expected index: 0.60 * 0.50 + 0.80 * 0.50 = 0.30 + 0.40 = 0.7000 -> HIGH

        MultiHazardAggregationEngine.MultiHazardResult result = aggregationEngine.aggregate(participants, defaultConfig);

        assertNotNull(result);
        assertEquals(0.7000, result.multiHazardIndex(), 0.0001);
        assertEquals(SeverityTier.HIGH, result.severityTier());
        assertEquals(HazardType.EXTREME_RAINFALL, result.dominantHazard());
        assertEquals(0.80, result.dominantHazardScore(), 0.0001);
        assertEquals(HazardType.FLOOD, result.secondaryHazard());
        assertEquals(0.60, result.secondaryHazardScore(), 0.0001);
        assertEquals(1.00, result.completenessRatio());
    }

    @Test
    @DisplayName("2. Single Hazard Context: Effective Weight Recalibrates to 1.00")
    void testSingleHazardEffectiveWeight() {
        List<HazardParticipationDto> participants = new ArrayList<>();
        // Only Flood is present (0.75 -> SEVERE)
        participants.add(new HazardParticipationDto("DFO-1", HazardType.FLOOD, "DFO", "Sitamarhi", 0.75, SeverityTier.SEVERE, 0.50, 1.00, 0.75, null, null, null));

        MultiHazardAggregationEngine.MultiHazardResult result = aggregationEngine.aggregate(participants, defaultConfig);

        assertNotNull(result);
        assertEquals(0.7500, result.multiHazardIndex(), 0.0001);
        assertEquals(SeverityTier.SEVERE, result.severityTier());
        assertEquals(HazardType.FLOOD, result.dominantHazard());
        assertNull(result.secondaryHazard());
        assertEquals(0.50, result.completenessRatio(), 0.01); // 1 of 2 configured hazards
    }

    @Test
    @DisplayName("3. Configured Multi-Hazard Weights Validation Fails if Sum != 1.0000")
    void testInvalidWeightsValidation() {
        Map<HazardType, Double> badWeights = new LinkedHashMap<>();
        badWeights.put(HazardType.FLOOD, 0.60);
        badWeights.put(HazardType.EXTREME_RAINFALL, 0.60); // Sum = 1.20 != 1.00

        assertThrows(IllegalStateException.class, () ->
                new MultiHazardConfig(badWeights, 25000.0, 3, "Invalid weights")
        );
    }

    @Test
    @DisplayName("4. Clamping and Rounding to [0.0000, 1.0000]")
    void testClampingAndRounding() {
        List<HazardParticipationDto> participants = new ArrayList<>();
        participants.add(new HazardParticipationDto("H1", HazardType.FLOOD, "SRC", "Loc", 1.00, SeverityTier.SEVERE, 0.50, 0.50, 0.50, null, null, null));
        participants.add(new HazardParticipationDto("H2", HazardType.EXTREME_RAINFALL, "SRC", "Loc", 1.00, SeverityTier.SEVERE, 0.50, 0.50, 0.50, null, null, null));

        MultiHazardAggregationEngine.MultiHazardResult result = aggregationEngine.aggregate(participants, defaultConfig);

        assertNotNull(result);
        assertEquals(1.0000, result.multiHazardIndex());
        assertEquals(SeverityTier.SEVERE, result.severityTier());
    }
}
