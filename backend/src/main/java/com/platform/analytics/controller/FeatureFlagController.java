package com.platform.analytics.controller;

import com.platform.analytics.dto.response.FeatureFlagResponse;
import com.platform.analytics.exception.UnauthorizedException;
import com.platform.analytics.model.Role;
import com.platform.analytics.security.UserPrincipal;
import com.platform.analytics.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for per-tenant feature flag management.
 * Flags are stored in Redis and are tenant-scoped.
 */
@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    /** List all feature flags for the current tenant. Requires ADMIN or above. */
    @GetMapping
    public ResponseEntity<List<FeatureFlagResponse>> getAll(
            @AuthenticationPrincipal UserPrincipal principal) {

        requireRole(principal, Role.ADMIN);

        Map<String, Boolean> flags = featureFlagService.getAll(principal.getTenantId());
        List<FeatureFlagResponse> response = flags.entrySet().stream()
                .map(e -> new FeatureFlagResponse(e.getKey(), e.getValue()))
                .toList();
        return ResponseEntity.ok(response);
    }

    /** Enable or disable a feature flag. Requires OWNER. */
    @PutMapping("/{flag}")
    public ResponseEntity<FeatureFlagResponse> setFlag(
            @PathVariable String flag,
            @RequestParam boolean enabled,
            @AuthenticationPrincipal UserPrincipal principal) {

        requireRole(principal, Role.OWNER);

        featureFlagService.setFlag(principal.getTenantId(), flag, enabled);
        return ResponseEntity.ok(new FeatureFlagResponse(flag, enabled));
    }

    /** Delete a feature flag. Requires OWNER. */
    @DeleteMapping("/{flag}")
    public ResponseEntity<Void> deleteFlag(
            @PathVariable String flag,
            @AuthenticationPrincipal UserPrincipal principal) {

        requireRole(principal, Role.OWNER);

        featureFlagService.deleteFlag(principal.getTenantId(), flag);
        return ResponseEntity.noContent().build();
    }

    private void requireRole(UserPrincipal principal, Role minimumRole) {
        if (principal.getRole().ordinal() > minimumRole.ordinal()) {
            throw new UnauthorizedException("Insufficient role for this operation");
        }
    }
}
