package com.hazard.dto.exposure;

import com.hazard.domain.exposure.ExposureCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result container for hazard-event or custom-geometry settlement exposure queries,
 * including summary metadata, category counts, and list of affected settlements.
 */
public class SettlementExposureAnalysisResultDto {

    private String geographicUnit;
    private String hazardIdentifier;
    private String hazardType;
    private Double hazardSeverityScore;

    private int totalSettlementsEvaluated;
    private int exposedSettlementsCount;
    private Double settlementExposurePercentage;
    private Double averageSettlementExposureScore;

    private Map<String, Integer> categoryCounts = new LinkedHashMap<>();

    private LocalDateTime timestamp;
    private String calculationMethod;
    private String explanation;

    private List<SettlementExposureDto> exposedSettlements = new ArrayList<>();
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public SettlementExposureAnalysisResultDto() {
        this.timestamp = LocalDateTime.now();
        for (ExposureCategory cat : ExposureCategory.values()) {
            categoryCounts.put(cat.name(), 0);
        }
    }

    public String getGeographicUnit() {
        return geographicUnit;
    }

    public void setGeographicUnit(String geographicUnit) {
        this.geographicUnit = geographicUnit;
    }

    public String getHazardIdentifier() {
        return hazardIdentifier;
    }

    public void setHazardIdentifier(String hazardIdentifier) {
        this.hazardIdentifier = hazardIdentifier;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public Double getHazardSeverityScore() {
        return hazardSeverityScore;
    }

    public void setHazardSeverityScore(Double hazardSeverityScore) {
        this.hazardSeverityScore = hazardSeverityScore;
    }

    public int getTotalSettlementsEvaluated() {
        return totalSettlementsEvaluated;
    }

    public void setTotalSettlementsEvaluated(int totalSettlementsEvaluated) {
        this.totalSettlementsEvaluated = totalSettlementsEvaluated;
    }

    public int getExposedSettlementsCount() {
        return exposedSettlementsCount;
    }

    public void setExposedSettlementsCount(int exposedSettlementsCount) {
        this.exposedSettlementsCount = exposedSettlementsCount;
    }

    public Double getSettlementExposurePercentage() {
        return settlementExposurePercentage;
    }

    public void setSettlementExposurePercentage(Double settlementExposurePercentage) {
        this.settlementExposurePercentage = settlementExposurePercentage;
    }

    public Double getAverageSettlementExposureScore() {
        return averageSettlementExposureScore;
    }

    public void setAverageSettlementExposureScore(Double averageSettlementExposureScore) {
        this.averageSettlementExposureScore = averageSettlementExposureScore;
    }

    public Map<String, Integer> getCategoryCounts() {
        return categoryCounts;
    }

    public void setCategoryCounts(Map<String, Integer> categoryCounts) {
        this.categoryCounts = categoryCounts != null ? categoryCounts : new LinkedHashMap<>();
    }

    public void incrementCategoryCount(ExposureCategory category) {
        if (category != null) {
            this.categoryCounts.merge(category.name(), 1, Integer::sum);
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(String calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<SettlementExposureDto> getExposedSettlements() {
        return exposedSettlements;
    }

    public void setExposedSettlements(List<SettlementExposureDto> exposedSettlements) {
        this.exposedSettlements = exposedSettlements != null ? exposedSettlements : new ArrayList<>();
    }

    public void addExposedSettlement(SettlementExposureDto settlement) {
        this.exposedSettlements.add(settlement);
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new LinkedHashMap<>();
    }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}
