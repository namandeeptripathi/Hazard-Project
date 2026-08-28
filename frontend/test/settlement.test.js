/**
 * Stage 8C — Settlement Intelligence (SettlementDetailView) Unit Tests
 */
import assert from "node:assert";
import { SettlementDetailView } from "../js/views/SettlementDetailView.js";
import { MOCK_SETTLEMENTS, MOCK_SAFE_SITES } from "../js/api/fixtures/mockData.js";

export async function runSettlementTests() {
    console.log("▶ Running Settlement Intelligence (SettlementDetailView) Tests...");

    const view = new SettlementDetailView({ params: { id: "HAB-SIT-001" } });
    const settlement = MOCK_SETTLEMENTS[0];
    const safeSite = MOCK_SAFE_SITES[0];

    // Test 1: Instantiation and context extraction
    assert.strictEqual(view.settlementId, "HAB-SIT-001", "Should extract settlement ID from params");

    // Test 2: Initial render contains loading/container structure
    const initialHtml = await view.render();
    assert.ok(initialHtml.includes("settlement-dynamic-container"), "Initial HTML should contain dynamic container");

    // Test 3: Dashboard content rendering
    const contentHtml = view.renderSettlementContent(settlement, safeSite);

    // Header & Breadcrumbs
    assert.ok(contentHtml.includes("Sonbarsa Flood Inundation Area"), "Should render settlement title");
    assert.ok(contentHtml.includes("HAB-SIT-001"), "Should render settlement ID");
    assert.ok(contentHtml.includes("Sitamarhi"), "Should render district name");
    assert.ok(contentHtml.includes("Block: Sonbarsa"), "Should render administrative block context");

    // Risk Hero & Pillars
    assert.ok(contentHtml.includes("OVERALL DISASTER RISK"), "Should render Overall Disaster Risk section");
    assert.ok(contentHtml.includes("92"), "Should render 92 score");
    assert.ok(contentHtml.includes("CRITICAL RISK TIER"), "Should render Critical Risk Tier badge");
    assert.ok(contentHtml.includes("Hazard Exposure (35%)"), "Should render Hazard Exposure pillar");
    assert.ok(contentHtml.includes("Vulnerability Profile (25%)"), "Should render Vulnerability pillar");

    // Hazard Exposure
    assert.ok(contentHtml.includes("Monsoon Riverine Flood Inundation"), "Should render primary hazard name");
    assert.ok(contentHtml.includes("2.8 meters"), "Should render inundation depth");
    assert.ok(contentHtml.includes("High Seepage / Breach Hazard"), "Should render embankment status");

    // Vulnerability Dimensions
    assert.ok(contentHtml.includes("Housing Vulnerability"), "Should render Housing Vulnerability dimension");
    assert.ok(contentHtml.includes("78% kachha"), "Should render housing description");
    assert.ok(contentHtml.includes("Healthcare Access Proximity"), "Should render healthcare travel time");

    // Spatial Mini-Map
    assert.ok(contentHtml.includes('id="settlement-mini-map"'), "Should contain settlement mini-map element");

    // Executive Decision Rationale
    assert.ok(contentHtml.includes("Executive Decision Rationale"), "Should render Stage 7 Decision section");
    assert.ok(contentHtml.includes("WHO:"), "Should render WHO rationale");
    assert.ok(contentHtml.includes("WHERE:"), "Should render WHERE rationale");
    assert.ok(contentHtml.includes("WHY:"), "Should render WHY rationale");
    assert.ok(contentHtml.includes("ACTION GUIDANCE:"), "Should render ACTION guidance callout");

    // Safe Site Preview
    assert.ok(contentHtml.includes("Sitamarhi Central Flood Shelter"), "Should render recommended safe site name");
    assert.ok(contentHtml.includes("FAC-EMG-003"), "Should render safe site ID");
    assert.ok(contentHtml.includes("5,000 beds"), "Should render total capacity");
    assert.ok(contentHtml.includes("4,150 beds free"), "Should render available headroom");
    assert.ok(contentHtml.includes("2.50 km"), "Should render transit distance");

    console.log("  ✅ All Settlement Intelligence Tests Passed.");
}
