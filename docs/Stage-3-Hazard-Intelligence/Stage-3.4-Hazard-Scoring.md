# Stage 3.4 — Single-Hazard Scoring & Severity Classification Specification

**Project:** Smart Hazard Risk Prediction and Relocation System  
**Event:** Smart India Hackathon (SIH) 2026 | Problem Statement ID: SIH26191  
**Document Status:** Approved & Implemented Sub-Stage 3.4 Specification  
**File Path:** `docs/Stage-3-Hazard-Intelligence/Stage-3.4-Hazard-Scoring.md`  

---

## 1. Executive Summary & Objective

**Stage 3.4 (Hazard Scoring)** converts multiple normalized indicators belonging to the **SAME** hazard type into a single, interpretable, and mathematically rigorous **Hazard Score** $\in [0.0000, 1.0000]$ and assigns a deterministic **Severity Tier** (`LOW`, `MODERATE`, `HIGH`, `SEVERE`).

$$\text{Hazard Score} = \sum_{i \in \text{available}} \left(\text{normalizedMetric}_i \times \text{effectiveWeight}_i\right)$$

### Key Conceptual Guardrails:
* **Single-Hazard Focus:** Combines indicators within `FLOOD` or within `EXTREME_RAINFALL` independently.
* **It does NOT combine:** `FLOOD` + `EXTREME_RAINFALL` into a multi-hazard composite (strictly deferred to Stage 3.5).
* **It does NOT represent:** Overall disaster risk, population exposure, vulnerability, or evacuation prioritization (strictly deferred to Stage 4+).
* **Labeling:** Clearly identified as an internal multi-criteria **Hazard Score**, not an official government prediction.

---

## 2. Input from Stage 3.3

Stage 3.4 consumes standardized $[0.0000, 1.0000]$ normalized indicators produced by Stage 3.3:
1. **Flood Indicators:** `FLOOD_SEVERITY_INDEX`, `FLOOD_MAGNITUDE_INDEX`, `FLOOD_AFFECTED_AREA_SQKM`, `FLOOD_DURATION_DAYS`, `FLOOD_DISPLACEMENT_DENSITY`.
2. **Rainfall Indicators:** `HOURLY_PRECIPITATION_MM`, `DAILY_RAINFALL_MM`, `PEAK_HOURLY_RAINFALL_MM`, `ROLLING_3H_RAINFALL_MM`, `ROLLING_6H_RAINFALL_MM`, `ROLLING_12H_RAINFALL_MM`, `ROLLING_24H_RAINFALL_MM`.
3. **Spatial & Temporal Metadata:** Administrative district association (Bihar 38 districts), WGS 84 (`EPSG:4326`) geometries, and `QualityStatus`.

---

## 3. Hazard-Specific Weight Configurations & Rationale

Weights for every hazard type must sum to exactly **1.0000** ($\pm 10^{-4}$ tolerance), enforced at configuration and test time.

### 3.1 Flood Hazard Scoring (`FLOOD`)

| Indicator Name | Configured Weight ($w_i$) | Normalization Reference | Domain Rationale |
| :--- | :---: | :---: | :--- |
| **`FLOOD_SEVERITY_INDEX`** | **0.30** (30%) | $[1.0, 2.0]$ | Dartmouth Flood Observatory (DFO) physical/structural severity class. Primary determinant of destructive power. |
| **`FLOOD_MAGNITUDE_INDEX`**| **0.25** (25%) | $[4.0, 9.0]$ | Logarithmic composite of duration, severity, and inundated area ($\log_{10}(\text{dur} \times \text{sev} \times \text{area})$). |
| **`FLOOD_AFFECTED_AREA_SQKM`** | **0.25** (25%) | $[0.0, 500,000.0 \text{ km²}]$ | Spatial footprint of the inundation zone across the Gangetic floodplains. |
| **`FLOOD_DURATION_DAYS`** | **0.20** (20%) | $[1.0, 90.0 \text{ days}]$ | Prolonged submergence and waterlogging persistence duration. |
| **Total Weight** | **1.0000** | - | **Balanced multi-criteria physical flood severity representation.** |

### 3.2 Extreme Rainfall Hazard Scoring (`EXTREME_RAINFALL`)

| Indicator Name | Configured Weight ($w_i$) | Normalization Reference | Domain Rationale |
| :--- | :---: | :---: | :--- |
| **`HOURLY_PRECIPITATION_MM`** / **`PEAK_HOURLY`** | **0.40** (40%) | $[0.0, 50.0 \text{ mm/hr}]$ | Instantaneous convective intensity; primary driver of flash floods and drainage overflow. |
| **`DAILY_RAINFALL_MM`** / **`ROLLING_24H`** | **0.35** (35%) | $[0.0, 150.0 \text{ mm/day}]$ | Diurnal total accumulation; primary driver of soil saturation and regional waterlogging. |
| **`ROLLING_6H_RAINFALL_MM`** | **0.25** (25%) | $[0.0, 90.0 \text{ mm}]$ | Intermediate storm burst window; critical flash-flood precursor window. |
| **Total Weight** | **1.0000** | - | **Captures both peak intensity burst and cumulative volume.** |

