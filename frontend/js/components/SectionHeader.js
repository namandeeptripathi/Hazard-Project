/**
 * Stage 8A — SectionHeader Component
 *
 * Provides a clean title and optional subtitle/action for sections within views.
 */
export class SectionHeader {
    /**
     * Renders SectionHeader HTML markup.
     *
     * @param {object} props
     * @param {string} props.title - Section title
     * @param {string} props.subtitle - Optional descriptive subtitle
     * @param {string} props.actionHtml - Optional action HTML (e.g. view all link)
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const title = props.title || "";
        const subtitle = props.subtitle || "";
        const actionHtml = props.actionHtml || "";

        return `
            <div class="section-header">
                <div>
                    <h2 class="section-title">${title}</h2>
                    ${subtitle ? `<p class="section-subtitle">${subtitle}</p>` : ""}
                </div>
                ${actionHtml ? `<div>${actionHtml}</div>` : ""}
            </div>
        `;
    }
}
