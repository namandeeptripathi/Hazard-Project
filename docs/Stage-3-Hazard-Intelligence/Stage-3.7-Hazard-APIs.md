# Stage 3.7 — Hazard Intelligence APIs & OpenAPI 3 Specification

**Project:** Smart Hazard Risk Prediction and Relocation System  
**Event:** Smart India Hackathon (SIH) 2026 | Problem Statement ID: SIH26191  
**Document Status:** Approved & Implemented Sub-Stage 3.7 Specification  
**File Path:** `docs/Stage-3-Hazard-Intelligence/Stage-3.7-Hazard-APIs.md`  

---

## 1. Executive Summary & Objective

**Stage 3.7 (Hazard APIs)** consolidates, standardizes, and documents the entire suite of Hazard Intelligence REST APIs (Stages 3.1 through 3.6). It introduces the **`HazardApiFacade`** for unified application orchestration, standardized **`ApiResponse<T>`** and **`ApiErrorResponse`** envelopes, centralized exception handling with sanitized error messages, SpringDoc **OpenAPI 3 / Swagger UI** documentation across 7 API groups, and subsystem health/readiness endpoints.

```text
CLIENT CONSUMERS (Frontend UI / GIS Clients / External Systems)
                       │
                       ▼
            SPRING REST API CONTROLLERS (/api/v1/hazards/**)
  ├── 1. HazardIntegrationController (/api/v1/hazards)
  ├── 2. HazardProcessingController (/api/v1/hazards/processed)
  ├── 3. HazardNormalizationController (/api/v1/hazards/normalized)
  ├── 4. HazardScoringController (/api/v1/hazards/scores)
  ├── 5. MultiHazardController (/api/v1/hazards/multi-hazard)
  ├── 6. HazardLayerController (/api/v1/hazards/layers)
  └── 7. HazardHealthController (/api/v1/hazards/health, /api/v1/hazards/overview/**)
                       │
                       ▼
          UNIFIED HAZARD API FACADE (com.hazard.service.facade.HazardApiFacade)
  ├── Consolidated District Hazard Profiles
  └── Operational Health & Subsystem Readiness Checks
                       │
                       ▼
          UNDERLYING DOMAIN SERVICES (Stages 3.1 to 3.6)
  ├── HazardIntegrationService  ├── HazardProcessingService
  ├── HazardNormalizationService├── HazardScoringService
  ├── MultiHazardService        └── HazardLayerService
                       │
                       ▼
      PostgreSQL 17.11 / PostGIS 3.6.4 (hazard_db, 159,005 records, EPSG:4326)
```

---

## 2. Complete API Inventory & Groupings

