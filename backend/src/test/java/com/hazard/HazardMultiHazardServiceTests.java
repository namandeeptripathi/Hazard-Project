package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.MultiHazardConfidence;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.hazard.GeoJsonFeatureCollectionDto;
import com.hazard.dto.multihazard.MultiHazardObservation;
import com.hazard.dto.multihazard.MultiHazardSummaryDto;
import com.hazard.service.multihazard.MultiHazardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional(readOnly = true)
class MultiHazardServiceTests {

    private static final Logger log = LoggerFactory.getLogger(MultiHazardServiceTests.class);

    @Autowired
    private MultiHazardService multiHazardService;

    @Test
    @DisplayName("1. Multi-Hazard Synthesis: Synthesizes Cross-Hazard Observations with Scores and Tiers")
    void testSynthesizeMultiHazardObservations() {
        List<MultiHazardObservation> observations = multiHazardService.getAllMultiHazardObservations(null, null, null, 100);

        assertNotNull(observations);
        assertFalse(observations.isEmpty());

        MultiHazardObservation sample = observations.get(0);
        assertNotNull(sample.getId());
        assertNotNull(sample.getMultiHazardIndex());
        assertTrue(sample.getMultiHazardIndex() >= 0.0);
        assertTrue(sample.getMultiHazardIndex() <= 1.0);
        assertNotNull(sample.getSeverityTier());
        assertNotNull(sample.getDominantHazard());
        assertNotNull(sample.getDominantHazardScore());
        assertNotNull(sample.getConfidence());
        assertFalse(sample.getParticipatingHazards().isEmpty());

        log.info("✅ Multi-Hazard observation synthesized: id={}, index={}, tier={}, dominant={}, confidence={}",
                sample.getId(), sample.getMultiHazardIndex(), sample.getSeverityTier(),
                sample.getDominantHazard(), sample.getConfidence());
    }

    @Test
    @DisplayName("2. Multi-Hazard Filtering by Administrative District (e.g. Patna, Sitamarhi)")
    void testFilterMultiHazardsByDistrict() {
        List<MultiHazardObservation> patnaObs = multiHazardService.getMultiHazardObservationsInDistrict("Patna", null, 20);
        assertFalse(patnaObs.isEmpty());
        assertTrue(patnaObs.stream().allMatch(m -> "Patna".equalsIgnoreCase(m.getAssociatedDistrict())));

        List<MultiHazardObservation> sitamarhiObs = multiHazardService.getMultiHazardObservationsInDistrict("Sitamarhi", null, 20);
        assertFalse(sitamarhiObs.isEmpty());
        assertTrue(sitamarhiObs.stream().allMatch(m -> "Sitamarhi".equalsIgnoreCase(m.getAssociatedDistrict())));

        log.info("✅ District filtering verified for Patna ({} observations) and Sitamarhi ({} observations)",
                patnaObs.size(), sitamarhiObs.size());
    }

    @Test
    @DisplayName("3. Multi-Hazard Dominant Hazard Analysis & Distribution")
    void testDominantHazardDistribution() {
        List<MultiHazardObservation> allObs = multiHazardService.getAllMultiHazardObservations(null, null, null, 100);

        long floodDominant = allObs.stream().filter(m -> m.getDominantHazard() == HazardType.FLOOD).count();
        long rainDominant = allObs.stream().filter(m -> m.getDominantHazard() == HazardType.EXTREME_RAINFALL).count();

        assertTrue(floodDominant > 0 || rainDominant > 0);
        log.info("✅ Dominant hazard distribution verified: Flood dominant={}, Extreme Rainfall dominant={}",
                floodDominant, rainDominant);
    }

    @Test
    @DisplayName("4. Executive Multi-Hazard Catalog Summary")
    void testGetMultiHazardSummary() {
        MultiHazardSummaryDto summary = multiHazardService.getMultiHazardSummary();

        assertNotNull(summary);
        assertEquals("EPSG:4326 (WGS 84)", summary.getCanonicalCrs());
        assertTrue(summary.getTotalMultiHazardObservations() > 0);
        assertNotNull(summary.getSeverityTierDistribution().get("LOW"));
        assertNotNull(summary.getSeverityTierDistribution().get("MODERATE"));
        assertNotNull(summary.getSeverityTierDistribution().get("HIGH"));
        assertNotNull(summary.getSeverityTierDistribution().get("SEVERE"));
        assertEquals(0.50, summary.getConfiguredHazardWeights().get("FLOOD"));
        assertEquals(0.50, summary.getConfiguredHazardWeights().get("EXTREME_RAINFALL"));
        assertFalse(summary.getActiveDistricts().isEmpty());

        log.info("✅ Multi-Hazard Summary verified: totalObs={}, tierDistribution={}",
                summary.getTotalMultiHazardObservations(), summary.getSeverityTierDistribution());
    }

    @Test
    @DisplayName("5. GeoJSON Vector Output of Multi-Hazard Observations")
    void testGetMultiHazardGeoJson() {
        GeoJsonFeatureCollectionDto geojson = multiHazardService.getMultiHazardGeoJson(null, null, 50);

        assertNotNull(geojson);
        assertEquals("FeatureCollection", geojson.getType());
        assertFalse(geojson.getFeatures().isEmpty());
        assertTrue(geojson.getFeatures().stream().allMatch(f ->
                f.getProperties().containsKey("multiHazardIndex") &&
                f.getProperties().containsKey("severityTier") &&
                f.getProperties().containsKey("dominantHazard")));

        log.info("✅ Multi-Hazard GeoJSON Vector Layer verified: {} features with multiHazardIndex and severityTier",
                geojson.getCount());
    }
}
