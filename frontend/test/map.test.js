/**
 * Stage 8B — Interactive GIS Map (MapView) Unit Tests
 */
import assert from "node:assert";
import { MapView } from "../js/views/MapView.js";
import { MOCK_SETTLEMENTS, MOCK_SAFE_SITES, MOCK_HAZARD_POLYGONS } from "../js/api/fixtures/mockData.js";

export async function runMapTests() {
    console.log("▶ Running Interactive GIS Map (MapView) Tests...");

    const view = new MapView();
    view.state.settlements = MOCK_SETTLEMENTS;
    view.state.safeSites = MOCK_SAFE_SITES;
    view.state.hazardGeoJson = MOCK_HAZARD_POLYGONS;

    // Test 1: Render returns spatial layout frame
    const html = await view.render();
    assert.ok(html.includes("Spatial Decision Map"), "MapView should render PageHeader title");
    assert.ok(html.includes("map-workspace-container"), "MapView should render workspace container");
    assert.ok(html.includes('id="gis-leaflet-map"'), "MapView should render leaflet container #gis-leaflet-map");

    // Test 2: Layer toggle controls presence
    assert.ok(html.includes('id="chk-layer-hazards"'), "Should contain Hazards layer toggle");
    assert.ok(html.includes('id="chk-layer-redZones"'), "Should contain Red Zones layer toggle");
    assert.ok(html.includes('id="chk-layer-settlements"'), "Should contain Settlements layer toggle");
    assert.ok(html.includes('id="chk-layer-safeSites"'), "Should contain Safe Sites layer toggle");
    assert.ok(html.includes('id="chk-layer-relocation"'), "Should contain Relocation layer toggle");

    // Test 3: Map legend rendering
    assert.ok(html.includes("map-legend-panel"), "Should render map legend panel");
    assert.ok(html.includes("Immediate Priority (Red)"), "Legend should detail Immediate Priority");
    assert.ok(html.includes("Validated Safe Shelter"), "Legend should detail Validated Safe Shelter");
    assert.ok(html.includes("Relocation Transit Line"), "Legend should detail Relocation Transit Line");

    // Test 4: Verify layer groups and visibility defaults
    assert.strictEqual(view.layerVisibility.hazards, true, "Hazards layer should default to visible");
    assert.strictEqual(view.layerVisibility.redZones, true, "Red Zones layer should default to visible");
    assert.strictEqual(view.layerVisibility.settlements, true, "Settlements layer should default to visible");
    assert.strictEqual(view.layerVisibility.safeSites, true, "Safe Sites layer should default to visible");
    assert.strictEqual(view.layerVisibility.relocation, true, "Relocation layer should default to visible");

    // Test 5: Verify Sitamarhi coordinates center
    assert.deepStrictEqual(view.state.center, [26.595, 85.503], "Map center should be configured to Sitamarhi coords");
    assert.strictEqual(view.state.zoom, 11, "Default zoom level should be 11");

    console.log("  ✅ All Interactive GIS Map Tests Passed.");
}