---

## 4. Missing Metric Handling & Effective Weight Recalculation

If an observation is missing one or more optional indicators, the system **does not assume zero**. Instead, it dynamically recalibrates the effective weights among the available eligible metrics:

$$\text{effectiveWeight}_i = \frac{w_i}{\sum_{j \in \text{available}} w_j}$$

$$\text{Hazard Score} = \sum_{i \in \text{available}} \left(\text{normalizedValue}_i \times \text{effectiveWeight}_i\right)$$

$$\text{Completeness Ratio} = \frac{|\text{available metrics}|}{|\text{configured metrics}|}$$

* **If zero eligible metrics are available:** $\text{Hazard Score} = \text{null}$, $\text{Completeness} = 0.0\%$.

---

## 5. Severity Tier Classification

Scores in $[0.0000, 1.0000]$ are mapped to presentation severity tiers using deterministic domain thresholds:

| Severity Tier | Numerical Score Range | Color Hex Code | Description |
| :--- | :---: | :---: | :--- |
| **`LOW`** | $[0.0000, 0.2499]$ | `#4CAF50` (Green) | Minor operational concern; baseline conditions. |
| **`MODERATE`** | $[0.2500, 0.4999]$ | `#FFC107` (Amber) | Elevated monitoring recommended; localized impact potential. |
| **`HIGH`** | $[0.5000, 0.7499]$ | `#FF9800` (Orange) | Significant physical hazard potential; intense storm/flood conditions. |
| **`SEVERE`** | $[0.7500, 1.0000]$ | `#F44336` (Red) | Critical extreme event conditions; major inundation/storm burst. |

---

## 6. Score Representation & Explanation Model

Every score produced includes a full transparent mathematical explanation:

```text
HazardScoreDto
  ├── id: "DFO-3"
  ├── hazardType: FLOOD
  ├── associatedDistrict: "Sitamarhi"
  ├── isWithinBiharBoundary: true
  ├── hazardScore: 0.2226
  ├── severityTier: LOW
  ├── completenessRatio: 1.00 (100%)
  ├── scoringMethod: "WEIGHTED_MULTI_CRITERIA_HAZARD_INDEX"
  ├── explanation: "FLOOD Hazard Score calculation: [FLOOD_SEVERITY_INDEX: norm=0.5000, eff_w=0.30, contrib=0.1500] [FLOOD_MAGNITUDE_INDEX: norm=0.2209, eff_w=0.25, contrib=0.0552] [FLOOD_AFFECTED_AREA_SQKM: norm=0.0424, eff_w=0.25, contrib=0.0106] [FLOOD_DURATION_DAYS: norm=0.0337, eff_w=0.20, contrib=0.0067] -> Final Score: 0.2226 (LOW, completeness: 100%)"
  └── metricContributions: List<MetricContributionDto>
        ├── FLOOD_SEVERITY_INDEX: norm=0.5000, weight=0.30, contrib=0.1500
        ├── FLOOD_MAGNITUDE_INDEX: norm=0.2209, weight=0.25, contrib=0.0552
        ├── FLOOD_AFFECTED_AREA_SQKM: norm=0.0424, weight=0.25, contrib=0.0106
        └── FLOOD_DURATION_DAYS: norm=0.0337, weight=0.20, contrib=0.0067
```

---

## 7. REST API Specifications (`/api/v1/hazards/scores`)

