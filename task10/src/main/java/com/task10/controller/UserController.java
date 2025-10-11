package com.task10.controller;

import com.task10.entity.UserData;
import com.task10.repository.UserDataRepository;
import com.task10.service.ManualTenantService;
import com.task10.util.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private ManualTenantService manualTenantService;

    @PostMapping
    public ResponseEntity<?> createData(@RequestParam String data,
                                        @RequestHeader("X-Tenant-ID") String tenantId) {
        logger.info("📥 Received request to create data for tenant: {}", tenantId);
        logger.info("📝 Data: {}", data);

        TenantContext.setCurrentTenant(tenantId);
        try {
            // Verify tenant context is set
            String currentTenant = TenantContext.getCurrentTenant();
            logger.info("🔍 Current tenant context: {}", currentTenant);

            UserData userData = new UserData();
            userData.setData(data);
            userData.setCreatedAt(LocalDateTime.now());

            logger.info("💾 Attempting to save data...");
            userData = userDataRepository.save(userData);
            logger.info("✅ Data saved successfully with ID: {}", userData.getId());

            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            logger.error("❌ Error saving data for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage() + ". Check server logs for details.");
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllData(@RequestHeader("X-Tenant-ID") String tenantId) {
        logger.info("📥 Received request to get all data for tenant: {}", tenantId);

        TenantContext.setCurrentTenant(tenantId);
        try {
            String currentTenant = TenantContext.getCurrentTenant();
            logger.info("🔍 Current tenant context: {}", currentTenant);

            var data = userDataRepository.findAll();
            logger.info("✅ Retrieved {} records for tenant {}", data.size(), tenantId);

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("❌ Error retrieving data for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/manual")
    public ResponseEntity<?> createDataManual(@RequestParam String data,
                                              @RequestHeader("X-Tenant-ID") String tenantId) {
        logger.info("📥 Manual: Creating data for tenant: {}", tenantId);
        logger.info("📝 Data: {}", data);

        try {
            UserData userData = manualTenantService.saveUserData(tenantId, data);
            logger.info("✅ Manual: Data saved successfully with ID: {}", userData.getId());
            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            logger.error("❌ Manual: Error saving data for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/manual")
    public ResponseEntity<?> getAllDataManual(@RequestHeader("X-Tenant-ID") String tenantId) {
        logger.info("📥 Manual: Getting all data for tenant: {}", tenantId);

        try {
            List<UserData> data = manualTenantService.findAllUserData(tenantId);
            logger.info("✅ Manual: Retrieved {} records for tenant {}", data.size(), tenantId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("❌ Manual: Error retrieving data for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/manual/tenants")
    public ResponseEntity<?> getRegisteredTenants() {
        try {
            List<String> tenants = manualTenantService.getRegisteredTenants();
            return ResponseEntity.ok(tenants);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }
}