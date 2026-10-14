package com.forknight.common.dto.repo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data transfer object representing a Repository.
 */
public record RepoDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("ownerId") Long ownerId,
        @JsonProperty("ownerUsername") String ownerUsername,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("isPublic") boolean isPublic,
        @JsonProperty("licenseType") String licenseType,
        @JsonProperty("defaultBranch") String defaultBranch,
        @JsonProperty("githubUrl") String githubUrl,
        @JsonProperty("starsCount") int starsCount,
        @JsonProperty("forksCount") int forksCount,
        @JsonProperty("isForSale") boolean isForSale,
        @JsonProperty("salePrice") BigDecimal salePrice,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt,
        @JsonProperty("updatedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant updatedAt
) {}
