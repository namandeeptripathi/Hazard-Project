package com.hazard.dto.exposure;

import com.hazard.domain.exposure.ExposureCategory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated settlement exposure summary for a specific administrative district,
 * detailing total settlements, exposed count, categorical breakdown, and individual settlement records.
 */
public class DistrictSettlementExposureSummaryDto {

    private Integer districtId;
    private String districtName;
    private String state;
    private String gid2;

    private int totalSettlementsEvaluated;
    private int exposedSettlementsCount;
    private Double settlementExposurePercentage;

    private Map<String, Integer> categoryCounts = new LinkedHashMap<>();

    private String dominantHazard;
    private Double peakHazardIndex;

    private List<SettlementExposureDto> settlements = new ArrayList<>();

    public DistrictSettlementExposureSummaryDto() {
        for (ExposureCategory cat : ExposureCategory.values()) {
            categoryCounts.put(cat.name(), 0);
        }
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGid2() {
        return gid2;
    }

    public void setGid2(String gid2) {
        this.gid2 = gid2;
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

    public String getDominantHazard() {
        return dominantHazard;
    }

    public void setDominantHazard(String dominantHazard) {
        this.dominantHazard = dominantHazard;
    }

    public Double getPeakHazardIndex() {
        return peakHazardIndex;
    }

    public void setPeakHazardIndex(Double peakHazardIndex) {
        this.peakHazardIndex = peakHazardIndex;
    }

    public List<SettlementExposureDto> getSettlements() {
        return settlements;
    }

    public void setSettlements(List<SettlementExposureDto> settlements) {
        this.settlements = settlements != null ? settlements : new ArrayList<>();
    }

    public void addSettlement(SettlementExposureDto settlement) {
        this.settlements.add(settlement);
    }
}
