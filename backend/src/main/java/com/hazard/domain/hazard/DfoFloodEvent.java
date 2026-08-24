package com.hazard.domain.hazard;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDate;

/**
 * Historical Flood Event from Dartmouth Flood Observatory (DFO)
 * Schema: hazard
 * Table: dfo_flood_events
 */
@Entity
@Table(name = "dfo_flood_events", schema = "hazard")
public class DfoFloodEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "register_no")
    private Double registerNo;

    @Column(name = "annual_dfo")
    private Double annualDfo;

    @Column(name = "glide_no")
    private String glideNo;

    @Column(name = "country")
    private String country;

    @Column(name = "detailed_location", columnDefinition = "text")
    private String detailedLocation;

    @Column(name = "rivers", columnDefinition = "text")
    private String rivers;

    @Column(name = "began_date")
    private LocalDate beganDate;

    @Column(name = "ended_date")
    private LocalDate endedDate;

    @Column(name = "duration_days")
    private Double durationDays;

    @Column(name = "deaths")
    private Double deaths;

    @Column(name = "displaced")
    private Double displaced;

    @Column(name = "damage_usd")
    private Double damageUsd;

    @Column(name = "main_cause", columnDefinition = "text")
    private String mainCause;

    @Column(name = "severity")
    private Double severity;

    @Column(name = "affected_sqkm")
    private Double affectedSqkm;

    @Column(name = "magnitude")
    private Double magnitude;

    @Column(name = "centroid_x")
    private Double centroidX;

    @Column(name = "centroid_y")
    private Double centroidY;

    @Column(name = "matched_by")
    private String matchedBy;

    @Column(name = "geom", columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    public DfoFloodEvent() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getRegisterNo() {
        return registerNo;
    }

    public void setRegisterNo(Double registerNo) {
        this.registerNo = registerNo;
    }

    public Double getAnnualDfo() {
        return annualDfo;
    }

    public void setAnnualDfo(Double annualDfo) {
        this.annualDfo = annualDfo;
    }

    public String getGlideNo() {
        return glideNo;
    }

    public void setGlideNo(String glideNo) {
        this.glideNo = glideNo;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDetailedLocation() {
        return detailedLocation;
    }

    public void setDetailedLocation(String detailedLocation) {
        this.detailedLocation = detailedLocation;
    }

    public String getRivers() {
        return rivers;
    }

    public void setRivers(String rivers) {
        this.rivers = rivers;
    }

    public LocalDate getBeganDate() {
        return beganDate;
    }

    public void setBeganDate(LocalDate beganDate) {
        this.beganDate = beganDate;
    }

    public LocalDate getEndedDate() {
        return endedDate;
    }

    public void setEndedDate(LocalDate endedDate) {
        this.endedDate = endedDate;
    }

    public Double getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Double durationDays) {
        this.durationDays = durationDays;
    }

    public Double getDeaths() {
        return deaths;
    }

    public void setDeaths(Double deaths) {
        this.deaths = deaths;
    }

    public Double getDisplaced() {
        return displaced;
    }

    public void setDisplaced(Double displaced) {
        this.displaced = displaced;
    }

    public Double getDamageUsd() {
        return damageUsd;
    }

    public void setDamageUsd(Double damageUsd) {
        this.damageUsd = damageUsd;
    }

    public String getMainCause() {
        return mainCause;
    }

    public void setMainCause(String mainCause) {
        this.mainCause = mainCause;
    }

    public Double getSeverity() {
        return severity;
    }

    public void setSeverity(Double severity) {
        this.severity = severity;
    }

    public Double getAffectedSqkm() {
        return affectedSqkm;
    }

    public void setAffectedSqkm(Double affectedSqkm) {
        this.affectedSqkm = affectedSqkm;
    }

    public Double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Double magnitude) {
        this.magnitude = magnitude;
    }

    public Double getCentroidX() {
        return centroidX;
    }

    public void setCentroidX(Double centroidX) {
        this.centroidX = centroidX;
    }

    public Double getCentroidY() {
        return centroidY;
    }

    public void setCentroidY(Double centroidY) {
        this.centroidY = centroidY;
    }

    public String getMatchedBy() {
        return matchedBy;
    }

    public void setMatchedBy(String matchedBy) {
        this.matchedBy = matchedBy;
    }

    public Point getGeom() {
        return geom;
    }

    public void setGeom(Point geom) {
        this.geom = geom;
    }
}
