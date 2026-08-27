/**
 * Stage 8A — Risk & Hazard Intelligence API Service
 *
 * Connects directly to backend Stage 3 & Stage 4 endpoints:
 * - GET /api/v1/risk (Risk Assessment)
 * - GET /api/v1/risk/explanation (Multi-Level Risk Explanation)
 * - GET /api/v1/hazards (Integrated Hazard Catalog)
 */
import { apiClient } from "../apiClient.js";
import { API_CONFIG } from "../config.js";

export const hazardService = {
    /**
     * Fetches risk calculation summary for a district.
     */
    async getDistrictRisk(district = "Sitamarhi") {
        const params = new URLSearchParams({ district });
        return apiClient.get(`${API_CONFIG.ENDPOINTS.RISK.BASE}?${params.toString()}`);
    },

    /**
     * Fetches full transparent risk explanation for a district.
     */
    async getRiskExplanation(district = "Sitamarhi") {
        const params = new URLSearchParams({ district });
        return apiClient.get(`${API_CONFIG.ENDPOINTS.RISK.EXPLANATION}?${params.toString()}`);
    },

    /**
     * Fetches RFC 7946 GeoJSON FeatureCollection for a specific hazard GIS layer (Stage 3.6).
     *
     * @param {string} layerId - e.g. "FLOOD_HAZARD_SCORES", "FLOOD_EVENTS", "MULTI_HAZARD_INDEX", "DISTRICT_HAZARD_SUMMARIES"
     * @param {string} district - Optional district name filter
     */
    async getLayerGeoJson(layerId = "FLOOD_HAZARD_SCORES", district = null) {
        const params = new URLSearchParams();
        if (district) params.append("district", district);

        const baseEndpoint = API_CONFIG.ENDPOINTS.HAZARDS.LAYER_GEOJSON(layerId);
        const url = params.toString() ? `${baseEndpoint}?${params.toString()}` : baseEndpoint;
        return apiClient.get(url);
    },

    /**
     * Fetches metadata catalog for all available hazard GIS layers.
     */
    async getLayerCatalog() {
        return apiClient.get(API_CONFIG.ENDPOINTS.HAZARDS.LAYERS);
    }
};
