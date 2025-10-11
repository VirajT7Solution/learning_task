package com.task10.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        logger.debug("Getting current tenant: {}", tenant);
        return tenant;
    }

    public static void setCurrentTenant(String tenant) {
        logger.debug("Setting current tenant to: {}", tenant);
        CURRENT_TENANT.set(tenant);
    }

    public static void clear() {
        String tenant = CURRENT_TENANT.get();
        logger.debug("Clearing tenant context. Previous tenant: {}", tenant);
        CURRENT_TENANT.remove();
    }
}