package com.forknight.common.dto.pr;

import java.time.Instant;

public record PullRequestDTO(
        Long id,
        Long repoId,
        String repoName,
        Long authorId,
        String authorUsername,
        String title,
        String description,
        String sourceBranch,
        String targetBranch,
        PRStatus status,
        int filesChanged,
        int additions,
        int deletions,
        Instant createdAt,
        Instant updatedAt
) {}
