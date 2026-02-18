package com.platform.analytics.repository;

import com.platform.analytics.model.AiInsight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiInsightRepository extends JpaRepository<AiInsight, UUID> {

    Page<AiInsight> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<AiInsight> findByDatasetIdOrderByCreatedAtDesc(UUID datasetId);

    long countByDatasetId(UUID datasetId);
}
