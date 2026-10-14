package com.forknight.common.dto.repo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for single-file download bypassing full repo clone.
 */
public record SingleFileDownloadRequest(
        @JsonProperty("owner") String owner,
        @JsonProperty("repo") String repo,
        @JsonProperty("branch") String branch,
        @JsonProperty("filePath") String filePath,
        @JsonProperty("githubToken") String githubToken
) {}
