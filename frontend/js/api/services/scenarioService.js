/**
 * Stage 9 — Scenario & Decision Simulation Service
 *
 * Connects frontend UI directly to Stage 9 REST APIs:
 * - Scenario creation (Stage 9A)
 * - In-memory risk simulation (Stage 9B)
 * - Dynamic Red-Zone recalculation (Stage 9C)
 * - Priority & Relocation recalculation (Stage 9D)
 * - Before vs After comparison (Stage 9E)
 */
import { apiClient } from "../apiClient.js";
import { API_CONFIG } from "../config.js";

const getBaseUrl = () => API_CONFIG?.ENDPOINTS?.SCENARIOS?.BASE || "/api/v1/scenarios";
const getCompareUrl = (scenarioId) => API_CONFIG?.ENDPOINTS?.SCENARIOS?.COMPARE
    ? API_CONFIG.ENDPOINTS.SCENARIOS.COMPARE(scenarioId)
    : `/api/v1/scenarios/${encodeURIComponent(scenarioId)}/compare`;
const getCompareAllUrl = (scenarioId) => API_CONFIG?.ENDPOINTS?.SCENARIOS?.COMPARE_ALL
    ? API_CONFIG.ENDPOINTS.SCENARIOS.COMPARE_ALL(scenarioId)
    : `/api/v1/scenarios/${encodeURIComponent(scenarioId)}/compare/all`;

export const scenarioService = {
    /**
     * Retrieves all existing disaster scenario definitions.
     */
    async listScenarios() {
        return apiClient.get(getBaseUrl());
    },

    /**
     * Creates a new what-if disaster simulation scenario.
     */
    async createScenario(scenarioData) {
        return apiClient.post(getBaseUrl(), scenarioData);
    },

    /**
     * Executes Before vs After comparison for a single district or default district.
     */
    async compareScenarioDistrict(scenarioId, districtName = "Sitamarhi") {
        const endpoint = getCompareUrl(scenarioId);
        return apiClient.post(endpoint, { districtName });
    },

    /**
     * Executes Before vs After comparison across all 38 districts in Bihar.
     */
    async compareScenarioAllDistricts(scenarioId) {
        const endpoint = getCompareAllUrl(scenarioId);
        return apiClient.post(endpoint, {});
    },

    /**
     * High-level helper: Creates a scenario if needed, and executes Before vs After comparison.
     *
     * @param {object} params
     * @param {string} params.scenarioType - "BASELINE" | "RAINFALL_CHANGE" | "HAZARD_INTENSITY" | "POPULATION_EXPOSURE" | "MULTI_FACTOR"
     * @param {number} params.rainfallChange - percentage change (e.g. 20)
     * @param {number} params.hazardIntensityChange - percentage change (e.g. 15)
     * @param {number} params.populationExposureChange - percentage change (e.g. 10)
     * @param {string} [params.districtName] - target district (e.g. "Sitamarhi")
     * @param {boolean} [params.isAllDistricts] - whether to run across all 38 districts
     * @returns {Promise<{ success: boolean, data?: object, error?: string }>}
     */
    async runWhatIfSimulation(params = {}) {
        const {
            scenarioType = "RAINFALL_CHANGE",
            rainfallChange = 0,
            hazardIntensityChange = 0,
            populationExposureChange = 0,
            districtName = "Sitamarhi",
            isAllDistricts = false
        } = params;

        let scenarioId = "SCEN-BASELINE";

        if (scenarioType !== "BASELINE") {
            const scenarioName = `What-If Simulation (${scenarioType.replace("_", " ")})`;
            const createRes = await this.createScenario({
                scenarioName,
                scenarioType,
                description: `Live interactive what-if simulation from Decision Dashboard: Rain=${rainfallChange}%, Hazard=${hazardIntensityChange}%, Pop=${populationExposureChange}%`,
                rainfallChange: Number(rainfallChange) || 0,
                hazardIntensityChange: Number(hazardIntensityChange) || 0,
                populationExposureChange: Number(populationExposureChange) || 0
            });

            if (!createRes.success || !createRes.data) {
                return {
                    success: false,
                    error: createRes.error || "Failed to create disaster scenario on backend."
                };
            }

            scenarioId = createRes.data.scenarioId;
        }

        if (isAllDistricts) {
            return this.compareScenarioAllDistricts(scenarioId);
        } else {
            return this.compareScenarioDistrict(scenarioId, districtName || "Sitamarhi");
        }
    }
};
