package com.platform.analytics.service;

import com.platform.analytics.model.AuditLog;
import com.platform.analytics.repository.AuditLogRepository;
import com.platform.analytics.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Writes an audit log entry in the current tenant's audit_logs table.
     * Uses REQUIRES_NEW so audit entries are persisted even if the caller's
     * transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAuditLog(UUID userId, String action, String resource,
                              UUID resourceId, String ipAddress) {
        writeAuditLog(userId, action, resource, resourceId, ipAddress, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAuditLog(UUID userId, String action, String resource,
                              UUID resourceId, String ipAddress, Map<String, Object> metadata) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .resource(resource)
                    .resourceId(resourceId)
                    .ipAddress(ipAddress)
                    .metadata(metadata)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log [action={}, userId={}]: {}", action, userId, e.getMessage());
        }
    }

    /**
     * Writes an audit log entry when tenant context must be set manually
     * (e.g., during registration or login, before TenantResolutionFilter runs).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAuditLogWithTenant(String tenantId, UUID userId, String action,
                                        String resource, UUID resourceId, String ipAddress) {
        String previous = TenantContextHolder.getTenantId();
        try {
            TenantContextHolder.setTenantId(tenantId);
            writeAuditLog(userId, action, resource, resourceId, ipAddress);
        } finally {
            if (previous != null) {
                TenantContextHolder.setTenantId(previous);
            } else {
                TenantContextHolder.clear();
            }
        }
    }
}
