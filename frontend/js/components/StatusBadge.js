/**
 * Stage 8A — StatusBadge Component
 *
 * Provides standardized semantic status pill badges mapped to backend domain constants.
 */
export class StatusBadge {
    /**
     * Maps backend status domain terms to badge variant CSS class and label.
     */
    static getVariant(term = "") {
        const normalized = String(term).toUpperCase().trim();

        // Priority Levels (Stage 7A)
        if (normalized === "IMMEDIATE" || normalized === "CRITICAL" || normalized === "RED_ZONE") {
            return { variant: "critical", label: "Immediate Priority", icon: "🔴" };
        }
        if (normalized === "SHORT_TERM" || normalized === "HIGH" || normalized === "HIGH_RISK") {
            return { variant: "warning", label: "Short-Term", icon: "🟠" };
        }
        if (normalized === "MEDIUM_TERM" || normalized === "MODERATE") {
            return { variant: "moderate", label: "Medium-Term", icon: "🟣" };
        }
        if (normalized === "MONITORING" || normalized === "LOW" || normalized === "NEUTRAL") {
            return { variant: "neutral", label: "Monitoring", icon: "⚪" };
        }

        // Recommendation & Allocation Statuses (Stage 6 & 7B)
        if (normalized === "RECOMMENDED" || normalized === "ALLOCATED" || normalized === "SAFE" || normalized === "HIGHLY_SUITABLE") {
            return { variant: "safe", label: normalized.replace(/_/g, " "), icon: "🟢" };
        }
        if (normalized === "SUITABLE" || normalized === "PARTIALLY_ALLOCATED") {
            return { variant: "info", label: normalized.replace(/_/g, " "), icon: "🔵" };
        }
        if (normalized === "MARGINAL" || normalized === "CAPACITY_DEFICIT" || normalized === "PARTIAL_DEFICIT") {
            return { variant: "warning", label: normalized.replace(/_/g, " "), icon: "🟡" };
        }
        if (normalized === "NO_FEASIBLE_DESTINATION" || normalized === "UNSUITABLE" || normalized === "UNALLOCATED_NO_SAFE_SITE" || normalized === "AT_RISK") {
            return { variant: "critical", label: normalized.replace(/_/g, " "), icon: "⛔" };
        }

        return { variant: "neutral", label: term, icon: "" };
    }

    /**
     * Renders a StatusBadge HTML string.
     *
     * @param {object|string} props - String term or options object
     * @param {string} props.status - Status term
     * @param {string} props.label - Optional custom label
     * @param {string} props.variant - Force variant ("critical", "warning", "moderate", "safe", "info", "neutral")
     * @param {boolean} props.showIcon - Whether to display emoji/icon
     * @returns {string} HTML markup
     */
    static render(props) {
        if (typeof props === "string") {
            props = { status: props };
        }

        const status = props?.status || "UNKNOWN";
        const meta = this.getVariant(status);

        const variant = props?.variant || meta.variant;
        const label = props?.label || meta.label || status;
        const icon = (props?.showIcon !== false && meta.icon) ? `<span aria-hidden="true">${meta.icon}</span> ` : "";

        return `<span class="badge badge-${variant}">${icon}${label}</span>`;
    }
}
