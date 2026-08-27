/**
 * Stage 8A — EmptyState Component
 *
 * Displays when queries or lists return zero results, with an optional call to action.
 */
export class EmptyState {
    /**
     * Renders EmptyState HTML markup.
     *
     * @param {object} props
     * @param {string} props.title - Empty state title
     * @param {string} props.description - Explanatory text
     * @param {string} props.icon - Emoji or icon
     * @param {string} props.actionHtml - Optional button markup
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const title = props.title || "No Records Found";
        const description = props.description || "There are no records matching your current filter criteria.";
        const icon = props.icon || "📂";
        const actionHtml = props.actionHtml || "";

        return `
            <div class="empty-state">
                <div class="empty-state-icon" aria-hidden="true">${icon}</div>
                <h3 class="empty-state-title">${title}</h3>
                <p class="empty-state-description">${description}</p>
                ${actionHtml ? `<div style="margin-top: var(--space-2);">${actionHtml}</div>` : ""}
            </div>
        `;
    }
}
