package com.hazard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Startup health check and database connection verifier.
 * Validates connectivity to PostgreSQL 17 and PostGIS 3.6.4 in hazard_db on application boot.
 */
@Component
public class DatabaseConnectionVerifier implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionVerifier.class);
    private final DataSource dataSource;

    public DatabaseConnectionVerifier(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        log.info("================================================================================");
        log.info("STAGE 2.5.1: VERIFYING BACKEND -> POSTGRESQL / POSTGIS CONNECTION");
        log.info("================================================================================");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // 1. Verify PostgreSQL Engine Version
            try (ResultSet rs = statement.executeQuery("SELECT version()")) {
                if (rs.next()) {
                    String pgVersion = rs.getString(1);
                    log.info("✅ PostgreSQL Connection Established: {}", pgVersion);
                }
            }

            // 2. Verify PostGIS Extension
            try (ResultSet rs = statement.executeQuery("SELECT PostGIS_Full_Version()")) {
                if (rs.next()) {
                    String postgisVersion = rs.getString(1);
                    log.info("✅ PostGIS Extension Verified: {}", postgisVersion);
                }
            }

            // 3. Verify Database Schemas & Base Tables
            try (ResultSet rs = statement.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema IN ('hazard', 'weather', 'terrain', 'hydro', 'boundaries', 'population')")) {
                if (rs.next()) {
                    int tableCount = rs.getInt(1);
                    log.info("✅ Accessible Domain Tables: {} base tables ready in hazard_db", tableCount);
                }
            }

            log.info("================================================================================");
            log.info("STAGE 2.5.1: DATABASE CONFIGURATION & BACKEND CONNECTION READY");
            log.info("================================================================================");

        } catch (Exception ex) {
            log.error("❌ Failed to connect to PostgreSQL/PostGIS in hazard_db: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Database connection verification failed during startup", ex);
        }
    }
}
