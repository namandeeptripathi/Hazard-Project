/**
 * Stage 8A — Button Component
 *
 * Standardized button with variants (primary, secondary, outline, danger) and icon support.
 */
export class Button {
    /**
     * Renders a Button HTML string.
     *
     * @param {object} props
     * @param {string} props.label - Button text
     * @param {string} props.icon - Optional leading icon
     * @param {string} props.variant - "primary", "secondary", "outline", "danger"
     * @param {string} props.size - "sm", "md", "lg"
     * @param {string} props.id - HTML element id
     * @param {string} props.onclick - Inline handler string
     * @param {boolean} props.disabled - Disabled state
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const label = props.label || "Button";
        const icon = props.icon ? `<span class="btn-icon-leading" aria-hidden="true">${props.icon}</span>` : "";
        const variant = props.variant || "primary";
        const sizeClass = props.size === "sm" ? "btn-sm" : props.size === "lg" ? "btn-lg" : "";
        const idAttr = props.id ? `id="${props.id}"` : "";
        const onclickAttr = props.onclick ? `onclick="${props.onclick}"` : "";
        const disabledAttr = props.disabled ? "disabled" : "";

        return `
            <button type="button" class="btn btn-${variant} ${sizeClass}" ${idAttr} ${onclickAttr} ${disabledAttr}>
                ${icon}
                <span>${label}</span>
            </button>
        `;
    }
}
