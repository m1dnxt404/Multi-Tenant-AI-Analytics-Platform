package com.platform.analytics.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs after JwtAuthFilter. Reads the tenantId from the authenticated
 * UserPrincipal and sets it in TenantContextHolder for Hibernate's
 * TenantIdentifierResolver to pick up.
 *
 * Always clears the tenant context in a finally block to prevent
 * thread-local leakage between requests in the thread pool.
 */
@Slf4j
@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
                String tenantId = principal.getTenantId();
                if (tenantId != null && !tenantId.isBlank()) {
                    TenantContextHolder.setTenantId(tenantId);
                    log.debug("Tenant context set to: {}", tenantId);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
