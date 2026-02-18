package com.platform.analytics.controller;

import com.platform.analytics.dto.request.LoginRequest;
import com.platform.analytics.dto.request.RegisterRequest;
import com.platform.analytics.dto.response.AuthResponse;
import com.platform.analytics.security.JwtUtil;
import com.platform.analytics.security.UserPrincipal;
import com.platform.analytics.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Register a new organisation and owner account")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        AuthResponse auth = authService.register(request);
        setAuthCookies(response, auth, request.organizationSlug());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth);
    }

    @Operation(summary = "Login with email and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse auth = authService.login(request);
        setAuthCookies(response, auth, auth.organization().slug());
        return ResponseEntity.ok(auth);
    }

    @Operation(summary = "Get the currently authenticated user")
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        AuthResponse auth = authService.getMe(principal.getUserId());
        return ResponseEntity.ok(auth);
    }

    @Operation(summary = "Logout — clears auth cookies")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, jwtUtil.clearAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtUtil.clearRefreshTokenCookie().toString());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Refresh the access token using the refresh token cookie")
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            var claims  = jwtUtil.validateAndExtractClaims(refreshToken);
            var userId  = jwtUtil.extractUserId(claims);
            var auth    = authService.getMe(userId);
            String newAccess  = authService.generateAccessToken(
                    auth.userId(), auth.email(), auth.organization().slug(), auth.role());
            String newRefresh = authService.generateRefreshToken(userId);
            response.addHeader(HttpHeaders.SET_COOKIE, jwtUtil.buildAccessTokenCookie(newAccess).toString());
            response.addHeader(HttpHeaders.SET_COOKIE, jwtUtil.buildRefreshTokenCookie(newRefresh).toString());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private void setAuthCookies(HttpServletResponse response, AuthResponse auth, String tenantId) {
        String access  = authService.generateAccessToken(auth.userId(), auth.email(), tenantId, auth.role());
        String refresh = authService.generateRefreshToken(auth.userId());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtUtil.buildAccessTokenCookie(access).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtUtil.buildRefreshTokenCookie(refresh).toString());
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (var cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
