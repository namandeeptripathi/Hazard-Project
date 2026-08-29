/**
 * Stage 8E — Full Frontend Test Runner
 *
 * Runs all 11 frontend test suites in Node.js ES module environment:
 * 1. Router Tests
 * 2. API Client Tests
 * 3. Component Tests
 * 4. Command Center (OverviewView) Tests
 * 5. Interactive GIS Map (MapView) Tests
 * 6. Settlement Intelligence (SettlementDetailView) Tests
 * 7. Safe Sites Intelligence (SafeSitesView) Tests
 * 8. Relocation Planner & Capacity (RelocationView) Tests
 * 9. Priority Intelligence Tests (Stage 8E)
 * 10. Explainability Intelligence Tests (Stage 8E)
 * 11. UX Integration & End-to-End Consistency Tests (Stage 8E)
 */
import { runRouterTests } from "./router.test.js";
import { runApiClientTests } from "./apiClient.test.js";
import { runComponentTests } from "./components.test.js";
import { runOverviewTests } from "./overview.test.js";
import { runMapTests } from "./map.test.js";
import { runSettlementTests } from "./settlement.test.js";
import { runSafeSitesTests } from "./safeSites.test.js";
import { runRelocationTests } from "./relocation.test.js";
import { runPriorityTests } from "./priority.test.js";
import { runExplainabilityTests } from "./explainability.test.js";
import { runUxIntegrationTests } from "./uxIntegration.test.js";
import { runSimulationTests } from "./simulation.test.js";

async function main() {
    console.log("=================================================");
    console.log("Stage 8E & 9 — Disaster Decision Frontend Suite");
    console.log("=================================================\n");

    try {
        await runRouterTests();
        await runApiClientTests();
        await runComponentTests();
        await runOverviewTests();
        await runMapTests();
        await runSettlementTests();
        await runSafeSitesTests();
        await runRelocationTests();
        await runPriorityTests();
        await runExplainabilityTests();
        await runUxIntegrationTests();
        await runSimulationTests();

        console.log("\n=================================================");
        console.log("🎉 ALL 12 FRONTEND TEST SUITES PASSED (100%)");
        console.log("=================================================");
        process.exit(0);
    } catch (err) {
        console.error("\n❌ Test Suite Failed:", err);
        process.exit(1);
    }
}

main();
