/**
 * Stage 9 — What-If Disaster Scenario Simulation Modal Component
 *
 * Self-contained, modular UI modal providing:
 * - Scenario configuration (Baseline, Rainfall, Hazard, Population, Multi-Factor)
 * - Scope selection (Single District vs All Districts)
 * - Interactive percentage adjustment with quick presets
 * - Live Before -> After comparison across Risk, Red-Zone, Priority, Relocation
 * - Compact list/table of shifted districts for all-district simulations
 * - Loading and API error states
 * - Fully reversible and decoupled from core dashboard
 */
import { scenarioService } from "../api/services/scenarioService.js";
import { LoadingState } from "./LoadingState.js";
import { ErrorState } from "./ErrorState.js";
import { StatusBadge } from "./StatusBadge.js";

export class SimulationModal {
    static currentDistrict = "Sitamarhi";
    static lastResult = null;

    /**
     * Renders the complete Simulation Modal HTML markup.
     */
    static render(props = {}) {
        const district = props.district || this.currentDistrict || "Sitamarhi";

        return `
            <div class="simulation-modal-overlay" id="simulationModalOverlay" role="dialog" aria-modal="true" aria-labelledby="simModalTitle">
                <div class="simulation-modal-dialog">
                    <!-- Modal Header -->
                    <div class="simulation-modal-header">
                        <div>
                            <div class="simulation-badge-row">
                                <span class="badge-dot pulse" style="background: var(--status-info-bright);"></span>
                                <span class="badge-tag">STAGE 9 SIMULATION ENGINE</span>
                            </div>
                            <h2 class="simulation-modal-title" id="simModalTitle">What-If Disaster Scenario Simulation</h2>
                            <p class="simulation-modal-subtitle">Simulate climate & hazard perturbations in-memory to project shifts in Risk, Red-Zones, Priorities, and Relocation.</p>
                        </div>
                        <button type="button" class="btn-close-modal" id="closeSimModalBtn" aria-label="Close Simulation Modal">&times;</button>
                    </div>

                    <!-- Modal Body -->
                    <div class="simulation-modal-body">
                        <!-- Configuration Panel -->
                        <div class="sim-config-card">
                            <div class="sim-form-grid">
                                <!-- Scenario Type Selector -->
                                <div class="sim-form-group">
                                    <label for="simScenarioType" class="sim-label">1. Scenario Type</label>
                                    <select id="simScenarioType" class="sim-select">
                                        <option value="RAINFALL_CHANGE" selected>Rainfall Change Scenario (+/- %)</option>
                                        <option value="HAZARD_INTENSITY">Hazard Intensity Scenario (+/- %)</option>
                                        <option value="POPULATION_EXPOSURE">Population Exposure Scenario (+/- %)</option>
                                        <option value="MULTI_FACTOR">Multi-Factor Compound Scenario</option>
                                        <option value="BASELINE">Baseline Scenario (0% Perturbation)</option>
                                    </select>
                                </div>

                                <!-- Evaluation Scope -->
                                <div class="sim-form-group">
                                    <label for="simScope" class="sim-label">2. Scope</label>
                                    <select id="simScope" class="sim-select">
                                        <option value="DISTRICT" selected>Single District</option>
                                        <option value="ALL">All 38 Bihar Districts</option>
                                    </select>
                                </div>

                                <!-- Target District Name -->
                                <div class="sim-form-group" id="simDistrictGroup">
                                    <label for="simDistrictInput" class="sim-label">3. Target District</label>
                                    <select id="simDistrictInput" class="sim-select">
                                        <option value="Sitamarhi" ${district === "Sitamarhi" ? "selected" : ""}>Sitamarhi (High Flood Risk)</option>
                                        <option value="Patna" ${district === "Patna" ? "selected" : ""}>Patna (Urban Gangetic)</option>
                                        <option value="Muzaffarpur" ${district === "Muzaffarpur" ? "selected" : ""}>Muzaffarpur</option>
                                        <option value="Darbhanga" ${district === "Darbhanga" ? "selected" : ""}>Darbhanga</option>
                                        <option value="Bhagalpur" ${district === "Bhagalpur" ? "selected" : ""}>Bhagalpur</option>
                                        <option value="Katihar" ${district === "Katihar" ? "selected" : ""}>Katihar</option>
                                        <option value="Purnia" ${district === "Purnia" ? "selected" : ""}>Purnia</option>
                                        <option value="Supaul" ${district === "Supaul" ? "selected" : ""}>Supaul</option>
                                    </select>
                                </div>
                            </div>

                            <!-- Perturbation Controls -->
                            <div class="sim-sliders-grid" id="simSlidersArea">
                                <!-- Rainfall Slider -->
                                <div class="sim-slider-box" id="rainSliderBox">
                                    <div class="sim-slider-header">
                                        <span class="sim-slider-label">🌧️ Rainfall Change</span>
                                        <span class="sim-slider-val" id="rainValDisplay">+20%</span>
                                    </div>
                                    <input type="range" id="rainRangeInput" min="-50" max="100" step="5" value="20" class="sim-range">
                                </div>

                                <!-- Hazard Intensity Slider -->
                                <div class="sim-slider-box" id="hazardSliderBox" style="display: none;">
                                    <div class="sim-slider-header">
                                        <span class="sim-slider-label">⚡ Hazard Intensity</span>
                                        <span class="sim-slider-val" id="hazardValDisplay">0%</span>
                                    </div>
                                    <input type="range" id="hazardRangeInput" min="-50" max="100" step="5" value="0" class="sim-range">
                                </div>

                                <!-- Population Exposure Slider -->
                                <div class="sim-slider-box" id="popSliderBox" style="display: none;">
                                    <div class="sim-slider-header">
                                        <span class="sim-slider-label">👥 Population Exposure</span>
                                        <span class="sim-slider-val" id="popValDisplay">0%</span>
                                    </div>
                                    <input type="range" id="popRangeInput" min="-50" max="100" step="5" value="0" class="sim-range">
                                </div>
                            </div>

                            <!-- Quick Presets & Action Bar -->
                            <div class="sim-actions-bar">
                                <div class="sim-presets-group">
                                    <span class="sim-presets-label">Presets:</span>
                                    <button type="button" class="btn-preset" data-preset="monsoon">+20% Monsoon</button>
                                    <button type="button" class="btn-preset" data-preset="catastrophe">+50% Catastrophe</button>
                                    <button type="button" class="btn-preset" data-preset="mitigation">-30% Mitigation</button>
                                    <button type="button" class="btn-preset" data-preset="baseline">0% Baseline</button>
                                </div>
                                <button type="button" class="btn btn-primary btn-run-sim" id="runSimulationBtn">
                                    <span>⚡ Run Simulation</span>
                                </button>
                            </div>
                        </div>

                        <!-- Dynamic Results Area -->
                        <div class="sim-results-container" id="simResultsArea">
                            <div class="sim-idle-state">
                                <p class="sim-idle-text">Select parameters above and click <strong>Run Simulation</strong> to compute multi-dimensional Before &rarr; After comparative metrics.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Modal Footer -->
                    <div class="simulation-modal-footer">
                        <span class="sim-zero-mutation-tag">🛡️ Zero-Mutation Guarantee: Simulated entirely in-memory</span>
                        <button type="button" class="btn btn-secondary btn-sm" id="footerCloseSimModalBtn">Close</button>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Mounts and displays the Simulation Modal.
     */
    static open(props = {}) {
        if (typeof document === "undefined") return;

        let container = document.getElementById("global-simulation-modal-container");
        if (!container) {
            container = document.createElement("div");
            container.id = "global-simulation-modal-container";
            document.body.appendChild(container);
        }

        container.innerHTML = this.render(props);

        // Bind interactive controls
        this.bindEvents();
    }

    /**
     * Closes and unmounts the Simulation Modal.
     */
    static close() {
        if (typeof document === "undefined") return;
        const container = document.getElementById("global-simulation-modal-container");
        if (container) {
            container.innerHTML = "";
        }
    }

    /**
     * Binds DOM event listeners inside the modal.
     */
    static bindEvents() {
        // Close Buttons & Overlay
        document.getElementById("closeSimModalBtn")?.addEventListener("click", () => this.close());
        document.getElementById("footerCloseSimModalBtn")?.addEventListener("click", () => this.close());
        document.getElementById("simulationModalOverlay")?.addEventListener("click", (e) => {
            if (e.target.id === "simulationModalOverlay") this.close();
        });

        // Scenario Type Change -> Toggle Sliders
        const typeSelect = document.getElementById("simScenarioType");
        const rainBox = document.getElementById("rainSliderBox");
        const hazardBox = document.getElementById("hazardSliderBox");
        const popBox = document.getElementById("popSliderBox");

        const updateSliderVisibility = () => {
            const val = typeSelect?.value;
            if (val === "BASELINE") {
                if (rainBox) rainBox.style.display = "none";
                if (hazardBox) hazardBox.style.display = "none";
                if (popBox) popBox.style.display = "none";
            } else if (val === "RAINFALL_CHANGE") {
                if (rainBox) rainBox.style.display = "block";
                if (hazardBox) hazardBox.style.display = "none";
                if (popBox) popBox.style.display = "none";
            } else if (val === "HAZARD_INTENSITY") {
                if (rainBox) rainBox.style.display = "none";
                if (hazardBox) hazardBox.style.display = "block";
                if (popBox) popBox.style.display = "none";
            } else if (val === "POPULATION_EXPOSURE") {
                if (rainBox) rainBox.style.display = "none";
                if (hazardBox) hazardBox.style.display = "none";
                if (popBox) popBox.style.display = "block";
            } else if (val === "MULTI_FACTOR") {
                if (rainBox) rainBox.style.display = "block";
                if (hazardBox) hazardBox.style.display = "block";
                if (popBox) popBox.style.display = "block";
            }
        };

        typeSelect?.addEventListener("change", updateSliderVisibility);
        updateSliderVisibility();

        // Scope Change -> Toggle District input
        const scopeSelect = document.getElementById("simScope");
        const districtGroup = document.getElementById("simDistrictGroup");
        scopeSelect?.addEventListener("change", () => {
            if (districtGroup) {
                districtGroup.style.display = scopeSelect.value === "ALL" ? "none" : "block";
            }
        });

        // Sliders value display synchronization
        const rainInput = document.getElementById("rainRangeInput");
        const rainVal = document.getElementById("rainValDisplay");
        rainInput?.addEventListener("input", (e) => {
            const v = Number(e.target.value);
            if (rainVal) rainVal.textContent = `${v >= 0 ? "+" : ""}${v}%`;
        });

        const hazardInput = document.getElementById("hazardRangeInput");
        const hazardVal = document.getElementById("hazardValDisplay");
        hazardInput?.addEventListener("input", (e) => {
            const v = Number(e.target.value);
            if (hazardVal) hazardVal.textContent = `${v >= 0 ? "+" : ""}${v}%`;
        });

        const popInput = document.getElementById("popRangeInput");
        const popVal = document.getElementById("popValDisplay");
        popInput?.addEventListener("input", (e) => {
            const v = Number(e.target.value);
            if (popVal) popVal.textContent = `${v >= 0 ? "+" : ""}${v}%`;
        });

        // Presets Buttons
        document.querySelectorAll(".btn-preset").forEach(btn => {
            btn.addEventListener("click", (e) => {
                const preset = e.currentTarget.getAttribute("data-preset");
                if (preset === "monsoon") {
                    if (typeSelect) typeSelect.value = "RAINFALL_CHANGE";
                    if (rainInput) { rainInput.value = "20"; rainVal.textContent = "+20%"; }
                } else if (preset === "catastrophe") {
                    if (typeSelect) typeSelect.value = "MULTI_FACTOR";
                    if (rainInput) { rainInput.value = "50"; rainVal.textContent = "+50%"; }
                    if (hazardInput) { hazardInput.value = "30"; hazardVal.textContent = "+30%"; }
                    if (popInput) { popInput.value = "20"; popVal.textContent = "+20%"; }
                } else if (preset === "mitigation") {
                    if (typeSelect) typeSelect.value = "MULTI_FACTOR";
                    if (rainInput) { rainInput.value = "-30"; rainVal.textContent = "-30%"; }
                    if (hazardInput) { hazardInput.value = "-20"; hazardVal.textContent = "-20%"; }
                    if (popInput) { popInput.value = "0"; popVal.textContent = "0%"; }
                } else if (preset === "baseline") {
                    if (typeSelect) typeSelect.value = "BASELINE";
                }
                updateSliderVisibility();
            });
        });

        // Run Simulation Button
        document.getElementById("runSimulationBtn")?.addEventListener("click", () => this.executeSimulation());

        // Escape key listener
        const handleKeyDown = (e) => {
            if (e.key === "Escape") {
                this.close();
                document.removeEventListener("keydown", handleKeyDown);
            }
        };
        document.addEventListener("keydown", handleKeyDown);
    }

    /**
     * Executes the simulation against the real Stage 9 backend API.
     */
    static async executeSimulation() {
        const resultsEl = document.getElementById("simResultsArea");
        const runBtn = document.getElementById("runSimulationBtn");
        if (!resultsEl) return;

        // Extract form parameters
        const scenarioType = document.getElementById("simScenarioType")?.value || "RAINFALL_CHANGE";
        const isAllDistricts = document.getElementById("simScope")?.value === "ALL";
        const districtName = document.getElementById("simDistrictInput")?.value || "Sitamarhi";

        const rainfallChange = Number(document.getElementById("rainRangeInput")?.value) || 0;
        const hazardIntensityChange = Number(document.getElementById("hazardRangeInput")?.value) || 0;
        const populationExposureChange = Number(document.getElementById("popRangeInput")?.value) || 0;

        // UI Loading State
        if (runBtn) {
            runBtn.disabled = true;
            runBtn.innerHTML = `<span>⏳ Simulating...</span>`;
        }
        resultsEl.innerHTML = LoadingState.render({ message: "Executing in-memory disaster scenario simulation via Stage 9 Engine..." });

        try {
            const res = await scenarioService.runWhatIfSimulation({
                scenarioType,
                rainfallChange,
                hazardIntensityChange,
                populationExposureChange,
                districtName,
                isAllDistricts
            });

            if (!res.success || !res.data) {
                resultsEl.innerHTML = ErrorState.render({
                    title: "Simulation Failed",
                    message: res.error || "Backend scenario evaluation returned an unexpected error."
                });
                return;
            }

            this.lastResult = res.data;
            resultsEl.innerHTML = this.renderSimulationResults(res.data, isAllDistricts);

        } catch (err) {
            resultsEl.innerHTML = ErrorState.render({
                title: "Network / Service Error",
                message: err.message || "Failed to communicate with Disaster Simulation service."
            });
        } finally {
            if (runBtn) {
                runBtn.disabled = false;
                runBtn.innerHTML = `<span>⚡ Run Simulation</span>`;
            }
        }
    }

    /**
     * Formats and renders the Before vs After comparison results.
     */
    static renderSimulationResults(data, isAllDistricts) {
        const districtComp = (data.districtComparisons && data.districtComparisons.length > 0)
            ? data.districtComparisons[0]
            : null;

        // Render Before vs After Cards Grid
        let singleDistrictHtml = "";
        if (districtComp) {
            const riskDelta = districtComp.deltaRiskScore100 || 0;
            const riskDirection = districtComp.riskDirection || "UNCHANGED";
            const isRedZoneEntered = districtComp.redZoneTransitionType === "ENTERED_RED_ZONE";
            const isPriorityEscalated = districtComp.priorityEscalated;

            singleDistrictHtml = `
                <div class="sim-comparison-header">
                    <div class="sim-comparison-meta">
                        <span class="sim-target-tag">📍 District: <strong>${districtComp.districtName}</strong></span>
                        <span class="sim-scenario-tag">⚙️ ${data.scenarioName || "Simulated Scenario"}</span>
                    </div>
                </div>

                <div class="sim-comparison-grid">
                    <!-- 1. RISK SHIFT -->
                    <div class="sim-compare-card ${riskDirection === 'INCREASED' ? 'shifted-up' : riskDirection === 'DECREASED' ? 'shifted-down' : ''}">
                        <div class="sim-compare-title">1. DISASTER RISK SCORE</div>
                        <div class="sim-before-after-row">
                            <div class="sim-val-block">
                                <span class="sim-val-label">BEFORE</span>
                                <span class="sim-val-num">${(districtComp.baselineRiskScore100 || 0).toFixed(1)}%</span>
                                <span class="sim-val-sub">${districtComp.baselineRiskTier || 'NORMAL'}</span>
                            </div>
                            <div class="sim-arrow">&rarr;</div>
                            <div class="sim-val-block">
                                <span class="sim-val-label">AFTER</span>
                                <span class="sim-val-num ${riskDelta > 0 ? 'text-critical' : riskDelta < 0 ? 'text-safe' : ''}">${(districtComp.simulatedRiskScore100 || 0).toFixed(1)}%</span>
                                <span class="sim-val-sub">${districtComp.simulatedRiskTier || 'NORMAL'}</span>
                            </div>
                        </div>
                        <div class="sim-delta-badge ${riskDelta > 0 ? 'badge-critical' : riskDelta < 0 ? 'badge-safe' : 'badge-neutral'}">
                            ${riskDelta >= 0 ? "+" : ""}${riskDelta.toFixed(1)} pts (${riskDirection})
                        </div>
                    </div>

                    <!-- 2. RED ZONE TRANSITION -->
                    <div class="sim-compare-card ${isRedZoneEntered ? 'shifted-up' : ''}">
                        <div class="sim-compare-title">2. RED ZONE STATUS</div>
                        <div class="sim-before-after-row">
                            <div class="sim-val-block">
                                <span class="sim-val-label">BEFORE</span>
                                <span class="sim-val-num">${districtComp.baselineRedZone ? 'RED ZONE' : 'NO'}</span>
                                <span class="sim-val-sub">${districtComp.baselineRedZone ? 'Active Danger' : 'Safe Area'}</span>
                            </div>
                            <div class="sim-arrow">&rarr;</div>
                            <div class="sim-val-block">
                                <span class="sim-val-label">AFTER</span>
                                <span class="sim-val-num ${districtComp.simulatedRedZone ? 'text-critical' : 'text-safe'}">${districtComp.simulatedRedZone ? 'RED ZONE' : 'NO'}</span>
                                <span class="sim-val-sub">${districtComp.simulatedRedZone ? 'High Exposure' : 'Safe Area'}</span>
                            </div>
                        </div>
                        <div class="sim-delta-badge ${isRedZoneEntered ? 'badge-critical' : 'badge-neutral'}">
                            ${(districtComp.redZoneTransitionType || 'UNCHANGED').replace(/_/g, ' ')}
                        </div>
                    </div>

                    <!-- 3. EVACUATION PRIORITY -->
                    <div class="sim-compare-card ${isPriorityEscalated ? 'shifted-up' : ''}">
                        <div class="sim-compare-title">3. EVACUATION PRIORITY</div>
                        <div class="sim-before-after-row">
                            <div class="sim-val-block">
                                <span class="sim-val-label">BEFORE</span>
                                <span class="sim-val-num">${districtComp.baselinePriorityLevel || 'MONITORING'}</span>
                                <span class="sim-val-sub">Score: ${(districtComp.baselinePriorityScore || 0).toFixed(3)}</span>
                            </div>
                            <div class="sim-arrow">&rarr;</div>
                            <div class="sim-val-block">
                                <span class="sim-val-label">AFTER</span>
                                <span class="sim-val-num ${isPriorityEscalated ? 'text-critical' : ''}">${districtComp.simulatedPriorityLevel || 'MONITORING'}</span>
                                <span class="sim-val-sub">Score: ${(districtComp.simulatedPriorityScore || 0).toFixed(3)}</span>
                            </div>
                        </div>
                        <div class="sim-delta-badge ${isPriorityEscalated ? 'badge-critical' : 'badge-neutral'}">
                            ${isPriorityEscalated ? 'ESCALATED' : districtComp.priorityShiftDirection || 'UNCHANGED'}
                        </div>
                    </div>

                    <!-- 4. RELOCATION & SHELTER -->
                    <div class="sim-compare-card">
                        <div class="sim-compare-title">4. RELOCATION & SHELTER</div>
                        <div class="sim-card-help">People requiring relocation and shelter fit</div>
                        
                        <div class="sim-reloc-metrics">
                            <div class="sim-reloc-row">
                                <span class="sim-reloc-label">Demand:</span>
                                <span class="sim-reloc-val">${(districtComp.baselineVulnerablePopulation || 0).toLocaleString()} &rarr; ${(districtComp.simulatedVulnerablePopulation || 0).toLocaleString()}</span>
                            </div>
                            <div class="sim-reloc-row">
                                <span class="sim-reloc-label">Allocated:</span>
                                <span class="sim-reloc-val">${(districtComp.baselineAllocatedPopulation || 0).toLocaleString()} &rarr; ${(districtComp.simulatedAllocatedPopulation || 0).toLocaleString()}</span>
                            </div>
                            <div class="sim-reloc-row">
                                <span class="sim-reloc-label">Unallocated:</span>
                                <span class="sim-reloc-val ${(districtComp.simulatedUnallocatedPopulation || 0) > 0 ? 'text-critical' : ''}">${(districtComp.baselineUnallocatedPopulation || 0).toLocaleString()} &rarr; ${(districtComp.simulatedUnallocatedPopulation || 0).toLocaleString()}</span>
                            </div>
                        </div>

                        <div class="sim-delta-badge badge-neutral" style="font-size: 0.65rem; white-space: normal; line-height: 1.2;">
                            Status: ${(districtComp.simulatedRelocationStatus || 'UNALLOCATED').replace(/_/g, ' ')}
                        </div>
                    </div>
                </div>
            `;
        }

        // Render All-District Aggregates & Table if multi-district
        let allDistrictsHtml = "";
        if (isAllDistricts && data.districtComparisons && data.districtComparisons.length > 1) {
            const tableRows = data.districtComparisons.map((d, idx) => {
                const deltaRisk = d.deltaRiskScore100 || 0;
                const rzChange = d.redZoneTransitionType === "ENTERED_RED_ZONE";
                const prioChange = d.priorityEscalated;

                return `
                    <tr class="${(deltaRisk > 0 || rzChange || prioChange) ? 'row-highlight' : ''}">
                        <td class="td-district"><strong>${d.districtName}</strong></td>
                        <td>${(d.baselineRiskScore100 || 0).toFixed(1)}% &rarr; <strong>${(d.simulatedRiskScore100 || 0).toFixed(1)}%</strong> <span class="delta-inline ${deltaRisk > 0 ? 'text-critical' : deltaRisk < 0 ? 'text-safe' : ''}">(${deltaRisk >= 0 ? '+' : ''}${deltaRisk.toFixed(1)})</span></td>
                        <td>${d.baselineRedZone ? 'RED' : 'NO'} &rarr; <span class="${d.simulatedRedZone ? 'text-critical font-bold' : ''}">${d.simulatedRedZone ? 'RED' : 'NO'}</span></td>
                        <td>${d.baselinePriorityLevel || 'MONITORING'} &rarr; <span class="${prioChange ? 'text-critical font-bold' : ''}">${d.simulatedPriorityLevel || 'MONITORING'}</span></td>
                        <td>${(d.simulatedVulnerablePopulation || 0).toLocaleString()} (Deficit: ${(d.simulatedUnallocatedPopulation || 0).toLocaleString()})</td>
                    </tr>
                `;
            }).join("");

            allDistrictsHtml = `
                <div class="sim-all-districts-section">
                    <div class="sim-aggregate-ribbon">
                        <div class="sim-ribbon-stat"><span>Districts Evaluated:</span> <strong>${data.totalDistrictsEvaluated || data.districtComparisons.length}</strong></div>
                        <div class="sim-ribbon-stat"><span>Risk Increased:</span> <strong class="text-critical">${data.districtsWithIncreasedRiskCount || 0}</strong></div>
                        <div class="sim-ribbon-stat"><span>Red Zones:</span> <strong>${data.baselineRedZoneCount || 0} &rarr; ${data.simulatedRedZoneCount || 0} (${data.netRedZoneChange >= 0 ? '+' : ''}${data.netRedZoneChange || 0})</strong></div>
                        <div class="sim-ribbon-stat"><span>Immediate Priorities:</span> <strong>${data.baselineImmediatePriorityCount || 0} &rarr; ${data.simulatedImmediatePriorityCount || 0}</strong></div>
                    </div>

                    <div class="sim-table-wrapper">
                        <table class="sim-districts-table">
                            <thead>
                                <tr>
                                    <th>District</th>
                                    <th>Risk Shift</th>
                                    <th>Red Zone</th>
                                    <th>Priority Shift</th>
                                    <th>Vulnerable & Deficit</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${tableRows}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        }

        return `
            <div class="sim-results-wrapper">
                ${singleDistrictHtml}
                ${allDistrictsHtml}
            </div>
        `;
    }
}
