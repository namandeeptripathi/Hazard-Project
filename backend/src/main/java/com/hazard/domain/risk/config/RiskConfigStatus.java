package com.hazard.domain.risk.config;

/**
 * Status lifecycle for Risk Configuration Profiles.
 */
public enum RiskConfigStatus {
    ACTIVE("Currently active production configuration for risk scoring"),
    INACTIVE("Validated configuration currently not active"),
    DRAFT("Draft configuration undergoing review"),
    ARCHIVED("Historical configuration retained for audit and reproduction");

    private final String description;

    RiskConfigStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
