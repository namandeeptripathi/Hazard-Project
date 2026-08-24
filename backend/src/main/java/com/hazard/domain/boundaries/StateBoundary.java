package com.hazard.domain.boundaries;

import jakarta.persistence.*;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * State Boundary polygon (Bihar)
 * Schema: boundaries
 * Table: state_boundaries
 */
@Entity
@Table(name = "state_boundaries", schema = "boundaries")
public class StateBoundary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "gid_0")
    private String gid0;

    @Column(name = "country")
    private String country;

    @Column(name = "gid_1")
    private String gid1;

    @Column(name = "name_1")
    private String name1;

    @Column(name = "varname_1")
    private String varname1;

    @Column(name = "type_1")
    private String type1;

    @Column(name = "engtype_1")
    private String engtype1;

    @Column(name = "hasc_1")
    private String hasc1;

    @Column(name = "iso_1")
    private String iso1;

    @Column(name = "geom", columnDefinition = "geometry(MultiPolygon, 4326)")
    private MultiPolygon geom;

    public StateBoundary() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGid0() {
        return gid0;
    }

    public void setGid0(String gid0) {
        this.gid0 = gid0;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGid1() {
        return gid1;
    }

    public void setGid1(String gid1) {
        this.gid1 = gid1;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getVarname1() {
        return varname1;
    }

    public void setVarname1(String varname1) {
        this.varname1 = varname1;
    }

    public String getType1() {
        return type1;
    }

    public void setType1(String type1) {
        this.type1 = type1;
    }

    public String getEngtype1() {
        return engtype1;
    }

    public void setEngtype1(String engtype1) {
        this.engtype1 = engtype1;
    }

    public String getHasc1() {
        return hasc1;
    }

    public void setHasc1(String hasc1) {
        this.hasc1 = hasc1;
    }

    public String getIso1() {
        return iso1;
    }

    public void setIso1(String iso1) {
        this.iso1 = iso1;
    }

    public MultiPolygon getGeom() {
        return geom;
    }

    public void setGeom(MultiPolygon geom) {
        this.geom = geom;
    }
}
