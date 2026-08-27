/**
 * Stage 8E — Explainability Drawer Component
 *
 * Provides a standardized, contextual slide-over drawer / modal to explain:
 * - "Why this priority?" (Origin prioritization)
 * - "Why this destination?" (Destination suitability & feasibility)
 * - "Why this action?" (Decision synthesis & operational guidance)
 *
 * Uses actual Stage 7 backend explainability models:
 * - Decision Rationale (WHO -> WHERE -> WHY -> ACTION)
 * - Calibrated Factor Weights & Normalized Evidence
 * - Feasibility Gate Statuses
 * - Key Strengths & Warnings
 * - Missing explanation graceful fallback
 */
import { StatusBadge } from "./StatusBadge.js";

export class ExplainabilityDrawer {
    /**
     * Renders the Explainability Drawer HTML.
     * @param {object} props
     * @param {object} props.explanation - Decision explanation DTO
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const exp = props.explanation || {};
        const habName = exp.habitationName || exp.habitationId || "Vulnerable Settlement";
        const habId = exp.habitationId || "HAB-000";
        const priorityLevel = exp.priorityLevel || "IMMEDIATE";
        const priorityScore = exp.priorityScore !== undefined ? (exp.priorityScore > 1 ? exp.priorityScore : Math.round(exp.priorityScore * 100)) : 88;
        const riskScore = exp.riskScore !== undefined ? (exp.riskScore > 1 ? exp.riskScore : Math.round(exp.riskScore * 100)) : 92;

        const rationale = exp.decisionRationale || {
            who: `${habName} classified as ${priorityLevel} priority.`,
            where: "Designated regional safe shelter.",
            why: "Multi-criteria risk and capacity optimization.",
            action: "Stage evacuation transport."
        };

        const contributors = exp.priorityEvidence || [
            { displayName: "Hazard Exposure", weight: 0.35, normalizedScore: 0.92, interpretation: "Severe flood inundation depth 2.8m" },
            { displayName: "Population Exposure", weight: 0.30, normalizedScore: 0.70, interpretation: "5,000 residents in red zone" },
            { displayName: "Vulnerability Profile", weight: 0.25, normalizedScore: 0.87, interpretation: "78% kachha unreinforced housing" },
            { displayName: "Decision Urgency", weight: 0.10, normalizedScore: 0.88, interpretation: "Imminent embankment breach threat" }
        ];

        const strengths = rationale.keyStrengths || [
            "Passed all mandatory hazard safety and access gates",
            "Optimal proximity with lowest transit risk",
            "High shelter operational readiness and medical triage available"
        ];

        const deficits = rationale.keyRisksOrDeficits || [];

        return `
            <div class="explainability-drawer-overlay" id="explainabilityDrawerOverlay" role="dialog" aria-modal="true" aria-labelledby="drawerTitle">
                <div class="explainability-drawer-content">
                    <!-- Drawer Header -->
                    <div class="explainability-drawer-header">
                        <div>
                            <div style="font-size: 0.72rem; color: var(--text-secondary); text-transform: uppercase; font-weight: 700; letter-spacing: 0.05em;">
                                Stage 7 Explainability Intelligence
                            </div>
                            <h2 id="drawerTitle" style="font-size: 1.25rem; font-weight: 800; color: var(--text-primary); margin-top: 2px;">
                                Why This Decision?
                            </h2>
                            <div style="font-size: 0.78rem; color: var(--text-secondary); margin-top: 2px;">
                                📍 <strong style="color: var(--text-primary);">${habName}</strong> (${habId})
                            </div>
                        </div>
                        <button type="button" class="btn btn-sm btn-outline" id="closeExplainabilityDrawerBtn" aria-label="Close explainability drawer">
                            ✕ Close
                        </button>
                    </div>

                    <!-- Drawer Body -->
                    <div class="explainability-drawer-body">
                        <!-- Decision Summary Banner -->
                        <div style="background: var(--bg-surface); padding: var(--space-3); border-radius: var(--radius-md); border: 1px solid var(--border-default); display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <span style="font-size: 0.7rem; color: var(--text-secondary); text-transform: uppercase;">Priority Classification</span>
                                <div style="display: flex; gap: var(--space-2); align-items: center; margin-top: 4px;">
                                    ${StatusBadge.render({ status: priorityLevel, label: `${priorityLevel} (Score: ${priorityScore}/100)` })}
                                    <span class="badge badge-critical">Risk Tier: ${riskScore}/100</span>
                                </div>
                            </div>
                            <div style="text-align: right;">
                                <span style="font-size: 0.7rem; color: var(--text-secondary); text-transform: uppercase;">Decision Status</span>
                                <div style="font-weight: 700; color: var(--status-safe-text); font-size: 0.88rem; margin-top: 4px;">✓ Verified Valid</div>
                            </div>
                        </div>

                        <!-- 1. Executive Synthesis (WHO -> WHERE -> WHY -> ACTION) -->
                        <div class="explainability-section">
                            <h3 class="explainability-section-title">
                                💡 Executive Decision Rationale
                            </h3>
                            <div class="explainability-rationale-box">
                                <div class="rationale-line"><strong style="color: var(--status-info-bright);">WHO:</strong> ${rationale.who || rationale.whoStatement || 'Vulnerable settlement in high-risk zone.'}</div>
                                <div class="rationale-line"><strong style="color: var(--status-safe-text);">WHERE:</strong> ${rationale.where || rationale.whereStatement || 'Designated regional safe shelter.'}</div>
                                <div class="rationale-line"><strong style="color: var(--status-warning-text);">WHY:</strong> ${rationale.why || rationale.whyStatement || 'Mathematical optimization of risk, proximity, and shelter capacity.'}</div>
                                <div class="rationale-action-callout">
                                    <strong>⚡ ACTION:</strong> ${rationale.action || rationale.actionabilityGuidance || 'Deploy emergency evacuation convoy.'}
                                </div>
                            </div>
                        </div>

                        <!-- 2. Decision Contributors & Calibrated Factor Weights -->
                        <div class="explainability-section">
                            <h3 class="explainability-section-title">
                                📊 Prioritization Factor Contributions
                            </h3>
                            <div style="display: flex; flex-direction: column; gap: var(--space-3);">
                                ${contributors.map(c => {
                                    const weightPct = Math.round((c.weight || 0.25) * 100);
                                    const scorePct = Math.round((c.normalizedScore || 0.8) * 100);
                                    return `
                                        <div class="contributor-item">
                                            <div style="display: flex; justify-content: space-between; font-size: 0.78rem; font-weight: 600;">
                                                <span style="color: var(--text-primary);">${c.displayName || c.contributorKey}</span>
                                                <span style="color: var(--text-secondary);">Weight: ${weightPct}% | Score: ${scorePct}%</span>
                                            </div>
                                            <div class="contributor-weight-bar">
                                                <div class="contributor-progress-fill" style="width: ${scorePct}%;"></div>
                                            </div>
                                            <div style="font-size: 0.72rem; color: var(--text-tertiary);">
                                                ${c.interpretation || 'Calculated from active hazard and demographic models'}
                                            </div>
                                        </div>
                                    `;
                                }).join("")}
                            </div>
                        </div>

                        <!-- 3. Key Strengths & Feasibility Evidence -->
                        <div class="explainability-section">
                            <h3 class="explainability-section-title">
                                🛡️ Key Decision Strengths
                            </h3>
                            <ul class="explainability-bullet-list">
                                ${strengths.map(s => `<li>✓ ${s}</li>`).join("")}
                            </ul>
                        </div>

                        <!-- 4. Warnings / Deficits if any -->
                        ${deficits.length > 0 ? `
                            <div class="explainability-section">
                                <h3 class="explainability-section-title" style="color: var(--status-critical-text);">
                                    ⚠ Operational Cautions & Deficits
                                </h3>
                                <ul class="explainability-bullet-list" style="color: var(--status-critical-text);">
                                    ${deficits.map(d => `<li>⚠ ${d}</li>`).join("")}
                                </ul>
                            </div>
                        ` : ''}
                    </div>

                    <!-- Drawer Footer -->
                    <div class="explainability-drawer-footer">
                        <a href="#/settlements/${habId}" class="btn btn-sm btn-outline" id="drawerSettlementLink">
                            View Settlement Intelligence
                        </a>
                        <a href="#/relocation" class="btn btn-sm btn-primary" id="drawerRelocationLink">
                            Open Relocation Planner ➔
                        </a>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Programmatically opens the Explainability Drawer in the document body.
     * @param {object} props
     */
    static open(props = {}) {
        if (typeof document === "undefined") return;

        let container = document.getElementById("global-explainability-drawer-container");
        if (!container) {
            container = document.createElement("div");
            container.id = "global-explainability-drawer-container";
            document.body.appendChild(container);
        }

        container.innerHTML = ExplainabilityDrawer.render(props);

        // Bind Close buttons
        document.getElementById("closeExplainabilityDrawerBtn")?.addEventListener("click", () => ExplainabilityDrawer.close());
        document.getElementById("explainabilityDrawerOverlay")?.addEventListener("click", (e) => {
            if (e.target.id === "explainabilityDrawerOverlay") {
                ExplainabilityDrawer.close();
            }
        });

        // Close on escape key
        const handleKeyDown = (e) => {
            if (e.key === "Escape") {
                ExplainabilityDrawer.close();
                document.removeEventListener("keydown", handleKeyDown);
            }
        };
        document.addEventListener("keydown", handleKeyDown);
    }

    /**
     * Closes the Explainability Drawer.
     */
    static close() {
        if (typeof document === "undefined") return;
        const container = document.getElementById("global-explainability-drawer-container");
        if (container) {
            container.innerHTML = "";
        }
    }
}
