package com.hazard.service.exposure;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.dto.exposure.PopulationExposureConfigDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized, configurable properties bean for Stage 4.1 Population Exposure.
 * Prevents hardcoding of classification thresholds and population density defaults.
 */
@Component
@ConfigurationProperties(prefix = "hazard.exposure.population")
public class PopulationExposureConfig {

    // Exposure percentage thresholds
    private double lowThresholdPercent = 15.0;
    private double moderateThresholdPercent = 40.0;
    private double highThresholdPercent = 70.0;

    // Default geographic buffer radius in meters for point hazard events (e.g. 5 km = 5000m)
    private double defaultHazardBufferMeters = 5000.0;

    // Default residential density for unpopulated residential polygon footprints (persons per hectare)
    private double residentialDensityPersonsPerHectare = 350.0;

    // Fallback baseline population counts for settlement types lacking explicit census numbers
    private Map<String, Long> settlementArchetypeDefaults = new LinkedHashMap<>();

    public PopulationExposureConfig() {
        // Default archetypes based on standard Indian Census / UN-Habitat settlement tiers
        settlementArchetypeDefaults.put("city", 100000L);
        settlementArchetypeDefaults.put("town", 20000L);
        settlementArchetypeDefaults.put("suburb", 8000L);
        settlementArchetypeDefaults.put("village", 2500L);
        settlementArchetypeDefaults.put("hamlet", 350L);
        settlementArchetypeDefaults.put("isolated_dwelling", 50L);
        settlementArchetypeDefaults.put("residential", 1200L);
    }

    public double getLowThresholdPercent() {
        return lowThresholdPercent;
    }

    public void setLowThresholdPercent(double lowThresholdPercent) {
        this.lowThresholdPercent = lowThresholdPercent;
    }

    public double getModerateThresholdPercent() {
        return moderateThresholdPercent;
    }

    public void setModerateThresholdPercent(double moderateThresholdPercent) {
        this.moderateThresholdPercent = moderateThresholdPercent;
    }

    public double getHighThresholdPercent() {
        return highThresholdPercent;
    }

    public void setHighThresholdPercent(double highThresholdPercent) {
        this.highThresholdPercent = highThresholdPercent;
    }

    public double getDefaultHazardBufferMeters() {
        return defaultHazardBufferMeters;
    }

    public void setDefaultHazardBufferMeters(double defaultHazardBufferMeters) {
        this.defaultHazardBufferMeters = defaultHazardBufferMeters;
    }

    public double getResidentialDensityPersonsPerHectare() {
        return residentialDensityPersonsPerHectare;
    }

    public void setResidentialDensityPersonsPerHectare(double residentialDensityPersonsPerHectare) {
        this.residentialDensityPersonsPerHectare = residentialDensityPersonsPerHectare;
    }

    public Map<String, Long> getSettlementArchetypeDefaults() {
        return settlementArchetypeDefaults;
    }

    public void setSettlementArchetypeDefaults(Map<String, Long> settlementArchetypeDefaults) {
        this.settlementArchetypeDefaults = settlementArchetypeDefaults;
    }

    /**
     * Determines the ExposureCategory dynamically from the configured thresholds.
     */
    public ExposureCategory classifyExposurePercentage(Double percentage) {
        if (percentage == null || percentage <= 0.0) {
            return ExposureCategory.LOW;
        }
        double clamped = Math.min(100.0, Math.max(0.0, percentage));
        if (clamped < lowThresholdPercent) {
            return ExposureCategory.LOW;
        } else if (clamped < moderateThresholdPercent) {
            return ExposureCategory.MODERATE;
        } else if (clamped < highThresholdPercent) {
            return ExposureCategory.HIGH;
        } else {
            return ExposureCategory.VERY_HIGH;
        }
    }

    /**
     * Resolves fallback population for a settlement type if explicit count is unavailable.
     */
    public long resolveArchetypePopulation(String placeType, String landuse) {
        if (placeType != null) {
            String key = placeType.trim().toLowerCase();
            if (settlementArchetypeDefaults.containsKey(key)) {
                return settlementArchetypeDefaults.get(key);
            }
        }
        if (landuse != null) {
            String key = landuse.trim().toLowerCase();
            if (settlementArchetypeDefaults.containsKey(key)) {
                return settlementArchetypeDefaults.get(key);
            }
        }
        return settlementArchetypeDefaults.getOrDefault("residential", 1200L);
    }

    /**
     * Exports active configuration as a DTO.
     */
    public PopulationExposureConfigDto toDto() {
        PopulationExposureConfigDto dto = new PopulationExposureConfigDto();
        dto.setLowThresholdPercent(lowThresholdPercent);
        dto.setModerateThresholdPercent(moderateThresholdPercent);
        dto.setHighThresholdPercent(highThresholdPercent);
        dto.setDefaultHazardBufferMeters(defaultHazardBufferMeters);
        dto.setResidentialDensityPersonsPerHectare(residentialDensityPersonsPerHectare);
        dto.setSettlementArchetypeDefaultPopulations(new LinkedHashMap<>(settlementArchetypeDefaults));

        Map<String, String> colors = new LinkedHashMap<>();
        for (ExposureCategory cat : ExposureCategory.values()) {
            colors.put(cat.name(), cat.getColorHex());
        }
        dto.setCategoryColorCodes(colors);
        return dto;
    }
}
