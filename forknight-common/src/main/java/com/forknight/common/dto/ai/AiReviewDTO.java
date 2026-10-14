package com.forknight.common.dto.ai;

import java.time.Instant;
import java.util.List;

public record AiReviewDTO(
        Long id,
        Long pullRequestId,
        String summary,
        String reviewBody,
        int score, // 1 to 10
        List<AiReviewIssueDTO> issues,
        String modelUsed,
        Integer tokensUsed,
        Instant createdAt
) {}
