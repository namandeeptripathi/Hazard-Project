package com.hazard.service.historical;

import com.hazard.domain.hazard.HazardType;
import com.hazard.dto.historical.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure calculation engine for Stage 4.6 — Historical Disaster Intelligence.
 * Computes empirical event frequency, recurrence gaps, severity statistics,
 * seasonal temporal patterns, and descriptive historical hotspot indices.
 */
@Component
public class HistoricalDisasterEngine {

    private final HistoricalDisasterConfig config;

    public HistoricalDisasterEngine(HistoricalDisasterConfig config) {
        this.config = config;
    }

    /**
     * Filters historical disaster events by date range, hazard type, and minimum severity threshold.
     */
    public List<HistoricalEventDto> filterEvents(List<HistoricalEventDto> events,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 HazardType hazardType,
                                                 Double minSeverity) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }

        return events.stream()
                .filter(e -> {
                    if (hazardType != null && e.getHazardType() != hazardType) {
                        return false;
                    }
                    if (minSeverity != null && (e.getSeverity() == null || e.getSeverity() < minSeverity)) {
                        return false;
                    }
                    LocalDate d = e.getEventDate() != null ? e.getEventDate() : e.getStartDate();
                    if (d != null) {
                        if (startDate != null && d.isBefore(startDate)) return false;
                        if (endDate != null && d.isAfter(endDate)) return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(e -> e.getEventDate() != null ? e.getEventDate() : (e.getStartDate() != null ? e.getStartDate() : LocalDate.MIN), Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates empirical severity statistics across historical events.
     */
    public SeverityStatisticsDto calculateSeverityStatistics(List<HistoricalEventDto> events) {
        SeverityStatisticsDto stats = new SeverityStatisticsDto();
        stats.setSeverityScaleDescription("Source severity metric (DFO severity scale 1.0-2.0+ / Weather mm)");

        if (events == null || events.isEmpty()) {
            stats.setMinimumSeverity(0.0);
            stats.setMaximumSeverity(0.0);
            stats.setAverageSeverity(0.0);
            stats.setMedianSeverity(0.0);
            stats.setLatestSeverity(0.0);
            stats.setHighSeverityEventCount(0);
            return stats;
        }

        List<Double> severities = events.stream()
                .map(HistoricalEventDto::getSeverity)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        if (severities.isEmpty()) {
            stats.setMinimumSeverity(0.0);
            stats.setMaximumSeverity(0.0);
            stats.setAverageSeverity(0.0);
            stats.setMedianSeverity(0.0);
            stats.setLatestSeverity(0.0);
            stats.setHighSeverityEventCount(0);
            return stats;
        }

        stats.setMinimumSeverity(round4(severities.get(0)));
        stats.setMaximumSeverity(round4(severities.get(severities.size() - 1)));
        stats.setAverageSeverity(round4(severities.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)));

        int mid = severities.size() / 2;
        double median = (severities.size() % 2 == 1) ? severities.get(mid) : (severities.get(mid - 1) + severities.get(mid)) / 2.0;
        stats.setMedianSeverity(round4(median));

        // Latest event severity
        HistoricalEventDto latest = events.stream()
                .filter(e -> e.getSeverity() != null)
                .findFirst().orElse(null);
        stats.setLatestSeverity(latest != null ? round4(latest.getSeverity()) : stats.getAverageSeverity());

        // Count of high severity events (DFO severity >= 1.5 or normalized >= 0.70)
        long highCount = events.stream()
                .filter(e -> (e.getSeverity() != null && e.getSeverity() >= config.getHighSeverityThresholdDfo()) ||
                        (e.getNormalizedSeverity() != null && e.getNormalizedSeverity() >= 0.70))
                .count();
        stats.setHighSeverityEventCount((int) highCount);

        return stats;
    }

    /**
     * Calculates empirical recurrence statistics based strictly on historical intervals.
     */
    public RecurrenceStatisticsDto calculateRecurrenceStatistics(List<HistoricalEventDto> events, double windowDurationYears) {
        RecurrenceStatisticsDto rec = new RecurrenceStatisticsDto();

        if (events == null || events.isEmpty()) {
            rec.setAverageHistoricalGapYears(null);
            rec.setMinimumHistoricalGapYears(null);
            rec.setMaximumHistoricalGapYears(null);
            rec.setTotalIntervalsEvaluated(0);
            return rec;
        }

        List<LocalDate> sortedDates = events.stream()
                .map(e -> e.getEventDate() != null ? e.getEventDate() : e.getStartDate())
                .filter(Objects::nonNull)
                .sorted()
                .distinct()
                .toList();

        if (sortedDates.size() < 2) {
            rec.setAverageHistoricalGapYears(round4(windowDurationYears / Math.max(1, events.size())));
            rec.setMinimumHistoricalGapYears(round4(windowDurationYears / Math.max(1, events.size())));
            rec.setMaximumHistoricalGapYears(round4(windowDurationYears / Math.max(1, events.size())));
            rec.setTotalIntervalsEvaluated(0);
            return rec;
        }

        List<Double> gapYears = new ArrayList<>();
        for (int i = 1; i < sortedDates.size(); i++) {
            long days = ChronoUnit.DAYS.between(sortedDates.get(i - 1), sortedDates.get(i));
            double y = Math.max(0.01, days / 365.25);
            gapYears.add(y);
        }

        double avgGap = gapYears.stream().mapToDouble(Double::doubleValue).average().orElse(windowDurationYears);
        double minGap = gapYears.stream().mapToDouble(Double::doubleValue).min().orElse(avgGap);
        double maxGap = gapYears.stream().mapToDouble(Double::doubleValue).max().orElse(avgGap);

        rec.setAverageHistoricalGapYears(round4(avgGap));
        rec.setMinimumHistoricalGapYears(round4(minGap));
        rec.setMaximumHistoricalGapYears(round4(maxGap));
        rec.setTotalIntervalsEvaluated(gapYears.size());

        return rec;
    }

    /**
     * Calculates annual, monthly, and seasonal temporal distributions.
     */
    public TemporalPatternDto calculateTemporalPatterns(List<HistoricalEventDto> events) {
        TemporalPatternDto dto = new TemporalPatternDto();

        if (events == null || events.isEmpty()) {
            dto.setPeakDisasterMonth("N/A");
            dto.setPrimaryDisasterSeason("N/A");
            dto.setDescriptiveTrend("INSUFFICIENT_DATA");
            dto.setTrendExplanation("No historical disaster records in evaluated window.");
            return dto;
        }

        Map<Integer, Integer> byYear = new TreeMap<>();
        Map<String, Integer> byMonth = new LinkedHashMap<>();
        Map<String, Integer> bySeason = new LinkedHashMap<>();

        // Initialize months and seasons
        for (Month m : Month.values()) {
            byMonth.put(m.name(), 0);
        }
        bySeason.put("MONSOON (Jun-Sep)", 0);
        bySeason.put("POST_MONSOON (Oct-Dec)", 0);
        bySeason.put("WINTER (Jan-Feb)", 0);
        bySeason.put("PRE_MONSOON (Mar-May)", 0);

        for (HistoricalEventDto e : events) {
            LocalDate d = e.getEventDate() != null ? e.getEventDate() : e.getStartDate();
            if (d != null) {
                byYear.put(d.getYear(), byYear.getOrDefault(d.getYear(), 0) + 1);
                String monthName = d.getMonth().name();
                byMonth.put(monthName, byMonth.getOrDefault(monthName, 0) + 1);

                int mVal = d.getMonthValue();
                if (mVal >= 6 && mVal <= 9) {
                    bySeason.put("MONSOON (Jun-Sep)", bySeason.get("MONSOON (Jun-Sep)") + 1);
                } else if (mVal >= 10 && mVal <= 12) {
                    bySeason.put("POST_MONSOON (Oct-Dec)", bySeason.get("POST_MONSOON (Oct-Dec)") + 1);
                } else if (mVal <= 2) {
                    bySeason.put("WINTER (Jan-Feb)", bySeason.get("WINTER (Jan-Feb)") + 1);
                } else {
                    bySeason.put("PRE_MONSOON (Mar-May)", bySeason.get("PRE_MONSOON (Mar-May)") + 1);
                }
            }
        }

        dto.setEventsByYear(byYear);
        dto.setEventsByMonth(byMonth);
        dto.setEventsBySeason(bySeason);

        // Peak month
        String peakMonth = byMonth.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("JULY");
        dto.setPeakDisasterMonth(peakMonth);

        // Primary season
        String primarySeason = bySeason.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("MONSOON (Jun-Sep)");
        dto.setPrimaryDisasterSeason(primarySeason);

        // Descriptive Trend (split timeline into 2 halves)
        if (byYear.size() >= 4) {
            List<Integer> years = new ArrayList<>(byYear.keySet());
            int split = years.size() / 2;
            double firstHalfAvg = years.subList(0, split).stream().mapToInt(byYear::get).average().orElse(0.0);
            double secondHalfAvg = years.subList(split, years.size()).stream().mapToInt(byYear::get).average().orElse(0.0);

            if (secondHalfAvg > firstHalfAvg * 1.25) {
                dto.setDescriptiveTrend("INCREASING");
                dto.setTrendExplanation("Recorded event frequency in the second half of the archive is higher than the initial baseline.");
            } else if (secondHalfAvg < firstHalfAvg * 0.75) {
                dto.setDescriptiveTrend("DECREASING");
                dto.setTrendExplanation("Recorded event frequency shows a lower empirical count in the latter half of the archive.");
            } else {
                dto.setDescriptiveTrend("STABLE");
                dto.setTrendExplanation("Recorded historical event recurrence remains relatively consistent across the observation window.");
            }
        } else {
            dto.setDescriptiveTrend("INSUFFICIENT_DATA");
            dto.setTrendExplanation("Fewer than 4 distinct recorded disaster years; insufficient history to establish empirical trend.");
        }

        return dto;
    }

    /**
     * Computes a normalized descriptive historical hotspot index in [0.0000, 1.0000] and [0.0, 100.0].
     */
    public double calculateHotspotIndex(int totalEvents, double eventsPerYear, double avgSeverity, int highSeverityCount, Double avgGapYears) {
        if (totalEvents <= 0) {
            return 0.0000;
        }

        // 1. Frequency factor [0, 1] (Benchmark: 2 events/year is extreme hotspot)
        double freqFactor = Math.min(1.0, eventsPerYear / 1.5);

        // 2. Severity factor [0, 1] (Benchmark: DFO severity 2.0 is maximum)
        double sevFactor = Math.min(1.0, Math.max(0.2, avgSeverity / 2.0));

        // 3. Recurrence factor [0, 1] (Shorter gap = higher recurrence intensity)
        double recFactor = 0.5;
        if (avgGapYears != null) {
            if (avgGapYears <= 1.0) recFactor = 1.0;
            else if (avgGapYears <= 3.0) recFactor = 0.8;
            else if (avgGapYears <= 6.0) recFactor = 0.5;
            else recFactor = Math.max(0.1, 1.0 - (avgGapYears / 15.0));
        }

        double index = (0.40 * freqFactor) + (0.30 * sevFactor) + (0.30 * recFactor);
        return round4(Math.min(1.0, Math.max(0.0, index)));
    }

    public static double round4(double val) {
        return Math.round(val * 10000.0) / 10000.0;
    }

    public static double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
