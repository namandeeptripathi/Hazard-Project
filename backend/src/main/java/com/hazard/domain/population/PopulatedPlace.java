package com.hazard.domain.population;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

/**
 * Populated Place / Residential Footprint (HOT OpenStreetMap)
 * Schema: population
 * Table: populated_places
 */
@Entity
@Table(name = "populated_places", schema = "population")
public class PopulatedPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "osm_id")
    private String osmId;

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @Column(name = "name_en", columnDefinition = "text")
    private String nameEn;

    @Column(name = "name_hi", columnDefinition = "text")
    private String nameHi;

    @Column(name = "place")
    private String place;

    @Column(name = "landuse")
    private String landuse;

    @Column(name = "population")
    private Long population;

    @Column(name = "adm0_name")
    private String adm0Name;

    @Column(name = "adm1_name")
    private String adm1Name;

    @Column(name = "adm1_pcode")
    private String adm1Pcode;

    @Column(name = "adm2_name")
    private String adm2Name;

    @Column(name = "adm2_pcode")
    private String adm2Pcode;

    @Column(name = "adm3_name")
    private String adm3Name;

    @Column(name = "adm3_pcode")
    private String adm3Pcode;

    @Column(name = "geom", columnDefinition = "geometry(Geometry, 4326)")
    private Geometry geom;

    public PopulatedPlace() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOsmId() {
        return osmId;
    }

    public void setOsmId(String osmId) {
        this.osmId = osmId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameHi() {
        return nameHi;
    }

    public void setNameHi(String nameHi) {
        this.nameHi = nameHi;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getLanduse() {
        return landuse;
    }

    public void setLanduse(String landuse) {
        this.landuse = landuse;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public String getAdm0Name() {
        return adm0Name;
    }

    public void setAdm0Name(String adm0Name) {
        this.adm0Name = adm0Name;
    }

    public String getAdm1Name() {
        return adm1Name;
    }

    public void setAdm1Name(String adm1Name) {
        this.adm1Name = adm1Name;
    }

    public String getAdm1Pcode() {
        return adm1Pcode;
    }

    public void setAdm1Pcode(String adm1Pcode) {
        this.adm1Pcode = adm1Pcode;
    }

    public String getAdm2Name() {
        return adm2Name;
    }

    public void setAdm2Name(String adm2Name) {
        this.adm2Name = adm2Name;
    }

    public String getAdm2Pcode() {
        return adm2Pcode;
    }

    public void setAdm2Pcode(String adm2Pcode) {
        this.adm2Pcode = adm2Pcode;
    }

    public String getAdm3Name() {
        return adm3Name;
    }

    public void setAdm3Name(String adm3Name) {
        this.adm3Name = adm3Name;
    }

    public String getAdm3Pcode() {
        return adm3Pcode;
    }

    public void setAdm3Pcode(String adm3Pcode) {
        this.adm3Pcode = adm3Pcode;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }
}
