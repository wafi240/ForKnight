package com.forknight.common.dto.licensing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.forknight.common.enums.CopyLicenseStatus;
import java.math.BigDecimal;

/**
 * Payload for repository owner to approve (Free or Paid) or deny a copy/license request.
 */
public record CopyLicenseDecisionRequest(
        @JsonProperty("status") CopyLicenseStatus status,
        @JsonProperty("licenseFee") BigDecimal licenseFee,
        @JsonProperty("responseMsg") String responseMsg
) {}
