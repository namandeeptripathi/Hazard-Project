# Geospatial Data Processing & PostGIS Ingestion Pipeline

**Project:** Hazard-Project — Smart Hazard Risk Prediction and Relocation System  
**Event:** Smart India Hackathon (SIH) 2026 | Problem Statement ID: SIH26191  
**Directory:** `scripts/geospatial/`  

---

## 1. Overview

This directory contains the automated, deterministic geo-processing pipeline that transforms raw heterogeneous hazard, meteorological, terrain, hydrological, administrative, and demographic datasets (`data/raw/`) into clean, standardized GIS formats (`data/processed/`) and loads them into PostgreSQL 17 / PostGIS 3.6 (`hazard_db`).

---

## 2. Pipeline Execution Flow

```
RAW SOURCES (data/raw/)
  ├── boundaries/ (GADM 4.1 India)
  ├── hazard_events/ (DFO Global Shapefile, EM-DAT Excel)
  ├── rainfall/ (Open-Meteo Hourly CSVs)
  ├── elevation/ (Copernicus GLO-30 DSM COGs)
  ├── water/ (HydroRIVERS Asia Shapefile, OSM Overpass Water JSON)
  └── population/ (HOT OSM Populated Places, OSM Settlements JSON)
          │
          ▼
GEO-PROCESSING PIPELINE (scripts/geospatial/process_all.py)
  • WGS 84 (EPSG:4326) CRS standardization
  • Spatial clipping & polygon mask filtering to Bihar state boundary
  • Timestamp temporal normalization (2020-2024 hourly)
  • NoData standard (-9999) & COG compression
  • Attribute normalization & schema mapping
          │
          ▼
PROCESSED DATA (data/processed/)
  ├── boundaries/ (State, 38 Districts, 53 Sub-districts GeoJSON/SHP)
  ├── hazard_events/ (23 Bihar DFO events GeoJSON/SHP, 53 India EM-DAT CSV)
  ├── rainfall/ (Patna, Muzaffarpur, Bhagalpur clean hourly CSVs - 131,544 rows)
  ├── elevation/ (N25_E085, N26_E085 clean COG GeoTIFFs)
  ├── water/ (6,093 HydroRIVERS reaches, 4,401 OSM waterways GeoJSON)
  └── population/ (16,208 populated places, 589 OSM settlements GeoJSON)
          │
          ▼
POSTGIS INGESTION (scripts/geospatial/ingest_all_postgis.py)
  • Bulk loading into hazard_db (6 logical schemas, 11 base tables)
  • PostGIS GIST spatial indexing on all 10 geometry columns
  • BTREE indexing on temporal & categorical filter keys
  • VACUUM ANALYZE optimization
          │
          ▼
BACKEND DATA ACCESS (Java 21 / Spring Boot 3.3 / Hibernate Spatial)
```

---

## 3. How to Run

### Step 1: Run the Geo-Processing Pipeline
```bash
/usr/bin/python3 scripts/geospatial/process_all.py
```

### Step 2: Ingest Processed Datasets into PostGIS
```bash
/usr/bin/python3 scripts/geospatial/ingest_all_postgis.py
```
