package com.forknight.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data transfer object for login requests.
 */
public record LoginRequest(
        @JsonProperty("username") String username,
        @JsonProperty("password") String password
) {}
