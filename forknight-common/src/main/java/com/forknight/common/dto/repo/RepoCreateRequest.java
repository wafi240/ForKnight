package com.forknight.common.dto.repo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Data transfer object for creating a new repository.
 */
public record RepoCreateRequest(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("isPublic") boolean isPublic,
        @JsonProperty("licenseType") String licenseType,
        @JsonProperty("githubUrl") String githubUrl,
        @JsonProperty("isForSale") boolean isForSale,
        @JsonProperty("salePrice") BigDecimal salePrice
) {}
