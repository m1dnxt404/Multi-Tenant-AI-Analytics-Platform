package com.platform.analytics.config;

import com.platform.analytics.security.TenantContextHolder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
public class RedisConfig {

    public static final String DATASETS      = "datasets";
    public static final String AI_INSIGHTS   = "ai_insights";
    public static final String ORGANIZATIONS = "organizations";

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        GenericJacksonJsonRedisSerializer jsonSerializer = createJsonSerializer();

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        GenericJacksonJsonRedisSerializer jsonSerializer = createJsonSerializer();

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> perCacheTtl = new HashMap<>();
        perCacheTtl.put(DATASETS,      defaults.entryTtl(Duration.ofMinutes(10)));
        perCacheTtl.put(AI_INSIGHTS,   defaults.entryTtl(Duration.ofMinutes(60)));
        perCacheTtl.put(ORGANIZATIONS, defaults.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(factory))
                .cacheDefaults(defaults.entryTtl(Duration.ofMinutes(5)))
                .withInitialCacheConfigurations(perCacheTtl)
                .build();
    }

    /**
     * Tenant-aware key generator (fallback).
     * Services use explicit SpEL keys; this generator is a safety net for
     * any @Cacheable without an explicit key expression.
     * Key format: {tenantId}:{className}.{methodName}:{params}
     */
    @Bean("tenantAwareKeyGenerator")
    public org.springframework.cache.interceptor.KeyGenerator tenantAwareKeyGenerator() {
        return (target, method, params) -> {
            String tenantId = TenantContextHolder.getTenantId();
            String paramStr = Arrays.stream(params)
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            return (tenantId != null ? tenantId : "global")
                    + ":" + target.getClass().getSimpleName()
                    + "." + method.getName()
                    + ":" + paramStr;
        };
    }

    private GenericJacksonJsonRedisSerializer createJsonSerializer() {
        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build())
                .build();
    }
}
