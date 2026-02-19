package com.platform.analytics.controller;

import com.platform.analytics.exception.UnauthorizedException;
import com.platform.analytics.model.Role;
import com.platform.analytics.security.UserPrincipal;
import com.platform.analytics.service.UsageCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes daily usage counters for the current tenant.
 * Counters are stored in Redis and reset automatically after 7 days.
 */
@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageCounterService usageCounterService;

    /** Returns today's usage counts for the tenant. Requires ADMIN or above. */
    @GetMapping
    public ResponseEntity<Map<String, Long>> getTodayCounts(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal.getRole().ordinal() > Role.ADMIN.ordinal()) {
            throw new UnauthorizedException("Insufficient role for this operation");
        }

        Map<String, Long> counts = usageCounterService.getTodayCounts(principal.getTenantId());
        return ResponseEntity.ok(counts);
    }
}
