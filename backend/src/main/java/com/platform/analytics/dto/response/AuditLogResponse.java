package com.platform.analytics.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
    UUID id,
    UUID userId,
    String action,
    String resource,
    UUID resourceId,
    Map<String, Object> metadata,
    String ipAddress,
    Instant createdAt
) {}
