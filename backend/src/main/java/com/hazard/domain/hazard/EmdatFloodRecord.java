package com.hazard.domain.hazard;

import jakarta.persistence.*;

/**
 * Historical Flood Economic and Impact Record from EM-DAT
 * Schema: hazard
 * Table: emdat_flood_records
 */
@Entity
@Table(name = "emdat_flood_records", schema = "hazard")
public class EmdatFloodRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "year")
    private Integer year;

    @Column(name = "country")
    private String country;

    @Column(name = "iso")
    private String iso;

    @Column(name = "disaster_group")
    private String disasterGroup;

    @Column(name = "disaster_subgroup")
    private String disasterSubgroup;

    @Column(name = "disaster_type")
    private String disasterType;

    @Column(name = "disaster_subtype")
    private String disasterSubtype;

    @Column(name = "total_events")
    private Integer totalEvents;

    @Column(name = "total_affected")
    private Long totalAffected;

    @Column(name = "total_deaths")
    private Integer totalDeaths;

    @Column(name = "total_damage_usd_original")
    private Double totalDamageUsdOriginal;

    @Column(name = "total_damage_usd_adjusted")
    private Double totalDamageUsdAdjusted;

    @Column(name = "cpi")
    private Double cpi;

    @Column(name = "spatial_granularity")
    private String spatialGranularity;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    public EmdatFloodRecord() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getDisasterGroup() {
        return disasterGroup;
    }

    public void setDisasterGroup(String disasterGroup) {
        this.disasterGroup = disasterGroup;
    }

    public String getDisasterSubgroup() {
        return disasterSubgroup;
    }

    public void setDisasterSubgroup(String disasterSubgroup) {
        this.disasterSubgroup = disasterSubgroup;
    }

    public String getDisasterType() {
        return disasterType;
    }

    public void setDisasterType(String disasterType) {
        this.disasterType = disasterType;
    }

    public String getDisasterSubtype() {
        return disasterSubtype;
    }

    public void setDisasterSubtype(String disasterSubtype) {
        this.disasterSubtype = disasterSubtype;
    }

    public Integer getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(Integer totalEvents) {
        this.totalEvents = totalEvents;
    }

    public Long getTotalAffected() {
        return totalAffected;
    }

    public void setTotalAffected(Long totalAffected) {
        this.totalAffected = totalAffected;
    }

    public Integer getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(Integer totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public Double getTotalDamageUsdOriginal() {
        return totalDamageUsdOriginal;
    }

    public void setTotalDamageUsdOriginal(Double totalDamageUsdOriginal) {
        this.totalDamageUsdOriginal = totalDamageUsdOriginal;
    }

    public Double getTotalDamageUsdAdjusted() {
        return totalDamageUsdAdjusted;
    }

    public void setTotalDamageUsdAdjusted(Double totalDamageUsdAdjusted) {
        this.totalDamageUsdAdjusted = totalDamageUsdAdjusted;
    }

    public Double getCpi() {
        return cpi;
    }

    public void setCpi(Double cpi) {
        this.cpi = cpi;
    }

    public String getSpatialGranularity() {
        return spatialGranularity;
    }

    public void setSpatialGranularity(String spatialGranularity) {
        this.spatialGranularity = spatialGranularity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
