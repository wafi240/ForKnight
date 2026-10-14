package com.forknight.common.dto.pr;

public record PRCreateRequest(
        Long repoId,
        String title,
        String description,
        String sourceBranch,
        String targetBranch
) {
    public PRCreateRequest {
        if (targetBranch == null || targetBranch.isBlank()) {
            targetBranch = "main";
        }
    }
}
