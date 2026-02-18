package com.platform.analytics.service;

import com.platform.analytics.dto.request.LoginRequest;
import com.platform.analytics.dto.request.RegisterRequest;
import com.platform.analytics.dto.response.AuthResponse;
import com.platform.analytics.dto.response.OrganizationResponse;
import com.platform.analytics.exception.ConflictException;
import com.platform.analytics.exception.UnauthorizedException;
import com.platform.analytics.model.*;
import com.platform.analytics.repository.OrganizationMemberRepository;
import com.platform.analytics.repository.OrganizationRepository;
import com.platform.analytics.repository.UserRepository;
import com.platform.analytics.security.JwtUtil;
import com.platform.analytics.tenant.TenantSchemaInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final TenantSchemaInitializer schemaInitializer;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered: " + request.email());
        }
        if (organizationRepository.existsBySlug(request.organizationSlug())) {
            throw new ConflictException("Organization slug is already taken: " + request.organizationSlug());
        }

        // Create organization
        Organization org = organizationRepository.save(Organization.builder()
                .name(request.organizationName())
                .slug(request.organizationSlug())
                .build());

        // Create user
        User user = userRepository.save(User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .isActive(true)
                .build());

        // Assign OWNER role
        memberRepository.save(OrganizationMember.builder()
                .user(user)
                .organization(org)
                .role(Role.OWNER)
                .build());

        // Create tenant schema (outside the JPA transaction to avoid Hibernate routing issues)
        schemaInitializer.initializeSchema(org.getSlug());

        log.info("Registered new org [{}] and owner user [{}]", org.getSlug(), user.getEmail());

        return buildAuthResponse(user, org, Role.OWNER);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        OrganizationMember membership = memberRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("No organization membership found"));

        return buildAuthResponse(user, membership.getOrganization(), membership.getRole());
    }

    @Transactional(readOnly = true)
    public AuthResponse getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        OrganizationMember membership = memberRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("No organization membership found"));

        return buildAuthResponse(user, membership.getOrganization(), membership.getRole());
    }

    private AuthResponse buildAuthResponse(User user, Organization org, Role role) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                role,
                new OrganizationResponse(org.getId(), org.getName(), org.getSlug(), org.getCreatedAt())
        );
    }

    public String generateAccessToken(UUID userId, String email, String tenantId, Role role) {
        return jwtUtil.generateAccessToken(userId, email, tenantId, role);
    }

    public String generateRefreshToken(UUID userId) {
        return jwtUtil.generateRefreshToken(userId);
    }
}
