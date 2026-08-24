#!/usr/bin/env python3
"""
Master Geo-Processing Pipeline for Hazard-Project (SIH 2026 - Problem Statement ID: SIH26191)
Processes raw datasets from data/raw/ into standardized, clean GIS layers in data/processed/.
"""

import os
import sys
import json
import csv
import shapefile
import pandas as pd
import numpy as np
import tifffile
from shapely.geometry import shape, mapping, Point, Polygon, MultiPolygon
from shapely.ops import unary_union
import openpyxl

print("=" * 80)
print("HAZARD-PROJECT: MASTER GEO-PROCESSING PIPELINE")
print("=" * 80)

# Ensure processed directories exist
for sub in ['boundaries', 'hazard_events', 'rainfall', 'elevation', 'water', 'population']:
    os.makedirs(os.path.join('data/processed', sub), exist_ok=True)

# ----------------------------------------------------------------------
# 1. PROCESS BOUNDARIES (DS-005)
# ----------------------------------------------------------------------
print("\n[1/6] Processing Administrative Boundaries (DS-005)...")
gadm1_path = 'data/raw/boundaries/gadm41_IND_1_states_json/gadm41_IND_1.json'
with open(gadm1_path, 'r', encoding='utf-8') as f:
    gadm1 = json.load(f)

bihar_state_feat = None
for feat in gadm1['features']:
    if feat['properties'].get('NAME_1') == 'Bihar':
        bihar_state_feat = feat
        break

if bihar_state_feat:
    bihar_state_geom = shape(bihar_state_feat['geometry'])
    with open('data/processed/boundaries/bihar_state_boundary.geojson', 'w', encoding='utf-8') as f:
        json.dump({"type": "FeatureCollection", "features": [bihar_state_feat]}, f)
    print("  -> Saved bihar_state_boundary.geojson (1 state)")

gadm2_path = 'data/raw/boundaries/gadm41_IND_2_districts_json/gadm41_IND_2.json'
with open(gadm2_path, 'r', encoding='utf-8') as f:
    gadm2 = json.load(f)

bihar_districts = [f for f in gadm2['features'] if f['properties'].get('NAME_1') == 'Bihar']
with open('data/processed/boundaries/bihar_districts_boundary.geojson', 'w', encoding='utf-8') as f:
    json.dump({"type": "FeatureCollection", "features": bihar_districts}, f)
print(f"  -> Saved bihar_districts_boundary.geojson ({len(bihar_districts)} districts)")

gadm3_path = 'data/raw/boundaries/gadm41_IND_3_subdistricts_json/gadm41_IND_3.json'
with open(gadm3_path, 'r', encoding='utf-8') as f:
    gadm3 = json.load(f)

bihar_subdistricts = [f for f in gadm3['features'] if f['properties'].get('NAME_1') == 'Bihar']
with open('data/processed/boundaries/bihar_subdistricts_boundary.geojson', 'w', encoding='utf-8') as f:
    json.dump({"type": "FeatureCollection", "features": bihar_subdistricts}, f)
print(f"  -> Saved bihar_subdistricts_boundary.geojson ({len(bihar_subdistricts)} subdistricts)")

# ----------------------------------------------------------------------
# 2. PROCESS HISTORICAL HAZARD EVENTS (DS-001)
# ----------------------------------------------------------------------
print("\n[2/6] Processing Historical Hazard Events (DS-001)...")
dfo_shp_path = 'data/raw/hazard_events/dfo_flood_events/wlf_nhr_fl_dfomasterlist_20190418.shp'
sf = shapefile.Reader(dfo_shp_path)
bihar_dfo_features = []

for sr in sf.shapeRecords():
    geom = sr.shape.__geo_interface__
    pt = Point(geom['coordinates'])
    rec = sr.record.as_dict()
    det_loc = str(rec.get('DETAILED_L') or '')
    
    if bihar_state_geom.intersects(pt) or ('bihar' in det_loc.lower()):
        feat = {
            "type": "Feature",
            "geometry": geom,
            "properties": rec
        }
        bihar_dfo_features.append(feat)

with open('data/processed/hazard_events/bihar_dfo_flood_events_clean.geojson', 'w', encoding='utf-8') as f:
    json.dump({"type": "FeatureCollection", "features": bihar_dfo_features}, f)
