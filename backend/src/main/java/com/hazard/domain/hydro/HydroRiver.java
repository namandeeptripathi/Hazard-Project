package com.hazard.domain.hydro;

import jakarta.persistence.*;
import org.locationtech.jts.geom.MultiLineString;

/**
 * River Reach from HydroRIVERS Asia dataset
 * Schema: hydro
 * Table: hydrorivers
 */
@Entity
@Table(name = "hydrorivers", schema = "hydro")
public class HydroRiver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "hyriv_id")
    private Long hyrivId;

    @Column(name = "next_down")
    private Long nextDown;

    @Column(name = "main_riv")
    private Long mainRiv;

    @Column(name = "length_km")
    private Double lengthKm;

    @Column(name = "dist_dn_km")
    private Double distDnKm;

    @Column(name = "dist_up_km")
    private Double distUpKm;

    @Column(name = "catch_skm")
    private Double catchSkm;

    @Column(name = "upland_skm")
    private Double uplandSkm;

    @Column(name = "endorheic")
    private Integer endorheic;

    @Column(name = "dis_av_cms")
    private Double disAvCms;

    @Column(name = "ord_stra")
    private Integer ordStra;

    @Column(name = "ord_clas")
    private Integer ordClas;

    @Column(name = "ord_flow")
    private Integer ordFlow;

    @Column(name = "hybas_l12")
    private Long hybasL12;

    @Column(name = "geom", columnDefinition = "geometry(MultiLineString, 4326)")
    private MultiLineString geom;

    public HydroRiver() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getHyrivId() {
        return hyrivId;
    }

    public void setHyrivId(Long hyrivId) {
        this.hyrivId = hyrivId;
    }

    public Long getNextDown() {
        return nextDown;
    }

    public void setNextDown(Long nextDown) {
        this.nextDown = nextDown;
    }

    public Long getMainRiv() {
        return mainRiv;
    }

    public void setMainRiv(Long mainRiv) {
        this.mainRiv = mainRiv;
    }

    public Double getLengthKm() {
        return lengthKm;
    }

    public void setLengthKm(Double lengthKm) {
        this.lengthKm = lengthKm;
    }

    public Double getDistDnKm() {
        return distDnKm;
    }

    public void setDistDnKm(Double distDnKm) {
        this.distDnKm = distDnKm;
    }

    public Double getDistUpKm() {
        return distUpKm;
    }

    public void setDistUpKm(Double distUpKm) {
        this.distUpKm = distUpKm;
    }

    public Double getCatchSkm() {
        return catchSkm;
    }

    public void setCatchSkm(Double catchSkm) {
        this.catchSkm = catchSkm;
    }

    public Double getUplandSkm() {
        return uplandSkm;
    }

    public void setUplandSkm(Double uplandSkm) {
        this.uplandSkm = uplandSkm;
    }

    public Integer getEndorheic() {
        return endorheic;
    }

    public void setEndorheic(Integer endorheic) {
        this.endorheic = endorheic;
    }

    public Double getDisAvCms() {
        return disAvCms;
    }

    public void setDisAvCms(Double disAvCms) {
        this.disAvCms = disAvCms;
    }

    public Integer getOrdStra() {
        return ordStra;
    }

    public void setOrdStra(Integer ordStra) {
        this.ordStra = ordStra;
    }

    public Integer getOrdClas() {
        return ordClas;
    }

    public void setOrdClas(Integer ordClas) {
        this.ordClas = ordClas;
    }

    public Integer getOrdFlow() {
        return ordFlow;
    }

    public void setOrdFlow(Integer ordFlow) {
        this.ordFlow = ordFlow;
    }

    public Long getHybasL12() {
        return hybasL12;
    }

    public void setHybasL12(Long hybasL12) {
        this.hybasL12 = hybasL12;
    }

    public MultiLineString getGeom() {
        return geom;
    }

    public void setGeom(MultiLineString geom) {
        this.geom = geom;
    }
}
