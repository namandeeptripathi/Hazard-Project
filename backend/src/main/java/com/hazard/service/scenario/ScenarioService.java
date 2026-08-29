package com.hazard.service.scenario;

import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.domain.scenario.ScenarioType;
import com.hazard.dto.scenario.ScenarioCreateRequestDto;
import com.hazard.dto.scenario.ScenarioDto;
import com.hazard.dto.scenario.ScenarioTypeInfoDto;
import com.hazard.exception.HazardNotFoundException;
import com.hazard.exception.InvalidHazardParameterException;
import com.hazard.repository.scenario.ScenarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Stage 9A — Scenario Service for Creating and Validating What-If Disaster Simulation Definitions.
 *
 * IMPORTANT:
 * This service strictly defines and validates scenario parameter inputs.
 * It does NOT execute risk recalculations, dynamic red-zone recalculations,
 * priority scoring recalculations, or relocation recalculations (deferred to Stage 9B+).
 */
@Service
public class ScenarioService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioService.class);

    private final ScenarioRepository scenarioRepository;
    private final AtomicLong scenarioIdSequence = new AtomicLong(100);

    public ScenarioService(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    /**
     * Retrieves the unperturbed reference baseline scenario.
     * Guaranteed to have 0% rainfall change, 0% hazard intensity change, and 0% population exposure shift.
     */
    public ScenarioDto getBaselineScenario() {
        ScenarioDefinition baseline = scenarioRepository.findBaseline();
        return ScenarioDto.fromDomain(baseline);
    }

    /**
     * Retrieves all supported scenario simulation types with descriptive metadata.
     */
    public List<ScenarioTypeInfoDto> getScenarioTypes() {
        return Arrays.stream(ScenarioType.values())
                .map(ScenarioTypeInfoDto::fromScenarioType)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all registered scenarios, optionally filtered by scenario type.
     */
    public List<ScenarioDto> getAllScenarios(String typeFilter) {
        if (typeFilter != null && !typeFilter.trim().isEmpty()) {
            ScenarioType type = ScenarioType.fromString(typeFilter);
            return scenarioRepository.findByType(type).stream()
                    .map(ScenarioDto::fromDomain)
                    .collect(Collectors.toList());
        }

        return scenarioRepository.findAll().stream()
                .sorted(Comparator.comparing(ScenarioDefinition::getCreatedAt))
                .map(ScenarioDto::fromDomain)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single scenario definition by its unique identifier.
     */
    public ScenarioDto getScenarioById(String scenarioId) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario ID cannot be null or empty");
        }

        ScenarioDefinition scenario = scenarioRepository.findById(scenarioId.trim())
                .orElseThrow(() -> new HazardNotFoundException("Scenario not found with ID: " + scenarioId));

        return ScenarioDto.fromDomain(scenario);
    }

    /**
     * Creates and validates a new scenario definition.
     *
     * @param request Creation request DTO containing parameters
     * @return Created scenario DTO
     */
    public ScenarioDto createScenario(ScenarioCreateRequestDto request) {
        validateScenarioRequest(request);

        ScenarioType type = ScenarioType.fromString(request.getScenarioType());
        String name = request.getScenarioName().trim();
        String description = request.getDescription() != null ? request.getDescription().trim() : "";

        double rainfallChange = sanitizeChangeValue(request.getRainfallChange());
        double hazardIntensityChange = sanitizeChangeValue(request.getHazardIntensityChange());
        double populationExposureChange = sanitizeChangeValue(request.getPopulationExposureChange());

        // Baseline scenario enforcement
        if (type == ScenarioType.BASELINE) {
            if (rainfallChange != 0.0 || hazardIntensityChange != 0.0 || populationExposureChange != 0.0) {
                throw new InvalidHazardParameterException(
                        "Baseline scenario must have 0.0% change across all dimensions (rainfall, hazard intensity, population exposure)."
                );
            }
            return getBaselineScenario();
        }

        String scenarioId = generateScenarioId(type);

        ScenarioDefinition scenario = new ScenarioDefinition(
                scenarioId,
                name,
                type,
                description,
                rainfallChange,
                hazardIntensityChange,
                populationExposureChange,
                false
        );

        scenarioRepository.save(scenario);

        log.info("Stage 9A: Created scenario definition '{}' [ID: {}, Type: {}, Rain: {}%, Hazard: {}%, Pop: {}%]",
                name, scenarioId, type, rainfallChange, hazardIntensityChange, populationExposureChange);

        return ScenarioDto.fromDomain(scenario);
    }

    /**
     * Deletes a user-created scenario definition by ID.
     * The baseline scenario cannot be deleted.
     */
    public boolean deleteScenario(String scenarioId) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario ID cannot be null or empty");
        }

        if (ScenarioDefinition.BASELINE_SCENARIO_ID.equalsIgnoreCase(scenarioId.trim())) {
            throw new InvalidHazardParameterException("The baseline reference scenario (SCEN-BASELINE) is permanent and cannot be deleted.");
        }

        boolean deleted = scenarioRepository.deleteById(scenarioId.trim());
        if (!deleted) {
            throw new HazardNotFoundException("Scenario not found for deletion with ID: " + scenarioId);
        }

        log.info("Stage 9A: Deleted scenario definition with ID: {}", scenarioId);
        return true;
    }

    // =========================================================================
    // VALIDATION HELPERS
    // =========================================================================

    private void validateScenarioRequest(ScenarioCreateRequestDto request) {
        if (request == null) {
            throw new InvalidHazardParameterException("Scenario creation request cannot be null");
        }

        if (request.getScenarioName() == null || request.getScenarioName().trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario name is required and cannot be blank");
        }

        if (request.getScenarioType() == null || request.getScenarioType().trim().isEmpty()) {
            throw new InvalidHazardParameterException("Scenario type is required");
        }

        // Validate numeric integrity
        validateNumericDelta("Rainfall change", request.getRainfallChange(), true);
        validateNumericDelta("Hazard intensity change", request.getHazardIntensityChange(), true);
        validateNumericDelta("Population exposure change", request.getPopulationExposureChange(), true);
    }

    private void validateNumericDelta(String fieldName, Double value, boolean checkLowerBound) {
        if (value == null) {
            return; // Allowed, will default to 0.0
        }

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new InvalidHazardParameterException(fieldName + " must be a valid finite number.");
        }

        if (checkLowerBound && value < -100.0) {
            throw new InvalidHazardParameterException(
                    String.format("%s cannot be less than -100.0%% (provided: %.2f%%)", fieldName, value)
            );
        }

        if (value > 1000.0) {
            throw new InvalidHazardParameterException(
                    String.format("%s exceeds allowable simulation upper bound of +1000.0%% (provided: %.2f%%)", fieldName, value)
            );
        }
    }

    private double sanitizeChangeValue(Double val) {
        if (val == null) return 0.0;
        return Math.round(val * 100.0) / 100.0; // Round to 2 decimal places
    }

    private String generateScenarioId(ScenarioType type) {
        String prefix = switch (type) {
            case BASELINE -> "SCEN-BASE";
            case RAINFALL_CHANGE -> "SCEN-RAIN";
            case HAZARD_INTENSITY -> "SCEN-HAZ";
            case POPULATION_EXPOSURE -> "SCEN-POP";
            case MULTI_FACTOR -> "SCEN-MULTI";
        };
        return prefix + "-" + scenarioIdSequence.incrementAndGet();
    }
}
