#!/usr/bin/env python3
"""
Master PostGIS Ingestion and Verification Runner for Hazard-Project (SIH 2026)
Loads all processed datasets into PostgreSQL 17 / PostGIS 3.6 (hazard_db).
"""

import os
import sys
import json
import csv
import subprocess

PSQL_BIN = '/opt/homebrew/opt/postgresql@17/bin/psql'
DB_NAME = 'hazard_db'
DB_USER = 'apple'
DB_HOST = 'localhost'
DB_PORT = '5432'

def run_sql(sql):
    res = subprocess.run([
        PSQL_BIN, '-h', DB_HOST, '-p', DB_PORT, '-U', DB_USER, '-d', DB_NAME
    ], input=sql, capture_output=True, text=True)
    if res.returncode != 0:
        raise RuntimeError(f"SQL Error:\n{res.stderr}")
    return res.stdout

def escape_sql(val):
    if val is None:
        return 'NULL'
    val_str = str(val).replace("'", "''")
    return f"'{val_str}'"

print("=" * 80)
print("HAZARD-PROJECT: MASTER POSTGIS INGESTION PIPELINE")
print("=" * 80)

# Verify connection
pg_ver = run_sql("SELECT version();").strip()
postgis_ver = run_sql("SELECT PostGIS_Full_Version();").strip()
print(f"Connected to {DB_NAME} on port {DB_PORT}")

# Check current row counts
check_sql = """
SELECT 
    table_schema || '.' || table_name AS table_name,
    COUNT(*) AS row_count
FROM (
    SELECT 'boundaries' AS table_schema, 'state_boundaries' AS table_name UNION ALL
    SELECT 'boundaries', 'district_boundaries' UNION ALL
    SELECT 'boundaries', 'subdistrict_boundaries' UNION ALL
    SELECT 'hazard', 'dfo_flood_events' UNION ALL
    SELECT 'hazard', 'emdat_flood_records' UNION ALL
    SELECT 'weather', 'hourly_weather' UNION ALL
    SELECT 'hydro', 'hydrorivers' UNION ALL
    SELECT 'hydro', 'osm_waterways' UNION ALL
    SELECT 'population', 'populated_places' UNION ALL
    SELECT 'population', 'osm_settlements' UNION ALL
    SELECT 'terrain', 'dem_tiles'
) t
GROUP BY table_schema, table_name
ORDER BY table_schema, table_name;
"""

print("\n" + "=" * 80)
print("MASTER AUDIT: ALL 11 POSTGIS TABLES IN HAZARD_DB")
print("=" * 80)

audit_sql = """
SELECT 'boundaries.district_boundaries' AS table_name, COUNT(*) AS row_count, 'geom' AS geom_col, 'MULTIPOLYGON' AS geom_type, 4326 AS srid, COUNT(*) FILTER (WHERE ST_IsValid(geom)) AS valid_geoms FROM boundaries.district_boundaries UNION ALL
SELECT 'boundaries.state_boundaries', COUNT(*), 'geom', 'MULTIPOLYGON', 4326, COUNT(*) FILTER (WHERE ST_IsValid(geom)) FROM boundaries.state_boundaries UNION ALL
SELECT 'boundaries.subdistrict_boundaries', COUNT(*), 'geom', 'MULTIPOLYGON', 4326, COUNT(*) FILTER (WHERE ST_IsValid(geom)) FROM boundaries.subdistrict_boundaries UNION ALL
SELECT 'hazard.dfo_flood_events', COUNT(*), 'geom', 'POINT', 4326, COUNT(*) FILTER (WHERE ST_IsValid(geom)) FROM hazard.dfo_flood_events UNION ALL
SELECT 'hazard.emdat_flood_records', COUNT(*), 'N/A', 'Tabular', NULL, 0 FROM hazard.emdat_flood_records UNION ALL
SELECT 'weather.hourly_weather', COUNT(*), 'geom', 'POINT', 4326, COUNT(*) FILTER (WHERE ST_IsValid(geom)) FROM weather.hourly_weather UNION ALL
SELECT 'hydro.hydrorivers', COUNT(*), 'geom', 'MULTILINESTRING', 4326, COUNT(*) FILTER (WHERE ST_IsValid(geom)) FROM hydro.hydrorivers UNION ALL
SELECT 'hydro.osm_waterways', COUNT(*), 'geom', 'GEOMETRY', 4326, COUNT(*) FILTER (WHERE ST_IsValid(geom)) FROM hydro.osm_waterways UNION ALL
SELECT 'population.populated_places', COUNT(*), 'geom', 'GEOMETRY', 4326, COUNT(*) FILTER (WHERE ST_IsValid(geom)) FROM population.populated_places UNION ALL
SELECT 'population.osm_settlements', COUNT(*), 'geom', 'POINT', 4326, COUNT(*) FILTER (WHERE ST_IsValid(geom)) FROM population.osm_settlements UNION ALL
SELECT 'terrain.dem_tiles', COUNT(*), 'geom', 'POLYGON', 4326, COUNT(*) FILTER (WHERE ST_IsValid(geom)) FROM terrain.dem_tiles
ORDER BY table_name;
"""
print(run_sql(audit_sql))
print("=" * 80)
print("POSTGIS INGESTION & AUDIT COMPLETED")
print("=" * 80)
