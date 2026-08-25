# Stage 3.3 — Hazard Normalization Specification

**Project:** Smart Hazard Risk Prediction and Relocation System  
**Event:** Smart India Hackathon (SIH) 2026 | Problem Statement ID: SIH26191  
**Document Status:** Approved & Implemented Sub-Stage 3.3 Specification  
**File Path:** `docs/Stage-3-Hazard-Intelligence/Stage-3.3-Hazard-Normalization.md`  

---

## 1. Executive Summary & Objective

**Stage 3.3 (Hazard Normalization)** builds directly upon the clean and analysis-ready hazard observations established in Stage 3.2. Its core objective is to:

> *"Convert selected processed hazard metrics into comparable normalized values on a standardized, continuous [0.0000, 1.0000] scale, preserving full provenance, scientific reference ranges, and directionality."*

### Key Semantic Principle:
A normalized value of `0.72` represents **relative empirical and physical magnitude within the chosen scientific normalization framework**.
* **It does NOT mean:** 72% probability of a disaster.
* **It does NOT mean:** 72% disaster risk.
* **It does NOT mean:** 72% hazard certainty.

### Architectural Invariants & Scope Boundaries:
* **What Stage 3.3 Delivers:** Deterministic min-max normalization, directionality handling (`INCREASING` vs `DECREASING`), boundary clamping $[0.0000, 1.0000]$, division-by-zero (`min == max`) protection, scientific reference range registry (`HazardMetricNormConfig`), quality-aware filtering, normalized DTOs, domain normalization service, and REST APIs under `/api/v1/hazards/normalized`.
* **What Stage 3.3 Explicitly Defers (Strict Quality Gates):**
  * Final multi-criteria hazard scoring and composite index (Deferred to Stage 3.4).
  * Vulnerability, population exposure, and risk index calculation (Deferred to Stage 4).
  * Safe zone identification and relocation matching (Deferred to Stages 5 and 6).

---

## 2. Inputs from Stage 3.2

Stage 3.3 consumes validated and analysis-ready observations from Stage 3.2:
1. **Cleaned Flood Observations (`DFO`):** Spatially located events with sanitized coordinates, duration in days, affected area in km², displacement density, severity class, and magnitude index.
2. **National Macro Flood Records (`EM-DAT`):** Tabular macro-disaster impact statistics kept unlocated (`UNLOCATED`) without fabricating artificial point coordinates.
3. **Hourly Meteorological Observations (`Open-Meteo`):** Continuous precipitation time-series (131,544 hourly observations across Patna, Muzaffarpur, Bhagalpur) with floored negative values and dynamic district spatial containment.
4. **Aggregated Rainfall Data:** Daily rainfall summaries (`dailyTotalMm`, `peakHourlyMm`, rainy hours) and rolling multi-window metrics (`rolling3hMm`, `rolling6hMm`, `rolling12hMm`, `rolling24hMm`).

---

## 3. Metrics Normalized vs. Metrics Intentionally Not Normalized

| Metric Name | Raw Units | Reference Min ($min$) | Reference Max ($max$) | Method & Direction | Scientific Rationale |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **`HOURLY_PRECIPITATION_MM`** | `mm/hr` | 0.0 | 50.0 | Linear Min-Max (INCREASING) | IMD heavy to very heavy hourly precipitation ceiling. |
| **`DAILY_RAINFALL_MM`** | `mm/day` | 0.0 | 150.0 | Linear Min-Max (INCREASING) | IMD very heavy 24h monsoon rainfall ceiling (0 to 150 mm/day). |
| **`PEAK_HOURLY_RAINFALL_MM`** | `mm/hr` | 0.0 | 50.0 | Linear Min-Max (INCREASING) | Maximum single-hour precipitation intensity within diurnal cycle. |
| **`ROLLING_3H_RAINFALL_MM`** | `mm` | 0.0 | 60.0 | Linear Min-Max (INCREASING) | Flash-flood trigger window accumulation ceiling. |
| **`ROLLING_6H_RAINFALL_MM`** | `mm` | 0.0 | 90.0 | Linear Min-Max (INCREASING) | Short-duration monsoon convective storm accumulation ceiling. |
| **`ROLLING_12H_RAINFALL_MM`** | `mm` | 0.0 | 120.0 | Linear Min-Max (INCREASING) | Sub-daily intense precipitation window ceiling. |
| **`ROLLING_24H_RAINFALL_MM`** | `mm` | 0.0 | 150.0 | Linear Min-Max (INCREASING) | Full-day continuous rolling rainfall accumulation ceiling. |
| **`FLOOD_DURATION_DAYS`** | `days` | 1.0 | 90.0 | Linear Min-Max (INCREASING) | DFO monsoon flood duration range (1 to 90 days). |
| **`FLOOD_AFFECTED_AREA_SQKM`** | `km²` | 0.0 | 500,000.0 | Linear Min-Max (INCREASING) | Regional Gangetic basin inundated footprint area ceiling. |
| **`FLOOD_DISPLACEMENT_DENSITY`**| `people/km²` | 0.0 | 25.0 | Linear Min-Max (INCREASING) | Displaced population density per km² of affected area. |
| **`FLOOD_SEVERITY_INDEX`** | `class` | 1.0 | 2.0 | Linear Min-Max (INCREASING) | DFO qualitative severity class scale (1.0 to 2.0). |
| **`FLOOD_MAGNITUDE_INDEX`** | `index` | 4.0 | 9.0 | Linear Min-Max (INCREASING) | DFO calculated logarithmic flood magnitude index. |

