package com.platform.analytics.dto.response;

import java.util.UUID;

public record DatasetColumnResponse(
    UUID id,
    String columnName,
    String dataType,
    String sampleValue,
    int columnIndex
) {}
