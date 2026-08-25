# Stage 3.1 — Hazard Data Integration Specification

**Project:** Smart Hazard Risk Prediction and Relocation System  
**Event:** Smart India Hackathon (SIH) 2026 | Problem Statement ID: SIH26191  
**Document Status:** Approved & Implemented Sub-Stage 3.1 Specification  
**File Path:** `docs/Stage-3-Hazard-Intelligence/Stage-3.1-Hazard-Data-Integration.md`  

---

## 1. Executive Summary & Objective

**Stage 3.1 (Hazard Data Integration)** forms the foundational integration layer of **Stage 3 — Hazard Intelligence**. Its core objective is to:

> *"Bring existing hazard-related data into a clean, consistent, application-level hazard domain that can be consumed by the future Hazard Processing, Normalization, Scoring and Prediction layers."*

In accordance with architectural principles:
* **No Rebuilding of Stage 2:** 100% reuse of PostgreSQL 17 / PostGIS 3.6 (`hazard_db`), 6 domain schemas, 11 base tables, 159,005 records, canonical EPSG:4326 CRS, and spatial GiST indexes.
* **No Premature Risk/Prediction Logic:** Stage 3.1 strictly integrates hazard observations. Risk calculation, vulnerability scoring, population exposure, red zones, ML predictions, and multi-hazard aggregation are deferred to Stages 3.2, 4, 5, and 6.

---

## 2. Audit of Existing Stage 2 Foundation

| Component | Stage 2 Existing Asset | Reuse / Integration Strategy |
| :--- | :--- | :--- |
| **Database Engine** | PostgreSQL 17.11 / PostGIS 3.6.4 in `hazard_db` | Zero schema modifications or truncations; accessed read-only via HikariCP. |
| **Canonical CRS** | WGS 84 (`EPSG:4326`) | All coordinates, bounding boxes, and GeoJSON outputs strictly adhere to `EPSG:4326`. |
| **DS-001 Historical Flood Events** | `hazard.dfo_flood_events` (23 Bihar events) | Mapped to `IntegratedHazardEvent` (`HazardType.FLOOD`, source `"DFO"`). |
| **DS-001 National Flood Records** | `hazard.emdat_flood_records` (53 India records) | Mapped to `IntegratedHazardEvent` (`HazardType.FLOOD`, source `"EM_DAT"`). |
| **DS-002 Meteorological Observations** | `weather.hourly_weather` (131,544 hourly rows) | High-intensity observations (>10 mm/hr) mapped to `IntegratedHazardEvent` (`HazardType.EXTREME_RAINFALL`). |
| **DS-005 Administrative Boundaries** | `boundaries.district_boundaries` (38 Bihar districts) | Used for spatial polygon containment and intersection (`ST_Intersects`). |
| **Spatial Indexes** | PostGIS GiST on `geom` columns | Leveraged for sub-millisecond point-in-polygon and distance calculations (`ST_DWithin`, `ST_MakeEnvelope`). |

---

## 3. Architecture & Data Flow

