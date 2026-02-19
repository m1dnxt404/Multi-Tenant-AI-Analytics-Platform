package com.platform.analytics.config;

import com.platform.analytics.tenant.TenantConnectionProvider;
import com.platform.analytics.tenant.TenantIdentifierResolver;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Registers the multi-tenancy components with Hibernate.
 * Defines the EntityManagerFactory explicitly so that the TenantIdentifierResolver
 * and TenantConnectionProvider beans can be wired into Hibernate's configuration.
 * JpaProperties carries all spring.jpa.properties.* settings from application.yml.
 */
@Configuration
@RequiredArgsConstructor
public class MultiTenancyConfig {

    private final TenantIdentifierResolver tenantIdentifierResolver;
    private final TenantConnectionProvider tenantConnectionProvider;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            JpaProperties jpaProperties) {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(jpaProperties.isShowSql());

        // Start with spring.jpa.properties.* from application.yml
        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
        // DDL is managed by Flyway — Hibernate must not alter the schema
        properties.put(AvailableSettings.HBM2DDL_AUTO, "none");
        // Wire in multi-tenancy beans (cannot be set via YAML — must be object instances)
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, tenantConnectionProvider);

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.platform.analytics.model");
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaPropertyMap(properties);

        return em;
    }
}
