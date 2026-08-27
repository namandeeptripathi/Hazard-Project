/**
 * Stage 8A — Header Component
 *
 * Top navigation bar featuring institutional emblem, platform title, operational status badge, and clock.
 */
export class Header {
    /**
     * Renders the Header HTML string.
     *
     * @param {object} props
     * @param {string} props.platformTitle - Main platform title
     * @param {string} props.platformSubtitle - Subtitle / authority tag
     * @param {string} props.operationalMode - Active mode ("LIVE EMERGENCY", "SIMULATION", "ROUTINE MONITORING")
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const title = props.platformTitle || "DISASTER DECISION SUPPORT";
        const subtitle = props.platformSubtitle || "NDRF & SDMA Relocation Intelligence System";
        const mode = props.operationalMode || "ACTIVE MONITORING";

        return `
            <header class="app-header" role="banner">
                <div class="header-brand">
                    <div class="header-emblem" aria-hidden="true">ND</div>
                    <div class="header-title-group">
                        <div class="header-platform-title">${title}</div>
                        <div class="header-platform-subtitle">${subtitle}</div>
                    </div>
                </div>

                <div class="header-actions">
                    <div class="header-status-pill" title="System Operational Status">
                        <span class="header-pulse-dot" aria-hidden="true"></span>
                        <span>${mode}</span>
                    </div>
                    <div id="liveClock" class="header-clock" aria-label="System Time">--:--:-- UTC</div>
                </div>
            </header>
        `;
    }

    /**
     * Initializes live clock updates.
     */
    static initClock() {
        const clockEl = document.getElementById("liveClock");
        if (!clockEl) return;

        const updateTime = () => {
            const now = new Date();
            const timeStr = now.toTimeString().split(" ")[0] + " IST";
            clockEl.textContent = timeStr;
        };

        updateTime();
        setInterval(updateTime, 1000);
    }
}
