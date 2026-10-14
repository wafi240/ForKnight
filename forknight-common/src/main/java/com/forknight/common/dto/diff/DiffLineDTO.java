package com.forknight.common.dto.diff;

public record DiffLineDTO(
        LineType type,
        String content,
        Integer oldLineNo,
        Integer newLineNo
) {
    public enum LineType {
        CONTEXT,
        ADD,
        DELETE,
        HEADER
    }
}