### Group 1: Hazard Integration (`/api/v1/hazards`) — Stage 3.1
| Method | Path | Description | Response Type |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards` | Lists integrated hazard events with optional type/limit filters. | `List<IntegratedHazardEvent>` |
| `GET` | `/api/v1/hazards/{id}` | Retrieves single integrated hazard observation by unified ID. | `IntegratedHazardEvent` |
| `GET` | `/api/v1/hazards/type/{type}` | Filters hazards by type (`FLOOD`, `EXTREME_RAINFALL`). | `List<IntegratedHazardEvent>` |
| `GET` | `/api/v1/hazards/district/{name}`| Intersects hazards with a Bihar administrative district. | `List<IntegratedHazardEvent>` |
| `GET` | `/api/v1/hazards/nearby` | Spatial proximity query within `radiusMeters` via `ST_DWithin`. | `List<IntegratedHazardEvent>` |
| `GET` | `/api/v1/hazards/bbox` | Spatial bounding box query `[minLon, minLat, maxLon, maxLat]`. | `List<IntegratedHazardEvent>` |
| `GET` | `/api/v1/hazards/time-range` | Temporal date interval query `[startDate, endDate]`. | `List<IntegratedHazardEvent>` |
| `GET` | `/api/v1/hazards/rainfall/extreme`| Precipitation threshold extraction ($\ge \text{thresholdMm}$). | `List<IntegratedHazardEvent>` |
| `GET` | `/api/v1/hazards/geojson` | Integrated observations as RFC 7946 GeoJSON. | `GeoJsonFeatureCollectionDto` |
| `GET` | `/api/v1/hazards/summary` | Catalog summary of data sources, CRS, and temporal spans. | `HazardSummaryDto` |

### Group 2: Hazard Processing (`/api/v1/hazards/processed`) — Stage 3.2
| Method | Path | Description | Response Type |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/processed` | Lists cleaned, analysis-ready processed observations. | `List<ProcessedHazardObservation>` |
| `GET` | `/api/v1/hazards/processed/{id}` | Retrieves a single processed observation by unified ID. | `ProcessedHazardObservation` |
| `GET` | `/api/v1/hazards/processed/quality/{status}` | Filters by quality status (`VALID`, `UNLOCATED`, `PARTIAL`). | `List<ProcessedHazardObservation>` |
| `GET` | `/api/v1/hazards/processed/district/{name}` | Retrieves processed observations for an administrative district. | `List<ProcessedHazardObservation>` |
| `GET` | `/api/v1/hazards/processed/rainfall/daily` | Diurnal rainfall aggregation (totals, peak intensity, duration). | `List<DailyRainfallSummary>` |
| `GET` | `/api/v1/hazards/processed/rainfall/rolling`| Multi-window rolling rainfall accumulation (3h, 6h, 12h, 24h). | `RollingRainfallMetrics` |
| `GET` | `/api/v1/hazards/processed/quality-summary` | Executive summary of cleaning actions and valid record counts. | `ProcessingQualitySummaryDto` |
| `GET` | `/api/v1/hazards/processed/geojson` | Spatially valid processed hazards as RFC 7946 GeoJSON. | `GeoJsonFeatureCollectionDto` |

### Group 3: Hazard Normalization (`/api/v1/hazards/normalized`) — Stage 3.3
| Method | Path | Description | Response Type |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/normalized` | Lists normalized hazard indicators scaled to $[0.0000, 1.0000]$. | `List<NormalizedHazardObservation>` |
| `GET` | `/api/v1/hazards/normalized/{id}` | Retrieves a single normalized observation with metric vectors. | `NormalizedHazardObservation` |
| `GET` | `/api/v1/hazards/normalized/metric/{name}` | Filters observations by specific indicator name. | `List<NormalizedHazardObservation>` |
| `GET` | `/api/v1/hazards/normalized/district/{name}` | Retrieves normalized observations within a district. | `List<NormalizedHazardObservation>` |
| `GET` | `/api/v1/hazards/normalized/rainfall/daily` | Normalized daily rainfall totals and peak hourly intensity. | `List<NormalizedDailyRainfall>` |
| `GET` | `/api/v1/hazards/normalized/rainfall/rolling` | Normalized rolling 3h, 6h, 12h, and 24h storm metrics. | `NormalizedRollingRainfall` |
| `GET` | `/api/v1/hazards/normalized/summary` | Reference range catalog for all 12 scientific normalization ranges. | `NormalizationSummaryDto` |
| `GET` | `/api/v1/hazards/normalized/geojson` | Normalized hazard indicators as RFC 7946 GeoJSON. | `GeoJsonFeatureCollectionDto` |

### Group 4: Hazard Scoring (`/api/v1/hazards/scores`) — Stage 3.4
| Method | Path | Description | Response Type |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/scores` | Lists single-hazard composite scores and severity tiers. | `List<HazardScoreDto>` |
| `GET` | `/api/v1/hazards/scores/{id}` | Retrieves scored observation with metric contribution breakdown. | `HazardScoreDto` |
| `GET` | `/api/v1/hazards/scores/type/{type}` | Filters hazard scores by hazard type (`FLOOD`, `EXTREME_RAINFALL`). | `List<HazardScoreDto>` |
| `GET` | `/api/v1/hazards/scores/district/{name}` | Retrieves single-hazard scores for an administrative district. | `List<HazardScoreDto>` |
| `GET` | `/api/v1/hazards/scores/rainfall/daily` | Scored daily meteorological rainfall observations. | `List<DailyRainfallScoreDto>` |
| `GET` | `/api/v1/hazards/scores/rainfall/rolling` | Scored rolling accumulation multi-window storm metrics. | `RollingRainfallScoreDto` |
| `GET` | `/api/v1/hazards/scores/summary` | Executive catalog summary of score and tier distributions. | `HazardScoringSummaryDto` |
| `GET` | `/api/v1/hazards/scores/geojson` | Scored hazard observations as RFC 7946 GeoJSON. | `GeoJsonFeatureCollectionDto` |

