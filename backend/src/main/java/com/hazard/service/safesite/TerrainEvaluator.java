package com.hazard.service.safesite;

import com.hazard.domain.safesite.TerrainStatus;
import com.hazard.domain.terrain.DemTile;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.service.terrain.TerrainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Stage 5.4 — Terrain / Slope Evaluator.
 *
 * Spatially evaluates terrain feasibility (FAVORABLE, UNFAVORABLE, UNKNOWN) for candidate safe sites.
 *
 * Principles:
 * 1. Evidence-based: Evaluates real slope and elevation attributes when provided.
 * 2. No Data Fabrication: When point-level elevation/slope raster data is not present in the database,
 *    identifies tile footprint coverage via TerrainService and cleanly sets UNKNOWN without inventing fake numbers.
 * 3. Orthogonal: Completely independent of Stage 5.3 hazard-safety status.
 */
@Component
public class TerrainEvaluator {

    private static final Logger log = LoggerFactory.getLogger(TerrainEvaluator.class);

    private final TerrainService terrainService;
    private final TerrainEvaluationConfig config;

    public TerrainEvaluator(TerrainService terrainService, TerrainEvaluationConfig config) {
        this.terrainService = terrainService;
        this.config = config;
    }

    /**
     * Evaluates terrain and slope suitability for an individual candidate safe site.
     * Mutates the site DTO to populate elevationMeters, slopeDegrees, terrainStatus, and terrainReason.
     */
    public void evaluateTerrain(CandidateSafeSiteDto site) {
        if (site == null) {
            return;
        }

        Double lat = site.getLatitude();
        Double lon = site.getLongitude();

        // 1. Validate geographic coordinates
        if (lat == null || lon == null || lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            site.setTerrainStatus(TerrainStatus.UNKNOWN);
            site.setTerrainReason("Missing or invalid geographic coordinates; terrain evaluation cannot be performed.");
            return;
        }

        // 2. Case A: Site has explicit slope degrees (e.g. from terrain provider or test fixture)
        if (site.getSlopeDegrees() != null) {
            double slope = site.getSlopeDegrees();
            if (slope <= config.getMaxFavorableSlopeDegrees()) {
                site.setTerrainStatus(TerrainStatus.FAVORABLE);
                site.setTerrainReason(String.format(
                        "Site terrain slope is favorable (%.1f° <= %.1f° threshold) for emergency shelter operations.",
                        slope, config.getMaxFavorableSlopeDegrees()));
            } else if (slope >= config.getMinUnfavorableSlopeDegrees()) {
                site.setTerrainStatus(TerrainStatus.UNFAVORABLE);
                site.setTerrainReason(String.format(
                        "Site terrain slope is unfavorable (%.1f° >= %.1f° threshold); steep terrain presents stability or accessibility risks.",
                        slope, config.getMinUnfavorableSlopeDegrees()));
            } else {
                site.setTerrainStatus(TerrainStatus.UNKNOWN);
                site.setTerrainReason(String.format(
                        "Site terrain slope is intermediate (%.2f°), falling between configured favorable (%.1f°) and unfavorable (%.1f°) thresholds; terrain suitability is indeterminate.",
                        slope, config.getMaxFavorableSlopeDegrees(), config.getMinUnfavorableSlopeDegrees()));
            }
            return;
        }

        // 3. Case B: Site has explicit elevation but NO slope degrees
        if (site.getElevationMeters() != null) {
            site.setSlopeDegrees(null);
            site.setTerrainStatus(TerrainStatus.UNKNOWN);
            site.setTerrainReason(String.format(
                    "Site elevation is %.1f meters, but site-level slope data is not available to determine terrain feasibility.",
                    site.getElevationMeters()));
            return;
        }

        // 4. Case C: Point-level elevation and slope are not present in dataset (Default Production State)
        // Perform spatial query to resolve intersecting DEM tile footprint from PostGIS terrain.dem_tiles
        Optional<DemTile> tileOpt = Optional.empty();
        try {
            tileOpt = terrainService.getDemTileForCoordinate(lon, lat);
        } catch (Exception e) {
            log.warn("DEM tile lookup failed for site {} at ({}, {}): {}", site.getSiteId(), lon, lat, e.getMessage());
        }

        site.setElevationMeters(null);
        site.setSlopeDegrees(null);
        site.setTerrainStatus(TerrainStatus.UNKNOWN);

        if (tileOpt.isPresent()) {
            DemTile tile = tileOpt.get();
            site.setTerrainReason(String.format(
                    "DEM tile footprint '%s' covers location (tile bounds: %.1fm - %.1fm elevation, %.1fm resolution), but point-level elevation/slope raster sampling is not currently ingested.",
                    tile.getTileName(),
                    tile.getMinElevationM() != null ? tile.getMinElevationM() : 0.0,
                    tile.getMaxElevationM() != null ? tile.getMaxElevationM() : 0.0,
                    tile.getResolutionMeters() != null ? tile.getResolutionMeters() : 30.0));
        } else {
            site.setTerrainReason("Terrain elevation and slope data are not currently available for this location.");
        }
    }
}
