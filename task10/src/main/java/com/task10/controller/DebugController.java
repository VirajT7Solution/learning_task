package com.task10.controller;

import com.task10.util.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/test-direct-connection/{databaseName}")
    public String testDirectConnection(@PathVariable String databaseName) {
        String url = "jdbc:postgresql://localhost:5432/" + databaseName;
        String username = "postgres";
        String password = "postgres";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            // Test the exact same query
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM user_data");
            rs.next();
            int count = rs.getInt(1);

            return "✅ Direct connection successful! Database: " + databaseName +
                    ", user_data table has " + count + " records";

        } catch (Exception e) {
            return "❌ Direct connection failed to " + databaseName + ": " + e.getMessage();
        }
    }

    @GetMapping("/test-multi-tenant")
    public String testMultiTenant(@RequestHeader("X-Tenant-ID") String tenantId) {
        StringBuilder result = new StringBuilder();

        try {
            // Test 1: Check current tenant context
            TenantContext.setCurrentTenant(tenantId);
            String currentTenant = TenantContext.getCurrentTenant();
            result.append("1. Tenant Context: ").append(currentTenant).append("\n");

            // Test 2: Try to get connection from datasource
            Connection conn = dataSource.getConnection();
            String catalog = conn.getCatalog();
            result.append("2. Database Catalog: ").append(catalog).append("\n");

            // Test 3: Try to query
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM user_data");
                rs.next();
                int count = rs.getInt(1);
                result.append("3. Query Success! Records in user_data: ").append(count).append("\n");
                stmt.close();
            } catch (Exception e) {
                result.append("3. Query Failed: ").append(e.getMessage()).append("\n");
            }

            conn.close();

        } catch (Exception e) {
            result.append("❌ Overall Error: ").append(e.getMessage());
        } finally {
            TenantContext.clear();
        }

        return result.toString();
    }

    @GetMapping("/tables/{tenantId}")
    public String listTables(@PathVariable String tenantId) {
        StringBuilder result = new StringBuilder();
        result.append("📊 Tables in tenant ").append(tenantId).append(":\n");

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                result.append(" - ").append(tableName).append("\n");
            }
        } catch (Exception e) {
            result.append("❌ Error: ").append(e.getMessage());
        }

        return result.toString();
    }
}