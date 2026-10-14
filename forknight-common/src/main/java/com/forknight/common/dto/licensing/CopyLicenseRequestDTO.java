package com.forknight.common.dto.licensing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.forknight.common.enums.CopyLicenseStatus;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data transfer object representing a copy / licensing request.
 */
public record CopyLicenseRequestDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("repoName") String repoName,
        @JsonProperty("ownerUsername") String ownerUsername,
        @JsonProperty("requesterId") Long requesterId,
        @JsonProperty("requesterUsername") String requesterUsername,
        @JsonProperty("status") CopyLicenseStatus status,
        @JsonProperty("licenseFee") BigDecimal licenseFee,
        @JsonProperty("message") String message,
        @JsonProperty("responseMsg") String responseMsg,
        @JsonProperty("paymentStatus") String paymentStatus,
        @JsonProperty("decidedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant decidedAt,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt
) {}
