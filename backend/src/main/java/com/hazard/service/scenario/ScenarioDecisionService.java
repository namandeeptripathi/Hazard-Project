package com.hazard.service.scenario;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.relocation.PriorityLevel;
import com.hazard.domain.relocation.RelocationUrgency;
import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.dto.relocation.RelocationPlanDto;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRequestDto;
import com.hazard.dto.relocation.VulnerableHabitationDto;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.scenario.*;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.scenario.ScenarioRepository;
import com.hazard.service.relocation.RelocationPlanningService;
import com.hazard.service.relocation.RelocationPriorityService;
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
 * Stage 9D — Priority & Relocation Recalculation Service.
 *
 * Chains simulated risk (Stage 9B) and simulated Red-Zone classification (Stage 9C)
 * directly into the existing Stage 7 Priority Engine and Stage 6 Relocation Planning Engine.
 * Operates purely in memory with zero mutation of stored baseline data, relocation plans,
 * priority records, or database entities.
 */
@Service
@Transactional(readOnly = true)
public class ScenarioDecisionService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioDecisionService.class);

    private final ScenarioRepository scenarioRepository;
    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final ScenarioExecutionService scenarioExecutionService;
    private final ScenarioRedZoneService scenarioRedZoneService;
    private final RelocationPlanningService relocationPlanningService;
    private final RelocationPriorityService relocationPriorityService;

    public ScenarioDecisionService(ScenarioRepository scenarioRepository,
                                   DistrictBoundaryRepository districtBoundaryRepository,
                                   ScenarioExecutionService scenarioExecutionService,
                                   ScenarioRedZoneService scenarioRedZoneService,
                                   RelocationPlanningService relocationPlanningService,
                                   RelocationPriorityService relocationPriorityService) {
        this.scenarioRepository = scenarioRepository;
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.scenarioExecutionService = scenarioExecutionService;
        this.scenarioRedZoneService = scenarioRedZoneService;
        this.relocationPlanningService = relocationPlanningService;
        this.relocationPriorityService = relocationPriorityService;
    }

    /**
     * Recalculates simulated Priority and Relocation decision outcomes for a single district.
     *
     * @param scenarioId Unique scenario identifier
     * @param districtName Target administrative district
     * @return District-level simulated decision outcome
     */
    public DistrictDecisionSimulationDto recalculateDistrictDecision(String scenarioId, String districtName) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario ID cannot be null or empty");
        }

        String targetDistrict = (districtName != null && !districtName.trim().isEmpty())
                ? districtName.trim()
                : "Sitamarhi";

        DistrictBoundary boundary = districtBoundaryRepository.findByName2IgnoreCase(targetDistrict)
                .orElseThrow(() -> new HazardNotFoundException("Administrative district not found: " + targetDistrict));

        // 1. Stage 9B Simulation
        ScenarioExecutionRequestDto execReq = new ScenarioExecutionRequestDto(boundary.getName2());
        ScenarioSimulationResultDto simRiskResult = scenarioExecutionService.executeScenario(scenarioId.trim(), execReq);

        // 2. Stage 9C Red-Zone Recalculation (reuse simulation result)
        DistrictRedZoneSimulationDto redZoneDto = scenarioRedZoneService.buildDistrictRedZoneDto(simRiskResult);

        return buildDistrictDecisionDto(simRiskResult, redZoneDto);
    }

    /**
     * Recalculates Priority and Relocation outcomes across all applicable districts.
     *
     * @param scenarioId Unique scenario identifier
     * @return Aggregated decision simulation result across all districts
     */
    public ScenarioDecisionSimulationResultDto recalculateDecisionAllDistricts(String scenarioId) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario ID cannot be null or empty");
        }

        ScenarioDefinition scenario = scenarioRepository.findById(scenarioId.trim())
                .orElseThrow(() -> new HazardNotFoundException("Scenario definition not found with ID: " + scenarioId));

        List<DistrictBoundary> districts = districtBoundaryRepository.findAllByOrderByName2Asc();
        List<DistrictDecisionSimulationDto> districtResults = new ArrayList<>(districts.size());

        int baselineRedCount = 0;
        int simulatedRedCount = 0;
        int immediateCount = 0;
        int shortTermCount = 0;
        int mediumTermCount = 0;
        int monitoringCount = 0;
        int shiftUpCount = 0;
        int shiftDownCount = 0;
        int shiftUnchangedCount = 0;

        long totalVulnPop = 0L;
        long totalAllocPop = 0L;
        long totalUnallocPop = 0L;

        for (DistrictBoundary boundary : districts) {
            try {
                DistrictDecisionSimulationDto districtDto = recalculateDistrictDecision(scenario.getScenarioId(), boundary.getName2());
                districtResults.add(districtDto);

                if (districtDto.isBaselineRedZone()) baselineRedCount++;
                if (districtDto.isSimulatedRedZone()) simulatedRedCount++;

                PriorityLevel simLevel = districtDto.getSimulatedPriorityLevel();
                if (simLevel != null) {
                    switch (simLevel) {
                        case IMMEDIATE -> immediateCount++;
                        case SHORT_TERM -> shortTermCount++;
                        case MEDIUM_TERM -> mediumTermCount++;
                        case MONITORING -> monitoringCount++;
                    }
                }

                if ("INCREASED".equalsIgnoreCase(districtDto.getPriorityShiftDirection())) {
                    shiftUpCount++;
                } else if ("DECREASED".equalsIgnoreCase(districtDto.getPriorityShiftDirection())) {
                    shiftDownCount++;
                } else {
                    shiftUnchangedCount++;
                }

                if (districtDto.getVulnerablePopulation() != null) totalVulnPop += districtDto.getVulnerablePopulation();
                if (districtDto.getSimulatedAllocatedPopulation() != null) totalAllocPop += districtDto.getSimulatedAllocatedPopulation();
                if (districtDto.getSimulatedUnallocatedPopulation() != null) totalUnallocPop += districtDto.getSimulatedUnallocatedPopulation();

            } catch (Exception e) {
                log.warn("Failed decision recalculation for district {} in scenario {}: {}",
                        boundary.getName2(), scenarioId, e.getMessage());
            }
        }

        // Sort by simulated priority score descending
        districtResults.sort(Comparator.comparingDouble((DistrictDecisionSimulationDto d) ->
                d.getSimulatedPriorityScore() != null ? d.getSimulatedPriorityScore() : 0.0).reversed());

        double deficitPct = totalVulnPop > 0
                ? RiskCalculationEngine.round1((double) totalUnallocPop / (double) totalVulnPop * 100.0)
                : 0.0;

        ScenarioDecisionSimulationResultDto result = new ScenarioDecisionSimulationResultDto();
        result.setScenarioId(scenario.getScenarioId());
        result.setScenarioName(scenario.getScenarioName());
        result.setScenarioType(scenario.getScenarioType());
        result.setScenarioTypeDisplayName(scenario.getScenarioType() != null ? scenario.getScenarioType().getDisplayName() : null);
        result.setDescription(scenario.getDescription());
        result.setAppliedRainfallChange(scenario.getRainfallChange());
        result.setAppliedHazardIntensityChange(scenario.getHazardIntensityChange());
        result.setAppliedPopulationExposureChange(scenario.getPopulationExposureChange());

        result.setTotalDistrictsEvaluated(districtResults.size());
        result.setBaselineRedZoneCount(baselineRedCount);
        result.setSimulatedRedZoneCount(simulatedRedCount);
        result.setNetRedZoneChange(simulatedRedCount - baselineRedCount);

        result.setImmediatePriorityCount(immediateCount);
        result.setShortTermPriorityCount(shortTermCount);
        result.setMediumTermPriorityCount(mediumTermCount);
        result.setMonitoringPriorityCount(monitoringCount);

        result.setPriorityShiftUpCount(shiftUpCount);
        result.setPriorityShiftDownCount(shiftDownCount);
        result.setPriorityUnchangedCount(shiftUnchangedCount);

        result.setTotalVulnerablePopulation(totalVulnPop);
        result.setTotalAllocatedPopulation(totalAllocPop);
        result.setTotalUnallocatedPopulation(totalUnallocPop);
        result.setOverallCapacityDeficitPercentage(deficitPct);

        result.setDistrictResults(districtResults);
        result.setSimulatedAt(LocalDateTime.now());

        String summary = String.format(
                "Decision simulation for scenario '%s' [%s] across %d districts: Red Zones = %d (net %s%d), Priorities: %d IMMEDIATE, %d SHORT_TERM, %d MEDIUM_TERM, %d MONITORING (Escalated: %d). Total relocation deficit: %d/%d (%.1f%%).",
                scenario.getScenarioName(), scenario.getScenarioId(), districtResults.size(),
                simulatedRedCount, (simulatedRedCount - baselineRedCount) >= 0 ? "+" : "", (simulatedRedCount - baselineRedCount),
                immediateCount, shortTermCount, mediumTermCount, monitoringCount, shiftUpCount,
                totalUnallocPop, totalVulnPop, deficitPct
        );
        result.setSummary(summary);

        log.info("Stage 9D: Recalculated decisions for scenario '{}' [{}] across {} districts — Immediate: {}, Deficit: {}/{}",
                scenario.getScenarioName(), scenario.getScenarioId(), districtResults.size(),
                immediateCount, totalUnallocPop, totalVulnPop);

        return result;
    }

    /**
     * Flexible dispatcher handling both single-district and batch requests.
     */
    public ScenarioDecisionSimulationResultDto recalculateDecision(String scenarioId, ScenarioExecutionRequestDto request) {
        if (request != null && request.getDistrictName() != null && !request.getDistrictName().trim().isEmpty()) {
            ScenarioDefinition scenario = scenarioRepository.findById(scenarioId.trim())
                    .orElseThrow(() -> new HazardNotFoundException("Scenario definition not found with ID: " + scenarioId));

            DistrictDecisionSimulationDto single = recalculateDistrictDecision(scenarioId, request.getDistrictName());
            ScenarioDecisionSimulationResultDto result = new ScenarioDecisionSimulationResultDto();
            result.setScenarioId(scenario.getScenarioId());
            result.setScenarioName(scenario.getScenarioName());
            result.setScenarioType(scenario.getScenarioType());
            result.setScenarioTypeDisplayName(scenario.getScenarioType() != null ? scenario.getScenarioType().getDisplayName() : null);
            result.setDescription(scenario.getDescription());
            result.setAppliedRainfallChange(scenario.getRainfallChange());
            result.setAppliedHazardIntensityChange(scenario.getHazardIntensityChange());
            result.setAppliedPopulationExposureChange(scenario.getPopulationExposureChange());
            result.setTotalDistrictsEvaluated(1);

            result.setBaselineRedZoneCount(single.isBaselineRedZone() ? 1 : 0);
            result.setSimulatedRedZoneCount(single.isSimulatedRedZone() ? 1 : 0);
            result.setNetRedZoneChange((single.isSimulatedRedZone() ? 1 : 0) - (single.isBaselineRedZone() ? 1 : 0));

            if (single.getSimulatedPriorityLevel() == PriorityLevel.IMMEDIATE) result.setImmediatePriorityCount(1);
            else if (single.getSimulatedPriorityLevel() == PriorityLevel.SHORT_TERM) result.setShortTermPriorityCount(1);
            else if (single.getSimulatedPriorityLevel() == PriorityLevel.MEDIUM_TERM) result.setMediumTermPriorityCount(1);
            else if (single.getSimulatedPriorityLevel() == PriorityLevel.MONITORING) result.setMonitoringPriorityCount(1);

            if ("INCREASED".equalsIgnoreCase(single.getPriorityShiftDirection())) result.setPriorityShiftUpCount(1);
            else if ("DECREASED".equalsIgnoreCase(single.getPriorityShiftDirection())) result.setPriorityShiftDownCount(1);
            else result.setPriorityUnchangedCount(1);

            result.setTotalVulnerablePopulation(single.getVulnerablePopulation() != null ? single.getVulnerablePopulation() : 0L);
            result.setTotalAllocatedPopulation(single.getSimulatedAllocatedPopulation() != null ? single.getSimulatedAllocatedPopulation() : 0L);
            result.setTotalUnallocatedPopulation(single.getSimulatedUnallocatedPopulation() != null ? single.getSimulatedUnallocatedPopulation() : 0L);

            double defPct = result.getTotalVulnerablePopulation() > 0
                    ? RiskCalculationEngine.round1((double) result.getTotalUnallocatedPopulation() / (double) result.getTotalVulnerablePopulation() * 100.0)
                    : 0.0;
            result.setOverallCapacityDeficitPercentage(defPct);

            result.getDistrictResults().add(single);
            result.setSummary(String.format("District %s Decision Simulation: %s", single.getDistrictName(), single.getSummary()));
            return result;
        }

        return recalculateDecisionAllDistricts(scenarioId);
    }

    // =========================================================================
    // HELPER: BUILD DISTRICT DECISION DTO USING EXISTING ENGINES
    // =========================================================================

    private DistrictDecisionSimulationDto buildDistrictDecisionDto(ScenarioSimulationResultDto simRiskResult,
                                                                   DistrictRedZoneSimulationDto redZoneDto) {
        String district = simRiskResult.getDistrictName();
        DistrictRiskScoreDto baseRisk = simRiskResult.getBaselineRisk();
        DistrictRiskScoreDto simRisk = simRiskResult.getSimulatedRisk();
        ScenarioSimulationContextDto simCtx = simRiskResult.getSimulationContext();

        // 1. Determine baseline vulnerable population
        long basePop = (simCtx != null && simCtx.getBaselineExposedPopulation() != null && simCtx.getBaselineExposedPopulation() > 0)
                ? Math.min(50000L, simCtx.getBaselineExposedPopulation())
                : 250L;

        // 2. Determine simulated vulnerable population
        double popMultiplier = (simCtx != null)
                ? simCtx.getEffectivePopulationMultiplier()
                : 1.0;
        long simPop = (simCtx != null && simCtx.getSimulatedExposedPopulation() != null && simCtx.getSimulatedExposedPopulation() > 0)
                ? Math.min(50000L, simCtx.getSimulatedExposedPopulation())
                : Math.max(10L, Math.round(basePop * popMultiplier));

        // 3. Construct Baseline Habitation & Relocation Plan
        VulnerableHabitationDto baseHab = new VulnerableHabitationDto();
        baseHab.setHabitationId("HAB-" + district.toUpperCase() + "-BASE");
        baseHab.setHabitationName(district + " Baseline Habitation");
        baseHab.setDistrict(district);
        baseHab.setState(baseRisk.getState() != null ? baseRisk.getState() : "Bihar");
        baseHab.setVulnerablePopulation(basePop);
        baseHab.setRiskScore(baseRisk.getRiskScore());
        baseHab.setHazardSeverityScore(simCtx != null ? simCtx.getBaselineHazardScore() : 0.50);
        baseHab.setRedZone(redZoneDto.isBaselineRedZone());
        baseHab.setUrgency(redZoneDto.isBaselineRedZone() ? RelocationUrgency.CRITICAL : (baseRisk.getRiskScore() >= 0.40 ? RelocationUrgency.HIGH : RelocationUrgency.MODERATE));

        RelocationRequestDto baseRelocReq = new RelocationRequestDto();
        baseRelocReq.setDistrict(district);
        baseRelocReq.setHabitation(baseHab);
        baseRelocReq.setVulnerablePopulation(basePop);

        RelocationPlanDto basePlan = relocationPlanningService.planRelocation(baseRelocReq);
        RelocationPriorityResultDto basePriority = relocationPriorityService.scoreSingle(basePlan, baseHab);

        // 4. Construct Simulated Habitation & Relocation Plan
        VulnerableHabitationDto simHab = new VulnerableHabitationDto();
        simHab.setHabitationId("HAB-" + district.toUpperCase() + "-SIM");
        simHab.setHabitationName(district + " Simulated Habitation");
        simHab.setDistrict(district);
        simHab.setState(simRisk.getState() != null ? simRisk.getState() : "Bihar");
        simHab.setVulnerablePopulation(simPop);
        simHab.setRiskScore(simRisk.getRiskScore());
        simHab.setHazardSeverityScore(simCtx != null ? simCtx.getSimulatedHazardScore() : 0.50);
        simHab.setRedZone(redZoneDto.isSimulatedRedZone());
        simHab.setUrgency(redZoneDto.isSimulatedRedZone() ? RelocationUrgency.CRITICAL : (simRisk.getRiskScore() >= 0.40 ? RelocationUrgency.HIGH : RelocationUrgency.MODERATE));

        RelocationRequestDto simRelocReq = new RelocationRequestDto();
        simRelocReq.setDistrict(district);
        simRelocReq.setHabitation(simHab);
        simRelocReq.setVulnerablePopulation(simPop);

        RelocationPlanDto simPlan = relocationPlanningService.planRelocation(simRelocReq);
        RelocationPriorityResultDto simPriority = relocationPriorityService.scoreSingle(simPlan, simHab);

        // 5. Compute Priority Shifts
        double deltaPriority = RiskCalculationEngine.round4(simPriority.getPriorityScore() - basePriority.getPriorityScore());
        String shiftDirection = (Math.abs(deltaPriority) < 0.0001) ? "UNCHANGED" : (deltaPriority > 0 ? "INCREASED" : "DECREASED");

        // 6. Assemble District Decision DTO
        DistrictDecisionSimulationDto dto = new DistrictDecisionSimulationDto();
        dto.setDistrictId(simRisk.getDistrictId() != null ? simRisk.getDistrictId() : baseRisk.getDistrictId());
        dto.setDistrictName(district);
        dto.setGid2(simRisk.getGid2() != null ? simRisk.getGid2() : baseRisk.getGid2());
        dto.setState(simRisk.getState() != null ? simRisk.getState() : "Bihar");

        dto.setBaselineRiskScore(baseRisk.getRiskScore());
        dto.setBaselineRiskScore100(baseRisk.getRiskScore100());
        dto.setBaselineRiskTier(baseRisk.getRiskTier());
        dto.setBaselineRedZone(redZoneDto.isBaselineRedZone());

        dto.setSimulatedRiskScore(simRisk.getRiskScore());
        dto.setSimulatedRiskScore100(simRisk.getRiskScore100());
        dto.setSimulatedRiskTier(simRisk.getRiskTier());
        dto.setSimulatedRedZone(redZoneDto.isSimulatedRedZone());
        dto.setRedZoneTransitionType(redZoneDto.getTransitionType());

        dto.setBaselinePriorityScore(basePriority.getPriorityScore());
        dto.setBaselinePriorityLevel(basePriority.getPriorityLevel());
        dto.setSimulatedPriorityScore(simPriority.getPriorityScore());
        dto.setSimulatedPriorityLevel(simPriority.getPriorityLevel());
        dto.setDeltaPriorityScore(deltaPriority);
        dto.setPriorityShiftDirection(shiftDirection);
        dto.setSimulatedPriorityResult(simPriority);

        dto.setVulnerablePopulation(simPop);
        dto.setBaselineAllocatedPopulation(basePlan.getTotalAllocatedPopulation());
        dto.setBaselineUnallocatedPopulation(basePlan.getTotalUnallocatedPopulation());
        dto.setBaselineRelocationStatus(basePlan.getOverallStatus());

        dto.setSimulatedAllocatedPopulation(simPlan.getTotalAllocatedPopulation());
        dto.setSimulatedUnallocatedPopulation(simPlan.getTotalUnallocatedPopulation());
        dto.setSimulatedRelocationStatus(simPlan.getOverallStatus());
        dto.setFeasibleCandidateSitesCount(simPlan.getTotalCandidateSitesEvaluated() != null ? simPlan.getTotalCandidateSitesEvaluated() : 0);
        dto.setSimulatedRelocationPlan(simPlan);

        String summary = String.format(
                "District %s: Risk %.1f->%.1f/100 (%s), RedZone: %s->%s (%s), Priority: %.4f->%.4f (%s->%s, %s), Relocation: Allocated %d/%d (Deficit: %d, %s)",
                district, dto.getBaselineRiskScore100(), dto.getSimulatedRiskScore100(), dto.getSimulatedRiskTier(),
                dto.isBaselineRedZone() ? "YES" : "NO", dto.isSimulatedRedZone() ? "YES" : "NO", dto.getRedZoneTransitionType(),
                dto.getBaselinePriorityScore(), dto.getSimulatedPriorityScore(),
                dto.getBaselinePriorityLevel(), dto.getSimulatedPriorityLevel(), shiftDirection,
                dto.getSimulatedAllocatedPopulation(), dto.getVulnerablePopulation(),
                dto.getSimulatedUnallocatedPopulation(), dto.getSimulatedRelocationStatus()
        );
        dto.setSummary(summary);

        return dto;
    }
}
