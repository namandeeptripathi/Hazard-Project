/**
 * Stage 8E — UX Integration & End-to-End Consistency Tests
 */
import assert from "node:assert";
import { MOCK_SETTLEMENTS, MOCK_SAFE_SITES, MOCK_RELOCATION_CASES, MOCK_CAPACITY_SUMMARY } from "../js/api/fixtures/mockData.js";
import { OverviewView } from "../js/views/OverviewView.js";
import { SettlementDetailView } from "../js/views/SettlementDetailView.js";
import { SafeSitesView } from "../js/views/SafeSitesView.js";
import { RelocationView } from "../js/views/RelocationView.js";

export async function runUxIntegrationTests() {
    console.log("▶ Running UX Integration & End-to-End Consistency Tests...");

    // Trace 1: HAB-SIT-001 (Sonbarsa Flood Inundation Area)
    const sonbarsaOverview = MOCK_SETTLEMENTS.find(s => s.habitationId === "HAB-SIT-001");
    const sonbarsaRelocation = MOCK_RELOCATION_CASES.find(c => c.habitationId === "HAB-SIT-001");
    const centralShelter = MOCK_SAFE_SITES.find(s => s.siteId === "FAC-EMG-003");

    assert.ok(sonbarsaOverview, "Sonbarsa should exist in settlements");
    assert.ok(sonbarsaRelocation, "Sonbarsa should exist in relocation cases");
    assert.ok(centralShelter, "Central Shelter should exist in safe sites");

    // Check Data Consistency across views
    assert.strictEqual(sonbarsaOverview.population, 5000, "Overview population should be 5,000");
    assert.strictEqual(sonbarsaRelocation.population, 5000, "Relocation case population should be 5,000");
    assert.strictEqual(sonbarsaOverview.priorityLevel, sonbarsaRelocation.priorityLevel, "Priority levels must match (IMMEDIATE)");
    assert.strictEqual(sonbarsaOverview.priorityScore, sonbarsaRelocation.priorityScore, "Priority scores must match (0.88)");
    assert.strictEqual(sonbarsaOverview.recommendedSiteId, sonbarsaRelocation.primaryDestination.siteId, "Recommended destination IDs must match (FAC-EMG-003)");
    assert.strictEqual(sonbarsaOverview.transitDistanceKm, sonbarsaRelocation.primaryDestination.transitDistanceKm, "Transit distance must match (2.50 km)");

    // Check Capacity Accounting Consistency
    assert.strictEqual(centralShelter.totalCapacity, 5000, "Central Shelter total capacity should be 5,000");
    assert.strictEqual(centralShelter.availableCapacity, 4150, "Central Shelter available headroom should be 4,150");
    assert.strictEqual(sonbarsaRelocation.primaryDestination.availableCapacity, 4150, "Relocation primary available capacity must match 4,150");
    assert.strictEqual(sonbarsaRelocation.hasCapacityGap, true, "Sonbarsa must flag capacity gap (5,000 > 4,150)");
    assert.strictEqual(sonbarsaRelocation.capacityShortfall, 850, "Capacity shortfall must exactly equal 850");
    assert.strictEqual(sonbarsaRelocation.primaryDestination.allocatedPopulation + sonbarsaRelocation.overflowDestination.allocatedPopulation, 5000, "Total allocated across primary + overflow must equal 5,000 (100%)");

    // Trace 2: Non-immediate settlement HAB-SIT-002 (Bairgania)
    const bairganiaOverview = MOCK_SETTLEMENTS.find(s => s.habitationId === "HAB-SIT-002");
    const bairganiaRelocation = MOCK_RELOCATION_CASES.find(c => c.habitationId === "HAB-SIT-002");
    const pupriShelter = MOCK_SAFE_SITES.find(s => s.siteId === "FAC-EMG-005");

    assert.strictEqual(bairganiaOverview.population, 3200, "Bairgania population should be 3,200");
    assert.strictEqual(bairganiaRelocation.population, 3200, "Bairgania relocation population should be 3,200");
    assert.strictEqual(bairganiaOverview.priorityLevel, "SHORT_TERM", "Bairgania priority level should be SHORT_TERM");
    assert.strictEqual(bairganiaRelocation.hasCapacityGap, false, "Bairgania should have NO capacity gap");
    assert.strictEqual(bairganiaRelocation.primaryDestination.siteId, pupriShelter.siteId, "Bairgania destination should be Pupri Shelter (FAC-EMG-005)");

    console.log("  ✅ All UX Integration & End-to-End Consistency Tests Passed.");
}
