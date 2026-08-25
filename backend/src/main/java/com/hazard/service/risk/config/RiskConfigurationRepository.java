package com.hazard.service.risk.config;

import com.hazard.domain.risk.config.RiskConfigAuditEntry;
import com.hazard.domain.risk.config.RiskConfigStatus;
import com.hazard.domain.risk.config.RiskConfigurationProfile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Repository managing in-memory and persistent Risk Configuration Profiles and Audit Logs.
 */
@Repository
public class RiskConfigurationRepository {

    private final Map<String, RiskConfigurationProfile> profiles = new ConcurrentHashMap<>();
    private final List<RiskConfigAuditEntry> auditLogs = new CopyOnWriteArrayList<>();

    public RiskConfigurationRepository() {
        initializePresets();
    }

    private void initializePresets() {
        // 1. Default Baseline (Active)
        RiskConfigurationProfile defaultBaseline = RiskConfigurationProfile.createDefaultBaseline();
        profiles.put(defaultBaseline.getConfigId(), defaultBaseline);

        // 2. Hazard Focused Preset
        RiskConfigurationProfile hazardPreset = new RiskConfigurationProfile();
        hazardPreset.setConfigId("risk-preset-hazard");
        hazardPreset.setVersion("1.0");
        hazardPreset.setName("Hazard Focused Preset");
        hazardPreset.setDescription("Emphasizes acute hazard intensity and footprint over socio-economic vulnerability (50% H, 25% E, 15% V, 10% T)");
        hazardPreset.setStatus(RiskConfigStatus.INACTIVE);
        hazardPreset.setHazardWeight(0.50);
        hazardPreset.setExposureWeight(0.25);
        hazardPreset.setVulnerabilityWeight(0.15);
        hazardPreset.setHistoricalWeight(0.10);
        hazardPreset.setPreset(true);
        hazardPreset.setImmutable(true);
        hazardPreset.setAuthor("NDMA-PRESET");
        profiles.put(hazardPreset.getConfigId(), hazardPreset);

        // 3. Population Focused Preset
        RiskConfigurationProfile popPreset = new RiskConfigurationProfile();
        popPreset.setConfigId("risk-preset-pop");
        popPreset.setVersion("1.0");
        popPreset.setName("Population Focused Preset");
        popPreset.setDescription("Prioritizes dense human population settlement exposure (25% H, 45% E, 20% V, 10% T | Pop Exposure: 60%)");
        popPreset.setStatus(RiskConfigStatus.INACTIVE);
        popPreset.setHazardWeight(0.25);
        popPreset.setExposureWeight(0.45);
        popPreset.setVulnerabilityWeight(0.20);
        popPreset.setHistoricalWeight(0.10);
        popPreset.setPopulationWeight(0.60);
        popPreset.setSettlementWeight(0.20);
        popPreset.setInfrastructureWeight(0.20);
        popPreset.setPreset(true);
        popPreset.setImmutable(true);
        popPreset.setAuthor("NDMA-PRESET");
        profiles.put(popPreset.getConfigId(), popPreset);

        // 4. Infrastructure Focused Preset
        RiskConfigurationProfile infraPreset = new RiskConfigurationProfile();
        infraPreset.setConfigId("risk-preset-infra");
        infraPreset.setVersion("1.0");
        infraPreset.setName("Infrastructure Focused Preset");
        infraPreset.setDescription("Emphasizes critical lifeline and hydraulic defense asset vulnerability (25% H, 40% E, 25% V, 10% T | Infra Exposure: 55%)");
        infraPreset.setStatus(RiskConfigStatus.INACTIVE);
        infraPreset.setHazardWeight(0.25);
        infraPreset.setExposureWeight(0.40);
        infraPreset.setVulnerabilityWeight(0.25);
        infraPreset.setHistoricalWeight(0.10);
        infraPreset.setPopulationWeight(0.25);
        infraPreset.setSettlementWeight(0.20);
        infraPreset.setInfrastructureWeight(0.55);
        infraPreset.setPreset(true);
        infraPreset.setImmutable(true);
        infraPreset.setAuthor("NDMA-PRESET");
        profiles.put(infraPreset.getConfigId(), infraPreset);

        // Initial Audit Log
        auditLogs.add(new RiskConfigAuditEntry("AUDIT-INIT", defaultBaseline.getConfigId(), "1.0", "INITIALIZED",
                "System initialized with standard default baseline configuration profile", "SYSTEM"));
    }

    public RiskConfigurationProfile save(RiskConfigurationProfile profile) {
        if (profile == null || profile.getConfigId() == null) {
            throw new IllegalArgumentException("Cannot save null profile or profile with null configId");
        }
        profiles.put(profile.getConfigId(), profile);
        return profile;
    }

    public Optional<RiskConfigurationProfile> findById(String configId) {
        if (configId == null) return Optional.empty();
        return Optional.ofNullable(profiles.get(configId.trim()));
    }

    public Optional<RiskConfigurationProfile> findActive() {
        return profiles.values().stream()
                .filter(p -> p.getStatus() == RiskConfigStatus.ACTIVE)
                .findFirst();
    }

    public List<RiskConfigurationProfile> findAll() {
        return new ArrayList<>(profiles.values());
    }

    public List<RiskConfigAuditEntry> findAuditLogs() {
        return new ArrayList<>(auditLogs);
    }

    public void addAuditLog(RiskConfigAuditEntry entry) {
        if (entry != null) {
            auditLogs.add(entry);
        }
    }
}
