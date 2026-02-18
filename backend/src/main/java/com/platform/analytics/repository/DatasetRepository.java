package com.platform.analytics.repository;

import com.platform.analytics.model.Dataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Tenant-scoped repository. Hibernate routes to the correct schema
 * via TenantConnectionProvider. No tenant_id filtering needed.
 */
@Repository
public interface DatasetRepository extends JpaRepository<Dataset, UUID> {

    Page<Dataset> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
