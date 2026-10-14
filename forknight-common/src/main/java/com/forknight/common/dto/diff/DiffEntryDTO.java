package com.forknight.common.dto.diff;

import java.util.List;

public record DiffEntryDTO(
        String oldPath,
        String newPath,
        String changeType, // ADD, MODIFY, DELETE, RENAME
        int additions,
        int deletions,
        String rawPatch,
        List<DiffLineDTO> lines
) {}
