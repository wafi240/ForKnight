package com.forknight.common.dto.repo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data transfer object containing the raw text content and metadata of a file.
 */
public record FileContentDTO(
        @JsonProperty("filePath") String filePath,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("content") String content,
        @JsonProperty("size") long size,
        @JsonProperty("isBinary") boolean isBinary
) {}
