package com.songplayer.api.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** Standard error body for all HTTP endpoints. */
@Schema(description = "Standard error response returned by all endpoints on failure")
public record ApiError(
        @Schema(description = "Timestamp when the error occurred") Instant timestamp,
        @Schema(description = "HTTP status code", example = "404") int status,
        @Schema(description = "Machine-readable error code", example = "RESOURCE_NOT_FOUND") String code,
        @Schema(description = "Human-readable error message", example = "Playlist not found") String message,
        @Schema(description = "Field-level validation violations (populated on 400 responses)") List<FieldViolation> violations
) {
    @Schema(description = "A single field validation violation")
    public record FieldViolation(
            @Schema(description = "Field that failed validation", example = "name") String field,
            @Schema(description = "Violation message", example = "must not be blank") String message
    ) {
    }
}
