package com.forknight.common.dto.contrib;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.forknight.common.enums.AccessLevel;
import com.forknight.common.enums.RequestStatus;
import java.time.Instant;

/**
 * Data transfer object representing a contribution access request.
 */
public record ContribRequestDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("repoName") String repoName,
        @JsonProperty("ownerUsername") String ownerUsername,
        @JsonProperty("requesterId") Long requesterId,
        @JsonProperty("requesterUsername") String requesterUsername,
        @JsonProperty("requesterDisplayName") String requesterDisplayName,
        @JsonProperty("status") RequestStatus status,
        @JsonProperty("accessLevel") AccessLevel accessLevel,
        @JsonProperty("message") String message,
        @JsonProperty("responseMsg") String responseMsg,
        @JsonProperty("decidedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant decidedAt,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt
) {}
