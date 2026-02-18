package com.platform.analytics.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Creates a new tenant schema by invoking the create_tenant_schema()
 * stored procedure (defined in V2 Flyway migration).
 * Called atomically within the registration transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public void initializeSchema(String slug) {
        if (!slug.matches("^[a-z0-9-]{3,50}$")) {
            throw new IllegalArgumentException("Invalid organization slug: " + slug);
        }
        log.info("Initialising tenant schema for slug: {}", slug);
        jdbcTemplate.execute("SELECT public.create_tenant_schema('" + slug.replace("'", "") + "')");
        log.info("Tenant schema tenant_{} created successfully", slug);
    }
}
