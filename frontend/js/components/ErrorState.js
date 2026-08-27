/**
 * Stage 8A — ErrorState Component
 *
 * Displays error notifications with optional retry button.
 */
export class ErrorState {
    /**
     * Renders ErrorState HTML markup.
     *
     * @param {object} props
     * @param {string} props.title - Error title
     * @param {string} props.message - Descriptive error message
     * @param {string} props.retryFnName - Optional global or inline function name to call for retry
     * @returns {string} HTML markup
     */
    static render(props = {}) {
        const title = props.title || "Failed to Load Data";
        const message = props.message || "An unexpected error occurred while communicating with the decision service.";
        const retryFn = props.retryFnName || "";

        const retryButton = retryFn
            ? `<button type="button" class="btn btn-sm btn-outline" style="margin-top: var(--space-2); align-self: flex-start;" onclick="${retryFn}">🔄 Retry</button>`
            : "";

        return `
            <div class="error-state" role="alert">
                <div class="error-state-icon" aria-hidden="true">⚠️</div>
                <div class="error-state-content">
                    <div class="error-state-title">${title}</div>
                    <div class="error-state-message">${message}</div>
                    ${retryButton}
                </div>
            </div>
        `;
    }
}
