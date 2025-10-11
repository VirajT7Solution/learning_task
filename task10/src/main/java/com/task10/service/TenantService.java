package com.task10.service;

import com.task10.config.MultiTenantConnectionProvider;
import com.task10.entity.Tenant;
import com.task10.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MultiTenantConnectionProvider connectionProvider;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ManualTenantService manualTenantService; // Add this

    @Transactional
    public Tenant createTenant(String tenantName) {
        logger.info("🚀 Starting tenant creation for: {}", tenantName);

        try {
            // Generate database name
            String dbName = "tenant_" + tenantName.toLowerCase()
                    .replace(" ", "_")
                    .replace("-", "_");

            logger.info("📁 Database name: {}", dbName);

            // Step 1: Create database and tables
            logger.info("🔄 Creating database...");
            databaseService.createDatabase(dbName);
            logger.info("✅ Database created successfully");

            // Step 2: Save tenant info
            logger.info("💾 Saving tenant information...");
            Tenant tenant = new Tenant(tenantName, dbName);
            tenant = tenantRepository.save(tenant);
            logger.info("✅ Tenant saved with ID: {}", tenant.getId());

            // Step 3: Register datasource for Hibernate multi-tenancy (keep this for now)
            logger.info("🔗 Registering Hibernate datasource...");
            connectionProvider.addTenantDataSource(tenant.getId().toString(), dbName);
            logger.info("✅ Hibernate datasource registered for tenant: {}", tenant.getId());

            // Step 4: Register datasource for manual switching (NEW)
            logger.info("🔗 Registering manual datasource...");
            manualTenantService.registerTenantDataSource(tenant.getId().toString(), dbName);
            logger.info("✅ Manual datasource registered for tenant: {}", tenant.getId());

            logger.info("🎉 Tenant created successfully: {} (ID: {})", tenantName, tenant.getId());
            return tenant;

        } catch (Exception e) {
            logger.error("💥 Error creating tenant '{}': {}", tenantName, e.getMessage(), e);
            throw new RuntimeException("Failed to create tenant: " + tenantName, e);
        }
    }
}