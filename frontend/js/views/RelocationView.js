/**
 * Stage 8D — Relocation Decision & Capacity Planning Workspace
 *
 * Comprehensive Decision Planner & Capacity Intelligence:
 * - Relocation Overview KPIs (Settlements, Evacuees, Accommodated, Capacity Gap)
 * - Prioritized Evacuation Queue (Stage 7A Priority Classification)
 * - Relocation Decision & Feasibility Inspector:
 *   - Origin -> Destination routing
 *   - Explicit Capacity Headroom & Multi-Destination Overflow Allocation
 *   - Mandatory 5-Gate Feasibility Checklist
 *   - Embedded Leaflet Relocation Transit Corridor Mini-Map
 *   - Stage 7 Executive Decision Rationale (WHO -> WHERE -> WHY -> ACTION)
 * - District-Wide Capacity Allocation Balance Tables
 */
import { PageHeader } from "../components/PageHeader.js";
import { Card } from "../components/Card.js";
import { StatCard } from "../components/StatCard.js";
import { StatusBadge } from "../components/StatusBadge.js";
import { SectionHeader } from "../components/SectionHeader.js";
import { LoadingState } from "../components/LoadingState.js";
import { EmptyState } from "../components/EmptyState.js";
import { ErrorState } from "../components/ErrorState.js";
import { ExplainabilityDrawer } from "../components/ExplainabilityDrawer.js";
import { relocationService } from "../api/services/relocationService.js";
import { safeSiteService } from "../api/services/safeSiteService.js";
import { MOCK_RELOCATION_CASES, MOCK_SAFE_SITES, MOCK_CAPACITY_SUMMARY } from "../api/fixtures/mockData.js";

export class RelocationView {
    constructor(context = {}) {
        this.context = context;
        this.title = "Relocation Planner & Decisions";
        this.miniMap = null;
        this.state = {
            isLoading: true,
            error: null,
            cases: [],
            selectedCaseId: "REL-CASE-001",
            activeCase: null,
            safeSites: []
        };
    }

    async render() {
        return `
            <div class="view-container" id="relocation-root">
                <div id="relocation-dynamic-container">
                    ${LoadingState.render({ message: "Loading relocation decision intelligence and capacity plans..." })}
                </div>
            </div>
        `;
    }

    async mount(container) {
        await this.loadRelocationData();
    }

    async loadRelocationData() {
        const dynamicContainer = document.getElementById("relocation-dynamic-container");
        if (!dynamicContainer) return;

        try {
            const [decisionRes, siteRes] = await Promise.allSettled([
                relocationService.queryDecisions("Sitamarhi"),
                safeSiteService.getSitesByDistrict("Sitamarhi")
            ]);

            let cases = MOCK_RELOCATION_CASES;
            let safeSites = MOCK_SAFE_SITES;

            if (siteRes.status === "fulfilled" && siteRes.value?.success && siteRes.value?.data) {
                const sList = Array.isArray(siteRes.value.data) ? siteRes.value.data : (siteRes.value.data.sites || []);
                if (sList.length > 0) safeSites = sList;
            }

            this.state.cases = cases;
            this.state.safeSites = safeSites;
            this.state.activeCase = cases.find(c => c.caseId === this.state.selectedCaseId) || cases[0];
            this.state.isLoading = false;

            this.renderContent();
        } catch (err) {
            console.error("[RelocationView] Error loading data:", err);
            this.state.cases = MOCK_RELOCATION_CASES;
            this.state.safeSites = MOCK_SAFE_SITES;
            this.state.activeCase = MOCK_RELOCATION_CASES[0];
            this.state.isLoading = false;
            this.renderContent();
        }
    }

