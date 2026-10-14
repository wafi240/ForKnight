package com.forknight.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data transfer object for user registration.
 */
public record RegisterRequest(
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("password") String password,
        @JsonProperty("displayName") String displayName
) {}
