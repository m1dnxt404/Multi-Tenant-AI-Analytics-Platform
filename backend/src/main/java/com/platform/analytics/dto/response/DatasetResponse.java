package com.platform.analytics.dto.response;

import com.platform.analytics.model.DatasetStatus;

import java.time.Instant;
import java.util.UUID;

public record DatasetResponse(
    UUID id,
    String name,
    String description,
    String fileName,
    Integer rowCount,
    DatasetStatus status,
    UUID uploadedBy,
    Instant createdAt,
    Instant updatedAt
) {}
