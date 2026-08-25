package com.hazard.dto.hazard;

import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard GeoJSON Geometry primitive (Point, Polygon, MultiPolygon, LineString, MultiLineString)
 * in WGS 84 (EPSG:4326) adhering to RFC 7946 specifications.
 */
public class GeoJsonGeometryDto {

    private String type;
    private Object coordinates;

    public GeoJsonGeometryDto() {
    }

    public GeoJsonGeometryDto(String type, Object coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public static GeoJsonGeometryDto point(double longitude, double latitude) {
        return new GeoJsonGeometryDto("Point", new double[]{longitude, latitude});
    }

    /**
     * Converts a JTS Geometry object into a standard GeoJSON Geometry DTO.
     */
    public static GeoJsonGeometryDto fromJtsGeometry(Geometry geom) {
        if (geom == null) {
            return null;
        }

        if (geom instanceof Point p) {
            return point(p.getX(), p.getY());
        }

        if (geom instanceof Polygon poly) {
            List<List<double[]>> coordinates = new ArrayList<>();
            coordinates.add(coordsToRing(poly.getExteriorRing().getCoordinates()));
            for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                coordinates.add(coordsToRing(poly.getInteriorRingN(i).getCoordinates()));
            }
            return new GeoJsonGeometryDto("Polygon", coordinates);
        }

        if (geom instanceof MultiPolygon mp) {
            List<List<List<double[]>>> coordinates = new ArrayList<>();
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Polygon poly = (Polygon) mp.getGeometryN(i);
                List<List<double[]>> polyCoords = new ArrayList<>();
                polyCoords.add(coordsToRing(poly.getExteriorRing().getCoordinates()));
                for (int j = 0; j < poly.getNumInteriorRing(); j++) {
                    polyCoords.add(coordsToRing(poly.getInteriorRingN(j).getCoordinates()));
                }
                coordinates.add(polyCoords);
            }
            return new GeoJsonGeometryDto("MultiPolygon", coordinates);
        }

        if (geom instanceof LineString ls) {
            return new GeoJsonGeometryDto("LineString", coordsToRing(ls.getCoordinates()));
        }

        if (geom instanceof MultiLineString mls) {
            List<List<double[]>> coordinates = new ArrayList<>();
            for (int i = 0; i < mls.getNumGeometries(); i++) {
                coordinates.add(coordsToRing(mls.getGeometryN(i).getCoordinates()));
            }
            return new GeoJsonGeometryDto("MultiLineString", coordinates);
        }

        return new GeoJsonGeometryDto(geom.getGeometryType(), null);
    }

    private static List<double[]> coordsToRing(Coordinate[] coords) {
        List<double[]> ring = new ArrayList<>(coords.length);
        for (Coordinate c : coords) {
            ring.add(new double[]{c.getX(), c.getY()});
        }
        return ring;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Object coordinates) {
        this.coordinates = coordinates;
    }
}