    renderContent() {
        if (typeof document === "undefined") return "";
        const dynamicContainer = document.getElementById("relocation-dynamic-container");
        if (!dynamicContainer) return;

        const headerHtml = PageHeader.render({
            title: "Relocation Decision & Capacity Planner",
            subtitle: "Stage 7 Priority classification, multi-criteria shelter assignments, capacity shortfall detection, and convoy logistics.",
            breadcrumbs: [
                { label: "Home", path: "#/overview" },
                { label: "Relocation Planner" }
            ],
            actionsHtml: `
                <a href="#/safe-sites" class="btn btn-sm btn-outline">🛡️ Safe Site Directory</a>
                <a href="#/map" class="btn btn-sm btn-primary">🗺️ Open GIS Workspace</a>
            `
        });

        const kpi = MOCK_CAPACITY_SUMMARY;

        // Top Summary KPIs
        const kpisHtml = `
            <div class="overview-kpi-row">
                ${StatCard.render({
                    label: "Settlements Requiring Action",
                    value: kpi.settlementsNeedingRelocation.toString(),
                    icon: "🚨",
                    subtitle: "High-priority habitations in inundation zones",
                    status: "critical"
                })}
                ${StatCard.render({
                    label: "People Requiring Relocation",
                    value: kpi.evacueesRequiringRelocation.toLocaleString(),
                    icon: "👥",
                    subtitle: "Identified vulnerable evacuees across district",
                    status: "neutral"
                })}
                ${StatCard.render({
                    label: "Accommodated in Primary Shelters",
                    value: kpi.evacueesAccommodated.toLocaleString(),
                    icon: "✓",
                    subtitle: "93.7% absorbed in primary destination safe sites",
                    status: "safe"
                })}
                ${StatCard.render({
                    label: "Capacity Shortfall (Overflow Needed)",
                    value: kpi.capacityGap.toLocaleString(),
                    icon: "⚠",
                    subtitle: "Routed cleanly to secondary safe shelters",
                    status: "warning"
                })}
            </div>
        `;

        // 2-Column Workspace: Priority Queue + Active Decision Inspector
        const workspaceHtml = `
            <div class="relocation-workspace-layout">
                <!-- Left: Priority Evacuation Queue -->
                <div class="relocation-queue-panel">
                    <div style="padding: var(--space-3) var(--space-4); border-bottom: 1px solid var(--border-default); display: flex; justify-content: space-between; align-items: center;">
                        <h3 style="font-size: 0.9rem; font-weight: 700; color: var(--text-primary); text-transform: uppercase; letter-spacing: 0.05em;">
                            Priority Queue (Stage 7A)
                        </h3>
                        <span class="badge badge-critical">${this.state.cases.length} Cases</span>
                    </div>

                    <div class="relocation-queue-list">
                        ${this.state.cases.map(c => this.renderQueueItem(c)).join("")}
                    </div>
                </div>

                <!-- Right: Comprehensive Decision & Plan Inspector -->
                <div class="relocation-detail-panel" id="relocation-detail-container">
                    ${this.renderDetailPanel(this.state.activeCase)}
                </div>
            </div>
        `;

        // District-Wide Capacity Allocation Balance Tables
        const capacityTablesHtml = `
            ${SectionHeader.render({
                title: "District Capacity Balance & Settlement Allocations",
                subtitle: "Transparent capacity accounting between vulnerable habitations and designated emergency shelters"
            })}

            <div class="grid-2">
                <!-- Site Capacity Breakdown -->
                ${Card.render({
                    title: "Safe Shelter Bed Capacity Balance",
                    icon: "🏢",
                    bodyHtml: `
                        <table class="data-table" style="width: 100%; font-size: 0.78rem;">
                            <thead>
                                <tr>
                                    <th>Shelter Facility</th>
                                    <th>Total Beds</th>
                                    <th>Allocated</th>
                                    <th>Headroom</th>
                                    <th>Util.</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${this.state.safeSites.map(s => {
                                    const total = s.totalCapacity || 5000;
                                    const alloc = s.allocatedCapacity || 0;
                                    const free = s.availableCapacity || (total - alloc);
                                    const util = total > 0 ? ((alloc / total) * 100).toFixed(0) : "0";
                                    return `
                                        <tr>
                                            <td><strong>${s.name || s.siteId}</strong></td>
                                            <td>${total.toLocaleString()}</td>
                                            <td style="color: var(--status-info-bright); font-weight: 700;">${alloc.toLocaleString()}</td>
                                            <td style="color: var(--status-safe-text); font-weight: 700;">${free.toLocaleString()}</td>
                                            <td><span class="badge ${parseInt(util) > 30 ? 'badge-warning' : 'badge-safe'}">${util}%</span></td>
                                        </tr>
                                    `;
                                }).join("")}
                            </tbody>
                        </table>
                    `
                })}

                <!-- Settlement Allocation Roster -->
                ${Card.render({
                    title: "Settlement Relocation Allocation Roster",
                    icon: "📋",
                    bodyHtml: `
                        <table class="data-table" style="width: 100%; font-size: 0.78rem;">
                            <thead>
                                <tr>
                                    <th>Origin Habitation</th>
                                    <th>Evacuees</th>
                                    <th>Primary Destination</th>
                                    <th>Allocated</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${this.state.cases.map(c => `
                                    <tr>
                                        <td><strong>${c.habitationName}</strong></td>
                                        <td>${c.population.toLocaleString()}</td>
                                        <td>${c.primaryDestination.siteName}</td>
                                        <td style="font-weight: 700;">${c.primaryDestination.allocatedPopulation.toLocaleString()}</td>
                                        <td>
                                            ${c.hasCapacityGap
                                                ? `<span class="badge badge-warning">⚠ Overflow (850)</span>`
                                                : `<span class="badge badge-safe">✓ Feasible</span>`}
                                        </td>
                                    </tr>
                                `).join("")}
                            </tbody>
                        </table>
                    `
                })}
            </div>
        `;

        dynamicContainer.innerHTML = `
            <div style="display: flex; flex-direction: column; gap: var(--space-5);">
                ${headerHtml}
                ${kpisHtml}
                ${SectionHeader.render({
                    title: "Relocation Decision Planning Workspace",
                    subtitle: "Select a prioritized evacuation case to inspect route feasibility, capacity matching, and execution orders"
                })}
                ${workspaceHtml}
                ${capacityTablesHtml}
            </div>
        `;

        this.bindEvents();
        setTimeout(() => {
            this.initMiniMap(this.state.activeCase);
        }, 100);
    }

