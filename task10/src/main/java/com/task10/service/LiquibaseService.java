package com.task10.service;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;

@Service
public class LiquibaseService {

    public void runLiquibaseForTenant(String databaseName) {
        String url = "jdbc:postgresql://localhost:5432/" + databaseName;
        String username = "postgres";
        String password = "postgres";

        System.out.println("🚀 Running Liquibase for tenant database: " + databaseName);
        System.out.println("🔗 Connection URL: " + url);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("✅ Connected to tenant database: " + databaseName);

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "db/changelog/tenant-master-changelog.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            System.out.println("📋 Executing Liquibase changelog...");
            liquibase.update(new Contexts(), new LabelExpression());
            System.out.println("✅ Liquibase executed successfully for tenant: " + databaseName);

        } catch (Exception e) {
            System.err.println("❌ Failed to run Liquibase for tenant: " + databaseName);
            System.err.println("💥 Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to run Liquibase for tenant: " + databaseName, e);
        }
    }
}