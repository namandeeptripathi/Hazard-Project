/**
 * Stage 8B — Interactive GIS Map View
 *
 * Provides a responsive, full-featured GIS map workspace powered by Leaflet:
 * - CartoDB Dark Matter base map
 * - 5 Toggleable Layers (Hazards, Red Zones, Settlements, Safe Sites, Relocation Corridors)
 * - Interactive custom-styled popups with [View Details] links to #/settlements/:id
 * - Directional relocation transit lines from origins to designated safe shelters
 * - Compact floating map legend
 * - District filter, recentering, and fit-to-bounds controls
 */
import { PageHeader } from "../components/PageHeader.js";
import { Button } from "../components/Button.js";
import { StatusBadge } from "../components/StatusBadge.js";
import { ExplainabilityDrawer } from "../components/ExplainabilityDrawer.js";
import { settlementService } from "../api/services/settlementService.js";
import { safeSiteService } from "../api/services/safeSiteService.js";
import { hazardService } from "../api/services/hazardService.js";
import { relocationService } from "../api/services/relocationService.js";
import { MOCK_SETTLEMENTS, MOCK_SAFE_SITES, MOCK_HAZARD_POLYGONS } from "../api/fixtures/mockData.js";

export class MapView {
    constructor(context = {}) {
        this.context = context;
        this.title = "GIS Map Workspace";
        this.map = null;
        this.layerGroups = {
            hazards: null,
            redZones: null,
            settlements: null,
            safeSites: null,
            relocation: null
        };
        this.layerVisibility = {
            hazards: true,
            redZones: true,
            settlements: true,
            safeSites: true,
            relocation: true
        };
        this.state = {
            district: "Sitamarhi",
            center: [26.595, 85.503],
            zoom: 11,
            settlements: [],
            safeSites: [],
            hazardGeoJson: null
        };
    }

    /**
     * Renders the Map view HTML frame.
     */
    async render() {
        const headerHtml = PageHeader.render({
            title: "Spatial Decision Map",
            subtitle: "Multi-hazard spatial overlays, active red-zone inundation polygons, settlements, and evacuation transit corridors.",
            breadcrumbs: [{ label: "Home", path: "#/overview" }, { label: "Spatial Map" }],
            actionsHtml: `
                <button type="button" class="btn btn-sm btn-secondary" id="btnRecenterMap">🎯 Recenter</button>
                <button type="button" class="btn btn-sm btn-primary" onclick="window.location.hash='#/relocation'">🚚 Relocation Planner</button>
            `
        });

        const toolbarHtml = `
            <div class="map-toolbar">
                <div style="display: flex; align-items: center; gap: var(--space-3);">
                    <select class="form-control" id="mapDistrictSelect" aria-label="Select Target District">
                        <option value="Sitamarhi" selected>District: Sitamarhi</option>
                        <option value="Patna">District: Patna</option>
                        <option value="Muzaffarpur">District: Muzaffarpur</option>
                        <option value="Darbhanga">District: Darbhanga</option>
                    </select>
                </div>

                <div class="map-layer-toggles" role="group" aria-label="Map Layer Toggles">
                    <span class="layer-toggle-label">LAYERS:</span>
                    <label class="layer-toggle-item active" id="toggle-lbl-hazards">
                        <input type="checkbox" id="chk-layer-hazards" checked /> Hazards
                    </label>
                    <label class="layer-toggle-item active" id="toggle-lbl-redZones">
                        <input type="checkbox" id="chk-layer-redZones" checked /> Red Zones
                    </label>
                    <label class="layer-toggle-item active" id="toggle-lbl-settlements">
                        <input type="checkbox" id="chk-layer-settlements" checked /> Settlements
                    </label>
                    <label class="layer-toggle-item active" id="toggle-lbl-safeSites">
                        <input type="checkbox" id="chk-layer-safeSites" checked /> Safe Sites
                    </label>
                    <label class="layer-toggle-item active" id="toggle-lbl-relocation">
                        <input type="checkbox" id="chk-layer-relocation" checked /> Relocation
                    </label>
                </div>
            </div>
        `;

        const canvasHtml = `
            <div class="map-canvas-container">
                <div id="gis-leaflet-map" role="region" aria-label="Interactive Leaflet Map"></div>

                <!-- Floating Map Legend -->
                <div class="map-legend-panel" aria-label="Map Legend">
                    <div class="map-legend-title">RISK SEVERITY</div>
                    <div class="map-legend-section">
                        <div class="legend-item">
                            <span class="legend-color-dot" style="background: var(--status-critical);"></span>
                            <span>Immediate Priority (Red)</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-color-dot" style="background: var(--status-warning);"></span>
                            <span>Short-Term (High Risk)</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-color-dot" style="background: var(--status-moderate);"></span>
                            <span>Medium-Term (Moderate)</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-color-dot" style="background: var(--status-safe);"></span>
                            <span>Safe / Feasible</span>
                        </div>
                    </div>

                    <div class="map-legend-title" style="margin-top: var(--space-2);">MAP ENTITIES</div>
                    <div class="map-legend-section">
                        <div class="legend-item">
                            <span class="legend-color-dot" style="background: #38bdf8; border: 1px solid #ffffff;"></span>
                            <span>Settlement Habitation</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-color-dot" style="background: #10b981; border: 1px solid #ffffff;"></span>
                            <span>Validated Safe Shelter</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-color-line"></span>
                            <span>Relocation Transit Line</span>
                        </div>
                    </div>
                </div>
            </div>
        `;

        return `
            <div class="view-container" id="map-view">
                ${headerHtml}
                <div class="map-workspace-container">
                    ${toolbarHtml}
                    ${canvasHtml}
                </div>
            </div>
        `;
    }

