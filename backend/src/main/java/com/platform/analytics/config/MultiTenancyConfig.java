package com.platform.analytics.config;

import com.platform.analytics.tenant.TenantConnectionProvider;
import com.platform.analytics.tenant.TenantIdentifierResolver;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the multi-tenancy components with Hibernate.
 * Uses HibernatePropertiesCustomizer to inject the resolver and
 * connection provider into Hibernate's configuration at startup.
 */
@Configuration
@RequiredArgsConstructor
public class MultiTenancyConfig {

    private final TenantIdentifierResolver tenantIdentifierResolver;
    private final TenantConnectionProvider tenantConnectionProvider;

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return properties -> {
            properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
            properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, tenantConnectionProvider);
        };
    }
}
