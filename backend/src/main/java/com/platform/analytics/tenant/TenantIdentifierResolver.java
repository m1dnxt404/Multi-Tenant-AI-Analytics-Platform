package com.platform.analytics.tenant;

import com.platform.analytics.security.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Tells Hibernate which schema to use for the current request.
 * Reads the tenant slug from TenantContextHolder (set by TenantResolutionFilter).
 * Falls back to "public" for auth operations that only touch global tables.
 */
@Slf4j
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_SCHEMA = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null && !tenantId.isBlank()) {
            return "tenant_" + tenantId;
        }
        return DEFAULT_SCHEMA;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
