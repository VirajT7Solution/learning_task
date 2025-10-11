package com.task10.service;

import com.task10.entity.UserData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManualTenantService {

    private final Map<String, DataSource> tenantDataSources = new HashMap<>();

    public void registerTenantDataSource(String tenantId, String databaseName) {
        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/" + databaseName);
        ds.setUsername("postgres");
        ds.setPassword("postgres");
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        ds.setConnectionTimeout(30000);
        tenantDataSources.put(tenantId, ds);
        System.out.println("✅ Manual: Registered datasource for tenant " + tenantId + " -> " + databaseName);
    }

    public UserData saveUserData(String tenantId, String data) {
        DataSource tenantDataSource = tenantDataSources.get(tenantId);
        if (tenantDataSource == null) {
            throw new RuntimeException("No datasource registered for tenant: " + tenantId);
        }

        JdbcTemplate jdbcTemplate = new JdbcTemplate(tenantDataSource);

        String sql = "INSERT INTO user_data (data, created_at) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, data);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();

        // Create and return UserData object
        UserData userData = new UserData();
        userData.setId(id);
        userData.setData(data);
        userData.setCreatedAt(LocalDateTime.now());

        return userData;
    }

    public List<UserData> findAllUserData(String tenantId) {
        DataSource tenantDataSource = tenantDataSources.get(tenantId);
        if (tenantDataSource == null) {
            throw new RuntimeException("No datasource registered for tenant: " + tenantId);
        }

        JdbcTemplate jdbcTemplate = new JdbcTemplate(tenantDataSource);

        return jdbcTemplate.query("SELECT id, data, created_at FROM user_data ORDER BY created_at DESC",
                (rs, rowNum) -> {
                    UserData userData = new UserData();
                    userData.setId(rs.getLong("id"));
                    userData.setData(rs.getString("data"));
                    userData.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return userData;
                });
    }

    // Method to check if tenant is registered
    public boolean isTenantRegistered(String tenantId) {
        return tenantDataSources.containsKey(tenantId);
    }

    // Method to list all registered tenants
    public List<String> getRegisteredTenants() {
        return List.copyOf(tenantDataSources.keySet());
    }
}