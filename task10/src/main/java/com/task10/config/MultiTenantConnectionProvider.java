package com.task10.config;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String> {

    private static final Logger logger = LoggerFactory.getLogger(MultiTenantConnectionProvider.class);

    private final DataSource mainDataSource;
    private final Map<String, DataSource> tenantDataSources = new HashMap<>();

    @Autowired
    public MultiTenantConnectionProvider(DataSource mainDataSource) {
        this.mainDataSource = mainDataSource;
    }

    @Override
    protected DataSource selectAnyDataSource() {
        logger.debug("🔧 Selecting default datasource (main)");
        return mainDataSource;
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        logger.debug("🔧 Selecting datasource for tenant: {}", tenantIdentifier);

        if ("main".equals(tenantIdentifier)) {
            return mainDataSource;
        }

        DataSource tenantDataSource = tenantDataSources.get(tenantIdentifier);
        if (tenantDataSource == null) {
            logger.warn("⚠️ No datasource found for tenant: {}. Creating dynamically...", tenantIdentifier);
            tenantDataSource = createDataSourceForTenant(tenantIdentifier);
            tenantDataSources.put(tenantIdentifier, tenantDataSource);
        }

        return tenantDataSource;
    }

    private DataSource createDataSourceForTenant(String tenantIdentifier) {
        logger.info("🔧 Creating datasource for tenant database: {}", tenantIdentifier);

        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/" + tenantIdentifier);
        ds.setUsername("postgres");
        ds.setPassword("postgres");
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        ds.setConnectionTimeout(30000);

        return ds;
    }

    public void addTenantDataSource(String tenantId, String databaseName) {
        logger.info("🔧 Registering datasource for tenant ID: {} -> database: {}", tenantId, databaseName);
        tenantDataSources.put(tenantId, createDataSourceForTenant(databaseName));
    }
}