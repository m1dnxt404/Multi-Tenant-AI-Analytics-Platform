package com.platform.analytics.controller;

import com.platform.analytics.dto.request.UpdateOrganizationRequest;
import com.platform.analytics.dto.response.OrganizationResponse;
import com.platform.analytics.security.UserPrincipal;
import com.platform.analytics.service.OrganizationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Organization")
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/current")
    public ResponseEntity<OrganizationResponse> getCurrent(Authentication auth) {
        String tenantId = principal(auth).getTenantId();
        return ResponseEntity.ok(organizationService.getBySlug(tenantId));
    }

    @PutMapping("/current")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<OrganizationResponse> update(
            @Valid @RequestBody UpdateOrganizationRequest request,
            Authentication auth) {
        String tenantId = principal(auth).getTenantId();
        return ResponseEntity.ok(organizationService.updateName(tenantId, request));
    }

    private UserPrincipal principal(Authentication auth) {
        return (UserPrincipal) auth.getPrincipal();
    }
}
