/**
 * Stage 8B — Settlement Exposure API Service
 *
 * Connects directly to backend Stage 4.2 endpoints:
 * - GET /api/v1/exposure/settlements/district/{district}
 * - GET /api/v1/exposure/settlements/geojson
 * - GET /api/v1/exposure/settlements/all
 */
import { apiClient } from "../apiClient.js";
import { API_CONFIG } from "../config.js";

export const settlementService = {
    /**
     * Retrieves exposure summary for a given administrative district.
     *
     * @param {string} district - e.g. "Sitamarhi", "Patna"
     * @returns {Promise<{ success: boolean, data?: object, error?: string }>}
     */
    async getDistrictSettlementExposure(district = "Sitamarhi") {
        return apiClient.get(API_CONFIG.ENDPOINTS.EXPOSURE.SETTLEMENTS_DISTRICT(district));
    },

    /**
     * Retrieves GeoJSON FeatureCollection of exposed settlements for GIS mapping.
     *
     * @param {string} district - Optional district filter
     * @param {string} hazardId - Optional hazard observation ID
     * @returns {Promise<{ success: boolean, data?: object, error?: string }>}
     */
    async getSettlementsGeoJson(district = "Sitamarhi", hazardId = null) {
        const params = new URLSearchParams();
        if (district) params.append("district", district);
        if (hazardId) params.append("hazardId", hazardId);

        const url = params.toString()
            ? `${API_CONFIG.ENDPOINTS.EXPOSURE.SETTLEMENTS_GEOJSON}?${params.toString()}`
            : API_CONFIG.ENDPOINTS.EXPOSURE.SETTLEMENTS_GEOJSON;

        return apiClient.get(url);
    },

    /**
     * Retrieves summary for all 38 districts of Bihar.
     */
    async getAllDistrictsExposure() {
        return apiClient.get(`${API_CONFIG.ENDPOINTS.EXPOSURE.SETTLEMENTS}/all`);
    }
};
