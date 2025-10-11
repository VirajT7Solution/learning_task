package com.task10.service;

import com.task10.config.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;

@Service
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    @Autowired
    private DataSource mainDataSource;

    @Autowired
    private MultiTenantConnectionProvider connectionProvider;

    public void createDatabase(String databaseName) {
        logger.info("🔄 Starting database creation for: {}", databaseName);

        // Use a separate connection to create the database
        String mainUrl = "jdbc:postgresql://localhost:5432/postgres";
        String username = "postgres";
        String password = "postgres";

        try (Connection conn = DriverManager.getConnection(mainUrl, username, password);
             Statement stmt = conn.createStatement()) {

            logger.info("🔍 Checking if database exists: {}", databaseName);
            ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'"
            );

            if (!rs.next()) {
                logger.info("📦 Creating database: {}", databaseName);
                stmt.execute("CREATE DATABASE " + databaseName);
                logger.info("✅ Database created: {}", databaseName);

                // Create tables in the new database
                createTablesInTenantDatabase(databaseName);
            } else {
                logger.info("ℹ️ Database already exists: {}", databaseName);
            }

        } catch (SQLException e) {
            logger.error("❌ Error creating database '{}': {}", databaseName, e.getMessage(), e);
            throw new RuntimeException("Failed to create database: " + databaseName, e);
        }
    }

    private void createTablesInTenantDatabase(String databaseName) {
        String url = "jdbc:postgresql://localhost:5432/" + databaseName;
        String username = "postgres";
        String password = "postgres";

        logger.info("🛠️ Creating tables in: {}", databaseName);

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            // Create user_data table
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS user_data (
                    id BIGSERIAL PRIMARY KEY,
                    data VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;

            stmt.execute(createTableSQL);
            logger.info("✅ user_data table created in: {}", databaseName);

        } catch (SQLException e) {
            logger.error("❌ Error creating tables in '{}': {}", databaseName, e.getMessage(), e);
            throw new RuntimeException("Failed to create tables in database: " + databaseName, e);
        }
    }
}