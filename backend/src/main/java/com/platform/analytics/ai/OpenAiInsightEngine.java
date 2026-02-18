package com.platform.analytics.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Placeholder for OpenAI GPT integration.
 *
 * To activate: spring.profiles.active=openai
 * Required: OPENAI_API_KEY environment variable
 *
 * Implementation steps:
 *   1. Add dependency: com.openai:openai-java (check latest version)
 *   2. Inject OpenAIClient via constructor
 *   3. Build a structured prompt from InsightRequest
 *   4. Call gpt-4o-mini with JSON response format enabled
 *   5. Parse the JSON response into AiInsightResult
 */
@Slf4j
@Service
@Profile("openai")
public class OpenAiInsightEngine implements AiInsightEngine {

    // TODO: inject OpenAIClient when SDK dependency is added
    // private final OpenAIClient openAiClient;

    @Override
    public AiInsightResult generateInsight(InsightRequest request) {
        // TODO: implement
        throw new UnsupportedOperationException(
            "OpenAiInsightEngine is not yet implemented. " +
            "Add the OpenAI SDK dependency and implement this method."
        );
    }
}
