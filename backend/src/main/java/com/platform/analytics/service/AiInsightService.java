package com.platform.analytics.service;

import com.platform.analytics.ai.AiInsightEngine;
import com.platform.analytics.ai.AiInsightResult;
import com.platform.analytics.ai.ColumnInfo;
import com.platform.analytics.ai.InsightRequest;
import com.platform.analytics.dto.response.AiInsightResponse;
import com.platform.analytics.dto.response.PagedResponse;
import com.platform.analytics.exception.ResourceNotFoundException;
import com.platform.analytics.model.AiInsight;
import com.platform.analytics.model.Dataset;
import com.platform.analytics.repository.AiInsightRepository;
import com.platform.analytics.repository.DatasetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInsightService {

    private final AiInsightRepository insightRepository;
    private final DatasetRepository datasetRepository;
    private final AiInsightEngine aiInsightEngine;

    @Transactional
    public AiInsightResponse generate(UUID datasetId, UUID generatedBy) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset", datasetId));

        List<ColumnInfo> columnInfos = dataset.getColumns().stream()
                .map(c -> new ColumnInfo(c.getColumnName(), c.getDataType(), c.getSampleValue()))
                .toList();

        InsightRequest request = new InsightRequest(
                dataset.getName(),
                dataset.getRowCount() != null ? dataset.getRowCount() : 0,
                columnInfos
        );

        AiInsightResult result = aiInsightEngine.generateInsight(request);

        AiInsight insight = AiInsight.builder()
                .datasetId(datasetId)
                .title(result.title())
                .summary(result.summary())
                .details(result.details())
                .modelUsed(result.modelUsed())
                .generatedBy(generatedBy)
                .build();

        AiInsight saved = insightRepository.save(insight);
        log.info("Insight [{}] generated for dataset [{}] using model [{}]",
                saved.getId(), datasetId, result.modelUsed());

        return toResponse(saved, dataset.getName());
    }

    @Transactional(readOnly = true)
    public PagedResponse<AiInsightResponse> findAll(int page, int size) {
        return PagedResponse.from(
            insightRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)),
            insight -> {
                String datasetName = datasetRepository.findById(insight.getDatasetId())
                        .map(Dataset::getName).orElse("Deleted Dataset");
                return toResponse(insight, datasetName);
            }
        );
    }

    @Transactional(readOnly = true)
    public AiInsightResponse findById(UUID id) {
        AiInsight insight = insightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insight", id));
        String datasetName = datasetRepository.findById(insight.getDatasetId())
                .map(Dataset::getName).orElse("Deleted Dataset");
        return toResponse(insight, datasetName);
    }

    private AiInsightResponse toResponse(AiInsight i, String datasetName) {
        return new AiInsightResponse(i.getId(), i.getDatasetId(), datasetName,
                i.getTitle(), i.getSummary(), i.getDetails(), i.getModelUsed(),
                i.getGeneratedBy(), i.getCreatedAt());
    }
}