### Group 5: Multi-Hazard Intelligence (`/api/v1/hazards/multi-hazard`) — Stage 3.5
| Method | Path | Description | Response Type |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/multi-hazard` | Lists synthesized cross-hazard coincidence observations. | `List<MultiHazardObservation>` |
| `GET` | `/api/v1/hazards/multi-hazard/{id}` | Retrieves single multi-hazard observation with dominance. | `MultiHazardObservation` |
| `GET` | `/api/v1/hazards/multi-hazard/district/{name}`| Retrieves multi-hazard observations in a district. | `List<MultiHazardObservation>` |
| `GET` | `/api/v1/hazards/multi-hazard/summary` | Catalog summary of coincidence counts and tier distributions. | `MultiHazardSummaryDto` |
| `GET` | `/api/v1/hazards/multi-hazard/geojson` | Synthesized multi-hazard observations as RFC 7946 GeoJSON. | `GeoJsonFeatureCollectionDto` |

### Group 6: Map-Ready GIS Layers (`/api/v1/hazards/layers`) — Stage 3.6
| Method | Path | Description | Response Type |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/layers` | Returns the catalog of all 8 available map layers. | `HazardLayerCatalogDto` |
| `GET` | `/api/v1/hazards/layers/{layerId}/metadata` | Retrieves metadata, supported filters, and geometry types for layer. | `HazardLayerMetadataDto` |
| `GET` | `/api/v1/hazards/layers/{layerId}` | Delivers requested map layer as RFC 7946 GeoJSON. | `GeoJsonFeatureCollectionDto` |

### Group 7: System Health & District Overview (`/api/v1/hazards`) — Stage 3.7
| Method | Path | Description | Response Type |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/health` | Operational health status, DB connectivity, and active capabilities. | `ApiResponse<HazardSystemHealthDto>` |
| `GET` | `/api/v1/hazards/overview/district/{name}` | Consolidated multi-stage hazard profile for a district. | `ApiResponse<DistrictHazardOverviewDto>` |

---

## 3. Response Conventions & Envelopes

### 3.1 Standard API Success Response Envelope (`ApiResponse<T>`)
Used for overview, management, and health endpoints:
```json
{
  "success": true,
  "message": "District hazard overview compiled successfully for Patna",
  "data": {
    "districtId": 24,
    "districtName": "Patna",
    "state": "Bihar",
    "country": "India",
    "hasActiveWeatherStation": true,
    "recordedFloodCount": 0,
    "recordedExtremeRainfallCount": 100,
    "floodHazardScore": null,
    "rainfallHazardScore": 0.7388,
    "multiHazardIndex": 0.7388,
    "severityTier": "HIGH",
    "dominantHazard": "EXTREME_RAINFALL",
    "intersectingMajorRivers": [
      "River Reach #70471714 (Strahler: 5, Length: 42.1km)"
    ],
    "summaryExplanation": "Multi-Hazard Composite Index calculation: [EXTREME_RAINFALL: score=0.7388 (HIGH), eff_w=1.00, contrib=0.7388] -> Final Multi-Hazard Index: 0.7388 (HIGH, Dominant: EXTREME_RAINFALL=0.7388, Completeness: 50%)"
  },
  "timestamp": "2026-08-25T20:45:21.220",
  "meta": {}
}
```

### 3.2 Standard Error Response Envelope (`ApiErrorResponse`)
Produced by `GlobalExceptionHandler` across all controllers:
```json
{
  "timestamp": "2026-08-25T20:45:21.361",
  "status": 404,
  "error": "Not Found",
  "message": "Administrative district not found: UnknownDistrict999",
  "path": "/api/v1/hazards/overview/district/UnknownDistrict999"
}
```

### 3.3 Strict RFC 7946 GeoJSON Standard Adherence
All GeoJSON endpoints (`/geojson`, `/layers/{layerId}`) **strictly omit** generic JSON wrapping envelopes to remain 100% compliant with standard GIS consumers (Leaflet, MapLibre GL, OpenLayers, QGIS):
```json
{
  "type": "FeatureCollection",
  "count": 7,
  "features": [
    {
      "type": "Feature",
      "id": "DFO-3",
      "geometry": {
        "type": "Point",
        "coordinates": [85.5, 26.5]
      },
      "properties": {
        "id": "DFO-3",
        "hazardType": "FLOOD",
        "hazardScore": 0.2226,
        "severityTier": "LOW",
        "associatedDistrict": "Sitamarhi"
      }
    }
  ]
}
```

---

## 4. OpenAPI 3 / Swagger Documentation

* **Swagger UI Documentation:** `http://localhost:8080/swagger-ui/index.html`
* **OpenAPI 3 JSON Specification:** `http://localhost:8080/v3/api-docs`
* **API Title:** *Smart Hazard Risk Prediction & Relocation System - Hazard Intelligence API*
* **Grouped Specifications:** 7 logical OpenAPI groups configured in `OpenApiConfig.java`.

