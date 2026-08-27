/**
 * Stage 8A — Tabs Component
 *
 * Accessible tab bar component supporting keyboard navigation and panel switching.
 */
export class Tabs {
    /**
     * Renders a Tab Bar HTML string.
     *
     * @param {object} props
     * @param {string} props.id - Component container ID
     * @param {Array<{ id: string, label: string, badge?: string, active?: boolean }>} props.tabs - Tab definitions
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const id = props.id || "tab-group";
        const tabs = props.tabs || [];

        const tabButtons = tabs.map((tab, idx) => {
            const isActive = tab.active || (idx === 0 && !tabs.some(t => t.active));
            const activeClass = isActive ? "active" : "";
            const ariaSelected = isActive ? "true" : "false";

            const badgeHtml = tab.badge ? ` <span class="badge badge-neutral">${tab.badge}</span>` : "";

            return `
                <button type="button" 
                        class="tab-button ${activeClass}" 
                        id="${id}-tab-${tab.id}" 
                        role="tab" 
                        aria-selected="${ariaSelected}" 
                        aria-controls="${id}-panel-${tab.id}"
                        data-tab-target="${tab.id}">
                    ${tab.label}${badgeHtml}
                </button>
            `;
        }).join("");

        return `
            <div class="tab-nav" role="tablist" id="${id}-tablist">
                ${tabButtons}
            </div>
        `;
    }
}
