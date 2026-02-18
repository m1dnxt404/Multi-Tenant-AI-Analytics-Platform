package com.platform.analytics.ai;

import java.util.Map;

public record AiInsightResult(
    String title,
    String summary,
    Map<String, Object> details,
    String modelUsed
) {}
