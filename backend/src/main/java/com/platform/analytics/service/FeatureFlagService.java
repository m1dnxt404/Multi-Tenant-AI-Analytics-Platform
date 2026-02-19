package com.platform.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-tenant feature flags stored in a Redis hash.
 * Key pattern: feature:{tenantSlug}  (one hash per tenant)
 * Field: flag name → "true" | "false"
 *
 * No TTL — flags persist until explicitly deleted.
 */
@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Returns all feature flags for the tenant as a map of flag → enabled.
     */
    public Map<String, Boolean> getAll(String tenantSlug) {
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
        Map<String, Object> raw = ops.entries(hashKey(tenantSlug));
        if (raw.isEmpty()) return Collections.emptyMap();

        Map<String, Boolean> result = new LinkedHashMap<>();
        raw.forEach((flag, value) -> result.put(flag, "true".equalsIgnoreCase(value.toString())));
        return result;
    }

    /**
     * Returns whether a specific flag is enabled. Defaults to false if not set.
     */
    public boolean isEnabled(String tenantSlug, String flag) {
        Object value = redisTemplate.opsForHash().get(hashKey(tenantSlug), flag);
        return value != null && "true".equalsIgnoreCase(value.toString());
    }

    /**
     * Sets a feature flag value for the tenant.
     */
    public void setFlag(String tenantSlug, String flag, boolean enabled) {
        redisTemplate.opsForHash().put(hashKey(tenantSlug), flag, enabled ? "true" : "false");
    }

    /**
     * Removes a feature flag for the tenant.
     */
    public void deleteFlag(String tenantSlug, String flag) {
        redisTemplate.opsForHash().delete(hashKey(tenantSlug), flag);
    }

    private String hashKey(String tenantSlug) {
        return "feature:" + tenantSlug;
    }
}
