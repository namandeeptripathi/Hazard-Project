package com.hazard.dto.exposure;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request payload for analyzing population exposure for an arbitrary hazard geometry
 * supplied as WKT or GeoJSON geometry string.
 */
@Schema(description = "Request payload for custom geometry population exposure analysis")
public class GeometryExposureRequestDto {

    @Schema(description = "Geometry in Well-Known Text (WKT) format e.g. POLYGON((85.0 25.5, 85.5 25.5, 85.5 26.0, 85.0 26.0, 85.0 25.5))",
            example = "POLYGON((85.0 25.5, 85.5 25.5, 85.5 26.0, 85.0 26.0, 85.0 25.5))")
    private String wktGeometry;

    @Schema(description = "Optional hazard identifier or event label", example = "CUSTOM-FLOOD-ZONE-1")
    private String hazardIdentifier;

    @Schema(description = "Optional hazard type", example = "FLOOD")
    private String hazardType;

    @Schema(description = "Optional associated district name for contextual reference", example = "Patna")
    private String associatedDistrict;

    @Schema(description = "Optional buffer radius in meters to expand the geometry before intersection", example = "0.0")
    private Double bufferMeters = 0.0;

    public GeometryExposureRequestDto() {}

    public GeometryExposureRequestDto(String wktGeometry, String hazardIdentifier, String hazardType) {
        this.wktGeometry = wktGeometry;
        this.hazardIdentifier = hazardIdentifier;
        this.hazardType = hazardType;
    }

    public String getWktGeometry() {
        return wktGeometry;
    }

    public void setWktGeometry(String wktGeometry) {
        this.wktGeometry = wktGeometry;
    }

    public String getHazardIdentifier() {
        return hazardIdentifier;
    }

    public void setHazardIdentifier(String hazardIdentifier) {
        this.hazardIdentifier = hazardIdentifier;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public String getAssociatedDistrict() {
        return associatedDistrict;
    }

    public void setAssociatedDistrict(String associatedDistrict) {
        this.associatedDistrict = associatedDistrict;
    }

    public Double getBufferMeters() {
        return bufferMeters;
    }

    public void setBufferMeters(Double bufferMeters) {
        this.bufferMeters = bufferMeters != null ? bufferMeters : 0.0;
    }
}
