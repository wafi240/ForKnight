package com.forknight.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data transfer object for a successful login/auth response.
 */
public record LoginResponse(
        @JsonProperty("userId") long userId,
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("avatarUrl") String avatarUrl,
        @JsonProperty("role") String role,
        @JsonProperty("accessToken") String accessToken,
        @JsonProperty("refreshToken") String refreshToken
) {}
