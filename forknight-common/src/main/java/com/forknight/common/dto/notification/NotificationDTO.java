package com.forknight.common.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.forknight.common.enums.NotificationType;
import java.time.Instant;

/**
 * Data transfer object representing a user notification.
 */
public record NotificationDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("userId") Long userId,
        @JsonProperty("type") NotificationType type,
        @JsonProperty("title") String title,
        @JsonProperty("message") String message,
        @JsonProperty("referenceType") String referenceType,
        @JsonProperty("referenceId") Long referenceId,
        @JsonProperty("isRead") boolean isRead,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt
) {}
