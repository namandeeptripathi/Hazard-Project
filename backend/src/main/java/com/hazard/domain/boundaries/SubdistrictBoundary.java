package com.hazard.domain.boundaries;

import jakarta.persistence.*;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * Sub-district Boundary polygon (53 Bihar Sub-districts / Tehsils)
 * Schema: boundaries
 * Table: subdistrict_boundaries
 */
@Entity
@Table(name = "subdistrict_boundaries", schema = "boundaries")
public class SubdistrictBoundary {

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

    @Column(name = "gid_3")
    private String gid3;

    @Column(name = "name_3")
    private String name3;

    @Column(name = "type_3")
    private String type3;

    @Column(name = "engtype_3")
    private String engtype3;

    @Column(name = "geom", columnDefinition = "geometry(MultiPolygon, 4326)")
    private MultiPolygon geom;

    public SubdistrictBoundary() {
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

    public String getGid3() {
        return gid3;
    }

    public void setGid3(String gid3) {
        this.gid3 = gid3;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    public String getType3() {
        return type3;
    }

    public void setType3(String type3) {
        this.type3 = type3;
    }

    public String getEngtype3() {
        return engtype3;
    }

    public void setEngtype3(String engtype3) {
        this.engtype3 = engtype3;
    }

    public MultiPolygon getGeom() {
        return geom;
    }

    public void setGeom(MultiPolygon geom) {
        this.geom = geom;
    }
}
