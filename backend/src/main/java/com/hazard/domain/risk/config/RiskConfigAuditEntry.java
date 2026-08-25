package com.hazard.domain.risk.config;

import java.time.LocalDateTime;

/**
 * Audit log entry recording configuration lifecycle events (create, update, activate, deactivate).
 */
public class RiskConfigAuditEntry {

    private String auditId;
    private String configId;
    private String version;
    private String action; // CREATED, ACTIVATED, DEACTIVATED, BRANCHED, SCENARIO_SIMULATED
    private String details;
    private String actor;
    private LocalDateTime timestamp;

    public RiskConfigAuditEntry() {
        this.timestamp = LocalDateTime.now();
    }

    public RiskConfigAuditEntry(String auditId, String configId, String version, String action, String details, String actor) {
        this.auditId = auditId;
        this.configId = configId;
        this.version = version;
        this.action = action;
        this.details = details;
        this.actor = actor != null ? actor : "OPERATOR";
        this.timestamp = LocalDateTime.now();
    }

    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
