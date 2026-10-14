package com.forknight.common.dto.contrib;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.forknight.common.enums.AccessLevel;
import java.time.Instant;

/**
 * Data transfer object representing an approved collaborator on a repository.
 */
public record CollaboratorDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("userId") Long userId,
        @JsonProperty("username") String username,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("accessLevel") AccessLevel accessLevel,
        @JsonProperty("grantedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant grantedAt
) {}
