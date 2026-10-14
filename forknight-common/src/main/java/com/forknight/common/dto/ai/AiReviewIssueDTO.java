package com.forknight.common.dto.ai;

public record AiReviewIssueDTO(
        String type,        // SECURITY, BUG, PERFORMANCE, STYLE
        String severity,    // CRITICAL, HIGH, MEDIUM, LOW, INFO
        String file,
        Integer line,
        String message,
        String suggestion
) {}
