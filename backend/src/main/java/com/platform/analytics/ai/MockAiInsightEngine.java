package com.platform.analytics.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default AI engine for testing and local development.
 * Generates realistic-looking insights based on dataset metadata
 * (column types and names) without making any external API calls.
 *
 * To use a real AI provider, activate a Spring profile:
 *   spring.profiles.active=claude   (requires ANTHROPIC_API_KEY)
 *   spring.profiles.active=openai   (requires OPENAI_API_KEY)
 */
@Slf4j
@Primary
@Service
public class MockAiInsightEngine implements AiInsightEngine {

    private static final String MODEL_ID = "mock-v1";

    @Override
    public AiInsightResult generateInsight(InsightRequest request) {
        log.debug("MockAiInsightEngine generating insight for dataset: {}", request.datasetName());

        List<String> numericColumns = columnsOfType(request.columns(), "INTEGER", "DECIMAL");
        List<String> dateColumns    = columnsOfType(request.columns(), "DATE");
        List<String> textColumns    = columnsOfType(request.columns(), "TEXT");
        List<String> boolColumns    = columnsOfType(request.columns(), "BOOLEAN");

        String title   = buildTitle(request.datasetName(), numericColumns, dateColumns, textColumns);
        String summary = buildSummary(request, numericColumns, dateColumns, textColumns, boolColumns);
        Map<String, Object> details = buildDetails(request, numericColumns, dateColumns, textColumns);

        return new AiInsightResult(title, summary, details, MODEL_ID);
    }

    private String buildTitle(String datasetName, List<String> numeric, List<String> dates, List<String> text) {
        if (!numeric.isEmpty() && !dates.isEmpty()) {
            return "Temporal Trend Analysis: " + datasetName;
        }
        if (numeric.size() >= 2) {
            return "Correlation Opportunity Detected in " + datasetName;
        }
        if (!dates.isEmpty()) {
            return "Time-Series Pattern Found in " + datasetName;
        }
        if (!text.isEmpty()) {
            return "Categorical Distribution Insight: " + datasetName;
        }
        return "Data Quality Assessment: " + datasetName;
    }

    private String buildSummary(InsightRequest req, List<String> numeric, List<String> dates,
                                 List<String> text, List<String> bools) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analysis of '").append(req.datasetName()).append("' (")
          .append(req.rowCount()).append(" rows, ")
          .append(req.columns().size()).append(" columns) reveals");

        if (!numeric.isEmpty() && !dates.isEmpty()) {
            sb.append(" a statistically significant temporal trend in ")
              .append(numeric.get(0))
              .append(" over time. A 12–18% growth rate is projected based on the current trajectory. ")
              .append("Seasonality patterns suggest peak performance in Q2 and Q4.");
        } else if (numeric.size() >= 2) {
            sb.append(" a strong positive correlation (r ≈ 0.78) between ")
              .append(numeric.get(0)).append(" and ").append(numeric.get(1))
              .append(". Anomalies detected in approximately 3.2% of records may indicate ")
              .append("data quality issues or genuine outlier events requiring investigation.");
        } else if (!text.isEmpty()) {
            sb.append(" an uneven distribution across ")
              .append(text.get(0))
              .append(" categories. The top 3 categories account for ~67% of records, ")
              .append("suggesting a long-tail distribution. Consider focusing resources ")
              .append("on underperforming segments.");
        } else {
            sb.append(" that data completeness is at 94.3%. Missing values are concentrated ")
              .append("in optional fields. Core metrics are well-populated and suitable for analysis.");
        }

        if (!bools.isEmpty()) {
            sb.append(" The binary field '").append(bools.get(0))
              .append("' shows a 62%/38% split which may be significant for segmentation.");
        }

        return sb.toString();
    }

    private Map<String, Object> buildDetails(InsightRequest req, List<String> numeric,
                                              List<String> dates, List<String> text) {
        Map<String, Object> details = new HashMap<>();

        List<String> findings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (!numeric.isEmpty()) {
            findings.add("Numeric columns: " + String.join(", ", numeric));
            findings.add("Detected " + Math.max(1, numeric.size() - 1) + " potential correlation(s)");
            recommendations.add("Run regression analysis on " + numeric.get(0));
        }
        if (!dates.isEmpty()) {
            findings.add("Time-series data found in: " + String.join(", ", dates));
            findings.add("Data spans multiple reporting periods");
            recommendations.add("Apply seasonal decomposition for forecasting");
        }
        if (!text.isEmpty()) {
            findings.add("Categorical segmentation available via: " + String.join(", ", text));
            recommendations.add("Consider group-by analysis on categorical columns");
        }
        findings.add("Dataset size: " + req.rowCount() + " records across " + req.columns().size() + " dimensions");
        recommendations.add("Schedule weekly automated refresh for real-time monitoring");

        details.put("keyFindings", findings);
        details.put("recommendations", recommendations);
        details.put("dataQuality", Map.of(
            "completeness", "94.3%",
            "duplicateRate", "0.8%",
            "outlierRate", "3.2%"
        ));
        details.put("columnSummary", req.columns().stream()
            .map(c -> Map.of("name", c.name(), "type", c.dataType()))
            .toList());

        return details;
    }

    private List<String> columnsOfType(List<ColumnInfo> columns, String... types) {
        List<String> typeList = List.of(types);
        return columns.stream()
                .filter(c -> typeList.contains(c.dataType()))
                .map(ColumnInfo::name)
                .toList();
    }
}
