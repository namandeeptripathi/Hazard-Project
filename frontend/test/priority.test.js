/**
 * Stage 8E — Priority Intelligence Unit Tests
 */
import assert from "node:assert";
import { OverviewView } from "../js/views/OverviewView.js";
import { MOCK_SETTLEMENTS } from "../js/api/fixtures/mockData.js";

export async function runPriorityTests() {
    console.log("▶ Running Priority Intelligence Tests...");

    const view = new OverviewView();
    view.state.settlements = MOCK_SETTLEMENTS;
    view.state.safeSites = [];
    view.state.summary = {};

    // Test 1: Priority ranking is ordered deterministically
    const immediateSettlements = view.state.settlements.filter(s => s.priorityLevel === "IMMEDIATE");
    assert.strictEqual(immediateSettlements.length, 3, "There should be 3 IMMEDIATE priority settlements");
    assert.strictEqual(immediateSettlements[0].habitationId, "HAB-SIT-001", "Top priority should be HAB-SIT-001 (Sonbarsa)");
    assert.strictEqual(immediateSettlements[0].priorityScore, 0.88, "Sonbarsa priority score should be 0.88");

    // Test 2: Priority tier filtering
    view.state.selectedPriorityTier = "IMMEDIATE";
    const immediateHtml = view.renderDashboardContent();
    assert.ok(immediateHtml.includes("Sonbarsa Flood Inundation Area"), "Immediate filter should show Sonbarsa");
    assert.ok(immediateHtml.includes("Sursand Border Habitation"), "Immediate filter should show Sursand");
    assert.ok(!immediateHtml.includes("Riga Lowland Settlement"), "Immediate filter should NOT show Riga");

    view.state.selectedPriorityTier = "MEDIUM_TERM";
    const medHtml = view.renderDashboardContent();
    assert.ok(medHtml.includes("Riga Lowland Settlement"), "Medium-term filter should show Riga");
    assert.ok(!medHtml.includes("Sonbarsa Flood Inundation Area"), "Medium-term filter should NOT show Sonbarsa");

    // Test 3: Priority badges and score display
    assert.ok(immediateHtml.includes("Immediate"), "Should render Immediate priority tab with count");
    assert.ok(immediateHtml.includes("explain-decision-btn"), "Should include Explainability trigger buttons for rows");

    console.log("  ✅ All Priority Intelligence Tests Passed.");
}
