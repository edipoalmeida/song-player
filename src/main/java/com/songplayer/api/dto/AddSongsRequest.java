package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** Request body for adding multiple catalog songs to a playlist. */
@Schema(description = "Request body for adding multiple catalog songs to a playlist in one request")
public record AddSongsRequest(
        @Schema(description = "List of catalog song IDs to add (must not be empty)")
        @NotEmpty List<@NotNull UUID> songIds
) {
}
