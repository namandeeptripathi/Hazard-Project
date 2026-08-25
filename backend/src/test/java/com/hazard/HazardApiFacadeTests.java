package com.hazard;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.hazard.SeverityTier;
import com.hazard.dto.facade.DistrictHazardOverviewDto;
import com.hazard.dto.facade.HazardSystemHealthDto;
import com.hazard.service.facade.HazardApiFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional(readOnly = true)
class HazardApiFacadeTests {

    private static final Logger log = LoggerFactory.getLogger(HazardApiFacadeTests.class);

    @Autowired
    private HazardApiFacade hazardApiFacade;

    @Test
    @DisplayName("1. System Health Overview: Returns Stage 3 Active Capabilities & 159,005 Baseline Count")
    void testGetSystemHealthOverview() {
        HazardSystemHealthDto health = hazardApiFacade.getSystemHealthOverview();

        assertNotNull(health);
        assertEquals("UP", health.getStatus());
        assertEquals("EPSG:4326 (WGS 84)", health.getCanonicalCrs());
        assertEquals(159005, health.getStage2BaseRecordCount());
        assertEquals(8, health.getActiveCapabilities().size());

        log.info("✅ Hazard System Health verified: status={}, capabilities={}",
                health.getStatus(), health.getActiveCapabilities().size());
    }

    @Test
    @DisplayName("2. Consolidated District Hazard Profile: Patna (Weather Station & Rainfall Hazard Score)")
    void testGetDistrictHazardIntelligencePatna() {
        DistrictHazardOverviewDto patna = hazardApiFacade.getDistrictHazardIntelligence("Patna");

        assertNotNull(patna);
        assertEquals("Patna", patna.getDistrictName());
        assertEquals("Bihar", patna.getState());
        assertTrue(patna.isHasActiveWeatherStation());
        assertTrue(patna.getRecordedExtremeRainfallCount() > 0);
        assertNotNull(patna.getRainfallHazardScore());
        assertNotNull(patna.getMultiHazardIndex());
        assertNotNull(patna.getSeverityTier());
        assertFalse(patna.getIntersectingMajorRivers().isEmpty());

        log.info("✅ Patna District Profile verified: rainfallScore={}, multiHazardIndex={}, tier={}, rivers={}",
                patna.getRainfallHazardScore(), patna.getMultiHazardIndex(),
                patna.getSeverityTier(), patna.getIntersectingMajorRivers().size());
    }

    @Test
    @DisplayName("3. Consolidated District Hazard Profile: Sitamarhi (Historical Flood Hazard Score)")
    void testGetDistrictHazardIntelligenceSitamarhi() {
        DistrictHazardOverviewDto sitamarhi = hazardApiFacade.getDistrictHazardIntelligence("Sitamarhi");

        assertNotNull(sitamarhi);
        assertEquals("Sitamarhi", sitamarhi.getDistrictName());
        assertTrue(sitamarhi.getRecordedFloodCount() > 0);
        assertNotNull(sitamarhi.getFloodHazardScore());
        assertNotNull(sitamarhi.getMultiHazardIndex());
        assertEquals(HazardType.FLOOD, sitamarhi.getDominantHazard());

        log.info("✅ Sitamarhi District Profile verified: floodScore={}, dominant={}",
                sitamarhi.getFloodHazardScore(), sitamarhi.getDominantHazard());
    }

    @Test
    @DisplayName("4. Unknown District Query Rejection with HazardNotFoundException")
    void testUnknownDistrictQuery() {
        assertThrows(RuntimeException.class, () ->
                hazardApiFacade.getDistrictHazardIntelligence("NonExistentDistrict123")
        );
    }
}
