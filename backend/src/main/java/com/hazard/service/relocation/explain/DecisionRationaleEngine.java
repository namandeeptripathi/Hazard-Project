package com.hazard.service.relocation.explain;

import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.explain.DecisionRationaleDto;
import org.springframework.stereotype.Component;

/**
 * Stage 7C.1 — Decision Rationale Synthesis Engine.
 *
 * Combines Stage 7A Priority Assessment and Stage 7B Destination Recommendation into an executive
 * decision rationale answering WHO, WHERE, and WHY, alongside actionable operational guidance.
 */
@Component
public class DecisionRationaleEngine {

    /**
     * Synthesizes the overall DecisionRationaleDto.
     */
    public DecisionRationaleDto synthesizeRationale(RelocationPriorityResultDto priorityResult,
                                                    RelocationRecommendationDto recommendation) {
        DecisionRationaleDto rationale = new DecisionRationaleDto();

        String habName = (recommendation != null && recommendation.getHabitationName() != null)
                ? recommendation.getHabitationName()
                : (priorityResult != null && priorityResult.getHabitationName() != null
                ? priorityResult.getHabitationName()
                : "Origin Habitation");

        long pop = (recommendation != null && recommendation.getVulnerablePopulation() != null)
                ? recommendation.getVulnerablePopulation()
                : (priorityResult != null && priorityResult.getVulnerablePopulation() != null
                ? priorityResult.getVulnerablePopulation()
                : 0L);

        PriorityLevel priorityLevel = (priorityResult != null && priorityResult.getPriorityLevel() != null)
                ? priorityResult.getPriorityLevel()
                : (recommendation != null && recommendation.getPriorityLevel() != null
                ? recommendation.getPriorityLevel()
                : PriorityLevel.MONITORING);

        double priorityScore = (priorityResult != null && priorityResult.getPriorityScore() != null)
                ? priorityResult.getPriorityScore()
                : (recommendation != null && recommendation.getPriorityScore() != null
                ? recommendation.getPriorityScore()
                : 0.0);

        RecommendationStatus status = (recommendation != null && recommendation.getStatus() != null)
                ? recommendation.getStatus()
                : RecommendationStatus.NO_FEASIBLE_DESTINATION;

        RecommendedDestinationDto primary = (recommendation != null) ? recommendation.getPrimaryDestination() : null;

        // 1. WHO Statement
        rationale.setWhoStatement(String.format(
                "%s (%d vulnerable evacuees) classified as %s Priority (Score: %.2f/1.00).",
                habName, pop, priorityLevel.getDisplayName(), priorityScore
        ));

        // 2. WHERE Statement
        if (primary != null && status == RecommendationStatus.RECOMMENDED) {
            String distStr = primary.getDistanceKilometers() != null ? String.format("%.2f km", primary.getDistanceKilometers()) : "N/A";
            rationale.setWhereStatement(String.format(
                    "Relocate to '%s' [%s] in %s (Distance: %s, Destination Suitability Score: %.4f).",
                    primary.getSiteName(), primary.getSiteId(),
                    primary.getDistrict() != null ? primary.getDistrict() : "local district",
                    distStr,
                    primary.getDestinationScore() != null ? primary.getDestinationScore() : 0.0
            ));
        } else if (status == RecommendationStatus.CAPACITY_DEFICIT && primary != null) {
            rationale.setWhereStatement(String.format(
                    "Partial relocation to '%s' [%s] (%d accommodated; %d unallocated deficit).",
                    primary.getSiteName(), primary.getSiteId(),
                    recommendation.getAllocatedPopulation() != null ? recommendation.getAllocatedPopulation() : 0L,
                    recommendation.getUnallocatedPopulation() != null ? recommendation.getUnallocatedPopulation() : 0L
            ));
        } else {
            rationale.setWhereStatement("No feasible emergency safe shelter destination currently identified.");
        }

        // 3. WHY Statement
        if (primary != null && status == RecommendationStatus.RECOMMENDED) {
            rationale.setWhyStatement(String.format(
                    "Origin has %s priority requiring evacuation; '%s' passed all 5 mandatory feasibility gates and ranked #1 among %d candidate shelters with superior suitability (%.2f/100) and proximity.",
                    priorityLevel.name(), primary.getSiteName(),
                    recommendation.getTotalFeasibleCandidates(),
                    primary.getSuitabilityScore() != null ? primary.getSuitabilityScore() : 80.0
            ));
        } else if (status == RecommendationStatus.NO_FEASIBLE_DESTINATION) {
            rationale.setWhyStatement(String.format(
                    "Origin requires evacuation, but all %d candidate safe sites in the region failed mandatory hazard safety, transit distance, or shelter capacity gates.",
                    recommendation != null ? recommendation.getTotalCandidatesEvaluated() : 0
            ));
        } else {
            rationale.setWhyStatement(String.format(
                    "Origin requires evacuation; partial shelter accommodation was found but total regional capacity is insufficient for the full cohort of %d evacuees.",
                    pop
            ));
        }

        // 4. Actionability Guidance
        rationale.setActionabilityGuidance(generateActionabilityGuidance(priorityLevel, status, primary));

        // 5. Key Strengths
        if (primary != null && status == RecommendationStatus.RECOMMENDED) {
            rationale.addKeyStrength("100% population accommodation achieved without capacity deficit.");
            if (primary.getDistanceKilometers() != null && primary.getDistanceKilometers() <= 10.0) {
                rationale.addKeyStrength(String.format("Close transit proximity (%.2f km) enables swift evacuation.", primary.getDistanceKilometers()));
            }
            if (primary.getSuitabilityClass() != null && primary.getSuitabilityClass().isHighlySuitable()) {
                rationale.addKeyStrength("High-suitability infrastructure with established road, water, and healthcare connectivity.");
            }
            if (recommendation.getAlternativeDestinations() != null && !recommendation.getAlternativeDestinations().isEmpty()) {
                rationale.addKeyStrength(String.format("%d fallback alternative safe site(s) available.", recommendation.getAlternativeDestinations().size()));
            }
        }

        // 6. Key Risks or Deficits
        if (priorityLevel == PriorityLevel.IMMEDIATE) {
            rationale.addKeyRiskOrDeficit("Urgent life-safety risk in origin habitation requires immediate emergency response mobilization.");
        }
        if (status == RecommendationStatus.NO_FEASIBLE_DESTINATION) {
            rationale.addKeyRiskOrDeficit("Zero feasible destinations: immediate inter-district shelter requisition or temporary relief camp establishment required.");
        } else if (status == RecommendationStatus.CAPACITY_DEFICIT) {
            rationale.addKeyRiskOrDeficit(String.format("Capacity deficit: %d evacuees require secondary shelter assignment or modular tent staging.",
                    recommendation.getUnallocatedPopulation() != null ? recommendation.getUnallocatedPopulation() : 0L));
        }

        return rationale;
    }