```text
┌──────────────────────────────────────────────────────────────────────────────────┐
│                   POSTGIS SPATIAL DATABASE (hazard_db: 159,005 rows)             │
│   • hazard.dfo_flood_events (23)           • hazard.emdat_flood_records (53)     │
│   • weather.hourly_weather (131,544)       • boundaries.district_boundaries (38) │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                                         │ Spring Data JPA / Hibernate Spatial / JTS
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             REPOSITORIES & SPATIAL QUERIES                       │
│   • DfoFloodEventRepository (ST_Intersects, ST_DWithin, ST_MakeEnvelope)         │
│   • EmdatFloodRecordRepository (findByYearBetween, findByCountry)                │
│   • HourlyWeatherRepository (findExtremeRainfallEvents, findInBoundingBox)       │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                                         │ Entity Entities
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                       HAZARD INTEGRATION MAPPER (HazardDataMapper)               │
│   • Maps DFO Flood -> IntegratedHazardEvent (Preserves GLIDE, severity, geom)   │
│   • Maps EM-DAT Record -> IntegratedHazardEvent (Preserves economic damage, CPI) │
│   • Maps Weather -> IntegratedHazardEvent (Derived intensity index, mm/hr)       │
│   • Converts to GeoJSON Feature & FeatureCollection (RFC 7946 standard)          │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                                         │ Clean Domain DTOs
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                   HAZARD INTEGRATION SERVICE (HazardIntegrationService)          │
│   • Unified ID resolution ("DFO-1", "EMDAT-5", "WEATHER-PATNA-100")              │
│   • Spatial filtering (by district, coordinate proximity, bounding box)          │
│   • Temporal filtering & Extreme rainfall threshold extraction                   │
│   • Catalog metadata generation & Input validation                               │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                                         │ REST Payloads
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                   HAZARD REST CONTROLLER (/api/v1/hazards)                       │
│   • GET /api/v1/hazards                     • GET /api/v1/hazards/{id}           │
│   • GET /api/v1/hazards/type/{type}         • GET /api/v1/hazards/district/{name}│
│   • GET /api/v1/hazards/nearby              • GET /api/v1/hazards/bbox           │
│   • GET /api/v1/hazards/time-range          • GET /api/v1/hazards/rainfall/extreme│
│   • GET /api/v1/hazards/geojson             • GET /api/v1/hazards/summary        │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Hazard Domain Model & DTOs

### 4.1 HazardType Enumeration
Controlled, extensible representation of activated natural hazard phenomena:
* `FLOOD`: Historical flood inundations, riverine overtopping, and flash floods.
* `EXTREME_RAINFALL`: High-intensity meteorological precipitation observations.
* `OTHER`: Extensible fallback for future datasets (Landslide, Earthquake, Cyclone).

### 4.2 IntegratedHazardEvent
Unified application-level DTO representing integrated hazard events across heterogeneous sources:
* `id` (`String`): Globally unique identifier across sources (e.g. `"DFO-1"`, `"EMDAT-5"`, `"WEATHER-PATNA-100"`).
* `sourceRecordId` (`Object`): Original numeric identifier in the primary database table.
* `hazardType` (`HazardType`): `FLOOD`, `EXTREME_RAINFALL`, `OTHER`.
* `dataSource` (`String`): Origin provenance (`"DFO"`, `"EM_DAT"`, `"OPEN_METEO"`).
* `locationName` (`String`): Human-readable location description or station name.
* `country` (`String`): Country name / ISO code.
* `longitude` / `latitude` (`Double`): WGS 84 geographic coordinates (EPSG:4326).
* `startDate` / `endDate` (`LocalDate`): Event start and end dates.
* `timestamp` (`LocalDateTime`): High-resolution timestamp for meteorological observations.
* `severity` (`Double`): Standardized intensity / severity metric.
* `magnitude` (`Double`): Observational magnitude where recorded.
* `durationDays` (`Double`): Duration in days.
* `displacedPopulation` (`Double`): Number of persons displaced.
* `fatalities` (`Double`): Fatality count.
* `affectedAreaSqKm` (`Double`): Affected area footprint in square kilometers.
* `economicDamageUsd` (`Double`): Estimated damage in USD.
* `precipitationMm` (`Double`): Hourly rainfall intensity (mm).
* `externalReference` (`String`): GLIDE number, ISO code, or weather station identifier.
* `metadata` (`Map<String, Object>`): Extensible key-value pairs (cause, affected rivers, CPI, notes).

### 4.3 GeoJSON Standards (RFC 7946)
* `GeoJsonFeatureCollectionDto`: Top-level GeoJSON container with `type: "FeatureCollection"`, `crs: "urn:ogc:def:crs:OGC:1.3:CRS84"`, and `count`.
* `GeoJsonFeatureDto`: Feature with `geometry` (`Point` in EPSG:4326) and strongly-typed `properties`.

---

## 5. REST API Specifications

| Method | Endpoint | Query Parameters | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards` | `type`, `limit` | Lists integrated hazard events. |
| `GET` | `/api/v1/hazards/{id}` | - | Retrieves a specific hazard event by unified ID (e.g. `DFO-1`, `EMDAT-1`). |
| `GET` | `/api/v1/hazards/type/{type}` | `limit` | Filters hazards strictly by type (`FLOOD`, `EXTREME_RAINFALL`). |
| `GET` | `/api/v1/hazards/district/{districtName}` | `type`, `limit` | Spatial intersection query within a Bihar administrative district. |
| `GET` | `/api/v1/hazards/nearby` | `longitude`, `latitude`, `radiusMeters`, `type`, `limit` | Spatial proximity query using `ST_DWithin`. |
| `GET` | `/api/v1/hazards/bbox` | `minLon`, `minLat`, `maxLon`, `maxLat`, `type`, `limit` | Spatial bounding box query using `ST_MakeEnvelope`. |
| `GET` | `/api/v1/hazards/time-range` | `startDate`, `endDate`, `type`, `limit` | Temporal query for events within a date window. |
| `GET` | `/api/v1/hazards/rainfall/extreme` | `thresholdMm`, `start`, `end`, `limit` | Meteorological hazard query for extreme rainfall observations. |
| `GET` | `/api/v1/hazards/geojson` | `type`, `district`, `limit` | Standard GeoJSON FeatureCollection for direct Leaflet/Mapbox map layers. |
| `GET` | `/api/v1/hazards/summary` | - | Executive catalog summary detailing dataset row counts and CRS metadata. |

