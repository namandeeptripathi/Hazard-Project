package com.hazard;

import com.hazard.dto.risk.contributor.ContributorTreeNodeDto;
import com.hazard.dto.risk.contributor.DetailedRiskContributorDto;
import com.hazard.dto.risk.contributor.DistrictRiskContributorsProfileDto;
import com.hazard.dto.risk.contributor.RiskExplanationDto;
import com.hazard.service.risk.contributor.RiskContributorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for Stage 4.9 — Risk Contributor Service.
 */
@SpringBootTest
@DisplayName("Stage 4.9: Risk Contributor Service Tests")
public class RiskContributorServiceTests {

    private static final Logger log = LoggerFactory.getLogger(RiskContributorServiceTests.class);

    @Autowired
    private RiskContributorService riskContributorService;

    @Test
    @DisplayName("Evaluate complete risk contributors profile for Sitamarhi")
    void testGetSitamarhiContributorsProfile() {
        DistrictRiskContributorsProfileDto profile = riskContributorService.getDistrictContributorsProfile("Sitamarhi", 5);

        assertNotNull(profile);
        assertEquals("Sitamarhi", profile.getDistrictName());
        assertNotNull(profile.getRiskScore());
        assertNotNull(profile.getRiskScore100());
        assertNotNull(profile.getRiskTier());
        assertNotNull(profile.getConfigurationId());

        assertFalse(profile.getTopContributors().isEmpty());
        assertNotNull(profile.getContributorTree());
        assertNotNull(profile.getExplanation());

        log.info("✅ Sitamarhi Contributor Profile verified: Score={}/100, TopDriver={}, TreeNodes={}",
                profile.getRiskScore100(), profile.getTopContributors().get(0).getName(),
                profile.getContributorTree().getChildren().size());
    }

    @Test
    @DisplayName("Evaluate complete risk contributors profile for Patna")
    void testGetPatnaContributorsProfile() {
        DistrictRiskContributorsProfileDto profile = riskContributorService.getDistrictContributorsProfile("Patna", 5);

        assertNotNull(profile);
        assertEquals("Patna", profile.getDistrictName());
        assertFalse(profile.getTopContributors().isEmpty());
        assertNotNull(profile.getExplanation().getSummaryHeadline());

        log.info("✅ Patna Contributor Profile verified: Score={}/100, DominantPillar={}",
                profile.getRiskScore100(), profile.getExplanation().getDominantPillar());
    }

    @Test
    @DisplayName("Retrieve top N ranked contributors list with configurable limit")
    void testGetTopContributorsList() {
        List<DetailedRiskContributorDto> top3 = riskContributorService.getTopContributors("Sitamarhi", 3);
        assertNotNull(top3);
        assertEquals(3, top3.size());
        assertEquals(1, top3.get(0).getRank());
        assertEquals(2, top3.get(1).getRank());
        assertEquals(3, top3.get(2).getRank());

        // Verify descending order of contribution
        assertTrue(top3.get(0).getContribution() >= top3.get(1).getContribution());
        assertTrue(top3.get(1).getContribution() >= top3.get(2).getContribution());
    }

    @Test
    @DisplayName("Retrieve hierarchical contributor tree")
    void testGetContributorTree() {
        ContributorTreeNodeDto tree = riskContributorService.getContributorTree("Sitamarhi");
        assertNotNull(tree);
        assertEquals("TOTAL_RISK", tree.getId());
        assertFalse(tree.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Retrieve natural-language risk explanation")
    void testGetRiskExplanation() {
        RiskExplanationDto explanation = riskContributorService.getRiskExplanation("Sitamarhi");
        assertNotNull(explanation);
        assertNotNull(explanation.getSummaryHeadline());
        assertNotNull(explanation.getNarrative());
        assertFalse(explanation.getPrimaryDrivers().isEmpty());
    }
}