    private String generateActionabilityGuidance(PriorityLevel level, RecommendationStatus status, RecommendedDestinationDto primary) {
        if (status == RecommendationStatus.NO_FEASIBLE_DESTINATION) {
            return "ESCALATE: Declare shelter emergency and dispatch mobile NDRF relief assets to construct temporary emergency camps.";
        }
        if (status == RecommendationStatus.CAPACITY_DEFICIT) {
            return "PARTIAL ACTION: Mobilize primary cohort to recommended site; dispatch overflow requests to neighboring administrative districts.";
        }

        return switch (level) {
            case IMMEDIATE -> String.format(
                    "DEPLOY IMMEDIATELY: Issue evacuation order for origin habitation and begin convoy transport to '%s'.",
                    primary != null ? primary.getSiteName() : "Safe Site"
            );
            case SHORT_TERM -> String.format(
                    "PRE-STAGE: Issue 24-hour evacuation notice and prepare '%s' reception logistics.",
                    primary != null ? primary.getSiteName() : "Safe Site"
            );
            case MEDIUM_TERM -> String.format(
                    "SCHEDULE: Coordinate scheduled planned transfer to '%s' in 48-72 hour window.",
                    primary != null ? primary.getSiteName() : "Safe Site"
            );
            case MONITORING -> "MONITOR: Keep destination on standby; reassess upon subsequent meteorological hazard updates.";
        };
    }
}
