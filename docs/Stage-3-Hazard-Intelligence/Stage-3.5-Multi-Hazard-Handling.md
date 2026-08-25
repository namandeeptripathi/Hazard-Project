# Stage 3.5 — Multi-Hazard Integration Specification

**Project:** Smart Hazard Risk Prediction and Relocation System  
**Event:** Smart India Hackathon (SIH) 2026 | Problem Statement ID: SIH26191  
**Document Status:** Approved & Implemented Sub-Stage 3.5 Specification  
**File Path:** `docs/Stage-3-Hazard-Intelligence/Stage-3.5-Multi-Hazard-Handling.md`  

---

## 1. Executive Summary & Objective

**Stage 3.5 (Multi-Hazard Handling)** synthesizes individual single-hazard scores from Stage 3.4 (`FLOOD` and `EXTREME_RAINFALL`) into a unified, transparent **Multi-Hazard Index** $\in [0.0000, 1.0000]$ based on empirical spatial and temporal coincidence across Bihar.

$$\text{Multi-Hazard Index} = \sum_{i \in \text{available}} \left(\text{hazardScore}_i \times \text{effectiveWeight}_i\right)$$

### Core Question Answered in Stage 3.5:
* **Stage 3.4 asked:** *"What is the intensity of THIS specific hazard?"*
* **Stage 3.5 asks:** *"Are MULTIPLE hazards affecting the same place and time, and what is the combined multi-hazard intensity signal?"*

### Key Presentation & Semantic Guardrails:
* **Not a Predictive Probability:** The Multi-Hazard Index is **not** a "percentage chance of disaster" or "compound probability of independent events". It represents the combined multi-criteria hazard intensity signal under a transparent weighting scheme.
* **Strict Scope Boundaries:** Stage 3.5 explicitly defers population exposure, asset vulnerability, and overall disaster risk calculation to Stage 4.

---

## 2. Spatial & Temporal Coincidence Methodology

```text
       FLOOD SCORE                          EXTREME RAINFALL SCORE
 (e.g. DFO Sitamarhi: 0.2226)             (e.g. Patna Station: 0.7388)
            │                                         │
            └────────────────────┬────────────────────┘
                                 │
                                 ▼
         SPATIAL & TEMPORAL COINCIDENCE MATCHER (PostGIS & Geodesic)
           ├── Spatial: EXACT_POINT (<100m) | PROXIMITY (<=25km) | DISTRICT_CONTAINMENT | DISJOINT
           └── Temporal: EXACT_OVERLAP | SAME_DAY | PROXIMATE_WINDOW (<=3 days) | DISJOINT_TIME
                                 │
                                 ▼
                     MATCH CONFIDENCE CLASSIFICATION
           ├── FULL_MATCH (Both Spatial Coincidence & Temporal Overlap Verified)
           ├── SPATIAL_ONLY (Coincident Location, Disjoint Time Window)
           ├── TEMPORAL_ONLY (Coincident Time Window, Disjoint Locations)
           └── SINGLE_HAZARD_CONTEXT (Standalone Single Hazard Occurrence)
                                 │
                                 ▼
             MULTI-HAZARD AGGREGATION (MultiHazardAggregationEngine)
           ├── Configured Weights: Flood (0.50) + Extreme Rainfall (0.50) = 1.0000
           ├── Dynamic Effective Weight Recalculation (eff_w = w_i / sum(available_w))
           ├── Clamping to [0.0000, 1.0000]
           ├── Dominant Hazard Identification (highest score + secondary hazard)
           └── Severity Tier Mapping: LOW (<0.25), MODERATE (0.25-0.49), HIGH (0.50-0.74), SEVERE (>=0.75)
                                 │
                                 ▼
                 REST APIS & GEOJSON (/api/v1/hazards/multi-hazard)
```

### 2.1 Spatial Relationship Evaluation
* `EXACT_POINT`: Great-circle distance $\le 100 \text{ m}$.
* `PROXIMITY`: Geodesic distance $\le 25,000 \text{ m}$ (25 km buffer via PostGIS `ST_DWithin`).
* `DISTRICT_CONTAINMENT`: Fallback when coordinates are at district centroid/boundary level.
* `DISJOINT`: Non-coincident geographic locations.
* **Unlocated Exclusions:** Unlocated EM-DAT and sentinel-cleaned records are strictly excluded from spatial coincidence matching.

### 2.2 Temporal Relationship Evaluation
* `EXACT_OVERLAP`: Active event date intervals directly intersect ($[\text{start}_1, \text{end}_1] \cap [\text{start}_2, \text{end}_2] \ne \emptyset$).
* `SAME_DAY`: Observations recorded on the exact same calendar day.
* `PROXIMATE_WINDOW`: Events occur within a 3-day temporal buffer ($\le 3 \text{ days}$).
* `DISJOINT_TIME`: Non-overlapping time periods.

---

## 3. Weighting Scheme & Dominant Hazard Analysis

### 3.1 Configured Multi-Hazard Weights (MVP)
* **`FLOOD` Weight ($w_{\text{flood}}$):** **0.50** (50%)
* **`EXTREME_RAINFALL` Weight ($w_{\text{rain}}$):** **0.50** (50%)
* **Total Weight:** Exactly **1.0000** (enforced with fail-fast validation).

