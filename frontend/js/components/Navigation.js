/**
 * Stage 8A — Navigation (Sidebar) Component
 *
 * Core 4-view navigation menu: Overview, Map, Safe Sites, Relocation.
 */
export class Navigation {
    static NAV_ITEMS = [
        { id: "overview", label: "Overview", icon: "📊", path: "#/overview", badge: null },
        { id: "map", label: "Map", icon: "🗺️", path: "#/map", badge: "GIS" },
        { id: "safe-sites", label: "Safe Sites", icon: "🛡️", path: "#/safe-sites", badge: null },
        { id: "relocation", label: "Relocation", icon: "🚚", path: "#/relocation", badge: "Stage 7" }
    ];

    /**
     * Renders the Sidebar HTML string.
     *
     * @param {object} props
     * @param {string} props.currentPath - Currently active route (e.g. "/overview")
     * @param {string} props.currentRegion - Currently active region name (e.g. "Sitamarhi, Bihar")
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const currentPath = props.currentPath || "/overview";
        const region = props.currentRegion || "Sitamarhi, Bihar";

        const navLinksHtml = this.NAV_ITEMS.map(item => {
            const isActive = currentPath === item.path.replace("#", "") ||
                             (item.id === "overview" && currentPath === "/");
            const activeClass = isActive ? "active" : "";
            const ariaCurrent = isActive ? 'aria-current="page"' : "";

            const badgeHtml = item.badge
                ? `<span class="nav-badge">${item.badge}</span>`
                : "";

            return `
                <li>
                    <a href="${item.path}" class="nav-link ${activeClass}" ${ariaCurrent} id="nav-${item.id}">
                        <span class="nav-icon" aria-hidden="true">${item.icon}</span>
                        <span class="nav-label">${item.label}</span>
                        ${badgeHtml}
                    </a>
                </li>
            `;
        }).join("");

        return `
            <aside class="app-sidebar" role="navigation" aria-label="Main Navigation">
                <ul class="sidebar-nav-list">
                    ${navLinksHtml}
                </ul>

                <div class="sidebar-footer">
                    <div class="sidebar-region-badge">
                        <div class="sidebar-region-label">Active Jurisdiction</div>
                        <div class="sidebar-region-name">${region}</div>
                    </div>
                </div>
            </aside>
        `;
    }

    /**
     * Updates active class in sidebar DOM based on route.
     *
     * @param {string} routePath
     */
    static updateActiveLink(routePath) {
        if (typeof document === "undefined") return;
        const links = document.querySelectorAll(".nav-link");
        links.forEach(link => {
            const href = link.getAttribute("href") || "";
            const itemPath = href.replace("#", "");
            const isActive = itemPath === routePath || (itemPath === "/overview" && routePath === "/");

            if (isActive) {
                link.classList.add("active");
                link.setAttribute("aria-current", "page");
            } else {
                link.classList.remove("active");
                link.removeAttribute("aria-current");
            }
        });
    }
}