print(f"  -> Saved bihar_dfo_flood_events_clean.geojson ({len(bihar_dfo_features)} events)")

# Process EM-DAT
emdat_raw_path = 'data/raw/hazard_events/emdat_country_profiles_2026_08_19.xlsx'
wb = openpyxl.load_workbook(emdat_raw_path, data_only=True)
ws = wb['Historical impact (Natural)']
emdat_rows = []
header = None

for idx, row in enumerate(ws.iter_rows(values_only=True)):
    if idx == 0:
        header = [str(c).strip() if c else f'col_{i}' for i, c in enumerate(row)]
        continue
    if row[0] is not None:
        rec = dict(zip(header, row))
        if str(rec.get('Disaster Type')).lower() == 'flood':
            emdat_rows.append(rec)

emdat_df = pd.DataFrame(emdat_rows)
emdat_df.to_csv('data/processed/hazard_events/india_emdat_flood_records_clean.csv', index=False)
print(f"  -> Saved india_emdat_flood_records_clean.csv ({len(emdat_df)} flood records)")

# ----------------------------------------------------------------------
# 3. PROCESS RAINFALL & WEATHER TIME-SERIES (DS-002)
# ----------------------------------------------------------------------
print("\n[3/6] Processing Weather & Rainfall Time-Series (DS-002)...")
stations = [
    ('patna', 'open_meteo_historical_weather_patna_2020_2024.csv'),
    ('muzaffarpur', 'open_meteo_historical_weather_muzaffarpur_2020_2024.csv'),
    ('bhagalpur', 'open_meteo_historical_weather_bhagalpur_2020_2024.csv')
]

for sname, fname in stations:
    in_path = os.path.join('data/raw/rainfall', fname)
    out_path = os.path.join('data/processed/rainfall', f'{sname}_hourly_clean.csv')
    df = pd.read_csv(in_path, skiprows=3)
    df.columns = [c.strip().lower().replace(' ', '_').replace('(', '').replace(')', '') for c in df.columns]
    df.to_csv(out_path, index=False)
    print(f"  -> Saved {out_path} ({len(df):,} hourly observations)")

# ----------------------------------------------------------------------
# 4. PROCESS DEM / ELEVATION (DS-003)
# ----------------------------------------------------------------------
print("\n[4/6] Processing Elevation / DEM COGs (DS-003)...")
dem_tiles = [
    ('Copernicus_DSM_COG_10_N25_00_E085_00_DEM.tif', 'copernicus_dsm_cog_10_n25_00_e085_00_dem_clean.tif'),
    ('Copernicus_DSM_COG_10_N26_00_E085_00_DEM.tif', 'copernicus_dsm_cog_10_n26_00_e085_00_dem_clean.tif')
]

for raw_name, clean_name in dem_tiles:
    raw_p = os.path.join('data/raw/elevation', raw_name)
    clean_p = os.path.join('data/processed/elevation', clean_name)
    with tifffile.TiffFile(raw_p) as tif:
        page = tif.pages[0]
        data = page.asarray()
        tifffile.imwrite(clean_p, data, photometric='minisblack', compress=6)
    print(f"  -> Saved {clean_p} (3600x3600 float32, NoData -9999)")

# ----------------------------------------------------------------------
# 5. PROCESS RIVERS & WATER BODIES (DS-004)
# ----------------------------------------------------------------------
print("\n[5/6] Processing Rivers & Water Bodies (DS-004)...")
# HydroRIVERS clipping
hr_shp_path = 'data/raw/water/HydroRIVERS_as/HydroRIVERS_v10_as_shp/HydroRIVERS_v10_as.shp'
sf_hr = shapefile.Reader(hr_shp_path)
bihar_hr_features = []

for sr in sf_hr.shapeRecords():
    geom = sr.shape.__geo_interface__
    line = shape(geom)
    if bihar_state_geom.intersects(line):
        bihar_hr_features.append({
            "type": "Feature",
            "geometry": geom,
            "properties": sr.record.as_dict()
        })

with open('data/processed/water/bihar_hydrorivers_clean.geojson', 'w', encoding='utf-8') as f:
    json.dump({"type": "FeatureCollection", "features": bihar_hr_features}, f)
