package com.platform.analytics.dto.response;

import com.platform.analytics.model.Role;

import java.util.UUID;

public record AuthResponse(
    UUID userId,
    String email,
    String fullName,
    Role role,
    OrganizationResponse organization
) {}
