package com.platform.analytics.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateInsightRequest(
    @NotNull UUID datasetId
) {}
