# Stage 3.6 — Map-Ready GIS Hazard Layers Specification

**Project:** Smart Hazard Risk Prediction and Relocation System  
**Event:** Smart India Hackathon (SIH) 2026 | Problem Statement ID: SIH26191  
**Document Status:** Approved & Implemented Sub-Stage 3.6 Specification  
**File Path:** `docs/Stage-3-Hazard-Intelligence/Stage-3.6-Hazard-Layers.md`  

---

## 1. Executive Summary & Objective

**Stage 3.6 (Hazard Layers)** translates the entire pipeline of Hazard Intelligence outputs (Stages 3.1 through 3.5) into standardized, map-ready GIS vector and choropleth layers conforming to the **RFC 7946 GeoJSON** specification. These layers are structured for immediate visualization in modern frontend mapping clients (Leaflet, MapLibre GL, OpenLayers, Mapbox) and desktop GIS tooling (QGIS, ArcGIS).

```text
STAGE 3.1 - 3.5 HAZARD INTELLIGENCE
  ├── Integrated Hazard Events (Stage 3.1)
  ├── Processed Observations & Quality Status (Stage 3.2)
  ├── Normalized Indicators (Stage 3.3)
  ├── Single-Hazard Scores & Severity Tiers (Stage 3.4)
  ├── Multi-Hazard Index & Dominance (Stage 3.5)
  └── Spatial Baselines: 38 District Boundaries & River Reaches (Stage 2)
          │
          ▼
MAP LAYER ENGINE (com.hazard.service.layer.HazardLayerService)
  ├── JTS to RFC 7946 GeoJSON Geometry Converter (Points, Polygons, MultiPolygons, LineStrings)
  ├── Performance Limits (Default: 50, Max Safe Limit: 1000)
  ├── Unlocated & Out-of-Bounds Sentinel Record Filtering
  └── Spatial, Temporal, Severity, and District Filtering
          │
          ▼
MAP LAYER CATALOG (com.hazard.dto.layer)
  ├── 1. FLOOD_EVENTS (Point / DFO Historical Inundation Events)
  ├── 2. EXTREME_RAINFALL_EVENTS (Point / Meteorological Precipitation Station Events)
  ├── 3. FLOOD_HAZARD_SCORES (Point / Single-Hazard Flood Scores & Severity)
  ├── 4. EXTREME_RAINFALL_SCORES (Point / Single-Hazard Rainfall Scores & Severity)
  ├── 5. MULTI_HAZARD_INDEX (Point / Cross-Hazard Synthesized Multi-Hazard Index)
  ├── 6. DISTRICT_HAZARD_SUMMARIES (MultiPolygon / 38 Bihar District Boundary Choropleths)
  ├── 7. DISTRICT_BOUNDARIES (MultiPolygon / Official Administrative District Boundaries)
  └── 8. RIVERS_REFERENCE (MultiLineString / HydroRIVERS Hydrological Drainage Network)
          │
          ▼
REST APIS (/api/v1/hazards/layers)
```

---

## 2. Layer Catalog & Metadata Schema

All layers are discoverable through the centralized Layer Catalog API endpoint (`GET /api/v1/hazards/layers`):

| Layer ID | Category | Geometry Type | Hazard Type | Description |
| :--- | :--- | :--- | :--- | :--- |
| **`FLOOD_EVENTS`** | `EVENT_LAYER` | `Point` | `FLOOD` | Discrete spatial point observations of historical flood events from Dartmouth Flood Observatory (DFO). |
| **`EXTREME_RAINFALL_EVENTS`** | `EVENT_LAYER` | `Point` | `EXTREME_RAINFALL` | Meteorological extreme precipitation observations from Open-Meteo weather stations ($\ge 15\text{ mm/hr}$). |
| **`FLOOD_HAZARD_SCORES`** | `HAZARD_SCORE_LAYER` | `Point` | `FLOOD` | Single-hazard Flood Hazard Scores $\in [0.0000, 1.0000]$ and categorical severity tiers (`LOW` to `SEVERE`). |
| **`EXTREME_RAINFALL_SCORES`** | `HAZARD_SCORE_LAYER` | `Point` | `EXTREME_RAINFALL` | Single-hazard Extreme Rainfall Hazard Scores $\in [0.0000, 1.0000]$ and categorical severity tiers. |
| **`MULTI_HAZARD_INDEX`** | `MULTI_HAZARD_LAYER` | `Point` | *Multi-Hazard* | Cross-hazard synthesized Multi-Hazard Index $\in [0.0000, 1.0000]$ with dominant hazard classification. |
| **`DISTRICT_HAZARD_SUMMARIES`** | `DISTRICT_SUMMARY_LAYER` | `MultiPolygon` | *Multi-Hazard* | Administrative boundary polygons for all 38 Bihar districts enriched with peak multi-hazard indices and severity. |
| **`DISTRICT_BOUNDARIES`** | `REFERENCE_LAYER` | `MultiPolygon` | *Reference* | Official Survey of India administrative district boundaries for Bihar (38 districts). |
| **`RIVERS_REFERENCE`** | `REFERENCE_LAYER` | `MultiLineString` | *Reference* | HydroRIVERS hydrological river reaches and drainage network for Bihar floodplains. |

