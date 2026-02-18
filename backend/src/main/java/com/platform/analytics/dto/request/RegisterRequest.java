package com.platform.analytics.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    @NotBlank @Size(min = 2, max = 255) String fullName,
    @NotBlank @Size(min = 2, max = 255) String organizationName,
    @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,50}$",
        message = "Slug must be 3-50 characters: lowercase letters, numbers, hyphens only")
    String organizationSlug
) {}