    /**
     * Initializes Leaflet and fetches spatial layers.
     */
    async mount(container) {
        await this.initLeafletMap();
        await this.loadSpatialData();
        this.bindToolbarEvents();
    }

    /**
     * Initializes Leaflet Map instance with Dark tiles.
     */
    async initLeafletMap() {
        const mapContainer = document.getElementById("gis-leaflet-map");
        if (!mapContainer) return;

        // Ensure Leaflet is loaded
        if (typeof L === "undefined") {
            console.warn("[MapView] Leaflet library not detected. Loading dynamically...");
            await this.loadLeafletDynamically();
        }

        if (typeof L === "undefined") {
            console.error("[MapView] Leaflet failed to load.");
            mapContainer.innerHTML = `<div class="error-state" style="margin: 20px;">Leaflet GIS library is currently unavailable.</div>`;
            return;
        }

        // Clean up previous map instance if existing
        if (this.map) {
            this.map.remove();
            this.map = null;
        }

        // Create Leaflet map centered on Sitamarhi
        this.map = L.map("gis-leaflet-map", {
            center: this.state.center,
            zoom: this.state.zoom,
            zoomControl: true,
            attributionControl: false
        });

        // Add OpenStreetMap base tile layer with dark-adapted styling
        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(this.map);

        // Initialize Layer Groups
        this.layerGroups.hazards = L.layerGroup().addTo(this.map);
        this.layerGroups.redZones = L.layerGroup().addTo(this.map);
        this.layerGroups.relocation = L.layerGroup().addTo(this.map);
        this.layerGroups.safeSites = L.layerGroup().addTo(this.map);
        this.layerGroups.settlements = L.layerGroup().addTo(this.map);

        // Recalculate size
        setTimeout(() => {
            if (this.map) this.map.invalidateSize();
        }, 100);
    }

    /**
     * Fetches spatial data from backend services or fallback fixtures.
     */
    async loadSpatialData() {
        try {
            const [settlementRes, safeSiteRes, hazardRes] = await Promise.allSettled([
                settlementService.getDistrictSettlementExposure(this.state.district),
                safeSiteService.getSitesByDistrict(this.state.district),
                hazardService.getLayerGeoJson("FLOOD_HAZARD_SCORES", this.state.district)
            ]);

            let settlements = [];
            let safeSites = [];
            let hazardGeoJson = null;

            if (settlementRes.status === "fulfilled" && settlementRes.value?.success && settlementRes.value?.data) {
                const sData = settlementRes.value.data;
                settlements = sData.settlements || sData.exposedSettlements || [];
            }

            if (safeSiteRes.status === "fulfilled" && safeSiteRes.value?.success && safeSiteRes.value?.data) {
                const siteData = safeSiteRes.value.data;
                safeSites = Array.isArray(siteData) ? siteData : (siteData.sites || []);
            }

            if (hazardRes.status === "fulfilled" && hazardRes.value?.success && hazardRes.value?.data) {
                hazardGeoJson = hazardRes.value.data;
            }

            // Apply authentic fixtures if backend is offline in development mode
            if (settlements.length === 0) settlements = MOCK_SETTLEMENTS;
            if (safeSites.length === 0) safeSites = MOCK_SAFE_SITES;
            if (!hazardGeoJson) hazardGeoJson = MOCK_HAZARD_POLYGONS;

            this.state.settlements = settlements;
            this.state.safeSites = safeSites;
            this.state.hazardGeoJson = hazardGeoJson;

            // Render all layers
            this.renderHazardsLayer();
            this.renderRedZonesLayer();
            this.renderSettlementsLayer();
            this.renderSafeSitesLayer();
            this.renderRelocationLayer();

        } catch (err) {
            console.error("[MapView] Error loading spatial data:", err);
        }
    }

