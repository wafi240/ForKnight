package com.forknight.common.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response payload returned upon purchasing a snippet.
 */
public record SnippetPurchaseResponse(
        @JsonProperty("orderId") Long orderId,
        @JsonProperty("snippetId") Long snippetId,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("status") String status,
        @JsonProperty("codeContent") String codeContent,
        @JsonProperty("message") String message
) {}
