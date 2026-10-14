package com.forknight.common.dto.secret;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.forknight.common.enums.SecretSeverity;
import com.forknight.common.enums.SecretType;
import java.time.Instant;

/**
 * Data transfer object representing a secret detected in code.
 */
public record SecretScanResultDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("filePath") String filePath,
        @JsonProperty("lineNumber") int lineNumber,
        @JsonProperty("secretType") SecretType secretType,
        @JsonProperty("ruleName") String ruleName,
        @JsonProperty("maskedSnippet") String maskedSnippet,
        @JsonProperty("severity") SecretSeverity severity,
        @JsonProperty("isResolved") boolean isResolved,
        @JsonProperty("detectedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant detectedAt
) {}
