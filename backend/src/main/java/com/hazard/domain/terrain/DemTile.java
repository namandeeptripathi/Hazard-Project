package com.hazard.domain.terrain;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Polygon;

/**
 * Digital Elevation Model (DEM) Tile Metadata Catalog & Spatial Footprint
 * Schema: terrain
 * Table: dem_tiles
 */
@Entity
@Table(name = "dem_tiles", schema = "terrain")
public class DemTile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tile_name", nullable = false, unique = true)
    private String tileName;

    @Column(name = "file_path", nullable = false, columnDefinition = "text")
    private String filePath;

    @Column(name = "resolution_arcsec")
    private Double resolutionArcsec;

    @Column(name = "resolution_meters")
    private Double resolutionMeters;

    @Column(name = "crs")
    private String crs;

    @Column(name = "nodata_value")
    private Double nodataValue;

    @Column(name = "min_elevation_m")
    private Double minElevationM;

    @Column(name = "max_elevation_m")
    private Double maxElevationM;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "bbox_minx")
    private Double bboxMinx;

    @Column(name = "bbox_miny")
    private Double bboxMiny;

    @Column(name = "bbox_maxx")
    private Double bboxMaxx;

    @Column(name = "bbox_maxy")
    private Double bboxMaxy;

    @Column(name = "geom", columnDefinition = "geometry(Polygon, 4326)")
    private Polygon geom;

    public DemTile() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTileName() {
        return tileName;
    }

    public void setTileName(String tileName) {
        this.tileName = tileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Double getResolutionArcsec() {
        return resolutionArcsec;
    }

    public void setResolutionArcsec(Double resolutionArcsec) {
        this.resolutionArcsec = resolutionArcsec;
    }

    public Double getResolutionMeters() {
        return resolutionMeters;
    }

    public void setResolutionMeters(Double resolutionMeters) {
        this.resolutionMeters = resolutionMeters;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public Double getNodataValue() {
        return nodataValue;
    }

    public void setNodataValue(Double nodataValue) {
        this.nodataValue = nodataValue;
    }

    public Double getMinElevationM() {
        return minElevationM;
    }

    public void setMinElevationM(Double minElevationM) {
        this.minElevationM = minElevationM;
    }

    public Double getMaxElevationM() {
        return maxElevationM;
    }

    public void setMaxElevationM(Double maxElevationM) {
        this.maxElevationM = maxElevationM;
    }

    public Integer getWidthPx() {
        return widthPx;
    }

    public void setWidthPx(Integer widthPx) {
        this.widthPx = widthPx;
    }

    public Integer getHeightPx() {
        return heightPx;
    }

    public void setHeightPx(Integer heightPx) {
        this.heightPx = heightPx;
    }

    public Double getBboxMinx() {
        return bboxMinx;
    }

    public void setBboxMinx(Double bboxMinx) {
        this.bboxMinx = bboxMinx;
    }

    public Double getBboxMiny() {
        return bboxMiny;
    }

    public void setBboxMiny(Double bboxMiny) {
        this.bboxMiny = bboxMiny;
    }

    public Double getBboxMaxx() {
        return bboxMaxx;
    }

    public void setBboxMaxx(Double bboxMaxx) {
        this.bboxMaxx = bboxMaxx;
    }

    public Double getBboxMaxy() {
        return bboxMaxy;
    }

    public void setBboxMaxy(Double bboxMaxy) {
        this.bboxMaxy = bboxMaxy;
    }

    public Polygon getGeom() {
        return geom;
    }

    public void setGeom(Polygon geom) {
        this.geom = geom;
    }
}
