package com.hazard.service.risk.config;

import com.hazard.domain.risk.config.RiskConfigAuditEntry;
import com.hazard.domain.risk.config.RiskConfigStatus;
import com.hazard.domain.risk.config.RiskConfigurationProfile;
import com.hazard.dto.risk.config.RiskConfigDiffDto;
import com.hazard.dto.risk.config.RiskConfigurationRequestDto;
import com.hazard.dto.risk.config.RiskConfigurationResponseDto;
import com.hazard.exception.HazardNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Core Service for Stage 4.8 — Configurable Risk Weights.
 * Handles validation, versioning, single-active transactional management, immutability,
 * version diffs, presets, and audit logging.
 */
@Service
@Transactional
public class RiskConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(RiskConfigurationService.class);

    private final RiskConfigurationRepository repository;
    private final AtomicInteger versionSequence = new AtomicInteger(2);

    public RiskConfigurationService(RiskConfigurationRepository repository) {
        this.repository = repository;
    }

    // =========================================================================
    // 1. ACTIVE CONFIGURATION & FALLBACK
    // =========================================================================

    public RiskConfigurationProfile getActiveConfiguration() {
        return repository.findActive().orElseGet(() -> {
            log.warn("⚠️ No active risk configuration found in repository! Falling back to safe default baseline.");
            RiskConfigurationProfile fallback = RiskConfigurationProfile.createDefaultBaseline();
            repository.save(fallback);
            return fallback;
        });
    }

    public RiskConfigurationResponseDto getActiveConfigurationDto() {
        return toResponseDto(getActiveConfiguration());
    }

    // =========================================================================
    // 2. RETRIEVAL
    // =========================================================================

    public RiskConfigurationProfile getConfigurationById(String configId) {
        if (configId == null || configId.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration ID cannot be null or empty");
        }
        return repository.findById(configId.trim())
                .orElseThrow(() -> new HazardNotFoundException("Risk configuration not found: " + configId));
    }

    public RiskConfigurationResponseDto getConfigurationDtoById(String configId) {
        return toResponseDto(getConfigurationById(configId));
    }

    public List<RiskConfigurationResponseDto> getAllConfigurations() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(RiskConfigurationProfile::getCreatedAt).reversed())
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<RiskConfigurationResponseDto> getPresets() {
        return repository.findAll().stream()
                .filter(RiskConfigurationProfile::isPreset)
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // 3. CREATION & VERSIONING
    // =========================================================================

    public synchronized RiskConfigurationResponseDto createConfiguration(RiskConfigurationRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Configuration request cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration name is required");
        }

        RiskConfigurationProfile profile = new RiskConfigurationProfile();
        int nextId = versionSequence.getAndIncrement();
        profile.setConfigId("risk-v" + nextId);
        profile.setVersion(nextId + ".0");
        profile.setName(request.getName().trim());
        profile.setDescription(request.getDescription() != null ? request.getDescription().trim() : "Custom risk configuration");
        profile.setAuthor(request.getAuthor() != null ? request.getAuthor().trim() : "OPERATOR");

        // Top-Level Weights
        if (request.getHazardWeight() != null) profile.setHazardWeight(request.getHazardWeight());
        if (request.getExposureWeight() != null) profile.setExposureWeight(request.getExposureWeight());
        if (request.getVulnerabilityWeight() != null) profile.setVulnerabilityWeight(request.getVulnerabilityWeight());
        if (request.getHistoricalWeight() != null) profile.setHistoricalWeight(request.getHistoricalWeight());

        // Exposure Sub-Weights
        if (request.getPopulationWeight() != null) profile.setPopulationWeight(request.getPopulationWeight());
        if (request.getSettlementWeight() != null) profile.setSettlementWeight(request.getSettlementWeight());
        if (request.getInfrastructureWeight() != null) profile.setInfrastructureWeight(request.getInfrastructureWeight());

        // Thresholds
        if (request.getThresholdLowMax() != null) profile.setThresholdLowMax(request.getThresholdLowMax());
        if (request.getThresholdModerateMax() != null) profile.setThresholdModerateMax(request.getThresholdModerateMax());
        if (request.getThresholdHighMax() != null) profile.setThresholdHighMax(request.getThresholdHighMax());
        if (request.getThresholdVeryHighMax() != null) profile.setThresholdVeryHighMax(request.getThresholdVeryHighMax());
        if (request.getThresholdCriticalMin() != null) profile.setThresholdCriticalMin(request.getThresholdCriticalMin());

        if (request.getMinimumComponents() != null) profile.setMinimumComponents(request.getMinimumComponents());

        validateProfile(profile);

        if (Boolean.TRUE.equals(request.getActivateImmediately())) {
            deactivateAllInternal();
            profile.setStatus(RiskConfigStatus.ACTIVE);
            profile.setImmutable(true);
        } else {
            profile.setStatus(RiskConfigStatus.INACTIVE);
        }

        repository.save(profile);
        repository.addAuditLog(new RiskConfigAuditEntry(
                "AUDIT-" + UUID.randomUUID().toString().substring(0, 8),
                profile.getConfigId(),
                profile.getVersion(),
                "CREATED",
                "Created new risk configuration profile: " + profile.getName(),
                profile.getAuthor()
        ));

        log.info("✅ Created Risk Configuration Profile: {} (Version {})", profile.getConfigId(), profile.getVersion());
        return toResponseDto(profile);
    }

    public synchronized RiskConfigurationResponseDto updateConfiguration(String configId, RiskConfigurationRequestDto request) {
        RiskConfigurationProfile existing = getConfigurationById(configId);

        // If existing is immutable or active, branch into a new version
        if (existing.isImmutable() || existing.getStatus() == RiskConfigStatus.ACTIVE) {
            log.info("ℹ️ Profile {} is immutable/active. Branching into a new version profile.", configId);
            return createConfiguration(request);
        }

        // Otherwise update draft profile
        if (request.getName() != null) existing.setName(request.getName().trim());
        if (request.getDescription() != null) existing.setDescription(request.getDescription().trim());
        if (request.getHazardWeight() != null) existing.setHazardWeight(request.getHazardWeight());
        if (request.getExposureWeight() != null) existing.setExposureWeight(request.getExposureWeight());
        if (request.getVulnerabilityWeight() != null) existing.setVulnerabilityWeight(request.getVulnerabilityWeight());
        if (request.getHistoricalWeight() != null) existing.setHistoricalWeight(request.getHistoricalWeight());

        if (request.getPopulationWeight() != null) existing.setPopulationWeight(request.getPopulationWeight());
        if (request.getSettlementWeight() != null) existing.setSettlementWeight(request.getSettlementWeight());
        if (request.getInfrastructureWeight() != null) existing.setInfrastructureWeight(request.getInfrastructureWeight());

        if (request.getThresholdLowMax() != null) existing.setThresholdLowMax(request.getThresholdLowMax());
        if (request.getThresholdModerateMax() != null) existing.setThresholdModerateMax(request.getThresholdModerateMax());
        if (request.getThresholdHighMax() != null) existing.setThresholdHighMax(request.getThresholdHighMax());
        if (request.getThresholdVeryHighMax() != null) existing.setThresholdVeryHighMax(request.getThresholdVeryHighMax());
        if (request.getThresholdCriticalMin() != null) existing.setThresholdCriticalMin(request.getThresholdCriticalMin());

        existing.setUpdatedAt(LocalDateTime.now());
        validateProfile(existing);
        repository.save(existing);

        return toResponseDto(existing);
    }

    // =========================================================================
    // 4. ACTIVATION & DEACTIVATION
    // =========================================================================

    public synchronized RiskConfigurationResponseDto activateConfiguration(String configId, String actor) {
        RiskConfigurationProfile profile = getConfigurationById(configId);

        validateProfile(profile);
        deactivateAllInternal();

        profile.setStatus(RiskConfigStatus.ACTIVE);
        profile.setImmutable(true); // Once activated, parameter set is frozen
        profile.setUpdatedAt(LocalDateTime.now());
        repository.save(profile);

        repository.addAuditLog(new RiskConfigAuditEntry(
                "AUDIT-" + UUID.randomUUID().toString().substring(0, 8),
                profile.getConfigId(),
                profile.getVersion(),
                "ACTIVATED",
                "Activated configuration profile as primary production model: " + profile.getName(),
                actor != null ? actor : "OPERATOR"
        ));

        log.info("✅ Activated Risk Configuration: {} (Version {})", profile.getConfigId(), profile.getVersion());
        return toResponseDto(profile);
    }

    public synchronized RiskConfigurationResponseDto deactivateConfiguration(String configId, String actor) {
        RiskConfigurationProfile profile = getConfigurationById(configId);
        profile.setStatus(RiskConfigStatus.INACTIVE);
        profile.setUpdatedAt(LocalDateTime.now());
        repository.save(profile);

        // Fallback guarantee: ensure at least default baseline is active
        if (repository.findActive().isEmpty()) {
            activateConfiguration("risk-v1", "SYSTEM-FALLBACK");
        }

        repository.addAuditLog(new RiskConfigAuditEntry(
                "AUDIT-" + UUID.randomUUID().toString().substring(0, 8),
                profile.getConfigId(),
                profile.getVersion(),
                "DEACTIVATED",
                "Deactivated configuration profile: " + profile.getName(),
                actor != null ? actor : "OPERATOR"
        ));

        return toResponseDto(profile);
    }

    private void deactivateAllInternal() {
        for (RiskConfigurationProfile p : repository.findAll()) {
            if (p.getStatus() == RiskConfigStatus.ACTIVE) {
                p.setStatus(RiskConfigStatus.INACTIVE);
                p.setUpdatedAt(LocalDateTime.now());
                repository.save(p);
            }
        }
    }

    // =========================================================================
    // 5. CONFIGURATION DIFF
    // =========================================================================

    public RiskConfigDiffDto compareConfigurations(String baseConfigId, String targetConfigId) {
        RiskConfigurationProfile base = getConfigurationById(baseConfigId);
        RiskConfigurationProfile target = getConfigurationById(targetConfigId);

        RiskConfigDiffDto diff = new RiskConfigDiffDto();
        diff.setBaseConfigId(base.getConfigId());
        diff.setBaseVersion(base.getVersion());
        diff.setTargetConfigId(target.getConfigId());
        diff.setTargetVersion(target.getVersion());

        // Metadata diffs
        if (!base.getName().equals(target.getName())) {
            diff.getMetadataChanges().put("name", String.format("'%s' -> '%s'", base.getName(), target.getName()));
        }

        // Top-Level Weight Diffs
        diff.getTopLevelWeightDiffs().put("HAZARD", new Double[]{base.getHazardWeight(), target.getHazardWeight(), round4(target.getHazardWeight() - base.getHazardWeight())});
        diff.getTopLevelWeightDiffs().put("EXPOSURE", new Double[]{base.getExposureWeight(), target.getExposureWeight(), round4(target.getExposureWeight() - base.getExposureWeight())});
        diff.getTopLevelWeightDiffs().put("VULNERABILITY", new Double[]{base.getVulnerabilityWeight(), target.getVulnerabilityWeight(), round4(target.getVulnerabilityWeight() - base.getVulnerabilityWeight())});
        diff.getTopLevelWeightDiffs().put("HISTORICAL", new Double[]{base.getHistoricalWeight(), target.getHistoricalWeight(), round4(target.getHistoricalWeight() - base.getHistoricalWeight())});

        // Exposure Sub-Weight Diffs
        diff.getExposureWeightDiffs().put("POPULATION", new Double[]{base.getPopulationWeight(), target.getPopulationWeight(), round4(target.getPopulationWeight() - base.getPopulationWeight())});
        diff.getExposureWeightDiffs().put("SETTLEMENT", new Double[]{base.getSettlementWeight(), target.getSettlementWeight(), round4(target.getSettlementWeight() - base.getSettlementWeight())});
        diff.getExposureWeightDiffs().put("INFRASTRUCTURE", new Double[]{base.getInfrastructureWeight(), target.getInfrastructureWeight(), round4(target.getInfrastructureWeight() - base.getInfrastructureWeight())});

        return diff;
    }

    public List<RiskConfigAuditEntry> getAuditLogs() {
        return repository.findAuditLogs();
    }

    // =========================================================================
    // 6. VALIDATION & MAPPING
    // =========================================================================

    public void validateProfile(RiskConfigurationProfile p) {
        if (p == null) throw new IllegalArgumentException("Profile cannot be null");

        // 1. Weight Non-Negativity & Finiteness
        double[] topWeights = {p.getHazardWeight(), p.getExposureWeight(), p.getVulnerabilityWeight(), p.getHistoricalWeight()};
        for (double w : topWeights) {
            if (Double.isNaN(w) || Double.isInfinite(w) || w < 0.0) {
                throw new IllegalArgumentException("Top-level risk weight must be finite and non-negative: " + w);
            }
        }
        if (p.getTopLevelWeightSum() <= 0.0) {
            throw new IllegalArgumentException("Total top-level risk weight sum must be strictly greater than 0.0");
        }

        // 2. Exposure Sub-Weight Non-Negativity
        double[] expWeights = {p.getPopulationWeight(), p.getSettlementWeight(), p.getInfrastructureWeight()};
        for (double w : expWeights) {
            if (Double.isNaN(w) || Double.isInfinite(w) || w < 0.0) {
                throw new IllegalArgumentException("Exposure sub-weight must be finite and non-negative: " + w);
            }
        }
        if (p.getExposureSubWeightSum() <= 0.0) {
            throw new IllegalArgumentException("Total exposure sub-weight sum must be strictly greater than 0.0");
        }

        // 3. Threshold Monotonic Ordering
        if (p.getThresholdLowMax() <= 0.0 ||
                p.getThresholdLowMax() >= p.getThresholdModerateMax() ||
                p.getThresholdModerateMax() >= p.getThresholdHighMax() ||
                p.getThresholdHighMax() >= p.getThresholdVeryHighMax() ||
                p.getThresholdVeryHighMax() > 1.0) {
            throw new IllegalArgumentException("Risk tier thresholds must be strictly monotonic in range (0.0, 1.0]");
        }

        // 4. Minimum Components
        if (p.getMinimumComponents() < 1 || p.getMinimumComponents() > 4) {
            throw new IllegalArgumentException("Minimum required components must be between 1 and 4");
        }
    }

    public RiskConfigurationResponseDto toResponseDto(RiskConfigurationProfile p) {
        RiskConfigurationResponseDto dto = new RiskConfigurationResponseDto();
        dto.setConfigId(p.getConfigId());
        dto.setVersion(p.getVersion());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setStatus(p.getStatus());

        double topSum = Math.max(0.0001, p.getTopLevelWeightSum());
        Map<String, Double> cfgTop = new LinkedHashMap<>();
        Map<String, Double> normTop = new LinkedHashMap<>();
        cfgTop.put("HAZARD", round4(p.getHazardWeight()));
        cfgTop.put("EXPOSURE", round4(p.getExposureWeight()));
        cfgTop.put("VULNERABILITY", round4(p.getVulnerabilityWeight()));
        cfgTop.put("HISTORICAL", round4(p.getHistoricalWeight()));

        normTop.put("HAZARD", round4(p.getHazardWeight() / topSum));
        normTop.put("EXPOSURE", round4(p.getExposureWeight() / topSum));
        normTop.put("VULNERABILITY", round4(p.getVulnerabilityWeight() / topSum));
        normTop.put("HISTORICAL", round4(p.getHistoricalWeight() / topSum));

        dto.setConfiguredTopLevelWeights(cfgTop);
        dto.setNormalizedTopLevelWeights(normTop);

        double expSum = Math.max(0.0001, p.getExposureSubWeightSum());
        Map<String, Double> cfgExp = new LinkedHashMap<>();
        Map<String, Double> normExp = new LinkedHashMap<>();
        cfgExp.put("POPULATION", round4(p.getPopulationWeight()));
        cfgExp.put("SETTLEMENT", round4(p.getSettlementWeight()));
        cfgExp.put("INFRASTRUCTURE", round4(p.getInfrastructureWeight()));

        normExp.put("POPULATION", round4(p.getPopulationWeight() / expSum));
        normExp.put("SETTLEMENT", round4(p.getSettlementWeight() / expSum));
        normExp.put("INFRASTRUCTURE", round4(p.getInfrastructureWeight() / expSum));

        dto.setConfiguredExposureWeights(cfgExp);
        dto.setNormalizedExposureWeights(normExp);

        Map<String, Double> th = new LinkedHashMap<>();
        th.put("LOW_MAX", p.getThresholdLowMax());
        th.put("MODERATE_MAX", p.getThresholdModerateMax());
        th.put("HIGH_MAX", p.getThresholdHighMax());
        th.put("VERY_HIGH_MAX", p.getThresholdVeryHighMax());
        th.put("CRITICAL_MIN", p.getThresholdCriticalMin());
        dto.setThresholds(th);

        dto.setMinimumComponents(p.getMinimumComponents());
        dto.setImmutable(p.isImmutable());
        dto.setPreset(p.isPreset());
        dto.setAuthor(p.getAuthor());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        return dto;
    }

    public static double round4(double val) {
        return Math.round(val * 10000.0) / 10000.0;
    }
}
