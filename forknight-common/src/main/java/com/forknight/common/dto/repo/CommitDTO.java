package com.forknight.common.dto.repo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * Data transfer object representing a Git commit.
 */
public record CommitDTO(
        @JsonProperty("hash") String hash,
        @JsonProperty("shortHash") String shortHash,
        @JsonProperty("message") String message,
        @JsonProperty("authorName") String authorName,
        @JsonProperty("authorEmail") String authorEmail,
        @JsonProperty("parentHashes") List<String> parentHashes,
        @JsonProperty("timestamp")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant timestamp
) {}