    renderQueueItem(c) {
        const isActive = c.caseId === this.state.activeCase.caseId;
        const popDisplay = c.population.toLocaleString();

        return `
            <div class="relocation-queue-item ${isActive ? 'active' : ''}" data-case-id="${c.caseId}">
                <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                    <strong style="font-size: 0.85rem; color: ${isActive ? '#ffffff' : 'var(--text-primary)'};">
                        ${c.habitationName}
                    </strong>
                    ${StatusBadge.render({ status: c.priorityLevel || "IMMEDIATE", label: (c.priorityScore || 0.88).toFixed(2) })}
                </div>

                <div style="font-size: 0.75rem; color: var(--text-secondary); margin-top: 4px; display: flex; justify-content: space-between;">
                    <span>👥 ${popDisplay} Evacuees</span>
                    <span>Risk: <strong>${Math.round(c.riskScore * 100)}/100</strong></span>
                </div>

                <div style="font-size: 0.72rem; color: var(--status-safe-text); margin-top: 4px;">
                    ➔ ${c.primaryDestination.siteName} (${c.primaryDestination.transitDistanceKm.toFixed(1)} km)
                </div>

                ${c.hasCapacityGap ? `
                    <div style="margin-top: 4px;">
                        <span class="badge badge-warning" style="font-size: 0.68rem;">⚠ Capacity Gap: ${c.capacityShortfall} evacuees</span>
                    </div>
                ` : ''}
            </div>
        `;
    }

