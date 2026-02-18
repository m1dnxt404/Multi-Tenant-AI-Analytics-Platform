package com.platform.analytics.controller;

import com.platform.analytics.dto.request.GenerateInsightRequest;
import com.platform.analytics.dto.response.AiInsightResponse;
import com.platform.analytics.dto.response.PagedResponse;
import com.platform.analytics.security.UserPrincipal;
import com.platform.analytics.service.AiInsightService;
import com.platform.analytics.service.AuditService;
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

@Tag(name = "AI Insights")
@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class AiInsightController {

    private final AiInsightService aiInsightService;
    private final AuditService auditService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    public ResponseEntity<AiInsightResponse> generate(
            @Valid @RequestBody GenerateInsightRequest request,
            Authentication auth,
            HttpServletRequest httpRequest) {

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        AiInsightResponse response = aiInsightService.generate(request.datasetId(), principal.getUserId());
        auditService.writeAuditLog(principal.getUserId(), "INSIGHT_GENERATE", "ai_insight",
                response.id(), httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AiInsightResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(aiInsightService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiInsightResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(aiInsightService.findById(id));
    }
}
