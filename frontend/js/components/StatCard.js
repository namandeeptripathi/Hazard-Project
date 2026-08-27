/**
 * Stage 8A — StatCard Component
 *
 * KPI metric display with status color borders, icon, and optional trend/subtitle.
 */
export class StatCard {
    /**
     * Renders a StatCard HTML string.
     *
     * @param {object} props
     * @param {string} props.label - KPI metric title (e.g. "VULNERABLE POPULATION")
     * @param {string|number} props.value - Metric value (e.g. "94,293" or "0.88")
     * @param {string} props.icon - Emoji or icon symbol (e.g. "👥")
     * @param {string} props.status - Semantic status ("critical", "warning", "safe", "moderate", "info", "neutral")
     * @param {string} props.subtitle - Descriptive subtitle or trend (e.g. "+12% in red zone")
     * @param {string} props.id - HTML element id
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const label = props.label || props.title || "METRIC";
        const value = props.value !== undefined ? props.value : "--";
        const icon = props.icon || "";
        const status = props.status || props.trendType || "info";
        const subtitle = props.subtitle || props.trendText || "";
        const idAttr = props.id ? `id="${props.id}"` : "";

        return `
            <div class="stat-card stat-${status}" ${idAttr}>
                <div class="stat-header">
                    <span class="stat-label">${label}</span>
                    ${icon ? `<span class="stat-icon" aria-hidden="true">${icon}</span>` : ""}
                </div>
                <div class="stat-value">${value}</div>
                ${subtitle ? `<div class="stat-subtitle">${subtitle}</div>` : ""}
            </div>
        `;
    }
}