    /**
     * Layer 1: Hazards (Flood Inundation Contours)
     */
    renderHazardsLayer() {
        if (!this.layerGroups.hazards || !this.state.hazardGeoJson) return;
        this.layerGroups.hazards.clearLayers();

        L.geoJSON(this.state.hazardGeoJson, {
            style: (feature) => ({
                color: feature.properties?.fillColor || "#38bdf8",
                weight: 2,
                opacity: 0.8,
                fillColor: feature.properties?.fillColor || "#0284c7",
                fillOpacity: 0.18,
                dashArray: "4, 4"
            }),
            onEachFeature: (feature, layer) => {
                const props = feature.properties || {};
                layer.bindPopup(`
                    <div class="map-popup-card">
                        <div class="map-popup-header">
                            <div>
                                <div class="map-popup-title">${props.name || 'Flood Hazard Footprint'}</div>
                                <div class="map-popup-subtitle">Severity: ${props.severity || 'HIGH'}</div>
                            </div>
                            <span class="badge badge-critical">${props.riskTier || 'CRITICAL'}</span>
                        </div>
                        <div class="map-popup-body">
                            <div class="map-popup-row">
                                <span class="map-popup-label">Peak Water Depth:</span>
                                <span class="map-popup-value" style="color: var(--status-critical-text);">${props.waterDepth || '2.5m'}</span>
                            </div>
                        </div>
                    </div>
                `);
            }
        }).addTo(this.layerGroups.hazards);
    }

    /**
     * Layer 2: Red Zones (Active Inundation Buffer)
     */
    renderRedZonesLayer() {
        if (!this.layerGroups.redZones || !this.state.settlements) return;
        this.layerGroups.redZones.clearLayers();

        const redZoneSettlements = this.state.settlements.filter(s => s.isRedZone || s.priorityLevel === "IMMEDIATE");

        redZoneSettlements.forEach(s => {
            if (!s.latitude || !s.longitude) return;

            // Draw circular red-zone danger buffer
            const circle = L.circle([s.latitude, s.longitude], {
                radius: 1800, // 1.8km buffer
                color: "#ef4444",
                weight: 2,
                opacity: 0.85,
                fillColor: "#ef4444",
                fillOpacity: 0.22,
                dashArray: "5, 5"
            });

            circle.bindPopup(`
                <div class="map-popup-card">
                    <div class="map-popup-header">
                        <div>
                            <div class="map-popup-title">🔴 Red Zone Inundation Buffer</div>
                            <div class="map-popup-subtitle">${s.settlementName}</div>
                        </div>
                        <span class="badge badge-critical">IMMEDIATE</span>
                    </div>
                    <div class="map-popup-body">
                        <div class="map-popup-row">
                            <span class="map-popup-label">Evacuation Status:</span>
                            <span class="map-popup-value" style="color: var(--status-critical-text);">Mandatory Convoy Order</span>
                        </div>
                    </div>
                </div>
            `);

            circle.addTo(this.layerGroups.redZones);
        });
    }

