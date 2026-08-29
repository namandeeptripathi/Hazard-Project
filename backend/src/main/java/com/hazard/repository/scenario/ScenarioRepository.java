package com.hazard.repository.scenario;

import com.hazard.domain.scenario.ScenarioDefinition;
import com.hazard.domain.scenario.ScenarioType;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stage 9A — Thread-safe in-memory repository for disaster simulation scenario definitions.
 * Pre-populated with the immutable Baseline Scenario (SCEN-BASELINE).
 */
@Repository
public class ScenarioRepository {

    private final Map<String, ScenarioDefinition> scenarios = new ConcurrentHashMap<>();

    public ScenarioRepository() {
        initializeBaseline();
    }

    private void initializeBaseline() {
        ScenarioDefinition baseline = ScenarioDefinition.createBaseline();
        scenarios.put(baseline.getScenarioId(), baseline);
    }

    /**
     * Saves or updates a scenario definition.
     * Prevents mutation of the immutable baseline definition.
     */
    public ScenarioDefinition save(ScenarioDefinition scenario) {
        if (scenario == null || scenario.getScenarioId() == null) {
            throw new IllegalArgumentException("Cannot save null scenario or scenario with null scenarioId");
        }

        // Defensive guard: if trying to overwrite the baseline ID with non-baseline properties, preserve baseline integrity
        if (ScenarioDefinition.BASELINE_SCENARIO_ID.equalsIgnoreCase(scenario.getScenarioId()) && !scenario.isBaseline()) {
            throw new IllegalArgumentException("The baseline scenario definition (SCEN-BASELINE) is immutable and cannot be overwritten with custom parameters.");
        }

        scenarios.put(scenario.getScenarioId(), scenario);
        return scenario;
    }

    /**
     * Retrieves a scenario definition by ID.
     */
    public Optional<ScenarioDefinition> findById(String scenarioId) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(scenarios.get(scenarioId.trim()));
    }

    /**
     * Retrieves the reference baseline scenario.
     */
    public ScenarioDefinition findBaseline() {
        return scenarios.computeIfAbsent(
                ScenarioDefinition.BASELINE_SCENARIO_ID,
                k -> ScenarioDefinition.createBaseline()
        );
    }

    /**
     * Retrieves all saved scenario definitions.
     */
    public List<ScenarioDefinition> findAll() {
        return new ArrayList<>(scenarios.values());
    }

    /**
     * Retrieves scenario definitions matching a specific scenario type.
     */
    public List<ScenarioDefinition> findByType(ScenarioType type) {
        if (type == null) {
            return Collections.emptyList();
        }
        List<ScenarioDefinition> result = new ArrayList<>();
        for (ScenarioDefinition scenario : scenarios.values()) {
            if (scenario.getScenarioType() == type) {
                result.add(scenario);
            }
        }
        return result;
    }

    /**
     * Deletes a scenario definition by ID.
     * Prevents deletion of the baseline scenario.
     */
    public boolean deleteById(String scenarioId) {
        if (scenarioId == null || ScenarioDefinition.BASELINE_SCENARIO_ID.equalsIgnoreCase(scenarioId.trim())) {
            return false; // Cannot delete baseline scenario
        }
        return scenarios.remove(scenarioId.trim()) != null;
    }

    /**
     * Checks if a scenario ID exists.
     */
    public boolean existsById(String scenarioId) {
        if (scenarioId == null) return false;
        return scenarios.containsKey(scenarioId.trim());
    }

    /**
     * Returns total count of registered scenarios.
     */
    public long count() {
        return scenarios.size();
    }

    /**
     * Clears all custom scenarios while preserving the baseline scenario.
     */
    public void resetToBaselineOnly() {
        scenarios.clear();
        initializeBaseline();
    }
}
