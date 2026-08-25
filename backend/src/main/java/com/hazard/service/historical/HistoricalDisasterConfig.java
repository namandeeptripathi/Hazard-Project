package com.hazard.service.historical;

import com.hazard.domain.hazard.HazardType;
import com.hazard.domain.historical.HistoricalTimeWindow;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Configuration bean for Stage 4.6 — Historical Disaster Intelligence.
 */
@Component
public class HistoricalDisasterConfig {

    private final HistoricalTimeWindow defaultTimeWindow = HistoricalTimeWindow.ALL_HISTORY;
    private final List<HazardType> supportedHazardTypes = List.of(HazardType.FLOOD, HazardType.EXTREME_RAINFALL);
    private final double highSeverityThresholdDfo = 1.5;
    private final double highSeverityThresholdWeatherMm = 25.0;
    private final double defaultHistoricalPeriodYears = 25.0; // 2000-2025 baseline
    private final Map<String, Double> hotspotWeightComponents = new LinkedHashMap<>();
    private final String calculationVersion = "v1.0";

    public HistoricalDisasterConfig() {
        hotspotWeightComponents.put("EVENT_FREQUENCY", 0.40);
        hotspotWeightComponents.put("HIGH_SEVERITY_RATIO", 0.30);
        hotspotWeightComponents.put("RECURRENCE_INTENSITY", 0.30);
    }

    public HistoricalTimeWindow getDefaultTimeWindow() {
        return defaultTimeWindow;
    }

    public List<HazardType> getSupportedHazardTypes() {
        return Collections.unmodifiableList(supportedHazardTypes);
    }

    public double getHighSeverityThresholdDfo() {
        return highSeverityThresholdDfo;
    }

    public double getHighSeverityThresholdWeatherMm() {
        return highSeverityThresholdWeatherMm;
    }

    public double getDefaultHistoricalPeriodYears() {
        return defaultHistoricalPeriodYears;
    }

    public Map<String, Double> getHotspotWeightComponents() {
        return Collections.unmodifiableMap(hotspotWeightComponents);
    }

    public String getCalculationVersion() {
        return calculationVersion;
    }
}
