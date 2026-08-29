package com.hazard.service.scenario;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.scenario.RedZoneTransitionType;
import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.dto.scenario.*;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.scenario.ScenarioRepository;
import com.hazard.service.risk.RiskCalculationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stage 9E — Before vs After Scenario Comparison Service.
 *
 * Reuses the simulated decision outputs produced by Stage 9B, 9C, and 9D
 * (via ScenarioDecisionService) to provide a clear, explainable Before vs After comparison:
 * 1. Risk shift (scores, tiers, directions)
 * 2. Red-Zone status transitions (entered, left, retained, unchanged)
 * 3. Priority escalation/de-escalation (scores, levels, directions)
 * 4. Relocation impact (vulnerable population demand, allocations, shelter deficits)
 *
 * Operates purely in memory with zero mutation of database entities or baseline data.
 */
@Service
@Transactional(readOnly = true)
public class ScenarioComparisonService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioComparisonService.class);

    private final ScenarioRepository scenarioRepository;
    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final ScenarioDecisionService scenarioDecisionService;

    public ScenarioComparisonService(ScenarioRepository scenarioRepository,
                                     DistrictBoundaryRepository districtBoundaryRepository,
                                     ScenarioDecisionService scenarioDecisionService) {
        this.scenarioRepository = scenarioRepository;
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.scenarioDecisionService = scenarioDecisionService;
    }

    /**
     * Compares Before vs After scenario impact for a single administrative district.
     *
     * @param scenarioId Unique scenario identifier
     * @param districtName Target administrative district name
     * @return DistrictScenarioComparisonDto detailing Before vs After changes
     */
    public DistrictScenarioComparisonDto compareDistrictScenario(String scenarioId, String districtName) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario ID cannot be null or empty");
        }

        String targetDistrict = (districtName != null && !districtName.trim().isEmpty())
                ? districtName.trim()
                : "Sitamarhi";

        DistrictBoundary boundary = districtBoundaryRepository.findByName2IgnoreCase(targetDistrict)
                .orElseThrow(() -> new HazardNotFoundException("Administrative district not found: " + targetDistrict));

        // Reuse Stage 9D decision output (which already executes 9B + 9C + 6 + 7 in-memory)
        DistrictDecisionSimulationDto decision = scenarioDecisionService.recalculateDistrictDecision(
                scenarioId.trim(), boundary.getName2()
        );

        return mapDecisionToComparisonDto(decision);
    }

    /**
     * Compares Before vs After scenario impact across all administrative districts in Bihar.
     *
     * @param scenarioId Unique scenario identifier
     * @return ScenarioComparisonResultDto containing state-wide aggregate shifts and district comparisons
     */
    public ScenarioComparisonResultDto compareAllDistrictsScenario(String scenarioId) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario ID cannot be null or empty");
        }

        ScenarioDefinition scenario = scenarioRepository.findById(scenarioId.trim())
                .orElseThrow(() -> new HazardNotFoundException("Scenario definition not found with ID: " + scenarioId));

        // Reuse Stage 9D batch decision output
        ScenarioDecisionSimulationResultDto decisionResult = scenarioDecisionService.recalculateDecisionAllDistricts(
                scenario.getScenarioId()
        );

        List<DistrictScenarioComparisonDto> comparisons = new ArrayList<>(decisionResult.getDistrictResults().size());

        int riskUp = 0;
        int riskDown = 0;
        int riskUnchanged = 0;
        double sumRiskDelta100 = 0.0;

        int baseRedCount = 0;
        int simRedCount = 0;
        int enteredRed = 0;
        int leftRed = 0;
        int retainedRed = 0;
        int unchangedNonRed = 0;

        int baseImmediate = 0;
        int simImmediate = 0;
        int prioEscalated = 0;
        int prioDeEscalated = 0;
        int prioUnchanged = 0;

        long totalBaseVuln = 0L;
        long totalSimVuln = 0L;
        long totalBaseAlloc = 0L;
        long totalSimAlloc = 0L;
        long totalBaseUnalloc = 0L;
        long totalSimUnalloc = 0L;

        for (DistrictDecisionSimulationDto d : decisionResult.getDistrictResults()) {
            DistrictScenarioComparisonDto comp = mapDecisionToComparisonDto(d);
            comparisons.add(comp);

            // Risk aggregates
            if ("INCREASED".equalsIgnoreCase(comp.getRiskDirection())) riskUp++;
            else if ("DECREASED".equalsIgnoreCase(comp.getRiskDirection())) riskDown++;
            else riskUnchanged++;
            if (comp.getDeltaRiskScore100() != null) sumRiskDelta100 += comp.getDeltaRiskScore100();

            // Red-Zone aggregates
            if (comp.isBaselineRedZone()) baseRedCount++;
            if (comp.isSimulatedRedZone()) simRedCount++;

            if (comp.getRedZoneTransitionType() != null) {
                switch (comp.getRedZoneTransitionType()) {
                    case ENTERED_RED_ZONE -> enteredRed++;
                    case LEFT_RED_ZONE -> leftRed++;
                    case RETAINED_RED_ZONE -> retainedRed++;
                    case UNCHANGED_NON_RED_ZONE -> unchangedNonRed++;
                }
            }

            // Priority aggregates
            if (comp.getBaselinePriorityLevel() == PriorityLevel.IMMEDIATE) baseImmediate++;
            if (comp.getSimulatedPriorityLevel() == PriorityLevel.IMMEDIATE) simImmediate++;

            if (comp.isPriorityEscalated()) prioEscalated++;
            else if ("DECREASED".equalsIgnoreCase(comp.getPriorityShiftDirection())) prioDeEscalated++;
            else prioUnchanged++;

            // Relocation aggregates
            if (comp.getBaselineVulnerablePopulation() != null) totalBaseVuln += comp.getBaselineVulnerablePopulation();
            if (comp.getSimulatedVulnerablePopulation() != null) totalSimVuln += comp.getSimulatedVulnerablePopulation();
            if (comp.getBaselineAllocatedPopulation() != null) totalBaseAlloc += comp.getBaselineAllocatedPopulation();
            if (comp.getSimulatedAllocatedPopulation() != null) totalSimAlloc += comp.getSimulatedAllocatedPopulation();
            if (comp.getBaselineUnallocatedPopulation() != null) totalBaseUnalloc += comp.getBaselineUnallocatedPopulation();
            if (comp.getSimulatedUnallocatedPopulation() != null) totalSimUnalloc += comp.getSimulatedUnallocatedPopulation();
        }

        // Sort by simulated risk score descending
        comparisons.sort(Comparator.comparingDouble((DistrictScenarioComparisonDto c) ->
                c.getSimulatedRiskScore() != null ? c.getSimulatedRiskScore() : 0.0).reversed());

        double avgRiskDelta100 = !comparisons.isEmpty()
                ? RiskCalculationEngine.round1(sumRiskDelta100 / comparisons.size())
                : 0.0;

        double baseDeficitPct = totalBaseVuln > 0
                ? RiskCalculationEngine.round1((double) totalBaseUnalloc / (double) totalBaseVuln * 100.0)
                : 0.0;
        double simDeficitPct = totalSimVuln > 0
                ? RiskCalculationEngine.round1((double) totalSimUnalloc / (double) totalSimVuln * 100.0)
                : 0.0;

        ScenarioComparisonResultDto result = new ScenarioComparisonResultDto();
        result.setScenarioId(scenario.getScenarioId());
        result.setScenarioName(scenario.getScenarioName());
        result.setScenarioType(scenario.getScenarioType());
        result.setScenarioTypeDisplayName(scenario.getScenarioType() != null ? scenario.getScenarioType().getDisplayName() : null);
        result.setDescription(scenario.getDescription());
        result.setAppliedRainfallChange(scenario.getRainfallChange());
        result.setAppliedHazardIntensityChange(scenario.getHazardIntensityChange());
        result.setAppliedPopulationExposureChange(scenario.getPopulationExposureChange());

        result.setTotalDistrictsEvaluated(comparisons.size());

        // 1. Risk Aggregates
        result.setDistrictsWithIncreasedRiskCount(riskUp);
        result.setDistrictsWithDecreasedRiskCount(riskDown);
        result.setDistrictsWithUnchangedRiskCount(riskUnchanged);
        result.setAverageRiskDelta100(avgRiskDelta100);

        // 2. Red-Zone Aggregates
        result.setBaselineRedZoneCount(baseRedCount);
        result.setSimulatedRedZoneCount(simRedCount);
        result.setNetRedZoneChange(simRedCount - baseRedCount);
        result.setEnteredRedZoneCount(enteredRed);
        result.setLeftRedZoneCount(leftRed);
        result.setRetainedRedZoneCount(retainedRed);
        result.setUnchangedNonRedZoneCount(unchangedNonRed);

        // 3. Priority Aggregates
        result.setBaselineImmediatePriorityCount(baseImmediate);
        result.setSimulatedImmediatePriorityCount(simImmediate);
        result.setNetImmediatePriorityChange(simImmediate - baseImmediate);
        result.setPriorityEscalatedCount(prioEscalated);
        result.setPriorityDeEscalatedCount(prioDeEscalated);
        result.setPriorityUnchangedCount(prioUnchanged);

        // 4. Relocation Aggregates
        result.setTotalBaselineVulnerablePopulation(totalBaseVuln);
        result.setTotalSimulatedVulnerablePopulation(totalSimVuln);
        result.setNetVulnerablePopulationChange(totalSimVuln - totalBaseVuln);
        result.setTotalBaselineAllocatedPopulation(totalBaseAlloc);
        result.setTotalSimulatedAllocatedPopulation(totalSimAlloc);
        result.setNetAllocatedPopulationChange(totalSimAlloc - totalBaseAlloc);
        result.setTotalBaselineUnallocatedPopulation(totalBaseUnalloc);
        result.setTotalSimulatedUnallocatedPopulation(totalSimUnalloc);
        result.setNetUnallocatedDeficitChange(totalSimUnalloc - totalBaseUnalloc);
        result.setBaselineCapacityDeficitPercentage(baseDeficitPct);
        result.setSimulatedCapacityDeficitPercentage(simDeficitPct);

        result.setDistrictComparisons(comparisons);
        result.setComparedAt(LocalDateTime.now());

        String summary = String.format(
                "Before vs After Comparison for scenario '%s' [%s] across %d districts: Risk Increased in %d districts (Avg Delta: %s%.1f pts), Red Zones %d -> %d (Net %s%d, %d Entered), Immediate Priorities %d -> %d (Escalated: %d), Relocation Deficit: %d (%.1f%%) -> %d (%.1f%%).",
                scenario.getScenarioName(), scenario.getScenarioId(), comparisons.size(),
                riskUp, avgRiskDelta100 >= 0 ? "+" : "", avgRiskDelta100,
                baseRedCount, simRedCount, (simRedCount - baseRedCount) >= 0 ? "+" : "", (simRedCount - baseRedCount), enteredRed,
                baseImmediate, simImmediate, prioEscalated,
                totalBaseUnalloc, baseDeficitPct, totalSimUnalloc, simDeficitPct
        );
        result.setSummary(summary);

        log.info("Stage 9E: Generated Before vs After Comparison for scenario '{}' [{}] — RiskUp: {}, RedZones: {}->{}, ImmPrio: {}->{}",
                scenario.getScenarioName(), scenario.getScenarioId(), riskUp, baseRedCount, simRedCount, baseImmediate, simImmediate);

        return result;
    }

    /**
     * Flexible dispatcher handling both single-district and batch scenario comparisons.
     */
    public ScenarioComparisonResultDto compareScenario(String scenarioId, ScenarioExecutionRequestDto request) {
        if (request != null && request.getDistrictName() != null && !request.getDistrictName().trim().isEmpty()) {
            ScenarioDefinition scenario = scenarioRepository.findById(scenarioId.trim())
                    .orElseThrow(() -> new HazardNotFoundException("Scenario definition not found with ID: " + scenarioId));

            DistrictScenarioComparisonDto single = compareDistrictScenario(scenarioId, request.getDistrictName());
            ScenarioComparisonResultDto result = new ScenarioComparisonResultDto();
            result.setScenarioId(scenario.getScenarioId());
            result.setScenarioName(scenario.getScenarioName());
            result.setScenarioType(scenario.getScenarioType());
            result.setScenarioTypeDisplayName(scenario.getScenarioType() != null ? scenario.getScenarioType().getDisplayName() : null);
            result.setDescription(scenario.getDescription());
            result.setAppliedRainfallChange(scenario.getRainfallChange());
            result.setAppliedHazardIntensityChange(scenario.getHazardIntensityChange());
            result.setAppliedPopulationExposureChange(scenario.getPopulationExposureChange());
            result.setTotalDistrictsEvaluated(1);

            if ("INCREASED".equalsIgnoreCase(single.getRiskDirection())) result.setDistrictsWithIncreasedRiskCount(1);
            else if ("DECREASED".equalsIgnoreCase(single.getRiskDirection())) result.setDistrictsWithDecreasedRiskCount(1);
            else result.setDistrictsWithUnchangedRiskCount(1);
            result.setAverageRiskDelta100(single.getDeltaRiskScore100() != null ? single.getDeltaRiskScore100() : 0.0);

            result.setBaselineRedZoneCount(single.isBaselineRedZone() ? 1 : 0);
            result.setSimulatedRedZoneCount(single.isSimulatedRedZone() ? 1 : 0);
            result.setNetRedZoneChange((single.isSimulatedRedZone() ? 1 : 0) - (single.isBaselineRedZone() ? 1 : 0));

            if (single.getRedZoneTransitionType() != null) {
                switch (single.getRedZoneTransitionType()) {
                    case ENTERED_RED_ZONE -> result.setEnteredRedZoneCount(1);
                    case LEFT_RED_ZONE -> result.setLeftRedZoneCount(1);
                    case RETAINED_RED_ZONE -> result.setRetainedRedZoneCount(1);
                    case UNCHANGED_NON_RED_ZONE -> result.setUnchangedNonRedZoneCount(1);
                }
            }

            result.setBaselineImmediatePriorityCount(single.getBaselinePriorityLevel() == PriorityLevel.IMMEDIATE ? 1 : 0);
            result.setSimulatedImmediatePriorityCount(single.getSimulatedPriorityLevel() == PriorityLevel.IMMEDIATE ? 1 : 0);
            result.setNetImmediatePriorityChange(result.getSimulatedImmediatePriorityCount() - result.getBaselineImmediatePriorityCount());
            result.setPriorityEscalatedCount(single.isPriorityEscalated() ? 1 : 0);
            result.setPriorityDeEscalatedCount("DECREASED".equalsIgnoreCase(single.getPriorityShiftDirection()) ? 1 : 0);
            result.setPriorityUnchangedCount("UNCHANGED".equalsIgnoreCase(single.getPriorityShiftDirection()) ? 1 : 0);

            result.setTotalBaselineVulnerablePopulation(single.getBaselineVulnerablePopulation() != null ? single.getBaselineVulnerablePopulation() : 0L);
            result.setTotalSimulatedVulnerablePopulation(single.getSimulatedVulnerablePopulation() != null ? single.getSimulatedVulnerablePopulation() : 0L);
            result.setNetVulnerablePopulationChange(result.getTotalSimulatedVulnerablePopulation() - result.getTotalBaselineVulnerablePopulation());

            result.setTotalBaselineAllocatedPopulation(single.getBaselineAllocatedPopulation() != null ? single.getBaselineAllocatedPopulation() : 0L);
            result.setTotalSimulatedAllocatedPopulation(single.getSimulatedAllocatedPopulation() != null ? single.getSimulatedAllocatedPopulation() : 0L);
            result.setNetAllocatedPopulationChange(result.getTotalSimulatedAllocatedPopulation() - result.getTotalBaselineAllocatedPopulation());

            result.setTotalBaselineUnallocatedPopulation(single.getBaselineUnallocatedPopulation() != null ? single.getBaselineUnallocatedPopulation() : 0L);
            result.setTotalSimulatedUnallocatedPopulation(single.getSimulatedUnallocatedPopulation() != null ? single.getSimulatedUnallocatedPopulation() : 0L);
            result.setNetUnallocatedDeficitChange(result.getTotalSimulatedUnallocatedPopulation() - result.getTotalBaselineUnallocatedPopulation());

            double baseDefPct = result.getTotalBaselineVulnerablePopulation() > 0
                    ? RiskCalculationEngine.round1((double) result.getTotalBaselineUnallocatedPopulation() / (double) result.getTotalBaselineVulnerablePopulation() * 100.0)
                    : 0.0;
            double simDefPct = result.getTotalSimulatedVulnerablePopulation() > 0
                    ? RiskCalculationEngine.round1((double) result.getTotalSimulatedUnallocatedPopulation() / (double) result.getTotalSimulatedVulnerablePopulation() * 100.0)
                    : 0.0;
            result.setBaselineCapacityDeficitPercentage(baseDefPct);
            result.setSimulatedCapacityDeficitPercentage(simDefPct);

            result.getDistrictComparisons().add(single);
            result.setSummary(String.format("District %s Comparison: %s", single.getDistrictName(), single.getSummary()));
            return result;
        }

        return compareAllDistrictsScenario(scenarioId);
    }

    // =========================================================================
    // HELPER: MAP DECISION DTO TO BEFORE/AFTER COMPARISON DTO
    // =========================================================================

    private DistrictScenarioComparisonDto mapDecisionToComparisonDto(DistrictDecisionSimulationDto decision) {
        DistrictScenarioComparisonDto comp = new DistrictScenarioComparisonDto();
        comp.setDistrictId(decision.getDistrictId());
        comp.setDistrictName(decision.getDistrictName());
        comp.setGid2(decision.getGid2());
        comp.setState(decision.getState());

        // 1. Risk Comparison
        comp.setBaselineRiskScore(decision.getBaselineRiskScore());
        comp.setBaselineRiskScore100(decision.getBaselineRiskScore100());
        comp.setBaselineRiskTier(decision.getBaselineRiskTier());

        comp.setSimulatedRiskScore(decision.getSimulatedRiskScore());
        comp.setSimulatedRiskScore100(decision.getSimulatedRiskScore100());
        comp.setSimulatedRiskTier(decision.getSimulatedRiskTier());

        double deltaRisk = (decision.getSimulatedRiskScore() != null && decision.getBaselineRiskScore() != null)
                ? RiskCalculationEngine.round4(decision.getSimulatedRiskScore() - decision.getBaselineRiskScore())
                : 0.0;
        double deltaRisk100 = (decision.getSimulatedRiskScore100() != null && decision.getBaselineRiskScore100() != null)
                ? RiskCalculationEngine.round1(decision.getSimulatedRiskScore100() - decision.getBaselineRiskScore100())
                : 0.0;

        comp.setDeltaRiskScore(deltaRisk);
        comp.setDeltaRiskScore100(deltaRisk100);
        String riskDir = (Math.abs(deltaRisk) < 0.0001) ? "UNCHANGED" : (deltaRisk > 0 ? "INCREASED" : "DECREASED");
        comp.setRiskDirection(riskDir);

        // 2. Red-Zone Comparison
        comp.setBaselineRedZone(decision.isBaselineRedZone());
        comp.setSimulatedRedZone(decision.isSimulatedRedZone());
        comp.setRedZoneTransitionType(decision.getRedZoneTransitionType());
        boolean rzChanged = (decision.getRedZoneTransitionType() == RedZoneTransitionType.ENTERED_RED_ZONE
                || decision.getRedZoneTransitionType() == RedZoneTransitionType.LEFT_RED_ZONE);
        comp.setRedZoneChanged(rzChanged);

        // 3. Priority Comparison
        comp.setBaselinePriorityScore(decision.getBaselinePriorityScore());
        comp.setBaselinePriorityLevel(decision.getBaselinePriorityLevel());
        comp.setSimulatedPriorityScore(decision.getSimulatedPriorityScore());
        comp.setSimulatedPriorityLevel(decision.getSimulatedPriorityLevel());
        comp.setDeltaPriorityScore(decision.getDeltaPriorityScore());
        comp.setPriorityShiftDirection(decision.getPriorityShiftDirection());
        comp.setPriorityEscalated("INCREASED".equalsIgnoreCase(decision.getPriorityShiftDirection()));

        // 4. Relocation Comparison
        long baseAlloc = decision.getBaselineAllocatedPopulation() != null ? decision.getBaselineAllocatedPopulation() : 0L;
        long baseUnalloc = decision.getBaselineUnallocatedPopulation() != null ? decision.getBaselineUnallocatedPopulation() : 0L;
        long baseVuln = baseAlloc + baseUnalloc;

        long simAlloc = decision.getSimulatedAllocatedPopulation() != null ? decision.getSimulatedAllocatedPopulation() : 0L;
        long simUnalloc = decision.getSimulatedUnallocatedPopulation() != null ? decision.getSimulatedUnallocatedPopulation() : 0L;
        long simVuln = simAlloc + simUnalloc;

        long deltaVuln = simVuln - baseVuln;
        long deltaAlloc = simAlloc - baseAlloc;
        long deltaUnalloc = simUnalloc - baseUnalloc;

        comp.setBaselineVulnerablePopulation(baseVuln);
        comp.setSimulatedVulnerablePopulation(simVuln);
        comp.setDeltaVulnerablePopulation(deltaVuln);

        comp.setBaselineAllocatedPopulation(baseAlloc);
        comp.setSimulatedAllocatedPopulation(simAlloc);
        comp.setDeltaAllocatedPopulation(deltaAlloc);

        comp.setBaselineUnallocatedPopulation(baseUnalloc);
        comp.setSimulatedUnallocatedPopulation(simUnalloc);
        comp.setDeltaUnallocatedPopulation(deltaUnalloc);

        comp.setBaselineRelocationStatus(decision.getBaselineRelocationStatus());
        comp.setSimulatedRelocationStatus(decision.getSimulatedRelocationStatus());

        String relocDir = (deltaVuln == 0) ? "UNCHANGED" : (deltaVuln > 0 ? "INCREASED" : "DECREASED");
        comp.setRelocationDemandDirection(relocDir);

        String summary = String.format(
                "District %s: Risk %.1f->%.1f/100 (%s%s), RedZone: %s->%s (%s), Priority: %.4f->%.4f (%s->%s, %s), Relocation Deficit: %d->%d (Net: %s%d)",
                decision.getDistrictName(), comp.getBaselineRiskScore100(), comp.getSimulatedRiskScore100(),
                deltaRisk100 >= 0 ? "+" : "", deltaRisk100,
                comp.isBaselineRedZone() ? "YES" : "NO", comp.isSimulatedRedZone() ? "YES" : "NO", comp.getRedZoneTransitionType(),
                comp.getBaselinePriorityScore(), comp.getSimulatedPriorityScore(),
                comp.getBaselinePriorityLevel(), comp.getSimulatedPriorityLevel(), comp.getPriorityShiftDirection(),
                baseUnalloc, simUnalloc, deltaUnalloc >= 0 ? "+" : "", deltaUnalloc
        );
        comp.setSummary(summary);

        return comp;
    }
}
