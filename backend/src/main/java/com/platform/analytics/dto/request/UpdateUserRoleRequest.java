package com.platform.analytics.dto.request;

import com.platform.analytics.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
    @NotNull Role role
) {}
