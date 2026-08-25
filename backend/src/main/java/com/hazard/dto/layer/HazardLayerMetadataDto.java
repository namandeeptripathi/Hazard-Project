package com.hazard.dto.layer;

import com.hazard.domain.hazard.HazardLayerCategory;
import com.hazard.domain.hazard.HazardType;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata descriptor for an available map-ready GIS hazard layer.
 */
public class HazardLayerMetadataDto {

    private String layerId;
    private String layerName;
    private HazardLayerCategory category;
    private String geometryType;
    private HazardType hazardType;
    private String description;
    private List<String> supportedFilters = new ArrayList<>();
    private boolean hasScore;
    private boolean hasSeverityTier;
    private String canonicalCrs = "EPSG:4326 (WGS 84)";
    private String dataSource;
    private String endpointUrl;

    public HazardLayerMetadataDto() {
    }

    public HazardLayerMetadataDto(String layerId, String layerName, HazardLayerCategory category,
                                  String geometryType, HazardType hazardType, String description,
                                  List<String> supportedFilters, boolean hasScore, boolean hasSeverityTier,
                                  String dataSource, String endpointUrl) {
        this.layerId = layerId;
        this.layerName = layerName;
        this.category = category;
        this.geometryType = geometryType;
        this.hazardType = hazardType;
        this.description = description;
        this.supportedFilters = supportedFilters != null ? supportedFilters : new ArrayList<>();
        this.hasScore = hasScore;
        this.hasSeverityTier = hasSeverityTier;
        this.dataSource = dataSource;
        this.endpointUrl = endpointUrl;
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public HazardLayerCategory getCategory() {
        return category;
    }

    public void setCategory(HazardLayerCategory category) {
        this.category = category;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    public HazardType getHazardType() {
        return hazardType;
    }

    public void setHazardType(HazardType hazardType) {
        this.hazardType = hazardType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSupportedFilters() {
        return supportedFilters;
    }

    public void setSupportedFilters(List<String> supportedFilters) {
        this.supportedFilters = supportedFilters != null ? supportedFilters : new ArrayList<>();
    }

    public boolean isHasScore() {
        return hasScore;
    }

    public void setHasScore(boolean hasScore) {
        this.hasScore = hasScore;
    }

    public boolean isHasSeverityTier() {
        return hasSeverityTier;
    }

    public void setHasSeverityTier(boolean hasSeverityTier) {
        this.hasSeverityTier = hasSeverityTier;
    }

    public String getCanonicalCrs() {
        return canonicalCrs;
    }

    public void setCanonicalCrs(String canonicalCrs) {
        this.canonicalCrs = canonicalCrs;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
}
