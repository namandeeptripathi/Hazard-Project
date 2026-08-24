package com.hazard.domain.hydro;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

/**
 * OpenStreetMap Local Waterway or Waterbody
 * Schema: hydro
 * Table: osm_waterways
 */
@Entity
@Table(name = "osm_waterways", schema = "hydro")
public class OsmWaterway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "osm_id")
    private Long osmId;

    @Column(name = "osm_type")
    private String osmType;

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @Column(name = "waterway")
    private String waterway;

    @Column(name = "water")
    private String water;

    @Column(name = "natural_feature")
    private String naturalFeature;

    @Column(name = "landuse")
    private String landuse;

    @Column(name = "intermittent")
    private String intermittent;

    @Column(name = "tunnel")
    private String tunnel;

    @Column(name = "geom", columnDefinition = "geometry(Geometry, 4326)")
    private Geometry geom;

    public OsmWaterway() {
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

    public String getWaterway() {
        return waterway;
    }

    public void setWaterway(String waterway) {
        this.waterway = waterway;
    }

    public String getWater() {
        return water;
    }

    public void setWater(String water) {
        this.water = water;
    }

    public String getNaturalFeature() {
        return naturalFeature;
    }

    public void setNaturalFeature(String naturalFeature) {
        this.naturalFeature = naturalFeature;
    }

    public String getLanduse() {
        return landuse;
    }

    public void setLanduse(String landuse) {
        this.landuse = landuse;
    }

    public String getIntermittent() {
        return intermittent;
    }

    public void setIntermittent(String intermittent) {
        this.intermittent = intermittent;
    }

    public String getTunnel() {
        return tunnel;
    }

    public void setTunnel(String tunnel) {
        this.tunnel = tunnel;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }
}
