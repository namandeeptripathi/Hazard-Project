/**
 * Stage 8A — Relocation & Decision Intelligence API Service
 *
 * Connects directly to backend Stage 6 & Stage 7 REST endpoints:
 * - POST/GET /api/v1/relocation/decision (Consolidated Decision)
 * - POST/GET /api/v1/relocation/explain (Explainability)
 * - POST/GET /api/v1/relocation/recommendation (Stage 7B Recommendations)
 * - POST/GET /api/v1/relocation/priority (Stage 7A Priority Assessment)
 * - POST/GET /api/v1/relocation/plan (Stage 6 Relocation Plan)
 */
import { apiClient } from "../apiClient.js";
import { API_CONFIG } from "../config.js";

export const relocationService = {
    /**
     * Fetches consolidated decisions for given requests or district queries.
     *
     * @param {Array<object>} requests - Array of RelocationRequestDto
     * @returns {Promise<{ success: boolean, data?: object, error?: string }>}
     */
    async getDecisions(requests) {
        return apiClient.post(API_CONFIG.ENDPOINTS.RELOCATION.DECISION, requests);
    },

    /**
     * Queries consolidated decisions by district with query parameters.
     *
     * @param {string} districts - Comma-separated district names (e.g. "Sitamarhi,Patna")
     * @param {number} maxDistanceKm - Maximum transit distance radius
     * @param {string} minSuitability - Minimum suitability class ("HIGHLY_SUITABLE", "SUITABLE", "MARGINAL")
     * @param {number} population - Population count per district
     */
    async queryDecisions(districts = "Sitamarhi", maxDistanceKm = 25.0, minSuitability = "MARGINAL", population = 250) {
        const params = new URLSearchParams({
            districts,
            maxDistanceKm: maxDistanceKm.toString(),
            minSuitability,
            ...(population ? { population: population.toString() } : {})
        });
        return apiClient.get(`${API_CONFIG.ENDPOINTS.RELOCATION.DECISION}?${params.toString()}`);
    },

    /**
     * Fetches priority ranking for given requests.
     */
    async getPriorities(requests) {
        return apiClient.post(API_CONFIG.ENDPOINTS.RELOCATION.PRIORITY, requests);
    },

    /**
     * Fetches recommendations for given requests.
     */
    async getRecommendations(requests) {
        return apiClient.post(API_CONFIG.ENDPOINTS.RELOCATION.RECOMMENDATION, requests);
    },

    /**
     * Generates a single relocation plan.
     */
    async generatePlan(request) {
        return apiClient.post(API_CONFIG.ENDPOINTS.RELOCATION.PLAN, request);
    },

    /**
     * Queries relocation plan by district via GET /api/v1/relocation/plan.
     */
    async queryPlan(district = "Sitamarhi", maxDistanceKm = 25.0, minSuitability = "MARGINAL", population = 5000) {
        const params = new URLSearchParams({
            district,
            maxDistanceKm: maxDistanceKm.toString(),
            minSuitability,
            ...(population ? { population: population.toString() } : {})
        });
        return apiClient.get(`${API_CONFIG.ENDPOINTS.RELOCATION.PLAN}?${params.toString()}`);
    }
};
