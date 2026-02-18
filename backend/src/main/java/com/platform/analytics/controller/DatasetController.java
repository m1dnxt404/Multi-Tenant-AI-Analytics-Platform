package com.platform.analytics.controller;

import com.platform.analytics.dto.response.DatasetDetailResponse;
import com.platform.analytics.dto.response.DatasetResponse;
import com.platform.analytics.dto.response.PagedResponse;
import com.platform.analytics.security.UserPrincipal;
import com.platform.analytics.service.AuditService;
import com.platform.analytics.service.DatasetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Datasets")
@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;
    private final AuditService auditService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    public ResponseEntity<DatasetDetailResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            Authentication auth,
            HttpServletRequest request) {

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        DatasetDetailResponse response = datasetService.upload(file, name, principal.getUserId());
        auditService.writeAuditLog(principal.getUserId(), "DATASET_UPLOAD", "dataset",
                response.id(), request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<DatasetResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(datasetService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatasetDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(datasetService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication auth,
            HttpServletRequest request) {

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        datasetService.delete(id);
        auditService.writeAuditLog(principal.getUserId(), "DATASET_DELETE", "dataset",
                id, request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
