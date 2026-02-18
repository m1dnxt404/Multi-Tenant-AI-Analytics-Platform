package com.platform.analytics.dto.request;

import com.platform.analytics.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteUserRequest(
    @NotBlank @Email String email,
    @NotNull Role role
) {}
