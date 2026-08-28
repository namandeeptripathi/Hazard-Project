/**
 * Stage 8E — Explainability Intelligence Unit Tests
 */
import assert from "node:assert";
import { ExplainabilityDrawer } from "../js/components/ExplainabilityDrawer.js";
import { MOCK_SETTLEMENTS } from "../js/api/fixtures/mockData.js";

export async function runExplainabilityTests() {
    console.log("▶ Running Explainability Intelligence Tests...");

    const sonbarsa = MOCK_SETTLEMENTS[0];

    // Test 1: ExplainabilityDrawer renders structured rationale (WHO -> WHERE -> WHY -> ACTION)
    const drawerHtml = ExplainabilityDrawer.render({
        explanation: {
            habitationId: sonbarsa.habitationId,
            habitationName: sonbarsa.settlementName,
            priorityLevel: sonbarsa.priorityLevel,
            priorityScore: sonbarsa.priorityScore,
            riskScore: sonbarsa.riskScore,
            decisionRationale: sonbarsa.decisionRationale,
            priorityEvidence: [
                { displayName: "Hazard Exposure", weight: 0.35, normalizedScore: 0.92, interpretation: "Severe flood depth 2.8m" },
                { displayName: "Population Exposure", weight: 0.30, normalizedScore: 0.70, interpretation: "5,000 residents in red zone" },
                { displayName: "Vulnerability Profile", weight: 0.25, normalizedScore: 0.87, interpretation: "78% kachha housing" },
                { displayName: "Decision Urgency", weight: 0.10, normalizedScore: 0.88, interpretation: "Imminent embankment breach threat" }
            ]
        }
    });

    assert.ok(drawerHtml.includes("Why This Decision?"), "Drawer should render 'Why This Decision?' title");
    assert.ok(drawerHtml.includes("Sonbarsa Flood Inundation Area"), "Drawer should contain settlement name");
    assert.ok(drawerHtml.includes("WHO:"), "Drawer should render WHO rationale");
    assert.ok(drawerHtml.includes("WHERE:"), "Drawer should render WHERE rationale");
    assert.ok(drawerHtml.includes("WHY:"), "Drawer should render WHY rationale");
    assert.ok(drawerHtml.includes("ACTION:"), "Drawer should render ACTION guidance");

    // Test 2: Factor weights and contributor bars
    assert.ok(drawerHtml.includes("Hazard Exposure"), "Drawer should list Hazard Exposure contributor");
    assert.ok(drawerHtml.includes("Weight: 35%"), "Drawer should render calibrated 35% weight");
    assert.ok(drawerHtml.includes("Weight: 30%"), "Drawer should render calibrated 30% weight");
    assert.ok(drawerHtml.includes("Weight: 25%"), "Drawer should render calibrated 25% weight");
    assert.ok(drawerHtml.includes("Weight: 10%"), "Drawer should render calibrated 10% weight");

    // Test 3: Graceful fallback for minimal / missing explanation data
    const minimalHtml = ExplainabilityDrawer.render({
        explanation: {
            habitationId: "HAB-MIN-999",
            habitationName: "Minimal Settlement",
            priorityLevel: "SHORT_TERM"
        }
    });

    assert.ok(minimalHtml.includes("Minimal Settlement"), "Minimal drawer should render settlement name");
    assert.ok(minimalHtml.includes("SHORT_TERM"), "Minimal drawer should render priority badge");
    assert.ok(minimalHtml.includes("Verified Valid"), "Minimal drawer should render valid status");

    console.log("  ✅ All Explainability Intelligence Tests Passed.");
}
