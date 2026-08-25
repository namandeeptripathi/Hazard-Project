package com.hazard.dto.hazard;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standard GeoJSON Feature object (RFC 7946 compliant).
 */
public class GeoJsonFeatureDto {

    private String type = "Feature";
    private String id;
    private GeoJsonGeometryDto geometry;
    private Map<String, Object> properties = new LinkedHashMap<>();

    public GeoJsonFeatureDto() {
    }

    public GeoJsonFeatureDto(String id, GeoJsonGeometryDto geometry, Map<String, Object> properties) {
        this.id = id;
        this.geometry = geometry;
        this.properties = properties != null ? properties : new LinkedHashMap<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GeoJsonGeometryDto getGeometry() {
        return geometry;
    }

    public void setGeometry(GeoJsonGeometryDto geometry) {
        this.geometry = geometry;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
