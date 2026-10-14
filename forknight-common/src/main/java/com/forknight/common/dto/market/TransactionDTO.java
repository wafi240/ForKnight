package com.forknight.common.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("type") String type, // "SPEND" or "EARN"
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("itemType") String itemType,
        @JsonProperty("itemId") Long itemId,
        @JsonProperty("counterparty") String counterparty, // Person paid to or received from
        @JsonProperty("createdAt") Instant createdAt
) {}