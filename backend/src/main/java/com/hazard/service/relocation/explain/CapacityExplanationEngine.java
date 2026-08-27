package com.hazard.service.relocation.explain;

import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.explain.CapacityExplanationDto;
import org.springframework.stereotype.Component;

/**
 * Stage 7C.4 — Capacity Explanation Engine.
 *
 * Details the population vs shelter capacity balance, sufficiency status,
 * surplus headroom buffers, or unallocated population deficits.
 */
@Component
public class CapacityExplanationEngine {

    /**
     * Generates a CapacityExplanationDto from a RelocationRecommendationDto.
     */
    public CapacityExplanationDto explainCapacity(RelocationRecommendationDto recommendation) {
        CapacityExplanationDto explanation = new CapacityExplanationDto();

        if (recommendation == null) {
            explanation.setCapacitySufficiencyStatus("NO_DESTINATION");
            explanation.setCapacityNarrative("No relocation recommendation context provided.");
            explanation.setHeadroomMetric("0 beds buffer");
            return explanation;
        }

        long reqPop = (recommendation.getVulnerablePopulation() != null) ? recommendation.getVulnerablePopulation() : 0L;
        long allocPop = (recommendation.getAllocatedPopulation() != null) ? recommendation.getAllocatedPopulation() : 0L;
        long unallocPop = (recommendation.getUnallocatedPopulation() != null) ? recommendation.getUnallocatedPopulation() : reqPop;
        double fitRate = (recommendation.getCapacityFitRatePercentage() != null) ? recommendation.getCapacityFitRatePercentage() : 0.0;

        explanation.setRequiredPopulation(reqPop);
        explanation.setAllocatedPopulation(allocPop);
        explanation.setUnallocatedPopulation(unallocPop);
        explanation.setCapacityFitPercentage(fitRate);

        RecommendedDestinationDto primary = recommendation.getPrimaryDestination();

        // 1. Handle No Destination / No Feasible Safe Sites
        if (primary == null || recommendation.getStatus() == RecommendationStatus.NO_FEASIBLE_DESTINATION) {
            explanation.setDestinationCapacity(0);
            explanation.setAvailableCapacity(0);
            explanation.setCapacitySufficiencyStatus("NO_DESTINATION");
            explanation.setCapacityNarrative(String.format(
                    "Capacity Unavailable: Zero feasible shelters found to house the %d vulnerable evacuees (%d unallocated deficit).",
                    reqPop, unallocPop
            ));
            explanation.setHeadroomMetric("0 buffer (100% capacity deficit)");
            return explanation;
        }

        explanation.setDestinationCapacity(primary.getTotalCapacity());
        explanation.setAvailableCapacity(primary.getAvailableCapacity());

        Integer availCap = primary.getAvailableCapacity();

        // 2. Unbounded Capacity (null in raw infrastructure data)
        if (availCap == null) {
            explanation.setCapacitySufficiencyStatus("UNBOUNDED");
            explanation.setCapacityNarrative(String.format(
                    "Unconstrained Capacity: Destination '%s' has unbounded emergency capacity, fully housing all %d evacuees.",
                    primary.getSiteName(), reqPop
            ));
            explanation.setHeadroomMetric("Unbounded surplus capacity");
            return explanation;
        }

        // 3. Zero Available Capacity
        if (availCap <= 0) {
            explanation.setCapacitySufficiencyStatus("ZERO_CAPACITY");
            explanation.setCapacityNarrative(String.format(
                    "Capacity Exhausted: Destination '%s' has 0 available beds (total capacity %d is saturated).",
                    primary.getSiteName(), primary.getTotalCapacity() != null ? primary.getTotalCapacity() : 0
            ));
            explanation.setHeadroomMetric("0 beds buffer (saturated)");
            return explanation;
        }

        // 4. Exact Capacity Match
        if (availCap == reqPop) {
            explanation.setCapacitySufficiencyStatus("EXACT_MATCH");
            explanation.setCapacityNarrative(String.format(
                    "Exact Capacity Match: Destination '%s' has exactly %d available beds, perfectly matching the %d evacuees with zero remaining buffer.",
                    primary.getSiteName(), availCap, reqPop
            ));
            explanation.setHeadroomMetric("0 beds surplus buffer (100% utilized)");
            return explanation;
        }

        // 5. Sufficient Capacity with Surplus Headroom
        if (availCap > reqPop) {
            int surplus = availCap - (int) reqPop;
            double headroomPct = (reqPop > 0) ? Math.round(((double) surplus / reqPop) * 1000.0) / 10.0 : 100.0;
            explanation.setCapacitySufficiencyStatus("SUFFICIENT_HEADROOM");
            explanation.setCapacityNarrative(String.format(
                    "Sufficient Capacity Headroom: '%s' provides %d available beds for %d evacuees, leaving a surplus safety buffer of %d beds (%.1f%% headroom).",
                    primary.getSiteName(), availCap, reqPop, surplus, headroomPct
            ));
            explanation.setHeadroomMetric(String.format("+%d surplus beds (%.1f%% buffer)", surplus, headroomPct));
            return explanation;
        }

        // 6. Partial Capacity Deficit
        int deficit = (int) reqPop - availCap;
        explanation.setCapacitySufficiencyStatus("PARTIAL_DEFICIT");
        explanation.setCapacityNarrative(String.format(
                "Partial Capacity Deficit: '%s' can only accommodate %d of %d evacuees, resulting in an unallocated deficit of %d people.",
                primary.getSiteName(), availCap, reqPop, deficit
        ));
        explanation.setHeadroomMetric(String.format("-%d bed deficit (%.1f%% accommodated)", deficit, fitRate));
        return explanation;
    }
}