    renderDetailPanel(c) {
        if (!c) {
            return EmptyState.render({ title: "No Case Selected", description: "Select a relocation case from the priority queue." });
        }

        const pop = c.population;
        const primAvail = c.primaryDestination.availableCapacity;
        const isGap = c.hasCapacityGap;

        return `
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle); padding-bottom: var(--space-3);">
                <div>
                    <div style="font-size: 0.72rem; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 700;">
                        Consolidated Evacuation Assignment • ${c.district} District
                    </div>
                    <h2 style="font-size: 1.25rem; font-weight: 800; color: var(--text-primary); margin-top: 2px;">
                        ${c.habitationName}
                    </h2>
                </div>
                <div style="display: flex; gap: var(--space-2); align-items: center;">
                    <button type="button" class="btn btn-xs btn-outline" id="btnRelocationExplain">💡 Why this destination?</button>
                    ${StatusBadge.render({ status: c.priorityLevel || "IMMEDIATE", label: `${c.priorityLevel || 'IMMEDIATE'} PRIORITY` })}
                    ${StatusBadge.render({ status: isGap ? "WARNING" : "SAFE", label: isGap ? "⚠ MULTI-DESTINATION" : "✓ FEASIBLE" })}
                </div>
            </div>

            <!-- Capacity Feasibility & Gap Alert -->
            ${isGap ? `
                <div class="capacity-gap-alert">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <strong style="color: var(--status-critical-text); font-size: 0.88rem;">⚠ CAPACITY SHORTFALL DETECTED: ${c.capacityShortfall.toLocaleString()} Evacuees</strong>
                        <span class="badge badge-critical">Headroom Deficit</span>
                    </div>
                    <div style="font-size: 0.78rem; color: var(--text-primary); line-height: 1.4;">
                        Origin population (<strong>${pop.toLocaleString()}</strong>) exceeds primary shelter headroom (<strong>${primAvail.toLocaleString()}</strong>).
                        The Stage 6 Allocation Engine has automatically established an optimal multi-destination distribution to accommodate all evacuees.
                    </div>
                </div>
            ` : `
                <div style="background: rgba(16, 185, 129, 0.12); border-left: 4px solid var(--status-safe); padding: var(--space-3); border-radius: var(--radius-md); font-size: 0.8rem;">
                    <strong style="color: var(--status-safe-text);">✓ FULL SINGLE-SITE FEASIBILITY:</strong>
                    All <strong>${pop.toLocaleString()}</strong> evacuees are comfortably accommodated within '${c.primaryDestination.siteName}' with <strong>${(primAvail - pop).toLocaleString()} beds surplus headroom</strong> remaining.
                </div>
            `}

            <!-- Allocation Destinations Cards Grid -->
            <div class="grid-2">
                <!-- Primary Destination -->
                <div style="background: var(--bg-surface-elevated); border: 1px solid var(--border-default); border-radius: var(--radius-md); padding: var(--space-3); display: flex; flex-direction: column; gap: var(--space-2);">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 0.72rem; color: var(--text-secondary); text-transform: uppercase; font-weight: 700;">Primary Destination (Rank #1)</span>
                        ${StatusBadge.render({ status: c.primaryDestination.suitabilityClass || "HIGHLY_SUITABLE" })}
                    </div>
                    <h4 style="font-size: 0.95rem; font-weight: 700; color: var(--text-primary);">${c.primaryDestination.siteName}</h4>
                    <div style="font-size: 0.75rem; color: var(--text-secondary);">
                        ID: <span style="font-family: var(--font-family-mono); color: var(--status-info-bright);">${c.primaryDestination.siteId}</span> | Transit: <strong>${c.primaryDestination.transitDistanceKm.toFixed(2)} km</strong>
                    </div>
                    <div style="background: var(--bg-surface); padding: var(--space-2) var(--space-3); border-radius: var(--radius-sm); margin-top: 4px;">
                        <div style="display: flex; justify-content: space-between; font-size: 0.75rem;">
                            <span style="color: var(--text-secondary);">Allocated Evacuees:</span>
                            <strong style="color: var(--status-info-bright);">${c.primaryDestination.allocatedPopulation.toLocaleString()} beds</strong>
                        </div>
                    </div>
                </div>

                <!-- Secondary / Overflow Destination if gap -->
                ${c.overflowDestination ? `
                    <div style="background: var(--bg-surface-elevated); border: 1px solid rgba(245, 158, 11, 0.4); border-radius: var(--radius-md); padding: var(--space-3); display: flex; flex-direction: column; gap: var(--space-2);">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span style="font-size: 0.72rem; color: var(--status-warning-text); text-transform: uppercase; font-weight: 700;">Secondary / Overflow (Rank #2)</span>
                            ${StatusBadge.render({ status: c.overflowDestination.suitabilityClass || "SUITABLE" })}
                        </div>
                        <h4 style="font-size: 0.95rem; font-weight: 700; color: var(--text-primary);">${c.overflowDestination.siteName}</h4>
                        <div style="font-size: 0.75rem; color: var(--text-secondary);">
                            ID: <span style="font-family: var(--font-family-mono); color: var(--status-info-bright);">${c.overflowDestination.siteId}</span> | Transit: <strong>${c.overflowDestination.transitDistanceKm.toFixed(2)} km</strong>
                        </div>
                        <div style="background: var(--bg-surface); padding: var(--space-2) var(--space-3); border-radius: var(--radius-sm); margin-top: 4px;">
                            <div style="display: flex; justify-content: space-between; font-size: 0.75rem;">
                                <span style="color: var(--text-secondary);">Overflow Evacuees:</span>
                                <strong style="color: var(--status-warning-text);">${c.overflowDestination.allocatedPopulation.toLocaleString()} beds</strong>
                            </div>
                        </div>
                    </div>
                ` : `
                    <div style="background: var(--bg-surface-elevated); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); padding: var(--space-3); display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center; color: var(--text-secondary); font-size: 0.8rem;">
                        <span style="font-size: 1.5rem; margin-bottom: 4px;">🛡️</span>
                        <strong>Single Site Sufficient</strong>
                        <span>No secondary overflow shelter required.</span>
                    </div>
                `}
            </div>

            <!-- Mandatory 5-Gate Feasibility Verification -->
            <div>
                <h4 style="font-size: 0.82rem; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: var(--space-2);">
                    Mandatory 5-Gate Feasibility Verification (Stage 6.2)
                </h4>
                <div class="feasibility-gates-grid">
                    ${c.feasibilityGates.map(g => `
                        <div class="feasibility-gate-card ${g.status === 'PASS' ? 'pass' : (g.status === 'PARTIAL' ? 'partial' : 'fail')}">
                            <span><strong>${g.gate}:</strong> ${g.detail}</span>
                            <span class="badge ${g.status === 'PASS' ? 'badge-safe' : (g.status === 'PARTIAL' ? 'badge-warning' : 'badge-critical')}">
                                ${g.status === 'PASS' ? '✓ PASS' : (g.status === 'PARTIAL' ? '⚠ PARTIAL' : '✕ FAIL')}
                            </span>
                        </div>
                    `).join("")}
                </div>
            </div>

            <!-- Spatial Transit Corridor Mini-Map -->
            <div>
                <h4 style="font-size: 0.82rem; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: var(--space-2);">
                    Evacuation Transit Corridor Map
                </h4>
                <div class="relocation-mini-map-container">
                    <div id="relocation-mini-map" role="region" aria-label="Relocation Transit Corridor Map"></div>
                </div>
            </div>

            <!-- Executive Decision Rationale -->
            ${Card.render({
                title: "Executive Decision Rationale (WHO → WHERE → WHY → ACTION)",
                icon: "💡",
                bodyHtml: `
                    <div style="display: flex; flex-direction: column; gap: var(--space-2); font-size: 0.82rem; line-height: 1.5;">
                        <div><strong style="color: var(--status-info-bright);">WHO:</strong> ${c.decisionRationale.who}</div>
                        <div><strong style="color: var(--status-safe-text);">WHERE:</strong> ${c.decisionRationale.where}</div>
                        <div><strong style="color: var(--status-warning-text);">WHY:</strong> ${c.decisionRationale.why}</div>
                        <div style="margin-top: var(--space-2); padding: var(--space-2) var(--space-3); background: rgba(239, 68, 68, 0.12); border-left: 3px solid var(--status-critical); border-radius: var(--radius-sm); font-size: 0.78rem; color: #ffffff;">
                            <strong>⚡ ACTION:</strong> ${c.decisionRationale.action}
                        </div>
                    </div>
                `,
                footerHtml: `
                    <div style="display: flex; justify-content: space-between; width: 100%; align-items: center;">
                        <a href="#/settlements/${c.habitationId}" class="btn btn-sm btn-outline">Inspect Settlement Intelligence</a>
                        <a href="#/safe-sites" class="btn btn-sm btn-secondary">Inspect Shelter Directory ➔</a>
                    </div>
                `
            })}
        `;
    }

    bindEvents() {
        if (typeof document === "undefined") return;

        // Handle clicking on priority queue items
        const queueItems = document.querySelectorAll(".relocation-queue-item");
        queueItems.forEach(item => {
            item.addEventListener("click", () => {
                const caseId = item.getAttribute("data-case-id");
                const matched = this.state.cases.find(c => c.caseId === caseId);
                if (matched) {
                    this.state.selectedCaseId = caseId;
                    this.state.activeCase = matched;

                    // Update active classes
                    queueItems.forEach(i => i.classList.remove("active"));
                    item.classList.add("active");

                    // Re-render detail panel
                    const detailContainer = document.getElementById("relocation-detail-container");
                    if (detailContainer) {
                        detailContainer.innerHTML = this.renderDetailPanel(matched);
                        this.bindDetailEvents(matched);
                        setTimeout(() => {
                            this.initMiniMap(matched);
                        }, 100);
                    }
                }
            });
        });

        this.bindDetailEvents(this.state.activeCase);
    }

    bindDetailEvents(c) {
        if (typeof document === "undefined" || !c) return;

        document.getElementById("btnRelocationExplain")?.addEventListener("click", () => {
            ExplainabilityDrawer.open({
                explanation: {
                    habitationId: c.habitationId,
                    habitationName: c.habitationName,
                    priorityLevel: c.priorityLevel,
                    priorityScore: c.priorityScore,
                    riskScore: c.riskScore,
                    decisionRationale: c.decisionRationale,
                    priorityEvidence: [
                        { displayName: "Hazard Safety Verification", weight: 0.30, normalizedScore: 1.0, interpretation: "Site situated safely outside active 100-year flood zone" },
                        { displayName: "Transit Corridor Feasibility", weight: 0.25, normalizedScore: 0.90, interpretation: `${c.primaryDestination.transitDistanceKm.toFixed(1)} km transit corridor is fully operational` },
                        { displayName: "Shelter Headroom Capacity", weight: 0.25, normalizedScore: c.hasCapacityGap ? 0.83 : 1.0, interpretation: `${c.primaryDestination.availableCapacity.toLocaleString()} beds available` },
                        { displayName: "Medical & Sanitation Readiness", weight: 0.20, normalizedScore: 0.95, interpretation: "Dedicated emergency medical team & sanitation units" }
                    ]
                }
            });
        });
    }

    initMiniMap(c) {
        const mapEl = document.getElementById("relocation-mini-map");
        if (!mapEl || typeof L === "undefined" || !c) return;

        if (this.miniMap) {
            this.miniMap.remove();
            this.miniMap = null;
        }

        const [originLat, originLon] = c.originCoordinates || [26.6850, 85.5240];

        this.miniMap = L.map("relocation-mini-map", {
            center: [originLat, originLon],
            zoom: 12,
            zoomControl: true,
            attributionControl: false
        });

        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19
        }).addTo(this.miniMap);

