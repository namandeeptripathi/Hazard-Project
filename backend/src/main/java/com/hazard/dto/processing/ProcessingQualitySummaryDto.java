package com.hazard.dto.processing;

import java.util.ArrayList;
import java.util.List;

/**
 * Processing and Quality Audit Summary DTO.
 */
public class ProcessingQualitySummaryDto {

    private long totalSourceRecords;
    private long totalProcessedRecords;
    private long validRecordsCount;
    private long unlocatedRecordsCount;
    private long partialRecordsCount;
    private long invalidRecordsCount;

    // By Dataset
    private long dfoTotal;
    private long dfoValid;
    private long dfoUnlocated;

    private long emdatTotal;
    private long emdatUnlocated;

    private long weatherTotal;
    private long weatherValid;

    private long anomaliesCleanedCount;
    private List<String> cleaningRulesApplied = new ArrayList<>();
    private List<String> coveredDistricts = new ArrayList<>();
    private List<String> activeWeatherStations = new ArrayList<>();
    private String temporalCoverage;
    private String canonicalCrs;

    public ProcessingQualitySummaryDto() {
    }

    public long getTotalSourceRecords() {
        return totalSourceRecords;
    }

    public void setTotalSourceRecords(long totalSourceRecords) {
        this.totalSourceRecords = totalSourceRecords;
    }

    public long getTotalProcessedRecords() {
        return totalProcessedRecords;
    }

    public void setTotalProcessedRecords(long totalProcessedRecords) {
        this.totalProcessedRecords = totalProcessedRecords;
    }

    public long getValidRecordsCount() {
        return validRecordsCount;
    }

    public void setValidRecordsCount(long validRecordsCount) {
        this.validRecordsCount = validRecordsCount;
    }

    public long getUnlocatedRecordsCount() {
        return unlocatedRecordsCount;
    }

    public void setUnlocatedRecordsCount(long unlocatedRecordsCount) {
        this.unlocatedRecordsCount = unlocatedRecordsCount;
    }

    public long getPartialRecordsCount() {
        return partialRecordsCount;
    }

    public void setPartialRecordsCount(long partialRecordsCount) {
        this.partialRecordsCount = partialRecordsCount;
    }

    public long getInvalidRecordsCount() {
        return invalidRecordsCount;
    }

    public void setInvalidRecordsCount(long invalidRecordsCount) {
        this.invalidRecordsCount = invalidRecordsCount;
    }

    public long getDfoTotal() {
        return dfoTotal;
    }

    public void setDfoTotal(long dfoTotal) {
        this.dfoTotal = dfoTotal;
    }

    public long getDfoValid() {
        return dfoValid;
    }

    public void setDfoValid(long dfoValid) {
        this.dfoValid = dfoValid;
    }

    public long getDfoUnlocated() {
        return dfoUnlocated;
    }

    public void setDfoUnlocated(long dfoUnlocated) {
        this.dfoUnlocated = dfoUnlocated;
    }

    public long getEmdatTotal() {
        return emdatTotal;
    }

    public void setEmdatTotal(long emdatTotal) {
        this.emdatTotal = emdatTotal;
    }

    public long getEmdatUnlocated() {
        return emdatUnlocated;
    }

    public void setEmdatUnlocated(long emdatUnlocated) {
        this.emdatUnlocated = emdatUnlocated;
    }

    public long getWeatherTotal() {
        return weatherTotal;
    }

    public void setWeatherTotal(long weatherTotal) {
        this.weatherTotal = weatherTotal;
    }

    public long getWeatherValid() {
        return weatherValid;
    }

    public void setWeatherValid(long weatherValid) {
        this.weatherValid = weatherValid;
    }

    public long getAnomaliesCleanedCount() {
        return anomaliesCleanedCount;
    }

    public void setAnomaliesCleanedCount(long anomaliesCleanedCount) {
        this.anomaliesCleanedCount = anomaliesCleanedCount;
    }

    public List<String> getCleaningRulesApplied() {
        return cleaningRulesApplied;
    }

    public void setCleaningRulesApplied(List<String> cleaningRulesApplied) {
        this.cleaningRulesApplied = cleaningRulesApplied != null ? cleaningRulesApplied : new ArrayList<>();
    }

    public List<String> getCoveredDistricts() {
        return coveredDistricts;
    }

    public void setCoveredDistricts(List<String> coveredDistricts) {
        this.coveredDistricts = coveredDistricts != null ? coveredDistricts : new ArrayList<>();
    }

    public List<String> getActiveWeatherStations() {
        return activeWeatherStations;
    }

    public void setActiveWeatherStations(List<String> activeWeatherStations) {
        this.activeWeatherStations = activeWeatherStations != null ? activeWeatherStations : new ArrayList<>();
    }

    public String getTemporalCoverage() {
        return temporalCoverage;
    }

    public void setTemporalCoverage(String temporalCoverage) {
        this.temporalCoverage = temporalCoverage;
    }

    public String getCanonicalCrs() {
        return canonicalCrs;
    }

    public void setCanonicalCrs(String canonicalCrs) {
        this.canonicalCrs = canonicalCrs;
    }
}
