/**
 * Stage 8A — API Configuration & Endpoint Registry
 */
export const API_CONFIG = {
    BASE_URL: typeof window !== "undefined" && window.__API_BASE_URL__ ? window.__API_BASE_URL__ : "http://localhost:8080",
    TIMEOUT_MS: 15000,
    DEFAULT_HEADERS: {
        "Content-Type": "application/json",
        "Accept": "application/json"
    },
    ENDPOINTS: {
        // Stage 6 & 7: Relocation & Decision Intelligence
        RELOCATION: {
            DECISION: "/api/v1/relocation/decision",
            EXPLAIN: "/api/v1/relocation/explain",
            RECOMMENDATION: "/api/v1/relocation/recommendation",
            PRIORITY: "/api/v1/relocation/priority",
            PLAN: "/api/v1/relocation/plan"
        },
        // Safe Site Intelligence
        SAFE_SITES: {
            BASE: "/api/v1/safe-sites",
            GEOJSON: "/api/v1/safe-sites/geojson",
            CANDIDATES: "/api/v1/safe-sites/candidates",
            DISTRICT: (district) => `/api/v1/safe-sites?district=${encodeURIComponent(district)}`,
            SEARCH: "/api/v1/safe-sites/search"
        },
        // Risk & Hazard Intelligence
        RISK: {
            BASE: "/api/v1/risk",
            CONFIG: "/api/v1/risk/config",
            EXPLANATION: "/api/v1/risk/explanation",
            CONTRIBUTORS: "/api/v1/risk/contributors"
        },
        HAZARDS: {
            BASE: "/api/v1/hazards",
            LAYERS: "/api/v1/hazards/layers",
            LAYER_GEOJSON: (layerId) => `/api/v1/hazards/layers/${encodeURIComponent(layerId)}`,
            SCORES: "/api/v1/hazards/scores",
            MULTI_HAZARD: "/api/v1/hazards/multi-hazard"
        },
        EXPOSURE: {
            POPULATION: "/api/v1/exposure/population",
            SETTLEMENTS: "/api/v1/exposure/settlements",
            SETTLEMENTS_GEOJSON: "/api/v1/exposure/settlements/geojson",
            SETTLEMENTS_DISTRICT: (district) => `/api/v1/exposure/settlements/district/${encodeURIComponent(district)}`,
            INFRASTRUCTURE: "/api/v1/exposure/infrastructure"
        },
        VULNERABILITY: {
            BASE: "/api/v1/vulnerability",
            SCORE: (district) => `/api/v1/vulnerability/score/district/${encodeURIComponent(district)}`,
            INDICATORS: (district) => `/api/v1/vulnerability/indicators/district/${encodeURIComponent(district)}`
        },
        // Stage 9: Scenario & Decision Simulation
        SCENARIOS: {
            BASE: "/api/v1/scenarios",
            BY_ID: (scenarioId) => `/api/v1/scenarios/${encodeURIComponent(scenarioId)}`,
            COMPARE: (scenarioId) => `/api/v1/scenarios/${encodeURIComponent(scenarioId)}/compare`,
            COMPARE_ALL: (scenarioId) => `/api/v1/scenarios/${encodeURIComponent(scenarioId)}/compare/all`
        }
    }
};
