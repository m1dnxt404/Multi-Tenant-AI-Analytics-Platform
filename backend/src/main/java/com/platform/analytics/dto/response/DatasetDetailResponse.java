package com.platform.analytics.dto.response;

import com.platform.analytics.model.DatasetStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DatasetDetailResponse(
    UUID id,
    String name,
    String description,
    String fileName,
    Integer rowCount,
    DatasetStatus status,
    UUID uploadedBy,
    Instant createdAt,
    Instant updatedAt,
    List<DatasetColumnResponse> columns
) {}
