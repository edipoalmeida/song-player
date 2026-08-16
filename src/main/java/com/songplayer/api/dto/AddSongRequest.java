package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Request body for adding one catalog song to a playlist. */
@Schema(description = "Request body for adding a single catalog song to a playlist")
public record AddSongRequest(
        @Schema(description = "ID of the catalog song to add", example = "00000000-0000-0000-0000-000000000001")
        @NotNull UUID songId
) {
}
