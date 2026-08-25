# Stage 3.2 — Hazard Processing Specification

**Project:** Smart Hazard Risk Prediction and Relocation System  
**Event:** Smart India Hackathon (SIH) 2026 | Problem Statement ID: SIH26191  
**Document Status:** Approved & Implemented Sub-Stage 3.2 Specification  
**File Path:** `docs/Stage-3-Hazard-Intelligence/Stage-3.2-Hazard-Processing.md`  

---

## 1. Executive Summary & Objective

**Stage 3.2 (Hazard Processing)** builds directly upon the integration layer of Stage 3.1. Its core objective is to:

> *"Transform integrated raw/source-level hazard observations into clean, consistent, analysis-ready hazard observations."*

### Architectural Invariants & Scope Boundaries:
* **What Stage 3.2 Delivers:** Data cleaning, sentinel coordinate removal, negative metric flooring, temporal aggregation (daily rainfall totals, peak hourly intensities, rolling multi-window accumulation), dynamic spatial district association (`ST_Contains`), data quality classification (`QualityStatus`), and analysis-ready DTO models.
* **What Stage 3.2 Explicitly Deferres (Strict Quality Gates):**
  * Final hazard normalization to 0.00–1.00 scale (Deferred to Stage 3.3).
  * Final hazard scoring and composite multi-hazard index (Deferred to Stage 3.4).
  * Vulnerability, population exposure, and disaster risk calculation (Deferred to Stage 4).
  * Red zone polygon generation and relocation matching (Deferred to Stages 5 and 6).

---

## 2. Input Datasets & Real-Data Audit

| Dataset ID | Source Table | Raw Record Count | Quality Status Distribution | Processing Actions & Anomalies Handled |
| :--- | :--- | :--- | :--- | :--- |
| **DS-001** | `hazard.dfo_flood_events` | 23 | • 7 `VALID` (Spatially Located in Bihar)<br>• 16 `UNLOCATED` (Sentinel Cleaned) | 16 records contained `-1.797e+308` sentinel coordinates in raw shapefile; sanitized coordinates to `null` with `UNLOCATED` status. Reconciled event duration `(ended_date - began_date + 1)`. Derived displacement density and fatality ratios. |
| **DS-001** | `hazard.emdat_flood_records` | 53 | • 53 `UNLOCATED` (National Tabular) | National-level macro impact records. Maintained coordinates as `null` without fabricating fake point locations. Derived average deaths/event, affected/event, and economic damage/event. |
| **DS-002** | `weather.hourly_weather` | 131,544 | • 131,544 `VALID` (Patna, Muzaffarpur, Bhagalpur) | Verified timestamps and coordinates. Floored negative precipitation at `0.0 mm`. Aggregated into daily summaries, peak hourly intensities, and rolling 3h, 6h, 12h, and 24h accumulation metrics. |
| **Total** | - | **131,620** | • **131,551 `VALID`**<br>• **69 `UNLOCATED`** | **100% deterministic, zero data loss, zero destructive table modifications.** |

---

## 3. Processing Pipeline Architecture

