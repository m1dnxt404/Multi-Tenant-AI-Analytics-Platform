package com.platform.analytics.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AiInsightResponse(
    UUID id,
    UUID datasetId,
    String datasetName,
    String title,
    String summary,
    Map<String, Object> details,
    String modelUsed,
    UUID generatedBy,
    Instant createdAt
) {}
