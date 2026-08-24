package com.hazard.domain.weather;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

/**
 * Hourly Meteorological Observation Time-Series
 * Schema: weather
 * Table: hourly_weather
 */
@Entity
@Table(name = "hourly_weather", schema = "weather")
public class HourlyWeather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "observation_time", nullable = false)
    private LocalDateTime observationTime;

    @Column(name = "precipitation_mm")
    private Double precipitationMm;

    @Column(name = "rain_mm")
    private Double rainMm;

    @Column(name = "snowfall_cm")
    private Double snowfallCm;

    @Column(name = "cloud_cover_pct")
    private Double cloudCoverPct;

    @Column(name = "surface_pressure_hpa")
    private Double surfacePressureHpa;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "geom", columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    public HourlyWeather() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public LocalDateTime getObservationTime() {
        return observationTime;
    }

    public void setObservationTime(LocalDateTime observationTime) {
        this.observationTime = observationTime;
    }

    public Double getPrecipitationMm() {
        return precipitationMm;
    }

    public void setPrecipitationMm(Double precipitationMm) {
        this.precipitationMm = precipitationMm;
    }

    public Double getRainMm() {
        return rainMm;
    }

    public void setRainMm(Double rainMm) {
        this.rainMm = rainMm;
    }

    public Double getSnowfallCm() {
        return snowfallCm;
    }

    public void setSnowfallCm(Double snowfallCm) {
        this.snowfallCm = snowfallCm;
    }

    public Double getCloudCoverPct() {
        return cloudCoverPct;
    }

    public void setCloudCoverPct(Double cloudCoverPct) {
        this.cloudCoverPct = cloudCoverPct;
    }

    public Double getSurfacePressureHpa() {
        return surfacePressureHpa;
    }

    public void setSurfacePressureHpa(Double surfacePressureHpa) {
        this.surfacePressureHpa = surfacePressureHpa;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Point getGeom() {
        return geom;
    }

    public void setGeom(Point geom) {
        this.geom = geom;
    }
}
