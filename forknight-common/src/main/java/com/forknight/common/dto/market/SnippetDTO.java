package com.forknight.common.dto.market;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Data transfer object representing a code snippet for sale in the marketplace.
 */
public record SnippetDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("authorId") Long authorId,
        @JsonProperty("authorUsername") String authorUsername,
        @JsonProperty("authorDisplayName") String authorDisplayName,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("codeContent") String codeContent,
        @JsonProperty("language") String language,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("salesCount") int salesCount,
        @JsonProperty("ratingAvg") double ratingAvg,
        @JsonProperty("ratingCount") int ratingCount,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("isPurchasedByCurrentUser") boolean isPurchasedByCurrentUser,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt
) {}
