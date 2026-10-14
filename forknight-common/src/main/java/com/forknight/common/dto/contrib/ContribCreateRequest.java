package com.forknight.common.dto.contrib;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload to request contribution access to a repository.
 */
public record ContribCreateRequest(
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("message") String message
) {}