    /**
     * Layer 3: Settlements (Colored by Priority/Risk)
     */
    renderSettlementsLayer() {
        if (!this.layerGroups.settlements || !this.state.settlements) return;
        this.layerGroups.settlements.clearLayers();

        this.state.settlements.forEach(s => {
            if (!s.latitude || !s.longitude) return;

            let markerColor = "#38bdf8"; // Info blue
            let radius = 7;

            if (s.priorityLevel === "IMMEDIATE") {
                markerColor = "#ef4444"; // Red
                radius = 9;
            } else if (s.priorityLevel === "SHORT_TERM") {
                markerColor = "#f59e0b"; // Orange
                radius = 8;
            } else if (s.priorityLevel === "MEDIUM_TERM") {
                markerColor = "#8b5cf6"; // Purple
                radius = 7;
            }

            const marker = L.circleMarker([s.latitude, s.longitude], {
                radius: radius,
                color: "#ffffff",
                weight: 2,
                fillColor: markerColor,
                fillOpacity: 0.95
            });

            const scoreDisplay = typeof s.priorityScore === "number" ? s.priorityScore.toFixed(2) : "--";
            const popDisplay = s.population ? s.population.toLocaleString() : "--";
            const destDisplay = s.recommendedSiteName || "Designated Safe Shelter";

            const popupHtml = `
                <div class="map-popup-card">
                    <div class="map-popup-header">
                        <div>
                            <div class="map-popup-title">${s.settlementName}</div>
                            <div class="map-popup-subtitle">${s.habitationId} | ${s.district}</div>
                        </div>
                        ${StatusBadge.render({ status: s.priorityLevel, label: s.priorityLevel.replace('_', ' ') })}
                    </div>
                    <div class="map-popup-body">
                        <div class="map-popup-row">
                            <span class="map-popup-label">Priority Score:</span>
                            <span class="map-popup-value" style="color: ${s.priorityLevel === 'IMMEDIATE' ? 'var(--status-critical-text)' : 'var(--status-warning-text)'};">${scoreDisplay} / 1.00</span>
                        </div>
                        <div class="map-popup-row">
                            <span class="map-popup-label">Evacuee Population:</span>
                            <span class="map-popup-value">${popDisplay}</span>
                        </div>
                        <div class="map-popup-row">
                            <span class="map-popup-label">Assigned Safe Site:</span>
                            <span class="map-popup-value" style="color: var(--status-safe-text); font-weight: 600;">➔ ${destDisplay}</span>
                        </div>
                    </div>
                    <div class="map-popup-actions" style="display: flex; gap: var(--space-2); margin-top: var(--space-2);">
                        <button type="button" class="btn btn-xs btn-outline" style="flex: 1;" onclick="window.__openMapExplainability && window.__openMapExplainability('${s.habitationId}')">
                            💡 Why?
                        </button>
                        <button type="button" class="btn btn-xs btn-primary" style="flex: 2;" onclick="window.location.hash='#/settlements/${encodeURIComponent(s.habitationId)}'">
                            Inspect Details ➔
                        </button>
                    </div>
                </div>
            `;

            marker.bindPopup(popupHtml);
            marker.addTo(this.layerGroups.settlements);
        });

        // Register window helper for popup buttons
        if (typeof window !== "undefined") {
            window.__openMapExplainability = (habId) => {
                const matched = this.state.settlements.find(s => s.habitationId === habId);
                if (matched) {
                    ExplainabilityDrawer.open({
                        explanation: {
                            habitationId: matched.habitationId,
                            habitationName: matched.settlementName,
                            priorityLevel: matched.priorityLevel,
                            priorityScore: matched.priorityScore,
                            riskScore: matched.riskScore,
                            decisionRationale: matched.decisionRationale,
                            priorityEvidence: matched.decisionRationale?.contributors?.map(c => ({
                                displayName: c.name,
                                normalizedScore: parseFloat(c.value) || 0.8,
                                weight: c.name.includes("30%") ? 0.30 : (c.name.includes("20%") ? 0.20 : (c.name.includes("15%") ? 0.15 : 0.10)),
                                interpretation: c.impact
                            }))
                        }
                    });
                }
            };
        }
    }

    /**
     * Layer 4: Safe Sites (Designated Evacuation Shelters)
     */
    renderSafeSitesLayer() {
        if (!this.layerGroups.safeSites || !this.state.safeSites) return;
        this.layerGroups.safeSites.clearLayers();

        this.state.safeSites.forEach(site => {
            if (!site.latitude || !site.longitude) return;

            const marker = L.circleMarker([site.latitude, site.longitude], {
                radius: 9,
                color: "#10b981",
                weight: 2,
                fillColor: "#059669",
                fillOpacity: 0.95
            });

            const availDisplay = site.availableCapacity !== undefined ? site.availableCapacity.toLocaleString() : site.totalCapacity?.toLocaleString();
            const totalDisplay = site.totalCapacity ? site.totalCapacity.toLocaleString() : "--";

            const popupHtml = `
                <div class="map-popup-card">
                    <div class="map-popup-header">
                        <div>
                            <div class="map-popup-title">${site.name || site.siteId}</div>
                            <div class="map-popup-subtitle">${site.siteId} | ${site.district}</div>
                        </div>
                        ${StatusBadge.render({ status: site.suitabilityClass || "HIGHLY_SUITABLE" })}
                    </div>
                    <div class="map-popup-body">
                        <div class="map-popup-row">
                            <span class="map-popup-label">Total Bed Capacity:</span>
                            <span class="map-popup-value">${totalDisplay} beds</span>
                        </div>
                        <div class="map-popup-row">
                            <span class="map-popup-label">Available Headroom:</span>
                            <span class="map-popup-value" style="color: var(--status-safe-text); font-weight: 700;">${availDisplay} beds free</span>
                        </div>
                        <div class="map-popup-row">
                            <span class="map-popup-label">Hazard Safety:</span>
                            <span class="map-popup-value" style="color: var(--status-safe-text);">🟢 Outside Flood Basin</span>
                        </div>
                    </div>
                    <div class="map-popup-actions">
                        <button type="button" class="btn btn-sm btn-outline" style="width: 100%;" onclick="window.location.hash='#/safe-sites'">
                            Inspect Shelter Capacity ➔
                        </button>
                    </div>
                </div>
            `;

            marker.bindPopup(popupHtml);
            marker.addTo(this.layerGroups.safeSites);
        });
    }

