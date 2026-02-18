package com.platform.analytics.controller;

import com.platform.analytics.dto.request.InviteUserRequest;
import com.platform.analytics.dto.request.UpdateUserRoleRequest;
import com.platform.analytics.dto.response.PagedResponse;
import com.platform.analytics.dto.response.UserResponse;
import com.platform.analytics.security.UserPrincipal;
import com.platform.analytics.service.AuditService;
import com.platform.analytics.service.UserManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User Management")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;
    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PagedResponse<UserResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        String tenantId = principal(auth).getTenantId();
        return ResponseEntity.ok(userManagementService.listMembers(tenantId, page, size));
    }

    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> invite(
            @Valid @RequestBody InviteUserRequest request,
            Authentication auth,
            HttpServletRequest httpRequest) {

        UserPrincipal p = principal(auth);
        userManagementService.inviteUser(p.getTenantId(), request, p.getUserId());
        auditService.writeAuditLog(p.getUserId(), "USER_INVITE", "invitation", null, httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            Authentication auth,
            HttpServletRequest httpRequest) {

        UserPrincipal p = principal(auth);
        UserResponse updated = userManagementService.updateRole(
                p.getTenantId(), id, request, p.getUserId(), p.getRole());
        auditService.writeAuditLog(p.getUserId(), "USER_ROLE_UPDATE", "user", id, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> remove(
            @PathVariable UUID id,
            Authentication auth,
            HttpServletRequest httpRequest) {

        UserPrincipal p = principal(auth);
        userManagementService.removeUser(p.getTenantId(), id, p.getUserId());
        auditService.writeAuditLog(p.getUserId(), "USER_REMOVE", "user", id, httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    private UserPrincipal principal(Authentication auth) {
        return (UserPrincipal) auth.getPrincipal();
    }
}
