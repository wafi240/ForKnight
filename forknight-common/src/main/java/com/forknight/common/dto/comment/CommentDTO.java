package com.forknight.common.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * Data transfer object representing a public comment or code suggestion on a repo.
 */
public record CommentDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("authorId") Long authorId,
        @JsonProperty("authorUsername") String authorUsername,
        @JsonProperty("authorDisplayName") String authorDisplayName,
        @JsonProperty("parentId") Long parentId,
        @JsonProperty("content") String content,
        @JsonProperty("isSuggestion") boolean isSuggestion,
        @JsonProperty("filePath") String filePath,
        @JsonProperty("lineNumber") Integer lineNumber,
        @JsonProperty("replies") List<CommentDTO> replies,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt
) {}
