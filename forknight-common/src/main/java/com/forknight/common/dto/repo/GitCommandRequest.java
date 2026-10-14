package com.forknight.common.dto.repo;

public record GitCommandRequest(
        String command, // "COMMIT", "BRANCH", "CHECKOUT", "STASH", "STATUS"
        String branchName,
        String commitMessage
) {}