### Metrics Intentionally Not Normalized in Stage 3.3:
* **EM-DAT National Aggregates (`averageDeathsPerEvent`, `averageEconomicLossPerEventUsd`):** Omitted from local spatial hazard scaling because national-scale aggregates cannot be directly mapped to local grid-cell or district hazard scales without inducing aggregation bias.
* **Surface Pressure & Cloud Cover (`surfacePressureHpa`, `cloudCoverPct`):** Retained as auxiliary meteorological attributes but excluded from hazard intensity scaling.

---

## 4. Normalization Formulas & Boundary Handling

### 4.1 Linear Min-Max Normalization
For `INCREASING` direction:
$$\text{norm}_{\text{unclamped}} = \frac{x - x_{\min}}{x_{\max} - x_{\min}}$$

For `DECREASING` direction (inverse scaling):
$$\text{norm}_{\text{unclamped}} = \frac{x_{\max} - x}{x_{\max} - x_{\min}}$$

### 4.2 Deterministic Clamping & Precision
$$\text{norm} = \min(1.0000, \max(0.0000, \text{norm}_{\text{unclamped}}))$$
$$\text{normalizedValue} = \frac{\text{round}(\text{norm} \times 10000)}{10000}$$

### 4.3 Division-by-Zero Protection
If $x_{\min} == x_{\max}$, the engine returns `0.0000` with `clamped = false`, preventing any `NaN` or `Infinity` from propagating.

---

## 5. Normalized Data Model Architecture

```text
NormalizedHazardObservation
  ├── id: String ("DFO-3", "WEATHER-PATNA-100")
  ├── hazardType: HazardType (FLOOD, EXTREME_RAINFALL)
  ├── dataSource: String ("DFO", "OPEN_METEO")
  ├── locationName: String
  ├── associatedDistrict: String ("Sitamarhi", "Patna")
  ├── isWithinBiharBoundary: Boolean (true / false)
  ├── longitude: Double (EPSG:4326)
  ├── latitude: Double (EPSG:4326)
  ├── qualityStatus: QualityStatus (VALID, UNLOCATED, PARTIAL, INVALID)
  ├── normalizedMetrics: Map<String, NormalizedHazardMetric>
  │     ├── metricName: "DAILY_RAINFALL_MM"
  │     ├── rawValue: 101.7
  │     ├── normalizedValue: 0.6780
  │     ├── referenceMin: 0.0
  │     ├── referenceMax: 150.0
  │     ├── units: "mm/day"
  │     ├── direction: INCREASING
  │     ├── method: MIN_MAX
  │     ├── clamped: false
  │     └── referenceRationale: "IMD standard very heavy 24h monsoon rainfall ceiling"
  └── processingMetadata: ProcessingMetadata
```

---

## 6. REST API Specifications (`/api/v1/hazards/normalized`)

