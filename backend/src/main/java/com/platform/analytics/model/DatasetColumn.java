package com.platform.analytics.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Metadata about a single CSV column within a tenant-scoped dataset.
 * NO schema= attribute — routed via search_path.
 */
@Entity
@Table(name = "dataset_columns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "column_name", nullable = false, length = 255)
    private String columnName;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @Column(name = "sample_value", columnDefinition = "TEXT")
    private String sampleValue;

    @Column(name = "column_index", nullable = false)
    private int columnIndex;
}
