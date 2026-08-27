/**
 * Stage 8A — PageHeader Component
 *
 * Provides a standardized header block for views containing breadcrumbs, title, subtitle, and action buttons.
 */
export class PageHeader {
    /**
     * Renders the PageHeader HTML string.
     *
     * @param {object} props
     * @param {string} props.title - Main page title
     * @param {string} props.subtitle - Descriptive subtitle
     * @param {Array<{ label: string, path?: string }>} props.breadcrumbs - Breadcrumb trail
     * @param {string} props.actionsHtml - Optional actions HTML string (e.g. buttons)
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const title = props.title || "Dashboard";
        const subtitle = props.subtitle || "";
        const breadcrumbs = props.breadcrumbs || [{ label: "Dashboard", path: "#/overview" }];
        const actionsHtml = props.actionsHtml || "";

        const breadcrumbsHtml = breadcrumbs.map((b, idx) => {
            const isLast = idx === breadcrumbs.length - 1;
            if (isLast) {
                return `<span class="page-breadcrumb-item active" aria-current="location">${b.label}</span>`;
            }
            return `<a href="${b.path || '#'}" class="page-breadcrumb-item">${b.label}</a> <span class="breadcrumb-separator" aria-hidden="true">/</span>`;
        }).join(" ");

        return `
            <div class="page-header">
                <div class="page-header-main">
                    <nav class="page-breadcrumbs" aria-label="Breadcrumb">
                        ${breadcrumbsHtml}
                    </nav>
                    <h1 class="page-title">${title}</h1>
                    ${subtitle ? `<p class="page-subtitle">${subtitle}</p>` : ""}
                </div>
                ${actionsHtml ? `<div class="page-header-actions">${actionsHtml}</div>` : ""}
            </div>
        `;
    }
}