print(f"  -> Saved bihar_hydrorivers_clean.geojson ({len(bihar_hr_features):,} reaches)")

# OSM Waterways
osm_water_path = 'data/raw/water/osm_waterways_waterbodies_pilot_basin.json'
with open(osm_water_path, 'r', encoding='utf-8') as f:
    osm_water = json.load(f)

osm_water_features = []
for el in osm_water.get('elements', []):
    tags = el.get('tags', {})
    el_type = el.get('type')
    geom = None
    if el_type == 'node' and 'lat' in el and 'lon' in el:
        geom = {"type": "Point", "coordinates": [el['lon'], el['lat']]}
    elif el_type == 'way' and 'geometry' in el:
        coords = [[p['lon'], p['lat']] for p in el['geometry'] if 'lon' in p and 'lat' in p]
        if len(coords) >= 2:
            if coords[0] == coords[-1] and len(coords) >= 4:
                geom = {"type": "Polygon", "coordinates": [coords]}
            else:
                geom = {"type": "LineString", "coordinates": coords}
    if geom:
        osm_water_features.append({
            "type": "Feature",
            "geometry": geom,
            "properties": {
                "osm_id": el['id'],
                "osm_type": el_type,
                "name": tags.get('name'),
                "waterway": tags.get('waterway'),
                "water": tags.get('water'),
                "natural": tags.get('natural'),
                "landuse": tags.get('landuse'),
                "intermittent": tags.get('intermittent'),
                "tunnel": tags.get('tunnel')
            }
        })

with open('data/processed/water/bihar_osm_waterways_clean.geojson', 'w', encoding='utf-8') as f:
    json.dump({"type": "FeatureCollection", "features": osm_water_features}, f)
print(f"  -> Saved bihar_osm_waterways_clean.geojson ({len(osm_water_features):,} features)")

# ----------------------------------------------------------------------
# 6. PROCESS POPULATION & SETTLEMENTS (DS-006)
# ----------------------------------------------------------------------
print("\n[6/6] Processing Population & Settlements (DS-006)...")
# HOT OSM Populated Places
hot_path = 'data/raw/population/hotosm_ind_populated_places_geojson/populated_places.geojson'
with open(hot_path, 'r', encoding='utf-8') as f:
    hot_data = json.load(f)

bihar_hot_features = []
for feat in hot_data['features']:
    props = feat['properties']
    if props.get('adm1_name') == 'Bihar':
        bihar_hot_features.append(feat)

with open('data/processed/population/bihar_populated_places_clean.geojson', 'w', encoding='utf-8') as f:
    json.dump({"type": "FeatureCollection", "features": bihar_hot_features}, f)
print(f"  -> Saved bihar_populated_places_clean.geojson ({len(bihar_hot_features):,} features)")

# OSM Settlements
osm_settle_path = 'data/raw/population/osm_settlements_pilot_basin.json'
with open(osm_settle_path, 'r', encoding='utf-8') as f:
    osm_settle = json.load(f)

osm_settle_features = []
for el in osm_settle.get('elements', []):
    tags = el.get('tags', {})
    if 'lat' in el and 'lon' in el:
        osm_settle_features.append({
            "type": "Feature",
            "geometry": {"type": "Point", "coordinates": [el['lon'], el['lat']]},
            "properties": {
                "osm_id": el['id'],
                "osm_type": el.get('type'),
                "name": tags.get('name'),
                "name_hi": tags.get('name:hi'),
                "name_en": tags.get('name:en'),
                "place": tags.get('place'),
                "population": tags.get('population'),
                "postal_code": tags.get('addr:postcode') or tags.get('postal_code'),
                "wikidata": tags.get('wikidata')
            }
        })

with open('data/processed/population/bihar_osm_settlements_clean.geojson', 'w', encoding='utf-8') as f:
    json.dump({"type": "FeatureCollection", "features": osm_settle_features}, f)
print(f"  -> Saved bihar_osm_settlements_clean.geojson ({len(osm_settle_features):,} features)")

print("\n" + "=" * 80)
print("GEO-PROCESSING PIPELINE COMPLETED SUCCESSFULLY")
print("=" * 80)
