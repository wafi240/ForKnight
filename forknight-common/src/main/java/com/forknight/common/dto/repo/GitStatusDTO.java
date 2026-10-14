package com.forknight.common.dto.repo;

import java.util.List;

public record GitStatusDTO(
        String currentBranch,
        boolean isClean,
        List<String> modifiedFiles,
        List<String> untrackedFiles,
        List<String> stagedFiles,
        List<String> branches
) {}
