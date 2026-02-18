package com.platform.analytics.repository;

import com.platform.analytics.model.DatasetColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DatasetColumnRepository extends JpaRepository<DatasetColumn, UUID> {

    List<DatasetColumn> findByDatasetIdOrderByColumnIndexAsc(UUID datasetId);
}
