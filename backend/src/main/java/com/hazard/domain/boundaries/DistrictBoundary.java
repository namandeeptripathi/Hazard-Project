package com.hazard.domain.boundaries;

import jakarta.persistence.*;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * District Boundary polygon (38 Bihar Districts)
 * Schema: boundaries
 * Table: district_boundaries
 */
@Entity
@Table(name = "district_boundaries", schema = "boundaries")
public class DistrictBoundary {

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

    @Column(name = "gid_2")
    private String gid2;

    @Column(name = "name_2")
    private String name2;

    @Column(name = "varname_2")
    private String varname2;

    @Column(name = "type_2")
    private String type2;

    @Column(name = "engtype_2")
    private String engtype2;

    @Column(name = "hasc_2")
    private String hasc2;

    @Column(name = "geom", columnDefinition = "geometry(MultiPolygon, 4326)")
    private MultiPolygon geom;

    public DistrictBoundary() {
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

    public String getGid2() {
        return gid2;
    }

    public void setGid2(String gid2) {
        this.gid2 = gid2;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getVarname2() {
        return varname2;
    }

    public void setVarname2(String varname2) {
        this.varname2 = varname2;
    }

    public String getType2() {
        return type2;
    }

    public void setType2(String type2) {
        this.type2 = type2;
    }

    public String getEngtype2() {
        return engtype2;
    }

    public void setEngtype2(String engtype2) {
        this.engtype2 = engtype2;
    }

    public String getHasc2() {
        return hasc2;
    }

    public void setHasc2(String hasc2) {
        this.hasc2 = hasc2;
    }

    public MultiPolygon getGeom() {
        return geom;
    }

    public void setGeom(MultiPolygon geom) {
        this.geom = geom;
    }
}
