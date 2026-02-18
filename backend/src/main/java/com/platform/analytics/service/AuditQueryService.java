package com.platform.analytics.service;

import com.platform.analytics.dto.response.AuditLogResponse;
import com.platform.analytics.dto.response.PagedResponse;
import com.platform.analytics.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> findAll(int page, int size) {
        return PagedResponse.from(
            auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)),
            log -> new AuditLogResponse(
                log.getId(), log.getUserId(), log.getAction(), log.getResource(),
                log.getResourceId(), log.getMetadata(), log.getIpAddress(), log.getCreatedAt()
            )
        );
    }
}