---

## 3. RFC 7946 GeoJSON Standard Adherence

All spatial layer outputs strictly follow the **RFC 7946 GeoJSON** standard:
1. **Structure:** Top-level `FeatureCollection` with a list of `Feature` objects containing valid `geometry` (`Point`, `Polygon`, `MultiPolygon`, `LineString`, `MultiLineString`) and domain-oriented `properties`.
2. **Canonical CRS:** Coordinates are formatted in WGS 84 / `EPSG:4326` with standard `[longitude, latitude]` order (Eastings, Northings).
3. **Geometry Isolation:** Geometries are never embedded inside properties bags; properties contain only attributes needed for symbology, tooltips, and client-side querying.

---

## 4. Layer Property Encodings

### 4.1 Single-Hazard & Multi-Hazard Score Properties
* `hazardScore` / `multiHazardIndex`: Floating-point scalar $\in [0.0000, 1.0000]$ rounded to 4 decimal places.
* `severityTier`: Categorical string (`LOW`, `MODERATE`, `HIGH`, `SEVERE`).
* `dominantHazard`: Identified primary hazard driver (`FLOOD`, `EXTREME_RAINFALL`).
* `completenessRatio`: Decimal indicator of participating data completeness.
* `associatedDistrict`: Associated Bihar administrative district name.

### 4.2 District Choropleth Summary Properties (`DISTRICT_HAZARD_SUMMARIES`)
* `districtId`: Integer identifier.
* `districtName`: Name of the district (e.g. `Patna`, `Sitamarhi`).
* `state`: `Bihar`.
* `gid2`: GADM Level-2 identifier.
* `totalHazardEventsCount`: Number of recorded hazard events in district.
* `hasActiveWeatherStation`: Boolean (`true` for Patna, Muzaffarpur, Bhagalpur).
* `peakMultiHazardIndex`: Maximum recorded multi-hazard index $\in [0.0000, 1.0000]$.
* `severityTier`: Resulting severity tier classification.
* `dominantHazard`: Primary driver in district (`FLOOD` or `EXTREME_RAINFALL`).

---

## 5. Performance Controls & Data Integrity

1. **Payload Protection:** Default query limit is **50 features**, with a maximum safety ceiling of **1000 features** (`sanitizeLimit`).
2. **Unlocated Record Exclusion:** 69 unlocated tabular records (16 DFO sentinel-cleaned and 53 EM-DAT national records) lack verified coordinates and are strictly excluded from point and polygon map layers to prevent visual artifacts.
3. **Spatial Index Utilization:** All district and bounding box lookups utilize the GiST spatial indexes established in Stage 2 (`idx_district_boundaries_geom`, `idx_hydrorivers_geom`, `idx_hourly_weather_geom`).

---

## 6. REST API Specifications (`/api/v1/hazards/layers`)

