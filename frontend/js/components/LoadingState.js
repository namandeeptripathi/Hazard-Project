/**
 * Stage 8A — LoadingState Component
 *
 * Provides a clean skeleton/spinner state during asynchronous data fetching.
 */
export class LoadingState {
    /**
     * Renders LoadingState HTML markup.
     *
     * @param {object} props
     * @param {string} props.message - Descriptive loading message
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const message = props.message || "Loading decision intelligence data...";

        return `
            <div class="loading-state" role="status" aria-live="polite">
                <div class="loading-spinner" aria-hidden="true"></div>
                <p class="loading-message">${message}</p>
            </div>
        `;
    }
}
