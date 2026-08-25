package com.hazard.dto.layer;

import java.util.ArrayList;
import java.util.List;

/**
 * Catalog DTO describing all available map-ready GIS hazard layers in the system.
 */
public class HazardLayerCatalogDto {

    private String catalogTitle = "Hazard Intelligence Map Layer Catalog";
    private String description = "Available GIS vector and choropleth layers formatted in RFC 7946 GeoJSON";
    private String canonicalCrs = "EPSG:4326 (WGS 84)";
    private int totalAvailableLayers;
    private List<HazardLayerMetadataDto> layers = new ArrayList<>();

    public HazardLayerCatalogDto() {
    }

    public HazardLayerCatalogDto(List<HazardLayerMetadataDto> layers) {
        this.layers = layers != null ? layers : new ArrayList<>();
        this.totalAvailableLayers = this.layers.size();
    }

    public String getCatalogTitle() {
        return catalogTitle;
    }

    public void setCatalogTitle(String catalogTitle) {
        this.catalogTitle = catalogTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCanonicalCrs() {
        return canonicalCrs;
    }

    public void setCanonicalCrs(String canonicalCrs) {
        this.canonicalCrs = canonicalCrs;
    }

    public int getTotalAvailableLayers() {
        return totalAvailableLayers;
    }

    public void setTotalAvailableLayers(int totalAvailableLayers) {
        this.totalAvailableLayers = totalAvailableLayers;
    }

    public List<HazardLayerMetadataDto> getLayers() {
        return layers;
    }

    public void setLayers(List<HazardLayerMetadataDto> layers) {
        this.layers = layers != null ? layers : new ArrayList<>();
        this.totalAvailableLayers = this.layers.size();
    }

    public void addLayer(HazardLayerMetadataDto layer) {
        this.layers.add(layer);
        this.totalAvailableLayers = this.layers.size();
    }
}
