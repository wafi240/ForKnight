package com.forknight.common.dto.licensing;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload to request copy/fork licensing permission for a repository.
 */
public record CopyLicenseCreateRequest(
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("message") String message
) {}
