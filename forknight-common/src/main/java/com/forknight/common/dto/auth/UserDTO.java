package com.forknight.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Data transfer object representing user information.
 */
public record UserDTO(
        @JsonProperty("id") long id,
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("avatarUrl") String avatarUrl,
        @JsonProperty("bio") String bio,
        @JsonProperty("role") String role,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt
) {}
