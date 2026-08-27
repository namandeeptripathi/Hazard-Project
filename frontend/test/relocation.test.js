/**
 * Stage 8D — Relocation Planner & Capacity Intelligence (RelocationView) Unit Tests
 */
import assert from "node:assert";
import { RelocationView } from "../js/views/RelocationView.js";
import { MOCK_RELOCATION_CASES, MOCK_SAFE_SITES } from "../js/api/fixtures/mockData.js";

export async function runRelocationTests() {
    console.log("▶ Running Relocation Planner & Capacity (RelocationView) Tests...");

    const view = new RelocationView();
    view.state.cases = MOCK_RELOCATION_CASES;
    view.state.safeSites = MOCK_SAFE_SITES;
    view.state.activeCase = MOCK_RELOCATION_CASES[0];

    // Test 1: Render returns skeleton structure
    const initialHtml = await view.render();
    assert.ok(initialHtml.includes("relocation-root"), "RelocationView should render root container");

    // Test 2: Priority Queue items
    const sonbarsaQueueItem = view.renderQueueItem(MOCK_RELOCATION_CASES[0]);
    assert.ok(sonbarsaQueueItem.includes("Sonbarsa Flood Inundation Area"), "Queue item should render Sonbarsa");
    assert.ok(sonbarsaQueueItem.includes("5,000 Evacuees"), "Queue item should display 5,000 evacuees");
    assert.ok(sonbarsaQueueItem.includes("Capacity Gap: 850"), "Queue item should flag 850 capacity gap");

    // Test 3: Relocation Detail Panel with Capacity Gap & Multi-Destination
    const detailPanelGap = view.renderDetailPanel(MOCK_RELOCATION_CASES[0]);
    assert.ok(detailPanelGap.includes("CAPACITY SHORTFALL DETECTED"), "Detail panel should display capacity shortfall warning");
    assert.ok(detailPanelGap.includes("4,150 beds"), "Detail panel should show primary allocation of 4,150 beds");
    assert.ok(detailPanelGap.includes("850 beds"), "Detail panel should show overflow allocation of 850 beds");
    assert.ok(detailPanelGap.includes("Dumra High School Community Center"), "Detail panel should include secondary destination");
    assert.ok(detailPanelGap.includes("Gate 1: Hazard Safety"), "Detail panel should render feasibility gates");

    // Test 4: Fully Feasible Case without Gap
    const detailPanelFeasible = view.renderDetailPanel(MOCK_RELOCATION_CASES[1]);
    assert.ok(detailPanelFeasible.includes("FULL SINGLE-SITE FEASIBILITY"), "Bairgania should show full single-site feasibility");
    assert.ok(detailPanelFeasible.includes("Pupri Cyclone & Flood Relief Centre"), "Bairgania destination should be Pupri Centre");
    assert.ok(detailPanelFeasible.includes("Single Site Sufficient"), "Bairgania should indicate single site is sufficient");

    console.log("  ✅ All Relocation Planner Tests Passed.");
}
