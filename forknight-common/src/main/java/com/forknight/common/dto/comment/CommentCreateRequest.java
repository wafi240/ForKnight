package com.forknight.common.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for creating a new comment or file suggestion.
 */
public record CommentCreateRequest(
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("parentId") Long parentId,
        @JsonProperty("content") String content,
        @JsonProperty("isSuggestion") boolean isSuggestion,
        @JsonProperty("filePath") String filePath,
        @JsonProperty("lineNumber") Integer lineNumber
) {}
