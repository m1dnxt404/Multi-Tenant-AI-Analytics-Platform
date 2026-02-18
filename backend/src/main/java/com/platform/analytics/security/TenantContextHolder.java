package com.platform.analytics.security;

/**
 * Thread-local holder for the current tenant identifier (org slug).
 * Set by TenantResolutionFilter at the start of every authenticated request
 * and always cleared in a finally block to prevent context leakage.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void setTenantId(String tenantId) {
        CONTEXT.set(tenantId);
    }

    public static String getTenantId() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
