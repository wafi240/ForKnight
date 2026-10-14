package com.forknight.common.dto.graph;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * Represents a single commit node located in 3D coordinate space.
 */
public record CommitNode3D(
        @JsonProperty("hash") String hash,
        @JsonProperty("shortHash") String shortHash,
        @JsonProperty("message") String message,
        @JsonProperty("authorName") String authorName,
        @JsonProperty("authorEmail") String authorEmail,
        @JsonProperty("branchName") String branchName,
        @JsonProperty("colorHex") String colorHex,
        @JsonProperty("x") double x,
        @JsonProperty("y") double y,
        @JsonProperty("z") double z,
        @JsonProperty("parentHashes") List<String> parentHashes,
        @JsonProperty("timestamp")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant timestamp
) {}
