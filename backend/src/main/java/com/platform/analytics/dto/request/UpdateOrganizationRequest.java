package com.platform.analytics.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
    @NotBlank @Size(min = 2, max = 255) String name
) {}