| Method | Endpoint | Query Parameters | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/scores` | `type`, `district`, `severity`, `quality`, `limit` | Lists scored hazard observations. |
| `GET` | `/api/v1/hazards/scores/{id}` | - | Retrieves a single scored hazard observation by unified ID. |
| `GET` | `/api/v1/hazards/scores/type/{type}` | `district`, `limit` | Retrieves hazard scores filtered by type (`FLOOD`, `EXTREME_RAINFALL`). |
| `GET` | `/api/v1/hazards/scores/district/{districtName}` | `type`, `severity`, `limit` | Retrieves hazard scores for an administrative district. |
| `GET` | `/api/v1/hazards/scores/rainfall/daily` | `stationName`, `startDate`, `endDate` | Retrieves scored daily meteorological rainfall observations. |
| `GET` | `/api/v1/hazards/scores/rainfall/rolling` | `stationName`, `targetTime` | Retrieves scored multi-window rolling rainfall metrics. |
| `GET` | `/api/v1/hazards/scores/summary` | - | Executive catalog summary describing score and severity tier distributions. |
| `GET` | `/api/v1/hazards/scores/geojson` | `type`, `district`, `limit` | Delivers scored hazards as RFC 7946 GeoJSON `FeatureCollection` with `hazardScore` and `severityTier` properties. |

---

## 8. Test Suite & Verification Results

All **95 automated test cases** across 13 test suites pass with **0 failures**:

| Test Suite | Tests Run | Result | Key Verified Functionality |
| :--- | :---: | :---: | :--- |
| `HazardApplicationTests` | 7 | ✅ PASS | Verified all 11 Stage 2 base tables (159,005 rows); regression-free baseline. |
| `HazardDomainModelTests` | 5 | ✅ PASS | Verified `HazardType`, `SeverityTier`, `HazardDataMapper`, GeoJSON. |
| `HazardIntegrationServiceTests` | 9 | ✅ PASS | Verified Stage 3.1 PostGIS spatial queries, bounding boxes, proximity. |
| `HazardIntegrationControllerTests` | 10 | ✅ PASS | Verified Stage 3.1 REST API endpoints and validation error handling. |
| `HazardDataCleanerTests` | 6 | ✅ PASS | Verified sentinel coordinate cleaning, negative metric flooring, duration derivation. |
| `HazardProcessingServiceTests` | 7 | ✅ PASS | Verified Stage 3.2 processing pipeline, daily aggregation, rolling accumulation. |
| `HazardProcessingControllerTests` | 8 | ✅ PASS | Verified Stage 3.2 REST API endpoints, daily/rolling feeds, GeoJSON outputs. |
| `HazardNormalizationEngineTests` | 9 | ✅ PASS | Verified normalization scaling, boundary clamping, min==max safety, inverse direction. |
| `HazardNormalizationServiceTests` | 6 | ✅ PASS | Verified normalization of live PostGIS data. |
| `HazardNormalizationControllerTests`| 8 | ✅ PASS | Verified Stage 3.3 REST API endpoints, metric filtering, GeoJSON vector layers. |
| `HazardScoringEngineTests` | 6 | ✅ PASS | Verified weighted scoring math, weight sum validation (=1.0000), effective weights on missing metrics, clamping, tier mapping. |
| `HazardScoringServiceTests` | 6 | ✅ PASS | Verified live scoring of real DFO floods, extreme daily rainfall (Patna 101.7mm $\to 0.7388$ `HIGH`), rolling rainfall, summaries. |
| `HazardScoringControllerTests` | 8 | ✅ PASS | Verified Stage 3.4 REST API endpoints, severity filters, type filters, GeoJSON with `hazardScore` and `severityTier`. |
| **Total** | **95** | **✅ PASS** | **100% test success rate across backend, GIS, normalization, and scoring layers.** |

---

## 9. Real-Data Scoring Results

* **Patna 2020-06-29 Extreme Rainfall Storm:**
  * Raw Daily: `101.7 mm` (norm: `0.6780` $\times$ 0.60 = `0.4068`)
  * Raw Peak Hourly: `41.5 mm/hr` (norm: `0.8300` $\times$ 0.40 = `0.3320`)
  * **Rainfall Hazard Score:** **`0.7388`** $\to$ Severity Tier: **`HIGH`**
* **DFO-3 Sitamarhi Historical Flood:**
  * Severity Class `1.5` (norm: `0.5000` $\times$ 0.30 = `0.1500`)
  * Magnitude `6.2088` (norm: `0.2209` $\times$ 0.25 = `0.0552`)
  * Affected Area `21,200 km²` (norm: `0.0424` $\times$ 0.25 = `0.0106`)
  * Duration `4 days` (norm: `0.0337` $\times$ 0.20 = `0.0067`)
  * **Flood Hazard Score:** **`0.2226`** $\to$ Severity Tier: **`LOW`** (Localized short-duration inundation)
* **Score Distribution (Active Sample):**
  * `LOW`: 70 observations (36.6%)
  * `MODERATE`: 59 observations (30.9%)
  * `HIGH`: 7 observations (3.7%)
  * `SEVERE`: 55 observations (28.8%) — extreme hourly thunderstorm burst events.

---

## 10. Exact Boundary Between Stage 3.4 and Stage 3.5

* **Completed in Stage 3.4 (Hazard Scoring):**
  * Weighted single-hazard composite scoring within `FLOOD` and within `EXTREME_RAINFALL`.
  * Dynamic effective weight recalibration for missing metrics.
  * Deterministic mapping to categorical severity tiers (`LOW`, `MODERATE`, `HIGH`, `SEVERE`).
  * Transparent contribution breakdown explaining the exact composition of each score.
  * REST APIs and GeoJSON vector layers under `/api/v1/hazards/scores`.
* **Deferred to Stage 3.5 (Multi-Hazard Integration):**
  * Cross-hazard synthesis combining `FLOOD` + `EXTREME_RAINFALL` scores into a comprehensive multi-hazard index.
  * Joint spatial hazard coincidence analysis across Bihar administrative boundaries.
