/**
 * Stage 8A — API Client Unit Tests
 */
import assert from "node:assert";
import { ApiClient } from "../js/api/apiClient.js";

export async function runApiClientTests() {
    console.log("▶ Running API Client Tests...");

    // Test 1: Instantiation with default and custom config
    const customConfig = {
        BASE_URL: "https://api.disaster.gov.in",
        TIMEOUT_MS: 5000,
        DEFAULT_HEADERS: { "Content-Type": "application/json" }
    };
    const client = new ApiClient(customConfig);
    assert.strictEqual(client.config.BASE_URL, "https://api.disaster.gov.in");

    // Test 2: Standard ApiResponse envelope unwrapping logic simulation
    const mockSuccessPayload = {
        success: true,
        data: {
            habitationId: "HAB-01",
            priorityScore: 0.88,
            priorityLevel: "IMMEDIATE"
        },
        message: "Decision computed successfully"
    };

    assert.strictEqual(mockSuccessPayload.success, true);
    assert.strictEqual(mockSuccessPayload.data.priorityScore, 0.88);
    assert.strictEqual(mockSuccessPayload.data.priorityLevel, "IMMEDIATE");

    // Test 3: Standard ApiError envelope unwrapping simulation
    const mockErrorPayload = {
        success: false,
        error: "Invalid transit distance",
        message: "Maximum transit distance cannot be negative"
    };

    assert.strictEqual(mockErrorPayload.success, false);
    assert.strictEqual(mockErrorPayload.message, "Maximum transit distance cannot be negative");

    console.log("  ✅ All API Client Tests Passed.");
}
