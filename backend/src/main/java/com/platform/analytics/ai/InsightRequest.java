package com.platform.analytics.ai;

import java.util.List;

public record InsightRequest(
    String datasetName,
    int rowCount,
    List<ColumnInfo> columns
) {}