| Method | Endpoint | Query Parameters | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/layers` | - | Returns catalog of all 8 available map layers. |
| `GET` | `/api/v1/hazards/layers/{layerId}/metadata` | - | Retrieves metadata for a specific layer. |
| `GET` | `/api/v1/hazards/layers/{layerId}` | `district`, `severity`, `from`, `to`, `limit` | Returns map-ready RFC 7946 GeoJSON `FeatureCollection`. |

---

## 7. Test Suite & Verification Results

All **129 automated test cases** across 20 test suites pass with **0 failures**:

| Test Suite | Tests Run | Result | Key Verified Functionality |
| :--- | :---: | :---: | :--- |
| `HazardApplicationTests` | 7 | ✅ PASS | Verified all 11 Stage 2 base tables (159,005 rows); regression-free baseline. |
| `HazardDomainModelTests` | 5 | ✅ PASS | Verified `HazardType`, `SeverityTier`, `SpatialRelationship`, GeoJSON. |
| `HazardIntegrationServiceTests` | 9 | ✅ PASS | Verified Stage 3.1 PostGIS spatial queries, bounding boxes, proximity. |
| `HazardIntegrationControllerTests` | 10 | ✅ PASS | Verified Stage 3.1 REST API endpoints and validation error handling. |
| `HazardDataCleanerTests` | 6 | ✅ PASS | Verified sentinel coordinate cleaning, negative metric flooring, duration derivation. |
| `HazardProcessingServiceTests` | 7 | ✅ PASS | Verified Stage 3.2 processing pipeline, daily aggregation, rolling accumulation. |
| `HazardProcessingControllerTests` | 8 | ✅ PASS | Verified Stage 3.2 REST API endpoints, daily/rolling feeds, GeoJSON outputs. |
| `HazardNormalizationEngineTests` | 9 | ✅ PASS | Verified normalization scaling, boundary clamping, min==max safety, inverse direction. |
| `HazardNormalizationServiceTests` | 6 | ✅ PASS | Verified normalization of live PostGIS data. |
| `HazardNormalizationControllerTests`| 8 | ✅ PASS | Verified Stage 3.3 REST API endpoints, metric filtering, GeoJSON vector layers. |
| `HazardScoringEngineTests` | 6 | ✅ PASS | Verified single-hazard weighted scoring, weight validation, missing metric handling. |
| `HazardScoringServiceTests` | 6 | ✅ PASS | Verified single-hazard scoring of DFO floods and extreme rainfall. |
| `HazardScoringControllerTests` | 8 | ✅ PASS | Verified Stage 3.4 REST API endpoints. |
| `MultiHazardAggregationEngineTests` | 4 | ✅ PASS | Verified cross-hazard weighted aggregation, weight sum validation, dominant hazard selection. |
| `MultiHazardSpatialTemporalMatcherTests` | 4 | ✅ PASS | Verified geodesic distance matching, district containment fallback, temporal overlap. |
| `HazardMultiHazardServiceTests` | 5 | ✅ PASS | Verified live multi-hazard synthesis against PostGIS data, district queries, summaries. |
| `MultiHazardControllerTests` | 5 | ✅ PASS | Verified Stage 3.5 REST API endpoints. |
| `HazardLayerCatalogTests` | 3 | ✅ PASS | Verified Layer Catalog metadata, categories, geometry types, and error handling. |
| `HazardLayerServiceTests` | 7 | ✅ PASS | Verified all 8 map layers generate valid RFC 7946 GeoJSON against live PostGIS data. |
| `HazardLayerControllerTests` | 6 | ✅ PASS | Verified Stage 3.6 REST API endpoints. |
| **Total** | **129** | **✅ PASS** | **100% test success rate across backend, GIS, scoring, multi-hazard, and layer services.** |

---

## 8. Real-Data Layer Verification Results

* **`FLOOD_EVENTS`:** 7 discrete spatial points in Bihar (Sitamarhi, Begusarai, Purba Champaran).
* **`EXTREME_RAINFALL_EVENTS`:** Convective extreme storm events across Patna, Muzaffarpur, and Bhagalpur stations.
* **`FLOOD_HAZARD_SCORES`:** 7 scored flood points with `hazardScore` $\in [0.1220, 0.4500]$ and severity tiers (`LOW`, `MODERATE`).
* **`EXTREME_RAINFALL_SCORES`:** Extreme rainfall scored points up to `0.7388` (`HIGH`) and `0.8500` (`SEVERE`).
* **`MULTI_HAZARD_INDEX`:** Synthesized multi-hazard index points with dominant hazard classifications.
* **`DISTRICT_HAZARD_SUMMARIES`:** 38 official Bihar District `MultiPolygon` choropleth boundaries with embedded hazard index and severity properties.
* **`DISTRICT_BOUNDARIES`:** 38 official administrative district `MultiPolygon` reference features.
* **`RIVERS_REFERENCE`:** 589 hydrological river reach `MultiLineString` / `LineString` drainage features.

---

## 9. Exact Boundary Between Stage 3.6 and Stage 3.7

* **Completed in Stage 3.6 (Map-Ready Hazard Layers):**
  * GeoJSON layer generation for event, score, multi-hazard, and district choropleth layers.
  * Layer metadata catalog and layer discovery endpoints.
  * Spatial and property filtering (`district`, `severity`, `from`, `to`, `limit`).
  * REST APIs under `/api/v1/hazards/layers`.
* **Deferred to Stage 3.7 (Dedicated Hazard API Integration):**
  * Consolidated Hazard API surface, unified facade services, API documentation, and OpenAPI / Swagger integration.