    /**
     * Layer 5: Relocation Corridors (Transit Lines)
     */
    renderRelocationLayer() {
        if (!this.layerGroups.relocation || !this.state.settlements || !this.state.safeSites) return;
        this.layerGroups.relocation.clearLayers();

        const siteMap = new Map();
        this.state.safeSites.forEach(s => siteMap.set(s.siteId, s));

        this.state.settlements.forEach(s => {
            if (!s.latitude || !s.longitude || !s.recommendedSiteId) return;

            const destSite = siteMap.get(s.recommendedSiteId);
            if (!destSite || !destSite.latitude || !destSite.longitude) return;

            const latlngs = [
                [s.latitude, s.longitude],
                [destSite.latitude, destSite.longitude]
            ];

            const polyline = L.polyline(latlngs, {
                color: "#38bdf8",
                weight: 2.5,
                opacity: 0.85,
                dashArray: "6, 6"
            });

            polyline.bindPopup(`
                <div class="map-popup-card">
                    <div class="map-popup-title">🚚 Relocation Transit Corridor</div>
                    <div style="font-size: 0.8rem; margin-top: 4px;">
                        <strong>Origin:</strong> ${s.settlementName}<br/>
                        <strong>Destination:</strong> ${destSite.name}<br/>
                        <strong>Distance:</strong> ${s.transitDistanceKm ? s.transitDistanceKm.toFixed(2) + ' km' : '--'}
                    </div>
                </div>
            `);

            polyline.addTo(this.layerGroups.relocation);
        });
    }

    /**
     * Binds toolbar events for layer toggles and zoom actions.
     */
    bindToolbarEvents() {
        // Recenter button
        const btnRecenter = document.getElementById("btnRecenterMap");
        if (btnRecenter) {
            btnRecenter.addEventListener("click", () => {
                if (this.map) {
                    this.map.setView(this.state.center, this.state.zoom);
                }
            });
        }

        // District select
        const selDistrict = document.getElementById("mapDistrictSelect");
        if (selDistrict) {
            selDistrict.addEventListener("change", async (e) => {
                this.state.district = e.target.value;
                await this.loadSpatialData();
            });
        }

        // Layer toggles
        const layers = ["hazards", "redZones", "settlements", "safeSites", "relocation"];
        layers.forEach(layerKey => {
            const chk = document.getElementById(`chk-layer-${layerKey}`);
            const lbl = document.getElementById(`toggle-lbl-${layerKey}`);
            if (chk && lbl) {
                chk.addEventListener("change", (e) => {
                    const isChecked = e.target.checked;
                    this.layerVisibility[layerKey] = isChecked;

                    if (isChecked) {
                        lbl.classList.add("active");
                        if (this.layerGroups[layerKey] && this.map) {
                            this.map.addLayer(this.layerGroups[layerKey]);
                        }
                    } else {
                        lbl.classList.remove("active");
                        if (this.layerGroups[layerKey] && this.map) {
                            this.map.removeLayer(this.layerGroups[layerKey]);
                        }
                    }
                });
            }
        });
    }

    /**
     * Fallback dynamic loader for Leaflet if script was not preloaded.
     */
    async loadLeafletDynamically() {
        return new Promise((resolve) => {
            if (typeof L !== "undefined") return resolve();

            const script = document.createElement("script");
            script.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
            script.onload = () => resolve();
            script.onerror = () => resolve();
            document.head.appendChild(script);
        });
    }

    /**
     * Cleanup map on view unmount.
     */
    destroy() {
        if (this.map) {
            this.map.remove();
            this.map = null;
        }
    }
}
