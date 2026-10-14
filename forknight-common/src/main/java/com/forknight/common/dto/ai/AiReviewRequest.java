package com.forknight.common.dto.ai;

public record AiReviewRequest(
        Long pullRequestId,
        String codeOrDiff,
        String language,
        String modelName, // e.g. "gpt-4o", "claude-3-5-sonnet", "gemini-1.5-pro", or "local-heuristic"
        String apiKey
) {
    public AiReviewRequest {
        if (modelName == null || modelName.isBlank()) {
            modelName = "intelligent-heuristic";
        }
    }
}
