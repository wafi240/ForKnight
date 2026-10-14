package com.forknight.common.dto.secret;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for scanning code content or repository for secrets.
 */
public record SecretScanRequest(
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("filePath") String filePath,
        @JsonProperty("content") String content
) {}
