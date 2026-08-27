/**
 * Stage 8A — Standardized API Client
 *
 * Provides resilient HTTP communication with the backend:
 * - Base URL resolution
 * - AbortController timeout enforcement
 * - Unified ApiResponse unwrapping
 * - Clean error normalization
 * - Configurable retry for transient failures
 */
import { API_CONFIG } from "./config.js";

export class ApiClient {
    constructor(config = API_CONFIG) {
        this.config = config;
    }

    /**
     * Executes an HTTP request with timeout, JSON parsing, and retry handling.
     *
     * @param {string} endpoint - Relative API endpoint or full URL
     * @param {object} options - Fetch options (method, headers, body, etc.)
     * @param {number} retries - Number of retry attempts for network errors
     * @returns {Promise<{ success: boolean, data?: any, error?: string, status?: number }>}
     */
    async request(endpoint, options = {}, retries = 1) {
        const url = endpoint.startsWith("http")
            ? endpoint
            : `${this.config.BASE_URL}${endpoint}`;

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), this.config.TIMEOUT_MS);

        const mergedHeaders = {
            ...this.config.DEFAULT_HEADERS,
            ...(options.headers || {})
        };

        const fetchOptions = {
            ...options,
            headers: mergedHeaders,
            signal: controller.signal
        };

        try {
            const response = await fetch(url, fetchOptions);
            clearTimeout(timeoutId);

            let body;
            const contentType = response.headers.get("content-type") || "";
            if (contentType.includes("application/json")) {
                body = await response.json();
            } else {
                body = await response.text();
            }

            if (!response.ok) {
                const errorMessage = (typeof body === "object" && body !== null && (body.message || body.error))
                    ? (body.message || body.error)
                    : `HTTP ${response.status}: ${response.statusText}`;

                return {
                    success: false,
                    error: errorMessage,
                    status: response.status,
                    data: null
                };
            }

            // Standardize Spring Boot ApiResponse envelope unwrap: { success: true, data: ..., message: ... }
            if (typeof body === "object" && body !== null && "data" in body && "success" in body) {
                return {
                    success: body.success,
                    data: body.data,
                    message: body.message,
                    status: response.status
                };
            }

            return {
                success: true,
                data: body,
                status: response.status
            };

        } catch (err) {
            clearTimeout(timeoutId);

            if (err.name === "AbortError") {
                return {
                    success: false,
                    error: `Request timeout exceeded (${this.config.TIMEOUT_MS}ms)`,
                    status: 408
                };
            }

            // Retry on transient network errors if remaining attempts
            if (retries > 0 && options.method !== "POST" && options.method !== "DELETE") {
                console.warn(`[ApiClient] Retrying request to ${endpoint} (${retries} attempts remaining)...`);
                return this.request(endpoint, options, retries - 1);
            }

            return {
                success: false,
                error: err.message || "Network connection failure",
                status: 0
            };
        }
    }

    get(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: "GET" });
    }

    post(endpoint, body, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: "POST",
            body: typeof body === "string" ? body : JSON.stringify(body)
        });
    }

    put(endpoint, body, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: "PUT",
            body: typeof body === "string" ? body : JSON.stringify(body)
        });
    }

    delete(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: "DELETE" });
    }
}

// Global default instance
export const apiClient = new ApiClient();
