package com.platform.analytics.dto.response;

import com.platform.analytics.model.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullName,
    Role role,
    Instant joinedAt
) {}