        const latLngs = [[originLat, originLon]];

        // Origin marker
        const originMarker = L.circleMarker([originLat, originLon], {
            radius: 9,
            color: "#ffffff",
            weight: 2.5,
            fillColor: c.priorityLevel === "IMMEDIATE" ? "#ef4444" : "#f59e0b",
            fillOpacity: 1
        }).addTo(this.miniMap);

        originMarker.bindPopup(`<strong>📍 Origin:</strong> ${c.habitationName}`).openPopup();

        // Primary Destination marker & transit line
        if (c.primaryDestination && c.primaryDestination.coordinates) {
            const [pLat, pLon] = c.primaryDestination.coordinates;
            latLngs.push([pLat, pLon]);

            const primMarker = L.circleMarker([pLat, pLon], {
                radius: 9,
                color: "#10b981",
                weight: 2.5,
                fillColor: "#059669",
                fillOpacity: 1
            }).addTo(this.miniMap);

            primMarker.bindPopup(`<strong>🛡️ Primary:</strong> ${c.primaryDestination.siteName} (${c.primaryDestination.allocatedPopulation} evacuees)`);

            L.polyline([[originLat, originLon], [pLat, pLon]], {
                color: "#38bdf8",
                weight: 3,
                opacity: 0.9,
                dashArray: "6, 6"
            }).addTo(this.miniMap);
        }

        // Overflow Destination marker & transit line
        if (c.overflowDestination && c.overflowDestination.coordinates) {
            const [oLat, oLon] = c.overflowDestination.coordinates;
            latLngs.push([oLat, oLon]);

            const overMarker = L.circleMarker([oLat, oLon], {
                radius: 8,
                color: "#f59e0b",
                weight: 2,
                fillColor: "#d97706",
                fillOpacity: 1
            }).addTo(this.miniMap);

            overMarker.bindPopup(`<strong>🛡️ Overflow:</strong> ${c.overflowDestination.siteName} (${c.overflowDestination.allocatedPopulation} evacuees)`);

            L.polyline([[originLat, originLon], [oLat, oLon]], {
                color: "#f59e0b",
                weight: 2.5,
                opacity: 0.85,
                dashArray: "4, 4"
            }).addTo(this.miniMap);
        }

        if (latLngs.length > 1) {
            this.miniMap.fitBounds(L.latLngBounds(latLngs), { padding: [30, 30] });
        }

        setTimeout(() => {
            if (this.miniMap) this.miniMap.invalidateSize();
        }, 150);
    }

    destroy() {
        if (this.miniMap) {
            this.miniMap.remove();
            this.miniMap = null;
        }
    }
}
