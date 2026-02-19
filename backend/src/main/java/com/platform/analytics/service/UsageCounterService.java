package com.platform.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks daily usage counters per tenant using Redis INCR.
 * Key pattern: usage:{tenantSlug}:{metric}:{yyyyMMdd}
 * Keys expire automatically after 7 days.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsageCounterService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Duration KEY_TTL = Duration.ofDays(7);

    public static final String METRIC_DATASETS_UPLOADED   = "datasets_uploaded";
    public static final String METRIC_INSIGHTS_GENERATED  = "insights_generated";

    private static final List<String> KNOWN_METRICS = List.of(
            METRIC_DATASETS_UPLOADED,
            METRIC_INSIGHTS_GENERATED
    );

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Increments the counter for a metric on today's date.
     * Sets a 7-day TTL when the key is first created.
     */
    public void increment(String tenantSlug, String metric) {
        if (tenantSlug == null || tenantSlug.isBlank()) return;
        String key = buildKey(tenantSlug, metric, LocalDate.now());
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Long newValue = ops.increment(key);
        if (newValue != null && newValue == 1L) {
            redisTemplate.expire(key, KEY_TTL);
        }
    }

    /**
     * Returns the counter value for a specific metric and date.
     */
    public long getCount(String tenantSlug, String metric, LocalDate date) {
        String key = buildKey(tenantSlug, metric, date);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return 0L;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Unexpected value in usage counter key {}: {}", key, value);
            return 0L;
        }
    }

    /**
     * Returns today's counts for all known metrics for the tenant.
     */
    public Map<String, Long> getTodayCounts(String tenantSlug) {
        LocalDate today = LocalDate.now();
        Map<String, Long> result = new LinkedHashMap<>();
        for (String metric : KNOWN_METRICS) {
            result.put(metric, getCount(tenantSlug, metric, today));
        }
        return result;
    }

    private String buildKey(String tenantSlug, String metric, LocalDate date) {
        return "usage:" + tenantSlug + ":" + metric + ":" + date.format(DATE_FMT);
    }
}
