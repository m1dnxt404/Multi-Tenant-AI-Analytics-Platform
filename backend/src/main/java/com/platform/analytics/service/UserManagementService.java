package com.platform.analytics.service;

import com.platform.analytics.dto.request.InviteUserRequest;
import com.platform.analytics.dto.request.UpdateUserRoleRequest;
import com.platform.analytics.dto.response.PagedResponse;
import com.platform.analytics.dto.response.UserResponse;
import com.platform.analytics.exception.ConflictException;
import com.platform.analytics.exception.ResourceNotFoundException;
import com.platform.analytics.exception.UnauthorizedException;
import com.platform.analytics.model.*;
import com.platform.analytics.repository.InvitationRepository;
import com.platform.analytics.repository.OrganizationMemberRepository;
import com.platform.analytics.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final InvitationRepository invitationRepository;

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> listMembers(String tenantSlug, int page, int size) {
        Organization org = getOrg(tenantSlug);
        List<OrganizationMember> all = memberRepository.findByOrganizationIdWithUser(org.getId());

        int start = page * size;
        int end = Math.min(start + size, all.size());
        List<UserResponse> content = (start >= all.size()) ? List.of() :
                all.subList(start, end).stream().map(this::toUserResponse).toList();

        Page<UserResponse> paged = new PageImpl<>(content, PageRequest.of(page, size), all.size());
        return new PagedResponse<>(paged.getContent(), page, size,
                paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    @Transactional
    public void inviteUser(String tenantSlug, InviteUserRequest request, UUID invitedByUserId) {
        Organization org = getOrg(tenantSlug);

        if (invitationRepository.existsByOrganizationIdAndInvitedEmailAndAcceptedAtIsNull(
                org.getId(), request.email())) {
            throw new ConflictException("A pending invitation already exists for: " + request.email());
        }

        if (request.role() == Role.OWNER) {
            throw new UnauthorizedException("Cannot invite users with OWNER role");
        }

        Invitation invitation = Invitation.builder()
                .organization(org)
                .invitedEmail(request.email())
                .role(request.role())
                .token(UUID.randomUUID().toString())
                .invitedBy(invitedByUserId)
                .expiresAt(Instant.now().plusSeconds(7 * 24 * 3600))
                .build();

        invitationRepository.save(invitation);
        log.info("Invitation sent to [{}] for org [{}] with role [{}]",
                request.email(), tenantSlug, request.role());
    }

    @Transactional
    public UserResponse updateRole(String tenantSlug, UUID targetUserId, UpdateUserRoleRequest request,
                                   UUID callerUserId, Role callerRole) {
        Organization org = getOrg(tenantSlug);

        // Prevent role escalation
        if (request.role() == Role.OWNER) {
            throw new UnauthorizedException("Cannot assign OWNER role");
        }

        // Callers cannot change their own role
        if (targetUserId.equals(callerUserId)) {
            throw new UnauthorizedException("Cannot change your own role");
        }

        OrganizationMember member = memberRepository
                .findByUserIdAndOrganizationId(targetUserId, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found in this organization"));

        // Admins cannot change other admins
        if (callerRole == Role.ADMIN && member.getRole() == Role.ADMIN) {
            throw new UnauthorizedException("Admins cannot modify other admins");
        }

        member.setRole(request.role());
        memberRepository.save(member);
        log.info("Role of user [{}] changed to [{}] in org [{}]", targetUserId, request.role(), tenantSlug);

        return toUserResponse(member);
    }

    @Transactional
    public void removeUser(String tenantSlug, UUID targetUserId, UUID callerUserId) {
        if (targetUserId.equals(callerUserId)) {
            throw new UnauthorizedException("Cannot remove yourself from the organization");
        }

        Organization org = getOrg(tenantSlug);
        OrganizationMember member = memberRepository
                .findByUserIdAndOrganizationId(targetUserId, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found in this organization"));

        if (member.getRole() == Role.OWNER) {
            throw new UnauthorizedException("Cannot remove the organization owner");
        }

        memberRepository.delete(member);
        log.info("User [{}] removed from org [{}]", targetUserId, tenantSlug);
    }

    private Organization getOrg(String slug) {
        return organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + slug));
    }

    private UserResponse toUserResponse(OrganizationMember member) {
        return new UserResponse(
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getFullName(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
