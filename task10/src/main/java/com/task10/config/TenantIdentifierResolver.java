package com.task10.config;

import com.task10.util.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private static final Logger logger = LoggerFactory.getLogger(TenantIdentifierResolver.class);
    private static final String DEFAULT_TENANT = "main";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getCurrentTenant();
        String resolvedTenant = Objects.requireNonNullElse(tenant, DEFAULT_TENANT);
        logger.debug("🔍 Resolving tenant identifier: {} -> {}", tenant, resolvedTenant);
        return resolvedTenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public boolean isRoot(String tenantIdentifier) {
        boolean isRoot = DEFAULT_TENANT.equals(tenantIdentifier);
        logger.debug("🌳 Is root tenant? {} -> {}", tenantIdentifier, isRoot);
        return isRoot;
    }
}