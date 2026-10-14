package com.forknight.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Data transfer object for API errors.
 */
public record ApiError(
        @JsonProperty("status") int status,
        @JsonProperty("error") String error,
        @JsonProperty("message") String message,
        @JsonProperty("timestamp") 
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant timestamp
) {
    /**
     * Factory method to create an ApiError with current timestamp.
     *
     * @param status  the HTTP status code
     * @param error   the error title
     * @param message the detailed error message
     * @return a new ApiError instance
     */
    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, Instant.now());
    }
}