---

## 5. Test Suite & Verification Results

All **138 automated test cases** across 24 test suites execute and pass with **0 failures**:

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
| `HazardApiFacadeTests` | 4 | ✅ PASS | Verified `HazardApiFacade` district intelligence synthesis and system health status. |
| `HazardHealthControllerTests` | 3 | ✅ PASS | Verified health status and district overview REST endpoints. |
| `HazardOpenApiTests` | 2 | ✅ PASS | Verified OpenAPI 3 JSON specification and Swagger UI availability. |
| `HazardApiIntegrationTestSuite` | 4 | ✅ PASS | Verified standardized error codes (400, 404), safety limits, and unwrapped GeoJSON. |
| **Total** | **138** | **✅ PASS** | **100% test success rate across entire backend, GIS, and API surface.** |

---

## 6. Real API Verification Results

* `GET /api/v1/hazards/health` $\to$ **`200 OK`**, status `"UP"`, baseline records `159,005`, active capabilities: 7.
* `GET /api/v1/hazards/overview/district/Patna` $\to$ **`200 OK`**, rainfall score `0.7388` (`HIGH`), multi-hazard index `0.7388` (`HIGH`), active station `true`.
* `GET /api/v1/hazards/overview/district/Sitamarhi` $\to$ **`200 OK`**, flood score `0.2226` (`LOW`), multi-hazard index `0.2226` (`LOW`), dominant `FLOOD`.
* `GET /api/v1/hazards/type/INVALID_TYPE` $\to$ **`400 Bad Request`**, standardized `ApiErrorResponse` payload.
* `GET /api/v1/hazards/DFO-99999` $\to$ **`404 Not Found`**, standardized `ApiErrorResponse` payload.
* `GET /api/v1/hazards/layers/DISTRICT_HAZARD_SUMMARIES` $\to$ **`200 OK`**, RFC 7946 `FeatureCollection` with 38 district polygons.

---

## 7. Exact Boundary Between Stage 3.7 and Stage 3.8

* **Completed in Stage 3.7 (Hazard APIs & Documentation):**
  * Complete API audit and inventory across all 7 controller groups.
  * Application-level facade (`HazardApiFacade`) orchestrating multi-stage services.
  * Standardized response envelopes (`ApiResponse<T>` and `ApiErrorResponse`).
  * SpringDoc OpenAPI 3 / Swagger documentation and UI.
  * Health and consolidated district overview endpoints.
  * 138 passing automated tests with zero failures.
* **Deferred to Stage 3.8 (Hazard Validation & Ground Truth Calibration):**
  * Empirical validation of model hazard scores and multi-hazard indices against historical disaster ground truth (e.g. 2008 Kosi flood, 2019 Patna deluge).