| Method | Endpoint | Query Parameters | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/normalized` | `type`, `quality`, `district`, `metric`, `limit` | Lists normalized hazard observations. |
| `GET` | `/api/v1/hazards/normalized/{id}` | - | Retrieves a single normalized hazard observation by ID. |
| `GET` | `/api/v1/hazards/normalized/metric/{metricName}`| `limit` | Filters observations containing a specific normalized metric. |
| `GET` | `/api/v1/hazards/normalized/district/{districtName}` | `type`, `limit` | Retrieves normalized observations for a specific administrative district. |
| `GET` | `/api/v1/hazards/normalized/rainfall/daily` | `stationName`, `startDate`, `endDate` | Retrieves normalized daily rainfall summaries (`normalizedDailyTotal`, `normalizedPeakHourly`). |
| `GET` | `/api/v1/hazards/normalized/rainfall/rolling` | `stationName`, `targetTime` | Retrieves multi-window normalized rolling rainfall metrics (3h, 6h, 12h, 24h). |
| `GET` | `/api/v1/hazards/normalized/summary` | - | Executive catalog summary describing all configured normalization reference ranges. |
| `GET` | `/api/v1/hazards/normalized/geojson` | `type`, `district`, `limit` | Delivers verified normalized hazards as RFC 7946 GeoJSON `FeatureCollection`. |

---

## 7. Test Suite & Verification Results

All **75 automated test cases** across 10 test suites execute and pass with **0 failures**:

| Test Suite | Tests Run | Result | Key Verified Functionality |
| :--- | :---: | :---: | :--- |
| `HazardApplicationTests` | 7 | ✅ PASS | Verified all 11 Stage 2 base tables (159,005 rows); regression-free baseline. |
| `HazardDomainModelTests` | 5 | ✅ PASS | Verified `HazardType` parsing, `HazardDataMapper`, RFC 7946 GeoJSON. |
| `HazardIntegrationServiceTests` | 9 | ✅ PASS | Verified Stage 3.1 PostGIS spatial queries, bounding boxes, proximity. |
| `HazardIntegrationControllerTests` | 10 | ✅ PASS | Verified Stage 3.1 REST API endpoints and validation error handling. |
| `HazardDataCleanerTests` | 6 | ✅ PASS | Verified sentinel coordinate cleaning, negative metric flooring, duration calculation. |
| `HazardProcessingServiceTests` | 7 | ✅ PASS | Verified Stage 3.2 processing pipeline, daily aggregation, rolling accumulation. |
| `HazardProcessingControllerTests` | 8 | ✅ PASS | Verified Stage 3.2 REST API endpoints, daily/rolling feeds, GeoJSON outputs. |
| `HazardNormalizationEngineTests` | 9 | ✅ PASS | Verified value at min $\to 0.0$, max $\to 1.0$, midpoint $\to 0.5$, boundary clamping, min==max safety, inverse direction. |
| `HazardNormalizationServiceTests` | 6 | ✅ PASS | Verified live normalization of real PostGIS data (Patna 101.7mm $\to 0.6780$, rolling 24h $\to 0.6907$, DFO severity $\to 0.5000$). |
| `HazardNormalizationControllerTests`| 8 | ✅ PASS | Verified Stage 3.3 REST API endpoints, metric filtering, GeoJSON vector layers. |
| **Total** | **75** | **✅ PASS** | **100% test success rate across backend, GIS, and normalization layers.** |

---

## 8. Real-Data Normalization Results

* **Daily Rainfall Extreme (Patna on 2020-06-29):**
  * Raw Daily Total: `101.7 mm` $\to$ Normalized Daily Total: **`0.6780`** on $[0.0, 150.0 \text{ mm/day}]$ scale.
  * Raw Peak Hourly: `41.5 mm/hr` $\to$ Normalized Peak Hourly: **`0.8300`** on $[0.0, 50.0 \text{ mm/hr}]$ scale.
* **Rolling 24h Monsoon Peak (Patna storm window):**
  * Raw 24h Rolling Total: `103.6 mm` $\to$ Normalized 24h Rolling: **`0.6907`** on $[0.0, 150.0 \text{ mm}]$ scale.
* **Historical Flood Event (DFO-3 Sitamarhi):**
  * Raw Severity: `1.5` $\to$ Normalized Severity: **`0.5000`** on $[1.0, 2.0]$ class scale.
  * Raw Duration: `4.0 days` $\to$ Normalized Duration: **`0.0337`** on $[1.0, 90.0 \text{ days}]$ scale.
* **Total Configured Reference Metrics:** 12 standard scientific indicators.
* **Spatial Normalization:** 100% WGS 84 (`EPSG:4326`) coordinate and district containment preserved.

---

## 9. Exact Boundary Between Stage 3.3 and Stage 3.4

* **Completed in Stage 3.3 (Hazard Normalization):**
  * Independent conversion of raw/processed metrics into standardized $[0.0000, 1.0000]$ scales.
  * Preserving provenance, directionality, clamping flags, reference ranges, and spatial associations.
  * REST APIs exposing normalized metric vectors.
* **Deferred to Stage 3.4 (Hazard Scoring & Index Generation):**
  * Multi-criteria weighted aggregation (e.g. $S_{\text{flood}} = w_1 \cdot \text{norm}_{\text{dur}} + w_2 \cdot \text{norm}_{\text{area}} + \dots$).
  * Composite hazard index calculation combining flood and extreme rainfall hazards.
  * Categorical hazard classification tiers (Low, Medium, High, Severe).
