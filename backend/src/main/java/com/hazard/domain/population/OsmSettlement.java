package com.hazard.domain.population;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

/**
 * OpenStreetMap Settlement Node (Cities, Towns, Villages)
 * Schema: population
 * Table: osm_settlements
 */
@Entity
@Table(name = "osm_settlements", schema = "population")
public class OsmSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "osm_id")
    private Long osmId;

    @Column(name = "osm_type")
    private String osmType;

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @Column(name = "name_hi", columnDefinition = "text")
    private String nameHi;

    @Column(name = "name_en", columnDefinition = "text")
    private String nameEn;

    @Column(name = "place")
    private String place;

    @Column(name = "population")
    private Long population;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "wikidata")
    private String wikidata;

    @Column(name = "geom", columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    public OsmSettlement() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getOsmId() {
        return osmId;
    }

    public void setOsmId(Long osmId) {
        this.osmId = osmId;
    }

    public String getOsmType() {
        return osmType;
    }

    public void setOsmType(String osmType) {
        this.osmType = osmType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameHi() {
        return nameHi;
    }

    public void setNameHi(String nameHi) {
        this.nameHi = nameHi;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getWikidata() {
        return wikidata;
    }

    public void setWikidata(String wikidata) {
        this.wikidata = wikidata;
    }

    public Point getGeom() {
        return geom;
    }

    public void setGeom(Point geom) {
        this.geom = geom;
    }
}
