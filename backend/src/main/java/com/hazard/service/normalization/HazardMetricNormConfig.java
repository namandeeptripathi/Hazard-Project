package com.hazard.service.normalization;

import com.hazard.domain.hazard.NormalizationDirection;
import com.hazard.domain.hazard.NormalizationMethod;

import java.util.*;

/**
 * Normalization Metric Configuration and Reference Range Registry.
 * Defines standard min-max reference boundaries, units, directions,
 * and scientific rationale for each eligible hazard indicator.
 */
public class HazardMetricNormConfig {

    private final String metricName;
    private final String metricLabel;
    private final String units;
    private final double referenceMin;
    private final double referenceMax;
    private final NormalizationMethod method;
    private final NormalizationDirection direction;
    private final String referenceRationale;

    public HazardMetricNormConfig(String metricName, String metricLabel, String units,
                                  double referenceMin, double referenceMax,
                                  NormalizationMethod method, NormalizationDirection direction,
                                  String referenceRationale) {
        this.metricName = metricName;
        this.metricLabel = metricLabel;
        this.units = units;
        this.referenceMin = referenceMin;
        this.referenceMax = referenceMax;
        this.method = method;
        this.direction = direction;
        this.referenceRationale = referenceRationale;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getMetricLabel() {
        return metricLabel;
    }

    public String getUnits() {
        return units;
    }

    public double getReferenceMin() {
        return referenceMin;
    }

    public double getReferenceMax() {
        return referenceMax;
    }

    public NormalizationMethod getMethod() {
        return method;
    }

    public NormalizationDirection getDirection() {
        return direction;
    }

    public String getReferenceRationale() {
        return referenceRationale;
    }

    // =========================================================================
    // STANDARD REGISTRY OF NORMALIZATION CONFIGURATIONS
    // =========================================================================

    public static final Map<String, HazardMetricNormConfig> REGISTRY = new LinkedHashMap<>();

    static {
        // 1. Meteorological / Rainfall Metrics
        register(new HazardMetricNormConfig(
                "HOURLY_PRECIPITATION_MM", "Hourly Precipitation", "mm/hr",
                0.0, 50.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "IMD standard heavy to very heavy hourly precipitation ceiling (0 to 50 mm/hr)"
        ));

        register(new HazardMetricNormConfig(
                "DAILY_RAINFALL_MM", "Daily Total Rainfall", "mm/day",
                0.0, 150.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "IMD standard very heavy 24h rainfall ceiling (0 to 150 mm/day)"
        ));

        register(new HazardMetricNormConfig(
                "PEAK_HOURLY_RAINFALL_MM", "Peak Hourly Rainfall", "mm/hr",
                0.0, 50.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Maximum single-hour precipitation intensity recorded within the diurnal cycle"
        ));

        register(new HazardMetricNormConfig(
                "ROLLING_3H_RAINFALL_MM", "3-Hour Cumulative Rainfall", "mm",
                0.0, 60.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Flash-flood trigger window accumulation ceiling (0 to 60 mm)"
        ));

        register(new HazardMetricNormConfig(
                "ROLLING_6H_RAINFALL_MM", "6-Hour Cumulative Rainfall", "mm",
                0.0, 90.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Short-duration storm accumulation ceiling (0 to 90 mm)"
        ));

        register(new HazardMetricNormConfig(
                "ROLLING_12H_RAINFALL_MM", "12-Hour Cumulative Rainfall", "mm",
                0.0, 120.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Sub-daily intense precipitation window ceiling (0 to 120 mm)"
        ));

        register(new HazardMetricNormConfig(
                "ROLLING_24H_RAINFALL_MM", "24-Hour Cumulative Rainfall", "mm",
                0.0, 150.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Full-day continuous rolling rainfall accumulation ceiling (0 to 150 mm)"
        ));

        // 2. Historical Flood Event Metrics (DFO)
        register(new HazardMetricNormConfig(
                "FLOOD_DURATION_DAYS", "Flood Event Duration", "days",
                1.0, 90.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Dartmouth Flood Observatory monsoon flood duration range (1 to 90 days)"
        ));

        register(new HazardMetricNormConfig(
                "FLOOD_AFFECTED_AREA_SQKM", "Flood Affected Area", "km²",
                0.0, 500000.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Regional Gangetic basin inundated footprint area ceiling (0 to 500,000 km²)"
        ));

        register(new HazardMetricNormConfig(
                "FLOOD_DISPLACEMENT_DENSITY", "Displacement Density", "people/km²",
                0.0, 25.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Displaced population density per square kilometer of affected flood zone"
        ));

        register(new HazardMetricNormConfig(
                "FLOOD_SEVERITY_INDEX", "DFO Flood Severity Class", "class",
                1.0, 2.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Dartmouth Flood Observatory qualitative severity class scale (1.0 to 2.0)"
        ));

        register(new HazardMetricNormConfig(
                "FLOOD_MAGNITUDE_INDEX", "DFO Flood Magnitude", "log10(dur*sev*area)",
                4.0, 9.0,
                NormalizationMethod.MIN_MAX, NormalizationDirection.INCREASING,
                "Dartmouth Flood Observatory calculated logarithmic flood magnitude index"
        ));
    }

    private static void register(HazardMetricNormConfig config) {
        REGISTRY.put(config.getMetricName(), config);
    }

    public static Optional<HazardMetricNormConfig> getConfig(String metricName) {
        if (metricName == null) return Optional.empty();
        return Optional.ofNullable(REGISTRY.get(metricName.trim().toUpperCase()));
    }

    public static List<HazardMetricNormConfig> getAllConfigs() {
        return new ArrayList<>(REGISTRY.values());
    }
}
