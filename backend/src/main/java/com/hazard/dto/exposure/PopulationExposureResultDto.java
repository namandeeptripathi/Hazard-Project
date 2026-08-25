package com.hazard.dto.exposure;

import com.hazard.domain.exposure.ExposureCategory;
import com.hazard.domain.exposure.PopulationDataSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Standardized DTO encapsulating the results of a Population Exposure analysis.
 */
public class PopulationExposureResultDto {

    private String geographicUnit;
    private String districtName;
    private String hazardIdentifier;
    private String hazardType;

    private Long totalPopulation;
    private Long exposedPopulation;
    private Long unexposedPopulation;
    private Double exposurePercentage;
    private Double exposureScore;
    private ExposureCategory exposureCategory;

    private int intersectingSettlementsCount;
    private Long explicitPopulationCount;
    private Long estimatedPopulationCount;
    private PopulationDataSource dataSourceProvenance;

    private String calculationMethod;
    private String explanation;
    private LocalDateTime timestamp;

    private List<SettlementExposureSummaryDto> affectedSettlementsSummary = new ArrayList<>();
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public PopulationExposureResultDto() {
        this.timestamp = LocalDateTime.now();
    }

    public String getGeographicUnit() {
        return geographicUnit;
    }

    public void setGeographicUnit(String geographicUnit) {
        this.geographicUnit = geographicUnit;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
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

    public Long getTotalPopulation() {
        return totalPopulation;
    }

    public void setTotalPopulation(Long totalPopulation) {
        this.totalPopulation = totalPopulation;
    }

    public Long getExposedPopulation() {
        return exposedPopulation;
    }

    public void setExposedPopulation(Long exposedPopulation) {
        this.exposedPopulation = exposedPopulation;
    }

    public Long getUnexposedPopulation() {
        return unexposedPopulation;
    }

    public void setUnexposedPopulation(Long unexposedPopulation) {
        this.unexposedPopulation = unexposedPopulation;
    }

    public Double getExposurePercentage() {
        return exposurePercentage;
    }

    public void setExposurePercentage(Double exposurePercentage) {
        this.exposurePercentage = exposurePercentage;
    }

    public Double getExposureScore() {
        return exposureScore;
    }

    public void setExposureScore(Double exposureScore) {
        this.exposureScore = exposureScore;
    }

    public ExposureCategory getExposureCategory() {
        return exposureCategory;
    }

    public void setExposureCategory(ExposureCategory exposureCategory) {
        this.exposureCategory = exposureCategory;
    }

    public int getIntersectingSettlementsCount() {
        return intersectingSettlementsCount;
    }

    public void setIntersectingSettlementsCount(int intersectingSettlementsCount) {
        this.intersectingSettlementsCount = intersectingSettlementsCount;
    }

    public Long getExplicitPopulationCount() {
        return explicitPopulationCount;
    }

    public void setExplicitPopulationCount(Long explicitPopulationCount) {
        this.explicitPopulationCount = explicitPopulationCount;
    }

    public Long getEstimatedPopulationCount() {
        return estimatedPopulationCount;
    }

    public void setEstimatedPopulationCount(Long estimatedPopulationCount) {
        this.estimatedPopulationCount = estimatedPopulationCount;
    }

    public PopulationDataSource getDataSourceProvenance() {
        return dataSourceProvenance;
    }

    public void setDataSourceProvenance(PopulationDataSource dataSourceProvenance) {
        this.dataSourceProvenance = dataSourceProvenance;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<SettlementExposureSummaryDto> getAffectedSettlementsSummary() {
        return affectedSettlementsSummary;
    }

    public void setAffectedSettlementsSummary(List<SettlementExposureSummaryDto> affectedSettlementsSummary) {
        this.affectedSettlementsSummary = affectedSettlementsSummary != null ? affectedSettlementsSummary : new ArrayList<>();
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
