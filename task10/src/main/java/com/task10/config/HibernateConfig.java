package com.task10.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class HibernateConfig {

    @Autowired
    private MultiTenantConnectionProvider multiTenantConnectionProvider;

    @Autowired
    private CurrentTenantIdentifierResolver currentTenantIdentifierResolver;

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return (Map<String, Object> hibernateProperties) -> {
            // Use string constants instead of AvailableSettings constants
            hibernateProperties.put("hibernate.multiTenancy", "DATABASE");
            hibernateProperties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
            hibernateProperties.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver);

            // Debug settings
            hibernateProperties.put("hibernate.show_sql", true);
            hibernateProperties.put("hibernate.format_sql", true);
            hibernateProperties.put("hibernate.highlight_sql", true);
        };
    }
}