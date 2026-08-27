/**
 * Stage 8A — Card Component
 *
 * Provides a standardized structural card with optional header, footer, and elevation.
 */
export class Card {
    /**
     * Renders a Card HTML string.
     *
     * @param {object} props
     * @param {string} props.title - Optional card title
     * @param {string} props.icon - Optional header icon
     * @param {string} props.headerAction - Optional header right action
     * @param {string} props.bodyHtml - Content markup
     * @param {string} props.footerHtml - Optional footer markup
     * @param {boolean} props.elevated - Whether to use elevated background
     * @param {string} props.className - Extra CSS classes
     * @param {string} props.id - HTML element id
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const title = props.title || "";
        const icon = props.icon ? `<span class="card-title-icon" aria-hidden="true">${props.icon}</span>` : "";
        const headerAction = props.headerAction || "";
        const bodyHtml = props.bodyHtml || "";
        const footerHtml = props.footerHtml || "";
        const elevatedClass = props.elevated ? "card-elevated" : "";
        const extraClass = props.className || "";
        const idAttr = props.id ? `id="${props.id}"` : "";

        const headerSection = title || headerAction
            ? `
                <div class="card-header">
                    <h3 class="card-title">${icon}${title}</h3>
                    ${headerAction ? `<div>${headerAction}</div>` : ""}
                </div>
            `
            : "";

        const footerSection = footerHtml
            ? `<div class="card-footer">${footerHtml}</div>`
            : "";

        return `
            <div class="card ${elevatedClass} ${extraClass}" ${idAttr}>
                ${headerSection}
                <div class="card-body">
                    ${bodyHtml}
                </div>
                ${footerSection}
            </div>
        `;
    }
}
