package com.hazard.service.scenario;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.scenario.RedZoneTransitionType;
import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.RedZoneDto;
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
 * Stage 9C — Dynamic Red-Zone Recalculation Service.
 *
 * Evaluates hypothetical Red-Zone shifts under disaster scenario simulations.
 * Directly reuses Stage 9B scenario execution and the canonical Stage 5.1 RedZone classification
 * rules (RedZoneDto / ZoneLevel / RiskTier) without mutating stored baseline state.
 */
@Service
@Transactional(readOnly = true)
public class ScenarioRedZoneService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioRedZoneService.class);

    private final ScenarioRepository scenarioRepository;
    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final ScenarioExecutionService scenarioExecutionService;

    public ScenarioRedZoneService(ScenarioRepository scenarioRepository,
                                  DistrictBoundaryRepository districtBoundaryRepository,
                                  ScenarioExecutionService scenarioExecutionService) {
        this.scenarioRepository = scenarioRepository;
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.scenarioExecutionService = scenarioExecutionService;
    }

    /**
     * Recalculates Red-Zone classification and transition for a single district.
     *
     * @param scenarioId Unique scenario identifier
     * @param districtName Target administrative district
     * @return District-level Red-Zone simulation outcome
     */
    public DistrictRedZoneSimulationDto recalculateDistrictRedZone(String scenarioId, String districtName) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario ID cannot be null or empty");
        }

        String targetDistrict = (districtName != null && !districtName.trim().isEmpty())
                ? districtName.trim()
                : "Sitamarhi";

        // Execute scenario using Stage 9B execution engine
        ScenarioExecutionRequestDto req = new ScenarioExecutionRequestDto(targetDistrict);
        ScenarioSimulationResultDto simResult = scenarioExecutionService.executeScenario(scenarioId.trim(), req);

        return buildDistrictRedZoneDto(simResult);
    }

    /**
     * Recalculates Red-Zone classifications across all applicable districts for a scenario.
     *
     * @param scenarioId Unique scenario identifier
     * @return Aggregated Red-Zone simulation result across all districts
     */
    public ScenarioRedZoneSimulationResultDto recalculateRedZonesAllDistricts(String scenarioId) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario ID cannot be null or empty");
        }

        ScenarioDefinition scenario = scenarioRepository.findById(scenarioId.trim())
                .orElseThrow(() -> new HazardNotFoundException("Scenario definition not found with ID: " + scenarioId));

        List<DistrictBoundary> districts = districtBoundaryRepository.findAllByOrderByName2Asc();
        List<DistrictRedZoneSimulationDto> districtResults = new ArrayList<>(districts.size());

        int baselineRedCount = 0;
        int simulatedRedCount = 0;
        int newlyEnteredCount = 0;
        int leftCount = 0;
        int retainedCount = 0;
        int unchangedNonRedCount = 0;

        List<String> newlyEnteredList = new ArrayList<>();
        List<String> leftList = new ArrayList<>();
        List<String> retainedList = new ArrayList<>();
        List<String> unchangedNonRedList = new ArrayList<>();

        for (DistrictBoundary boundary : districts) {
            try {
                ScenarioExecutionRequestDto req = new ScenarioExecutionRequestDto(boundary.getName2());
                ScenarioSimulationResultDto simResult = scenarioExecutionService.executeScenario(scenario.getScenarioId(), req);
                DistrictRedZoneSimulationDto districtDto = buildDistrictRedZoneDto(simResult);

                districtResults.add(districtDto);

                if (districtDto.isBaselineRedZone()) {
                    baselineRedCount++;
                }
                if (districtDto.isSimulatedRedZone()) {
                    simulatedRedCount++;
                }

                switch (districtDto.getTransitionType()) {
                    case ENTERED_RED_ZONE -> {
                        newlyEnteredCount++;
                        newlyEnteredList.add(districtDto.getDistrictName());
                    }
                    case LEFT_RED_ZONE -> {
                        leftCount++;
                        leftList.add(districtDto.getDistrictName());
                    }
                    case RETAINED_RED_ZONE -> {
                        retainedCount++;
                        retainedList.add(districtDto.getDistrictName());
                    }
                    case UNCHANGED_NON_RED_ZONE -> {
                        unchangedNonRedCount++;
                        unchangedNonRedList.add(districtDto.getDistrictName());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed Red-Zone calculation for district {} in scenario {}: {}",
                        boundary.getName2(), scenarioId, e.getMessage());
            }
        }

        // Sort results by simulated risk score descending
        districtResults.sort(Comparator.comparingDouble((DistrictRedZoneSimulationDto d) ->
                d.getSimulatedRiskScore() != null ? d.getSimulatedRiskScore() : 0.0).reversed());

        int netChange = simulatedRedCount - baselineRedCount;

        ScenarioRedZoneSimulationResultDto result = new ScenarioRedZoneSimulationResultDto();
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
        result.setNetRedZoneChange(netChange);
        result.setNewlyEnteredRedZoneCount(newlyEnteredCount);
        result.setLeftRedZoneCount(leftCount);
        result.setRetainedRedZoneCount(retainedCount);
        result.setUnchangedNonRedZoneCount(unchangedNonRedCount);

        result.setNewlyEnteredDistricts(newlyEnteredList);
        result.setLeftRedZoneDistricts(leftList);
        result.setRetainedRedZoneDistricts(retainedList);
        result.setUnchangedNonRedZoneDistricts(unchangedNonRedList);
        result.setDistrictResults(districtResults);
        result.setSimulatedAt(LocalDateTime.now());

        String summary = String.format(
                "Red-Zone simulation for scenario '%s' [%s] across %d districts: Baseline Red Zones = %d, Simulated Red Zones = %d (Net shift: %s%d). Newly entered Red Zones: %d (%s), Exited: %d (%s), Retained: %d.",
                scenario.getScenarioName(), scenario.getScenarioId(), districtResults.size(),
                baselineRedCount, simulatedRedCount, netChange >= 0 ? "+" : "", netChange,
                newlyEnteredCount, String.join(", ", newlyEnteredList.isEmpty() ? List.of("None") : newlyEnteredList),
                leftCount, String.join(", ", leftList.isEmpty() ? List.of("None") : leftList),
                retainedCount
        );
        result.setSummary(summary);

        log.info("Stage 9C: Recalculated Red Zones for scenario '{}' [{}] across {} districts — Baseline: {}, Sim: {}, Net: {}{}",
                scenario.getScenarioName(), scenario.getScenarioId(), districtResults.size(),
                baselineRedCount, simulatedRedCount, netChange >= 0 ? "+" : "", netChange);

        return result;
    }

    /**
     * Flexible execution endpoint handling both single-district and all-districts requests.
     */
    public ScenarioRedZoneSimulationResultDto recalculateRedZones(String scenarioId, ScenarioExecutionRequestDto request) {
        if (request != null && request.getDistrictName() != null && !request.getDistrictName().trim().isEmpty()) {
            ScenarioDefinition scenario = scenarioRepository.findById(scenarioId.trim())
                    .orElseThrow(() -> new HazardNotFoundException("Scenario definition not found with ID: " + scenarioId));

            DistrictRedZoneSimulationDto singleDistrict = recalculateDistrictRedZone(scenarioId, request.getDistrictName());
            ScenarioRedZoneSimulationResultDto result = new ScenarioRedZoneSimulationResultDto();
            result.setScenarioId(scenario.getScenarioId());
            result.setScenarioName(scenario.getScenarioName());
            result.setScenarioType(scenario.getScenarioType());
            result.setScenarioTypeDisplayName(scenario.getScenarioType() != null ? scenario.getScenarioType().getDisplayName() : null);
            result.setDescription(scenario.getDescription());
            result.setAppliedRainfallChange(scenario.getRainfallChange());
            result.setAppliedHazardIntensityChange(scenario.getHazardIntensityChange());
            result.setAppliedPopulationExposureChange(scenario.getPopulationExposureChange());
            result.setTotalDistrictsEvaluated(1);

            result.setBaselineRedZoneCount(singleDistrict.isBaselineRedZone() ? 1 : 0);
            result.setSimulatedRedZoneCount(singleDistrict.isSimulatedRedZone() ? 1 : 0);
            result.setNetRedZoneChange((singleDistrict.isSimulatedRedZone() ? 1 : 0) - (singleDistrict.isBaselineRedZone() ? 1 : 0));
            result.setNewlyEnteredRedZoneCount(singleDistrict.getTransitionType().isNewlyEntered() ? 1 : 0);
            result.setLeftRedZoneCount(singleDistrict.getTransitionType().isLeft() ? 1 : 0);
            result.setRetainedRedZoneCount(singleDistrict.getTransitionType().isRetained() ? 1 : 0);
            result.setUnchangedNonRedZoneCount(singleDistrict.getTransitionType() == RedZoneTransitionType.UNCHANGED_NON_RED_ZONE ? 1 : 0);

            if (singleDistrict.getTransitionType().isNewlyEntered()) {
                result.getNewlyEnteredDistricts().add(singleDistrict.getDistrictName());
            } else if (singleDistrict.getTransitionType().isLeft()) {
                result.getLeftRedZoneDistricts().add(singleDistrict.getDistrictName());
            } else if (singleDistrict.getTransitionType().isRetained()) {
                result.getRetainedRedZoneDistricts().add(singleDistrict.getDistrictName());
            } else {
                result.getUnchangedNonRedZoneDistricts().add(singleDistrict.getDistrictName());
            }

            result.getDistrictResults().add(singleDistrict);
            result.setSummary(String.format("District %s Red-Zone Simulation: %s", singleDistrict.getDistrictName(), singleDistrict.getTransitionDescription()));
            return result;
        }

        return recalculateRedZonesAllDistricts(scenarioId);
    }

    // =========================================================================
    // HELPER: BUILD DISTRICT RED-ZONE DTO USING CANONICAL STAGE 5.1 CLASSIFIERS
    // =========================================================================

    public DistrictRedZoneSimulationDto buildDistrictRedZoneDto(ScenarioSimulationResultDto simResult) {
        DistrictRiskScoreDto baseRisk = simResult.getBaselineRisk();
        DistrictRiskScoreDto simRisk = simResult.getSimulatedRisk();

        // 1. Reuse canonical Stage 5.1 RedZone classification wrapper
        RedZoneDto baseRedZone = RedZoneDto.fromDistrictRiskScore(baseRisk);
        RedZoneDto simRedZone = RedZoneDto.fromDistrictRiskScore(simRisk);

        boolean baseIsRed = baseRedZone != null && baseRedZone.isRedZone();
        boolean simIsRed = simRedZone != null && simRedZone.isRedZone();

        // 2. Classify transition
        RedZoneTransitionType transition = RedZoneTransitionType.from(baseIsRed, simIsRed);

        // 3. Assemble DTO
        DistrictRedZoneSimulationDto dto = new DistrictRedZoneSimulationDto();
        dto.setDistrictId(simRisk.getDistrictId() != null ? simRisk.getDistrictId() : baseRisk.getDistrictId());
        dto.setDistrictName(simResult.getDistrictName());
        dto.setGid2(simRisk.getGid2() != null ? simRisk.getGid2() : baseRisk.getGid2());
        dto.setState(simRisk.getState() != null ? simRisk.getState() : "Bihar");

        dto.setBaselineRiskScore(baseRisk.getRiskScore());
        dto.setBaselineRiskScore100(baseRisk.getRiskScore100());
        dto.setBaselineRiskTier(baseRisk.getRiskTier());
        dto.setBaselineZoneLevel(baseRedZone != null ? baseRedZone.getZoneLevel() : null);
        dto.setBaselineRedZone(baseIsRed);

        dto.setSimulatedRiskScore(simRisk.getRiskScore());
        dto.setSimulatedRiskScore100(simRisk.getRiskScore100());
        dto.setSimulatedRiskTier(simRisk.getRiskTier());
        dto.setSimulatedZoneLevel(simRedZone != null ? simRedZone.getZoneLevel() : null);
        dto.setSimulatedRedZone(simIsRed);

        double deltaRisk = RiskCalculationEngine.round4(simRisk.getRiskScore() - baseRisk.getRiskScore());
        double deltaRisk100 = RiskCalculationEngine.round1(simRisk.getRiskScore100() - baseRisk.getRiskScore100());
        dto.setDeltaRiskScore(deltaRisk);
        dto.setDeltaRiskScore100(deltaRisk100);

        dto.setTransitionType(transition);
        dto.setTransitionDescription(buildTransitionDescription(dto, transition));

        dto.setBaselineRedZoneDto(baseRedZone);
        dto.setSimulatedRedZoneDto(simRedZone);

        return dto;
    }

    private String buildTransitionDescription(DistrictRedZoneSimulationDto dto, RedZoneTransitionType transition) {
        String baseDesc = String.format("Baseline: %.1f/100 (%s, %s)",
                dto.getBaselineRiskScore100(), dto.getBaselineRiskTier(), dto.isBaselineRedZone() ? "RED ZONE" : "Non-Red Zone");
        String simDesc = String.format("Simulated: %.1f/100 (%s, %s)",
                dto.getSimulatedRiskScore100(), dto.getSimulatedRiskTier(), dto.isSimulatedRedZone() ? "RED ZONE" : "Non-Red Zone");

        return switch (transition) {
            case ENTERED_RED_ZONE -> String.format("NEWLY ENTERED RED ZONE — %s -> %s (Delta: %s%.1f pts)",
                    baseDesc, simDesc, dto.getDeltaRiskScore100() >= 0 ? "+" : "", dto.getDeltaRiskScore100());
            case LEFT_RED_ZONE -> String.format("EXITED RED ZONE — %s -> %s (Delta: %s%.1f pts)",
                    baseDesc, simDesc, dto.getDeltaRiskScore100() >= 0 ? "+" : "", dto.getDeltaRiskScore100());
            case RETAINED_RED_ZONE -> String.format("RETAINED RED ZONE — %s -> %s (Delta: %s%.1f pts)",
                    baseDesc, simDesc, dto.getDeltaRiskScore100() >= 0 ? "+" : "", dto.getDeltaRiskScore100());
            case UNCHANGED_NON_RED_ZONE -> String.format("UNCHANGED NON-RED ZONE — %s -> %s (Delta: %s%.1f pts)",
                    baseDesc, simDesc, dto.getDeltaRiskScore100() >= 0 ? "+" : "", dto.getDeltaRiskScore100());
        };
    }
}
