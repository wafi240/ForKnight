package com.forknight.common.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * Payload for listing a new code snippet on the marketplace.
 */
public record SnippetCreateRequest(
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("codeContent") String codeContent,
        @JsonProperty("language") String language,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("tags") List<String> tags
) {}
