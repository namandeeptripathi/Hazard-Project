package com.hazard.dto.hazard;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard GeoJSON FeatureCollection container (RFC 7946 compliant)
 * ready for direct consumption by frontend Leaflet, Mapbox, or OpenLayers maps.
 */
public class GeoJsonFeatureCollectionDto {

    private String type = "FeatureCollection";
    private String crs = "urn:ogc:def:crs:OGC:1.3:CRS84";
    private int count;
    private List<GeoJsonFeatureDto> features = new ArrayList<>();

    public GeoJsonFeatureCollectionDto() {
    }

    public GeoJsonFeatureCollectionDto(List<GeoJsonFeatureDto> features) {
        this.features = features != null ? features : new ArrayList<>();
        this.count = this.features.size();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<GeoJsonFeatureDto> getFeatures() {
        return features;
    }

    public void setFeatures(List<GeoJsonFeatureDto> features) {
        this.features = features != null ? features : new ArrayList<>();
        this.count = this.features.size();
    }
}
