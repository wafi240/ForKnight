package com.forknight.common.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for submitting a star rating and review.
 */
public record RatingCreateRequest(
        @JsonProperty("stars") int stars,
        @JsonProperty("review") String review
) {}