### 3.2 Dominance Logic
In addition to the combined index, every multi-hazard observation exposes:
* **Dominant Hazard:** The participating hazard with the highest individual single-hazard score.
* **Dominant Hazard Score:** The numerical score of the primary driver.
* **Secondary Hazard:** The second-highest participating hazard and its score.

---

## 4. Multi-Hazard Data Model Architecture

```text
MultiHazardObservation
  ├── id: "MULTI-DFO-3"
  ├── associatedDistrict: "Sitamarhi"
  ├── isWithinBiharBoundary: true
  ├── longitude: 85.5000
  ├── latitude: 26.5000
  ├── startDate: 2009-08-01
  ├── endDate: 2009-08-04
  ├── spatialRelationship: EXACT_POINT
  ├── temporalRelationship: EXACT_OVERLAP
  ├── confidence: SINGLE_HAZARD_CONTEXT
  ├── multiHazardIndex: 0.2226
  ├── severityTier: LOW
  ├── dominantHazard: FLOOD (score: 0.2226)
  ├── secondaryHazard: null
  ├── completenessRatio: 0.50 (1 of 2 configured hazards)
  ├── explanation: "Multi-Hazard Composite Index calculation: [FLOOD: score=0.2226 (LOW), eff_w=1.00, contrib=0.2226] -> Final Multi-Hazard Index: 0.2226 (LOW, Dominant: FLOOD=0.2226, Completeness: 50%)"
  └── participatingHazards: List<HazardParticipationDto>
        └── FLOOD: score=0.2226, tier=LOW, eff_w=1.00, contrib=0.2226
```

---

## 5. REST API Specifications (`/api/v1/hazards/multi-hazard`)

| Method | Endpoint | Query Parameters | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/hazards/multi-hazard` | `district`, `severity`, `dominantHazard`, `limit` | Lists synthesized multi-hazard observations. |
| `GET` | `/api/v1/hazards/multi-hazard/{id}` | - | Retrieves a single multi-hazard observation by unified ID. |
| `GET` | `/api/v1/hazards/multi-hazard/district/{districtName}` | `severity`, `limit` | Retrieves multi-hazard observations for an administrative district. |
| `GET` | `/api/v1/hazards/multi-hazard/summary` | - | Executive catalog summary of coincidence counts, tier distributions, and active weights. |
| `GET` | `/api/v1/hazards/multi-hazard/geojson` | `district`, `severity`, `limit` | Delivers multi-hazard observations as an RFC 7946 GeoJSON `FeatureCollection` with index and dominance properties. |

---

## 6. Test Suite & Verification Results

All **113 automated test cases** across 17 test suites pass with **0 failures**:

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
| `MultiHazardAggregationEngineTests` | 4 | ✅ PASS | Verified cross-hazard weighted aggregation, weight sum validation, dominant hazard selection, tier mapping. |
| `MultiHazardSpatialTemporalMatcherTests` | 4 | ✅ PASS | Verified geodesic distance matching, district containment fallback, temporal overlap, confidence evaluation. |
| `HazardMultiHazardServiceTests` | 5 | ✅ PASS | Verified live multi-hazard synthesis against PostGIS data, district queries, summaries, GeoJSON. |
| `MultiHazardControllerTests` | 5 | ✅ PASS | Verified Stage 3.5 REST API endpoints. |
| **Total** | **113** | **✅ PASS** | **100% test success rate across backend, GIS, normalization, scoring, and multi-hazard layers.** |

---

## 7. Real-Data Multi-Hazard Synthesis Results

* **Total Synthesized Multi-Hazard Observations:** 198 observations across Bihar.
* **Dominant Hazard Distribution:**
  * `FLOOD` Dominant: 7 historical flood observations in North/Central Bihar (Sitamarhi, Begusarai, Purba Champaran).
  * `EXTREME_RAINFALL` Dominant: 191 extreme storm burst observations across Patna, Muzaffarpur, Bhagalpur.
* **Multi-Hazard Severity Tier Distribution:**
  * `LOW`: 70 observations (35.4%)
  * `MODERATE`: 66 observations (33.3%)
  * `HIGH`: 7 observations (3.5%)
  * `SEVERE`: 55 observations (27.8%) — severe convective thunderstorm bursts.
* **Spatial Relationship Breakdown:** All spatially located events resolved to exact station/footprint coordinates or administrative district boundaries; unlocated EM-DAT records cleanly excluded from spatial coincidence.

---

## 8. Exact Boundary Between Stage 3.5 and Stage 3.6

* **Completed in Stage 3.5 (Multi-Hazard Integration):**
  * Cross-hazard spatial and temporal coincidence evaluation.
  * Multi-hazard weighted composite index calculation ($0.50 \cdot \text{Score}_{\text{flood}} + 0.50 \cdot \text{Score}_{\text{rain}}$).
  * Dominant hazard driver identification.
  * Match confidence classification (`FULL_MATCH`, `SPATIAL_ONLY`, `TEMPORAL_ONLY`, `SINGLE_HAZARD_CONTEXT`).
  * REST APIs and GeoJSON vector layer under `/api/v1/hazards/multi-hazard`.
* **Deferred to Stage 3.6 (Map-Ready Hazard Layer Preparation):**
  * Map-ready styling, color ramp encoding, choropleth layer generation, and raster/vector tiling for UI rendering.
