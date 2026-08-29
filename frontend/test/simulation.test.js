/**
 * Stage 9 — What-If Scenario Simulation Frontend Unit Tests
 */
import assert from "node:assert";
import { SimulationModal } from "../js/components/SimulationModal.js";
import { scenarioService } from "../js/api/services/scenarioService.js";

export async function runSimulationTests() {
    console.log("▶ Running What-If Simulation (Stage 9 Frontend) Tests...");

    // Test 1: SimulationModal markup contains key elements
    const modalHtml = SimulationModal.render({ district: "Sitamarhi" });
    assert.ok(modalHtml.includes("What-If Disaster Scenario Simulation"), "Modal should render header title");
    assert.ok(modalHtml.includes("simScenarioType"), "Modal should contain scenario type selector");
    assert.ok(modalHtml.includes("simScope"), "Modal should contain scope selector");
    assert.ok(modalHtml.includes("runSimulationBtn"), "Modal should contain Run Simulation button");
    assert.ok(modalHtml.includes("Zero-Mutation Guarantee"), "Modal should display zero mutation guarantee");

    // Test 2: Simulation results formatting for single district
    const sampleResult = {
        scenarioId: "SCEN-RAIN-101",
        scenarioName: "Monsoon Extreme Rainfall +20%",
        totalDistrictsEvaluated: 1,
        districtComparisons: [
            {
                districtName: "Sitamarhi",
                baselineRiskScore100: 23.6,
                simulatedRiskScore100: 26.0,
                deltaRiskScore100: 2.4,
                riskDirection: "INCREASED",
                baselineRiskTier: "LOW",
                simulatedRiskTier: "LOW",
                baselineRedZone: false,
                simulatedRedZone: false,
                redZoneTransitionType: "UNCHANGED_NON_RED_ZONE",
                baselinePriorityScore: 0.354,
                simulatedPriorityScore: 0.378,
                baselinePriorityLevel: "MONITORING",
                simulatedPriorityLevel: "MONITORING",
                priorityShiftDirection: "INCREASED",
                priorityEscalated: true,
                baselineVulnerablePopulation: 50000,
                simulatedVulnerablePopulation: 50000,
                baselineUnallocatedPopulation: 50000,
                simulatedUnallocatedPopulation: 50000,
                simulatedRelocationStatus: "UNALLOCATED_NO_SAFE_SITE"
            }
        ]
    };

    const resultsHtml = SimulationModal.renderSimulationResults(sampleResult, false);
    assert.ok(resultsHtml.includes("1. DISASTER RISK SCORE"), "Should render Risk Score section");
    assert.ok(resultsHtml.includes("23.6%"), "Should render baseline risk score");
    assert.ok(resultsHtml.includes("26.0%"), "Should render simulated risk score");
    assert.ok(resultsHtml.includes("+2.4 pts"), "Should render delta risk points");
    assert.ok(resultsHtml.includes("2. RED ZONE STATUS"), "Should render Red Zone status section");
    assert.ok(resultsHtml.includes("3. EVACUATION PRIORITY"), "Should render Evacuation Priority section");
    assert.ok(resultsHtml.includes("4. RELOCATION & SHELTER"), "Should render Relocation section");
    assert.ok(resultsHtml.includes("Demand:"), "Should render Demand label");
    assert.ok(resultsHtml.includes("Allocated:"), "Should render Allocated label");
    assert.ok(resultsHtml.includes("Unallocated:"), "Should render Unallocated label");
    assert.ok(resultsHtml.includes("ESCALATED"), "Should indicate escalated priority badge");

    // Test 3: Multi-district aggregate formatting
    const multiResult = {
        scenarioId: "SCEN-RAIN-101",
        scenarioName: "Monsoon Extreme Rainfall +20%",
        totalDistrictsEvaluated: 38,
        districtsWithIncreasedRiskCount: 12,
        baselineRedZoneCount: 0,
        simulatedRedZoneCount: 3,
        netRedZoneChange: 3,
        baselineImmediatePriorityCount: 1,
        simulatedImmediatePriorityCount: 5,
        districtComparisons: [
            sampleResult.districtComparisons[0],
            {
                districtName: "Patna",
                baselineRiskScore100: 57.7,
                simulatedRiskScore100: 77.9,
                deltaRiskScore100: 20.2,
                riskDirection: "INCREASED",
                baselineRedZone: false,
                simulatedRedZone: true,
                redZoneTransitionType: "ENTERED_RED_ZONE",
                baselinePriorityLevel: "SHORT_TERM",
                simulatedPriorityLevel: "IMMEDIATE",
                priorityEscalated: true,
                simulatedVulnerablePopulation: 50000,
                simulatedUnallocatedPopulation: 50000
            }
        ]
    };

    const multiHtml = SimulationModal.renderSimulationResults(multiResult, true);
    assert.ok(multiHtml.includes("Districts Evaluated:"), "Should render multi-district ribbon");
    assert.ok(multiHtml.includes("sim-districts-table"), "Should render districts comparison table");
    assert.ok(multiHtml.includes("Patna"), "Should include Patna in the table");
    assert.ok(multiHtml.includes("ENTERED_RED_ZONE") || multiHtml.includes("RED"), "Should indicate Red Zone entry");

    console.log("  ✅ All What-If Simulation Frontend Tests Passed.");
}