---

## 6. Verification & Test Suite

All 31 unit, spatial, service, and controller tests pass with 0 failures:

| Test Class | Tests Run | Result | Key Verified Functionality |
| :--- | :---: | :---: | :--- |
| `HazardApplicationTests` | 7 | ✅ PASS | All 11 Stage 2 base tables verified (159,005 rows); regression-free baseline. |
| `HazardDomainModelTests` | 5 | ✅ PASS | `HazardType` parsing, `HazardDataMapper` mappings for DFO/EM-DAT/Weather, GeoJSON serialization. |
| `HazardIntegrationServiceTests` | 9 | ✅ PASS | Unified ID lookups, district intersections, `ST_DWithin` proximity, `ST_MakeEnvelope` bbox, extreme rainfall, catalog summary. |
| `HazardIntegrationControllerTests` | 10 | ✅ PASS | REST HTTP 200/400/404 responses, JSON/GeoJSON schema compliance, input validation. |
| **Total** | **31** | **✅ PASS** | **100% test success rate across backend and GIS layers.** |

---

## 7. Boundary & Transition to Stage 3.2

* **Completed in Stage 3.1:**
  * Clean, unified Hazard domain DTO representation (`IntegratedHazardEvent`).
  * Extensible `HazardType` representation (`FLOOD`, `EXTREME_RAINFALL`, `OTHER`).
  * Heterogeneous data mapping preserving original provenance, IDs, coordinates, and metrics.
  * Spatial query capabilities (District `ST_Intersects`, Proximity `ST_DWithin`, Bounding box `ST_MakeEnvelope`).
  * Production-grade REST API (`/api/v1/hazards`) with RFC 7946 GeoJSON output.
  * Validation rules and `@RestControllerAdvice` error handling.
* **Remaining for Stage 3.2 (Hazard Processing):**
  * Hazard intensity normalization algorithms (0.0 to 1.0 standard scaling).
  * Spatial hazard grid generation (raster-to-grid aggregation).
  * Multi-hazard index computation (combining flood history + extreme rainfall frequency + stream order buffers).
  * Continuous hazard surface interpolation (IDW / Kriging or raster zonal stats).
