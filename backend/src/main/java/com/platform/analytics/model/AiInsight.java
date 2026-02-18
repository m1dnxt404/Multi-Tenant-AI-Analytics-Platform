package com.platform.analytics.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * AI-generated insight for a tenant-scoped dataset.
 * NO schema= attribute — routed via search_path.
 * Details stored as JSONB for flexible structured output.
 */
@Entity
@Table(name = "ai_insights")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dataset_id", nullable = false)
    private UUID datasetId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(name = "model_used", nullable = false, length = 100)
    @Builder.Default
    private String modelUsed = "mock-v1";

    @Column(name = "generated_by", nullable = false)
    private UUID generatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
