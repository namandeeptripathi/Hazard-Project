package com.hazard.service.scenario;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.risk.config.RiskConfigurationProfile;
import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.risk.ExposureSubBreakdownDto;
import com.hazard.dto.scenario.ScenarioExecutionRequestDto;
import com.hazard.dto.scenario.ScenarioSimulationContextDto;
import com.hazard.dto.scenario.ScenarioSimulationResultDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.repository.scenario.ScenarioRepository;
import com.hazard.service.risk.RiskCalculationEngine;
import com.hazard.service.risk.RiskCalculationService;
import com.hazard.service.risk.config.RiskConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Stage 9B — Scenario Execution Service.
 *
 * Executes a defined disaster scenario against the existing risk engine in-memory,
 * producing temporary simulated risk outcomes without mutating baseline data,
 * database records, or stored project configurations.
 */
@Service
@Transactional(readOnly = true)
public class ScenarioExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioExecutionService.class);

    private final ScenarioRepository scenarioRepository;
    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final RiskCalculationService riskCalculationService;
    private final RiskCalculationEngine riskCalculationEngine;
    private final RiskConfigurationService riskConfigurationService;

    public ScenarioExecutionService(ScenarioRepository scenarioRepository,
                                   DistrictBoundaryRepository districtBoundaryRepository,
                                   RiskCalculationService riskCalculationService,
                                   RiskCalculationEngine riskCalculationEngine,
                                   RiskConfigurationService riskConfigurationService) {
        this.scenarioRepository = scenarioRepository;
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.riskCalculationService = riskCalculationService;
        this.riskCalculationEngine = riskCalculationEngine;
        this.riskConfigurationService = riskConfigurationService;
    }

    /**
     * Executes a scenario definition against the risk engine for a target district.
     *
     * @param scenarioId Identifier of the scenario to execute
     * @param request Execution request specifying the target district
     * @return Simulation result containing temporary risk calculation and baseline reference
     */
    public ScenarioSimulationResultDto executeScenario(String scenarioId, ScenarioExecutionRequestDto request) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario ID cannot be null or empty");
        }

        // 1. Load and validate scenario definition
        ScenarioDefinition scenario = scenarioRepository.findById(scenarioId.trim())
                .orElseThrow(() -> new HazardNotFoundException("Scenario definition not found with ID: " + scenarioId));

        // 2. Resolve target administrative district
        String targetDistrict = (request != null && request.getDistrictName() != null && !request.getDistrictName().trim().isEmpty())
                ? request.getDistrictName().trim()
                : "Sitamarhi";

        DistrictBoundary boundary = districtBoundaryRepository.findByName2IgnoreCase(targetDistrict)
                .orElseThrow(() -> new HazardNotFoundException("Administrative district not found: " + targetDistrict));

        // 3. Obtain unperturbed baseline risk data (Pure Read-Only)
        DistrictRiskScoreDto baselineRisk = riskCalculationService.getDistrictRiskScore(boundary.getName2(), null);

        // 4. Build temporary simulation context and apply scenario parameter deltas
        ScenarioSimulationContextDto simContext = buildSimulationContext(scenario, boundary.getName2(), baselineRisk);

        // 5. Recompute simulated risk using the EXISTING RiskCalculationEngine
        RiskConfigurationProfile activeProfile = riskConfigurationService.getActiveConfiguration();

        ExposureSubBreakdownDto baseExposureBreakdown = baselineRisk.getExposureSubBreakdown();
        Double settleScore = baseExposureBreakdown != null ? baseExposureBreakdown.getSettlementExposureScore() : 0.40;
        Integer settleCount = baseExposureBreakdown != null ? baseExposureBreakdown.getSettlementsExposedCount() : 0;
        Double infraScore = baseExposureBreakdown != null ? baseExposureBreakdown.getInfrastructureExposureScore() : 0.45;
        Integer infraCount = baseExposureBreakdown != null ? baseExposureBreakdown.getInfrastructureAssetsExposedCount() : 0;

        ExposureSubBreakdownDto simExposureBreakdown = riskCalculationEngine.calculateCombinedExposure(
                simContext.getSimulatedPopulationExposureScore(),
                settleScore,
                infraScore,
                simContext.getSimulatedExposedPopulation(),
                simContext.getSimulatedExposedPopulation() != null && baseExposureBreakdown != null && baseExposureBreakdown.getExposedPopulationPercentage() != null
                        ? Math.min(100.0, Math.max(0.0, RiskCalculationEngine.round1(baseExposureBreakdown.getExposedPopulationPercentage() * simContext.getEffectivePopulationMultiplier())))
                        : 0.0,
                settleCount,
                infraCount,
                activeProfile.toExposureSubWeightMap()
        );

        simContext.setSimulatedCombinedExposureScore(simExposureBreakdown.getCombinedExposureScore());

        Map<String, String> simSourceDescs = new HashMap<>();
        simSourceDescs.put("HAZARD", String.format("Simulated Hazard (%s, Multiplier: %.2fx)",
                scenario.getScenarioName(), simContext.getEffectiveHazardMultiplier()));

        DistrictRiskScoreDto simulatedRisk = riskCalculationEngine.calculateDistrictRisk(
                boundary.getName2(),
                simContext.getSimulatedHazardScore(),
                simExposureBreakdown.getCombinedExposureScore(),
                simContext.getSimulatedVulnerabilityScore(),
                simContext.getSimulatedHistoricalScore(),
                simExposureBreakdown,
                activeProfile.toTopLevelWeightMap(),
                simSourceDescs
        );

        // Enrich simulated risk metadata
        simulatedRisk.setDistrictId(boundary.getId());
        simulatedRisk.setGid2(boundary.getGid2());
        simulatedRisk.setState(boundary.getName1() != null ? boundary.getName1() : "Bihar");
        simulatedRisk.setConfigurationId(activeProfile.getConfigId());
        simulatedRisk.setConfigurationVersion(activeProfile.getVersion());
        simulatedRisk.setConfigurationName(scenario.getScenarioName());

        // 6. Compute deltas
        double deltaRisk = RiskCalculationEngine.round4(simulatedRisk.getRiskScore() - baselineRisk.getRiskScore());
        double deltaRisk100 = RiskCalculationEngine.round1(simulatedRisk.getRiskScore100() - baselineRisk.getRiskScore100());
        String direction = (Math.abs(deltaRisk) < 0.0001) ? "UNCHANGED" : (deltaRisk > 0 ? "INCREASED" : "DECREASED");

        // 7. Assemble final simulation result DTO
        ScenarioSimulationResultDto result = new ScenarioSimulationResultDto();
        result.setScenarioId(scenario.getScenarioId());
        result.setScenarioName(scenario.getScenarioName());
        result.setScenarioType(scenario.getScenarioType());
        result.setScenarioTypeDisplayName(scenario.getScenarioType() != null ? scenario.getScenarioType().getDisplayName() : null);
        result.setDistrictName(boundary.getName2());
        result.setDescription(scenario.getDescription());
        result.setAppliedRainfallChange(scenario.getRainfallChange());
        result.setAppliedHazardIntensityChange(scenario.getHazardIntensityChange());
        result.setAppliedPopulationExposureChange(scenario.getPopulationExposureChange());
        result.setSimulationContext(simContext);
        result.setSimulatedRisk(simulatedRisk);
        result.setBaselineRisk(baselineRisk);
        result.setDeltaRiskScore(deltaRisk);
        result.setDeltaRiskScore100(deltaRisk100);
        result.setRiskDirection(direction);
        result.setSimulatedAt(LocalDateTime.now());

        String summary = String.format(
                "Scenario '%s' [%s] executed for district %s: Simulated risk score is %.4f (%.1f/100, %s), baseline was %.4f (%.1f/100, %s). Delta: %s%.1f pts (%s). Production database remains unchanged.",
                scenario.getScenarioName(), scenario.getScenarioId(), boundary.getName2(),
                simulatedRisk.getRiskScore(), simulatedRisk.getRiskScore100(), simulatedRisk.getRiskTier().name(),
                baselineRisk.getRiskScore(), baselineRisk.getRiskScore100(), baselineRisk.getRiskTier().name(),
                deltaRisk100 >= 0 ? "+" : "", deltaRisk100, direction
        );
        result.setSummary(summary);

        log.info("Stage 9B: Executed scenario '{}' [{}] for district {} — SimRisk: {}/100 (Base: {}/100, Delta: {} pts)",
                scenario.getScenarioName(), scenario.getScenarioId(), boundary.getName2(),
                simulatedRisk.getRiskScore100(), baselineRisk.getRiskScore100(), deltaRisk100);

        return result;
    }

    /**
     * Executes a scenario simulation across all 38 districts of Bihar.
     */
    public List<ScenarioSimulationResultDto> executeScenarioAllDistricts(String scenarioId) {
        List<DistrictBoundary> districts = districtBoundaryRepository.findAllByOrderByName2Asc();
        List<ScenarioSimulationResultDto> results = new ArrayList<>(districts.size());

        for (DistrictBoundary district : districts) {
            try {
                ScenarioExecutionRequestDto req = new ScenarioExecutionRequestDto(district.getName2());
                results.add(executeScenario(scenarioId, req));
            } catch (Exception e) {
                log.warn("Failed scenario execution for district {}: {}", district.getName2(), e.getMessage());
            }
        }
        return results;
    }

    // =========================================================================
    // HELPER: BUILD TEMPORARY SIMULATION CONTEXT
    // =========================================================================

    private ScenarioSimulationContextDto buildSimulationContext(ScenarioDefinition scenario,
                                                               String districtName,
                                                               DistrictRiskScoreDto baselineRisk) {
        ScenarioSimulationContextDto ctx = new ScenarioSimulationContextDto();
        ctx.setDistrictName(districtName);

        double deltaR = scenario.getRainfallChange();
        double deltaH = scenario.getHazardIntensityChange();
        double deltaP = scenario.getPopulationExposureChange();

        ctx.setAppliedRainfallChange(deltaR);
        ctx.setAppliedHazardIntensityChange(deltaH);
        ctx.setAppliedPopulationExposureChange(deltaP);

        // Effective Multipliers
        double rainMultiplier = 1.0 + (deltaR / 100.0);
        double hazardMultiplier = 1.0 + (deltaH / 100.0);
        double popMultiplier = 1.0 + (deltaP / 100.0);

        // Compound hazard multiplier: Rainfall increase and Hazard intensity scaling compound
        double effectiveHazardMultiplier = RiskCalculationEngine.round4(rainMultiplier * hazardMultiplier);
        double effectivePopMultiplier = RiskCalculationEngine.round4(popMultiplier);

        ctx.setEffectiveHazardMultiplier(effectiveHazardMultiplier);
        ctx.setEffectivePopulationMultiplier(effectivePopMultiplier);

        // 1. Extract Baseline Hazard Pillar
        Double baseHazardScore = (baselineRisk.getComponents() != null && baselineRisk.getComponents().containsKey("HAZARD"))
                ? baselineRisk.getComponents().get("HAZARD").getScore()
                : 0.60;
        ctx.setBaselineHazardScore(baseHazardScore);

        // Apply hazard scaling (clamped to [0.0, 1.0])
        double simHazardScore = Math.min(1.0, Math.max(0.0, RiskCalculationEngine.round4(baseHazardScore * effectiveHazardMultiplier)));
        ctx.setSimulatedHazardScore(simHazardScore);

        // 2. Extract Baseline Exposure Pillar
        ExposureSubBreakdownDto expBreakdown = baselineRisk.getExposureSubBreakdown();
        Long baseExposedPop = expBreakdown != null ? expBreakdown.getExposedPopulation() : 0L;
        Double basePopScore = expBreakdown != null ? expBreakdown.getPopulationExposureScore() : 0.0;
        Double baseCombScore = expBreakdown != null ? expBreakdown.getCombinedExposureScore() : 0.45;

        ctx.setBaselineExposedPopulation(baseExposedPop);
        ctx.setBaselinePopulationExposureScore(basePopScore);
        ctx.setBaselineCombinedExposureScore(baseCombScore);

        // Apply population scaling (clamped to [0.0, 1.0])
        long simExposedPop = Math.max(0L, Math.round(baseExposedPop * effectivePopMultiplier));
        double simPopScore = Math.min(1.0, Math.max(0.0, RiskCalculationEngine.round4(basePopScore * effectivePopMultiplier)));

        ctx.setSimulatedExposedPopulation(simExposedPop);
        ctx.setSimulatedPopulationExposureScore(simPopScore);

        // 3. Vulnerability & Historical Pillars (Unperturbed)
        Double baseVulnScore = (baselineRisk.getComponents() != null && baselineRisk.getComponents().containsKey("VULNERABILITY"))
                ? baselineRisk.getComponents().get("VULNERABILITY").getScore()
                : 0.50;
        Double baseHistScore = (baselineRisk.getComponents() != null && baselineRisk.getComponents().containsKey("HISTORICAL"))
                ? baselineRisk.getComponents().get("HISTORICAL").getScore()
                : 0.30;

        ctx.setBaselineVulnerabilityScore(baseVulnScore);
        ctx.setSimulatedVulnerabilityScore(baseVulnScore);

        ctx.setBaselineHistoricalScore(baseHistScore);
        ctx.setSimulatedHistoricalScore(baseHistScore);

        return ctx;
    }
}
