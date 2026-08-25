package com.hazard.dto.facade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * System Health and Readiness Status DTO for Hazard Intelligence Subsystem.
 */
public class HazardSystemHealthDto {

    private String status = "UP";
    private String subsystem = "Hazard Intelligence (Stage 3)";
    private String database = "PostgreSQL 17.11 / PostGIS 3.6.4 (hazard_db)";
    private String canonicalCrs = "EPSG:4326 (WGS 84)";
    private long stage2BaseRecordCount = 159005;
    private List<String> activeCapabilities = new ArrayList<>();
    private LocalDateTime timestamp;

    public HazardSystemHealthDto() {
        this.timestamp = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getCanonicalCrs() {
        return canonicalCrs;
    }

    public void setCanonicalCrs(String canonicalCrs) {
        this.canonicalCrs = canonicalCrs;
    }

    public long getStage2BaseRecordCount() {
        return stage2BaseRecordCount;
    }

    public void setStage2BaseRecordCount(long stage2BaseRecordCount) {
        this.stage2BaseRecordCount = stage2BaseRecordCount;
    }

    public List<String> getActiveCapabilities() {
        return activeCapabilities;
    }

    public void setActiveCapabilities(List<String> activeCapabilities) {
        this.activeCapabilities = activeCapabilities != null ? activeCapabilities : new ArrayList<>();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
