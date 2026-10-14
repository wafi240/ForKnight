package com.forknight.common.dto.contrib;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.forknight.common.enums.AccessLevel;
import com.forknight.common.enums.RequestStatus;

/**
 * Payload for repository owner to approve or deny a contribution request.
 */
public record ContribDecisionRequest(
        @JsonProperty("status") RequestStatus status,
        @JsonProperty("accessLevel") AccessLevel accessLevel,
        @JsonProperty("responseMsg") String responseMsg
) {}
