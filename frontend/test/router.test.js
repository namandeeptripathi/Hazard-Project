/**
 * Stage 8A — Router Unit Tests
 */
import assert from "node:assert";
import { Router } from "../js/router/router.js";

export async function runRouterTests() {
    console.log("▶ Running Router Tests...");

    class MockOverviewView { constructor(ctx) { this.ctx = ctx; } render() { return "<div>Overview</div>"; } }
    class MockMapView { constructor(ctx) { this.ctx = ctx; } render() { return "<div>Map</div>"; } }
    class MockSettlementView { constructor(ctx) { this.ctx = ctx; } render() { return "<div>Settlement</div>"; } }

    const router = new Router();
    router
        .addRoute("/", MockOverviewView)
        .addRoute("/overview", MockOverviewView)
        .addRoute("/map", MockMapView)
        .addRoute("/settlements/:id", MockSettlementView);

    // Test 1: Exact route matching
    const matchOverview = router.matchRoute("/overview");
    assert.ok(matchOverview, "Should match /overview route");
    assert.strictEqual(matchOverview.viewClass, MockOverviewView);
    assert.deepStrictEqual(matchOverview.params, {});

    // Test 2: Exact route matching for /map
    const matchMap = router.matchRoute("/map");
    assert.ok(matchMap, "Should match /map route");
    assert.strictEqual(matchMap.viewClass, MockMapView);

    // Test 3: Parameterized route matching (/settlements/:id)
    const matchSettlement = router.matchRoute("/settlements/HAB-SIT-042");
    assert.ok(matchSettlement, "Should match parameterized /settlements/:id route");
    assert.strictEqual(matchSettlement.viewClass, MockSettlementView);
    assert.strictEqual(matchSettlement.params.id, "HAB-SIT-042");

    // Test 4: Unmatched route returns null
    const matchUnknown = router.matchRoute("/unknown/route/path");
    assert.strictEqual(matchUnknown, null, "Unmatched route should return null");

    console.log("  ✅ All Router Tests Passed.");
}
