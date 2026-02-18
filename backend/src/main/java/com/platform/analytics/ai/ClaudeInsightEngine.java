package com.platform.analytics.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Placeholder for Anthropic Claude integration.
 *
 * To activate: spring.profiles.active=claude
 * Required: ANTHROPIC_API_KEY environment variable
 *
 * Implementation steps:
 *   1. Add dependency: com.anthropic:anthropic-java-sdk (check latest version)
 *   2. Inject the SDK client via constructor
 *   3. Build a structured prompt from InsightRequest
 *   4. Call claude-haiku-4-5 (fast + cost-effective for analytics)
 *   5. Parse the JSON response into AiInsightResult
 *
 * Example prompt template:
 *   "Analyse the following dataset and return a JSON object with keys:
 *    title, summary, keyFindings (array), recommendations (array).
 *    Dataset: {datasetName}, rows: {rowCount}, columns: {columns}"
 */
@Slf4j
@Service
@Profile("claude")
public class ClaudeInsightEngine implements AiInsightEngine {

    // TODO: inject AnthropicClient when SDK dependency is added
    // private final AnthropicClient anthropicClient;

    @Override
    public AiInsightResult generateInsight(InsightRequest request) {
        // TODO: implement
        // String prompt = buildPrompt(request);
        // var response = anthropicClient.messages().create(...);
        // return parseResponse(response);
        throw new UnsupportedOperationException(
            "ClaudeInsightEngine is not yet implemented. " +
            "Add the Anthropic SDK dependency and implement this method."
        );
    }
}