```text
┌──────────────────────────────────────────────────────────────────────────────────┐
│              STAGE 3.1 INTEGRATED HAZARD DATA (131,620 records)                  │
│   • DFO Historical Flood Events (23)                                             │
│   • EM-DAT Macro Impact Records (53)                                             │
│   • Open-Meteo Hourly Weather Time-Series (131,544)                              │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                  DETERMINISTIC DATA CLEANING (HazardDataCleaner)                 │
│   • Coordinate Validation: [-180..180], [-90..90]                                │
│   • Sentinel Sanitization: -1.797e+308 -> null with UNLOCATED status             │
│   • Numeric Sanitization: Floored negative values (precipitation, deaths >= 0)   │
│   • Temporal Reconciliation: Calculated duration (ended_date - began_date + 1)   │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│              DYNAMIC SPATIAL ASSOCIATION (SpatialAssociationService)             │
│   • PostGIS ST_Contains Point-in-Polygon containment                             │
│   • Associates coordinates with Bihar 38 District Boundaries                     │
│   • Flags isWithinBiharBoundary and spatialResolutionStatus                      │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│           TEMPORAL RAINFALL AGGREGATION (TemporalRainfallAggregator)             │
│   • Daily Total Rainfall (SUM(precipitation_mm)) & Peak Hourly Intensity         │
│   • Multi-Window Rolling Accumulation: 3h, 6h, 12h, 24h                          │
│   • IMD Rainfall Thresholds: Heavy (>=15mm/hr, >=64.5mm/day), Very Heavy (>=35)  │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│               QUALITY CLASSIFICATION & DERIVED METRIC ENRICHMENT                 │
│   • QualityStatus: VALID, UNLOCATED, PARTIAL, INVALID                            │
│   • ProcessingMetadata: Audit trail, cleaning actions, anomalies detected        │
│   • Derived Metrics: displacementDensity, fatalityRate, rolling windows          │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                 ANALYSIS-READY HAZARD REST APIS (/api/v1/hazards/processed)      │
│   • GET /api/v1/hazards/processed               • GET /.../processed/{id}        │
│   • GET /.../processed/quality/{status}         • GET /.../district/{district}   │
│   • GET /.../processed/rainfall/daily           • GET /.../rainfall/rolling      │
│   • GET /.../processed/quality-summary          • GET /.../processed/geojson     │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Key Processed Hazard Attributes & Models

### 4.1 `QualityStatus` Classification
* `VALID`: Complete, spatially located in EPSG:4326, verified timestamps and metrics.
* `UNLOCATED`: Valid tabular data lacking discrete geographic coordinates (e.g. EM-DAT records, cleaned DFO sentinels).
* `PARTIAL`: Valid observation containing partial or approximated fields.
* `INVALID`: Corrupt or out-of-bounds record failing critical validation rules.

### 4.2 `ProcessedHazardObservation`
Unified analysis-ready representation containing:
* Core identifiers: `id` (e.g. `"DFO-3"`, `"EMDAT-1"`, `"WEATHER-PATNA-100"`), `sourceRecordId`, `dataSource`.
* Spatial attributes: `associatedDistrict`, `isWithinBiharBoundary`, `longitude`, `latitude`.
* Temporal attributes: `startDate`, `endDate`, `timestamp`, `durationDays`.
* Impact metrics: `severity`, `magnitude`, `displacedPopulation`, `fatalities`, `affectedAreaSqKm`, `economicDamageUsd`, `precipitationMm`.
* Audit & Metadata: `qualityStatus`, `rawAttributes`, `derivedMetrics`, `processingMetadata`.

### 4.3 `DailyRainfallSummary`
* Station name, observation date, associated district, `dailyTotalMm`, `peakHourlyMm`, `rainyHours`, `heavyRainHours`, `veryHeavyRainHours`, `exceedsHeavyThreshold`.

### 4.4 `RollingRainfallMetrics`
* Current hourly intensity, `rolling3hMm`, `rolling6hMm`, `rolling12hMm`, `rolling24hMm`, `isHeavyRainfall`, `isVeryHeavyRainfall`.

---

## 5. REST API Specifications (`/api/v1/hazards/processed`)

| Method | Endpoint | Query Parameters | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/processed` | `type`, `quality`, `district`, `limit` | Lists analysis-ready processed hazard observations. |
| `GET` | `/api/v1/hazards/processed/{id}` | - | Retrieves a single processed hazard observation by ID. |
| `GET` | `/api/v1/hazards/processed/quality/{status}` | `limit` | Filters observations strictly by `QualityStatus` (`VALID`, `UNLOCATED`, `PARTIAL`, `INVALID`). |
| `GET` | `/api/v1/hazards/processed/district/{districtName}` | `type`, `limit` | Retrieves processed observations for a specific administrative district. |
| `GET` | `/api/v1/hazards/processed/rainfall/daily` | `stationName`, `startDate`, `endDate` | Retrieves aggregated daily rainfall summaries. |
| `GET` | `/api/v1/hazards/processed/rainfall/rolling` | `stationName`, `targetTime` | Retrieves multi-window rolling rainfall accumulation metrics (3h, 6h, 12h, 24h). |
| `GET` | `/api/v1/hazards/processed/quality-summary` | - | Executive processing and data quality audit report detailing cleaning actions. |
| `GET` | `/api/v1/hazards/processed/geojson` | `type`, `district`, `limit` | Delivers verified, spatially located processed hazards as RFC 7946 GeoJSON. |

---

## 6. Test Suite & Verification Results

All **52 automated test cases** across 7 test suites execute and pass with **0 failures**:

| Test Suite | Tests Run | Result | Key Verified Functionality |
| :--- | :---: | :---: | :--- |
| `HazardApplicationTests` | 7 | ✅ PASS | Verified all 11 Stage 2 base tables (159,005 rows); regression-free baseline. |
| `HazardDomainModelTests` | 5 | ✅ PASS | Verified `HazardType` parsing, `HazardDataMapper`, RFC 7946 GeoJSON. |
| `HazardIntegrationServiceTests` | 9 | ✅ PASS | Verified Stage 3.1 PostGIS spatial queries, bounding boxes, proximity. |
| `HazardIntegrationControllerTests` | 10 | ✅ PASS | Verified Stage 3.1 REST API endpoints and validation error handling. |
| `HazardDataCleanerTests` | 6 | ✅ PASS | Verified sentinel coordinate cleaning, negative metric flooring, duration calculation. |
| `HazardProcessingServiceTests` | 7 | ✅ PASS | Verified DFO/EM-DAT/Weather processing, daily aggregation, rolling accumulation, quality summary. |
| `HazardProcessingControllerTests` | 8 | ✅ PASS | Verified Stage 3.2 REST API endpoints, daily/rolling rainfall feeds, GeoJSON outputs. |
| **Total** | **52** | **✅ PASS** | **100% test success rate across backend and GIS layers.** |

---

## 7. Real-Data Verification Metrics

* **Total Source Records Processed:** 131,620 (23 DFO + 53 EM-DAT + 131,544 Weather).
* **Valid Located Records:** 131,551 (7 DFO flood events + 131,544 Weather observations).
* **Unlocated Cleaned Records:** 69 (16 DFO flood events with cleaned sentinel coordinates + 53 EM-DAT national macro records).
* **Anomalies Cleaned:** 16 sentinel coordinates (`-1.797e+308`) converted to `null` with `UNLOCATED` quality status.
* **Maximum Daily Rainfall Recorded:** 101.7 mm (Patna Station on 2020-06-29 with 41.5 mm/hr peak hourly intensity).
* **Maximum 24h Rolling Accumulation:** 111.1 mm (Patna Station during 2020 monsoon storm window).
* **Districts Spatially Covered:** All 38 Bihar Administrative Districts.

---

## 8. Exact Boundary Between Stage 3.2 and Stage 3.3

* **Completed in Stage 3.2:** POST-integration data cleaning, sentinel coordinate removal, negative metric sanitization, duration reconciliation, daily and rolling rainfall aggregations, dynamic district spatial containment (`ST_Contains`), `QualityStatus` classification, analysis-ready DTOs, and REST APIs.
* **Deferred to Stage 3.3 (Hazard Normalization):** Multi-criteria hazard metric scaling (min-max normalization to 0.00–1.00), parameter distribution curve fitting, extreme rainfall return period normalization, and standardized hazard index generation.
