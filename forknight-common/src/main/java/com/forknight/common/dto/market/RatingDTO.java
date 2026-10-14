package com.forknight.common.dto.market;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Data transfer object representing a review and star rating for a snippet.
 */
public record RatingDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("snippetId") Long snippetId,
        @JsonProperty("userId") Long userId,
        @JsonProperty("username") String username,
        @JsonProperty("stars") int stars,
        @JsonProperty("review") String review,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt
) {}
