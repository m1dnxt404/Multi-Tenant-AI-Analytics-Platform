package com.platform.analytics.ai;

/**
 * Contract for AI insight generation.
 * Swap implementations via Spring @Profile without changing service code:
 *   - MockAiInsightEngine  (default, no API keys needed)
 *   - ClaudeInsightEngine  (profile: claude)
 *   - OpenAiInsightEngine  (profile: openai)
 */
public interface AiInsightEngine {

    AiInsightResult generateInsight(InsightRequest request);
}
